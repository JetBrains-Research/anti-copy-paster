package ide.fus

import com.intellij.concurrency.JobScheduler
import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.FeatureUsageData
import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

object ACPCollector {
    private const val LOG_DELAY_MIN = 24 * 60
    private const val LOG_INITIAL_DELAY_MIN = 10
    private val LOG = LoggerFactory.getLogger(ACPCollector::class.java)

    private val eventGroup = EventLogGroup("dbp.count", ACPEventLogger.version)

    init {
        JobScheduler.getScheduler().scheduleWithFixedDelay(
                { trackRegistered() },
                LOG_INITIAL_DELAY_MIN.toLong(),
                LOG_DELAY_MIN.toLong(),
                TimeUnit.MINUTES
        )
    }

    @JvmStatic fun prediction(project: Project, featuresVector: List<String>, predictedClass: Float) = log("prediction") {
        addProject(project)
        addData("features", featuresVector)
        addData("prediction", predictedClass)
    }

    @JvmStatic fun userDecisionForRecommendation(project: Project, featuresVector: List<String>, userDecision: Boolean) = log("userDecision") {
        addProject(project)
        addData("features", featuresVector)
        addData("prediction", userDecision)
    }

    private fun log(eventId: String, body: FeatureUsageData.() -> Unit) {
        return try {
            val data = FeatureUsageData()
                    .addPluginInfo(ACPPlugin.info ?: return)
                    .apply(body)
            ACPEventLogger.log(eventGroup, eventId, data)
        } catch (ex: Exception) {
            LOG.warn("Failed to get PluginInfo for ${ACPPlugin.name}")
        }
    }

    private fun trackRegistered() = ACPEventLogger.log(eventGroup, "registered")
}