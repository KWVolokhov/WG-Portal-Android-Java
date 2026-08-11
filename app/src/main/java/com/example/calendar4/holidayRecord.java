package com.example.calendar4;

import java.util.Date;

public class holidayRecord {
    public Integer id;
    public String CountryCode; // e.g., 'RUS' for Russia
    public Date HolidayDate;
    public String HolidayName;

    public holidayRecord() {
    }

    public holidayRecord(String countryCode, Date holidayDate, String holidayName) {
        this.CountryCode = countryCode;
        this.HolidayDate = holidayDate;
        this.HolidayName = holidayName;
    }
}