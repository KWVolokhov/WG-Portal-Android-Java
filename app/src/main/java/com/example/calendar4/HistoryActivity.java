package com.example.calendar4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Day history screen: shows History records (their own HISTORY table) for a concrete day,
 * like the contacts list (rows with edit/delete), but without the "Новый" button.
 * Header: "&lt;дата&gt; История" (left) + Back ImageButton like Cancel (right-most).
 */
public class HistoryActivity extends Activity {

    private ListView listViewHistory;
    private TextView headerTitle;
    private ImageButton btnBack;

    private ManageSQLDatabase owerDb;
    private HistorySQLManage historyDb;
    private ArrayAdapter<calPlanRecord> adapter;
    private ArrayList<calPlanRecord> records;
    private Date day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listViewHistory = findViewById(R.id.listViewHistory);
        headerTitle = findViewById(R.id.headerTitle);
        btnBack = findViewById(R.id.btnBack);

        day = (Date) getIntent().getSerializableExtra("historyDate");
        if (day == null) day = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        headerTitle.setText(sdf.format(day) + " История");

        owerDb = new ManageSQLDatabase(this);
        // Reuse the already existing connection to the database
        historyDb = new HistorySQLManage(owerDb.getWritableDatabase());
        records = new ArrayList<>();
        calPlanRecord[] arr = historyDb.getHistoryByDate(day);
        if (arr != null) {
            for (calPlanRecord r : arr) {
                if (r != null) records.add(r);
            }
        }

        adapter = new ArrayAdapter<calPlanRecord>(this, 0, records) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TwoLineListItem row;
                if (convertView instanceof TwoLineListItem) {
                    row = (TwoLineListItem) convertView;
                } else {
                    row = new TwoLineListItem(HistoryActivity.this);
                }
                final calPlanRecord record = getItem(position);
                row.setTopText(record.Name != null ? record.Name : "");
                row.setBottomText(shortBodyText(record.BodyText));
                row.setTypeIcon(R.drawable.ic_type_history);
                row.setOnEditClickListener(v -> openEdit(record));
                row.setOnDeleteClickListener(v -> confirmDelete(record));
                return row;
            }
        };
        listViewHistory.setAdapter(adapter);
        listViewHistory.setOnItemClickListener(null);

        btnBack.setOnClickListener(v -> finish());
    }

    private void openEdit(calPlanRecord record) {
        if (record == null) return;
        Intent intent = new Intent(HistoryActivity.this, HistoryEditActivity.class);
        intent.putExtra("activeDate", day);
        intent.putExtra("calPlanRecord", record);
        startActivityForResult(intent, 1);
    }

    private void confirmDelete(final calPlanRecord record) {
        if (record == null || record.id == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Удалить")
                .setMessage("Удалить запись? (" + (record.Name != null ? record.Name : "") + ")")
                .setPositiveButton("Ок", (d, w) -> {
                    historyDb.deleteHistory(record.id);
                    reload();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reload() {
        records.clear();
        calPlanRecord[] arr = historyDb.getHistoryByDate(day);
        if (arr != null) {
            for (calPlanRecord r : arr) {
                if (r != null) records.add(r);
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
                if (record != null) {
                    historyDb.upsertHistory(record);
                }
            }
            reload();
        }
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