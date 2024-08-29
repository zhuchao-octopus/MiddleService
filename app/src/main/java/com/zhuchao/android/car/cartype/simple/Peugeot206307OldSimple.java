package com.zhuchao.android.car.cartype.simple;

import android.os.Handler;
import android.os.Message;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;

public class Peugeot206307OldSimple extends Canbox {

    private final static byte[][] KEYS_WHEEL = {

            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_PREVIOUSSONG}, {0x4, KEY_NEXTSONG}, {0x5, KEY_SOURCE},

            {0x6, KEY_PREVIOUSSONG}, {0x7, KEY_NEXTSONG}, {0x13, MyCmd.Keycode.KEY_TURN_A}, {0x14, MyCmd.Keycode.KEY_TURN_D}, {(byte) 0x80, KEY_HOME},

    };
    private final static int HIDE_RADAR = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_RADAR) {
                RadarManager.stop();
            }
            super.handleMessage(msg);
        }
    };
    private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
    private int mDoorStatus = 0;

    public Peugeot206307OldSimple() {

        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});

    }

    private void parseWheelKey(byte[] data, int len) {
        if (doKeyStudy(data[2], data[3])) {
            return;
        }
        int key = 0;
        for (int i = 0; i < KEYS_WHEEL.length; ++i) {
            if (KEYS_WHEEL[i][0] == data[2]) {
                key = KEYS_WHEEL[i][1];
                break;
            }
        }

        if (key != 0) {
            doKey(key, data[3]);
        } else {
            if (data[3] == 0) {
                doKey(0, 0);
            }
        }

    }

    public void updateOutDoorTemp(int temp) {

        if (temp == CarUtil.INVALID_OUT_DOOR_TEMP) {
            if (mTempOutDoor != CarUtil.INVALID_OUT_DOOR_TEMP) {
                temp = mTempOutDoor;
            } else {
                return;
            }
        }
        mTempOutDoor = temp;
        String s;
        if (CarUtil.mTempUnit == 2) {
            temp = (int) ((temp) * 1.8f + 32);
            s = temp + mContext.getResources().getString(R.string.temp_unic_fahrenheit);
        } else {
            s = temp + mContext.getResources().getString(R.string.temp_unic_centigrade);
        }

        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);


    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data, len);
            }
            break;

            case 0x1: {
                int door = (data[12] & 0xfe);

                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((door & 0x04) << 3));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }
            }

            updateOutDoorTemp(data[11]);
            sendCanboxInfo("com.canboxsetting", data);
            break;
            case 0x30: {
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
        }
    }

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

}
