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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

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
    private static final long UPDATE_INTERVAL_MS = 2 * 60 * 1000L;        // каждые 2 минуты
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
    private long lastPersistedStepValue; // последнее значение датчика, уже учтённое в Steps
    private boolean active;         // включён ли шагомер
    private int lastRecordedSteps;  // сколько шагов записано в запись (для Истории)

    // Task 47: базовые (из справочника) поля "Голова".."Каллории", на которые
    // умножается количество шагов при обновлении записи HealthSport.
    private final Integer[] baseOrgan = new Integer[22];

    // Task 44: единственный активный сеанс - карточка HealthSportActivity показывает таймер.
    public static volatile Pedometer activeInstance;

    public static boolean isRunning() {
        Pedometer p = activeInstance;
        return p != null && p.active;
    }

    public static Integer getRunningRecordId() {
        Pedometer p = activeInstance;
        return (p != null) ? p.recordId : null;
    }

    public static long getElapsedMs() {
        Pedometer p = activeInstance;
        return (p != null) ? Math.max(0L, System.currentTimeMillis() - p.startTimeMs) : 0L;
    }

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
            //stepSensor.registerListener(this);
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);


            active = true;
            this.recordId = recordId;
            startTimeMs = System.currentTimeMillis();
            lastStepTimeMs = startTimeMs;
            lastStepValue = null;
            lastPersistedStepValue = 0;
            lastRecordedSteps = 0;
            activeInstance = this;

            // Task 47: запоминаем базовые поля справочника до умножения на шаги
            captureBaseOrganValues();

            // Запись в Историю о включении шагомера
            addHistoryRecord("Шагомер включен", 0, startTimeMs);

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
                    //stepSensor.unregisterListener(this);
                    sensorManager.unregisterListener(this, stepSensor);
                } catch (Exception e) {
                    toast("Шагомер: ошибка снятия слушателя: " + e.getMessage());
                }
                stepSensor = null;
            }
            sensorManager = null;
            if (timerRunnable != null) {
                handler.removeCallbacks(timerRunnable);
                timerRunnable = null;
            }
            if (finishRunnable != null) {
                handler.removeCallbacks(finishRunnable);
                finishRunnable = null;
            }

            if (recordId != null) {
                // Финальное обновление шагов
                updateRecord();
                try {
                    HealthSQLManage healthDb = new HealthSQLManage(db);
                    healthPlanRecord rec = healthDb.getHealthById(recordId);
                    if (rec != null) {
                        lastRecordedSteps = (rec.Steps != null) ? rec.Steps : 0;
                    }
                } catch (Exception e) {
                    toast("Шагомер: ошибка чтения шагов при завершении: " + e.getMessage());
                }
                recordId = null;
            }

            // Запись в Историю о выключении шагомера
            if (wasActive) {
                addHistoryRecord("Шагомер выключен", lastRecordedSteps, startTimeMs);
            }
            if (activeInstance == this) {
                activeInstance = null;
            }
        } catch (Exception e) {
            toast("Шагомер: ошибка при выключении: " + e.getMessage());
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

    /** Обновляет запись HEALTHPLAN текущим количеством шагов (раз в 10 минут и при выключении).
     * Task 42: число шагов накапливается в поле Steps записи события.
     * Task 47: поля "Голова".."Каллории" (из справочника) умножаются на количество шагов. */
    private void updateRecord() {
        try {
            if (db == null || recordId == null) return;
            long current = (lastStepValue != null) ? lastStepValue : 0L;
            long delta = current - lastPersistedStepValue;
            if (delta < 0) delta = 0; // датчик мог сброситься между сеансами
            HealthSQLManage healthDb = new HealthSQLManage(db);
            healthPlanRecord rec = healthDb.getHealthById(recordId);
            if (rec != null) {
                int base = (rec.Steps != null) ? rec.Steps : 0;
                rec.Steps = base + (int) delta;
                rec.BodyText = "Шагов: " + rec.Steps;
                // Task 44: поле Комментарий - только для пользователя, в него ничего не пишем.
                multiplyOrganFieldsBySteps(rec);
                rec.LastUpdatedDate = new java.util.Date();
                rec.LastUpdatedBy = ManageSQLDatabase.AuthorName;
                rec.LastUpdatedByID = ManageSQLDatabase.AuthorID;
                healthDb.upsertHealth(rec);
                lastPersistedStepValue = current;
                lastRecordedSteps = (rec.Steps != null) ? rec.Steps : 0;
            }
        } catch (Exception e) {
            toast("Шагомер: ошибка обновления шагов: " + e.getMessage());
        }
    }

    /** Task 47: поля из справочника умножаются на количество шагов (кроме самого поля Шаги). */
    private void multiplyOrganFieldsBySteps(healthPlanRecord rec) {
        if (rec == null || rec.Steps == null || rec.Steps <= 0) return;
        int steps = rec.Steps;
        rec.Head = mul(baseOrgan[0], steps);
        rec.Eyes = mul(baseOrgan[1], steps);
        rec.Ears = mul(baseOrgan[2], steps);
        rec.Nose = mul(baseOrgan[3], steps);
        rec.Throat = mul(baseOrgan[4], steps);
        rec.Teeth = mul(baseOrgan[5], steps);
        rec.Stomach = mul(baseOrgan[6], steps);
        rec.Intestines = mul(baseOrgan[7], steps);
        rec.Liver = mul(baseOrgan[8], steps);
        rec.Kidneys = mul(baseOrgan[9], steps);
        rec.Heart = mul(baseOrgan[10], steps);
        rec.Lungs = mul(baseOrgan[11], steps);
        rec.Pressure = mul(baseOrgan[12], steps);
        rec.Sleep = mul(baseOrgan[13], steps);
        rec.Weight = mul(baseOrgan[14], steps);
        rec.Nervous = mul(baseOrgan[15], steps);
        rec.Morality = mul(baseOrgan[16], steps);
        rec.Skin = mul(baseOrgan[17], steps);
        // Шаги (18) - сам множитель, его не умножаем
        rec.FoodWeight = mul(baseOrgan[19], steps);
        rec.DrinkValue = mul(baseOrgan[20], steps);
        rec.Kallory = mul(baseOrgan[21], steps);
    }

    /** Сохраняет базовые поля записи (до умножения на шаги) - см. Task 47. */
    private void captureBaseOrganValues() {
        Arrays.fill(baseOrgan, null);
        try {
            if (db == null || recordId == null) return;
            HealthSQLManage healthDb = new HealthSQLManage(db);
            healthPlanRecord rec = healthDb.getHealthById(recordId);
            if (rec == null) return;
            baseOrgan[0] = rec.Head;
            baseOrgan[1] = rec.Eyes;
            baseOrgan[2] = rec.Ears;
            baseOrgan[3] = rec.Nose;
            baseOrgan[4] = rec.Throat;
            baseOrgan[5] = rec.Teeth;
            baseOrgan[6] = rec.Stomach;
            baseOrgan[7] = rec.Intestines;
            baseOrgan[8] = rec.Liver;
            baseOrgan[9] = rec.Kidneys;
            baseOrgan[10] = rec.Heart;
            baseOrgan[11] = rec.Lungs;
            baseOrgan[12] = rec.Pressure;
            baseOrgan[13] = rec.Sleep;
            baseOrgan[14] = rec.Weight;
            baseOrgan[15] = rec.Nervous;
            baseOrgan[16] = rec.Morality;
            baseOrgan[17] = rec.Skin;
            baseOrgan[19] = rec.FoodWeight;
            baseOrgan[20] = rec.DrinkValue;
            baseOrgan[21] = rec.Kallory;
        } catch (Exception e) {
            toast("Шагомер: ошибка чтения базовых полей: " + e.getMessage());
        }
    }

    private Integer mul(Integer baseValue, int steps) {
        return (baseValue == null) ? null : baseValue * steps;
    }

    /** Запись в Историю (HISTORY) о включении/выключении шагомера.
     * Task 44/46: в Расшифровке указывается сколько шагов записано и с какой даты/времени запущен шагомер. */
    private void addHistoryRecord(String name, int steps, long startMs) {
        try {
            if (db == null || name == null || name.isEmpty()) return;
            calPlanRecord record = new calPlanRecord();
            record.Form = "History";
            record.Name = name;
            record.Okdate = new java.util.Date();
            record.StartDate = (startMs > 0) ? new java.util.Date(startMs) : new java.util.Date();
            record.LastUpdatedDate = new java.util.Date();
            record.LastUpdatedBy = ManageSQLDatabase.AuthorName;
            record.LastUpdatedByID = ManageSQLDatabase.AuthorID;
            String startStr = (startMs > 0)
                    ? new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new java.util.Date(startMs))
                    : "";
            record.BodyText = "Шагов: " + steps + (startStr.isEmpty() ? "" : "; запущен: " + startStr);
            // Field Comment - только для пользователя, в Историю его не пишем.
            HistorySQLManage historyDb = new HistorySQLManage(db);
            historyDb.upsertHistory(record);
        } catch (Exception e) {
            toast("Шагомер: ошибка записи в Историю: " + e.getMessage());
        }
    }

    private void toast(String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}