@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.jumpkking.android.application)
    alias(libs.plugins.jumpkking.android.application.compose)
    alias(libs.plugins.jumpkking.android.hilt)
    alias(libs.plugins.gms.google.services)
    id("kotlin-parcelize")
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.androidx.baselineprofile)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    namespace = "com.joeloewi.jumpkking"

    defaultConfig {
        applicationId = "com.joeloewi.jumpkking"
        versionCode = 17
        versionName = "1.0.15"
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        val release by creating {
            keyAlias = System.getenv("ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
            storeFile = file("../jump_kking_key_store.jks")
            storePassword = System.getenv("KEY_STORE_PASSWORD")
        }
    }

    buildTypes {
        val debug by getting {

        }

        val release by getting {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

baselineProfile {
    automaticGenerationDuringBuild = true
}

dependencies {
    implementation(project(":data"))
    implementation(project(":domain"))
    baselineProfile(project(":baselineprofile"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.android.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)

    implementation(libs.androidx.savedstate.ktx)

    implementation(libs.androidx.hilt.navigation.compose)

    //hilt
    implementation(libs.hilt.android)

    //hilt-extension
    implementation(libs.hilt.ext.work)

    //compose
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.material.iconsExtended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    //accompanist
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.placeholder)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.accompanist.themeadapter.material3)
    implementation(libs.accompanist.navigation.material)

    //work
    implementation(libs.androidx.work.ktx)

    //start up
    implementation(libs.androidx.startup)

    //splashscreen
    implementation(libs.androidx.core.splashscreen)

    //image load
    implementation(libs.coil.kt.base)
    implementation(libs.coil.kt.compose)

    //webkit
    implementation(libs.androidx.webkit)

    //paging
    implementation(libs.androidx.paging.compose)

    //in-app update
    implementation(libs.android.play.app.update.ktx)

    ///firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.firestore)

    implementation(libs.tts)

    implementation(libs.rxjava3.rxandroid)
    implementation(libs.rxjava3.rxkotlin)
    implementation(libs.rxjava3.rxjava)

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-rx3
    implementation(libs.kotlinx.coroutines.rx3)

    implementation(libs.androidx.profileinstaller)

    implementation(libs.kotlinx.collections.immutable)
}