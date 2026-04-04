package io.github.nonoas.worktools.platform.controller

import io.github.nonoas.worktools.platform.common.FuncCode
import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.ext.FuncPaneFactory
import io.github.nonoas.worktools.platform.view.ClassExtractPane
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/6/12 18:30
 */
@FuncCode("ClassExtract")
class ClassExtractController : FuncPaneFactory {

    override fun create(): FuncPane {
        return ClassExtractPane.instance!!
    }

    override fun getName(): String {
        return "Class文件提取"
    }

    override fun getDescription(): String {
        return "从构建文件中提取class文件"
    }
}
