plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "JunZi.Pixiv"
    compileSdk = 36

    defaultConfig {
        applicationId = "JunZi.Pixiv"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.1.1"
    }

    signingConfigs {
        create("illustFerry") {
            storeFile = file("keystore/IllustFerry.jks")
            storePassword = "IllustFerry2000"
            keyAlias = "IllustFerry"
            keyPassword = "IllustFerry2000"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("illustFerry")
        }

        release {
            signingConfig = signingConfigs.getByName("illustFerry")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.05.00"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.webkit:webkit:1.16.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("com.github.bumptech.glide:glide:5.0.7")
    implementation("com.github.bumptech.glide:okhttp3-integration:5.0.7")
    implementation("org.bouncycastle:bcprov-jdk18on:1.81")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.81")
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.7")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
