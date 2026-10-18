plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.a_raiceandrd"
    compileSdk {
        version = release(36)
    }
        buildFeatures {
            viewBinding = true
        }

    defaultConfig {
        applicationId = "com.example.a_raiceandrd"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
configurations.all {
    exclude(group = "com.intellij", module = "annotations")
}

dependencies {
    // Fragments + Navigation (screen-to-screen navigation, drag-and-drop graph editor included)
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")

    // Lists (scan history, alerts, encyclopedia, reports tables)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ViewModel + LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

    // View Binding is enabled via a Gradle flag below, not a dependency

    // Networking (for later backend integration)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Local offline caching
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation ("androidx.room:room-compiler:2.6.1")

    // Camera (for scan screens)
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")

    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Map
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation(libs.androidx.cardview)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
}