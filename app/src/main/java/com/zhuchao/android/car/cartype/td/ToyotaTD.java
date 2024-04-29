package com.zhuchao.android.car.cartype.td;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;


public class ToyotaTD extends Canbox {
    public ToyotaTD() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
        if (CarUtil.getCarEQ() == 1) {
            CarUtil.mIsNeedSendEQ = true;
        }
        mWindMaxStep = 0x7;
    }

    private byte mKey;

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
                doKey(KEY_BT_DIAL, data[3]);// mute
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
            case (byte) 0xb1: {
                doKey(MyCmd.Keycode.VIDEO, data[3]);// mute
            }
            break;
            case (byte) 0xb2: {
                doKey(MyCmd.Keycode.AUDIO, data[3]);// mute
            }
            break;
            case (byte) 0xb3: {
                doKey(MyCmd.Keycode.KEY_FM, data[3]);// mute
            }
            break;
            case (byte) 0xb4: {
                doKey(MyCmd.Keycode.KEY_AM, data[3]);// mute
            }
            break;
            case (byte) 0xb5: {
                doKey(MyCmd.Keycode.PLAY_PAUSE, data[3]);// mute
            }
            break;
            case (byte) 0xb6: {
                doKey(MyCmd.Keycode.MUTE, data[3]);// mute
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

    byte[] mAirData;

    private void parseACInfoHeat(byte[] data, int len) {

        mAirData[4] &= ~(0x33);
        mAirData[4] |= (((data[2] & 0x3) << 4) | ((data[3] & 0x3) << 0));

        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, mAirData));
        }
    }

    private boolean isValidTemp(int t) {
        return t >= 0 && t != 0x1e && t <= 0x1f;
    }

    @SuppressLint("DefaultLocale")
    private void parseACInfo(byte[] data, int len) {
        if (!isValidTemp((data[4] & 0xff)) || !isValidTemp((data[5] & 0xff))) { // invalid
            // data
            return;
        }

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
        airData[0] |= (byte) (/*((data[6] & 0x80) >> 6) | */((data[6] & 0x40) >> 6));

        airData[1] = (byte) (data[3] & 0xef);

        if ((data[2] & 0x80) != 0) {
            airData[2] = (byte) (data[4] & 0xff);
            airData[3] = (byte) (data[5] & 0xff);
        } else {
            airData[2] = airData[3] = (byte) 0xfa;
        }

        airData[4] = (byte) ((data[6] & 0x08) >> 1);
        airData[4] |= (byte) (((data[6] & 0x4) << 1));

        airData[5] = (byte) (data[6] & 0x01);

        //		airData[0] &= ~0x80; //test
        Handler handler = getHandler("CanService");
        if (null != handler && !Util.isBufEquals(airData, mAirData)) {
            // mAirData = airData;
            mAirData = airData.clone();

            //			if(!AirManager.mSetByUI){
            handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
            //			} else {
            //				AirManager.mSetByUI = false;
            //			}

        }


        if (mOutDoorTemp != data[7]) {
            mOutDoorTemp = data[7];
            int temp = (mOutDoorTemp & 0xff);
            temp = (temp * 10) / 2;
            temp -= 400;

            String s = String.format("%d.%d%s", temp / 10, (temp % 10) >= 0 ? (temp % 10) : -(temp % 10), mContext.getResources().getString(R.string.temp_unic_centigrade));

            GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
        }
    }

    private byte mOutDoorTemp;

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data, len);
            }
            break;
            case 0x28: {
                // mAcData = new byte[len];
                parseACInfo(data, len);

            }
            break;
            case 0x35: {
                parseACInfoHeat(data, len);
            }
            break;
            case 0x1D: // Radar font
            {
                for (int i = 4; i < 8; ++i) {
                    switch (data[2 + i - 4]) {
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
                // RadarManager.start(mContext);
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
            case 0x1E: // Radar back
            {
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
                        RadarManager.start(mContext);
                        checkHideRadarEx(5000);
                    }
                } else {
                    RadarManager.stop();
                }

                // RadarManager.start(mContext);
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                }

                sendCanboxInfo("com.canboxsetting", data);
            }
            break;
            case 0x24: {
                int door = (data[2] & 0xfc);
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
            }
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
            case 0x41: {
                if (data[2] == 3) {
                }
            }
            sendCanboxInfo("com.canboxsetting", data);
            break;
            case 0x32:
                CarUtil.mIsNeedSendEQ = (data[2] & 0x1) != 0;

                if ((data[2] & 0xc) == 0xc) {
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setClassName("com.car.ui", "com.zhuchao.android.car.frontcamera.FrontCameraActivity");
                    it.putExtra("camera", 1);
                    it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(it);
                }
                break;
            case 0x26:
            case 0x1A:
            case 0x27:
            case 0x21:
            case 0x22:
            case 0x23:
            case 0x25:
            case 0x1f:
            case 0x50:
            case 0x16:
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
                sendCanboxInfo("com.canboxsetting", data);
                break;
            case 0X61:

                sendCanboxInfo("com.canboxsetting", data);
                break;
            case 0x30: {
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
        }
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
                sendCanboxInfo("com.canboxsetting", mData0x25);
            } else if (data[1] == 0x1f) {
                sendCanboxInfo("com.canboxsetting", mData0x1f);
            } else if (data[1] == 0x21) {
                sendCanboxInfo("com.canboxsetting", mData0x21);
            } else if (data[1] == 0x22) {
                sendCanboxInfo("com.canboxsetting", mData0x22);
            } else if (data[1] == 0x23) {
                sendCanboxInfo("com.canboxsetting", mData0x23);
            }
        } else {
            if ((data[0] & 0xff) == 0xe0 && mAirData != null && (data[3] & 0xff) == 0) {
                if ((data[2] & 0xff) == 0x1) {
                    if ((mAirData[0] & 0x80) == 0) {
                        int wind = 0;
                        if ((mAirData[1] & 0x0f) == 0) {
                            wind = 2;
                        } else if ((mAirData[1] & 0x0f) == 1) {
                            wind = 1;
                        }
                        for (int i = 0; i < wind; ++i) {
                            byte[] buf = new byte[]{(byte) 0xe0, 0x02, (byte) 0xa, 0x1};
                            super.sendDataToCanbox(buf, len);
                            Util.doSleep(20);
                            buf[3] = 0x0;
                            super.sendDataToCanbox(buf, len);
                            Util.doSleep(20);
                        }
                    } else {
                        if ((mAirData[4] & 0x8) != 0) {
                            byte[] buf = new byte[]{(byte) 0xe0, 0x02, (byte) 0x2a, 0x1};
                            super.sendDataToCanbox(buf, len);
                            Util.doSleep(20);
                            buf[3] = 0x0;
                            super.sendDataToCanbox(buf, len);
                            Util.doSleep(20);
                        }
                    }
                }
            }
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

    public void setVolume(int volume) {

        byte[] data = new byte[]{(byte) 0x84, 0x2, 0x07, (byte) volume};
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
                param = (byte) ((eq2[0] * 22) / 14);
                sendEQ((byte) 1, param);
                Util.doSleep(5);
            }
            if (eq2[1] != mEqData[1]) {
                param = (byte) ((eq2[1] * 22) / 14);
                sendEQ((byte) 2, param);
                Util.doSleep(5);
            }
            // if (eq2[2] != mEqData[2]) {
            // param = eq2[2];
            // sendEQ((byte) 3, param);
            // Util.doSleep(5);
            // }

            if (eq2[3] != mEqData[3]) {
                param = (byte) ((eq2[3] * 12) / 60);
                sendEQ((byte) 4, param);
                Util.doSleep(5);
            }
            if (eq2[4] != mEqData[4]) {
                param = (byte) ((eq2[4] * 12) / 60);
                sendEQ((byte) 6, param);
                Util.doSleep(5);
            }
            if (eq2[5] != mEqData[5]) {
                param = (byte) ((eq2[5] * 12) / 60);
                sendEQ((byte) 5, param);
                Util.doSleep(5);
            }
            mEqData = eq2;
        }
    }

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
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_RADAR) {
                RadarManager.stop();
            }
        }
    };

}
