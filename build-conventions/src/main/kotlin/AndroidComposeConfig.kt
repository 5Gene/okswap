import org.gradle.api.JavaVersion
import org.gradle.api.Project

class AndroidComposeConfig : ComposeConfig() {

    override fun apply(target: Project) {
        with(target.pluginManager) {
            apply("kotlin-android")
            apply("kotlin-parcelize")
        }
        super.apply(target)
    }

}

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: AndroidExtension,
) {
    commonExtension.apply {
        compileSdk = 34

        defaultConfig {
            minSdk = 21
        }

        compileOptions {
            // Up to Java 11 APIs are available through desugaring
            // https://developer.android.com/studio/write/java11-minimal-support-table
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
            isCoreLibraryDesugaringEnabled = true
        }
    }

//    configureKotlin()

//    dependencies {
//        add("coreLibraryDesugaring", libs.findLibrary("android.desugarJdkLibs").get())
//    }
}