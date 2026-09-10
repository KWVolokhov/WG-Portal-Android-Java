package com.example.calendar4;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Task 110: СМС Чат - диалог сообщений между Ведущим (из параметров) и Контактом.
 * Открывается по кнопке СМС на карточке контакта (activity_editcontact.xml).
 * Если у Ведущего или Контакта не заполнен телефон (10 цифр) - Toast и выход.
 */
public class SmsChatActivity extends Activity {

    public static final String EXTRA_CONTACT_ID = "contactId";

    private ListView listViewSmsChat;
    private EditText editTextFilter;
    private EditText editTextInput;
    private TextView textViewVedushii;
    private ImageButton btnBack;
    private ImageButton btnSend;

    private ManageSQLDatabase owerDb;
    private SmsSQLManage smsDb;
    private ArrayAdapter<smsRecord> adapter;
    private ArrayList<smsRecord> chatSms;

    private ContactRecord contact;    // контакт, из которого открыли чат
    private ContactRecord vedushii;   // Ведущий из параметров (CONTACTS)
    private String vedushiiId;        // id Ведущего (как в SMSCALPLAN.FromID/ToID)
    private String contactId;         // id контакта

    private static final SimpleDateFormat DISPLAY_DATE =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smschat);

        listViewSmsChat = findViewById(R.id.listViewSmsChat);
        editTextFilter = findViewById(R.id.editTextSmsChatFilter);
        editTextInput = findViewById(R.id.editTextSmsChatInput);
        textViewVedushii = findViewById(R.id.textViewSmsChatVedushii);
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);

        owerDb = ManageSQLDatabase.getInstance(this);
        smsDb = new SmsSQLManage(owerDb.getWritableDatabase());

        int id = getIntent() != null ? getIntent().getIntExtra(EXTRA_CONTACT_ID, -1) : -1;
        contactId = id != -1 ? String.valueOf(id) : "";
        contact = id != -1 ? owerDb.getContactById(id) : null;
        if (contact == null) {
            Toast.makeText(this, "Контакт не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ведущий из параметров (CALPARAM.VedushiiID -> CONTACTS)
        vedushiiId = ManageSQLDatabase.AuthorID;
        if (vedushiiId == null || vedushiiId.trim().isEmpty()) {
            CalParamRecord param = owerDb.getCalParam();
            if (param != null) vedushiiId = param.VedushiiID;
        }
        vedushii = findContactById(vedushiiId);

        // Task 110: проверка телефонов (10 цифр) перед открытием чата
        String contactPhone = phoneDigits(contact.Phone);
        if (contactPhone.length() != 10) {
            Toast.makeText(this, "Заполните телефон контакта", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String vedushiiPhone = vedushii != null ? phoneDigits(vedushii.Phone) : "";
        if (vedushiiPhone.length() != 10) {
            Toast.makeText(this, "Заполните телефон Ведущего", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Титл: "СМС Чат: Фамилия Имя" + кнопка назад
        TextView title = findViewById(R.id.textViewSmsChatTitle);
        if (title != null) title.setText("СМС Чат: " + fullName(contact));

        // Инфо строка над полем ввода: "Ведущий: Фамилия Имя"
        textViewVedushii.setText("Ведущий: " + fullName(vedushii));

        chatSms = new ArrayList<>();
        loadChat("");

        adapter = new ArrayAdapter<smsRecord>(this, 0, chatSms) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                MessageListItem row;
                if (convertView instanceof MessageListItem) {
                    row = (MessageListItem) convertView;
                } else {
                    row = new MessageListItem(SmsChatActivity.this);
                }
                final smsRecord sms = chatSms.get(position);
                row.setTypeIcon(typeIcon(sms));
                row.setTopText(topText(sms));
                row.setMessageText(sms.Body != null ? sms.Body : "");
                row.setOnViewClickListener(v -> viewSms(sms));
                row.setOnDeleteClickListener(v -> confirmDelete(sms));
                return row;
            }
        };
        listViewSmsChat.setAdapter(adapter);
        listViewSmsChat.setOnItemClickListener(null);

        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadChat(s.toString().trim());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnBack.setOnClickListener(v -> finish());

        // Task 110: отправка = сохранение исходящего сообщения в SQLite (SMSCALPLAN)
        btnSend.setOnClickListener(v -> sendSms());
    }

    // ---------------------------------------------------------------------
    // Данные чата
    // ---------------------------------------------------------------------

    /** Загружает сообщения диалога Ведущий<->Контакт; filter (3+ симв.) ищет в тексте. */
    private void loadChat(String filter) {
        chatSms.clear();
        smsRecord[] records = smsDb.getSms(null, "");
        if (records != null) {
            for (smsRecord r : records) {
                if (isChatRecord(r) && matchesFilter(r, filter)) chatSms.add(r);
            }
        }
    }

    /** Запись принадлежит диалогу Ведущий<->Контакт (в любом направлении). */
    private boolean isChatRecord(smsRecord r) {
        if (r == null) return false;
        String fid = trim(r.FromID);
        String tid = trim(r.ToID);
        String vid = trim(vedushiiId);
        String cid = trim(contactId);
        return (fid.equals(vid) && tid.equals(cid)) || (fid.equals(cid) && tid.equals(vid));
    }

    /** Фильтр сообщений (3+ символа) по тексту/теме. */
    private boolean matchesFilter(smsRecord r, String filter) {
        if (filter == null || filter.length() < 3) return true;
        String f = filter.toLowerCase(Locale.getDefault());
        String body = r.Body != null ? r.Body.toLowerCase(Locale.getDefault()) : "";
        String subj = r.Subject != null ? r.Subject.toLowerCase(Locale.getDefault()) : "";
        return body.contains(f) || subj.contains(f);
    }

    /** Task 110: сохраняет исходящее СМС от Ведущего контакту в таблицу SMSCALPLAN. */
    private void sendSms() {
        String text = editTextInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show();
            return;
        }
        smsRecord sms = new smsRecord();
        sms.UNID = java.util.UUID.randomUUID().toString();
        sms.Type = smsRecord.TYPE_OUTGOING;
        sms.FromID = trim(vedushiiId);
        sms.FromName = fullName(vedushii);
        sms.ToID = trim(contactId);
        sms.ToName = fullName(contact);
        sms.Body = text;
        sms.Status = "New";
        sms.DateReceived = new Date();
        smsDb.upsertSms(sms);
        editTextInput.setText("");
        loadChat(editTextFilter.getText().toString().trim());
        adapter.notifyDataSetChanged();
    }

    private void confirmDelete(final smsRecord sms) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить")
                .setMessage("Удалить сообщение?")
                .setPositiveButton("Ок", (d, w) -> {
                    smsDb.deleteSms(sms.id);
                    loadChat(editTextFilter.getText().toString().trim());
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /** Просмотр СМС по кнопке "Посмотреть" - как просмотр на экранах из меню СМС. */
    private void viewSms(smsRecord sms) {
        if (sms == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("От: ").append(sms.FromName != null ? sms.FromName : "").append("\n");
        sb.append("Кому: ").append(sms.ToName != null ? sms.ToName : "").append("\n");
        if (sms.DateReceived != null) sb.append("Дата: ").append(DISPLAY_DATE.format(sms.DateReceived)).append("\n");
        if (sms.Subject != null && !sms.Subject.isEmpty()) sb.append("Тема: ").append(sms.Subject).append("\n");
        sb.append("\n").append(sms.Body != null ? sms.Body : "");
        new AlertDialog.Builder(this)
                .setTitle("СМС")
                .setMessage(sb.toString())
                .setPositiveButton("Ок", null)
                .show();
    }

    /** Иконка типа СМС (входящее/исходящее). */
    private int typeIcon(smsRecord sms) {
        if (sms == null || sms.Type == null) return R.drawable.ic_sms;
        if (smsRecord.TYPE_INCOMING.equals(sms.Type)) return R.drawable.ic_sms_in;
        if (smsRecord.TYPE_OUTGOING.equals(sms.Type)) return R.drawable.ic_sms_out;
        return R.drawable.ic_sms;
    }

    /** Верхняя строка списка: "Фамилия Имя  dd.MM.yyyy HH:mm". */
    private String topText(smsRecord sms) {
        StringBuilder sb = new StringBuilder();
        boolean outgoing = smsRecord.TYPE_OUTGOING.equals(sms.Type);
        sb.append(outgoing ? fullName(vedushii) : fullName(contact));
        if (sms.DateReceived != null) sb.append("  ").append(DISPLAY_DATE.format(sms.DateReceived));
        return sb.toString();
    }

    private String fullName(ContactRecord c) {
        StringBuilder sb = new StringBuilder();
        if (c != null) {
            if (c.Surname != null) sb.append(c.Surname);
            if (c.FirstName != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(c.FirstName);
            }
        }
        return sb.toString();
    }

    private ContactRecord findContactById(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) return null;
        try {
            return owerDb.getContactById(Integer.valueOf(idStr.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Только цифры телефона (10 цифр = валидный телефон). */
    private String phoneDigits(String phone) {
        return phone != null ? phone.replaceAll("\\D", "") : "";
    }

    private String trim(String s) {
        return s != null ? s.trim() : "";
    }
}

