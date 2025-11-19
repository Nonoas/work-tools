package indi.nonoas.worktools.config

import org.flywaydb.core.Flyway

/**
 * @author Nonoas
 * @datetime 2022/3/5 20:43
 */
class FlyWayMigration(private val dbConfig: DBConfigEnum) {
    fun migrate() {
        val flyway = Flyway.configure()
            .dataSource(dbConfig.url, dbConfig.username, dbConfig.password)
            .baselineOnMigrate(true)
            .load()
        flyway.migrate()
    }
}
