plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.mehtasunny.a11yjourney.demo"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.mehtasunny.a11yjourney.demo"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "0.1.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    lint {
        abortOnError = true
    }
}

dependencies {
    implementation(project(":library"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)
}
