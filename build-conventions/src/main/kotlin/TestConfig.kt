import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

class TestConfig : Plugin<Project> {
    override fun apply(target: Project) {
        println("========================================= start $this ${target.name}".green)
        with(target) {
            val projectName = name
//            ApplicationAndroidComponentsExtension -> ApplicationExtension
//            findByType 不存在返回空 getByType 不存在抛异常
            println("$projectName ApplicationExtension ===================== ${extensions.findByType<ApplicationExtension>()}")
            println("$projectName LibraryExtension ========================= ${extensions.findByType<LibraryExtension>()}")
            println("$projectName ApplicationAndroidComponentsExtension ==== ${extensions.findByType<ApplicationAndroidComponentsExtension>()}")
            println("$projectName LibraryAndroidComponentsExtension ======== ${extensions.findByType<LibraryAndroidComponentsExtension>()}")
            println("$projectName BaseAppModuleExtension =================== ${extensions.findByType<BaseAppModuleExtension>()}")
            println("$projectName getByName android ======================== ${extensions.findByName("android")}")
            println("$projectName getByName android ======================== ${extensions.getByName("android").javaClass}")
            println("$projectName getByName android ======================== ${android.javaClass}")
        }
        println("============================================== end $this ${target.name}".green)
    }
}