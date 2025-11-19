package indi.nonoas.worktools.controller

import indi.nonoas.worktools.common.FuncCode
import indi.nonoas.worktools.view.env.JdkVersionPane
import javafx.scene.Parent

/**
 * TODO 类描述
 * @author huangshengsheng
 * @date 2024/5/24 10:45
 */
@FuncCode("JdkVersion")
class JdkVersionController: BaseParentController() {
    override fun getRootView(): Parent {
        return JdkVersionPane()
    }
}