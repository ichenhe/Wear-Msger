plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId("cc.chenhe.lib.wearmsger.demo")
        minSdkVersion(22)
        targetSdkVersion(29)
        versionCode(1)
        versionName("1.0")
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
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    implementation(project(path = ":wearmsgerlib"))

    implementation("com.google.android.support:wearable:2.8.1")
    implementation("com.google.android.gms:play-services-wearable:17.1.0")
    implementation("androidx.percentlayout:percentlayout:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    compileOnly("com.google.android.wearable:wearable:2.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")
}
