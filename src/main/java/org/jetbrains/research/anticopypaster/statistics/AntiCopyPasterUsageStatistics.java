package org.jetbrains.research.anticopypaster.statistics;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores information about the plugin usage on the project level in the ./idea/anticopypaster-plugin.xml file.
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

    public static AntiCopyPasterUsageStatistics getInstance(Project project) {
        return project.getService(AntiCopyPasterUsageStatistics.class);
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

