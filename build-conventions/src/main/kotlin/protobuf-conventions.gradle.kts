//此插件引入方式
//plugins {
//    id("protobuf-conventions")
//}

plugins {
    id("com.google.protobuf")
}

protobuf {
    protoc {
        // By default the plugin will search for the protoc executable in the system search path. We recommend you to take the advantage of pre-compiled protoc that we have published on Maven Central:
        artifact = "com.google.protobuf:protoc:${vlibs.findVersion("protobuf").get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                maybeCreate("java").apply {
                    option("lite")
                }
                maybeCreate("kotlin").apply {
                    option("lite")
                }
            }
        }
    }
}


dependencies {
    add("implementation", vlibs.findLibrary("protobuf-kotlin").get())
//    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
}