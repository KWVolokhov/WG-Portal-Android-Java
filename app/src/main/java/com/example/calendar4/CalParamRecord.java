package com.example.calendar4;

import java.io.Serializable;

public class CalParamRecord implements Serializable {
    // Values stored in the CALPARAM.StartPage column
    public static final String START_PAGE_CALENDAR = "Calendar";   // MainActivity (по умолчанию)
    public static final String START_PAGE_CONTACTS = "Contacts";   // activity_contacts
    public static final String START_PAGE_PROJECTS = "Projects";   // activity_projects
    public static final String START_PAGE_PARAMS   = "Params";     // activity_params

    // Intent extra marking an activity opened as the app start page
    // (system "Back" then closes the app; the internal "X" still returns to MainActivity)
    public static final String EXTRA_IS_START_PAGE = "extra_is_start_page";

    // Три настраиваемые кнопки на MainActivity. Значение = числовой id записи LIVETYPE.
    // По умолчанию: Шагомер=1 (HealthSport/Прогулка), Бургер=2 (HealthEat), Кофе 200мл=3 (HealthDrink).
    // Эти id фиксированы в INSERT_LIVETYPE (предустановки 1..5).
    public static final int DEFAULT_BUTTON1_ID = 1; // Шагомер / Прогулка
    public static final int DEFAULT_BUTTON2_ID = 2; // Бургер
    public static final int DEFAULT_BUTTON3_ID = 3; // Кофе 200мл
	public static final int DEFAULT_BUTTON4_ID = 4; // Стресс
	public static final int DEFAULT_BUTTON5_ID = 5; // Гулянка

	// Task 54: параметры тела (умолчания по задаче)
	public static final int DEFAULT_HEIGHT = 190; // Рост, см
	public static final int DEFAULT_WEIGHT = 110; // Вес, кг
	public static final int DEFAULT_AGE = 50;     // Возраст, лет

	// Task 100: папка вложений поля InfoFieldView и имя базы данных
	public static final String DEFAULT_ATTACH_FOLDER = "Attachments";
	public static final String DEFAULT_DB_NAME = "WGPlanDatabase.db";

    public Integer id;
    public String Address;
    public String Name;
    public String Password;
    public String Vedushii;   // Ведущий (имя контакта, выбирается из CONTACTS)
    public String VedushiiID; // Идентификатор (EntryID/id) контакта-ведущего
    public String StartPage;  // Стартовая страница (null = Календарь по умолчанию)
    public Integer Button1Id; // Числовой id записи LIVETYPE для кнопки 1 (null = по умолчанию 1)
    public Integer Button2Id; // Числовой id записи LIVETYPE для кнопки 2 (null = по умолчанию 2)
    public Integer Button3Id; // Числовой id записи LIVETYPE для кнопки 3 (null = по умолчанию 3)
    public Integer Button4Id; // Числовой id записи LIVETYPE для кнопки 4 (null = по умолчанию 4)
    public Integer Button5Id; // Числовой id записи LIVETYPE для кнопки 5 (null = по умолчанию 5)
    public Integer Height;      // Рост, см (null = умолчание 190)
    public Integer Weight;      // Вес, кг (null = умолчание 110)
    public Integer Age;         // Возраст, лет (null = умолчание 50)
    public String AttachFolder; // Папка вложений поля InfoFieldView (null = "Attachments")
    public String DBName;       // Название базы данных (null = "WGPlanDatabase.db")

    public CalParamRecord() {
    }

    public CalParamRecord(String address, String name, String password) {
        this.Address = address;
        this.Name = name;
        this.Password = password;
    }
}