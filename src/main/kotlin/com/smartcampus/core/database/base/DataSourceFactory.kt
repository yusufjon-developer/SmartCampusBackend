package com.smartcampus.core.database.base

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.slf4j.LoggerFactory

/**
 * Фабрика для создания и конфигурации источников данных (DataSource) для подключения к базе данных.
 * Использует HikariCP для управления пулом соединений.
 */
object DataSourceFactory {
    private val log = LoggerFactory.getLogger(DataSourceFactory::class.java)

    /**
     * Создает и конфигурирует экземпляр [HikariDataSource] на основе предоставленной конфигурации.
     *
     * Эта функция читает параметры подключения к базе данных (хост, порт, имя БД, пользователь, пароль)
     * и другие настройки пула соединений (например, размер пула, таймауты) из объекта [ApplicationConfig].
     * Формирует JDBC URL для MS SQL Server и инициализирует [HikariConfig] с этими параметрами.
     *
     * @param dbConfig Конфигурация приложения Ktor ([ApplicationConfig]), содержащая параметры
     *                 подключения к базе данных. Ожидается, что эти параметры будут находиться
     *                 в соответствующей секции конфигурационного файла (например, `database.host`).
     *                 Свойства, считываемые из `dbConfig`:
     *                 **Обязательные:**
     *                 - `host` (String): Адрес сервера базы данных.
     *                 - `port` (String): Порт сервера базы данных.
     *                 - `name` (String): Имя базы данных.
     *                 - `user` (String): Имя пользователя для подключения.
     *                 - `password` (String): Пароль пользователя.
     *                 **Опциональные (имеют значения по умолчанию):**
     *                 - `encrypt` (Boolean, по умолчанию `false`): Указывает, следует ли использовать шифрование (TLS/SSL)
     *                                                           для соединения.
     *                 - `trustServerCertificate` (Boolean, по умолчанию `true`): Указывает, следует ли доверять сертификату сервера
     *                                                                      без его проверки в хранилище доверенных сертификатов.
     *                                                                      **Внимание:** В производственной среде рекомендуется
     *                                                                      устанавливать в `false` и настраивать доверенные сертификаты.
     *                 - `loginTimeout` (Int, секунды, по умолчанию `30`): Максимальное время ожидания (в секундах)
     *                                                                 при установлении соединения.
     *                 - `driver` (String, по умолчанию `"com.microsoft.sqlserver.jdbc.SQLServerDriver"`):
     *                                  Полное имя класса JDBC драйвера.
     *                 - `maxPoolSize` (Int, по умолчанию `10`): Максимальное количество активных соединений,
     *                                                       которое может поддерживать пул.
     *                 - `minimumIdle` (Int, по умолчанию `5`): Минимальное количество простаивающих соединений,
     *                                                      которое HikariCP будет стараться поддерживать в пуле.
     *                 - `connectionTimeout` (Long, миллисекунды, по умолчанию `30000`): Максимальное время ожидания
     *                                                                              при получении соединения из пула.
     *                 - `idleTimeout` (Long, миллисекунды, по умолчанию `600000`): Максимальное время, в течение которого
     *                                                                         соединение может простаивать в пуле
     *                                                                         перед тем, как оно будет закрыто (если количество
     *                                                                         соединений превышает `minimumIdle`).
     *                 - `maxLifetime` (Long, миллисекунды, по умолчанию `1800000`): Максимальное время жизни соединения в пуле.
     *                                                                          По истечении этого времени соединение будет закрыто
     *                                                                          (даже если оно активно), чтобы избежать проблем
     *                                                                          со "старыми" соединениями.
     *                 - `poolName` (String, по умолчанию значение из `defaultPoolName`): Имя, присваиваемое пулу соединений.
     *                                                                             Полезно для идентификации в логах и JMX.
     * @param defaultPoolName Имя пула соединений, используемое по умолчанию, если оно не указано
     *                        в `dbConfig` через свойство `poolName`.
     * @return Сконфигурированный и готовый к использованию экземпляр [HikariDataSource].
     */
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