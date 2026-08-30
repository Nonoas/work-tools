import groovy.lang.Closure
import io.github.fvarrui.javapackager.gradle.PackageTask
import io.github.fvarrui.javapackager.model.HeaderType
import io.github.fvarrui.javapackager.model.WindowsConfig
import io.github.fvarrui.javapackager.model.WindowsExeCreationTool
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openjfx.gradle.JavaFXOptions

buildscript {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots/")
        maven("https://mirrors.huaweicloud.com/repository/maven/")
        maven("https://maven.aliyun.com/repository/central/")
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("io.github.fvarrui:javapackager:1.7.5")
    }
}

plugins {
    java
    application
    kotlin("jvm")
    id("org.openjfx.javafxplugin")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

nexusPublishing {
    repositories {
        sonatype {
            // OSSRH was shut down on 2025-06-30. Publish via Sonatype Central's compatibility API.
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(findProperty("sonatypeUsername") as String?)
            password.set(findProperty("sonatypePassword") as String?)
        }
    }
}

val jfxVersion: String by project
val myMainClassName: String by project
val platformName: String by project
val appVersion: String by project
val platformVersion: String by project
val publishPlatform = providers.gradleProperty("publishPlatform")
    .map(String::toBoolean)
    .orElse(false)
val appDisplayName = "WorkTools"
val appExecutableName = "worktools.exe"
val winFileVersion = appVersion
    .substringBefore("-")
    .split(".")
    .map { it.toIntOrNull()?.coerceAtLeast(0)?.toString() ?: "0" }
    .let { (it + listOf("0", "0", "0", "0")).take(4).joinToString(".") }
val winIconFile = layout.projectDirectory.file("assets/windows/worktools.ico").asFile
val appJvmMinHeap = providers.gradleProperty("appJvmMinHeap").orElse("64m")
val appJvmMaxHeap = providers.gradleProperty("appJvmMaxHeap").orElse("256m")

fun appJvmArgs() = listOf(
    "-Xms${appJvmMinHeap.get()}",
    "-Xmx${appJvmMaxHeap.get()}",
    "-Djavafx.enablePreview=true",
    "--add-exports=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED"
)

fun windowsConfig(action: WindowsConfig.() -> Unit): Closure<WindowsConfig> =
    object : Closure<WindowsConfig>(Unit) {
        @Suppress("unused")
        fun doCall(): WindowsConfig {
            val config = delegate as WindowsConfig
            config.action()
            return config
        }
    }

the<JavaFXOptions>().apply {
    version = jfxVersion
    modules = listOf("javafx.controls", "javafx.swing")
}

apply(plugin = "io.github.fvarrui.javapackager.plugin")

group = "io.github.nonoas"
version = if (publishPlatform.get()) platformVersion else appVersion

java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.openjfx.javafxplugin")

    java {
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(23))
        }
    }

    kotlin {
        jvmToolchain(23)
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget("23"))
        }
    }

    the<JavaFXOptions>().apply {
        version = jfxVersion
        modules = listOf("javafx.controls", "javafx.swing", "javafx.graphics")
    }

    // 非 platform 模块引入 platform
    if (project.name != platformName) {
        dependencies {
            compileOnly(project(":$platformName"))
        }
    }
}

application {
    mainClass.set(myMainClassName)
    applicationDefaultJvmArgs = appJvmArgs()
}

// 命令行指定要打包的可选模块
val selectedModules = listOf(
    ":modules:boot-launcher",
    ":modules:recent-touch",
    ":modules:boot-website",
    ":modules:windows-util",
    ":modules:stock-monitor",
)
println("Selected optional modules: $selectedModules")

dependencies {
    implementation(project(":$platformName"))
    selectedModules.forEach {
        implementation(project(it))
    }
}

tasks.named<JavaExec>("run") {
    dependsOn(":platform:classes")
    mainClass.set(myMainClassName)
}

// 统一打包 thin jar + libs
tasks.register<PackageTask>("packageMyApp") {
    dependsOn(tasks.clean)

    displayName = appDisplayName
    appDescription = "$appDisplayName desktop application"
    appName = "worktools"
    organizationName = "nonoas"

    vmArgs = appJvmArgs()

    // 打包哪些模块：platform 必选 + 可选模块
    val modulesToInclude = listOf(":platform") + selectedModules
    println("Modules to package: $modulesToInclude")

    // 确保子模块先 build
    dependsOn(modulesToInclude.map { project(it).tasks.named("build") })

    mainClass = myMainClassName

    // Java 模块配置
    modules = listOf(
        "java.base",
        "java.management",
        "java.net.http",
        "java.scripting",
        "java.sql",
        "java.naming",
        "jdk.jsobject",
        "jdk.unsupported",
        "jdk.unsupported.desktop",
        "jdk.xml.dom",
        "jdk.crypto.ec"
    )

    isBundleJre = true
    isGenerateInstaller = false
    isAdministratorRequired = false
    isCreateZipball = true

    winConfig(windowsConfig {
        headerType = HeaderType.gui
        exeCreationTool = WindowsExeCreationTool.winrun4j
        icoFile = winIconFile

        fileDescription = appDisplayName
        productName = appDisplayName
        internalName = "worktools"
        originalFilename = appExecutableName
        companyName = "nonoas"

        fileVersion = winFileVersion
        productVersion = winFileVersion
        txtFileVersion = appVersion
        txtProductVersion = appVersion

        shortcutName = appDisplayName
    })

    doLast {
        val runnableJarName = "worktools-$appVersion-runnable.jar"
        val packageDir = layout.buildDirectory.dir("worktools").get().asFile
        val iniFile = packageDir.resolve("worktools.ini")

        delete(packageDir.resolve(runnableJarName))
        if (iniFile.exists()) {
            iniFile.writeText(
                iniFile.readLines()
                    .filterNot { it == "classpath.2=$runnableJarName" }
                    .joinToString(System.lineSeparator()) + System.lineSeparator()
            )
        }
    }
}
