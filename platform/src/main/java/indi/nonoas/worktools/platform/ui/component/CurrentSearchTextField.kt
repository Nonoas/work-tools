package indi.nonoas.worktools.platform.ui.component

import indi.nonoas.worktools.platform.global.message.MsgBusManager

class CurrentSearchTextField : AbstractBusSearchTextField(SearchMode.CURRENT, MsgBusManager::getCurrentBus)
