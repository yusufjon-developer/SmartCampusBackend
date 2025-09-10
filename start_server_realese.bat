@echo off
REM --- Server Startup Script (Interactive) ---

echo Starting Ktor Server...

REM Запрос JWT_SECRET у пользователя
set /p JWT_SECRET_INPUT="Enter JWT_SECRET: "
if "%JWT_SECRET_INPUT%"=="" (
    echo JWT_SECRET cannot be empty. Exiting.
    pause
    exit /b 1
)

REM Запрос DB_PASSWORD у пользователя
set /p DB_PASSWORD_INPUT="Enter DB_PASSWORD: "
if "%DB_PASSWORD_INPUT%"=="" (
    echo DB_PASSWORD cannot be empty. Exiting.
    pause
    exit /b 1
)

REM Установка переменных окружения для текущей сессии командной строки
set JWT_SECRET=%JWT_SECRET_INPUT%
set DB_PASSWORD=%DB_PASSWORD_INPUT%

echo.
echo JWT_SECRET has been set (for this session).
echo DB_PASSWORD has been set (for this session).
echo.
echo Launching server-all.jar...
echo Make sure 'server-all.jar' is in the same directory as this script,
echo or provide the full path to it.
echo.

REM Запуск JAR файла сервера
REM Предполагается, что server-all.jar находится в той же директории, что и .bat файл.
REM Если нет, замените '.\server-all.jar' на полный путь к файлу.
java -jar .\build\libs\server-all.jar

REM Очистка переменных после завершения работы сервера (опционально, но хорошая практика)
set JWT_SECRET=
set DB_PASSWORD=

echo.
echo Server has been stopped or encountered an error.
pause
