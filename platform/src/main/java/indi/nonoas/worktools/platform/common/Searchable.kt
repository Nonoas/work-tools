package indi.nonoas.worktools.platform.common

import indi.nonoas.worktools.platform.ui.component.FileLinkButton

interface Searchable {
    /**
     * 当搜索框文本变更时
     */
    fun onSearchKeywordChange(keyword: String?): MutableList<FileLinkButton>

    /**
     * 当回车确认时
     */
    fun onEntered(button: FileLinkButton)
}