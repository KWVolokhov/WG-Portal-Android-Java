package com.example.calendar4;

import java.io.Serializable;
import java.util.Date;

public class calPlanRecord implements Serializable {
    // Auto-filled fields (not input by user)
    public Integer id;
    public String UNID;
    public Date Okdate;
    public String AuthorID; // Always "BUSINESS"
    public String LastUpdatedByID;
    public String LastUpdatedBy;
    public Date LastUpdatedDate;
    public Date HoldDate;
    public String Revisions;

    // User input fields
    public String Form; // 'Project', 'Note', 'Remember', 'Task'
    public String Name; // Название
    public Integer Priority;
    public String AuthorName;
    public String RequestName;
    public String RequestUNID;
    public String Status;
    public String StatusID;
    public String MainSystem;
    public String AnalitikID;
    public String AnalitikName;
    public String ExectorID;
    public String ExectorName;
    public String BodyText;
    public String Comment;
    public Date StartDate;
    public Date EndDate;
    public String InstallOrder;
    public String KeyWords;

    // Health numbers added for the Health edit screens and quick buttons
    // (Task 41/42): согласованы с healthPlanRecord / livetypeRecord.
    public Integer Steps;        // Шаги
    public Integer FoodWeight;   // Вес еды
    public Integer DrinkValue;   // Объем питья
    public Integer Kallory;      // Каллории

    // Task 45: полный массив полей "Голова".."Каллории" из activity_livetype_edit
    // переносится на карточки всех наследников HealthEditActivity.
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

    public calPlanRecord() {
        // Initialize with defaults
        this.AuthorID = "BUSINESS";
        this.Form = "Project";
    }

    public calPlanRecord(String form, String name) {
        this();
        this.Form = form;
        this.Name = name;
    }
}