plugins {
    id("java-library")
    alias(vcl.plugins.kotlin.jvm)
    alias(vcl.plugins.gene.protobuf)
}


//java {
//    sourceCompatibility = JavaVersion.VERSION_18
//    targetCompatibility = JavaVersion.VERSION_18
//}


dependencies {
    implementation(vcl.kotlinx.coroutines.core)
    testImplementation(vcl.test.junit)
}