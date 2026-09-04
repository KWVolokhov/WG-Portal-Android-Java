package com.example.calendar4;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.ArrayList;

/**
 * "Типы жизнедеятельности" reference book screen: lists all LIVETYPE records
 * (same look as the contacts list: two-line rows with per-row Edit/Delete buttons).
 * The search field filters by Name or Category when 3+ characters are typed.
 * "Add" opens a new LIVETYPE card.
 */
public class LivetypeActivity extends AppCompatActivity {

    private ListView listViewLivetype;
    private EditText editTextFilter;
    private ImageButton btnNew;
    private ImageButton btnBack;

    private LivetypeSQLManage livetypeDb;
    private ArrayAdapter<livetypeRecord> adapter;
    private final ArrayList<livetypeRecord> allRecords = new ArrayList<>();

    // Reloads the list after the edit screen returns (add/edit closed)
    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> reload());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livetype);

        listViewLivetype = findViewById(R.id.listViewLivetype);
        editTextFilter = findViewById(R.id.editTextLivetypeFilter);
        btnNew = findViewById(R.id.btnNew);
        btnBack = findViewById(R.id.btnBack);

		livetypeDb = new LivetypeSQLManage(ManageSQLDatabase.getInstance(this).getWritableDatabase());

        reload();

        // Search by name/category: 3+ characters filter the list (same as contacts screen)
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

        adapter = new ArrayAdapter<livetypeRecord>(this, 0, allRecords) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TwoLineListItem row;
                if (convertView instanceof TwoLineListItem) {
                    row = (TwoLineListItem) convertView;
                } else {
                    row = new TwoLineListItem(LivetypeActivity.this);
                }
                final livetypeRecord record = allRecords.get(position);
                row.setTopText(record.Name != null ? record.Name : "");
                row.setBottomText(record.Category != null ? record.Category : "");
                row.setTypeIcon(R.drawable.ic_type_note);
                row.setOnEditClickListener(v -> openEdit(record));
                row.setOnDeleteClickListener(v -> confirmDelete(record));
                row.setPosition(position);
                return row;
            }
        };
        listViewLivetype.setAdapter(adapter);


        // Add button - open a brand-new LIVETYPE card
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LivetypeActivity.this, LivetypeEditActivity.class);
                editLauncher.launch(intent);
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

    private void openEdit(livetypeRecord record) {
        if (record == null || record.id == null) return;
        Intent intent = new Intent(LivetypeActivity.this, LivetypeEditActivity.class);
        intent.putExtra("livetypeId", record.id);
        editLauncher.launch(intent);
    }

    private void confirmDelete(final livetypeRecord record) {
        if (record == null || record.id == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Удалить")
                .setMessage("Удалить запись? (" + (record.Name != null ? record.Name : "") + ")")
                .setPositiveButton("Ок", (d, w) -> {
                    livetypeDb.deleteLivetype(record.id);
                    reload();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reload() {
        if (livetypeDb == null) return;
        allRecords.clear();
        String filter = editTextFilter != null ? editTextFilter.getText().toString().trim() : "";
        livetypeRecord[] arr = livetypeDb.getAllLivetype(filter);
        if (arr != null) {
            for (livetypeRecord record : arr) {
                if (record != null) allRecords.add(record);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}