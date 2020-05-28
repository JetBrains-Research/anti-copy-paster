import com.intellij.internal.statistic.eventLog.StatisticsEventLoggerProvider
import com.intellij.internal.statistic.utils.StatisticsUploadAssistant
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.registry.Registry

class ACPEventLoggerProvider : StatisticsEventLoggerProvider("DBP", 1) {
        override fun isRecordEnabled(): Boolean =
        !ApplicationManager.getApplication().isUnitTestMode &&
        Registry.`is`("feature.usage.event.log.collect.and.upload") &&
        StatisticsUploadAssistant.isCollectAllowed()

        override fun isSendEnabled(): Boolean = isRecordEnabled() && StatisticsUploadAssistant.isSendAllowed()
}