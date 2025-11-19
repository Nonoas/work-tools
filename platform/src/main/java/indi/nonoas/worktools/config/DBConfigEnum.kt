package indi.nonoas.worktools.config

/**
 * @author Nonoas
 * @datetime 2022/3/5 21:04
 */
enum class DBConfigEnum(val url: String, val username: String, val password: String) {
    WORKTOOLS("jdbc:h2:./db/worktools", "worktools", "worktools")

}
