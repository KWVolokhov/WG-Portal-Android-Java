package com.example.calendar4;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Задача 29. Реальный шагомер для активностей типа HealthSport.
 *
 * - включает датчик шагов (Sensor.TYPE_STEP_COUNTER) на 2 часа;
 * - каждые 10 минут обновляет запись HEALTHPLAN (шаги пишутся в BodyText/Weight);
 * - через 2 часа шагомер выключается;
 * - если шаги не отсчитываются 20 минут - шагомер выключается досрочно.
 *
 * Все обращения к android.hardware обёрнуты в try/catch и проверки на null,
 * чтобы код оставался работоспособным и на Android 8 без датчика шагов.
 */
public class Pedometer implements SensorEventListener {

    private static final int PEDOMETER_DURATION_MIN = 120;                 // 2 часа
    private static final long UPDATE_INTERVAL_MS = 10 * 60 * 1000L;        // каждые 10 минут
    private static final long STOP_TIMEOUT_MS = 20 * 60 * 1000L;           // 20 минут без шагов

    private final Context context;
    private final SQLiteDatabase db;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private Runnable finishRunnable;
    private Runnable timerRunnable;

    private Integer recordId;       // id записи HEALTHPLAN (HealthSport)
    private long startTimeMs;       // когда начался 2-часовой интервал
    private long lastStepTimeMs;    // момент последнего отсчитанного шага
    private Long lastStepValue;     // сырое накопительное значение датчика
    private boolean active;         // включён ли шагомер

    public Pedometer(Context context, SQLiteDatabase db) {
        this.context = context;
        this.db = db;
    }

    public boolean isActive() {
        return active;
    }

    public Integer getRecordId() {
        return recordId;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.values == null || event.values.length == 0) return;
        lastStepValue = (long) event.values[0];
        lastStepTimeMs = System.currentTimeMillis();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
/** Включает шагомер на 2 часа для записи HEALTHPLAN с указанным id. */
    public void start(Integer recordId) {
        try {
            // Безопасный сброс предыдущего запуска
            if (active) stop();

            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager == null) {
                toast("Шагомер недоступен");
                return;
            }
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepSensor == null) {
                toast("На устройстве нет датчика шагов");
                return;
            }
            stepSensor.registerListener(this);

            active = true;
            this.recordId = recordId;
            startTimeMs = System.currentTimeMillis();
            lastStepTimeMs = startTimeMs;
            lastStepValue = null;

            // Запись в Историю о включении шагомера
            addHistoryRecord("Шагомер включен");

            // Через 2 часа шагомер выключается
            finishRunnable = () -> {
                if (active) stop();
            };
            handler.postDelayed(finishRunnable, PEDOMETER_DURATION_MIN * 60L * 1000L);

            // Периодическое обновление записи SQL каждые 10 минут
            scheduleUpdate();

            toast("Шагомер запущен на 2 часа");
        } catch (Exception e) {
            toast("Шагомер: " + e.getMessage());
        }
    }

    /** Полностью выключает шагомер (снимает слушатель, сбрасывает таймеры, финальное обновление). */
    public void stop() {
        try {
            boolean wasActive = active;
            active = false;

            if (stepSensor != null) {
                try {
                    stepSensor.unregisterListener(this);
                } catch (Exception ignored) {
                }
                stepSensor = null;
            }
            sensorManager = null;
            timerRunnable = null;
            finishRunnable = null;

            updateRecord();
            recordId = null;

            // Запись в Историю о выключении шагомера
            if (wasActive) {
                addHistoryRecord("Шагомер выключен");
            }
        } catch (Exception e) {
            // Не критично
        }
    }

    /** Планирует периодическое обновление (10 минут) + проверку 20 минут без шагов. */
    private void scheduleUpdate() {
        if (!active) return;
        if (timerRunnable != null) return; // один периодический цикл

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timerRunnable = null;
                if (!active) return;

                updateRecord();

                // 2 часа прошли - выключаем
                long elapsedMs = System.currentTimeMillis() - startTimeMs;
                if (elapsedMs >= PEDOMETER_DURATION_MIN * 60L * 1000L) {
                    stop();
                    return;
                }
                // 20 минут без шагов - выключаем
                if (System.currentTimeMillis() - lastStepTimeMs >= STOP_TIMEOUT_MS) {
                    stop();
                    return;
                }
                scheduleUpdate();
            }
        };
        handler.postDelayed(timerRunnable, UPDATE_INTERVAL_MS);
    }

    /** Обновляет запись HEALTHPLAN текущим количеством шагов (раз в 10 минут и при выключении). */
    private void updateRecord() {
        try {
            if (db == null || recordId == null) return;
            long steps = (lastStepValue != null) ? lastStepValue : 0L;
            HealthSQLManage healthDb = new HealthSQLManage(db);
            healthPlanRecord rec = healthDb.getHealthById(recordId);
            if (rec != null) {
                rec.BodyText = "Шагов: " + steps;
                rec.Comment = "Шагомер: " + steps + " шагов";
                rec.LastUpdatedDate = new java.util.Date();
                rec.LastUpdatedBy = ManageSQLDatabase.AuthorName;
                rec.LastUpdatedByID = ManageSQLDatabase.AuthorID;
                // Число шагов дополнительно сохраняем в поле Weight (орган "Вес")
                rec.Weight = (int) steps;
                healthDb.upsertHealth(rec);
            }
        } catch (Exception e) {
            // Не критично: обновление шагов не должно прерывать приложение
        }
    }

    /** Запись в Историю (HISTORY) о включении/выключении шагомера. */
    private void addHistoryRecord(String name) {
        try {
            if (db == null || name == null || name.isEmpty()) return;
            calPlanRecord record = new calPlanRecord();
            record.Form = "History";
            record.Name = name;
            record.Okdate = new java.util.Date();
            record.StartDate = new java.util.Date();
            record.LastUpdatedDate = new java.util.Date();
            record.LastUpdatedBy = ManageSQLDatabase.AuthorName;
            record.LastUpdatedByID = ManageSQLDatabase.AuthorID;
            HistorySQLManage historyDb = new HistorySQLManage(db);
            historyDb.upsertHistory(record);
        } catch (Exception e) {
            // Не критично: запись в Историю не должна прерывать работу шагомера
        }
    }

    private void toast(String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}