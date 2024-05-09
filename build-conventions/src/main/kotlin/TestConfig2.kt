import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.plugins.PluginManager
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonToolOptions

class TestConfig2 : AbsAndroidConfig() {
    override fun pluginConfigs(): PluginManager.() -> Unit = {

    }

    override fun androidExtensionConfig(): AndroidExtension.(Project, VersionCatalog) -> Unit = { _, _ ->
    }

    override fun kotlinOptionsConfig(): KotlinCommonToolOptions.(Project) -> Unit = { _ ->
    }

    override fun dependenciesConfig(): DependencyHandlerScope.(VersionCatalog) -> Unit = { _ ->
    }
}