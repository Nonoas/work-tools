package io.github.nonoas.worktools.platform.ui.component

import io.github.nonoas.worktools.platform.global.message.Topic
import javafx.scene.input.KeyEvent

interface SearchListener {

    fun onTextChange(keyword: String)

    fun onKeyPressed(event: KeyEvent) {

    }

    fun onEntered(event: KeyEvent)

    companion object {
        val TOPIC: Topic<SearchListener> = Topic.create("searchListener", SearchListener::class.java)
    }
}