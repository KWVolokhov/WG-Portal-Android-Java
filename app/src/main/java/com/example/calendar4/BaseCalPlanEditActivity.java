package com.example.calendar4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Base editable card for all CALPLAN forms (Project, Note, Remember, Task, History,
 * HealthEat, HealthDrink, HealthSport). Subclasses configure which fields/labels to show.
 */
public abstract class BaseCalPlanEditActivity extends Activity {

    protected static final String[] FORM_VALUES = {"Project", "Note", "Remember", "Task",
            "History", "HealthEat", "HealthDrink", "HealthSport"};
    protected static final String[] STATUS_LABELS = {"Черновик", "В работе", "Тестирование",
            "Выполнено", "Отменено", "Отложено"};
    protected static final String[] STATUS_IDS = {"Draft", "Inwork", "Intest",
            "Done", "Canceled", "Hold"};
    protected static final String[] MAIN_SYSTEMS = {"Lotus(HCL)", "VBA", "Java", "JavaScriptServer",
            "JavaScript", "SQL", "Busines", "ArtDesign", "Combo"};

    protected static final SimpleDateFormat DISPLAY_DATE =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    // =====================================================================
    // Form configuration (override in subclasses)
    // =====================================================================

    protected String getFormType() { return "Project"; }
    protected String getStartDateLabel() { return "Дата старта проекта:"; }
    protected String getBodyTextLabel() { return "Описание задачи:"; }
    protected String getRequestNameLabel() { return "Заявка на автоматизацию:"; }
    protected String getAuthorLabel() { return "Автор проекта:"; }
    protected String getEndDateLabel() { return "Дата завершения проекта (факт):"; }
    protected String getHoldDateLabel() { return "Дата откладывания проекта:"; }

    protected boolean showStatus() { return true; }
    protected boolean showMainSystem() { return true; }
    protected boolean showPriority() { return true; }
    protected boolean showStartDate() { return true; }
    protected boolean showRequestName() { return true; }
    protected boolean showAnalitikExector() { return true; }
    protected boolean showInstallOrder() { return true; }
    protected boolean showKeyWords() { return true; }
    protected boolean showLastUpdatedBy() { return true; }
    protected boolean showEndDate() { return true; }
    protected boolean showHoldDate() { return true; }
    protected boolean isRequestPicker() { return false; }
    protected boolean allowFormChange() { return true; }

    // ----- views -----
    protected Spinner spinnerForm, spinnerStatus, spinnerMainSystem, spinnerAnalitik, spinnerExector;
    protected EditText editTextName, editTextPriority, editTextRequestName,
            editTextComment, editTextInstallOrder, editTextKeyWords;
    protected DateFieldView dateFieldStartDate;
    protected InfoFieldView infoBodyText;
    protected TextView textViewOkdate, textViewLastUpdatedBy, textViewLastUpdatedDate,
            textViewEndDate, textViewHoldDate, textViewAuthorName;
    protected ImageButton btnOK, btnCancel, btnPickRequest;

    protected View rowStatus, rowMainSystem, rowPriority, rowStartDate, rowRequestName,
            rowAnalitik, rowExector, rowInstallOrder, rowKeyWords, rowLastUpdatedBy,
            rowEndDate, rowHoldDate;
    protected TextView labelStartDate, labelBodyText, labelRequestName, labelAuthorName,
            labelEndDate, labelHoldDate;

    protected calPlanRecord record;
    protected Date activeDate;
    protected Date okdateValue;
    protected ManageSQLDatabase owerDb;
    protected String selectedRequestUNID;

    protected final ArrayList<String> contactLabels = new ArrayList<>();
    protected final ArrayList<String> contactIds = new ArrayList<>();

    // =====================================================================
    // Lifecycle
    // =====================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_cal_plan);

        spinnerForm = findViewById(R.id.spinnerForm);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerMainSystem = findViewById(R.id.spinnerMainSystem);
        spinnerAnalitik = findViewById(R.id.spinnerAnalitik);
        spinnerExector = findViewById(R.id.spinnerExector);
        editTextName = findViewById(R.id.editTextName);
        editTextPriority = findViewById(R.id.editTextPriority);
        editTextRequestName = findViewById(R.id.editTextRequestName);
        editTextComment = findViewById(R.id.editTextComment);
        editTextInstallOrder = findViewById(R.id.editTextInstallOrder);
        editTextKeyWords = findViewById(R.id.editTextKeyWords);
        dateFieldStartDate = findViewById(R.id.dateFieldStartDate);
        infoBodyText = findViewById(R.id.infoBodyText);
        textViewOkdate = findViewById(R.id.textViewOkdate);
        textViewLastUpdatedBy = findViewById(R.id.textViewLastUpdatedBy);
        textViewLastUpdatedDate = findViewById(R.id.textViewLastUpdatedDate);
        textViewEndDate = findViewById(R.id.textViewEndDate);
        textViewHoldDate = findViewById(R.id.textViewHoldDate);
        textViewAuthorName = findViewById(R.id.textViewAuthorName);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);
        btnPickRequest = findViewById(R.id.btnPickRequest);

        rowStatus = findViewById(R.id.rowStatus);
        rowMainSystem = findViewById(R.id.rowMainSystem);
        rowPriority = findViewById(R.id.rowPriority);
        rowStartDate = findViewById(R.id.rowStartDate);
        rowRequestName = findViewById(R.id.rowRequestName);
        rowAnalitik = findViewById(R.id.rowAnalitik);
        rowExector = findViewById(R.id.rowExector);
        rowInstallOrder = findViewById(R.id.rowInstallOrder);
        rowKeyWords = findViewById(R.id.rowKeyWords);
        rowLastUpdatedBy = findViewById(R.id.rowLastUpdatedBy);
        rowEndDate = findViewById(R.id.rowEndDate);
        rowHoldDate = findViewById(R.id.rowHoldDate);

        labelStartDate = findViewById(R.id.labelStartDate);
        labelBodyText = findViewById(R.id.labelBodyText);
        labelRequestName = findViewById(R.id.labelRequestName);
        labelAuthorName = findViewById(R.id.labelAuthorName);
        labelEndDate = findViewById(R.id.labelEndDate);
        labelHoldDate = findViewById(R.id.labelHoldDate);

        Intent intent = getIntent();
        activeDate = (Date) intent.getSerializableExtra("activeDate");
        if (activeDate == null) activeDate = new Date();
        if (intent.hasExtra("calPlanRecord")) {
            record = (calPlanRecord) intent.getSerializableExtra("calPlanRecord");
        }

		owerDb = ManageSQLDatabase.getInstance(this);
        loadContacts();

        applyConfig();

        setupSpinner(spinnerForm, FORM_VALUES, record != null ? record.Form : getFormType());
        setupSpinner(spinnerStatus, STATUS_LABELS, record != null ? statusLabel(record) : null);
        setupSpinner(spinnerMainSystem, MAIN_SYSTEMS, record != null ? record.MainSystem : null);
        setupContactSpinner(spinnerAnalitik, record != null ? record.AnalitikName : null);
        setupContactSpinner(spinnerExector, record != null ? record.ExectorName : null);

        populateFields();

        btnOK.setOnClickListener(v -> saveAndFinish());
        btnCancel.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }

    // =====================================================================
    // Setups
    // =====================================================================

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

    /** Applies the subclass configuration to the shared layout. */
    private void applyConfig() {
        labelStartDate.setText(getStartDateLabel());
        labelBodyText.setText(getBodyTextLabel());
        labelRequestName.setText(getRequestNameLabel());
        labelAuthorName.setText(getAuthorLabel());
        labelEndDate.setText(getEndDateLabel());
        labelHoldDate.setText(getHoldDateLabel());

        setRowVisible(rowStatus, showStatus());
        setRowVisible(rowMainSystem, showMainSystem());
        setRowVisible(rowPriority, showPriority());
        setRowVisible(rowStartDate, showStartDate());
        setRowVisible(rowRequestName, showRequestName());
        setRowVisible(rowAnalitik, showAnalitikExector());
        setRowVisible(rowExector, showAnalitikExector());
        setRowVisible(rowInstallOrder, showInstallOrder());
        setRowVisible(rowKeyWords, showKeyWords());
        setRowVisible(rowLastUpdatedBy, showLastUpdatedBy());
        setRowVisible(rowEndDate, showEndDate());
        setRowVisible(rowHoldDate, showHoldDate());

        // Fixed form types in dedicated screens: disallow changing the Form spinner
        spinnerForm.setEnabled(allowFormChange());

        if (isRequestPicker()) {
            editTextRequestName.setFocusable(false);
            editTextRequestName.setClickable(true);
            editTextRequestName.setOnClickListener(v -> openProjectPicker());
            btnPickRequest.setVisibility(View.VISIBLE);
            btnPickRequest.setOnClickListener(v -> openProjectPicker());
        } else {
            editTextRequestName.setFocusable(true);
            editTextRequestName.setClickable(false);
            editTextRequestName.setOnClickListener(null);
            btnPickRequest.setVisibility(View.GONE);
        }
    }

    private void setRowVisible(View row, boolean visible) {
        if (row != null) {
            row.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    // =====================================================================
    // Fill / save
    // =====================================================================

    private void populateFields() {
        okdateValue = (record != null && record.Okdate != null) ? record.Okdate : activeDate;
        textViewOkdate.setText(DISPLAY_DATE.format(okdateValue));

        if (showStartDate()) {
            Date start = record != null && record.StartDate != null ? record.StartDate : okdateValue;
            dateFieldStartDate.setDate(start);
        }

        if (record == null) {
            // New record: set the Author to "Ведущий" from CALPARAM (all form types)
            CalParamRecord param = owerDb.getCalParam();
            record = new calPlanRecord();
            record.Form = getFormType();
            if (param != null) {
                record.AuthorName = param.Vedushii;
                record.AuthorID = param.VedushiiID;
            }
            textViewAuthorName.setText(record.AuthorName != null ? record.AuthorName : "");
            return;
        }

        textViewAuthorName.setText(record.AuthorName != null ? record.AuthorName : "");
        if (isRequestPicker() && record.RequestUNID != null) selectedRequestUNID = record.RequestUNID;

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

        record.RequestName = editTextRequestName.getText().toString().trim();
        if (isRequestPicker()) {
            record.RequestUNID = selectedRequestUNID;
        }

        // If the StartDate row is hidden (History/Health) the date equals the creation date
        record.StartDate = showStartDate()
                ? dateFieldStartDate.getDate()
                : (record.Okdate != null ? record.Okdate : okdateValue);

        if (showStatus()) {
            int si = spinnerStatus.getSelectedItemPosition();
            record.Status = STATUS_LABELS[si];
            record.StatusID = STATUS_IDS[si];
        }

        if (showMainSystem()) {
            record.MainSystem = spinnerMainSystem.getSelectedItem().toString();
        }

        if (showAnalitikExector()) {
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
        }

        record.BodyText = infoBodyText.getText();
        record.Comment = editTextComment.getText().toString().trim();

        if (showInstallOrder()) {
            record.InstallOrder = editTextInstallOrder.getText().toString().trim();
        }
        if (showKeyWords()) {
            record.KeyWords = editTextKeyWords.getText().toString().trim();
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("calPlanRecord", record);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /** Modal picker for "В Проекте" (Task): chooses from all CALPLAN Form=Project records. */
    private void openProjectPicker() {
        calPlanRecord[] projects = owerDb.getCalPlanByForm("Project");
        if (projects == null || projects.length == 0) {
            Toast.makeText(this, "Нет проектов для выбора", Toast.LENGTH_SHORT).show();
            return;
        }
        final String[] names = new String[projects.length];
        final String[] unids = new String[projects.length];
        for (int i = 0; i < projects.length; i++) {
            names[i] = projects[i].Name != null ? projects[i].Name : "";
            unids[i] = projects[i].UNID;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, names);
        new AlertDialog.Builder(this)
                .setTitle("В Проекте - выберите проект")
                .setAdapter(adapter, (d, which) -> {
                    editTextRequestName.setText(names[which]);
                    selectedRequestUNID = unids[which];
                    d.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}