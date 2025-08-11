import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")


}

android {
    namespace = "com.lowbyte.battery.animation"
    compileSdk = 36

    defaultConfig {
        applicationId = "emojibattery.widget.statusbar"
        minSdk = 24
        targetSdk = 35
        versionCode = 22
        versionName = "3.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    tasks.whenTaskAdded {
        if (name.startsWith("bundle") && name.endsWith("Release")) {
            doLast {
                val variantName = name.removePrefix("bundle").removeSuffix("Release").lowercase()
                val bundleDir = File(buildDir, "outputs/bundle/${variantName}/release")

                val originalBundle = bundleDir.listFiles()?.find { it.extension == "aab" } ?: return@doLast

                val appId = "BatteryEmoji"
                val versionCode = android.defaultConfig.versionCode
                val versionName = android.defaultConfig.versionName
                val date = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date())

                val newName = "${appId}_V${versionCode}_${versionName}_${date}.aab"
                val renamed = File(bundleDir, newName)

                if (originalBundle.renameTo(renamed)) {
                    println("✅ AAB renamed to: ${renamed.name}")
                } else {
                    println("❌ Failed to rename AAB")
                }
            }
        }
    }
    applicationVariants.all {
        outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl

            val appId ="BatteryEmoji"
            val versionCode = versionCode
            val versionName = versionName
            val date = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Date())
            val newApkName = "${appId}_V${versionCode}_${versionName}_${date}.apk"
            outputImpl.outputFileName = newApkName
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("/Users/admin/BatteryAnimation/batteryemoji.jks")
            storePassword = "batteryemoji"
            keyAlias = "batteryemoji"
            keyPassword = "batteryemoji"


        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
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
    implementation(libs.billing)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.multidex)
    implementation(libs.shimmer)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.glide)
    implementation(libs.picasso)
    implementation(libs.dotsindicator)
    implementation(libs.mintegral)
    implementation(libs.pangle)
    implementation(libs.vungle)
    implementation(libs.user.messaging.platform)
    implementation(libs.localization)
    implementation(libs.json)
    implementation(libs.gson)
    implementation("com.facebook.android:facebook-android-sdk:18.1.3")
//    implementation("com.adjust.sdk:adjust-android:5.4.2")
//    implementation("com.android.installreferrer:installreferrer:2.2")
//    implementation("com.adjust.sdk:adjust-android-webbridge:5.4.2")




    //Pamngel  , mentegral


}