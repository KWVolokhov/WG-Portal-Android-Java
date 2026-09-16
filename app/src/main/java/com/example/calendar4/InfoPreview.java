package com.example.calendar4;

import java.util.ArrayList;

/**
 * Task 139: свёрнутое превью InfoFieldView (Task 123/136) - вынесено из
 * InfoFieldView. Превью = первая непустая строка текста/таблиц + счётчики вложений.
 */
final class InfoPreview {

    private InfoPreview() {
    }

    /** Task 123: preview - первая строка текста + счётчики вложений (без лишних строк). */
    static void update(InfoFieldView view) {
        String preview = firstPreviewLine(plainPreview(view));
        String counters = attachmentCounters(view);
        if (!counters.isEmpty()) preview = preview + counters;
        view.tvCollapsed.setText(preview);
    }

    /** Plain-проекция всех текстовых и табличных блоков поля. */
    static String plainPreview(InfoFieldView view) {
        StringBuilder sb = new StringBuilder();
        for (InfoFieldView.Block b : view.blocks) {
            if (b.type.equals("text")) {
                String t = InfoFieldView.plainOfBlock(b);
                if (t.trim().isEmpty()) continue;
                if (sb.length() > 0) sb.append("\n");
                sb.append(t);
            } else if (b.type.equals("tbl") && b.cells != null) {
                String t = tablePlain(b.cells);
                if (t.isEmpty()) continue;
                if (sb.length() > 0) sb.append("\n");
                sb.append(t);
            }
        }
        return sb.toString();
    }

    // Task 123: первая непустая строка (убирает 2-е строки, добавляемые в поле над чертой)
    private static String firstPreviewLine(String plain) {
        if (plain == null) return "";
        String t = plain.trim();
        int nl = t.indexOf('\n');
        if (nl >= 0) t = t.substring(0, nl).trim();
        return t;
    }

    /** Task 136: plain projection of table cells (joined with " | "). */
    static String tablePlain(ArrayList<String> cells) {
        StringBuilder tb = new StringBuilder();
        for (String cell : cells) {
            String t = cell == null ? "" : InfoFieldView.htmlToPlain(cell).trim();
            if (t.isEmpty()) continue;
            if (tb.length() > 0) tb.append(" | ");
            tb.append(t);
        }
        return tb.toString();
    }

    // Счётчики вложений: 📷 картинки, 🎥 видео, 📎 файлы
    private static String attachmentCounters(InfoFieldView view) {
        int images = 0, videos = 0, files = 0;
        for (InfoFieldView.Block b : view.blocks) {
            if (b.type.equals("img")) images++;
            else if (b.type.equals("vid")) videos++;
            else if (b.type.equals("file")) files++;
        }
        if (images + videos + files == 0) return "";
        StringBuilder sb = new StringBuilder();
        if (images > 0) sb.append(" 📷").append(images);
        if (videos > 0) sb.append(" 🎥").append(videos);
        if (files > 0) sb.append(" 📎").append(files);
        return sb.toString();
    }
}
