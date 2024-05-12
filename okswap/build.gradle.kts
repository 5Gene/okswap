plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
//    id("protobuf-convention")
//    id("io.github.5hmlA.protobuf")
    id("io.github.5hmlA.protobuf")
}

//apply<ProtobufConfig>()
//apply<TestConfig>()
//apply<AndroidComposeConfig>()
rootProject.ext["GROUP_ID"] = "osp.sparkj.ok"
rootProject.ext["ARTIFACT_ID"] = "okswap"
rootProject.ext["VERSION"] = "2023.10.12"

//apply(from = "https://raw.githubusercontent.com/5hmlA/5hmlA/space/publish-plugin.gradle")
//apply(from = "../publish-plugin.gradle")

//apply from: "https://raw.githubusercontent.com/5hmlA/5hmlA/space/publish-plugin.gradle"


java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}


dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.test.junit)
}