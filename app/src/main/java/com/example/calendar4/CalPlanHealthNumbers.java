package com.example.calendar4;

import android.app.Activity;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Task 139: числовые поля Health ("Голова".."Каллории") на карточке - вынесены из
 * BaseCalPlanEditActivity (Task 41/45/128). Строит строки label+EditText внутри
 * переданного контейнера и переносит значения между экраном и calPlanRecord.
 */
public class CalPlanHealthNumbers {

    // Task 45: полный массив полей "Голова".."Каллории", как на activity_livetype_edit
    private static final String[] NAMES = {
            "Голова", "Глаза", "Уши", "Нос", "Горло", "Зубы",
            "Желудок", "Кишечник", "Печень", "Почки", "Сердце", "Лёгкие",
            "Давление", "Сон", "Вес", "Нервы", "Мораль", "Кожа",
            "Шаги", "Вес еды", "Объем питья", "Каллории"};

    private final Activity host;
    private final LinearLayout container;
    private EditText[] edits;

    public CalPlanHealthNumbers(Activity activity, LinearLayout healthContainer) {
        host = activity;
        container = healthContainer;
        buildRows();
    }

    // Строит строки "подпись + числовое поле" (Task 128: допускаются отрицательные значения)
    private void buildRows() {
        if (container == null || container.getChildCount() > 0) return;
        edits = new EditText[NAMES.length];
        for (int i = 0; i < NAMES.length; i++) container.addView(buildRow(i));
    }

    // Одна строка: подпись поля + EditText с числовым вводом
    private LinearLayout buildRow(int index) {
        LinearLayout row = new LinearLayout(host);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        TextView label = new TextView(host);
        label.setText(NAMES[index]);
        label.setTextSize(16);
        label.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(label);
        EditText edit = new EditText(host);
        edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        edit.setSingleLine(true);
        LinearLayout.LayoutParams weightLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(edit, weightLp);
        edits[index] = edit;
        return row;
    }

    /** Значение из поля по индексу (null - пусто). */
    public Integer value(int index) {
        if (edits == null || index < 0 || index >= edits.length || edits[index] == null) return null;
        String text = edits[index].getText().toString().trim();
        if (text.isEmpty()) return null;
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Записать значение в поле по индексу (null - очистить). */
    public void setValue(int index, Integer value) {
        if (edits == null || index < 0 || index >= edits.length || edits[index] == null) return;
        edits[index].setText(value != null ? String.valueOf(value) : "");
    }

    /** Task 45: заполняет поля значениями полей "Голова".."Каллории" из записи. */
    public void loadFrom(calPlanRecord r) {
        if (r == null) return;
        for (int i = 0; i < NAMES.length; i++) setValue(i, recordValue(r, i));
    }

    /** Task 45: записывает значения полей экрана в поля "Голова".."Каллории" записи. */
    public void applyTo(calPlanRecord r) {
        if (r == null) return;
        for (int i = 0; i < NAMES.length; i++) setRecordValue(r, i, value(i));
    }

    // Читает значение поля "Голова".."Каллории" из calPlanRecord по индексу
    private Integer recordValue(calPlanRecord r, int index) {
        switch (index) {
            case 0: return r.Head;
            case 1: return r.Eyes;
            case 2: return r.Ears;
            case 3: return r.Nose;
            case 4: return r.Throat;
            case 5: return r.Teeth;
            case 6: return r.Stomach;
            case 7: return r.Intestines;
            case 8: return r.Liver;
            case 9: return r.Kidneys;
            case 10: return r.Heart;
            case 11: return r.Lungs;
            case 12: return r.Pressure;
            case 13: return r.Sleep;
            case 14: return r.Weight;
            case 15: return r.Nervous;
            case 16: return r.Morality;
            case 17: return r.Skin;
            case 18: return r.Steps;
            case 19: return r.FoodWeight;
            case 20: return r.DrinkValue;
            case 21: return r.Kallory;
            default: return null;
        }
    }

    // Записывает значение поля "Голова".."Каллории" в calPlanRecord по индексу
    private void setRecordValue(calPlanRecord r, int index, Integer value) {
        switch (index) {
            case 0: r.Head = value; break;
            case 1: r.Eyes = value; break;
            case 2: r.Ears = value; break;
            case 3: r.Nose = value; break;
            case 4: r.Throat = value; break;
            case 5: r.Teeth = value; break;
            case 6: r.Stomach = value; break;
            case 7: r.Intestines = value; break;
            case 8: r.Liver = value; break;
            case 9: r.Kidneys = value; break;
            case 10: r.Heart = value; break;
            case 11: r.Lungs = value; break;
            case 12: r.Pressure = value; break;
            case 13: r.Sleep = value; break;
            case 14: r.Weight = value; break;
            case 15: r.Nervous = value; break;
            case 16: r.Morality = value; break;
            case 17: r.Skin = value; break;
            case 18: r.Steps = value; break;
            case 19: r.FoodWeight = value; break;
            case 20: r.DrinkValue = value; break;
            case 21: r.Kallory = value; break;
            default: break;
        }
    }
}
