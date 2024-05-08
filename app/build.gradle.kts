plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("protobuf-conventions")
//    alias(libs.plugins.protobuf)
}


//apply<ProtobufConfig>()
apply<ComposeConfig>()
apply<TestConfig>()


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
//            storePassword 'jzb1234'
//            keyAlias 'spark'
//            keyPassword 'jzb1234'
//        }
//    }

    kotlinOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
        jvmTarget = "20"
    }

    buildTypes {
        create("MyBuildType") {
            //子模块没有配置 MyBuildType 的时候默认用 debug
            matchingFallbacks.add("debug")
        }

        release {
            isMinifyEnabled = false
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro")
        }

        debug {
            extra["alwaysUpdateBuildId"] = false
        }
    }

    //https://developer.android.google.cn/studio/build/build-variants
    flavorDimensions += "mode"
    productFlavors {
        create("demo") {
            // Assigns this product flavor to the "mode" flavor dimension.
            dimension = "mode"
        }

        create("full") {
            dimension = "mode"
        }
    }
//    sourceSets.forEach {
//        it.java.srcDirs(protobuf.generatedFilesBaseDir)
//        println(" source set ${it.name}")
//    }

//    sourceSets.getByName("main").java.srcDirs(protobuf.generatedFilesBaseDir)

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
        compose = true
    }

    composeOptions {
        //https://developer.android.google.cn/jetpack/androidx/releases/compose-kotlin?hl=zh-cn
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    androidComponents {
        beforeVariants { variantBuilder ->
            println("====================================== ${variantBuilder.name}")
            variantBuilder.productFlavors.forEach { flavor ->
                println("====================================== $flavor")
            }
        }
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
//    implementation(libs.protobuf.kotlin)
}