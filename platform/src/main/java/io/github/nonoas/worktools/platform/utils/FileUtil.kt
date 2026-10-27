package io.github.nonoas.worktools.platform.utils

import org.mozilla.universalchardet.UniversalDetector
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths


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
        if (!file.isFile || file.length() == 0L) return null

        detectBom(file)?.let { return it }

        FileInputStream(file).use { fis ->
            val buffer = ByteArray(4096)
            var bytesRead: Int
            val detector = UniversalDetector(null)
            while (fis.read(buffer).also { bytesRead = it } != -1 && !detector.isDone) {
                detector.handleData(buffer, 0, bytesRead)
            }
            detector.dataEnd()
            detector.detectedCharset?.let { return it }
        }

        if (canDecodeAsText(file, StandardCharsets.UTF_8)) {
            return StandardCharsets.UTF_8.name()
        }

        val gbk = Charset.forName("GBK")
        if (canDecodeAsText(file, gbk, requireHanCharacter = true)) {
            return gbk.name()
        }

        return null
    }

    private fun detectBom(file: File): String? {
        val bom = ByteArray(4)
        val length = FileInputStream(file).use { it.read(bom) }
        if (length >= 4) {
            if (bom[0] == 0x00.toByte() && bom[1] == 0x00.toByte() &&
                bom[2] == 0xFE.toByte() && bom[3] == 0xFF.toByte()
            ) return "UTF-32BE"
            if (bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte() &&
                bom[2] == 0x00.toByte() && bom[3] == 0x00.toByte()
            ) return "UTF-32LE"
        }
        if (length >= 3 && bom[0] == 0xEF.toByte() &&
            bom[1] == 0xBB.toByte() && bom[2] == 0xBF.toByte()
        ) return StandardCharsets.UTF_8.name()
        if (length >= 2) {
            if (bom[0] == 0xFE.toByte() && bom[1] == 0xFF.toByte()) return StandardCharsets.UTF_16BE.name()
            if (bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte()) return StandardCharsets.UTF_16LE.name()
        }
        return null
    }

    private fun canDecodeAsText(
        file: File,
        charset: Charset,
        requireHanCharacter: Boolean = false
    ): Boolean {
        val decoder = charset.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        var hasCharacter = false
        var hasHanCharacter = false

        return try {
            InputStreamReader(FileInputStream(file), decoder).use { reader ->
                val buffer = CharArray(4096)
                var count: Int
                while (reader.read(buffer).also { count = it } != -1) {
                    for (index in 0 until count) {
                        val char = buffer[index]
                        hasCharacter = true
                        if (Character.isISOControl(char) && char != '\r' && char != '\n' && char != '\t') {
                            return false
                        }
                        if (char in '\u3400'..'\u4DBF' || char in '\u4E00'..'\u9FFF' || char in '\uF900'..'\uFAFF') {
                            hasHanCharacter = true
                        }
                    }
                }
            }
            hasCharacter && (!requireHanCharacter || hasHanCharacter)
        } catch (_: CharacterCodingException) {
            false
        }
    }

    @JvmStatic
    fun readTextFromProjectRelativePath(relativePath: String): String {
        val currentDir = Paths.get(System.getProperty("user.dir"))
        val absolutePath = currentDir.resolve(relativePath)
        if (!Files.exists(absolutePath)) {
            throw FileNotFoundException("文件不存在: ${absolutePath.toAbsolutePath()}")
        }
        return Files.readString(absolutePath)
    }
}
