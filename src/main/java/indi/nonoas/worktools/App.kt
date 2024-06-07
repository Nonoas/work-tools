package indi.nonoas.worktools

import cn.hutool.db.DbUtil
import com.melloware.jintellitype.JIntellitype
import github.nonoas.jfx.flat.ui.theme.LightTheme
import indi.nonoas.worktools.common.Identifier
import indi.nonoas.worktools.config.DBConfigEnum
import indi.nonoas.worktools.config.FlyWayMigration
import indi.nonoas.worktools.global.Manifest
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.ui.UIFactory
import indi.nonoas.worktools.ui.component.BaseStage
import indi.nonoas.worktools.ui.component.ExceptionAlter
import indi.nonoas.worktools.ui.component.MyAlert
import indi.nonoas.worktools.utils.DBUtil
import indi.nonoas.worktools.utils.UIUtil
import indi.nonoas.worktools.view.MainStage
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.layout.Pane
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.apache.logging.log4j.LogManager
import java.awt.AWTException
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import kotlin.system.exitProcess

/**
 * TODO 类描述
 *
 * @author Nonoas
 * @date 2024/4/4 10:27
 */
class App : Application() {
    private val LOG = LogManager.getLogger(TaskHandler::class)
    /**
     * 锁文件，标志程序是否正在运行
     */
    private val file = File("lock")

    /**
     * 文件写锁，防止程序重复启动和外界向文件写入内容
     */
    private var fileLock: FileLock? = null
    private var channel: FileChannel? = null

    /**
     * 系统托盘
     */
    private var systemTray: SystemTray? = null
    private var trayIcon: TrayIcon? = null

    /**
     * 全局快捷键类
     */
    private var jIntellitype: JIntellitype? = null

    /**
     * 是否已有程序运行
     */
    private var hasRun = false

    /**
     * 不可见的系统托盘宿主窗口，主要用于唤起托盘菜单
     */
    private var primaryStage = Stage()

    @Throws(Exception::class)
    override fun init() {
        Manifest.init()
        DBUtil.init()
    }

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
            ExceptionAlter.error(e)
            LOG.error("未知异常", e)
        }

        Platform.setImplicitExit(false)
        if (isRunning()) {
            MyAlert(Alert.AlertType.WARNING, "程序已经在运行了！").showAndWait()
            hasRun = true
            stop()
            return
        }

        dbMigrate()

        jIntellitype = JIntellitype.getInstance()

        setUserAgentStylesheet(LightTheme().userAgentStylesheet)
        initPrimaryStage(primaryStage)

        val stage: BaseStage = MainStage.instance as BaseStage
        // 设置系统托盘
        setSystemTray(stage)
        setGlobalHotKeys(stage)
        stage.show()
    }

    /**
     * 数据库升级
     */
    private fun dbMigrate() {
        val ds = DBConfigEnum.WORKTOOLS
        FlyWayMigration(ds).migrate()
    }

    @Throws(Exception::class)
    override fun stop() {
        super.stop()
        if (hasRun) {
            exitProcess(0)
        }
        fileLock?.release()
        channel?.close()
        // 清楚系统全局热键
        clearGlobalHotKeys()
        // 删除系统托盘
        systemTray?.remove(trayIcon)
        systemTray = null
    }

    /**
     * 设置系统托盘
     *
     * @throws AWTException awt异常
     */
    @Throws(AWTException::class)
    private fun setSystemTray(stage: BaseStage) {

        val url = javaClass.getResource("/image/tray.png")
        val image = Toolkit.getDefaultToolkit().getImage(url)

        val miOpen = MenuItem("显示").apply {
            onAction = EventHandler { stage.display() }
        }
        val miExit = MenuItem("退出").apply {
            onAction = EventHandler { Platform.exit() }
        }
        val contextMenu = ContextMenu(miOpen, miExit)

        trayIcon = TrayIcon(image).apply {
            toolTip = "WorkTools\n按 ALT+SHIFT+M 显示/隐藏"
            // 双击托盘时调用
            addActionListener {
                Platform.runLater {
                    stage.display()
                }
            }
            // 右键时调用
            addMouseListener(object : MouseAdapter() {
                override fun mouseReleased(e: MouseEvent) {
                    if (e.button == MouseEvent.BUTTON3) {
                        Platform.runLater {
                            primaryStage.requestFocus()
                            val screen = Screen.getPrimary()
                            val x = (e.xOnScreen + 16) / screen.outputScaleX
                            val y = e.yOnScreen / screen.outputScaleY
                            contextMenu.show(primaryStage, x, y)
                        }
                    }
                }
            })
        }

        systemTray = SystemTray.getSystemTray()
                .apply {
                    add(trayIcon)
                }


    }

    /**
     * 初始化托盘宿主窗口
     */
    private fun initPrimaryStage(primaryStage: Stage) {
        this.primaryStage = primaryStage.apply {
            scene = Scene(Pane(), 1.0, 1.0)
            initStyle(StageStyle.UTILITY)
            x = Double.MAX_VALUE
            show()
        }
    }

    /**
     * 设置全局快捷键
     *
     * @param stage 主窗口
     */
    private fun setGlobalHotKeys(stage: BaseStage) {
        jIntellitype!!.registerHotKey(
                Identifier.A_S_M,
                JIntellitype.MOD_ALT + JIntellitype.MOD_SHIFT,
                'M'.code
        )
        jIntellitype!!.addHotKeyListener { identifier ->
            if (Identifier.A_S_M == identifier) {
                Platform.runLater {
                    if (stage.isInsight) {
                        stage.hide()
                    } else {
                        stage.display()
                    }
                }
            }
        }
    }

    /**
     * 清楚全局快捷键
     */
    private fun clearGlobalHotKeys() {
        jIntellitype?.cleanUp()
    }

    @Throws(IOException::class)
    private fun isRunning(): Boolean {
        if (!file.exists()) {
            file.createNewFile()
            channel = FileOutputStream(file, true).channel
            channel?.let { fileLock = channel!!.lock() }
            return false
        }

        val fTemp = File("lock.tmp")
        val hasChanged = file.renameTo(fTemp)
        return if (hasChanged) {
            fTemp.renameTo(file)
            channel = FileOutputStream(file, true).channel
            channel?.let { fileLock = channel!!.lock() }
            false
        } else {
            true
        }
    }
}


