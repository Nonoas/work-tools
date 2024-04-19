package indi.nonoas.worktools.common

/**
 * @author Nonoas
 * @datetime 2022/5/14 13:49
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DataBind(
    /**
     * 绑定方式，UI属性名:数据属性名
     *
     * @return 例如 text:name
     */
    val value: String
)
