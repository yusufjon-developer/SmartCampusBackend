package com.smartcampus.data.database.base

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.slf4j.LoggerFactory

class DataSourceFactory {
    private val log = LoggerFactory.getLogger(DataSourceFactory::class.java)

    fun createAndConfigureDataSource(
        dbConfig: ApplicationConfig,
        defaultPoolName: String
    ): HikariDataSource {
        val host = dbConfig.property("host").getString()
        val port = dbConfig.property("port").getString()
        val name = dbConfig.property("name").getString()
        val user = dbConfig.property("user").getString()
        val password = dbConfig.property("password").getString()

        val encrypt = dbConfig.propertyOrNull("encrypt")?.getString()?.toBooleanStrictOrNull() ?: false
        val trustCertificate = dbConfig.propertyOrNull("trustServerCertificate")?.getString()?.toBooleanStrictOrNull() ?: true
        val loginTimeout = dbConfig.propertyOrNull("loginTimeout")?.getString()?.toIntOrNull() ?: 30

        var jdbcUrl = "jdbc:sqlserver://${host}:${port};databaseName=${name}"
        jdbcUrl += ";encrypt=${encrypt}"
        jdbcUrl += ";trustServerCertificate=${trustCertificate}"
        jdbcUrl += ";loginTimeout=${loginTimeout}"

        log.debug("JDBC URL for {}: {}", defaultPoolName, jdbcUrl.replaceAfter("password=", "****"))

        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = password
            this.driverClassName = dbConfig.propertyOrNull("driver")?.getString()
                ?: "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            this.maximumPoolSize = dbConfig.propertyOrNull("maxPoolSize")?.getString()?.toIntOrNull() ?: 10
            this.minimumIdle = dbConfig.propertyOrNull("minimumIdle")?.getString()?.toIntOrNull() ?: 5
            this.connectionTimeout = dbConfig.propertyOrNull("connectionTimeout")?.getString()?.toLongOrNull() ?: 30000L
            this.idleTimeout = dbConfig.propertyOrNull("idleTimeout")?.getString()?.toLongOrNull() ?: 600000L
            this.maxLifetime = dbConfig.propertyOrNull("maxLifetime")?.getString()?.toLongOrNull() ?: 1800000L
            this.poolName = dbConfig.propertyOrNull("poolName")?.getString() ?: defaultPoolName
            this.connectionTestQuery = "SELECT 1"
        }
        return HikariDataSource(hikariConfig)
    }

}