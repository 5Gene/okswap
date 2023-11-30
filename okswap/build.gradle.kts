plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}


rootProject.ext["GROUP_ID"] = "osp.sparkj.ok"
rootProject.ext["ARTIFACT_ID"] = "okswap"
rootProject.ext["VERSION"] = "2023.10.12"

//apply(from = "https://raw.githubusercontent.com/5hmlA/5hmlA/space/publish-plugin.gradle")
//apply(from = "../publish-plugin.gradle")

//apply from: "https://raw.githubusercontent.com/5hmlA/5hmlA/space/publish-plugin.gradle"


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.test.junit)
}