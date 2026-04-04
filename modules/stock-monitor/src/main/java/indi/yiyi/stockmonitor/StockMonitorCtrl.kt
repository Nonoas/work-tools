package indi.yiyi.stockmonitor

import io.github.nonoas.worktools.platform.controller.BaseParentController
import io.github.nonoas.worktools.platform.ext.FuncPane
import javafx.scene.Node
import javafx.scene.image.ImageView

class StockMonitorCtrl : BaseParentController() {

    override fun getName(): String {
        return "盯盘助手"
    }

    override fun getGraphic(): Node {
        return ImageView("image/stock-monitor-logo-64x64.png")
            .apply {
                isPreserveRatio = true;
                isSmooth = true
                fitWidth = 32.0
                fitHeight = 32.0
            }
    }

    override fun create(): FuncPane {
        return StockMonitorPane()
    }
}
