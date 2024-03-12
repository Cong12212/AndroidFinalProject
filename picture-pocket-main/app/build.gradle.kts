plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.finalproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.finalproject"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),   "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(project(mapOf("path" to ":mylibrary")))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    //change pages
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    //trans data
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("commons-io:commons-io:20030203.000550")
    //zoom img
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")
    //PDF
    implementation ("com.itextpdf:itextg:5.5.10")
    //DB
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

    implementation("com.squareup:gifencoder:0.10.1")

    implementation ("com.burhanrashid52:photoeditor:3.0.1")
}