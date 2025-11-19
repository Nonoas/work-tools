package indi.nonoas.worktools.view.db

import indi.nonoas.worktools.dao.PageParamsDao
import indi.nonoas.worktools.pojo.dto.PageParamsDto
import indi.nonoas.worktools.pojo.vo.PageParamsVo
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.ui.UIFactory
import indi.nonoas.worktools.ui.component.ItemCloseableComboBox
import indi.nonoas.worktools.ui.component.MyAlert
import indi.nonoas.worktools.utils.DBUtil
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.StringConverter

/**
 * SQL提取工具
 * @author Nonoas
 * @date 2021/9/15
 */
class SQLExtraction private constructor() : VBox(10.0) {

    private val cbSql: ItemCloseableComboBox<PageParamsVo> =
        ItemCloseableComboBox()
    private val tfParam: TextField = TextField()
    private val taLogBefore: TextArea = TextArea()
    private val taLogAfter: TextArea = TextArea()
    private val btnExchange: Button = UIFactory.getPrimaryButton("转换")

    private fun initView() {
        this.padding = Insets(20.0)
        this.isFillWidth = true

        cbSql.apply {
            promptText = "SQL前缀"
            maxWidth = Double.MAX_VALUE
            isEditable = true
            converter = object : StringConverter<PageParamsVo>() {
                override fun toString(vo: PageParamsVo?): String {
                    if (null == vo || null == vo.paramVal) {
                        return ""
                    }
                    return vo.paramVal
                }

                override fun fromString(str: String): PageParamsVo {
                    return PageParamsVo().apply {
                        paramCode = PKEY_SQL_PREFIX
                        paramVal = str
                    }
                }

            }
            setOnItemClosed { _, listView, index ->
                // todo 不应使用runLater
                Platform.runLater {
                    val dto = listView?.items?.get(index) as PageParamsVo
                    PageParamsDao(DBUtil.getConnection()).deleteById(dto.id)
                    cbSql.items.removeAt(index)
                }
            }
        }

        initCbSqlItems()

        tfParam.apply {
            promptText = "参数前缀"
            text = "当前参数:"
        }
        taLogBefore.promptText = "SQL日志替换前"
        taLogAfter.promptText = "替换后"

        val splitPane = SplitPane(taLogBefore, taLogAfter)

        setVgrow(splitPane, Priority.ALWAYS)
        setPrefSize(800.0, 600.0)

        this.children.addAll(cbSql, tfParam, btnExchange, splitPane)

        btnExchange.onAction = EventHandler {
            taLogAfter.text = extractSQL(taLogBefore.text.trim())
            savePageParam()
        }

    }

    /**
     * 初始化sql前缀列表
     */
    private fun initCbSqlItems() {

        TaskHandler<List<PageParamsVo>>()
            .whenCall {
                PageParamsDao(DBUtil.getConnection()).getByParamCode(PKEY_SQL_PREFIX)
            }
            .andThen { vos ->
                cbSql.items.clear()
                cbSql.items.addAll(vos)
                if (cbSql.items.size > 0) {
                    cbSql.value = cbSql.items[0]
                }
            }
            .handle()
    }

    /**
     * 保存页面参数
     */
    private fun savePageParam() {
        Thread {
            val conn = DBUtil.getConnection()
            val dto = PageParamsDto().apply {
                paramCode = PKEY_SQL_PREFIX
                paramVal = cbSql.value.paramVal
                lastUseTimestamp = System.currentTimeMillis()
            }
            PageParamsDao(conn).replaceInto(dto)
            initCbSqlItems()
        }.start()
    }

    /**
     * 提取SQL
     */
    private fun extractSQL(sqlLog: String): String {
        val strs = sqlLog.split("\n")
        val sb = StringBuilder()
        for (str in strs) {
            val sqlWithParam = replaceParam(str)
            val i = sqlWithParam.indexOf(cbSql.value.paramVal)
            if (i < 0) {
                MyAlert(Alert.AlertType.ERROR, "请检查SQL开始标志 “${cbSql.value}” 是否存在于日志的每一行中！").show()
                return "转换失败！"
            }
            val sql = sqlWithParam.substring(i + cbSql.value.paramVal.length).trim()
            if (sql.isNotEmpty()) {
                sb.append("$sql;\n")
            }
        }
        return sb.toString()
    }

    private fun replaceParam(str: String): String {

        val indexOfParamSplit = str.indexOf(tfParam.text)
        if (str.indexOf(tfParam.text) < 0) {
            return str
        }

        val paramStr = str.substring(indexOfParamSplit + tfParam.length)
        val params = paramStr.split(",")
        var sql = str.substring(0, str.indexOf(tfParam.text))
        for (param in params) {
            sql = sql.replaceFirst("?", "'$param'")
        }
        return sql
    }

    companion object {
        val instance: SQLExtraction by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            SQLExtraction()
        }
        private const val PKEY_SQL_PREFIX = "SQLExtraction\$sqlPrefix"
    }

    init {
        initView()
    }


}