plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("kotlinx-serialization")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "id.nkz.nokontzzzmanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.nkz.nokontzzzmanager"
        minSdk = 26
        targetSdk = 35
        versionCode = 23
        versionName = "1.0.1-hotfix-1"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        lint.disable.add("NullSafeMutableLiveData")
    }
    buildFeatures { compose = true }
    
    configurations.all {
        resolutionStrategy {
            force(libs.guava)
        }
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
}

kotlin {
    compilerOptions { 
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) 
    }
}

dependencies {
    // Core & App
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation & Lifecycle
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Hilt (Dependency Injection)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    kaptTest(libs.hilt.compiler)
    testImplementation(libs.hilt.android.testing)

    // Data
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    // Background Tasks
    implementation(libs.androidx.work.runtime.ktx)

    // Utility
    implementation(libs.libsu)
    implementation(libs.coil.compose)
    implementation(libs.guava) {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)
}
