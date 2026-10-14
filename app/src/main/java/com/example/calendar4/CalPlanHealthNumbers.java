package com.example.calendar4;

import android.app.Activity;
import android.text.InputType;
import android.text.Spanned;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

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

    // Task 149: индексы полей с дробным вводом (не более 2 знаков после запятой),
    // пустой ввод трактуется как 0: 19=Вес еды (FoodWeight), 20=Объем питья (DrinkValue).
    private static final int FOOD_WEIGHT_INDEX = 19;
    private static final int DRINK_VALUE_INDEX = 20;

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
        boolean decimal = (index == FOOD_WEIGHT_INDEX || index == DRINK_VALUE_INDEX);
        if (decimal) {
            edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else {
            edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        edit.setSingleLine(true);
        if (decimal) edit.setFilters(new android.text.InputFilter[]{this::filterMax2Decimals});
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
        for (int i = 0; i < NAMES.length; i++) {
            if (isDecimal(i)) setValueDecimal(i, recordDoubleValue(r, i));
            else setValue(i, recordValue(r, i));
        }
    }

    /** Task 45: записывает значения полей экрана в поля "Голова".."Каллории" записи. */
    public void applyTo(calPlanRecord r) {
        if (r == null) return;
        for (int i = 0; i < NAMES.length; i++) {
            if (isDecimal(i)) setRecordValueDouble(r, i, valueDecimal(i));
            else setRecordValue(r, i, value(i));
        }
    }

    private boolean isDecimal(int index) {
        return index == FOOD_WEIGHT_INDEX || index == DRINK_VALUE_INDEX;
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
            // Task 149: 19/20 (FoodWeight/DrinkValue) читаются через recordDoubleValue
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
            // Task 149: 19/20 (FoodWeight/DrinkValue) пишутся через setRecordValueDouble
            case 21: r.Kallory = value; break;
            default: break;
        }
    }

    // ----- Task 149: дробные поля (Вес еды / Объем питья), до 2 знаков, пусто = 0 -----

    // Читает дробное значение поля из записи (FoodWeight / DrinkValue).
    private Double recordDoubleValue(calPlanRecord r, int index) {
        switch (index) {
            case FOOD_WEIGHT_INDEX: return r.FoodWeight;
            case DRINK_VALUE_INDEX: return r.DrinkValue;
            default: return null;
        }
    }

    // Записывает дробное значение поля в запись (FoodWeight / DrinkValue).
    private void setRecordValueDouble(calPlanRecord r, int index, Double value) {
        switch (index) {
            case FOOD_WEIGHT_INDEX: r.FoodWeight = value; break;
            case DRINK_VALUE_INDEX: r.DrinkValue = value; break;
            default: break;
        }
    }

    /** Значение дробного поля (пустой ввод трактуется как 0 - Task 149). */
    private Double valueDecimal(int index) {
        if (edits == null || index < 0 || index >= edits.length || edits[index] == null) return null;
        return parseDecimal(edits[index].getText().toString().trim());
    }

    /** Записать дробное значение в поле (null - очистить). */
    private void setValueDecimal(int index, Double value) {
        if (edits == null || index < 0 || index >= edits.length || edits[index] == null) return;
        edits[index].setText(value != null ? formatDecimal(value) : "");
    }

    // Task 149: формат дробного значения не более чем с 2 знаками после запятой.
    private String formatDecimal(double value) {
        double rounded = Math.round(value * 100.0) / 100.0;
        if (rounded == Math.floor(rounded)) return String.valueOf((long) rounded);
        return String.format(Locale.getDefault(), "%.2f", rounded);
    }

    // Парсит дробный ввод: допускает и запятую, и точку; округляет до 2 знаков.
    // Пустая строка / мусор трактуются как 0 (требование Task 149).
    private Double parseDecimal(String text) {
        String normalized = text.replace(',', '.').trim();
        if (normalized.isEmpty() || ".".equals(normalized)) return 0.0;
        try {
            double d = Double.parseDouble(normalized);
            if (Double.isNaN(d) || Double.isInfinite(d)) return 0.0;
            return Math.round(d * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Task 149: не даёт ввести больше 2 знаков после запятой (и вторую точку/запятую).
    private CharSequence filterMax2Decimals(CharSequence source, int start, int end,
                                            Spanned dest, int dstart, int dend) {
        if (source == null || dest == null) return null;
        StringBuilder out = new StringBuilder();
        out.append(dest, 0, dstart).append(source, start, end).append(dest, dend, dest.length());
        String next = out.toString().replace(',', '.');
        if (next.isEmpty()) return null;
        int sep = next.indexOf('.');
        if (sep != next.lastIndexOf('.')) return "";      // вторая точка - запрет
        if (sep >= 0 && next.length() - sep - 1 > 2) return "";   // >2 знаков - запрет
        return null;
    }
}
