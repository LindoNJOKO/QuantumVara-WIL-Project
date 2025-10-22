plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("com.google.gms.google-services") // Firebase plugin
    id("kotlin-kapt") // Required for Room annotation processor
}

android {
    namespace = "com.example.nurture_nest"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nurture_nest"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "STRIPE_PUBLISHABLE_KEY",
            "\"pk_test_51SKL7qDHKF7VXR5pk9qMQtPUt8cIpCzDz6RDYozDsAxjGAYNkDUaGkPYclBYWmVAaKfqjk6zjXQPWY0LeKOC4Ck000sR9Mtgyr\""
        )
        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

val roomVersion = "2.6.1"
val stripeVersion = "20.49.0" // Latest stable Stripe Android SDK
val retrofitVersion = "2.9.0"
val okhttpVersion = "4.12.0"
val composeBom = "2024.02.00"

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Material Components
    implementation("com.google.android.material:material:1.11.0")

    // Activity & Fragment KTX
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.7.0")

    // ConstraintLayout
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.0")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.1.1"))

    // Firebase Modules
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // Google Play Services
    //implementation("com.google.android.gms:play-services-auth:20.7.0")
    //implementation("com.google.android.gms:play-services-wallet:19.1.0")
    //implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Room (with kapt for annotation processing)
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation(libs.androidx.activity)
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // ✅ Jetpack Compose - REQUIRED FOR STRIPE SDK
    implementation(platform("androidx.compose:compose-bom:$composeBom"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Stripe SDK
    implementation("com.stripe:stripe-android:$stripeVersion")

    // ✅ Retrofit & OkHttp (Networking)
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.6")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
