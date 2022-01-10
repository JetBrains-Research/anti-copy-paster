package org.jetbrains.research.anticopypaster.ide.fus;

import com.intellij.concurrency.JobScheduler;
import com.intellij.internal.statistic.eventLog.EventLogGroup;
import com.intellij.internal.statistic.eventLog.FeatureUsageData;
import com.intellij.openapi.project.Project;
import org.jetbrains.research.anticopypaster.ide.RefactoringEvent;
import org.jetbrains.research.extractMethod.metrics.features.Feature;
import org.jetbrains.research.extractMethod.metrics.features.FeatureItem;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class SuggestionLogsCollector {
    private static final Integer LOG_DELAY_MIN = 24 * 60;
    private static final Integer LOG_INITIAL_DELAY_MIN = 5;
    private static final EventLogGroup group = new EventLogGroup("dbp.ddtm.count", SuggestionLogger.version);
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

    public void migrationUndone(Project project, int typeChangeId) {
        FeatureUsageData data = new FeatureUsageData().addProject(project)
                .addData("type_change_id", typeChangeId);
        SuggestionLogger.log(group, "migration.undone", data);
    }

    public void renamePerformed(Project project, String elementCanonicalName) {
        FeatureUsageData data = new FeatureUsageData().addProject(project)
                .addData("element_canonical_name", elementCanonicalName);
        SuggestionLogger.log(group, "rename.performed", data);
    }

    public void recoveringIntentionApplied(Project project, int typeChangeId) {
        FeatureUsageData data = new FeatureUsageData().addProject(project)
                .addData("type_change_id", typeChangeId);
        SuggestionLogger.log(group, "recovering.intention.applied", data);
    }

    public void refactoringSuggestionMade(Project project, FeaturesVector featuresVector) {
        FeatureUsageData data = new FeatureUsageData().addProject(project);
        for(Feature f : Feature.values()){
            data.addData(f.getName(), featuresVector.getFeature(f));
        }
        SuggestionLogger.log(group, "recovering.intention.applied", data);
    }
}
