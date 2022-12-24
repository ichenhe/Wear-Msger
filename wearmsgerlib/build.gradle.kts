plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")

    id("com.vanniktech.maven.publish")
}

android {
    compileSdk = 30
    namespace = "me.chenhe.lib.wearmsger"

    defaultConfig {
        minSdk = 21
        targetSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.5.1")
    api("androidx.lifecycle:lifecycle-service:2.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.1")
    implementation("com.google.android.gms:play-services-wearable:17.0.0")
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)
    signAllPublications()
}