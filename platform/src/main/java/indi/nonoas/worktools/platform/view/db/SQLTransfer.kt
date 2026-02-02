package indi.nonoas.worktools.platform.view.db


import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.io.File
import java.nio.charset.StandardCharsets

class SQLTransfer : VBox() {

    private val fullSqlPathField = TextField()
    private val incrSqlPathField = TextField()
    private val keyFieldsField = TextField("busi_TYPE")
    private val modeComboBox = ComboBox<String>().apply {
        items.addAll("Delete-Insert", "Insert-Select-Not-Exists")
        value = "Delete-Insert"
    }
    private val logArea = TextArea().apply {
        isEditable = false
        promptText = "运行日志..."
    }

    init {
        spacing = 10.0
        padding = Insets(15.0)

        // --- UI 布局 ---
        val grid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            add(Label("全量SQL路径:"), 0, 0)
            add(fullSqlPathField.apply { prefWidth = 400.0 }, 1, 0)

            add(Label("输出SQL路径:"), 0, 1)
            add(incrSqlPathField, 1, 1)

            add(Label("主键字段(逗号分隔):"), 0, 2)
            add(keyFieldsField, 1, 2)

            add(Label("生成模式:"), 0, 3)
            add(modeComboBox, 1, 3)
        }

        val runBtn = Button("开始转换").apply {
            style = "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;"
            setOnAction { handleConversion() }
        }

        children.addAll(grid, runBtn, Label("日志输出:"), logArea)
        VBox.setVgrow(logArea, Priority.ALWAYS)
    }

    private fun handleConversion() {
        val inputPath = fullSqlPathField.text
        val outputPath = incrSqlPathField.text
        val keyFields = keyFieldsField.text.split(",").map { it.trim().lowercase() }
        val mode = modeComboBox.value

        if (inputPath.isEmpty() || outputPath.isEmpty()) {
            log("错误: 请指定输入和输出路径")
            return
        }

        try {
            processSql(inputPath, outputPath, keyFields, mode)
            log("✅ 转换成功! 输出至: $outputPath")
        } catch (e: Exception) {
            log("❌ 发生错误: ${e.message}")
        }
    }

    private fun processSql(input: String, output: String, keyFields: List<String>, mode: String) {
        val content = File(input).readText(StandardCharsets.UTF_8)

        // 1. 预处理：按分号分割，并过滤掉非 INSERT 语句
        val stmts = content.split(";")
            .map { it.trim() }
            .filter { it.startsWith("insert", ignoreCase = true) }

        val resultSql = mutableListOf<String>()

        for (stmt in stmts) {
            try {
                val info = parseInsert(stmt)

                // 2. 核心映射：将小写的字段名映射到其原始的值片段（包括单引号）
                // zip 会成对匹配 columns 和 values
                val kvMap = info.columns.map { it.lowercase() }.zip(info.values).toMap()

                // 3. 构建 WHERE 子句
                val whereParts = mutableListOf<String>()
                for (key in keyFields) {
                    val valueSnippet = kvMap[key.lowercase()]
                    if (valueSnippet != null) {
                        // 尽量寻找原始 SQL 中的列名写法（保持大小写）
                        val originalColName = info.columns.find { it.equals(key, ignoreCase = true) } ?: key
                        whereParts.add("$originalColName = $valueSnippet")
                    }
                }

                if (whereParts.isEmpty()) {
                    log("⚠️ 跳过语句：找不到主键字段 $keyFields \n语句: ${stmt.take(50)}...")
                    continue
                }

                val whereClause = whereParts.joinToString(" AND ")

                // 4. 根据模式生成结果
                when (mode) {
                    "Delete-Insert" -> {
                        resultSql.add("DELETE FROM ${info.table} WHERE $whereClause;")
                        resultSql.add("$stmt;")
                    }
                    "Insert-Select-Not-Exists" -> {
                        val cols = info.columns.joinToString(", ")
                        val vals = info.values.joinToString(", ")
                        val sql = """
                        INSERT INTO ${info.table} ($cols)
                        SELECT $vals FROM DUAL
                        WHERE NOT EXISTS (SELECT 1 FROM ${info.table} WHERE $whereClause);
                    """.trimIndent()
                        resultSql.add(sql)
                    }
                }
                resultSql.add("") // 空行分隔

            } catch (e: Exception) {
                log("❌ 解析失败: ${e.message}")
            }
        }

        if (resultSql.isNotEmpty()) {
            File(output).writeText(resultSql.joinToString("\n"), StandardCharsets.UTF_8)
        } else {
            log("⚠️ 未生成任何 SQL，请检查输入文件和主键配置。")
        }
    }

    /**
     * 修正后的 splitValues：支持识别单引号内的内容，并完整保留引号字符
     */
    private fun splitValues(valuesStr: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inString = false

        for (c in valuesStr) {
            when {
                // 碰到单引号，切换状态并存入 sb
                c == '\'' -> {
                    inString = !inString
                    sb.append(c)
                }
                // 只有在字符串外面的逗号才视作分隔符
                c == ',' && !inString -> {
                    result.add(sb.toString().trim())
                    sb.setLength(0)
                }
                else -> sb.append(c)
            }
        }
        // 最后一项
        if (sb.isNotEmpty()) {
            result.add(sb.toString().trim())
        }
        return result
    }

    /**
     * 配合使用的 parseInsert 正则解析函数
     */
    private fun parseInsert(stmt: String): SqlInfo {
        // 正则：忽略大小写，匹配 insert into 表名 (字段) values (值)
        val regex = Regex("(?i)insert\\s+into\\s+(\\w+)\\s*\\((.*?)\\)\\s*values\\s*\\((.*)\\)", RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(stmt) ?: throw Exception("SQL 格式不规范，无法匹配 INSERT 结构")

        val (table, colStr, valStr) = match.destructured

        val columns = colStr.split(",").map { it.trim() }
        val values = splitValues(valStr)

        if (columns.size != values.size) {
            throw Exception("字段数量(${columns.size})与值数量(${values.size})不匹配")
        }

        return SqlInfo(table, columns, values)
    }

    private fun log(msg: String) = logArea.appendText("$msg\n")

    data class SqlInfo(val table: String, val columns: List<String>, val values: List<String>)
}