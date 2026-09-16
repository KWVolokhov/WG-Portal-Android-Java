package com.example.calendar4;

import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Date;

/**
 * Task 139: обработка 5 быстрых кнопок MainActivity (задачи 27/28/29/37/42) -
 * вынесена из MainActivity. Кнопка создаёт запись HEALTHPLAN из записи LIVETYPE
 * (название/Form/поля органов), HealthSport с StepCounter=1 запускает шагомер.
 * Иконки кнопок берутся из поля Icon справочника (Task 37).
 */
final class MainQuickButtons {

    private static final int[] QUICK_BUTTON_VIEW_IDS = {
            R.id.buttonAction1, R.id.buttonAction2, R.id.buttonAction3,
            R.id.buttonAction4, R.id.buttonAction5
    };

    private MainQuickButtons() {
    }

    /** Обработчик нажатия быстрой кнопки (android:onClick на кнопках 1..5). */
    static void handle(MainActivity act, View view) {
        int buttonIndex = buttonIndexForView(view);
        if (buttonIndex < 1) return;
        handleQuickButton(act, buttonIndex, defaultButtonIdForIndex(buttonIndex));
    }

    /** Номер быстрой кнопки (1..5) по её id - позволяет масштабировать количество кнопок копированием. */
    private static int buttonIndexForView(View view) {
        if (view == null) return -1;
        int id = view.getId();
        if (id == R.id.buttonAction1) return 1;
        if (id == R.id.buttonAction2) return 2;
        if (id == R.id.buttonAction3) return 3;
        if (id == R.id.buttonAction4) return 4;
        if (id == R.id.buttonAction5) return 5;
        return -1;
    }

    /** Id LIVETYPE по умолчанию для номера кнопки (соответствует предустановкам справочника). */
    private static int defaultButtonIdForIndex(int index) {
        switch (index) {
            case 1: return CalParamRecord.DEFAULT_BUTTON1_ID;
            case 2: return CalParamRecord.DEFAULT_BUTTON2_ID;
            case 3: return CalParamRecord.DEFAULT_BUTTON3_ID;
            case 4: return CalParamRecord.DEFAULT_BUTTON4_ID;
            case 5: return CalParamRecord.DEFAULT_BUTTON5_ID;
            default: return index;
        }
    }

    // Создаёт запись HEALTHPLAN из LIVETYPE (автор = Ведущий, дата старта из календаря)
    private static void handleQuickButton(MainActivity act, int buttonIndex, int defaultId) {
        try {
            if (act.owerDb == null) act.owerDb = ManageSQLDatabase.getInstance(act);
            Integer configuredId = getConfiguredButtonId(act, buttonIndex, defaultId);
            LivetypeSQLManage livetypeDb = new LivetypeSQLManage(act.owerDb.getWritableDatabase());
            livetypeRecord typeRecord = livetypeDb.getLivetypeById(configuredId);
            if (typeRecord == null) {
                Toast.makeText(act, "Не найден тип жизнедеятельности (id=" + configuredId + ")", Toast.LENGTH_LONG).show();
                return;
            }
            healthPlanRecord record = buildRecord(act, typeRecord);
            HealthSQLManage healthDb = new HealthSQLManage(act.owerDb.getWritableDatabase());
            healthDb.upsertHealth(record);
            Toast.makeText(act, "Создано: " + (record.Name != null ? record.Name : ""), Toast.LENGTH_SHORT).show();
            act.refreshListView();
            // Задача 29/42: шагомер включается только для HealthSport с StepCounter=1
            if ("HealthSport".equals(record.Form) && isPedometerAllowed(typeRecord)) {
                act.startPedometer(record.id);
            }
        } catch (Exception err) {
            Toast.makeText(act, "Error: " + err.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Название/Form/поля органов - из LIVETYPE; автор и даты - по правилам карточки
    private static healthPlanRecord buildRecord(MainActivity act, livetypeRecord typeRecord) {
        healthPlanRecord record = new healthPlanRecord(typeRecord);
        record.AuthorID = ManageSQLDatabase.AuthorID;         // Ведущий
        record.AuthorName = ManageSQLDatabase.AuthorName;     // Ведущий
        record.Okdate = new Date();                            // Дата создания = сегодня
        record.StartDate = (act.russianCalendar != null && act.russianCalendar.activeDate != null)
                ? act.russianCalendar.activeDate : new Date(); // Дата старта из календаря
        return record;
    }

    /** Task 42: шагомер разрешён только если в справочнике LIVETYPE включён StepCounter. */
    private static boolean isPedometerAllowed(livetypeRecord typeRecord) {
        return typeRecord != null && typeRecord.StepCounter != null && typeRecord.StepCounter == 1;
    }

    /** Возвращает настроенный id LIVETYPE для кнопки (из CALPARAM), иначе значение по умолчанию. */
    private static Integer getConfiguredButtonId(MainActivity act, int buttonIndex, int defaultId) {
        try {
            CalParamRecord param = act.owerDb.getCalParam();
            if (param == null) return defaultId;
            switch (buttonIndex) {
                case 1: return param.Button1Id != null ? param.Button1Id : defaultId;
                case 2: return param.Button2Id != null ? param.Button2Id : defaultId;
                case 3: return param.Button3Id != null ? param.Button3Id : defaultId;
                case 4: return param.Button4Id != null ? param.Button4Id : defaultId;
                case 5: return param.Button5Id != null ? param.Button5Id : defaultId;
                default: return defaultId;
            }
        } catch (Exception e) {
            return defaultId;
        }
    }

    /**
     * Refreshes the icons of the 5 quick buttons (Task 37): имя drawable из поля
     * Icon записи LIVETYPE; если не найдено - иконка по Form или по номеру кнопки.
     */
    static void refreshIcons(MainActivity act) {
        try {
            if (act.owerDb == null) act.owerDb = ManageSQLDatabase.getInstance(act);
            LivetypeSQLManage livetypeDb = new LivetypeSQLManage(act.owerDb.getReadableDatabase());
            for (int i = 0; i < QUICK_BUTTON_VIEW_IDS.length; i++) {
                ImageButton btn = act.findViewById(QUICK_BUTTON_VIEW_IDS[i]);
                if (btn == null) continue;
                int buttonIndex = i + 1;
                Integer configuredId = getConfiguredButtonId(act, buttonIndex, defaultQuickButtonId(buttonIndex));
                livetypeRecord typeRecord = null;
                if (configuredId != null) {
                    try {
                        typeRecord = livetypeDb.getLivetypeById(configuredId);
                    } catch (Exception e) {
                        typeRecord = null;
                    }
                }
                int fallbackRes = defaultIconForQuickButton(typeRecord, buttonIndex);
                int iconRes = findIconDrawable(act, typeRecord != null ? typeRecord.Icon : null, fallbackRes);
                btn.setImageResource(iconRes);
            }
        } catch (Exception e) {
            // Non-fatal: icons are a visual improvement, the buttons still work
        }
    }

    /** Находит drawable по имени (поле Icon); если не найдено - запасная иконка. */
    private static int findIconDrawable(MainActivity act, String iconName, int fallbackRes) {
        if (iconName != null && !iconName.trim().isEmpty()) {
            int res = act.getResources().getIdentifier(iconName.trim(), "drawable", act.getPackageName());
            if (res != 0) return res;
        }
        return fallbackRes;
    }

    /** Значение id LIVETYPE по умолчанию для кнопки с индексом 1..5. */
    private static int defaultQuickButtonId(int buttonIndex) {
        switch (buttonIndex) {
            case 1: return CalParamRecord.DEFAULT_BUTTON1_ID;
            case 2: return CalParamRecord.DEFAULT_BUTTON2_ID;
            case 3: return CalParamRecord.DEFAULT_BUTTON3_ID;
            case 4: return CalParamRecord.DEFAULT_BUTTON4_ID;
            case 5: return CalParamRecord.DEFAULT_BUTTON5_ID;
            default: return 1;
        }
    }

    /** Иконка по Form жизнедеятельности (HealthSport->ic_pedometer и т.д.), иначе по индексу. */
    private static int defaultIconForQuickButton(livetypeRecord typeRecord, int buttonIndex) {
        if (typeRecord != null && typeRecord.Form != null) {
            switch (typeRecord.Form) {
                case "HealthSport": return R.drawable.ic_pedometer;
                case "HealthEat":   return R.drawable.ic_burger;
                case "HealthDrink": return R.drawable.ic_coffee;
                case "HealthStress": return R.drawable.ic_stress;
                case "HealthJoy":   return R.drawable.ic_joy;
                default: break;
            }
        }
        switch (buttonIndex) {
            case 1: return R.drawable.ic_pedometer;
            case 2: return R.drawable.ic_burger;
            case 3: return R.drawable.ic_coffee;
            case 4: return R.drawable.ic_stress;
            case 5: return R.drawable.ic_joy;
            default: return R.drawable.ic_pedometer;
        }
    }
}
