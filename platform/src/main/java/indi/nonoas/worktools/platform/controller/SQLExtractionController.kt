package indi.nonoas.worktools.platform.controller

import indi.nonoas.worktools.platform.common.FuncCode
import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import indi.nonoas.worktools.platform.view.db.SQLExtraction
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/5/12 21:06
 */
@FuncCode("SQLExtraction")
class SQLExtractionController : FuncPaneFactory {
    override fun getRootView(): Parent {
        return SQLExtraction.instance
    }

    override fun getName(): String {
        return "SQL Extraction"
    }

    override fun getDescription(): String {
        return "SQL提取"
    }
}
