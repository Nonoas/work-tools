package indi.nonoas.worktools.platform.controller

import indi.nonoas.worktools.platform.common.FuncCode
import indi.nonoas.worktools.platform.ui.FuncPaneFactory
import indi.nonoas.worktools.platform.view.ClassExtractPane
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/6/12 18:30
 */
@FuncCode("ClassExtract")
class ClassExtractController : FuncPaneFactory {
    override fun getRootView(): Parent {
        return ClassExtractPane.instance!!
    }
}
