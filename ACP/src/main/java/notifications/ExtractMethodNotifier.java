package notifications;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.util.concurrent.Callable;

public class ExtractMethodNotifier {
    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Extract Method suggest",
            NotificationDisplayType.BALLOON,
            true);

    public Notification notify(String content, Runnable callback) {
        return notify(null, content, callback);
    }

    public Notification notify(Project project, String content, Runnable callback) {
        final Notification notification = NOTIFICATION_GROUP.createNotification(content, NotificationType.INFORMATION);

        notification.setListener(new NotificationListener.Adapter() {
            @Override
            protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                callback.run();
            }
        });

        notification.notify(project);
        return notification;
    }
}