package com.zhuchao.android.car.canbox;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.common.util.MachineConfig;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.manager.McuManager;

import java.util.Locale;

public class SosManager {

    private static WindowManager mWindowManager;
    private static WindowManager.LayoutParams mLayoutParams;
    private static View mView;

    private static Context mContext;

    private static ImageView mSOS;

    private static void init(Context context) {

        if (mView == null) {
            mContext = context;
            mView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.sos, null);
            mSOS = mView.findViewById(R.id.sos);
            mLayoutParams = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.RGBA_8888);
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        }

    }

    public static int mPreCmd = 0;

    public static void start(Context context, int cmd) {
        init(context);
        if (mView.getParent() == null) {
            mWindowManager.addView(mView, mLayoutParams);
            McuManager mMcuManager = McuManager.getInstance(null);
            if (mMcuManager != null) {
                mMcuManager.setVolume(0);
            }

        }
        if (mPreCmd != cmd) {
            mPreCmd = cmd;
            String s = null;
            switch (cmd) {
                case 2:
                    s = "sos_test.png";
                    break;
                case 1:
                    Locale locale = Locale.getDefault();
                    Log.d("dd", locale.getLanguage());

                    s = "sos.png";
                    if (locale.getLanguage().equals("ru")) {
                        s = "sos_ru.png";
                    }
                    break;
            }

            s = MachineConfig.getParameterPath() + s;

            Drawable d = Drawable.createFromPath(s);
            if (d != null) {
                mSOS.setImageDrawable(d);
            }
        }
    }

    public static void stop() {
        if (mView.getParent() != null) {
            mWindowManager.removeView(mView);
        }
    }
}
