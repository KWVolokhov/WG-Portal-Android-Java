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
        textViewAuthor = findViewById(R.id.textViewLivetypeAuthor);
        textViewDateCreated = findViewById(R.id.textViewLivetypeDateCreated);
        organContainer = findViewById(R.id.organContainer);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);

        livetypeDb = new LivetypeSQLManage(new ManageSQLDatabase(this).getWritableDatabase());

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        buildOrganFields();

        int livetypeId = getIntent().getIntExtra("livetypeId", -1);
        if (livetypeId != -1) {
            loadRecord(livetypeId);
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