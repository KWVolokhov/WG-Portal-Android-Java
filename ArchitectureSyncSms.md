# Протокол синхронизации: SMS (SMSCALPLAN)

## 1. Направление

**Обе стороны** — клиент выгружает на сервер, сервер отдаёт клиенту изменения.

## 2. Фильтр "Etot phone"

SMS выгружаются только если `FromID` или `ToID` совпадает с `VedushiiID` (Ведущий хоть как-то участвует в переписке). SMS, где Ведущий не указан — не выгружаются.

## 3. SQL DDL — Сервер (MSSQL)

```sql
CREATE TABLE dbo.SMSCALPLAN (
    id INT IDENTITY(1,1) PRIMARY KEY,
    UNID NVARCHAR(36) UNIQUE,
    Type NVARCHAR(50) DEFAULT 'Incoming',
    FromID NVARCHAR(100),
    FromName NVARCHAR(200),
    ToID NVARCHAR(100),
    ToName NVARCHAR(200),
    Subject NVARCHAR(200),
    Body NVARCHAR(MAX),
    Status NVARCHAR(50),
    DateReceived DATETIME,
    LastUpdatedDate DATETIME DEFAULT GETDATE(),
    Revisions NVARCHAR(MAX),
    IsDeleted BIT DEFAULT 0,
    ConflictUNID NVARCHAR(36)
);

CREATE INDEX IX_SMSCALPLAN_UNID ON dbo.SMSCALPLAN(UNID);
CREATE INDEX IX_SMSCALPLAN_FROMTO ON dbo.SMSCALPLAN(FromID, ToID);
CREATE INDEX IX_SMSCALPLAN_UPDATED ON dbo.SMSCALPLAN(LastUpdatedDate);
CREATE INDEX IX_SMSCALPLAN_DELETED ON dbo.SMSCALPLAN(IsDeleted);
```

## 4. JSON-схема (запись SMSCALPLAN)

```json
{
  "id": 303,
  "UNID": "sms-uuid-abc",
  "Type": "Incoming",
  "FromID": "ved-001",
  "FromName": "Ведущий",
  "ToID": "contact-001",
  "ToName": "Контакт",
  "Subject": "Тестовая запись",
  "Body": "Hello world!",
  "Status": "New",
  "DateReceived": "2026-11-24 10:30:00",
  "LastUpdatedDate": "2026-11-24 10:30:00",
  "Revisions": "2026-11-24 10:30:00",
  "IsDeleted": 0
}
```

## 5. Ключ сущности и поле конфликта

- **Ключ:** `UNID`
- **Поле конфликта:** `LastUpdatedDate` (last-write-wins). Обе записи сохраняются: у проигрывшей `ConflictUNID = <UNID выигравшей>`, `IsDeleted = 2`.
- **Привязка:** к Ведущему через FromID/ToID