package com.example.calendar4;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.app.Activity;
import android.app.AlertDialog;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Task 124: список, связанный с карточкой проекта или заявки.
 * Из карточки Проекта - список задач проекта; из карточки Заявки - проекты,
 * привязанные к заявке, и задачи этих проектов. Заголовок "Для проекта:/Для заявки:",
 * кнопки назад и добавить, фильтр, список MessageListItem.
 */
public class ProjectTasksActivity extends BaseScreenActivity {

    public static final String EXTRA_SOURCE_FORM = "sourceForm"; // "Project" | "Request"
    public static final String EXTRA_SOURCE_UNID = "sourceUnid";
    public static final String EXTRA_SOURCE_NAME = "sourceName";

    private ListView listViewTasks;
    private EditText editTextFilter;
    private ImageButton btnNew;
    private ImageButton btnBack;
    private TextView textViewTitle;

    private ManageSQLDatabase owerDb;
    private ArrayAdapter<calPlanRecord> adapter;
    private final ArrayList<calPlanRecord> allRecords = new ArrayList<>();

    private String sourceForm = "Project";
    private String sourceUnid;
    private String sourceName = "";

    // Перезагрузка списка после закрытия карточки добавления/редактирования
    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    calPlanRecord record = (calPlanRecord) result.getData().getSerializableExtra("calPlanRecord");
                    if (record != null && owerDb != null) {
                        owerDb.upsertCalPlan(record);
                    }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_tasks);

        listViewTasks = findViewById(R.id.listViewTasks);
        editTextFilter = findViewById(R.id.editTextFilterTasks);
        btnNew = findViewById(R.id.btnNew);
        btnBack = findViewById(R.id.btnBack);
        textViewTitle = findViewById(R.id.textViewTasksTitle);

        Intent intent = getIntent();
        if (intent != null) {
            String f = intent.getStringExtra(EXTRA_SOURCE_FORM);
            if (f != null) sourceForm = f;
            sourceUnid = intent.getStringExtra(EXTRA_SOURCE_UNID);
            String n = intent.getStringExtra(EXTRA_SOURCE_NAME);
            if (n != null) sourceName = n;
        }
        updateTitle();

        owerDb = ManageSQLDatabase.getInstance(this);
        reload();

        // Фильтр по названию: 3+ символов (как на других экранах со списком)
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
        adapter = new ArrayAdapter<calPlanRecord>(this, 0, allRecords) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                MessageListItem row;
                if (convertView instanceof MessageListItem) {
                    row = (MessageListItem) convertView;
                } else {
                    row = new MessageListItem(ProjectTasksActivity.this);
                }
                final calPlanRecord record = allRecords.get(position);
                row.setTopText(record.Name != null ? record.Name : "");
                row.setBottomText(bottomLine(record));
                row.setTypeIcon(StatusIconFactory.getStatusDrawable(
                        ProjectTasksActivity.this, record.Form, record.StatusID, record.Status));
                row.setOnEditClickListener(v -> openRecord(record));
                row.setOnDeleteClickListener(v -> confirmDelete(record));
                row.setPosition(position);
                return row;
            }
        };
        listViewTasks.setAdapter(adapter);
        listViewTasks.setOnItemClickListener(null);

        btnNew.setOnClickListener(v -> addRecord());
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateTitle() {
        String prefix = "Request".equals(sourceForm) ? "Для заявки: " : "Для проекта: ";
        if (textViewTitle != null) textViewTitle.setText(prefix + sourceName);
    }

    private void reload() {
        allRecords.clear();
        String filter = editTextFilter != null ? editTextFilter.getText().toString().trim() : "";
        calPlanRecord[] arr = collectRecords();
        if (arr != null) {
            for (calPlanRecord record : arr) {
                if (record == null) continue;
                if (filter.length() >= 3) {
                    String name = record.Name != null
                            ? record.Name.toLowerCase(Locale.getDefault()) : "";
                    if (!name.contains(filter.toLowerCase(Locale.getDefault()))) continue;
                }
                allRecords.add(record);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    /** Данные списка: задачи проекта или проекты+задачи заявки. */
    private calPlanRecord[] collectRecords() {
        try {
            if ("Request".equals(sourceForm)) {
                ArrayList<calPlanRecord> list = new ArrayList<>();
                calPlanRecord[] projects = owerDb.getProjectsByRequestName(sourceName);
                if (projects != null) {
                    for (calPlanRecord p : projects) {
                        if (p == null) continue;
                        list.add(p);
                        calPlanRecord[] tasks = owerDb.getTasksByProject(p.UNID);
                        if (tasks != null) {
                            for (calPlanRecord t : tasks) {
                                if (t != null) list.add(t);
                            }
                        }
                    }
                }
                Collections.sort(list, BY_START_DATE);
                return list.toArray(new calPlanRecord[0]);
            }
            return owerDb.getTasksByProject(sourceUnid);
        } catch (Exception e) {
            return new calPlanRecord[0];
        }
    }

    private void addRecord() {
        Intent intent;
        if ("Request".equals(sourceForm)) {
            // Заявка -> новый Проект, привязанный к заявке
            intent = new Intent(ProjectTasksActivity.this, InputCalPlanActivity.class);
            intent.putExtra(BaseCalPlanEditActivity.EXTRA_PRESELECT_FORM, "Project");
            intent.putExtra("preselectRequestName", sourceName);
        } else {
            // Проект -> новая Задача в этом проекте
            intent = new Intent(ProjectTasksActivity.this, TaskActivity.class);
            intent.putExtra(BaseCalPlanEditActivity.EXTRA_PRESELECT_FORM, "Task");
            intent.putExtra("preselectProjectUNID", sourceUnid);
            intent.putExtra("preselectRequestName", sourceName);
        }
        intent.putExtra("activeDate", new Date());
        intent.putExtra(BaseCalPlanEditActivity.EXTRA_PROJECTS_FORM_MODE, true);
        editLauncher.launch(intent);
    }

    private void openRecord(calPlanRecord record) {
        if (record == null) return;
        Class<?> cls = "Task".equals(record.Form) ? TaskActivity.class : InputCalPlanActivity.class;
        Intent intent = new Intent(ProjectTasksActivity.this, cls);
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

    /** Нижняя строка: тип (Проект/Задача), проект для задач, дата старта. */
    private String bottomLine(calPlanRecord record) {
        if (record == null) return "";
        StringBuilder sb = new StringBuilder();
        if ("Task".equals(record.Form)) {
            sb.append("Задача");
            if (record.RequestName != null && !record.RequestName.trim().isEmpty()) {
                sb.append(" \u00b7 ").append(record.RequestName.trim());
            }
        } else if ("Project".equals(record.Form)) {
            sb.append("Проект");
        }
        if (record.StartDate != null) {
            if (sb.length() > 0) sb.append("  ");
            sb.append(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(record.StartDate));
        }
        return sb.toString();
    }
}