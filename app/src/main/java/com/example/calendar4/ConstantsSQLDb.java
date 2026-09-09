package com.example.calendar4;

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
            "FoodWeight INTEGER, " +//Label: Вес еды
            "DrinkValue INTEGER, " +//Label: Объем питья
            "Kallory INTEGER " +//Label: Каллории
            ")";
public static final String CREATE_TABLE_LIVETYPE = "CREATE TABLE IF NOT EXISTS LIVETYPE (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +//Label: Номер записи10 bytes
            "UNID TEXT UNIQUE, " +//Label: Уникальный ID34 bytes (у предустановленных равен Form из HEALTHPLAN)
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
            "FoodWeight INTEGER, " +//Label: Вес еды
            "DrinkValue INTEGER, " +//Label: Объем питья
            "Kallory INTEGER " +//Label: Каллории
            ")";
    public static final String CREATE_TABLE_CLASSIFICATOR = "CREATE TABLE IF NOT EXISTS CLASSIFICATOR (" +
            "ID TEXT PRIMARY KEY, " +		//Label: ID
            "IDS TEXT, " +
            "CATEGORY TEXT, " +				//Label: USERPLAN, STATUSPLAN, SYSTEMSPLAN
            "SONAME TEXT " +					//Label: Сотудник
            ")";
    public static final String[] INSERT_CLASSIFICATOR =
            {"INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('EXEC', 'USERPLAN', 'Волохов Вячеслав');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('IT', 'USERPLAN', 'Александов Алексей');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('BUSINESS', 'USERPLAN', 'Соколов Юрий');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('BOSSIT', 'USERPLAN', 'Солнцев Сергей');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('Draft', 'STATUSPLAN', 'Ченовик');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('Inwork', 'STATUSPLAN', 'В работе');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('Intest', 'STATUSPLAN', 'Тестирование');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('Done', 'STATUSPLAN', 'Завершено');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('Hold', 'STATUSPLAN', 'Отложенно');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('LotusNotes', 'SYSTEMSPLAN', 'HCL Lotus Notes');",
                    "INSERT INTO CLASSIFICATOR (IDS, CATEGORY, SONAME) VALUES ('RealLife', 'SYSTEMSPLAN', 'Без системы');"};

    // Начальное заполнение справочника LIVETYPE ("Типы жизнедеятельности").
    // По одной записи: Название, Тип (равен Form из HEALTHPLAN), Категория жизнедеятельности,
    // Имя картинки (Icon) для трёх настраиваемых кнопок на MainActivity.
    // Категория указывается сразу под названием в списке Типов жизнедеятельности.
    // ID предустановок фиксируются явно (1..5), чтобы кнопки по умолчанию
    // на MainActivity ссылались на них числовым id (Шагомер=1, Бургер=2, Кофе 200мл=3).
    // Column Form = ID-поле для Category; StepCounter=1 только у HealthSport (шагомер).
    public static final String[] INSERT_LIVETYPE =
            {"INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter) VALUES (1, 'HealthSport', 'Прогулка', 'Физ. активность', 'HealthSport', 'ic_pedometer', 'Исходная настройка', CURRENT_TIMESTAMP, 1);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter) VALUES (2, 'HealthEat', 'Бургер', 'Пища', 'HealthEat', 'ic_burger', 'Исходная настройка', CURRENT_TIMESTAMP, 0);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter) VALUES (3, 'HealthDrink', 'Кофе 200мл', 'Гидратация', 'HealthDrink', 'ic_coffee', 'Исходная настройка', CURRENT_TIMESTAMP, 0);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter) VALUES (4, 'HealthStress', 'Авария', 'Стресс', 'HealthStress', 'ic_stress', 'Исходная настройка', CURRENT_TIMESTAMP, 0);",
                    "INSERT OR IGNORE INTO LIVETYPE (id, UNID, Name, Category, Form, Icon, AuthorName, DateCreated, StepCounter) VALUES (5, 'HealthJoy', 'Гулянка', 'Гедонизм', 'HealthJoy', 'ic_joy', 'Исходная настройка', CURRENT_TIMESTAMP, 0);"};

    // Дозаполнение иконок у предустановленных записей LIVETYPE при обновлении уже существующей БД
    // (только там, где иконка ещё не задана, чтобы не перетирать пользовательские настройки).
    public static final String[] UPDATE_LIVETYPE_ICONS =
            {"UPDATE LIVETYPE SET Icon='ic_pedometer' WHERE UNID='HealthSport' AND (Icon IS NULL OR Icon='');",
                    "UPDATE LIVETYPE SET Icon='ic_burger' WHERE UNID='HealthEat' AND (Icon IS NULL OR Icon='');",
                    "UPDATE LIVETYPE SET Icon='ic_coffee' WHERE UNID='HealthDrink' AND (Icon IS NULL OR Icon='');",
                    "UPDATE LIVETYPE SET Icon='ic_stress' WHERE UNID='HealthStress' AND (Icon IS NULL OR Icon='');",
                    "UPDATE LIVETYPE SET Icon='ic_joy' WHERE UNID='HealthJoy' AND (Icon IS NULL OR Icon='');"};

    // Дозаполнение новых полей LIVETYPE при обновлении уже существующей БД:
    // шагомер разрешён по умолчанию только у предустановленного HealthSport (Прогулка),
    // у остальных предустановок - выключен (не перетираем пользовательские настройки).
    public static final String[] UPDATE_LIVETYPE_DEFAULTS =
            {"UPDATE LIVETYPE SET StepCounter=1 WHERE UNID='HealthSport' AND (StepCounter IS NULL OR StepCounter=0);",
                    "UPDATE LIVETYPE SET StepCounter=0 WHERE UNID IN ('HealthEat','HealthDrink','HealthStress','HealthJoy') AND StepCounter IS NULL;"};
    
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
            "DBName TEXT" +//Label: Название базы данных
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
            "AuthorID TEXT, " +        //Label: ID Автора
            "AuthorName TEXT, " +      //Label: Автор
            "LastUpdatedByID TEXT, " + //Label: ID Обновившего
            "LastUpdatedBy TEXT" +     //Label: Обновивший
            ")";
}
