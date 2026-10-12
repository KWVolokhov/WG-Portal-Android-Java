package com.example.calendar4;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Task 146: экран "О Программе" (пункт меню под "Параметры").
 * Показывает допустимую (минимальную) версию Android для запуска приложения
 * и текущую версию структур баз данных DATABASE_VERSION, плюс кнопку
 * "вернуться" крестиком, как на других экранах.
 */
public class AboutActivity extends BaseScreenActivity {

    // Task 146: минимальная поддерживаемая версия Android (minSdk = 26 = Android 8).
    private static final int MIN_ANDROID_API = 26;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textAndroidVersion = findViewById(R.id.textAndroidVersion);
        TextView textDbVersion = findViewById(R.id.textDbVersion);
        ImageButton btnBack = findViewById(R.id.btnBack);

        textAndroidVersion.setText("Допустимая версия Android: не ниже "
                + MIN_ANDROID_API + " (Android 8.0)");

        // Task 146: текущая версия структур баз данных из ManageSQLDatabase.
        textDbVersion.setText("Версия структур баз данных (DATABASE_VERSION): "
                + ManageSQLDatabase.getDatabaseVersion());

        // Кнопка "вернуться" крестиком - закрыть экран (как на других Activity).
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
