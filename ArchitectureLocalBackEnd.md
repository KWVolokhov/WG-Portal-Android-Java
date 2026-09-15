# Архитектура Локального BackEnd (WG-Portal-Android-Java)

> Назначение файла: машиночитаемый реестр локальной схемы данных SQLite, менеджеров БД, record-классов и сервисов.
> Используется архитектором и ИИ-агентом. Обновлять после КАЖДОЙ задачи, затронувшей данные/БД (чек-лист в конце).
> Схема UI — в `ArchitectureUI.md`, синхронизация с сервером — в `ArchitectureServerSynkhro.md`.

## 0. Сводные факты (коротко для ИИ)

- Единственная локальная БД: SQLite, файл `WGPlanDatabase.db`.
- Класс доступа: `ManageSQLDatabase extends SQLiteOpenHelper`, singleton (`getInstance(Context)`), `DATABASE_VERSION = 10`.
- Все DDL-строки собраны в `ConstantsSQLDb` (`CREATE_TABLE_*`, `INSERT_*`, `UPDATE_*`).
- Паттерн доступа: на таблицу — свой менеджер (`*SQLManage`) и свой record-класс (POJO, Serializable).
- Автор записи: статические `ManageSQLDatabase.AuthorName / AuthorID` (из CALPARAM: Vedushii/VedushiiID).
- Каждая бизнес-запись несёт: `UNID TEXT UNIQUE` (ключ будущей синхронизации) + аудит-поля `AuthorID/AuthorName`, `LastUpdatedByID/LastUpdatedBy/LastUpdatedDate`, `Revisions` (даты изменения подряд).
- Код совместим с Android 8.

## 1. Реестр таблиц

| Таблица | Назначение | Записей | Менеджер (java) | Record-класс | Основные экраны |
|---|---|---|---|---|---|
| `CALPLAN` | Проекты (Form=Project), Задачи (Task), Заявки (Request) | много | `ManageSQLDatabase` | `calPlanRecord` | MainActivity, ProjectsActivity, InputCalPlanActivity, TaskActivity |
| `HISTORY` | История (выделена из CALPLAN, Form=History) | много | `HistorySQLManage` | `calPlanRecord` | HistoryActivity, HistoryEditActivity |
| `NOTEPLAN` | Заметки (Form=Note) и Напоминания (Form=Remember) | много | `NoteRememSQLManage` | `calPlanRecord` | NoteActivity, RememberActivity |
| `HEALTHPLAN` | Здоровье (Form=HealthEat/HealthDrink/HealthSport и др.) | много | `HealthSQLManage` | `healthPlanRecord` | HealthActivity, HealthEditActivity + подклассы |
| `LIVETYPE` | Справочник «Типы жизнедеятельности» (категория, иконка, шагомер) | справочник | `LivetypeSQLManage` | `livetypeRecord` | LivetypeActivity, LivetypeEditActivity, кнопки MainActivity |
| `CLASSIFICATOR` | Справочники (IDS, CATEGORY, SONAME); категории: SYSTEMSPLAN и др. | справочник | `ManageSQLDatabase` | — (строки INSERT_CLASSIFICATOR) | Выпадающие списки карточек |
| `CALPARAM` | Параметры приложения (1 запись) | 1 | `ManageSQLDatabase` (`getCalParam`) | `CalParamRecord` | ParamsActivity |
| `HOLIDAYS` | Праздники (CountryCode, HolidayDate, HolidayName; UNIQUE(CountryCode,HolidayDate)) | справочник | `ManageSQLDatabase` | `holidayRecord` | RussianCalendarView |
| `CONTACTS` | Контакты | много | `ManageSQLDatabase` | `ContactRecord` | ContactsActivity, EditContactActivity |
| `SMSCALPLAN` | СМС (Type=Incoming/Outgoing/Draft) | много | `SmsSQLManage` | `smsRecord` | SmsActivity, SmsChatActivity, SmsViewActivity |

## 2. Ключевые поля CALPARAM (1 запись)

`Address, Name, Password, Vedushii, VedushiiID, StartPage` (стартовая страница),
`Button1Id..Button5Id` (id записей LIVETYPE для быстрых кнопок MainActivity: по умолчанию Шагомер/Бургер/Кофе/Авария/Гулянка),
`Height, Weight, Age` (рост/вес/возраст), `AttachFolder` (папка вложений, умолч. `Attachments`), `DBName`.

## 3. Менеджеры БД (java)

| Класс | Таблица | Основные методы |
|---|---|---|
| `ManageSQLDatabase` | CALPLAN, CALPARAM, CONTACTS, CLASSIFICATOR, HOLIDAYS, LIVETYPE (создание/миграции) | `upsertCalPlan`, выборки по дате, `getTasksByProject`, `getProjectsByRequestName`, `getCalParam`, singleton `getInstance` |
| `HistorySQLManage` | HISTORY | upsert / delete / getById / выборка по дате (record = calPlanRecord, сортировка `Okdate DESC, id DESC` — сверху самая поздняя) |
| `NoteRememSQLManage` | NOTEPLAN | `upsertNote`, `deleteNote`, `getNoteById`, `getNotesByDate` |
| `HealthSQLManage` | HEALTHPLAN | upsert / delete / getById / выборка по дате (record = healthPlanRecord) |
| `LivetypeSQLManage` | LIVETYPE | upsert / delete / getById / getByUNID / выборка `Name COLLATE NOCASE` |
| `SmsSQLManage` | SMSCALPLAN | папки СМС: Все/Входящие (Incoming)/Исходящие (Outgoing)/Корзина (без FromID и ToID) |

`ManageSQLDatabase.onCreate`: создаёт все таблицы (`ConstantsSQLDb.CREATE_TABLE_*`), заполняет `INSERT_CLASSIFICATOR`, предустановки LIVETYPE (id 1..5, UNID = Form из HEALTHPLAN, иконки ic_pedometer/ic_burger/ic_coffee/ic_stress/ic_joy), CALPARAM.
`onUpgrade`: дозаполнение через `UPDATE_LIVETYPE_ICONS`, `UPDATE_LIVETYPE_DEFAULTS` и др. (без DROP — данные сохраняются).

## 4. Record-классы

| Класс | Таблица | Примечание |
|---|---|---|
| `calPlanRecord` | CALPLAN / HISTORY / NOTEPLAN | общий record; поле `Form` различает Project/Task/Request/History/Note/Remember |
| `healthPlanRecord` | HEALTHPLAN | поля органов/состояний (Head, Eyes, ..., Skin), Steps, FoodWeight, DrinkValue, Kallory |
| `livetypeRecord` | LIVETYPE | Name, Category, Icon, Form, StepCounter |
| `CalParamRecord` | CALPARAM | параметры приложения |
| `ContactRecord` | CONTACTS | Surname, FirstName, Patronymic, Phone, Phone2, Email, BirthDate, HomeAddress... |
| `holidayRecord` | HOLIDAYS | CountryCode, HolidayDate, HolidayName |
| `smsRecord` | SMSCALPLAN | Type, FromID/FromName, ToID/ToName, Subject, Body, Status, DateReceived; `DisplayType` — вычисляемое дисплейное поле типа (Incoming/Outgoing по позиции Ведущего), в SQL не хранится |

## 5. Сервисы и вспомогательные классы

| Класс | Роль |
|---|---|
| `Pedometer` | Реальный шагомер, разрешение ACTIVITY_RECOGNITION (Android 10+); запись Steps в HEALTHPLAN |
| `RussianHolidaysFetcher` | Загрузка праздников года по HTTP (`HttpURLConnection`) в таблицу HOLIDAYS; единственный сетевой код проекта |
| `WGPortalApp` | Application (`android:name` в манифесте); ставит `HardcoreCrashHandler` в `onCreate` до любых экранов (Task 134) |
| `AttachmentStore` | Хранение файлов вложений `InfoFieldView` (папка CALPARAM.AttachFolder) |
| `HardcoreCrashHandler` | Перехват UncaughtException всех потоков (ставится в `WGPortalApp.onCreate`, Task 134); сохранение стека и запуск `CrashActivity` при ошибке на любом Activity |
| `ConstantsSQLDb` | Все DDL/INSERT/UPDATE-строки схемы (единственное место изменения схемы) |

## 6. Правила ведения файла (для ИИ)

1. После каждой задачи, изменившей схему (новая таблица/поле/миграция), менеджер или record — ОБНОВИТЬ этот файл в том же сеансе.
2. Новую таблицу добавлять одновременно: DDL в `ConstantsSQLDb` + создание в `ManageSQLDatabase.onCreate` (+ onUpgrade) + строка в реестре раздела 1.
3. Изменение `DATABASE_VERSION` обязательно сопровождать миграцией в `onUpgrade` без потери данных.
4. При изменении таблицы указывать, какие экраны (UI) её используют — синхронно править `ArchitectureUI.md`.
5. Всё, что касается обмена с сервером, — в `ArchitectureServerSynkhro.md`.
