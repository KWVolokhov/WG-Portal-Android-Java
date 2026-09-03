package com.example.calendar4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;
import android.view.Menu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.os.Build;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {
    ListView mainListView;
    private ManageSQLDatabase owerDb=null;
    //CalendarView mainCalendar;
    RussianCalendarView russianCalendar;
    calPlanRecord[] activRecordS=null;
    Integer rowNum=0;
    private ArrayAdapter<calPlanRecord> mainListAdapter;
    private RussianHolidaysFetcher holidaysFetcher;

    // ===== Real pedometer (Task 27) =====
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private final SensorEventListener stepListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event == null || event.values == null || event.values.length == 0) return;
            float steps = event.values[0];
            lastStepValue = (long) steps;
            lastStepTimeMs = System.currentTimeMillis();
            pedometerActive = true;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private static final int PEDOMETER_DURATION_MIN = 120;        // 2 часа
    private static final long PEDOMETER_UPDATE_INTERVAL_MS = 10 * 60 * 1000L; // каждые 10 минут
    private static final long PEDOMETER_STOP_TIMEOUT_MS = 20 * 60 * 1000L;    // 20 минут без шагов
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Runnable pedometerFinishRunnable;
    private Runnable pedometerTimerRunnable;
    private Integer pedometerRecordId;      // id записи HEALTHPLAN (HealthSport «Прогулка»)
    private long pedometerStartTimeMs;      // when the 2-hour window started
    private long lastStepTimeMs;            // last moment a step was counted
    private Long lastStepValue;             // raw cumulative value from the sensor
    private boolean pedometerActive;        // is the pedometer currently running

    private static final int REQUEST_ACTIVITY_RECOGNITION = 101;

    // ===== Modern Activity Result API (replaces deprecated startActivityForResult) =====
    // Launcher for the shared edit screens (old requestCode=1)
    private final ActivityResultLauncher<Intent> inputCalPlanLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Get calPlanRecord from the edit screen
                    calPlanRecord record = (calPlanRecord) result.getData().getSerializableExtra("calPlanRecord");
                    if (record != null && owerDb != null) {
                        owerDb.upsertCalPlan(record);
                        refreshListView();
                        Toast.makeText(MainActivity.this, "Запись сохранена: " + record.Name, Toast.LENGTH_SHORT).show();
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "Отменено", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> contactsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            });

    private final ActivityResultLauncher<Intent> paramsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            });

    private final ActivityResultLauncher<Intent> projectsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            });

    private final ActivityResultLauncher<Intent> livetypeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Включаем наш жесткий перехватчик для всех потоков приложения
        Thread.setDefaultUncaughtExceptionHandler(new HardcoreCrashHandler(this));
    
        setContentView(R.layout.activity_main);

        //savedInstanceState.windowActionBar = true;
        //windowNoTitle = false;

        mainListView = findViewById(R.id.listView1);
        //=============Старт
        russianCalendar = findViewById(R.id.calendarView1);
        russianCalendar.setOnDateSelectedListener(date -> {
            String[] monthStr = {"Январь","Февраль","Март","Апрель","Май","Июнь","Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"};
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            String dateStr = cal.get(Calendar.DAY_OF_MONTH) + " " + monthStr[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR);
            //WG в самом календаре Toast.makeText(this, dateStr, Toast.LENGTH_SHORT).show();
            
            // Refresh list view when date changes
            refreshListView();
        });

        // Fetch holidays for current year
        //fetchRussianHolidays();

        initMainListView();
        
        // Set active date to today
        russianCalendar.activeDate = Calendar.getInstance().getTime();
        
        // Initialize database and load records for today
        // owerDb = new ManageSQLDatabase(this); //OLD code
		owerDb = ManageSQLDatabase.getInstance(this);
        SQLiteDatabase classDb = owerDb.getWritableDatabase();
        owerDb.onCreate(classDb);

        // Auto-register this device as a contact and set up "Ведущий" at startup
        ensureDeviceContactOnStart();
        
        // Load and display records for today
        refreshListView();

        // Fetch holidays for current year
        fetchRussianHolidays();

        // Open the configured start page (except Календарь/MainActivity)
        openStartPageIfNeeded();

    }

    /**
     * Task 26: opens the "Стартовая страница" chosen in Параметры.
     * null/"Calendar" keeps this MainActivity (default). After reinstall the
     * database is empty, so the app always starts from MainActivity.
     */
    private void openStartPageIfNeeded() {
        try {
            if (owerDb == null) return;
            CalParamRecord param = owerDb.getCalParam();
            if (param == null || param.StartPage == null || param.StartPage.isEmpty()) return;

            if (CalParamRecord.START_PAGE_CONTACTS.equals(param.StartPage)) {
                contactsLauncher.launch(startPageIntent(new Intent(this, ContactsActivity.class)));
            } else if (CalParamRecord.START_PAGE_PROJECTS.equals(param.StartPage)) {
                projectsLauncher.launch(startPageIntent(new Intent(this, ProjectsActivity.class)));
            } else if (CalParamRecord.START_PAGE_PARAMS.equals(param.StartPage)) {
                paramsLauncher.launch(startPageIntent(new Intent(this, ParamsActivity.class)));
            }
            // START_PAGE_CALENDAR (or anything else) -> stay on MainActivity
        } catch (Exception e) {
            // Never break the app start because of the startup page logic
        }
    }

    /** Marks an activity opened as the app start page (system "Back" closes the app then). */
    private Intent startPageIntent(Intent intent) {
        intent.putExtra(CalParamRecord.EXTRA_IS_START_PAGE, true);
        return intent;
    }

    /**
     * Recalculate the day list from the CALPLAN table every time this screen is shown again.
     * This covers both the OK button in activity_input_cal_plan and the Back button in
     * activity_contacts, as well as any other screen returning to MainActivity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (owerDb != null && russianCalendar != null) {
            refreshListView();
        }
    }
    // ===================== Startup device registration =====================
    private static final int REQUEST_PHONE_STATE = 100;

    // Checks permission; if granted registers the device, otherwise asks the user.
    private void ensureDeviceContactOnStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            ensureDeviceContact();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PHONE_STATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PHONE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ensureDeviceContact();
            }
        }
    }

    // Registers the device as a CONTACT (Surname="Etot", FirstName="Phone") if not present,
    // then sets the "Ведущий" param if it is still empty.
    private void ensureDeviceContact() {
        try {
            if (owerDb == null) return;

            String phone = getDevicePhoneDigits();
            String model = getDeviceModel();
            String imei = getDeviceImei();

            ContactRecord contact = (phone != null) ? owerDb.getContactByPhone(phone) : null;
            // If a number is unavailable, look for the device record by Surname+FirstName
            if (contact == null) contact = owerDb.getContactBySurnameFirstName("Etot", "Phone");

            if (contact == null) {
                ContactRecord rec = new ContactRecord("Etot", "Phone", model, phone);
                rec.Info = imei;
                rec.DateCreated = new Date();
                owerDb.upsertContact(rec);   // inserts, no duplicates (single device record)
            }

            // Fetch the device record again to make sure it is present
            contact = (phone != null) ? owerDb.getContactByPhone(phone) : null;
            if (contact == null) contact = owerDb.getContactBySurnameFirstName("Etot", "Phone");
            if (contact == null) return;

            CalParamRecord param = owerDb.getCalParam();
            if (param == null) param = new CalParamRecord();
            boolean changed = false;
            if (param.Vedushii == null || param.Vedushii.trim().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                if (contact.Surname != null) sb.append(contact.Surname).append(" ");
                if (contact.FirstName != null) sb.append(contact.FirstName);
                param.Vedushii = sb.toString().trim();
                param.VedushiiID = contact.EntryID != null ? contact.EntryID
                        : (contact.id != null ? String.valueOf(contact.id) : null);
                changed = true;
            }
            if (changed) {
                owerDb.upsertCalParam(param);
                // Keep the static author fields (used by all record screens) in sync
                ManageSQLDatabase.AuthorName = param.Vedushii;
                ManageSQLDatabase.AuthorID = param.VedushiiID;
            }
        } catch (Exception e) {
            // Non-fatal: ignore device auto-registration errors
        }
    }

    private String getDevicePhoneDigits() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (tm == null) return null;
            String line = ""; tm.getLine1Number();
            if (line == null) return null;
            StringBuilder digits = new StringBuilder();
            for (int i = 0; i < line.length() && digits.length() < 10; i++) {
                char ch = line.charAt(i);
                if (Character.isDigit(ch)) digits.append(ch);
            }
            // Take the last 10 digits (drops country code if present)
            if (digits.length() > 10) digits.delete(0, digits.length() - 10);
            return digits.length() == 10 ? digits.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getDeviceModel() {
        String model = Build.MODEL;
        return (model == null || model.trim().isEmpty()) ? null : model.trim();
    }

    private String getDeviceImei() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (tm == null) return null;
            // IMEI on modern Android (API 29+) requires privileged access; best effort.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return tm.getImei();
            }

            return tm.getDeviceId();
        } catch (Exception e) {
            return null;
        }
    }

    private void fetchRussianHolidays() {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1; // 1-based month
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        
        // Check if holidays need to be updated (on 1st day of month or if no data)
        boolean isFirstDayOfMonth = (currentDay == 1);
        boolean needsUpdate = owerDb.needsHolidayUpdate("RUS", currentYear, currentMonth);
        
        if (needsUpdate || isFirstDayOfMonth) {
            // Fetch holidays from internet
            holidaysFetcher = new RussianHolidaysFetcher(this);
            
            // Modern approach for API 30+ (Android 11+)
            holidaysFetcher.fetchHolidaysForYear(currentYear, new RussianHolidaysFetcher.HolidaysFetchListener() {
                @Override
                public void onHolidaysFetched(java.util.HashSet<String> holidays) {
                    // Convert HashSet to holidayRecord array and save to database
                    holidayRecord[] holidayRecords = new holidayRecord[holidays.size()];
                    int index = 0;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    
                    for (String holidayStr : holidays) {
                        // Parse holiday string (format: "yyyy-MM-dd" or similar)
                        try {
                            // Assuming format is "yyyy-MM-dd HolidayName" or just date
                            String[] parts = holidayStr.split(" ", 2);
                            if (parts.length >= 1) {
                                Date holidayDate = sdf.parse(parts[0]);
                                String holidayName = parts.length > 1 ? parts[1] : "Праздник";
                                holidayRecords[index++] = new holidayRecord("RUS", holidayDate, holidayName);
                            }
                        } catch (Exception e) {
                            // Skip invalid entries
                        }
                    }
                    
                    // Save to database
                    if (index > 0) {
                        holidayRecord[] validRecords = new holidayRecord[index];
                        System.arraycopy(holidayRecords, 0, validRecords, 0, index);
                        owerDb.upsertHolidays("RUS", currentYear, validRecords);
                    }
                    
                    // Load holidays from database and set to calendar
                    loadHolidaysFromDatabase();
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Ошибка загрузки праздников: " + error, Toast.LENGTH_SHORT).show();
                    });
                    // Try to load from database anyway
                    //WG 11.08.26 loadHolidaysFromDatabase();   //Нарушение свежей безопасности Андроида
                }
            });
        } else {
            // Load holidays from database
            loadHolidaysFromDatabase();
        }
    }
    
    private void loadHolidaysFromDatabase() {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        
        // Get holidays from database
        holidayRecord[] holidays = owerDb.getHolidays("RUS", currentYear);
        
        // Convert to HashSet<String> for RussianCalendarView
        java.util.HashSet<String> holidayStrings = new java.util.HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        
        for (holidayRecord holiday : holidays) {
            if (holiday.HolidayDate != null) {
                holidayStrings.add(sdf.format(holiday.HolidayDate));
            }
        }
        
        // Set holidays to calendar
        RussianCalendarView russianCalendar = findViewById(R.id.calendarView1);
        russianCalendar.setHolidays(holidayStrings);
        
        runOnUiThread(() -> {
            String msg = "Праздники загружены: " + holidayStrings.size() + " дней";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (holidaysFetcher != null) {
            holidaysFetcher.shutdown();
        }
    }
    public boolean onCreateOptionsMenu(Menu menu1) {
        getMenuInflater().inflate(R.menu.main_menu, menu1);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        String JabText=null;
        if(item.getItemId()==R.id.calendar) JabText = "Меню Календарь";
        if(item.getItemId()==R.id.contacts) {
            JabText = "Меню Контакты";
            // Launch ContactsActivity modally
            Intent intent = new Intent(this, ContactsActivity.class);
            contactsLauncher.launch(intent);
        }
        if(item.getItemId()==R.id.calculator) JabText = "Меню Калькулятор";
        if(item.getItemId()==R.id.livetype) {
            JabText = "Меню Типы жизнедеятельности";
            // Launch the "Типы жизнедеятельности" reference screen
            Intent intent = new Intent(this, LivetypeActivity.class);
            livetypeLauncher.launch(intent);
        }
        if(item.getItemId()==R.id.parametrs) {
            JabText = "Меню Параметры";
            // Launch ParamsActivity modally
            Intent intent = new Intent(this, ParamsActivity.class);
            paramsLauncher.launch(intent);
        }
        if(item.getItemId()==R.id.projects_work) JabText = "Меню Проекты Рабочие";
        if(item.getItemId()==R.id.projects_all) {
            // Launch the "Проекты \ Все" screen
            Intent intent = new Intent(this, ProjectsActivity.class);
            projectsLauncher.launch(intent);
        }
        if(item.getItemId()==R.id.sms_income) JabText = "Меню СМС Входящие";
        if(item.getItemId()==R.id.sms_outcome) JabText = "Меню СМС Исходящие";
        /*AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.calendar:
                Toast.makeText(mainCalendar.getContext(), "menu calendar", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.projects:
                Toast.makeText(mainCalendar.getContext(), "menu calendar", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }*/
        if(JabText!=null) Toast.makeText(russianCalendar.getContext(), JabText, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }
    private void initMainListView(){ //Заполнение листа
        // Rows use TwoLineListItem: [text lines] [type icon 48dp] [Edit/Delete buttons]
        ArrayList<calPlanRecord> items = new ArrayList<>();
        mainListAdapter = new ArrayAdapter<calPlanRecord>(this, 0, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TwoLineListItem row;
                if (convertView instanceof TwoLineListItem) {
                    row = (TwoLineListItem) convertView;
                } else {
                    row = new TwoLineListItem(MainActivity.this);
                }
                final calPlanRecord record = getItem(position);
                if (record != null) {
                    // First line = Name, second line = first line / first 20 chars of BodyText
                    row.setTopText(record.Name != null ? record.Name : "");
                    row.setBottomText(shortBodyText(record.BodyText));
                    // Type icon according to Form field
                    row.setTypeIcon(typeIconForForm(record.Form));
                    row.setOnEditClickListener(v -> editCalPlan(record));
                    row.setOnDeleteClickListener(v -> confirmDeleteCalPlan(record));
                }
                return row;
            }
        };
        mainListView.setAdapter(mainListAdapter);
        // Editing/deleting is done via the row buttons
        mainListView.setOnItemClickListener(null);
    }

    // Second text line: first line of BodyText or no more than 20 characters
    private String shortBodyText(String bodyText) {
        if (bodyText == null) return "";
        String text = bodyText.trim();
        int newline = text.indexOf('\n');
        if (newline >= 0) text = text.substring(0, newline).trim();
        if (text.length() > 20) text = text.substring(0, 20);
        return text;
    }

    private int typeIconForForm(String form) {
        if (form == null) return R.drawable.ic_type_project;
        switch (form) {
            case "Project":     return R.drawable.ic_type_project;
            case "Note":        return R.drawable.ic_type_note;
            case "Remember":    return R.drawable.ic_type_remember;
            case "Task":        return R.drawable.ic_type_task;
            case "History":     return R.drawable.ic_type_history;
            case "HealthEat":   return R.drawable.ic_type_health_eat;
            case "HealthDrink": return R.drawable.ic_type_health_drink;
            case "HealthSport": return R.drawable.ic_type_health_sport;
            default:            return R.drawable.ic_type_project;
        }
    }

    private void editCalPlan(calPlanRecord record) {
        if (record == null) return;
        Intent intent = new Intent(this, activityClassFor(record.Form));
        intent.putExtra("activeDate", russianCalendar.activeDate);
        intent.putExtra("calPlanRecord", record);
        inputCalPlanLauncher.launch(intent);
    }

    /** Routes to the correct edit screen depending on the record Form. */
    private Class<?> activityClassFor(String form) {
        if (form == null) return InputCalPlanActivity.class;
        switch (form) {
            case "Note":        return NoteActivity.class;
            case "Remember":    return RememberActivity.class;
            case "Task":        return TaskActivity.class;
            case "History":     return HistoryEditActivity.class;
            case "HealthEat":   return HealthEatActivity.class;
            case "HealthDrink": return HealthDrinkActivity.class;
            case "HealthSport": return HealthSportActivity.class;
            default:            return InputCalPlanActivity.class;
        }
    }

    /** Opens the day History screen (list of CALPLAN Form=History for the active date). */
    public void openHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra("historyDate", russianCalendar.activeDate);
        startActivity(intent);
    }

    /** Opens the day Health screen (list of HealthEat/HealthDrink/HealthSport for the active date). */
    public void openHealth(View view) {
        Intent intent = new Intent(this, HealthActivity.class);
        intent.putExtra("healthDate", russianCalendar.activeDate);
        startActivity(intent);
    }

    private void confirmDeleteCalPlan(final calPlanRecord record) {
        if (record == null || record.id == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Удалить")
                .setMessage("Удалить запись? (" + (record.Name != null ? record.Name : "") + ")")
                .setPositiveButton("Ок", (d, w) -> {
                    owerDb.deleteCalPlanRecord(record);
                    refreshListView();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshListView() {
        if (russianCalendar != null && owerDb != null) {
            Date activeDate = russianCalendar.activeDate;
            if (activeDate != null) {
                activRecordS = owerDb.getDayRecords(activeDate);
                mainListAdapter.clear();
                if (activRecordS != null) {
                    for (calPlanRecord record : activRecordS) {
                        // History is hidden, Health is hidden, and Project/Task drafts
                        // are not shown in the MainActivity day list
                        if (record != null && !shouldHideFromMainList(record)) {
                            mainListAdapter.add(record);
                        }
                    }
                }
                mainListAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Records that must NOT appear in the first MainActivity list:
     * History, Health (HealthEat/HealthDrink/HealthSport are shown on the Health screen),
     * and drafts (StatusID='Draft') of Projects and Tasks.
     */
    private boolean shouldHideFromMainList(calPlanRecord record) {
        if (record == null) return true;
        String form = record.Form;
        if (form == null) return false;
        if ("History".equals(form)) return true;
        if ("HealthEat".equals(form) || "HealthDrink".equals(form) || "HealthSport".equals(form)) return true;
        // черновики проектов и задач
        if (("Project".equals(form) || "Task".equals(form)) && "Draft".equals(record.StatusID)) return true;
        return false;
    }

    public void taskAdd(View view) {
        try {
            // Initialize database if needed
            if (owerDb == null) {
                owerDb = new ManageSQLDatabase(this);
            }
            
            // Launch InputCalPlanActivity modally
            Intent intent = new Intent(this, InputCalPlanActivity.class);
            intent.putExtra("activeDate", russianCalendar.activeDate);  //WG12.08.26
            inputCalPlanLauncher.launch(intent);

        } catch(Exception err) {
            String selected = String.format("Error: "+err.getMessage());
            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
        }
    }

    public void onAction1(View view) {
        try {
			livetypeRecord recordIn = new livetypeRecord();
            healthPlanRecord recordOut = new healthPlanRecord(recordIn);
			

        } catch(Exception err) {
            String selected = String.format("Error: "+err.getMessage());
            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
        }
    }
    public void onAction2(View view) {
        try {
            

        } catch(Exception err) {
            String selected = String.format("Error: "+err.getMessage());
            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
        }
    }
    public void onAction3(View view) {
        try {
            

        } catch(Exception err) {
            String selected = String.format("Error: "+err.getMessage());
            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
        }
    }
	public void onAction4(View view) {
        try {
            

        } catch(Exception err) {
            String selected = String.format("Error: "+err.getMessage());
            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
        }
    }
	public void onAction5(View view) {
        try {
            

        } catch(Exception err) {
            String selected = String.format("Error: "+err.getMessage());
            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
        }

    }

}