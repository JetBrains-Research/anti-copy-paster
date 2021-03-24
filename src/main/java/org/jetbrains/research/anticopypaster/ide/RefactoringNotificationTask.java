package org.jetbrains.research.anticopypaster.ide;

import com.intellij.CommonBundle;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.anticopypaster.AntiCopyPasterBundle;
import org.jetbrains.research.anticopypaster.builders.DecisionPathBuilder;
import org.jetbrains.research.anticopypaster.models.IPredictionModel;
import org.jetbrains.research.anticopypaster.models.features.features_vector.IFeaturesVector;
import org.jetbrains.research.anticopypaster.models.offline.WekaBasedModel;
import org.jetbrains.research.anticopypaster.utils.DuplicatesInspection;
import weka.classifiers.trees.RandomTree;
import weka.core.SerializationHelper;

import javax.swing.event.HyperlinkEvent;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Shows a notification about discovered Extract Method refactoring opportunity.
 */
public class RefactoringNotificationTask extends TimerTask {
    private static IPredictionModel model;
    private static RandomTree tree;
    private static String treeString;
    private ConcurrentLinkedQueue<RefactoringEvent> eventsQueue = new ConcurrentLinkedQueue<>();
    private static DuplicatesInspection inspection = new DuplicatesInspection();
    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Extract Method suggestion",
                                                                               NotificationDisplayType.BALLOON,
                                                                               true);

    private static final Logger LOG = Logger.getInstance(RefactoringNotificationTask.class);

    public RefactoringNotificationTask() {
        model = new WekaBasedModel();
        readModel();
        treeString = getTreeAsString(tree.toString().split("\n"));
    }

    private void readModel() {
        try {
            tree = (RandomTree) SerializationHelper.read(
                AntiCopyPastePreProcessor.class.getClassLoader().getResourceAsStream("RTree-ACP-SH.model"));
        } catch (Exception e) {
            LOG.error("[ACP] Failed to read a tree from RTree-ACP-SH model.", e.getMessage());
        }
    }

    @Override
    public void run() {
        while (!eventsQueue.isEmpty()) {
            final RefactoringEvent event = eventsQueue.poll();

            ApplicationManager.getApplication().runReadAction(() -> {
                DuplicatesInspection.InspectionResult result =
                    inspection.resolve(event.project, event.text.replace('\n', ' ')
                        .replace('\t', ' ').replace('\r', ' ')
                        .replaceAll("\\s+", ""));
                int matchesAfterEvent = event.matches + 1;
                if (result.count <= 1 && result.count < matchesAfterEvent) {
                    return;
                }

                try {
                    List<Integer> prediction = model.predict(Collections.singletonList(event.vec));
                    int modelPrediction = prediction.get(0);

                    if (event.forceExtraction || (modelPrediction == 1 && event.linesOfCode > 3) ||
                        (event.linesOfCode <= 3)) {
                        notify(event.project,
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

    private Runnable getRunnableToShowSuggestionDialog(RefactoringEvent event) {
        return () -> {
            String message = event.reasonToExtract;
            if (message.isEmpty()) {
                message = buildMessage(event.vec);
            }
            int result =
                Messages.showOkCancelDialog(message,
                                            AntiCopyPasterBundle.message("anticopypaster.recommendation.dialog.name"),
                                            CommonBundle.getOkButtonText(),
                                            CommonBundle.getCancelButtonText(),
                                            Messages.getInformationIcon());

            //result is equal to 0 if a user accepted the suggestion and clicked on OK button, 1 otherwise
            if (result == 0) {
                scheduleExtraction(event.project,
                                   event.file,
                                   event.editor,
                                   event.text);
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

    private String getTreeAsString(String[] split) {
        StringBuilder resBuilder = new StringBuilder();
        for (int i = 4; i < split.length - 2; ++i) {
            resBuilder.append(split[i]);
            resBuilder.append("\n");
        }
        return resBuilder.substring(0, resBuilder.toString().length() - 1);
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
}
