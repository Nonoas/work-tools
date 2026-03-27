package indi.nonoas.worktools.platform.ui.component

import indi.nonoas.worktools.platform.global.message.MsgBusManager

class GlobalSearchTextField : AbstractBusSearchTextField(SearchMode.GLOBAL, MsgBusManager::getGlobalBus)
