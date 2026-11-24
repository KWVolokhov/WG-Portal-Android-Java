# Протокол синхронизации: Параметры (CALPARAM)

## 1. Направление

**На сервер** — клиент выгружает на сервер, обратная корректировка локально — только поле `SyncTime`.

## 2. Фильтр "Etot phone"

- Если `Vedushii = "Etot phone"`, запись CALPARAM не синхронизируется на сервер.

## 3. SQL DDL — Сервер (MSSQL)

```sql
CREATE TABLE dbo.CALPARAM (
    id INT IDENTITY(1,1) PRIMARY KEY,
    Address NVARCHAR(500),
    Name NVARCHAR(200),
    Password NVARCHAR(200),
    Vedushii NVARCHAR(200),
    VedushiiID NVARCHAR(100),
    StartPage NVARCHAR(50),
    Button1Id NVARCHAR(50),
    Button2Id NVARCHAR(50),
    Button3Id NVARCHAR(50),
    Button4Id NVARCHAR(50),
    Button5Id NVARCHAR(50),
    Height INT,
    Weight INT,
    Age INT,
    AttachFolder NVARCHAR(200),
    DBName NVARCHAR(200),
    SyncTime DATETIME,
    UNID NVARCHAR(36) UNIQUE,
    IsDeleted BIT DEFAULT 0,
    ConflictUNID NVARCHAR(36),
    LastUpdatedDate DATETIME DEFAULT GETDATE(),
    Revisions NVARCHAR(MAX),
    AuthorID NVARCHAR(100),
    AuthorName NVARCHAR(200),
    LastUpdatedByID NVARCHAR(100),
    LastUpdatedBy NVARCHAR(200)
);

CREATE INDEX IX_CALPARAM_UNID ON dbo.CALPARAM(UNID);
CREATE INDEX IX_CALPARAM_UPDATED ON dbo.CALPARAM(LastUpdatedDate);
```

## 4. JSON-схема (запись CALPARAM)

```json
{
  "id": 1,
  "Address": "https://server.example.com",
  "Name": "appuser",
  "Password": "****",
  "Vedushii": "Ведущий",
  "VedushiiID": "ved-001",
  "StartPage": "Calendar",
  "Button1Id": "1",
  "Button2Id": "2",
  "Button3Id": "3",
  "Button4Id": "4",
  "Button5Id": "5",
  "Height": 190,
  "Weight": 110,
  "Age": 50,
  "AttachFolder": "Attachments",
  "DBName": "WGPlanDatabase.db",
  "SyncTime": "2026-11-24 09:00:00",
  "UNID": "calparam-uuid",
  "IsDeleted": 0,
  "LastUpdatedDate": "2026-11-24 10:30:00",
  "Revisions": "2026-11-24 10:30:00",
  "AuthorID": "ved-001",
  "AuthorName": "Ведущий",
  "LastUpdatedByID": "ved-001",
  "LastUpdatedBy": "Ведущий"
}
```

## 5. Ключ сущности и поле конфликта

- **Ключ:** `id` (числовой id записи CALPARAM)
- **Поле конфликта:** `SyncTime` (последняя успешная синхронизация). Обе записи сохраняются: у проигрывшей `ConflictUNID = <UNID выигравшей>`, `IsDeleted = 2`.
- **Привязка:** к Ведущему (`Vedushii`/`VedushiiID`)