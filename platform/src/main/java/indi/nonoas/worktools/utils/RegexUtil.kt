package indi.nonoas.worktools.utils

/**
 * @author Nonoas
 * @date 2022/4/13
 */
object RegexUtil {
    fun removeIllegal(str: String): String {
        val regExp: String = "[\n`~!@#$%^&*()+=|{}':;,\\[\\].<>/?！￥…（）—【】‘；：”“’。， 、？]"
        return str.replace(regExp.toRegex(), "")
    }
}
