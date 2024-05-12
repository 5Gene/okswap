plugins {
    id("com.android.library")
    id("io.github.5hmlA.android.compose")
    id("io.github.5hmlA.protobuf")
//    id("io.github.5hmlA.protobuf-convention")
//    id("protobuf-convention")
}


rootProject.ext["GROUP_ID"] = "osp.sparkj.ok"
rootProject.ext["ARTIFACT_ID"] = "okswap-ipc"
rootProject.ext["VERSION"] = "2023.10.12"

//apply(from = "https://raw.githubusercontent.com/5hmlA/5hmlA/space/publish-plugin.gradle")
//apply(from = "../publish-plugin.gradle")


android {
    buildTypes {
        getByName("debug") {
            extra["alwaysUpdateBuildId"] = false
        }
        release {
            isMinifyEnabled = false
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro")
        }
    }

    namespace = "osp.sparkj.okswap.ipc"
}

dependencies {
    implementation(project(mapOf("path" to ":okswap")))
}