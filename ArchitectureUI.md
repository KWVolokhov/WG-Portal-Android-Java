# Архитектура UI (WG-Portal-Android-Java)

> Назначение файла: единый машиночитаемый реестр экранов, контролов и правил UI проекта.
> Файл используется и человеком (архитектором), и ИИ-агентом. Правила работы с файлом — в конце.
> Обновлять после КАЖДОЙ выполненной задачи, затронувшей UI (чек-лист в конце).

## 0. Сводные факты (коротко для ИИ)

- Пакет: `com.example.calendar4` (код в `app/src/main/java/com/example/calendar4/`).
- Тема приложения: `@style/Theme.AppCompat` (см. `app/src/main/AndroidManifest.xml`).
- Все экраны — `Activity` (Fragment в проекте НЕ используются).
- Экраны наследуются от `BaseScreenActivity` (кроме `CrashActivity`).
- Единое меню на всех экранах: `BaseScreenActivity` надувает `res/menu/main_menu.xml`.
- Перехват падений: `WGPortalApp` (Application, манифест `android:name`) ставит `HardcoreCrashHandler` — `CrashActivity` показывается при любой необработанной ошибке на любом Activity и в любом потоке (Task 134).
- Layout-ы экранов: `app/src/main/res/layout/activity_*.xml`.
- Стиль заголовка: сверху заголовок, центрован влево; в той же строке ImageButton-кнопки, центрованы вправо.
- Минимальная совместимость: Android 8 (API 26).

## 1. Базовые классы экранов

| Класс | Наследование | Роль |
|---|---|---|
| `BaseScreenActivity` | `AppCompatActivity` | Единый ActionBar с титлом и меню `main_menu.xml`; обработка пунктов меню (`onOptionsItemSelected`). Все обычные экраны наследуются отсюда. |
| `BaseCalPlanEditActivity` | `BaseScreenActivity` | Абстрактная карточка редактирования записей CALPLAN/HISTORY/NOTEPLAN/HEALTHPLAN. Подклассы переопределяют `getFormType()` и набор показываемых полей (`showStatus()`, `showMainSystem()`, `showPriority()`, `showStartDate()`, `getStartDateLabel()` и т.п.). |
| `MainActivity` | `BaseScreenActivity` | Главный экран (LAUNCHER); меню `main_menu.xml` наследуется из `BaseScreenActivity` (Task 133). |
| `CrashActivity` | `android.app.Activity` | Экран отображения перехваченного краша (запускается `HardcoreCrashHandler`). |

### Константы `BaseCalPlanEditActivity`
- `FORM_VALUES = {Project, Note, Remember, Task, History, HealthEat, HealthDrink, HealthSport, HealthStress, HealthJoy}`.
- `PROJECT_FORM_VALUES = {Project, Task, Request}` (+ extra `EXTRA_PROJECTS_FORM_MODE`, `EXTRA_PRESELECT_FORM`); подписи `PROJECT_FORM_LABELS = {Проекты, Задачи, Заявку на проект}`.
- `STATUS_LABELS/IDS`: Черновик/Draft, В работе/Inwork, Тестирование/Intest, Выполнено/Done, Отменено/Canceled, Отложено/Hold.
- `MAIN_SYSTEMS`: Lotus(HCL), VBA, Java, JavaScriptServer, JavaScript, SQL, Busines, ArtDesign, Combo.
- Формат дат: `dd.MM.yyyy`, с временем: `dd.MM.yyyy HH:mm:ss`.
- Числовые поля карточек Health (Голова..Каллории) и органов Livetype: ввод целых чисел со знаком (Task 128).

## 2. Реестр экранов

| # | Класс | Layout | Название экрана | Тип | Локальный Backend (таблица / менеджер) |
|---|---|---|---|---|---|
| 1 | `MainActivity` | `activity_main.xml` | «Календарный план» | Список + календарь | CALPLAN, HISTORY, NOTEPLAN, HEALTHPLAN / `ManageSQLDatabase` |
| 2 | `ParamsActivity` | `activity_params.xml` | «Параметры» | Настройки, 1 запись | CALPARAM / `ManageSQLDatabase` (`CalParamRecord`) |
| 3 | `ContactsActivity` | `activity_contacts.xml` | «Контакты» | Список + фильтр по набору букв | CONTACTS / `ManageSQLDatabase` |
| 4 | `EditContactActivity` | `activity_editcontact.xml` | Карточка контакта | Редактирование | CONTACTS / `ManageSQLDatabase` |
| 5 | `ProjectsActivity` | `activity_projects.xml` | Универсальный экран списков (Task 138, объединил ProjectTasksActivity): «Проекты\Все» / «Проекты\Рабочие» (extra `EXTRA_WORK_MODE`), «Для проекта: <имя>» / «Для заявки: <имя>» (extras `sourceForm`/`sourceUnid`/`sourceName`; фабрики `allIntent`/`projectIntent`/`requestIntent`/`linkedIntent`) | Список + поиск от 3 символов + добавление по режиму | CALPLAN / `ManageSQLDatabase` |
| 6 | `InputCalPlanActivity` | `activity_input_cal_plan.xml` | Карточка Проекта/Заявки (кнопка «Переделать» `btnRework`: Заявка→Проект/Задача, Проект→Задача/Заявка) | Редактирование | CALPLAN / `ManageSQLDatabase` |
| 7 | `TaskActivity` | `activity_input_cal_plan.xml` | Карточка Задачи (кнопка «Переделать» `btnRework`: Задача→Проект/Заявку) | Редактирование | CALPLAN / `ManageSQLDatabase` |
| 8 | `NoteActivity` | `activity_input_cal_plan.xml` | Карточка Заметки | Редактирование | NOTEPLAN / `NoteRememSQLManage` |
| 9 | `RememberActivity` | `activity_input_cal_plan.xml` | Карточка Напоминания | Редактирование | NOTEPLAN / `NoteRememSQLManage` |
| 10 | `HistoryActivity` | `activity_history.xml` | «<дата> История»; 3 строки в списке: название / дата создания со временем / текст, сверху самая поздняя (Task 129) | Список | HISTORY / `HistorySQLManage` |
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
| 21 | `SmsViewActivity` | `activity_smsview.xml` | «СМС от/для <Контакт>» (тип — вычисляемый `DisplayType` по позиции Ведущего, Task 130) | Просмотр (только чтение) | SMSCALPLAN / `SmsSQLManage` |
| 22 | `CrashActivity` | `activity_crash.xml` | Отчёт о сбое | Диагностика | — |
| 23 | — | — | Task 138: бывший `ProjectTasksActivity` (`activity_project_tasks.xml`) объединён с `ProjectsActivity` (режимы «Для проекта:/Для заявки:», layout `activity_projects.xml`) | — | CALPLAN / `ManageSQLDatabase` |

Все экраны зарегистрированы в `app/src/main/AndroidManifest.xml` (LAUNCHER — только `MainActivity`).

### Экран `MainActivity` (зоны)
1. Календарь: нестандартный `RussianCalendarView` (id `calendarView1`), выбор даты обновляет список.
2. Кнопки управления (в т.ч. быстрые кнопки LIVETYPE Button1..Button5 из CALPARAM: Шагомер/Бургер/Кофе и т.д.; Task 139: обработка кнопок и иконок — в хелпере `MainQuickButtons`, загрузка праздников — в `RussianHolidaysLoader`).
3. Список (id `listView1`) записей за выбранную дату: Заявки на проекты, Проекты, Задачи, Уведомления, Заметки.

### Шаблон заголовка экрана (единый стиль)
- Сверху заголовок, центрован влево, в той же строке ImageButton-кнопки (Add, Back и др.), центрованы вправо.
- Под заголовком (для списков): строка фильтра/поиска, затем список.

## 3. Иерархия классов Activity

```
AppCompatActivity
+-- BaseScreenActivity                      (меню main_menu.xml для всех наследников)
|   +-- MainActivity                        (LAUNCHER, меню из BaseScreenActivity, Task 133)
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
CrashActivity (extends android.app.Activity, singleTask)
```

## 4. Меню и карта навигации

Единое меню `res/menu/main_menu.xml` (в каждом экране через `BaseScreenActivity`; обработка в `BaseScreenActivity.onOptionsItemSelected`):

| Пункт меню (id) | Название | Переход / поведение |
|---|---|---|
| `calendar` | Календарь | `MainActivity` — вызов/возврат к первому экрану (`FLAG_ACTIVITY_CLEAR_TOP` + `FLAG_ACTIVITY_SINGLE_TOP`) (Task 135) |
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
- Карточки Проекта/Заявки (`BaseCalPlanEditActivity`, кнопка «Список задач») -> `ProjectsActivity.linkedIntent` в режимах «Для проекта:/Для заявки:» (Task 138).
- `SmsActivity` -> `SmsChatActivity` (extra `contactId`) / `SmsViewActivity` (extra `smsRecord`).
- `CrashActivity` запускается из `HardcoreCrashHandler` (перехват UncaughtException всех потоков). Обработчик ставится в `WGPortalApp.onCreate` (Application, Task 134) — `CrashActivity` показывается при любой необработанной ошибке на любом Activity, включая падения до `onCreate` экранов и рестарт процесса в другое Activity.
- Пункт меню `calendar` (в любом экране через `BaseScreenActivity`) — переход/возврат к первому `MainActivity` (Task 135).
- Вложения `InfoFieldView` открываются через `FileProvider` (authority `${applicationId}.fileprovider`, пути `res/xml/file_paths.xml`).

## 5. Нестандартные контролы (custom views)

| Класс | Наследование | Назначение | Использование |
|---|---|---|---|
| `RussianCalendarView` | `ConstraintLayout` | Нестандартный российский календарь (GridView + `CalendarAdapter`, праздники через `RussianHolidaysFetcher` в таблицу HOLIDAYS) | `MainActivity` (id `calendarView1`), layout `russian_calendar_view.xml`, callback `setOnDateSelectedListener` |
| `DateFieldView` | `LinearLayout` | Поле выбора даты с маской `__.__.____` | Карточки редактирования |
| `PhoneFieldView` | `LinearLayout` | Поле телефона, до 10 цифр, маска-заполнитель `•` | `EditContactActivity` |
| `InfoFieldView` | `LinearLayout` | Блок описания с вложениями и таблицами (файлы в папке CALPARAM.AttachFolder, раскрытие/сворачивание, RecyclerView блоков); строка под текст есть сразу при создании карточки, пустые текстовые блоки над/под текстом удаляются (Task 127). Кнопки: текст/картинка/видео/файл/**таблица**/**Ж-жирный**/**цвет текста** (Task 136). Таблица: диалог выбора строк×колонок (NumberPicker 1..10, умолчание 2×2), блок JSON `"tbl"` (`r`,`c`,`cells` — HTML каждой ячейки), EditText в каждой ячейке с рамкой `bg_table_cell`, у таблицы кнопка удаления. Жирный (`ic_bold_t`) и цвет (`ic_text_color`, вертикальный ряд из 7 цветов) применяются к выделению, а без выделения — ко всему тексту последнего фокусного EditText (строки текста или ячейки таблицы), спаны `StyleSpan(BOLD)`/`ForegroundColorSpan` → `<b>`/`<font color>`; Task 139: форматирование (жирный/цвет) вынесено в хелпер `InfoTextFormat`, свёрнутое превью — в `InfoPreview`. Task 141: цвет/жирный в ячейках таблицы сохраняется (после смены спанов — явный `writeBack`/`writeCell`); пустой текстовый блок всегда вставляется ПЕРВЫМ элементом (`ensureTextLine` добавляет блок в позицию 0). Task 142: хелпер `listText(stored, comment)` — в списках показывается Info, а если оно пусто — Комментарий (MainActivity, HistoryActivity, HealthActivity, ProjectsActivity). Task 143: хелперы `fromHtmlTrimmed`/`toHtmlTrimmed` обрезают хвостовые `\n` при загрузке/записи блоков — лишние строки в EditText могут создавать только пользователь | Карточки редактирования |
| `MessageListItem` | `LinearLayout` | Универсальный элемент списка: иконка + 1..N строк текста + кнопки (Редактировать/Удалить, или Просмотр), разделитель `--5--` | Все списки (бывший TwoLineListItem, Task 125) |
| `FlyOutContainer` | `LinearLayout` | Выдвижной контейнер (fly-out) | Резерв |
| `StatusIconFactory` | (utility, final) | Иконки статусов записей по цветам состояний; статус рисуется полным словом (Draft/Work/Test/Ok/Hold/Cancel), шрифт автоуменьшается под ширину иконки (Task 132) | Списки |
| `InfoBlocksAdapter` | `RecyclerView.Adapter` | Сетка вложений (1/4 ширины строки) и таблицы (viewType 4, `TableHolder`: сетка rows×cols EditText-ячеек), тап по вложению открывает стандартный просмотрщик; `focusedOrLastEditor()` — EditText где стоял курсор, `writeBack()` — явная запись HTML после смены спанов (Task 136) | `InfoFieldView` |

## 6. Данные, передаваемые между экранами

- Карточка -> вызывающий экран: `Intent` result, extra `"calPlanRecord"` (Serializable) — сохраняет вызывающий экран.
- `ProjectsActivity`: extra `EXTRA_WORK_MODE` (boolean, режим «Рабочие»), `EXTRA_PRESELECT_FORM` (Form новой записи); Task 138 — extras `sourceForm`/`sourceUnid`/`sourceName` (режимы «Для проекта:/Для заявки:», фабрики `allIntent`/`projectIntent`/`requestIntent`/`linkedIntent`), результат карточек через `ActivityResultLauncher`.
- `SmsActivity`: extra `EXTRA_SMS_FOLDER` (FOLDER_ALL/FOLDER_INCOME/FOLDER_OUTCOME/FOLDER_TRASH); Корзина = записи без FromID и ToID.
- `SmsChatActivity`: extra `EXTRA_CONTACT_ID` (`"contactId"`); требует телефон 10 цифр у Ведущего или Контакта, иначе Toast и выход.
- `SmsViewActivity`: extra `EXTRA_SMS_RECORD` (`"smsRecord"`); тип записи — вычисляемое поле `smsRecord.DisplayType` (`effectiveType(vedushiiId)`): From и To разные и один из них Ведущий → по позиции Ведущего (от Ведущего — Outgoing, до Ведущего — Incoming), иначе сохранённый `Type` (Task 130).
- Кнопка «Переделать» (`btnRework`, иконка `ic_type_project`) на карточках Проекта/Задачи/Заявки (`BaseCalPlanEditActivity`): диалог выбора новой Form (Заявка: Проект/Задача; Проект: Задача/Заявка; Задача: Проект/Заявка) → `record.Form` меняется, запись переоткрывается в `InputCalPlanActivity`/`TaskActivity` (extra `calPlanRecord`, `activeDate`, `EXTRA_PROJECTS_FORM_MODE`) (Task 131).

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
