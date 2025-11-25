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
