package indi.nonoas.worktools.controller

import indi.nonoas.worktools.common.FuncCode
import indi.nonoas.worktools.ui.UIController
import indi.nonoas.worktools.view.ClassExtractPane
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/6/12 18:30
 */
@FuncCode("ClassExtract")
class ClassExtractController : UIController {
    override fun getRootView(): Parent {
        return ClassExtractPane.instance!!
    }
}
