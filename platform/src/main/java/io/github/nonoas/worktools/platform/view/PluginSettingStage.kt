package io.github.nonoas.worktools.platform.view

import github.nonoas.jfx.flat.ui.control.Switch
import io.github.nonoas.worktools.platform.common.CommonInsets
import io.github.nonoas.worktools.platform.dao.FuncSettingDao
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
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Modality
import java.io.File
import java.util.Arrays

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
                val allPlugin = PluginManager.getAll()
                vos = allPlugin.map {
                    FuncSettingVo.covertFrom(it).apply {
                        val enabled = settingMap[it.id] ?: it.isEnabled
                        setEnableFlag(enabled)
                    }
                }
                vos
            }
            .andThen { data ->
                listView.items = FXCollections.observableArrayList(data)
            }
            .handle()
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
                PluginManager.applyEnableStates(vos.associate { vo -> vo.getFuncCode() to vo.isEnableFlag() })
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
                PluginManager.applyEnableStates(enableStates)
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
                    PluginManager.applyEnableStates(enableStates)
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
