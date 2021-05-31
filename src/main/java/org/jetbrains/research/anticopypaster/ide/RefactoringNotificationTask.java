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
import com.intellij.refactoring.extractMethod.ExtractMethodProcessor;
import com.intellij.refactoring.extractMethod.PrepareFailedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.anticopypaster.AntiCopyPasterBundle;
import org.jetbrains.research.anticopypaster.models.IPredictionModel;
import org.jetbrains.research.anticopypaster.models.offline.WekaBasedModel;

import javax.swing.event.HyperlinkEvent;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.intellij.refactoring.extractMethod.ExtractMethodHandler.getProcessor;
import static org.jetbrains.research.anticopypaster.utils.PsiUtil.getElements;
import static org.jetbrains.research.anticopypaster.utils.PsiUtil.getStartOffset;

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
                DuplicatesInspection.InspectionResult result = inspection.resolve(event.file, event.text);
                if (result.getDuplicatesCount() < 1) {
                    return;
                }

                try {
                    List<Integer> prediction = model.predict(Collections.singletonList(event.vec));
                    int modelPrediction = prediction.get(0);

                    if ((event.forceExtraction || modelPrediction == 1) && canBeExtracted(event)) {
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

    public boolean canBeExtracted(RefactoringEvent event) {
        boolean canBeExtracted;
        int startOffset = getStartOffset(event.editor, event.file, event.text);
        PsiElement[] elementsInCodeFragment = getElements(event.project, event.file,
                                            startOffset, startOffset + event.text.length());
        final ExtractMethodProcessor processor = getProcessor(event.project, elementsInCodeFragment,
                                                              event.file, false);
        if (processor == null) return false;
        try {
            canBeExtracted = processor.prepare(null);
        } catch (PrepareFailedException e) {
            LOG.error("[ACP] Failed to check if a code fragment can be extracted.", e.getMessage());
            return false;
        }

        return canBeExtracted;
    }

    private Runnable getRunnableToShowSuggestionDialog(RefactoringEvent event) {
        return () -> {
            String message = event.reasonToExtract;
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

}
