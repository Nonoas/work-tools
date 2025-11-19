package indi.nonoas.worktools.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Nonoas
 * @date 2021/11/24
 */
object DateTimeUtil {
    val currDate: Int
        /**
         * 获取当前日期
         * @return 格式 yyyyMMdd
         */
        get() {
            val date: Date = Date()
            val sdf: SimpleDateFormat = SimpleDateFormat("yyyyMMdd")
            return sdf.format(date).toInt()
        }
}
