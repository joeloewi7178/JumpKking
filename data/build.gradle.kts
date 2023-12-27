@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.jumpkking.android.library)
    alias(libs.plugins.jumpkking.android.hilt)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.joeloewi.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.android.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.espresso.core)

    //protobuf
    implementation(libs.protobuf.javalite)
    implementation(libs.protobuf.kotlin.lite)

    //datastore
    implementation(libs.androidx.dataStore.core)

    //hilt
    implementation(libs.hilt.android)

    implementation(libs.androidx.paging.common.ktx)

    implementation(libs.kotlinx.coroutines.play.services)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
}

protobuf {

    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                val java by registering {
                    option("lite")
                }
                val kotlin by registering {
                    option("lite")
                }
            }
        }
    }
}

hilt {
    enableAggregatingTask = true
}