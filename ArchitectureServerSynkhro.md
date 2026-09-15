# Архитектура Синхронизации с Сервером (WG-Portal-Android-Java)

> Назначение файла: описание текущего состояния и целевой схемы обмена данными с сервером.
> Файл заполняется по мере реализации. Обновлять после КАЖДОЙ задачи, затронувшей синхронизацию/сервер (чек-лист в конце).
> Схема UI — в `ArchitectureUI.md`, локальный BackEnd — в `ArchitectureLocalBackEnd.md`.

## 0. Текущее состояние (кратко для ИИ)

- Серверная синхронизация данных НЕ реализована. Классов SyncAdapter/WorkManager/OkHttp/Retrofit/Volley в проекте НЕТ.
- Приложение работает автономно на локальной SQLite `WGPlanDatabase.db`.
- Единственная сетевая функция: `RussianHolidaysFetcher` — загрузка праздников (HTTP GET через `HttpURLConnection`, API holidays: год/CountryCode=RU) в таблицу HOLIDAYS. Разрешения: `INTERNET`, `ACCESS_NETWORK_STATE`.
- Все бизнес-записи уже подготовлены к будущей синхронизации: имеют `UNID TEXT UNIQUE` и аудит-поля (`AuthorID/AuthorName`, `LastUpdatedByID/LastUpdatedBy`, `LastUpdatedDate`, `Revisions`).

## 1. Таблицы, подлежащие синхронизации (целевая схема)

| Таблица | Направление | Ключ сущности | Конфликт-поле |
|---|---|---|---|
| `CALPLAN` | обе стороны | `UNID` | `LastUpdatedDate` |
| `HISTORY` | обе стороны | `UNID` | `LastUpdatedDate` |
| `NOTEPLAN` | обе стороны | `UNID` | `LastUpdatedDate` |
| `HEALTHPLAN` | обе стороны | `UNID` | `LastUpdatedDate` |
| `CONTACTS` | обе стороны | `id`/`EntryID` | `DateModified` |
| `SMSCALPLAN` | обе стороны | `UNID` | `DateReceived` |
| `LIVETYPE` | на клиент (справочник) | `UNID` (у предустановок = Form из HEALTHPLAN) | — |
| `CALPARAM` | не синхронизируется (локальные настройки устройства) | — | — |
| `HOLIDAYS` | на клиент (внешний API, уже реализовано) | (CountryCode, HolidayDate) | — |

## 2. Планируемая целевая схема (заготовка для реализации)

- Транспорт: REST/JSON (библиотека выбирается при реализации; текущий проект — Java + HttpURLConnection).
- Аутентификация: по `CALPARAM.Address / Name / Password` (поля уже есть в CALPARAM).
- Идентификация пользователя: `CALPARAM.Vedushii / VedushiiID` (= `ManageSQLDatabase.AuthorName / AuthorID`).
- Цикл обмена: выгрузка записей с `LastUpdatedDate` > метки последней синхронизации, затем загрузка чужих изменений; конфликты — по более новой `LastUpdatedDate` (last-write-wins), журнал правок — `Revisions`.
- Запись данных всегда через менеджеры из `ArchitectureLocalBackEnd.md`, чтобы аудит-поля заполнялись единообразно.
- Место для деталей (заполняется по мере реализации):
  - Endpoint-ы: —
  - Формат пакетов (batch/дельта): —
  - Хранение метки последней синхронизации: —
  - Обработка ошибок/повторов: —

## 3. Правила ведения файла (для ИИ)

1. После каждой задачи, добавившей/изменившей сетевой обмен, протокол, endpoint или правила конфликтов — ОБНОВИТЬ этот файл в том же сеансе.
2. Раздел 2 дополнять фактами (точные имена классов, URL, поля), а не намерениями; нереализованное держать помеченным «НЕ реализовано».
3. При реализации синхронизации таблицы из раздела 1 не переименовывать без обновления `ArchitectureLocalBackEnd.md` и `ArchitectureUI.md`.
