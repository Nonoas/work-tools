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

    override fun getName(): String {
        return "文件编码"
    }

    override fun getDescription(): String {
        return "用于批量修改文件编码"
    }

}