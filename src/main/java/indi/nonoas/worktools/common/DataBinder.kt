package indi.nonoas.worktools.common

import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.scene.Parent
import javafx.scene.control.TextField
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.stream.Collectors

/**
 * @author Nonoas
 * @datetime 2022/5/14 14:01
 */
object DataBinder {
    @JvmStatic
    fun <T> getBindModel(parent: Parent, clazz: Class<T>): T? {
        try {
            val t = clazz.newInstance()
            val pClazz: Class<out Parent> = parent.javaClass
            val bindingField = Arrays.stream(pClazz.getDeclaredFields())
                .filter { f: Field ->
                    null != f.getAnnotation(
                        DataBind::class.java
                    )
                }
                .collect(Collectors.toList())
            bindData(parent, bindingField, t)
            return t
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }

    private fun <T> bindData(parent: Parent, bindingField: List<Field>, t: T) {
        var annotation: DataBind
        var split: Array<String>
        for (field in bindingField) {
            annotation = field.getAnnotation(DataBind::class.java)
            split = splitBindRule(annotation)
            try {
                field.setAccessible(true)
                val fromProperty = getProperty(split[0], field[parent])
                val toProperty = getProperty(split[1], t as Any)
                toProperty.bind(fromProperty)
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class)
    private fun getProperty(propertyName: String, o: Any): Property<out Nothing> {
        val proFrom = o.javaClass.getMethod(propertyName + "Property")
        return proFrom.invoke(o) as Property<out Nothing>
    }

    private fun splitBindRule(annotation: DataBind): Array<String> {
        val split = annotation.value.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        require(split.size == 2) { "注解 DataBind 的 value 值格式为 [被绑定属性名:绑定数据属性名]" }
        return split
    }
}
