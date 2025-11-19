package indi.nonoas.worktools.view.db

import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.ui.UIFactory
import indi.nonoas.worktools.ui.component.MyAlert
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.SplitPane
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.util.regex.Pattern

/**
 * @author Nonoas
 * @date 2022/7/15
 */
class SQLTransfer : VBox(10.0) {

    private val btnApply = UIFactory.getPrimaryButton("应用")
    private val taFrom = TextArea().apply { promptText = "格式化前" }
    private val taTo = TextArea().apply { promptText = "格式化后" }

    init {
        padding = CommonInsets.PADDING_20
        val spInput = SplitPane(taFrom, taTo)
        setVgrow(spInput, Priority.ALWAYS)
        children.addAll(btnApply, spInput)
        initView()
    }

    private fun initView() {
        btnApply.onAction = EventHandler { onApply() }
    }

    private fun onApply() {
        val sqlList = ArrayList<String>()
        val pattern = Pattern.compile("delete.*?insert.*?;", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val matcher = pattern.matcher(taFrom.text)
        while (matcher.find()) {
            sqlList.add(matcher.group())
        }
        taTo.text = delInsert2InsertQry(sqlList)
    }

    /**
     * delete-insert 转为 insert-select
     * @param s delete-insert SQL语句集合
     */
    private fun delInsert2InsertQry(sqlList: List<String>): String {
        return sqlList.joinToString("\n") { delInsert2InsertQry(it) }
    }

    /**
     * delete-insert 转为 insert-select
     * @param s delete-insert SQL语句
     */
    private fun delInsert2InsertQry(s: String): String {
        val patternDel = Pattern.compile("delete .*?;", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val patternInsert = Pattern.compile("insert .*?;", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val matcherDel = patternDel.matcher(s)
        val matcherInsert = patternInsert.matcher(s)

        if (!(matcherInsert.find() && matcherDel.find())) {
            MyAlert(Alert.AlertType.ERROR, "SQL语句格式有误").showAndWait()
            throw Exception("SQL语句格式有误")
        }

        val sb = StringBuilder()

        // 转换 insert
        sb.append(
            matcherInsert.group()
                .replace(Regex("values.*?\\(", RegexOption.IGNORE_CASE), "select ")
                .replace(Regex("\\).*?;", RegexOption.IGNORE_CASE), " from dual")
        ).append("\n")

        // 转换 delete
        sb.append(
            matcherDel.group()
                .replace(Regex("delete", RegexOption.IGNORE_CASE), "where not exists( select 1 ")
                .replace(";", ");")
        ).append("\n")
        return sb.toString()

    }

}