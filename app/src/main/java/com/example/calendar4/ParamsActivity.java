package com.example.calendar4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ParamsActivity extends Activity {

    private EditText editTextAddress;
    private EditText editTextName;
    private EditText editTextPassword;
    private Button btnOK;
    private Button btnCancel;

    private ManageSQLDatabase owerDb;
    private CalParamRecord currentRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_params);

        // Initialize views
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);

        // Initialize database and load current parameters
        owerDb = new ManageSQLDatabase(this);
        currentRecord = owerDb.getCalParam();

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
}