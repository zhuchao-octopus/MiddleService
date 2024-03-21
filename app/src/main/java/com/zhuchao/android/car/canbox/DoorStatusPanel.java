package com.zhuchao.android.car.canbox;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.zhuchao.android.car.R;
import com.common.util.MachineConfig;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.cartype.CarUtil;

import com.zhuchao.android.car.tts.TextSpeaker;

public class DoorStatusPanel extends Handler {
    /**
     * Called when the activity is first created.
     */
    private static final String TAG = "DoorStatusPanel";
    public static final int MESSAGE_DOOR_CONDITION = 0x01;
    public static DoorStatusPanel mThis;

    //	private Toast mToast = null;
    public void postChanged(int type, int arg1) {
        if (hasMessages(type)) return;
        obtainMessage(type, arg1, 0).sendToTarget();
    }
    /*bit
     *
     * 0:左前
     * 1：右前
     * 2.左后
     * 3.右后
     * 4.后
     * 5。前
     * 6.天窗半 开
     * 7.开窗全开
     * */

    public void handleMessage(Message msg) {
        if (msg.what == MESSAGE_DOOR_CONDITION) {//if (msg.arg1 != 0) {
            if (CarUtil.getFrontDoor() == 2 && CarUtil.getBackDoor() == 2) { //hide
                return;
            }


            if (OBDView.mNeedData) { //hide
                return;
            }

            if (CarUtil.getFrontDoor() == 1) {// change temp
                int temp = msg.arg1;
                msg.arg1 &= ~0x3;
                msg.arg1 |= (((temp & 0x1) << 1) | ((temp & 0x2) >> 1));
            }
            if (CarUtil.getBackDoor() == 1) {// change temp
                int temp = msg.arg1;
                msg.arg1 &= ~0xC;
                msg.arg1 |= (((temp & 0x4) << 1) | ((temp & 0x8) >> 1));
            }

            if (GlobalDefinition.mRudder) {// change temp
                {
                    int temp = msg.arg1;
                    msg.arg1 &= ~0x3;
                    msg.arg1 |= (((temp & 0x1) << 1) | ((temp & 0x2) >> 1));
                }
                {
                    int temp = msg.arg1;
                    msg.arg1 &= ~0xC;
                    msg.arg1 |= (((temp & 0x4) << 1) | ((temp & 0x8) >> 1));
                }
            }

            if (CarUtil.getFrontDoor() == 2) { // hide
                msg.arg1 &= ~0x3;
            }
            if (CarUtil.getBackDoor() == 2) { // hide
                msg.arg1 &= ~0xC;
            }

            View mainView = airConditionView;//mToast.getView();
            View v;
            int type = MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_CANBOX_DOOR_TYPE);
            if (MachineConfig.VALUE_CANBOX_MAZDA_BT50_SIMPLE.equals(CarUtil.getCanboxType()) || (type == 1)) {
                v = mainView.findViewById(R.id.door_car);
                if (v != null) {
                    v.setVisibility(View.GONE);
                }
                v = mainView.findViewById(R.id.door_car_pickup);
                if (v != null) {
                    v.setVisibility(View.VISIBLE);
                }
            } else {
                v = mainView.findViewById(R.id.door_car_pickup);
                if (v != null) {
                    v.setVisibility(View.GONE);
                }
                v = mainView.findViewById(R.id.door_car);
                if (v != null) {
                    v.setVisibility(View.VISIBLE);
                }
            }

            int visible;
            v = mainView.findViewById(R.id.door_status_1);
            visible = v.getVisibility();
            if ((msg.arg1 & 0x1) == 0) {
                v.setVisibility(View.INVISIBLE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
            if ((msg.arg1 & 0x1) != (mDoorStatus & 0x1)) {
                prepareVoice(MSG_LF, v.getVisibility() == View.VISIBLE);
            }


            v = mainView.findViewById(R.id.door_status_2);
            visible = v.getVisibility();
            if ((msg.arg1 & 0x2) == 0) {
                v.setVisibility(View.INVISIBLE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
            if ((msg.arg1 & 0x2) != (mDoorStatus & 0x2)) {
                prepareVoice(MSG_RF, v.getVisibility() == View.VISIBLE);
            }
            v = mainView.findViewById(R.id.door_status_3);
            visible = v.getVisibility();
            if ((msg.arg1 & 0x4) == 0) {
                v.setVisibility(View.INVISIBLE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
            if ((msg.arg1 & 0x4) != (mDoorStatus & 0x4)) {
                prepareVoice(MSG_LR, v.getVisibility() == View.VISIBLE);
            }
            v = mainView.findViewById(R.id.door_status_4);
            visible = v.getVisibility();
            if ((msg.arg1 & 0x8) == 0) {
                v.setVisibility(View.INVISIBLE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
            if ((msg.arg1 & 0x8) != (mDoorStatus & 0x8)) {
                prepareVoice(MSG_RR, v.getVisibility() == View.VISIBLE);
            }
            v = mainView.findViewById(R.id.door_status_5);
            if ((msg.arg1 & 0x10) == 0) {
                v.setVisibility(View.INVISIBLE);
            } else {
                v.setVisibility(View.VISIBLE);
            }

            if ((msg.arg1 & 0x10) != (mDoorStatus & 0x10)) {
                prepareVoice(MSG_TAIL, v.getVisibility() == View.VISIBLE);
            }

            v = mainView.findViewById(R.id.door_status_6);
            if ((msg.arg1 & 0x20) == 0) {
                v.setVisibility(View.INVISIBLE);
            } else {
                v.setVisibility(View.VISIBLE);
            }

            if ((msg.arg1 & 0x20) != (mDoorStatus & 0x20)) {
                prepareVoice(MSG_HOOD, v.getVisibility() == View.VISIBLE);
            }

            v = mainView.findViewById(R.id.door_status_7);
            if ((msg.arg1 & 0x40) == 0) {
                v.setVisibility(View.INVISIBLE);
            } else {
                v.setVisibility(View.VISIBLE);
            }

            //				if ((msg.arg1 & 0x40) != (mDoorStatus & 0x40)) {
            //					prepareVoice(MSG_SKYLIGHT_HALF, v.getVisibility() == View.VISIBLE);
            //				}

            v = mainView.findViewById(R.id.door_status_8);
            if ((msg.arg1 & 0x80) == 0) {
                v.setVisibility(View.INVISIBLE);
            } else {
                v.setVisibility(View.VISIBLE);
            }
            if ((msg.arg1 & 0xC0) != (mDoorStatus & 0xC0)) {
                if ((msg.arg1 & 0xC0) == 0) {
                    prepareVoice(MSG_SKYLIGHT, false);
                } else if ((msg.arg1 & 0x80) != 0) {
                    prepareVoice(MSG_SKYLIGHT, true);
                } else if ((msg.arg1 & 0x40) != 0) {
                    prepareVoice(MSG_SKYLIGHT_HALF, true);
                }

            }

            mWarningTime = 0;
            //				mToast.setView(mainView);
            //				mToast.show();
            if (mDoorStatus != msg.arg1) {
                mDoorStatus = msg.arg1;
                mHandler.removeMessages(0);
                mHandler.sendEmptyMessageDelayed(0, 3000);
                if (airConditionView != null && airConditionView.getParent() == null) {
                    mWindowManager.addView(airConditionView, mLayoutParams);
                }
            }
            //}
        }
    }

    View airConditionView = null;
    private final Context mContext;

    public DoorStatusPanel(Context context) {
        mThis = this;
        mContext = context;
        //	mToast = new Toast(context); // Toast.makeText(context,"",
        // Toast.LENGTH_SHORT);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        airConditionView = inflater.inflate(R.layout.door_status, null);
        //	mToast.setView(airConditionView);

        mLayoutParams = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.RGBA_8888);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    WindowManager mWindowManager;
    WindowManager.LayoutParams mLayoutParams;

    private final static int MSG_LF = 100;
    private final static int MSG_RF = 101;
    private final static int MSG_LR = 102;
    private final static int MSG_RR = 103;


    private final static int MSG_HOOD = 104;
    private final static int MSG_TAIL = 105;
    private final static int MSG_SKYLIGHT_HALF = 106;
    private final static int MSG_SKYLIGHT = 107;

    private void prepareVoice(int msg, boolean show) {
        if (GlobalDefinition.mSettingDoorVoice == 1) {
            mHandler.removeMessages(msg);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(msg, show), 1000);
        }
    }

    private void doVoice(int msg, boolean show) {
        if (GlobalDefinition.mSettingDoorVoice == 1) {
            String s = null;
            switch (msg) {

                case MSG_LF:
                    s = mContext.getString(R.string.left_front);
                    break;
                case MSG_LR:
                    s = mContext.getString(R.string.left_rear);
                    break;
                case MSG_RF:
                    s = mContext.getString(R.string.right_front);
                    break;
                case MSG_RR:
                    s = mContext.getString(R.string.right_rear);
                    break;
                case MSG_HOOD:
                    s = mContext.getString(R.string.hood);
                    break;
                case MSG_TAIL:
                    s = mContext.getString(R.string.tail);
                    break;
                case MSG_SKYLIGHT:
                    s = mContext.getString(R.string.skylight_all);
                    break;
                case MSG_SKYLIGHT_HALF:
                    s = mContext.getString(R.string.skylight_half);
                    break;
            }
            if (s != null) {

                String s2;
                if (show) {
                    s2 = mContext.getString(R.string.open);
                } else {
                    s2 = mContext.getString(R.string.close);
                }
                s = s + " " + s2;//String.format(mContext.getString(R.string.is), s, s2);
                TextSpeaker.speakDirect(GlobalDefinition.getContext(), s);
            }
        }
    }

    public static int mDoorStatus = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (airConditionView != null) {
                        if (airConditionView.getParent() != null) {
                            mWindowManager.removeView(airConditionView);
                        }
                    }
                    break;
                case MSG_LF:
                case MSG_LR:
                case MSG_RF:
                case MSG_RR:
                case MSG_TAIL:
                case MSG_HOOD:
                case MSG_SKYLIGHT:
                case MSG_SKYLIGHT_HALF:
                    doVoice(msg.what, (Boolean) msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private long mWarningTime = 0;
    private long mLastWarningTime = 0;

    //	private final static int WARNING_TIME = 20000;
    public static void checkDoorOK() {
        if (mThis != null) {
            mThis.startcheckDoorOK();
        }
    }

    public void startcheckDoorOK() {
        if (mDoorStatus == 0 || mWarningTime > 10) {
            return;
        }

        int msg = getWarningMsg();
        if (msg != 0) {
            long l = System.currentTimeMillis();
            if (mLastWarningTime == 0 || ((l - mLastWarningTime >= 4000))) {
                mLastWarningTime = l;
                prepareVoice(msg, true);
                ++mWarningTime;
            }
        }
    }

    private int getWarningMsg() {
        int msg = 0;
        if ((mDoorStatus & 0x1) != 0) {
            msg = MSG_LF;
            return msg;
        }

        if ((mDoorStatus & 0x2) != 0) {
            msg = MSG_RF;
            return msg;
        }

        if ((mDoorStatus & 0x4) != 0) {
            msg = MSG_LR;
            return msg;
        }

        if ((mDoorStatus & 0x8) != 0) {
            msg = MSG_RF;
            return msg;
        }

        if ((mDoorStatus & 0x10) != 0) {
            msg = MSG_TAIL;
            return msg;
        }

        if ((mDoorStatus & 0x20) != 0) {
            msg = MSG_HOOD;
            return msg;
        }

        if ((mDoorStatus & 0x40) != 0) {
            msg = MSG_SKYLIGHT;
            return msg;
        }
        return 0;
    }
}