package org.jetbrains.research.anticopypaster.ide.fus;

import com.intellij.internal.statistic.eventLog.StatisticsEventLoggerProvider;
import com.intellij.internal.statistic.utils.StatisticsUploadAssistant;
import com.intellij.openapi.application.ApplicationManager;

import java.util.concurrent.TimeUnit;

public class SuggestionLoggerProvider extends StatisticsEventLoggerProvider {
    public SuggestionLoggerProvider() {
        super("DBP", 1, TimeUnit.MINUTES.toMillis(5), "50KB");
    }

    @Override
    public boolean isRecordEnabled() {
        return !ApplicationManager.getApplication().isUnitTestMode() &&
                !ApplicationManager.getApplication().isHeadlessEnvironment() &&
                StatisticsUploadAssistant.isCollectAllowed();
    }

    @Override
    public boolean isSendEnabled() {
        return isRecordEnabled() && StatisticsUploadAssistant.isSendAllowed();
    }
}
