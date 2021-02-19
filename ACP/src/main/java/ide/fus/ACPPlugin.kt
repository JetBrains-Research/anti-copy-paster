package ide.fus

import com.intellij.ide.plugins.*
import com.intellij.internal.statistic.utils.*
import com.intellij.openapi.application.ApplicationManager
import org.jetbrains.annotations.TestOnly
import java.io.File

object ACPPlugin {
    private val classLoader: ClassLoader
        get() = this::class.java.classLoader

    private var myTestPluginId: String? = null

    private val descriptor: IdeaPluginDescriptor
        get() = PluginManagerCore.getLoadedPlugins().single {
            (ApplicationManager.getApplication().isUnitTestMode && it.pluginId.idString == myTestPluginId) ||
                    (ApplicationManager.getApplication().isUnitTestMode.not() && it.pluginClassLoader == classLoader)
        }

    val name: String
        get() = descriptor.name

    val installationFolder: File
        get() = descriptor.pluginPath.toFile()

    val info: PluginInfo?
        get() = try {
            getPluginInfoByDescriptor(descriptor)
        } catch (ex: Exception) {
            null
        }

    @TestOnly
    fun setTestPlugin(id: String) {
        myTestPluginId = id
    }
}