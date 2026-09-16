package com.example.calendar4;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Task 139: форматирование текста в InfoFieldView (Ж-жирный и цвет текста,
 * Task 136) - вынесено из InfoFieldView. Применяется к выделению, а без выделения
 * - ко всему тексту последнего фокусного EditText.
 */
final class InfoTextFormat {

    /** Task 136: vertical row of 7 text colors in the color picker dialog. */
    private static final int[] TEXT_COLORS = {
            0xFF000000, 0xFFE53935, 0xFFFB8C00, 0xFF43A047,
            0xFF1E88E5, 0xFF8E24AA, 0xFF757575
    };

    private InfoTextFormat() {
    }

    /** Task 136: жирный/нежирный - выделение, а без выделения весь текст последнего EditText. */
    static void toggleBold(InfoFieldView view) {
        EditText et = view.adapter.focusedOrLastEditor();
        if (et == null) {
            Toast.makeText(view.getContext(), "Поставьте курсор в текст", Toast.LENGTH_SHORT).show();
            return;
        }
        int[] range = targetRange(et);
        if (range[0] >= range[1]) {
            Toast.makeText(view.getContext(), "Нет текста для форматирования", Toast.LENGTH_SHORT).show();
            return;
        }
        Editable s = et.getText();
        if (rangeFullyBold(s, range[0], range[1])) removeStyleSpans(s, range[0], range[1], Typeface.BOLD);
        else s.setSpan(new StyleSpan(Typeface.BOLD), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.adapter.writeBack(et);
    }

    /** Task 136: vertical row of 7 colors; the pick recolors the last focused EditText. */
    static void showColorPicker(InfoFieldView view) {
        final EditText editor = view.adapter.focusedOrLastEditor();
        if (editor == null) {
            Toast.makeText(view.getContext(), "Поставьте курсор в текст", Toast.LENGTH_SHORT).show();
            return;
        }
        Context ctx = view.getContext();
        LinearLayout column = new LinearLayout(ctx);
        column.setOrientation(LinearLayout.VERTICAL);
        int pad = view.dpToPx(24);
        column.setPadding(pad, pad, pad, pad);
        final AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle("Цвет текста")
                .setView(column)
                .create();
        for (final int color : TEXT_COLORS) {
            View swatch = new View(ctx);
            swatch.setBackgroundColor(color);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(view.dpToPx(140), view.dpToPx(32));
            lp.bottomMargin = view.dpToPx(8);
            swatch.setLayoutParams(lp);
            swatch.setOnClickListener(v -> {
                applyColorToEditor(editor, color);
                dialog.dismiss();
            });
            column.addView(swatch);
        }
        dialog.show();
    }

    // Task 136: recolors the selection (or the whole text), replacing existing color spans
    private static void applyColorToEditor(EditText et, int color) {
        int[] range = targetRange(et);
        if (range[0] >= range[1]) return;
        Editable s = et.getText();
        for (ForegroundColorSpan span : s.getSpans(range[0], range[1], ForegroundColorSpan.class)) {
            int a = s.getSpanStart(span), b = s.getSpanEnd(span);
            int flags = s.getSpanFlags(span);
            int old = span.getForegroundColor();
            s.removeSpan(span);
            if (a < range[0]) s.setSpan(new ForegroundColorSpan(old), a, range[0], flags);
            if (range[1] < b) s.setSpan(new ForegroundColorSpan(old), range[1], b, flags);
        }
        s.setSpan(new ForegroundColorSpan(color), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    // Selection bounds of an editor; without a selection - the whole text
    private static int[] targetRange(EditText et) {
        int a = et.getSelectionStart(), b = et.getSelectionEnd();
        int start = Math.min(a, b), end = Math.max(a, b);
        if (start < 0 || end < 0 || start == end) { start = 0; end = et.length(); }
        return new int[]{start, end};
    }

    // True when every character in [start, end) carries a BOLD StyleSpan
    private static boolean rangeFullyBold(Editable s, int start, int end) {
        ArrayList<int[]> parts = new ArrayList<>();
        for (StyleSpan span : s.getSpans(start, end, StyleSpan.class)) {
            if ((span.getStyle() & Typeface.BOLD) == 0) continue;
            int a = Math.max(start, s.getSpanStart(span)), b = Math.min(end, s.getSpanEnd(span));
            if (a < b) parts.add(new int[]{a, b});
        }
        if (parts.isEmpty()) return false;
        Collections.sort(parts, (p, q) -> p[0] - q[0]);
        int covered = start;
        for (int[] p : parts) {
            if (p[0] > covered) return false;
            if (p[1] > covered) covered = p[1];
        }
        return covered >= end;
    }

    // Removes (with splitting) all StyleSpans of the given style crossing [start, end)
    private static void removeStyleSpans(Editable s, int start, int end, int style) {
        for (StyleSpan span : s.getSpans(start, end, StyleSpan.class)) {
            if ((span.getStyle() & style) == 0) continue;
            int a = s.getSpanStart(span), b = s.getSpanEnd(span);
            int flags = s.getSpanFlags(span);
            s.removeSpan(span);
            if (a < start) s.setSpan(new StyleSpan(style), a, start, flags);
            if (end < b) s.setSpan(new StyleSpan(style), end, b, flags);
        }
    }
}
