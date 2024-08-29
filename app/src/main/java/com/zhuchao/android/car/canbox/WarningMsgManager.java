package com.zhuchao.android.car.canbox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhuchao.android.car.R;

public class WarningMsgManager {
    public final static String TAG = "WarningMsgManager";
    public static boolean isShow = false;
    private static WindowManager mWindowManager;
    private static WindowManager.LayoutParams mLayoutParams;
    private static View mView;
    private static TextView mTextWarn;
    private static ImageView mImageWarn;
    private static ImageView mImageWarn2;

    public static void init(Context context) {
        if (mView == null) {
            mView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.warning_msg, null);

            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            mLayoutParams = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.RGBA_8888);

            mView.findViewById(R.id.warning_ok).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    stop();
                }
            });

            mTextWarn = mView.findViewById(R.id.warning_text);
            mImageWarn = mView.findViewById(R.id.waring_image);
            mImageWarn2 = mView.findViewById(R.id.waring_image2);

        }

    }

    public static void start(Context context) {
        if (!isShow) {
            init(context);
            mWindowManager.addView(mView, mLayoutParams);
            // mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);


            isShow = true;
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void updateView(Context context, int drawable, int drawable2, int string) {
        if (isShow) {
            if (mTextWarn != null) {
                mTextWarn.setText(context.getResources().getString(string));
            }
            if (mImageWarn != null) {
                if (drawable != 0) {
                    mImageWarn.setBackground(context.getResources().getDrawable(drawable, null));
                    mImageWarn.setVisibility(View.VISIBLE);
                } else {
                    mImageWarn.setVisibility(View.GONE);
                }

            }
            if (mImageWarn2 != null) {
                if (drawable2 != 0) {
                    mImageWarn2.setBackground(context.getResources().getDrawable(drawable2, null));
                    mImageWarn2.setVisibility(View.VISIBLE);
                } else {
                    mImageWarn2.setVisibility(View.GONE);
                }
            }

        }
    }

    public static void stop() {
        if (isShow) {
            // mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            mWindowManager.removeView(mView);
            isShow = false;
        }
    }
}
