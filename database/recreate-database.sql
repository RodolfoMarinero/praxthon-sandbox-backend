USE master;
GO
IF DB_ID(N'praxthon_sandbox') IS NOT NULL
BEGIN
    ALTER DATABASE praxthon_sandbox
    SET SINGLE_USER
    WITH ROLLBACK IMMEDIATE;
    DROP DATABASE praxthon_sandbox;
END;
GO
CREATE DATABASE praxthon_sandbox;
GO
