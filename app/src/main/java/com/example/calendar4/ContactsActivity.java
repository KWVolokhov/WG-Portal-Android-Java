package com.example.calendar4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
    private ArrayAdapter<String> adapter;
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

        // Setup adapter
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
            @Override
            public int getCount() {
                return allContacts.size();
            }

            @Override
            public String getItem(int position) {
                ContactRecord contact = allContacts.get(position);
                // Format: "Фамилия Имя О. (phone)"
                StringBuilder sb = new StringBuilder();
                if (contact.Surname != null) sb.append(contact.Surname).append(" ");
                if (contact.FirstName != null) sb.append(contact.FirstName).append(" ");
                if (contact.Patronymic != null && contact.Patronymic.length() > 0) {
                    sb.append(contact.Patronymic.substring(0, 1)).append(".");
                }
                if (contact.Phone != null) sb.append(" (").append(contact.Phone).append(")");
                return sb.toString();
            }
        };
        listViewContacts.setAdapter(adapter);

        // Contact click - open for editing
        listViewContacts.setOnItemClickListener((parent, view, position, id) -> {
            ContactRecord selectedContact = allContacts.get(position);
            Intent intent = new Intent(ContactsActivity.this, EditContactActivity.class);
            intent.putExtra("contactId", selectedContact.id);
            startActivityForResult(intent, 1);
        });

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