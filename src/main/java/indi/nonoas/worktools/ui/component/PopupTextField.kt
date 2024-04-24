package indi.nonoas.worktools.ui.component

import indi.nonoas.worktools.utils.UIUtil.getScreeBounds
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Popup
import javafx.util.Callback

/**
 * @author Nonoas
 * @date 2022/7/18
 */
class PopupTextField : TextField() {
    private var popupContentFactory: Callback<String, Node>? = null
    private var popup: Popup? = null

    init {
        initListener()
    }

    private fun initListener() {
        textProperty().addListener { observable: ObservableValue<out String>?, oldValue: String?, newValue: String ->
            if (null == popup) {
                popup = Popup()
                popup!!.isAutoHide = true
            }
            val node = updatePopupContent(newValue)
            popup!!.content.setAll(node)
            val bounds = getScreeBounds(this@PopupTextField)
            if (!popup!!.isShowing) {
                popup!!.show(this@PopupTextField, bounds.minX, bounds.maxY)
            }
            if (newValue.isEmpty()) {
                popup!!.hide()
            }
        }
    }

    private fun updatePopupContent(newValue: String): Node {
        val vBox: VBox = object : VBox() {
            init {
                style = """
                    -fx-border-width:1px;
                    -fx-border-color:#aaa;
                    -fx-background-color:white;
                    -fx-effect: none
                 """.trimIndent()
                prefWidth = this@PopupTextField.width
            }
        }
        if (popupContentFactory != null) {
            vBox.children.setAll(popupContentFactory!!.call(newValue))
        } else {
            vBox.children.setAll(Label("你在找「$newValue」吗"))
        }
        return vBox
    }

    fun setPopupContentFactory(popupContentFactory: Callback<String, Node>?) {
        this.popupContentFactory = popupContentFactory
    }

    fun hidePopup() {
        if (null == popup) {
            return
        }
        popup!!.hide()
    }
}
