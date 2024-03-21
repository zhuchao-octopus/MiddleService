package com.zhuchao.android.car.canbox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.cartype.CarUtil;

import com.zhuchao.android.car.R;
import com.common.util.MachineConfig;

public class RadarUI extends UIBase {
    private Canbox mCanBox;

    private static final RadarUI[] mUI = new RadarUI[MAX_DISPLAY];

    /**
     * Called when the activity is first created.
     */
    public static RadarUI getInstanse(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }

        mUI[index] = new RadarUI(context, view, index);

        return mUI[index];
    }

    public RadarUI(Context context, View view, int index) {
        super(context, view, index);
    }

    public void onCreate() {

        super.onCreate();

        Canbox.addHandler(RadarManager.TAG, mHandlerCanbox);

        mCanBox = CarUtil.getCanboxInstance();
        initRadarView();
        mShow = true;
    }

    @Override
    public void onPause() {
        clearBeep();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        GlobalDefinition.mAutoFrontCameraStatus = 0;
        mMainView.setVisibility(View.GONE);
    }

    public void onDestroy() {
        Canbox.removeHandler(RadarManager.TAG);
        super.onDestroy();
    }

    private RadarView1 mImageViewCarLeft;

    // private ReverseLocus mImageViewLocus;
    String mSystemUI;

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initRadarView() {
        mImageViewCarLeft = mMainView.findViewById(R.id.reverse_left_image);
        mMainView.findViewById(R.id.radar_switch).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mShow = !mShow;
                if (mShow) {
                    //							mMainView.findViewById(R.id.reverse_left_image)
                    //									.setVisibility(View.VISIBLE);
                    RadarManager.updateView(0);
                } else {
                    //							mMainView.findViewById(R.id.reverse_left_image)
                    //									.setVisibility(View.GONE);
                    RadarManager.updateView(1);
                }
            }
        });


        if (GlobalDefinition.mSystemUI != null && GlobalDefinition.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI_KLD7_1992)) {
            Drawable d = mContext.getResources().getDrawable(R.drawable.reverse_left_pickup,null);
            if (d != null) {
                mImageViewCarLeft.setImageDrawable(d);
            }
        }
    }

    private boolean mShow = true;

    private final static int MSG_HIDE_FRONT_CAMRA_BY_OPEN = 0xfffff;
    private final static int TIME_HIDE_FRONT_CAMRA_BY_OPEN = 3000;

    private void showFrontCamera() {
        if (GlobalDefinition.mSettingRadarFrontCamera == 1) {
            if ((GlobalDefinition.mAutoFrontCameraStatus == 0) && (mCanBox.mRadar[4] != 0 || mCanBox.mRadar[5] != 0 || mCanBox.mRadar[6] != 0 || mCanBox.mRadar[7] != 0 || mCanBox.mRadarFontEx[0] != 0 || mCanBox.mRadarFontEx[1] != 0)) {
                GlobalDefinition.autoOpenFrontByGpsSpeed();
            }
            if (GlobalDefinition.mAutoFrontCameraStatus == 1) {
                mHandlerCanbox.removeMessages(MSG_HIDE_FRONT_CAMRA_BY_OPEN);
                mHandlerCanbox.sendEmptyMessageDelayed(MSG_HIDE_FRONT_CAMRA_BY_OPEN, TIME_HIDE_FRONT_CAMRA_BY_OPEN);
            }
        }
    }

    private final Handler mHandlerCanbox = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Canbox.CANBOX_RADAR_FRONT: {
                    if (msg.arg1 != 1) {
                        mMainView.setVisibility(View.VISIBLE);
                    }
                    if (mImageViewCarLeft != null) {
                        if (msg.obj != null) {
                            mImageViewCarLeft.setRadarColor((int[]) msg.obj);
                        } else {
                            mImageViewCarLeft.setRadarColor(null);
                        }

                        //					if(msg.arg2 != 0){
                        mImageViewCarLeft.setRadarNum(msg.arg2);
                        //					}
                    }
                    showFrontCamera();
                    showRadarOSD();
                    showBeep();
                }
                break;
                case Canbox.CANBOX_RADAR_BACK: {
                    if (msg.arg1 != 1) {
                        mMainView.setVisibility(View.VISIBLE);
                    }
                    if (mImageViewCarLeft != null) {
                        if (msg.obj != null) {
                            mImageViewCarLeft.setRadarColor((int[]) msg.obj);
                        } else {
                            mImageViewCarLeft.setRadarColor(null);
                        }
                        //					if(msg.arg2 != 0){
                        mImageViewCarLeft.setRadarNum(msg.arg2);
                        //					}
                    }
                    showRadarOSD();
                    showBeep();
                }
                break;

                case Canbox.CANBOX_RADAR_LEFT: {
                    if (msg.arg1 != 1) {
                        mMainView.setVisibility(View.VISIBLE);
                    }
                    if (mImageViewCarLeft != null) {
                        if (msg.obj != null) {
                            mImageViewCarLeft.setRadarLeftColor((int[]) msg.obj);
                        } else {
                            mImageViewCarLeft.setRadarLeftColor(null);
                        }

                        mImageViewCarLeft.setRadarLeft(mCanBox.mRadarLeft);
                    }
                    showRadarOSD();
                }
                break;

                case Canbox.CANBOX_RADAR_RIGHT: {
                    if (msg.arg1 != 1) {
                        mMainView.setVisibility(View.VISIBLE);
                    }
                    if (mImageViewCarLeft != null) {
                        if (msg.obj != null) {
                            mImageViewCarLeft.setRadarRightColor((int[]) msg.obj);
                        } else {
                            mImageViewCarLeft.setRadarRightColor(null);
                        }

                        mImageViewCarLeft.setRadarRight(mCanBox.mRadarRight);
                    }
                    showRadarOSD();
                }
                break;

                case Canbox.CANBOX_RADAR_STATUS: {
                    // byte[] status = (byte[]) msg.obj;
                    // showRadarStatus(status);
                }
                break;
                case Canbox.CANBOX_RADAR_SWITCH: {
                    if (msg.arg1 == 0) {
                        mMainView.setVisibility(View.GONE);
                    } else {
                        mMainView.setVisibility(View.VISIBLE);
                    }
                }
                break;
                case MSG_HIDE_FRONT_CAMRA_BY_OPEN:
                    GlobalDefinition.autoCloseFrontByGpsSpeed();
                    break;
                case MSG_BEEP:
                    if (!mPause) {
                        if (TIME_BEEP_DANGER == msg.arg1) {
                            GlobalDefinition.beep(3);
                        } else {
                            GlobalDefinition.beep(2);
                        }

                        mHandlerCanbox.removeMessages(MSG_BEEP);
                        mHandlerCanbox.sendMessageDelayed(mHandlerCanbox.obtainMessage(MSG_BEEP, msg.arg1, 0), msg.arg1);
                    }
                    break;
            }

        }
    };

    private void showRadarOSD() {

        if (mCanBox != null) {
            if (mImageViewCarLeft != null) {
                mImageViewCarLeft.setRadarData(mCanBox.mRadar);
                mImageViewCarLeft.setRadarDataFrontEx(mCanBox.mRadarFontEx);
                mImageViewCarLeft.invalidate();
            }
        }

    }

    private int mBeepType = 0;
    private final static int MSG_BEEP = 0xFFFF;
    private final static int TIME_BEEP_LONG = 2000;
    private final static int TIME_BEEP_SHORT = 201;
    private final static int TIME_BEEP_DANGER = 200;

    private void clearBeep() {
        mBeepType = 0;
        mHandlerCanbox.removeMessages(MSG_BEEP);
    }

    private void showBeep() {
        if (CarUtil.getRadarBeep()) {
            if (mCanBox != null) {
                int type = TIME_BEEP_LONG + 1;
                for (int i = 0; i < mCanBox.mRadar.length; ++i) {
                    if (mCanBox.mRadar[i] > 0) {
                        if (mCanBox.mRadar[i] == 1) {
                            if (type > TIME_BEEP_DANGER) {
                                type = TIME_BEEP_DANGER;
                            }
                        } else if (mCanBox.mRadar[i] <= 4) {
                            if (type > TIME_BEEP_SHORT) {
                                type = TIME_BEEP_SHORT;
                            }
                        } else if (mCanBox.mRadar[i] <= 6) {
                            if (type > TIME_BEEP_LONG) {
                                type = TIME_BEEP_LONG;
                            }
                        }
                    }
                }

                if (type == TIME_BEEP_LONG || type == TIME_BEEP_SHORT) {
                    if (type != mBeepType) {
                        GlobalDefinition.beep(2);
                        mHandlerCanbox.removeMessages(MSG_BEEP);
                        mHandlerCanbox.sendMessageDelayed(mHandlerCanbox.obtainMessage(MSG_BEEP, type, 0), type);
                    }
                } else if (type == TIME_BEEP_DANGER) {
                    if (type != mBeepType) {
                        mHandlerCanbox.removeMessages(MSG_BEEP);
                        mHandlerCanbox.sendMessageDelayed(mHandlerCanbox.obtainMessage(MSG_BEEP, type, 0), type);
                        GlobalDefinition.beep(3);
                    }
                } else {
                    mHandlerCanbox.removeMessages(MSG_BEEP);
                }

                mBeepType = type;
            }
        }
    }

}
