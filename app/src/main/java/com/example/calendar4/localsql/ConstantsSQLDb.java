package com.example.calendar4.localsql;

public class ConstantsSQLDb {
    public static final String CREATE_TABLE_CALPLAN = "CREATE TABLE IF NOT EXISTS CALPLAN (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +	//Label: Номер Проекта	10 bytes
            "UNID TEXT UNIQUE, " +			//Label: Уникальный ID	34 bytes
            "Form TEXT DEFAULT 'Project', " +	//Label: Форма 'Project', 'Task', 'Request'
            "Priority INTEGER, " +		 	//Label: Приоритет	10 bytes
            "Okdate DATE, " + 				//Label: Дата заведения проекта
            "AuthorID TEXT, " +				//Label: ID Автора Проекта
            "AuthorName TEXT, " +			//Label: Автор Проекта
            "Name TEXT NOT NULL, " +		//Label: Проект		41 bytes
            "RequestName TEXT, " +			//Label: Заявка на автоматизацию
            "RequestUNID TEXT, " +			//Label: UNID Заявка на автоматизацию	34 bytes
            "Status TEXT, " +				//Label: Назваие Статуса
            "StatusID INTEGER, " +			//Label: Идентификатор Статуса
            "MainSystem TEXT, " +			//Label: Основная система
            "AnalitikID TEXT, " +			//Label: ID Постановщика
            "AnalitikName TEXT, " +			//Label: Постановщик
            "ExectorID TEXT, " +			//Label: ID Исполнителя
            "ExectorName TEXT, " +			//Label: Исполнитель
            "LastUpdatedByID TEXT, " +		//Label: ID Последнего обновившего
            "LastUpdatedBy TEXT, " +		//Label: Последний обновивший
            "LastUpdatedDate DATE, " +		//Label: Дата Последнего обновления
            "BodyText TEXT, " +				//Label: Описание задачи
            "Comment TEXT, " +				//Label: Комментарий
            "StartDate DATE, " +        		//Label: Дата старта проекта, по умолчанию должна быть ровна Okdate и может меняться в процессе
            "EndDate DATE, " + 				//Label: Дата завершения проекта факт
            "HoldDate DATE, " + 			//Label: Дата Откладывания проекта
            "InstallOrder TEXT, " +			//Label: Описание порядка установки или настройки
            "KeyWords TEXT, " +				//Label: Ключевые слова
            "Revisions TEXT " +			//Label: Даты изменения друг за друом
            ")";
    public static final String CREATE_TABLE_HISTORY = "CREATE TABLE IF NOT EXISTS HISTORY (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +//Label: Номер записи10 bytes
            "UNID TEXT UNIQUE, " +//Label: Уникальный ID34 bytes
            "Okdate DATE, " + //Label: Дата заведения записи
            "AuthorID TEXT, " +//Label: ID Автора
            "AuthorName TEXT, " +//Label: Автор
            "LastUpdatedByID TEXT, " +//Label: ID Последнего обновившего
            "LastUpdatedBy TEXT, " +//Label: Последний обновивший
            "LastUpdatedDate DATE, " +//Label: Дата Последнего обновления
            "Name TEXT NOT NULL, " +//Label: Название записи
            "BodyText TEXT, " +//Label: Расшифровка
            "Comment TEXT, " +//Label: Комментарий
            "StartDate DATE, " + //Label: Дата записи (для показа в календаре)
            "Revisions TEXT " +//Label: Даты изменения друг за друом
            ")";
    public static final String CREATE_TABLE_NOTEPLAN = "CREATE TABLE IF NOT EXISTS NOTEPLAN (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +//Label: Номер записи10 bytes
            "UNID TEXT UNIQUE, " +//Label: Уникальный ID34 bytes
            "Form TEXT DEFAULT 'Note', " +//Label: Форма 'Note', 'Remember'
            "Okdate DATE, " + //Label: Дата заведения записи
            "AuthorID TEXT, " +//Label: ID Автора
            "AuthorName TEXT, " +//Label: Автор
            "LastUpdatedByID TEXT, " +//Label: ID Последнего обновившего
            "LastUpdatedBy TEXT, " +//Label: Последний обновивший
            "LastUpdatedDate DATE, " +//Label: Дата Последнего обновления
            "Name TEXT NOT NULL, " +//Label: Название
            "Status TEXT, " +//Label: Состояние
            "StatusID TEXT, " +//Label: Идентификатор состояния
            "StartDate DATE, " + //Label: Дата записи/напоминания
            "BodyText TEXT, " +//Label: Текст Заметки/Напоминания
            "Comment TEXT, " +//Label: Комментарий
            "KeyWords TEXT, " +//Label: Ключевые слова
            "Revisions TEXT " +//Label: Даты изменения друг за друом
            ")";
    public static final String CREATE_TABLE_HEALTHPLAN = "CREATE TABLE IF NOT EXISTS HEALTHPLAN (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +//Label: Номер записи10 bytes
            "UNID TEXT UNIQUE, " +//Label: Уникальный ID34 bytes
            "Form TEXT DEFAULT 'HealthEat', " +//Label: Форма 'HealthEat', 'HealthDrink', 'HealthSport'
            "Okdate DATE, " + //Label: Дата заведения записи
            "AuthorID TEXT, " +//Label: ID Автора
            "AuthorName TEXT, " +//Label: Автор
            "LastUpdatedByID TEXT, " +//Label: ID Последнего обновившего
            "LastUpdatedBy TEXT, " +//Label: Последний обновивший
            "LastUpdatedDate DATE, " +//Label: Дата Последнего обновления
            "Name TEXT NOT NULL, " +//Label: Название
            "BodyText TEXT, " +//Label: Расшифровка
            "Comment TEXT, " +//Label: Комментарий
            "StartDate DATE, " + //Label: Дата записи (для показа в календаре)
            "EndDate DATE, " + //Label: Дата завершения (факт)
            "Revisions TEXT, " +//Label: Даты изменения друг за друом
            "Head INTEGER, " +//Label: Голова
            "Eyes INTEGER, " +//Label: Глаза
            "Ears INTEGER, " +//Label: Уши
            "Nose INTEGER, " +//Label: Нос
            "Throat INTEGER, " +//Label: Горло
            "Teeth INTEGER, " +//Label: Зубы
            "Stomach INTEGER, " +//Label: Желудок
            "Intestines INTEGER, " +//Label: Кишечник
            "Liver INTEGER, " +//Label: Печень
            "Kidneys INTEGER, " +//Label: Почки
            "Heart INTEGER, " +//Label: Сердце
            "Lungs INTEGER, " +//Label: Лёгкие
            "Pressure INTEGER, " +//Label: Давление
            "Sleep INTEGER, " +//Label: Сон
            "Weight INTEGER, " +//Label: Вес
            "Nervous INTEGER, " +//Label: Нервная система
            "Morality INTEGER, " +//Label: Мораль
            "Skin INTEGER, " +//Label: Состояние кожи
            "Steps INTEGER, " +//Label: Шаги
            "FoodWeight REAL, " +//Label: Вес еды (дробный - Task 149)
            "DrinkValue REAL, " +//Label: Объем питья (дробный - Task 149)
            "Kallory INTEGER, " +//Label: Каллории
            "Icon TEXT " +//Label: Имя иконки drawable (из LIVETYPE)
            ")";
public static final String CREATE_TABLE_LIVETYPE = "CREATE TABLE IF NOT EXISTS LIVETYPE (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +//Label: Номер записи10 bytes
            "UNID TEXT UNIQUE, " +//Label: Уникальный ID 32 символа HEX (предустановки: Form_+ counter)
            "Name TEXT NOT NULL, " +//Label: Название типа жизнедеятельности
            "Category TEXT, " +//Label: Категория жизнедеятельности (Пища, Гидратация, Физ. активность, Стресс, Гедонизм)
            "Icon TEXT, " +//Label: Имя картинки (drawable) для кнопки, например ic_pedometer
            "AuthorID TEXT, " +//Label: ID Автора
            "AuthorName TEXT, " +//Label: Автор
            "DateCreated DATE, " +//Label: Дата создания
            "Head INTEGER, " +//Label: Голова
            "Eyes INTEGER, " +//Label: Глаза
            "Ears INTEGER, " +//Label: Уши
            "Nose INTEGER, " +//Label: Нос
            "Throat INTEGER, " +//Label: Горло
            "Teeth INTEGER, " +//Label: Зубы
            "Stomach INTEGER, " +//Label: Желудок
            "Intestines INTEGER, " +//Label: Кишечник
            "Liver INTEGER, " +//Label: Печень
            "Kidneys INTEGER, " +//Label: Почки
            "Heart INTEGER, " +//Label: Сердце
            "Lungs INTEGER, " +//Label: Лёгкие
            "Pressure INTEGER, " +//Label: Давление
            "Sleep INTEGER, " +//Label: Сон
            "Weight INTEGER, " +//Label: Вес
            "Nervous INTEGER, " +//Label: Нервная система
            "Morality INTEGER, " +//Label: Мораль
            "Skin INTEGER, " +//Label: Состояние кожи
            "Form TEXT, " +//Label: Тип жизнедеятельности (ID-поле для Category: HealthEat/HealthDrink/HealthSport/HealthStress/HealthJoy)
            "StepCounter INTEGER DEFAULT 0, " +//Label: Разрешён ли шагомер (1=да, 0/нет)
            "Steps INTEGER, " +//Label: Шаги
            "FoodWeight REAL, " +//Label: Вес еды (дробный - Task 149)
            "DrinkValue REAL, " +//Label: Объем питья (дробный - Task 149)
            "Kallory INTEGER " +//Label: Каллории
            ")";
    public static final String CREATE_TABLE_CLASSIFICATOR = "CREATE TABLE IF NOT EXISTS CLASSIFICATOR (" +
            "ID TEXT PRIMARY KEY, " +		//Label: ID
            "IDS TEXT, " +
            "CATEGORY TEXT, " +				//Label: USERPLAN, STATUSPLAN, SYSTEMSPLAN
            "SONAME TEXT " +					//Label: Сотудник
            ")";
    public static final String[] INSERT_CLASSIFICATOR =
            {"INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('Draft', 'STATUSPLAN', 'Ченовик');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('Inwork', 'STATUSPLAN', 'В работе');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('Intest', 'STATUSPLAN', 'Тестирование');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('Done', 'STATUSPLAN', 'Завершено');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('Hold', 'STATUSPLAN', 'Отложенно');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('LotusNotes', 'SYSTEMSPLAN', 'HCL Lotus Notes');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('RealLife', 'SYSTEMSPLAN', 'Без системы');"};

    // Начальное заполнение справочника LIVETYPE ("Типы жизнедеятельности").
    // UNID предустановок = Form + "_" + zero-padded ID (итого 32 символа).
    // Icon у предустановок пустой (""); иконка подтягивается по умолчанию на основании Form.
    // ID предустановок фиксируются явно (1..9), чтобы кнопки по умолчанию
    // на MainActivity ссылались на них числовым id.
    // Column Form = ID-поле для Category; StepCounter=1 только у HealthSport (шагомер).
    // FoodWeight/DrinkValue/Kallory заполняются для соответствующих типов.
    public static final String[] INSERT_LIVETYPE =
            {"INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter) VALUES (1, 'HealthSport_00000000000000000001', 'Прогулка', 'Физ. активность', 'HealthSport', '', 'Исходная настройка', CURRENT_TIMESTAMP, 1);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter, FoodWeight, Kallory) VALUES (2, 'HealthEat_0000000000000000000002', 'Бургер', 'Пища', 'HealthEat', '', 'Исходная настройка', CURRENT_TIMESTAMP, 0, 0.16, 420);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter, DrinkValue, Kallory) VALUES (3, 'HealthDrink_00000000000000000003', 'Кофе 200мл раств', 'Гидратация', 'HealthDrink', '', 'Исходная настройка', CURRENT_TIMESTAMP, 0, 0.2, 3);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter) VALUES (4, 'HealthStress_0000000000000000004', 'Авария', 'Стресс', 'HealthStress', '', 'Исходная настройка', CURRENT_TIMESTAMP, 0);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter) VALUES (5, 'HealthJoy_0000000000000000000005', 'Гулянка', 'Гедонизм', 'HealthJoy', '', 'Исходная настройка', CURRENT_TIMESTAMP, 0);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter, DrinkValue, Kallory) VALUES (6, 'HealthDrink_00000000000000000006', 'Американо 200мл', 'Гидратация', 'HealthDrink', 'ic_americano', 'Исходная настройка', CURRENT_TIMESTAMP, 0, 0.2, 7);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter, DrinkValue, Kallory) VALUES (7, 'HealthDrink_00000000000000000007', 'Латте 250мл', 'Гидратация', 'HealthDrink', 'ic_latte', 'Исходная настройка', CURRENT_TIMESTAMP, 0, 0.25, 130);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter, DrinkValue) VALUES (8, 'HealthDrink_00000000000000000008', 'Вода Королевская 200мл', 'Гидратация', 'HealthDrink', '', 'Исходная настройка', CURRENT_TIMESTAMP, 0, 0.2);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter, FoodWeight, Kallory) VALUES (9, 'HealthEat_0000000000000000000009', 'Каша с Мясом', 'Пища', 'HealthEat', 'ic_container400', 'Исходная настройка', CURRENT_TIMESTAMP, 0, 0.33, 550);"};

    // Дозаполнение иконок у предустановленных записей LIVETYPE при обновлении уже существующей БД
    // (ICON у предустановок теперь пустой — подтягивается по умолчанию на основании Form;
    // массив оставлен пустым, чтобы не перезаписывать пустые иконки предустановок).
    public static final String[] UPDATE_LIVETYPE_ICONS = {};

    // Дозаполнение новых полей LIVETYPE при обновлении уже существующей БД:
    // шагомер разрешён по умолчанию только у предустановленного HealthSport (Прогулка),
    // у остальных предустановок - выключен (не перетираем пользовательские настройки).
    public static final String[] UPDATE_LIVETYPE_DEFAULTS =
            {"UPDATE LIVETYPE SET StepCounter=1 WHERE FORM='HealthSport' AND (StepCounter IS NULL OR StepCounter=0);",
                    "UPDATE LIVETYPE SET StepCounter=0 WHERE FORM IN ('HealthEat','HealthDrink','HealthStress','HealthJoy') AND StepCounter IS NULL;"};
    
    public static final String CREATE_TABLE_CALPARAM = "CREATE TABLE IF NOT EXISTS CALPARAM (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "Address TEXT, " +
            "Name TEXT, " +
            "Password TEXT, " +
            "Vedushii TEXT, " +
            "VedushiiID TEXT, " +
            "StartPage TEXT, " +
            "Button1Id TEXT, " +//Label: id записи LIVETYPE для кнопки 1 (по умолчанию HealthSport/Шагомер)
            "Button2Id TEXT, " +//Label: id записи LIVETYPE для кнопки 2 (по умолчанию HealthEat/Бургер)
			"Button3Id TEXT, " +//Label: id записи LIVETYPE для кнопки 3 (по умолчанию HealthDrink/Кофе 200мл)
			"Button4Id TEXT, " +//Label: id записи LIVETYPE для кнопки 4 (по умолчанию HealthStress/Авария)
            "Button5Id TEXT, " +//Label: id записи LIVETYPE для кнопки 5 (по умолчанию HealthJoy/Гулянка)
            "Height INTEGER, " +//Label: Рост, см (умолчание 190)
            "Weight INTEGER, " +//Label: Вес, кг (умолчание 110)
            "Age INTEGER, " +//Label: Возраст, лет (умолчание 50)
            "AttachFolder TEXT, " +//Label: Папка вложений InfoFieldView (умолчание Attachments)
            "DBName TEXT, " +//Label: Название базы данных
            "SyncTime TEXT" +//Label: Время синхронизации (Task 143)
            ")";
    
    public static final String CREATE_TABLE_HOLIDAYS = "CREATE TABLE IF NOT EXISTS HOLIDAYS (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "CountryCode TEXT NOT NULL, " +
            "HolidayDate DATE NOT NULL, " +
            "HolidayName TEXT NOT NULL, " +
            "UNIQUE(CountryCode, HolidayDate)" +
            ")";
    
    public static final String CREATE_TABLE_CONTACTS = "CREATE TABLE IF NOT EXISTS CONTACTS (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "Surname TEXT, " +
            "FirstName TEXT, " +
            "Patronymic TEXT, " +
            "Phone TEXT, " +
            "Info TEXT, " +
            "Phone2 TEXT, " +
            "Email TEXT, " +
            "BirthDate DATE, " +
            "HomeAddress TEXT, " +
            "DateReceived DATE, " +
            "DateCreated DATE, " +
            "DateModified DATE, " +
            "EntryID TEXT, " +
            "UNID TEXT UNIQUE, " +    //Label: Уникальный ID контакта (Etot phone_ + до 32 символов, не отправляется на сервер)
            "AuthorID TEXT, " +        //Label: ID Автора
            "AuthorName TEXT, " +      //Label: Автор
            "LastUpdatedByID TEXT, " + //Label: ID Обновившего
            "LastUpdatedBy TEXT" +     //Label: Обновивший
            ")";

    // Task 106: таблица СМС (одна и та же локальная БД, отдельный java-класс SmsSQLManage).
    // Type наследуется от стандартного СМС: Incoming (входящие) / Outgoing (исходящие) / Draft (черновик).
    public static final String CREATE_TABLE_SMSCALPLAN = "CREATE TABLE IF NOT EXISTS SMSCALPLAN (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +         //Label: Номер записи СМС
            "UNID TEXT UNIQUE, " +                             //Label: Уникальный ID
            "Type TEXT DEFAULT 'Incoming', " +                 //Label: Тип (Incoming/Outgoing/Draft)
            "FromID TEXT, " +                                  //Label: ID контакта отправителя
            "FromName TEXT, " +                                //Label: Имя контакта отправителя
            "ToID TEXT, " +                                    //Label: ID контакта получателя
            "ToName TEXT, " +                                  //Label: Имя контакта получателя
            "Subject TEXT, " +                                 //Label: Тема
            "Body TEXT, " +                                    //Label: Тело СМС
            "Status TEXT, " +                                  //Label: Статус (New/Read) из стандартного СМС
            "DateReceived DATE " +                             //Label: Дата получения/отправки
            ")";
}
