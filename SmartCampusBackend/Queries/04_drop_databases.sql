/***********************************************
  04_drop_databases.sql
  Полностью удаляет тестовые базы SmartCampus и SmartCampusAuth.
  Используй, чтобы вернуть чистую систему перед новым запуском.
  WARNING: это удалит ВСЕ данные в этих БД.
***********************************************/

-- Drop SmartCampus safely
USE master;
GO

IF EXISTS (SELECT * FROM sys.databases WHERE name = N'SmartCampus')
    BEGIN
        PRINT 'Dropping SmartCampus database (forced)...';
        ALTER DATABASE [SmartCampus] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
        DROP DATABASE [SmartCampus];
        PRINT 'SmartCampus dropped.';
    END
ELSE
    PRINT 'SmartCampus not found, skipping drop.';
GO

-- Drop SmartCampusAuth safely
IF EXISTS (SELECT * FROM sys.databases WHERE name = N'SmartCampusAuth')
    BEGIN
        PRINT 'Dropping SmartCampusAuth database (forced)...';
        ALTER DATABASE [SmartCampusAuth] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
        DROP DATABASE [SmartCampusAuth];
        PRINT 'SmartCampusAuth dropped.';
    END
ELSE
    PRINT 'SmartCampusAuth not found, skipping drop.';
GO
