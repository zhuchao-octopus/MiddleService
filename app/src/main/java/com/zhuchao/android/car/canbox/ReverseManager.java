package com.zhuchao.android.car.canbox;

import android.app.Presentation;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.cartype.simple.Nissan2013Simple;
import com.zhuchao.android.car.manager.McuManager;

import java.util.Objects;

public class ReverseManager {

    private static WindowManager mWindowManager;
    private static WindowManager.LayoutParams mLayoutParams;
    private static View mView;

    private static View mEmptyView;
    public static boolean isShow = false;

    private static Presentation mPresentation = null;

    private static ReverseUI mUI;
    public static boolean mShowScreen1 = false;
    private static Context mContext;

    public static void reinit(Context context) {
        stop();
        mUI = null;
        mView = null;
        init(context);
    }

    private static void init(Context context) {
        mContext = context;
        if (mView == null) {

            mView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.back, null);

            mEmptyView = mView.findViewById(R.id.empty);

            mLayoutParams = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_FULLSCREEN, PixelFormat.RGBA_8888);
            if (Util.isRK356X() || Util.isPX6() || Util.isPX30() || (Util.isPX5() && (Util.isAndroidQ() || Util.isAndroidR()))) {
                mLayoutParams.setTitle("AK_RFV240658f4");
            }
            String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_SCREEN1_VIEW);

            if (s != null && s.contains(MachineConfig.VALUE_SCREEN1_VIEW_REVERSE)) {
                mShowScreen1 = true;
                DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
                Display[] display = displayManager.getDisplays();

                if (display.length > 1) {
                    mPresentation = new Presentation(context, display[1], R.style.TranslucentTheme);
                    mPresentation.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    mPresentation.setContentView(mView);
                }
            } else {
                mPresentation = null;
            }

            if (mPresentation == null) {
                mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            }

        }
    }

    //	public static void addRadarView(Context context) {
    //		mPresentation.addContentView(RadarManager.init(context), mLayoutParams);
    //	}

    private final static int START_UI = 0;
    //	private final static int HIDE_EMPTY = 1;
    private final static int START_SET_SOURCE = 2;
    private final static int REQUEST_ANGLE = 3;
    private final static int REQUEST_ANGLE_TIME = 200;

    private final static int REMOVE_UI = 4;
    private final static int STOP_UI = 5;
    private static final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_UI:
                    mUI.onResume();
                    break;
                case START_SET_SOURCE:
                    mUI.setSource();
                    mHandler.sendEmptyMessageDelayed(START_UI, 200);
                    break;
                case REQUEST_ANGLE:
                    startRequestAngleData();
                    break;
                case REMOVE_UI:
                    if (!isShow) {
                        if (mView.getParent() != null) {
                            mWindowManager.removeView(mView);
                            mView = null;
                            mUI = null;
                        }
                        if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 30) {
                            GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SYSTEMUI_STATUS_BAR_VISIBLE);
                        }

                        Log.e("allen", "REMOVE_UI ");

                    }
                    break;
                case STOP_UI:
                    if (!isShow) {

                        Log.e("allen", "STOP_UI ");
                        mUI.onPause();
                        mUI.onDestroy();
                        mLastStopTime = SystemClock.uptimeMillis();
                        BroadcastUtil.sendByCarService(mContext, Util.isRKSystem() ? null : AppConfig.getCarAPPPackage(mContext), MyCmd.Cmd.REVERSE_STATUS, 0);

                        mHandler.removeMessages(REMOVE_UI);
                        int delay;
                        if (mLastStartDelay == 0) {
                            delay = 1;
                        } else {
                            delay = 700;
                        }
                        mLastStartDelay = 0;
                        mHandler.sendEmptyMessageDelayed(REMOVE_UI, delay);

                    }
                    break;
            }

        }
    };

    private static void startRequestAngleData() {
        if (isShow) {
            if (CarUtil.requestAngleData()) {
                mHandler.sendEmptyMessageDelayed(REQUEST_ANGLE, REQUEST_ANGLE_TIME);
            }
        } else {
            mHandler.removeMessages(REQUEST_ANGLE);
        }
    }

    //	public static void hideEmpty(){
    //
    //		Log.d("camera", "HIDE_EMPTY");
    //		mEmptyView.setVisibility(View.GONE);
    //		mUI.initBackTrack();
    //	}

    public static int isStarted = -1;
    private static int mLastStartDelay = 0;
    private static long mLastStopTime = 0;

    public static void start(Context context, int delay) {
        Log.e("ReverseUI", "reverse start: " + isShow);
        if (!isShow) {
            mShowFrontCamera = false;
            if (Util.isRKSystem()) {
                mLastStartDelay = delay;
                mHandler.removeMessages(STOP_UI);
                mHandler.removeMessages(REMOVE_UI);
            }

            //			if (RadarManager.isShow) {
            //				RadarManager.stop();
            //				RadarManager.start(context);
            //			}

            if (GlobalDefinition.mReverseBrightness != 0) {
                Log.d("ReverseManager", "mReverseBrightness:" + GlobalDefinition.mReverseBrightness);
                Util.setFileValue(GlobalDefinition.BRIGHTNESS_CVBS, GlobalDefinition.mReverseBrightness);
            }
            if (GlobalDefinition.mReverseContrast != 0) {
                Log.d("ReverseManager", "mReverseContrast:" + GlobalDefinition.mReverseContrast);
                Util.setFileValue(GlobalDefinition.BRIGHTNESS_CONTRAST, GlobalDefinition.mReverseContrast);
            }
            if (mView == null) {
                init(context);
            }

            if (mPresentation != null) {
                mPresentation.show();
            } else {
                if (mView.getParent() == null) {
                    mWindowManager.addView(mView, mLayoutParams);
                }
            }
            // mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            if (mUI == null) {
                mUI = ReverseUI.getInstance(context, mView.findViewById(R.id.screen1_main), 0);

            }

            mUI.onCreate();
            isShow = true;
            mEmptyView.setVisibility(View.VISIBLE);

            if (Util.isRKSystem()) {
                if (mLastStopTime != 0 && (SystemClock.uptimeMillis() - mLastStopTime) < 1500) {
                    mUI.showBlackEx(true, 800);
                }
            }

            if (delay == 0) {
                mUI.setSource();

                mUI.onResume();
                mHandler.removeMessages(START_UI);
                //				mHandler.sendEmptyMessage(START_UI);
            } else {
                mHandler.removeMessages(START_SET_SOURCE);
                mHandler.sendEmptyMessageDelayed(START_SET_SOURCE, delay);
            }

            //			if(MachineConfig.VALUE_CANBOX_VW.equals(   CarUtil.getCanboxType())){
            startRequestAngleData();
            //			}

            if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 30) {
                GlobalDefinition.sendByCarServiceToSystemUI(context, "com.android.systemui", MyCmd.Cmd.SYSTEMUI_STATUS_BAR_GONE);
            }

            Canbox c = CarUtil.getCanboxInstance();
            if (c instanceof Nissan2013Simple) {
                Nissan2013Simple new_name = (Nissan2013Simple) c;
                new_name.show360Button();
            }
            CarUtil.notifyReverse(1);

        }
    }


    private static boolean mShowFrontCamera = false;

    public static boolean switchToFrontCamera(boolean s) {
        boolean ret = false;
        if (isShow) {
            if (mUI != null) {
                int source = MyCmd.CAMERA_SOURCE_FRONT_CAMERA;
                if (!s) {
                    if (CarUtil.isFocusSync3Reverse()) {
                        source = MyCmd.CAMERA_SOURCE_AUX;
                    } else {
                        source = MyCmd.CAMERA_SOURCE_REVERSE;
                    }

                }

                McuManager mcu = McuManager.getInstance();
                if (mcu != null) {
                    mcu.setFrontCamerPower((byte) (s ? 1 : 0), 0x2);
                }

                mShowFrontCamera = s;
                mUI.switchToFrontCamera(source);
                ret = true;
            }
        }
        return ret;
    }


    public static boolean toggleFrontCmaera() {
        boolean ret = false;
        if (AppConfig.isHidePackage("com.zhuchao.android.car.frontcamera.FrontCameraActivity")) {
            return ret;
        }

        if (isShow) {
            ret = switchToFrontCamera(!mShowFrontCamera);
        }
        return ret;
    }

    public static void stop() {
        Log.e("ReverseUI", "reverse stop: " + isShow);
        if (isShow) {
            mHandler.removeMessages(REQUEST_ANGLE);
            //			Log.d("test", "stop");
            if (mPresentation != null) {
                mPresentation.dismiss();
            } else {
                if (Util.isRKSystem()) {
                    //					mHandler.removeMessages(REMOVE_UI);
                    //					int delay;
                    //					if(mLastStartDelay == 0){
                    //						delay = 1000;
                    //					} else {
                    //						delay = 30;
                    //					}
                    //					mLastStartDelay = 0;
                    //					mHandler.sendEmptyMessageDelayed(REMOVE_UI,delay);
                } else {
                    if (mView.getParent() != null) {
                        mWindowManager.removeView(mView);
                    }
                    if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT < 30) {
                        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SYSTEMUI_STATUS_BAR_VISIBLE);
                    }
                }
            }
            mEmptyView.setVisibility(View.VISIBLE);
            mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            if (Util.isRKSystem()) {
                mView.findViewById(R.id.only_black).setVisibility(View.VISIBLE);
                mHandler.removeMessages(STOP_UI);
                mHandler.sendEmptyMessage(STOP_UI);
            } else {
                mUI.onPause();
                mUI.onDestroy();
            }

            mHandler.removeMessages(START_UI);
            mHandler.removeMessages(START_SET_SOURCE);
            isShow = false;

            if (GlobalDefinition.mReverseBrightness != 0) {
                Util.setFileValue(GlobalDefinition.BRIGHTNESS_CVBS, GlobalDefinition.CVBS_DEFALUT_BRIGHTNESS);
            }

            if (GlobalDefinition.mReverseContrast != 0) {
                Util.setFileValue(GlobalDefinition.BRIGHTNESS_CONTRAST, GlobalDefinition.CVBS_DEFALUT_CONTRAST);
            }

            CarUtil.notifyReverse(0);
            McuManager mcu = McuManager.getInstance();
            if (mcu != null) {
                mcu.setFrontCamerPower((byte) 0, 0x2);
            }
            //			Log.d("test", "<<stop");
        }
    }
}
