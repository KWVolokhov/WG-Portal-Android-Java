package com.example.calendar4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task 150: системное соответствие версий Android API -> название версии.
 * Хранится как static HashMap от 1-й версии, чтобы понимать всё соответствие,
 * включая смартчасы (Android Wear / Wear OS). Используется на экране "О Программе"
 * для показа допустимой/текущей версии и полного списка в диалоге.
 */
public final class AndroidVersions {

    // API level -> название версии Android (включая Wear / смартчасы).
    private static final Map<Integer, String> API_TO_VERSION = new HashMap<>();

    static {
        API_TO_VERSION.put(1, "1.0");
        API_TO_VERSION.put(2, "1.1 Petit Four");
        API_TO_VERSION.put(3, "1.5 Cupcake");
        API_TO_VERSION.put(4, "1.6 Donut");
        API_TO_VERSION.put(5, "2.0 Eclair");
        API_TO_VERSION.put(6, "2.0.1 Eclair");
        API_TO_VERSION.put(7, "2.1 Eclair");
        API_TO_VERSION.put(8, "2.2 Froyo");
        API_TO_VERSION.put(9, "2.3 Gingerbread");
        API_TO_VERSION.put(10, "2.3.3 Gingerbread");
        API_TO_VERSION.put(11, "3.0 Honeycomb (планшет)");
        API_TO_VERSION.put(12, "3.1 Honeycomb (планшет)");
        API_TO_VERSION.put(13, "3.2 Honeycomb (планшет)");
        API_TO_VERSION.put(14, "4.0 Ice Cream Sandwich");
        API_TO_VERSION.put(15, "4.0.3 Ice Cream Sandwich");
        API_TO_VERSION.put(16, "4.1 Jelly Bean");
        API_TO_VERSION.put(17, "4.2 Jelly Bean");
        API_TO_VERSION.put(18, "4.3 Jelly Bean");
        API_TO_VERSION.put(19, "4.4 KitKat");
        API_TO_VERSION.put(20, "4.4W KitKat Watch (смартчасы)");
        API_TO_VERSION.put(21, "5.0 Lollipop");
        API_TO_VERSION.put(22, "5.1 Lollipop");
        API_TO_VERSION.put(23, "6.0 Marshmallow (Android Wear 1.x)");
        API_TO_VERSION.put(24, "7.0 Nougat");
        API_TO_VERSION.put(25, "7.1 Nougat (Wear 2.0)");
        API_TO_VERSION.put(26, "8.0 Oreo");
        API_TO_VERSION.put(27, "8.1 Oreo");
        API_TO_VERSION.put(28, "9 Pie (Wear OS)");
        API_TO_VERSION.put(29, "10");
        API_TO_VERSION.put(30, "11 (Wear OS 3)");
        API_TO_VERSION.put(31, "12");
        API_TO_VERSION.put(32, "12L");
        API_TO_VERSION.put(33, "13");
        API_TO_VERSION.put(34, "14");
        API_TO_VERSION.put(35, "15");
        API_TO_VERSION.put(36, "16");
    }

    private AndroidVersions() {
    }

    /** Название версии Android для API; "неизвестно" - если соответствия нет. */
    public static String versionName(int api) {
        String name = API_TO_VERSION.get(api);
        return name != null ? name : "неизвестно";
    }

    /** Строки "Api X - <версия>", отсортированные по возрастанию API (для диалога). */
    public static String[] allEntries() {
        List<Integer> keys = new ArrayList<>(API_TO_VERSION.keySet());
        Collections.sort(keys);
        String[] out = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            int api = keys.get(i);
            out[i] = "Api " + api + " - " + API_TO_VERSION.get(api);
        }
        return out;
    }
}
