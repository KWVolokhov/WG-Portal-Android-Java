package com.example.calendar4;

import java.io.Serializable;
import java.util.Date;

/**
 * Record for the LIVETYPE SQLite table ("Типы жизнедеятельности" справочник).
 * Repeats the organ/morality/skin Number fields of HEALTHPLAN (healthPlanRecord),
 * plus Название (Name), Категория (Category), Автор (AuthorID/AuthorName) and
 * Дата создания (DateCreated).
 */
public class livetypeRecord implements Serializable {

    // =====================================================================
    // Task 40: Form is the ID field for Category (одно поле выбора - Категория).
    // Category -> Form: "Пища"->HealthEat, "Гидратация"->HealthDrink,
    // "Физ. активность"->HealthSport, "Стресс"->HealthStress, "Гедонизм"->HealthJoy.
    // =====================================================================
    public static final String[] CATEGORIES = {"Пища", "Гидратация", "Физ. активность", "Стресс", "Гедонизм"};
    public static final String[] CATEGORY_FORMS = {"HealthEat", "HealthDrink", "HealthSport", "HealthStress", "HealthJoy"};

    /** Form for the given Category (null when the category is unknown). */
    public static String formForCategory(String category) {
        if (category != null) {
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].equalsIgnoreCase(category)) return CATEGORY_FORMS[i];
            }
        }
        return null;
    }

    /** Category for the given Form (null when the form is unknown). */
    public static String categoryForForm(String form) {
        if (form != null) {
            for (int i = 0; i < CATEGORY_FORMS.length; i++) {
                if (CATEGORY_FORMS[i].equalsIgnoreCase(form)) return CATEGORIES[i];
            }
        }
        return null;
    }

    // ----- Common fields -----
    public Integer id;
    public String UNID;
    public String Category; // Категория (Пища, Гидратация, Физ. активность, Стресс, Гедонизм)
    public String Form;     // Тип (равен Form из HEALTHPLAN: HealthSport/HealthEat/...)
    public String Name;     // Название типа жизнедеятельности
    public String Icon;     // Имя картинки (drawable), например ic_pedometer - для кнопок
    public String AuthorID; // ID Автора (из "Ведущий" в CALPARAM)
    public String AuthorName; // Автор
    public Date DateCreated; // Дата создания

    // ----- Organ / state Number fields (same as HEALTHPLAN) -----
    public Integer Head;        // Голова
    public Integer Eyes;        // Глаза
    public Integer Ears;        // Уши
    public Integer Nose;        // Нос
    public Integer Throat;      // Горло
    public Integer Teeth;       // Зубы
    public Integer Stomach;     // Желудок
    public Integer Intestines;  // Кишечник
    public Integer Liver;       // Печень
    public Integer Kidneys;     // Почки
    public Integer Heart;       // Сердце
    public Integer Lungs;       // Лёгкие
    public Integer Pressure;    // Давление
    public Integer Sleep;       // Сон
    public Integer Weight;      // Вес
    public Integer Nervous;     // Нервная система
    public Integer Morality;    // Мораль
    public Integer Skin;        // Состояние кожи

	public Integer Steps;        // Шаги
	public Integer FoodWeight;   // Вес еды
	public Integer DrinkValue;   // Объем питья
	public Integer Kallory;      // Каллории
	public Integer StepCounter;  // Разрешён ли шагомер для этого типа (1=да, 0/нет - Task 42)
	
    public livetypeRecord() {
    }

    public livetypeRecord(String name, String category) {
        this.Name = name;
        this.Category = category;
    }
}