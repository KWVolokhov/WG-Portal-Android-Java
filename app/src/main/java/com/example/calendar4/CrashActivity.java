package com.example.calendar4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
    }

    /** Hard-terminates the process to free the crashed app state. */
    private void killProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}