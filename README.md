# WorkTools

> **注意：** 功能通用化，涉及个人及集体隐私的文件切勿上传！

## 项目架构

WorkTools 是一个基于 JavaFX 的桌面应用框架，采用 Gradle 多模块架构。

### 技术栈

- **JDK**: 17
- **Kotlin**: 1.9.0
- **JavaFX**: 21.0.3
- **构建工具**: Gradle (Kotlin DSL)

### 模块结构

```
worktools/
├── platform/              # 核心平台模块（必选）
│   ├── 提供基础 API、UI 组件
│   ├── 插件系统
│   └── 公共依赖
├── modules/               # 功能模块（可选）
│   ├── boot-launcher/     # 启动器
│   ├── boot-website/      # 常用网站快捷启动
│   ├── recent-touch/      # 最近访问文件
│   ├── stock-monitor/     # 股票监控
│   └── windows-util/      # Windows 工具集
└── assets/                # 资源文件
```

## 快速开始

### 运行项目

```bash
./gradlew run
```

### 打包应用

打包完整应用（包含 JRE）：

```bash
./gradlew packageMyApp
```

打包结果位于 `build/launch/` 目录下。

## 开发指南

### 添加新模块

1. 在 `modules/` 目录下创建新文件夹（如 `my-module`）
2. 创建 `build.gradle.kts` 文件：

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.openjfx.javafxplugin")
}

dependencies {
    // 自动引入 platform 模块
}
```

3. 在根目录 `build.gradle.kts` 中的 `selectedModules` 列表添加模块：

```kotlin
val selectedModules = listOf(
    // ... 已有模块
    ":modules:my-module",
)
```

### 开发功能模块

功能模块自动依赖 `platform` 模块，可直接使用平台提供的 API：

```kotlin
// 创建工具面板
class MyToolPanel : VBox() {
    init {
        // 使用 platform 提供的组件
    }
}
```

### 插件开发

#### 1. 插件配置

在插件项目 `resources/META-INF/` 目录下创建 `plugin.yml`：

```yaml
version: 1.0.0
id: indi.myplugin.example
name: 示例插件
extensions:
  io.github.nonoas.worktools.platform.ext.FuncPaneFactory:
    - indi.myplugin.MyToolPanel
```

配置说明：
- `version`: 插件版本号
- `id`: 插件唯一标识符（全限定名格式）
- `name`: 插件显示名称
- `extensions`: 扩展点注册，key 为扩展点接口全限定名，value 为实现类列表

#### 2. 插件主类

实现 `FuncPaneFactory` 接口创建功能面板：

```kotlin
class MyToolPanel : FuncPaneFactory {
    override fun getPane(): Pane {
        return VBox().apply {
            // 自定义面板内容
        }
    }
    
    override fun getName(): String {
        return "我的工具"
    }
}
```

#### 3. 打包与部署

```bash
# 插件单独打包（不包含 platform）
./gradlew jar

# 将插件 jar 放入主程序 plugins/ 目录
```

## 配置说明

### gradle.properties

```properties
jfxVersion=21.0.3           # JavaFX 版本
kotlinVersion=1.9.0         # Kotlin 版本
myMainClassName=io.github.nonoas.worktools.platform.MainKt  # 主类
platformName=platform       # 平台模块名称
```

## 常用命令

| 命令 | 说明 |
|------|------|
| `./gradlew run` | 运行应用 |
| `./gradlew build` | 构建所有模块 |
| `./gradlew packageMyApp` | 打包完整应用 |
| `./gradlew :platform:publishToMavenLocal` | 发布 platform 到本地 Maven |