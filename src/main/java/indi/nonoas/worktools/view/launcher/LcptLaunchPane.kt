package indi.nonoas.worktools.view.launcher

import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.ui.UIFactory
import indi.nonoas.worktools.utils.DesktopUtil
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.io.File
import java.util.*

/**
 * .bat的快速执行程序
 *
 * @author Nonoas
 * @date 2021/9/4
 */
class LcptLaunchPane(spacing: Double) : VBox(spacing) {
    private val launcherMap = HashMap<String?, Launcher>()
    private val cBoxes: MutableList<CheckBox> = LinkedList()

    // UI 组件 beg
    private val button = UIFactory.getPrimaryButton("启动")
    private val shutBtn = Button("关闭")
    private val textArea = TextArea()
    //UI 组件 end

    private constructor() : this(16.0) {
        initView()
    }

    /**
     * 启动按钮事件
     */
    private val handler = EventHandler { _: ActionEvent? ->
        for (cb in cBoxes) {
            if (!cb.isSelected) {
                continue
            }
            launcherMap[cb.text]!!.launch(textArea)
        }
    }

    /**
     * 启动按钮事件
     */
    private val shutHandler = EventHandler { _: ActionEvent? ->
        for (cb in cBoxes) {
            if (!cb.isSelected) {
                continue
            }
            launcherMap[cb.text]!!.shutDown(textArea)
        }
    }

    private fun initView() {
        val file = File("func/launchers")
        val fileNames = file.list()
        this.isFillWidth = true
        var launcher: Launcher
        if (fileNames != null) {
            for (name in fileNames) {
                val launcherPath = "func${File.separator}${file.name}${File.separator}$name"
                launcher = Launcher(launcherPath)

                val launcherName = launcher.name

                val cb = CheckBox(launcherName).apply {
                    alignment = Pos.CENTER_LEFT
                    maxWidth = Double.MAX_VALUE
                }
                HBox.setHgrow(cb, Priority.ALWAYS)

                val linkToPath = Hyperlink("打开目录").apply {
                    onAction = EventHandler {
                        DesktopUtil.open(File(launcherPath))
                        tooltip = Tooltip(launcherPath)
                        alignment = Pos.CENTER
                    }
                }

                cBoxes.add(cb)
                // 将启动器的引用存入map
                launcherMap[launcherName] = launcher

                val hBox = HBox(20.0).apply {
                    alignment = Pos.CENTER_LEFT
                    children.addAll(cb, linkToPath)
                }
                children.add(hBox)
            }
        }
        setPrefSize(340.0, 360.0)
        padding = CommonInsets.PADDING_20
        textArea.isEditable = false

        // 设置样式
        shutBtn.styleClass.add("danger")

        // 设置按钮监听
        button.onAction = handler
        shutBtn.onAction = shutHandler

        // 设置文本框监听，用实时将文本框滚动到文末
        textArea.textProperty()
            .addListener { _, _, _ -> textArea.appendText("") }
        children.addAll(
            HBox(10.0, button, shutBtn),
            textArea
        )
        setVgrow(textArea, Priority.ALWAYS)
    }

    companion object {
        //同步代码块
        //对外提供获取实例对象的方法
        @Volatile
        var instance: LcptLaunchPane? = null
            get() {
                if (field != null) return field
                //同步代码块
                synchronized(LcptLaunchPane::class.java) {
                    if (field == null) {
                        field = LcptLaunchPane()
                    }
                }
                return field
            }
            private set
    }
}