package indi.nonoas.worktools.platform.controller

import indi.nonoas.worktools.platform.common.FuncCode
import indi.nonoas.worktools.platform.ext.FuncPane
import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import indi.nonoas.worktools.platform.view.todolist.TodoListPane
import javafx.scene.Parent

/**
 * @author Nonoas
 * @date 2022/7/16
 */
@FuncCode("TodoList")
class TodoListController : FuncPaneFactory {

    override fun create(): FuncPane {
        return TodoListPane()
    }

    override fun getName(): String {
        return "待办"
    }

    override fun getDescription(): String {
        return "待办事项"
    }
}
