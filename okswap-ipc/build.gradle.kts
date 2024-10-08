plugins {
    alias(vcl.plugins.android.library)
    alias(vcl.plugins.gene.android)
    alias(vcl.plugins.gene.protobuf)
}

android {
    namespace = "osp.sparkj.okswap.ipc"
}

dependencies {
    implementation(project(mapOf("path" to ":okswap")))
}