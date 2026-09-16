package com.example.calendar4;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Task 138: универсальный экран списков Проектов/Задач/Заявок (объединил бывшие
 * ProjectsActivity и ProjectTasksActivity). Режим задаётся Intent-параметрами:
 * - без extras (или EXTRA_WORK_MODE=false) - "Проекты\Все" (Project/Task/Request);
 * - EXTRA_WORK_MODE=true - "Проекты\Рабочие" (только статусы Inwork/Intest);
 * - EXTRA_SOURCE_FORM="Project" + EXTRA_SOURCE_UNID - задачи проекта ("Для проекта:");
 * - EXTRA_SOURCE_FORM="Request" + EXTRA_SOURCE_NAME - проекты и задачи заявки ("Для заявки:").
 * Фабрики allIntent/projectIntent/requestIntent/linkedIntent дают гибкое управление параметрами.
 * Однотипные строки (MessageListItem): иконка статуса, Edit/Delete, поиск от 3 символов.
 */
public class ProjectsActivity extends BaseScreenActivity {

    // Режим "Проекты\Рабочие" (vs "Проекты\Все")
    public static final String EXTRA_WORK_MODE = "extra_work_mode";
    // Связанный режим: "Project" - задачи проекта, "Request" - проекты и задачи заявки
    public static final String EXTRA_SOURCE_FORM = "sourceForm";
    public static final String EXTRA_SOURCE_UNID = "sourceUnid";
    public static final String EXTRA_SOURCE_NAME = "sourceName";

    public static final String FORM_PROJECT = "Project";
    public static final String FORM_TASK = "Task";
    public static final String FORM_REQUEST = "Request";

    private ListView listView;
    private EditText editTextFilter;
    private ImageButton btnNew;
    private ImageButton btnBack;
    private TextView textViewTitle;

    private ManageSQLDatabase owerDb;
    private ArrayAdapter<calPlanRecord> adapter;
    private final ArrayList<calPlanRecord> allRecords = new ArrayList<>();

    private boolean workMode = false;
    private String sourceForm; // null = режимы "Все"/"Рабочие"
    private String sourceUnid;
    private String sourceName = "";

    // Перезагрузка списка после закрытия карточки добавления/редактирования
    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    calPlanRecord record = (calPlanRecord) result.getData().getSerializableExtra("calPlanRecord");
                    if (record != null && owerDb != null) owerDb.upsertCalPlan(record);
                }
                reload();
            });

    // Сортировка по дате старта (без даты - в конец)
    private static final Comparator<calPlanRecord> BY_START_DATE = new Comparator<calPlanRecord>() {
        @Override
        public int compare(calPlanRecord a, calPlanRecord b) {
            Date da = a != null ? a.StartDate : null;
            Date db = b != null ? b.StartDate : null;
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return da.compareTo(db);
        }
    };

    /** Intent "Проекты\Все" (workOnly=false) или "Проекты\Рабочие" (workOnly=true). */
    public static Intent allIntent(Context context, boolean workOnly) {
        return new Intent(context, ProjectsActivity.class).putExtra(EXTRA_WORK_MODE, workOnly);
    }

    /** Intent списка задач проекта: "Для проекта: <projectName>". */
    public static Intent projectIntent(Context context, String projectUnid, String projectName) {
        return linkedIntent(context, FORM_PROJECT, projectUnid, projectName);
    }

    /** Intent списка проектов и задач заявки: "Для заявки: <requestName>". */
    public static Intent requestIntent(Context context, String requestName) {
        return linkedIntent(context, FORM_REQUEST, null, requestName);
    }

    /** Универсальная фабрика связанного списка: form = "Project" | "Request". */
    public static Intent linkedIntent(Context context, String form, String unid, String name) {
        Intent intent = new Intent(context, ProjectsActivity.class);
        intent.putExtra(EXTRA_SOURCE_FORM, form);
        if (unid != null) intent.putExtra(EXTRA_SOURCE_UNID, unid);
        if (name != null) intent.putExtra(EXTRA_SOURCE_NAME, name);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        bindViews();
        readParams();
        updateTitle();
        owerDb = ManageSQLDatabase.getInstance(this);
        reload();
        setupFilter();
        setupAdapter();
        setupButtons();
    }

    private void bindViews() {
        listView = findViewById(R.id.listViewProjects);
        editTextFilter = findViewById(R.id.editTextFilterProjects);
        btnNew = findViewById(R.id.btnNew);
        btnBack = findViewById(R.id.btnBack);
        textViewTitle = findViewById(R.id.textViewProjectsTitle);
    }

    /** Чтение входных параметров: связанный режим (sourceForm) приоритетнее EXTRA_WORK_MODE. */
    private void readParams() {
        Intent intent = getIntent();
        if (intent == null) return;
        String form = intent.getStringExtra(EXTRA_SOURCE_FORM);
        if (FORM_PROJECT.equals(form) || FORM_REQUEST.equals(form)) {
            sourceForm = form;
            sourceUnid = intent.getStringExtra(EXTRA_SOURCE_UNID);
            String name = intent.getStringExtra(EXTRA_SOURCE_NAME);
            if (name != null) sourceName = name;
        } else {
            workMode = intent.getBooleanExtra(EXTRA_WORK_MODE, false);
        }
    }

    /** Титл по режиму (Task 118/124); ActionBar-титл остаётся "Календарный План". */
    private void updateTitle() {
        if (textViewTitle == null) return;
        if (FORM_REQUEST.equals(sourceForm)) {
            textViewTitle.setText("Для заявки: " + sourceName);
        } else if (FORM_PROJECT.equals(sourceForm)) {
            textViewTitle.setText("Для проекта: " + sourceName);
        } else {
            textViewTitle.setText(workMode ? "Проекты\\Рабочие" : "Проекты\\Все");
        }
    }

    // Поиск по названию: 3+ символа фильтруют список (как на других экранах)
    private void setupFilter() {
        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                reload();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupAdapter() {
        adapter = new ArrayAdapter<calPlanRecord>(this, 0, allRecords) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                MessageListItem row;
                if (convertView instanceof MessageListItem) {
                    row = (MessageListItem) convertView;
                } else {
                    row = new MessageListItem(ProjectsActivity.this);
                }
                final calPlanRecord record = allRecords.get(position);
                row.setTopText(record.Name != null ? record.Name : "");
                row.setBottomText(bottomLine(record));
                row.setTypeIcon(StatusIconFactory.getStatusDrawable(
                        ProjectsActivity.this, record.Form, record.StatusID, record.Status));
                row.setOnEditClickListener(v -> openRecord(record));
                row.setOnDeleteClickListener(v -> confirmDelete(record));
                row.setPosition(position);
                return row;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(null);
    }

    private void setupButtons() {
        btnNew.setOnClickListener(v -> addRecord());
        btnBack.setOnClickListener(v -> finish());
    }

    /** Добавление: режимы Все/Рабочие - диалог выбора типа; связанные - тип по источнику. */
    private void addRecord() {
        if (sourceForm == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Добавить")
                    .setItems(new String[]{"Проект", "Задача", "Заявка"},
                            (d, which) -> addDialogItem(which))
                    .setNegativeButton("Cancel", null)
                    .show();
        } else if (FORM_REQUEST.equals(sourceForm)) {
            openNewCard(InputCalPlanActivity.class, FORM_PROJECT, null, sourceName);
        } else {
            openNewCard(TaskActivity.class, FORM_TASK, sourceUnid, sourceName);
        }
    }

    /** Task 126: тип новой записи из диалога (Проект/Задача/Заявка). */
    private void addDialogItem(int which) {
        if (which == 1) openNewCard(TaskActivity.class, FORM_TASK, null, null);
        else openNewCard(InputCalPlanActivity.class,
                which == 2 ? FORM_REQUEST : FORM_PROJECT, null, null);
    }

    /** Открыть карточку новой записи form; предвыбор привязки к проекту/заявке. */
    private void openNewCard(Class<?> cardClass, String form, String projectUnid, String requestName) {
        Intent intent = new Intent(this, cardClass);
        intent.putExtra(BaseCalPlanEditActivity.EXTRA_PRESELECT_FORM, form);
        if (projectUnid != null) intent.putExtra("preselectProjectUNID", projectUnid);
        if (requestName != null) intent.putExtra("preselectRequestName", requestName);
        intent.putExtra("activeDate", new Date());
        // Task 35: Form picker ограничен Проекты/Задачи/Заявка на автоматизацию
        intent.putExtra(BaseCalPlanEditActivity.EXTRA_PROJECTS_FORM_MODE, true);
        editLauncher.launch(intent);
    }

    /** Открыть карточку записи: Задача - TaskActivity, Проект/Заявка - InputCalPlanActivity. */
    private void openRecord(calPlanRecord record) {
        if (record == null) return;
        Class<?> cls = FORM_TASK.equals(record.Form) ? TaskActivity.class : InputCalPlanActivity.class;
        Intent intent = new Intent(this, cls);
        intent.putExtra("activeDate", new Date());
        intent.putExtra("calPlanRecord", record);
        intent.putExtra(BaseCalPlanEditActivity.EXTRA_PROJECTS_FORM_MODE, true);
        editLauncher.launch(intent);
    }

    private void confirmDelete(final calPlanRecord record) {
        if (record == null || record.id == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Удалить")
                .setMessage("Удалить запись? (" + (record.Name != null ? record.Name : "") + ")")
                .setPositiveButton("Ок", (d, w) -> {
                    owerDb.deleteCalPlanRecord(record);
                    reload();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reload() {
        allRecords.clear();
        String filter = editTextFilter != null ? editTextFilter.getText().toString().trim() : "";
        calPlanRecord[] arr = collectRecords(filter);
        if (arr != null) {
            for (calPlanRecord record : arr) {
                if (record != null && matchesFilter(record, filter)) allRecords.add(record);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    /** Данные списка по режиму: Все/Рабочие, задачи проекта, проекты+задачи заявки. */
    private calPlanRecord[] collectRecords(String filter) {
        if (FORM_REQUEST.equals(sourceForm)) return collectRequestRecords();
        if (FORM_PROJECT.equals(sourceForm)) return owerDb.getTasksByProject(sourceUnid);
        return owerDb.getProjectsTasks(filter, workMode);
    }

    // Заявка -> её проекты + задачи каждого проекта, по дате старта
    private calPlanRecord[] collectRequestRecords() {
        try {
            ArrayList<calPlanRecord> list = new ArrayList<>();
            calPlanRecord[] projects = owerDb.getProjectsByRequestName(sourceName);
            if (projects != null) {
                for (calPlanRecord p : projects) {
                    if (p == null) continue;
                    list.add(p);
                    addProjectTasks(list, p.UNID);
                }
            }
            Collections.sort(list, BY_START_DATE);
            return list.toArray(new calPlanRecord[0]);
        } catch (Exception e) {
            return new calPlanRecord[0];
        }
    }

    private void addProjectTasks(ArrayList<calPlanRecord> list, String projectUnid) {
        calPlanRecord[] tasks = owerDb.getTasksByProject(projectUnid);
        if (tasks == null) return;
        for (calPlanRecord t : tasks) {
            if (t != null) list.add(t);
        }
    }

    // Фильтр по названию только в связанных режимах; в Все/Рабочие фильтрует SQL
    private boolean matchesFilter(calPlanRecord record, String filter) {
        if (sourceForm == null || filter.length() < 3) return true;
        String name = record.Name != null ? record.Name.toLowerCase(Locale.getDefault()) : "";
        return name.contains(filter.toLowerCase(Locale.getDefault()));
    }

    // Нижняя строка: связанные режимы - тип/дата, иначе дата или начало текста
    private String bottomLine(calPlanRecord record) {
        if (sourceForm == null) return displayStartDate(record);
        return linkedBottomLine(record);
    }

    /** Нижняя строка связанного режима: тип (Проект/Задача), заявка для задач, дата старта. */
    private String linkedBottomLine(calPlanRecord record) {
        if (record == null) return "";
        StringBuilder sb = new StringBuilder();
        if (FORM_TASK.equals(record.Form)) {
            sb.append("Задача");
            if (record.RequestName != null && !record.RequestName.trim().isEmpty()) {
                sb.append(" \u00b7 ").append(record.RequestName.trim());
            }
        } else if (FORM_PROJECT.equals(record.Form)) {
            sb.append("Проект");
        }
        if (record.StartDate != null) {
            if (sb.length() > 0) sb.append("  ");
            sb.append(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(record.StartDate));
        }
        return sb.toString();
    }

    // StartDate (форматированный) или сокращённый BodyText, если даты нет
    private String displayStartDate(calPlanRecord record) {
        if (record != null && record.StartDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return sdf.format(record.StartDate);
        }
        return shortBodyText(InfoFieldView.plainText(record != null ? record.BodyText : null));
    }

    private String shortBodyText(String bodyText) {
        if (bodyText == null) return "";
        String text = bodyText.trim();
        int newline = text.indexOf('\n');
        if (newline >= 0) text = text.substring(0, newline).trim();
        if (text.length() > 30) text = text.substring(0, 30);
        return text;
    }

    @Override
    public void onBackPressed() {
        // Как стартовая страница (вместо MainActivity): системный "Back" закрывает приложение.
        // Внутренняя кнопка "X" возвращает на MainActivity через finish().
        if (getIntent() != null && getIntent().getBooleanExtra(CalParamRecord.EXTRA_IS_START_PAGE, false)) {
            finishAndRemoveTask();
        } else {
            super.onBackPressed();
        }
    }
}
