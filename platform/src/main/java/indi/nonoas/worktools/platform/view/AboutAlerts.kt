package indi.nonoas.worktools.platform.view

import indi.nonoas.worktools.platform.common.CommonInsets
import indi.nonoas.worktools.platform.ui.component.MyAlert
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

/**
 * 关于弹窗
 *
 * @author Nonoas
 * @date 2021/9/4
 */
class AboutAlerts private constructor() : MyAlert(AlertType.INFORMATION, null) {

    init {
        title = "关于"
        graphic = null
        dialogPane.apply {
            minWidth = 420.0
            prefWidth = 460.0
            content = buildContent()
        }
    }

    private fun buildContent(): VBox {
        val logo = ImageView(Image("image/logo.png")).apply {
            fitWidth = 56.0
            fitHeight = 56.0
            isPreserveRatio = true
        }

        val titleLabel = Label("WorkTools").apply {
            style = "-fx-font-size: 22px; -fx-font-weight: bold;"
        }

        val subTitleLabel = Label("常用开发辅助工具集合").apply {
            style = "-fx-text-fill: #666666; -fx-font-size: 13px;"
        }

        val header = HBox(16.0, logo, VBox(6.0, titleLabel, subTitleLabel)).apply {
            alignment = Pos.CENTER_LEFT
        }

        val descLabel = Label("聚合常见的开发效率工具，统一处理日常脚本、环境和文本类工作。").apply {
            isWrapText = true
            style = "-fx-text-fill: #444444;"
        }

        val infoGrid = GridPane().apply {
            hgap = 18.0
            vgap = 12.0
            padding = Insets(4.0, 0.0, 0.0, 0.0)
            columnConstraints.addAll(
                ColumnConstraints().apply { minWidth = 84.0 },
                ColumnConstraints().apply { hgrow = Priority.ALWAYS }
            )
        }

        buildInfoRows().forEachIndexed { index, row ->
            val keyLabel = Label(row.first).apply {
                style = "-fx-font-weight: bold; -fx-text-fill: #666666;"
            }
            val valueLabel = Label(row.second).apply {
                isWrapText = true
                maxWidth = Double.MAX_VALUE
                style = "-fx-text-fill: #111111;"
            }
            infoGrid.add(keyLabel, 0, index)
            infoGrid.add(valueLabel, 1, index)
        }

        val footer = Label("感谢使用 WorkTools。").apply {
            style = "-fx-text-fill: #666666;"
        }

        return VBox(16.0, header, descLabel, Separator(), infoGrid, footer).apply {
            padding = CommonInsets.PADDING_20
        }
    }

    private fun buildInfoRows(): List<Pair<String, String>> {
        return listOf(
            "当前版本" to resolveVersion(),
            "创建作者" to "Nonoas",
            "参与贡献" to "WorkTools 项目组",
            "技术栈" to "JavaFX / Kotlin / Java"
        )
    }

    private fun resolveVersion(): String {
        return sequenceOf(
            System.getProperty("worktools.version"),
            AboutAlerts::class.java.`package`?.implementationVersion,
            AboutAlerts::class.java.module?.descriptor?.rawVersion()?.orElse(null)
        ).firstOrNull { !it.isNullOrBlank() } ?: "开发版本"
    }

    companion object {
        @Volatile
        var instance: AboutAlerts? = null
            get() {
                if (field != null) return field
                synchronized(AboutAlerts::class.java) {
                    if (field == null) {
                        field = AboutAlerts()
                    }
                }
                return field
            }
            private set
    }
}
