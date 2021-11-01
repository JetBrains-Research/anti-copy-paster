plugins {
    java
    id("org.jetbrains.intellij") version "0.7.2"
}

intellij {
    type = "IC"
    version = "2021.1"
    setPlugins("java")
}

group = "org.jetbrains.research.anticopypaster"
version = "2021.1-2.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

val extractMethodProjectName = "org.jetbrains.research.extractMethod"

dependencies {
    implementation("com.google.code.gson:gson:2.8.6")
    compile("org.apache.commons:commons-lang3:3.0")
    implementation("org.pmml4s:pmml4s_2.13:0.9.10")

    // extract-method-metrics module inclusion
    implementation("$extractMethodProjectName:extract-method-metrics") {
        version {
            branch = "master" // until merged into master
        }
    }
}

tasks {
    withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
        .forEach { it.enabled = false }
}