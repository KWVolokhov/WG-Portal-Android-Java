package com.example.calendar4.health;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.calendar4.BaseScreenActivity;
import com.example.calendar4.localsql.ManageSQLDatabase;
import com.example.calendar4.localsql.LivetypeSQLManage;
import com.example.calendar4.records.ContactRecord;
import com.example.calendar4.records.livetypeRecord;
import com.example.calendar4.R;

public class LivetypeEditActivity extends BaseScreenActivity {

    private static final String[] CATEGORIES = {"Пища", "Гидратация", "Физ. активность", "Стресс", "Гедонизм"};

    private static final String[] ORGAN_NAMES = {"Голова", "Глаза", "Уши", "Нос", "Горло", "Зубы",
            "Желудок", "Кишечник", "Печень", "Почки", "Сердце", "Лёгкие",
            "Давление", "Сон", "Вес", "Нервная система", "Мораль", "Состояние кожи",
            "Шаги", "Вес еды", "Объем питья", "Каллории"};

    private static final String[] ORGAN_COLUMNS = {"Head", "Eyes", "Ears", "Nose", "Throat", "Teeth",
            "Stomach", "Intestines", "Liver", "Kidneys", "Heart", "Lungs",
            "Pressure", "Sleep", "Weight", "Nervous", "Morality", "Skin",
            "Steps", "FoodWeight", "DrinkValue", "Kallory"};
    private static final int FOOD_WEIGHT_INDEX = 19;
    private static final int DRINK_VALUE_INDEX = 20;
    private EditText editTextName;
    private Spinner spinnerCategory;
    private Button buttonLivetypeIcon;
    private CheckBox checkboxStepCounter;
    private TextView textViewAuthor;
    private TextView textViewDateCreated;
    private LinearLayout organContainer;
    private ImageButton btnOK;
    private ImageButton btnCancel;

    /** Кандидаты имён иконок (drawable); реально существующие ресурсы определяются динамически. */
    private static final String[] ICON_CANDIDATES = {
            "ic_pedometer", "ic_burger", "ic_coffee", "ic_stress", "ic_joy",
            "ic_americano", "ic_latte", "ic_container400",
            "ic_person_contact", "ic_person_contact_child", "ic_person_contact_senior",
            "ic_type_project", "ic_type_task", "ic_type_request", "ic_type_note",
            "ic_ok_green", "ic_cancel_gray"
    };
    /** Имена иконок, которые реально существуют в ресурсах drawable (заполняется в onCreate). */
    private String[] availableIcons;
    /** Текущее выбранное имя иконки (может быть пустым). */
    private String selectedIconName;

    private EditText[] organEdits = new EditText[ORGAN_NAMES.length];
    private LivetypeSQLManage livetypeDb;
    private livetypeRecord currentRecord; // null = brand-new record

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livetype_edit);
        ;

        editTextName = findViewById(R.id.editTextLivetypeName);
        spinnerCategory = findViewById(R.id.spinnerLivetypeCategory);
        buttonLivetypeIcon = findViewById(R.id.buttonLivetypeIcon);
        checkboxStepCounter = findViewById(R.id.checkboxLivetypePedometer);

        // Список реально существующих иконок (drawable)
        availableIcons = getAvailableIcons();
        buttonLivetypeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIconDialog();
            }
        });
        textViewAuthor = findViewById(R.id.textViewLivetypeAuthor);
        textViewDateCreated = findViewById(R.id.textViewLivetypeDateCreated);
        organContainer = findViewById(R.id.organContainer);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);

        livetypeDb = new LivetypeSQLManage(ManageSQLDatabase.getInstance(this).getWritableDatabase());

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // Task 43: галочка "Шагомер" редактируется только для категории "Физ. активность",
        // для остальных категорий она выключена и неактивная (серая).
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateStepCounterCheckboxState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        buildOrganFields();

        int livetypeId = getIntent().getIntExtra("livetypeId", -1);
        if (livetypeId != -1) {
            loadRecord(livetypeId);
        } else {
            // Brand-new record: preview author ("Ведущий") and creation date.
            // DateCreated is actually stored when the record is saved.
            if (ManageSQLDatabase.AuthorName != null) {
                textViewAuthor.setText(ManageSQLDatabase.AuthorName);
            }
            textViewDateCreated.setText(
                    new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
        }

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndFinish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

    private void saveAndFinish() {
        String name = editTextName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Название не может быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentRecord == null) {
            currentRecord = new livetypeRecord();
            currentRecord.DateCreated = new Date();
        }
        currentRecord.Name = name;

        // Одно поле выбора - Категория; Form выводится автоматически (Form - ID-поле для Category).
        int catIdx = spinnerCategory.getSelectedItemPosition();
        currentRecord.Category = (catIdx >= 0 && catIdx < livetypeRecord.CATEGORIES.length) ? livetypeRecord.CATEGORIES[catIdx] : livetypeRecord.CATEGORIES[0];
        currentRecord.Form = livetypeRecord.formForCategory(currentRecord.Category);

        // Task 42/43: разрешение шагомера для этого типа (только для категории "Физ. активность")
        currentRecord.StepCounter = (checkboxStepCounter != null && checkboxStepCounter.isEnabled()
                && checkboxStepCounter.isChecked()) ? 1 : 0;

        // Иконка (имя drawable для настраиваемых кнопок) - необязательное поле
        currentRecord.Icon = (selectedIconName != null && !selectedIconName.isEmpty()) ? selectedIconName : null;

        // Read the reference (organ/state) numbers from the generated fields
        currentRecord.Head        = organValue(0);
        currentRecord.Eyes        = organValue(1);
        currentRecord.Ears        = organValue(2);
        currentRecord.Nose        = organValue(3);
        currentRecord.Throat      = organValue(4);
        currentRecord.Teeth       = organValue(5);
        currentRecord.Stomach     = organValue(6);
        currentRecord.Intestines  = organValue(7);
        currentRecord.Liver       = organValue(8);
        currentRecord.Kidneys     = organValue(9);
        currentRecord.Heart       = organValue(10);
        currentRecord.Lungs       = organValue(11);
        currentRecord.Pressure    = organValue(12);
        currentRecord.Sleep       = organValue(13);
        currentRecord.Weight      = organValue(14);
        currentRecord.Nervous     = organValue(15);
        currentRecord.Morality    = organValue(16);
        currentRecord.Skin        = organValue(17);
        // Task 41: новые числовые поля справочника
        currentRecord.Steps       = organValue(18);
        currentRecord.FoodWeight  = organValueDbl(FOOD_WEIGHT_INDEX);
        currentRecord.DrinkValue  = organValueDbl(DRINK_VALUE_INDEX);
        currentRecord.Kallory     = organValue(21);

        livetypeDb.upsertLivetype(currentRecord);
        setResult(Activity.RESULT_OK);
        finish();
    }

    /** Returns the Integer typed in the organ field with the given index (null when empty). */
    private Integer organValue(int index) {
        if (organEdits == null || index < 0 || index >= organEdits.length) return null;
        EditText edit = organEdits[index];
        if (edit == null) return null;
        String text = edit.getText().toString().trim();
        if (text.isEmpty()) return null;
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private Double organValueDbl(int index) {
        if (organEdits == null || index < 0 || index >= organEdits.length) return null;
        EditText edit = organEdits[index];
        if (edit == null) return null;
        String text = edit.getText().toString().trim();
        if (text.isEmpty()) return null;
        try {
            return Double.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Task 43: галочка "Шагомер" активна (и серая - нет) только для категории "Физ. активность". */
    private void updateStepCounterCheckboxState() {
        if (checkboxStepCounter == null) return;
        int idx = spinnerCategory.getSelectedItemPosition();
        boolean isSport = idx >= 0 && idx < CATEGORIES.length
                && "Физ. активность".equals(CATEGORIES[idx]);
        if (!isSport) {
            checkboxStepCounter.setChecked(false);
        }
        checkboxStepCounter.setEnabled(isSport);
        checkboxStepCounter.setAlpha(isSport ? 1.0f : 0.3f);
    }

    private void buildOrganFields() {
        for (int i = 0; i < ORGAN_NAMES.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView label = new TextView(this);
            label.setText(ORGAN_NAMES[i]);
            label.setTextSize(16);
            label.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(label);

            EditText edit = new EditText(this);
            // Task 128: числовые поля допускают и отрицательные значения,а вес и объем только положительные, но дробные
            if (i == FOOD_WEIGHT_INDEX || i == DRINK_VALUE_INDEX) {
                edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                edit.setFilters(new android.text.InputFilter[]{this::filterMax2Decimals});
            } else {
                edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            }
            edit.setSingleLine(true);
            LinearLayout.LayoutParams weightLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(edit, weightLp);
            organEdits[i] = edit;

            organContainer.addView(row);
        }
    }
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
    private void loadRecord(int id) {
        currentRecord = livetypeDb.getLivetypeById(id);
        if (currentRecord == null) return;

        if (currentRecord.Name != null) editTextName.setText(currentRecord.Name);
        if (currentRecord.Category != null) {
            int idx = indexOf(CATEGORIES, currentRecord.Category);
            spinnerCategory.setSelection(idx >= 0 ? idx : 0);
        }
        /* Task 43: состояние галочки "Шагомер" зависит от выбранной категории
           (даже если запись загружена, но категория не "Физ. активность" - выключаем). */
        updateStepCounterCheckboxState();
        if (currentRecord.Icon != null) {
            selectedIconName = currentRecord.Icon;
            buttonLivetypeIcon.setText(currentRecord.Icon);
        } else {
            selectedIconName = "";
            buttonLivetypeIcon.setText("Выбрать иконку...");
        }
        // Task 102: имя автора подтягиваем из справочника контактов по ID
        String author = contactNameById(currentRecord.AuthorID);
        if (author != null) currentRecord.AuthorName = author;
        if (currentRecord.AuthorName != null) textViewAuthor.setText(currentRecord.AuthorName);
        if (currentRecord.DateCreated != null) {
            textViewDateCreated.setText(
                    new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(currentRecord.DateCreated));
        }

        Integer[] vals = {currentRecord.Head, currentRecord.Eyes, currentRecord.Ears,
                currentRecord.Nose, currentRecord.Throat, currentRecord.Teeth,
                currentRecord.Stomach, currentRecord.Intestines, currentRecord.Liver,
                currentRecord.Kidneys, currentRecord.Heart, currentRecord.Lungs,
                currentRecord.Pressure, currentRecord.Sleep, currentRecord.Weight,
                currentRecord.Nervous, currentRecord.Morality, currentRecord.Skin,
                currentRecord.Steps, 0,0,
                currentRecord.Kallory};
        for (int i = 0; i < organEdits.length; i++) {
            if (vals[i] != null) organEdits[i].setText(String.valueOf(vals[i]));
        }
        if(currentRecord.FoodWeight!=null) organEdits[FOOD_WEIGHT_INDEX].setText(String.valueOf(currentRecord.FoodWeight));
        if(currentRecord.DrinkValue!=null) organEdits[DRINK_VALUE_INDEX].setText(String.valueOf(currentRecord.DrinkValue));

        // Task 42: разрешён ли шагомер
        if (checkboxStepCounter != null && currentRecord.StepCounter != null && currentRecord.StepCounter == 1) {
            checkboxStepCounter.setChecked(true);
        }
    }

    /** Task 102: текущее имя контакта (Фамилия Имя) из справочника по числовому ID. */
    private String contactNameById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        try {
            ContactRecord c = ManageSQLDatabase.getInstance(this).getContactById(Integer.valueOf(id.trim()));
            if (c == null) return null;
            StringBuilder sb = new StringBuilder();
            if (c.Surname != null) sb.append(c.Surname).append(" ");
            if (c.FirstName != null) sb.append(c.FirstName);
            return sb.toString().trim().isEmpty() ? null : sb.toString().trim();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int indexOf(String[] arr, String value) {
        if (value == null) return -1;
        for (int i = 0; i < arr.length; i++) {
            if (value.equals(arr[i])) return i;
        }
        return -1;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /** Проверяет какие из ICON_CANDIDATES реально существуют как drawable-ресурсы. */
    private String[] getAvailableIcons() {
        java.util.ArrayList<String> result = new java.util.ArrayList<>();
        result.add(""); // пустая иконка (по умолчанию)
        for (String cand : ICON_CANDIDATES) {
            int resId = getResources().getIdentifier(cand, "drawable", getPackageName());
            if (resId != 0) {
                result.add(cand);
            }
        }
        return result.toArray(new String[0]);
    }

    /** Показывает диалог со списком доступных иконок для выбора. */
    private void showIconDialog() {
        if (availableIcons == null || availableIcons.length == 0) return;
        // Подписи для отображения: пустая строка -> "(по умолчанию)"
        final String[] labels = new String[availableIcons.length];
        for (int i = 0; i < availableIcons.length; i++) {
            labels[i] = availableIcons[i].isEmpty() ? "(по умолчанию)" : availableIcons[i];
        }
        int checkedItem = 0;
        if (selectedIconName != null) {
            for (int i = 0; i < availableIcons.length; i++) {
                if (availableIcons[i].equals(selectedIconName)) {
                    checkedItem = i;
                    break;
                }
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Выберите иконку")
                .setSingleChoiceItems(labels, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedIconName = availableIcons[which];
                        buttonLivetypeIcon.setText(labels[which]);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

}