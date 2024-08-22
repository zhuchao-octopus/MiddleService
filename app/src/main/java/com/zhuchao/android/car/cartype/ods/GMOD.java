package com.zhuchao.android.car.cartype.ods;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.common.utils.AppConfig;
import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;


public class GMOD extends Canbox {

    public GMOD() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x4, KEY_NEXTSONG}, {0x3, KEY_PREVIOUSSONG}, {0x5, KEY_SOURCE}, {0x6, MyCmd.Keycode.MULT_SPEECH_AND_BT},
            {0x7, MyCmd.Keycode.MULT_MUTE_AND_HANG},

    };

    @Override
    public void setContext(Context c) {
        // TODO Auto-generated method stub
        super.setContext(c);
        byte[] data = new byte[]{(byte) 0x91, 0x1, 0};
        if (CarUtil.getCarType() == 1) {
            data[2] = 1;
        }
        sendDataToCanbox(data, data.length);
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

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x3, KEY_NEXTSONG}, {0x2, KEY_PREVIOUSSONG}, {0x4, MyCmd.Keycode.SETUP}, {0x5, MyCmd.Keycode.EQ}, {0x6, MyCmd.Keycode.BACK}, {0x7, MyCmd.Keycode.RADIO},
            {0x8, MyCmd.Keycode.DVD}, {0x9, MyCmd.Keycode.MUTE}, {0xa, MyCmd.Keycode.NUMBER1}, {0xb, MyCmd.Keycode.NUMBER2}, {0xc, MyCmd.Keycode.NUMBER3}, {0xd, MyCmd.Keycode.NUMBER4},
            {0xe, MyCmd.Keycode.NUMBER5}, {0xf, MyCmd.Keycode.NUMBER6}, {0x10, MyCmd.Keycode.NAVIGATION}, {0x11, MyCmd.Keycode.EJECT}, {0x12, MyCmd.Keycode.SETUP}, {0x13, MyCmd.Keycode.TIME_SETTING},
            {0x14, MyCmd.Keycode.NAVIGATION}, {0x15, MyCmd.Keycode.AS}, {0x16, MyCmd.Keycode.PLAY_PAUSE}, {0x17, AK_KEYPAD_VOLUME_A}, {0x18, AK_KEYPAD_VOLUME_D}, {0x19, MyCmd.Keycode.KEY_SEEK_NEXT},
            {0x1a, MyCmd.Keycode.KEY_SEEK_PREV}, {0x1b, MyCmd.Keycode.PLAY_PAUSE}, {0x1c, MyCmd.Keycode.PREVIOUS}, {0x1d, MyCmd.Keycode.NEXT}, {0x40, MyCmd.Keycode.AUX_IN},
            {0x34, MyCmd.Keycode.KEY_TURN_A}, {0x35, MyCmd.Keycode.KEY_TURN_D}, {0x50, MyCmd.Keycode.HOME}, {0x51, MyCmd.Keycode.MODLE}, {0x52, MyCmd.Keycode.RDS_TA_SWITCH},
            {0x53, MyCmd.Keycode.MENU}, {0x54, MyCmd.Keycode.AUDIO},

    };

    private void parsePannelKey(byte[] data) {

        if (doKeyStudy(data[2], data[3])) {
            return;
        }

        byte key = 0;
        for (int i = 0; i < KEYS_WHEEL2.length; ++i) {
            if (KEYS_WHEEL2[i][0] == data[2]) {
                key = KEYS_WHEEL2[i][1];
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

    byte[] mAirData = new byte[8];

    private byte mTemp = 127;

    private byte getTemp(byte in) {
        if (in == 0x1e) {
            in = (byte) 0xff;
        } else if (in == 0x1d) {
            in = 32;
        } else if (in == 0x1f) {
            in = 33;
        } else if (in == 0x20) {
            in = 30;
        } else if (in == 0x21) {
            in = 31;
        } else if (in == 0x22) {
            in = 62;
        } else if (in == 0x23) {
            in = 63;
        } else if (in == 0x24) {
            in = 64;
        } else if (in == (byte) 0xff) {
            in = (byte) 0xfa;
        } else if (in > 0) {
            in = (byte) ((16.5f + (0.5f * in)) * 2);
        }
        return in;
    }

    private void parseACInfo(byte[] data, int len) {
        data[4] = getTemp(data[4]);
        data[5] = getTemp(data[5]);

        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xc0) | ((data[2] & 0x20) << 0) | ((data[2] & 0x10) >> 4));


        switch (data[3]) {
            case 1:
                airData[0] |= 0x08;
                break;
            case 2:
                airData[0] |= 0x02;
                break;
            case 3:
                airData[1] = (byte) 0x20;
                break;
            case 4:
                airData[1] = (byte) 0x60;
                break;
            case 5:
                airData[1] = (byte) 0x40;
                break;
            case 6:
                airData[1] = (byte) 0xc0;
                break;
            case 7:
                airData[1] = (byte) 0x80;
                break;
            case 8:
                airData[1] = (byte) 0xa0;
                break;
            case 9:
                airData[1] = (byte) 0xe0;
                break;
        }


        airData[1] |= (byte) (((data[2] & 0x07)));


        airData[2] = data[4];
        airData[3] = data[5];


        airData[4] = data[6];

        boolean airControl = false;
        if (airData[0] != (byte) (mAirData[0] & 0xff) || airData[1] != (byte) (mAirData[1] & 0xff) || airData[2] != (byte) (mAirData[2] & 0xff) || airData[3] != (byte) (mAirData[3] & 0xff) || airData[4] != (byte) (mAirData[4] & 0xff)) {
            airControl = true;

            mAirData = airData;

        }


        if (data[7] >= -40 && data[7] <= 87) {
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

        if (!isShowAir()) {
            byte[] send = new byte[airData.length + 2];
            send[0] = (byte) 0xff;
            send[1] = (byte) 0xff;
            Util.byteArrayCopy(send, airData, 2, 0, airData.length);
            sendCanboxInfo("com.canboxsetting", send);
            //	return;
        }

        if (airControl) {
            Handler handler = getHandler("CanService");
            if (null != handler) {
                handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
            }
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
                data = 3;
                break;
            case 3:
                data = 5;
                break;
            case 4:
                data = 7;
                break;
            case 5:
                data = 9;
                break;
            case 6:
                data = 11;
                break;
            case 7:
                data = 13;
                break;
        }
        return data;
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x1: {
                parseWheelKey(data);
            }

            break;

            case 0x2: {
                parsePannelKey(data);
            }

            break;
            case 0x3: {
                parseACInfo(data, len);

                sendCanboxInfo("com.canboxsetting", data);
            }
            break;
            case 0x22: // Radar back
            {

                mRadar[0] = getRadarData(data[2]);
                mRadar[1] = getRadarData(data[3]);
                mRadar[2] = getRadarData(data[4]);
                mRadar[3] = getRadarData(data[5]);

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
            case 0x23: {

                mRadar[4] = getRadarData(data[2]);
                mRadar[5] = getRadarData(data[3]);
                mRadar[6] = getRadarData(data[4]);
                mRadar[7] = getRadarData(data[5]);

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
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
            case 0x24: {
                int door = (data[2] & 0xfc);
                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((door & 0x4) << 3));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }

            }
            break;

            case 0x27: {

                int t = (short) ((data[3] & 0xff) | ((data[4] & 0xff) << 8));
                int temp = t;
                mUnit = data[2];
                updateOutDoorTemp(temp);

                // }
            }
            break;

            case 0x26: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {

                    short angle = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));
                    int angle2 = (angle * 300 / 7800);
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle2, 10));
                }
            }
            break;
            //		case 0x33:
            //			sendCanboxInfo("com.canboxsetting", data);
            //			break;

            case 0x66:
                Intent it = null;
                String top = AppConfig.getTopActivity();
                if (data[2] == 1) {
                    if (!"com.canboxsetting/com.canboxsetting.JeepCarCDPlayerActivity".equals(top)) {
                        it = new Intent(Intent.ACTION_VIEW);
                        it.setClassName("com.canboxsetting", "com.canboxsetting.JeepCarCDPlayerActivity");
                        it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);


                    }

                } else if (data[2] == 2) {
                    if ("com.canboxsetting/com.canboxsetting.JeepCarCDPlayerActivity".equals(top)) {
                        it = new Intent(Intent.ACTION_VIEW);
                        it.setClassName("com.canboxsetting", "com.canboxsetting.JeepCarCDPlayerActivity");
                        it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        it.putExtra("finish", 1);
                    }
                }

                if (it != null) {
                    try {
                        mContext.startActivity(it);
                    } catch (Exception e) {
                        // Log.e(TAG, e.getMessage());
                    }
                }
                break;
        }

        if (data[0] == 0x40 || data[0] == 0x41 || data[0] == 0x50 || data[0] == 0x63 || data[0] == 0x21 || data[0] == 0x25 || data[0] == 0x16) {
            sendCanboxInfo("com.canboxsetting", data);
        }
    }


    private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
    private int mUnit = 0;

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
            s = String.format("%d%s", temp / 10, mContext.getResources().getString(R.string.temp_unic_fahrenheit));
        }

        if (s.length() > 1) {
            GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
        }

    }

    private int mDoorStatus = 0;


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
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
}
