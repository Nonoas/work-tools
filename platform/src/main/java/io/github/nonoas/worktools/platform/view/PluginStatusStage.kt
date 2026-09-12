package io.github.nonoas.worktools.platform.view

import github.nonoas.jfx.flat.ui.theme.Styles
import io.github.nonoas.worktools.platform.common.CommonInsets
import io.github.nonoas.worktools.platform.ui.component.BaseStage
import javafx.beans.binding.Bindings
import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

class PluginStatusStage(
    private val mainStage: MainStage
) : BaseStage() {

    private val listView = ListView<MainFuncPane.OpenFuncPage>()

    private fun initView() {
        stage.width = 520.0
        stage.height = 420.0
        stage.isResizable = false
        stage.isAlwaysOnTop = true

        val title = Label("插件状态").apply {
            style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        }

        val summary = Label().apply {
            textProperty().bind(
                Bindings.size(mainStage.getOpenFuncPages()).asString("运行中 %d 个")
            )
            style = "-fx-text-fill: -color-base-5;"
        }

        val headerSpacer = Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        val header = HBox(10.0, title, headerSpacer, summary).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(0.0, 0.0, 12.0, 0.0)
        }

        listView.apply {
            items = mainStage.getOpenFuncPages()
            placeholder = Label("当前没有打开的插件页")
            setCellFactory { PluginStatusCell(mainStage) }
        }

        val root = VBox(0.0, header, listView).apply {
            padding = CommonInsets.PADDING_20
        }

        setContentView(root)
    }

    init {
        setTitle("插件状态")
        initView()
    }

    private class PluginStatusCell(
        private val mainStage: MainStage
    ) : ListCell<MainFuncPane.OpenFuncPage>() {

        private val nameLabel = Label()
        private val codeLabel = Label().apply {
            style = "-fx-text-fill: -color-base-5; -fx-font-size: 11px;"
        }
        private val statusLabel = Label("运行中").apply {
            styleClass.addAll(
                Styles.SMALL,
                Styles.ROUNDED,
                Styles.BG_SUBTLE,
                Styles.BORDER_DEFAULT,
                Styles.TEXT_MUTED
            )
        }
        private val textBox = VBox(3.0, nameLabel, codeLabel)
        private val spacer = Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }
        private val openButton = Button("打开").apply {
            styleClass.addAll(Styles.SMALL, Styles.BUTTON_OUTLINED)
            tooltip = Tooltip("定位到该插件页")
            onAction = javafx.event.EventHandler {
                item?.let { page -> mainStage.selectOpenFuncPage(page.funcCode) }
            }
        }
        private val closeButton = Button("关闭").apply {
            styleClass.addAll(Styles.SMALL, Styles.BUTTON_OUTLINED, Styles.DANGER)
            tooltip = Tooltip("关闭该插件页并释放资源")
            onAction = javafx.event.EventHandler {
                item?.let { page -> mainStage.closeOpenFuncPage(page.funcCode) }
            }
        }
        private val row = HBox(12.0, textBox, spacer, statusLabel, openButton, closeButton).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(8.0, 4.0, 8.0, 4.0)
        }

        private var boundPage: MainFuncPane.OpenFuncPage? = null
        private val activeListener = ChangeListener<Boolean> { _, _, _ -> refreshStatus() }

        init {
            textBox.alignment = Pos.CENTER_LEFT
            HBox.setHgrow(textBox, Priority.ALWAYS)
        }

        override fun updateItem(item: MainFuncPane.OpenFuncPage?, empty: Boolean) {
            boundPage?.activeProperty()?.removeListener(activeListener)
            boundPage = null
            super.updateItem(item, empty)

            if (empty || item == null) {
                text = null
                graphic = null
                return
            }

            boundPage = item
            nameLabel.text = item.funcName
            codeLabel.text = item.funcCode
            item.activeProperty().addListener(activeListener)
            refreshStatus()
            text = null
            graphic = row
        }

        private fun refreshStatus() {
            val active = boundPage?.isActive ?: false
            statusLabel.text = if (active) "当前" else "运行中"
            openButton.isDisable = active
        }
    }
}
