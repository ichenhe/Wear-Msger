plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    compileSdk = 30
    defaultConfig {
        applicationId = "me.chenhe.wearmsger.demo"
        minSdk = 22
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

    implementation("com.google.android.support:wearable:2.8.1")
    implementation("com.google.android.gms:play-services-wearable:17.1.0")
    implementation("androidx.percentlayout:percentlayout:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    compileOnly("com.google.android.wearable:wearable:2.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.1")
}
