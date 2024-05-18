package indi.nonoas.worktools.view

import indi.nonoas.worktools.ui.component.MyAlert
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.layout.VBox
import org.quartz.CronScheduleBuilder
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import java.time.LocalTime


/**
 *
 * @author huangshengsheng
 * @date 2024/5/13 17:21
 */
class TodoListPane : VBox() {

    private val listView = ListView<String>()

    private val btnAdd = Button("+").apply {
        maxWidth= Double.MAX_VALUE
    }

    init {
        isFillWidth = true
        children.addAll(listView,btnAdd)
//        add()
    }

    private fun add() {
        val scheduler = StdSchedulerFactory.getDefaultScheduler()
        scheduler.start()

        val job = JobBuilder.newJob(MyJob::class.java)
            .withIdentity("myJob", "group1")
            .build()

        val trigger: Trigger = TriggerBuilder.newTrigger()
            .withIdentity("myTrigger", "group1")
            .withSchedule(CronScheduleBuilder.cronSchedule("0/3 * * * * ?")) // 每天早上9点执行
            .build()

        scheduler.scheduleJob(job, trigger)
    }

    class MyJob : Job {
        @Throws(JobExecutionException::class)
        override fun execute(context: JobExecutionContext) {
            // 在任务执行中更新UI
            Platform.runLater {
                val now = LocalTime.now()
                MyAlert(Alert.AlertType.WARNING, "Biu弟,${now}了嘞，下班吗").show()
            }
        }
    }
}
