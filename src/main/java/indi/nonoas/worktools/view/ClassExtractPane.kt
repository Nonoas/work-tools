package indi.nonoas.worktools.view

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.ui.UIFactory
import indi.nonoas.worktools.ui.component.MyAlert
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import org.apache.log4j.Logger
import java.io.File
import java.util.*

/**
 * @author Nonoas
 * @date 2021/9/4
 */
class ClassExtractPane private constructor() : VBox(16.0) {

    private val logger = Logger.getLogger(ClassExtractPane::class.java)

    /**
     * 文本框：输出路径
     */
    private val tfOutPutPath = TextField()
    private val btnOutPutBrow = Button("选择")

    private val textArea = TextArea()

    private val btnExtract = UIFactory.getPrimaryButton("提取")

    private fun initView() {
        tfOutPutPath.promptText = "选择输出路径"
        HBox.setHgrow(tfOutPutPath, Priority.ALWAYS)

        btnOutPutBrow.onAction = EventHandler { onOutPutBrow() }

        textArea.apply {
            promptText = "粘贴java文件路径至此，以换行分隔"
            isEditable = true
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
        }
        setVgrow(textArea, Priority.ALWAYS)

        val hbExclude = HBox(10.0, tfOutPutPath, btnOutPutBrow)
        hbExclude.alignment = Pos.CENTER_LEFT

        padding = CommonInsets.PADDING_20

        children.addAll(hbExclude, textArea)

        // 初始化按钮组
        initBtnGroup()
    }

    /**
     * 选择输出路径
     */
    private fun onOutPutBrow() {
        val chooser = DirectoryChooser()
        val file = chooser.showDialog(MainStage.instance) ?: return
        tfOutPutPath.text = file.absolutePath
    }

    /**
     * 生成按钮组
     */
    private fun initBtnGroup() {
        btnExtract.onAction = EventHandler { onExtract() }

        val hBox = HBox(10.0).apply {
            alignment = Pos.CENTER_RIGHT
        }

        hBox.children.addAll(btnExtract)
        children.add(hBox)
    }

    /**
     * 提取class文件到指定目录
     */
    private fun onExtract() {
        var text = textArea.text
        if (StrUtil.isBlank(text)) {
            return
        } else {
            text = text.replace("/", "\\")
                .replace(".java", ".class")
                .replace("src\\indi.nonoas.worktools.main\\java", "target\\classes")

        }
        val paths = text.split("\n").toMutableList()
        // 追加匿名类文件
        appendAnonymousClassFile(paths)
        extractClassFile(paths)
    }

    private fun appendAnonymousClassFile(paths: MutableList<String>) {
        val list = ArrayList<String>()
        var file: File
        for (path in paths) {
            file = File(path)
            val acFilePaths = getAnonymousClassFiles(file).map { it.absolutePath }
            list.addAll(acFilePaths)
        }
        paths.addAll(list)
    }

    private fun getAnonymousClassFiles(file: File): List<File> {
        if (!file.isFile || !file.absolutePath.endsWith(".class")) return Collections.emptyList()
        val regex = Regex(file.name.replace(".class", "\\$.*\\.class"))
        return file.parentFile.listFiles()!!
            .filter { it.isFile && regex.matches(it.name) }
    }

    /**
     * 提取Class文件到输出目录
     * @param paths class文件路径数组
     */
    private fun extractClassFile(paths: List<String>) {
        val dir = tfOutPutPath.text
        var newPath: String
        for (path in paths) {
            newPath = dir + path.substring(path.lastIndexOf("\\"), path.length)
            if (!checkExist(path)) {
                return
            }
            FileUtil.copy(path, newPath, true)
        }
        MyAlert(Alert.AlertType.INFORMATION, "提取成功").showAndWait()
    }

    /**
     * 校验文件是否存在
     */
    private fun checkExist(path: String): Boolean {
        if (!FileUtil.exist(path)) {
            MyAlert(Alert.AlertType.WARNING, "文件${path}不存在").showAndWait()
            return false
        }
        return true
    }

    companion object {
        //同步代码块
        //对外提供获取实例对象的方法
        //声明私有静态对象，用volatile修饰
        @Volatile
        var instance: ClassExtractPane? = null
            get() {
                if (field != null) return field
                //同步代码块
                synchronized(ClassExtractPane::class.java) {
                    if (field == null) {
                        field = ClassExtractPane()
                    }
                }
                return field
            }
            private set
    }

    //私有构造器
    init {
        initView()
    }
}