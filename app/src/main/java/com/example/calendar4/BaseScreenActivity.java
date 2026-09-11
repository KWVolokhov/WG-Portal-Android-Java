package com.example.calendar4;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Task 118: единый стиль экранов в аспекте титла - на всех экранах Activity
 * показывается стандартный ActionBar с титлом "Календарный План" (как на MainActivity)
 * и то же меню (main_menu.xml) с переходами на другие экраны.
 * Экраны наследуются отсюда вместо android.app.Activity / AppCompatActivity.
 */
public abstract class BaseScreenActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.contacts) startActivity(new Intent(this, ContactsActivity.class));
        else if (id == R.id.livetype) startActivity(new Intent(this, LivetypeActivity.class));
        else if (id == R.id.projects_all) startActivity(new Intent(this, ProjectsActivity.class));
        else if (id == R.id.projects_work) startActivity(new Intent(this, ProjectsActivity.class)
                .putExtra(ProjectsActivity.EXTRA_WORK_MODE, true));
        else if (id == R.id.sms_all) startActivity(new Intent(this, SmsActivity.class)
                .putExtra(SmsActivity.EXTRA_SMS_FOLDER, SmsActivity.FOLDER_ALL));
        else if (id == R.id.sms_income) startActivity(new Intent(this, SmsActivity.class)
                .putExtra(SmsActivity.EXTRA_SMS_FOLDER, SmsActivity.FOLDER_INCOME));
        else if (id == R.id.sms_outcome) startActivity(new Intent(this, SmsActivity.class)
                .putExtra(SmsActivity.EXTRA_SMS_FOLDER, SmsActivity.FOLDER_OUTCOME));
        else if (id == R.id.sms_trash) startActivity(new Intent(this, SmsActivity.class)
                .putExtra(SmsActivity.EXTRA_SMS_FOLDER, SmsActivity.FOLDER_TRASH));
        else if (id == R.id.parametrs) startActivity(new Intent(this, ParamsActivity.class));
        // Календарь и Калькулятор пока не реализованы (как на MainActivity) - только Жаба
        else Toast.makeText(this, "Меню " + item.getTitle(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }
}
