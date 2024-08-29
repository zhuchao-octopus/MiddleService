package com.zhuchao.android.car.cartype.bagoo;

import android.annotation.SuppressLint;
import android.os.Handler;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class AlphaBagoo extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x2, AK_KEYPAD_VOLUME_A}, {0x3, AK_KEYPAD_VOLUME_D}, {0x4, KEY_NEXTSONG}, {0x5, KEY_PREVIOUSSONG}, {0x13, KEY_NEXTSONG}, {0x12, KEY_PREVIOUSSONG}, {0x1, KEY_MUTE},

            {0x11, KEY_SOURCE}, {0x15, KEY_MIC}, {0x14, KEY_BT},

    };
    byte[] data;
    private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
    private int mUnit = 0;
    private int mDoorStatus = 0;

    public AlphaBagoo() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});
    }

    private void parseWheelKey(byte[] data) {

        byte updown = 1;
        if (data[2] == 0) {
            updown = 0;
        }

        if (doKeyStudy(data[2], updown)) {
            return;
        }

        byte key = 0;
        for (int i = 0; i < KEYS_WHEEL.length; ++i) {
            if (KEYS_WHEEL[i][0] == data[2]) {
                key = KEYS_WHEEL[i][1];
                break;
            }
        }

        // if (key != 0) {
        doKey(key, updown);
        // }
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x01: {
                parseWheelKey(data);
            }

            break;

            case 0x71: {
                byte[] version = new byte[9];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = Util.byte2HexStr(version);
                break;
            }
            case 0x4: {
                int door = (data[2] & 0xf8);
                door = (((door & 0x40) >> 5) | ((door & 0x80) >> 7) | ((door & 0x10) >> 1) | ((door & 0x20) >> 3) | ((door & 0x08) << 1));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }

            }
            break;

            case 0x6: {

                int t = ((data[2] & 0xff));
                int temp = -400 + t * 5;
                mUnit = data[3];
                updateOutDoorTemp(temp);

            }
            break;
        }

    }

    @SuppressLint("DefaultLocale")
    public void updateOutDoorTemp(int temp) {

        if (temp == CarUtil.INVALID_OUT_DOOR_TEMP) {
            if (mTempOutDoor != CarUtil.INVALID_OUT_DOOR_TEMP) {
                temp = mTempOutDoor;
            } else {
                return;
            }
        }
        mTempOutDoor = temp;

        if (CarUtil.mTempUnit == 2) {
            if (mUnit != 1) {
                float t = temp / 10.0f;
                temp = (int) (((t) * 1.8f + 32) * 10);
            }
            mUnit = 1;
        } else if (CarUtil.mTempUnit == 1) {
            if (mUnit == 1) {
                temp = (int) ((((float) temp / 10.0f) - 32) / 1.8f) * 10;
            }
            mUnit = 0;
        }

        String s = "";
        // if (temp >= -58 && temp <= 171) {
        if (mUnit != 1) {

            s = String.format("%d.%d%s", temp / 10, (temp % 10) >= 0 ? (temp % 10) : -(temp % 10), mContext.getResources().getString(R.string.temp_unic_centigrade));

        } else {
            float t = temp / 10.0f;
            temp = (int) (((t) * 1.8f + 32) * 10);

            s = String.format("%d%s", temp, mContext.getResources().getString(R.string.temp_unic_fahrenheit));
        }

        if (s.length() > 1) {
            GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
        }

    }

    public void requestInfo(byte param) {
        byte[] data = new byte[]{(byte) 0x90, 0x2, param, 0};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        switch (source) {
            case MyCmd.SOURCE_DVD:
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                ++play;
                break;
            default:
                return;
        }

        if (play > 99) {
            play = 99;
        }
        byte[] data = new byte[]{(byte) 0x82, 0x5, 0x2, 0, (byte) (play), 0, 0};
        sendDataToCanbox(data, data.length);

    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        if (b[0] >= 0x10) {
            b[0] = 5;
        } else {
            if (b[0] == 2) {
                b[0] = 6;
            }

            int freq = (((b[2] & 0xff) << 8) | (b[1] & 0xff)) / 10;

            b[2] = (byte) ((freq & 0xff00) >> 8);
            b[1] = (byte) ((freq & 0xff));
        }

        byte[] data = new byte[]{(byte) 0x82, 0x5, 0x1, b[0], 0, b[2], b[1]};
        sendDataToCanbox(data, data.length);
    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        super.startConnect();
        byte[] data = new byte[]{(byte) 0xf1, 0x1, 0x1};
        sendDataToCanbox(data, data.length);
    }

    @Override
    public void stopConnect() {
        // TODO Auto-generated method stub
        byte[] data = new byte[]{(byte) 0x82, 0x5, 0x0, 0, 0, 0, 0};
        sendDataToCanbox(data, data.length);
        super.stopConnect();
    }

    public void setMediaSrc(int source) {// default is simple box
        switch (source) {
            case MyCmd.SOURCE_DVD:
            case MyCmd.SOURCE_RADIO:
                return;
            default:
                byte[] data = new byte[]{(byte) 0x82, 0x5, 0xf, 0, 0, 0, 0};
                sendDataToCanbox(data, data.length);
                break;
        }
    }

}
