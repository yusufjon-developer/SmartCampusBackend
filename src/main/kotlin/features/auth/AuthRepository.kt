package com.smartcampus.features.auth

import com.smartcampus.core.database.auth.SmartCampusAuthDb
import com.smartcampus.core.database.auth.entities.UserDevicesTable
import com.smartcampus.core.database.auth.entities.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

/**
 * Репозиторий для взаимодействия с данными, связанными с аутентификацией и пользователями,
 * в базе данных. Предоставляет методы для поиска, создания и обновления записей пользователей
 * и их устройств.
 *
 * @property db Экземпляр [SmartCampusAuthDb] для выполнения запросов к базе данных аутентификации.
 * @constructor Создает экземпляр AuthRepository.
 */
class AuthRepository(
    private val db: SmartCampusAuthDb
) {

    /**
     * Находит пользователя в базе данных по его имени пользователя (логину).
     *
     * @param username Имя пользователя для поиска.
     * @return [ResultRow], представляющий найденного пользователя, или `null`, если пользователь не найден.
     */
    suspend fun findUserByUsername(username: String): ResultRow? = db.query {
        UsersTable.selectAll().where { UsersTable.username eq username }.singleOrNull()
    }

    /**
     * Находит пользователя в базе данных по его адресу электронной почты.
     *
     * @param email Адрес электронной почты для поиска.
     * @return [ResultRow], представляющий найденного пользователя, или `null`, если пользователь не найден.
     */
    suspend fun findUserByEmail(email: String): ResultRow? = db.query {
        UsersTable.selectAll().where { UsersTable.email eq email }.singleOrNull()
    }

    /**
     * Находит пользователя в базе данных по его уникальному идентификатору.
     *
     * @param userId Уникальный идентификатор пользователя для поиска.
     * @return [ResultRow], представляющий найденного пользователя, или `null`, если пользователь не найден.
     */
    suspend fun findUserById(userId: Int): ResultRow? = db.query {
        UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()
    }

    /**
     * Находит запись об устройстве пользователя по идентификатору пользователя и UUID устройства.
     *
     * @param userId Идентификатор пользователя, которому принадлежит устройство.
     * @param deviceUuid Уникальный идентификатор устройства.
     * @return [ResultRow], представляющий найденное устройство, или `null`, если устройство не найдено.
     */
    suspend fun findUserDevice(userId: Int, deviceUuid: String): ResultRow? = db.query {
        UserDevicesTable.selectAll().where {
            (UserDevicesTable.userId eq userId) and (UserDevicesTable.deviceUuid eq deviceUuid)
        }.singleOrNull()
    }

    /**
     * Обновляет время последнего входа для указанного устройства.
     *
     * @param deviceId Уникальный идентификатор записи устройства в таблице `UserDevicesTable`.
     *                 Предполагается, что это первичный ключ таблицы устройств.
     */
    suspend fun updateUserDeviceLastLogin(deviceId: Int) = db.query {
        UserDevicesTable.update({ UserDevicesTable.id eq deviceId }) {
            it[lastLoginAt] = LocalDateTime.now()
        }
    }

    /**
     * Находит одобренное устройство пользователя по типу устройства.
     * Используется для логики, где у пользователя может быть только одно одобренное устройство
     * определенного типа (например, один "PC" и один "Mobile").
     *
     * @param userId Идентификатор пользователя.
     * @param deviceType Тип устройства для поиска (например, "PC", "Mobile").
     * @return [ResultRow], представляющий найденное одобренное устройство, или `null`, если таковое отсутствует.
     */
    suspend fun findApprovedDeviceByType(userId: Int, deviceType: String): ResultRow? = db.query {
        UserDevicesTable.selectAll().where {
            (UserDevicesTable.userId eq userId) and
                    (UserDevicesTable.deviceType eq deviceType) and
                    (UserDevicesTable.isApproved eq true)
        }.singleOrNull()
    }

    /**
     * Отзывает одобрение для указанного устройства.
     * Устанавливает флаг `isApproved` в `false` и очищает поля, связанные с одобрением.
     *
     * @param deviceId Уникальный идентификатор записи устройства в таблице `UserDevicesTable`.
     */
    suspend fun revokeDeviceApproval(deviceId: Int) = db.query {
        UserDevicesTable.update({ UserDevicesTable.id eq deviceId }) {
            it[isApproved] = false
            it[approvedAt] = null
            it[approvedByUserId] = null
        }
    }

    /**
     * Регистрирует новое устройство для пользователя.
     * Изначально устройство регистрируется как неодобренное (`isApproved = false`).
     *
     * @param userId Идентификатор пользователя, для которого регистрируется устройство.
     * @param deviceUuid Уникальный идентификатор нового устройства.
     * @param deviceType Тип нового устройства.
     * @return Уникальный идентификатор (ID) вновь созданной записи об устройстве в таблице `UserDevicesTable`.
     */
    suspend fun registerNewDevice(
        userId: Int,
        deviceUuid: String,
        deviceType: String
    ): Int = db.query {
        UserDevicesTable.insert {
            it[UserDevicesTable.userId] = userId
            it[UserDevicesTable.deviceUuid] = deviceUuid
            it[UserDevicesTable.deviceType] = deviceType
            it[UserDevicesTable.isApproved] = false
            it[UserDevicesTable.lastLoginAt] = LocalDateTime.now()
        } [UserDevicesTable.id].value
    }

    /**
     * Создает новую запись пользователя в базе данных.
     *
     * @param username Уникальное имя пользователя (логин).
     * @param email Уникальный адрес электронной почты.
     * @param passwordHash Хешированный пароль пользователя.
     * @param fullName Полное имя пользователя.
     * @param role Роль пользователя в системе.
     * @param isActive Флаг активности пользователя (по умолчанию `true`).
     * @return Уникальный идентификатор (ID) вновь созданного пользователя.
     */
    suspend fun createUser(
        username: String,
        email: String,
        passwordHash: String,
        fullName: String,
        role: String,
        isActive: Boolean = true
    ): Int {
        return db.query {
            UsersTable.insertAndGetId { statement ->
                statement[UsersTable.username] = username
                statement[UsersTable.passwordHash] = passwordHash
                statement[UsersTable.email] = email
                statement[UsersTable.fullName] = fullName
                statement[UsersTable.role] = role
                statement[UsersTable.isActive] = isActive
                statement[UsersTable.createdAt] = LocalDateTime.now()
            }.value
        }
    }
}