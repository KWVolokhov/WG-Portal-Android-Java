# Архитектура UI (WG-Portal-Android-Java)

> Назначение файла: единый машиночитаемый реестр экранов, контролов и правил UI проекта.
> Файл используется и человеком (архитектором), и ИИ-агентом. Правила работы с файлом — в конце.
> Обновлять после КАЖДОЙ выполненной задачи, затронувшей UI (чек-лист в конце).

## 0. Сводные факты (коротко для ИИ)

- Пакет: `com.example.calendar4` (код в `app/src/main/java/com/example/calendar4/`).
- Тема приложения: `@style/Theme.AppCompat` (см. `app/src/main/AndroidManifest.xml`).
- Все экраны — `Activity` (Fragment в проекте НЕ используются).
- Экраны наследуются от `BaseScreenActivity` (кроме `MainActivity` и `CrashActivity`).
- Единое меню на всех экранах: `BaseScreenActivity` надувает `res/menu/main_menu.xml`.
- Layout-ы экранов: `app/src/main/res/layout/activity_*.xml`.
- Стиль заголовка: сверху заголовок, центрован влево; в той же строке ImageButton-кнопки, центрованы вправо.
- Минимальная совместимость: Android 8 (API 26).

## 1. Базовые классы экранов

| Класс | Наследование | Роль |
|---|---|---|
| `BaseScreenActivity` | `AppCompatActivity` | Единый ActionBar с титлом и меню `main_menu.xml`; обработка пунктов меню (`onOptionsItemSelected`). Все обычные экраны наследуются отсюда. |
| `BaseCalPlanEditActivity` | `BaseScreenActivity` | Абстрактная карточка редактирования записей CALPLAN/HISTORY/NOTEPLAN/HEALTHPLAN. Подклассы переопределяют `getFormType()` и набор показываемых полей (`showStatus()`, `showMainSystem()`, `showPriority()`, `showStartDate()`, `getStartDateLabel()` и т.п.). |
| `MainActivity` | `AppCompatActivity` | Главный экран (LAUNCHER), свой обработчик меню (не наследует BaseScreenActivity). |
| `CrashActivity` | `android.app.Activity` | Экран отображения перехваченного краша (запускается `HardcoreCrashHandler`). |

### Константы `BaseCalPlanEditActivity`
- `FORM_VALUES = {Project, Note, Remember, Task, History, HealthEat, HealthDrink, HealthSport, HealthStress, HealthJoy}`.
- `PROJECT_FORM_VALUES = {Project, Task, Request}` (+ extra `EXTRA_PROJECTS_FORM_MODE`, `EXTRA_PRESELECT_FORM`).
- `STATUS_LABELS/IDS`: Черновик/Draft, В работе/Inwork, Тестирование/Intest, Выполнено/Done, Отменено/Canceled, Отложено/Hold.
- `MAIN_SYSTEMS`: Lotus(HCL), VBA, Java, JavaScriptServer, JavaScript, SQL, Busines, ArtDesign, Combo.
- Формат дат: `dd.MM.yyyy`, с временем: `dd.MM.yyyy HH:mm:ss`.

## 2. Реестр экранов

| # | Класс | Layout | Название экрана | Тип | Локальный Backend (таблица / менеджер) |
|---|---|---|---|---|---|
| 1 | `MainActivity` | `activity_main.xml` | «Календарный план» | Список + календарь | CALPLAN, HISTORY, NOTEPLAN, HEALTHPLAN / `ManageSQLDatabase` |
| 2 | `ParamsActivity` | `activity_params.xml` | «Параметры» | Настройки, 1 запись | CALPARAM / `ManageSQLDatabase` (`CalParamRecord`) |
| 3 | `ContactsActivity` | `activity_contacts.xml` | «Контакты» | Список + фильтр по набору букв | CONTACTS / `ManageSQLDatabase` |
| 4 | `EditContactActivity` | `activity_editcontact.xml` | Карточка контакта | Редактирование | CONTACTS / `ManageSQLDatabase` |
| 5 | `ProjectsActivity` | `activity_projects.xml` | «Проекты\Все» / «Проекты\Рабочие» (extra `EXTRA_WORK_MODE`) | Список + поиск от 3 символов | CALPLAN / `ManageSQLDatabase` |
| 6 | `InputCalPlanActivity` | `activity_input_cal_plan.xml` | Карточка Проекта/Заявки | Редактирование | CALPLAN / `ManageSQLDatabase` |
| 7 | `TaskActivity` | `activity_input_cal_plan.xml` | Карточка Задачи | Редактирование | CALPLAN / `ManageSQLDatabase` |
| 8 | `NoteActivity` | `activity_input_cal_plan.xml` | Карточка Заметки | Редактирование | NOTEPLAN / `NoteRememSQLManage` |
| 9 | `RememberActivity` | `activity_input_cal_plan.xml` | Карточка Напоминания | Редактирование | NOTEPLAN / `NoteRememSQLManage` |
| 10 | `HistoryActivity` | `activity_history.xml` | «<дата> История» | Список | HISTORY / `HistorySQLManage` |
| 11 | `HistoryEditActivity` | `activity_input_cal_plan.xml` | Карточка Истории | Редактирование | HISTORY / `HistorySQLManage` |
| 12 | `HealthActivity` | `activity_health.xml` | «<дата> Health» + кнопка Add | Список | HEALTHPLAN / `HealthSQLManage` |
| 13 | `HealthEditActivity` | `activity_input_cal_plan.xml` | Карточка Health | Редактирование | HEALTHPLAN / `HealthSQLManage` |
| 14 | `HealthEatActivity` | `activity_input_cal_plan.xml` | Карточка Еды | Редактирование | HEALTHPLAN / `HealthSQLManage` |
| 15 | `HealthDrinkActivity` | `activity_input_cal_plan.xml` | Карточка Гидратации | Редактирование | HEALTHPLAN / `HealthSQLManage` |
| 16 | `HealthSportActivity` | `activity_input_cal_plan.xml` | Карточка Физ. активности (шагомер) | Редактирование | HEALTHPLAN / `HealthSQLManage` |
| 17 | `LivetypeActivity` | `activity_livetype.xml` | «Типы жизнедеятельности» | Список | LIVETYPE / `LivetypeSQLManage` |
| 18 | `LivetypeEditActivity` | `activity_livetype_edit.xml` | Карточка Типа жизнедеятельности | Редактирование | LIVETYPE / `LivetypeSQLManage` |
| 19 | `SmsActivity` | `activity_sms.xml` | «СМС\Все / Входящие / Исходящие / Корзина» (extra `EXTRA_SMS_FOLDER`) | Список сообщений | SMSCALPLAN / `SmsSQLManage` |
| 20 | `SmsChatActivity` | `activity_smschat.xml` | «СМС-чат Ведущий<->Контакт» (extra `contactId`) | Чат | SMSCALPLAN / `SmsSQLManage` |
| 21 | `SmsViewActivity` | `activity_smsview.xml` | «СМС от/для <Контакт>» | Просмотр (только чтение) | SMSCALPLAN / `SmsSQLManage` |
| 22 | `CrashActivity` | `activity_crash.xml` | Отчёт о сбое | Диагностика | — |

Все экраны зарегистрированы в `app/src/main/AndroidManifest.xml` (LAUNCHER — только `MainActivity`).

### Экран `MainActivity` (зоны)
1. Календарь: нестандартный `RussianCalendarView` (id `calendarView1`), выбор даты обновляет список.
2. Кнопки управления (в т.ч. быстрые кнопки LIVETYPE Button1..Button5 из CALPARAM: Шагомер/Бургер/Кофе и т.д.).
3. Список (id `listView1`) записей за выбранную дату: Заявки на проекты, Проекты, Задачи, Уведомления, Заметки.

### Шаблон заголовка экрана (единый стиль)
- Сверху заголовок, центрован влево, в той же строке ImageButton-кнопки (Add, Back и др.), центрованы вправо.
- Под заголовком (для списков): строка фильтра/поиска, затем список.

## 3. Иерархия классов Activity

```
AppCompatActivity
+-- BaseScreenActivity                      (меню main_menu.xml для всех наследников)
|   +-- BaseCalPlanEditActivity             (карточка записи, layout activity_input_cal_plan.xml)
|   |   +-- InputCalPlanActivity            (Form=Project/Request)
|   |   +-- TaskActivity                    (Form=Task)
|   |   +-- NoteActivity                    (Form=Note, таблица NOTEPLAN)
|   |   +-- RememberActivity                (Form=Remember, таблица NOTEPLAN)
|   |   +-- HistoryEditActivity             (таблица HISTORY)
|   |   +-- HealthEditActivity              (таблица HEALTHPLAN)
|   |       +-- HealthEatActivity           (Form=HealthEat)
|   |       +-- HealthDrinkActivity         (Form=HealthDrink)
|   |       +-- HealthSportActivity         (Form=HealthSport)
|   +-- ContactsActivity
|   +-- EditContactActivity
|   +-- ProjectsActivity
|   +-- HistoryActivity
|   +-- HealthActivity
|   +-- LivetypeActivity
|   +-- LivetypeEditActivity
|   +-- ParamsActivity
|   +-- SmsActivity
|   +-- SmsChatActivity
|   +-- SmsViewActivity
+-- MainActivity                            (LAUNCHER, свой обработчик меню)
CrashActivity (extends android.app.Activity, singleTask)
```

## 4. Меню и карта навигации

Единое меню `res/menu/main_menu.xml` (в каждом экране через `BaseScreenActivity`; обработка в `BaseScreenActivity.onOptionsItemSelected`):

| Пункт меню (id) | Название | Переход / поведение |
|---|---|---|
| `calendar` | Календарь | НЕ реализовано — Toast |
| `contacts` | Контакты | `ContactsActivity` |
| `calculator` | Калькулятор | НЕ реализовано — Toast |
| `livetype` | Типы жизнедеятельности | `LivetypeActivity` |
| `projects_all` | Проекты\Все | `ProjectsActivity` (без extra) |
| `projects_work` | Проекты\Рабочие | `ProjectsActivity` + `EXTRA_WORK_MODE=true` |
| `sms_all` | СМС\Все | `SmsActivity` + `EXTRA_SMS_FOLDER=FOLDER_ALL` |
| `sms_income` | СМС\Входящие | `SmsActivity` + `FOLDER_INCOME` |
| `sms_outcome` | СМС\Исходящие | `SmsActivity` + `FOLDER_OUTCOME` |
| `sms_trash` | СМС\Корзина | `SmsActivity` + `FOLDER_TRASH` |
| `parametrs` | Параметры | `ParamsActivity` |

### Программные переходы (основные)
- `MainActivity` — карточки записей через `ActivityResultLauncher` (современный Activity Result API, старый `startActivityForResult` не используется): результат `calPlanRecord` сохраняется `owerDb.upsertCalPlan(record)`.
- `ContactsActivity` -> `EditContactActivity` (редактирование контакта).
- `ProjectsActivity` — по Form записи: `Task` -> `TaskActivity`, иначе -> `InputCalPlanActivity`.
- `SmsActivity` -> `SmsChatActivity` (extra `contactId`) / `SmsViewActivity` (extra `smsRecord`).
- `CrashActivity` запускается из `HardcoreCrashHandler` (перехват UncaughtException в `MainActivity.onCreate`).
- Вложения `InfoFieldView` открываются через `FileProvider` (authority `${applicationId}.fileprovider`, пути `res/xml/file_paths.xml`).

## 5. Нестандартные контролы (custom views)

| Класс | Наследование | Назначение | Использование |
|---|---|---|---|
| `RussianCalendarView` | `ConstraintLayout` | Нестандартный российский календарь (GridView + `CalendarAdapter`, праздники через `RussianHolidaysFetcher` в таблицу HOLIDAYS) | `MainActivity` (id `calendarView1`), layout `russian_calendar_view.xml`, callback `setOnDateSelectedListener` |
| `DateFieldView` | `LinearLayout` | Поле выбора даты с маской `__.__.____` | Карточки редактирования |
| `PhoneFieldView` | `LinearLayout` | Поле телефона, до 10 цифр, маска-заполнитель `•` | `EditContactActivity` |
| `InfoFieldView` | `LinearLayout` | Блок описания с вложениями (файлы в папке CALPARAM.AttachFolder, раскрытие/сворачивание, RecyclerView блоков) | Карточки редактирования |
| `MessageListItem` | `LinearLayout` | Элемент списка сообщений: автор + дата/время сверху, до 2 строк текста | СМС-экраны |
| `TwoLineListItem` | `LinearLayout` | Двухстрочный элемент списка с разделителем вида `--5--` | Списки |
| `FlyOutContainer` | `LinearLayout` | Выдвижной контейнер (fly-out) | Резерв |
| `StatusIconFactory` | (utility, final) | Иконки статусов записей по цветам состояний | Списки |
| `InfoBlocksAdapter` | `RecyclerView.Adapter` | Сетка вложений (1/4 ширины строки), тап открывает стандартный просмотрщик | `InfoFieldView` |

## 6. Данные, передаваемые между экранами

- Карточка -> вызывающий экран: `Intent` result, extra `"calPlanRecord"` (Serializable) — сохраняет вызывающий экран.
- `ProjectsActivity`: extra `EXTRA_WORK_MODE` (boolean, режим «Рабочие»), `EXTRA_PRESELECT_FORM` (Form новой записи).
- `SmsActivity`: extra `EXTRA_SMS_FOLDER` (FOLDER_ALL/FOLDER_INCOME/FOLDER_OUTCOME/FOLDER_TRASH); Корзина = записи без FromID и ToID.
- `SmsChatActivity`: extra `EXTRA_CONTACT_ID` (`"contactId"`); требует телефон 10 цифр у Ведущего или Контакта, иначе Toast и выход.
- `SmsViewActivity`: extra `EXTRA_SMS_RECORD` (`"smsRecord"`).

## 7. Разрешения, влияющие на UI

- `INTERNET`, `ACCESS_NETWORK_STATE` — загрузка праздников (`RussianHolidaysFetcher`).
- `READ_PHONE_STATE` — телефон/IMEI устройства при старте (`MainActivity`).
- `ACTIVITY_RECOGNITION` — реальный шагомер Android 10+ (`Pedometer`).

## 8. Правила ведения файла (для ИИ)

1. После каждой выполненной задачи, затронувшей UI (новый/изменённый экран, контрол, меню, навигация, layout), ОБНОВИТЬ соответствующие разделы этого файла в том же сеансе работы.
2. Новые экраны добавлять в реестр раздела 2 (и иерархию раздела 3, если меняется наследование) с точными именами классов, layout-ов и таблиц backend.
3. Новые нестандартные контролы — в реестр раздела 5.
4. Новые пункты меню/переходы — в карту навигации раздела 4.
5. Не удалять реализованные пункты; нереализованное помечать «НЕ реализовано».
6. Формат: таблицы с точными именами (класс, файл, id) — это машиночитаемая часть для ИИ; текстовые пояснения — для архитектора.
7. Изменения backend-схемы (таблицы/менеджеры) фиксировать в `ArchitectureLocalBackEnd.md`, синхронизации — в `ArchitectureServerSynkhro.md`.
