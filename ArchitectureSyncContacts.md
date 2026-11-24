# Протокол синхронизации: Контакты (CONTACTS)

## 1. Направление

**Обе стороны** — клиент выгружает на сервер, сервер отдаёт клиенту изменения.

## 2. Фильтр "Etot phone"

- Контакт с `AuthorID`/`VedushiiID` = `"Etot phone"` не выгружается на сервер.
- Контакт с `UNID` начинающимся на `"Etot phone_"` не выгружается на сервер.
- Сама запись с `UNID = "Etot phone"` не синхронизируется.

## 3. SQL DDL — Сервер (MSSQL)

```sql
CREATE TABLE dbo.CONTACTS (
    id INT IDENTITY(1,1) PRIMARY KEY,
    Surname NVARCHAR(200),
    FirstName NVARCHAR(200),
    Patronymic NVARCHAR(200),
    Phone NVARCHAR(100),
    Info NVARCHAR(MAX),
    Phone2 NVARCHAR(100),
    Email NVARCHAR(200),
    BirthDate DATE,
    HomeAddress NVARCHAR(500),
    DateReceived DATE,
    DateCreated DATETIME,
    DateModified DATETIME,
    EntryID NVARCHAR(100),
    UNID NVARCHAR(36) UNIQUE,
    AuthorID NVARCHAR(100),
    AuthorName NVARCHAR(200),
    LastUpdatedByID NVARCHAR(100),
    LastUpdatedBy NVARCHAR(200),
    LastUpdatedDate DATETIME DEFAULT GETDATE(),
    Revisions NVARCHAR(MAX),
    IsDeleted BIT DEFAULT 0,
    ConflictUNID NVARCHAR(36)
);

CREATE INDEX IX_CONTACTS_UNID ON dbo.CONTACTS(UNID);
CREATE INDEX IX_CONTACTS_AUTHOR ON dbo.CONTACTS(AuthorID);
CREATE INDEX IX_CONTACTS_DELETED ON dbo.CONTACTS(IsDeleted);
CREATE INDEX IX_CONTACTS_UPDATED ON dbo.CONTACTS(LastUpdatedDate);
```

## 4. JSON-схема (запись CONTACTS)

```json
{
  "id": 123,
  "Surname": "Иванов",
  "FirstName": "Иван",
  "Patronymic": "Иванович",
  "Phone": "+79001234567",
  "Info": "партнёр",
  "Phone2": "+79009876543",
  "Email": "ivan@example.com",
  "BirthDate": "1980-01-15",
  "HomeAddress": "г. Москва, ул. Ленина, 1",
  "DateReceived": "2026-01-10",
  "DateCreated": "2026-01-10 09:00:00",
  "DateModified": "2026-11-24 10:30:00",
  "EntryID": "ENT-001",
  "UNID": "Etot phone_A1B2C3D4E5F6",
  "AuthorID": "ved-001",
  "AuthorName": "Ведущий",
  "LastUpdatedByID": "ved-001",
  "LastUpdatedBy": "Ведущий",
  "LastUpdatedDate": "2026-11-24 10:30:00",
  "Revisions": "2026-01-10 09:00:00;2026-11-24 10:30:00",
  "IsDeleted": 0
}
```

## 5. Ключ сущности и поле конфликта

- **Ключ:** `UNID`
- **Поле конфликта:** `LastUpdatedDate` (last-write-wins). Обе записи сохраняются: у проигрывшей `ConflictUNID = <UNID выигравшей>`, `IsDeleted = 2`.
- **Привязка:** к Ведущему (`AuthorID`/`VedushiiID`)