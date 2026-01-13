package indi.yiyi.stockmonitor

import indi.nonoas.worktools.platform.controller.BaseParentController
import javafx.scene.Parent

class StockMonitorCtrl : BaseParentController() {
    override fun getRootView(): Parent {
        return StockMonitorPane.getInstance()
    }
}