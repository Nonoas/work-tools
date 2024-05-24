package indi.nonoas.worktools.ui

import javafx.beans.value.ObservableValue
import javafx.concurrent.Task
import java.util.*
import java.util.concurrent.Executors
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * 用于新建一个线程执行一个操作，并对返回值做出响应
 *
 * @author Nonoas
 * @datetime 2022/1/8 15:02
 */
class TaskHandler<T> {
    private var whenCall: Supplier<T>? = null
    private var andThen: Consumer<T>? = null
    private val task: Task<T> = object : Task<T>() {
        override fun call(): T {
            return whenCall!!.get()
        }
    }

    init {
        task.valueProperty().addListener { _: ObservableValue<out T>?, _: T, newValue: T ->
            if (null != andThen) {
                andThen!!.accept(newValue)
            }
        }
    }

    /**
     * 当线程执行时调用，在子线程执行
     *
     * @return 返回处理结果
     */
    fun whenCall(supplier: Supplier<T>): TaskHandler<T> {
        whenCall = supplier
        return this
    }

    /**
     * 当线程结果返回时调用，在 UI 线程执行
     */
    fun andThen(consumer: Consumer<T>?): TaskHandler<T> {
        andThen = consumer
        return this
    }

    fun handle() {
        checkNotNull(whenCall) { "未调用 whenCall 指定执行任务" }
        val service = Executors.newSingleThreadExecutor()
        service.execute(task)
        service.shutdown()
    }
}
