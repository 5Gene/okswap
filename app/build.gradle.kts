plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
}


android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
//        consumerProguardFiles()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
//        signingConfig = signingConfigs.debug
    }

//    signingConfigs {
//        debug {
//            storeFile file('android.keystore')
//            storePassword 'oppo1234'
//            keyAlias 'ccc'
//            keyPassword 'oppo1234'
//        }
//    }

    productFlavors {
        //https://developer.android.google.cn/studio/build/build-variants
    }

    buildTypes {
        getByName("debug") {
            extra["alwaysUpdateBuildId"] = false
        }
        release {
            isMinifyEnabled = false
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    kotlinOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
        jvmTarget = "18"
    }

    buildFeatures {
        viewBinding = true
    }


    buildFeatures {
        compose = true
    }

    composeOptions {
        //https://developer.android.google.cn/jetpack/androidx/releases/compose-kotlin?hl=zh-cn
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
    namespace = "osp.sparkj.more"
}

dependencies {
    implementation(project(mapOf("path" to ":okswap-ipc")))
    implementation(project(mapOf("path" to ":okswap")))
    implementation(project(mapOf("path" to ":okswap-bluetooth")))
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.bundles.compose)
    androidTestImplementation(composeBom)
    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.okhttp)
    implementation(libs.bundles.android.project)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.bundles.androidx.benchmark)
}