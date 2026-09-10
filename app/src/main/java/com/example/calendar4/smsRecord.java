package com.example.calendar4;

import java.io.Serializable;
import java.util.Date;

/**
 * Task 106: одна запись СМС таблицы SMSCALPLAN.
 * Поля типа (Входящие/Исходящие/Черновик) унаследованы от стандартного СМС,
 * дополнительные поля - два контакта (имя и ID контакта) отправителя и получателя.
 */
public class smsRecord implements Serializable {
    public static final String TYPE_INCOMING = "Incoming";
    public static final String TYPE_OUTGOING = "Outgoing";
    public static final String TYPE_DRAFT = "Draft";

    public Integer id;
    public String UNID;
    public String Type;       // Incoming / Outgoing / Draft
    public String FromID;     // ID контакта отправителя (заполнен = контакт сопоставлен)
    public String FromName;   // Имя контакта отправителя
    public String ToID;       // ID контакта получателя (заполнен = контакт сопоставлен)
    public String ToName;     // Имя контакта получателя
    public String Subject;    // Тема
    public String Body;       // Тело СМС
    public String Status;     // Статус (New/Read) - необязательное из стандартного СМС
    public java.util.Date DateReceived; // Дата получения/отправки

    public smsRecord() {
    }

    public smsRecord(String type, String fromName, String fromId,
                     String toName, String toId, String subject, String body) {
        this.Type = type;
        this.FromName = fromName;
        this.FromID = fromId;
        this.ToName = toName;
        this.ToID = toId;
        this.Subject = subject;
        this.Body = body;
    }
}