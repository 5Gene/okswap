//https://docs.gradle.org/current/userguide/custom_plugins.html
//预编译脚本插件
//预编译脚本插件在执行之前会被编译成类文件并打包成 JAR。这些插件使用 Groovy 或 Kotlin DSL，
// 而不是纯 Java、Kotlin 或 Groovy。它们最好用作跨项目共享构建逻辑的约定插件，或者作为整齐组织构建逻辑的一种方式。
//要创建预编译脚本插件，您可以：
// - 使用 Gradle 的 Kotlin DSL - 插件是一个.gradle.kts文件，并应用id("kotlin-dsl").
// - 使用 Gradle 的 Groovy DSL - 该插件是一个.gradle文件，并应用id("groovy-gradle-plugin").
plugins {
    `kotlin-dsl`
}
//要应用预编译脚本插件，您需要知道其ID。 ID 源自插件脚本的文件名及其（可选）包声明。
//例如，该脚本src/main/*/java-library.gradle(.kts)的插件 ID 为java-library（假设它没有包声明）。
//同样，只要它的包声明为 ，src/main/*/my/java-library.gradle(.kts)就有一个插件 ID 。my.java-librarymy

repositories {
    gradlePluginPortal()
    google()
}


dependencies {
    compileOnly("com.android.tools.build:gradle:8.2.0")
    compileOnly("com.android.tools.build:gradle-api:8.2.0")
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
}
//https://docs.gradle.org/current/userguide/custom_plugins.html


println("============================ ${this} ===============")
//val vlibs2:VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

//println("============================ ${vlibs2.findVersion("protobuf").get()} ===============")
println("============================ build-conventions ===============")
