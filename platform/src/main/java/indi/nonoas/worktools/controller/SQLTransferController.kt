package indi.nonoas.worktools.controller

import indi.nonoas.worktools.common.FuncCode
import indi.nonoas.worktools.ui.FuncPaneFactory
import indi.nonoas.worktools.view.db.SQLTransfer
import javafx.scene.Parent

/**
 * @author Nonoas
 * @date 2022/7/16
 */
@FuncCode("SQLTransfer")
class SQLTransferController : FuncPaneFactory {
    override fun getRootView(): Parent {
        return SQLTransfer()
    }
}
