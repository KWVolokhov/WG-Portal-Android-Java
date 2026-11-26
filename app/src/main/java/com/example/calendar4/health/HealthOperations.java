package com.example.calendar4.health;

import java.util.Date;

import com.example.calendar4.records.healthPlanRecord;
import com.example.calendar4.records.livetypeRecord;

/**
 * Операции над записями здоровья (HEALTHPLAN).
 * Вынесено из {@link HealthActivity} и {@link com.example.calendar4.MainQuickButtons},
 * чтобы логика копирования записи из справочника LIVETYPE в новую Health-запись
 * была в одном месте и легко апгрейдировалась новыми операциями.
 */
public final class HealthOperations {

    private HealthOperations() {
    }

    /**
     * Создаёт новую Health-запись (HEALTHPLAN) на основе записи справочника LIVETYPE:
     * копируются Form, Название, все числовые поля органов/состояния,
     * Steps/FoodWeight/DrinkValue/Kallory и Icon; проставляются Автор, Okdate и StartDate.
     *
     * @param typeRecord запись справочника LIVETYPE-шаблона (не null)
     * @param authorID   ID автора ("Ведущий")
     * @param authorName имя автора
     * @param okdate     дата/время создания записи
     * @param startDate  дата старта (день здоровья)
     * @return новая {@link healthPlanRecord} без id (вставляется при сохранении), или null при null-шаблоне
     */
    public static healthPlanRecord buildRecordFromLivetype(livetypeRecord typeRecord,
                                                           String authorID, String authorName,
                                                           Date okdate, Date startDate) {
        if (typeRecord == null) return null;
        healthPlanRecord record = new healthPlanRecord(typeRecord);
        record.AuthorID = authorID;
        record.AuthorName = authorName;
        record.Okdate = okdate;
        record.StartDate = startDate;
        return record;
    }
}
