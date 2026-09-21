# Архитектура Синхронизации с Сервером (WG-Portal-Android-Java)

> Назначение файла: описание текущего состояния и целевой схемы обмена данными с сервером.
> Файл заполняется по мере реализации. Обновлять после КАЖДОЙ задачи, затронувшей синхронизацию/сервер (чек-лист в конце).
> Схема UI — в `ArchitectureUI.md`, локальный BackEnd — в `ArchitectureLocalBackEnd.md`.

## 0. Текущее состояние (кратко для ИИ)

- Task 143: первая фаза серверной синхронизации реализована — класс `SynchroCal` (POST JSON `CalParamRecord` на сервер, Basic-авторизация). Классов SyncAdapter/WorkManager/OkHttp/Retrofit/Volley в проекте НЕТ; транспорт — `HttpURLConnection`.
- Приложение работает автономно на локальной SQLite `WGPlanDatabase.db`.
- Единственная сетевая функция: `RussianHolidaysFetcher` — загрузка праздников (HTTP GET через `HttpURLConnection`, API holidays: год/CountryCode=RU) в таблицу HOLIDAYS. Разрешения: `INTERNET`, `ACCESS_NETWORK_STATE`. Task 144: с 1 сентября текущего года загружается также следующий год (`fetchHolidaysForYears` — объединённый результат; список лет — `yearsToFetch()`).
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
| `CALPARAM` | на сервер (Task 143, `SynchroCal`); обратная корректировка локально — только поле `SyncTime` | `id` | `SyncTime` |
| `HOLIDAYS` | на клиент (внешний API, уже реализовано) | (CountryCode, HolidayDate) | — |

## 2. Планируемая целевая схема (заготовка для реализации)

- Транспорт: REST/JSON (библиотека выбирается при реализации; текущий проект — Java + HttpURLConnection).
- Аутентификация: по `CALPARAM.Address / Name / Password` (поля уже есть в CALPARAM).
- Идентификация пользователя: `CALPARAM.Vedushii / VedushiiID` (= `ManageSQLDatabase.AuthorName / AuthorID`).
- Цикл обмена: выгрузка записей с `LastUpdatedDate` > метки последней синхронизации, затем загрузка чужих изменений; конфликты — по более новой `LastUpdatedDate` (last-write-wins), журнал правок — `Revisions`.
- Запись данных всегда через менеджеры из `ArchitectureLocalBackEnd.md`, чтобы аудит-поля заполнялись единообразно.
- Место для деталей (заполняется по мере реализации):

### 2.1. Реализовано (Task 143, SynchroCal)
- Класс: `SynchroCal` (`com.example.calendar4`); инициализируется из `ParamsActivity` значениями `editTextAddress / editTextName / editTextPassword`; запуск — кнопка `btnSync` «Синхронизовать» (`ParamsActivity.startSync`).
- Endpoint: адрес из `CALPARAM.Address` (без схемы добавляется `http://`); запрос POST, тело JSON; таймауты 10 с connect / 15 с read; сеть — фоновый поток `ExecutorService`, callback — Handler main looper.
- Аутентификация: заголовок `Authorization: Basic base64(Name:Password)` (`android.util.Base64`, NO_WRAP).
- Формат запроса: `{"ContentType":"CalParamRecord","LastSyncDate":<CALPARAM.SyncTime|null>,"Updated":<текущее время "yyyy-MM-dd HH:mm:ss">,"Record":{id, Address, Name, Password, Vedushii, VedushiiID, StartPage, Button1Id..Button5Id, Height, Weight, Age, AttachFolder, DBName}}` (null передаётся как JSON null).
- Формат ответа (ожидается): JSON `{"Status":"OK","Session":"<номер сессии>"}`; ключи статуса разыскиваются как `Status/Result/Confirmation`, сессии — `Session/SessionId/SessionID/session`; статус (если задан) должен быть OK/success/true/2xx; без номера сессии обмен считается неуспешным.
- Номер сессии: `SynchroCal.getSession()` (static, в памяти процесса; НЕ реализовано: персистентное хранение).
- Метка последней синхронизации: колонка `CALPARAM.SyncTime` (миграция DATABASE_VERSION 10→11, `upgradeToV11` + safety-net); после успешного обмена обновляется ТОЛЬКО она: `ManageSQLDatabase.updateCalParamSyncTime(id, время)` → `CalParamSQLManage.updateSyncTime`.
- Обработка ошибок: HTTP 401 — «Сервер отклонил имя/пароль»; прочие HTTP-коды и не-JSON ответ — ошибка; результат (успех/ошибка, сессия) через `SynchroCal.SynchroListener` → Toast в `ParamsActivity`.
- Прочие таблицы (CALPLAN, HISTORY, NOTEPLAN, HEALTHPLAN, CONTACTS, SMSCALPLAN, LIVETYPE) — НЕ реализовано (следующие шаги: дельта-выгрузка по `LastUpdatedDate`, приём чужих изменений, конфликты last-write-wins).

## 3. Правила ведения файла (для ИИ)

1. После каждой задачи, добавившей/изменившей сетевой обмен, протокол, endpoint или правила конфликтов — ОБНОВИТЬ этот файл в том же сеансе.
2. Раздел 2 дополнять фактами (точные имена классов, URL, поля), а не намерениями; нереализованное держать помеченным «НЕ реализовано».
3. При реализации синхронизации таблицы из раздела 1 не переименовывать без обновления `ArchitectureLocalBackEnd.md` и `ArchitectureUI.md`.
