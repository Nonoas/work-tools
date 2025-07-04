package indi.nonoas.worktools.ui

import javafx.concurrent.Task
import org.apache.logging.log4j.LogManager
import java.util.concurrent.Executors
import java.util.function.Consumer

/**
 * 用于新建一个线程执行一个操作，并对返回值做出响应
 *
 * @author Nonoas
 * @datetime 2022/1/8 15:02
 */
class TaskHandler<T> {

    private var whenCall: (() -> T)? = null
    private var andThen: Consumer<T>? = null

    private val task = object : Task<T>() {
        override fun call(): T {
            return try {
                checkNotNull(whenCall) { "whenCall 未设置" }.invoke()
            } catch (e: Throwable) {
                LOG.error("任务执行异常", e)
                throw e
            }
        }
    }

    init {
        task.valueProperty().addListener { _, _, newValue ->
            andThen?.accept(newValue)
        }
    }

    /**
     * 设置任务，子线程执行
     */
    fun whenCall(supplier: () -> T) = apply {
        this.whenCall = supplier
    }

    /**
     * 设置回调，UI线程执行
     */
    fun andThen(consumer: Consumer<T>) = apply {
        this.andThen = consumer
    }

    fun handle() {
        checkNotNull(whenCall) { "未调用 whenCall 指定执行任务" }
        THREAD_POOL.execute(task)
    }

    companion object {
        private val LOG = LogManager.getLogger(TaskHandler::class)
        private val THREAD_POOL = Executors.newCachedThreadPool()

        /**
         * 简单后台任务
         */
        fun backRun(run: () -> Unit) {
            THREAD_POOL.execute(run)
        }
    }
}
