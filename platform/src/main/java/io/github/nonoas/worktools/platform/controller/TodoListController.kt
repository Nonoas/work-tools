package io.github.nonoas.worktools.platform.controller

import io.github.nonoas.worktools.platform.common.FuncCode
import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.ext.FuncPaneFactory
import io.github.nonoas.worktools.platform.view.todolist.TodoListPane
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
