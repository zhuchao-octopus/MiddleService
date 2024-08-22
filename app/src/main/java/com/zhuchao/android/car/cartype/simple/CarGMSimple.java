package com.zhuchao.android.car.cartype.simple;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.utils.AppConfig;
import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;

import java.nio.charset.StandardCharsets;
import java.util.Locale;


public class CarGMSimple extends Canbox {

    public CarGMSimple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });

        updateCanboxKeySettings();

        buildCmdEQ((byte) 0x2f, (byte) 0x0, 6);
    }

    private byte[][] mKeyPannel;
    private final static byte[][] KEYS_PANNEL_GL8 = {
            {0x1, KEY_PREVIOUSSONG}, {0x2, KEY_NUM_5}, {0x3, KEY_NUM_4}, {0x4, KEY_NUM_3}, {0x5, KEY_POWER}, {0x6, KEY_GPS}, {0x7, KEY_FM}, {0x8, KEY_NUM_1}, {0x9, KEY_NUM_2}, {0xa, KEY_NEXTSONG},
            {0xb, KEY_MENU}, {0xc, KEY_BACK}, {0xd, KEY_EQ}, {0xe, KEY_EJECT}, {0x11, KEY_SET}, {0x12, KEY_MEDIA}, {0x13, KEY_PLAYPAUSE}, {0x14, KEY_FM}, {0x15, KEY_MUTE}, {0x16, KEY_NUM_6},
            {0x17, AK_KEYPAD_VOLUME_A}, {0x18, AK_KEYPAD_VOLUME_D}, {0x19, KEY_SEEK_NEXT}, {0x1a, KEY_SEEK_PREV}, {0x34, MyCmd.Keycode.KEY_TURN_A}, {0x35, MyCmd.Keycode.KEY_TURN_D},
    };

    private final static byte[][] KEYS_PANNEL_ASTRA_J = {

            {0x17, AK_KEYPAD_VOLUME_A}, {0x18, AK_KEYPAD_VOLUME_D}, {0x9, KEY_PREVIOUSSONG}, {0x6, KEY_NEXTSONG}, {0xb, KEY_POWER}, {0xc, KEY_NUM_1}, {0xd, KEY_NUM_2}, {0xe, KEY_NUM_3},
            {0xf, KEY_NUM_4}, {0x1e, KEY_NUM_5}, {0x1f, KEY_NUM_6}, {0x1, KEY_HOME}, {0x16, KEY_MODE}, {0x1b, KEY_PLAYPAUSE}, {0x12, KEY_BT}, {0x50, KEY_EQ}, {0x19, MyCmd.Keycode.KEY_TURN_A},
            {0x1a, MyCmd.Keycode.KEY_TURN_D}, {0x3, KEY_MENU}, {0x14, KEY_SET}, {0x4, KEY_GPS}, {0x5, KEY_FM}, {0xa, MyCmd.Keycode.AS}, {0x11, KEY_EJECT},
    };

    private final static byte[][] KEYS_PANNEL_ENVISION_L = {
            {0x1, KEY_NUM_2}, {0x2, KEY_MUTE}, {0x3, KEY_BACK}, {0x4, KEY_GPS}, {0x5, KEY_FM}, {0x6, KEY_PREVIOUSSONG}, {0x7, KEY_POWER}, {0x8, KEY_MENU}, {0x9, KEY_GPS}, {0xa, KEY_NUM_3},
            {0xb, KEY_NUM_4}, {0xc, KEY_NUM_5}, {0xd, KEY_NUM_6}, {0x10, MyCmd.Keycode.AS}, {0x11, KEY_EJECT}, {0x12, KEY_SET}, {0x13, MyCmd.Keycode.TIME_SETTING}, {0x14, KEY_EQ}, {0x15, KEY_NUM_1},
            {0x16, KEY_NEXTSONG}, {0x17, AK_KEYPAD_VOLUME_A}, {0x18, AK_KEYPAD_VOLUME_D}, {0x19, KEY_SEEK_NEXT}, {0x1a, KEY_SEEK_PREV}, {0x1b, KEY_PLAYPAUSE}, {0x40, KEY_AUX}, {0x36, KEY_DVD},
    };

    private final static byte[][] KEYS_PANNEL_NORMAL = {
            {0x1, KEY_POWER}, {0x2, KEY_PREVIOUSSONG}, {0x3, KEY_NEXTSONG}, {0x4, KEY_SET}, {0x5, KEY_EQ}, {0x6, KEY_BACK}, {0x7, KEY_FM}, {0x8, KEY_DVD}, {0x9, KEY_MUTE}, {0xa, KEY_NUM_1},
            {0xb, KEY_NUM_2}, {0xc, KEY_NUM_3}, {0xd, KEY_NUM_4}, {0xe, KEY_NUM_5}, {0xf, KEY_NUM_6}, {0x10, KEY_GPS}, {0x11, KEY_EJECT}, {0x12, KEY_GPS}, {0x13, MyCmd.Keycode.TIME_SETTING},
            {0x14, KEY_FM}, {0x15, MyCmd.Keycode.AS}, {0x16, KEY_MENU}, {0x17, AK_KEYPAD_VOLUME_A}, {0x18, AK_KEYPAD_VOLUME_D}, {0x19, KEY_SEEK_NEXT}, {0x1a, KEY_SEEK_PREV}, {0x1b, KEY_PLAYPAUSE},
            {0x1c, KEY_PREVIOUSSONG}, {0x1d, KEY_NEXTSONG}, {0x40, KEY_AUX}, {0x34, MyCmd.Keycode.KEY_TURN_A}, {0x35, MyCmd.Keycode.KEY_TURN_D}, {0x50, KEY_HOME}, {0x51, KEY_SOURCE},
            {0x52, KEY_PLAYPAUSE}, {0x53, KEY_MENU}, {0x54, KEY_MEDIA}, {0x55, KEY_GPS}, {0x56, KEY_GPS}, {0x57, KEY_GPS}, {0x58, KEY_SEEK_PREV}, {0x59, KEY_SEEK_NEXT}, {0x5a, KEY_PREVIOUSSONG},
            {0x5b, KEY_NEXTSONG},
    };

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_PREVIOUSSONG}, {0x4, KEY_NEXTSONG}, {0x5, KEY_SOURCE}, {0x6, MyCmd.Keycode.MULT_SPEECH_AND_BT},
            {0x7, MyCmd.Keycode.MULT_MUTE_AND_HANG}, {0x8, KEY_BT}, {0x9, KEY_MUTE},


            {(byte) 0x81, MyCmd.Keycode.BT_HANG}, {(byte) 0x82, MyCmd.Keycode.BT_DIAL}, {(byte) 0x83, MyCmd.Keycode.BT_DIAL},

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
            doKey(key, data[3]);
        } else {
            if (data[3] == 0) {
                doKey(0, 0);
            }
        }
    }

    private void parsePannelKey(byte[] data) {
        if (doKeyStudy(1, data[2], data[3])) {
            return;
        }
        byte key = 0;
        if (mKeyPannel == null) {
            return;
        }
        for (int i = 0; i < mKeyPannel.length; ++i) {
            if (mKeyPannel[i][0] == data[2]) {
                key = mKeyPannel[i][1];
                break;
            }
        }

        if (key != 0) {
            doKey(key, data[3]);
        }

    }

    private final byte[] mAirData = new byte[8];

    private void parseACInfo(byte[] data, int len) {

        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xe0) | ((data[2] & 0x10) >> 4));

        airData[1] = (byte) (((data[3] & 0x10) >> 1) | (data[2] & 0x7));

        if (((data[3] & 0xf) == 1)) {
            airData[0] |= 0x8;
        } else if (((data[3] & 0xf) == 2)) {
            airData[0] |= 0x2;
        }

        if (((data[3] & 0xf) == 6) || ((data[3] & 0xf) == 7) || ((data[3] & 0xf) == 8) || ((data[3] & 0xf) == 9)) {
            airData[1] |= 0x80;
        }

        if (((data[3] & 0xf) == 4) || ((data[3] & 0xf) == 5) || ((data[3] & 0xf) == 6) || ((data[3] & 0xf) == 9)) {
            airData[1] |= 0x40;
        }

        if (((data[3] & 0xf) == 3) || ((data[3] & 0xf) == 4) || ((data[3] & 0xf) == 8) || ((data[3] & 0xf) == 9)) {
            airData[1] |= 0x20;
        }

        if (data[4] == 0x1e) {
            airData[2] = (byte) 0xff;
        } else if (data[4] == 0x1d) {
            airData[2] = 32;
        } else if (data[4] == 0x1f) {
            airData[2] = 33;
        } else if (data[4] == 0x20) {
            airData[2] = 30;
        } else if (data[4] == 0x21) {
            airData[2] = 31;
        } else if (data[4] == 0x22) {
            airData[2] = 62;
        } else if (data[4] == 0x0 || data[4] == 0xff) {
            airData[2] = data[4];
        } else if (data[4] >= 0x1 && data[4] <= 0x1c) {
            airData[2] = (byte) (2 * (((data[4] & 0xff) * 0.5) + 16.5));
        } else {
        }

        if (data[5] == 0x1e) {
            airData[3] = (byte) 0xff;
        } else if (data[5] == 0x1d) {
            airData[3] = 32;
        } else if (data[5] == 0x1f) {
            airData[3] = 33;
        } else if (data[5] == 0x20) {
            airData[3] = 30;
        } else if (data[5] == 0x21) {
            airData[3] = 31;
        } else if (data[5] == 0x22) {
            airData[3] = 62;
        } else if (data[5] == 0x0 || data[5] == 0xff) {
            airData[3] = data[4];
        } else if (data[5] >= 0x1 && data[5] <= 0x1c) {
            airData[3] = (byte) (2 * (((data[5] & 0xff) * 0.5) + 16.5));
        } else {
        }

        airData[4] = (byte) (data[6] & 0xff);
        airData[4] |= (byte) (((data[2] & 0x8) << 4));

        airData[7] = (byte) (((data[3] & 0x40) >> 6));

        Handler handler = getHandler("CanService");
        if (null != handler) {
            int i = 0;
            for (i = 0; i < mAirData.length; ++i) {
                if (mAirData[i] != airData[i]) {
                    break;
                }
            }
            if (i < mAirData.length) {

                Util.byteArrayCopy(mAirData, airData, 0, 0, mAirData.length);
                handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
            }
            // Log.d("allen", ""+data[7]);
            if (data[7] >= -40 && data[7] <= 87) {
                // handler.sendMessage(handler.obtainMessage(CANBOX_OUT_DOOR_TEMP,
                // data[7], 0));
                updateOutDoorTemp(data[7]);


            }

        }
    }


    private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;

    public void updateOutDoorTemp(int temp) {

        if (temp == CarUtil.INVALID_OUT_DOOR_TEMP) {
            if (mTempOutDoor != CarUtil.INVALID_OUT_DOOR_TEMP) {
                temp = mTempOutDoor;
            } else {
                return;
            }
        }
        mTempOutDoor = temp;


        String s = "";
        if (CarUtil.mTempUnit == 2) {
            temp = (int) ((temp) * 1.8f + 32);
            s = temp + mContext.getResources().getString(R.string.temp_unic_fahrenheit);
        } else {//if (CarUtil.mTempUnit == 1) {
            s = temp + mContext.getResources().getString(R.string.temp_unic_centigrade);
        }

        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);

    }

    private final static byte[] RADAR_CHANGE = new byte[]{1, 5, 7, 9, 11, 14};

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x1: {
                parseWheelKey(data);
                break;
            }
            case 0x02: {
                parsePannelKey(data);

            }
            break;
            case 0x3: {
                parseACInfo(data, len);
            }
            break;
            case 0x22: // Radar back
            {
                byteArrayCopy(mRadar, data, 0, 2, 4);
                for (int i = 0; i < 4; ++i) {
                    mRadar[i] *= 2;
                }
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
            case 0x23: // Radar front
            {
                byteArrayCopy(mRadar, data, 4, 2, 4);
                for (int i = 4; i < 8; ++i) {
                    mRadar[i] *= 2;
                }
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
            // case 0x7: // Radar status
            // {
            //
            // Handler handler = getHandler(RadarManager.TAG);
            // if (null != handler) {
            // if ((data[2] & 0x1) == 0) {
            // handler.sendMessage(handler.obtainMessage(
            // CANBOX_RADAR_BACK, 1, 0));
            // for (int i = 0; i < 8; ++i) {
            // mRadar[i] = 0;
            // }
            // }
            //
            // }
            // mHandler.removeMessages(HIDE_RADAR);
            // if ((data[2] & 0x1) != 0) {
            // mRadarSwitch = 1;
            // RadarManager.start(mContext);
            // } else {
            // mRadarSwitch = 0;
            // RadarManager.stop();
            // }
            //
            // sendCanboxInfo("com.canboxsetting", data);
            // }
            // break;
            // case 0x23: // Radar front
            // {
            // byteArrayCopy(mRadar, data, 4, 2, 4);
            // RadarManager.start(mContext);
            // Handler handler = getHandler(RadarManager.TAG);
            // if (null != handler) {
            // handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
            // }
            // }
            // break;
            // case 0x25: // Radar status
            // {
            // byte[] status = new byte[2];
            // status[0] = data[2];
            // status[1] = data[3];
            //
            // Handler handler = getHandler("Reverse");
            // if (null != handler) {
            // handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_STATUS,
            // status));
            // }
            // }
            // break;
            case 0x26: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    // Log.e("1", ""+(data[2] & 0xff));
                    // Log.e("2", ""+(data[3] & 0xff));
                    // Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
                    int angle = ((data[3] << 8) | (data[2] & 0xff)) / 100;
                    //				Log.d("aa", "angle+" + angle);
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 0));
                }
            }
            break;
            case 0x24: {
                int door = (data[2] & 0xf8);
                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((data[3] & 0x80) >> 2));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }

            }
            break;
            case 0x5:
            case 0x6:
            case 0x7:
            case 0xa:
            case 0xb:
            case 0xd:
            case 0x31:
            case 0x32:
            case 0x33:
            case 0x1a:
                sendCanboxInfo("com.canboxsetting", data);
                break;
            case 0x8:
                Util.byteArrayCopy(data0x8, data, 0, 0, data0x8.length);
                sendCanboxInfo("com.canboxsetting", data);
                break;
            case 0x9:
                int delay = 0;
                if (data[2] != data0x9[2]) {
                    if (data[2] == 0x1 || data[2] == 0x2 || data[2] == 0x3 || data[2] == 0x4) {
                        String top = AppConfig.getTopActivity();

                        Intent it = new Intent(Intent.ACTION_VIEW);
                        boolean topIsCamera = top != null && top.contains("com.canboxsetting.AnStartActivity");

                        if (!topIsCamera) {
                            try {
                                it.setClassName("com.canboxsetting", "com.canboxsetting.AnStartActivity");
                                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

                                mContext.startActivity(it);
                                delay = 800;
                            } catch (Exception e) {
                                //							 Log.e(TAG, e.getMessage())
                            }
                        }
                    }
                }
                Util.byteArrayCopy(data0x9, data, 0, 0, data0x9.length);
                //			sendCanboxInfo("com.canboxsetting", data);
                if (delay == 0) {
                    sendCanboxInfo("com.canboxsetting", data);
                } else {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(DELAY_SEND_ANSTART, data), delay);
                }
                break;
            default:
                super.parseCanboxData(data, len);
        }
    }

    private final byte[] data0x8 = new byte[12];
    private final byte[] data0x9 = new byte[3];
    private final byte mRadarSwitch = 0;

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        // if (mRadarSwitch != 1) {
        // int i;
        // for (i = 0; i < mRadar.length; ++i) {
        // if(mRadar[i]!=0){
        // break;
        // }
        // }
        // if (i >= mRadar.length) {
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 5000);
        // }

        // }
    }

    private final static int HIDE_RADAR = 0;
    private final static int DELAY_SEND_ANSTART = 1;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_RADAR: {
                    RadarManager.stop();
                    break;
                }
                case DELAY_SEND_ANSTART:
                    sendCanboxInfo("com.canboxsetting", (byte[]) msg.obj);
                    break;

            }
        }
    };

    private int mDoorStatus = 0;

    public void setReverseRadaVol(byte param) {
        byte[] data = new byte[]{(byte) 0xc6, 0x2, 0x0, param};
        sendDataToCanbox(data, data.length);
    }

    public void setParkCarMode(byte param) {
        byte[] data = new byte[]{(byte) 0xc6, 0x2, 0x2, param};
        sendDataToCanbox(data, data.length);
    }

    public void requestInfo(byte param) {
        byte[] data = new byte[]{(byte) 0x90, 0x2, param, 0};
        sendDataToCanbox(data, data.length);
    }

    // public void setMediaMoreInfo(int source, int play, int total, int time,
    // int total_time) {
    // // byte min = (byte) ((time / 60) % 60);
    // // byte sec = (byte) ((time) % 60);
    // // ++play;
    // // byte[] data = new byte[] { (byte) 0xa3, 0x1, (byte) (total & 0xFF),
    // // (byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF),
    // // (byte) ((play >> 8) & 0xFF), min, sec };
    // // sendDataToCanbox(data, data.length);
    // }
    //
    // public void setMediaSrc(int source, byte type, byte[] b) {
    // if (b[0] == 0x3) {
    // b[0] = 5;
    // } else {
    // b[0] = 1;
    // }
    // byte[] data = new byte[] { (byte) 0x9a, 0x5, 8, b[0], b[1], b[2], 0 };
    // sendDataToCanbox(data, data.length);
    // }
    //
    // public void setMediaSrc(int source) {// default is simple box
    // switch (source) {
    // case MyCmd.SOURCE_DVD:
    // mSource = 0x2;
    // break;
    // case MyCmd.SOURCE_RADIO:
    // mSource = 0x1;
    // break;
    // case MyCmd.SOURCE_AUX:
    // mSource = 0x4;
    // break;
    // case MyCmd.SOURCE_BT:
    // mSource = 0x7;
    // break;
    // default:
    // mSource = 0x6;
    // break;
    // }
    //
    // byte[] data = new byte[] { (byte) 0x99, 0x2, mSource, mVolume };
    // sendDataToCanbox(data, data.length);
    // }


    private byte[] mData = new byte[]{
            (byte) 0xc0, 0x8, 0, 0, 0, 0, 0, 0, 0, 0
    };

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte h = (byte) ((time / 3600));
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        // ++play;

        byte s = 0;
        byte s2 = 0;
        switch (source) {
            case MyCmd.SOURCE_DVD:
                s = 0x2;
                s2 = 0x10;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                ++play;
                s = 8;
                s2 = 0x13;
                break;
            case MyCmd.SOURCE_BT:
                s = 0xb;
                s2 = 0x10;
                break;
            default:
                s = 0x07;
                s2 = 0x30;
                break;
        }

        if (MyCmd.SOURCE_DVD == source) {
            mData = new byte[]{
                    (byte) 0xc0, 0x8, s, s2, 0, (byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec
            };

        } else {
            mData = new byte[]{
                    (byte) 0xc0, 0x8, s, s2, (byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), 0, h, min, sec
            };
        }


        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] >= 0x10) {
            b[0] = 0x10;
        } else {
            b[0] = 0;
        }
        mData = new byte[]{
                (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0, 0, 0
        };
        sendDataToCanbox(mData, mData.length);


        byte[] data = new byte[]{(byte) 0xc2, 0x4, b[0], b[1], b[2], 0};
        sendDataToCanbox(data, data.length);
    }

    private int mSource = 0;

    public void setMediaSrc(int source) {// default is simple box
        mSource = source;
        if (source == MyCmd.SOURCE_RADIO) {
            return;
        }
        byte s = 0;
        byte mediaType = 0;
        switch (source) {
            case 0:
                s = 1;
                mediaType = 1;
                break;
            case 1:
                s = 2;
                mediaType = 0x10;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 6;
                mediaType = 0x12;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x08;
                mediaType = 0x11;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                mediaType = 0x30;
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                mediaType = 0x30;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0b;
                mediaType = 0x30;
                break;
            case MyCmd.SOURCE_AV_OFF:
                s = 0x0;
                mediaType = 0x0;
                break;
            default:
                s = 0x0c;
                mediaType = 0x0;
                break;
        }

        mData = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0};


        sendDataToCanbox(mData, mData.length);

        if ((source != MyCmd.SOURCE_MUSIC) && (source != MyCmd.SOURCE_VIDEO)) {
            sendId3((byte) 2, "");
            sendId3((byte) 3, "");
            sendId3((byte) 4, "");
        }
    }

    public void setVolume(int volume) {
        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    public void updateCanboxKeySettings() {
        if (CarUtil.getKeyType() == 2) {
            mKeyPannel = KEYS_PANNEL_GL8;
        } else if (CarUtil.getKeyType() == 1) {
            mKeyPannel = KEYS_PANNEL_ENVISION_L;
        } else if (CarUtil.getKeyType() == 3) {
            mKeyPannel = KEYS_PANNEL_ASTRA_J;
        } else {
            mKeyPannel = KEYS_PANNEL_NORMAL;
        }
    }

    public void setContext(Context c) {
        super.setContext(c);

        udpateLang();
    }

    public void udpateLang() {
        int lang = -1;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("en")) {
                lang = 1;
            } else if (locale.equals("zh")) {
                lang = 0;
            } else if (locale.equals("de")) {
                lang = 2;
            } else if (locale.equals("it")) {
                lang = 3;
            } else if (locale.equals("fr")) {
                lang = 4;
            } else if (locale.equals("sv")) {
                lang = 5;
            } else if (locale.equals("es")) {
                lang = 6;
            } else if (locale.equals("nl")) {
                lang = 7;
            } else if (locale.equals("pt")) {
                lang = 8;
            } else if (locale.equals("nb")) {
                lang = 9;
            } else if (locale.equals("fi")) {
                lang = 0xa;
            } else if (locale.equals("da")) {
                lang = 0xb;
            } else if (locale.equals("pl")) {
                lang = 0xc;
            } else if (locale.equals("tr")) {
                lang = 0xd;
            } else if (locale.equals("ar")) {
                lang = 0xe;
            } else if (locale.equals("ru")) {
                lang = 0xf;
            }

        }
        if (lang != -1) {
            byte[] buf = {(byte) 0x87, 0x1, (byte) lang};
            sendDataToCanbox(buf, buf.length);
        }
    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (0x3f << 16) | (25 << 8) | 25;

            byte[] buf = new byte[]{(byte) 0x90, 0x2, (byte) 0x2f, 0};
            sendDataToCanbox(buf, buf.length);
        } else {
            byte[] buf = new byte[]{(byte) 0xce, 0x2, 0x0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    buf[2] = 4;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    buf[2] = 5;
                    break;
                case EQ_CMD_SET_LOW:
                    buf[2] = 6;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    buf[2] = 3;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    buf[2] = 2;
                    break;
                case EQ_CMD_SET_VOLUME:
                    buf[2] = 1;
                    break;
                default:
                    return 0;
            }

            sendDataToCanbox(buf, buf.length);
        }
        return ret;
    }

    public void parseEQ(int id, byte[] buf) {
        if (mEQData == null) {
            mEQData = new byte[6];
        }
        mEQData[0] = buf[6];
        mEQData[1] = buf[7];
        mEQData[2] = buf[8];
        mEQData[3] = buf[5];
        mEQData[4] = buf[4];
        if ((buf[2] & 0x1) == 0x1) {
            mEQData[5] = 0;
        } else {
            mEQData[5] = buf[3];
        }
        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
    }


    public void sendId3(byte index, String num) {
        sendId3Ex(index, num, 0);
    }

    public void sendId3Ex(byte index, String num, int code) {

        try {
            if (num == null) {
                num = "";
            }

            byte[] n;
            if (code == 1) {
                n = num.getBytes();
            } else {
                n = num.getBytes(StandardCharsets.UTF_8);
            }


            int num_len = n.length;

            int len = num_len + 3;
            if (len > 0x21) {
                len = 0x21;
            }
            byte[] data = new byte[len];

            data[0] = (byte) 0xcb;
            data[1] = (byte) (num_len + 1);
            data[2] = index;

            System.arraycopy(n, 0, data, 3, num_len);

            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("Accord2013Simple", "sendId3" + e);
        }
    }

    String mName = null;
    String mArtist = null;
    String mAlbum = null;
    //	public void setPhone(int status, String num) {
    //		sendId3((byte)0x1, num);
    //	}

    public void setSongName(String s) {
        sendId3((byte) 0x2, s);
        mName = s;
    }

    public void setSongAritst(String s) {
        sendId3((byte) 0x4, s);
        mArtist = s;
    }

    public void setSongAlbum(String s) {
        sendId3((byte) 0x3, s);
        mAlbum = s;
    }

    private int mPhoneStatus = HFP_INFO_INITIAL;

    public void setPhoneEx(int status, String num, String name) {
        switch (status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:
            case HFP_INFO_CONNECTING:
            case HFP_INFO_CONNECTED:
                if (mPhoneStatus > HFP_INFO_CONNECTED) {
                    if (mSource == MyCmd.SOURCE_MUSIC) {
                        sendId3((byte) 4, mArtist);
                        sendId3((byte) 3, mAlbum);
                    }
                }
                sendId3Ex((byte) 1, "", 1);
                break;
            case HFP_INFO_CALLED:
            case HFP_INFO_INCOMING:
            case HFP_INFO_CALLING:
                sendId3Ex((byte) 1, num, 1);
                sendId3((byte) 4, num);
                sendId3((byte) 3, name);
                break;
        }

        mPhoneStatus = status;


    }
}
