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

public class ParamsActivity extends Activity {

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
    private EditText editTextAddress;
    private EditText editTextName;
    private EditText editTextPassword;
    private ImageButton btnOK;
    private ImageButton btnCancel;

    private ManageSQLDatabase owerDb;
    private CalParamRecord currentRecord;

    // Contacts for the "Ведущий" picker (display text and identifier)
    private final ArrayList<String> contactLabels = new ArrayList<>();
    private final ArrayList<String> contactIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_params);

        // Initialize views
        spinnerVedushii = findViewById(R.id.spinnerVedushii);
        spinnerStartPage = findViewById(R.id.spinnerStartPage);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);

        // Initialize database and load current parameters
        // owerDb = new ManageSQLDatabase(this);
		owerDb = ManageSQLDatabase.getInstance(this);
        currentRecord = owerDb.getCalParam();

        loadContacts();

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
        }

        // Populate "Стартовая страница" (default = Календарь when not set yet)
        setupStartPageSpinner(currentRecord != null ? currentRecord.StartPage : null);

        // OK button click handler
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values from form
                String address = editTextAddress.getText().toString().trim();
                String name = editTextName.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Create or update CalParamRecord
                if (currentRecord == null) {
                    currentRecord = new CalParamRecord(address, name, password);
                } else {
                    currentRecord.Address = address;
                    currentRecord.Name = name;
                    currentRecord.Password = password;
                }

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
}