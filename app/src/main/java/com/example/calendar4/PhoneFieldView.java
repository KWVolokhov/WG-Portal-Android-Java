package com.example.calendar4;

import static android.app.ProgressDialog.show;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class PhoneFieldView extends LinearLayout {
    private static final char PLACEHOLDER = '•';
    private static final int MAX_DIGITS = 10;
    private EditText cdCityPhone, etPhone3, etPhone12, etPhone22, etAdditional;

    public PhoneFieldView(Context context) {
        this(context, null);
    }

    public PhoneFieldView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhoneFieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        TextView tvPrev = new TextView(context);
        tvPrev.setText("+7(");
        tvPrev.setTextSize(14);
        LinearLayout.LayoutParams LpTV = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(tvPrev, LpTV);

        cdCityPhone = new EditText(context);
        cdCityPhone.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        cdCityPhone.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(3) });
        //cdCityPhone.setEms(3);
        cdCityPhone.setMaxLines(1);
        cdCityPhone.setSingleLine(true);
        cdCityPhone.setSelectAllOnFocus(false);
        cdCityPhone.setText("");
        int widthPixels =(int)cdCityPhone.getPaint().measureText("H");
        LinearLayout.LayoutParams LpETpx3 = new LinearLayout.LayoutParams(3*widthPixels, LayoutParams.WRAP_CONTENT);
        addView(cdCityPhone, LpETpx3);

        TextView tvMiddle = new TextView(context);
        tvMiddle.setText(")");
        tvMiddle.setTextSize(14);
        addView(tvMiddle, LpTV);

        // ----- masked phone EditText -----
        etPhone3 = new EditText(context);
        etPhone3.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etPhone3.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(3) });
        //etPhone3.setEms(3);
        etPhone3.setMaxLines(1);
        etPhone3.setSingleLine(true);
        etPhone3.setSelectAllOnFocus(false);
        etPhone3.setText("");
        addView(etPhone3, LpETpx3);

        TextView tvMiddle3 = new TextView(context);
        tvMiddle3.setText("-");
        tvMiddle3.setTextSize(14);
        addView(tvMiddle3, LpTV);

        etPhone12 = new EditText(context);
        etPhone12.setInputType(InputType.TYPE_CLASS_PHONE);
        etPhone12.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(2) });
        //etPhone12.setEms(2);
        etPhone12.setMaxLines(1);
        etPhone12.setSingleLine(true);
        etPhone12.setSelectAllOnFocus(false);
        etPhone12.setText("");
        LinearLayout.LayoutParams LpETpx2 = new LinearLayout.LayoutParams(2*widthPixels, LayoutParams.WRAP_CONTENT);
        addView(etPhone12, LpETpx2);

        TextView tvMiddle4 = new TextView(context);
        tvMiddle4.setText("-");
        tvMiddle4.setTextSize(14);
        addView(tvMiddle4, LpTV);

        etPhone22 = new EditText(context);
        etPhone22.setInputType(InputType.TYPE_CLASS_PHONE);
        etPhone22.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(2) });
        etPhone22.setEms(2);
        etPhone22.setMaxLines(1);
        etPhone22.setSingleLine(true);
        etPhone22.setSelectAllOnFocus(false);
        etPhone22.setText("");
        addView(etPhone22, LpETpx2);

        // ----- "доп." label + free TextEdit -----
        TextView tvDop = new TextView(context);
        tvDop.setText(" доп.");
        tvDop.setTextSize(14);
        addView(tvDop, LpTV);

        etAdditional = new EditText(context);
        etAdditional.setInputType(InputType.TYPE_CLASS_TEXT);
        etAdditional.setMaxLines(1);
        etAdditional.setSingleLine(true);
        etAdditional.setHint("");
        etAdditional.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.2f));
        addView(etAdditional);

        cdCityPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { if (s.length() == 3) { etPhone3.requestFocus();}}
        });

        etPhone3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { if (s.length() == 3) { etPhone12.requestFocus();}}
        });

        etPhone12.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { if (s.length() == 2) { etPhone22.requestFocus();}}
        });

        etPhone22.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { if (s.length() == 2) { etAdditional.requestFocus();}}
        });

        etPhone3.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode ==KeyEvent.KEYCODE_DEL && event.getAction()==KeyEvent.ACTION_DOWN){
                    if(etPhone3.getSelectionStart()==0){
                        cdCityPhone.requestFocus();
                        cdCityPhone.setSelection(cdCityPhone.getText().length());
                        return true;
                    }
                }
                return false;
            }
        });

        etPhone12.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode ==KeyEvent.KEYCODE_DEL && event.getAction()==KeyEvent.ACTION_DOWN){
                    if(etPhone12.getSelectionStart()==0){
                        etPhone3.requestFocus();
                        etPhone3.setSelection(etPhone3.getText().length());
                        return true;
                    }
                }
                return false;
            }
        });

        etPhone22.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode ==KeyEvent.KEYCODE_DEL && event.getAction()==KeyEvent.ACTION_DOWN){
                    if(etPhone22.getSelectionStart()==0){
                        etPhone12.requestFocus();
                        etPhone12.setSelection(etPhone12.getText().length());
                        return true;
                    }
                }
                return false;
            }
        });

        etAdditional.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode ==KeyEvent.KEYCODE_DEL && event.getAction()==KeyEvent.ACTION_DOWN){
                    if(etAdditional.getSelectionStart()==0){
                        etPhone22.requestFocus();
                        etPhone22.setSelection(etPhone22.getText().length());
                        return true;
                    }
                }
                return false;
            }
        });

    }

    /** Returns the phone digits actually entered (0..10 characters) or "" if empty. */
    public String getPhoneDigits() {
        return onlyDigits(cdCityPhone.getText().toString()+etPhone3.getText().toString()+
                etPhone12.getText().toString()+etPhone22.getText().toString());
    }

    /** Sets the additional ("доп.") text. */
    /*public void setAdditionalText(String text) {
        etAdditional.setText(text == null ? "" : text);
    }*/

    /** Returns the additional ("доп.") text. */
    public String getAdditionalText() {
        return etAdditional.getText().toString();
    }

    /**
     * Sets both parts from the stored value ("<10 digits><доп text>").
     * If the stored value does not start with 10 digits, the whole string is
     * treated as the "доп." text and the phone part is left empty.
     */
    public void setValue(String stored) {
        if (stored == null) stored = "";
        //if (stored == null || stored.length() < MAX_DIGITS) return ;
        String digitStr ="", dopStr=stored;
        for(int i=0; (i< stored.length()) && (i<MAX_DIGITS); i++){
            if(!stored.substring(i, i+1).matches("\\d{1}")) break;
            digitStr = stored.substring(0, i+1);
            dopStr = stored.substring(i+1);
        }
        if(digitStr!="") setPhoneDigits(digitStr);
        if(dopStr!="") etAdditional.setText(dopStr);
    }
    public void setPhoneDigits(String raw) {
        String digits = onlyDigits(raw == null ? "" : raw);
        if (digits.length() > MAX_DIGITS) {
            digits = digits.substring(0, MAX_DIGITS);
        }
        if(digits!= null && !"".equals(digits)) {
            if(digits.length()>=3)
                cdCityPhone.setText(digits.substring(0, 3));
            else {
                cdCityPhone.setText(digits.substring(0));
                return;
            }

            if(digits.length()>=6)
                etPhone3.setText(digits.substring(3, 6));
            else {
                etPhone3.setText(digits.substring(3));
                return;
            }
            if(digits.length()>=8)
                etPhone12.setText(digits.substring(6, 8));
            else {
                etPhone12.setText(digits.substring(6));
                return;
            }
            etPhone22.setText(digits.substring(8));
        }
    }

    /** Returns the value as it should be stored: "<10 digits><доп text>". */
    public String getValue() {
        return getPhoneDigits() + getAdditionalText();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static String onlyDigits(String s) {
        return s.replaceAll("\\D", "");
    }

    /*private static boolean startsWith10Digits(String s) {
        if (s == null || s.length() < MAX_DIGITS) {
            return false;
        }
        return s.substring(0, MAX_DIGITS).matches("\\d{10}");
    }*/

}

