package io.github.nonoas.worktools.platform.ui.component

import io.github.nonoas.worktools.platform.global.message.MsgBusManager

class GlobalSearchTextField : AbstractBusSearchTextField(SearchMode.GLOBAL, MsgBusManager::getGlobalBus)
