package com.zhuchao.android.car.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;


//import android.app.ActivityTaskManager;
public class ParentView extends LinearLayout {
    public ParentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

    }

    private View.OnKeyListener mOnKeyListener;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (mOnKeyListener != null) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
                mOnKeyListener.onKey(this, event.getKeyCode(), event);
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void setOnKeyListener(OnKeyListener l) {
        super.setOnKeyListener(l);
        mOnKeyListener = l;
    }

}
