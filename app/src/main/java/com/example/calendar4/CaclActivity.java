package com.example.calendar4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Point;
import android.os.Bundle;
//import android.text.Html;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

//import com.google.android.gms.common.util.ArrayUtils;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CaclActivity extends AppCompatActivity {
    //общие переменные
    private static BigDecimal bdResult = new BigDecimal("0.0");       //Основное значение ввиде числа. Для Высокой точночти использовать BigDecimal, BigInteger
    private String txtResult="0";       //Основное значение ввиде текста
    private String txtFormula="";       //Формула ввиде текста
    private String txtHistory="";       //История ввиде текста
    private String txtLastPush="D";     //предыдущее нажатие D - цифра и точка, O - операция
    private boolean resultPoint=false;  //Наличие точки для основного значения
    private static BigDecimal memValue = new BigDecimal("0.0");        //значение в памяти ввиде числа
    private static int digitOperation;   //0 - нет действий, 1+, 2-, 3*, 4/  Переменная операций
    private static BigDecimal prevResult = new BigDecimal("0.0");  //предыдущее значение ввиде числа
    private String[] arrArgsTxt = new String[100];        //Массив аргументов и операций
    private int[] arrcalcLevel = new int[100];        //Массив порядка вычислений
    private String[] arrOperTxt = new String[100];        //Массив действий по порядку
    private int arrArgsTxtPointer = 0;      //Указатель на последний элемент
    private int calcLevel=0;      //номер порядка вычислений, чтобы потом было проще считать
    private static String BufMemoryText="";  //Буферизация текста поля мемори
    private static String BufFormulaText="";  //Буферизация текста поля формула
    private static String BufResultText="";  //Буферизация текста поля Результат
    private static String BufHistoryText="";  //Буферизация текста поля мемори

    private static int PrevkeyCode;     //предыдущий код клавиши для сложных клавиш
    private static int clickcounter;  //WG DEBUG счетчик

    private boolean isUpdateMainValue=false;
    private boolean isUpdateMemLabel=false;
    private boolean isUpdateFormula=false;
    private boolean isUpdateHistory=false;

    private Integer screenWidth;
    private Integer screenHeight;
    private Integer screenOrientation;
    private Integer parMulti = 1;   //Умножитель размеров для пропорций

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //String[] mas1= {"asss", "Fala"};
        //StringBuilder sada = new StringBuilder();
        //ArrayUtils.writeStringArray(sada,mas1);

        // Получим Display из the WindowManager и запомним размер и ориентацию
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenWidth = point.x;
        screenHeight = point.y;
        if(screenHeight>screenWidth)screenOrientation =0; else screenOrientation =1;

        // Теперь проставим размер и зависимости полей в зависимости от ориентации
        Integer fldWidth = 0;
        Integer fldLeft = 0;
        Integer fldWidthMem = 0;
        // Память Лебел
        TextView infoTextView2 = (TextView) findViewById(R.id.textView2);
        ConstraintLayout.LayoutParams tvLaypar2 = (ConstraintLayout.LayoutParams) infoTextView2.getLayoutParams();
        parMulti = tvLaypar2.leftMargin /5;     // левый отступ 5 (связано с привязкой элемента textView2 к левому краю)
        //screenOrientation == 0 - это ориентация портрет, 1 - ландшафт
        if(screenOrientation == 0) tvLaypar2.topMargin = 50*parMulti; else tvLaypar2.topMargin = 2*parMulti;   //UP
        if(screenOrientation == 0) fldWidth = (screenWidth-10*parMulti); else fldWidth = 225*parMulti;  //WIDTH
        tvLaypar2.width = 50*parMulti;
        if(screenOrientation == 0) fldLeft = 5*parMulti; else fldLeft =370*parMulti;  //LEFT
        tvLaypar2.leftMargin=fldLeft;
        infoTextView2.setLayoutParams(tvLaypar2);

        //==========ДЕБАГ МЕСС==================
        //TextView dspresult = (TextView) findViewById(R.id.textView5);
        //dspresult.setText("tvLaypar2.rightMargin=" +String.valueOf(tvLaypar2.leftMargin));

        // Память содержание
        TextView infoTextView5 = (TextView) findViewById(R.id.textView5);
        ConstraintLayout.LayoutParams tvLaypar5 = (ConstraintLayout.LayoutParams) infoTextView5.getLayoutParams();
        if(screenOrientation == 0) fldWidthMem = (screenWidth-(15+50)*parMulti);  else fldWidthMem = (225-50-5)*parMulti;
        tvLaypar5.width = fldWidthMem;
        if(screenOrientation == 0) tvLaypar5.topMargin = 50*parMulti;  else tvLaypar5.topMargin = 2*parMulti;
        infoTextView5.setLayoutParams(tvLaypar5);
        if(!BufMemoryText.equals("")) infoTextView5.setText(BufMemoryText);

        TextView infoTextView4 = (TextView) findViewById(R.id.textView4);
        ConstraintLayout.LayoutParams tvLaypar4 = (ConstraintLayout.LayoutParams) infoTextView4.getLayoutParams();
        tvLaypar4.width = fldWidth;
        tvLaypar4.leftMargin=fldLeft;
        infoTextView4.setLayoutParams(tvLaypar4);
        if(!BufFormulaText.equals("")) infoTextView4.setText(BufFormulaText);

        TextView infoTextView = (TextView) findViewById(R.id.textView);
        ConstraintLayout.LayoutParams tvLaypar = (ConstraintLayout.LayoutParams) infoTextView.getLayoutParams();
        tvLaypar.width = fldWidth;
        tvLaypar.leftMargin=fldLeft;
        infoTextView.setLayoutParams(tvLaypar);
        if(!BufResultText.equals("")) infoTextView.setText(BufResultText);

        TextView infoTextView6 = (TextView) findViewById(R.id.textView6);
        ConstraintLayout.LayoutParams tvLaypar6 = (ConstraintLayout.LayoutParams) infoTextView6.getLayoutParams();
        tvLaypar6.width = fldWidth;
        tvLaypar6.leftMargin=fldLeft;
        infoTextView6.setLayoutParams(tvLaypar6);

        TextView infoTextView3 = (TextView) findViewById(R.id.textView3);
        ConstraintLayout.LayoutParams tvLaypar3 = (ConstraintLayout.LayoutParams) infoTextView3.getLayoutParams();
        tvLaypar3.width = fldWidth;
        tvLaypar3.leftMargin=fldLeft;
        infoTextView3.setLayoutParams(tvLaypar3);
        if(!BufHistoryText.equals("")) infoTextView3.setText(BufHistoryText);
        // ЕНД Теперь проставим размер и зависимости полей в зависимости от ориентации
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Обработайте нажатие клавиатуры, верните true, если обработка выполнена
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Обработайте отпускание клавиши клавиатуры, верните true, если обработка выполнена
        if(!event.isShiftPressed()){
            //keyCode=7 - "0", 16 - "9"
            if(keyCode >6 && keyCode<17 && PrevkeyCode!=59 ) {pushDigit(keyCode-7);updFields();}
            if((keyCode == 56 || keyCode == 55) && !resultPoint){  //56 point
                txtResult=txtResult + ".";
                resultPoint=true;
                isUpdateMainValue=true;
                updFields();
            }
            if((keyCode == 67 || keyCode == 112) && !txtResult.equals("0")) //67 Backspace
            {   if(txtResult.substring(txtResult.length()-1).equals(".")) resultPoint=false;
                if(txtResult.length()==1) txtResult="0";
                else txtResult=txtResult.substring(0,txtResult.length()-1);
                isUpdateMainValue=true;
                updFields();
            }
            //С серыми кнопочками (*-+/) какая-то лажа, видимо там 2-е кнопки нажимаются
            if(keyCode == 15 && PrevkeyCode==59) {   //*
                PushOperation(3);
                updFields();
            }
            if(keyCode == 70 && PrevkeyCode==59) {   //+
                PushOperation(1);
                updFields();
            }
            if(keyCode == 69) {   //-
                PushOperation(2);
                updFields();
            }
            if(keyCode == 76) {   //разделить, косая дробь
                PushOperation(4);
                updFields();
            }
            if(keyCode == 70) {   //=
                PushOperation(10);
                updFields();
            }
        }

        //clickcounter = clickcounter + 1;
        TextView dspresult3 = (TextView) findViewById(R.id.textView3);
        dspresult3.setText("PrevkeyCode: " + String.valueOf(PrevkeyCode) + ", keyCode: " + String.valueOf(keyCode));

        PrevkeyCode = keyCode;
        return true;
    }

    public void onMyButton1Click(View view)
    {
        //((Activity)view.getContext()).finish();
        //обработка кнопок с цифрами
        switch (view.getId())
        {	case R.id.button17:	//1
                pushDigit(1);break;
            case R.id.button18:		//2
                pushDigit(2);break;
            case R.id.button19:		//3
                pushDigit(3);break;
            case R.id.button7:	//4
                pushDigit(4);break;
            case R.id.button16:		//5
                pushDigit(5);break;
            case R.id.button15:		//6
                pushDigit(6);break;
            case R.id.button:	//7
                pushDigit(7);break;
            case R.id.button13:		//8
                pushDigit(8);break;
            case R.id.button14:		//9
                pushDigit(9);break;
            case R.id.button9:		//0
                pushDigit(0);break;
            case R.id.button12:		//Point
                if(!resultPoint)
                {   txtResult=txtResult + ".";
                    resultPoint=true;
                    isUpdateMainValue=true;
                }
                break;
            case R.id.button3:      //Из Памяти
            case R.id.button4:      //Память +
            case R.id.button5:      //Память *
            case R.id.button6:      //Память Сброс
                pushMemory(view.getId());
                break;
            case R.id.button11:		//Сброс
                PushOperation(0);break;
            case R.id.button33:		//Равно
                pushEQUAL();break;
            case R.id.button39:		//+
                PushOperation(1);break;
            case R.id.button38:		//-
                PushOperation(2);break;
            case R.id.button37:		//*
                PushOperation(3);break;
            case R.id.button35:		// делить
                PushOperation(4); break;
            case R.id.button36:		// 1/x
                PushOperation(6); break;
            case R.id.button34:		// +/-
                PushOperation(7); break;
            default:
                return;
        }
        updFields();
    }

    //=================================================== Отработка числовых операций
    private void PushOperation(int digitOperation) {
        try {// Определим значок операции по коду
            String txtPrevBOper = ""; //Префикс
            String txtPostBOper = ""; //Суфикс
            String txtBuffer1 = ""; //Для смены знака
            switch (digitOperation) {
                case 0:     //Сброс
                    txtPostBOper = "";
                    txtPrevBOper = "";
                    txtFormula = "";
                    for (arrArgsTxtPointer = arrArgsTxtPointer; arrArgsTxtPointer > -1; arrArgsTxtPointer--)
                        {arrArgsTxt[arrArgsTxtPointer] = "";
                        arrcalcLevel[arrArgsTxtPointer] = 0;
                        }
                    arrArgsTxtPointer = 0;
                    calcLevel = 0;
                    break;
                case 1:     //+
                    arrArgsTxt[arrArgsTxtPointer] = txtResult;
                    arrcalcLevel[arrArgsTxtPointer] = calcLevel;
                    arrArgsTxtPointer++;
                    arrArgsTxt[arrArgsTxtPointer] = "+";
                    arrcalcLevel[arrArgsTxtPointer] = calcLevel;
                    arrArgsTxtPointer++;
                    txtPostBOper = txtResult + "+";
                    break;
                case 2:     //-
                    arrArgsTxt[arrArgsTxtPointer] = txtResult;
                    arrcalcLevel[arrArgsTxtPointer] = calcLevel;
                    arrArgsTxtPointer++;
                    arrArgsTxt[arrArgsTxtPointer] = "-";
                    arrcalcLevel[arrArgsTxtPointer] = calcLevel;
                    arrArgsTxtPointer++;
                    txtPostBOper = txtResult + "-";
                    break;
                case 3:     //*
                    arrArgsTxt[arrArgsTxtPointer] = txtResult;
                    arrcalcLevel[arrArgsTxtPointer] = calcLevel;
                    arrArgsTxtPointer++;
                    arrArgsTxt[arrArgsTxtPointer] = "*";
                    arrcalcLevel[arrArgsTxtPointer] = calcLevel;
                    arrArgsTxtPointer++;
                    txtPostBOper = txtResult + "*";
                    break;
                case 4:     //разделить
                    arrArgsTxt[arrArgsTxtPointer] = txtResult;
                    arrcalcLevel[arrArgsTxtPointer] = calcLevel;
                    arrArgsTxtPointer++;
                    arrArgsTxt[arrArgsTxtPointer] = "/";
                    arrcalcLevel[arrArgsTxtPointer] = calcLevel;
                    arrArgsTxtPointer++;
                    txtPostBOper = txtResult + "/";
                    break;
                case 6:     // 1/x
                    if (txtFormula == "") {

                        arrArgsTxt[arrArgsTxtPointer] = "1";
                        arrcalcLevel[arrArgsTxtPointer] = calcLevel;
                        arrArgsTxtPointer++;
                        arrArgsTxt[arrArgsTxtPointer] = "/";
                        arrcalcLevel[arrArgsTxtPointer] = calcLevel;
                        arrArgsTxtPointer++;
                        arrArgsTxt[arrArgsTxtPointer] = txtResult;
                        arrcalcLevel[arrArgsTxtPointer] = calcLevel;
                        arrArgsTxtPointer++;
                        txtFormula = "1/" + txtResult;
                    }
/*                if (txtLastPush=="O") {
                    txtPrevBOper = "";
                    txtPostBOper = "1/";
                }
                if (txtLastPush=="D") {
                    txtPrevBOper = "1/(";
                    txtPostBOper = ")";
                }*/

                    break;
                case 7:     // +/-
                    if (txtFormula == "") {
                        txtPrevBOper = "-";
                    }
                    if (txtFormula == "-") {
                        txtFormula = "";
                        txtPostBOper = "";
                    }
                    break;
                default:
                    return;
            }
            txtFormula = txtPrevBOper + txtFormula + txtPostBOper;
            txtLastPush = "O";    //Признак последней нажатой операции
            isUpdateFormula = true;
            txtResult = "0";
            resultPoint = false;
            isUpdateMainValue = true;
        }
        catch (Exception ex)
            {   Logger.getLogger(CaclActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    //=================================================== Отработка РАВНО
    private void pushEQUAL()
    {
        try
        {
        int reversecount;
        int actionpointer=0;
        bdResult = BigDecimal.valueOf(0.0);
        BigDecimal bdResult = new BigDecimal("0.0");
        //1) Запишем последнее введенное число как аргумент в массив
        arrArgsTxt[arrArgsTxtPointer] = txtResult;
        //3) Проведем вычисления
        for(reversecount=arrArgsTxtPointer; reversecount>-1 ; reversecount--)
        {

            if (reversecount == arrArgsTxtPointer)
                bdResult = BigDecimal.valueOf(Long.parseLong(arrArgsTxt[arrArgsTxtPointer]));


            //arrOperTxt[actionpointer] = "";
        }

        //txtResult= arrArgsTxt[arrArgsTxtPointer] + "["+ String.valueOf(arrArgsTxtPointer) +"]" + txtResult;

        //4) занесем все выражение в строку Истории

        //5) Сложим результат в основную строку
        //txtResult = "";
        //for(arrArgsTxtPointer=arrArgsTxtPointer; arrArgsTxtPointer>-1 ; arrArgsTxtPointer--)
            //txtResult= arrArgsTxt[arrArgsTxtPointer] + "["+ String.valueOf(arrArgsTxtPointer) +"]" + txtResult;
        //txtResult = arrArgsTxt[arrArgsTxtPointer] + "$" + txtResult;
        //txtResult =String.valueOf(arrArgsTxtPointer);
        //txtResult= "$" +arrArgsTxt[0] + "["+ String.valueOf(0) +"]" +"$"+ txtResult ;

        //        txtLastPush="O";    //Признак последней нажатой операции
        //      txtFormula="";
        //    isUpdateFormula=true;
        //  resultPoint=false;


        //6) Очистим массив значений и операций для следующих вычислен
        for(reversecount=arrArgsTxtPointer; reversecount>-1 ;reversecount-- )
            arrArgsTxt[reversecount] = "";
        arrArgsTxtPointer = 0;

        if(txtFormula !="") // Защита от тупых нажатий равно подряд, возможно нужно будет переделать в повтор операции
            txtHistory =BufFormulaText + txtResult + "=" + bdResult.toString() + "\n" + txtHistory; // Сшивание формулы,основного поля и результата в строку истории
        txtResult = bdResult.toString();    // В основном поле результат после вычислений
        txtFormula = "";
        isUpdateFormula = true;
        isUpdateMainValue = true;
        isUpdateHistory = true;
        }
        catch (Exception ex)
        {
            Logger.getLogger(CaclActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //=================================================== Отработка нажатия Кнопок Памяти
    private void pushMemory(int buttonId)
    {   switch (buttonId)
        {
            case R.id.button3:      //Из Памяти
                digitOperation=0;
                bdResult=memValue;
                txtResult=memValue.toString();
                resultPoint=true;
                isUpdateMainValue=true;
                break;
            case R.id.button4:      //Память +
                // != "undefined")
                memValue = memValue.add(bdResult);
                isUpdateMemLabel=true;
                break;
            case R.id.button5:      //Память *
                memValue = memValue.multiply(bdResult);
                isUpdateMemLabel=true;
                resultPoint=false;
                isUpdateMainValue=true;
                isUpdateHistory=true;
                break;
            case R.id.button6:      //Память Сброс
                memValue = BigDecimal.valueOf(0);
                isUpdateMemLabel=true;
                break;
        }
    }

    //=================================================== Отработка нажатия цифры
    private void pushDigit(int digit)
    {   if (txtResult.equals("0")) {txtResult=Integer.toString(digit); }
        else {txtResult=txtResult + Integer.toString(digit); }
        txtLastPush="D";    // Признак последней нажатой цифры
        isUpdateMainValue=true;
    }

    //===================================================вычисление действия
    private void calcCurrentValue()
    {
        switch (digitOperation)
        {
            case 0:     //Нет операции
                prevResult=bdResult;
                break;
            case 1:     //+
                prevResult=prevResult.add(bdResult);
                break;
            case 2:     //-
                prevResult=prevResult.add(bdResult.negate()) ;
                break;
            case 3:     //*
                prevResult=prevResult.multiply(bdResult);
                break;
            case 4:     //разделить
                prevResult=prevResult.divide(bdResult);
                break;
            case 5:     //-/+
                bdResult=bdResult.negate();
                break;
            case 6:     // 1/x
                bdResult=BigDecimal.valueOf(1).divide(bdResult);
                break;
            default:
                return;
        }
    }

    //===================================================Обновление полей на экране
    private void updFields()
    {
        //вывод памяти
        if(isUpdateMemLabel) {
            TextView dspresult = (TextView) findViewById(R.id.textView5);
            dspresult.setText(memValue.toString());
            BufMemoryText = memValue.toString();
            isUpdateMemLabel = false;
        }

        //вывод текста текущей операции
        if(isUpdateFormula) {
            TextView dspresult = (TextView) findViewById(R.id.textView4);
            dspresult.setText(txtFormula);
            BufFormulaText = (String) dspresult.getText();
            isUpdateFormula = false;
        }

        //вывод результата
        if(isUpdateMainValue) {
            TextView dspresult = (TextView) findViewById(R.id.textView);
            bdResult = BigDecimal.valueOf(Double.parseDouble(txtResult));
            dspresult.setText(txtResult);
            BufResultText = txtResult;
            //dspresult.setText("78" + (char) 0x00B0 +" 56");
            //dspresult.setText(Html.fromHtml("X<sup>2</sup>&nbsp;X<sub>3</sub>"));
            //dspresult.setText("78" + (char) 0x00B0 +" 56");
            isUpdateMainValue = false;
        }

        //вывод истории
        if(isUpdateHistory) {
            TextView dspresult = (TextView) findViewById(R.id.textView3);
            //if(prevResult.equals(BigDecimal.valueOf(0))) dspresult.setText(""); else dspresult.setText(prevResult.toString());
            dspresult.setText(txtHistory);
            BufHistoryText = (String) dspresult.getText();
            isUpdateHistory = false;
        }
    }

}
