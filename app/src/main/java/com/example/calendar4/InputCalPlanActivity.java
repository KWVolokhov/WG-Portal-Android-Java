package com.example.calendar4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Date;

public class InputCalPlanActivity extends Activity {

    private Spinner spinnerForm;
    private EditText editTextName;
    private Button btnOK;
    private Button btnCancel;

    private calPlanRecord record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_cal_plan);

        // Initialize views
        spinnerForm = findViewById(R.id.spinnerForm);
        editTextName = findViewById(R.id.editTextName);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup Form dropdown with fixed values
        String[] formValues = {"Project", "Note", "Remember", "Task"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, formValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerForm.setAdapter(adapter);

        // Set default selection to "Project"
        spinnerForm.setSelection(0);

        // OK button click handler
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get values from form
                String form = spinnerForm.getSelectedItem().toString();
                String name = editTextName.getText().toString().trim();

                // Validate Name field (required)
                if (name.isEmpty()) {
                    Toast.makeText(InputCalPlanActivity.this, 
                        "Введите название", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create calPlanRecord
                record = new calPlanRecord(form, name);
                
                // Set StartDate to today (or active date from calendar)
                record.StartDate = new Date();
                record.Okdate = new Date();

                // Return result to calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("calPlanRecord", record);
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
    protected void onDestroy() {
        super.onDestroy();
    }
}