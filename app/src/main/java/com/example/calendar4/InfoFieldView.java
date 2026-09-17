package com.example.calendar4;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Task 100: "Информация"/"Описание" field built on top of RecyclerView
 * (see InfoBlocksAdapter for the item views).
 *
 * The value is a sequence of blocks: text (formatting kept as HTML spans), images,
 * videos and arbitrary files. Images/videos show as thumbnails sized to 1/4 of the
 * row width; a video never starts playing by itself - a preview frame with a play
 * icon is shown, and a tap opens the standard Android viewer (fullscreen when the
 * viewer supports it).
 *
 * SQL storage: the same TEXT columns as before, but the value is a JSON document
 * ({@code {"v":1,"blocks":[{"t":"text","s":...},{"t":"img","f":...},
 * {"t":"vid","f":...},{"t":"file","f":...,"n":...}]}}). Legacy plain text values
 * stay plain text, so existing records and list screens keep working. The
 * attachment files themselves are stored in a separate folder (AttachmentStore);
 * the folder name and the database name are kept as two text fields in the
 * parameters (CALPARAM.AttachFolder / CALPARAM.DBName).
 */
public class InfoFieldView extends LinearLayout {

    // Attachment picker request codes (forwarded by host activities, see onHostActivityResult)
    private static final int REQ_PICK_IMAGE = 4101;
    private static final int REQ_PICK_VIDEO = 4102;
    private static final int REQ_PICK_FILE = 4103;

    /** The view that currently waits for a picker result (one picker at a time). */
    private static InfoFieldView activePickerView;

    /** One content block of the field (package-private for InfoBlocksAdapter). */
    static class Block {
        String type;   // "text" | "img" | "vid" | "file" | "tbl"
        String text;   // "text": plain text, or HTML when isHtml = true
        boolean isHtml;
        String file;   // attachment file name inside the attachments folder
        String name;   // display caption for "file" blocks
        int rows, cols;          // "tbl": table size (Task 136)
        ArrayList<String> cells; // "tbl": HTML per cell, size = rows * cols (Task 136)
    }

    final ArrayList<Block> blocks = new ArrayList<>();
    final Map<String, Bitmap> thumbs = new HashMap<>();
    CharSequence hint = "";

    TextView tvCollapsed;
    private LinearLayout expandedPane;
    RecyclerView recycler;
    InfoBlocksAdapter adapter;

    public InfoFieldView(Context context) {
        this(context, null);
    }

    public InfoFieldView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfoFieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        // ----- collapsed: 2-line read-only preview (looks like an EditText) -----
        tvCollapsed = new TextView(context);
        tvCollapsed.setMaxLines(2);
        tvCollapsed.setSingleLine(false);
        tvCollapsed.setFocusable(true);
        tvCollapsed.setClickable(true);
        tvCollapsed.setBackgroundResource(R.drawable.bg_underline);
        tvCollapsed.setGravity(Gravity.START | Gravity.TOP);
        tvCollapsed.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        tvCollapsed.setTextIsSelectable(true);
        tvCollapsed.setOnClickListener(v -> expand());
        addView(tvCollapsed, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // ----- expanded: панель с RecyclerView + кнопки (высота по содержимому) -----
        expandedPane = new LinearLayout(context);
        expandedPane.setOrientation(VERTICAL);
        expandedPane.setVisibility(GONE);
        expandedPane.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(expandedPane);

        recycler = new RecyclerView(context);
        recycler.setVerticalScrollBarEnabled(true);
        recycler.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        expandedPane.addView(recycler);

        adapter = new InfoBlocksAdapter(this);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setAdapter(adapter);
        // Media thumbnails are 1/4 of the row width -> rebind when the width changes
        recycler.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, orr, ob) -> {
            if ((r - l) != (orr - ol)) adapter.notifyDataSetChanged();
        });

        HorizontalScrollView buttonsScroller = new HorizontalScrollView(context);
        buttonsScroller.setHorizontalScrollBarEnabled(false);
        buttonsScroller.addView(buildButtonsRow(context));
        expandedPane.addView(buttonsScroller, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    /** Tool buttons row: collapse (left) + text / picture / video / file / table / bold / color (right). */
    private LinearLayout buildButtonsRow(Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.topMargin = dpToPx(4);
        row.setLayoutParams(lp);

        ImageButton collapseBtn = new ImageButton(context);
        collapseBtn.setImageResource(R.drawable.ic_cancel_gray);
        collapseBtn.setBackgroundColor(Color.TRANSPARENT);
        collapseBtn.setContentDescription("Свернуть");
        collapseBtn.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(32), dpToPx(32)));
        collapseBtn.setOnClickListener(v -> collapse());
        row.addView(collapseBtn);

        View spacer = new View(context);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1f));
        row.addView(spacer);

        row.addView(makeToolButton(context, R.drawable.ic_add_plus, "Добавить текст", () -> addTextBlock()));
        row.addView(makeToolButton(context, R.drawable.ic_attach_image, "Добавить картинку",
                () -> startPick(REQ_PICK_IMAGE, "Добавить картинку")));
        row.addView(makeToolButton(context, R.drawable.ic_attach_video, "Добавить видео",
                () -> startPick(REQ_PICK_VIDEO, "Добавить видео")));
        row.addView(makeToolButton(context, R.drawable.ic_attach_file, "Добавить файл",
                () -> startPick(REQ_PICK_FILE, "Добавить файл")));
        row.addView(makeToolButton(context, R.drawable.ic_add_table, "Добавить таблицу", this::showTableSizeDialog));
        row.addView(makeToolButton(context, R.drawable.ic_bold_t, "Жирный текст", this::toggleBoldOnEditor));
        row.addView(makeToolButton(context, R.drawable.ic_text_color, "Цвет текста", this::showColorPickerDialog));
        return row;
    }

    private ImageButton makeToolButton(Context context, int iconRes, String desc, final Runnable action) {
        ImageButton b = new ImageButton(context);
        b.setImageResource(iconRes);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setContentDescription(desc);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dpToPx(32), dpToPx(32));
        lp.leftMargin = dpToPx(8);
        b.setLayoutParams(lp);
        b.setOnClickListener(v -> action.run());
        return b;
    }

    /** Appends an empty text block and scrolls the editor to it. */
    private void addTextBlock() {
        Block b = new Block();
        b.type = "text";
        b.text = "";
        b.isHtml = false;
        blocks.add(b);
        adapter.notifyItemInserted(blocks.size() - 1);
        recycler.post(() -> recycler.smoothScrollToPosition(blocks.size() - 1));
    }

    // ------------------------------------------------------------------
    // Task 136: таблицы, жирный текст, цвет текста
    // ------------------------------------------------------------------

    /** Task 136: диалог выбора количества строк и колонок, затем вставка таблицы. */
    private void showTableSizeDialog() {
        Context ctx = getContext();
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(VERTICAL);
        box.setPadding(dpToPx(20), dpToPx(8), dpToPx(20), 0);

        TextView rowsLabel = new TextView(ctx);
        rowsLabel.setText("Строки");
        rowsLabel.setTextAppearance(ctx, android.R.style.TextAppearance_Small);
        box.addView(rowsLabel);
        final NumberPicker rowsPicker = new NumberPicker(ctx);
        rowsPicker.setMinValue(1);
        rowsPicker.setMaxValue(10);
        rowsPicker.setValue(2);
        rowsPicker.setWrapSelectorWheel(false);
        box.addView(rowsPicker);

        TextView colsLabel = new TextView(ctx);
        colsLabel.setText("Колонки");
        colsLabel.setTextAppearance(ctx, android.R.style.TextAppearance_Small);
        box.addView(colsLabel);
        final NumberPicker colsPicker = new NumberPicker(ctx);
        colsPicker.setMinValue(1);
        colsPicker.setMaxValue(10);
        colsPicker.setValue(2);
        colsPicker.setWrapSelectorWheel(false);
        box.addView(colsPicker);

        new AlertDialog.Builder(ctx)
                .setTitle("Размер таблицы")
                .setView(box)
                .setPositiveButton("Вставить",
                        (d, w) -> addTableBlock(rowsPicker.getValue(), colsPicker.getValue()))
                .setNegativeButton("Отмена", null)
                .show();
    }

    /** Task 136: appends a table block with rows*cols editable cells. */
    private void addTableBlock(int rows, int cols) {
        Block b = new Block();
        b.type = "tbl";
        b.rows = rows;
        b.cols = cols;
        b.cells = new ArrayList<>();
        for (int i = 0; i < rows * cols; i++) b.cells.add("");
        blocks.add(b);
        expand();
        adapter.notifyItemInserted(blocks.size() - 1);
        recycler.post(() -> recycler.smoothScrollToPosition(blocks.size() - 1));
        updateCollapsed();
    }

    /** Task 136: жирный/нежирный - выделение, а без выделения весь текст последнего EditText. */
    private void toggleBoldOnEditor() {
        InfoTextFormat.toggleBold(this);
    }

    /** Task 136: vertical row of 7 colors; the pick recolors the last focused EditText. */
    private void showColorPickerDialog() {
        InfoTextFormat.showColorPicker(this);
    }

    /** Starts the system content picker through the host activity. */
    private void startPick(int reqCode, String title) {
        Context ctx = getContext();
        if (!(ctx instanceof android.app.Activity)) {
            Toast.makeText(ctx, "Выбор вложения недоступен", Toast.LENGTH_SHORT).show();
            return;
        }
        String mime;
        if (reqCode == REQ_PICK_IMAGE) mime = "image/*";
        else if (reqCode == REQ_PICK_VIDEO) mime = "video/*";
        else mime = "*/*";
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mime);
        activePickerView = this;
        try {
            ((android.app.Activity) ctx).startActivityForResult(
                    Intent.createChooser(intent, title), reqCode);
        } catch (Exception e) {
            activePickerView = null;
            Toast.makeText(ctx, "Не найдено приложение для выбора", Toast.LENGTH_SHORT).show();
        }
    }

    /** Host activities must forward onActivityResult() here (Task 100). */
    public static boolean onHostActivityResult(int requestCode, int resultCode, Intent data) {
        if (activePickerView == null) return false;
        boolean handled = activePickerView.handleActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE || requestCode == REQ_PICK_VIDEO || requestCode == REQ_PICK_FILE) activePickerView = null;
        return handled;
    }

    private boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != android.app.Activity.RESULT_OK || data == null || data.getData() == null) return false;
        Uri uri = data.getData();
        String stored = AttachmentStore.addFromUri(getContext(), uri);
        if (stored == null) {
            Toast.makeText(getContext(), "Не удалось сохранить вложение", Toast.LENGTH_SHORT).show();
            return true;
        }
        Block b = new Block();
        b.file = stored;
        String mime = AttachmentStore.mimeOf(getContext(), uri, stored);
        if (mime.startsWith("image/")) b.type = "img";
        else if (mime.startsWith("video/")) b.type = "vid";
        else {
            b.type = "file";
            b.name = AttachmentStore.displayName(getContext(), uri, stored);
        }
        blocks.add(b);
        expand();
        adapter.notifyItemInserted(blocks.size() - 1);
        updateCollapsed();
        return true;
    }

    // ------------------------------------------------------------------
    // Expanded / collapsed state
    // ------------------------------------------------------------------

    private void expand() {
        tvCollapsed.setVisibility(GONE);
        expandedPane.setVisibility(VISIBLE);
    }

    private void collapse() {
        View focused = getFocusedChild();
        if (focused != null) focused.clearFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(getWindowToken(), 0);
        expandedPane.setVisibility(GONE);
        tvCollapsed.setVisibility(VISIBLE);
        dropEmptyTextBlocks();
        ensureTextLine();
        adapter.notifyDataSetChanged();
        updateCollapsed();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (activePickerView == this) activePickerView = null;
        super.onDetachedFromWindow();
    }

    // ------------------------------------------------------------------
    // Public API (compatible with the previous implementation)
    // ------------------------------------------------------------------

    /** Loads a stored value: a JSON document or legacy plain text. */
    public void setText(String stored) {
        blocks.clear();
        thumbs.clear();
        parseValue(stored);
        dropEmptyTextBlocks();
        ensureTextLine();
        updateCollapsed();
        adapter.notifyDataSetChanged();
    }

    /** Task 127: пустые текстовые блоки (над и под заполненным текстом) не показываются. */
    private void dropEmptyTextBlocks() {
        for (int i = blocks.size() - 1; i >= 0; i--) {
            Block b = blocks.get(i);
            if (b.type.equals("text") && plainOfBlock(b).trim().isEmpty()) blocks.remove(i);
        }
    }

    /** Task 127: строка под текст есть сразу, даже если записи в SQL ещё нет; Task 141: текстовый блок всегда ПЕРВЫЙ. */
    private void ensureTextLine() {
        if (!blocks.isEmpty() && blocks.get(0).type.equals("text")) return;
        Block b = new Block();
        b.type = "text";
        b.text = "";
        b.isHtml = false;
        blocks.add(0, b);
    }

    public void setHint(CharSequence hint) {
        this.hint = hint == null ? "" : hint;
        tvCollapsed.setHint(this.hint);
        adapter.notifyDataSetChanged();
    }

    /** Returns the full stored value (a JSON document or plain text). */
    public String getText() {
        return serializeValue();
    }

    /** Row width for the media thumbnails (1/4 of it is used). */
    int recyclerWidth() {
        return recycler.getWidth();
    }

    // ------------------------------------------------------------------
    // Value (de)serialization
    // ------------------------------------------------------------------

    private void parseValue(String stored) {
        if (stored == null) stored = "";
        String s = stored.trim();
        if (s.startsWith("{") && s.endsWith("}")) {
            try {
                JSONObject root = new JSONObject(s);
                JSONArray arr = root.optJSONArray("blocks");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.optJSONObject(i);
                        if (o == null) continue;
                        Block b = new Block();
                        b.type = o.optString("t", "text");
                        if (b.type.equals("text")) {
                            b.text = o.optString("s", "");
                            b.isHtml = true;
                        } else if (b.type.equals("tbl")) {
                            b.rows = Math.max(1, o.optInt("r", 2));
                            b.cols = Math.max(1, o.optInt("c", 2));
                            b.cells = new ArrayList<>();
                            JSONArray cs = o.optJSONArray("cells");
                            int need = b.rows * b.cols;
                            for (int j = 0; j < need; j++) b.cells.add(cs != null && j < cs.length() ? cs.optString(j, "") : "");
                        } else {
                            b.file = o.optString("f", "");
                            b.name = o.optString("n", null);
                        }
                        blocks.add(b);
                    }
                    return;
                }
            } catch (Exception e) {
                // not a JSON document - fall through to plain text
            }
        }
        Block b = new Block();
        b.type = "text";
        b.text = stored;
        b.isHtml = false;
        blocks.add(b);
    }

    /** Builds the stored value: plain text when possible, a JSON document otherwise. */
    private String serializeValue() {
        ArrayList<Block> keep = new ArrayList<>();
        for (Block b : blocks) {
            if (b.type.equals("text") && plainOfBlock(b).trim().isEmpty()) continue;
            keep.add(b);
        }
        if (keep.isEmpty()) return "";
        if (keep.size() == 1 && keep.get(0).type.equals("text")) {
            Block b = keep.get(0);
            if (!b.isHtml) return b.text;                               // legacy value, unchanged
            if (!hasHtmlFormatting(b.text)) return htmlToPlain(b.text); // edited plain text
        }
        try {
            JSONObject root = new JSONObject();
            root.put("v", 1);
            JSONArray arr = new JSONArray();
            for (Block b : keep) {
                JSONObject o = new JSONObject();
                o.put("t", b.type);
                if (b.type.equals("text")) o.put("s", b.text == null ? "" : b.text);
                else if (b.type.equals("tbl")) {
                    o.put("r", b.rows);
                    o.put("c", b.cols);
                    JSONArray cs = new JSONArray();
                    for (String cell : b.cells) cs.put(cell == null ? "" : cell);
                    o.put("cells", cs);
                } else {
                    o.put("f", b.file == null ? "" : b.file);
                    if (b.name != null && !b.name.isEmpty()) o.put("n", b.name);
                }
                arr.put(o);
            }
            root.put("blocks", arr);
            return root.toString();
        } catch (Exception e) {
            return plainPreview();
        }
    }

    static String plainOfBlock(Block b) {
        if (b.text == null) return "";
        return b.isHtml ? htmlToPlain(b.text) : b.text;
    }

    private static boolean hasHtmlFormatting(String html) {
        if (html == null) return false;
        String h = html.toLowerCase(Locale.US);
        return h.contains("<b>") || h.contains("<i>") || h.contains("<u>") || h.contains("<s>")
                || h.contains("<font") || h.contains("<span") || h.contains("<big")
                || h.contains("<small") || h.contains("<tt") || h.contains("<blockquote")
                || h.contains("<ul") || h.contains("<ol") || h.contains("<li");
    }

    static String htmlToPlain(String html) {
        if (html == null) return "";
        try { return Html.fromHtml(html).toString(); } catch (Exception e) { return html; }
    }

    /**
     * Task 143: fromHtml для загрузки блока - хвостовые \n (от <p>/<br> обёртки toHtml)
     * обрезаются, чтобы лишние строки не накапливались при каждом редактировании.
     */
    static CharSequence fromHtmlTrimmed(String html) {
        CharSequence cs;
        try { cs = Html.fromHtml(html == null ? "" : html); } catch (Exception e) { cs = html == null ? "" : html; }
        int end = cs.length();
        while (end > 0 && Character.isWhitespace(cs.charAt(end - 1))) end--;
        if (end == cs.length()) return cs;
        return new android.text.SpannableString(cs.subSequence(0, end));
    }

    /** Task 143: toHtml для записи блока - хвостовые \n пользователя не сохраняются. */
    static String toHtmlTrimmed(CharSequence s) {
        int end = s == null ? 0 : s.length();
        while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) end--;
        try { return Html.toHtml(new android.text.SpannableString(s.subSequence(0, end))); } catch (Exception e) { return s == null ? "" : s.toString(); }
    }

    /** Text-only projection of a stored value (JSON or legacy text) for the list screens. */
    public static String plainText(String stored) {
        if (stored == null) return "";
        String s = stored.trim();
        if (s.startsWith("{") && s.endsWith("}")) {
            try {
                JSONObject root = new JSONObject(s);
                JSONArray arr = root.optJSONArray("blocks");
                StringBuilder sb = new StringBuilder();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.optJSONObject(i);
                        if (o == null) continue;
                        if ("text".equals(o.optString("t"))) {
                            if (sb.length() > 0) sb.append("\n");
                            sb.append(Html.fromHtml(o.optString("s", "")).toString());
                        } else if ("tbl".equals(o.optString("t"))) {
                            JSONArray cs = o.optJSONArray("cells");
                            if (cs == null) continue;
                            ArrayList<String> cells = new ArrayList<>();
                            for (int j = 0; j < cs.length(); j++) cells.add(cs.optString(j, ""));
                            String t = InfoPreview.tablePlain(cells);
                            if (t.isEmpty()) continue;
                            if (sb.length() > 0) sb.append("\n");
                            sb.append(t);
                        }
                    }
                }
                return sb.toString();
            } catch (Exception e) {
                // fall through - not a JSON document
            }
        }
        return stored;
    }

    /** Task 142: text of the stored value; when the Info field is empty - the Comment field. */
    public static String listText(String stored, String comment) {
        String t = plainText(stored);
        if (t != null && !t.trim().isEmpty()) return t;
        return comment == null ? "" : comment;
    }

    // ------------------------------------------------------------------
    // Collapsed preview
    // ------------------------------------------------------------------

    /** Task 123: preview - первая строка текста + счётчики вложений (Task 139: в InfoPreview). */
    public void updateCollapsed() {
        InfoPreview.update(this);
    }

    // Plain-проекция блоков поля (для getText/превью) - см. InfoPreview
    private String plainPreview() {
        return InfoPreview.plainPreview(this);
    }

    int dpToPx(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return Math.round(dp * scale);
    }
}
