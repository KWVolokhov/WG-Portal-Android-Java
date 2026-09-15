package com.example.calendar4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.HashMap;
import java.util.Map;

/**
 * Task 36/132: builds the row icon for Проекты / Задачи / Заявки
 * depending on the record status.
 * <p>
 * The base shape (папка / кружок с галочкой / документ) is drawn in a state color:
 * <ul>
 *     <li>Черновик (Draft)        - серый folder + слово "Draft"</li>
 *     <li>Тестирование (Intest)   - зелёный folder + слово "Test"</li>
 *     <li>Выполнено (Done)        - зелёный folder + слово "Ok"</li>
 *     <li>В работе (Inwork)       - белый folder + слово "Work"</li>
 *     <li>Отложено (Hold)         - белый folder + слово "Hold"</li>
 *     <li>Отменено (Canceled)     - белый folder + слово "Cancel"</li>
 * </ul>
 * Records without a known status keep the plain, unchanged icon.
 */
public final class StatusIconFactory {

    // State base colors
    private static final int COLOR_GRAY  = Color.rgb(158, 158, 158);
    private static final int COLOR_GREEN = Color.rgb(76, 175, 80);
    private static final int COLOR_WHITE = Color.WHITE;
    private static final int COLOR_DARK  = Color.rgb(33, 33, 33);

    /** Simple cache: identical (form, state, word) always produce the same drawable. */
    private static final Map<String, Drawable> CACHE = new HashMap<>();

    private StatusIconFactory() {
    }

    /**
     * Returns the ready-to-show icon for the given CALPLAN record.
     *
     * @param form        Form value: "Project" / "Task" / "Request"
     * @param statusId    StatusID value: "Draft", "Inwork", "Intest", "Done", "Hold", "Canceled"
     * @param statusLabel Russian status label (used as a fallback when StatusID is empty)
     */
    public static Drawable getStatusDrawable(Context context, String form, String statusId, String statusLabel) {
        String state = normalizeStatus(statusId, statusLabel);
        if (state == null) {
            // No recognized status - plain base icon without letter/remap colors
            return ContextCompat.getDrawable(context, baseIconForForm(form));
        }

        int baseRes = baseIconForForm(form);
        int shapeColor = shapeColorForStatus(state);
        String word = wordForStatus(state);

        String key = baseRes + "|" + shapeColor + "|" + word;
        Drawable cached = CACHE.get(key);
        if (cached != null) return cached;

        Drawable result = compose(context, baseRes, shapeColor, word);
        CACHE.put(key, result);
        return result;
    }

    // ---------------------------------------------------------------------
    // Mapping helpers
    // ---------------------------------------------------------------------

    private static int baseIconForForm(String form) {
        if ("Task".equals(form)) return R.drawable.ic_type_task;
        if ("Request".equals(form)) return R.drawable.ic_type_request;
        return R.drawable.ic_type_project;
    }
/** Returns a normalized single-token state, or null when not recognized. */
    private static String normalizeStatus(String statusId, String statusLabel) {
        if (statusId != null) {
            String s = statusId.trim();
            if ("Draft".equalsIgnoreCase(s)) return "Draft";
            if ("Inwork".equalsIgnoreCase(s)) return "Inwork";
            if ("Intest".equalsIgnoreCase(s)) return "Intest";
            if ("Done".equalsIgnoreCase(s)) return "Done";
            if ("Hold".equalsIgnoreCase(s)) return "Hold";
            if ("Canceled".equalsIgnoreCase(s) || "Cancelled".equalsIgnoreCase(s)) return "Canceled";
        }
        if (statusLabel != null) {
            String s = statusLabel.trim();
            if ("Черновик".equalsIgnoreCase(s)) return "Draft";
            if ("В работе".equalsIgnoreCase(s)) return "Inwork";
            if ("Тестирование".equalsIgnoreCase(s)) return "Intest";
            if ("Выполнено".equalsIgnoreCase(s)) return "Done";
            if ("Отложено".equalsIgnoreCase(s)) return "Hold";
            if ("Отменено".equalsIgnoreCase(s) || "Отменён".equalsIgnoreCase(s)) return "Canceled";
        }
        return null;
    }

    private static int shapeColorForStatus(String state) {
        switch (state) {
            case "Draft":    return COLOR_GRAY;
            case "Intest":   return COLOR_GREEN;
            case "Done":     return COLOR_GREEN;
            case "Inwork":   return COLOR_WHITE;
            case "Hold":     return COLOR_WHITE;
            case "Canceled": return COLOR_WHITE;
            default:         return 0; // keep the original icon color
        }
    }

    /** Task 132: полное слово статуса, которое рисуется на иконке. */
    private static String wordForStatus(String state) {
        switch (state) {
            case "Draft":    return "Draft";
            case "Inwork":   return "Work";
            case "Intest":   return "Test";
            case "Done":     return "Ok";
            case "Hold":     return "Hold";
            case "Canceled": return "Cancel";
            default:         return "";
        }
    }

    /** Task 132: слово тёмное на белых папках и белое на цветных. */
    private static int wordColorFor(int shapeColor) {
        return shapeColor == COLOR_WHITE ? COLOR_DARK : Color.WHITE;
    }

    // ---------------------------------------------------------------------
    // Drawing
    // ---------------------------------------------------------------------

    /**
     * Draws the state icon: the base vector shape tinted with the state color and
     * the full state word (Draft/Work/Test/Ok/Hold/Cancel) centered on top of it.
     */
    private static Drawable compose(Context context, int baseRes, int shapeColor, String word) {
        int sizePx = Math.round(48 * context.getResources().getDisplayMetrics().density);

        Drawable base = ContextCompat.getDrawable(context, baseRes);
        if (base == null) return ContextCompat.getDrawable(context, R.drawable.ic_type_project);

        Drawable icon = DrawableCompat.wrap(base.mutate());
        if (shapeColor != 0) {
            DrawableCompat.setTint(icon, shapeColor);
        }

        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.setBounds(0, 0, sizePx, sizePx);
        icon.draw(canvas);

        if (word != null && !word.isEmpty()) drawStateWord(canvas, sizePx, shapeColor, word);

        return new BitmapDrawable(context.getResources(), bitmap);
    }

    /** Task 132: слово статуса целиком; шрифт уменьшается, чтобы слово уместилось по ширине иконки. */
    private static void drawStateWord(Canvas canvas, int sizePx, int shapeColor, String word) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(wordColorFor(shapeColor));
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(sizePx * 0.30f);
        float maxWidth = sizePx * 0.88f;
        float wordWidth = paint.measureText(word);
        if (wordWidth > maxWidth) paint.setTextSize(sizePx * 0.30f * maxWidth / wordWidth);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float baseline = sizePx / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(word, sizePx / 2f, baseline, paint);
    }
}