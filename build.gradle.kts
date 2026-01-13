import io.github.fvarrui.javapackager.gradle.PackageTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openjfx.gradle.JavaFXOptions

buildscript {
    repositories {
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
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.openjfx.javafxplugin") version "0.0.14"
}

val jfxVersion: String by project
val myMainClassName: String by project
val platformName: String by project

the<JavaFXOptions>().apply {
    version = jfxVersion
    modules = listOf("javafx.controls", "javafx.swing")
}

apply(plugin = "io.github.fvarrui.javapackager.plugin")

group = "indi.nonoas"
version = "1.3.3"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    the<JavaFXOptions>().apply {
        version = jfxVersion
        modules = listOf("javafx.controls", "javafx.swing")
    }


    // 非 platform 模块引入 platform
    if (project.name != platformName) {
        dependencies {
            implementation(project(":$platformName"))
        }
    }
}

application {
    mainClass.set(myMainClassName)
}

tasks.withType<JavaExec> {
    jvmArgs(
        "--add-exports=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED"
    )
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

    // 打包哪些模块：platform 必选 + 可选模块
    val modulesToInclude = listOf(":platform") + selectedModules
    println("Modules to package: $modulesToInclude")

    // 确保子模块先 build
    dependsOn(modulesToInclude.map { project(it).tasks.named("build") })

    mainClass = myMainClassName

    // Java 模块配置
    modules = listOf(
        "java.base", "java.management", "java.net.http", "java.scripting", "java.sql",
        "java.naming", "jdk.jsobject", "jdk.unsupported", "jdk.unsupported.desktop", "jdk.xml.dom"
    )

    isBundleJre = true
    isGenerateInstaller = false
    isAdministratorRequired = false

    winConfig.apply {
        isCreateZipball = true
    }
}