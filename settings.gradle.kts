import java.net.URI

rootProject.name = "AntiCopyPaster"

val utilitiesRepo = "https://github.com/JetBrains-Research/extract-method-experiments.git"
val utilitiesProjectName = "org.jetbrains.research.extractMethod"

sourceControl {
    gitRepository(URI.create(utilitiesRepo)) {
        producesModule("$utilitiesProjectName:extract-method-metrics")
    }
}
