package com.example.calendar4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.stream.Collectors;

/**
 * Reusable "Информация" control for the contact card.
 *
 * Collapsed state (default): a read-only TextView styled like an EditText, showing at
 * most 2 lines of text with mandatory line wrapping (long words are broken if they do
 * not fit in width) and ellipsize="end" (the visible part is cut with "..." if the
 * text is longer). The full text is always stored internally and returned by
 * {@link #getText()}.
 *
 * Expanded state (when the field receives focus or is tapped): the 2-line TextView is
 * replaced by a scrollable window - a ScrollView containing a multi-line EditText
 * (inputType=textMultiLine, gravity=top, scrollbars=vertical), so the whole text can
 * be edited and scrolled while words wrap to the next line. Pressing "done" on the
 * keyboard or losing focus collapses the control back to the 2-line TextView.
 *
 * The whole control has a contrasting border (see bg_info_field.xml). Its collapsed
 * height is only 2 lines, so the phone field above (editTextPhone) and the phone
 * field below (editTextPhone2) stay visible on the screen at the same time.
 */
public class InfoFieldView extends LinearLayout {

    /** Expanded (editing) height in dp. */
    private static final int EXPANDED_HEIGHT_DP = 160;

    private TextView tvCollapsed;
    private EditText etExpanded;
    private ScrollView svContainer;

    /** Full text (the truncated 2-line view is only for display). */
    private String fullText = "";

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

    @SuppressLint("ResourceAsColor")
    private void init(Context context) {
        setOrientation(VERTICAL);
        // The frame/border drawable + inner padding create the "field with border" look.
        //setBackgroundResource(R.drawable.bg_info_field);
        setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        // ----- collapsed: 2-line read-only TextView (looks like an EditText) -----
        tvCollapsed = new TextView(context);
        tvCollapsed.setMaxLines(2);
        //tvCollapsed.setEllipsize(TextUtils.TruncateAt.END);
        tvCollapsed.setSingleLine(false);
        tvCollapsed.setFocusable(true);
        tvCollapsed.setClickable(true);
        tvCollapsed.setBackgroundResource(R.drawable.bg_underline);
        tvCollapsed.setGravity(Gravity.START | Gravity.TOP);
        tvCollapsed.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        tvCollapsed.setTextIsSelectable(true);
        // Expand only on tap (auto-expand on focus would break long-press selection)
        tvCollapsed.setOnClickListener(v -> expand());
		
		
		/*tvCollapsed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Код для открытия окна редактирования (например, запуск новой Activity)
			}
		}); // Тут явно еще надо искать решение с длинным тапом по выделению */

        addView(tvCollapsed, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // ----- expanded: ScrollView window with a scrollable multi-line EditText
        // (inputType=textMultiLine, gravity=top, scrollbars=vertical). The EditText
        // has no maxLines cap, so it is measured at its full content height and the
        // ScrollView scrolls long text; ScrollingMovementMethod stays as a backup so
        // the EditText itself can also scroll if needed -----
        svContainer = new ScrollView(context);
        svContainer.setVerticalScrollBarEnabled(true);
        svContainer.setVisibility(GONE);
        svContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, dpToPx(EXPANDED_HEIGHT_DP)));
        addView(svContainer);

        etExpanded = new EditText(context);
        etExpanded.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etExpanded.setSingleLine(false);
        etExpanded.setMinLines(3);
        etExpanded.setGravity(Gravity.START | Gravity.TOP);
        etExpanded.setVerticalScrollBarEnabled(true);
        etExpanded.setMovementMethod(ScrollingMovementMethod.getInstance());
        etExpanded.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etExpanded.setBackground(null);
        etExpanded.setHorizontalScrollBarEnabled(false);
        etExpanded.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        etExpanded.setBackgroundResource(R.drawable.bg_underline);
        svContainer.addView(etExpanded);

        etExpanded.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) collapse();
        });
        etExpanded.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                collapse();
                return true;
            }
            return false;
        });
        etExpanded.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                fullText = s == null ? "" : s.toString();
            }
        });
    }


    // ---------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------

    public void setText(String text) {
        this.fullText = text == null ? "" : text;
        String[] lines = fullText.split("\\r?\\n");
        String twoLines = lines.length >= 2 ? lines[0] + "\n" + lines[1]+"..." : fullText;
        tvCollapsed.setText(twoLines);
        etExpanded.setText(fullText);
    }

    public void setHint(CharSequence hint) {
        tvCollapsed.setHint(hint);
        etExpanded.setHint(hint);
    }

    /** Returns the full (non-truncated) text. */
    public String getText() {
        return fullText;
    }

    // ---------------------------------------------------------------------
    // State switching
    // ---------------------------------------------------------------------

    private void expand() {
        tvCollapsed.setVisibility(GONE);
        etExpanded.setText(fullText);
        svContainer.setVisibility(VISIBLE);
        etExpanded.requestFocus();
        etExpanded.setSelection(etExpanded.getText().length());
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etExpanded, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void collapse() {
        fullText = etExpanded.getText().toString();
        String[] lines = fullText.split("\\r?\\n");
        String twoLines = lines.length >= 2 ? lines[0] + "\n" + lines[1]+"..." : fullText;
        tvCollapsed.setText(twoLines);
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etExpanded.getWindowToken(), 0);
        }
        svContainer.setVisibility(GONE);
        tvCollapsed.setVisibility(VISIBLE);
        etExpanded.clearFocus();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private int dpToPx(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return Math.round(dp * scale);
    }
}

