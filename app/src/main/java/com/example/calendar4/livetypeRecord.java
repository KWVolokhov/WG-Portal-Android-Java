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

    // ----- Common fields -----
    public Integer id;
    public String UNID;
    public String Form;     // Тип (равен Form из HEALTHPLAN: HealthSport/HealthEat/...)
    public String Name;     // Название типа жизнедеятельности
    public String Category; // Категория (Пища, Гидратация, Физ. активность, Стресс, Гедонизм)
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

    public livetypeRecord() {
    }

    public livetypeRecord(String name, String category) {
        this.Name = name;
        this.Category = category;
    }
}