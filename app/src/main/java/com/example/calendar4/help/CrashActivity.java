package com.example.calendar4.help;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calendar4.MainActivity;
import com.example.calendar4.R;

/**
 * Crash screen launched by {@link HardcoreCrashHandler} when the application crashes.
 * Shows the captured stack trace and lets the user either restart the app or close it.
 */
public class CrashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        String errorText = getIntent().getStringExtra("error_text");
        TextView tvError = findViewById(R.id.errorText);
        tvError.setText(errorText != null ? errorText : "Неизвестная ошибка");

        findViewById(R.id.btnRestart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CrashActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                killProcess();
            }
        });

        findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                killProcess();
            }
        });

        // Task 178: кнопка "Копировать" - копирует текст ошибки в буфер обмена.
        findViewById(R.id.btnCopy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String errorText = tvError.getText().toString();
                copyToClipboard(errorText);
            }
        });
    }

    /** Task 178: копирует строку в буфер обмена и показывает Toast. */
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Ошибка", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Скопировано в буфер обмена", Toast.LENGTH_SHORT).show();
    }

    /** Hard-terminates the process to free the crashed app state. */
    private void killProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}