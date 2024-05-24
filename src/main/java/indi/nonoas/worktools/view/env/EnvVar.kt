package indi.nonoas.worktools.view.env

/**
 * TODO 类描述
 *
 * @author huangshengsheng
 * @date 2024/5/24 13:23
 */
class EnvVar {
    var id: Int? = null
    var name: String? = null
    var content: String? = null
    var desc: String? = null
    var createTimestamp: Long? = null
    var modTimestamp: Long = System.currentTimeMillis()
}
