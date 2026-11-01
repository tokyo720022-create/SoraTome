import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Signing key, supplied by the build machine (GitHub Actions secrets). It is never stored in the
// repository. Without it the build falls back to Android's temporary debug key.
val signingKeyPath: String? = System.getenv("SORATOME_KEYSTORE_PATH")?.takeIf { it.isNotBlank() }
val signingKeyPassword: String? = System.getenv("SORATOME_KEYSTORE_PASSWORD")?.takeIf { it.isNotBlank() }
val hasSigningKey = signingKeyPath != null && signingKeyPassword != null

// Each GitHub Actions run gets a higher number, so a newer build always installs over an older one.
val buildNumber: Int = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

android {
    namespace = "io.github.tokyo720022create.soratome"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.tokyo720022create.soratome"
        minSdk = 26
        targetSdk = 35
        versionCode = buildNumber
        versionName = "0.1.0"
    }

    if (hasSigningKey) {
        signingConfigs {
            create("soratome") {
                storeFile = file(signingKeyPath!!)
                storePassword = signingKeyPassword
                keyAlias = "soratome"
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            if (hasSigningKey) signingConfig = signingConfigs.getByName("soratome")
        }
        release {
            isMinifyEnabled = false
            if (hasSigningKey) signingConfig = signingConfigs.getByName("soratome")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":plugin-api"))
    implementation(project(":plugin-runtime"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
