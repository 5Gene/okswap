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

import com.android.build.api.dsl.*
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

typealias AndroidExtension = CommonExtension<
        ApplicationBuildFeatures,
        ApplicationBuildType,
        ApplicationDefaultConfig,
        ApplicationProductFlavor,
        ApplicationAndroidResources>

val Project.android
    get(): AndroidExtension? = extensions.findByName("android") as? AndroidExtension

