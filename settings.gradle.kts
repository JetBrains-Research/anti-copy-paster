import java.net.URI

rootProject.name = "AntiCopyPaster"

val extractMethodExperimentsRepository = "https://github.com/JetBrains-Research/extract-method-experiments.git"
val extractMethodExperimentsProjectName = "org.jetbrains.research.extractMethod"

sourceControl {
    gitRepository(URI.create(extractMethodExperimentsRepository)) {
        producesModule("$extractMethodExperimentsProjectName:extract-method-metrics")
    }
}
