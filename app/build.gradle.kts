plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.zetadev.locationwidget"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zetadev.locationwidget"
        minSdk = 29
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("androidx.preference:preference:1.2.1")
    //posizione
    implementation (libs.play.services.location)
    //work manager per update in background*
    implementation (libs.work.runtime)
    //json
    implementation (libs.gson)

    //osm
    implementation ("org.osmdroid:osmdroid-android:6.1.20")
}