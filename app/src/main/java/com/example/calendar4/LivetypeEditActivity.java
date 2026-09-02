package com.example.calendar4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LivetypeEditActivity extends Activity {

    private static final String[] CATEGORIES = {"Пища", "Гидратация", "Физ. активность", "Стресс", "Гедонизм"};

    private static final String[] ORGAN_NAMES = {"Голова", "Глаза", "Уши", "Нос", "Горло", "Зубы",
            "Желудок", "Кишечник", "Печень", "Почки", "Сердце", "Лёгкие",
            "Давление", "Сон", "Вес", "Нервная система", "Мораль", "Состояние кожи"};

    private static final String[] ORGAN_COLUMNS = {"Head", "Eyes", "Ears", "Nose", "Throat", "Teeth",
            "Stomach", "Intestines", "Liver", "Kidneys", "Heart", "Lungs",
            "Pressure", "Sleep", "Weight", "Nervous", "Morality", "Skin"};
    private EditText editTextName;
    private Spinner spinnerCategory;
    private EditText editTextIcon;
    private TextView textViewAuthor;
    private TextView textViewDateCreated;
    private LinearLayout organContainer;
    private ImageButton btnOK;
    private ImageButton btnCancel;

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
        editTextIcon = findViewById(R.id.editTextLivetypeIcon);
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
                    new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date()));
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

        int catIdx = spinnerCategory.getSelectedItemPosition();
        currentRecord.Category = (catIdx >= 0 && catIdx < CATEGORIES.length) ? CATEGORIES[catIdx] : CATEGORIES[0];

        // Иконка (имя drawable для настраиваемых кнопок) - необязательное поле
        String icon = editTextIcon.getText().toString().trim();
        currentRecord.Icon = icon.isEmpty() ? null : icon;

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
            edit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            edit.setSingleLine(true);
            LinearLayout.LayoutParams weightLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(edit, weightLp);
            organEdits[i] = edit;

            organContainer.addView(row);
        }
    }

    private void loadRecord(int id) {
        currentRecord = livetypeDb.getLivetypeById(id);
        if (currentRecord == null) return;

        if (currentRecord.Name != null) editTextName.setText(currentRecord.Name);
        if (currentRecord.Category != null) {
            int idx = indexOf(CATEGORIES, currentRecord.Category);
            spinnerCategory.setSelection(idx >= 0 ? idx : 0);
        }
        if (currentRecord.Icon != null) editTextIcon.setText(currentRecord.Icon);
        if (currentRecord.AuthorName != null) textViewAuthor.setText(currentRecord.AuthorName);
        if (currentRecord.DateCreated != null) {
            textViewDateCreated.setText(
                    new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(currentRecord.DateCreated));
        }

        Integer[] vals = {currentRecord.Head, currentRecord.Eyes, currentRecord.Ears,
                currentRecord.Nose, currentRecord.Throat, currentRecord.Teeth,
                currentRecord.Stomach, currentRecord.Intestines, currentRecord.Liver,
                currentRecord.Kidneys, currentRecord.Heart, currentRecord.Lungs,
                currentRecord.Pressure, currentRecord.Sleep, currentRecord.Weight,
                currentRecord.Nervous, currentRecord.Morality, currentRecord.Skin};
        for (int i = 0; i < organEdits.length; i++) {
            if (vals[i] != null) organEdits[i].setText(String.valueOf(vals[i]));
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

}