plugins {
    id("com.android.library")
//    id("android.compose")
    id("spark.build.proto.config")
}

apply<AndroidComposeConfig>()
//为啥不能这么引用
//apply<ProtobufConfig>()

rootProject.ext["GROUP_ID"] = "osp.sparkj.ok"
rootProject.ext["ARTIFACT_ID"] = "okswap-bluetooth"
rootProject.ext["VERSION"] = "2023.10.12"

//apply(from = "https://raw.githubusercontent.com/5hmlA/5hmlA/space/publish-plugin.gradle")
//apply(from = "../publish-plugin.gradle")


android {

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

    namespace = "osp.sparkj.okswap.bluetooth"
}

dependencies {
    implementation(project(mapOf("path" to ":okswap")))
}