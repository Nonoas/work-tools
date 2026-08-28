package io.github.nonoas.worktools.platform.view

import github.nonoas.jfx.flat.ui.control.Switch
import io.github.nonoas.worktools.platform.common.CommonInsets
import io.github.nonoas.worktools.platform.dao.FuncSettingDao
import io.github.nonoas.worktools.platform.ext.FuncPaneFactory
import io.github.nonoas.worktools.platform.ext.Plugin
import io.github.nonoas.worktools.platform.ext.PluginLoader
import io.github.nonoas.worktools.platform.ext.PluginManager
import io.github.nonoas.worktools.platform.pojo.vo.FuncSettingVo
import io.github.nonoas.worktools.platform.ui.TaskHandler
import io.github.nonoas.worktools.platform.ui.UIFactory
import io.github.nonoas.worktools.platform.ui.component.BaseStage
import io.github.nonoas.worktools.platform.ui.component.MyAlert
import io.github.nonoas.worktools.platform.utils.DBUtil
import io.github.nonoas.worktools.platform.utils.DBUtil.withTransaction
import javafx.beans.property.BooleanProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Modality
import java.io.File
import java.util.*

class PluginSettingStage : BaseStage() {

    private data class PluginInstallResult(
        val installedPlugins: List<String> = emptyList(),
        val errorMessage: String? = null
    )

    private var vos: List<FuncSettingVo> = emptyList()

    private val listView = ListView<FuncSettingVo>()

    private val btnInstall = UIFactory.getPrimaryButton("安装插件").apply {
        onAction = EventHandler { installPluginArchive() }
        tooltip = Tooltip("选择插件 zip 包，自动安装到 plugins 目录")
    }

    private val btnRefresh = Button("扫描外部插件").apply {
        onAction = EventHandler { reloadExternalPlugins() }
        tooltip = Tooltip("扫描目录: ${PluginLoader.getExternalPluginsDir()}")
    }

    private fun initView() {
        stage.width = 420.0
        stage.height = 500.0

        val btnApply = UIFactory.getPrimaryButton("应用").apply {
            onAction = EventHandler { onApply() }
        }

        val btnCancel = Button("取消").apply {
            onAction = EventHandler { onCancel() }
        }

        val hBox = HBox(10.0, btnInstall, btnRefresh, btnApply, btnCancel).apply {
            padding = CommonInsets.PADDING_T20
            alignment = Pos.CENTER_RIGHT
        }

        val root = VBox(listView, hBox).apply {
            padding = Insets(40.0, 10.0, 20.0, 10.0)
        }

        setContentView(root)
        configureListView()
        loadPlugins()
    }

    private fun configureListView() {
        listView.setCellFactory {
            object : ListCell<FuncSettingVo>() {
                private val label = Label()
                private val toggle = Switch()
                private val spacer = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }
                private val box = HBox(10.0, label, spacer, toggle).apply {
                    alignment = Pos.CENTER_LEFT
                }

                private var boundProperty: BooleanProperty? = null

                init {
                    itemProperty().addListener { _, oldItem, newItem ->
                        if (oldItem != null) {
                            boundProperty?.let { toggle.selectedProperty().unbindBidirectional(it) }
                            boundProperty = null
                        }

                        if (newItem == null) {
                            graphic = null
                        } else {
                            label.text = newItem.getFuncName()
                            boundProperty = newItem.enableFlagProperty()
                            toggle.selectedProperty().bindBidirectional(boundProperty)
                            graphic = box
                        }
                    }
                }

                override fun updateItem(item: FuncSettingVo?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        text = null
                        graphic = null
                    } else {
                        text = null
                        label.text = item.getFuncName()
                        if (graphic == null) {
                            graphic = box
                        }
                    }
                }
            }
        }
    }

    private fun loadPlugins() {
        TaskHandler<List<FuncSettingVo>>()
            .whenCall {
                val settingMap = FuncSettingDao().getAll().associate { it.funcCode to it.isEnableFlag }
                vos = buildFuncSettings(PluginManager.getAll(), settingMap)
                vos
            }
            .andThen { data ->
                listView.items = FXCollections.observableArrayList(data)
            }
            .handle()
    }

    /**
     * 根据插件扩展构建设置窗口展示的功能列表。
     *
     * 主界面展示的是 `FuncPaneFactory` 提供的功能卡片，而不是插件描述文件
     * 本身。一个插件可能注册多个功能，例如平台插件同时包含 JDK 版本管理、
     * SQL 转换、待办和文件编码，因此设置窗口必须把插件展开成具体功能项。
     * 每一项使用 `FuncPaneFactory.getCode()` 作为持久化编码，保证设置窗口、
     * 主界面卡片、全局搜索和面板路由使用同一个功能标识。
     *
     * @param plugins 所有已加载插件，包括当前被禁用的插件，确保旧配置仍可展示并重新启用
     * @param settingMap 从 `func_setting` 读取的启用状态，优先按功能编码匹配，同时兼容旧插件 id
     * @return 去重后的功能设置项，顺序保持插件发现顺序和插件内部扩展声明顺序
     */
    private fun buildFuncSettings(
        plugins: List<Plugin>,
        settingMap: Map<String, Boolean>
    ): List<FuncSettingVo> {
        val result = LinkedHashMap<String, FuncSettingVo>()
        plugins.forEach { plugin ->
            plugin.getExtensionByType(FuncPaneFactory::class.java).forEach { function ->
                result.putIfAbsent(
                    function.getCode(),
                    FuncSettingVo(
                        function.getCode(),
                        function.getName(),
                        resolveFuncEnabled(plugin, function, settingMap)
                    )
                )
            }
        }
        return result.values.toList()
    }

    /**
     * 解析单个功能在设置窗口中应显示的开关状态。
     *
     * 新版本按 `FuncPaneFactory.getCode()` 保存配置，使同一个插件下的多个功能
     * 可以独立控制。旧版本可能按插件 id 保存，例如
     * `io.github.nonoas.worktools.platform`；只有当功能级记录不存在时，才使用
     * 这个旧值作为兜底。若功能级和插件级记录都不存在，则使用插件当前运行态，
     * 也就是插件加载后的默认启用状态。
     *
     * @param plugin 功能所属插件
     * @param function 设置行对应的功能工厂
     * @param settingMap 从 `func_setting` 读取的持久化启用状态
     * @return 开关控件应展示的启用状态
     */
    private fun resolveFuncEnabled(
        plugin: Plugin,
        function: FuncPaneFactory,
        settingMap: Map<String, Boolean>
    ): Boolean {
        return settingMap[function.getCode()]
            ?: settingMap[plugin.id]
            ?: plugin.isEnabled
    }

    private fun onApply() {
        TaskHandler<Int?>()
            .whenCall {
                var result = 0
                DBUtil.withTransaction {
                    val dao = FuncSettingDao()
                    result += dao.deleteAll()
                    result += Arrays.stream(dao.insertBatch(vos)).sum()
                }
                if (result != 0) result else null
            }
            .andThen {
                PluginManager.applyFunctionEnableStates(vos.associate { vo -> vo.getFuncCode() to vo.isEnableFlag() })
                close()
                MainStage.instance?.reInit()
            }
            .handle()
    }

    private fun reloadExternalPlugins() {
        TaskHandler<Unit>()
            .whenCall {
                PluginManager.reloadExternalPlugins()
                val enableStates = FuncSettingDao().getAll().associate { it.funcCode to it.isEnableFlag }
                PluginManager.applyFunctionEnableStates(enableStates)
            }
            .andThen {
                loadPlugins()
                MainStage.instance?.reInit()
            }
            .handle()
    }

    private fun installPluginArchive() {
        val archive = choosePluginArchive() ?: return

        TaskHandler<PluginInstallResult>()
            .whenCall {
                try {
                    val installedPlugins = PluginLoader.installPluginArchive(archive.toPath())
                    PluginManager.reloadExternalPlugins()
                    val enableStates = FuncSettingDao().getAll().associate { it.funcCode to it.isEnableFlag }
                    PluginManager.applyFunctionEnableStates(enableStates)
                    PluginInstallResult(installedPlugins = installedPlugins)
                } catch (e: Exception) {
                    PluginInstallResult(errorMessage = e.message ?: "插件安装失败")
                }
            }
            .andThen { result ->
                if (result.errorMessage != null) {
                    MyAlert(Alert.AlertType.ERROR, result.errorMessage).showAndWait()
                    return@andThen
                }

                loadPlugins()
                MainStage.instance?.reInit()
                val installed = result.installedPlugins.joinToString(", ").ifBlank { archive.name }
                MyAlert(Alert.AlertType.INFORMATION, "插件安装完成: $installed").showAndWait()
            }
            .handle()
    }

    private fun choosePluginArchive(): File? {
        return FileChooser().apply {
            title = "选择插件 zip 包"
            extensionFilters.add(FileChooser.ExtensionFilter("Plugin Zip", "*.zip"))
        }.showOpenDialog(stage)
    }

    private fun onCancel() {
        close()
    }

    init {
        setTitle("插件设置")
        stage.apply {
            isResizable = false
            isAlwaysOnTop = true
            width = 420.0
            initModality(Modality.APPLICATION_MODAL)
        }
        initView()
    }
}
