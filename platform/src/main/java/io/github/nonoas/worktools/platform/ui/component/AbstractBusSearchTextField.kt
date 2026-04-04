package io.github.nonoas.worktools.platform.ui.component

import io.github.nonoas.worktools.platform.global.message.MessageBus
import javafx.scene.input.KeyEvent

abstract class AbstractBusSearchTextField(
    mode: SearchMode,
    private val busSupplier: () -> MessageBus,
) : AbstractSearchTextField(mode) {

    private fun publisher(): SearchListener {
        return busSupplier().getPublisher<SearchListener>(SearchListener.TOPIC)
    }

    override fun onKeywordChanged(keyword: String) {
        publisher().onTextChange(keyword)
    }

    override fun onKeyReleased(event: KeyEvent) {
        publisher().onKeyPressed(event)
    }

    override fun onEnter(event: KeyEvent) {
        publisher().onEntered(event)
    }
}
