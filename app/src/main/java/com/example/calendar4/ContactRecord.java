package com.example.calendar4;

import java.io.Serializable;
import java.util.Date;

public class ContactRecord implements Serializable {
    public Integer id;
    public String Surname; // Фамилия
    public String FirstName; // Имя
    public String Patronymic; // Отчество
    public String Phone; // телефон
    public String Info; // Информация
    public String Phone2; // 2й телефон
    public String Email;
    public Date BirthDate; // Дата рождения
    public String HomeAddress; // Дом.Адресс
    public Date DateReceived; // Дата получения
    public Date DateCreated; // Дата создания
    public Date DateModified; // Дата изменения
    public String EntryID; // Идентификатор занесения

    public ContactRecord() {
    }

    public ContactRecord(String surname, String firstName, String patronymic, String phone) {
        this.Surname = surname;
        this.FirstName = firstName;
        this.Patronymic = patronymic;
        this.Phone = phone;
    }
}