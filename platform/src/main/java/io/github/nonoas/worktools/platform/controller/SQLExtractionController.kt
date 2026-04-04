package io.github.nonoas.worktools.platform.controller

import io.github.nonoas.worktools.platform.common.FuncCode
import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.ext.FuncPaneFactory
import io.github.nonoas.worktools.platform.view.db.SQLExtraction
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/5/12 21:06
 */
@FuncCode("SQLExtraction")
class SQLExtractionController : FuncPaneFactory {
    override fun create(): FuncPane {
        return SQLExtraction.instance
    }

    override fun getName(): String {
        return "SQL Extraction"
    }

    override fun getDescription(): String {
        return "SQL提取"
    }
}
