package indi.nonoas.worktools.platform.ui.component

import indi.nonoas.worktools.platform.global.message.Topic

interface LlmSearchListener {

    fun onLoading(prompt: String)

    fun onResponse(prompt: String, response: String)

    fun onError(prompt: String, message: String)

    fun onCleared() {
    }

    companion object {
        val TOPIC: Topic<LlmSearchListener> = Topic.create("llmSearchListener", LlmSearchListener::class.java)
    }
}
