plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "es.ucm.fdi.pad.collabup"
    compileSdk = 36

    defaultConfig {
        applicationId = "es.ucm.fdi.pad.collabup"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    // Firebase products

    // Para Autenticación (Login, Signup)
    implementation("com.google.firebase:firebase-auth")

    // Para la Base de Datos (Cloud Firestore)
    implementation("com.google.firebase:firebase-firestore")

    // Para Analytics (recomendado)
    implementation("com.google.firebase:firebase-analytics")

    // Para agregar MUI
    implementation("com.google.android.material:material:1.12.0")

    // Para guardar archivos (fotos)
    implementation("com.google.firebase:firebase-storage")

    // Para cargar imágenes desde una URL (muy recomendado)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Google Sign-In
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
}