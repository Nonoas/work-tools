import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

/**
 * worktools 插件开发 Convention Plugin
 *
 * 自动生成 META-INF/plugin.yml 并配置 jar 打包，
 * 插件开发者只需声明 worktools {} 配置块即可。
 */
class WorktoolsPluginConventionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // 不自动应用 kotlin 插件，让用户自己选择
        // project.plugins.apply("java-library")
        // project.plugins.apply("org.jetbrains.kotlin.jvm")

        // 检查用户是否已应用必要的插件
        val hasJava = project.plugins.hasPlugin("java-library") || project.plugins.hasPlugin("java")
        val hasKotlin = project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")

        if (!hasJava && !hasKotlin) {
            project.logger.warn("WorktoolsPlugin: No java or kotlin plugin applied. Please apply 'java-library' or 'kotlin' plugin first.")
        }

        // 创建 DSL 扩展
        val ext = project.extensions.create<WorktoolsPluginExtension>("worktools")

        project.afterEvaluate {
            // 校验必填字段
            require(ext.id.isNotBlank()) { "worktools.id is required" }
            require(ext.name.isNotBlank()) { "worktools.name is required" }

            val resolvedPlatformVersion = ext.platformVersion.ifBlank {
                project.findProperty("worktoolsPlatformVersion")?.toString()
                    ?: project.findProperty("platformVersion")?.toString()
                    ?: ""
            }
            val platformDependencyNotation = if (resolvedPlatformVersion.isNotBlank()) {
                "${ext.platformGroup}:${ext.platformArtifact}:$resolvedPlatformVersion"
            } else {
                null
            }

            if (ext.addPlatformDependency && platformDependencyNotation != null) {
                project.dependencies.add("compileOnly", platformDependencyNotation)
            }

            // 注册生成 plugin.yml 的 Task
            val generateDescriptor = project.tasks.register("generatePluginDescriptor") {
                group = "worktools"
                description = "Generate META-INF/plugin.yml"

                val outputDir = project.layout.buildDirectory.dir("generated/plugin/META-INF")
                outputs.dir(outputDir)

                doLast {
                    val ymlFile = outputDir.get().file("plugin.yml").asFile
                    ymlFile.parentFile.mkdirs()

                    val content = buildPluginYml(ext)
                    ymlFile.writeText(content)
                    project.logger.lifecycle("Generated plugin.yml for: ${ext.id}")
                }
            }

            // 配置 jar Task
            project.tasks.named<Jar>("jar").configure {
                dependsOn(generateDescriptor)
                from(project.layout.buildDirectory.dir("generated/plugin"))
                archiveBaseName.set(ext.id)
                doLast {
                    val jarFile = archiveFile.get().asFile
                    java.util.jar.JarFile(jarFile).use { jar ->
                        requireNotNull(jar.getJarEntry("META-INF/plugin.yml")) {
                            "META-INF/plugin.yml not found in plugin jar!"
                        }
                    }
                }
            }

            val hostProjectDir = ext.hostProjectDir?.takeIf { it.isNotBlank() }?.let {
                project.layout.projectDirectory.dir(it)
            }
            val sandboxDir = project.layout.projectDirectory.dir(ext.sandboxDir)
            val sandboxPluginsDir = sandboxDir.dir(ext.hostPluginsDir)
            val installedPluginDir = if (hostProjectDir != null) {
                hostProjectDir.dir(ext.hostPluginsDir).dir(ext.id)
            } else {
                sandboxPluginsDir.dir(ext.id)
            }

            val runtimeClasspath = project.configurations.findByName("runtimeClasspath")

            project.tasks.register<Copy>("copyPluginRuntimeLibs") {
                group = "worktools"
                description = "Copy plugin runtime dependencies to the plugin libs directory"

                onlyIf { runtimeClasspath != null }
                into(
                    if (hostProjectDir != null) {
                        hostProjectDir.dir(ext.hostPluginsDir).dir("libs")
                    } else {
                        sandboxPluginsDir.dir("libs")
                    }
                )

                if (runtimeClasspath != null) {
                    from(runtimeClasspath.incoming.artifactView { }.files.filter { it.extension == "jar" })
                }
            }

            project.tasks.register<Zip>("packagePluginZip") {
                group = "distribution"
                description = "Package the plugin jar and runtime dependencies into an installable zip"
                dependsOn("jar")

                archiveBaseName.set(ext.id)
                archiveVersion.set(ext.version)
                destinationDirectory.set(project.layout.buildDirectory.dir("distributions"))

                from(project.tasks.named<Jar>("jar").flatMap { it.archiveFile }) {
                    into(ext.id)
                }

                if (runtimeClasspath != null) {
                    from(runtimeClasspath.incoming.artifactView { }.files.filter { it.extension == "jar" }) {
                        into("libs")
                    }
                }
            }

            if (hostProjectDir != null) {
                project.tasks.register<Copy>("copyToPlugins") {
                    group = "worktools"
                    description = "Copy built plugin jar to the configured host application plugins directory"
                    dependsOn("jar")

                    from(project.tasks.named<Jar>("jar").flatMap { it.archiveFile })
                    into(installedPluginDir)
                }

                project.tasks.register<Exec>("runPlugin") {
                    group = "application"
                    description = "Copy plugin jar and run the configured host worktools application"
                    dependsOn("copyToPlugins", "copyPluginRuntimeLibs")

                    val isWindows = System.getProperty("os.name", "")
                        .contains("Windows", ignoreCase = true)
                    val gradlew = if (isWindows) "gradlew.bat" else "gradlew"
                    val gradlewFile = hostProjectDir.file(gradlew).asFile

                    doFirst {
                        require(hostProjectDir.asFile.exists()) {
                            "Configured hostProjectDir does not exist: ${hostProjectDir.asFile}"
                        }
                        require(gradlewFile.exists()) {
                            "Gradle wrapper not found in hostProjectDir: $gradlewFile"
                        }
                    }

                    workingDir(hostProjectDir.asFile)

                    commandLine(
                        gradlewFile.absolutePath,
                        ext.hostRunTask
                    )
                }
            } else {
                val worktoolsHostRuntime = project.configurations.maybeCreate("worktoolsHostRuntime").apply {
                    isCanBeConsumed = false
                    isCanBeResolved = true
                    isVisible = false
                }

                require(platformDependencyNotation != null) {
                    "worktools.platformVersion (or Gradle property worktoolsPlatformVersion/platformVersion) is required for runPlugin without hostProjectDir"
                }
                project.dependencies.add(worktoolsHostRuntime.name, platformDependencyNotation)

                project.tasks.register<Copy>("copyToPlugins") {
                    group = "worktools"
                    description = "Copy built plugin jar to the temporary runPlugin sandbox"
                    dependsOn("jar")

                    from(project.tasks.named<Jar>("jar").flatMap { it.archiveFile })
                    into(installedPluginDir)
                }

                project.tasks.register<JavaExec>("runPlugin") {
                    group = "application"
                    description = "Run the plugin in a temporary worktools host built from the platform dependency"
                    dependsOn("copyToPlugins", "copyPluginRuntimeLibs")

                    workingDir(sandboxDir.asFile)
                    classpath(worktoolsHostRuntime)
                    mainClass.set(ext.hostMainClass)
                    systemProperty("javafx.enablePreview", "true")
                    systemProperty("worktools.plugins.dir", sandboxPluginsDir.asFile.absolutePath)
                    jvmArgs("--add-exports=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED")
                }
            }
        }
    }

    private fun buildPluginYml(ext: WorktoolsPluginExtension): String {
        val lines = mutableListOf<String>()
        lines.add("id: \"${ext.id}\"")
        lines.add("name: \"${ext.name}\"")
        lines.add("version: \"${ext.version}\"")

        if (ext.mainClass.isNotBlank()) {
            lines.add("mainClass: \"${ext.mainClass}\"")
        }
        if (ext.description.isNotBlank()) {
            lines.add("description: \"${ext.description}\"")
        }

        if (ext.depends.isNotEmpty()) {
            lines.add("depends:")
            ext.depends.forEach { dep ->
                lines.add("  - \"$dep\"")
            }
        }

        if (ext.extensionPoints.isNotEmpty()) {
            lines.add("extensions:")
            ext.extensionPoints.forEach { (interfaceName, impls) ->
                lines.add("  \"$interfaceName\":")
                impls.forEach { impl ->
                    lines.add("    - \"$impl\"")
                }
            }
        }

        return lines.joinToString("\n") + "\n"
    }
}
