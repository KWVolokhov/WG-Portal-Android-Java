package com.example.calendar4;

import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Task 139: перенос записи calPlanRecord <-> поля карточки редактирования -
 * вынесен из BaseCalPlanEditActivity (методы populateFields / fillRecordFromUi).
 * Работает с protected-полями карточки (тот же пакет), UI-логику не дублирует.
 */
final class CalPlanCardMapper {

    private CalPlanCardMapper() {
    }

    /** Заполняет поля экрана значениями record (бывший populateFields). */
    static void populate(BaseCalPlanEditActivity a) {
        a.okdateValue = (a.record != null && a.record.Okdate != null) ? a.record.Okdate : a.activeDate;
        a.textViewOkdate.setText(BaseCalPlanEditActivity.DISPLAY_DATE_TIME.format(a.okdateValue));

        if (a.showStartDate()) {
            Date start = a.record != null && a.record.StartDate != null ? a.record.StartDate : a.okdateValue;
            a.dateFieldStartDate.setDate(start);
        }

        if (a.record == null) {
            populateNewRecord(a);
            return;
        }
        populateExistingRecord(a);
    }

    // Ветвь новой записи: автор из CALPARAM, предвыбор постановщика/заявки/проекта
    private static void populateNewRecord(BaseCalPlanEditActivity a) {
        CalParamRecord param = a.owerDb.getCalParam();
        a.record = new calPlanRecord();
        a.record.Form = a.preselectFormValue != null ? a.preselectFormValue : a.getFormType();
        if (param != null) {
            a.record.AuthorName = param.Vedushii;
            a.record.AuthorID = param.VedushiiID;
        }
        // Task 122: в проектах/задачах/заявках Постановщик по умолчанию = Ведущий
        if (projectLikeForm(a.record.Form)) {
            a.record.AnalitikName = param != null ? param.Vedushii : null;
            a.record.AnalitikID = param != null ? param.VedushiiID : null;
            a.setupContactSpinner(a.spinnerAnalitik, a.record.AnalitikName);
        }
        a.textViewAuthorName.setText(a.record.AuthorName != null ? a.record.AuthorName : "");
        // Task 124: предвыбранные проект (для задачи) / заявка (для проекта)
        if (a.preselectRequestName != null) {
            a.editTextRequestName.setText(a.preselectRequestName);
            if (a.isRequestPicker() && a.preselectProjectUNID != null) {
                a.selectedRequestUNID = a.preselectProjectUNID;
            }
        }
        // Task 127: строка инфо-поля под текст есть сразу, даже если записи в SQL ещё нет
        a.infoBodyText.setText("");
    }

    // Ветвь существующей записи: все поля карточки + подтягивание имён по ID (Task 102)
    private static void populateExistingRecord(BaseCalPlanEditActivity a) {
        calPlanRecord record = a.record;
        String authorName = contactNameById(a, record.AuthorID);
        if (authorName != null) record.AuthorName = authorName;
        a.textViewAuthorName.setText(record.AuthorName != null ? record.AuthorName : "");
        if (a.isRequestPicker() && record.RequestUNID != null) a.selectedRequestUNID = record.RequestUNID;

        if (record.Name != null) a.editTextName.setText(record.Name);
        if (record.Priority != null) a.editTextPriority.setText(String.valueOf(record.Priority));
        if (record.RequestName != null) a.editTextRequestName.setText(record.RequestName);
        // Task 127: setText вызывается всегда - пустые блоки убираются, строка под текст добавляется
        a.infoBodyText.setText(record.BodyText);
        if (record.Comment != null) a.editTextComment.setText(record.Comment);
        if (record.InstallOrder != null) a.editTextInstallOrder.setText(record.InstallOrder);
        if (record.KeyWords != null) a.editTextKeyWords.setText(record.KeyWords);

        String updaterName = contactNameById(a, record.LastUpdatedByID);
        if (updaterName != null) record.LastUpdatedBy = updaterName;
        a.textViewLastUpdatedBy.setText(record.LastUpdatedBy != null ? record.LastUpdatedBy : "");
        SimpleDateFormat dt = BaseCalPlanEditActivity.DISPLAY_DATE_TIME;
        a.textViewLastUpdatedDate.setText(record.LastUpdatedDate != null ? dt.format(record.LastUpdatedDate) : "");
        a.textViewEndDate.setText(record.EndDate != null ? dt.format(record.EndDate) : "");
        a.textViewHoldDate.setText(record.HoldDate != null ? dt.format(record.HoldDate) : "");

        // Task 41/45: числовые поля Health на карточке ("Голова".."Каллории")
        if (a.showHealthNumbers() && a.healthNumbers != null) a.healthNumbers.loadFrom(record);
    }

    /** Заполняет record значениями с экрана; false - валидация не прошла (тост показан). */
    static boolean fill(BaseCalPlanEditActivity a) {
        String form = a.selectedFormValue();
        String name = a.editTextName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(a, "Введите название", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (a.record == null) a.record = new calPlanRecord();
        if (a.record.Okdate == null) a.record.Okdate = a.okdateValue;
        a.record.Form = form;
        a.record.Name = name;
        if (!fillPriority(a)) return false;
        a.record.RequestName = a.editTextRequestName.getText().toString().trim();
        if (a.isRequestPicker()) a.record.RequestUNID = a.selectedRequestUNID;
        // Task 115: Дата старта хранится с 0ч 0м 0с; когда строка скрыта - дата создания
        a.record.StartDate = dateAtMidnight(a.showStartDate()
                ? a.dateFieldStartDate.getDate()
                : (a.record.Okdate != null ? a.record.Okdate : a.okdateValue));
        fillStatusAndDates(a, form);
        // Task 48/49: последний изменивший и дата/время обновления на каждом сохранении
        a.record.LastUpdatedDate = new Date();
        a.record.LastUpdatedBy = ManageSQLDatabase.AuthorName;
        a.record.LastUpdatedByID = ManageSQLDatabase.AuthorID;
        if (a.showMainSystem()) a.record.MainSystem = a.spinnerMainSystem.getSelectedItem().toString();
        fillContacts(a);
        a.record.BodyText = a.infoBodyText.getText();
        a.record.Comment = a.editTextComment.getText().toString().trim();
        if (a.showInstallOrder()) a.record.InstallOrder = a.editTextInstallOrder.getText().toString().trim();
        if (a.showKeyWords()) a.record.KeyWords = a.editTextKeyWords.getText().toString().trim();
        if (a.showHealthNumbers() && a.healthNumbers != null) a.healthNumbers.applyTo(a.record);
        return true;
    }

    // Приоритет: пусто -> null, не число -> тост и отмена сохранения
    private static boolean fillPriority(BaseCalPlanEditActivity a) {
        String prio = a.editTextPriority.getText().toString().trim();
        if (prio.isEmpty()) {
            a.record.Priority = null;
            return true;
        }
        try {
            a.record.Priority = Integer.parseInt(prio);
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(a, "Приоритет должен быть числом", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Task 48: статус из спиннера; для Project/Task/Request даты завершения/откладывания по состоянию
    private static void fillStatusAndDates(BaseCalPlanEditActivity a, String form) {
        if (a.showStatus()) {
            int si = a.spinnerStatus.getSelectedItemPosition();
            a.record.Status = BaseCalPlanEditActivity.STATUS_LABELS[si];
            a.record.StatusID = BaseCalPlanEditActivity.STATUS_IDS[si];
        }
        if (!projectLikeForm(form)) return;
        if ("Выполнено".equals(a.record.Status) || "Отменено".equals(a.record.Status)) {
            a.record.EndDate = new Date();      // дата завершения проекта (факт) - сегодня со временем
            a.record.HoldDate = null;
        } else if ("Отложено".equals(a.record.Status)) {
            a.record.HoldDate = new Date();     // дата откладывания проекта - сегодня со временем
            a.record.EndDate = null;
        } else {
            a.record.EndDate = null;
            a.record.HoldDate = null;
        }
    }

    // Постановщик/Исполнитель из выпадающих списков контактов
    private static void fillContacts(BaseCalPlanEditActivity a) {
        if (!a.showAnalitikExector()) return;
        int ai = a.spinnerAnalitik.getSelectedItemPosition();
        a.record.AnalitikName = ai > 0 ? a.contactLabels.get(ai) : null;
        a.record.AnalitikID = ai > 0 ? a.contactIds.get(ai) : null;
        int ei = a.spinnerExector.getSelectedItemPosition();
        a.record.ExectorName = ei > 0 ? a.contactLabels.get(ei) : null;
        a.record.ExectorID = ei > 0 ? a.contactIds.get(ei) : null;
    }

    // Task 102: текущее имя контакта (Фамилия Имя) из справочника по числовому ID
    private static String contactNameById(BaseCalPlanEditActivity a, String id) {
        if (id == null || id.trim().isEmpty()) return null;
        try {
            ContactRecord c = a.owerDb.getContactById(Integer.valueOf(id.trim()));
            if (c == null) return null;
            StringBuilder sb = new StringBuilder();
            if (c.Surname != null) sb.append(c.Surname).append(" ");
            if (c.FirstName != null) sb.append(c.FirstName);
            return sb.toString().trim().isEmpty() ? null : sb.toString().trim();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Task 115: обнуляет время даты (0ч 0м 0с 0мс) - Дата старта хранится без времени. */
    private static Date dateAtMidnight(Date d) {
        if (d == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /** Task 122: Form проекта/задачи/заявки - для них показывается Постановщик. */
    private static boolean projectLikeForm(String form) {
        return "Project".equals(form) || "Task".equals(form) || "Request".equals(form);
    }
}
