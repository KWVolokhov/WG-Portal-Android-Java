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

    public CalParamRecord() {
    }

    public CalParamRecord(String address, String name, String password) {
        this.Address = address;
        this.Name = name;
        this.Password = password;
    }
}