package indi.nonoas.worktools.platform.ui.component

import indi.nonoas.worktools.platform.global.message.MsgBusManager
import indi.nonoas.worktools.platform.service.LlmSearchService
import javafx.application.Platform
import javafx.scene.input.KeyEvent
import kotlin.concurrent.thread

class LlmSearchTextField : AbstractSearchTextField(SearchMode.LLM, "Enter提问，Tab切换模式") {

    private fun publisher(): LlmSearchListener {
        return MsgBusManager.getGlobalBus().getPublisher<LlmSearchListener>(LlmSearchListener.TOPIC)
    }

    override fun onKeywordChanged(keyword: String) {
        if (keyword.isBlank()) {
            publisher().onCleared()
        }
    }

    override fun onEnter(event: KeyEvent) {
        val prompt = text.trim()
        if (prompt.isEmpty()) {
            publisher().onCleared()
            return
        }

        isDisable = true
        publisher().onLoading(prompt)

        thread(name = "llm-search", isDaemon = true) {
            runCatching {
                LlmSearchService.chat(prompt)
            }.onSuccess { response ->
                Platform.runLater {
                    isDisable = false
                    publisher().onResponse(prompt, response)
                    focusInput()
                }
            }.onFailure { throwable ->
                Platform.runLater {
                    isDisable = false
                    publisher().onError(prompt, throwable.message ?: "LLM 请求失败")
                    focusInput()
                }
            }
        }
    }
}
