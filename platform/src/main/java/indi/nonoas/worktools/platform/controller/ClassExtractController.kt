package indi.nonoas.worktools.platform.controller

import indi.nonoas.worktools.platform.common.FuncCode
import indi.nonoas.worktools.platform.ext.FuncPane
import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import indi.nonoas.worktools.platform.view.ClassExtractPane
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/6/12 18:30
 */
@FuncCode("ClassExtract")
class ClassExtractController : FuncPaneFactory {

    override fun create(): FuncPane {
        TODO("Not yet implemented")
    }

    override fun getName(): String {
        return "Class文件提取"
    }

    override fun getDescription(): String {
        return "从构建文件中提取class文件"
    }
}
