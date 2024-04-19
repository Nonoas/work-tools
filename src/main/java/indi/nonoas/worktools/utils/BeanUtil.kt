package indi.nonoas.worktools.utils

import cn.hutool.core.util.StrUtil
import org.dozer.DozerBeanMapper
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * 数据类转换工具
 *
 * @author Nonoas
 * @date 2022/1/26
 */
object BeanUtil {
    fun <T> map(obj: Any?, clzz: Class<T>?): T {
        val mapper = DozerBeanMapper()
        return mapper.map(obj, clzz)
    }

    fun <T> mapToBean(map: Map<String, Any>, tClass: Class<T>): T? {
        val entries = map.entries
        var obj: T? = null
        try {
            obj = tClass.newInstance()
            for ((key, value1) in entries) {
                var field: Field
                field = try {
                    tClass.getDeclaredField(StrUtil.toCamelCase(key))
                } catch (e: NoSuchFieldException) {
                    continue
                }
                val method = setterMethod(field, tClass) ?: continue
                val value = ensureType(field, value1)
                method.invoke(obj, value)
            }
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return obj
    }

    /**
     * 确保字段值类型匹配
     *
     * @param field 字段
     * @param value 字段值
     * @return 正确类型的字段值
     */
    private fun ensureType(field: Field, value: Any): Any {
        return if (field.type == String::class.java) {
            value.toString()
        } else value
    }

    /**
     * 获取字段对应的 setter方法
     *
     * @param field 字段名
     * @param clazz 类对象
     * @return setter方法
     */
    private fun setterMethod(field: String, clazz: Class<*>): Method? {
        return try {
            val f = clazz.getDeclaredField(field)
            setterMethod(f, clazz)
        } catch (e: NoSuchFieldException) {
            null
        }
    }

    /**
     * 获取字段对应的 setter方法
     *
     * @param field 字段名
     * @param clazz 类对象
     * @return setter方法
     */
    private fun setterMethod(field: Field, clazz: Class<*>): Method? {
        val methodName = "set" + StrUtil.upperFirst(field.name)
        return try {
            clazz.getMethod(methodName, field.type)
        } catch (e: NoSuchMethodException) {
            null
        }
    }
}
