import java.util.Properties
import java.io.FileInputStream

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
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.a_raiceandrd"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "API_BASE_URL", "\"https://a-raice-web-production-be92.up.railway.app/api/\"")

        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        val allowDebugSigned = project.findProperty("allowDebugSignedRelease") == "true"
        if (keystorePropertiesFile.exists()) {
            create("release") {
                val props = Properties()
                FileInputStream(keystorePropertiesFile).use { props.load(it) }
                storeFile = rootProject.file(props.getProperty("storeFile") ?: "keystore.jks")
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        } else if (allowDebugSigned) {
            // CI/dev verification only — logs a warning and uses debug key
            println("WARNING: keystore.properties not found. Using debug signing for release (allowDebugSignedRelease=true).")
        }
        // else: no release config created; buildTypes block will throw at config time below
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val allowDebugSigned = project.findProperty("allowDebugSignedRelease") == "true"
            when {
                keystorePropertiesFile.exists() -> {
                    signingConfig = signingConfigs.getByName("release")
                }
                allowDebugSigned -> {
                    // intentional debug-key fallback for CI only
                    signingConfig = signingConfigs.getByName("debug")
                }
                else -> {
                    // Fail the build immediately instead of silently shipping an unsigned APK
                    val releaseTask = gradle.startParameter.taskNames.any { name ->
                        name.contains("Release", ignoreCase = true) &&
                            (name.contains("assemble", ignoreCase = true) ||
                             name.contains("bundle", ignoreCase = true))
                    }
                    if (releaseTask) {
                        throw GradleException(
                            "\n\n=== RELEASE SIGNING REQUIRED ===\n" +
                            "keystore.properties was not found in the project root.\n" +
                            "Create it with the following keys:\n\n" +
                            "  storeFile=path/to/your.keystore\n" +
                            "  storePassword=YOUR_STORE_PASSWORD\n" +
                            "  keyAlias=YOUR_KEY_ALIAS\n" +
                            "  keyPassword=YOUR_KEY_PASSWORD\n\n" +
                            "To build without a keystore (CI/dev only) add: -PallowDebugSignedRelease=true\n" +
                            "=================================\n"
                        )
                    }
                    // Non-release task (e.g. lint) — use debug key so lint can still run
                    signingConfig = signingConfigs.getByName("debug")
                }
            }
        }
    }

    lint {
        checkReleaseBuilds = true
        abortOnError = true
        baseline = file("lint-baseline.xml")
        // Run ./gradlew updateLintBaseline to regenerate for existing warnings
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Fragments + Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")

    // Lists
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // ViewModel + LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Camera
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")

    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

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