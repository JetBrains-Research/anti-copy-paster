package org.jetbrains.research.anticopypaster.statistics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores information about the plugin usage on the application level, not a specific project.
 * It could be retrieved from File -> Manage IDE Settings -> Export Settings -> Choose AntiCopyPasterUsageStatistics checkbox.
 */
@State(name = "AntiCopyPasterUsageStatistics", storages = {@Storage("anticopypaster-plugin.xml")})
public class AntiCopyPasterUsageStatistics implements PersistentStateComponent<AntiCopyPasterUsageStatistics.PluginState> {
    private PluginState usageState = new PluginState();

    @Override
    public @Nullable PluginState getState() {
        return usageState;
    }

    @Override
    public void loadState(@NotNull PluginState state) {
        usageState = state;
    }

    public static AntiCopyPasterUsageStatistics getInstance() {
        return ApplicationManager.getApplication().getService(AntiCopyPasterUsageStatistics.class);
    }

    public void notificationShown() {
        usageState.notification();
    }

    public void extractMethodApplied() {
        usageState.extractMethodApplied();
    }

    public void extractMethodRejected() {
        usageState.extractMethodRejected();
    }

    public void onCopy() {
        usageState.onCopy();
    }

    public void onPaste() {
        usageState.onPaste();
    }

    public static class PluginState {
        public int notificationCount = 0;
        public int extractMethodAppliedCount = 0;
        public int extractMethodRejectedCount = 0;
        public int copyCount = 0;
        public int pasteCount = 0;

        public void notification() {
            notificationCount += 1;
        }

        public void extractMethodApplied() {
            extractMethodAppliedCount += 1;
        }

        public void extractMethodRejected() {
            extractMethodRejectedCount += 1;
        }

        public void onCopy() {
            copyCount += 1;
        }

        public void onPaste() {
            pasteCount += 1;
        }
    }
}

