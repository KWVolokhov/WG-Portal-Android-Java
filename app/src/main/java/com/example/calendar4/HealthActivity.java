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
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Day Health screen: shows HEALTHPLAN records (Form=HealthEat / HealthDrink / HealthSport)
 * for a concrete day. List looks like the projects/contacts list (rows with edit/delete)
 * plus a search field that filters by name when 3+ characters are typed.
 * Header: "&lt;дата&gt; Health" (left) + "Add" + Back ImageButton (right-most).
 */
public class HealthActivity extends Activity {

    private ListView listViewHealth;
    private EditText editTextFilter;
    private TextView headerTitle;
    private ImageButton btnNew;
    private ImageButton btnBack;

    private HealthSQLManage healthDb;
    private ArrayAdapter<healthPlanRecord> adapter;
    private ArrayList<healthPlanRecord> records;
    private Date day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);

        listViewHealth = findViewById(R.id.listViewHealth);
        editTextFilter = findViewById(R.id.editTextHealthFilter);
        headerTitle = findViewById(R.id.headerTitle);
        btnNew = findViewById(R.id.btnNew);
        btnBack = findViewById(R.id.btnBack);

        day = (Date) getIntent().getSerializableExtra("healthDate");
        if (day == null) day = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        headerTitle.setText(sdf.format(day) + " Health");

        // Reuse the already existing connection to the database
        healthDb = new HealthSQLManage(ManageSQLDatabase.getInstance(this).getWritableDatabase());
        records = new ArrayList<>();

        adapter = new ArrayAdapter<healthPlanRecord>(this, 0, records) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TwoLineListItem row;
                if (convertView instanceof TwoLineListItem) {
                    row = (TwoLineListItem) convertView;
                } else {
                    row = new TwoLineListItem(HealthActivity.this);
                }
                final healthPlanRecord record = getItem(position);
                row.setTopText(record.Name != null ? record.Name : "");
                row.setBottomText(shortBodyText(record.BodyText));
                row.setTypeIcon(iconForForm(record.Form));
                row.setOnEditClickListener(v -> openEdit(record));
                row.setOnDeleteClickListener(v -> confirmDelete(record));
                row.setPosition(position);
                return row;
            }
        };
        listViewHealth.setAdapter(adapter);
        listViewHealth.setOnItemClickListener(null);

        reload();

        // Search by name: 3+ characters filter the list
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

        btnNew.setOnClickListener(v -> chooseTypeAndAdd());
        btnBack.setOnClickListener(v -> finish());
    }

    private void reload() {
        records.clear();
        String filter = editTextFilter != null ? editTextFilter.getText().toString().trim() : "";
        healthPlanRecord[] arr = healthDb.getHealthByDate(day, filter);
        if (arr != null) {
            for (healthPlanRecord r : arr) {
                if (r != null) records.add(r);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void openEdit(healthPlanRecord record) {
        if (record == null) return;
        Intent intent = new Intent(this, activityForForm(record.Form));
        intent.putExtra("activeDate", day);
        intent.putExtra("calPlanRecord", HealthSQLManage.toCalPlan(record));
        startActivityForResult(intent, 1);
    }

    private void confirmDelete(final healthPlanRecord record) {
        if (record == null || record.id == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Удалить")
                .setMessage("Удалить запись? (" + (record.Name != null ? record.Name : "") + ")")
                .setPositiveButton("Ок", (d, w) -> {
                    healthDb.deleteHealth(record.id);
                    reload();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void chooseTypeAndAdd() {
        new AlertDialog.Builder(this)
                .setTitle("Добавить запись здоровья")
                .setItems(new String[]{"Питание (HealthEat)", "Питьё (HealthDrink)", "Спорт (HealthSport)"},
                        (d, which) -> {
                            String form = which == 0 ? "HealthEat" : which == 1 ? "HealthDrink" : "HealthSport";
                            Intent intent = new Intent(HealthActivity.this, activityForForm(form));
                            intent.putExtra("activeDate", day);
                            startActivityForResult(intent, 1);
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                calPlanRecord record = (calPlanRecord) data.getSerializableExtra("calPlanRecord");
                if (record != null) {
                    healthDb.upsertHealth(record);
                }
            }
            reload();
        }
    }

    private int iconForForm(String form) {
        if ("HealthDrink".equals(form)) return R.drawable.ic_type_health_drink;
        if ("HealthSport".equals(form)) return R.drawable.ic_type_health_sport;
        return R.drawable.ic_type_health_eat;
    }

    private Class<?> activityForForm(String form) {
        if ("HealthDrink".equals(form)) return HealthDrinkActivity.class;
        if ("HealthSport".equals(form)) return HealthSportActivity.class;
        return HealthEatActivity.class;
    }

    // Second text line: first line of BodyText or no more than 20 characters
    private String shortBodyText(String bodyText) {
        if (bodyText == null) return "";
        String text = bodyText.trim();
        int newline = text.indexOf('\n');
        if (newline >= 0) text = text.substring(0, newline).trim();
        if (text.length() > 20) text = text.substring(0, 20);
        return text;
    }
}