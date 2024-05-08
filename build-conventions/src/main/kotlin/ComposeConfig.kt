import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * 插件引入方式
 * ```kotlin
 * apply<ProtobufConfig>()
 * ```
 */

open class ComposeConfig : Plugin<Project> {

    val androidConfigs: MutableList<AndroidExtensionConfig> = mutableListOf(
        composeConfig()
    )

    val dependenicesConfigs: MutableList<DependenicesConfig> = mutableListOf(
        composeDependenices()
    )

    private fun composeConfig(): AndroidExtension.(Project, VersionCatalog) -> Unit =
        { project: Project, vlibs: VersionCatalog ->
            buildFeatures {
                compose = true
            }
            composeOptions {
                kotlinCompilerExtensionVersion = vlibs.findVersion("androidx-compose-compiler").get().toString()
            }
        }

    private fun composeDependenices(): DependencyHandlerScope.(VersionCatalog) -> Unit =
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
            val catalog = vlibs
            target.android.apply {
                androidConfigs.forEach {
                    it(target, catalog)
                }
            }
            dependencies {
                dependenicesConfigs.forEach {
                    it(catalog)
                }
            }

            tasks.withType<KotlinCompile>().configureEach {
                kotlinOptions {
                    freeCompilerArgs += buildComposeMetricsParameters()
//                    freeCompilerArgs += stabilityConfiguration()
                    freeCompilerArgs += strongSkippingConfiguration()
                }
            }
        }
    }
}

private fun Project.buildComposeMetricsParameters(): List<String> {
    val metricParameters = mutableListOf<String>()
    val enableMetricsProvider = project.providers.gradleProperty("enableComposeCompilerMetrics")
    val relativePath = projectDir.relativeTo(rootDir)
    val buildDir = layout.buildDirectory.get().asFile
    val enableMetrics = (enableMetricsProvider.orNull == "true")
    if (enableMetrics) {
        val metricsFolder = buildDir.resolve("compose-metrics").resolve(relativePath)
        metricParameters.add("-P")
        metricParameters.add(
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + metricsFolder.absolutePath,
        )
    }

    val enableReportsProvider = project.providers.gradleProperty("enableComposeCompilerReports")
    val enableReports = (enableReportsProvider.orNull == "true")
    if (enableReports) {
        val reportsFolder = buildDir.resolve("compose-reports").resolve(relativePath)
        metricParameters.add("-P")
        metricParameters.add(
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + reportsFolder.absolutePath
        )
    }

    return metricParameters.toList()
}

private fun Project.stabilityConfiguration() = listOf(
    "-P",
    "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=${project.rootDir.absolutePath}/compose_compiler_config.conf",
)

private fun Project.strongSkippingConfiguration() = listOf(
    "-P",
    "plugin:androidx.compose.compiler.plugins.kotlin:experimentalStrongSkipping=true",
)