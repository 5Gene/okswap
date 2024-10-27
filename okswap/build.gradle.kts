import june.wing.GroupIdMavenCentral

plugins {
    alias(vcl.plugins.kotlin.jvm)
    alias(vcl.plugins.gene.protobuf)
}

group = GroupIdMavenCentral
version = libs.versions.gene.okswap.get()


dependencies {
    implementation(vcl.kotlinx.coroutines.core)
    testImplementation(vcl.test.junit)
}