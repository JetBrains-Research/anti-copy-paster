package ide;

import builders.DecisionPathBuilder;
import checkers.FragmentCorrectnessChecker;
import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageRefactoringSupport;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringActionHandler;
import metrics.extractors.ConnectivityExtractor;
import metrics.extractors.KeywordMetricsExtractor;
import metrics.extractors.MethodDeclarationMetricsExtractor;
import models.IPredictionModel;
import models.VectorValidator;
import models.features.feature.Feature;
import models.features.feature.FeatureItem;
import models.features.features_vector.FeaturesVector;
import models.features.features_vector.IFeaturesVector;
import models.offline.WekaBasedModel;
import ide.notifications.ExtractMethodNotifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.DuplicatesInspection;
import weka.classifiers.trees.RandomTree;
import weka.core.SerializationHelper;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

/**
 * Handles any copy-paste action and check if the pasted code fragment could be extracted into a separate method.
 */
public class ExtractMethodPreProcessor implements CopyPastePreProcessor {
    private static IPredictionModel model;
    private static RandomTree tree;
    private static String treeString;
    private static DuplicatesInspection inspection = new DuplicatesInspection();
    private static ConcurrentLinkedQueue<Event> events_queue = new ConcurrentLinkedQueue();
    private static class Event {
        PsiFile file;
        String text;
        int matches;
        IFeaturesVector vec;
        Project project;
        Editor editor;
        String content;
        double pred_boost;
        int linesOfCode;
        boolean forceExtraction;
        String textProof;

        public Event(PsiFile file, String text, int matches, IFeaturesVector vec, Project project, Editor editor, String content, double pred_boost, int linesOfCode, boolean forceExtraction, String textProof) {
            this.file = file;
            this.text = text;
            this.matches = matches;
            this.vec = vec;
            this.project = project;
            this.editor = editor;
            this.content = content;
            this.pred_boost = pred_boost;
            this.linesOfCode = linesOfCode;
            this.forceExtraction = forceExtraction;
            this.textProof = textProof;
        }
    }

    static {
        try {
            model = new WekaBasedModel();
            tree = (RandomTree) SerializationHelper.read(ExtractMethodPreProcessor.class.getClassLoader().getResourceAsStream("RTree-ACP-SH.model"));
            treeString = tree.toString();
            String[] split = treeString.split("\n");
            StringBuilder resBuilder = new StringBuilder();
            foo(split, resBuilder);
            foo(split, resBuilder);
            treeString = resBuilder.toString();
            treeString = treeString.substring(0, treeString.length() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Timer timer = new Timer();
            TimerTask delayedRefactoringsTask = new TimerTask() {
                @Override
                //analyze queue and suggest refactoring
                public void run() {

                    while (!events_queue.isEmpty()) {
                        final Event event = events_queue.poll();

                        ApplicationManager.getApplication().runReadAction(() -> {
                            DuplicatesInspection.InspectionResult result = inspection.resolve(event.project, event.text.replace('\n', ' ').replace('\t', ' ').replace('\r', ' ').replaceAll("\\s+",""));
                            int matchesAfterEvent = event.matches + 1;
                            if (result.count <= 1 && result.count < matchesAfterEvent) {
                                return;
                            }

                            try {
                                List<Integer> prediction = model.predict(Collections.singletonList(event.vec));
                                int pred = prediction.get(0);

                                if (event.forceExtraction || (pred == 1 && event.linesOfCode > 3) || (event.linesOfCode <= 3 && new Random().nextDouble() < event.pred_boost)) {
                                    new ExtractMethodNotifier().notify(event.project,
                                                                       AntiCopyPasterBundle.message("extract.method.refactoring.is.available"),
                                                                       new Runnable() {
                                        @Override
                                        public void run() {
                                            String message = event.textProof;
                                            if (message.isEmpty()) {
                                                message = buildMessage(event.vec);
                                            }
                                            int result = Messages.showOkCancelDialog(message,
                                                                                     AntiCopyPasterBundle.message("anticopypaster.recommendation.dialog.name"),
                                                                                     Messages.getWarningIcon());

                                            if (result == 0) {
                                                scheduleExtraction(event.project, event.file, event.editor, event.text);
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            };

            timer.schedule(delayedRefactoringsTask, 15000, 15000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void foo(String[] split, StringBuilder resBuilder) {
        for (int i = 4; i < split.length - 2; ++i) {
            resBuilder.append(split[i]);
            resBuilder.append("\n");
        }
    }

    private PsiFile srcFile = null;

    @Nullable
    @Override
    public String preprocessOnCopy(PsiFile file, int[] startOffsets, int[] endOffsets, String text) {
        srcFile = file;

        return null;
    }

    @NotNull
    @Override
    public String preprocessOnPaste(Project project, PsiFile file, Editor editor, String text, RawText rawText) {
        HashSet<String> vars_in_fragment = new HashSet<>();
        HashMap<String, Integer> vars_counts_in_fragment = new HashMap<>();


        if (project == null || editor == null || file == null || !FragmentCorrectnessChecker.isCorrect(project, file, text, vars_in_fragment, vars_counts_in_fragment)) {
            return text;
        }

        // find number of code fragments considered as duplicated
        DuplicatesInspection.InspectionResult result = inspection.resolve(project, text.replace('\n', ' ').replace('\t', ' ').replace('\r', ' ').replaceAll("\\s+",""));

        if (result.count == 0) {
            return text;
        }

        //number of lines in fragment
        int linesOfCode = StringUtils.countMatches(text,"\n") + 1;


        MethodDeclarationMetricsExtractor.ParamsScores scores = new MethodDeclarationMetricsExtractor.ParamsScores();

        IFeaturesVector featuresVector;

        if (result.files.contains(file)) {
            featuresVector = calculateFeatures(file, text, vars_in_fragment, vars_counts_in_fragment, scores, linesOfCode);
        } else if (srcFile != null && result.files.contains(srcFile)) {
            featuresVector = calculateFeatures(srcFile, text, vars_in_fragment, vars_counts_in_fragment, scores, linesOfCode);
        } else if (!result.files.isEmpty()) {
            featuresVector = calculateFeatures(result.files.iterator().next(), text, vars_in_fragment, vars_counts_in_fragment, scores, linesOfCode);
        } else {
            return text;
        }

        if (!VectorValidator.isValid(featuresVector)) {
            return text;
        }

        linesOfCode = cleanedLocs(text, linesOfCode);

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
            if ((featuresVector.getFeature(Feature.KeywordNewTotalCount) > 0.0 || text.contains(".")) && StringUtils.countMatches(text, ",") > 1 && scores.in <= 1) {
                reasonToExtractMethod = AntiCopyPasterBundle.message("code.fragment.could.remove.duplicated.constructor.call.or.factory.method");
                forceExtraction = true;
            } else {
                return text;
            }
        }


        double size_score = Math.min(3.0, 0.3*Math.min(scores.method_lines - linesOfCode, linesOfCode ));
        double params_score = 2.0 - scores.out + Math.min(0, 2 - scores.in);


        double total_dep_fragment = featuresVector.getFeature(Feature.TotalLinesDepth);
        double total_dep_method = scores.all_dep;
        double max_dep_fragment = MethodDeclarationMetricsExtractor.maxDepth(text);
        double max_dep_method = scores.max_dep;

        double score_area = 2.0 * max_dep_method / Math.max(1.0, total_dep_method) * Math.min(total_dep_method - total_dep_fragment, total_dep_method - scores.all_rest);
        double score_max_dep = Math.min(max_dep_method - max_dep_fragment, max_dep_method - scores.max_rest);

        double score_overall = size_score + params_score + score_area + score_max_dep;

        if (score_overall >= 4.99) {
            reasonToExtractMethod = AntiCopyPasterBundle.message("code.fragment.strongly.simplifies.logic.of.enclosing.method");
            forceExtraction = true;
        }

        if ((score_overall >= 4.5 && result.count >= 4) && (result.count >= 5 && score_overall >= 3.0)) {
            reasonToExtractMethod = AntiCopyPasterBundle.message("code.fragment.simplifies.and.removes.duplicates",
                                                                 String.valueOf(result.count));
            forceExtraction = true;
        }

        int muchMatches = Math.max(0, result.count - 2);
        double pred_boost = Math.min(1, 0.33 * muchMatches);

        events_queue.add(new Event(file, text, result.count, featuresVector, project, editor, file.getText(), pred_boost, linesOfCode, forceExtraction, reasonToExtractMethod));

        return text;
    }

    private IFeaturesVector calculateFeatures(PsiFile file, String text, HashSet<String> vars_in_fragment, HashMap<String, Integer> vars_counts_in_fragment, MethodDeclarationMetricsExtractor.ParamsScores paramsScores, int linesCount) {
        IFeaturesVector featuresVector = new FeaturesVector(117);

        KeywordMetricsExtractor.calculate(text, linesCount, featuresVector);
        ConnectivityExtractor.calculate(file, text, linesCount, "", featuresVector);
        featuresVector.addFeature(new FeatureItem(Feature.TotalSymbolsInCodeFragment, text.length()));
        featuresVector.addFeature(new FeatureItem(Feature.AverageSymbolsInCodeLine, (double)text.length() / linesCount));

        int depthTotal = MethodDeclarationMetricsExtractor.totalDepth(text);

        featuresVector.addFeature(new FeatureItem(Feature.TotalLinesDepth, depthTotal));
        featuresVector.addFeature(new FeatureItem(Feature.AverageLinesDepth, (double)depthTotal / linesCount));
        featuresVector.addFeature(new FeatureItem(Feature.TotalCommitsInFragment, 1));
        featuresVector.addFeature(new FeatureItem(Feature.TotalAuthorsInFragment, 1));
        featuresVector.addFeature(new FeatureItem(Feature.LiveTimeOfFragment, 1e6));
        featuresVector.addFeature(new FeatureItem(Feature.AverageLiveTimeOfLine, 1e6));

        featuresVector.addFeature(new FeatureItem(Feature.TotalLinesOfCode, linesCount));

        MethodDeclarationMetricsExtractor.ParamsScores scores = MethodDeclarationMetricsExtractor.calculate(file, text, featuresVector, vars_in_fragment, vars_counts_in_fragment);
        paramsScores.in = scores.in;
        paramsScores.out = scores.out;
        paramsScores.max_rest = scores.max_rest;
        paramsScores.max_dep = scores.max_dep;
        paramsScores.all_rest = scores.all_rest;
        paramsScores.all_dep = scores.all_dep;
        paramsScores.method_lines = scores.method_lines;
        paramsScores.isSet = scores.isSet;

        return featuresVector;
    }

    private static String buildMessage(final IFeaturesVector featuresVector) {
      String reasonToExtractMethod = "";
        try {
            DecisionPathBuilder dpb = new DecisionPathBuilder(treeString);
            reasonToExtractMethod = AntiCopyPasterBundle.message("code.fragment.could.be.extracted.reason",
                                                                 dpb.collect(dpb.buildPath(featuresVector)));
        } catch (Exception e) {
            //skip
        }
        return reasonToExtractMethod;
    }

    private static void scheduleExtraction(Project project, PsiFile file, Editor editor, String text) {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            int caretPos = editor.getCaretModel().getOffset();

                            String fileText = file.getText();


                            int best_dist = 1000000000;
                            int st = -1;

                            if (!fileText.contains(text)) {
                                return;
                            }

                            int fromIdx = 0;
                            while (true) {
                                int idx = fileText.indexOf(text, fromIdx);
                                fromIdx = idx + 1;

                                if (idx == -1) {
                                    break;
                                }

                                int dist = Math.abs(idx - caretPos) + Math.abs(idx + text.length() - 1 - caretPos);
                                if (dist < best_dist) {
                                    best_dist = dist;
                                    st = idx;
                                }
                            }

                            if (st == -1) {
                                return;
                            }

                            editor.getSelectionModel().setSelection(st, st + text.length());


                            Language language = file.getLanguage();
                            PsiElement element = file.getOriginalElement();
                            List<RefactoringSupportProvider> providers = LanguageRefactoringSupport.INSTANCE.allForLanguage(language);
                            for (RefactoringSupportProvider provider : providers) {
                                if (provider.isAvailable(element)) {
                                    RefactoringActionHandler handler = provider.getExtractMethodHandler();
                                    DataContext dc = SimpleDataContext.getProjectContext(project);
                                    if (handler != null) {
                                        handler.invoke(project, editor, file, dc);
                                    }
                                }
                            }
                        });


                    }
                },
                100
        );
    }

    private int cleanedLocs(String text, int rawLocs) {
        Pattern p = Pattern.compile("/\\*[\\s\\S]*?\\*/");

        java.util.regex.Matcher m = p.matcher(text);

        int total_with_comment = 0;
        while (m.find()) {

            String lines[] = m.group(0).split("\n");
            total_with_comment += lines.length;
        }

        int total_unused = 0;
        for (String s: text.split("\n")) {
            String tmp = s.trim();
            if (tmp.isEmpty() || tmp.startsWith("//")) {
                total_unused++;
            }

        }

        return Math.max(0, rawLocs - total_unused - total_with_comment);
    }
}