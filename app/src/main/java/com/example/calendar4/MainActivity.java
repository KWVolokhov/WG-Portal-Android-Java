package com.example.calendar4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;
import android.view.Menu;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private int oldOrientation;
    private int screenOrientation, oldScreenOrientation;
    private int screenWidth;
    private int screenHeight;
    ListView mainListView;
    private ManageSQLDatabase owerDb=null;
    CalendarView mainCalendar;
    Integer rowNum=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenWidth = point.x;
        screenHeight = point.y;

        //savedInstanceState.windowActionBar = true;
        //windowNoTitle = false;

        if(screenHeight>screenWidth)screenOrientation=0; else screenOrientation=1;

        mainListView = findViewById(R.id.listView1);
        mainCalendar = findViewById(R.id.calendarView1);
        //=============Старт
        mainCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String[] monthStr = {"Январь","Февраль","Март","Апрель","Май","Июнь","Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"};
                String date = dayOfMonth + " " + monthStr[month] + " " + year ;
                Toast.makeText(mainCalendar.getContext(), date, Toast.LENGTH_SHORT).show();
            }
        });

        initMainListView();
        refreshScreenOrientation(screenOrientation);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu1) {
        getMenuInflater().inflate(R.menu.main_menu, menu1);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String JabText=null;
        if(item.getItemId()==R.id.calendar) JabText = "Меню Календарь";
        if(item.getItemId()==R.id.contacts) JabText = "Меню Контакты";
        if(item.getItemId()==R.id.calculator) JabText = "Меню Калькулятор";
        if(item.getItemId()==R.id.parametrs) {
            JabText = "Меню Параметры";
            /*Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);*/
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
        if(JabText!=null) Toast.makeText(mainCalendar.getContext(), JabText, Toast.LENGTH_SHORT).show();
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
        String[] items = {"Задача 1", "Не Задачка 2", "Задача 4","Зметка 1", "Заметка 2", "Напоминание 1", "Напоминание 31", "Напомнить32"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        mainListView.setAdapter(adapter);

        mainListView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = "Жаба: " + (String) parent.getItemAtPosition(position);
            Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
        });
    }

    public void taskAdd(View view) {
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