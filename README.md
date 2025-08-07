# 🧾 SmartCampus Backend – Startup Guide

## 📦 Requirements
- **Java 17+**
- **SQL Server** (Express or other)
- **Gradle** (or use `gradlew.bat`)
- **Windows OS**
- Administrator rights for database restoration

---

## ⚙️ Startup Steps

### 1. 🔄 Restore Databases

Open the terminal **as administrator**, then run:

```bash
databases_restore.bat
```

Follow the prompts:
- Enter the SQL instance name (e.g., `.\SQLEXPRESS`)
- Enter the username (default: `sa`)
- Enter the password for `sa`

Two databases will be restored:
- `SmartCampus`
- `SmartCampusAuth`

> ⚠️ Make sure the files `SmartCampus.bak` and `SmartCampusAuth.bak` are located in the folder `SmartCampusBackend\DbBackUp`

---

### 2. 🛠️ Build the Server

After successful database restoration, execute:

```bash
gradlew.bat build
```

or, if Gradle is installed globally:

```bash
gradle build
```

---

### 3. 🚀 Start the Server

To start, use:

```bash
start_server_release.bat
```

You will be asked to provide:
- `JWT_SECRET`
- `DB_PASSWORD` — same as the `sa` user password

**Example values for testing:**

```env
JWT_SECRET=B&ERaiVz[n+x7_xWE#K82AOzuTh&{vh6
DB_PASSWORD=<sa user password>
```

---

## 📁 Project Structure

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

## 📌 Notes

- Environment variables work only within the current `cmd` window.
- To automate startup, you can use `.env` files or `setx` (use with caution).
- Ensure that SQL Server ports and Ktor server ports are free and not blocked.

---

## ❓ Support

Having issues?
- Check that you run `sqlcmd` with administrator rights
- Make sure `server-all.jar` is built and located in `build/libs`
- Ensure the SQL instance is accessible and port `1433` is free
