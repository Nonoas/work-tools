package indi.nonoas.worktools.view

import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.ui.UIFactory
import indi.nonoas.worktools.dao.PageParamsDao
import indi.nonoas.worktools.pojo.dto.PageParamsDto
import indi.nonoas.worktools.ui.component.ItemCloseableComboBox
import indi.nonoas.worktools.utils.DBUtil
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.StringConverter

/**
 * @author Nonoas
 * @date 2021/9/4
 */
class TextReplacePane private constructor() : VBox(16.0) {

    private val textArea = TextArea()
    private val cbFrom =
        ItemCloseableComboBox<PageParamsDto>()
    private val tfTo = TextField()
    private val btnReplaceOne = UIFactory.getPrimaryButton("替换一处")
    private val btn = Button("替换全部")

    private fun initView() {
        textArea.apply {
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
            isEditable = true
        }

        setVgrow(textArea, Priority.ALWAYS)
        val lbBefore = Label("替换前：").apply {
            prefWidth = 50.0
        }
        val hbExclude = HBox(lbBefore, cbFrom)
        hbExclude.alignment = Pos.CENTER_LEFT

        cbFrom.apply {
            maxWidth = Double.MAX_VALUE
            isEditable = true
            converter = object : StringConverter<PageParamsDto>() {
                override fun toString(dto: PageParamsDto?): String {
                    if (null == dto || null == dto.paramVal) {
                        return ""
                    }
                    return dto.paramVal
                }

                override fun fromString(str: String): PageParamsDto {
                    return PageParamsDto().apply {
                        paramCode = PARAM_KEY_FROM
                        paramVal = str
                    }
                }

            }
            setOnItemClosed { _, listView, index ->
                // todo 不应使用runLater
                Platform.runLater {
                    val dto = listView?.items?.get(index) as PageParamsDto
                    PageParamsDao(DBUtil.getConnection()).deleteById(dto.id)
                    cbFrom.items.removeAt(index)
                }
            }
        }

        HBox.setHgrow(cbFrom, Priority.ALWAYS)
        val labelAfter=Label("替换后：").apply {
            prefWidth = 50.0
        }
        val hbPrefix = HBox(labelAfter, tfTo)
        hbPrefix.alignment = Pos.CENTER_LEFT
        HBox.setHgrow(tfTo, Priority.ALWAYS)
        padding = CommonInsets.PADDING_20

        initForm()
        children.addAll(hbExclude, hbPrefix)
        initBtnGroup()
        children.add(textArea)
    }

    /**
     * 初始化表单
     */
    private fun initForm() {
        initFromItems()
        if (cbFrom.items.size > 0) {
            cbFrom.value = cbFrom.items[0]
        }
    }

    /**
     * 初始化“替换前”下拉框选项
     */
    private fun initFromItems() {
        val conn = DBUtil.getConnection()
        val ps = conn.prepareStatement("select * from PAGE_PARAMS where param_code=?")
        ps.setString(1, PARAM_KEY_FROM)
        val rs = ps.executeQuery()

        val items = cbFrom.items
        val value = cbFrom.value
        items.clear()
        cbFrom.value = value
        while (rs.next()) {
            val dto = PageParamsDto().apply {
                id = rs.getLong("id")
                paramCode = rs.getString("param_code")
                paramVal = rs.getString("param_val")
            }
            cbFrom.items.add(dto)
        }
        rs.close()
    }

    /**
     * 生成按钮组
     */
    private fun initBtnGroup() {
        val hBox = HBox(10.0)

        // 替换一处
        btnReplaceOne.apply {
            styleClass.add("primary")
            onAction = EventHandler {
                if (null == cbFrom.value?.paramVal || null == tfTo.text) {
                    return@EventHandler
                }
                textArea.text = textArea.text.replaceFirst(cbFrom.value.paramVal, tfTo.text)
                saveForm()
            }
        }

        // 替换全部
        btn.onAction = EventHandler {
            if (null == cbFrom.value?.paramVal || null == tfTo.text) {
                return@EventHandler
            }
            textArea.text = textArea.text.replace(cbFrom.value.paramVal, tfTo.text)
            saveForm()
        }

        hBox.children.addAll(btnReplaceOne, btn)
        children.add(hBox)
    }

    /**
     * 保存页面参数
     */
    private fun saveForm() {
        Platform.runLater(Runnable {
            synchronized(Unit) {
                val conn = DBUtil.getConnection()
                val ps = conn.prepareStatement(
                    """
                        insert into page_params(param_code, param_val)
                        select ? param_code,? param_val from dual
                        where not exists(select 1 from PAGE_PARAMS where param_code=? and param_val=?)
                    """
                )
                DBUtil.executeUpdate(ps, PARAM_KEY_FROM, cbFrom.value.paramVal, PARAM_KEY_FROM, cbFrom.value.paramVal)
            }
            initFromItems()
        })
    }

    companion object {
        //同步代码块
        //对外提供获取实例对象的方法
        //声明私有静态对象，用volatile修饰
        @Volatile
        var instance: TextReplacePane? = null
            get() {
                if (field != null) return field
                //同步代码块
                synchronized(TextReplacePane::class.java) {
                    if (field == null) {
                        field = TextReplacePane()
                    }
                }
                return field
            }
            private set

        private const val PARAM_KEY_FROM = "from"
        private const val PARAM_KEY_TO = "to"

    }

    //私有构造器
    init {
        initView()
    }
}