package org.jetbrains.research.anticopypaster.statistics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "UsageStatisticsCollector", storages = @Storage("anticopypaster.xml"))
public
class UsageStatisticsCollector implements PersistentStateComponent<PluginState> {
    private PluginState usageState = new PluginState();

    @Override
    public @Nullable PluginState getState() {
        return usageState;
    }

    @Override
    public void loadState(@NotNull PluginState state) {
        usageState = state;
    }

    public static UsageStatisticsCollector getInstance() {
        return ApplicationManager.getApplication().getService(UsageStatisticsCollector.class);
    }

    public void notificationShown() {
        usageState.notification();
    }

    public void extractMethodApplied() {
        usageState.extractMethodApplied();
    }

    public void extractMethodRejected() {
        usageState.extractMethodApplied();
    }
}

class PluginState {
    private int notificationCount = 0;
    private int extractMethodAppliedCount = 0;
    private int extractMethodRejectedCount = 0;

    public void notification() {
        notificationCount += 1;
    }

    public void extractMethodApplied() {
        extractMethodAppliedCount += 1;
    }

    public void extractMethodRejected() {
        extractMethodRejectedCount += 1;
    }
}

