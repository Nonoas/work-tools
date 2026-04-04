package io.github.nonoas.worktools.platform.controller

import io.github.nonoas.worktools.platform.common.FuncCode
import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.ext.FuncPaneFactory
import io.github.nonoas.worktools.platform.view.db.SQLTransfer
import javafx.scene.Parent

/**
 * @author Nonoas
 * @date 2022/7/16
 */
@FuncCode("SQLTransfer")
class SQLTransferController : FuncPaneFactory {

    override fun create(): FuncPane {
        return SQLTransfer()
    }

    override fun getName(): String {
        return "SQL转换"
    }

    override fun getDescription(): String {
        return "转换SQL"
    }
}
