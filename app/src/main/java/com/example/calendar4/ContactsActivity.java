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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ContactsActivity extends Activity {

    private ListView listViewContacts;
    private EditText editTextFilter;
    private Button btnNew;
    private Button btnBack;

    private ManageSQLDatabase owerDb;
    private ArrayAdapter<ContactRecord> adapter;
    private ArrayList<ContactRecord> allContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // Initialize views
        listViewContacts = findViewById(R.id.listViewContacts);
        editTextFilter = findViewById(R.id.editTextFilter);
        btnNew = findViewById(R.id.btnNew);
        btnBack = findViewById(R.id.btnBack);

        // Initialize database
        owerDb = new ManageSQLDatabase(this);

        // Load all contacts
        allContacts = new ArrayList<>();
        loadContacts("");

        // Setup adapter: two-line rows
        // (top = "Surname FirstName Patronymic(3)", bottom = Phone),
        // with per-row "Редактировать" / "Удалить" buttons on the left.
        adapter = new ArrayAdapter<ContactRecord>(this, 0, allContacts) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TwoLineListItem row;
                if (convertView instanceof TwoLineListItem) {
                    row = (TwoLineListItem) convertView;
                } else {
                    row = new TwoLineListItem(ContactsActivity.this);
                }
                final ContactRecord contact = allContacts.get(position);
                row.setTopText(fullName(contact));
                row.setBottomText(contact.Phone != null ? contact.Phone : "");
                row.setOnEditClickListener(v -> openContact(contact));
                row.setOnDeleteClickListener(v -> confirmDeleteContact(contact));
                return row;
            }
        };
        listViewContacts.setAdapter(adapter);

        // Editing is done via the per-row "Редактировать" button; tapping the row does nothing.
        listViewContacts.setOnItemClickListener(null);

        // Filter text change
        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String filter = s.toString().trim();
                loadContacts(filter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // New button - open empty contact form
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsActivity.this, EditContactActivity.class);
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

    private String fullName(ContactRecord c) {
        StringBuilder sb = new StringBuilder();
        if (c.Surname != null) sb.append(c.Surname);
        if (c.FirstName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(c.FirstName);
        }
        if (c.Patronymic != null && c.Patronymic.length() > 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(c.Patronymic, 0, Math.min(3, c.Patronymic.length()));
        }
        return sb.toString();
    }

    private void openContact(ContactRecord contact) {
        Intent intent = new Intent(ContactsActivity.this, EditContactActivity.class);
        intent.putExtra("contactId", contact.id);
        startActivityForResult(intent, 1);
    }

    private void confirmDeleteContact(final ContactRecord contact) {
        if (contact == null || contact.id == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Удалить")
                .setMessage("Удалить запись?")
                .setPositiveButton("Ок", (d, w) -> {
                    owerDb.deleteContact(contact.id);
                    loadContacts(editTextFilter.getText().toString().trim());
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadContacts(String filter) {
        ContactRecord[] contacts = owerDb.getContacts(filter);
        allContacts.clear();
        for (ContactRecord contact : contacts) {
            allContacts.add(contact);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            // Refresh contacts list after editing
            String currentFilter = editTextFilter.getText().toString().trim();
            loadContacts(currentFilter);
            adapter.notifyDataSetChanged();
        }
    }
}