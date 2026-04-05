import java.io.Serializable

/**
 * worktools 插件开发 DSL 配置扩展
 *
 * 用法:
 * ```kotlin
 * worktools {
 *     id = "my-plugin"
 *     name = "My Plugin"
 *     version = "1.0.0"
 *     mainClass = "com.example.MyPluginService"
 *     description = "插件描述"
 *     platformVersion = "1.0.0"
 *     dependsOnPlugin("platform")
 *     extension("com.example.SomeExtension", "com.example.impl.SomeImpl")
 *     hostProject("../worktools")
 * }
 * ```
 */
open class WorktoolsPluginExtension : Serializable {

    /** 插件唯一标识符（必填） */
    var id: String = ""

    /** 插件名称（必填） */
    var name: String = ""

    /** 插件版本号 */
    var version: String = "1.0.0"

    /** 插件主类全限定名，必须实现 PluginService 接口 */
    var mainClass: String = ""

    /** 插件描述 */
    var description: String = ""

    /** 依赖的其他插件 id 列表 */
    var depends: MutableList<String> = mutableListOf()

    /** 扩展点映射：接口全限定名 -> 实现类全限定名列表 */
    var extensionPoints: MutableMap<String, MutableList<String>> = mutableMapOf()

    /** 宿主 worktools 工程根目录。不配置时不注册 copyToPlugins / runPlugin。 */
    var hostProjectDir: String? = null

    /** 宿主工程中用于加载插件的目录名 */
    var hostPluginsDir: String = "plugins"

    /** 启动宿主工程时执行的 Gradle task */
    var hostRunTask: String = "run"

    /** 自动添加 platform API 的 compileOnly 依赖。 */
    var addPlatformDependency: Boolean = true

    /** 用于 runPlugin 临时宿主以及 compileOnly API 的 platform 依赖坐标。 */
    var platformGroup: String = "io.github.nonoas"
    var platformArtifact: String = "work-tools-platform"
    var platformVersion: String = ""

    /** 临时宿主启动主类。 */
    var hostMainClass: String = "io.github.nonoas.worktools.platform.MainKt"

    /** 无宿主源码时 runPlugin 使用的工作目录。 */
    var sandboxDir: String = "build/runPlugin"

    fun dependsOnPlugin(vararg pluginIds: String) {
        depends.addAll(pluginIds)
    }

    fun extension(interfaceName: String, vararg implementations: String) {
        if (implementations.isEmpty()) {
            return
        }
        val impls = extensionPoints.getOrPut(interfaceName) { mutableListOf() }
        impls.addAll(implementations)
    }

    fun hostProject(projectDir: String, pluginsDir: String = "plugins", runTask: String = "run") {
        hostProjectDir = projectDir
        hostPluginsDir = pluginsDir
        hostRunTask = runTask
    }

    fun targetPlatform(
        version: String,
        group: String = "io.github.nonoas",
        artifact: String = "work-tools-platform",
        mainClass: String = "io.github.nonoas.worktools.platform.MainKt"
    ) {
        platformVersion = version
        platformGroup = group
        platformArtifact = artifact
        hostMainClass = mainClass
    }
}
