import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java") // Java support
    id("org.jetbrains.intellij.platform") // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    id("com.diffplug.spotless") version "6.25.0"
    id("checkstyle")
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
    implementation(project(":core"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.+")

    // Adding other Jackson dependencies explicitly
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.17.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdea(properties("platformVersion").get())
        pluginVerifier()
        testFramework(TestFrameworkType.Platform)
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

val pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
    val start = "<!-- Plugin description -->"
    val end = "<!-- Plugin description end -->"

    with(it.lines()) {
        if (!containsAll(listOf(start, end))) {
            throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
        }
        val html = subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
        // Marketplace validator requires the description to start with Latin characters.
        "GherkinLint validates BDD Gherkin steps in the editor and enforces team style.\n$html"
    }
}

val pluginChangeNotes = properties("pluginVersion").map { pluginVersion ->
    with(project.changelog) {
        renderItem(
            (getOrNull(pluginVersion) ?: getUnreleased())
                .withHeader(false)
                .withEmptySections(false),
            Changelog.OutputType.HTML,
        )
    }
}

intellijPlatform {
    pluginConfiguration {
        name.set(properties("pluginName"))
        version.set(properties("pluginVersion"))
        description.set(pluginDescription)
        changeNotes.set(pluginChangeNotes)
        ideaVersion {
            sinceBuild.set(properties("pluginSinceBuild"))
            untilBuild.set(properties("pluginUntilBuild"))
        }
    }

    pluginVerification {
        ides {
            ide("IU", properties("platformVersion").get())
        }
    }

    signing {
        certificateChain.set(environment("CERTIFICATE_CHAIN"))
        privateKey.set(environment("PRIVATE_KEY"))
        password.set(environment("PRIVATE_KEY_PASSWORD"))
    }

    publishing {
        token.set(environment("PUBLISH_TOKEN"))
        channels.set(properties("pluginVersion").map {
            listOf(
                it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
        })
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    jar {
        enabled = true
    }

    test {
        useJUnitPlatform()
    }

    register<DefaultTask>("format") {
        group = "formatting"
        description = "Apply code formatting (Spotless)."
        dependsOn("spotlessApply", ":core:spotlessApply")
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "checkstyle")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.19.2")
            target("src/**/*.java")
        }
    }

    configure<CheckstyleExtension> {
        toolVersion = "10.12.5"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        isShowViolations = true
    }

    tasks.named("check") {
        dependsOn("spotlessCheck", "checkstyleMain", "checkstyleTest")
    }
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        googleJavaFormat("1.19.2")
        target("src/**/*.java")
    }
}

configure<CheckstyleExtension> {
    toolVersion = "10.12.5"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    isShowViolations = true
}

tasks.named("check") {
    dependsOn("spotlessCheck", "checkstyleMain", "checkstyleTest")
}
