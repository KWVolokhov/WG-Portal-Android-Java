package com.example.calendar4;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.PrintWriter;
import java.io.StringWriter;

public class HardcoreCrashHandler implements Thread.UncaughtExceptionHandler {

    /** Защита от повторного входа (если упадёт сам CrashActivity) - Task 134. */
    private static boolean handling = false;

    private final Context context;

    public HardcoreCrashHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Task 134: process-wide installation (called once from WGPortalApp.onCreate). */
    public static void install(Context context) {
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof HardcoreCrashHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new HardcoreCrashHandler(context));
        }
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        if (handling) {
            killProcess();
            return;
        }
        handling = true;
        try {
            // 1. Превращаем весь стек ошибки (StackTrace) в одну большую строку
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            String stackTraceString = sw.toString();

            // 2. Собираем информацию о системе
            String fullReport = "--- СБОЙ ПРИЛОЖЕНИЯ ---\n" +
                    "Устройство: " + Build.MANUFACTURER + " " + Build.MODEL + "\n" +
                    "Android: " + Build.VERSION.RELEASE + "\n" +
                    "Поток: " + thread.getName() + "\n\n" +
                    "--- СТЕК ОШИБКИ ---\n" +
                    stackTraceString;

            // 3. Дублируем в системный лог Android Studio
            Log.e("CRITICAL_CRASH", fullReport);

            // 4. Запускаем наш специальный экран ошибки
            Intent intent = new Intent(context, CrashActivity.class);
            intent.putExtra("error_text", fullReport);
            // Флаги нужны, чтобы запустить экран из фонового обработчика в чистом виде
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            // Даём системе зарегистрировать запуск экрана, прежде чем убьём процесс
            try { Thread.sleep(300); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

        } catch (Exception e) {
            // Если сломался сам обработчик ошибок — просто пишем в консоль
            Log.e("CRITICAL_CRASH", "Ошибка внутри самого CrashHandler", e);
        } finally {
            killProcess();
        }
    }

    /** 5. Жестко ликвидируем упавший процесс, чтобы он не завис в памяти. */
    private static void killProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}