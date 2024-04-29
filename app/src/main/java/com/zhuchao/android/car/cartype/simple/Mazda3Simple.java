package com.zhuchao.android.car.cartype.simple;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;


public class Mazda3Simple extends Canbox {

    public Mazda3Simple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });

    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x4, KEY_PREVIOUSSONG}, {0x3, KEY_NEXTSONG},

            {0x6, KEY_MUTE},

            {0x8, KEY_MIC}, {0x9, KEY_BT_DIAL}, {0xa, KEY_BT_HANG}, {0x20, MyCmd.Keycode.AUDIO}, {0x21, MyCmd.Keycode.RADIO}, {0x22, MyCmd.Keycode.HOME}, {0x23, MyCmd.Keycode.BACK},
            {0x24, MyCmd.Keycode.NAVIGATION}, {0x25, MyCmd.Keycode.PLAY_PAUSE}, {0x26, MyCmd.Keycode.PREVIOUS}, {0x27, MyCmd.Keycode.NEXT}, {0x28, MyCmd.Keycode.PREVIOUS}, {0x29, MyCmd.Keycode.NEXT},
            {0x2a, MyCmd.Keycode.MUTE}, {0x2b, MyCmd.Keycode.KEY_TURN_A}, {0x2c, MyCmd.Keycode.KEY_TURN_D}, {0x2d, MyCmd.Keycode.VOLUME_UP}, {0x2e, MyCmd.Keycode.VOLUME_DOWN},
    };

    private void parseWheelKey(byte[] data) {
        if (doKeyStudy(data[2], data[3])) {
            return;
        }
        byte key = 0;

        for (int i = 0; i < KEYS_WHEEL.length; ++i) {
            if (KEYS_WHEEL[i][0] == data[2]) {
                key = KEYS_WHEEL[i][1];
                break;
            }
        }

        if (key != 0) {

            if (isRollKey(data[2])) {
                int step = data[3] & 0xff;
                // step = 0x10;
                if (step > 0 && step <= 10) {
                    doKeyStep(key, step);
                }
            } else {
                doKey(key, data[3]);
            }
        } else {
            if (data[3] == 0 && !isRollKey(data[2])) {
                doKey(0, 0);
            }
        }
    }

    private boolean isRollKey(byte b) {
        return ((b & 0xff) >= 0x2b) && ((b & 0xff) <= 0x2e);
    }

    private void doKeyStep(int key, int step) {
        mHandler.removeMessages(SHOW_VOLUME_STEP);
        doKey(key, 1);
        doKey(key, 0);
        --step;
        if (step > 0) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW_VOLUME_STEP, key, step), 30);
        }
    }

    private final static int SHOW_VOLUME_STEP = 1;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == SHOW_VOLUME_STEP) {
                doKeyStep(msg.arg1, msg.arg2);
            }
            super.handleMessage(msg);
        }
    };

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
    }

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, KEY_FM}, {0x2, KEY_FM}, {0x3, KEY_FM}, {0x9, KEY_FM},

            {0x4, KEY_DVD}, {0x5, KEY_MEDIA}, {0x6, KEY_MEDIA}, {0xa, KEY_MEDIA},

            {0x7, MyCmd.Keycode.BT_MUSIC}, {0x8, MyCmd.Keycode.AUX_IN},

            {0xe, MyCmd.Keycode.KEY_TV}, {0x10, MyCmd.Keycode.ALL_APP},

            {0x11, KEY_BT_DIAL}, {0x12, KEY_BT_HANG},
    };

    private int mDoorStatus = 0;

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data);
            }

            break;

            case 0x24: {
                int door = (data[2] & 0xff);
                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((door & 0x04) << 3));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }
            }
            break;

            case 0x30: {
                byte[] version = new byte[0x10];
                Util.byteArrayCopy(version, data, 0, 2, version.length);
                mVersion = (new String(version));
                break;
            }
            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu
                    // ma

                    int angle = -(((a * 3000) / 0x1200));

                    if (angle > -50 && angle < 0) {
                        angle = -50;
                    } else if (angle > 0 && angle < 50) {
                        angle = 50;
                    }
                    // Log.e("1", ""+(data[2] & 0xff));
                    // Log.e("2", ""+(data[3] & 0xff));
                    // Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
                }
            }
            break;
            case 0x27:
            case 0x25:
            case 0x26:
            case 0x28:
            case 0x40:
            case 0x16: {
                sendCanboxInfo("com.canboxsetting", data);
            }
            break;

        }
    }

}
