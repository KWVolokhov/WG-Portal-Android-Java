package com.example.calendar4;

import java.io.Serializable;
import java.util.Date;

/**
 * Record for the HEALTHPLAN SQLite table (screens HealthEat / HealthDrink / HealthSport).
 * Repeats the common fields of calPlanRecord and adds a set of Number fields
 * corresponding to human organs, plus Morality (Мораль) and Skin condition
 * (Состояние кожи). AuthorID/AuthorName are filled from the "Ведущий"
 * (CALPARAM) parameters when a record is saved (see HealthSQLManage).
 */
public class healthPlanRecord implements Serializable {

    // ----- Common fields (same as calPlanRecord) -----
    public Integer id;
    public String UNID;
    public String Form; // 'HealthEat', 'HealthDrink', 'HealthSport'
    public Date Okdate;
    public String AuthorID;
    public String AuthorName;
    public String LastUpdatedByID;
    public String LastUpdatedBy;
    public Date LastUpdatedDate;
    public Date StartDate;
    public Date EndDate;
    public String Name;
    public String BodyText;
    public String Comment;
    public String Revisions;

    // ----- Health-specific Number fields (human organs) -----
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

    public healthPlanRecord() {
        this.Form = "HealthEat";
    }

    public healthPlanRecord(String form, String name) {
        this();
        this.Form = form;
        this.Name = name;
    }
}