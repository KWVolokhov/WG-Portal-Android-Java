package com.example.calendar4;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * "Проекты \ Все" screen: lists all CALPLAN records with Form='Project' and Form='Task',
 * sorted by StartDate (earliest first). Same look as the contacts list:
 * two-line rows with per-row Edit/Delete buttons. The search field filters by Name
 * when 3 or more characters are typed. "Add" opens a new Project card.
 */
public class ProjectsActivity extends Activity {

    private ListView listViewProjects;
    private EditText editTextFilter;
    private ImageButton btnNew;
    private ImageButton btnBack;

    private ManageSQLDatabase owerDb;
    private ArrayAdapter<calPlanRecord> adapter;
    private final ArrayList<calPlanRecord> allProjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        listViewProjects = findViewById(R.id.listViewProjects);
        editTextFilter = findViewById(R.id.editTextFilterProjects);
        btnNew = findViewById(R.id.btnNew);
        btnBack = findViewById(R.id.btnBack);

        //owerDb = new ManageSQLDatabase(this);
		owerDb = ManageSQLDatabase.getInstance(this);

        reload();

        // Search by name: 3+ characters filter the list (same as the contacts screen)
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

        adapter = new ArrayAdapter<calPlanRecord>(this, 0, allProjects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TwoLineListItem row;
                if (convertView instanceof TwoLineListItem) {
                    row = (TwoLineListItem) convertView;
                } else {
                    row = new TwoLineListItem(ProjectsActivity.this);
                }
                final calPlanRecord record = allProjects.get(position);
                row.setTopText(record.Name != null ? record.Name : "");
                row.setBottomText(displayStartDate(record));
                row.setTypeIcon("Task".equals(record.Form) ? R.drawable.ic_type_task : R.drawable.ic_type_project);
                row.setOnEditClickListener(v -> openProject(record));
                row.setOnDeleteClickListener(v -> confirmDeleteProject(record));
                row.setPosition(position);
                return row;
            }
        };
        listViewProjects.setAdapter(adapter);
        listViewProjects.setOnItemClickListener(null);

        // Add button - open a brand-new Project card
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectsActivity.this, InputCalPlanActivity.class);
                intent.putExtra("activeDate", new Date());
                startActivityForResult(intent, 1);
            }
        });

        // Back button - close activity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    private void openProject(calPlanRecord project) {
        if (project == null) return;
        // Tasks open the Task card, everything else (Project/Request) opens the Project card
        Class<?> cls = "Task".equals(project.Form) ? TaskActivity.class : InputCalPlanActivity.class;
        Intent intent = new Intent(ProjectsActivity.this, cls);
        intent.putExtra("activeDate", new Date());
        intent.putExtra("calPlanRecord", project);
        startActivityForResult(intent, 1);
    }

    private void confirmDeleteProject(final calPlanRecord project) {
        if (project == null || project.id == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Удалить")
                .setMessage("Удалить запись? (" + (project.Name != null ? project.Name : "") + ")")
                .setPositiveButton("Ок", (d, w) -> {
                    owerDb.deleteCalPlanRecord(project);
                    reload();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reload() {
        allProjects.clear();
        String filter = editTextFilter != null ? editTextFilter.getText().toString().trim() : "";
        calPlanRecord[] arr = owerDb.getProjectsTasks(filter);
        if (arr != null) {
            for (calPlanRecord record : arr) {
                if (record != null) allProjects.add(record);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                calPlanRecord record = (calPlanRecord) data.getSerializableExtra("calPlanRecord");
                if (record != null && owerDb != null) {
                    owerDb.upsertCalPlan(record);
                }
            }
            reload();
        }
    }

    // Bottom line: StartDate (formatted) or shortened BodyText if no date
    private String displayStartDate(calPlanRecord record) {
        if (record != null && record.StartDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return sdf.format(record.StartDate);
        }
        return shortBodyText(record != null ? record.BodyText : null);
    }

    private String shortBodyText(String bodyText) {
        if (bodyText == null) return "";
        String text = bodyText.trim();
        int newline = text.indexOf('\n');
        if (newline >= 0) text = text.substring(0, newline).trim();
        if (text.length() > 30) text = text.substring(0, 30);
        return text;
    }
}