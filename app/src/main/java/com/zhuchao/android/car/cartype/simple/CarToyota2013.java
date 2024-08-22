package com.zhuchao.android.car.cartype.simple;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.utils.AppConfig;
import com.common.utils.MachineConfig;
import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.OSProManager;

import java.util.ArrayList;
import java.util.Locale;


public class CarToyota2013 extends Canbox {
    public CarToyota2013() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });

        if (CarUtil.getCarEQ() == 1) {
            CarUtil.mIsNeedSendEQ = true;
            CarUtil.setMcuEQZoneUsed(1);
        } else {
            CarUtil.setMcuEQZoneUsed(0);
        }

        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0xca, 0x01, 0};
        switch (CarUtil.getModelId()) {
            case 9:
            case 25:
                cmd[2] = 0;
                break;
            case 0:
                cmd[2] = 1;
                break;
            case 12:
                cmd[2] = 2;
                break;
            case 67:
                cmd[2] = 3;
                break;
            case 34:
                cmd[2] = 4;
                break;
            case 71:
                cmd[2] = 5;
                break;
            case 47:
                cmd[2] = 6;
                break;
            case 59:
                cmd[2] = 7;
                break;
            default:
                return null;
        }
        return cmd;
    }

    @Override
    public void setContext(Context c) {
        // TODO Auto-generated method stub
        super.setContext(c);
        mHandlerSendEQ.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mContext != null) {
                    byte[] data = new byte[]{(byte) 0xff, 0x1};
                    Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
                    i.putExtra("buf", data);
                    mContext.sendBroadcast(i);
                }
            }
        }, 1500);

    }

    public void startConnect() {// default is simple box
        super.startConnect();
        if (CarUtil.mIsNeedSendEQ) {
            sendEQ((byte) 0x8, (byte) 1);
        }
        mHandlerSendEQ.removeMessages(0);
        if (CarUtil.mIsNeedSendEQ) {
            // int volume = SettingProperties.getIntProperty2(mContext,
            // SettingProperties.CANBOX_EQ_VOLUME);
            // if (volume == -1) {
            // volume = 45;
            // }
            // sendEQ((byte) 0xa, (byte)0);//unmute
            // setEQVolume(volume);
            mResetVolume = true;
            mHandlerSendEQ.sendEmptyMessageDelayed(0, 800);
        } else {
            if (CarUtil.getCarEQ() == 0) {
                sendEQ((byte) 0x8, (byte) 1);
            }
        }

        if (CarUtil.getCarType() != 0) {
            byte[] data = new byte[]{
                    (byte) 0xCA, 0x1, (byte) (CarUtil.getCarType() - 1)
            };
            sendDataToCanbox(data, data.length);
        }
        udpateLang();
        requestBTCallLog();

    }

    private void requestBTCallLog() {
        if (mContext != null) {
            byte[] data = new byte[]{(byte) 0xff, 0x1};
            Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
            i.putExtra("buf", data);
            mContext.sendBroadcast(i);
        }
    }

    public void stopConnect() {// default is simple box
        mHandlerSendEQ.removeMessages(0);
        if (CarUtil.mIsNeedSendEQ) {
            sendEQ((byte) 0x8, (byte) 0);
        }
        super.stopConnect();
    }

    private void startEQ() {

    }


    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.KEY_AM}, {0x2, MyCmd.Keycode.KEY_FM}, {0x3, MyCmd.Keycode.DVD}, {0x4, MyCmd.Keycode.AUX_IN}, {0x5, MyCmd.Keycode.AUDIO}, {0x6, MyCmd.Keycode.AUDIO},
            {0x7, MyCmd.Keycode.BT_MUSIC}, {0x8, MyCmd.Keycode.KEY_TV}, {0x11, MyCmd.Keycode.BT_DIAL}, {0x12, MyCmd.Keycode.BT_HANG}, {0x13, MyCmd.Keycode.PREVIOUS}, {0x14, MyCmd.Keycode.NEXT},
    };

    private void parsePannelKey(byte[] data, int len) {
        if (data[2] >= 0x21 && data[2] <= 0x25) {
            doCallLogList(data[2] - 0x21);
        } else {
            byte key = 0;
            for (int i = 0; i < KEYS_WHEEL.length; ++i) {
                if (KEYS_WHEEL[i][0] == data[2]) {
                    key = KEYS_WHEEL[i][1];
                    break;
                }
            }

            if (key != 0) {
                doKey(key, 1);
                doKey(key, 0);
            }

        }
    }

    private byte mKey = 0;

    private void parseWheelKey(byte[] data, int len) {


        if (doKeyStudy(data[2], data[3])) {
            return;
        }

        switch (data[2]) {
            case 0x0:
                doKey(0, 0);
                break;
            case 0x1:
            case (byte) 0x82:
                doKey(AK_KEYPAD_VOLUME_A, data[3]); // vol+
                break;
            case (byte) 0x81:
            case 0x2:
                doKey(AK_KEYPAD_VOLUME_D, data[3]);// vol-
                break;
            case (byte) 0x83:
            case 0x13:
            case 0x4:
                doKey(KEY_PREVIOUSSONG, data[3]);
                break;
            case (byte) 0x84:
            case 0x3:
            case 0x14:
                doKey(KEY_NEXTSONG, data[3]);
                break;
            case 0x8:
                doKey(MyCmd.Keycode.KEY_MIC, data[3]);
                break;
            case 0x9:
                doKey(KEY_BT, data[3]);// mute
                break;
            case 0x7:
            case (byte) 0x88:
                mKey = data[2];
                doKey(KEY_MODE, data[3]);
                break;
            case 0xa: // HANGUP
            {
                doKey(KEY_BT_HANG, data[3]);
            }
            break;
            case (byte) 0x85: {
                doKey(MyCmd.Keycode.KEY_TURN_D, data[3]);
            }
            break;
            case (byte) 0x15: {
                doKey(KEY_BACK, data[3]);
            }
            break;
            case (byte) 0x16: {
                doKey(KEY_PLAYPAUSE, data[3]);
            }
            break;
            case (byte) 0x86: {
                doKey(MyCmd.Keycode.KEY_TURN_A, data[3]);
            }
            break;
            case (byte) 0x87: {
                doKey(KEY_POWER, data[3]);// mute
            }
            break;
        }
    }


    public int getLongKey(int key) {
        int ret = 0;
        if (key == MyCmd.Keycode.MODLE) {
            if (mKey == 7) {
                ret = MyCmd.Keycode.MUTE;
            }
        } else {
            ret = super.getLongKey(key);
        }
        return ret;
    }

    private void parseACInfo(byte[] data, int len) {
        if ((data[6] & 0x1) == 0) {
            if (data[4] >= 0x1f) {
                if (data[4] >= 0x20 && data[4] <= 0x23) {
                    data[4] = (byte) ((16f + (0.5f * (data[4] - 0x20))) * 2);
                } else {
                    data[4] = (byte) 0xff;
                }

            } else if (data[4] > 0) {
                data[4] = (byte) ((17.5f + (0.5f * data[4])) * 2);
            }
            if (data[5] >= 0x1f) {
                if (data[5] >= 0x20 && data[5] <= 0x23) {
                    data[5] = (byte) ((16f + (0.5f * (data[5] - 0x20))) * 2);
                } else {
                    data[5] = (byte) 0xff;
                }
            } else if (data[5] > 0) {
                data[5] = (byte) ((17.5f + (0.5f * data[5])) * 2);
            }
        } else {

        }

        byte[] airData = new byte[8];
        airData[0] = (byte) (data[2] & 0xff);
        airData[0] |= (byte) (((data[6] & 0x80) >> 6) | ((data[6] & 0x40) >> 6));

        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        airData[4] = (byte) ((data[6] & 0x08) >> 1);
        airData[4] |= (byte) (((data[7] & 0x0c) >> 2) | ((data[7] & 0x3) << 4));

        airData[5] = (byte) (data[6] & 0x01);

        boolean airControl = (data[3] & 0x10) != 0;
        /* (data[2] & 0x80) != 0 && */

        Handler handler = getHandler("CanService");
        if (airControl && null != handler) {
            handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
        }
    }

    private boolean mResetVolume = true;
    Handler mHandlerSendEQ = new Handler() {
        public void handleMessage(Message msg) {

            int volume = MachineConfig.getIntProperty2(SettingProperties.CANBOX_EQ_VOLUME);
            if (volume == -1) {
                volume = 45;
            }
            if (mResetVolume) {
                sendEQ((byte) 0x8, (byte) 1);
                sendEQ((byte) 0xa, (byte) 0);// unmute
                mResetVolume = false;
            }
            setEQVolume(volume);

            mHandlerSendEQ.removeMessages(0);
            mHandlerSendEQ.sendEmptyMessageDelayed(0, 1000);
        }
    };

    private byte[] mAcData = new byte[1];

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data, len);
            }
            break;
            case 0x2f: {
                parsePannelKey(data, len);
            }
            break;
            case 0x28: {
                // mAcData = new byte[len];
                if (!isBufEqual(data, mAcData)) {
                    mAcData = data.clone();
                    parseACInfo(data, len);
                }

            }
            break;
            case 0x1D: // Radar font
            {
                if (m360Exit) {
                    return;
                }
                boolean show = false;
                for (int i = 4; i < 8; ++i) {
                    switch (data[2 + i - 4]) {
                        case 0:
                            if (mRadar[i] != 0) {
                                show = true;
                                mRadar[i] = 0;
                            }
                            break;
                        case 1:
                            if (mRadar[i] != 1) {
                                show = true;
                                mRadar[i] = 1;
                            }
                            break;
                        case 2:
                            if (mRadar[i] != 4) {
                                show = true;
                                mRadar[i] = 4;
                            }
                            break;
                        case 3:
                            if (mRadar[i] != 6) {
                                show = true;
                                mRadar[i] = 6;
                            }
                            break;
                        case 4:
                            if (mRadar[i] != 0xa) {
                                show = true;
                                mRadar[i] = 0xa;
                            }
                            break;
                    }
                }
                boolean zero = Util.isZero(mRadar);
                if (show && !zero) {
                    RadarManager.start(mContext);
                    checkHideRadarEx(5000);
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (show && null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                }
            }
            break;
            case 0x1E: // Radar back
            {
                if (m360Exit) {
                    return;
                }
                for (int i = 0; i < 4; ++i) {
                    switch (data[2 + i]) {
                        case 0:
                            mRadar[i] = 0;
                            break;
                        case 1:
                            mRadar[i] = 1;
                            break;
                        case 2:
                            mRadar[i] = 4;
                            break;
                        case 3:
                            mRadar[i] = 6;
                            break;
                        case 4:
                            mRadar[i] = 0xa;
                            break;
                    }
                }

                if ((data[6] & 0x20) != 0) {
                    boolean zero = Util.isZero(mRadar);
                    if (!zero) {

                        if (CarUtil.getCarType2() == 1 || CarUtil.getCarType2() == 2) {
                            if (!mIsOpenCamera) {
                                do360CameraSwitch(1);
                                mIsOpenCamera = true;
                            }
                        }

                        if (CarUtil.getCarType2() != 1) {
                            RadarManager.start(mContext);
                            checkHideRadarEx(5000);
                        }
                    }
                } else {
                    mIsOpenCamera = false;
                    RadarManager.stop();
                }

                // RadarManager.start(mContext);
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                }

                sendCanboxInfo(data);
            }
            break;
            case 0x24: {
                int door = (data[2]);
                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((data[3] & 0x80) >> 2));

                if ((data[3] & 0x60) == 0x20) {
                    door |= 0x80;
                } else if ((data[3] & 0x60) == 0x60) {

                    door |= 0x40;
                }

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }
                sendCanboxInfo(data);
            }
            break;
            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    short a = (short) ((data[2] & 0xff) | (((data[3] & 0x0f)) << 8));

                    if ((data[3] & 0x4) != 0) {
                        a = (short) ((data[2] & 0xff) | (((data[3] & 0x0f) | 0xf0) << 8));
                    }

                    int angle = ((a * 3000) / 380);

                    if (angle > -50 && angle < 50) {
                        angle = 50;
                    }

                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
                }
            }
            break;

            case 0x30:
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            case 0x31:
                returnEQData(data);
                break;
            case 0x41: {
                if (data[2] == 3) {
                    updateOutDoorTemp(data[9]);
                }
            }
            sendCanboxInfo(data);
            break;
            case 0x32:
                if (CarUtil.getCarEQ() == 0) {
                    if ((data[2] & 0x1) == 0) {
                        if (CarUtil.mIsNeedSendEQ) {

                            CarUtil.mIsNeedSendEQ = false;
                            CarUtil.setMcuEQZoneUsed(0);

                            mHandlerSendEQ.removeMessages(0);
                        }
                    } else {
                        if (!CarUtil.mIsNeedSendEQ) {
                            CarUtil.mIsNeedSendEQ = true;
                            CarUtil.setMcuEQZoneUsed(1);

                            mResetVolume = true;
                            mHandlerSendEQ.removeMessages(0);
                            mHandlerSendEQ.sendEmptyMessageDelayed(0, 1000);
                        }
                    }
                }

                if ((data[2] & 0x4) != 0) {
                    m360Exit = true;
                    mIsOpenCamera = false;
                    RadarManager.stop();
                } else {
                    m360Exit = false;
                }

                if (m360Exit) {
                    if ((data[2] & 0x8) != 0) {
                        OSProManager.simulationReverse((byte) 1);
                    } else {
                        OSProManager.simulationReverse((byte) 0);
                    }
                }

                mTNGA360 = (data[2] & 0x10) != 0;
                break;
            case 0x26:
            case 0x1A:
            case 0x27:
            case 0x21:
            case 0x22:
            case 0x23:
            case 0x25:
            case 0x1f:
                if (data[0] == 0x25) {
                    mData0x25 = data;
                } else if (data[0] == 0x1f) {
                    mData0x1f = data;
                } else if (data[0] == 0x21) {
                    mData0x21 = data;
                } else if (data[0] == 0x22) {
                    mData0x22 = data;
                } else if (data[0] == 0x23) {
                    mData0x23 = data;
                }
                sendCanboxInfo(data);
                break;
        }

        returnDriveData(data);
    }

    public void hideRadar() {
        mIsOpenCamera = false;
    }

    private boolean mIsOpenCamera = false;

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

    private boolean m360Exit = false;
    private boolean mTNGA360 = false;

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

    private int mDoorStatus;
    private byte[] mData0x21;
    private byte[] mData0x22;
    private byte[] mData0x23;
    private byte[] mData0x25;
    private byte[] mData0x1f;

    public void sendDataToCanbox(byte[] data, int len) {
        if ((data[0] & 0xff) == 0xff) {
            if (data[1] == 0x25) {
                sendCanboxInfo(mData0x25);
            } else if (data[1] == 0x1f) {
                sendCanboxInfo(mData0x1f);
            } else if (data[1] == 0x21) {
                sendCanboxInfo(mData0x21);
            } else if (data[1] == 0x22) {
                sendCanboxInfo(mData0x22);
            } else if (data[1] == 0x23) {
                sendCanboxInfo(mData0x23);
            }
        } else {
            super.sendDataToCanbox(data, len);
        }

    }

    // public void setReverseRadaVol(byte param){
    // byte []data = new byte[]{(byte)0xc6, 0x2, 0x0, param};
    // sendDataToCanbox(data, data.length);
    // }
    // public void setParkCarMode(byte param){
    // byte []data = new byte[]{(byte)0xc6, 0x2, 0x2, param};
    // sendDataToCanbox(data, data.length);
    // }
    // public void requestInfo(byte param){
    // byte []data = new byte[]{(byte)0x90, 0x2, param, 0};
    // sendDataToCanbox(data, data.length);
    // }

    private void sendEQ(byte cmd, byte param) {
        byte[] data = new byte[]{(byte) 0x84, 0x2, cmd, param};
        sendDataToCanbox(data, data.length);
    }

    private void setEQVolume(int volume) {

        byte[] data = new byte[]{(byte) 0x84, 0x2, 0x07, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    private int mVolume = -1;

    public void setVolume(int volume) {
        if (volume == 0) {
            sendEQ((byte) 0xa, (byte) 1);
        } else if (mVolume == 0) {
            sendEQ((byte) 0xa, (byte) 0);
        }

        mVolume = volume;


        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    byte[] mEqData = new byte[6];

    public void sendEqToCanbox(byte[] eq) {
        if (eq != null && eq.length >= 11) {

            byte[] data = new byte[]{(byte) 0x84, 0x2, 0, 0};

            byte[] eq2 = new byte[6];

            eq2[0] = eq[0];
            eq2[1] = eq[1];

            eq2[3] = (byte) (eq[2] + eq[3] + eq[4]);
            eq2[4] = (byte) (eq[5] + eq[6] + eq[7]);
            eq2[5] = (byte) (eq[8] + eq[9] + eq[10]);
            switch (eq[12]) {
                case 2:
                    eq2[2] = 3;

                    break;
                case 3:
                    eq2[2] = 1;

                    break;
                case 5:
                    eq2[2] = 0;

                    break;
                case 4:
                    eq2[2] = 2;

                    break;
                default:
                    eq2[2] = 4;
                    break;
            }

            byte param;

            if (eq2[0] != mEqData[0]) {
                param = (eq2[0]);
                sendEQ((byte) 1, param);
                Util.doSleep(5);
            }
            if (eq2[1] != mEqData[1]) {
                param = (eq2[1]);
                sendEQ((byte) 2, param);
                Util.doSleep(5);
            }
            // if (eq2[2] != mEqData[2]) {
            // param = eq2[2];
            // sendEQ((byte) 3, param);
            // Util.doSleep(5);
            // }

            if (eq2[3] != mEqData[3]) {
                param = (byte) ((eq2[3] * 11) / 45);
                sendEQ((byte) 4, param);
                Util.doSleep(5);
            }
            if (eq2[4] != mEqData[4]) {
                param = (byte) ((eq2[4] * 11) / 45);
                sendEQ((byte) 6, param);
                Util.doSleep(5);
            }
            if (eq2[5] != mEqData[5]) {
                param = (byte) ((eq2[5] * 11) / 45);
                sendEQ((byte) 5, param);
                Util.doSleep(5);
            }
            mEqData = eq2;
        }
    }

    public void udpateLang() {
        int lang = -1;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("en")) {
                if ("US".equals(Locale.getDefault().getCountry())) {
                    lang = 2;
                } else if ("GB".equals(Locale.getDefault().getCountry())) {
                    lang = 8;
                } else {
                    lang = 1;
                }
            } else if (locale.equals("zh")) {
                lang = 0;
            } else if (locale.equals("ru")) {
                lang = 9;
            } else if (locale.equals("tr")) {
                lang = 7;
            } else if (locale.equals("fr")) {
                lang = 4;
            } else if (locale.equals("de")) {
                lang = 6;
            } else if (locale.equals("es")) {
                lang = 3;
            }

        }
        if (lang != -1) {
            byte[] buf = {(byte) 0x83, 0x2, 0x24, (byte) lang};
            sendDataToCanbox(buf, buf.length);
        }
    }


    private final static int DEFAULT_SCREEN_W = 1024;
    private final static int DEFAULT_SCREEN_H = 600;

    private final static int BUTTON_AUTO_W = 160;
    private final static int BUTTON_AUTO_H = 120;

    private int mButtonW = -1;
    private int mButtonH = -1;


    private final static int TNGA_TOUCH_WIDTH = 1024;
    private final static int TNGA_TOUCH_HEIGHT = TNGA_TOUCH_WIDTH;

    public void touchInReverse(int x, int y, int w, int h) {
        //if (!m360Exit) {
        //	return;
        //}
        if (mButtonW == -1) {
            if (w > DEFAULT_SCREEN_W) {
                mButtonW = ((w * 100 / DEFAULT_SCREEN_W) * BUTTON_AUTO_W) / 100;
            } else {
                mButtonW = BUTTON_AUTO_W;
            }

            if (h > DEFAULT_SCREEN_H) {
                mButtonH = ((h * 100 / DEFAULT_SCREEN_H) * BUTTON_AUTO_H) / 100;
            } else {
                mButtonH = BUTTON_AUTO_H;
            }
        }

        if (mTNGA360) {
            x = (x * TNGA_TOUCH_WIDTH) / w;
            y = (y * TNGA_TOUCH_WIDTH) / h;

            byte[] buf = new byte[]{
                    (byte) 0x85, 0x5, (byte) ((x & 0xff00) >> 8), (byte) (x & 0xff), (byte) ((y & 0xff00) >> 8), (byte) (y & 0xff), 0
            };

            sendDataToCanbox(buf, buf.length);


            Log.d(TAG, "touchInReverse" + x + ":" + y);
        } else {
            int button = 0;
            if (OSProManager.mReverse == 1 && OSProManager.mSimulationReverse == 0) {
                if (CarUtil.getKeyType() == 0) {

                    if (x > (w - mButtonW) && y > (h - mButtonH)) {
                        button = 3;
                    } else if ((x < (w - mButtonW)) && x > ((w / 2 - mButtonW)) && y > (h - mButtonH)) {
                        button = 7;
                    } else if (x < mButtonW && y > (h - mButtonH)) {
                        button = 4;
                    }
                } else if (CarUtil.getKeyType() == 1) {

                    if (x < mButtonW && y > (h - mButtonH)) {
                        button = 4;
                    } else if (x > (w - mButtonW) && (y < mButtonH)) {
                        button = 6;
                    } else if (x > (w - mButtonW) && y > (h - mButtonH)) {
                        button = 3;
                    }
                } else {

                    if (x < mButtonW && y > (h - mButtonH)) {
                        button = 0x2a01;
                    } else if (x > (w - mButtonW) && y > (h - mButtonH)) {
                        button = 0x2901;
                    }
                }
            } else {

                if (CarUtil.getKeyType() == 0) {
                    if (x < mButtonW && y > (h - mButtonH)) {
                        button = 4;
                    } else if ((x < (w - mButtonW)) && x > ((w / 2 - mButtonW)) && y > (h - mButtonH)) {
                        button = 7;
                    }
                } else if (CarUtil.getKeyType() == 1) {

                    if (x < mButtonW && y < mButtonH) {
                        button = 1;
                    } else if ((x > (w - mButtonW)) && y > (h - mButtonH)) {
                        button = 2;
                    }

                } else {

                    if (x < mButtonW && y < mButtonH) {
                        button = 1;
                    } else if ((x > (w - mButtonW)) && y > (h - mButtonH)) {
                        button = 2;
                    }

                }

            }

            // Log.d("eed", "!!!!!!!!!!!!:"+button);
            if (button != 0) {
                byte[] buf;
                if ((button & 0xff00) == 0) {
                    buf = new byte[]{(byte) 0x83, 0x2, 0x21, (byte) button};
                } else {
                    buf = new byte[]{
                            (byte) 0x83, 0x2, (byte) ((button & 0xff00) >> 8), (byte) (button & 0xff)
                    };
                }
                sendDataToCanbox(buf, buf.length);
            }
        }
    }

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
                s2 = 0x10;
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

        // if (MyCmd.SOURCE_DVD == source) {
        // data = new byte[] { (byte) 0xc0, 0x8, s, s2, 0,
        // (byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec };
        //
        // } else {
        data = new byte[]{
                (byte) 0xc0, 0x8, s, s2, (byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), 0, h, min, sec
        };
        // }

        if (mPhoneStatus < HFP_INFO_CALLED) {

            sendDataToCanbox(data, data.length);
        }
    }

    private int mPreSource = -1;

    public void setMediaSrc(int source) {// default is simple box
        byte s;
        byte mediaType = 0;
        switch (source) {
            case 0:
                s = 1;
                // mediaType = 1;
                break;
            case MyCmd.SOURCE_DVD:
                s = 0x2;
                mediaType = 0x10;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 8;
                mediaType = 0x10;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                mediaType = 0x10;
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
        if (s != 8 && mPreSource == 8) {
            clearID3();
        }
        mPreSource = s;
        data = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0};
        if (mediaType != 0) {
            sendDataToCanbox(data, data.length);
        }
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        // setMediaSrc(0);
        // if (b[0] != 0x10) {
        // b[0] += 1;
        // }
        if (b[3] >= 0 && b[3] <= 30) {
            b[3]++;
        } else {
            b[3] = 0;
        }
        data = new byte[]{
                (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], b[3], 0, 0
        };
        sendDataToCanbox(data, data.length);
    }

    byte[] data;

    private int mPhoneStatus = -1;
    private String mPhoneName = null;
    private String mPhoneNum = null;

    boolean isStringEqual(String s1, String s2) {
        return (s1 == null && s2 == null) || (s1 != null && s1.equals(s2));
    }

    private int mBTStatusSend = -1;

    public void setPhoneEx(int status, String num, String name) {
        Log.d(TAG, "setPhoneEx:" + num);
        if (mPhoneStatus == status && isStringEqual(num, mPhoneNum) && isStringEqual(name, mPhoneName)) {
            return;
        }
        mPhoneName = name;
        mPhoneNum = num;

        if (mPhoneStatus < HFP_INFO_CONNECTED && status == HFP_INFO_CONNECTED) {
            requestBTCallLog();
        }


        int s = 0;
        switch (status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:
            case HFP_INFO_CONNECTING:
                s = 0;
                break;
            case HFP_INFO_CONNECTED:
                //if (mPhoneStatus > HFP_INFO_CONNECTED) {
                s = 4;
                //}
                break;
            case HFP_INFO_CALLED:
                s = 3;
                break;
            case HFP_INFO_INCOMING:
                s = 1;
                break;
            case HFP_INFO_CALLING:
                s = 2;
                break;
        }

        mPhoneStatus = status;

        byte[] data2;

        if (num == null) {
            num = " ";
        }

        if (mBTStatusSend != s) {
            mBTStatusSend = s;
            data2 = new byte[]{
                    (byte) 0xc0, 0x8, 0x5, 0x40, (byte) s, 0, 0, 0, 0, 0
            };
            sendDataToCanbox(data2, data2.length);

            if (status >= HFP_INFO_CALLED && status <= HFP_INFO_CALLING) {
                sendId3((byte) 1, num);
                Util.doSleep(5);
                sendId3((byte) 2, name);
                Util.doSleep(5);
            } else {
                sendId3((byte) 1, "");
                Util.doSleep(5);
                sendId3((byte) 2, "");
                Util.doSleep(5);
            }

            if (s == 4) {
                if (data != null) {
                    sendDataToCanbox(data, data.length);
                }
            }
        } else {
            Log.d(TAG, "BT send status same:");
        }

    }

    public void sendId3(byte index, String num) {

        try {
            if (num == null) {
                num = "";
            }
            byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

            int num_len = n.length;
            byte[] data;
            if (num_len == 0) {
                data = new byte[4];

                data[0] = (byte) 0xc8;
                data[1] = 2;
                data[2] = 0x10;
                data[3] = index;
            } else {
                if (num_len > 2) {
                    if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
                        num_len -= 2;
                    }
                }
                int len = 36;
                // if (len > 31) {
                // len = 31;
                // }
                data = new byte[len];

                data[0] = (byte) 0xc8;
                data[1] = (byte) (len - 2);
                data[2] = 0x10;
                data[3] = index;
                for (int i = 0; i < num_len && i < (len - 4); ++i) {
                    data[4 + i] = n[i + (n.length - num_len)];
                }
            }
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("Nissan2013Simple", "sendId3" + e);
        }
    }

    String mName = null;
    String mArtist = null;
    String mAlbum = null;

    // public void setPhone(int status, String num) {
    // sendId3((byte)0x1, num);
    // }

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

    private void doCallLogList(int index) {
        if (list != null && index < list.size()) {
            String[] ss = list.get(index).split(";");
            try {

                Intent it = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ss[1]));
                //				it.setClassName("com.android.car.bt", "com.android.car.bt.ATBluetoothActivity");
                //				mContext.startActivity(it);

                //				Intent it = new Intent(Intent.ACTION_VIEW);
                it.setClassName("com.android.car.bt", "com.android.car.bt.ATBluetoothActivity");
                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(it);

            } catch (Exception e) {
                //				Log.e(TAG, e.getMessage());
            }
        }

    }

    ArrayList<String> list = null;

    public void updateCallLog(Object obj) {
        try {
            if (obj != null) {
                list = (ArrayList<String>) obj;
                for (int i = 0; i < list.size(); ++i) {

                    String[] ss = list.get(i).split(";");

                    int index = Integer.valueOf(ss[0]);

                    byte[] n = getBytesUnicodeLittleEndian(ss[1]); //del 0xff 0xfe
                    int len = n.length;
                    if (len > 0x40) {
                        len = 0x40;
                    }

                    int num_len = n.length;

                    if (num_len > 2) {
                        if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
                            num_len -= 2;
                        }
                    }

                    byte[] data = new byte[num_len + 5];
                    data[0] = (byte) 0xc7;
                    data[1] = (byte) (num_len + 3);
                    data[2] = 0x10;
                    data[3] = (byte) (i + 1);
                    data[4] = (byte) index;

                    System.arraycopy(n, 0 + (len - num_len), data, 5, num_len);

                    sendDataToCanbox(data, data.length);
                    Util.doSleep(1);

                }
            }
        } catch (Exception e) {

        }
    }


    private void returnDriveData(byte[] buf) {
        if (mRequestDriveData > 0) {
            boolean update = true;
            int index;
            if (buf[0] == 0x41) {
                switch (buf[2]) {
                    case 0x1: {
                        index = buf[4] & 0xf;
                        if (index == 0) {
                            mDriveData[10] = 3;
                        } else if (index == 8) {
                            mDriveData[10] = 4;
                        } else if (index == 1 || index == 2 || index == 4) {
                            mDriveData[10] = (byte) index;
                        }
                        mDriveData[10] |= (buf[4] & 0x10);


                        mDriveData[14] = (byte) (buf[7] & 0xF8);
                        mDriveData[14] |= (byte) ((buf[6] & 0x80) >> 5);
                        mDriveData[14] |= (byte) ((buf[6] & 0x40) >> 5);
                        mDriveData[14] |= (byte) ((buf[6] & 0x20) >> 5);
                        break;
                    }
                    case 0x2: {
                        mDriveData[1] = buf[15];
                        mDriveData[2] = buf[14];
                        mDriveData[5] = buf[5];
                        mDriveData[6] = buf[4];
                        mDriveData[7] = buf[3];
                        mDriveData[8] = buf[7];
                        mDriveData[9] = buf[6];
                    }
                    break;
                    case 0x3: {
                        mDriveData[3] = buf[4];
                        mDriveData[4] = buf[3];
                    }
                    break;
                    case 0x7: {
                        switch (buf[2]) {
                            case 0x1:
                                mDriveData[8] = buf[4];
                                mDriveData[9] = buf[3];
                                break;
                            case 0x5:
                                mDriveData[5] = buf[5];
                                mDriveData[6] = buf[4];
                                mDriveData[7] = buf[3];
                        }
                    }
                    break;
                    case 0x40:
                        mDriveData[0] = buf[3];
                        break;
                    case 0x24:
                        if ((buf[3] & 0x2) == 0) {
                            mDriveData[10] = 3;
                        } else {
                            if ((buf[3] & 0x1) != 0) {
                                mDriveData[10] = 1;
                            } else {
                                mDriveData[10] = 4;
                            }
                        }

                        if ((buf[3] & 0x4) != 0) {
                            mDriveData[14] = 1;
                        } else {
                            mDriveData[14] = 0;
                        }
                        break;
                    default:
                        update = false;
                        break;
                }
            }
            if (update) {
                returnDriveData();
            }
        }
    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (63 << 16) | (15 << 8) | 11;

            byte[] buf = new byte[]{(byte) 0x90, 0x2, (byte) 0x31, 0};
            sendDataToCanbox(buf, buf.length);
        } else {
            byte[] buf = new byte[]{(byte) 0x84, 0x2, 0x0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    buf[2] = 5;
                    buf[3] += 2;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    buf[2] = 6;
                    buf[3] += 2;
                    break;
                case EQ_CMD_SET_LOW:
                    buf[2] = 4;
                    buf[3] += 2;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    buf[2] = 1;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    buf[2] = 2;
                    break;
                case EQ_CMD_SET_VOLUME:
                    buf[2] = 7;
                    break;
                default:
                    return 0;
            }

            sendDataToCanbox(buf, buf.length);
        }
        return ret;
    }

    private void returnEQData(byte[] buf) {
        byte[] data = new byte[6];

        data[0] = (byte) (buf[3] & 0x0f);
        data[1] = (byte) ((buf[4] & 0xf0) >> 4);
        data[2] = (byte) ((buf[3] & 0xf0) >> 4);
        data[3] = (byte) ((buf[2] & 0xf0) >> 4);
        data[4] = (byte) (buf[2] & 0x0f);

        data[5] = buf[5];

        data[0] -= 2;
        data[1] -= 2;
        data[2] -= 2;

        super.returnEQData(EQ_CMD_SET_ALL_DATA, data);
    }
}
