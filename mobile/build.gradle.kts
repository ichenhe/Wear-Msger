plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    compileSdk = 30
    defaultConfig {
        applicationId = "me.chenhe.wearmsger.demo"
        minSdk = 21
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    namespace = "me.chenhe.lib.wearmsger.demo"
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    implementation(project(path = ":wearmsgerlib"))
    wearApp(":wear")

    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    testImplementation("junit:junit:4.13.2")
    implementation("com.google.android.gms:play-services-wearable:17.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")
}
