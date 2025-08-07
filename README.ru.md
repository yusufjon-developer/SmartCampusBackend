# 🧾 SmartCampus Backend – Инструкция по запуску

## 📦 Требования
- **Java 17+**
- **SQL Server** (Express или другой)
- **Gradle** (или используйте `gradlew.bat`)
- **Windows OS**
- Права администратора для восстановления БД

---

## ⚙️ Этапы запуска

### 1. 🔄 Восстановление баз данных

Откройте терминал **от имени администратора**, затем:

```bash
databases_restore.bat
```

Следуйте подсказкам:
- Введите имя SQL-инстанса (например, `.\SQLEXPRESS`)
- Введите имя пользователя (по умолчанию: `sa`)
- Введите пароль пользователя `sa`

Будут восстановлены две базы данных:
- `SmartCampus`
- `SmartCampusAuth`

> ⚠️ Убедитесь, что файлы `SmartCampus.bak` и `SmartCampusAuth.bak` находятся в папке `SmartCampusBackend\DbBackUp`

---

### 2. 🛠️ Сборка сервера

После успешного восстановления баз данных выполните:

```bash
gradlew.bat build
```

или, если Gradle установлен глобально:

```bash
gradle build
```

---

### 3. 🚀 Запуск сервера

Для запуска используйте:

```bash
start_server_release.bat
```

Будет запрошено:
- `JWT_SECRET`
- `DB_PASSWORD` — тот же, что и для `sa`

**Примерные значения для теста:**

```env
JWT_SECRET=B&ERaiVz[n+x7_xWE#K82AOzuTh&{vh6
DB_PASSWORD=<пароль пользователя sa>
```

---

## 📁 Структура проекта

```
/
├── databases_restore.bat
├── start_server_release.bat
├── build/libs/server-all.jar
├── SmartCampusBackend/
│   └── DbBackUp/
│       ├── SmartCampus.bak
│       └── SmartCampusAuth.bak
├── gradlew.bat
└── README.md
```

---

## 📌 Заметки

- Переменные окружения работают только в пределах текущего окна `cmd`.
- Если нужно автоматизировать запуск, можно использовать `.env` или `setx` (с осторожностью).
- Убедитесь, что порты SQL Server и сервера Ktor свободны и не заблокированы.

---

## ❓ Поддержка

Возникли проблемы?
- Проверьте, что используете `sqlcmd` с правами администратора
- Убедитесь, что `server-all.jar` сгенерирован и находится в `build/libs`
- Убедитесь, что SQL-инстанс доступен и порт `1433` не занят

---
