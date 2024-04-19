package indi.nonoas.worktools.utils

import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * 配置文件工具类
 */
object PropertiesUtil {

    /**
     * 从外部路径读取配置文件
     */
    @Throws(IOException::class)
    fun getProperties(path: String): Properties {
        val properties = Properties()
        val reader = InputStreamReader(FileInputStream(path), StandardCharsets.UTF_8)
        properties.load(reader)
        return properties
    }

    @Throws(IOException::class)
    fun getInnerProperties(path: String): Properties {
        val properties = Properties()
        val inputStream = javaClass.classLoader.getResourceAsStream(path) ?: throw IOException("找不到指定url")
        properties.load(inputStream)
        return properties
    }
}