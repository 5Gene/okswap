import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * 插件引入方式
 * ```kotlin
 * apply<ProtobufConfig>()
 * ```
 */

class ProtobufConfig : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.protobuf")
            }
            val pbExtension = extensions.getByType<com.google.protobuf.gradle.ProtobufExtension>()
            println("=========================== $pbExtension")
            pbExtension.apply {
                protoc {
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
            extensions.getByType<ApplicationExtension>().apply {
                dependencies {
                    add("implementation", vlibs.findLibrary("protobuf-kotlin").get())
                }
            }
        }
    }
}