plugins {
    alias(vcl.plugins.android.library)
    alias(vcl.plugins.gene.android)
    alias(vcl.plugins.gene.protobuf)
}

android {
    namespace = "osp.sparkj.okswap.p2p"
}

dependencies {
    implementation(project(mapOf("path" to ":okswap")))
}