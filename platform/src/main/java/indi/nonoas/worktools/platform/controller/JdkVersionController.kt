package indi.nonoas.worktools.platform.controller

import indi.nonoas.worktools.platform.common.FuncCode
import indi.nonoas.worktools.platform.view.env.JdkVersionPane
import javafx.scene.Parent

/**
 * TODO 类描述
 * @author huangshengsheng
 * @date 2024/5/24 10:45
 */
class JdkVersionController: BaseParentController() {
    override fun getRootView(): Parent {
        return JdkVersionPane()
    }

    override fun getCode(): String {
        return "JdkVersion"
    }
}