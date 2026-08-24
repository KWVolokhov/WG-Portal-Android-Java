package com.example.calendar4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Card (edit form) for a CALPLAN project.
 *
 * Editable: Form, Name, Priority, StartDate (date control), RequestName, Status
 * (drop-down, English alias saved to StatusID), MainSystem (drop-down),
 * AnalitikName/ExectorName (drop-down from CONTACTS, EntryID to AnalitikID/ExectorID),
 * BodyText (InfoFieldView), Comment, InstallOrder, KeyWords.
 *
 * Read-only (after the divider): Okdate (= creation date), LastUpdatedBy,
 * LastUpdatedDate, EndDate, HoldDate, AuthorName (constant "Исполнитель").
 */
public class InputCalPlanActivity extends Activity {

    // Form (fixed values)
    private static final String[] FORM_VALUES = {"Project", "Note", "Remember", "Task"};

    // Status: Russian label shown in the list, English alias (no spaces) saved to StatusID
    private static final String[] STATUS_LABELS = {"Черновик", "В работе", "Тестирование",
            "Выполнено", "Отменено", "Отложено"};
    private static final String[] STATUS_IDS = {"Draft", "Inwork", "Intest",
            "Done", "Canceled", "Hold"};

    // Main system (fixed values)
    private static final String[] MAIN_SYSTEMS = {"Lotus(HCL)", "VBA", "Java", "JavaScriptServer",
            "JavaScript", "SQL", "Busines", "ArtDesign", "Combo"};

    private static final SimpleDateFormat DISPLAY_DATE =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private Spinner spinnerForm;
    private EditText editTextName;
    private EditText editTextPriority;
    private TextView textViewOkdate;
    private DateFieldView dateFieldStartDate;
    private EditText editTextRequestName;
    private Spinner spinnerStatus;
    private Spinner spinnerMainSystem;
    private Spinner spinnerAnalitik;
    private Spinner spinnerExector;
    private InfoFieldView infoBodyText;
    private EditText editTextComment;
    private EditText editTextInstallOrder;
    private EditText editTextKeyWords;
    private TextView textViewLastUpdatedBy;
    private TextView textViewLastUpdatedDate;
    private TextView textViewEndDate;
    private TextView textViewHoldDate;
    private TextView textViewAuthorName;
    private Button btnOK;
    private Button btnCancel;

    private Date activeDate;
    private Date okdateValue;
    private calPlanRecord record;
    private ManageSQLDatabase owerDb;

    // Contacts for "Постановщик" / "Исполнитель" (display text and EntryID)
    private final ArrayList<String> contactLabels = new ArrayList<>();
    private final ArrayList<String> contactIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_cal_plan);

        spinnerForm = findViewById(R.id.spinnerForm);
        editTextName = findViewById(R.id.editTextName);
        editTextPriority = findViewById(R.id.editTextPriority);
        textViewOkdate = findViewById(R.id.textViewOkdate);
        dateFieldStartDate = findViewById(R.id.dateFieldStartDate);
        editTextRequestName = findViewById(R.id.editTextRequestName);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerMainSystem = findViewById(R.id.spinnerMainSystem);
        spinnerAnalitik = findViewById(R.id.spinnerAnalitik);
        spinnerExector = findViewById(R.id.spinnerExector);
        infoBodyText = findViewById(R.id.infoBodyText);
        editTextComment = findViewById(R.id.editTextComment);
        editTextInstallOrder = findViewById(R.id.editTextInstallOrder);
        editTextKeyWords = findViewById(R.id.editTextKeyWords);
        textViewLastUpdatedBy = findViewById(R.id.textViewLastUpdatedBy);
        textViewLastUpdatedDate = findViewById(R.id.textViewLastUpdatedDate);
        textViewEndDate = findViewById(R.id.textViewEndDate);
        textViewHoldDate = findViewById(R.id.textViewHoldDate);
        textViewAuthorName = findViewById(R.id.textViewAuthorName);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);

        Intent intent = getIntent();
        activeDate = (Date) intent.getSerializableExtra("activeDate");
        if (activeDate == null) activeDate = new Date();
        if (intent.hasExtra("calPlanRecord")) {
            record = (calPlanRecord) intent.getSerializableExtra("calPlanRecord");
        }

        owerDb = new ManageSQLDatabase(this);
        loadContacts();

        setupSpinner(spinnerForm, FORM_VALUES, record != null ? record.Form : null);
        setupSpinner(spinnerStatus, STATUS_LABELS, record != null ? statusLabel(record) : null);
        setupSpinner(spinnerMainSystem, MAIN_SYSTEMS, record != null ? record.MainSystem : null);
        setupContactSpinner(spinnerAnalitik, record != null ? record.AnalitikName : null);
        setupContactSpinner(spinnerExector, record != null ? record.ExectorName : null);

        populateFields();

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

    // ---------------------------------------------------------------------
    // Setups
    // ---------------------------------------------------------------------

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

    private void setupSpinner(Spinner spinner, String[] values, String current) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int idx = indexOf(values, current);
        spinner.setSelection(idx >= 0 ? idx : 0);
    }

    private void setupContactSpinner(Spinner spinner, String current) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, contactLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int idx = current != null ? contactLabels.indexOf(current) : -1;
        spinner.setSelection(idx >= 0 ? idx : 0);
    }

    /** Russian status label matching the record's StatusID/Status. */
    private String statusLabel(calPlanRecord r) {
        if (r.Status != null) {
            int idx = indexOf(STATUS_LABELS, r.Status);
            if (idx >= 0) return STATUS_LABELS[idx];
        }
        if (r.StatusID != null) {
            for (int i = 0; i < STATUS_IDS.length; i++) {
                if (STATUS_IDS[i].equalsIgnoreCase(r.StatusID)) return STATUS_LABELS[i];
            }
        }
        return null;
    }

    private static int indexOf(String[] arr, String val) {
        if (val == null) return -1;
        for (int i = 0; i < arr.length; i++) {
            if (val.equals(arr[i])) return i;
        }
        return -1;
    }

    // ---------------------------------------------------------------------
    // Fill / save
    // ---------------------------------------------------------------------

    private void populateFields() {
        okdateValue = (record != null && record.Okdate != null) ? record.Okdate : activeDate;
        textViewOkdate.setText(DISPLAY_DATE.format(okdateValue));

        Date start = record != null && record.StartDate != null ? record.StartDate : okdateValue;
        dateFieldStartDate.setDate(start);

        textViewAuthorName.setText("Исполнитель");
        if (record == null) {
            return;
        }

        if (record.Name != null) editTextName.setText(record.Name);
        if (record.Priority != null) editTextPriority.setText(String.valueOf(record.Priority));
        if (record.RequestName != null) editTextRequestName.setText(record.RequestName);
        if (record.BodyText != null) infoBodyText.setText(record.BodyText);
        if (record.Comment != null) editTextComment.setText(record.Comment);
        if (record.InstallOrder != null) editTextInstallOrder.setText(record.InstallOrder);
        if (record.KeyWords != null) editTextKeyWords.setText(record.KeyWords);

        textViewLastUpdatedBy.setText(record.LastUpdatedBy != null ? record.LastUpdatedBy : "");
        textViewLastUpdatedDate.setText(
                record.LastUpdatedDate != null ? DISPLAY_DATE.format(record.LastUpdatedDate) : "");
        textViewEndDate.setText(record.EndDate != null ? DISPLAY_DATE.format(record.EndDate) : "");
        textViewHoldDate.setText(record.HoldDate != null ? DISPLAY_DATE.format(record.HoldDate) : "");
    }

    private void saveAndFinish() {
        String form = spinnerForm.getSelectedItem().toString();
        String name = editTextName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
            return;
        }

        if (record == null) {
            record = new calPlanRecord();
        }
        if (record.Okdate == null) record.Okdate = okdateValue;

        record.Form = form;
        record.Name = name;

        String prio = editTextPriority.getText().toString().trim();
        if (prio.isEmpty()) {
            record.Priority = null;
        } else {
            try {
                record.Priority = Integer.parseInt(prio);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Приоритет должен быть числом", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        record.StartDate = dateFieldStartDate.getDate();
        record.RequestName = editTextRequestName.getText().toString().trim();

        int si = spinnerStatus.getSelectedItemPosition();
        record.Status = STATUS_LABELS[si];
        record.StatusID = STATUS_IDS[si];

        record.MainSystem = spinnerMainSystem.getSelectedItem().toString();

        int ai = spinnerAnalitik.getSelectedItemPosition();
        if (ai > 0) {
            record.AnalitikName = contactLabels.get(ai);
            record.AnalitikID = contactIds.get(ai);
        } else {
            record.AnalitikName = null;
            record.AnalitikID = null;
        }

        int ei = spinnerExector.getSelectedItemPosition();
        if (ei > 0) {
            record.ExectorName = contactLabels.get(ei);
            record.ExectorID = contactIds.get(ei);
        } else {
            record.ExectorName = null;
            record.ExectorID = null;
        }

        record.BodyText = infoBodyText.getText();
        record.Comment = editTextComment.getText().toString().trim();
        record.InstallOrder = editTextInstallOrder.getText().toString().trim();
        record.KeyWords = editTextKeyWords.getText().toString().trim();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("calPlanRecord", record);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
