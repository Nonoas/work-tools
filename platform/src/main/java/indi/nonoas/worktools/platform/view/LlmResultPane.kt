package indi.nonoas.worktools.platform.view

import indi.nonoas.worktools.platform.common.CommonInsets
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class LlmResultPane : VBox(10.0) {

    private val lbStatus = Label("LLM 模式")

    private val taPrompt = TextArea().apply {
        isEditable = false
        isWrapText = true
        prefRowCount = 3
        isFocusTraversable = false
    }

    private val taResponse = TextArea().apply {
        isEditable = false
        isWrapText = true
        isFocusTraversable = false
        VBox.setVgrow(this, Priority.ALWAYS)
    }

    init {
        padding = CommonInsets.PADDING_20
        children.addAll(
            lbStatus,
            Label("提问"),
            taPrompt,
            Label("回答"),
            taResponse,
        )
    }

    fun showLoading(prompt: String) {
        lbStatus.text = "LLM 正在思考..."
        taPrompt.text = prompt
        taResponse.text = ""
    }

    fun showResponse(prompt: String, response: String) {
        lbStatus.text = "LLM 已回复"
        taPrompt.text = prompt
        taResponse.text = response
    }

    fun showError(prompt: String, message: String) {
        lbStatus.text = "LLM 请求失败"
        taPrompt.text = prompt
        taResponse.text = message
    }
}
