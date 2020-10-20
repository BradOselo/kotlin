/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle

import junit.framework.Assert.assertTrue
import org.jetbrains.kotlin.gradle.util.modify
import org.junit.Test
import java.util.*

class IncrementalKotlinCompilationIT : BaseGradleIT() {
    val localBuildCacheSettings =
        "buildCache {\n" +
            "    local {\n" +
            "        System.out.println(\"Build dir folder stored in \" + rootDir.canonicalPath)\n" +
            "        directory = new File(rootDir, \"build-cache\")\n" +
            "    }\n" +
            "}"

    @Test
    fun compileKotlinWithGradleCache() {
        val project = createIncrementalProjectWithCache()

        project.build("build", "--build-cache") {
            assertSuccessful()
            assertTasksExecuted(":app:compileKotlin", ":lib:compileKotlin")
        }

        project.projectDir.resolve("build-cache").exists()

        //clean kotlin build cache
        project.projectDir.resolve("lib").resolve("build")
            .resolve("kotlin").resolve("compileKotlin").deleteRecursively()

        //local cache doesn't copy output
        project.build("build", "--build-cache") {
            assertSuccessful()
            assertTasksUpToDate(":app:compileKotlin")
            assertTasksGetFromCache(":lib:compileKotlin")
        }
    }

    @Test
    fun compileKotlinRestoreBuildDirAfterFail() {
        val project = createIncrementalProjectWithCache()

        project.build("build", "--build-cache") {
            assertSuccessful()
            assertTasksExecuted(":app:compileKotlin", ":lib:compileKotlin")
        }

        project.projectDir.resolve("lib").resolve("src").resolve("main").resolve("kotlin")
            .resolve("bar").resolve("B.kt").modify { it.replace("A", "UNKNOWN_CLASS") }

        project.build("build", "--build-cache") {
            assertFailed()
        }

        project.projectDir.resolve("lib").resolve("src").resolve("main").resolve("kotlin")
            .resolve("bar").resolve("B.kt").modify { it.replace("UNKNOWN_CLASS", "A") }

        //local cache doesn't copy output
        project.build("build", "--build-cache") {
            assertSuccessful()
        }
    }

    @Test
    fun testBuildHistoryRelocation() {
        val project = Project("incrementalMultiproject", workingDirRelativePath = "first/project")
        project.setupWorkingDir()

        project.build("build") {
            assertSuccessful()
            assertTasksExecuted(":app:compileKotlin", ":lib:compileKotlin")
        }

        val appKotlinCompile = project.projectDir.resolve("app/build/kotlin")
        val libKotlinCompile = project.projectDir.resolve("lib/build/kotlin")
        appKotlinCompile.exists()
        libKotlinCompile.exists()

        val project2 = Project("incrementalMultiproject", workingDirRelativePath = "second/new-project")
        project2.setupWorkingDir()

        appKotlinCompile.copyRecursively(project2.projectDir.resolve("app/build/kotlin"))
        libKotlinCompile.copyRecursively(project2.projectDir.resolve("lib/build/kotlin"))

        project.projectDir.deleteRecursively()

        project2.build("build", "-Dorg.gradle.debug=true") {
            assertSuccessful()
            assertTasksUpToDate(":app:compileKotlin", ":lib:compileKotlin")
        }

    }

    @Test
    fun testBuildHistoryPathIndependence() {
        val project = Project("incrementalMultiproject", workingDirRelativePath = "first/project")
        project.setupWorkingDir()

        project.debugKotlinDaemon("build") {
            assertSuccessful()
            assertTasksExecuted(":app:compileKotlin", ":lib:compileKotlin")
        }

        val project2 = Project("incrementalMultiproject", workingDirRelativePath = "second/new-project")
        project2.setupWorkingDir()

        project2.build("build", "-Dkotlin.daemon.jvm.options=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5007 --max-workers=1") {
            assertSuccessful()
            assertTasksExecuted(":app:compileKotlin", ":lib:compileKotlin")
        }

        //TODO move to new method, check existens
        val appProject1BuildHistory = project.projectDir.resolve("app/build/kotlin/compileKotlin/build-history.bin").readBytes()
        val appProject2BuildHistory = project2.projectDir.resolve("app/build/kotlin/compileKotlin/build-history.bin").readBytes()

        assertTrue(Arrays.equals(appProject1BuildHistory, appProject2BuildHistory))

    }

    private fun createIncrementalProjectWithCache(): Project {
        val project = Project("incrementalMultiproject")
        return setupLocalBuildCache(project)
    }

    private fun setupLocalBuildCache(project: Project): Project {
        project.setupWorkingDir()

        project.gradleSettingsScript().modify { "$it\n$localBuildCacheSettings" }
        return project
    }

    fun CompiledProject.assertTasksGetFromCache(vararg tasks: String) {
        for (task in tasks) {
            assertContains("$task FROM-CACHE")
        }
    }
}