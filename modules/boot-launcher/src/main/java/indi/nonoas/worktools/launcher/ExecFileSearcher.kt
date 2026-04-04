package indi.nonoas.worktools.launcher

import io.github.nonoas.worktools.platform.ext.Searchable
import io.github.nonoas.worktools.platform.dao.ExecFileDao
import io.github.nonoas.worktools.platform.pojo.vo.ExecFileVo
import io.github.nonoas.worktools.platform.ui.component.FileLinkButton
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