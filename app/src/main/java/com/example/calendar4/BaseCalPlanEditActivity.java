package com.example.calendar4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
public abstract class BaseCalPlanEditActivity extends BaseScreenActivity {

    protected static final String[] FORM_VALUES = {"Project", "Note", "Remember", "Task",
            "History", "HealthEat", "HealthDrink", "HealthSport", "HealthStress", "HealthJoy"};

    // Task 35: when the card is opened from the "Проекты" screen (Add / edit), the Form
    // picker offers only Проекты/Задачи/Заявка на автоматизацию (Project/Task/Request).
    public static final String EXTRA_PROJECTS_FORM_MODE = "extra_projects_form_mode";
    // Task 114: предвыбранная Form для НОВОЙ записи (например "Request" из диалога Добавить).
    public static final String EXTRA_PRESELECT_FORM = "extra_preselect_form";
    protected static final String[] PROJECT_FORM_LABELS = {"Проекты", "Задачи", "Заявку на проект"};
    protected static final String[] PROJECT_FORM_VALUES = {"Project", "Task", "Request"};
    protected static final String[] STATUS_LABELS = {"Черновик", "В работе", "Тестирование",
            "Выполнено", "Отменено", "Отложено"};
    protected static final String[] STATUS_IDS = {"Draft", "Inwork", "Intest",
            "Done", "Canceled", "Hold"};
    protected static final String[] MAIN_SYSTEMS = {"Lotus(HCL)", "VBA", "Java", "JavaScriptServer",
            "JavaScript", "SQL", "Busines", "ArtDesign", "Combo"};

    protected static final SimpleDateFormat DISPLAY_DATE =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    // Task 44/115: дата создания/обновления/завершения показывается вместе со временем (с секундами).
    protected static final SimpleDateFormat DISPLAY_DATE_TIME =
            new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

    // =====================================================================
    // Form configuration (override in subclasses)
    // =====================================================================

    protected String getFormType() { return "Project"; }
    protected String getStartDateLabel() { return "Дата старта проекта:"; }
    protected String getBodyTextLabel() { return "Описание задачи:"; }
    protected String getRequestNameLabel() { return "Заявку на проект:"; }
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

    // Task 45: подписи полей "Голова".."Каллории" - в CalPlanHealthNumbers (Task 139)

    // ----- views -----
    protected Spinner spinnerForm, spinnerStatus, spinnerMainSystem, spinnerAnalitik, spinnerExector;
    protected EditText editTextName, editTextPriority, editTextRequestName,
            editTextComment, editTextInstallOrder, editTextKeyWords;
    protected DateFieldView dateFieldStartDate;
    protected InfoFieldView infoBodyText;
    protected TextView textViewOkdate, textViewLastUpdatedBy, textViewLastUpdatedDate,
            textViewEndDate, textViewHoldDate, textViewAuthorName;
    protected ImageButton btnOK, btnCancel, btnPickRequest, btnTasks, btnRework;

    protected View rowStatus, rowMainSystem, rowPriority, rowStartDate, rowRequestName,
            rowAnalitik, rowExector, rowInstallOrder, rowKeyWords, rowLastUpdatedBy,
            rowEndDate, rowHoldDate;
    protected TextView labelStartDate, labelBodyText, labelRequestName, labelAuthorName,
            labelEndDate, labelHoldDate;

    // Task 41: контейнер числовых полей Health на карточке
    protected LinearLayout healthContainer;
    // Task 139: числовые поля Health ("Голова".."Каллории") вынесены в отдельный класс
    protected CalPlanHealthNumbers healthNumbers;

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
    // Task 124: предвыбранные проект (для задачи) / заявка (для проекта)
    protected String preselectProjectUNID;
    protected String preselectRequestName;

    /** Task 35: true when the card was opened from the "Проекты" screen (Form = Проекты/Задачи/Заявка). */
    private boolean projectsFormMode = false;

    // Task 114: предвыбранная Form для новой записи (для InputCalPlanActivity из диалога Добавить)
    protected String preselectFormValue;

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
        btnTasks = findViewById(R.id.btnTasks);
        btnRework = findViewById(R.id.btnRework);
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
        preselectFormValue = intent.getStringExtra(EXTRA_PRESELECT_FORM);
        preselectProjectUNID = intent.getStringExtra("preselectProjectUNID");
        preselectRequestName = intent.getStringExtra("preselectRequestName");

		owerDb = ManageSQLDatabase.getInstance(this);
        loadContacts();

        // Task 35: "Проекты"-mode restricts the Form picker to Проекты/Задачи/Заявка
        projectsFormMode = intent.getBooleanExtra(EXTRA_PROJECTS_FORM_MODE, false);

        applyConfig();

        setupHealthNumbers();

        if (projectsFormMode) {
            setupSpinner(spinnerForm, PROJECT_FORM_LABELS, formLabelFor(initialFormValue()));
        } else {
            setupSpinner(spinnerForm, FORM_VALUES, initialFormValue());
        }
        setupSpinner(spinnerStatus, STATUS_LABELS, record != null ? statusLabel(record) : null);
        setupSpinner(spinnerMainSystem, MAIN_SYSTEMS, record != null ? record.MainSystem : null);
        setupContactSpinner(spinnerAnalitik, record != null ? record.AnalitikName : null);
        setupContactSpinner(spinnerExector, record != null ? record.ExectorName : null);

        populateFields();

        // Task 44: на карточке HealthSportActivity показываем таймер запущенного шагомера
        setupPedometerTimer();

        setupTasksButton();

        // Task 131: кнопка "Переделать" (смена Form) на карточках Проекта/Задачи/Заявки
        setupReworkButton();

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

    protected void setupContactSpinner(Spinner spinner, String current) {
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

    /** Task 124: кнопка "Список задач" видна только на карточке Проекта/Заявки. */
    private void setupTasksButton() {
        if (btnTasks == null) return;
        String form = initialFormValue();
        boolean projectLike = "Project".equals(form) || "Request".equals(form);
        setRowVisible(btnTasks, projectLike);
        if (projectLike) btnTasks.setOnClickListener(v -> openTasksList(form));
    }

    /** Task 124: открывает список задач проекта (или проектов/задач заявки). Task 138. */
    private void openTasksList(String form) {
        String name = editTextName.getText().toString().trim();
        String unid = record != null ? record.UNID : null;
        startActivity(ProjectsActivity.linkedIntent(BaseCalPlanEditActivity.this, form, unid, name));
    }

    // =====================================================================
    // Task 131: кнопка "Переделать" (смена Form с переоткрытием карточки)
    // =====================================================================

    /** Кнопка "Переделать" видна только на карточках Проекта/Задачи/Заявки. */
    private void setupReworkButton() {
        if (btnRework == null) return;
        setRowVisible(btnRework, projectLikeForm(initialFormValue()));
        btnRework.setOnClickListener(v -> openReworkPicker());
    }

    /** Task 131: допустимые переделки - Заявка: Проект/Задача, Проект: Задача/Заявка, Задача: Проект/Заявка. */
    private void openReworkPicker() {
        String form = selectedFormValue();
        final String[] values;
        String[] labels;
        if ("Request".equals(form)) {
            values = new String[]{"Project", "Task"};
            labels = new String[]{PROJECT_FORM_LABELS[0], PROJECT_FORM_LABELS[1]};
        } else if ("Project".equals(form)) {
            values = new String[]{"Task", "Request"};
            labels = new String[]{PROJECT_FORM_LABELS[1], PROJECT_FORM_LABELS[2]};
        } else if ("Task".equals(form)) {
            values = new String[]{"Project", "Request"};
            labels = new String[]{PROJECT_FORM_LABELS[0], PROJECT_FORM_LABELS[2]};
        } else {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Переделать в:")
                .setItems(labels, (d, which) -> reworkTo(values[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Task 131: меняет Form записи и переоткрывает соответствующую карточку (Проект/Задача/Заявка). */
    private void reworkTo(String newForm) {
        if (!fillRecordFromUi()) return;
        record.Form = newForm;
        Class<?> cls = "Task".equals(newForm) ? TaskActivity.class : InputCalPlanActivity.class;
        Intent intent = new Intent(BaseCalPlanEditActivity.this, cls);
        intent.putExtra("activeDate", activeDate);
        intent.putExtra("calPlanRecord", record);
        intent.putExtra(EXTRA_PROJECTS_FORM_MODE, true);
        startActivity(intent);
        finish();
    }

    // =====================================================================
    // Task 41: числовые поля Health (Шаги/Вес еды/Объем питья/Каллории)
    // =====================================================================

    /** Builds the Health number fields (only when the subclass asks for them). Task 139. */
    private void setupHealthNumbers() {
        if (healthContainer == null) return;
        boolean visible = showHealthNumbers();
        setRowVisible(healthContainer, visible);
        if (!visible) return;
        if (healthNumbers == null) healthNumbers = new CalPlanHealthNumbers(this, healthContainer);
    }

    // Task 139: healthNumberValue/setHealthNumber - в CalPlanHealthNumbers

    // =====================================================================
    // Fill / save
    // =====================================================================

    /** Task 139: заполнение полей экрана из record вынесено в CalPlanCardMapper. */
    private void populateFields() {
        CalPlanCardMapper.populate(this);
    }

    /** Task 139: contactNameById/dateAtMidnight - в CalPlanCardMapper. */

    /** Task 139: чтение значений экрана в record вынесено в CalPlanCardMapper. */
    private boolean fillRecordFromUi() {
        return CalPlanCardMapper.fill(this);
    }

    private void saveAndFinish() {
        if (!fillRecordFromUi()) return;
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

    /** Task 122: Form проекта/задачи/заявки - для них показывается Постановщик. */
    private boolean projectLikeForm(String form) {
        return "Project".equals(form) || "Task".equals(form) || "Request".equals(form);
    }

    /** Task 114: Form для новой записи - предвыбранная из диалога либо тип класса. */
    private String initialFormValue() {
        if (record != null && record.Form != null) return record.Form;
        return preselectFormValue != null ? preselectFormValue : getFormType();
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
    protected String selectedFormValue() {
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