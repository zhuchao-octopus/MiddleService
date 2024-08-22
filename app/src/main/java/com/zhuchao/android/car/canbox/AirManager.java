package com.zhuchao.android.car.canbox;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.common.utils.MachineConfig;
import com.zhuchao.android.car.R;

import java.util.Objects;

public class AirManager {

    private static WindowManager mWindowManager;
    private static WindowManager.LayoutParams mLayoutParams;
    @SuppressLint("StaticFieldLeak")
    private static View mView;

    public static boolean isShow = false;

    private static Presentation mPresentation = null;

    private static AirUI mUI;
    public static boolean mShowScreen1 = false;

    public static void reinit(Context context) {
        stop();
        mUI = null;
        mView = null;
        init(context);
    }

    private static void init(Context context) {
        if (mView == null) {

            mView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.air_toyota_screen1, null);

            mLayoutParams = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.RGBA_8888);

            String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_SCREEN1_VIEW);
            //s = "1"; // test
            if (s != null) {
                mShowScreen1 = true;
                DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
                Display[] display = displayManager.getDisplays();

                if (display.length > 1) {
                    mPresentation = new Presentation(context, display[1], R.style.TranslucentTheme2);
                    mPresentation.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    mPresentation.setContentView(mView);

                    // mPresentation = new Presentation(getApplicationContext(),
                    // display[1], R.style.TranslucentTheme);
                    WindowManager.LayoutParams lp = mPresentation.getWindow().getAttributes();
                    lp.alpha = 0.98f;
                    // lp.width = LayoutParams.WRAP_CONTENT;
                    // lp.height = LayoutParams.WRAP_CONTENT;

                    mPresentation.getWindow().setGravity(Gravity.BOTTOM);

                }
            } else {
                mPresentation = null;
            }

            if (mPresentation == null) {
                mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            }

            mView.findViewById(R.id.air_control_back).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    stop();
                }
            });
        }

    }

    private static final int HIDE = 0;
    private static final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        public void handleMessage(Message msg) {
            if (msg.what == HIDE) {
                stop();
            }

        }
    };

    public static void startAll(Context context) {
        AirUI.mStyle = AirUI.STYLE_ALL;
        start(context, null);
    }

    public static void start(Context context, byte[] airData) {
        if (mView == null) {
            init(context);
        }

        // mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        if (mUI == null) {
            mUI = AirUI.getInstanse(context, mView.findViewById(R.id.screen1_main), 0);

        }

        mUI.setAirData(airData);

        if (airData == null || (airData[0] & 0x80) != 0) {
            if (!isShow) {

                if (mPresentation != null) {
                    mPresentation.show();
                } else {
                    mWindowManager.addView(mView, mLayoutParams);
                }

                mUI.onCreate();
                isShow = true;

                mUI.onResume();

            }
            prepareHide();
        } else {
            if (isShow) {
                stop();
            }
        }
    }

    public static boolean mSetByUI = false;

    public static void sendAirFunchtion(int i) {
        if (mUI != null) {
            mSetByUI = true;
            mUI.sendAirFunchtion(i);
        }
    }

    public static void prepareHide() {
        // if(mStyle != AirUI.STYLE_ALL){
        mHandler.removeMessages(HIDE);
        mHandler.sendEmptyMessageDelayed(HIDE, 5500);
        // }
    }

    public static void stop() {
        if (isShow) {
            AirUI.mStyle = 0;
            if (mPresentation != null) {
                mPresentation.dismiss();
            } else {
                mWindowManager.removeView(mView);
            }
            mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            mUI.onPause();
            mUI.onDestroy();

            isShow = false;
        }
    }
}
