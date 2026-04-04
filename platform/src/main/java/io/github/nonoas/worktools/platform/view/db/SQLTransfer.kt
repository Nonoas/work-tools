package io.github.nonoas.worktools.platform.view.db

import github.nonoas.jfx.flat.ui.concurrent.TaskHandler
import io.github.nonoas.worktools.platform.dao.PageParamsDao
import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.pojo.dto.PageParamsDto
import io.github.nonoas.worktools.platform.pojo.vo.PageParamsVo
import io.github.nonoas.worktools.platform.utils.DBUtil
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.text.iterator

class SQLTransfer : FuncPane() {

    private val keyInput: String = "SQLTransfer\$input"
    private val keyOutput: String = "SQLTransfer\$onput"

    private val root = VBox()

    private val fullSqlPathField = TextField()
    private val incrSqlPathField = TextField()
    private val keyFieldsField = TextField()

    private val modeComboBox = ComboBox<String>().apply {
        items.addAll("Delete-Insert", "Insert-Select-Not-Exists")
        value = "Delete-Insert"
    }
    private val logArea = TextArea().apply {
        isEditable = false
        promptText = "运行日志..."
    }

    private fun initForm() {
        TaskHandler<List<PageParamsVo>>()
            .whenCall {
                PageParamsDao.getByParamCode(keyInput)
            }
            .andThen {
                if (it.isEmpty()) {
                    return@andThen
                }
                val get = it[0]
                fullSqlPathField.text = get.paramVal
            }
            .handle()

        TaskHandler<List<PageParamsVo>>()
            .whenCall {
                PageParamsDao.getByParamCode(keyOutput)
            }
            .andThen {
                if (it.isEmpty()) {
                    return@andThen
                }
                val get = it[0]
                incrSqlPathField.text = get.paramVal
            }
            .handle()

        PageParamsDao.getByParamCode(keyOutput)

    }


    /**
     * 保存页面参数
     */
    private fun savePageParam() {
        TaskHandler.backRun {
            var dto = PageParamsDto().apply {
                paramCode = keyInput
                paramVal = fullSqlPathField.text
                lastUseTimestamp = System.currentTimeMillis()
            }
            PageParamsDao.replaceInto(dto)

            dto = PageParamsDto().apply {
                paramCode = keyOutput
                paramVal = incrSqlPathField.text
                lastUseTimestamp = System.currentTimeMillis()
            }
            PageParamsDao.replaceInto(dto)
        }
    }

    private fun chooseInputFile() {
        val fileChooser = FileChooser().apply {
            title = "选择原始 SQL 文件"
            extensionFilters.add(FileChooser.ExtensionFilter("SQL Files", "*.sql"))
        }
        val file = fileChooser.showOpenDialog(root.scene.window)
        if (file != null) {
            fullSqlPathField.text = file.absolutePath
            // 自动推测输出路径：原文件名 + _incr.sql
            if (incrSqlPathField.text.isEmpty()) {
                incrSqlPathField.text = File(file.parent, "generated_increment.sql").absolutePath
            }
        }
    }

    private fun chooseOutputPath() {
        // 提供两个选项：选文件夹或选文件
        val alert = Alert(
            Alert.AlertType.CONFIRMATION, "请选择输出保存方式",
            ButtonType("指定文件夹"), ButtonType("指定具体文件"), ButtonType.CANCEL
        )

        val result = alert.showAndWait()
        if (result.get().text == "指定文件夹") {
            val dir = DirectoryChooser().apply { title = "选择输出目录" }.showDialog(root.scene.window)
            if (dir != null) incrSqlPathField.text = dir.absolutePath
        } else if (result.get().text == "指定具体文件") {
            val file = FileChooser().apply {
                title = "另存为"
                extensionFilters.add(FileChooser.ExtensionFilter("SQL Files", "*.sql"))
            }.showSaveDialog(root.scene.window)
            if (file != null) incrSqlPathField.text = file.absolutePath
        }
    }

    private fun handleConversion() {
        savePageParam()

        val inputPath = fullSqlPathField.text
        var outputPath = incrSqlPathField.text
        val keyFields = keyFieldsField.text.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

        if (inputPath.isEmpty() || outputPath.isEmpty()) {
            log("❌ 错误: 请完整填写输入和输出路径")
            return
        }

        // 逻辑处理：如果是文件夹，自动补全文件名
        val outFile = File(outputPath)
        if (outFile.isDirectory) {
            outputPath = File(outFile, "increment_result.sql").absolutePath
        }

        try {
            processSql(inputPath, outputPath, keyFields, modeComboBox.value)
            log("✅ 成功! 结果已保存至: $outputPath")
        } catch (e: Exception) {
            log("❌ 失败: ${e.message}")
        }
    }

    private fun processSql(input: String, output: String, keyFields: List<String>, mode: String) {
        val content = File(input).readText(StandardCharsets.UTF_8)
        val stmts = content.split(";").map { it.trim() }.filter { it.lowercase().startsWith("insert") }
        val resultSql = mutableListOf<String>()

        for (stmt in stmts) {
            try {
                val info = parseInsert(stmt)
                val kvMap = info.columns.map { it.lowercase() }.zip(info.values).toMap()

                val whereParts = keyFields.mapNotNull { key ->
                    kvMap[key]?.let { value ->
                        val originalCol = info.columns.find { it.equals(key, ignoreCase = true) } ?: key
                        "$originalCol = $value"
                    }
                }

                if (whereParts.size < keyFields.size) {
                    log("⚠️ 跳过: 无法找齐所有主键字段 ${keyFields} 在语句中")
                    continue
                }

                val whereClause = whereParts.joinToString(" AND ")

                when (mode) {
                    "Delete-Insert" -> {
                        resultSql.add("DELETE FROM ${info.table} WHERE $whereClause;")
                        resultSql.add("$stmt;")
                    }

                    "Insert-Select-Not-Exists" -> {
                        resultSql.add(
                            """
                            INSERT INTO ${info.table} (${info.columns.joinToString(", ")})
                            SELECT ${info.values.joinToString(", ")} FROM DUAL
                            WHERE NOT EXISTS (SELECT 1 FROM ${info.table} WHERE $whereClause);
                        """.trimIndent()
                        )
                    }
                }
                resultSql.add("")
            } catch (e: Exception) {
                log("❌ 解析行失败: ${e.message}")
            }
        }
        File(output).writeText(resultSql.joinToString("\n"), StandardCharsets.UTF_8)
    }

    private fun parseInsert(stmt: String): SqlInfo {
        val regex =
            Regex("(?i)insert\\s+into\\s+(\\w+)\\s*\\((.*?)\\)\\s*values\\s*\\((.*)\\)", RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(stmt) ?: throw Exception("不符合 INSERT 结构")
        val (table, colStr, valStr) = match.destructured
        return SqlInfo(table, colStr.split(",").map { it.trim() }, splitValues(valStr))
    }

    private fun splitValues(valuesStr: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inString = false
        for (c in valuesStr) {
            when {
                c == '\'' -> {
                    inString = !inString; sb.append(c)
                }

                c == ',' && !inString -> {
                    result.add(sb.toString().trim()); sb.setLength(0)
                }

                else -> sb.append(c)
            }
        }
        result.add(sb.toString().trim())
        return result
    }

    private fun log(msg: String) = logArea.appendText("$msg\n")

    data class SqlInfo(val table: String, val columns: List<String>, val values: List<String>)

    override fun getRootView(): Parent {
        root.spacing = 15.0
        root.padding = Insets(20.0)

        val grid = GridPane().apply {
            hgap = 10.0
            vgap = 15.0
            alignment = Pos.TOP_LEFT

            // --- 输入文件选择 ---
            add(Label("输入 SQL 文件:"), 0, 0)
            val inputHBox = HBox(5.0, fullSqlPathField.apply { HBox.setHgrow(this, Priority.ALWAYS) },
                Button("选择").apply { setOnAction { chooseInputFile() } })
            add(inputHBox, 1, 0)

            // --- 输出路径选择 ---
            add(Label("输出路径/文件:"), 0, 1)
            val outputHBox = HBox(5.0, incrSqlPathField.apply { HBox.setHgrow(this, Priority.ALWAYS) },
                Button("选择").apply { setOnAction { chooseOutputPath() } })
            add(outputHBox, 1, 1)

            add(Label("主键字段:"), 0, 2)
            add(keyFieldsField.apply { promptText = "多个主键请用逗号分隔" }, 1, 2)

            add(Label("生成模式:"), 0, 3)
            add(modeComboBox, 1, 3)

            // 设置列约束，让第二列自动拉伸
            val col2 = ColumnConstraints().apply { hgrow = Priority.ALWAYS }
            columnConstraints.addAll(ColumnConstraints(), col2)
        }

        val runBtn = Button("执行转换").apply {
            prefWidth = 200.0
            style = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;"
            setOnAction { handleConversion() }
        }

        root.children.addAll(grid, runBtn, Label("运行日志:"), logArea)
        VBox.setVgrow(logArea, Priority.ALWAYS)

        initForm()
        return root
    }

    override fun dispose() {
        root.children.clear()
        logArea.clear()
    }
}
