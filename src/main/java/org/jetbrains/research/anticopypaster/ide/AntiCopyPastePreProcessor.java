package org.jetbrains.research.anticopypaster.ide;

import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.anticopypaster.AntiCopyPasterBundle;
import org.jetbrains.research.anticopypaster.checkers.FragmentCorrectnessChecker;
import org.jetbrains.research.anticopypaster.metrics.extractors.CouplingCalculator;
import org.jetbrains.research.anticopypaster.metrics.extractors.HistoricalFeaturesExtractor;
import org.jetbrains.research.anticopypaster.metrics.extractors.KeywordMetricsExtractor;
import org.jetbrains.research.anticopypaster.metrics.extractors.MethodDeclarationMetricsExtractor;
import org.jetbrains.research.anticopypaster.models.VectorValidator;
import org.jetbrains.research.anticopypaster.models.features.feature.Feature;
import org.jetbrains.research.anticopypaster.models.features.feature.FeatureItem;
import org.jetbrains.research.anticopypaster.models.features.features_vector.FeaturesVector;
import org.jetbrains.research.anticopypaster.models.features.features_vector.IFeaturesVector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.regex.Pattern;

import static org.jetbrains.research.anticopypaster.utils.PsiUtil.*;

/**
 * Handles any copy-paste action and checks if the pasted code fragment could be extracted into a separate method.
 */
public class AntiCopyPastePreProcessor implements CopyPastePreProcessor {
    private PsiFile sourceFile;
    private PsiMethod sourceMethod;
    private PsiMethod destinationMethod;
    private final DuplicatesInspection inspection = new DuplicatesInspection();
    private final RefactoringNotificationTask refactoringNotificationTask = new RefactoringNotificationTask();

    private static final Logger LOG = Logger.getInstance(AntiCopyPastePreProcessor.class);

    public AntiCopyPastePreProcessor() {
        setCheckingForRefactoringOpportunities();
    }

    /**
     * Triggers on each copy action to catch the information about a file and a method
     * the code fragment was copied from.
     */
    @Nullable
    @Override
    public String preprocessOnCopy(PsiFile file, int[] startOffsets, int[] endOffsets, String text) {
        sourceFile = file;
        sourceMethod = findMethodByOffset(file, startOffsets[0]);
        return null;
    }

    /**
     * Triggers on each paste action to search for duplicates and check the Extract Method refactoring opportunities
     * for a copied-pasted code fragment.
     */
    @NotNull
    @Override
    public String preprocessOnPaste(Project project, PsiFile file, Editor editor, String text, RawText rawText) {
        HashSet<String> variablesInCodeFragment = new HashSet<>();
        HashMap<String, Integer> variablesCountsInCodeFragment = new HashMap<>();

        if (project == null || editor == null || file == null ||
            !FragmentCorrectnessChecker.isCorrect(project, file,
                                                  text,
                                                  variablesInCodeFragment,
                                                  variablesCountsInCodeFragment)) {
            return text;
        }

        @Nullable Caret caret = CommonDataKeys.CARET.getData(DataManager.getInstance().getDataContext());
        int offset = caret == null ? 0 : caret.getOffset();
        destinationMethod = findMethodByOffset(file, offset);

        // find number of code fragments considered as duplicated
        DuplicatesInspection.InspectionResult result = inspection.resolve(file, text);

        if (result.getDuplicatesCount() == 0) {
            return text;
        }

        //number of lines in fragment
        int linesOfCode = StringUtils.countMatches(text, "\n") + 1;
        MethodDeclarationMetricsExtractor.ParamsScores scores = new MethodDeclarationMetricsExtractor.ParamsScores();
        IFeaturesVector featuresVector;

        if (result.getDuplicatesCount() != 0) {
            featuresVector =
                calculateFeatures(sourceFile, text, variablesInCodeFragment, variablesCountsInCodeFragment, scores,
                                  linesOfCode);
        } else {
            return text;
        }

        if (!VectorValidator.isValid(featuresVector)) {
            return text;
        }

        linesOfCode = getCountOfCodeLines(text, linesOfCode);

        if (scores.out > 1 || scores.in > 3 || linesOfCode <= 0) {
            return text;
        }

        if (featuresVector.getFeature(Feature.KeywordBreakTotalCount) >= 3.0) {
            return text;
        }

        boolean forceExtraction = false;
        String reasonToExtractMethod = "";

        if (linesOfCode >= 4 && scores.out == 1 && scores.in >= 1) {
            forceExtraction = true;
            reasonToExtractMethod = AntiCopyPasterBundle.message("code.fragment.simplifies.logic.of.enclosing.method");
        }

        if (linesOfCode == 1) {
            if ((featuresVector.getFeature(Feature.KeywordNewTotalCount) > 0.0 ||
                text.contains(".")) && StringUtils.countMatches(text, ",") > 1 && scores.in <= 1) {
                reasonToExtractMethod =
                    AntiCopyPasterBundle.message(
                        "code.fragment.could.remove.duplicated.constructor.call.or.factory.method");
                forceExtraction = true;
            } else {
                return text;
            }
        }

        double scoreOverall = getScoreOverall(text, linesOfCode, scores, featuresVector);

        if (scoreOverall >= 4.99) {
            reasonToExtractMethod =
                AntiCopyPasterBundle.message("code.fragment.strongly.simplifies.logic.of.enclosing.method");
            forceExtraction = true;
        }

        if ((scoreOverall >= 4.5 && result.getDuplicatesCount() >= 4) && (result.getDuplicatesCount() >= 5 && scoreOverall >= 3.0)) {
            reasonToExtractMethod = AntiCopyPasterBundle.message("code.fragment.simplifies.and.removes.duplicates",
                                                                 String.valueOf(result.getDuplicatesCount()));
            forceExtraction = true;
        }

        int muchMatches = Math.max(0, result.getDuplicatesCount() - 2);
        double predBoost = Math.min(1, 0.33 * muchMatches);

        refactoringNotificationTask.addEvent(
            new RefactoringEvent(file, text, result.getDuplicatesCount(), featuresVector, project, editor,
                                 predBoost, linesOfCode, forceExtraction, reasonToExtractMethod));

        return text;
    }

    /**
     * Sets the regular checking for Extract Method refactoring opportunities.
     */
    private void setCheckingForRefactoringOpportunities() {
        try {
            Timer timer = new Timer();
            timer.schedule(refactoringNotificationTask, 15000, 15000);
        } catch (Exception ex) {
            LOG.error("[ACP] Failed to schedule the checking for refactorings.", ex.getMessage());
        }
    }

    private double getScoreOverall(String text,
                                   int linesOfCode,
                                   MethodDeclarationMetricsExtractor.ParamsScores scores,
                                   IFeaturesVector featuresVector) {
        double sizeScore = Math.min(3.0, 0.3 * Math.min(scores.methodLines - linesOfCode, linesOfCode));
        double paramsScore = 2.0 - scores.out + Math.min(0, 2 - scores.in);

        double totalDepFragment = featuresVector.getFeature(Feature.TotalLinesDepth);
        double totalDepMethod = scores.allDep;
        double maxDepFragment = MethodDeclarationMetricsExtractor.maxDepth(text);
        double maxDepMethod = scores.maxDep;

        double scoreArea =
            2.0 * maxDepMethod / Math.max(1.0, totalDepMethod) * Math.min(totalDepMethod - totalDepFragment,
                                                                          totalDepMethod - scores.allRest);
        double scoreMaxDep = Math.min(maxDepMethod - maxDepFragment, maxDepMethod - scores.maxRest);

        return sizeScore + paramsScore + scoreArea + scoreMaxDep;
    }

    /**
     * Calculates the metrics for the pasted code fragment and a method where the code fragment was pasted into.
     */
    private IFeaturesVector calculateFeatures(PsiFile file,
                                              String text,
                                              HashSet<String> varsInFragment,
                                              HashMap<String, Integer> varsCountsInFragment,
                                              MethodDeclarationMetricsExtractor.ParamsScores paramsScores,
                                              int linesCount) {
        IFeaturesVector featuresVector = new FeaturesVector(117);

        KeywordMetricsExtractor.calculate(text, linesCount, featuresVector);
        CouplingCalculator.calculate(file, text, linesCount, featuresVector);
        featuresVector.addFeature(new FeatureItem(Feature.TotalSymbolsInCodeFragment, text.length()));
        featuresVector.addFeature(
            new FeatureItem(Feature.AverageSymbolsInCodeLine, (double) text.length() / linesCount));

        int depthTotal = MethodDeclarationMetricsExtractor.totalDepth(text);

        featuresVector.addFeature(new FeatureItem(Feature.TotalLinesDepth, depthTotal));
        featuresVector.addFeature(new FeatureItem(Feature.AverageLinesDepth, (double) depthTotal / linesCount));

        MethodDeclarationMetricsExtractor.ParamsScores scores =
            MethodDeclarationMetricsExtractor.calculate(file, text, featuresVector, varsInFragment,
                                                        varsCountsInFragment);
        paramsScores.in = scores.in;
        paramsScores.out = scores.out;
        paramsScores.maxRest = scores.maxRest;
        paramsScores.maxDep = scores.maxDep;
        paramsScores.allRest = scores.allRest;
        paramsScores.allDep = scores.allDep;
        paramsScores.methodLines = scores.methodLines;
        paramsScores.isSet = scores.isSet;

        try {
            final String virtualFilePath = file.getVirtualFile().getCanonicalPath();
            PsiMethod psiMethodBeforeRevision = getMethodStartLineInBeforeRevision(file, destinationMethod);
            if (virtualFilePath != null && psiMethodBeforeRevision != null)
                HistoricalFeaturesExtractor.calculateHistoricalFeatures(file.getProject().getBasePath(),
                                                                        getNumberOfLine(file,
                                                                                        psiMethodBeforeRevision.getTextRange().getStartOffset()),
                                                                        getNumberOfLine(file,
                                                                                        psiMethodBeforeRevision.getTextRange().getEndOffset()),
                                                                        virtualFilePath,
                                                                        featuresVector);
        } catch (GitAPIException e) {
            LOG.error("[ACP] Failed to calculate historical features.");
        }
        return featuresVector;
    }

    private int getCountOfCodeLines(String text, int rawLocs) {
        Pattern p = Pattern.compile("/\\*[\\s\\S]*?\\*/");
        java.util.regex.Matcher m = p.matcher(text);
        int totalCountWithComment = 0;
        while (m.find()) {
            String[] lines = m.group(0).split("\n");
            totalCountWithComment += lines.length;
        }

        int totalUnused = 0;
        for (String s : text.split("\n")) {
            String tmp = s.trim();
            if (tmp.isEmpty() || tmp.startsWith("//")) {
                totalUnused++;
            }
        }

        return Math.max(0, rawLocs - totalUnused - totalCountWithComment);
    }
}