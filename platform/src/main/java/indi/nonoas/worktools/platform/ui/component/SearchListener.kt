package indi.nonoas.worktools.platform.ui.component

import indi.nonoas.worktools.platform.global.message.Topic

interface SearchListener {

    fun search(keyword: String)

    companion object {
        val TOPIC: Topic<SearchListener> = Topic.create("searchListener", SearchListener::class.java)
    }
}