// Top-level build file where you can add configuration options common to all sub-projects/modules.
//@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(wings.plugins.protobuf) apply false
    alias(wings.plugins.android) apply false
    alias(wings.plugins.compose) apply false
    alias(wings.plugins.knife) apply false
}

//buildscript {
////val compose_version by extra("1.2.1")
//    //    val kotlin_version = "1.7.10"
////    val compose_version = "1.2.1"
////    project.ext {
////        set("compose_version", "1.2.1")
////        set("kotlin_version", "1.7.10")
////    }
//    println(project.extensions.getByName("ext"))
//    repositories {
//        google()
//        mavenCentral()
//    }
//    dependencies {
//        classpath(libs.android.gradle.plugin)
//        classpath(libs.kotlin.gradle.plugin)
//        // NOTE: Do not place your application dependencies here; they belong
//        // in the individual module build.gradle files
//    }
//}


val clean by tasks.creating(Delete::class) {
    delete(rootProject.buildDir)
}