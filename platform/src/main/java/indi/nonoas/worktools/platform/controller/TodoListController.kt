package indi.nonoas.worktools.platform.controller

import indi.nonoas.worktools.platform.common.FuncCode
import indi.nonoas.worktools.platform.ui.FuncPaneFactory
import indi.nonoas.worktools.platform.view.todolist.TodoListPane
import javafx.scene.Parent

/**
 * @author Nonoas
 * @date 2022/7/16
 */
@FuncCode("TodoList")
class TodoListController : FuncPaneFactory {
    override fun getRootView(): Parent {
        return TodoListPane()
    }
}
