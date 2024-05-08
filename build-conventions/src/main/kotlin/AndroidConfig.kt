import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonToolOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidConfig : Plugin<Project> {

    val kotlinOptionsConfigs: MutableList<KotlinCommonToolOptions.() -> Unit> = mutableListOf(
        {
            freeCompilerArgs += "-Xcontext-receivers"
        }
    )

    val androidConfigs: MutableList<AndroidExtensionConfig> = mutableListOf(
        androidCommonConfig()
    )

    val dependenicesConfigs: MutableList<DependenicesConfig> = mutableListOf(
        androidCommonDependenices()
    )

    val applyPluginsConfigConfigs: MutableList<ApplyPluginsConfig> = mutableListOf(
        {
            apply("kotlin-android")
            apply("kotlin-parcelize")
        }
    )

    private fun androidCommonConfig(): AndroidExtension.(Project, VersionCatalog) -> Unit =
        { project: Project, vlibs: VersionCatalog ->
            project.tasks.withType<KotlinCompile>().configureEach {
                kotlinOptions {
                    kotlinOptionsConfigs.forEach {
                        it.invoke(this)
                    }
                }
            }
        }

    private fun androidCommonDependenices(): DependencyHandlerScope.(VersionCatalog) -> Unit =
        { vlibs: VersionCatalog ->
            val bom = vlibs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
            add("implementation", vlibs.findBundle("compose").get())
            add("debugImplementation", vlibs.findLibrary("androidx-compose-ui-tooling-preview").get())
            add("debugImplementation", vlibs.findLibrary("androidx-compose-ui-tooling").get())
        }

    override fun apply(target: Project) {
        with(target) {
            val pluginManager = target.pluginManager
            applyPluginsConfigConfigs.forEach {
                it.invoke(pluginManager)
            }
            val catalog = vlibs
            android.apply {
                androidConfigs.forEach {
                    it(target, catalog)
                }
            }
            tasks.withType<KotlinCompile>().configureEach {
                kotlinOptions {
                    freeCompilerArgs += "-Xcontext-receivers"
                }
            }

            dependencies {
                dependenicesConfigs.forEach {
                    it(catalog)
                }
            }
        }
    }
}