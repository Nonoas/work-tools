package indi.nonoas.worktools.view.launcher

import cn.hutool.setting.dialect.Props
import indi.nonoas.worktools.ui.component.ExceptionAlter
import javafx.scene.control.TextInputControl
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

/**
 * @author Nonoas
 * @date 2021/8/23
 */
class Launcher(
    /**
     * 驱动器根目录
     */
    private val path: String
) {
    private var properties: Props? = null
    private val port: Int
    val name: String

    init {
        try {
            properties = Props("$path/manifest.properties")
        } catch (e: IOException) {
            ExceptionAlter(e).showAndWait()
        }
        port = properties!!.getProperty("port").toInt()
        name = properties!!.getProperty("name")
    }

    /**
     * 程序启动
     *
     * @param tic 用于输出信息的文本框
     */
    fun launch(tic: TextInputControl) {
        val starterPath = path + System.getProperty("file.separator") + "starter.bat"
        val rt = Runtime.getRuntime()
        tic.appendText("${name}启动...\n\n")
        try {
            val process = rt.exec("cmd.exe /k cmd /c start $starterPath")
        } catch (e: IOException) {
            ExceptionAlter(e).showAndWait()
        }
        tic.appendText("${name}启动完成！\n\n")
    }

    /**
     * 关闭进程
     */
    fun shutDown(tic: TextInputControl) {
        tic.appendText("关闭$name\n\n")
        val stopFilePath = path + System.getProperty("file.separator") + "stop.bat"
        val file = File(stopFilePath)
        val rt = Runtime.getRuntime()
        if (file.exists()) {
            try {
                val process = rt.exec("cmd /c start $stopFilePath")
                return
            } catch (e: IOException) {
                ExceptionAlter(e).showAndWait()
            }
        }
        var pid: String? = null
        try {
            val process = rt.exec("cmd /c netstat -ano | findstr $port")
            val inputStream = process.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream, "GB2312"))
            var tmpStr: String
            while (reader.readLine().also { tmpStr = it } != null) {
                println(tmpStr)
                val index = tmpStr.lastIndexOf(" ")
                if (index >= 0) {
                    pid = tmpStr.substring(index + 1).trim { it <= ' ' }
                    break
                }
            }
        } catch (e: IOException) {
            ExceptionAlter(e).showAndWait()
        }
        try {
            Runtime.getRuntime().exec("taskkill /F /pid $pid")
        } catch (e: IOException) {
            ExceptionAlter(e).showAndWait()
        }
    }

}