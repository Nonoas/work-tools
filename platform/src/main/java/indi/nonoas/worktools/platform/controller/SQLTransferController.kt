package indi.nonoas.worktools.platform.controller

import indi.nonoas.worktools.platform.common.FuncCode
import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import indi.nonoas.worktools.platform.view.db.SQLTransfer
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

    override fun getName(): String {
        return "SQL转换"
    }

    override fun getDescription(): String {
        return "转换SQL"
    }
}
