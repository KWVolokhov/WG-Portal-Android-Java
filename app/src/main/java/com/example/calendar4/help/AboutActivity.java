package com.example.calendar4.help;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendar4.help.AndroidVersions;
import com.example.calendar4.localsql.ManageSQLDatabase;

/**
 * Task 146: экран "О Программе" (пункт меню под "Параметры").
 * Показывает допустимую (минимальную) версию Android для запуска приложения,
 * текущую версию Android устройства, версию структур баз данных DATABASE_VERSION,
 * плюс кнопку показа полного соответствия API<->версии и кнопку "вернуться" крестиком.
 */
public class AboutActivity extends AppCompatActivity {

    // Task 146: минимальная поддерживаемая версия Android (minSdk = 26 = Android 8).
    private static final int MIN_ANDROID_API = 26;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textAndroidVersion = findViewById(R.id.textAndroidVersion);
        TextView textCurrentAndroidVersion = findViewById(R.id.textCurrentAndroidVersion);
        TextView textDbVersion = findViewById(R.id.textDbVersion);
        Button btnShowVersions = findViewById(R.id.btnShowVersions);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Task 150: допустимая версия из словаря API -> версия Android.
        textAndroidVersion.setText("Допустимая версия Android не ниже: "
                + AndroidVersions.versionName(MIN_ANDROID_API) + " (Api " + MIN_ANDROID_API + ")");

        // Task 151: текущая версия Android устройства (рядом с допустимой).
        int curApiVer = Build.VERSION.SDK_INT;
        textCurrentAndroidVersion.setText("Текущая версия Android: "
                + AndroidVersions.versionName(curApiVer) + " (Api " + curApiVer + ")");

        // Task 146: текущая версия структур баз данных из ManageSQLDatabase.
        textDbVersion.setText("Версия структур баз данных (DATABASE_VERSION): "
                + ManageSQLDatabase.getDatabaseVersion());

        // Task 150: по кнопке показываем полное соответствие версий API и Android.
        btnShowVersions.setOnClickListener(v -> showVersionsDialog());

        // Кнопка "вернуться" крестиком - закрыть экран (как на других Activity).
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Task 177: кнопка "Копировать" - копирует в буфер текущую версию Android и версию БД.
        Button btnCopyInfo = findViewById(R.id.btnCopyInfo);
        btnCopyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int curApiVer = Build.VERSION.SDK_INT;
                String copyText = "Версия Android " + AndroidVersions.versionName(curApiVer)
                        + "(Api " + curApiVer + "), Database_Version: " + ManageSQLDatabase.getDatabaseVersion();
                copyToClipboard(copyText);
            }
        });
    }

    /** Task 177: копирует строку в буфер обмена и показывает Toast. */
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Информация о версии", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show();
    }

    /** Task 150: диалог со всем соответствием API <-> версия Android (включая смартчасы). */
    private void showVersionsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Соответствие версий Android")
                .setItems(AndroidVersions.allEntries(), null)
                .setPositiveButton("Ок", null)
                .show();
    }
}
