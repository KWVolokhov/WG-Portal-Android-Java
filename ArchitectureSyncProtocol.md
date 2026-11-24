# Архитектура Протоколов Обмена с Сервером (WG-Portal-Android-Java)

> Назначение файла: описание протоколов синхронизации данных между клиентом (SQLite на Android) и сервером (HCL Domino Java Agent <=> ХРАНИЛИЩЕ типа SQL/MSSQL).
> Файл заполняется по мере реализации. Обновлять после КАЖДОЙ задачи, затронувшей синхронизацию/сервер.
> Схема UI — в `ArchitectureUI.md`, локальный BackEnd — в `ArchitectureLocalBackEnd.md`.

## 0. Общие принципы

- **Транспорт:** HTTPS (POST/GET JSON) через `java.net.HttpURLConnection` (проект не содержит OkHttp/Retrofit/Volley).
- **Аутентификация:** `Authorization: Basic base64(Name:Password)` из `CALPARAM.Address / Name / Password`.
- **Идентификация пользователя:** `CALPARAM.Vedushii / VedushiiID` (= `ManageSQLDatabase.AuthorName / AuthorID`).
- **Единый идентификатор:** у каждой бизнес-записи есть `UNID` (строка до 32 символов, HEX). На клиенте генерируется `ManageSQLDatabase.generateUNID32(String prefix)` (для контактов префикс `"Etot phone_"`, иначе 32 случайных HEX).
- **Удаление на сервере:** только логическое (мягкое) — поле `IsDeleted = 1`, физическое удаление не выполняется.
- **Конфликты:** last-write-wins по полю `LastUpdatedDate` (самая актуальная инфо побеждает).
- **Цикл обмена:** клиент выгружает свои изменения (`LastUpdatedDate` > метки последней синхронизации `CALPARAM.SyncTime`), затем загружает чужие изменения с сервера.
- **Ограничение "Etot phone":** если в качестве Ведущего указан `"Etot phone"`, контакт и сама запись параметров с таким Ведущим НЕ синхронизируются на сервер.

## 1. Транспорт и конечные точки

| Метод | Endpoint | Назначение |
|---|---|---|
| `POST` | `/sync/upload` | Выгрузка локальных изменений с Android на сервер (тело JSON конверта) |
| `GET`  | `/sync/download?entity={entityName}&since={LastSyncDate}` | Загрузка изменений с сервера на Android по сущности и дате |
| `POST` | `/sync/session` | Установка сессии (Basic-авторизация, получение SessionId) |

### 1.1. Конверт (envelope) запроса

```json
{
  "EntityType": "CONTACTS",
  "Action": "INSERT",
  "LastSyncDate": "2026-11-24 10:00:00",
  "Updated": "2026-11-24 10:30:00",
  "Session": "sess-12345",
  "Record": { ...поля сущности... },
  "Records": [ {...} ],
  "UserID": "VedushiiID",
  "UserName": "Vedushii"
}
```

### 1.2. Конверт (envelope) ответа

```json
{
  "Status": "OK",
  "Session": "sess-12345",
  "EntityType": "CONTACTS",
  "Records": [ {...}, {...} ],
  "DeletedUNIDs": ["UNID1", "UNID2"],
  "Errors": [],
  "ServerTime": "2026-11-24 10:35:00"
}
```

Ключи ответа ищутся по mehreren вариантам: `Status/Result/Confirmation`, `Session/SessionId/SessionID/session`.

## 2. Ссылки на протоколы по сущностям

| Сущность | Файл протокола | Направление |
|---|---|---|
| Контакты | `ArchitectureSyncContacts.md` | Обе стороны |
| Типы жизнедеятельности (LIVETYPE) | `ArchitectureSyncLivetype.md` | Только на клиент |
| Параметры (CALPARAM) | `ArchitectureSyncCalparam.md` | На сервер |
| Проекты/Заявки/Задачи + Ежедневные активности (CALPLAN+HEALTHPLAN) | `ArchitectureSyncCalplanHealthplan.md` | Обе стороны |
| SMS (SMSCALPLAN) | `ArchitectureSyncSms.md` | Обе стороны |
| История (HISTORY) | `ArchitectureSyncHistory.md` | Обе стороны |

## 3. Правило "Etot phone" (общее)

- Контакт с `AuthorID`/`VedushiiID` = `"Etot phone"` или с `UNID` начинающимся на `"Etot phone_"` не выгружается на сервер.
- Сама запись `CALPARAM` с `Vedushii = "Etot phone"` не синхронизируется.
- SMS выгружаются только если `FromID` или `ToID` совпадает с `VedushiiID` (Ведущий участвует).

## 4. Мягкое удаление (сервер)

На сервере удаление реализовано как `UPDATE ... SET IsDeleted = 1, LastUpdatedDate = <текущее время> WHERE UNID = ?`. Клиент при получении `IsDeleted = 1` помечает запись локально как удалённую.

## 5. Правило конфликтов

last-write-wins по полю `LastUpdatedDate`. Обе записи сохраняются на сервере:
- запись с более новой `LastUpdatedDate` остаётся активной (`IsDeleted = 0`);
- у проигрывшей записи прописывается `ConflictUNID = <UNID выигравшей записи>` и `IsDeleted = 2` (отмечает проигрыш в конфликте, отличается от обычного удаления `IsDeleted = 1`).

## 6. Правила ведения файла (для ИИ)

1. После каждой задачи, изменившей протокол, endpoint, JSON-схему или правила конфликтов — обновить этот файл в том же сеансе.
2. При добавлении новой сущности — создать отдельный файл `ArchitectureSync{Entity}.md` и добавить ссылку в таблицу раздела 2.
3. Правило "Etot phone" и мягкое удаление не отменять без согласования с архитектором.
4. Изменения локальной схемы (таблицы/поля) синхронно отражать в `ArchitectureLocalBackEnd.md`; изменения UI — в `ArchitectureUI.md`.
5. Формат дат во всех JSON: `yyyy-MM-dd HH:mm:ss` (или `yyyy-MM-dd` для только даты).
6. Все текстовые поля могут быть `null`; числовые — `null` или `0`/`0.0` в зависимости от семантики.