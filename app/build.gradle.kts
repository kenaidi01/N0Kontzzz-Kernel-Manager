

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
        //noinspection OldTargetApi
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
            force("com.google.guava:guava:32.1.3-jre")
        }
    }
}

// Separate Kotlin configuration to avoid conflicts
kotlin {
    compilerOptions { 
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) 
    }
}

dependencies {
    // Core & App
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-compose:1.11.0")

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:alpha")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation & Lifecycle
    implementation("androidx.navigation:navigation-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")

    // Hilt (Dependency Injection)
    implementation("com.google.dagger:hilt-android:2.57.1")
    kapt("com.google.dagger:hilt-compiler:2.57.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("androidx.hilt:hilt-work:1.3.0")
    kaptTest("com.google.dagger:hilt-compiler:2.57.1")
    testImplementation("com.google.dagger:hilt-android-testing:2.57.1")

    // Data
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Background Tasks
    implementation("androidx.work:work-runtime-ktx:2.10.4")

    // Utility
    implementation("com.github.topjohnwu.libsu:core:6.0.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.guava:guava:32.1.3-jre") {
        exclude(mapOf("group" to "com.google.guava", "module" to "listenablefuture"))
    }

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.7.0")
    testImplementation("org.mockito:mockito-core:5.20.0")
    testImplementation("org.robolectric:robolectric:4.16")
}
