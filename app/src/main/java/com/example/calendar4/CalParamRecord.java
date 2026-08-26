package com.example.calendar4;

import java.io.Serializable;

public class CalParamRecord implements Serializable {
    public Integer id;
    public String Address;
    public String Name;
    public String Password;
    public String Vedushii;   // Ведущий (имя контакта, выбирается из CONTACTS)
    public String VedushiiID; // Идентификатор (EntryID/id) контакта-ведущего

    public CalParamRecord() {
    }

    public CalParamRecord(String address, String name, String password) {
        this.Address = address;
        this.Name = name;
        this.Password = password;
    }
}