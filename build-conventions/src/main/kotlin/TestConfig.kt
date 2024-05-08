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
        with(target) {
            val projectName = name
//            ApplicationAndroidComponentsExtension -> ApplicationExtension
//            findByType 不存在返回空 getByType 不存在抛异常
            println("TestConfig $projectName ApplicationExtension =================== ${extensions.findByType<ApplicationExtension>()}")
            println("TestConfig $projectName LibraryExtension =================== ${extensions.findByType<LibraryExtension>()}")
            println("TestConfig $projectName ApplicationAndroidComponentsExtension =================== ${extensions.findByType<ApplicationAndroidComponentsExtension>()} ================")
            println("TestConfig $projectName LibraryAndroidComponentsExtension =================== ${extensions.findByType<LibraryAndroidComponentsExtension>()} ================")
            println("TestConfig $projectName BaseAppModuleExtension =================== ${extensions.findByType<BaseAppModuleExtension>()} ================")
            println("TestConfig $projectName getByName android =================== ${extensions.findByName("android")} ================")
            println("TestConfig $projectName getByName android =================== ${extensions.getByName("android").javaClass}")
            println("TestConfig $projectName getByName android ========= ${android.javaClass}")
        }
    }
}