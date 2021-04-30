/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.session

import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform

class FirModuleInfoBasedModuleData(
    val moduleInfo: ModuleInfo,
    override val session: FirSession
) : FirModuleData() {
    override val name: Name
        get() = moduleInfo.name
    override val dependencies: List<FirModuleData> = moduleInfo.dependencies().map { FirModuleInfoBasedModuleData(it, session) }
    override val dependsOnDependencies: List<FirModuleData> = moduleInfo.expectedBy.map { FirModuleInfoBasedModuleData(it, session) }
    override val friendDependencies: List<FirModuleData> = moduleInfo.modulesWhoseInternalsAreVisible().map { FirModuleInfoBasedModuleData(it, session) }
    override val platform: TargetPlatform
        get() = moduleInfo.platform

    override fun equals(other: Any?): Boolean {
        if (other !is FirModuleInfoBasedModuleData) return false
        return moduleInfo == other.moduleInfo
    }

    override fun hashCode(): Int {
        return moduleInfo.hashCode()
    }
}
