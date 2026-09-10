package com.example.calendar4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditContactActivity extends AppCompatActivity {

    // Task 44: дата создания/обновления показывается вместе со временем.
    private static final SimpleDateFormat DISPLAY_DATE_TIME =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());

    private EditText editTextSurname;
    private EditText editTextFirstName;
    private EditText editTextPatronymic;
    private PhoneFieldView editTextPhone;;
    private InfoFieldView editTextInfo;
    private PhoneFieldView editTextPhone2;
    private EditText editTextEmail;
    private DateFieldView editTextBirthDate;
    private EditText editTextHomeAddress;
    private EditText editTextDateReceived;
    private ImageButton btnOK;
    private ImageButton btnCancel;
    private ImageButton btnCall;
    private ImageButton btnSMS;

    // Task 53: нередактируемые служебные поля карточки
    private TextView textViewAuthor;
    private TextView textViewDateCreated;
    private TextView textViewLastUpdatedBy;
    private TextView textViewDateModified;

    private ManageSQLDatabase owerDb;
    private ContactRecord currentRecord;
    private Integer contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editcontact);

        // Initialize views
        editTextSurname = findViewById(R.id.editTextSurname);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextPatronymic = findViewById(R.id.editTextPatronymic);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextInfo = findViewById(R.id.editTextInfo);
        editTextPhone2 = findViewById(R.id.editTextPhone2);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextBirthDate = findViewById(R.id.editTextBirthDate);
        editTextHomeAddress = findViewById(R.id.editTextHomeAddress);
        editTextDateReceived = findViewById(R.id.editTextDateReceived);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);
        btnCall = findViewById(R.id.btnCall);
        btnSMS = findViewById(R.id.btnSMS);
        textViewAuthor = findViewById(R.id.textViewContactAuthor);
        textViewDateCreated = findViewById(R.id.textViewContactDateCreated);
        textViewLastUpdatedBy = findViewById(R.id.textViewContactLastUpdatedBy);
        textViewDateModified = findViewById(R.id.textViewContactDateModified);

        // Initialize database
        //owerDb = new ManageSQLDatabase(this);
		owerDb = ManageSQLDatabase.getInstance(this);

        // Check if editing existing contact
        contactId = getIntent().getIntExtra("contactId", -1);
        if (contactId != -1) loadContact(contactId);
        else showNewContactDefaults();

        // OK button click handler
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContact();
            }
        });

        // Cancel button click handler
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        // Call button - stub
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditContactActivity.this, "Звонок (заглушка)", Toast.LENGTH_SHORT).show();
            }
        });

        // SMS button - stub
        btnSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditContactActivity.this, "СМС (заглушка)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Новый контакт: Автор = Ведущий (CALPARAM), Дата создания = сейчас (сохранятся по OK)
    private void showNewContactDefaults() {
        if (ManageSQLDatabase.AuthorName != null) textViewAuthor.setText(ManageSQLDatabase.AuthorName);
        textViewDateCreated.setText(DISPLAY_DATE_TIME.format(new Date()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Task 100: forward the attachment picker result to InfoFieldView
        InfoFieldView.onHostActivityResult(requestCode, resultCode, data);
    }

    private void loadContact(int id) {
        // Get contact from database by ID
        ContactRecord[] contacts = owerDb.getContacts("");
        for (ContactRecord contact : contacts) {
            if (contact.id != null && contact.id == id) {
                currentRecord = contact;
                populateFields();
                break;
            }
        }
    }

    private void populateFields() {
        if (currentRecord == null) return;

        if (currentRecord.Surname != null) editTextSurname.setText(currentRecord.Surname);
        if (currentRecord.FirstName != null) editTextFirstName.setText(currentRecord.FirstName);
        if (currentRecord.Patronymic != null) editTextPatronymic.setText(currentRecord.Patronymic);
        if (currentRecord.Phone != null) editTextPhone.setValue(currentRecord.Phone);
        editTextInfo.setText(currentRecord.Info);
        if (currentRecord.Phone2 != null) editTextPhone2.setValue(currentRecord.Phone2);
        if (currentRecord.Email != null) editTextEmail.setText(currentRecord.Email);
        if (currentRecord.HomeAddress != null) editTextHomeAddress.setText(currentRecord.HomeAddress);

        // Format dates for display
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        //if (currentRecord.BirthDate != null) editTextBirthDate.setText(sdf.format(currentRecord.BirthDate));
        editTextBirthDate.setDate(currentRecord.BirthDate);
        if (currentRecord.DateReceived != null) editTextDateReceived.setText(sdf.format(currentRecord.DateReceived));

        // Task 53: служебные поля (нередактируемые)
        // Task 102: имя автора/обновившего подтягиваем из справочника контактов по ID
        String author = contactNameById(currentRecord.AuthorID);
        if (author != null) currentRecord.AuthorName = author;
        if (currentRecord.AuthorName != null) textViewAuthor.setText(currentRecord.AuthorName);
        if (currentRecord.DateCreated != null) textViewDateCreated.setText(DISPLAY_DATE_TIME.format(currentRecord.DateCreated));
        String updater = contactNameById(currentRecord.LastUpdatedByID);
        if (updater != null) currentRecord.LastUpdatedBy = updater;
        if (currentRecord.LastUpdatedBy != null) textViewLastUpdatedBy.setText(currentRecord.LastUpdatedBy);
        if (currentRecord.DateModified != null) textViewDateModified.setText(DISPLAY_DATE_TIME.format(currentRecord.DateModified));
    }

    /** Task 102: текущее имя контакта (Фамилия Имя) из справочника по числовому ID. */
    private String contactNameById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        try {
            ContactRecord c = owerDb.getContactById(Integer.valueOf(id.trim()));
            if (c == null) return null;
            StringBuilder sb = new StringBuilder();
            if (c.Surname != null) sb.append(c.Surname).append(" ");
            if (c.FirstName != null) sb.append(c.FirstName);
            return sb.toString().trim().isEmpty() ? null : sb.toString().trim();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void saveContact() {
        // Get values from form
        String surname = editTextSurname.getText().toString().trim();
        String firstName = editTextFirstName.getText().toString().trim();
        String patronymic = editTextPatronymic.getText().toString().trim();
        String phone = editTextPhone.getValue().toString().trim();
        String info = editTextInfo.getText().toString().trim();
        String phone2 = editTextPhone2.getValue().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String homeAddress = editTextHomeAddress.getText().toString().trim();

        // Validate required fields
        if (surname.isEmpty() || firstName.isEmpty()) {
            Toast.makeText(this, "Введите фамилию и имя", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create or update record
        if (currentRecord == null) {
            currentRecord = new ContactRecord(surname, firstName, patronymic, phone);
            currentRecord.DateCreated = new Date();
            // Task 53: Автор = Ведущий из параметров
            currentRecord.AuthorID = ManageSQLDatabase.AuthorID;
            currentRecord.AuthorName = ManageSQLDatabase.AuthorName;
        } else {
            currentRecord.Surname = surname;
            currentRecord.FirstName = firstName;
            currentRecord.Patronymic = patronymic;
            currentRecord.Phone = phone;
        }

        currentRecord.Info = info;
        currentRecord.Phone2 = phone2;
        currentRecord.Email = email;
        currentRecord.HomeAddress = homeAddress;
        currentRecord.DateModified = new Date();
        // Task 53: Обновивший = Ведущий из параметров (проставляется при создании и каждом обновлении)
        currentRecord.LastUpdatedByID = ManageSQLDatabase.AuthorID;
        currentRecord.LastUpdatedBy = ManageSQLDatabase.AuthorName;

        // Parse dates
        currentRecord.BirthDate = editTextBirthDate.getDate();
        /*try {
            String birthDateStr = editTextBirthDate.getText().toString().trim();
            if (!birthDateStr.isEmpty()) {
                currentRecord.BirthDate = sdf.parse(birthDateStr);
            }
        } catch (Exception e) {
            currentRecord.BirthDate = null;
        }*/
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        try {
            String dateReceivedStr = editTextDateReceived.getText().toString().trim();
            if (!dateReceivedStr.isEmpty()) {
                currentRecord.DateReceived = sdf.parse(dateReceivedStr);
            }
        } catch (Exception e) {
            currentRecord.DateReceived = null;
        }

        // Save to database
        owerDb.upsertContact(currentRecord);

        // Return result
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}