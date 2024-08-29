package com.zhuchao.android.car.cartype.luzheng;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;


public class SmartHaozheng extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x5, KEY_BT},

    };
    byte[] mAirData = new byte[8];
    private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
    private int mUnit = 0;
    private int mDoorStatus = 0;

    public SmartHaozheng() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});
    }

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
            doKey(key, data[3]);
        } else {
            if (data[3] == 0) {
                doKey(0, 0);
            }
        }
    }

    private void parseACInfo(byte[] data, int len) {

        if (data[4] >= 0x1f) {
            data[4] = (byte) 0xff;
        } else if (data[4] > 0) {
            data[4] = (byte) (35 + data[4]);
        }

        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xf2) | ((data[2] & 0x08) >> 3));
        airData[1] = (byte) (data[3] & 0xff);

        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[4] & 0xff);

        boolean airControl = false;
        if (airData[0] != mAirData[0] || airData[1] != mAirData[1] || airData[2] != mAirData[2] || airData[3] != mAirData[3]) {
            airControl = true;

            mAirData = airData;
        }

        int msg = CANBOX_HIDE_AIR;
        if ((data[3] & 0x10) != 0) {
            msg = CANBOX_RETURN_AIR;
        }

        if (airControl) {
            Handler handler = getHandler("CanService");
            if (null != handler) {
                handler.sendMessage(handler.obtainMessage(msg, airData));
            }
        }

        if ((data[6] & 0xff) == 0xf0 || (data[6] & 0xff) == 0x0f) {
            if ((data[6] & 0xff) == 0x0f) {
                mUnit = 1;
            } else {
                mUnit = 0;
            }
            int temp = ((data[5] & 0xff) - 40) * 10;
            updateOutDoorTemp(temp);
        }

    }

    private byte getRadarData(byte i) {
        byte data = 0;
        switch (i) {
            case 0:
                data = 0;
                break;
            case 1:
                data = 1;
                break;
            case 2:
                data = 4;
                break;
            case 3:
                data = 7;
                break;
            case 4:
                data = 11;
                break;
        }
        return data;
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data);
            }

            break;
            case 0x21: {
                parseACInfo(data, len);
            }
            break;
            case 0x30: {
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
            case 0x24: {
                int door = (data[3] & 0x0f);

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }

            }
            break;

            case 0x26: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = ((data[2] & 0xff) | ((data[3]) << 8));
                    angle = (angle * 300 / 540);
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));
                }
            }
            break;

            case 0x38:
                sendCanboxInfo("com.canboxsetting", data);
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
            // if (mUnit != 1){
            float t = temp / 10.0f;
            temp = (int) (((t) * 1.8f + 32) * 10);
            // }
            mUnit = 1;
        } else if (CarUtil.mTempUnit == 1) {
            // if (mUnit == 1){
            // temp = (int)((((float)temp/10.0f)-32)/1.8f)*10;
            // }
            mUnit = 0;
        } else {
            if (mUnit == 1) {
                float t = temp / 10.0f;
                temp = (int) (((t) * 1.8f + 32) * 10);
            }
        }

        String s = "";
        // if (temp >= -58 && temp <= 171) {
        if (mUnit != 1) {

            s = String.format("%d.%d%s", temp / 10, (temp % 10) >= 0 ? (temp % 10) : -(temp % 10), mContext.getResources().getString(R.string.temp_unic_centigrade));

        } else {
            s = String.format("%d%s", temp / 10, mContext.getResources().getString(R.string.temp_unic_fahrenheit));
        }

        if (s.length() > 1) {
            GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
        }

    }

    public void updateTime() {
        if (mContext == null) {
            return;
        }
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        byte ampm = 0;
        if ("12".equals(strTimeFormat)) {
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
            ampm = 1;
        }

        byte m = (byte) curDate.getMinutes();
        //		byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();

        byte[] buf = new byte[]{(byte) 0xc6, 0x06, y, mon, d, h, m, ampm};
        sendDataToCanbox(buf, buf.length);
    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
    }

    public int getUpdateTime() {
        return 60000;
    }
}
