package indi.nonoas.worktools.utils

import indi.nonoas.worktools.ui.component.MyAlert
import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import java.util.*

/**
 * @author Nonoas
 * @date 2022/7/16
 */
object UIUtil {
    /**
     * 获取UI节点的在屏幕中的坐标边界
     *
     * @param node 当前节点
     * @return UI节点的在屏幕中的坐标边界
     */
    @JvmStatic
    fun getScreeBounds(node: Node): Bounds {
        val bounds: Bounds = node.getBoundsInLocal()
        return node.localToScreen(bounds)
    }

    fun warn(msg: String?): Optional<ButtonType> {
        return showMyAlter(AlertType.WARNING, msg)
    }

    fun error(msg: String?): Optional<ButtonType> {
        return showMyAlter(AlertType.ERROR, msg)
    }

    fun showMyAlter(type: AlertType?, msg: String?): Optional<ButtonType> {
        return MyAlert(type, msg).showAndWait()
    }
}
