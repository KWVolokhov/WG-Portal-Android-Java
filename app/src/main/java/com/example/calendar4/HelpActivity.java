package com.example.calendar4;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Task 147: экран "Help Справочник" (пункт меню над "Параметры").
 * Заглушка по образцу "Типов жизнедеятельности" (LivetypeActivity), но пока
 * БЕЗ привязки к SQL: основные элементы экрана - заголовок, поле поиска,
 * список и кнопка "назад" крестиком. Содержимое списка - демонстрационное.
 */
public class HelpActivity extends BaseScreenActivity {

    private ListView listViewHealth;
    private EditText editTextFilter;
    private TextView headerTitle;
    private ImageButton btnBack;

    private final ArrayList<String> allItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        listViewHealth = findViewById(R.id.listViewHealth);
        editTextFilter = findViewById(R.id.editTextFilter);
        headerTitle = findViewById(R.id.headerTitle);
        btnBack = findViewById(R.id.btnBack);

        headerTitle.setText("Help Справочник");

        // Заглушка: демонстрационные элементы справочника (без SQL).
        allItems.add("Календарный план");
        allItems.add("Контакты");
        allItems.add("Проекты и задачи");
        allItems.add("Здоровье");
        allItems.add("Типы жизнедеятельности");
        allItems.add("СМС");
        allItems.add("Параметры");
        allItems.add("Синхронизация");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allItems);
        listViewHealth.setAdapter(adapter);

        // Фильтр списка: от 3 символов сужаем выборку (как на других экранах).
        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s != null ? s.toString().trim() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Кнопка "назад" крестиком - закрыть экран.
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /** Простая фильтрация заглушки по подстроке (без SQL). */
    private void applyFilter(String filter) {
        adapter.clear();
        if (filter.length() >= 3) {
            String lower = filter.toLowerCase();
            for (String item : allItems) {
                if (item.toLowerCase().contains(lower)) adapter.add(item);
            }
        } else {
            adapter.addAll(allItems);
        }
    }
}
