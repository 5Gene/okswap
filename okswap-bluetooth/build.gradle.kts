plugins {
    alias(vcl.plugins.android.library)
    alias(vcl.plugins.gene.compose)
    alias(vcl.plugins.gene.protobuf)
}

android {
    namespace = "osp.sparkj.okswap.bluetooth"
}

dependencies {
    implementation(project(mapOf("path" to ":okswap")))
}