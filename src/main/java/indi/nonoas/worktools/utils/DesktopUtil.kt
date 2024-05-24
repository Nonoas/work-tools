package indi.nonoas.worktools.utils

import indi.nonoas.worktools.ui.component.ExceptionAlter
import org.apache.log4j.Logger
import java.awt.Desktop
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


/**
 * @author Nonoas
 * @datetime 2022/5/15 22:03
 */
object DesktopUtil {

    private val logger = Logger.getLogger(DesktopUtil::class.java)

    @JvmStatic
    fun open(file: File?) {
        try {
            Desktop.getDesktop().open(file)
        } catch (e: Exception) {
            ExceptionAlter.error(e)
        }
    }

    fun changeEnvVar(name: String, value: String) {
        try {
            // Windows下使用setx命令
            val builder = ProcessBuilder("cmd.exe", "/c", "setx $name $value")

            val process = builder.start()
            process.waitFor()

            // 打印命令输出
            val reader = BufferedReader(InputStreamReader(process.inputStream,"GBK"))
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                logger.info(line)
            }

            // 打印错误流
            val errorReader = BufferedReader(InputStreamReader(process.errorStream, "GBK"))
            while ((errorReader.readLine().also { line = it }) != null) {
                logger.error(line)
            }

            // 检查是否成功设置了环境变量
            logger.info("$name: " + System.getenv(name))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}
