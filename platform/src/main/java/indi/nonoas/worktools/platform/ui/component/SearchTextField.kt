package indi.nonoas.worktools.platform.ui.component

import atlantafx.base.controls.CustomTextField

interface SearchTextField {

    val mode: SearchMode

    val view: CustomTextField

    fun bindModeSwitcher(onSwitchMode: () -> Unit)

    fun syncText(value: String)

    fun focusInput()
}

enum class SearchMode(
    val prefix: String,
    val tooltipText: String,
) {
    GLOBAL(" Qry_>", "全局模式"),
    CURRENT(" Cur_>", "当前模式"),
    LLM(" LLM_>", "AI 模式"),
}
