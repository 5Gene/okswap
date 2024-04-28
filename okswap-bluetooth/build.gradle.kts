plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
}


rootProject.ext["GROUP_ID"] = "osp.sparkj.ok"
rootProject.ext["ARTIFACT_ID"] = "okswap-bluetooth"
rootProject.ext["VERSION"] = "2023.10.12"

//apply(from = "https://raw.githubusercontent.com/5hmlA/5hmlA/space/publish-plugin.gradle")
//apply(from = "../publish-plugin.gradle")


android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
//        consumerProguardFiles()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

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

    buildFeatures {
        compose = true
    }
    composeOptions {
        //https://developer.android.google.cn/jetpack/androidx/releases/compose-kotlin?hl=zh-cn
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    kotlinOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
        jvmTarget = "18"
    }

    namespace = "osp.sparkj.okswap.bluetooth"
}

dependencies {
    implementation(project(mapOf("path" to ":okswap")))
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.android.project)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.bundles.androidx.benchmark)
}