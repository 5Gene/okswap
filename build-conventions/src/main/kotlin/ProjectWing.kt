/*
 * Copyright 2023 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

@file:Suppress("UNCHECKED_CAST")

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.VariantDimension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.PluginManager
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.getByType

val Project.vlibs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")


typealias AndroidExtensionConfig = AndroidExtension.(project: Project, vlibs: VersionCatalog) -> Unit
typealias DependenicesConfig = DependencyHandlerScope.(vlibs: VersionCatalog) -> Unit
typealias ApplyPluginsConfig = PluginManager.() -> Unit

//要兼容 application和library 这里的泛型必须 用*全匹配
typealias AndroidExtension = CommonExtension<*, *, *, *, *>

val Project.android
    get(): AndroidExtension? = extensions.findByName("android") as? AndroidExtension

fun VariantDimension.defineStr(name: String, value: String) {
    buildConfigField("String", name, "\"$value\"")
}

fun VariantDimension.defineBool(name: String, value: Boolean) {
    buildConfigField("boolean", name, value.toString())
}

fun VariantDimension.defineInt(name: String, value: Int) {
    buildConfigField("int", name, value.toString())
}

fun VariantDimension.defineResStr(name: String, value: String) {
    //使用方式 getResources().getString(R.string.name) 值为value
    resValue("string", name, value)
}

fun VariantDimension.defineResInt(name: String, value: String) {
    //使用方式 getResources().getInteger(R.string.name) 值为value
    resValue("integer", name, value)
}

fun VariantDimension.defineResBool(name: String, value: String) {
    //使用方式 getResources().getBoolean(R.string.name) 值为value
    resValue("bool", name, value)
}