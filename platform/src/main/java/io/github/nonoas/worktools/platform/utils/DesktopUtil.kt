package io.github.nonoas.worktools.platform.utils

import cn.hutool.core.util.URLUtil
import io.github.nonoas.worktools.platform.ui.component.ExceptionAlter
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.io.File
import java.nio.charset.Charset

/**
 * 桌面能力工具类。
 *
 * @author Nonoas
 * @datetime 2022/5/15 22:03
 */
object DesktopUtil {

    private val logger = LogManager.getLogger(DesktopUtil::class.java)
    private val commandCharset: Charset = Charset.forName("GBK")

    /**
     * 外部命令执行结果。
     */
    data class CommandResult(
        val exitCode: Int,
        val standardOutput: String,
        val errorOutput: String
    ) {
        /**
         * 判断命令是否执行成功。
         */
        fun isSuccess(): Boolean = exitCode == 0

        /**
         * 将结果格式化为便于排查问题的详细提示。
         *
         * @param action 当前执行的动作描述
         * @return 带标准输出和错误输出的详细说明
         */
        fun toDetailedMessage(action: String): String {
            return buildString {
                append(action).append("，退出码: ").append(exitCode)
                if (standardOutput.isNotBlank()) {
                    append("\n标准输出:\n").append(standardOutput)
                }
                if (errorOutput.isNotBlank()) {
                    append("\n错误输出:\n").append(errorOutput)
                }
            }
        }
    }

    /**
     * 使用系统默认程序打开文件。
     *
     * @param file 要打开的文件
     */
    @JvmStatic
    fun open(file: File?) {
        try {
            Desktop.getDesktop().open(file)
        } catch (e: Exception) {
            ExceptionAlter.error(e)
        }
    }

    /**
     * 使用系统浏览器打开指定链接。
     *
     * @param url 目标链接
     */
    @JvmStatic
    fun browse(url: String) {
        try {
            Desktop.getDesktop().browse(URLUtil.url(url).toURI())
        } catch (e: Exception) {
            ExceptionAlter.error(e)
        }
    }

    /**
     * 设置当前用户级环境变量。
     *
     * @param name 环境变量名
     * @param value 环境变量值
     * @return `setx` 命令执行结果
     * @throws IllegalArgumentException 参数为空时抛出
     * @throws IllegalStateException `setx` 执行失败时抛出
     */
    fun changeEnvVar(name: String, value: String): CommandResult {
        require(name.isNotBlank()) { "环境变量名不能为空。" }
        require(value.isNotBlank()) { "环境变量值不能为空。" }

        val process = ProcessBuilder("setx", name, value).start()
        val standardOutput = process.inputStream.bufferedReader(commandCharset).use { it.readText().trim() }
        val errorOutput = process.errorStream.bufferedReader(commandCharset).use { it.readText().trim() }
        val exitCode = process.waitFor()

        if (standardOutput.isNotBlank()) {
            logger.info(standardOutput)
        }
        if (errorOutput.isNotBlank()) {
            logger.error(errorOutput)
        }

        return CommandResult(exitCode, standardOutput, errorOutput).also { result ->
            if (!result.isSuccess()) {
                throw IllegalStateException(result.toDetailedMessage("设置环境变量 $name 失败"))
            }
        }
    }
}
