# Протокол синхронизации: Проекты/Заявки/Задачи (CALPLAN) и Ежедневные активности (HEALTHPLAN)

## 1. Направление

**Обе стороны** — клиент выгружает на сервер, сервер отдаёт клиенту изменения.

## 2. Связь между сущностями

Проекты (Form=Project), Заявки (Form=Request), Задачи (Form=Task) и Ежедневные активности (HEALTHPLAN: Form=HealthEat/HealthDrink/HealthSport/HealthStress/HealthJoy) жестко связаны business-логикой, поэтому синхронизируются в рамках одного протокола.

## 3. Фильтр "Etot phone"

- CALPLAN: не применяется (проекты не привязаны к Ведущему, Ведущий там просто указан как поле AuthorID).
- HEALTHPLAN: привязан к Ведущему — если Ведущий = "Etot phone", активности не синхронизируются.

## 4. SQL DDL — Сервер (MSSQL)

### 4.1. CALPLAN

```sql
CREATE TABLE dbo.CALPLAN (
    id INT IDENTITY(1,1) PRIMARY KEY,
    UNID NVARCHAR(36) UNIQUE,
    Form NVARCHAR(50) DEFAULT 'Project',
    Priority INT,
    Okdate DATETIME,
    AuthorID NVARCHAR(100),
    AuthorName NVARCHAR(200),
    Name NVARCHAR(400) NOT NULL,
    RequestName NVARCHAR(200),
    RequestUNID NVARCHAR(36),
    Status NVARCHAR(100),
    StatusID INT,
    MainSystem NVARCHAR(100),
    AnalitikID NVARCHAR(100),
    AnalitikName NVARCHAR(200),
    ExectorID NVARCHAR(100),
    ExectorName NVARCHAR(200),
    LastUpdatedByID NVARCHAR(100),
    LastUpdatedBy NVARCHAR(200),
    LastUpdatedDate DATETIME,
    BodyText NVARCHAR(MAX),
    Comment NVARCHAR(MAX),
    StartDate DATETIME,
    EndDate DATETIME,
    HoldDate DATETIME,
    InstallOrder NVARCHAR(MAX),
    KeyWords NVARCHAR(MAX),
    Revisions NVARCHAR(MAX),
    IsDeleted BIT DEFAULT 0,
    ConflictUNID NVARCHAR(36)
);

CREATE INDEX IX_CALPLAN_UNID ON dbo.CALPLAN(UNID);
CREATE INDEX IX_CALPLAN_UPDATED ON dbo.CALPLAN(LastUpdatedDate);
CREATE INDEX IX_CALPLAN_DELETED ON dbo.CALPLAN(IsDeleted);
CREATE INDEX IX_CALPLAN_FORM ON dbo.CALPLAN(Form);
```

### 4.2. HEALTHPLAN

```sql
CREATE TABLE dbo.HEALTHPLAN (
    id INT IDENTITY(1,1) PRIMARY KEY,
    UNID NVARCHAR(36) UNIQUE,
    Form NVARCHAR(50) DEFAULT 'HealthEat',
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
    EndDate DATETIME,
    Revisions NVARCHAR(MAX),
    Head INT, Eyes INT, Ears INT, Nose INT, Throat INT, Teeth INT,
    Stomach INT, Intestines INT, Liver INT, Kidneys INT, Heart INT,
    Lungs INT, Pressure INT, Sleep INT, Weight INT, Nervous INT,
    Morality INT, Skin INT,
    Steps INT,
    FoodWeight DECIMAL(10,2),
    DrinkValue DECIMAL(10,2),
    Kallory INT,
    Icon NVARCHAR(100),
    IsDeleted BIT DEFAULT 0,
    ConflictUNID NVARCHAR(36)
);

CREATE INDEX IX_HEALTHPLAN_UNID ON dbo.HEALTHPLAN(UNID);
CREATE INDEX IX_HEALTHPLAN_UPDATED ON dbo.HEALTHPLAN(LastUpdatedDate);
CREATE INDEX IX_HEALTHPLAN_DELETED ON dbo.HEALTHPLAN(IsDeleted);
CREATE INDEX IX_HEALTHPLAN_FORM ON dbo.HEALTHPLAN(Form);
```

## 5. JSON-схемы

### 5.1. CALPLAN (Project/Request/Task)

```json
{
  "id": 456,
  "UNID": "A1B2C3D4E5F67890",
  "Form": "Project",
  "Priority": 1,
  "Okdate": "2026-11-24",
  "AuthorID": "ved-001",
  "AuthorName": "Ведущий",
  "Name": "Новый проект",
  "RequestName": null,
  "RequestUNID": null,
  "Status": "Черновик",
  "StatusID": 1,
  "MainSystem": "Java",
  "AnalitikID": null,
  "AnalitikName": null,
  "ExectorID": null,
  "ExectorName": null,
  "LastUpdatedByID": "ved-001",
  "LastUpdatedBy": "Ведущий",
  "LastUpdatedDate": "2026-11-24 10:30:00",
  "BodyText": "Описание проекта",
  "Comment": null,
  "StartDate": "2026-11-24",
  "EndDate": null,
  "HoldDate": null,
  "InstallOrder": null,
  "KeyWords": null,
  "Revisions": "2026-11-24 10:30:00",
  "IsDeleted": 0
}
```

### 5.2. HEALTHPLAN (Ежедневные активности)

```json
{
  "id": 202,
  "UNID": "health-uuid-789",
  "Form": "HealthEat",
  "Okdate": "2026-11-24",
  "AuthorID": "ved-001",
  "AuthorName": "Ведущий",
  "LastUpdatedByID": "ved-001",
  "LastUpdatedBy": "Ведущий",
  "LastUpdatedDate": "2026-11-24 10:30:00",
  "Name": "Бургер",
  "BodyText": null,
  "Comment": null,
  "StartDate": "2026-11-24",
  "EndDate": null,
  "Revisions": "2026-11-24 10:30:00",
  "Head": 0, "Eyes": 0, "Ears": 0, "Nose": 0, "Throat": 0, "Teeth": 0,
  "Stomach": 0, "Intestines": 0, "Liver": 0, "Kidneys": 0, "Heart": 0,
  "Lungs": 0, "Pressure": 0, "Sleep": 0, "Weight": 0, "Nervous": 0,
  "Morality": 0, "Skin": 0,
  "Steps": 0,
  "FoodWeight": 0.16,
  "DrinkValue": null,
  "Kallory": 420,
  "Icon": "",
  "IsDeleted": 0
}
```

## 6. Ключи сущностей и поля конфликтов

| Сущность | Ключ | Поле конфликта | Привязка |
|---|---|---|---|
| CALPLAN | `UNID` | `LastUpdatedDate` (last-write-wins). Обе записи сохраняются: у проигрывшей `ConflictUNID = <UNID выигравшей>`, `IsDeleted = 2`. | к UNID (генерируется на клиенте), Ведущий = поле AuthorID |
| HEALTHPLAN | `UNID` | `LastUpdatedDate` (last-write-wins). Обе записи сохраняются: у проигрывшей `ConflictUNID = <UNID выигравшей>`, `IsDeleted = 2`. | к Ведущему (AuthorID/VedushiiID) |