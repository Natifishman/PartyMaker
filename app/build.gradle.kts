// app/build.gradle.kts – Android application module configuration

import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.diffplug.spotless") version "7.2.1"
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.github.ben-manes.versions") version "0.52.0"
}

android {
    namespace = "com.example.partymaker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.partymaker"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = null // Disable Android instrumentation tests

        // Load API keys from local.properties OR secrets.properties
        val localPropertiesFile = rootProject.file("local.properties")
        val secretsPropertiesFile = rootProject.file("secrets.properties")

        val properties = Properties()

        // Try loading from local.properties first
        if (localPropertiesFile.exists()) {
            localPropertiesFile.reader().use { properties.load(it) }
        }

        // Also load from secrets.properties (will override if both exist)
        if (secretsPropertiesFile.exists()) {
            secretsPropertiesFile.reader().use { properties.load(it) }
        }

        // API Keys - Can be in either local.properties or secrets.properties
        val openAiKey =
            properties.getProperty("openai.api.key")
                ?: properties.getProperty("OPENAI_API_KEY")
                ?: System.getenv("OPENAI_API_KEY")
                ?: ""

        val mapsKey =
            properties.getProperty("maps.api.key")
                ?: properties.getProperty("MAPS_API_KEY")
                ?: System.getenv("MAPS_API_KEY")
                ?: ""

        buildConfigField("String", "OPENAI_API_KEY", "\"$openAiKey\"")
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey.ifEmpty { "YOUR_API_KEY_HERE" }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0"
    }

    // Disable all test options
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = false
        unitTests.all { it.enabled = false }
    }

    // Disable lint checks
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

// Disable all test and lint tasks
tasks.configureEach {
    if (
        name.contains("test", ignoreCase = true) ||
        name.contains("androidTest", ignoreCase = true) ||
        name.contains("lint", ignoreCase = true)
    ) {
        enabled = false
    }
}

// Custom build task that skips tests
tasks.register("buildWithoutTests") {
    group = "build"
    description = "Assembles the project without running tests"
    dependsOn("assemble")
}

// Override the default build task
tasks.named("build") {
    dependsOn("buildWithoutTests")
    setDependsOn(
        dependsOn.filterNot {
            it.toString().contains("test", ignoreCase = true) ||
                it.toString().contains("androidTest", ignoreCase = true) ||
                it.toString().contains("lint", ignoreCase = true)
        },
    )
}

// Spotless – Code formatting for Java, Kotlin and XML
spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.17.0") // required for JVM 21/24+
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }

    // Kotlin support
    kotlin {
        target("src/**/*.kt")
        ktlint("1.2.1")
        trimTrailingWhitespace()
        endWithNewline()
    }

    // Kotlin Gradle scripts (build.gradle.kts, etc.)
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.kt")
        ktlint("1.2.1")
        trimTrailingWhitespace()
        endWithNewline()
    }

    // Format XML layout/resources
    format("xml") {
        target("src/**/*.xml")
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
}

dependencies {
    // --- AndroidX Core ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.activity.compose)

    // --- Lifecycle & ViewModel ---
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    // --- Jetpack Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // --- Material Design (classic) ---
    implementation(libs.material)

    // --- Firebase ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.database)
    implementation(libs.google.firebase.storage)
    implementation(libs.firebase.firestore)
    // Note: Firebase Cloud Messaging intentionally excluded

    // --- Google Services ---
    implementation(libs.gms.play.services.auth)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.places)

    // --- Room Database ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    annotationProcessor(libs.androidx.room.compiler)

    // --- Media & Image Loading ---
    implementation(libs.picasso)
    implementation(libs.circleimageview)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    // --- Animations ---
    implementation(libs.lottie)

    // --- OpenAI & Networking ---
    implementation(libs.openai.client)
    implementation(libs.ktor.client.android)
    implementation(libs.okhttp)
    implementation(libs.json)

    // --- Gson JSON Parser ---
    implementation(libs.gson)

    // --- Concurrent Programming ---
    implementation(libs.androidx.concurrent.futures)

    // --- Security ---
    implementation(libs.androidx.security.crypto)
}

// Secrets plugin configuration
secrets {
    defaultPropertiesFileName = "secrets.properties"
}
