plugins {
    java
    id("org.jetbrains.intellij") version "1.6.0"
}

group = "org.jetbrains.research.anticopypaster"
version = "2022.1-1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

val extractMethodProjectName = "org.jetbrains.research.extractMethod"

dependencies {
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.apache.commons:commons-lang3:3.0")
    implementation("org.pmml4s:pmml4s_2.13:0.9.10")
    implementation("org.tensorflow:tensorflow:1.15.0")


    // extract-method-metrics module inclusion
    implementation("$extractMethodProjectName:extract-method-metrics") {
        version {
            branch = "tf-metrics" // until merged into master
        }
    }
}

fun properties(key: String) = project.findProperty(key).toString()

intellij {
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    downloadSources.set(properties("platformDownloadSources").toBoolean())
    updateSinceUntilBuild.set(true)
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

tasks {
    withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
        .forEach { it.enabled = false }
    runIde {
        maxHeapSize = "1g"
    }
}