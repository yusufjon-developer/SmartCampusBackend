USE SmartCampusAuth;
GO

CREATE PROCEDURE GrantPermissionIfSecurityOfficer
    @granted_by INT,
    @granted_to INT,
    @permission_name NVARCHAR(100),
    @expires_at DATETIME = NULL,
    @comment NVARCHAR(255) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    -- Проверка, является ли выдавший сотрудником отдела ИБ
    IF NOT EXISTS (
        SELECT 1
        FROM User_Departments ud
        JOIN Departments d ON ud.department_id = d.id
        WHERE ud.user_id = @granted_by
          AND d.name = N'Информационная безопасность'
    )
    BEGIN
        RAISERROR(N'Выдавать права может только сотрудник отдела ИБ.', 16, 1);
        RETURN;
    END

    DECLARE @permission_id INT;
    SELECT @permission_id = id FROM Permissions WHERE name = @permission_name;

    IF @permission_id IS NULL
    BEGIN
        RAISERROR(N'Указанное право не существует.', 16, 1);
        RETURN;
    END

    INSERT INTO Access_Grants (granted_by, granted_to, permission_id, expires_at, comment)
    VALUES (@granted_by, @granted_to, @permission_id, @expires_at, @comment);
END;
