package com.example.calendar4;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Task 111: просмотр одной записи СМС - все поля только для чтения.
 * Открывается из СМС-чата (кнопка "Посмотреть") и с экранов меню
 * СМС\\Все, СМС\\Входящие, СМС\\Исходящие, СМС\\Корзина.
 * Вверху слева от кнопки Назад - титл по типу записи:
 * "СМС от <Контакт>" для входящих, "СМС для <Контакт>" для исходящих.
 */
public class SmsViewActivity extends BaseScreenActivity {

    public static final String EXTRA_SMS_RECORD = "smsRecord";

    // Task 115: дата СМС показывается со секундами
    private static final SimpleDateFormat DISPLAY_DATE =
            new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

    private ImageButton btnBack;
    private TextView textViewTitle;
    private TextView textViewFrom;
    private TextView textViewTo;
    private TextView textViewDate;
    private TextView textViewSubject;
    private EditText editTextBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsview);

        btnBack = findViewById(R.id.btnBack);
        textViewTitle = findViewById(R.id.textViewSmsViewTitle);
        textViewFrom = findViewById(R.id.textViewSmsFrom);
        textViewTo = findViewById(R.id.textViewSmsTo);
        textViewDate = findViewById(R.id.textViewSmsDate);
        textViewSubject = findViewById(R.id.textViewSmsSubject);
        editTextBody = findViewById(R.id.editTextSmsBody);

        smsRecord sms = getIntent() != null
                ? (smsRecord) getIntent().getSerializableExtra(EXTRA_SMS_RECORD) : null;
        if (sms == null) {
            Toast.makeText(this, "СМС не найдено", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (textViewTitle != null) textViewTitle.setText(buildTitle(sms));
        textViewFrom.setText(sms.FromName != null ? sms.FromName : "");
        textViewTo.setText(sms.ToName != null ? sms.ToName : "");
        textViewDate.setText(sms.DateReceived != null ? DISPLAY_DATE.format(sms.DateReceived) : "");
        textViewSubject.setText(sms.Subject != null ? sms.Subject : "");
        editTextBody.setText(sms.Body != null ? sms.Body : "");
        editTextBody.setFocusable(false);

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

    /** "СМС от <Контакт>" для входящих, "СМС для <Контакт>" для исходящих. */
    private String buildTitle(smsRecord sms) {
        boolean incoming = smsRecord.TYPE_INCOMING.equals(sms.Type);
        String contact = incoming ? sms.FromName : sms.ToName;
        if (contact == null || contact.trim().isEmpty()) return "СМС";
        return incoming ? "СМС от " + contact.trim() : "СМС для " + contact.trim();
    }
}