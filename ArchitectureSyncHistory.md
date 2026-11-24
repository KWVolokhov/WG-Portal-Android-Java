# Протокол синхронизации: История (HISTORY)

## 1. Направление

**Обе стороны** — клиент выгружает на сервер, сервер отдаёт клиенту изменения.

## 2. Фильтр "Etot phone"

История привязана к Ведущему — если Ведущий = "Etot phone", записи истории не синхронизируются.

## 3. SQL DDL — Сервер (MSSQL)

```sql
CREATE TABLE dbo.HISTORY (
    id INT IDENTITY(1,1) PRIMARY KEY,
    UNID NVARCHAR(36) UNIQUE,
    Okdate DATETIME,
    AuthorID NVARCHAR(100),
    AuthorName NVARCHAR(200),
    LastUpdatedByID NVARCHAR(100),
    LastUpdatedBy NVARCHAR(200),
    LastUpdatedDate DATETIME,
    Name NVARCHAR(400) NOT NULL,
    BodyText NVARCHAR(MAX),
    Comment NVARCHAR(MAX),
    StartDate DATETIME,
    Revisions NVARCHAR(MAX),
    IsDeleted BIT DEFAULT 0,
    ConflictUNID NVARCHAR(36)
);

CREATE INDEX IX_HISTORY_UNID ON dbo.HISTORY(UNID);
CREATE INDEX IX_HISTORY_UPDATED ON dbo.HISTORY(LastUpdatedDate);
CREATE INDEX IX_HISTORY_DELETED ON dbo.HISTORY(IsDeleted);
CREATE INDEX IX_HISTORY_AUTHOR ON dbo.HISTORY(AuthorID);
```

## 4. JSON-схема (запись HISTORY)

```json
{
  "id": 789,
  "UNID": "hist-uuid-123",
  "Okdate": "2026-11-24",
  "AuthorID": "ved-001",
  "AuthorName": "Ведущий",
  "LastUpdatedByID": "ved-001",
  "LastUpdatedBy": "Ведущий",
  "LastUpdatedDate": "2026-11-24 10:30:00",
  "Name": "Удал. Конт.: Иванов Иван",
  "BodyText": "Фамилия=Иванов, Имя=Иван, ...",
  "Comment": null,
  "StartDate": "2026-11-24",
  "Revisions": "2026-11-24 10:30:00",
  "IsDeleted": 0
}
```

## 5. Ключ сущности и поле конфликта

- **Ключ:** `UNID`
- **Поле конфликта:** `LastUpdatedDate` (last-write-wins). Обе записи сохраняются: у проигрывшей `ConflictUNID = <UNID выигравшей>`, `IsDeleted = 2`.
- **Привязка:** к Ведущему (`AuthorID`)