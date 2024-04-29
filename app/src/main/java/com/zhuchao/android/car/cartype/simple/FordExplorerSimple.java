package com.zhuchao.android.car.cartype.simple;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.ProtocolAk47;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.AutoParkingMsgManager;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.canbox.WarningMsgManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.OSProManager;

import java.util.Date;
import java.util.Locale;

public class FordExplorerSimple extends Canbox {

    public FordExplorerSimple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });

        // updateCanboxSettings();
    }

    public void setContext(Context c) {
        super.setContext(c);

        udpateLang();
        updateTime();

        byte[] buf = {(byte) 0xc6, 0x2, (byte) 0xbe, (byte) 0};
        if (CarUtil.getCarType2() == 2) {
            buf[3] = 1;
        }

        Util.doSleep(5);
        sendDataToCanbox(buf, buf.length);
    }

    public void udpateLang() {
        int lang = 0;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("en")) {
                lang = 2;
            } else if (locale.equals("zh")) {
                lang = 0x1b;
            } else if (locale.equals("ar")) {
                lang = 3;
            } else if (locale.equals("iw")) {
                lang = 4;
            } else if (locale.equals("ru")) {
                lang = 0xb;
            } else if (locale.equals("zh")) {
                lang = 6;
            } else if (locale.equals("zh")) {
                lang = 7;
            } else if (locale.equals("pt")) {
                lang = 0x16;
            } else if (locale.equals("tr")) {
                lang = 0xa;
            } else if (locale.equals("fr")) {
                lang = 6;
            } else if (locale.equals("de")) {
                lang = 4;
            } else if (locale.equals("it")) {
                lang = 5;
            } else if (locale.equals("es")) {
                lang = 8;
            } else if (locale.equals("nl")) {
                lang = 0xc;
            } else if (locale.equals("pt")) {
                lang = 15;
            } else if (locale.equals("sv")) {
                lang = 0x12;
            } else if (locale.equals("pl")) {
                lang = 0xe;
            }
        }

        byte[] buf = {(byte) 0xc6, 0x2, (byte) 0xa4, (byte) lang};
        sendDataToCanbox(buf, buf.length);
    }

    private final static byte[][] KEYS_WHEEL = {

            {0x20, KEY_NUM_0}, {0x21, KEY_NUM_1}, {0x22, KEY_NUM_2}, {0x23, KEY_NUM_3}, {0x24, KEY_NUM_4}, {0x25, KEY_NUM_5}, {0x26, KEY_NUM_6}, {0x27, KEY_NUM_7}, {0x28, KEY_NUM_8},
            {0x29, KEY_NUM_9}, {0x2a, KEY_NUM_X}, {0x2b, KEY_NUM_J},

            {0x33, KEY_FM}, {0x34, KEY_FM}, {0x35, KEY_DVD}, {0x36, KEY_AUX}, {0x37, KEY_HOME}, {0x38, KEY_EQ}, {0x39, KEY_BT}, {0x3d, MyCmd.Keycode.TIME_SETTING}, {0x3f, KEY_POWER},

            {0x48, KEY_PLAYPAUSE}, {0x4a, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x49, MyCmd.Keycode.KEY_SEEK_PREV}, {0x4b, KEY_PREVIOUSSONG}, {0x4c, KEY_NEXTSONG},

            {0x52, MyCmd.Keycode.MULT_PREV_AND_RECEIVE}, {0x53, MyCmd.Keycode.MULT_NEXT_AND_HANG},

            {0x54, KEY_EJECT}, {0x56, MyCmd.Keycode.RDS_TA_SWITCH}, {0x57, KEY_GPS}, {0x59, KEY_EQ}, {0x5a, KEY_MUTE}, {0x5b, MyCmd.Keycode.DARK},
            // { 0x5c, 0 },
            // { 0x5d, 0 },
            // { 0x5e, 0 },
            // { 0x5f, 0 },
            {0x62, MyCmd.Keycode.PLAY_PAUSE}, {0x68, MyCmd.Keycode.BACK}, {0x6A, MyCmd.Keycode.SETUP}, {0x6F, MyCmd.Keycode.AUDIO},

            {0x61, MyCmd.Keycode.KEY_TURN_D}, {0x60, MyCmd.Keycode.KEY_TURN_A},

            // { (byte) 0x86, KEY_PLAYPAUSE },
            {(byte) 0xF0, AK_KEYPAD_VOLUME_A}, {(byte) 0xF1, AK_KEYPAD_VOLUME_D},
            // { (byte) 0xF2, MyCmd.Keycode.KEY_TURN_A },
            // { (byte) 0xF3, MyCmd.Keycode.KEY_TURN_D },

    };

    private void parseWheelKey(byte[] data, int len) {
        if (doKeyStudy(data[2], data[3])) {
            return;
        }
        switch (data[2]) {
            case 0x0:
                doKey(0, 0);
                break;
            case 0x1:
                doKey(AK_KEYPAD_VOLUME_A, data[3]); // vol+
                break;
            case 0x2:
                doKey(AK_KEYPAD_VOLUME_D, data[3]);// vol-
                break;
            case 0x3:
                doKey(MyCmd.Keycode.MULT_NEXT_AND_HANG, data[3]);
                break;
            case 0x4:
                doKey(MyCmd.Keycode.MULT_PREV_AND_RECEIVE, data[3]);
                break;
            case 0x5:
                doKey(KEY_BT, data[3]);
                break;
            case 0x6:
                doKey(AK_KEYPAD_MUTE_FAKE, data[3]);// mute
                break;
            case 0x7:
                doKey(KEY_MODE, data[3]);
                break;
            case 0xe:
                doKey(KEY_PREVIOUSSONG, data[3]);
                break;
            case 0xf:
                doKey(KEY_NEXTSONG, data[3]);
                break;
            case 0x10:
                doKey(MyCmd.Keycode.KEY_TURN_D, data[3]);
                break;
            case 0x11:
                doKey(MyCmd.Keycode.KEY_TURN_A, data[3]);
                break;
            case 0x12:
                doKey(KEY_PLAYPAUSE, data[3]);
                break;
            default:
                byte key = 0;
                for (int i = 0; i < KEYS_WHEEL.length; ++i) {
                    if (KEYS_WHEEL[i][0] == data[2]) {
                        key = KEYS_WHEEL[i][1];
                        break;
                    }
                }

                if (key != 0) {
                    doKey(key, data[3]);
                    if (data[2] == 0x60 || data[2] == 0x61 || data[2] == (byte) 0xf1 || data[2] == (byte) 0xf0) {
                        Util.doSleep(1);
                        doKey(0, 0);
                    }
                } else {
                    if (data[3] == 0) {
                        doKey(0, 0);
                    }
                }
                break;
        }
    }

    byte[] mAirData = new byte[8];

    private void parseACInfo(byte[] data, int len) {
        byte[] airData = new byte[8];
        if ((data[6] & 0x8) != 0) {

            if ((data[4] & 0xff) == 0x3C) {
                data[4] = (byte) 0xff;
            } else if ((data[4] & 0xff) >= 0x1f && (data[4] & 0xff) <= 0x3B) {
                data[4] = (byte) ((15.5f + (0.5f * (data[4] - 0x1f))) * 2);
            } else if ((data[4] & 0xff) == 0x1e) {
                data[4] = 0;
            } else if ((data[4] & 0xff) == 0xAB) {
                data[4] = (byte) 0xff;
            } else if ((data[4] & 0xff) >= 0x78 && (data[4] & 0xff) <= 0xAA) {
                data[4] = (byte) (60 + (((data[4] & 0xff) - 0x78) / 2));
            } else if ((data[4] & 0xff) == 0x77) {
                data[4] = 0;
            }

            if ((data[8] & 0xff) == 0x3C) {
                data[8] = (byte) 0xff;
            } else if ((data[8] & 0xff) >= 0x1f && (data[8] & 0xff) <= 0x3B) {
                data[8] = (byte) ((15.5f + (0.5f * (data[8] - 0x1f))) * 2);
            } else if ((data[8] & 0xff) == 0x1e) {
                data[8] = 0;
            } else if ((data[8] & 0xff) == 0xAB) {
                data[8] = (byte) 0xff;
            } else if ((data[8] & 0xff) >= 0x78 && (data[8] & 0xff) <= 0xAA) {
                data[8] = (byte) (60 + (((data[8] & 0xff) - 0x78) / 2));
            } else if ((data[8] & 0xff) == 0x77) {
                data[8] = 0;
            }

        } else {
            if ((data[4] & 0xff) == 0x1e) {
                data[4] = (byte) 0xff;
            } else if ((data[4] & 0xff) == 0xf) {
                if (((data[6] & 0x40)) == 0) {
                    data[4] = 44;
                } else {
                    data[4] = 71;
                }
            } else if ((data[4] & 0xff) > 0) {
                data[4] = (byte) 0xfa;
            }
            data[8] = (byte) 0xfa;
        }
        // airData[5] = (byte) ((data[6] & 0x40) >> 6);
        // data[6] &= ~0x40;

        boolean outDoorTemp = false;
        boolean airControl = false;
        // if (airData[0] != (byte) (data[2] & 0xff)
        // || airData[1] != (byte) (data[3] & 0xff)
        // || airData[2] != (byte) (data[4] & 0xff)
        // || airData[3] != (byte) (data[5] & 0xff)
        // || airData[4] != (byte) (data[6] & 0xff)) {
        // airControl = true;

        airData[0] = (byte) (data[2] & 0xff);
        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[8] & 0xff);
        airData[4] = (byte) (data[6] & 0xcc);

        airData[4] |= (byte) ((data[9] & 0x3) << 4 | ((data[9] & 0xc) >> 2));
        // }

        if (!isBufEqual(mAirData, airData)) {
            airControl = true;
            Util.byteArrayCopy(mAirData, airData, 0, 0, airData.length);
        }

        // data[5] = (byte)((data[6] & 0x40) >> 6);
        airData[5] = (byte) ((data[6] & 0x40) >> 6);

        if (airData[6] != (byte) (data[7] & 0xff)) {
            outDoorTemp = true;
            airData[6] = (byte) (data[7] & 0xff);

        }

        // airData[4] = (byte) (data[6] & 0xff);

        Handler handler = getHandler("CanService");
        if (null != handler) {
            if (airControl) {
                int msg = CANBOX_HIDE_AIR;
                if (/* (data[2] & 0x80) != 0 && */((data[3] & 0x10) != 0)) {
                    msg = CANBOX_RETURN_AIR;
                }
                handler.sendMessage(handler.obtainMessage(msg, airData));
            }
        }
        if (outDoorTemp) {
            // handler.sendMessage(handler.obtainMessage(CANBOX_OUT_DOOR_TEMP,
            // airData[6], 0));
            int t = airData[6];
            String unit = mContext.getResources().getString(R.string.temp_unic_centigrade);
            if ((data[6] & 0x40) != 0) {
                unit = mContext.getResources().getString(R.string.temp_unic_fahrenheit);
                t = (t * 18 + 320) / 10;
            }
            GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, t + unit);

        }
    }

    private byte getRadarData(byte i) {
        byte data = 0;
        if (i > 0x0 && i <= 0x1) {
            data = 1;
        } else if (i >= 0x2 && i <= 0x3) {
            data = 2;
        } else if (i >= 0x4 && i <= 0x5) {
            data = 3;
        } else if (i >= 0x6 && i <= 0x7) {
            data = 4;
        } else if (i >= 0x8 && i <= 0x9) {
            data = 5;
        } else if (i >= 0xa && i <= 0xb) {
            data = 6;
        } else if (i >= 0xc && i <= 0xd) {
            data = 7;
        } else if (i >= 0xe && i <= 0xf) {
            data = 8;
        } else if (i >= 0x10 && i <= 0x11) {
            data = 9;
        } else if (i >= 0x12 && i <= 0x13) {
            data = 10;
        } else if (i >= 0x14 && i <= 0x15) {
            data = 11;
        } else if (i >= 0x16 && i <= 0x17) {
            data = 13;
        } else if (i >= 0x18 && i <= 0x19) {
            data = 14;
        } else if (i >= 0x1a && i <= 0x1b) {
            data = 15;
        } else if (i >= 0x1c && i <= 0x1f) {
            data = 16;
        }
        return data;
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data, len);
            }
            break;
            case 0x29: {
                sendCanboxInfo("com.canboxsetting", data);
                parseACInfo(data, len);
            }
            break;
            case 0x22: // Radar back
            {
                mRadar[0] = getRadarData(data[2]);
                mRadar[1] = getRadarData(data[3]);
                mRadar[2] = getRadarData(data[4]);
                mRadar[3] = getRadarData(data[5]);

                // byteArrayCopy(mRadar, data, 0, 2, 4);
                boolean zero = Util.isZero(mRadar);
                if (!zero) {
                    RadarManager.start(mContext);
                    checkHideRadarEx(5000);
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                }
            }
            break;
            case 0x23: // Radar front
            {
                // byteArrayCopy(mRadar, data, 4, 2, 4);

                mRadar[4] = getRadarData(data[2]);
                mRadar[5] = getRadarData(data[3]);
                mRadar[6] = getRadarData(data[4]);
                mRadar[7] = getRadarData(data[5]);

                mRadarFontEx[0] = getRadarData(data[7]);
                mRadarFontEx[1] = getRadarData(data[6]);
                boolean zero = Util.isZero(mRadar);
                if (!zero) {
                    RadarManager.start(mContext);
                    checkHideRadarEx(5000);
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                }
            }
            break;
            case 0x25: // Radar status
            {

                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    if ((data[2] & 0x8) == 0) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK, 1, 0));
                        for (int i = 0; i < 4; ++i) {
                            mRadar[i] = 0;
                        }
                    }
                    if ((data[2] & 0x4) == 0) {
                        for (int i = 0; i < 4; ++i) {
                            mRadar[4 + i] = 0;
                        }

                        mRadarFontEx[0] = 0;
                        mRadarFontEx[1] = 0;
                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT, 1, 0));
                    }

                }
                if ((data[2] & 0xc) != 0) {
                    boolean zero = Util.isZero(mRadar);
                    if (!zero) {
                        RadarManager.start(mContext);
                        checkHideRadarEx(5000);
                    }
                } else {
                    RadarManager.stop();
                }
            }
            break;
            case 0x26: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    // Log.e("1", ""+(data[2] & 0xff));
                    // Log.e("2", ""+(data[3] & 0xff));
                    // Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, (-((data[2] & 0xff) | ((data[3]) << 8))) / 10, 0));
                }
            }

            sendCanboxInfo("com.canboxsetting", data);
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
                door = (((door & 0x4) << 3) | ((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }
            }

            sendCanboxInfo("com.canboxsetting", data);
            break;
            case 0x49: {
                if (CarUtil.getCarType2() > 0) {
                    if ((data[2] & 0x01) != 0) {
                        if (OSProManager.mReverse != 1 && OSProManager.mSimulationReverse == 0) {
                            OSProManager.simulationReverse((byte) 1);
                        }
                        Handler handler = getHandler("Reverse");
                        if (null != handler) {
                            handler.sendMessage(handler.obtainMessage(CANBOX_MAZDA_RAISE_UI_DATA, (data[2] & 0xff) | ((data[3] & 0xff) << 8), 0));
                        }
                    } else {
                        if (OSProManager.mSimulationReverse == 1) {
                            OSProManager.simulationReverse((byte) 0);
                        }
                    }
                }
                break;
            }

            case 0x28: {
                updateParkingMsg(data);
            }
            break;
            case 0x2A: {
                updateWaring(data);
                return;
            }
            case 0x4a:
                if (CarUtil.getCarType2() == 2) {
                    if (OSProManager.mReverse != 1 && OSProManager.mSimulationReverse != 1) {
                        if ((data[2] & 0x80) != 0) {
                            do360CameraSwitch(data[2] & 0x10);
                        }
                    }
                }
                break;
            case 0x79: {
                if (!"com.canboxsetting/com.focussync.MainActivity".equals(AppConfig.getTopActivity())) {
                    if (data[2] >= 1 && data[2] <= 4) {
                        try {
                            Intent it = new Intent(Intent.ACTION_VIEW);
                            it.setClassName("com.canboxsetting", "com.focussync.MainActivity");
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            it.putExtra("value", 1);
                            mContext.startActivity(it);
                        } catch (Exception e) {
                            // Log.e(TAG, ""+e);
                        }
                    }
                } else {
                    sendCanboxInfo("com.canboxsetting", data);
                }

            }
            break;

            case 0x35: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu
                    // ma

                    int angle = -((a * 3000) / 540);

                    // if (angle > -500 && angle < 500) {
                    // angle = 500;
                    // }
                    // Log.e("1", ""+(data[2] & 0xff));
                    // Log.e("2", ""+(data[3] & 0xff));
                    // Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
                }
            }
            break;
            case 0x56:
                doSyncControl(data);
                break;
            default:
                sendCanboxInfo("com.canboxsetting", data);
                break;
        }
    }

    private int showWarningMsg = -1;

    public void updateCanboxSettings() {

        showWarningMsg = Settings.System.getInt(mContext.getContentResolver(), SystemConfig.SHOW_FOCUS_CAR_WARNING_MSG, 0);
        if (showWarningMsg != 0) {
            WarningMsgManager.stop();
        }
    }

    private void updateWaring(byte[] data) {
        if (showWarningMsg == -1) {
            updateCanboxSettings();
        }
        if (showWarningMsg != 0) {
            return;
        }

        int drawable = 0, drawable2 = 0;
        int string = 0;
        // data 0
        if ((data[2] & 0x2) != 0) {
            drawable = R.drawable.w6;
            string = R.string.warn0_1;
            drawable2 = R.drawable.w_red;
        } else if ((data[2] & 0x4) != 0) {
            drawable = R.drawable.w5;
            string = R.string.warn0_2;
            drawable2 = R.drawable.w_red;
        } else if ((data[2] & 0x8) != 0) {
            drawable = R.drawable.w5;
            string = R.string.warn0_3;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[2] & 0x10) != 0) {
            drawable = R.drawable.w9;
            string = R.string.warn0_4;
        } else if ((data[2] & 0x20) != 0) {
            drawable = R.drawable.w22;
            string = R.string.warn0_5;
        } else if ((data[2] & 0x40) != 0) {
            drawable = R.drawable.w23;
            string = R.string.warn0_6;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[2] & 0x80) != 0) {
            drawable = R.drawable.w17;
            string = R.string.warn0_7;
            drawable2 = R.drawable.w_yellow;
        }

        // data 1

        else if ((data[3] & 0x1) != 0) {
            drawable = R.drawable.w4;
            string = R.string.warn1_0;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[3] & 0x2) != 0) {
            drawable = R.drawable.w16;
            string = R.string.warn1_1;
            drawable2 = R.drawable.w_red;
        } else if ((data[3] & 0x4) != 0) {
            drawable = R.drawable.w1;
            string = R.string.warn1_2;
            drawable2 = R.drawable.w_red;
        } else if ((data[3] & 0x8) != 0) {
            drawable = R.drawable.w8;
            string = R.string.warn1_3;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[3] & 0x10) != 0) {
            drawable = R.drawable.w2;
            string = R.string.warn1_4;
        } else if ((data[3] & 0x20) != 0) {
            drawable = R.drawable.w2;
            string = R.string.warn1_5;
        } else if ((data[3] & 0x40) != 0) {
            drawable = R.drawable.w24;
            string = R.string.warn1_6;
            drawable2 = R.drawable.w_yellow;
        }

        // data 2

        else if ((data[4] & 0x1) != 0) {
            drawable = R.drawable.w13;
            string = R.string.warn2_0;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[4] & 0x2) != 0) {
            drawable = R.drawable.w21;
            string = R.string.warn2_1;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[4] & 0x4) != 0) {
            drawable = R.drawable.w18;
            string = R.string.warn2_2;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[4] & 0x8) != 0) {
            drawable = R.drawable.w18;
            string = R.string.warn2_3;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[4] & 0x10) != 0) {
            drawable = R.drawable.w18;
            string = R.string.warn2_4;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[4] & 0x20) != 0) {
            drawable = R.drawable.w18;
            string = R.string.warn2_5;
        } else if ((data[4] & 0x80) != 0) {
            drawable = R.drawable.w27;
            string = R.string.warn2_7;
        }

        // data 3

        else if ((data[5] & 0x1) != 0) {
            drawable = R.drawable.w26;
            string = R.string.warn3_0;
        } else if ((data[5] & 0x2) != 0) {
            drawable = R.drawable.w21;
            string = R.string.warn3_1;
        } else if ((data[5] & 0x4) != 0) {
            drawable = R.drawable.w13;
            string = R.string.warn3_2;
            drawable2 = R.drawable.w_red;
        } else if ((data[5] & 0x8) != 0) {
            drawable = R.drawable.w18;
            string = R.string.warn3_3;
            drawable2 = R.drawable.w_red;
        } else if ((data[5] & 0x10) != 0) {
            drawable = R.drawable.w18;
            string = R.string.warn3_4;
            drawable2 = R.drawable.w_red;
        } else if ((data[5] & 0x20) != 0) {
            drawable = R.drawable.w18;
            string = R.string.warn3_5;
            drawable2 = R.drawable.w_red;
        } else if ((data[5] & 0x40) != 0) {
            drawable = R.drawable.w18;
            string = R.string.warn3_6;
            drawable2 = R.drawable.w_red;
        } else if ((data[5] & 0x80) != 0) {
            drawable = R.drawable.w18;
            string = R.string.warn3_7;
            drawable2 = R.drawable.w_red;
        }

        // data 4
        else if ((data[6] & 0x1) != 0) {
            drawable = R.drawable.w7;
            string = R.string.warn4_0;
        } else if ((data[6] & 0x2) != 0) {
            drawable = R.drawable.w7;
            string = R.string.warn4_1;
        } else if ((data[6] & 0x4) != 0) {
            drawable = R.drawable.w19;
            string = R.string.warn4_2;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[6] & 0x8) != 0) {
            drawable = R.drawable.w27;
            string = R.string.warn4_3;
        } else if ((data[6] & 0x10) != 0) {
            drawable = R.drawable.w11;
            string = R.string.warn4_4;
        } else if ((data[6] & 0x20) != 0) {
            drawable = R.drawable.w19;
            string = R.string.warn4_5;
        } else if ((data[6] & 0x40) != 0) {
            // drawable = R.drawable.w7;
            string = R.string.warn4_6;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[6] & 0x80) != 0) {
            // drawable = R.drawable.w7;
            string = R.string.warn4_7;
            drawable2 = R.drawable.w_yellow;
        }

        // data 5
        else if ((data[7] & 0x1) != 0) {
            drawable = R.drawable.w27;
            string = R.string.warn5_0;
        } else if ((data[7] & 0x2) != 0) {
            drawable = R.drawable.w19;
            string = R.string.warn5_1;
        } else if ((data[7] & 0x4) != 0) {
            drawable = R.drawable.w25;
            string = R.string.warn5_2;
        } else if ((data[7] & 0x8) != 0) {
            drawable = R.drawable.w24;
            string = R.string.warn5_3;
            drawable2 = R.drawable.w_red;
        } else if ((data[7] & 0x10) != 0) {
            drawable = R.drawable.w24;
            string = R.string.warn5_4;
            drawable2 = R.drawable.w_red;
        } else if ((data[7] & 0x20) != 0) {
            drawable = R.drawable.w19;
            string = R.string.warn5_5;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[7] & 0x40) != 0) {
            drawable = R.drawable.w19;
            string = R.string.warn5_6;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[7] & 0x80) != 0) {
            drawable = R.drawable.w12;
            string = R.string.warn5_7;
            drawable2 = R.drawable.w_yellow;
        }

        // data 6
        else if ((data[8] & 0x1) != 0) {
            string = R.string.warn6_0;
        } else if ((data[8] & 0x2) != 0) {
            drawable = R.drawable.w15;
            string = R.string.warn6_1;
        } else if ((data[8] & 0x4) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn6_2;
        } else if ((data[8] & 0x8) != 0) {
            drawable = R.drawable.w27;
            string = R.string.warn6_3;
        } else if ((data[8] & 0x10) != 0) {
            string = R.string.warn6_4;
        } else if ((data[8] & 0x20) != 0) {
            drawable = R.drawable.w3;
            string = R.string.warn6_5;
        } else if ((data[8] & 0x40) != 0) {
            drawable = R.drawable.w27;
            string = R.string.warn6_6;
            drawable2 = R.drawable.w_red;
        } else if ((data[8] & 0x80) != 0) {
            drawable = R.drawable.w20;
            string = R.string.warn6_7;
            drawable2 = R.drawable.w_red;
        }

        // data 7
        else if ((data[9] & 0x1) != 0) {
            drawable = R.drawable.w6;
            string = R.string.warn7_0;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[9] & 0x2) != 0) {
            drawable = R.drawable.w6;
            drawable2 = R.drawable.w_yellow;
            string = R.string.warn7_1;
        } else if ((data[9] & 0x4) != 0) {
            drawable = R.drawable.w6;
            drawable2 = R.drawable.w_yellow;
            string = R.string.warn7_2;
        } else if ((data[9] & 0x8) != 0) {
            drawable = R.drawable.w6;
            drawable2 = R.drawable.w_yellow;
            string = R.string.warn7_3;
        } else if ((data[9] & 0x10) != 0) {
            drawable = R.drawable.w11;
            string = R.string.warn7_4;
        } else if ((data[9] & 0x20) != 0) {
            drawable = R.drawable.w11;
            string = R.string.warn7_5;
        } else if ((data[9] & 0x40) != 0) {
            drawable = R.drawable.w11;
            string = R.string.warn7_6;
        } else if ((data[9] & 0x80) != 0) {
            drawable = R.drawable.w11;
            string = R.string.warn7_7;
        }

        // data 8
        else if ((data[10] & 0x1) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn8_0;
        } else if ((data[10] & 0x2) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn8_1;
        } else if ((data[10] & 0x4) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn8_2;
        } else if ((data[10] & 0x8) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn8_3;
        } else if ((data[10] & 0x10) != 0) {
            drawable = R.drawable.w10;
            drawable2 = R.drawable.w_yellow;
            string = R.string.warn8_4;
        } else if ((data[10] & 0x20) != 0) {
            drawable = R.drawable.w10;
            drawable2 = R.drawable.w_yellow;
            string = R.string.warn8_5;
        } else if ((data[10] & 0x40) != 0) {
            drawable = R.drawable.w16;
            drawable2 = R.drawable.w_yellow;
            string = R.string.warn8_6;
        } else if ((data[10] & 0x80) != 0) {
            drawable = R.drawable.w16;
            drawable2 = R.drawable.w_yellow;
            string = R.string.warn8_7;
        }

        // data 9
        else if ((data[11] & 0x1) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn9_0;
            drawable2 = R.drawable.w_yellow;
        } else if ((data[11] & 0x2) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn9_1;
        } else if ((data[11] & 0x4) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn9_2;
        } else if ((data[11] & 0x8) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn9_3;
            drawable2 = R.drawable.w_yellow;
        }
        // data 10
        else if ((data[12] & 0x1) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn10_0;
        }

        // data 11
        else if ((data[13] & 0x1) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn11_0;
        } else if ((data[13] & 0x2) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn11_1;
        }

        // data 12
        else if ((data[14] & 0x1) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn12_0;
        } else if ((data[14] & 0x2) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn12_1;
        }

        // data 13
        else if ((data[15] & 0x40) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn13_6;
        } else if ((data[15] & 0x80) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn13_7;
        }
        // data 14
        else if ((data[16] & 0x1) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn14_0;
        }

        // data 15
        else if ((data[17] & 0x20) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn15_5;
        } else if ((data[17] & 0x80) != 0) {
            drawable = R.drawable.w10;
            string = R.string.warn15_7;
        }

        if (string != 0) {
            WarningMsgManager.start(mContext);
            WarningMsgManager.updateView(mContext, drawable, drawable2, string);
        } else {
            WarningMsgManager.stop();
        }
    }

    private void updateParkingMsg(byte[] data) {

        int string = 0;
        int string1 = 0;
        int drawable1 = 0;
        if ((data[2] & 0x1) == 0) {
            AutoParkingMsgManager.stop();
            return;
        }
        switch (data[3]) {
            case 1:
                break;
            case 2:
                string = R.string.park_info2;
                string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 3:
                string = R.string.park_info3;
                string1 = R.string.prompt03;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 4:
                string = R.string.park_info4;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 5:
                string = R.string.park_info5;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_11;
                break;
            case 6:
                string = R.string.park_info6;
                string1 = R.string.prompt06;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 7:
                string = R.string.park_info7;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_10;
                break;
            case 8:
                string = R.string.park_info8;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_09;
                break;
            case 9:
                string = R.string.park_info9;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_10;
                break;
            case 10:
                string = R.string.park_info10;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_09;
                break;
            case 11:
                string = R.string.park_info11;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_15;
                break;
            case 12:
                string = R.string.park_info12;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_14;
                break;
            case 13:
                string = R.string.park_info13;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_14;
                break;

            case 14:
                string = R.string.park_info14;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_14;
                break;
            case 15:
                string = R.string.park_info15;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_13;
                break;
            case 16:
                string = R.string.park_info16;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_08;
                break;
            case 17:
                string = R.string.park_info17;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_13;
                break;
            case 18:
                string = R.string.park_info18;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_08;
                break;
            case 19:
                string = R.string.park_info19;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_07;
                break;
            case 20:
                string = R.string.park_info20;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_06;
                break;
            case 21:
                string = R.string.park_info21;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_05;
                break;
            case 22:
                string = R.string.park_info22;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_04;
                break;
            case 23:
                string = R.string.park_info23;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_03;
                break;
            case 24:
                string = R.string.park_info24;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_02;
                break;
            case 25:
                string = R.string.park_info25;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_01;
                break;
            case 26:
                string = R.string.park_info26;
                // string1 = R.string.prompt02;
                drawable1 = R.drawable.canbus62park_bg_14;
                break;
            case 27:
                string = R.string.park_info27;
                string1 = R.string.prompt1b;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 28:
                string = R.string.park_info28;
                string1 = R.string.prompt1c;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 29:
                string = R.string.park_info29;
                string1 = R.string.prompt1d;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 30:
                string = R.string.park_info30;
                string1 = R.string.prompt1e;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 31:
                string = R.string.park_info31;
                string1 = R.string.prompt1f;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 32:
                string = R.string.park_info32;
                string1 = R.string.prompt20;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 33:
                string = R.string.park_info33;
                string1 = R.string.prompt21;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 34:
                string = R.string.park_info34;
                string1 = R.string.prompt22;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;
            case 35:
                string = R.string.park_info35;
                // string1 = R.string.prompt20;
                drawable1 = R.drawable.canbus62park_bg_05;
                break;
            case 36:
                string = R.string.park_info36;
                // string1 = R.string.prompt20;
                drawable1 = R.drawable.canbus62park_bg_04;
                break;
            case 37:
                string = R.string.park_info37;
                // string1 = R.string.prompt20;
                drawable1 = R.drawable.canbus62park_bg_01;
                break;
            case 38:
                string = R.string.park_info38;
                // string1 = R.string.prompt20;
                drawable1 = R.drawable.canbus62park_bg_12;
                break;
            case 39:
                string = R.string.park_info39;
                string1 = R.string.prompt1d;
                drawable1 = R.drawable.canbus62park_bg_00;
                break;

        }
        if (string != 0) {
            AutoParkingMsgManager.start(mContext);
            AutoParkingMsgManager.updateView(mContext, drawable1, string, string1);
        } else {
            AutoParkingMsgManager.stop();
        }
    }

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

    private final int mSource = MyCmd.SOURCE_NONE;
    private final int mBaud = 0;

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

    public void setPhone(int status, String num) {

        if (status >= HFP_INFO_CONNECTED) {
            switch (status) {
                case HFP_INFO_CALLED:
                    status = 3;
                    break;
                case HFP_INFO_INCOMING:
                    status = 1;
                    break;
                case HFP_INFO_CALLING:
                    status = 2;
                    break;
                case HFP_INFO_CONNECTED:
                    status = 4;
                    break;
                default:
                    status = 0;
                    break;
            }
        } else {
            status = 0x43;
        }

        byte[] data = new byte[]{(byte) 0xc5, 0x4, 0, (byte) status, 0, 0};

        sendDataToCanbox(data, data.length);

        sendId3Ex((byte) 0x1, num);
    }

    private void doSyncControl(byte[] data) {
        switch (data[2]) {
            case 0x10:
                doKey(KEY_DVD, 1);
                doKey(KEY_DVD, 0);
                break;
            case 0x11:
                if (mSource == MyCmd.SOURCE_DVD) {
                    byte key = 0;
                    switch (data[3]) {
                        case 0x1:
                            key = MyCmd.Keycode.PLAY;
                            break;
                        case 0x2:
                            key = MyCmd.Keycode.PAUSE;
                            break;
                        case 0x3:
                            key = MyCmd.Keycode.KEY_REPEAT;
                            break;
                        case 0x4:
                            key = MyCmd.Keycode.KEY_SHUFFLE;
                            break;
                        case 0x5:
                            key = MyCmd.Keycode.PREVIOUS;
                            break;
                        case 0x6:
                            key = MyCmd.Keycode.NEXT;
                            break;
                    }

                    if (key != 0) {
                        doKey(key, 1);
                        doKey(key, 0);
                    }
                }
                break;
            case 0x12: {

                byte key = (byte) (data[3] % 10);

                key += MyCmd.Keycode.NUMBER0;

                doKey(key, 1);
                doKey(key, 0);
                break;
            }
            case 0x20: {
                switch (data[3]) {
                    case 0x0:
                    case 0x1:
                        if (mSource != MyCmd.SOURCE_RADIO || mBaud != 0) {
                            doKey(KEY_FM, 1);
                            doKey(KEY_FM, 0);
                        }
                        break;
                    case 0x2:
                    case 0x3:
                        if (mSource != MyCmd.SOURCE_RADIO || mBaud == 0) {
                            doKey(KEY_FM, 1);
                            doKey(KEY_FM, 0);
                        }
                        break;
                }

                BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_SET_CURRENT_FREQUENCY, data[5] & 0xff, data[4] & 0xff);
                break;
            }
            case 0x22:
                if (mBaud == 0) {
                    BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 0x3, data[3] - 1);
                }
                break;
            case 0x23:
                if (mBaud != 0) {

                    BroadcastUtil.sendToCarServiceMcuRadio(mContext, ProtocolAk47.SEND_RADIO_SUB_RADIO_OPERATION, 0x3, data[3] - 1);
                }
        }

    }

    public void updateTime() {

        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 0;

        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        byte format;
        if ("12".equals(strTimeFormat)) {
            ampm = 0x40;
            if (h >= 12) {
                ampm |= 0x80;
                h -= 12;
            }
        } else {
            // ampm = 0x40;
        }

        byte m = (byte) curDate.getMinutes();

        byte[] buf = new byte[]{(byte) 0xc8, 0x03, h, m, ampm};
        sendDataToCanbox(buf, buf.length);
    }

    public void sendId3Ex(byte index, String num) {

        try {
            if (num == null) {
                num = "";
            }
            byte[] n = num.getBytes(); // del 0xff 0xfe

            int num_len = n.length;
            int len = num_len + 4;
            if (num_len > 0x42) {
                len = 0x42;
            }
            byte[] data = new byte[len];

            data[0] = (byte) 0xcb;
            data[1] = (byte) (len - 2);
            data[2] = 0x1;
            data[3] = index;
            for (int i = 0; i < num_len && i < (len - 4); ++i) {
                data[4 + i] = n[i + (n.length - num_len)];
            }
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("Nissan2013Simple", "sendId3" + e);
        }
    }

    public void sendId3(byte index, String num) {

        try {
            if (num == null) {
                num = "";
            }
            byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

            int num_len = n.length;
            if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
                num_len -= 2;
            }
            int len = num_len + 4;
            if (num_len > 0x42) {
                len = 0x42;
            }
            byte[] data = new byte[len];

            data[0] = (byte) 0xcb;
            data[1] = (byte) (len - 2);
            data[2] = 0x11;
            data[3] = index;
            for (int i = 0; i < num_len && i < (len - 4); ++i) {
                data[4 + i] = n[i + (n.length - num_len)];
            }
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("Nissan2013Simple", "sendId3" + e);
        }
    }

    String mName = null;
    String mArtist = null;
    String mAlbum = null;

    public void setSongName(String s) {
        sendId3((byte) 0x3, s);
        mName = s;
    }

    public void setSongAritst(String s) {
        sendId3((byte) 0x5, s);
        mArtist = s;
    }

    public void setSongAlbum(String s) {
        sendId3((byte) 0x4, s);
        mAlbum = s;
    }


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
                //				Log.e(TAG, e.getMessage());
            }
        }
    }

    public int getUpdateTime() {
        return 60000;
    }
}
