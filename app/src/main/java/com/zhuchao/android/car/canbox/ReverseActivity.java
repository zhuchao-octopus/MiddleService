package com.zhuchao.android.car.canbox;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.zhuchao.android.car.R;
import com.zhuchao.android.car.manager.OSProManager;
import com.zhuchao.android.fbase.MMLog;

import java.util.Objects;

public class ReverseActivity extends Activity {

    private ReverseUI mRadioUI;
    private static ReverseActivity mThis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.back);
        mRadioUI = ReverseUI.getInstance(this, findViewById(R.id.screen1_main), 0);
        mRadioUI.onCreate();
        mThis = this;
        OSProManager.mHandlerReverse = mHandler;
        MMLog.d("ReverseActivity", "ReverseActivity.onCreate!");
    }

    private static final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (mThis != null) {
                    mThis.finish();
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (mRadioUI != null) mRadioUI.onResume();
    }

    protected void onPause() {
        super.onPause();
        finish();
        if (mRadioUI != null) mRadioUI.onPause();

    }

    protected void onDestroy() {
        super.onDestroy();
        if (mRadioUI != null) mRadioUI.onDestroy();
        if (mThis == this) {
            mThis = null;

        }
    }

}
