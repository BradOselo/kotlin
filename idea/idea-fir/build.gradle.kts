plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    compile(project(":plugins:uast-kotlin-fir"))
    compile(project(":idea:idea-frontend-fir"))
    compile(project(":idea:formatter"))
    compile(intellijDep())
    compile(intellijCoreDep())
    implementation(project(":idea:idea-fir-fe10-binding"))

// <temp>
    compile(project(":idea:idea-core"))
    compile(project(":idea"))
// </temp>

    testCompile(toolsJar())
    testCompile(projectTests(":idea"))
    testCompile(projectTests(":compiler:tests-common"))
    testCompile(projectTests(":idea:idea-test-framework"))
    testCompile(projectTests(":idea:idea-frontend-fir"))
    testCompile(project(":kotlin-test:kotlin-test-junit"))
    testCompile(commonDep("junit:junit"))
    testCompile(projectTests(":idea:idea-frontend-independent"))

    testCompileOnly(intellijDep())
    testRuntime(intellijDep())
    testImplementation(project(":idea:idea-fir-fe10-binding"))

    compile(intellijPluginDep("java"))
}

sourceSets {
    "main" { projectDefault() }
    "test" { projectDefault() }
}

projectTest(parallel = true) {
    dependsOn(":dist")
    workingDir = rootDir
    val useFirIdeaPlugin = kotlinBuildProperties.useFirIdeaPlugin
    doFirst {
        if (!useFirIdeaPlugin) {
            error("Test task in the module should be executed with -Pidea.fir.plugin=true")
        }
    }
}

testsJar()
