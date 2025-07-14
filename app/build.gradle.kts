plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")


}

android {
    namespace = "com.lowbyte.battery.animation"
    compileSdk = 35

    defaultConfig {
        applicationId = "emojibattery.widget.statusbar"
        minSdk = 24
        targetSdk = 35
        versionCode = 16
        versionName = "2.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
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
    }
    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.lottie)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.lottie)
    implementation (libs.colorpickerview)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.play.services.ads)
    implementation (libs.billing)
    implementation (libs.androidx.lifecycle.process)
    implementation (libs.androidx.multidex)
    implementation(libs.shimmer)
    implementation (libs.androidx.work.runtime.ktx)
    implementation (libs.glide)
    implementation (libs.picasso)
    implementation (libs.dotsindicator)
    implementation(libs.mintegral)
    implementation(libs.pangle)
    implementation(libs.user.messaging.platform)



    //Pamngel  , mentegral


}