package indi.nonoas.worktools.launcher

import indi.nonoas.worktools.platform.ext.Searchable
import indi.nonoas.worktools.platform.dao.ExecFileDao
import indi.nonoas.worktools.platform.pojo.vo.ExecFileVo
import indi.nonoas.worktools.platform.ui.component.FileLinkButton
import javafx.stage.Stage

class ExecFileSearcher : Searchable {

    override fun onSearchKeywordChange(keyword: String?): MutableList<ExecFileVo> {
        return ExecFileDao.search(keyword)
    }

    override fun onEntered(button: FileLinkButton) {
        button.fire()
        (button.scene.window as Stage).close()
    }

    override fun getType(): Searchable.Type {
        return Searchable.Type.EXECUTOR
    }

}