package org.jetbrains.research.anticopypaster.ide;

import com.intellij.CommonBundle;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.extractMethod.ExtractMethodProcessor;
import com.intellij.refactoring.extractMethod.PrepareFailedException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.anticopypaster.AntiCopyPasterBundle;
import org.jetbrains.research.anticopypaster.checkers.FragmentCorrectnessChecker;
import org.jetbrains.research.anticopypaster.metrics.extractors.CouplingCalculator;
import org.jetbrains.research.anticopypaster.metrics.extractors.HistoricalFeaturesExtractor;
import org.jetbrains.research.anticopypaster.metrics.extractors.KeywordMetricsExtractor;
import org.jetbrains.research.anticopypaster.metrics.extractors.MethodDeclarationMetricsExtractor;
import org.jetbrains.research.anticopypaster.models.IPredictionModel;
import org.jetbrains.research.anticopypaster.models.features.feature.Feature;
import org.jetbrains.research.anticopypaster.models.features.feature.FeatureItem;
import org.jetbrains.research.anticopypaster.models.features.features_vector.FeaturesVector;
import org.jetbrains.research.anticopypaster.models.features.features_vector.IFeaturesVector;
import org.jetbrains.research.anticopypaster.models.offline.WekaBasedModel;

import javax.swing.event.HyperlinkEvent;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.intellij.refactoring.extractMethod.ExtractMethodHandler.getProcessor;
import static org.jetbrains.research.anticopypaster.utils.PsiUtil.*;

/**
 * Shows a notification about discovered Extract Method refactoring opportunity.
 */
public class RefactoringNotificationTask extends TimerTask {
    private static IPredictionModel model;
    private ConcurrentLinkedQueue<RefactoringEvent> eventsQueue = new ConcurrentLinkedQueue<>();
    private static DuplicatesInspection inspection = new DuplicatesInspection();
    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Extract Method suggestion",
                                                                               NotificationDisplayType.BALLOON,
                                                                               true);

    private static final Logger LOG = Logger.getInstance(RefactoringNotificationTask.class);

    public RefactoringNotificationTask() {
        model = new WekaBasedModel();
    }

    @Override
    public void run() {
        while (!eventsQueue.isEmpty()) {
            final RefactoringEvent event = eventsQueue.poll();

            ApplicationManager.getApplication().runReadAction(() -> {
                DuplicatesInspection.InspectionResult result = inspection.resolve(event.getFile(), event.getText());
                if (result.getDuplicatesCount() < 1) {
                    return;
                }

                try {
                    HashSet<String> variablesInCodeFragment = new HashSet<>();
                    HashMap<String, Integer> variablesCountsInCodeFragment = new HashMap<>();

                    if (!FragmentCorrectnessChecker.isCorrect(event.getProject(), event.getFile(),
                                                              event.getText(),
                                                              variablesInCodeFragment,
                                                              variablesCountsInCodeFragment)) {
                        return;
                    }
                    FeaturesVector featuresVector =
                        calculateFeatures(event, variablesInCodeFragment, variablesCountsInCodeFragment);

                    if (event.getScores().out > 1 || event.getScores().in > 3 || event.getLinesOfCode() == 0) {
                        return;
                    }

                    List<Integer> prediction = model.predict(Collections.singletonList(featuresVector));
                    int modelPrediction = prediction.get(0);
                    calculateMessageToShow(event, featuresVector, result.getDuplicatesCount());

                    if ((event.isForceExtraction() || modelPrediction == 1) &&
                        canBeExtracted(event) && event.getReasonToExtract() != null) {
                        notify(event.getProject(),
                               AntiCopyPasterBundle.message(
                                   "extract.method.refactoring.is.available"),
                               getRunnableToShowSuggestionDialog(event)
                        );
                    }
                } catch (Exception e) {
                    LOG.error("[ACP] Failed to make a prediction.", e.getMessage());
                }
            });
        }
    }

    public boolean canBeExtracted(RefactoringEvent event) {
        boolean canBeExtracted;
        int startOffset = getStartOffset(event.getEditor(), event.getFile(), event.getText());
        PsiElement[] elementsInCodeFragment = getElements(event.getProject(), event.getFile(),
                                                          startOffset, startOffset + event.getText().length());
        final ExtractMethodProcessor processor = getProcessor(event.getProject(), elementsInCodeFragment,
                                                              event.getFile(), false);
        if (processor == null) return false;
        try {
            canBeExtracted = processor.prepare(null);
            processor.findOccurrences();
        } catch (PrepareFailedException e) {
            LOG.error("[ACP] Failed to check if a code fragment can be extracted.", e.getMessage());
            return false;
        }

        return canBeExtracted;
    }

    private Runnable getRunnableToShowSuggestionDialog(RefactoringEvent event) {
        return () -> {
            String message = event.getReasonToExtract();
            if (message.isEmpty()) {
                message = AntiCopyPasterBundle.message("extract.method.to.simplify.logic.of.enclosing.method");
            }
            int result =
                Messages.showOkCancelDialog(message,
                                            AntiCopyPasterBundle.message("anticopypaster.recommendation.dialog.name"),
                                            CommonBundle.getOkButtonText(),
                                            CommonBundle.getCancelButtonText(),
                                            Messages.getInformationIcon());

            //result is equal to 0 if a user accepted the suggestion and clicked on OK button, 1 otherwise
            if (result == 0) {
                scheduleExtraction(event.getProject(),
                                   event.getFile(),
                                   event.getEditor(),
                                   event.getText());
            }
        };
    }

    public void notify(Project project, String content, Runnable callback) {
        final Notification notification = NOTIFICATION_GROUP.createNotification(content, NotificationType.INFORMATION);
        notification.setListener(new NotificationListener.Adapter() {
            @Override
            protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                callback.run();
            }
        });
        notification.notify(project);
    }

    private static void scheduleExtraction(Project project, PsiFile file, Editor editor, String text) {
        new java.util.Timer().schedule(
            new ExtractionTask(editor, file, text, project),
            100
        );
    }

    public void addEvent(RefactoringEvent event) {
        this.eventsQueue.add(event);
    }

    /**
     * Calculates the metrics for the pasted code fragment and a method where the code fragment was pasted into.
     */
    private FeaturesVector calculateFeatures(RefactoringEvent event,
                                             HashSet<String> varsInFragment,
                                             HashMap<String, Integer> varsCountsInFragment) {
        FeaturesVector featuresVector = new FeaturesVector(117);

        KeywordMetricsExtractor.calculate(event.getText(), event.getLinesOfCode(), featuresVector);
        CouplingCalculator.calculate(event.getFile(), event.getText(), event.getLinesOfCode(), featuresVector);
        featuresVector.addFeature(new FeatureItem(Feature.TotalSymbolsInCodeFragment, event.getText().length()));
        featuresVector.addFeature(
            new FeatureItem(Feature.AverageSymbolsInCodeLine,
                            (double) event.getText().length() / event.getLinesOfCode()));

        int depthTotal = MethodDeclarationMetricsExtractor.totalDepth(event.getText());

        featuresVector.addFeature(new FeatureItem(Feature.TotalLinesDepth, depthTotal));
        featuresVector.addFeature(
            new FeatureItem(Feature.AverageLinesDepth, (double) depthTotal / event.getLinesOfCode()));

        MethodDeclarationMetricsExtractor.ParamsScores scores =
            MethodDeclarationMetricsExtractor.calculate(event.getFile(), event.getText(), featuresVector,
                                                        varsInFragment,
                                                        varsCountsInFragment);
        event.setScores(scores);

        try {
            PsiFile file = event.getFile();
            final String virtualFilePath = file.getVirtualFile().getCanonicalPath();
            PsiMethod psiMethodBeforeRevision = getMethodStartLineInBeforeRevision(file, event.getDestinationMethod());
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

    private void calculateMessageToShow(RefactoringEvent event, IFeaturesVector featuresVector, int duplicatesCount) {
        if (featuresVector.getFeature(Feature.KeywordBreakTotalCount) >= 3.0) {
            return;
        }

        if (event.getLinesOfCode() >= 4 && event.getScores().out == 1 && event.getScores().in >= 1) {
            event.setForceExtraction(true);
            event.setReasonToExtract(AntiCopyPasterBundle.message(
                "extract.method.to.simplify.logic.of.enclosing.method"));
        }

        if (event.getLinesOfCode() == 1) {
            if ((featuresVector.getFeature(Feature.KeywordNewTotalCount) > 0.0 ||
                event.getText().contains(".")) && StringUtils.countMatches(event.getText(),
                                                                           ",") > 1 && event.getScores().in <= 1) {
                event.setReasonToExtract(AntiCopyPasterBundle.message(
                    "extract.method.to.remove.duplicated.constructor.call.or.factory.method"));
                event.setForceExtraction(true);
            } else {
                return;
            }
        }

        double scoreOverall =
            getScoreOverall(event.getText(), event.getLinesOfCode(), event.getScores(), featuresVector);

        if (scoreOverall >= 4.99) {
            event.setReasonToExtract(AntiCopyPasterBundle.message(
                "code.fragment.strongly.simplifies.logic.of.enclosing.method"));
            event.setForceExtraction(true);
        }

        if ((scoreOverall >= 4.5 && duplicatesCount >= 4) && (duplicatesCount >= 5 && scoreOverall >= 3.0)) {
            event.setReasonToExtract(
                AntiCopyPasterBundle.message("extract.method.to.simplify.enclosing.method.and.remove.duplicates",
                                             String.valueOf(duplicatesCount)));
            event.setForceExtraction(true);
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
}
