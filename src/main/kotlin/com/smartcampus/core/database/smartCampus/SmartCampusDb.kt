package com.smartcampus.core.database.smartCampus

import com.smartcampus.core.database.base.DataSourceFactory
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

/**
 * Управляет подключением и транзакциями для основной базы данных приложения SmartCampus.
 *
 * Этот объект инкапсулирует инициализацию соединения с основной базой данных,
 * используя [HikariDataSource] и библиотеку Exposed (версии v1).
 * Он предоставляет метод для выполнения запросов к базе данных в рамках транзакций
 * и метод для закрытия пула соединений.
 *
 * Перед использованием необходимо вызвать метод [init] для настройки соединения.
 */
object SmartCampusDb {
    private val log = LoggerFactory.getLogger(SmartCampusDb::class.java)

    private var dataSource: HikariDataSource? = null

    /**
     * Экземпляр базы данных Exposed, используемый для выполнения транзакций с основной БД.
     * Инициализируется в методе [init].
     * Доступен только для чтения извне объекта.
     */
    lateinit var database: Database
        private set

    /**
     * Инициализирует соединение с основной базой данных SmartCampus.
     *
     * Создает [HikariDataSource] с помощью [DataSourceFactory] и предоставленной конфигурации,
     * затем подключается к базе данных, используя Exposed.
     * Имя пула соединений по умолчанию устанавливается как "SmartCampusPool".
     *
     * @param dbConfig Конфигурация приложения Ktor, содержащая параметры подключения
     *                 к основной базе данных. См. документацию [DataSourceFactory.createAndConfigureDataSource]
     *                 для списка ожидаемых свойств.
     * @throws RuntimeException если инициализация базы данных не удалась.
     */
    fun init(dbConfig: ApplicationConfig) {
        log.info("--- Initializing SmartCampusDb ---")
        try {
            dataSource = DataSourceFactory.createAndConfigureDataSource(dbConfig, "SmartCampusPool")
            database = Database.Companion.connect(
                datasource = dataSource!!,
                databaseConfig = DatabaseConfig.Companion {

                }
            )
            log.info("SmartCampusDb initialized and connected using pool '${dataSource?.poolName}'.")
        } catch (e: Exception) {
            log.error("Failed to initialize SmartCampusDb: ${e.message}", e)
            throw RuntimeException("Failed to initialize SmartCampusDb", e)
        }
    }

    /**
     * Выполняет предоставленный блок кода [block] внутри новой транзакции основной базы данных.
     * Транзакция выполняется асинхронно на [Dispatchers.IO].
     *
     * @param T Тип результата, возвращаемого блоком [block].
     * @param block Лямбда-выражение, содержащее операции с базой данных, которые должны быть выполнены
     *              в рамках транзакции. Этот блок будет вызван как `suspend` функция.
     * @return Результат выполнения блока [block].
     * @throws IllegalStateException если база данных не была инициализирована (метод [init] не был вызван).
     */
    suspend fun <T> query(block: suspend () -> T): T {
        if (!::database.isInitialized) {
            throw IllegalStateException("SmartCampusDb has not been initialized. Call init() first.")
        }
        return newSuspendedTransaction(Dispatchers.IO, db = database) {
            block()
        }
    }

    /**
     * Закрывает пул соединений [HikariDataSource] для основной базы данных.
     *
     * Этот метод следует вызывать при остановке приложения для корректного освобождения ресурсов.
     */
    fun close() {
        dataSource?.close()
        log.info("SmartCampusDb connection pool '${dataSource?.poolName}' closed.")
    }
}