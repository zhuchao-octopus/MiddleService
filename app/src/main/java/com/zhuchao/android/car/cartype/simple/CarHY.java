package com.zhuchao.android.car.cartype.simple;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.utils.MachineConfig;
import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.canbox.SosManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.McuManager;
import com.zhuchao.android.car.manager.OSProManager;

import java.util.Date;
import java.util.Locale;


public class CarHY extends Canbox {

    public CarHY() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
        updateCanboxKeySettings();

        buildCmdAngle((byte) 0x26, (byte) 0x5, 0x1200);
    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
        udpateLang();
        for (int i = 0; i < buf.length; ++i) {
            buf[i] = 10;
        }
    }

    // private void requestVersion(){
    // byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
    // sendDataToCanbox(data, data.length);
    // }

    public void updateCanboxKeySettings() {
        if (CarUtil.getKeyType() == 3) {
            mKeyPannel = KEYS_PANNEL_S_HIDE;
        } else if (CarUtil.getKeyType() == 4) {
            mKeyPannel = KEYS_PANNEL_S_M;
        } else if (CarUtil.getKeyType() == 1) {
            mKeyPannel = KEYS_PANNEL_KX5_H;
        } else if (CarUtil.getKeyType() == 2) {
            mKeyPannel = KEYS_PANNEL_KX5_M;
        } else if (CarUtil.getKeyType() == 5) {
            mKeyPannel = KEYS_SPORTAGE;
        } else {
            mKeyPannel = KEYS_PANNEL_NORMAL;
        }

        switch (CarUtil.getModelId()) {
            case 4:
                if (CarUtil.getCarTypeConfig() == 2) {
                    mKeyPannel = KEYS_PANNEL_S_HIDE;
                } else if (CarUtil.getCarTypeConfig() == 1) {
                    mKeyPannel = KEYS_PANNEL_S_M;
                }
                break;
            case 6:
                if (CarUtil.getCarTypeConfig() == 2) {
                    mKeyPannel = KEYS_PANNEL_KX5_H;
                } else if (CarUtil.getCarTypeConfig() == 1) {
                    mKeyPannel = KEYS_PANNEL_KX5_M;
                }
                break;
        }

        if (CarUtil.getCarEQ() == 1) {
            CarUtil.mIsNeedSendEQ = true;
            CarUtil.setMcuEQZoneUsed(1);
        }

        if (CarUtil.getCarType() >= 1 || CarUtil.getCarType() <= 3) {
            byte[] buf = new byte[]{
                    (byte) 0xca, 0x1, (byte) (CarUtil.getCarType() - 1)
            };
            sendDataToCanbox(buf, buf.length);
        }
    }

    private byte[][] mKeyPannel;

    private final static byte[][] KEYS_PANNEL_S_M = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_PREVIOUSSONG}, {0x4, KEY_NEXTSONG}, {0x5, KEY_BT}, {0xa, KEY_BT}, {0x38, KEY_BT}, {0x6, AK_KEYPAD_MUTE_FAKE},
            {0x12, KEY_MIC}, {(byte) 0x89, AK_KEYPAD_MUTE_FAKE}, {0x7, KEY_MODE}, {(byte) 0x88, KEY_MODE}, {0x9, MyCmd.Keycode.DARK}, {0x30, MyCmd.Keycode.DARK}, {0xb, KEY_MENU}, {0xc, KEY_BACK},
            {0x0d, KEY_CH_UP}, {(byte) 0x83, KEY_CH_UP}, {0x0e, KEY_CH_UP}, {(byte) 0x84, KEY_CH_DOWN}, {0x0f, KEY_POWER}, {0x3b, KEY_POWER}, {(byte) 0x87, KEY_POWER}, {0x10, KEY_BT_HANG},
            {0x31, MyCmd.Keycode.NAVIGATION}, {0x32, MyCmd.Keycode.NAVIGATION}, {0x33, MyCmd.Keycode.NAVIGATION}, {0x34, MyCmd.Keycode.SETUP}, {0x35, MyCmd.Keycode.RADIO}, {0x36, MyCmd.Keycode.RADIO},
            {0x37, MyCmd.Keycode.AUDIO}, {0x39, MyCmd.Keycode.KEY_SEEK_PREV}, {(byte) 0x86, MyCmd.Keycode.KEY_SEEK_PREV}, {0x3a, MyCmd.Keycode.KEY_SEEK_NEXT},
            {(byte) 0x85, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x3c, AK_KEYPAD_VOLUME_A}, {(byte) 0x81, AK_KEYPAD_VOLUME_A}, {(byte) 0x8a, AK_KEYPAD_VOLUME_A}, {0x3d, AK_KEYPAD_VOLUME_D},
            {(byte) 0x82, AK_KEYPAD_VOLUME_D}, {(byte) 0x8b, AK_KEYPAD_VOLUME_D}, {(byte) 0x3e, MyCmd.Keycode.KEY_TURN_A}, {(byte) 0x3f, MyCmd.Keycode.KEY_TURN_D},
    };

    private final static byte[][] KEYS_PANNEL_S_HIDE = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_PREVIOUSSONG}, {0x4, KEY_NEXTSONG}, {0x5, KEY_BT}, {0xa, KEY_BT}, {0x38, KEY_BT}, {0x6, AK_KEYPAD_MUTE_FAKE},
            {0x12, KEY_MIC}, {(byte) 0x89, AK_KEYPAD_MUTE_FAKE}, {0x7, KEY_MODE}, {(byte) 0x88, KEY_MODE}, {0x9, MyCmd.Keycode.DARK}, {0x30, MyCmd.Keycode.BT}, {0xb, KEY_MENU}, {0xc, KEY_BACK},
            {0x0d, KEY_CH_UP}, {(byte) 0x83, KEY_CH_UP}, {0x0e, KEY_CH_UP}, {(byte) 0x84, KEY_CH_DOWN}, {0x0f, KEY_POWER}, {0x3b, KEY_POWER}, {(byte) 0x87, KEY_POWER}, {0x10, KEY_BT_HANG},
            {0x31, MyCmd.Keycode.NAVIGATION}, {0x32, MyCmd.Keycode.NAVIGATION}, {0x33, MyCmd.Keycode.NAVIGATION}, {0x34, MyCmd.Keycode.SETUP}, {0x35, MyCmd.Keycode.RADIO}, {0x36, MyCmd.Keycode.RADIO},
            {0x37, MyCmd.Keycode.AUDIO}, {0x39, MyCmd.Keycode.KEY_SEEK_PREV}, {(byte) 0x86, MyCmd.Keycode.KEY_SEEK_PREV}, {0x3a, MyCmd.Keycode.KEY_SEEK_NEXT},
            {(byte) 0x85, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x3c, AK_KEYPAD_VOLUME_A}, {(byte) 0x81, AK_KEYPAD_VOLUME_A}, {(byte) 0x8a, AK_KEYPAD_VOLUME_A}, {0x3d, AK_KEYPAD_VOLUME_D},
            {(byte) 0x82, AK_KEYPAD_VOLUME_D}, {(byte) 0x8b, AK_KEYPAD_VOLUME_D}, {(byte) 0x3e, MyCmd.Keycode.KEY_TURN_A}, {(byte) 0x3f, MyCmd.Keycode.KEY_TURN_D},
    };

    private final static byte[][] KEYS_PANNEL_NORMAL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_PREVIOUSSONG}, {0x4, KEY_NEXTSONG}, {0x5, KEY_BT}, {0xa, KEY_BT}, {0x38, KEY_BT}, {0x6, AK_KEYPAD_MUTE_FAKE},
            {0x12, KEY_MIC}, {(byte) 0x89, AK_KEYPAD_MUTE_FAKE}, {0x7, KEY_MODE}, {(byte) 0x88, KEY_MODE}, {0x9, MyCmd.Keycode.DARK}, {0x30, MyCmd.Keycode.DARK}, {0xb, KEY_MENU}, {0xc, KEY_BACK},
            {0x0d, KEY_CH_UP}, {(byte) 0x83, KEY_CH_UP}, {0x0e, KEY_CH_UP}, {(byte) 0x84, KEY_CH_DOWN}, {0x0f, KEY_POWER}, {0x3b, KEY_POWER}, {(byte) 0x87, KEY_POWER}, {0x10, KEY_BT_HANG},
            {0x31, MyCmd.Keycode.NAVIGATION}, {0x32, MyCmd.Keycode.NAVIGATION}, {0x33, MyCmd.Keycode.NAVIGATION}, {0x34, MyCmd.Keycode.SETUP}, {0x35, MyCmd.Keycode.PLAY_PAUSE},
            {0x36, MyCmd.Keycode.RADIO}, {0x37, MyCmd.Keycode.AUDIO}, {0x39, MyCmd.Keycode.KEY_SEEK_PREV}, {(byte) 0x86, MyCmd.Keycode.KEY_SEEK_PREV}, {0x3a, MyCmd.Keycode.KEY_SEEK_NEXT},
            {(byte) 0x85, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x3c, AK_KEYPAD_VOLUME_A}, {(byte) 0x81, AK_KEYPAD_VOLUME_A}, {(byte) 0x8a, AK_KEYPAD_VOLUME_A}, {0x3d, AK_KEYPAD_VOLUME_D},
            {(byte) 0x82, AK_KEYPAD_VOLUME_D}, {(byte) 0x8b, AK_KEYPAD_VOLUME_D}, {(byte) 0x3e, MyCmd.Keycode.KEY_TURN_A}, {(byte) 0x3f, MyCmd.Keycode.KEY_TURN_D},
    };

    private final static byte[][] KEYS_PANNEL_KX5_M = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_PREVIOUSSONG}, {0x4, KEY_NEXTSONG}, {0x5, KEY_BT}, {0xa, KEY_BT}, {0x38, KEY_BT}, {0x6, AK_KEYPAD_MUTE_FAKE},
            {0x12, KEY_MIC}, {(byte) 0x89, AK_KEYPAD_MUTE_FAKE}, {0x7, KEY_MODE}, {(byte) 0x88, KEY_MODE}, {0x9, MyCmd.Keycode.DARK}, {0x30, MyCmd.Keycode.DARK}, {0xb, KEY_MENU}, {0xc, KEY_BACK},
            {0x0d, KEY_CH_UP}, {(byte) 0x83, KEY_CH_UP}, {0x0e, KEY_CH_UP}, {(byte) 0x84, KEY_CH_DOWN}, {0x0f, KEY_POWER}, {0x3b, KEY_POWER}, {(byte) 0x87, KEY_POWER}, {0x10, KEY_BT_HANG},
            {0x31, MyCmd.Keycode.AUDIO}, {0x32, MyCmd.Keycode.DARK}, {0x33, MyCmd.Keycode.SETUP}, {0x34, MyCmd.Keycode.SETUP}, {0x35, MyCmd.Keycode.PLAY_PAUSE}, {0x36, MyCmd.Keycode.HOME},
            {0x37, MyCmd.Keycode.RADIO}, {0x39, MyCmd.Keycode.KEY_SEEK_PREV}, {(byte) 0x86, MyCmd.Keycode.KEY_SEEK_PREV}, {0x3a, MyCmd.Keycode.KEY_SEEK_NEXT},
            {(byte) 0x85, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x3c, AK_KEYPAD_VOLUME_A}, {(byte) 0x81, AK_KEYPAD_VOLUME_A}, {(byte) 0x8a, AK_KEYPAD_VOLUME_A}, {0x3d, AK_KEYPAD_VOLUME_D},
            {(byte) 0x82, AK_KEYPAD_VOLUME_D}, {(byte) 0x8b, AK_KEYPAD_VOLUME_D}, {(byte) 0x3e, MyCmd.Keycode.KEY_TURN_A}, {(byte) 0x3f, MyCmd.Keycode.KEY_TURN_D},
    };

    private final static byte[][] KEYS_PANNEL_KX5_H = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_PREVIOUSSONG}, {0x4, KEY_NEXTSONG}, {0x5, KEY_BT}, {0xa, KEY_BT}, {0x38, KEY_BT}, {0x6, AK_KEYPAD_MUTE_FAKE},
            {0x12, KEY_MIC}, {(byte) 0x89, AK_KEYPAD_MUTE_FAKE}, {0x7, KEY_MODE}, {(byte) 0x88, KEY_MODE}, {0x9, MyCmd.Keycode.DARK}, {0x30, MyCmd.Keycode.DARK}, {0xb, KEY_MENU}, {0xc, KEY_BACK},
            {0x0d, KEY_CH_UP}, {(byte) 0x83, KEY_CH_UP}, {0x0e, KEY_CH_UP}, {(byte) 0x84, KEY_CH_DOWN}, {0x0f, KEY_POWER}, {0x3b, KEY_POWER}, {(byte) 0x87, KEY_POWER}, {0x10, KEY_BT_HANG},
            {0x31, MyCmd.Keycode.NAVIGATION}, {0x32, MyCmd.Keycode.NAVIGATION}, {0x33, MyCmd.Keycode.SETUP}, {0x34, MyCmd.Keycode.SETUP}, {0x35, MyCmd.Keycode.PLAY_PAUSE}, {0x36, MyCmd.Keycode.RADIO},
            {0x37, MyCmd.Keycode.AUDIO}, {0x39, MyCmd.Keycode.KEY_SEEK_PREV}, {(byte) 0x86, MyCmd.Keycode.KEY_SEEK_PREV}, {0x3a, MyCmd.Keycode.KEY_SEEK_NEXT},
            {(byte) 0x85, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x3c, AK_KEYPAD_VOLUME_A}, {(byte) 0x81, AK_KEYPAD_VOLUME_A}, {(byte) 0x8a, AK_KEYPAD_VOLUME_A}, {0x3d, AK_KEYPAD_VOLUME_D},
            {(byte) 0x82, AK_KEYPAD_VOLUME_D}, {(byte) 0x8b, AK_KEYPAD_VOLUME_D}, {(byte) 0x3e, MyCmd.Keycode.KEY_TURN_A}, {(byte) 0x3f, MyCmd.Keycode.KEY_TURN_D},
    };

    private final static byte[][] KEYS_SPORTAGE = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_PREVIOUSSONG}, {0x4, KEY_NEXTSONG}, {0x5, KEY_BT}, {0xa, KEY_BT}, {0x38, KEY_BT}, {0x6, AK_KEYPAD_MUTE_FAKE},
            {0x12, KEY_MIC}, {(byte) 0x89, AK_KEYPAD_MUTE_FAKE}, {0x7, KEY_MODE}, {(byte) 0x88, KEY_MODE}, {0x9, MyCmd.Keycode.DARK}, {0x30, MyCmd.Keycode.DARK}, {0xb, KEY_MENU}, {0xc, KEY_BACK},
            {0x0d, KEY_CH_UP}, {(byte) 0x83, KEY_CH_UP}, {0x0e, KEY_CH_UP}, {(byte) 0x84, KEY_CH_DOWN}, {0x0f, KEY_POWER}, {0x3b, KEY_POWER}, {(byte) 0x87, KEY_POWER}, {0x10, KEY_BT_HANG},
            {0x31, MyCmd.Keycode.HOME}, {0x32, MyCmd.Keycode.BT}, {0x33, MyCmd.Keycode.SETUP}, {0x34, MyCmd.Keycode.SETUP}, {0x35, MyCmd.Keycode.PLAY_PAUSE}, {0x36, MyCmd.Keycode.RADIO},
            {0x37, MyCmd.Keycode.AUDIO}, {0x39, MyCmd.Keycode.KEY_SEEK_PREV}, {(byte) 0x86, MyCmd.Keycode.KEY_SEEK_PREV}, {0x3a, MyCmd.Keycode.KEY_SEEK_NEXT},
            {(byte) 0x85, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x3c, AK_KEYPAD_VOLUME_A}, {(byte) 0x81, AK_KEYPAD_VOLUME_A}, {(byte) 0x8a, AK_KEYPAD_VOLUME_A}, {0x3d, AK_KEYPAD_VOLUME_D},
            {(byte) 0x82, AK_KEYPAD_VOLUME_D}, {(byte) 0x8b, AK_KEYPAD_VOLUME_D}, {(byte) 0x3e, MyCmd.Keycode.KEY_TURN_A}, {(byte) 0x3f, MyCmd.Keycode.KEY_TURN_D},
    };

    private boolean isOneKey(byte b) {
        return ((b & 0xff) >= 0x81) || ((b & 0xff) == 0x3c) || ((b & 0xff) == 0x3d) || ((b & 0xff) == 0x3e) || ((b & 0xff) == 0x3f);
    }

    private void parseWheelKey(byte[] data, int len) {

        if (doKeyStudy(data[2], data[3])) {
            return;
        }

        byte key = 0;
        if (mKeyPannel == null) {
            return;
        }
        for (int i = 0; i < mKeyPannel.length; ++i) {
            if (mKeyPannel[i][0] == data[2]) {
                key = mKeyPannel[i][1];
                //				if (CarUtil.getChangeKey() == 2){
                //					if (data[2] == 0x2){
                //						key = AK_KEYPAD_VOLUME_A;
                //					} else if (data[2] == 0x1){
                //						key = AK_KEYPAD_VOLUME_D;
                //					}
                //				}
                break;
            }
        }

        if (key != 0) {

            if (((data[2] & 0xff) == 0x81) || ((data[2] & 0xff) == 0x82) || ((data[2] & 0xff) == 0x3C) || ((data[2] & 0xff) == 0x3D)) {
                int step = data[3] & 0xff;
                // step = 0x10;
                if (step <= 8) {
                    doKeyStep(key, step);
                } else {
                    McuManager mcu = McuManager.getInstance();
                    if (mcu != null) {
                        int v = mcu.getMcuVolume();
                        switch (key) {
                            case AK_KEYPAD_VOLUME_A:
                                // if (v >= McuManager.MAX_VOLUME) {
                                // return;
                                // }
                                v += step;
                                break;
                            case AK_KEYPAD_VOLUME_D:
                                // if (v <= 0) {
                                // return;
                                // }
                                v -= step;
                                break;
                        }

                        if (v < 0) {
                            v = 0;
                        } else if (v > McuManager.MAX_VOLUME) {
                            v = McuManager.MAX_VOLUME;
                        }
                        mcu.setVolume(v);
                        Util.setFileValue("/sys/class/ak/source/beep", "2");
                    }
                }
            } else if (isOneKey(data[2])) {
                doKey(key, 1);
                Util.doSleep(10);
                doKey(key, 0);
            } else {
                doKey(key, data[3]);
            }
        } else {
            if (data[3] == 0) {
                doKey(0, 0);
            }
        }
    }

    private static final byte mTst = -10;

    private void parseACInfo(byte[] data, int len) {
        if (CarUtil.getAirCondition() == 4) {
            if (data[4] == 0x1e) {
                data[4] = (byte) 0xff;
            } else if (data[4] > 0) {
                data[4] = (byte) ((17f + (0.5f * data[4])) * 2);
            }
            if (data[5] == 0x1e) {
                data[5] = (byte) 0xff;
            } else if (data[5] > 0) {
                data[5] = (byte) ((17f + (0.5f * data[5])) * 2);
            }
        } else if (CarUtil.getAirCondition() == 6) {
            if (data[4] == 0x1a) {
                data[4] = (byte) 0xff;
            } else if (data[4] == -4) {
                data[4] = 0;
            } else {
                data[4] = (byte) ((17f + (0.5f * data[4])) * 2);
            }

            data[5] = 0x5b;
            if (data[5] == 0x1a) {
                data[5] = (byte) 0xff;
            } else if (data[5] > 0) {
                data[5] = (byte) ((17f + (0.5f * data[5])) * 2);
            }
        } else {
            if (data[4] == 0x22) {
                data[4] = (byte) 0xff;
            } else if (data[4] > 0) {
                data[4] = (byte) ((15f + (0.5f * data[4])) * 2);
            }
            if (data[5] == 0x22) {
                data[5] = (byte) 0xff;
            } else if (data[5] > 0) {
                data[5] = (byte) ((15f + (0.5f * data[5])) * 2);
            }
        }

        byte[] airData = new byte[8];
        airData[0] = (byte) ((data[2] & 0xfc) | ((data[2] & 0x1) << 1));

        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);
        airData[4] = (byte) ((data[6] & 0xf0) | ((data[2] & 0x2) << 1));
        int msg = CANBOX_HIDE_AIR;
        if (/*(data[2] & 0x80) != 0 && */((data[3] & 0x10) != 0)) {
            msg = CANBOX_RETURN_AIR;
        }

        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(msg, airData));
        }
    }

    private void do1050SosCustomCmd(byte[] data) {
        if (MachineConfig.VALUE_SYSTEM_UI22_1050.equals(GlobalDefinition.mSystemUI) || MachineConfig.VALUE_SYSTEM_UI_1050_2.equals(GlobalDefinition.mSystemUI) || CarUtil.getCarType() == 4) {
            switch (data[2]) {
                case 0x23:
                    if (data[3] == 1) {
                        SosManager.start(mContext, 1);
                    }
                    break;
                case 0x24:
                case 0x26:
                    if (data[3] == 1) {
                        SosManager.stop();
                    }
                    break;
                case 0x25:
                    if (data[3] == 1) {
                        SosManager.start(mContext, 2);
                    }
                    break;
            }
        }
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data, len);
                do1050SosCustomCmd(data);
            }
            break;
            case 0x21: {
                parseACInfo(data, len);
            }
            break;
            case 0x22: // Radar back
            {
                for (int i = 0; i < 4; ++i) {
                    switch (data[2 + i]) {
                        case 1:
                            data[2 + i] = RADAR_DISTANCE_WANRING;
                            break;
                        case 2:
                            data[2 + i] = RADAR_DISTANCE_NORMAL;
                            break;
                        case 3:
                            data[2 + i] = RADAR_DISTANCE_LONG;
                            break;
                    }
                }

                byteArrayCopy(mRadar, data, 0, 2, 4);

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
                boolean show = false;
                for (int i = 0; i < 4; ++i) {
                    switch (data[2 + i]) {
                        case 1:
                            data[2 + i] = RADAR_DISTANCE_WANRING;
                            break;
                        case 2:
                            data[2 + i] = RADAR_DISTANCE_NORMAL;
                            break;
                        case 3:
                            data[2 + i] = RADAR_DISTANCE_LONG;
                            break;
                    }
                    if (data[2 + i] != 0) {
                        show = true;
                    }
                }
                byteArrayCopy(mRadar, data, 4, 2, 4);
                if (show) {
                    boolean zero = Util.isZero(mRadar);
                    if (!zero) {
                        RadarManager.start(mContext);
                        checkHideRadar();
                    }
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                }
            }
            break;
            case 0x24: {
                int door = (data[3] & 0x3f);
                // door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6)
                // | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) <<
                // 1));

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
                updateOutDoorTemp(data[2]);

            }
            break;
            case 0x29: {
                if (CarUtil.getCarEQ() == 0) {
                    if (data[2] == 1) {
                        CarUtil.mIsNeedSendEQ = true;
                        CarUtil.setMcuEQZoneUsed(1);
                        McuManager mcu = McuManager.getInstance();
                        if (mcu != null) {
                            mcu.setAudio(0x6, 0x2);
                        }
                        startEQ();
                    } else {
                        CarUtil.mIsNeedSendEQ = false;
                        CarUtil.setMcuEQZoneUsed(0);
                        stopEQ();
                    }
                }
            }
            break;
            case 0x30:
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            case 0x50: {
                byte[] d = new byte[]{data[2], data[3]};
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_HY_UI_DATA, d));
                }
                byte reverse = 0;
                if ((((data[2] & 0xC0)) == 0x80) || (((data[2] & 0xC0)) == 0x40)) {
                    reverse = 1;
                }
                OSProManager.simulationReverse(reverse);

                break;
            }
            default:
                super.parseCanboxData(data, len);
                break;
        }


        returnDriveData(data);
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
        String s;
        if (CarUtil.mTempUnit == 2) {
            temp = (int) ((temp) * 1.8f + 32);
            s = temp + mContext.getResources().getString(R.string.temp_unic_fahrenheit);
        } else {
            s = temp + mContext.getResources().getString(R.string.temp_unic_centigrade);
        }

        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);


    }

    private int mDoorStatus = 0;

    public void updateTime() {
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        if (CarUtil.getCarType2() == 1) {
            h = (byte) ((h + 1) % 24);
        } else if (CarUtil.getCarType2() == 2) {
            h = (byte) ((h + 23) % 24);
        } else {
            h = fixTimeHour(h);
        }

        if ("12".equals(strTimeFormat)) {
            if (h >= 12) {
                ampm |= 0x80;
            }

            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }

            h |= 0x80;
        }

        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();


        byte[] buf = new byte[]{
                (byte) 0xc6, 0x08, 0x01, 0x0, 0x0, 0x0, h, m, s, ampm
        };
        sendDataToCanbox(buf, buf.length);
    }

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

    private final static int HIDE_RADAR = 0;
    private final static int SHOW_VOLUME_STEP = 1;
    private final static int REPEAT_SEND_EQ = 2;

    private void doKeyStep(int key, int step) {
        mHandler.removeMessages(SHOW_VOLUME_STEP);
        doKey(key, 1);
        doKey(key, 0);
        --step;
        if (step > 0) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW_VOLUME_STEP, key, step), 30);
        }
    }

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_RADAR:
                    RadarManager.stop();
                    break;
                case SHOW_VOLUME_STEP:
                    doKeyStep(msg.arg1, msg.arg2);
                    break;
                case REPEAT_SEND_EQ:
                    powerEQ(true);
                    mHandler.removeMessages(REPEAT_SEND_EQ);
                    mHandler.sendEmptyMessageDelayed(REPEAT_SEND_EQ, 300);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void powerEQ(boolean power) {
        buf[0] = (byte) 0xc7;
        buf[1] = 0x6;
        buf[2] = (byte) (power ? 0 : 1);

        sendDataToCanbox(buf, buf.length);
    }

    byte[] buf = new byte[8];

    public void sendEqToCanbox(byte[] eq) {
        if (eq != null && eq.length >= 11) {

            int i;
            buf[0] = (byte) 0xc7;
            buf[1] = 0x6;
            buf[2] = 0x0;

            i = (eq[0] * 200 / 14);
            if (i % 10 > 0) {
                i /= 10;
                ++i;
            } else {
                i /= 10;
            }

            buf[3] = (byte) (20 - i);
            i = (eq[1] * 200 / 14);

            if (i % 10 > 0) {
                i /= 10;
                ++i;
            } else {
                i /= 10;
            }

            buf[4] = (byte) (20 - i);

            buf[5] = (byte) ((eq[2] + eq[3] + eq[4]) / 3);
            buf[6] = (byte) ((eq[5] + eq[6] + eq[7]) / 3);
            buf[7] = (byte) ((eq[8] + eq[9] + eq[10]) / 3);

            sendDataToCanbox(buf, buf.length);
        }
    }

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

        // if (mPhoneStatus < HFP_INFO_CALLED) {

        sendDataToCanbox(mData, mData.length);
        // }
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        mData = new byte[]{
                (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0, 0, 0
        };
        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source) {// default is simple box

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

        // if (s == 0xb || s == 0x7) {
        // data = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0,
        // 0 };
        // } else {
        mData = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0};
        // }

        sendDataToCanbox(mData, mData.length);
    }

    private void setEQVolume(int volume) {

        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    public void udpateLang() {
        int lang = -1;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("en")) {
                lang = 2;
            } else if (locale.equals("zh")) {
                lang = 1;
            } else if (locale.equals("ko")) {
                lang = 3;
            } else if (locale.equals("ru")) {
                lang = 4;
            } else if (locale.equals("tr")) {
                lang = 5;
            } else if (locale.equals("fr")) {
                lang = 6;
            } else if (locale.equals("de")) {
                lang = 7;
            } else if (locale.equals("es")) {
                lang = 8;
            } else if (locale.equals("cs")) {
                lang = 9;
            } else if (locale.equals("da")) {
                lang = 0xa;
            } else if (locale.equals("it")) {
                lang = 0xb;
            } else if (locale.equals("ms")) {
                lang = 0xc;
            } else if (locale.equals("nl")) {
                lang = 0xd;
            } else if (locale.equals("nb")) {
                lang = 0xe;
            } else if (locale.equals("pl")) {
                lang = 0xf;
            } else if (locale.equals("sk")) {
                lang = 0x10;
            } else if (locale.equals("sv")) {
                lang = 0x11;
            } else if (locale.equals("pt")) {
                lang = 0x12;
            } else if (locale.equals("ar")) {
                lang = 0x13;
            }


        }
        if (lang != -1) {
            byte[] buf = {(byte) 0xc6, 0x2, 0x2, (byte) lang};
            sendDataToCanbox(buf, buf.length);
        }
    }

    private void startEQ() {

        if (CarUtil.getProIndex() <= 0) {

            if (!CarUtil.mIsNeedSendEQ) {
                stopEQ();
                return;
            }

            powerEQ(true); //power on
            mVolume = MachineConfig.getIntProperty2(SettingProperties.CANBOX_EQ_VOLUME);
            if (mVolume == -1) {
                mVolume = 30;
            }
            setEQVolume(mVolume);

        } else {
            String s = MachineConfig.getProperty(SettingProperties.CANBOX_EQ_VOLUME);
            if (s != null) {
                String[] ss = s.split(",");
                if (ss != null && ss.length > 5) {
                    try {
                        mEQData[0] = Byte.valueOf(ss[0]);
                        mEQData[1] = Byte.valueOf(ss[1]);
                        mEQData[2] = Byte.valueOf(ss[2]);
                        mEQData[3] = Byte.valueOf(ss[3]);
                        mEQData[4] = Byte.valueOf(ss[4]);
                        mEQData[5] = Byte.valueOf(ss[5]);

                        buf = new byte[]{(byte) 0xc7, 0x6, 0, mEQData[3], mEQData[4], mEQData[0], mEQData[1], mEQData[2]};

                        sendDataToCanbox(buf, buf.length);
                        buf = new byte[]{(byte) 0xc4, 0x1, mEQData[5]};
                        sendDataToCanbox(buf, buf.length);

                    } catch (Exception e) {

                    }
                }
            }
        }

        //		mHandler.removeMessages(REPEAT_SEND_EQ);
        //		mHandler.sendEmptyMessageDelayed(REPEAT_SEND_EQ, 300);
    }

    private void stopEQ() {
        //		mHandler.removeMessages(REPEAT_SEND_EQ);
        powerEQ(false);
    }

    public void startConnect() {
        super.startConnect();
        startEQ();
    }

    private int mVolume = 30;

    public void stopConnect() {
        stopEQ();
        super.stopConnect();
    }

    public int getUpdateTime() {
        return 60000;
    }

    private void returnDriveData(byte[] buf) {
        if (mRequestDriveData > 0) {
            boolean update = true;
            if (buf[0] == 0x24) {
                if ((buf[2] & 0x2) == 0) {
                    mDriveData[10] = 3;
                } else {
                    if ((buf[2] & 0x1) != 0) {
                        mDriveData[10] = 1;
                    } else {
                        mDriveData[10] = 4;
                    }
                }

                if ((buf[2] & 0x4) != 0) {
                    mDriveData[14] = 1;
                } else {
                    mDriveData[14] = 0;
                }
            } else {
                update = false;
            }
            if (update) {
                returnDriveData();
            }
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
            if (len > 40) {
                len = 40;
            }
            byte[] data = new byte[len];

            data[0] = (byte) 0xcb;
            data[1] = (byte) (len - 2);
            data[2] = 0x10;
            data[3] = index;
            //				if ((i % 2) == 0) {
            //					data[3 + i] = n[i + (n.length - num_len)  + 1];
            //				} else {
            //					data[3 + i] = n[i + (n.length - num_len)  - 1];
            //				}
            System.arraycopy(n, 0 + (n.length - num_len), data, 4, num_len);
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("CarHondaDASimple", "sendId3" + e);
        }
    }

    public void setSongName(String s) {
        sendId3((byte) 0x1, s);
    }


    byte[] mEQData = new byte[]{10, 10, 10, 7, 7, 30};

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            int volume = 36;
            if (CarUtil.getModelId() == 1) { //ix45
                volume = 46;
            }
            ret = (volume << 16) | (21 << 8) | 21;
            returnEQData();
        } else {
            byte[] buf;// = new byte[] { (byte) 0xc7, 0x6, 0, 0, 0, 0, 0, 0 };
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    mEQData[0] = (byte) data;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    mEQData[1] = (byte) data;
                    break;
                case EQ_CMD_SET_LOW:
                    mEQData[2] = (byte) data;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    mEQData[3] = (byte) data;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    mEQData[4] = (byte) data;
                    break;
                case EQ_CMD_SET_VOLUME:
                    mEQData[5] = (byte) data;
                    break;
                default:
                    return 0;
            }

            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                case EQ_CMD_SET_MIDDLE:
                case EQ_CMD_SET_LOW:
                case EQ_CMD_SET_ZONE_FR:
                case EQ_CMD_SET_ZONE_LR:
                    buf = new byte[]{(byte) 0xc7, 0x6, 0, mEQData[3], mEQData[4], mEQData[0], mEQData[1], mEQData[2]};
                    break;
                case EQ_CMD_SET_VOLUME:
                    buf = new byte[]{(byte) 0xc4, 0x1, mEQData[5]};
                    break;
                default:
                    return 0;
            }

            String value = mEQData[0] + "," + mEQData[1] + "," + mEQData[2] + "," + mEQData[3] + "," + mEQData[4] + "," + mEQData[5];
            MachineConfig.setProperty(SettingProperties.CANBOX_EQ_VOLUME, value);
            sendDataToCanbox(buf, buf.length);
            returnEQData();
        }
        return ret;
    }

    private void returnEQData() {
        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
    }
}
