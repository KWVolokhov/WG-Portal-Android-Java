package com.example.calendar4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Base editable card for all CALPLAN forms (Project, Note, Remember, Task, History,
 * HealthEat, HealthDrink, HealthSport). Subclasses configure which fields/labels to show.
 */
public abstract class BaseCalPlanEditActivity extends Activity {

    protected static final String[] FORM_VALUES = {"Project", "Note", "Remember", "Task",
            "History", "HealthEat", "HealthDrink", "HealthSport", "HealthStress", "HealthJoy"};

    // Task 35: when the card is opened from the "Проекты" screen (Add / edit), the Form
    // picker offers only Проекты/Задачи/Заявка на автоматизацию (Project/Task/Request).
    public static final String EXTRA_PROJECTS_FORM_MODE = "extra_projects_form_mode";
    protected static final String[] PROJECT_FORM_LABELS = {"Проекты", "Задачи", "Заявка на автоматизацию"};
    protected static final String[] PROJECT_FORM_VALUES = {"Project", "Task", "Request"};
    protected static final String[] STATUS_LABELS = {"Черновик", "В работе", "Тестирование",
            "Выполнено", "Отменено", "Отложено"};
    protected static final String[] STATUS_IDS = {"Draft", "Inwork", "Intest",
            "Done", "Canceled", "Hold"};
    protected static final String[] MAIN_SYSTEMS = {"Lotus(HCL)", "VBA", "Java", "JavaScriptServer",
            "JavaScript", "SQL", "Busines", "ArtDesign", "Combo"};

    protected static final SimpleDateFormat DISPLAY_DATE =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    // Task 44: дата создания/обновления/завершения показывается вместе со временем.
    protected static final SimpleDateFormat DISPLAY_DATE_TIME =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

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

    // Task 41: показывать ли на карточке числовые поля Health
    // (Шаги / Вес еды / Объем питья / Каллории). Включено только для Health-форм.
    protected boolean showHealthNumbers() { return false; }

    // Task 45: полный массив полей "Голова".."Каллории", как на activity_livetype_edit.
    private static final String[] HEALTH_NUMBER_NAMES = {
            "Голова", "Глаза", "Уши", "Нос", "Горло", "Зубы",
            "Желудок", "Кишечник", "Печень", "Почки", "Сердце", "Лёгкие",
            "Давление", "Сон", "Вес", "Нервная система", "Мораль", "Состояние кожи",
            "Шаги", "Вес еды", "Объем питья", "Каллории"};

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

    // Task 41: контейнер числовых полей Health на карточке
    protected LinearLayout healthContainer;
    protected EditText[] healthNumberEdits;

    // Task 44: таймер шагомера на карточке HealthSportActivity (рядом с "Дата создания").
    protected View rowPedometer;
    protected TextView textViewPedometerTimer;
    private final Handler pedometerHandler = new Handler(Looper.getMainLooper());
    private boolean pedometerTimerShown = false;
    private long pedometerTimerLastMs = 0;
    private final Runnable pedometerTickRunnable = new Runnable() {
        @Override
        public void run() {
            updatePedometerTimer();
        }
    };

    protected calPlanRecord record;
    protected Date activeDate;
    protected Date okdateValue;
    protected ManageSQLDatabase owerDb;
    protected String selectedRequestUNID;

    /** Task 35: true when the card was opened from the "Проекты" screen (Form = Проекты/Задачи/Заявка). */
    private boolean projectsFormMode = false;

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
        rowPedometer = findViewById(R.id.rowPedometer);
        textViewPedometerTimer = findViewById(R.id.textViewPedometerTimer);
        textViewAuthorName = findViewById(R.id.textViewAuthorName);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);
        btnPickRequest = findViewById(R.id.btnPickRequest);
        healthContainer = findViewById(R.id.healthContainer);

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

        // Task 35: "Проекты"-mode restricts the Form picker to Проекты/Задачи/Заявка
        projectsFormMode = intent.getBooleanExtra(EXTRA_PROJECTS_FORM_MODE, false);

        applyConfig();

        setupHealthNumbers();

        if (projectsFormMode) {
            setupSpinner(spinnerForm, PROJECT_FORM_LABELS, formLabelFor(record != null ? record.Form : getFormType()));
        } else {
            setupSpinner(spinnerForm, FORM_VALUES, record != null ? record.Form : getFormType());
        }
        setupSpinner(spinnerStatus, STATUS_LABELS, record != null ? statusLabel(record) : null);
        setupSpinner(spinnerMainSystem, MAIN_SYSTEMS, record != null ? record.MainSystem : null);
        setupContactSpinner(spinnerAnalitik, record != null ? record.AnalitikName : null);
        setupContactSpinner(spinnerExector, record != null ? record.ExectorName : null);

        populateFields();

        // Task 44: на карточке HealthSportActivity показываем таймер запущенного шагомера
        setupPedometerTimer();

        btnOK.setOnClickListener(v -> saveAndFinish());
        btnCancel.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }

    // =====================================================================
    // Task 44: таймер шагомера на карточке HealthSportActivity
    // =====================================================================

    private void setupPedometerTimer() {
        if (rowPedometer == null) return;
        pedometerTimerShown = false;
        updatePedometerTimer();
    }

    /** Запускает/останавливает индикацию времени шагомера (чч:мм:сс) рядом с "Дата создания". */
    private void updatePedometerTimer() {
        if (rowPedometer == null || textViewPedometerTimer == null) return;

        boolean healthSport = record != null && "HealthSport".equals(record.Form);
        if (!healthSport) {
            rowPedometer.setVisibility(View.GONE);
            pedometerHandler.removeCallbacks(pedometerTickRunnable);
            return;
        }

        Integer rid = Pedometer.getRunningRecordId();
        boolean running = Pedometer.isRunning() && rid != null && record != null
                && record.id != null && record.id.equals(rid);

        if (running) {
            pedometerTimerShown = true;
            pedometerTimerLastMs = Pedometer.getElapsedMs();
            textViewPedometerTimer.setText(formatPedometerTime(pedometerTimerLastMs));
            rowPedometer.setVisibility(View.VISIBLE);
            pedometerHandler.removeCallbacks(pedometerTickRunnable);
            pedometerHandler.postDelayed(pedometerTickRunnable, 1000);
        } else if (pedometerTimerShown) {
            // Шагомер завершился - таймер останавливается (последнее значение замораживается).
            textViewPedometerTimer.setText(formatPedometerTime(pedometerTimerLastMs));
            pedometerHandler.removeCallbacks(pedometerTickRunnable);
        } else {
            rowPedometer.setVisibility(View.GONE);
        }
    }

    private static String formatPedometerTime(long ms) {
        long totalSec = Math.max(0L, ms / 1000);
        long h = totalSec / 3600;
        long m = (totalSec % 3600) / 60;
        long s = totalSec % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }

    @Override
    protected void onDestroy() {
        pedometerHandler.removeCallbacks(pedometerTickRunnable);
        super.onDestroy();
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
    // Task 41: числовые поля Health (Шаги/Вес еды/Объем питья/Каллории)
    // =====================================================================

    /** Builds the Health number fields (only when the subclass asks for them). */
    private void setupHealthNumbers() {
        if (healthContainer == null) return;
        boolean visible = showHealthNumbers();
        setRowVisible(healthContainer, visible);
        if (!visible) return;

        if (healthContainer.getChildCount() == 0) {
            healthNumberEdits = new EditText[HEALTH_NUMBER_NAMES.length];
            for (int i = 0; i < HEALTH_NUMBER_NAMES.length; i++) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                TextView label = new TextView(this);
                label.setText(HEALTH_NUMBER_NAMES[i]);
                label.setTextSize(16);
                label.setGravity(Gravity.CENTER_VERTICAL);
                row.addView(label);

                EditText edit = new EditText(this);
                edit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                edit.setSingleLine(true);
                LinearLayout.LayoutParams weightLp = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                row.addView(edit, weightLp);
                healthNumberEdits[i] = edit;

                healthContainer.addView(row);
            }
        }
    }

    /** Value typed in the Health number field with the given index (null when empty). */
    private Integer healthNumberValue(int index) {
        if (healthNumberEdits == null || index < 0 || index >= healthNumberEdits.length) return null;
        EditText edit = healthNumberEdits[index];
        if (edit == null) return null;
        String text = edit.getText().toString().trim();
        if (text.isEmpty()) return null;
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setHealthNumber(int index, Integer value) {
        if (healthNumberEdits != null && index >= 0 && index < healthNumberEdits.length
                && healthNumberEdits[index] != null) {
            EditText edit = healthNumberEdits[index];
            if (value != null) {
                edit.setText(String.valueOf(value));
            } else {
                edit.setText("");
            }
        }
    }

    /** Task 45: записывает значение поля "Голова".."Каллории" в calPlanRecord по индексу. */
    private void putHealthRecordValue(calPlanRecord r, int index, Integer value) {
        if (r == null) return;
        switch (index) {
            case 0: r.Head = value; break;
            case 1: r.Eyes = value; break;
            case 2: r.Ears = value; break;
            case 3: r.Nose = value; break;
            case 4: r.Throat = value; break;
            case 5: r.Teeth = value; break;
            case 6: r.Stomach = value; break;
            case 7: r.Intestines = value; break;
            case 8: r.Liver = value; break;
            case 9: r.Kidneys = value; break;
            case 10: r.Heart = value; break;
            case 11: r.Lungs = value; break;
            case 12: r.Pressure = value; break;
            case 13: r.Sleep = value; break;
            case 14: r.Weight = value; break;
            case 15: r.Nervous = value; break;
            case 16: r.Morality = value; break;
            case 17: r.Skin = value; break;
            case 18: r.Steps = value; break;
            case 19: r.FoodWeight = value; break;
            case 20: r.DrinkValue = value; break;
            case 21: r.Kallory = value; break;
            default: break;
        }
    }

    /** Task 45: читает значение поля "Голова".."Каллории" из calPlanRecord по индексу. */
    private Integer getHealthRecordValue(calPlanRecord r, int index) {
        if (r == null) return null;
        switch (index) {
            case 0: return r.Head;
            case 1: return r.Eyes;
            case 2: return r.Ears;
            case 3: return r.Nose;
            case 4: return r.Throat;
            case 5: return r.Teeth;
            case 6: return r.Stomach;
            case 7: return r.Intestines;
            case 8: return r.Liver;
            case 9: return r.Kidneys;
            case 10: return r.Heart;
            case 11: return r.Lungs;
            case 12: return r.Pressure;
            case 13: return r.Sleep;
            case 14: return r.Weight;
            case 15: return r.Nervous;
            case 16: return r.Morality;
            case 17: return r.Skin;
            case 18: return r.Steps;
            case 19: return r.FoodWeight;
            case 20: return r.DrinkValue;
            case 21: return r.Kallory;
            default: return null;
        }
    }

    // =====================================================================
    // Fill / save
    // =====================================================================

    private void populateFields() {
        okdateValue = (record != null && record.Okdate != null) ? record.Okdate : activeDate;
        textViewOkdate.setText(DISPLAY_DATE_TIME.format(okdateValue));

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
                record.LastUpdatedDate != null ? DISPLAY_DATE_TIME.format(record.LastUpdatedDate) : "");
        textViewEndDate.setText(record.EndDate != null ? DISPLAY_DATE_TIME.format(record.EndDate) : "");
        textViewHoldDate.setText(record.HoldDate != null ? DISPLAY_DATE_TIME.format(record.HoldDate) : "");

        // Task 41/45: числовые поля Health на карточке ("Голова".."Каллории")
        if (showHealthNumbers()) {
            for (int i = 0; i < HEALTH_NUMBER_NAMES.length; i++) {
                setHealthNumber(i, getHealthRecordValue(record, i));
            }
        }
    }

    private void saveAndFinish() {
        String form = selectedFormValue();
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

        // Task 48: для Project/Task/Request даты завершения/откладывания проставляются по состоянию.
        if ("Project".equals(form) || "Task".equals(form) || "Request".equals(form)) {
            if ("Выполнено".equals(record.Status) || "Отменено".equals(record.Status)) {
                record.EndDate = new Date();      // дата завершения проекта (факт) - сегодня со временем
                record.HoldDate = null;
            } else if ("Отложено".equals(record.Status)) {
                record.HoldDate = new Date();     // дата откладывания проекта - сегодня со временем
                record.EndDate = null;
            } else {
                record.EndDate = null;
                record.HoldDate = null;
            }
        }

        // Task 48/49: последний изменивший и дата/время обновления на каждом сохранении
        record.LastUpdatedDate = new Date();
        record.LastUpdatedBy = ManageSQLDatabase.AuthorName;
        record.LastUpdatedByID = ManageSQLDatabase.AuthorID;

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

        // Task 41/45: числовые поля Health (Голова/Глаза/.../Каллории)
        if (showHealthNumbers()) {
            for (int i = 0; i < HEALTH_NUMBER_NAMES.length; i++) {
                putHealthRecordValue(record, i, healthNumberValue(i));
            }
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("calPlanRecord", record);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /** "Проекты"-mode display label for a Form value (Project/Task/Request). */
    private String formLabelFor(String form) {
        if ("Project".equals(form)) return PROJECT_FORM_LABELS[0];
        if ("Task".equals(form)) return PROJECT_FORM_LABELS[1];
        if ("Request".equals(form)) return PROJECT_FORM_LABELS[2];
        return form;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Task 100: forward the attachment picker result to InfoFieldView
        InfoFieldView.onHostActivityResult(requestCode, resultCode, data);
    }

    /**
     * Form value read from the spinner.
     * In "Проекты"-mode the spinner shows "Проекты"/"Задачи"/"Заявка на автоматизацию"
     * and the value is mapped back to Project/Task/Request before saving.
     */
    private String selectedFormValue() {
        if (projectsFormMode) {
            int pos = spinnerForm.getSelectedItemPosition();
            if (pos >= 0 && pos < PROJECT_FORM_VALUES.length) return PROJECT_FORM_VALUES[pos];
        }
        return spinnerForm.getSelectedItem().toString();
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