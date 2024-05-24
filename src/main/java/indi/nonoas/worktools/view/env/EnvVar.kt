package indi.nonoas.worktools.view.env

/**
 * TODO 类描述
 *
 * @author huangshengsheng
 * @date 2024/5/24 13:23
 */
data class EnvVar(
    val id: Int? = null,
    val name: String,
    val content: String,
    val desc: String,
    val createTimestamp: Long ,
    val modTimestamp: Long = System.currentTimeMillis(),
)
