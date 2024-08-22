package com.zhuchao.android.car.cartype.raise;

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

import java.util.Locale;


public class GMRaise extends Canbox {

    public GMRaise() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });


        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);

        updateCanboxKeySettings();
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0xe2, 0x01, 0};
        switch (CarUtil.getModelId()) {
            case 0:
                cmd[2] = 1;
                break;
            case 4:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 8;
                } else {
                    cmd[2] = 9;
                }
                break;
            case 7:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 4;
                } else {
                    cmd[2] = 5;
                }
                break;
            case 8:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0xc;
                } else {
                    cmd[2] = 0xd;
                }
                break;
            case 9:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0xa;
                } else {
                    cmd[2] = 0xb;
                }
                break;
            case 12:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 2;
                } else {
                    cmd[2] = 3;
                }
                break;
            case 13:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x12;
                } else {
                    cmd[2] = 0x13;
                }
                break;
            case 41:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x10;
                } else {
                    cmd[2] = 0x11;
                }
                break;
            case 38:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x18;
                } else {
                    cmd[2] = 0x19;
                }
                break;
            case 36:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x44;
                } else {
                    return null;
                }
                break;
            case 51:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x45;
                } else {
                    return null;
                }
                break;
            case 53:
                cmd[2] = 0x48;
                break;
            case 54:
                cmd[2] = 0x49;
                break;
            case 18:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x16;
                } else {
                    cmd[2] = 0x17;
                }
                break;
            default:
                return null;
        }
        return cmd;
    }

    private byte[][] mKeyPannel;
    private final static byte[][] KEYS_PANNEL_GL8 = {
            {0x1, KEY_PREVIOUSSONG}, {0x2, KEY_NUM_5}, {0x3, KEY_NUM_4}, {0x4, KEY_NUM_3}, {0x5, KEY_POWER}, {0x6, KEY_GPS}, {0x7, KEY_FM}, {0x8, KEY_NUM_1}, {0x9, KEY_NUM_2}, {0xa, KEY_NEXTSONG},
            {0xb, KEY_MENU}, {0xc, KEY_BACK}, {0xd, KEY_EQ}, {0xe, KEY_EJECT}, {0x11, KEY_SET}, {0x12, KEY_MEDIA}, {0x13, KEY_PLAYPAUSE}, {0x14, KEY_FM}, {0x15, KEY_MUTE}, {0x16, KEY_NUM_6},
            {0x17, AK_KEYPAD_VOLUME_A}, {0x18, AK_KEYPAD_VOLUME_D}, {0x19, KEY_SEEK_NEXT}, {0x1a, KEY_SEEK_PREV}, {0x34, MyCmd.Keycode.KEY_TURN_A}, {0x35, MyCmd.Keycode.KEY_TURN_D},
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
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_PREVIOUSSONG}, {0x4, KEY_NEXTSONG}, {0x5, KEY_SOURCE}, {0x6, KEY_BT}, {0x7, KEY_MUTE}, {0x8, MyCmd.Keycode.KEY_TURN_A},
            {0x9, MyCmd.Keycode.KEY_TURN_D}, {0xa, MyCmd.Keycode.HOME},

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

    private final byte[] mAirData = new byte[9];

    private void parseACInfo2(byte[] data) {
        mAirData[8] = (byte) ((data[2] & 0xf0) | ((data[2] & 0xf) << 2));
        super.parseACInfo(mAirData);
    }

    private void parseACInfo(byte[] data, int len) {

        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xe0) | ((data[2] & 0x10) >> 4) | ((data[3] & 0x20) >> 3));

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
            airData[3] = data[5];
        } else if (data[5] >= 0x1 && data[5] <= 0x1c) {
            airData[3] = (byte) (2 * (((data[5] & 0xff) * 0.5) + 16.5));
        } else {
        }

        airData[4] = (byte) (data[6] & 0xff);
        airData[4] |= (byte) (((data[2] & 0x8) << 4));

        airData[7] = (byte) (((data[3] & 0x40) >> 6));

        Util.byteArrayCopy(mAirData, airData, 0, 0, airData.length);
        super.parseACInfo(mAirData);
        if (data[7] >= -40 && data[7] <= 87) {
            // handler.sendMessage(handler.obtainMessage(CANBOX_OUT_DOOR_TEMP,
            // data[7], 0));
            updateOutDoorTemp(data[7]);

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
        } else {// if (CarUtil.mTempUnit == 1) {
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
            case 0x13:
                parseACInfo2(data);
                break;
            case 0x49:
                do360CameraSwitch(data[2]);
                break;
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
                    // Log.d("aa", "angle+" + angle);
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
            case 0x41:
            case 0x42:
            case 0x4a:
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

    private byte[] mEq = null;

    public void sendEqToCanbox(byte[] eq) {
        if (eq != null && eq.length >= 13) {
            if (mEq == null) {
                mEq = new byte[eq.length];
                Util.byteArrayCopy(mEq, eq, 0, 0, eq.length);
                return;
            }

            byte type = 0;
            if (eq[0] != mEq[0]) {
                type = 2;
            } else if (eq[1] != mEq[1]) {
                type = 1;
            } else if (eq[2] != mEq[2] || eq[3] != mEq[3] || eq[4] != mEq[4] || eq[5] != mEq[5]) {
                type = 3;
            } else {
                type = 4;
            }

            byte[] buf = new byte[10];
            int i;
            buf[0] = (byte) 0x98;
            buf[1] = 0x8;
            buf[2] = type;

            i = (eq[0] * 180 / 14);
            if (i % 10 > 0) {
                i /= 10;
                ++i;
            } else {
                i /= 10;
            }

            buf[4] = (byte) i;
            i = (eq[1] * 180 / 14);

            if (i % 10 > 0) {
                i /= 10;
                ++i;
            } else {
                i /= 10;
            }

            buf[3] = (byte) i;

            buf[5] = (byte) ((eq[2] + eq[3] + eq[4] + eq[5]) / 4);
            buf[6] = (byte) ((eq[6] + eq[7] + eq[8] + eq[9] + eq[10]) / 5);
            buf[7] = eq[11];

            switch (eq[12]) {
                case 4:
                    buf[9] = 1;
                    break;
                case 3:
                    buf[9] = 5;
                    break;
                case 5:
                    buf[9] = 3;
                    break;
                case 2:
                    buf[9] = 2;
                    break;
                case 1:
                    buf[9] = 4;
                    break;
                default:
                    buf[9] = 0;
                    break;
            }

            sendDataToCanbox(buf, buf.length);

            Util.byteArrayCopy(mEq, eq, 0, 0, eq.length);
        }
    }

    private byte mVolume;
    private final byte mSource = 0x6;

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        // ++play;
        byte[] data;

        if (MyCmd.SOURCE_DVD != source) {
            data = new byte[]{
                    (byte) 0xc3, 0x6, (byte) (total & 0xFF), (byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF), (byte) ((play >> 8) & 0xFF), min, sec
            };
        } else {
            data = new byte[]{
                    (byte) 0xc3, 0x6, (byte) (1 & 0xFF), (byte) ((play) & 0xFF), (byte) (total & 0xFF), (byte) ((0) & 0xFF), min, sec
            };
        }
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        byte[] data = new byte[]{(byte) 0xc2, 0x4, b[0], b[1], b[2], 0};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source) {// default is simple box
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
                s = 0x09;
                mediaType = 0x11;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                mediaType = 0x30;
                byte[] data2 = new byte[]{(byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0};
                sendDataToCanbox(data2, data2.length);
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                mediaType = 0x30;
                byte[] data3 = new byte[]{(byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0};
                sendDataToCanbox(data3, data3.length);
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0b;
                mediaType = 0x30;
                break;
            default:
                s = 0x00;
                mediaType = 0x0;
                break;
        }
        byte[] data;
        if (s == 0xb || s == 0x7) {
            data = new byte[]{
                    (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0
            };
        } else {
            data = new byte[]{(byte) 0xc0, 0x2, s, mediaType};
        }

        sendDataToCanbox(data, data.length);
    }


    public void updateCanboxKeySettings() {
        if (CarUtil.getCarType2() == 3) {
            mKeyPannel = KEYS_PANNEL_GL8;
        } else if (CarUtil.getCarType2() == 1) {
            mKeyPannel = KEYS_PANNEL_NORMAL;
        } else if (CarUtil.getCarType2() == 2) {
            mKeyPannel = KEYS_PANNEL_ENVISION_L;
        } else {
            switch (CarUtil.getModelId()) {
                case 3:
                    mKeyPannel = KEYS_PANNEL_ENVISION_L;
                    break;
                case 9:
                case 32:
                case 54:
                    mKeyPannel = KEYS_PANNEL_GL8;
                    break;
                default:
                    mKeyPannel = KEYS_PANNEL_NORMAL;
                    break;
            }
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

    private final boolean mIsOpenCamera = false;

    private void do360CameraSwitch(int s) {

        String top = AppConfig.getTopActivity();

        Intent it = new Intent(Intent.ACTION_VIEW);
        boolean topIsCamera = top != null && top.contains("com.android.car.frontcamera.FrontCameraActivity");
        if (s == 0) {
            if (topIsCamera) {
                it.putExtra("finish", 1);
            }
        } else {
            if (!topIsCamera) {
                topIsCamera = true;
            }
        }

        if (topIsCamera) {
            try {
                it.setClassName("com.car.ui", "com.android.car.frontcamera.FrontCameraActivity");
                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

                mContext.startActivity(it);
            } catch (Exception e) {
                // Log.e(TAG, e.getMessage());
            }
        }
    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        if ((data[0] & 0xff) == 0x90) {
            if (data[2] == 0x8) {
                sendCanboxInfo("com.canboxsetting", data0x8);
            } else if (data[2] == 0x9) {
                sendCanboxInfo("com.canboxsetting", data0x9);
            }
        }

        super.sendDataToCanbox(data, len);
    }
}
