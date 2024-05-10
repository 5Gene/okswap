//https://docs.gradle.org/current/userguide/custom_plugins.html
//预编译脚本插件
//预编译脚本插件在执行之前会被编译成类文件并打包成 JAR。这些插件使用 Groovy 或 Kotlin DSL，
// 而不是纯 Java、Kotlin 或 Groovy。它们最好用作跨项目共享构建逻辑的约定插件，或者作为整齐组织构建逻辑的一种方式。
//要创建预编译脚本插件，您可以：
// - 使用 Gradle 的 Kotlin DSL - 插件是一个.gradle.kts文件，并应用id("kotlin-dsl").
// - 使用 Gradle 的 Groovy DSL - 该插件是一个.gradle文件，并应用id("groovy-gradle-plugin").
plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    `java-library`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
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

group = "spark.build"
version = "1.0"

publishing {
    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/5hmlA/sparkj")
            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
//        maven(url = "../build/repository")
        maven {
            //name会成为任务名字的一部分 publishOspPublicationTo [LocalTest] Repository
            name = "LocalTest"
            setUrl("${rootDir}/repo")
        }
    }
}

System.setProperty("gradle.publish.skip.namespace.check", "true")
tasks.create("before publishPlugins") {
    doFirst {
        val ss = System.getProperty("gradle.publish.skip.namespace.check", "false")
        println("===========================xxxxxxxxxxxxxxx=========== $ss")
    }
    tasks.findByName("publishPlugins")?.dependsOn(this)

}


gradlePlugin {
    website = "https://github.com/5hmlA/jspark"
    vcsUrl = "https://github.com/5hmlA/jspark.git"
    plugins {
//        findByName()
        register("android-config") {
            id = "${group}.android.config"
            displayName = "android config plugin"
            description = "android config plugin"
            tags = listOf("config", "android", "convention")
            implementationClass = "AndroidConfig"
        }
        register("android-compose") {
            id = "${group}.android.compose"
            displayName = "android compose config plugin"
            description = "android compose config plugin"
            tags = listOf("compose", "config", "android", "convention")
            implementationClass = "AndroidComposeConfig"
        }
        register("proto-config") {
            id = "${group}.proto.config"
            displayName = "protobuf config plugin"
            description = "protobuf config plugin"
            tags = listOf("protobuf", "config", "convention")
            implementationClass = "ProtobufConfig"
        }
//        register("proto-convention") {
//            id = "protobuf.conventions"
//            displayName = "protobuf config plugin"
//            description = "protobuf config plugin"
//            tags = listOf("protobuf", "config", "convention")
//            implementationClass = "Protobuf_conventionsPlugin"
//        }

    }
}