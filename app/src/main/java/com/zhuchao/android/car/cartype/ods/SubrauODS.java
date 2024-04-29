package com.zhuchao.android.car.cartype.ods;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;


public class SubrauODS extends Canbox {

    public SubrauODS() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x6, KEY_MUTE}, {0x7, KEY_SOURCE}, {0x9, KEY_BT_DIAL}, {0xa, KEY_BT_HANG},

            {0x17, AK_KEYPAD_VOLUME_A}, {0x18, AK_KEYPAD_VOLUME_D},

            {0xc, KEY_NEXTSONG}, {0xb, KEY_PREVIOUSSONG},


            {(byte) 0x82, KEY_NEXTSONG}, {(byte) 0x83, KEY_PREVIOUSSONG}, {(byte) 0x81, KEY_FM}, {(byte) 0x87, KEY_MEDIA},


            {(byte) 0x80, KEY_POWER}, {(byte) 0x85, KEY_BT},


            {(byte) 0x84, KEY_BACK}, {(byte) 0x86, KEY_GPS}, {(byte) 0x8b, KEY_SET},

    };

    private int mKey = 0;

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
            mKey = key;
            if (data[3] > 1) {
                data[3] = 1;
            }
            doKey(key, data[3]);
        } else if (mKey != 0) {
            mKey = 0;
            doKey(mKey, 0);
        }
    }

    byte[] mAirData = new byte[8];

    private byte mTemp = 127;

    private void parseACInfo(byte[] data, int len) {
        if (data[6] >= 30) {
            data[6] = (byte) 0xff;
        } else if (data[6] > 0) {
            data[6] = (byte) ((17.5f + (0.5f * data[4])) * 2);
        }

        if (data[5] >= 30) {
            data[5] = (byte) 0xff;
        } else if (data[5] > 0) {
            data[5] = (byte) ((17.5f + (0.5f * data[5])) * 2);
        }

        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0x48) | ((data[2] & 0x10) << 1) | ((data[2] & 0x02) >> 1));

        airData[1] = 0;
        switch (data[3]) {
            case 1:
                airData[1] = (byte) 0x40;
                break;
            case 2:
                airData[1] = (byte) 0x60;
                break;
            case 3:
                airData[1] = (byte) 0x20;
                break;
            case 4:
                airData[1] = (byte) 0xa0;
                break;
            case 5:
                airData[1] = (byte) 0x80;
                break;
        }

        airData[1] |= data[4] & 0xf;

        airData[2] = data[5];
        airData[3] = data[6];

        boolean airControl = false;
        if (airData[0] != (byte) (mAirData[0] & 0xff) || airData[1] != (byte) (mAirData[1] & 0xff) || airData[2] != (byte) (mAirData[2] & 0xff) || airData[3] != (byte) (mAirData[3] & 0xff) || airData[4] != (byte) (mAirData[4] & 0xff)) {
            airControl = true;

            mAirData = airData;

        }

        if (airControl) {
            Handler handler = getHandler("CanService");
            if (null != handler) {
                handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
            }
        }

        if (mTemp != data[7]) {
            mTemp = data[7];
            int temp = 0;
            String s = "";

            if ((data[7] & 0xff) == 0xfe) {
                s = "";
            } else if ((data[7] & 0xff) == 0xff) {
                s = "--" + mContext.getResources().getString(R.string.temp_unic_centigrade);

            } else {

                temp = data[7] - 40;
                s = temp + mContext.getResources().getString(R.string.temp_unic_centigrade);

            }

            GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
        }

    }


    private byte getRadarData2(byte i) {
        byte data = 0;
        switch (i) {
            case 0:
                data = 0;
                break;
            case 1:
                data = 2;
                break;
            case 2:
                data = 5;
                break;
            case 3:
                data = 8;
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
            case 0x21: {
                parseWheelKey(data);
                break;
            }
            case 0x11: {
                parseACInfo(data, len);
            }
            break;

            case 0x22: // Radar back
            {

                mRadar[0] = getRadarData2(data[2]);
                mRadar[1] = getRadarData2(data[3]);
                mRadar[2] = getRadarData2(data[4]);
                mRadar[3] = getRadarData2(data[5]);

                boolean zero = Util.isZero(mRadar);
                if (!zero) {
                    RadarManager.start(mContext);
                    checkHideRadar();
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                }
            }
            break;
            case 0x23: // Radar back
            {

                mRadar[4] = getRadarData2(data[2]);
                mRadar[5] = getRadarData2(data[3]);
                mRadar[6] = getRadarData2(data[4]);
                mRadar[7] = getRadarData2(data[5]);

                boolean zero = Util.isZero(mRadar);
                if (!zero) {
                    RadarManager.start(mContext);
                    checkHideRadar();
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                }
            }
            break;

            case 0x30: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    short a = (short) ((data[3] & 0xff) | ((data[2] & 0x7f) << 8));// bu
                    // ma


                    int angle = (((a * 3000) / 12000));
                    if ((data[2] & 0x80) != 0) {
                        angle = -angle;
                    }

                    if (angle > -50 && angle < 0) {
                        angle = -50;
                    } else if (angle > 0 && angle < 50) {
                        angle = 50;
                    }
                    Log.e("1", angle + ":" + a);
                    // Log.e("2", ""+(data[3] & 0xff));
                    // Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
                }
            }
            break;
            case 0x28: {
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
        }
    }

    private int mDoorStatus = 0;

    public void setVolume(int volume) {

        // byte[] data = new byte[] { (byte) 0x84, 0x2, 0x2, (byte) volume };
        // sendDataToCanbox(data, data.length);
    }

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

    private final static int HIDE_RADAR = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_RADAR) {
                RadarManager.stop();
            }
            super.handleMessage(msg);
        }
    };

    public void setContext(Context c) {
        super.setContext(c);
        udpateLang();
    }

}
