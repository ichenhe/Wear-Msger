plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId("cc.chenhe.lib.wearmsger.demo")
        minSdkVersion(21)
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
    wearApp(":wear")

    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    testImplementation("junit:junit:4.13.2")
    implementation("com.google.android.gms:play-services-wearable:17.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")
}
