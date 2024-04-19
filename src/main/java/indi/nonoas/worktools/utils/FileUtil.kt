package indi.nonoas.worktools.utils

import org.mozilla.universalchardet.UniversalDetector
import java.io.*


/**
 * 文件工具类
 *
 * @author Nonoas
 */
object FileUtil {
    /**
     * 获取文件格式
     *
     * @param file 文件或文件夹
     * @return 文件格式
     */
    fun getFileType(file: File): String {
        try {
            if (file.isDirectory()) throw Exception("选择节点为文件夹")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val fileName: String = file.getName()
        var type: String = ""
        val index: Int = fileName.lastIndexOf(".") + 1
        if (index > 0) type = fileName.substring(index) // 获取文件格式
        return type
    }

    /**
     * 获取文件内容
     *
     * @param file 文件
     * @return 文本内容
     */
    fun getFileContent(file: File): String? {
        if (!file.exists() || file.length() == 0L) {
            return null
        }
        var content: StringBuilder = StringBuilder(500)
        try {
            val br: BufferedReader = BufferedReader(FileReader(file))
            content = StringBuilder(br.readLine())
            while (br.ready()) {
                content.append("\n").append(br.readLine())
            }
            br.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return content.toString()
    }

    /**
     * 输出字符串到文件
     *
     * @param file    文件
     * @param content 内容
     * @return 成功：true
     */
    fun setFileContent(file: File, content: String?): Boolean {
        if (!file.exists()) return false
        try {
            val bw: BufferedWriter = BufferedWriter(FileWriter(file))
            bw.write(content)
            bw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    /**
     * 递归删除文件或文件夹
     *
     * @param file 文件或文件夹
     * @return 删除成功返回true
     */
    @JvmStatic
    fun deleteFile(file: File): Boolean {
        if (!file.exists()) return false
        if (file.isDirectory()) {
            val files: Array<out File> = file.listFiles() ?: return false
            for (f: File in files) deleteFile(f)
        }
        return file.delete()
    }

    fun ensureDirPath(dirPath: String): String {
        var dirPath: String = dirPath
        val b: Boolean = dirPath.endsWith(File.separator)
        if (!b) {
            dirPath += File.separator
        }
        return dirPath
    }

    fun detectCharset(file: File): String? {
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(4096)
            var bytesRead: Int
            // 创建 UniversalDetector 实例
            val detector = UniversalDetector(null)
            // 循环读取文件内容并交给 UniversalDetector 处理
            while (fis.read(buffer).also { bytesRead = it } != -1 && !detector.isDone) {
                detector.handleData(buffer, 0, bytesRead)
            }
            // 完成处理
            detector.dataEnd()
            // 获取检测到的编码
            val charset: String? = detector.detectedCharset
            // 重置 UniversalDetector
            detector.reset()
            return charset
        }
    }
}
