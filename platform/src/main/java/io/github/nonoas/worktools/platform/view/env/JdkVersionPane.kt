package io.github.nonoas.worktools.platform.view.env

import atlantafx.base.theme.Styles
import io.github.nonoas.worktools.platform.common.CommonInsets
import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.ui.TaskHandler
import io.github.nonoas.worktools.platform.ui.UIFactory
import io.github.nonoas.worktools.platform.ui.component.MyAlert
import io.github.nonoas.worktools.platform.utils.DesktopUtil
import io.github.nonoas.worktools.platform.utils.UIUtil
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import java.io.File
import java.util.Properties

/**
 * JDK 管理面板，支持 JDK 的新增、编辑、删除、启用与本地 JAVA_HOME 检测。
 */
class JdkVersionPane : FuncPane() {

    private val javaHomeKey = "JAVA_HOME"
    private val isWindows = System.getProperty("os.name").contains("windows", ignoreCase = true)

    private val root = HBox(16.0)
    private val tableView = TableView<EnvVar>()
    private val jdkVersions: ObservableList<EnvVar> = FXCollections.observableArrayList()

    private val modeLabel = Label("新增模式")
    private val pathHintLabel = Label("请选择 JDK 安装目录，例如 C:\\Program Files\\Java\\jdk-21")

    private val nameField = TextField().apply {
        promptText = "例如：Temurin 21"
    }
    private val pathField = TextField().apply {
        promptText = "请选择 JDK 根目录，而不是 bin 或 java.exe"
    }

    private val browseButton = Button("浏览")
    private val detectButton = Button("检测 JAVA_HOME")
    private val newButton = Button("新建")
    private val saveButton = UIFactory.getPrimaryButton("新增到列表")
    private val deleteButton = Button("删除")
    private val enableButton = Button("启用选中 JDK").apply {
        styleClass.add(Styles.ACCENT)
    }

    private var activeJavaHomePath: String? = null
    private var activeJavaHomeSource: String = "未检测到"
    private var initialized = false
    private var tableSelectionListenerBound = false
    private var pathHintListenerBound = false

    private data class JdkDescriptor(
        val homePath: String,
        val version: String,
        val vendor: String?,
        val suggestedName: String
    )

    private data class ActionResult(
        val success: Boolean,
        val message: String,
        val activePath: String? = null,
        val activeSource: String? = null
    )

    private data class LocalJavaHomeDetection(
        val descriptor: JdkDescriptor,
        val source: String
    )

    private fun createTableView(): TableView<EnvVar> {
        val nameColumn = TableColumn<EnvVar, String>("名称").apply {
            cellValueFactory = PropertyValueFactory<EnvVar, String>("desc")
            prefWidth = 140.0
        }
        val versionColumn = TableColumn<EnvVar, String>("版本").apply {
            cellValueFactory = javafx.util.Callback { cellData ->
                ReadOnlyStringWrapper(readJdkDescriptor(cellData.value.content.orEmpty()).version)
            }
            prefWidth = 120.0
        }
        val pathColumn = TableColumn<EnvVar, String>("安装目录").apply {
            cellValueFactory = PropertyValueFactory<EnvVar, String>("content")
            prefWidth = 360.0
        }
        val statusColumn = TableColumn<EnvVar, String>("状态").apply {
            cellValueFactory = javafx.util.Callback { cellData ->
                ReadOnlyStringWrapper(if (isActiveJdk(cellData.value)) "启用" else "")
            }
            prefWidth = 90.0
        }

        tableView.columns.setAll(nameColumn, versionColumn, pathColumn, statusColumn)
        tableView.items = jdkVersions
        tableView.columnResizePolicy = TableView.UNCONSTRAINED_RESIZE_POLICY
        tableView.placeholder = Label("还没有保存任何 JDK。点击右侧“检测 JAVA_HOME”或手动新增。")
        if (!tableSelectionListenerBound) {
            tableView.selectionModel.selectedItemProperty().addListener { _, _, selected ->
                selected?.let { fillForm(it) }
            }
            tableSelectionListenerBound = true
        }
        tableView.setRowFactory {
            object : javafx.scene.control.TableRow<EnvVar>() {
                override fun updateItem(item: EnvVar?, empty: Boolean) {
                    super.updateItem(item, empty)
                    style = if (!empty && item != null && isActiveJdk(item)) {
                        "-fx-background-color: rgba(30, 136, 229, 0.10);"
                    } else {
                        ""
                    }
                }
            }
        }
        VBox.setVgrow(tableView, Priority.ALWAYS)
        return tableView
    }

    private fun createEditorPane(): VBox {
        modeLabel.style = "-fx-font-size: 13px; -fx-font-weight: bold;"
        pathHintLabel.isWrapText = true
        pathHintLabel.style = "-fx-text-fill: #666666;"

        browseButton.setOnAction { chooseJdkDirectory() }
        detectButton.setOnAction { detectAndFillJavaHome() }
        newButton.setOnAction { switchToCreateMode() }
        saveButton.setOnAction { saveCurrentJdk() }
        deleteButton.setOnAction { deleteSelectedJdk() }
        enableButton.setOnAction { enableSelectedJdk() }

        if (!pathHintListenerBound) {
            pathField.textProperty().addListener { _, _, newValue ->
                updatePathHint(newValue)
            }
            pathHintListenerBound = true
        }

        val title = Label("JDK 配置").apply {
            style = "-fx-font-size: 16px; -fx-font-weight: bold;"
        }

        val nameLabel = Label("名称")
        val pathLabel = Label("安装目录")
        val pathBox = HBox(CommonInsets.SPACING_1, pathField, browseButton).apply {
            alignment = Pos.CENTER_LEFT
            HBox.setHgrow(pathField, Priority.ALWAYS)
        }
        val actionBox = HBox(CommonInsets.SPACING_1, detectButton, saveButton).apply {
            alignment = Pos.CENTER_LEFT
        }

        return VBox(
            12.0,
            title,
            modeLabel,
            nameLabel,
            nameField,
            pathLabel,
            pathBox,
            pathHintLabel,
            actionBox
        ).apply {
            prefWidth = 380.0
            minWidth = 320.0
        }
    }

    private fun createListPane(): VBox {
        val title = Label("已保存的 JDK").apply {
            style = "-fx-font-size: 16px; -fx-font-weight: bold;"
        }
        val desc = Label("选中列表项后可直接修改、删除或启用。").apply {
            style = "-fx-text-fill: #666666;"
        }
        val actionBar = HBox(CommonInsets.SPACING_1, newButton, deleteButton, enableButton).apply {
            alignment = Pos.CENTER_LEFT
        }

        return VBox(12.0, title, desc, createTableView(), actionBar).apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }
    }

    private fun saveCurrentJdk() {
        val editingItem = tableView.selectionModel.selectedItem
        val descriptor = try {
            validateForm(editingItem)
        } catch (ex: Exception) {
            UIUtil.error(ex.message)
            return
        }

        val now = System.currentTimeMillis()
        val envVar = (editingItem ?: EnvVar()).apply {
            name = javaHomeKey
            desc = nameField.text.trim()
            content = descriptor.homePath
            createTimestamp = editingItem?.createTimestamp ?: now
            modTimestamp = now
        }

        runCatching {
            EnvVarDao.save(envVar)
            reloadJdkVersions(descriptor.homePath)
            MyAlert(
                Alert.AlertType.INFORMATION,
                if (editingItem == null) "JDK 已添加到列表。" else "JDK 配置已更新。"
            ).showAndWait()
        }.onFailure {
            UIUtil.error(
                buildString {
                    append("保存 JDK 失败。")
                    append("\n名称: ").append(nameField.text.trim())
                    append("\n路径: ").append(descriptor.homePath)
                    append("\n原因: ").append(it.message ?: it.javaClass.simpleName)
                }
            )
        }
    }

    private fun deleteSelectedJdk() {
        val selectedItem = tableView.selectionModel.selectedItem
        if (selectedItem == null) {
            UIUtil.warn("请先在左侧列表中选中一个 JDK，然后再执行删除。")
            return
        }

        val result = Alert(
            Alert.AlertType.CONFIRMATION,
            "确定删除 JDK “${selectedItem.desc.orEmpty()}” 吗？\n路径: ${selectedItem.content.orEmpty()}",
            ButtonType.OK,
            ButtonType.CANCEL
        ).showAndWait()
        if (result.orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return
        }

        runCatching {
            val id = selectedItem.id
                ?: throw IllegalStateException("当前 JDK 记录没有主键，无法执行删除。请重新打开面板后重试。")
            EnvVarDao.deleteById(id)
            reloadJdkVersions()
            switchToCreateMode()
        }.onFailure {
            UIUtil.error(
                buildString {
                    append("删除 JDK 失败。")
                    append("\n名称: ").append(selectedItem.desc.orEmpty())
                    append("\n路径: ").append(selectedItem.content.orEmpty())
                    append("\n原因: ").append(it.message ?: it.javaClass.simpleName)
                }
            )
        }
    }

    private fun enableSelectedJdk() {
        val selectedItem = tableView.selectionModel.selectedItem
        if (selectedItem == null) {
            UIUtil.warn("请先在左侧列表中选中一个 JDK，然后再点击“启用选中 JDK”。")
            return
        }

        val selectedPath = selectedItem.content.orEmpty()
        TaskHandler<ActionResult>()
            .whenCall {
                runCatching {
                    val descriptor = validateJdkHome(selectedPath)
                    val commandResult = DesktopUtil.changeEnvVar(javaHomeKey, descriptor.homePath)
                    val output = commandResult.standardOutput.ifBlank { "setx 未返回额外输出。" }
                    ActionResult(
                        success = true,
                        message = buildString {
                            append("JAVA_HOME 已更新为:\n").append(descriptor.homePath)
                            append("\n\n命令输出:\n").append(output)
                            append("\n\n说明: 新环境变量只会在新启动的终端、IDE 或命令行窗口中生效。")
                        },
                        activePath = descriptor.homePath,
                        activeSource = "本次启用（需重启新进程后完全生效）"
                    )
                }.getOrElse {
                    ActionResult(
                        success = false,
                        message = buildString {
                            append("启用 JDK 失败。")
                            append("\n名称: ").append(selectedItem.desc.orEmpty())
                            append("\n路径: ").append(selectedPath)
                            append("\n原因: ").append(it.message ?: it.javaClass.simpleName)
                        }
                    )
                }
            }
            .andThen { result ->
                if (!result.success) {
                    UIUtil.error(result.message)
                    return@andThen
                }
                result.activePath?.let {
                    activeJavaHomePath = it
                    activeJavaHomeSource = result.activeSource ?: activeJavaHomeSource
                }
                tableView.refresh()
                MyAlert(Alert.AlertType.INFORMATION, result.message).showAndWait()
            }
            .handle()
    }

    private fun detectAndFillJavaHome() {
        runCatching {
            detectLocalJavaHome()
        }.onSuccess { detection ->
            activeJavaHomePath = detection.descriptor.homePath
            activeJavaHomeSource = detection.source

            val existing = jdkVersions.firstOrNull { samePath(it.content.orEmpty(), detection.descriptor.homePath) }
            if (existing != null) {
                tableView.selectionModel.select(existing)
                tableView.scrollTo(existing)
                updatePathHint(existing.content.orEmpty())
                return@onSuccess
            }

            switchToCreateMode()
            nameField.text = detection.descriptor.suggestedName
            pathField.text = detection.descriptor.homePath
            updatePathHint(detection.descriptor.homePath)
        }.onFailure {
            UIUtil.error(it.message)
        }
    }

    private fun reloadJdkVersions(selectPath: String? = null) {
        val items = EnvVarDao.queryByName(javaHomeKey)
        jdkVersions.setAll(items)
        if (selectPath.isNullOrBlank()) {
            tableView.selectionModel.clearSelection()
            tableView.refresh()
            return
        }
        val matched = jdkVersions.firstOrNull { samePath(it.content.orEmpty(), selectPath) }
        if (matched != null) {
            tableView.selectionModel.select(matched)
            tableView.scrollTo(matched)
        }
        tableView.refresh()
    }

    private fun fillForm(envVar: EnvVar) {
        modeLabel.text = "编辑模式"
        saveButton.text = "保存修改"
        nameField.text = envVar.desc.orEmpty()
        pathField.text = envVar.content.orEmpty()
        updatePathHint(envVar.content.orEmpty())
    }

    private fun switchToCreateMode() {
        tableView.selectionModel.clearSelection()
        modeLabel.text = "新增模式"
        saveButton.text = "新增到列表"
        nameField.clear()
        pathField.clear()
        updatePathHint("")
    }

    private fun chooseJdkDirectory() {
        val chooser = DirectoryChooser().apply {
            title = "选择 JDK 安装目录"
            val currentPath = pathField.text.trim()
            if (currentPath.isNotBlank()) {
                val file = File(currentPath)
                if (file.exists()) {
                    initialDirectory = if (file.isDirectory) file else file.parentFile
                }
            }
        }
        val selectedDir = chooser.showDialog(root.scene?.window) ?: return
        pathField.text = selectedDir.absolutePath
    }

    private fun validateForm(editingItem: EnvVar?): JdkDescriptor {
        val displayName = nameField.text.trim()
        if (displayName.isBlank()) {
            throw IllegalArgumentException("名称不能为空。\n建议填写便于识别的名字，例如 Temurin 17、Oracle JDK 21。")
        }

        val descriptor = validateJdkHome(pathField.text)
        val duplicatedName = jdkVersions.firstOrNull {
            it.id != editingItem?.id && it.desc.orEmpty().equals(displayName, ignoreCase = true)
        }
        if (duplicatedName != null) {
            throw IllegalArgumentException(
                "名称“$displayName”已存在。\n已存在路径: ${duplicatedName.content.orEmpty()}\n请使用不同的名称，避免列表中难以区分。"
            )
        }

        val duplicatedPath = jdkVersions.firstOrNull {
            it.id != editingItem?.id && samePath(it.content.orEmpty(), descriptor.homePath)
        }
        if (duplicatedPath != null) {
            throw IllegalArgumentException(
                "路径已经存在于列表中。\n重复路径: ${descriptor.homePath}\n已存在名称: ${duplicatedPath.desc.orEmpty()}"
            )
        }
        return descriptor.copy(suggestedName = displayName)
    }

    private fun validateJdkHome(rawPath: String): JdkDescriptor {
        val trimmedPath = rawPath.trim().trim('"')
        if (trimmedPath.isBlank()) {
            throw IllegalArgumentException("安装目录不能为空。\n请选择 JDK 根目录，例如 C:\\Program Files\\Java\\jdk-21。")
        }

        val normalizedPath = normalizePath(trimmedPath)
        val homeDir = File(normalizedPath)
        if (!homeDir.exists()) {
            throw IllegalArgumentException("JDK 路径不存在。\n路径: $normalizedPath")
        }
        if (!homeDir.isDirectory) {
            throw IllegalArgumentException("当前路径不是目录。\n路径: $normalizedPath\n请填写 JDK 根目录，而不是具体文件。")
        }
        if (homeDir.name.equals("bin", ignoreCase = true)) {
            throw IllegalArgumentException(
                "当前路径指向 bin 目录。\n路径: $normalizedPath\n请改为上一级 JDK 根目录: ${homeDir.parentFile?.absolutePath.orEmpty()}"
            )
        }
        if (homeDir.name.equals("jre", ignoreCase = true)) {
            throw IllegalArgumentException(
                "当前路径更像是 JRE 而不是 JDK。\n路径: $normalizedPath\nJDK 目录下必须同时包含 bin/java 和 bin/javac。"
            )
        }

        val javaBinary = resolveExecutable(homeDir, "java")
        val javacBinary = resolveExecutable(homeDir, "javac")
        if (javaBinary == null || javacBinary == null) {
            val existingEntries = listOf(
                if (javaBinary != null) "bin/${javaBinary.name}" else null,
                if (File(homeDir, "release").exists()) "release" else null
            ).filterNotNull().joinToString(", ")
            throw IllegalArgumentException(
                buildString {
                    append("当前目录不是可用的 JDK 根目录。")
                    append("\n路径: ").append(normalizedPath)
                    append("\n必须存在: bin/java 和 bin/javac")
                    if (existingEntries.isNotBlank()) {
                        append("\n已检测到: ").append(existingEntries)
                    }
                    append("\n请确认你选择的是完整 JDK 安装目录，而不是 JRE、压缩包上层目录或运行时目录。")
                }
            )
        }

        val releaseProps = readReleaseProperties(homeDir)
        val version = releaseProps["JAVA_VERSION"]
            ?: releaseProps["JAVA_RUNTIME_VERSION"]
            ?: "未知版本"
        val vendor = releaseProps["IMPLEMENTOR"] ?: releaseProps["JAVA_VENDOR"]
        val suggestedName = listOfNotNull(vendor?.takeIf { it.isNotBlank() }, version.takeIf { it.isNotBlank() })
            .joinToString(" ")
            .ifBlank { "JDK" }

        return JdkDescriptor(
            homePath = normalizedPath,
            version = version,
            vendor = vendor,
            suggestedName = suggestedName
        )
    }

    private fun detectLocalJavaHome(): LocalJavaHomeDetection {
        val errors = mutableListOf<String>()
        val envJavaHome = System.getenv(javaHomeKey)?.trim().orEmpty()
        if (envJavaHome.isNotBlank()) {
            runCatching {
                val descriptor = validateJdkHome(envJavaHome)
                return LocalJavaHomeDetection(descriptor, "来自系统环境变量 JAVA_HOME")
            }.onFailure {
                errors += "系统环境变量 JAVA_HOME 指向 $envJavaHome，但校验失败: ${it.message}"
            }
        } else {
            errors += "系统环境变量 JAVA_HOME 未设置。"
        }

        val runtimeJavaHome = System.getProperty("java.home")?.trim().orEmpty()
        if (runtimeJavaHome.isNotBlank()) {
            val candidate = normalizeRuntimeHome(runtimeJavaHome)
            runCatching {
                val descriptor = validateJdkHome(candidate)
                return LocalJavaHomeDetection(descriptor, "来自当前运行时 java.home")
            }.onFailure {
                errors += "当前运行时 java.home 指向 $runtimeJavaHome，折算目录 $candidate 校验失败: ${it.message}"
            }
        } else {
            errors += "系统属性 java.home 未提供可用路径。"
        }

        throw IllegalStateException(
            buildString {
                append("未检测到可用的本地 JDK。")
                errors.forEach { append("\n").append(it) }
                append("\n请先在系统中配置 JAVA_HOME，或手动选择一个完整的 JDK 安装目录。")
            }
        )
    }

    private fun updatePathHint(path: String) {
        if (path.isBlank()) {
            pathHintLabel.text = "请选择 JDK 安装目录，例如 C:\\Program Files\\Java\\jdk-21"
            return
        }

        pathHintLabel.text = runCatching {
            val descriptor = validateJdkHome(path)
            buildString {
                append("检测通过")
                append("，版本: ").append(descriptor.version)
                descriptor.vendor?.takeIf { it.isNotBlank() }?.let {
                    append("，发行方: ").append(it)
                }
            }
        }.getOrElse {
            val firstLine = it.message?.lineSequence()?.firstOrNull() ?: "路径校验失败"
            "路径检查: $firstLine"
        }
    }

    private fun readJdkDescriptor(path: String): JdkDescriptor {
        return runCatching {
            validateJdkHome(path)
        }.getOrElse {
            JdkDescriptor(
                homePath = path,
                version = "未知",
                vendor = null,
                suggestedName = "JDK"
            )
        }
    }

    private fun readReleaseProperties(homeDir: File): Map<String, String> {
        val releaseFile = File(homeDir, "release")
        if (!releaseFile.exists()) {
            return emptyMap()
        }

        val properties = Properties()
        releaseFile.reader().use(properties::load)
        return properties.entries.associate { entry ->
            entry.key.toString() to entry.value.toString().trim().trim('"')
        }
    }

    private fun resolveExecutable(homeDir: File, command: String): File? {
        val windowsBinary = File(homeDir, "bin/$command.exe")
        if (windowsBinary.exists()) {
            return windowsBinary
        }
        val unixBinary = File(homeDir, "bin/$command")
        return unixBinary.takeIf { it.exists() }
    }

    private fun normalizeRuntimeHome(runtimeJavaHome: String): String {
        val normalized = normalizePath(runtimeJavaHome)
        val runtimeDir = File(normalized)
        return if (runtimeDir.name.equals("jre", ignoreCase = true) && runtimeDir.parentFile?.exists() == true) {
            runtimeDir.parentFile.absolutePath
        } else {
            normalized
        }
    }

    private fun normalizePath(path: String): String {
        val trimmed = path.trim().trim('"')
        if (trimmed.isBlank()) {
            return ""
        }
        return runCatching {
            File(trimmed).canonicalFile.absolutePath
        }.getOrElse {
            File(trimmed).absoluteFile.toPath().normalize().toString()
        }
    }

    private fun isActiveJdk(envVar: EnvVar): Boolean {
        val content = envVar.content ?: return false
        return !activeJavaHomePath.isNullOrBlank() && samePath(content, activeJavaHomePath!!)
    }

    private fun samePath(left: String, right: String): Boolean {
        if (left.isBlank() || right.isBlank()) {
            return false
        }
        val leftPath = normalizePath(left)
        val rightPath = normalizePath(right)
        return if (isWindows) leftPath.equals(rightPath, ignoreCase = true) else leftPath == rightPath
    }

    /**
     * 构建并返回 JDK 管理面板。
     */
    override fun getRootView(): Parent {
        if (initialized) {
            return root
        }
        initialized = true

        root.padding = CommonInsets.PADDING_20
        root.children.setAll(createListPane(), createEditorPane())

        runCatching {
            val detection = detectLocalJavaHome()
            activeJavaHomePath = detection.descriptor.homePath
            activeJavaHomeSource = detection.source
        }.onFailure {
            activeJavaHomePath = null
            activeJavaHomeSource = "未检测到"
        }
        reloadJdkVersions(activeJavaHomePath)
        if (jdkVersions.isEmpty()) {
            switchToCreateMode()
        }
        return root
    }

    /**
     * 释放面板内持有的 UI 组件与缓存数据。
     */
    override fun dispose() {
        root.children.clear()
        jdkVersions.clear()
        tableView.columns.clear()
        tableView.items = FXCollections.observableArrayList<EnvVar>()
        activeJavaHomePath = null
        activeJavaHomeSource = "未检测到"
        initialized = false
    }
}
