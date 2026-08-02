package com.example.calendar4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class FlyOutContainer extends LinearLayout {


    private View menu;
    private View context;

    protected static final int menuMargin = 125;

    public enum MenuState{
        Closed, Open
    };

    protected int currentContentOffset = 0;
    protected MenuState menuCurrentState = MenuState.Closed;

    public FlyOutContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub

    }

    public FlyOutContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public FlyOutContainer(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    protected void onAttachedToWindow(){
        super.onAttachedToWindow();





        this.menu = this.getChildAt(0);

        this.context = this.getChildAt(1);



        this.menu.setVisibility(View.GONE);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom){
        if(changed)
            this.calculateChildDimensions();

        this.menu.layout(left, top, right - menuMargin, bottom);

        this.context.layout(left + this.currentContentOffset, top, right
                + currentContentOffset, bottom);

    }

    public void toggleMenu(){
        switch(this.menuCurrentState){
            case Closed:
                this.menu.setVisibility(View.VISIBLE);
                this.currentContentOffset = this.getMenuWidth();
                this.context.offsetLeftAndRight(currentContentOffset);
                this.menuCurrentState = MenuState.Open;
                break;
            case Open:
                this.context.offsetLeftAndRight(-currentContentOffset);
                this.currentContentOffset = 0;
                this.menuCurrentState = MenuState.Closed;
                this.menu.setVisibility(View.GONE);
                break;
        }
        this.invalidate();
    }

    private int getMenuWidth(){
        return this.menu.getLayoutParams().width;
    }


    private void calculateChildDimensions(){
        this.context.getLayoutParams().height = this.getHeight();
        this.context.getLayoutParams().width = this.getWidth();

        this.menu.getLayoutParams().height = this.getHeight();
        this.menu.getLayoutParams().width = this.getWidth() - menuMargin;
    }


}
