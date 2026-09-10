package com.example.calendar4;

import android.app.Activity;
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
import java.util.Locale;

/**
 * Task 107: экран списка СМС (по одному на все папки СМС\Все, СМС\Входящие,
 * СМС\Исходящие, СМС\Корзина). Структура похожа на activity_contacts.xml:
 * вверху титл и кнопка только назад, под титлом строка фильтра, под фильтром список.
 * Создание СМС пока не делаем.
 */
public class SmsActivity extends Activity {

    // Папки и extra-ключ (Task 106/107)
    public static final String EXTRA_SMS_FOLDER = "sms_folder";
    public static final String FOLDER_ALL = "all";
    public static final String FOLDER_INCOME = "income";
    public static final String FOLDER_OUTCOME = "outcome";
    public static final String FOLDER_TRASH = "trash";

    private static final String[] FOLDER_TITLES = {"СМС\\Все", "СМС\\Входящие", "СМС\\Исходящие", "СМС\\Корзина"};

    private ListView listViewSms;
    private EditText editTextFilter;
    private ImageButton btnBack;

    private SmsSQLManage smsDb;
    private ArrayAdapter<smsRecord> adapter;
    private ArrayList<smsRecord> allSms;
    private String folder;

    private static final SimpleDateFormat DISPLAY_DATE =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        listViewSms = findViewById(R.id.listViewSms);
        editTextFilter = findViewById(R.id.editTextSmsFilter);
        btnBack = findViewById(R.id.btnBack);

        folder = getIntent() != null ? getIntent().getStringExtra(EXTRA_SMS_FOLDER) : FOLDER_ALL;
        if (folder == null || folder.isEmpty()) folder = FOLDER_ALL;

        TextView title = findViewById(R.id.textViewSmsTitle);
        if (title != null) title.setText(FOLDER_TITLES[titleIndex(folder)]);

        smsDb = new SmsSQLManage(ManageSQLDatabase.getInstance(this).getWritableDatabase());

        allSms = new ArrayList<>();
        loadSms("");

        adapter = new ArrayAdapter<smsRecord>(this, 0, allSms) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TwoLineListItem row;
                if (convertView instanceof TwoLineListItem) {
                    row = (TwoLineListItem) convertView;
                } else {
                    row = new TwoLineListItem(SmsActivity.this);
                }
                final smsRecord sms = allSms.get(position);
                row.setTypeIcon(typeIcon(sms));
                row.setTopText(topText(sms));
                row.setBottomText(bottomText(sms));
                row.setPosition(position);
                // Создание/редактирование СМС пока не делаем - кнопки без действий
                row.setOnEditClickListener(null);
                row.setOnDeleteClickListener(null);
                return row;
            }
        };
        listViewSms.setAdapter(adapter);
        listViewSms.setOnItemClickListener(null);

        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadSms(s.toString().trim());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra(CalParamRecord.EXTRA_IS_START_PAGE, false)) {
            finishAndRemoveTask();
        } else {
            super.onBackPressed();
        }
    }

    private int titleIndex(String f) {
        if (FOLDER_INCOME.equals(f)) return 1;
        if (FOLDER_OUTCOME.equals(f)) return 2;
        if (FOLDER_TRASH.equals(f)) return 3;
        return 0;
    }

    /** Task 106: три иконки по типу СМС (In / Out / Draft). */
    private int typeIcon(smsRecord sms) {
        if (sms == null || sms.Type == null) return R.drawable.ic_sms;
        if (smsRecord.TYPE_INCOMING.equals(sms.Type)) return R.drawable.ic_sms_in;
        if (smsRecord.TYPE_OUTGOING.equals(sms.Type)) return R.drawable.ic_sms_out;
        if (smsRecord.TYPE_DRAFT.equals(sms.Type)) return R.drawable.ic_sms_draft;
        return R.drawable.ic_sms;
    }

    private String topText(smsRecord sms) {
        if (sms == null) return "";
        return (sms.Subject != null && !sms.Subject.isEmpty()) ? sms.Subject : sms.Body;
    }

    private String bottomText(smsRecord sms) {
        if (sms == null) return "";
        StringBuilder sb = new StringBuilder();
        if (sms.FromName != null && !sms.FromName.isEmpty()) sb.append("От: ").append(sms.FromName);
        if (sms.ToName != null && !sms.ToName.isEmpty()) {
            if (sb.length() > 0) sb.append("  ");
            sb.append("Кому: ").append(sms.ToName);
        }
        if (sb.length() > 0) sb.append("  ");
        if (sms.DateReceived != null) sb.append(DISPLAY_DATE.format(sms.DateReceived));
        return sb.toString();
    }

    private void loadSms(String filter) {
        smsRecord[] records = smsDb.getSms(folder, filter);
        allSms.clear();
        if (records != null) {
            for (smsRecord r : records) allSms.add(r);
        }
    }
}