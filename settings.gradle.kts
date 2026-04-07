pluginManagement {
    val kotlinVersion: String by settings
    val javafxPluginVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }

    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.openjfx.javafxplugin") version javafxPluginVersion
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/")
        maven("https://mirrors.huaweicloud.com/repository/maven/")
        maven("https://maven.aliyun.com/repository/central")
    }
}

// 开启特性
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "worktools"

// 包含平台模块
val platformName: String by settings
include(platformName)

// 自动包含 modules 目录下的子模块
file("modules").listFiles()?.filter { it.isDirectory }?.forEach { dir ->
    include(":modules:${dir.name}")
}
