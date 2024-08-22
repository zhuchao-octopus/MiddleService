package com.zhuchao.android.car.cartype.hiworld;

import android.os.Handler;

import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;


public class MiniHiword extends Canbox {

    public MiniHiword() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x3, 0x0, 0x4, 0x0
        });
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x5, KEY_BT},

            {0x8, KEY_NEXTSONG}, {0x9, KEY_PREVIOUSSONG},

            {0x18, KEY_MIC},

    };

    public void startConnect() {

    }

    @Override
    public void stopConnect() {

    }

    private void parseWheelKey(byte data) {
        if (doKeyStudy(data, (data == 0) ? 0 : 1)) {
            return;
        }
        byte key = 0;
        for (int i = 0; i < KEYS_WHEEL.length; ++i) {
            if (KEYS_WHEEL[i][0] == data) {
                key = KEYS_WHEEL[i][1];
                break;
            }
        }

        if (key != 0) {
            doKey(key, 1);
        } else {
            doKey(0, 0);
        }
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[2]) {
            case 0x11: {
                parseWheelKey(data[5]);
            }

            break;
            case (byte) 0xF0: {
                byte[] version = new byte[0x11];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
            case 0x12: {
                int door = (data[5] & 0xc0);

                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }

                }

            }
            break;

        }

    }

    private int mDoorStatus = 0;

}
