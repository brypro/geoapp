plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.brypro.socialgeo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.brypro.socialgeo"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Dependencias para CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle.v140)
    implementation(libs.androidx.camera.view.v140)
    implementation(libs.androidx.camera.extensions)
    implementation (libs.androidx.camera.camera2)
    // Dependencia para ListenableFuture
    implementation (libs.guava)
    //material design
    implementation(libs.material)
    //mapbox
    implementation(libs.android)
    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    // TODO: Add the dependencies for Firebase products you want to use
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.play.services.location)
    // Glide para manejar imágenes
    implementation (libs.glide)
    annotationProcessor(libs.compiler)
}