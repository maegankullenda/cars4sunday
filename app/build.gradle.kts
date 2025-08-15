plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt")
    alias(libs.plugins.hilt)
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.services)
}

android {
    namespace = "com.maegankullenda.carsonsunday"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.maegankullenda.carsonsunday"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    }

    // Linting configuration
    lint {
        checkReleaseBuilds = true
        abortOnError = true
        disable += "MissingTranslation"
        error += "LogNotTimber"
        warning += "NewApi"
    }
}

// KtLint configuration
ktlint {
    version.set("0.50.0")
    android.set(true)
    filter {
        exclude("**/generated/**")
    }
}

// Detekt configuration
detekt {
    config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false

    // Exclude UI-related files from strict rules
    baseline = file("$projectDir/config/detekt/baseline.xml")

    // Configure source sets to apply different rules
    source.setFrom(
        files(
            "src/main/java",
            "src/test/java",
            "src/androidTest/java",
        ),
    )
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Architecture Components
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Networking
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.core)
    implementation(libs.moshi.kotlin)
    implementation("com.google.code.gson:gson:2.10.1")

    // Image Loading
    implementation(libs.coil.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)



    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk.core)
    testImplementation(libs.truth.core)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
