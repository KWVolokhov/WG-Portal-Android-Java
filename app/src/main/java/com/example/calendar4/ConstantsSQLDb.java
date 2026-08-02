package com.example.calendar4;

public class ConstantsSQLDb {
    public static final String CREATE_TABLE_CALPLAN = "CREATE TABLE IF NOT EXISTS CALPLAN (" +
            "id INTEGER AUTOINCREMENT, " +	//Label: Номер Проекта	10 bytes
            "UNID TEXT UNIQUE, " +			//Label: Уникальный ID	34 bytes
            "Form TEXT DEFAULT 'Project', " +	//Label: Форма
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
            "EndDate DATE, " + 				//Label: Дата завершения проекта факт
            "HoldDate DATE, " + 			//Label: Дата Откладывания проекта
            "InstallOrder TEXT, " +			//Label: Описание порядка установки или настройки
            "KeyWords TEXT, " +				//Label: Ключевые слова
            "Revisions TEXT, " +			//Label: Даты изменения друг за друом
            "PRIMARY KEY(id, UNID)" +
            ")";
    public static final String CREATE_TABLE_REQUESTPLAN = "CREATE TABLE IF NOT EXISTS REQUESTPLAN (" +
            "id INTEGER AUTOINCREMENT, " +	//Label: Номер Проекта	10 bytes
            "UNID TEXT UNIQUE, " +			//Label: Уникальный ID	34 bytes
            "Okdate DATE, " + 				//Label: Дата заведения проекта
            "AuthorID TEXT, " +				//Label: ID Автора Проекта
            "AuthorName TEXT, " +			//Label: Автор Проекта
            "RequestName TEXT, " +			//Label: Заявка на автоматизацию
            "AnalitikID TEXT, " +			//Label: ID Постановщика
            "AnalitikName TEXT, " +			//Label: Постановщик
            "LastUpdatedByID TEXT, " +		//Label: ID Последнего обновившего
            "LastUpdatedBy TEXT, " +		//Label: Последний обновивший
            "LastUpdatedDate DATE, " +		//Label: Дата Последнего обновления
            "BodyText TEXT, " +				//Label: Описание задачи
            "Comment TEXT, " +				//Label: Комментарий
            "Revisions TEXT, " +			//Label: Даты изменения друг за друом
            "PRIMARY KEY(id, UNID)" +
            ")";
    public static final String CREATE_TABLE_CLASSIFICATOR = "CREATE TABLE IF NOT EXISTS CLASSIFICATOR (" +
            "ID TEXT PRIMARY KEY, " +		//Label: ID
            "CATEGORY TEXT, " +				//Label: USERPLAN, STATUSPLAN, SYSTEMSPLAN
            "SONAME TEXT " +					//Label: Сотудник
            ")";
    public static final String[] INSERT_CLASSIFICATOR =
            {"INSERT INTO CLASSIFICATOR (ID, CATEGORY, SONAME) VALUES ('EXEC', 'USERPLAN', 'Волохов Вячеслав');",
                    "INSERT INTO CLASSIFICATOR (ID, CATEGORY, SONAME) VALUES ('IT', 'USERPLAN', 'Александов Алексей');",
                    "INSERT INTO CLASSIFICATOR (ID, CATEGORY, SONAME) VALUES ('BUSINESS', 'USERPLAN', 'Соколов Юрий');",
                    "INSERT INTO CLASSIFICATOR (ID, CATEGORY, SONAME) VALUES ('BOSSIT', 'USERPLAN', 'Солнцев Сергей');",
                    "INSERT INTO CLASSIFICATOR (ID, CATEGORY, SONAME) VALUES ('Draft', 'STATUSPLAN', 'Ченовик');",
                    "INSERT INTO CLASSIFICATOR (ID, CATEGORY, SONAME) VALUES ('Inwork', 'STATUSPLAN', 'В работе');",
                    "INSERT INTO CLASSIFICATOR (ID, CATEGORY, SONAME) VALUES ('Intest', 'STATUSPLAN', 'Тестирование');",
                    "INSERT INTO CLASSIFICATOR (ID, CATEGORY, SONAME) VALUES ('Done', 'STATUSPLAN', 'Завершено');",
                    "INSERT INTO CLASSIFICATOR (ID, CATEGORY, SONAME) VALUES ('Hold', 'STATUSPLAN', 'Отложенно');",
                    "INSERT INTO CLASSIFICATOR (ID, CATEGORY, SONAME) VALUES ('LotusNotes', 'SYSTEMSPLAN', 'HCL Lotus Notes');",
                    "INSERT INTO CLASSIFICATOR (ID, CATEGORY, SONAME) VALUES ('RealLife', 'SYSTEMSPLAN', 'Без системы');"};
}
