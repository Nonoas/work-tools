package indi.nonoas.worktools.global

import org.yaml.snakeyaml.Yaml

/**
 * 加载主清单文件
 *
 * @author huangshengsheng
 * @date 2024/6/7 10:43
 */
object Manifest {
    private val manifest = HashMap<String, Any>()

    fun init() {
        val inputStream = Manifest.javaClass.classLoader.getResourceAsStream("manifest.yml")
        val yml = Yaml()
        manifest.putAll(yml.load(inputStream))
    }

    /**
     * 支持递归取值，比如 app.version
     */
    fun get(value: String): Any? {
        val split = value.split(".")
        var rs: Any? = null
        for ((i, k) in split.withIndex()) {
            rs = if (i == 0) {
                manifest[k]!!
            } else {
                (rs as Map<*, *>)[k]
            }
        }
        return rs
    }
}
