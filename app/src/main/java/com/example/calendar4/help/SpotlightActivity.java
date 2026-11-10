package com.example.calendar4.help;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.calendar4.R;

/**
 * Task 148a: заготовка экрана Spotlight (контекстная подсказка).
 * Вызывается кнопкой Help из BaseScreenActivity. Содержит текстовое поле
 * с сутью Activity, на которой нажата кнопка Help (содержимое придёт из
 * будущего "Help Справочника"), и 3 кнопки 32dp*32dp: назад, вперёд, закрыть.
 */
public class SpotlightActivity extends AppCompatActivity {

    // Task 148a: ключ extra с описанием экрана (суть подсказки).
    public static final String EXTRA_HELP_TEXT = "help_text";
    private String helpText;
    TextView headerTitle;
    TextView textViewSpotlightContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotlight);

        headerTitle = findViewById(R.id.headerTitle);
        textViewSpotlightContent = findViewById(R.id.textViewSpotlightContent);
        ImageButton btnBack = findViewById(R.id.btnSpotlightBack);
        ImageButton btnForward = findViewById(R.id.btnSpotlightForward);
        ImageButton btnClose = findViewById(R.id.btnSpotlightClose);

        // Task 148a: суть Activity из будущего "Help Справочника" (пока заглушка).
        helpText = HelpConstants.getScreenTitle(getIntent().getStringExtra(EXTRA_HELP_TEXT));
        updateTextViews();

        // Назад/вперёд - заготовки; закрыть - выход.
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpText = HelpConstants.getPrevTitle(helpText);
                updateTextViews();
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpText = HelpConstants.getNextTitle(helpText);
                updateTextViews();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void updateTextViews(){
        headerTitle.setText(helpText != null ? "Экран: " + helpText : "");
        textViewSpotlightContent.setText(helpText != null ?
                HelpConstants.getDescByTitle(helpText) : "");
    }

}
