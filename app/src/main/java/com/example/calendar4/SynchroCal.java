package com.example.calendar4;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Task 143: синхронизация CalParamRecord с сервером.
 * Инициализируется параметрами из ParamsActivity (editTextAddress/editTextName/editTextPassword).
 * Отправляет POST JSON (ContentType=CalParamRecord, дата/время обновления, все поля параметров)
 * с Basic-авторизацией; в ответе ожидает подтверждение и номер сессии.
 * По успешному обмену корректирует запись CALPARAM только в поле SyncTime.
 * В будущем класс отправляет данные всех локальных SQL-таблиц.
 */
public class SynchroCal {

    public interface SynchroListener {
        void onSyncSuccess(String session, String message);
        void onSyncError(String error);
    }

    public static final String CONTENT_TYPE_CALPARAM = "CalParamRecord";
    private static final String TAG = "SynchroCal";
    private static final String[] STATUS_KEYS = {"Status", "Result", "Confirmation"};
    private static final String[] SESSION_KEYS = {"Session", "SessionId", "SessionID", "session"};
    // Номер сессии последнего успешного обмена (участвует в дальнейшей синхронизации)
    private static String currentSession = null;

    private final Context appContext;
    private final String address;
    private final String name;
    private final String password;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public SynchroCal(Context context, String address, String name, String password) {
        appContext = context.getApplicationContext();
        this.address = address;
        this.name = name;
        this.password = password;
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    /** Номер сессии последнего успешного обмена (null — обмена ещё не было). */
    public static String getSession() {
        return currentSession;
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) executor.shutdown();
    }

    public void synchronize(final CalParamRecord record, final SynchroListener listener) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = postJson(buildRequest(record));
                    handleResponse(record, response, listener);
                } catch (Exception e) {
                    Log.e(TAG, "synchronize", e);
                    notifyError(listener, "Ошибка синхронизации: " + e.getMessage());
                }
            }
        });
    }

    // Запрос: тип содержимого + дата последней синхронизации + все поля параметров
    private String buildRequest(CalParamRecord record) throws Exception {
        JSONObject body = new JSONObject();
        body.put("ContentType", CONTENT_TYPE_CALPARAM);
        body.put("LastSyncDate", orNull(record.SyncTime));
        body.put("Updated", nowStamp());
        JSONObject rec = new JSONObject();
        rec.put("id", orNull(record.id));
        rec.put("Address", orNull(record.Address));
        rec.put("Name", orNull(record.Name));
        rec.put("Password", orNull(record.Password));
        rec.put("Vedushii", orNull(record.Vedushii));
        rec.put("VedushiiID", orNull(record.VedushiiID));
        rec.put("StartPage", orNull(record.StartPage));
        rec.put("Button1Id", orNull(record.Button1Id));
        rec.put("Button2Id", orNull(record.Button2Id));
        rec.put("Button3Id", orNull(record.Button3Id));
        rec.put("Button4Id", orNull(record.Button4Id));
        rec.put("Button5Id", orNull(record.Button5Id));
        rec.put("Height", orNull(record.Height));
        rec.put("Weight", orNull(record.Weight));
        rec.put("Age", orNull(record.Age));
        rec.put("AttachFolder", orNull(record.AttachFolder));
        rec.put("DBName", orNull(record.DBName));
        body.put("Record", rec);
        return body.toString();
    }

    // POST JSON на адрес из параметров с Basic-авторизацией
    private String postJson(String requestBody) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(normalizeAddress(address));
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", basicAuth());
            OutputStream out = conn.getOutputStream();
            out.write(requestBody.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) throw new Exception("Сервер отклонил имя/пароль (HTTP 401)");
            if (code != HttpURLConnection.HTTP_OK) throw new Exception("HTTP код: " + code);
            return readStream(conn.getInputStream());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // Ответ: подтверждение + номер сессии; корректировка CALPARAM только в поле SyncTime
    private void handleResponse(CalParamRecord record, String response, SynchroListener listener) throws Exception {
        JSONObject json = new JSONObject(response);
        String status = firstNonEmpty(json, STATUS_KEYS);
        String session = firstNonEmpty(json, SESSION_KEYS);
        if (!status.isEmpty() && !isStatusOk(status)) {
            notifyError(listener, "Сервер отклонил обмен: " + status);
            return;
        }
        if (session.isEmpty()) {
            notifyError(listener, "Сервер не вернул номер сессии");
            return;
        }
        currentSession = session;
        String stamp = nowStamp();
        if (record.id != null) ManageSQLDatabase.getInstance(appContext).updateCalParamSyncTime(record.id, stamp);
        record.SyncTime = stamp;
        notifySuccess(listener, session, "Синхронизация " + CONTENT_TYPE_CALPARAM + " выполнена");
    }

    private String basicAuth() {
        String credentials = name + ":" + password;
        return "Basic " + Base64.encodeToString(credentials.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }

    private String normalizeAddress(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) throw new IllegalArgumentException("Адрес сервера не задан");
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://" + s;
        return s;
    }

    private String readStream(InputStream in) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) buffer.append(line);
        reader.close();
        return buffer.toString();
    }

    private String firstNonEmpty(JSONObject json, String[] keys) {
        for (String key : keys) {
            String value = json.optString(key, "");
            if (!value.isEmpty()) return value;
        }
        return "";
    }

    private boolean isStatusOk(String status) {
        String s = status.trim().toLowerCase(Locale.US);
        return s.equals("ok") || s.equals("success") || s.equals("true") || s.startsWith("2");
    }

    private Object orNull(Object value) {
        return value != null ? value : JSONObject.NULL;
    }

    private String nowStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void notifySuccess(final SynchroListener listener, final String session, final String message) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onSyncSuccess(session, message);
            }
        });
    }

    private void notifyError(final SynchroListener listener, final String message) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onSyncError(message);
            }
        });
    }
}