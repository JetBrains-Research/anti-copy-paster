package org.jetbrains.research.anticopypaster.ide.fus;

import com.intellij.concurrency.JobScheduler;
import com.intellij.internal.statistic.eventLog.EventLogGroup;
import com.intellij.internal.statistic.eventLog.FeatureUsageData;
import com.intellij.openapi.project.Project;
import org.jetbrains.research.extractMethod.metrics.features.Feature;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class SuggestionLogsCollector {
    private static final Integer LOG_DELAY_MIN = 24 * 60;
    private static final Integer LOG_INITIAL_DELAY_MIN = 5;
    private static final EventLogGroup group = new EventLogGroup("dbp.acp.count", SuggestionLogger.version);
    private static SuggestionLogsCollector instance;

    private SuggestionLogsCollector() {
        JobScheduler.getScheduler().scheduleWithFixedDelay(
                SuggestionLogsCollector::trackRegistered,
                LOG_INITIAL_DELAY_MIN.longValue(),
                LOG_DELAY_MIN.longValue(),
                TimeUnit.MINUTES
        );
    }

    public static SuggestionLogsCollector getInstance() {
        if (instance == null) {
            instance = new SuggestionLogsCollector();
        }
        return instance;
    }

    private static void trackRegistered() {
        SuggestionLogger.log(group, "registered");
    }

    private FeatureUsageData makeRefactoringData(Project project, FeaturesVector featuresVector) {
        FeatureUsageData data = new FeatureUsageData().addProject(project);
        for (Feature f : Feature.values()) {
            data.addData(f.getName(), featuresVector.getFeature(f));
        }
        return data;
    }

    public void refactoringNotificationMade(Project project, FeaturesVector featuresVector) {
        SuggestionLogger.log(group, "refactoring.notification.made",
                makeRefactoringData(project, featuresVector));
    }

    public void refactoringNotificationApplied(Project project, FeaturesVector featuresVector) {
        SuggestionLogger.log(group, "refactoring.notification.applied",
                makeRefactoringData(project, featuresVector));
    }

    public void refactoringNotificationDismissed(Project project, FeaturesVector featuresVector) {
        SuggestionLogger.log(group, "refactoring.notification.dismissed",
                makeRefactoringData(project, featuresVector));
    }
}
