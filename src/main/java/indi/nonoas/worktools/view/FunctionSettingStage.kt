package indi.nonoas.worktools.view

import github.nonoas.jfx.flat.ui.control.Switch
import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.dao.FuncSettingDao
import indi.nonoas.worktools.pojo.dto.FuncSettingDto
import indi.nonoas.worktools.pojo.vo.FuncSettingVo
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.ui.UIFactory
import indi.nonoas.worktools.ui.component.BaseStage
import indi.nonoas.worktools.utils.DBUtil
import indi.nonoas.worktools.utils.DBUtil.withTransaction
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Modality
import java.util.Arrays

/**
 * 功能入口：设置-功能
 *
 * @author Nonoas
 * @datetime 2022/1/22 22:33
 */
class FunctionSettingStage : BaseStage() {

    private var vos: List<FuncSettingVo> = emptyList()

    private var listView: ListView<FuncSettingVo> = ListView<FuncSettingVo>()

    private fun initView() {
        stage.width = 320.0
        stage.height = 500.0

        val btnApply = UIFactory.getPrimaryButton("应用").apply {
            onAction = EventHandler { onApply() }
        }

        val btnCancel = Button("取消").apply {
            onAction = EventHandler { onCancel() }
        }

        val hBox = HBox(10.0, btnApply, btnCancel).apply {
            padding = CommonInsets.PADDING_T20
            alignment = Pos.CENTER_RIGHT
        }

        val root = VBox(10.0, listView, hBox).apply {
            padding = Insets(40.0, 20.0, 20.0, 20.0)
        }

        setContentView(root)

        TaskHandler<List<FuncSettingVo>>()
            .whenCall {
                val settingList = FuncSettingDao().getAll()
                vos = settingList.map(FuncSettingDto::convertVo)
                vos
            }.andThen {
                val data = it
                listView.apply {
                    items = FXCollections.observableArrayList(data)

                    // 自定义每一行的显示
                    setCellFactory {
                        object : ListCell<FuncSettingVo>() {
                            private val label = Label()
                            private val toggle = Switch() // 暂时用 Switch；若有渲染问题可换成 CheckBox()
                            private val spacer = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }
                            private val box = HBox(10.0, label, spacer, toggle).apply {
                                alignment = Pos.CENTER_LEFT
                            }

                            // 保存当前绑定的 property（方便解绑）
                            private var boundProperty: javafx.beans.property.BooleanProperty? = null

                            init {
                                // 当 cell 的 item 发生变化时：先解绑旧的，再绑定新的
                                itemProperty().addListener { _, oldItem, newItem ->
                                    // 1) 解绑旧的双向绑定（如果有）
                                    if (oldItem != null) {
                                        boundProperty?.let { toggle.selectedProperty().unbindBidirectional(it) }
                                        boundProperty = null
                                    }

                                    // 2) 处理新 item（为空时清空 graphic）
                                    if (newItem == null) {
                                        graphic = null
                                    } else {
                                        label.text = newItem.getFuncName()
                                        boundProperty = newItem.enableFlagProperty() // 获取 VO 的 BooleanProperty
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
                                    text = null // 我们全部用 graphic 显示
                                    // label.text 已由 listener 设置；这里再保证一下
                                    label.text = item.getFuncName()
                                    if (graphic == null) graphic = box
                                }
                            }
                        }
                    }
                }
            }.handle()
    }

    /**
     * 点击 “应用按钮之后触发”
     */
    private fun onApply() {
        TaskHandler<Int?>()
            .whenCall {
                var result = 0
                DBUtil.withTransaction { conn ->
                    val dao = FuncSettingDao()
                    result += dao.deleteAll()
                    result += Arrays.stream(dao.insertBatch(vos)).sum()
                    return@whenCall if (result != 0) result else null
                }
            }
            .andThen {
                this.close()
                MainStage.instance?.reInit()
            }
            .handle()
    }

    private fun onCancel() {
        close()
    }

    init {
        setTitle("功能设置")
        stage.apply {
            isResizable = false
            isAlwaysOnTop = true
            width = 300.0
            initModality(Modality.APPLICATION_MODAL)
        }
        initView()
    }
}
