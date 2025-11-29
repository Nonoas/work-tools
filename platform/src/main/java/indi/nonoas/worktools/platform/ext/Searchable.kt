package indi.nonoas.worktools.platform.ext

import indi.nonoas.worktools.platform.pojo.vo.ExecFileVo
import indi.nonoas.worktools.platform.ui.component.FileLinkButton

interface Searchable {
    /**
     * 当搜索框文本变更时
     */
    fun onSearchKeywordChange(keyword: String?): MutableList<ExecFileVo>

    /**
     * 当回车确认时
     */
    fun onEntered(button: FileLinkButton)

    fun getType(): Type

    enum class Type {
        FUNCTION, EXECUTOR
    }
}