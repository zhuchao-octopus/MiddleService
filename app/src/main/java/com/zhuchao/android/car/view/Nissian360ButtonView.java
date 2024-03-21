package com.zhuchao.android.car.view;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.zhuchao.android.car.R;
import com.common.util.AppConfig;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.common.util.UtilSystem;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.ReverseManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.cartype.raise.NissanRaise;
import com.zhuchao.android.car.cartype.simple.Nissan2013Simple;


public class Nissian360ButtonView {
    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mLayoutParams;
    private final WindowManager.LayoutParams mLayoutParamsMicButton;

    private View mViewSpeech;
    private final View mViewMicButton;
    private TextView mTitle;
    private final boolean isShowSpeech = false;
    private boolean isShowMicButton = false;
    private final Context mContext;

    public static Nissian360ButtonView mThis;

    public static Nissian360ButtonView getInstance(Context context) {
        if (mThis != null) {
            mThis.hideSpeech();
            mThis.hideMicButton();
            mThis = null;
        }
        // if (mThis == null) {
        mThis = new Nissian360ButtonView(context);
        // }
        return mThis;
    }

    public static Nissian360ButtonView getInstance() {
        return mThis;
    }

    private boolean mSendLong = false;
    private int mSendKeyTime = 0;

    public Nissian360ButtonView(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(660, 380, 0, 0, LayoutParams.TYPE_PHONE, LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.RGBA_8888);
        mLayoutParams.gravity = Gravity.CENTER;
        // mLayoutParams.alpha = 0.92f;

        mLayoutParamsMicButton = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.RGBA_8888);
        mLayoutParamsMicButton.gravity = Gravity.TOP | Gravity.LEFT;
        mLayoutParamsMicButton.alpha = 0.80f;

        DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        Display[] display = displayManager.getDisplays();

        if (display[0].getWidth() == 800) {
            mLayoutParamsMicButton.x = 800 - 90;
            mLayoutParamsMicButton.y = 480 - 240;
        } else if (display[0].getWidth() == 1024) {
            mLayoutParamsMicButton.x = 1024 - 90;
            mLayoutParamsMicButton.y = 600 - 260;

        } else {
            mLayoutParamsMicButton.x = display[0].getWidth() - 90;
            mLayoutParamsMicButton.y = display[0].getHeight() - 260;
        }

        mViewMicButton = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.nissan360button, null);

        Drawable d = Drawable.createFromPath("/mnt/parameter/icon/mic_button.png");
        if (d != null) {
            ((ImageView) mViewMicButton.findViewById(R.id.mic_button)).setImageDrawable(d);
        }

        mViewMicButton.findViewById(R.id.mic_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mSendLong) {
                    mSendLong = false;
                    return;
                }

                //						if (ReverseManager.isShow) {
                send360Key();
                mSendKeyTime = 0;
                //						} else {
                //							if (!AppConfig.CAR_UI_FRONT_CAMERA.equals(AppConfig
                //									.getTopActivity())) {
                //								mSendKeyTime = 0;
                //								try {
                //									Intent it = new Intent(Intent.ACTION_VIEW);
                //									it.setClassName(AppConfig.PACKAGE_CAR_UI,
                //											"com.android.car.frontcamera.FrontCameraActivity");
                //									it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                //											| Intent.FLAG_ACTIVITY_NEW_TASK);
                //									it.putExtra("camera",
                //											MyCmd.CAMERA_SOURCE_REVERSE);
                //									mContext.startActivity(it);
                //
                //								} catch (Exception e) {
                //									// Log.e(TAG, e.getMessage());
                //								}
                //
                //							} else {
                //								if (mSendKeyTime == 3) {
                //									try {
                //										Intent it = new Intent(
                //												Intent.ACTION_VIEW);
                //										it.setClassName(
                //												AppConfig.PACKAGE_CAR_UI,
                //												"com.android.car.frontcamera.FrontCameraActivity");
                //										it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                //												| Intent.FLAG_ACTIVITY_NEW_TASK);
                //										it.putExtra("finish", 1);
                //										mContext.startActivity(it);
                //
                //									} catch (Exception e) {
                //										// Log.e(TAG, e.getMessage());
                //									}
                //								} else {
                //									send360Key();
                //									mSendKeyTime++;
                //								}
                //							}
                //						}
            }
        });

        mViewMicButton.findViewById(R.id.mic_button).setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                Canbox c = CarUtil.getCanboxInstance();
                if (c instanceof Nissan2013Simple) {
                    Nissan2013Simple new_name = (Nissan2013Simple) c;
                    new_name.sendLangKey();
                }

                mSendLong = true;
                // TODO Auto-generated method stub
                return false;
            }
        });

        mViewMicButton.findViewById(R.id.mic_button).setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                float x = event.getRawX();
                float y = event.getRawY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchX = x;
                        mTouchY = y;
                        mOldX = mLayoutParamsMicButton.x;
                        mOldY = mLayoutParamsMicButton.y;
                        mHaveMove = false;
                        v.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // Log.d("dd", ""+mHaveMove);
                        if (((x - mTouchX) != 0) || ((y - mTouchY) != 0)) {
                            mHaveMove = true;
                        }

                        if (!mHaveMove) {
                            break;
                        }

                        mLayoutParamsMicButton.x = (int) (mOldX + x - mTouchX);
                        mLayoutParamsMicButton.y = (int) (mOldY + y - mTouchY);
                        if (mLayoutParamsMicButton.x < 0) {
                            mLayoutParamsMicButton.x = 0;
                        }
                        if (mLayoutParamsMicButton.y < 0) {
                            mLayoutParamsMicButton.y = 0;
                        }

                        mWindowManager.updateViewLayout(mViewMicButton, mLayoutParamsMicButton);

                        break;
                    case MotionEvent.ACTION_UP:
                        v.setAlpha(0.8f);
                        if (mHaveMove) {
                            return true;
                        }
                        break;
                }
                return false;
            }
        });

    }

    public static void send360Key() {
        Canbox c = CarUtil.getCanboxInstance();
        if (c instanceof Nissan2013Simple) {
            Nissan2013Simple new_name = (Nissan2013Simple) c;
            new_name.sendAVMKey();
        } else if (c instanceof NissanRaise) {
            NissanRaise new_name = (NissanRaise) c;
            new_name.sendAVMKey();
        }
    }

    private String mSystemUI;

    private final static int MIN_MOVE = 2;
    private float mTouchX;
    private float mTouchY;
    private int mOldX;
    private int mOldY;
    private boolean mHaveMove;

    public void hideSpeech() {

        if (isShowSpeech) {

        }
    }

    public void showMicButton() {
        if (!isShowMicButton) {
            isShowMicButton = true;
            mWindowManager.addView(mViewMicButton, mLayoutParamsMicButton);
        }
    }

    public void hideMicButton() {
        if (isShowMicButton) {
            try {
                mWindowManager.removeView(mViewMicButton);
            } catch (Exception e) {

            }
            isShowMicButton = false;
        }
    }

}
