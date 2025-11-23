package indi.nonoas.worktools.launcher

import indi.nonoas.worktools.platform.common.Searchable
import indi.nonoas.worktools.platform.dao.ExecFileDao
import indi.nonoas.worktools.platform.ui.component.FileLinkButton
import javafx.stage.Stage

class ExecFileSearcher : Searchable {

    override fun onSearchKeywordChange(keyword: String?): MutableList<FileLinkButton> {
        val result = mutableListOf<FileLinkButton>()
        val search = ExecFileDao.search(keyword)
        for (execVo in search) {
            val button = ExecFileButton(execVo)
            result.add(button)
        }
        return result
    }

    override fun onEntered(button: FileLinkButton) {
        button.fire()
        (button.scene.window as Stage).close()
    }

}