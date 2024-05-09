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

println("================================================================== $this >:$name".blue)
println("protobuf文档: https://protobuf.dev/")
println("最佳实践: https://protobuf.dev/programming-guides/api/")
println("   - 不要重复使用标签号码 ")
println("   - 为已删除的字段保留标签号")
println("   - 为已删除的枚举值保留编号")
println("   - 不要更改字段的类型 ")
println("   - 不要发送包含很多字段的消息 ")
println("   - 不要更改字段的默认值 ")
println("   - 不要更改字段的默认值 ")
println("======================================================================".blue)

dependencies {
    add("implementation", vlibs.findLibrary("protobuf-kotlin").get())
//    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
}