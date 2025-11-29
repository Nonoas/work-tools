package indi.nonoas.worktools.platform.controller

import indi.nonoas.worktools.platform.common.FuncCode
import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import javafx.scene.Parent

/**
 * 文件编码
 * @author Nonoas
 * @date 2024/3/30 2:15
 */
@FuncCode("FileEncode")
class FileEncodeController : FuncPaneFactory {

    override fun getRootView(): Parent {
        return FileEncodePane.instance!!
    }

}