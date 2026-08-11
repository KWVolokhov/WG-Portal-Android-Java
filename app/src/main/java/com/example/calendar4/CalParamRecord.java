package com.example.calendar4;

import java.io.Serializable;

public class CalParamRecord implements Serializable {
    public Integer id;
    public String Address;
    public String Name;
    public String Password;

    public CalParamRecord() {
    }

    public CalParamRecord(String address, String name, String password) {
        this.Address = address;
        this.Name = name;
        this.Password = password;
    }
}