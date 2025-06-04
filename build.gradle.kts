import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

plugins {
    id("java") // Java support
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    id("jacoco") // Code coverage
    id("maven-publish")
}

group = "com.redhat.devtools.intellij"
version = providers.gradleProperty("projectVersion").get() // Plugin version
val platformVersion = providers.gradleProperty("ideaVersion").get()
val exhortRepoUser: String? = findProperty("gpr.username") as String?
    ?: System.getenv("GITHUB_USERNAME")
val exhortRepoToken: String? = findProperty("gpr.token") as String?
    ?: System.getenv("GITHUB_TOKEN")

// Set the JVM language level used to build the project.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenLocal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
    maven { url = uri("https://jitpack.io") }
    maven {
        url = uri("https://maven.pkg.github.com/trustification/exhort-java-api")
        credentials {
            username = exhortRepoUser
            password = exhortRepoToken
        }
    }
    maven {
        url = uri("https://maven.pkg.github.com/trustification/exhort-api-spec")
        credentials {
            username = exhortRepoUser
            password = exhortRepoToken
        }
    }
}

sourceSets {
    named("main") {
        java.srcDir("src/main/gen")
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(platformVersion)

        // Bundled Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        val platformBundledPlugins =  ArrayList<String>()
        platformBundledPlugins.addAll(providers.gradleProperty("platformBundledPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }.get())
        /*
         * starting from 2024.3, all json related code is know on its own plugin
         */
        if (platformVersion.startsWith("2024.3") || platformVersion.startsWith("2025.") || platformVersion.startsWith("25")) {
            platformBundledPlugins.add("com.intellij.modules.json")
        }
        println("use bundled Plugins: $platformBundledPlugins")
        bundledPlugins(platformBundledPlugins)

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        pluginVerifier()
        testFramework(TestFrameworkType.Platform)
    }

    implementation(libs.github.api)
    implementation(libs.commons.compress)
    implementation(libs.exhort.api.spec)
    implementation(libs.exhort.java.api)
    implementation(libs.caffeine)
    implementation(libs.packageurl.java)
    implementation(libs.commons.io)

    // for tests
    testImplementation(libs.junit)
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    runIde {
        systemProperties["com.redhat.devtools.intellij.telemetry.mode"] = "debug"
    }
}

// https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#runIdeForUiTests
val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
    task {
        systemProperties["com.redhat.devtools.intellij.telemetry.mode"] = "debug"
    }
    plugins {
        robotServerPlugin()
    }
}

intellijPlatform {
    pluginVerification {
        ides {
            recommended()
        }
    }
}