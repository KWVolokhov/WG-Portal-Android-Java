# Протокол синхронизации: Типы жизнедеятельности (LIVETYPE)

## 1. Направление

**Только на клиент** (справочник). Сервер отдаёт клиенту изменения, клиент не выгружает на сервер.

## 2. Фильтр "Etot phone"

Не применяется (справочник не привязан к Ведущему).

## 3. SQL DDL — Сервер (MSSQL)

```sql
CREATE TABLE dbo.LIVETYPE (
    id INT IDENTITY(1,1) PRIMARY KEY,
    UNID NVARCHAR(36) UNIQUE,
    Name NVARCHAR(200) NOT NULL,
    Category NVARCHAR(100),
    Icon NVARCHAR(100),
    AuthorID NVARCHAR(100),
    AuthorName NVARCHAR(200),
    DateCreated DATETIME,
    Head INT, Eyes INT, Ears INT, Nose INT, Throat INT, Teeth INT,
    Stomach INT, Intestines INT, Liver INT, Kidneys INT, Heart INT,
    Lungs INT, Pressure INT, Sleep INT, Weight INT, Nervous INT,
    Morality INT, Skin INT,
    Form NVARCHAR(50),
    StepCounter INT DEFAULT 0,
    Steps INT,
    FoodWeight DECIMAL(10,2),
    DrinkValue DECIMAL(10,2),
    Kallory INT,
    LastUpdatedDate DATETIME DEFAULT GETDATE(),
    Revisions NVARCHAR(MAX),
    IsDeleted BIT DEFAULT 0,
    ConflictUNID NVARCHAR(36)
);

CREATE INDEX IX_LIVETYPE_UNID ON dbo.LIVETYPE(UNID);
CREATE INDEX IX_LIVETYPE_FORM ON dbo.LIVETYPE(Form);
CREATE INDEX IX_LIVETYPE_DELETED ON dbo.LIVETYPE(IsDeleted);
CREATE INDEX IX_LIVETYPE_UPDATED ON dbo.LIVETYPE(LastUpdatedDate);
```

## 4. JSON-схема (запись LIVETYPE)

```json
{
  "id": 1,
  "UNID": "HealthSport_00000000000000000001",
  "Name": "Прогулка",
  "Category": "Физ. активность",
  "Icon": "",
  "AuthorID": "ved-001",
  "AuthorName": "Исходная настройка",
  "DateCreated": "2026-01-01 00:00:00",
  "Head": 0, "Eyes": 0, "Ears": 0, "Nose": 0, "Throat": 0, "Teeth": 0,
  "Stomach": 0, "Intestines": 0, "Liver": 0, "Kidneys": 0, "Heart": 0,
  "Lungs": 0, "Pressure": 0, "Sleep": 0, "Weight": 0, "Nervous": 0,
  "Morality": 0, "Skin": 0,
  "Form": "HealthSport",
  "StepCounter": 1,
  "Steps": 0,
  "FoodWeight": null,
  "DrinkValue": null,
  "Kallory": 0,
  "LastUpdatedDate": "2026-01-01 00:00:00",
  "Revisions": "2026-01-01 00:00:00",
  "IsDeleted": 0
}
```

## 5. Ключ сущности и поле конфликта

- **Ключ:** `UNID`
- **Поле конфликта:** не применимо (справочник, только скачивание). При конфликте: у проигрывшей `ConflictUNID = <UNID выигравшей>`, `IsDeleted = 2`.
- **Привязка:** к UNID (средоточник типов жизнедеятельности, на 99% уже присутствует на сервере)