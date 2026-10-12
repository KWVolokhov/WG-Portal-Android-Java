package com.example.calendar4;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Task 148a: заготовка экрана Spotlight (контекстная подсказка).
 * Вызывается кнопкой Help из BaseScreenActivity. Содержит текстовое поле
 * с сутью Activity, на которой нажата кнопка Help (содержимое придёт из
 * будущего "Help Справочника"), и 3 кнопки 32dp*32dp: назад, вперёд, закрыть.
 */
public class SpotlightActivity extends BaseScreenActivity {

    // Task 148a: ключ extra с описанием экрана (суть подсказки).
    public static final String EXTRA_HELP_TEXT = "help_text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotlight);

        TextView headerTitle = findViewById(R.id.headerTitle);
        EditText editTextSpotlight = findViewById(R.id.editTextSpotlight);
        ImageButton btnBack = findViewById(R.id.btnSpotlightBack);
        ImageButton btnForward = findViewById(R.id.btnSpotlightForward);
        ImageButton btnClose = findViewById(R.id.btnSpotlightClose);

        headerTitle.setText("Подсказка");

        // Task 148a: суть Activity из будущего "Help Справочника" (пока заглушка).
        String helpText = getIntent().getStringExtra(EXTRA_HELP_TEXT);
        editTextSpotlight.setText(helpText != null ? helpText : "");

        // Назад/вперёд - заготовки; закрыть - выход.
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SpotlightActivity.this, "Назад", Toast.LENGTH_SHORT).show();
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SpotlightActivity.this, "Вперёд", Toast.LENGTH_SHORT).show();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
