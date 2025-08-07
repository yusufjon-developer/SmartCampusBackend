@echo off
REM --- Restore SQL Server Databases ---

REM --- User Input ---
echo --- SQL Server Connection Details ---
set /p SQL_INSTANCE_NAME_INPUT=Enter SQL Server instance name (e.g., DESKTOP-3PP7MPF or .\SQLEXPRESS):
IF "%SQL_INSTANCE_NAME_INPUT%"=="" (
    echo ERROR: SQL Server instance name cannot be empty.
    goto HandleErrorNoPause
)

echo Enter SQL Server SA username (default: sa). Press Enter for default:
set /p SQL_USERNAME_INPUT=
IF "%SQL_USERNAME_INPUT%"=="" SET SQL_USERNAME_INPUT=sa

echo Enter password for %SQL_USERNAME_INPUT% on %SQL_INSTANCE_NAME_INPUT%:
REM Используем вспомогательный vbs для скрытого ввода пароля
echo Option Explicit                                > %temp%\getpassword.vbs
echo Dim Pass                                      >> %temp%\getpassword.vbs
echo Pass = InputBox("Enter password for %SQL_USERNAME_INPUT% on %SQL_INSTANCE_NAME_INPUT%", "Password Input") >> %temp%\getpassword.vbs
echo WScript.StdOut.Write(Pass)                    >> %temp%\getpassword.vbs

FOR /F "usebackq delims=" %%P IN (`CSCRIPT //nologo %temp%\getpassword.vbs`) DO SET SQL_PASSWORD_INPUT=%%P
DEL %temp%\getpassword.vbs
IF "%SQL_PASSWORD_INPUT%"=="" (
    echo WARNING: Password was not entered. Continuing with empty password if intended.
)
echo.
REM --- End User Input ---


REM --- Configuration ---
SET SOURCE_BACKUP_DIR_PROJECT=SmartCampusBackend\DbBackUp

SET DB_NAME_1=SmartCampus
SET DB_NAME_2=SmartCampusAuth

SET BACKUP_FILE_NAME_1=%DB_NAME_1%.bak
SET BACKUP_FILE_NAME_2=%DB_NAME_2%.bak
REM --- End Configuration ---

echo --- Restore Script Start ---
echo Using SQL Server instance: %SQL_INSTANCE_NAME_INPUT%
echo Using SQL User: %SQL_USERNAME_INPUT%
echo.

IF NOT EXIST "%SOURCE_BACKUP_DIR_PROJECT%\%BACKUP_FILE_NAME_1%" (
    echo ERROR: Backup file "%SOURCE_BACKUP_DIR_PROJECT%\%BACKUP_FILE_NAME_1%" not found!
    goto HandleError
)
IF NOT EXIST "%SOURCE_BACKUP_DIR_PROJECT%\%BACKUP_FILE_NAME_2%" (
    echo ERROR: Backup file "%SOURCE_BACKUP_DIR_PROJECT%\%BACKUP_FILE_NAME_2%" not found!
    goto HandleError
)

REM --- Восстановление базы данных SmartCampus ---
echo 1. Restoring database: %DB_NAME_1% from "%SOURCE_BACKUP_DIR_PROJECT%\%BACKUP_FILE_NAME_1%"
echo    Attempting to set database %DB_NAME_1% to single user mode...
sqlcmd -S %SQL_INSTANCE_NAME_INPUT% -U %SQL_USERNAME_INPUT% -P "%SQL_PASSWORD_INPUT%" -d master -Q "IF DB_ID('%DB_NAME_1%') IS NOT NULL BEGIN ALTER DATABASE [%DB_NAME_1%] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; END" -b -r0
IF %ERRORLEVEL% NEQ 0 (
    echo    WARNING: Failed to set database %DB_NAME_1% to single user mode or database does not exist. Errorlevel: %ERRORLEVEL%. Continuing restore attempt...
)

echo    Restoring %DB_NAME_1%...
sqlcmd -S %SQL_INSTANCE_NAME_INPUT% -U %SQL_USERNAME_INPUT% -P "%SQL_PASSWORD_INPUT%" -d master -Q "RESTORE DATABASE [%DB_NAME_1%] FROM DISK = N'%CD%\%SOURCE_BACKUP_DIR_PROJECT%\%BACKUP_FILE_NAME_1%' WITH FILE = 1, NOUNLOAD, REPLACE, STATS = 5" -b -r0
IF %ERRORLEVEL% NEQ 0 (
    echo    ERROR: Failed to restore database %DB_NAME_1%. Errorlevel: %ERRORLEVEL%
    sqlcmd -S %SQL_INSTANCE_NAME_INPUT% -U %SQL_USERNAME_INPUT% -P "%SQL_PASSWORD_INPUT%" -d master -Q "IF DB_ID('%DB_NAME_1%') IS NOT NULL BEGIN ALTER DATABASE [%DB_NAME_1%] SET MULTI_USER; END"
    goto HandleError
) ELSE (
    echo    Database %DB_NAME_1% restored successfully.
)
echo    Setting database %DB_NAME_1% back to multi user mode...
sqlcmd -S %SQL_INSTANCE_NAME_INPUT% -U %SQL_USERNAME_INPUT% -P "%SQL_PASSWORD_INPUT%" -d master -Q "IF DB_ID('%DB_NAME_1%') IS NOT NULL BEGIN ALTER DATABASE [%DB_NAME_1%] SET MULTI_USER; END" -b -r0
echo.

REM --- Восстановление базы данных SmartCampusAuth ---
echo 2. Restoring database: %DB_NAME_2% from "%SOURCE_BACKUP_DIR_PROJECT%\%BACKUP_FILE_NAME_2%"
echo    Attempting to set database %DB_NAME_2% to single user mode...
sqlcmd -S %SQL_INSTANCE_NAME_INPUT% -U %SQL_USERNAME_INPUT% -P "%SQL_PASSWORD_INPUT%" -d master -Q "IF DB_ID('%DB_NAME_2%') IS NOT NULL BEGIN ALTER DATABASE [%DB_NAME_2%] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; END" -b -r0
IF %ERRORLEVEL% NEQ 0 (
    echo    WARNING: Failed to set database %DB_NAME_2% to single user mode or database does not exist. Errorlevel: %ERRORLEVEL%. Continuing restore attempt...
)

echo    Restoring %DB_NAME_2%...
sqlcmd -S %SQL_INSTANCE_NAME_INPUT% -U %SQL_USERNAME_INPUT% -P "%SQL_PASSWORD_INPUT%" -d master -Q "RESTORE DATABASE [%DB_NAME_2%] FROM DISK = N'%CD%\%SOURCE_BACKUP_DIR_PROJECT%\%BACKUP_FILE_NAME_2%' WITH FILE = 1, NOUNLOAD, REPLACE, STATS = 5" -b -r0
IF %ERRORLEVEL% NEQ 0 (
    echo    ERROR: Failed to restore database %DB_NAME_2%. Errorlevel: %ERRORLEVEL%
    sqlcmd -S %SQL_INSTANCE_NAME_INPUT% -U %SQL_USERNAME_INPUT% -P "%SQL_PASSWORD_INPUT%" -d master -Q "IF DB_ID('%DB_NAME_2%') IS NOT NULL BEGIN ALTER DATABASE [%DB_NAME_2%] SET MULTI_USER; END"
    goto HandleError
) ELSE (
    echo    Database %DB_NAME_2% restored successfully.
)
echo    Setting database %DB_NAME_2% back to multi user mode...
sqlcmd -S %SQL_INSTANCE_NAME_INPUT% -U %SQL_USERNAME_INPUT% -P "%SQL_PASSWORD_INPUT%" -d master -Q "IF DB_ID('%DB_NAME_2%') IS NOT NULL BEGIN ALTER DATABASE [%DB_NAME_2%] SET MULTI_USER; END" -b -r0
echo.

echo --- Restore Script Finished Successfully ---
goto EndScript

:HandleErrorNoPause
echo Script aborted.
exit /b 1

:HandleError
echo.
echo !!! AN ERROR OCCURRED DURING RESTORE !!!
echo Please check the output above for details.
echo.
pause

:EndScript
pause