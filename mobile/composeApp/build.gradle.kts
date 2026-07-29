import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Dependency Injection
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // MVVM
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // Navigation
            implementation(libs.androidx.navigation.compose)

            // Networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)

            // Serialization
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            // Date & Time
            implementation(libs.kotlinx.datetime)

            // Local Storage
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.datastore.preferences.core)

            // Image Loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.lifeos.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.lifeos.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        // Unchanged from this project's previous single hardcoded value —
        // 10.0.2.2 is the emulator's alias for the host machine's localhost.
        getByName("debug") {
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/api/v1\"")
        }
        // Real value supplied at build time via `-PLIFEOS_API_BASE_URL=...`
        // or a `LIFEOS_API_BASE_URL` entry in `local.properties`/CI secrets —
        // e.g. a Railway/Render URL — so testing off this machine's network
        // needs a build-time value, never a source change or duplicated
        // constant. Falls back to an obvious placeholder if never supplied.
        getByName("release") {
            isMinifyEnabled = false
            val apiBaseUrl = (project.findProperty("LIFEOS_API_BASE_URL") as String?)
                ?: "https://REPLACE_WITH_DEPLOYED_BACKEND_URL/api/v1"
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
            // No signing config means `assembleRelease` produces an
            // unsigned APK Android refuses to install anywhere. Signing
            // with the auto-generated debug keystore (the standard,
            // pragmatic choice for internal/mentor test distribution) makes
            // `assembleRelease` produce a real installable APK without
            // fabricating a production upload keystore this project has no
            // secure way to hold yet — replace with a real keystore
            // (`signingConfigs.create("release") { ... }`) before ever
            // publishing to the Play Store.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
