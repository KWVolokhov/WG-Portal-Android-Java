package com.example.calendar4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class ParamsActivity extends Activity {

    private Spinner spinnerVedushii;
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
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);

        // Initialize database and load current parameters
        owerDb = new ManageSQLDatabase(this);
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
}