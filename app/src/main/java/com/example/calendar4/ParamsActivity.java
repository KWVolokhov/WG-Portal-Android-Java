package com.example.calendar4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ParamsActivity extends BaseScreenActivity {

    // "Стартовая страница" picker: display labels -> stored values (CALPARAM.StartPage)
    private static final String[] START_PAGE_LABELS = {
            "Календарь", "Контакты", "Проекты", "Параметры"
    };
    private static final String[] START_PAGE_VALUES = {
            CalParamRecord.START_PAGE_CALENDAR,
            CalParamRecord.START_PAGE_CONTACTS,
            CalParamRecord.START_PAGE_PROJECTS,
            CalParamRecord.START_PAGE_PARAMS
    };

    private Spinner spinnerVedushii;
    private Spinner spinnerStartPage;
    private Spinner spinnerButton1;
    private Spinner spinnerButton2;
    private Spinner spinnerButton3;
    private Spinner spinnerButton4;
    private Spinner spinnerButton5;
    private EditText editTextAddress;
    private EditText editTextName;
    private EditText editTextPassword;
    private EditText editTextHeight;       // Task 54: Рост, см
    private EditText editTextWeight;       // Task 54: Вес, кг
    private EditText editTextAge;          // Task 54: Возраст, лет
    private EditText editTextAttachFolder; // Task 100: папка вложений
    private EditText editTextDBName;       // Task 100: название базы
    private ImageButton btnOK;
    private ImageButton btnCancel;

    private ManageSQLDatabase owerDb;
    private CalParamRecord currentRecord;

    // Contacts for the "Ведущий" picker (display text and identifier)
    private final ArrayList<String> contactLabels = new ArrayList<>();
    private final ArrayList<String> contactIds = new ArrayList<>();

    // Types of life activity ("Типы жизнедеятельности") for the 5 quick buttons
    // (display text and числовой id записи LIVETYPE - only the id is stored in CALPARAM)
    private final ArrayList<String> livetypeLabels = new ArrayList<>();
    private final ArrayList<Integer> livetypeIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_params);

        // Initialize views
        spinnerVedushii = findViewById(R.id.spinnerVedushii);
        spinnerStartPage = findViewById(R.id.spinnerStartPage);
        spinnerButton1 = findViewById(R.id.spinnerButton1);
        spinnerButton2 = findViewById(R.id.spinnerButton2);
        spinnerButton3 = findViewById(R.id.spinnerButton3);
        spinnerButton4 = findViewById(R.id.spinnerButton4);
        spinnerButton5 = findViewById(R.id.spinnerButton5);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextAge = findViewById(R.id.editTextAge);
        editTextAttachFolder = findViewById(R.id.editTextAttachFolder);
        editTextDBName = findViewById(R.id.editTextDBName);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);

        // Initialize database and load current parameters
        // owerDb = new ManageSQLDatabase(this);
		owerDb = ManageSQLDatabase.getInstance(this);
        currentRecord = owerDb.getCalParam();

        loadContacts();
        loadLivetypes();

        // Populate "Ведущий" with the saved contact (if any)
        if (currentRecord != null) {
            setupVedushiiSpinner(currentRecord.Vedushii);
        } else {
            setupVedushiiSpinner(null);
        }

        // If record exists, populate fields
        if (currentRecord != null) {
            if (currentRecord.Address != null) editTextAddress.setText(currentRecord.Address);
            if (currentRecord.Name != null) editTextName.setText(currentRecord.Name);
            if (currentRecord.Password != null) editTextPassword.setText(currentRecord.Password);

            // Task 54: Рост/Вес/Возраст (getCalParam подставляет умолчания)
            editTextHeight.setText(String.valueOf(currentRecord.Height));
            editTextWeight.setText(String.valueOf(currentRecord.Weight));
            editTextAge.setText(String.valueOf(currentRecord.Age));

            // Task 100: папка вложений и имя базы (getCalParam подставляет умолчания)
            editTextAttachFolder.setText(currentRecord.AttachFolder);
            editTextDBName.setText(currentRecord.DBName);
        } else {
            // Task 54/100: умолчания, когда запись параметров ещё не создана
            editTextHeight.setText(String.valueOf(CalParamRecord.DEFAULT_HEIGHT));
            editTextWeight.setText(String.valueOf(CalParamRecord.DEFAULT_WEIGHT));
            editTextAge.setText(String.valueOf(CalParamRecord.DEFAULT_AGE));
            editTextAttachFolder.setText(CalParamRecord.DEFAULT_ATTACH_FOLDER);
            editTextDBName.setText(CalParamRecord.DEFAULT_DB_NAME);
        }

        // Populate "Стартовая страница" (default = Календарь when not set yet)
        setupStartPageSpinner(currentRecord != null ? currentRecord.StartPage : null);

        // Populate the 5 quick buttons from the "Типы жизнедеятельности" reference
        // (stored value = числовой id записи LIVETYPE; default = предустановки 1..5)
        setupButtonSpinner(spinnerButton1, currentRecord != null ? currentRecord.Button1Id : null, CalParamRecord.DEFAULT_BUTTON1_ID);
        setupButtonSpinner(spinnerButton2, currentRecord != null ? currentRecord.Button2Id : null, CalParamRecord.DEFAULT_BUTTON2_ID);
        setupButtonSpinner(spinnerButton3, currentRecord != null ? currentRecord.Button3Id : null, CalParamRecord.DEFAULT_BUTTON3_ID);
        setupButtonSpinner(spinnerButton4, currentRecord != null ? currentRecord.Button4Id : null, CalParamRecord.DEFAULT_BUTTON4_ID);
        setupButtonSpinner(spinnerButton5, currentRecord != null ? currentRecord.Button5Id : null, CalParamRecord.DEFAULT_BUTTON5_ID);

        // OK button click handler
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values from form
                String address = editTextAddress.getText().toString().trim();
                String name = editTextName.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Task 54/100: числовые параметры и параметры хранения вложений
                int height = parseIntOrDefault(editTextHeight, CalParamRecord.DEFAULT_HEIGHT);
                int weight = parseIntOrDefault(editTextWeight, CalParamRecord.DEFAULT_WEIGHT);
                int age = parseIntOrDefault(editTextAge, CalParamRecord.DEFAULT_AGE);
                String attachFolder = editTextAttachFolder.getText().toString().trim();
                String dbName = editTextDBName.getText().toString().trim();

                // Create or update CalParamRecord
                if (currentRecord == null) {
                    currentRecord = new CalParamRecord(address, name, password);
                } else {
                    currentRecord.Address = address;
                    currentRecord.Name = name;
                    currentRecord.Password = password;
                }

                // Task 54/100: сохранить новые параметры
                currentRecord.Height = height;
                currentRecord.Weight = weight;
                currentRecord.Age = age;
                currentRecord.AttachFolder = attachFolder.isEmpty() ? CalParamRecord.DEFAULT_ATTACH_FOLDER : attachFolder;
                currentRecord.DBName = dbName.isEmpty() ? CalParamRecord.DEFAULT_DB_NAME : dbName;

                // Save the chosen "Ведущий"
                int vi = spinnerVedushii.getSelectedItemPosition();
                if (vi > 0) {
                    currentRecord.Vedushii = contactLabels.get(vi);
                    currentRecord.VedushiiID = contactIds.get(vi);
                } else {
                    currentRecord.Vedushii = null;
                    currentRecord.VedushiiID = null;
                }

                // Save the chosen "Стартовая страница" (default = Календарь)
                int sp = spinnerStartPage.getSelectedItemPosition();
                currentRecord.StartPage = (sp >= 0 && sp < START_PAGE_VALUES.length)
                        ? START_PAGE_VALUES[sp] : CalParamRecord.START_PAGE_CALENDAR;

                // Save the chosen quick buttons (only the числовой id записи LIVETYPE is stored)
                currentRecord.Button1Id = selectedButtonId(spinnerButton1);
                currentRecord.Button2Id = selectedButtonId(spinnerButton2);
                currentRecord.Button3Id = selectedButtonId(spinnerButton3);
                currentRecord.Button4Id = selectedButtonId(spinnerButton4);
                currentRecord.Button5Id = selectedButtonId(spinnerButton5);

                // Keep the static author fields (used by all record screens) in sync
                ManageSQLDatabase.AuthorName = currentRecord.Vedushii;
                ManageSQLDatabase.AuthorID = currentRecord.VedushiiID;

                // Save to database
                owerDb.upsertCalParam(currentRecord);

                // Return result to MainActivity
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        // Cancel button click handler
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // As start page (launched instead of MainActivity): system "Back" closes the app.
        // The internal "X" button above still returns to MainActivity via finish().
        if (getIntent().getBooleanExtra(CalParamRecord.EXTRA_IS_START_PAGE, false)) {
            finishAndRemoveTask();
        } else {
            super.onBackPressed();
        }
    }

    private void loadContacts() {
        contactLabels.add("");
        contactIds.add(null);
        ContactRecord[] contacts = owerDb.getContacts("");
        if (contacts != null) {
            for (ContactRecord c : contacts) {
                StringBuilder sb = new StringBuilder();
                if (c.Surname != null) sb.append(c.Surname).append(" ");
                if (c.FirstName != null) sb.append(c.FirstName);
                if (sb.toString().trim().isEmpty()) continue;
                contactLabels.add(sb.toString().trim());
                contactIds.add(c.EntryID != null ? c.EntryID
                        : (c.id != null ? String.valueOf(c.id) : null));
            }
        }
    }

    private void setupVedushiiSpinner(String current) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, contactLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVedushii.setAdapter(adapter);
        int idx = current != null ? contactLabels.indexOf(current) : -1;
        spinnerVedushii.setSelection(idx >= 0 ? idx : 0);
    }

    private void setupStartPageSpinner(String current) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, START_PAGE_LABELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStartPage.setAdapter(adapter);
        int idx = 0; // По умолчанию "Календарь" (MainActivity)
        if (current != null) {
            for (int i = 0; i < START_PAGE_VALUES.length; i++) {
                if (current.equals(START_PAGE_VALUES[i])) {
                    idx = i;
                    break;
                }
            }
        }
        spinnerStartPage.setSelection(idx);
    }

    /** Loads the "Типы жизнедеятельности" reference for the quick buttons (id + name). */
    private void loadLivetypes() {
        livetypeLabels.clear();
        livetypeIds.clear();
        try {
            LivetypeSQLManage livetypeDb = new LivetypeSQLManage(owerDb.getWritableDatabase());
            livetypeRecord[] records = livetypeDb.getAllLivetype("");
            if (records != null) {
                for (livetypeRecord r : records) {
                    if (r.id == null) continue;
                    String label = (r.Name != null && !r.Name.trim().isEmpty()) ? r.Name.trim() : "(без названия)";
                    livetypeLabels.add(label);
                    livetypeIds.add(r.id);
                }
            }
        } catch (Exception e) {
            // Non-fatal: without the reference the spinners stay empty
        }
    }

    /** Fills one quick-button spinner with the LIVETYPE names and selects the saved id (or the default). */
    private void setupButtonSpinner(Spinner spinner, Integer currentId, int defaultId) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, livetypeLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int target = currentId != null ? currentId : defaultId;
        int idx = livetypeIds.indexOf(target);
        spinner.setSelection(idx >= 0 ? idx : 0);
    }

    /** Returns the числовой id LIVETYPE selected in the given quick-button spinner (or null). */
    private Integer selectedButtonId(Spinner spinner) {
        int pos = spinner.getSelectedItemPosition();
        if (pos >= 0 && pos < livetypeIds.size()) return livetypeIds.get(pos);
        return null;
    }

    /** Parses a numeric field; returns the default when the value is empty or invalid (Task 54). */
    private int parseIntOrDefault(EditText edit, int defaultValue) {
        try {
            return Integer.parseInt(edit.getText().toString().trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}