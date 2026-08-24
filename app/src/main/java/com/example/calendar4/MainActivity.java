package com.example.calendar4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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

import android.os.Build; // Modern approach for API 30+ (Android 11+)
import android.view.WindowInsets; // Modern approach for API 30+ (Android 11+)
import android.view.WindowMetrics; // Modern approach for API 30+ (Android 11+)


public class MainActivity extends AppCompatActivity {
    private int oldOrientation;
    private int screenOrientation, oldScreenOrientation;
    private int screenWidth;
    private int screenHeight;
    ListView mainListView;
    private ManageSQLDatabase owerDb=null;
    //CalendarView mainCalendar;
    RussianCalendarView russianCalendar;
    calPlanRecord[] activRecordS=null;
    Integer rowNum=0;
    private RussianHolidaysFetcher holidaysFetcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Modern approach for API 30+ (Android 11+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager windowManager = getWindowManager();
            WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
            android.graphics.Rect bounds = windowMetrics.getBounds();
            WindowInsets windowInsets = windowMetrics.getWindowInsets();

            int insetsLeft = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).left;
            int insetsTop = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).top;
            int insetsRight = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).right;
            int insetsBottom = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).bottom;

            screenWidth = bounds.width() - insetsLeft - insetsRight;
            screenHeight = bounds.height() - insetsTop - insetsBottom;
        } else {    // Legacy approach for API < 30
            Display display = getWindowManager().getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            screenWidth = point.x;
            screenHeight = point.y;
        }

        //savedInstanceState.windowActionBar = true;
        //windowNoTitle = false;

        if(screenHeight>screenWidth)screenOrientation=0; else screenOrientation=1;

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
        refreshScreenOrientation(screenOrientation);
        
        // Set active date to today
        russianCalendar.activeDate = Calendar.getInstance().getTime();
        
        // Initialize database and load records for today
        owerDb = new ManageSQLDatabase(this);
        SQLiteDatabase classDb = owerDb.getWritableDatabase();
        owerDb.onCreate(classDb);
        
        // Load and display records for today
        refreshListView();

        // Fetch holidays for current year
        fetchRussianHolidays();

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
            startActivityForResult(intent, 3);
        }
        if(item.getItemId()==R.id.calculator) JabText = "Меню Калькулятор";
        if(item.getItemId()==R.id.parametrs) {
            JabText = "Меню Параметры";
            // Launch ParamsActivity modally
            Intent intent = new Intent(this, ParamsActivity.class);
            startActivityForResult(intent, 2);
        }
        if(item.getItemId()==R.id.projects_work) JabText = "Меню Проекты Рабочие";
        if(item.getItemId()==R.id.projects_all) JabText = "Меню Проекты Все";
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
    private void refreshScreenOrientation(int screenOrientation) {
        if (oldScreenOrientation==screenOrientation)return;
        if(screenOrientation==1) {//Ландшафт
            ConstraintLayout constraintLayout = (ConstraintLayout) mainListView.getParent();
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.listView1, ConstraintSet.TOP, R.id.calendarView1, ConstraintSet.TOP);
            constraintSet.connect(R.id.listView1, ConstraintSet.START, R.id.calendarView1, ConstraintSet.END);
            constraintSet.applyTo(constraintLayout);
            oldScreenOrientation=1;
        } else if(screenOrientation==0){//ортрет
            ConstraintLayout constraintLayout = (ConstraintLayout) mainListView.getParent();
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.listView1, ConstraintSet.TOP, R.id.button1, ConstraintSet.END);
            constraintSet.connect(R.id.listView1, ConstraintSet.START, R.id.button1, ConstraintSet.START);
            constraintSet.applyTo(constraintLayout);
            oldScreenOrientation=0;
        }
    }
    private void initMainListView(){ //Заполнение листа
        //String[] items = {"Задача 1", "Не Задачка 2", "Задача 4","Зметка 1", "Заметка 2", "Напоминание 1", "Напоминание 31", "Напомнить32"};

        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        //mainListView.setAdapter(adapter);

        mainListView.setOnItemClickListener((parent, view, position, id) -> {
            // Launch InputCalPlanActivity modally      WG12.08.26
            Intent intent = new Intent(this, InputCalPlanActivity.class);
            intent.putExtra("activeDate", russianCalendar.activeDate);  //WG12.08.26
            for (calPlanRecord record : activRecordS) {
                if (record.Name == (String) parent.getItemAtPosition(position)) {
                    intent.putExtra("calPlanRecord", record);  //WG12.08.26
                }
            }
            startActivityForResult(intent, 1);
        });
    }

    private void refreshListView() {
        // Get records for the active date
        if (russianCalendar != null && owerDb != null) {
            Date activeDate = russianCalendar.activeDate;
            if (activeDate != null) {
                activRecordS = owerDb.getCalPlan(activeDate);
                
                // Create list of names for display
                ArrayList<String> namesList = new ArrayList<>();
                for (calPlanRecord record : activRecordS) {
                    if (record.Name != null) {
                        namesList.add(record.Name);
                    }
                }
                
                // Update ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, 
                    android.R.layout.simple_list_item_1, 
                    namesList
                );
                mainListView.setAdapter(adapter);
            }
        }
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
            startActivityForResult(intent, 1);

        } catch(Exception err) {
            String selected = String.format("Error: "+err.getMessage());
            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // Get calPlanRecord from InputCalPlanActivity
                calPlanRecord record = (calPlanRecord) data.getSerializableExtra("calPlanRecord");
                
                if (record != null && owerDb != null) {
                    // Save to database
                    owerDb.upsertCalPlan(record);
                    
                    // Refresh list view with records for the active date
                    refreshListView();
                    
                    Toast.makeText(this, "Запись сохранена: " + record.Name, Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled - do nothing
                Toast.makeText(this, "Отменено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void taskAddOLD(View view) {
        try{
            owerDb = new ManageSQLDatabase(view.getContext());
            SQLiteDatabase classDb = owerDb.getWritableDatabase();
            owerDb.onCreate(classDb);
            String selected = null;

            Map<Integer, Object> classRez = owerDb.execSelectArrMap("Select * From CLASSIFICATOR");
            if (classRez.size()>0){


                selected = String.format("Жаба: Row=" +rowNum +", ID="+ ((Map<String, String>)classRez.get(rowNum)).get("ID")
                        + ", CATEGORY="+((Map<String, String>)classRez.get(rowNum)).get("CATEGORY") +
                        ", SONAME="+((Map<String, String>)classRez.get(rowNum)).get("SONAME"));
                rowNum++; if(rowNum>10) rowNum=0;
            } else {
                selected = String.format("Жаба: Row=0");
            }

            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();

        } catch(Exception err) {
            String selected = String.format("Error: "+err.getMessage());
            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
        }
    }
    public void InitSQLBase(View view) {
        try{
            //if(owerDb==null) owerDb = new ManageSQLDatabase(view.getContext());
            //SQLiteDatabase classDb = owerDb.getWritableDatabase();
            //owerDb.onCreate(classDb);

            String selected = String.format("Жаба: EMPTY");
            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
        } catch(Exception err) {
            String selected = String.format("Error: "+err.getMessage());
            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
        }
    }


}