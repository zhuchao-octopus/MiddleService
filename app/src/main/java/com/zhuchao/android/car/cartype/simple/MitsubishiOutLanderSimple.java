package com.zhuchao.android.car.cartype.simple;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.common.util.MachineConfig;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.McuManager;


public class MitsubishiOutLanderSimple extends Canbox {

    public MitsubishiOutLanderSimple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
        updateCanboxKeySettings();
    }

    public void setContext(Context c) {
        super.setContext(c);

        int volume = MachineConfig.getIntProperty2(SystemConfig.CANBOX_EQ_VOLUME);
        if (volume == -1) {
            volume = 38;
        }
        setEQVolume(volume);
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG},

            {0x7, KEY_SOURCE}, {0x8, KEY_GPS},

            {0x9, KEY_BT_DIAL}, {0xa, KEY_BT_HANG},

    };

    public void updateCanboxKeySettings() {
        // if (CarUtil.getKeyType()==2) {
        // mKeyPannel = KEYS_PANNEL_GL8;
        // } else if (CarUtil.getKeyType()==1) {
        // mKeyPannel = KEYS_PANNEL_ENVISION_L;
        // } else {
        // mKeyPannel = KEYS_PANNEL_NORMAL;
        // }

        if (CarUtil.getCarEQ() == 1) {
            CarUtil.mIsNeedSendEQ = true;
            CarUtil.setMcuEQZoneUsed(1);
        } else {
            CarUtil.setMcuEQZoneUsed(0);
        }
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

    private final byte[] mAirData = new byte[8];

    private void parseACInfo(byte[] data, int len) {

        byte[] airData = new byte[8];
        airData[0] = (byte) (data[2] & 0xff);
        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);
        // airData[4] = (byte) (data[6] & 0xff);

        airData[0] |= (byte) ((data[6] & 0x40) >> 6);

        if (Util.isBufEquals(mAirData, airData)) {
            return;
        } else {
            Util.byteArrayCopy(mAirData, airData, 0, 0, mAirData.length);
        }

        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
        }
    }

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

    private final static int HIDE_RADAR = 0;
    private final static int UPDATE_EQ = 1;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_RADAR:
                    RadarManager.stop();
                    break;
                case UPDATE_EQ:
                    byte[] buf = new byte[]{(byte) 0x84, 0x02, 0x9, 0x1};
                    sendDataToCanbox(buf, buf.length);
                    mHandler.removeMessages(UPDATE_EQ);
                    mHandler.sendEmptyMessageDelayed(UPDATE_EQ, 1000);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void setMediaSrc(int source) {// default is simple box

    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    private byte getRadarData(byte i) {
        byte data = 0;
        switch (i) {
            case 0:
                data = 0;
                break;
            case 1:
                data = 2;
                break;
            case 2:
                data = 6;
                break;
            case 3:
                data = 10;
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
            case 0x22: // Radar back
            {
                mRadar[0] = getRadarData(data[2]);
                mRadar[1] = getRadarData(data[3]);
                mRadar[2] = getRadarData(data[4]);
                mRadar[3] = getRadarData(data[5]);

                if (!Util.isZero(mRadar)) {
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
                mRadar[4] = getRadarData(data[2]);
                mRadar[5] = getRadarData(data[3]);
                mRadar[6] = getRadarData(data[4]);
                mRadar[7] = getRadarData(data[5]);

                if (!Util.isZero(mRadar)) {
                    RadarManager.start(mContext);
                    checkHideRadar();
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                }
            }
            break;

            case 0x24: {
                int door = (data[2]);
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
            case 0x17: {
                Util.byteArrayCopy(mEqData, data, 0, 4, mEqData.length);
                // if (CarUtil.getCarEQ() == 0) {
                // if (data[2] == 1) {
                // if (!CarUtil.mIsNeedSendEQ) {
                // CarUtil.mIsNeedSendEQ = true;
                // CarUtil.setMcuEQZoneUsed(1);
                // // McuManager mcu = McuManager.getInstanse();
                // // if (mcu != null) {
                // // mcu.setAudio(0x6, 0x2);
                // // }
                // mHandler.removeMessages(UPDATE_EQ);
                // mHandler.sendEmptyMessageDelayed(UPDATE_EQ, 100);
                // }
                // } else {
                // if (CarUtil.mIsNeedSendEQ) {
                // CarUtil.mIsNeedSendEQ = false;
                // CarUtil.setMcuEQZoneUsed(0);
                // mHandler.removeMessages(UPDATE_EQ);
                // }
                // }
                // }
            }
            break;
            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu
                    // ma

                    int angle = -(((a * 3000) / 1350));

                    if (angle > -50 && angle < 0) {
                        angle = -50;
                    } else if (angle > 0 && angle < 50) {
                        angle = 50;
                    }
                    // Log.e("1", ""+(data[2] & 0xff));
                    // Log.e("2", ""+(data[3] & 0xff));
                    // Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
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
            case 0x40:
            case (byte) 0xcb:
            case (byte) 0xd0:
                sendCanboxInfo("com.canboxsetting", data);
                break;
        }
    }

    private int mDoorStatus = 0;

    // private Handler mHandler = new Handler() {
    // public void handleMessage(Message msg) {
    // switch (msg.what) {
    // case UPDATE_EQ:
    // byte[] buf = new byte[] { (byte) 0x84, 0x02, 0x1, 0x1 };
    // sendDataToCanbox(buf, buf.length);
    // mHandler.removeMessages(UPDATE_EQ);
    // mHandler.sendEmptyMessageDelayed(UPDATE_EQ, 1000);
    // break;
    // }
    // super.handleMessage(msg);
    // }
    // };
    private void sendEQ(byte cmd, byte param) {
        byte[] data = new byte[]{(byte) 0x84, 0x2, cmd, param};
        sendDataToCanbox(data, data.length);
    }

    public void setEQVolume(int volume) {

        byte[] data = new byte[]{(byte) 0x84, 0x2, 0x08, (byte) volume};
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
            if (eq2[2] != mEqData[2]) {
                param = eq2[2];
                sendEQ((byte) 3, param);
                Util.doSleep(5);
            }

            if (eq2[3] != mEqData[3]) {
                param = (byte) (((eq2[3] * 12) / 60) + 1);
                sendEQ((byte) 4, param);
                Util.doSleep(5);
            }
            if (eq2[4] != mEqData[4]) {
                param = (byte) (((eq2[4] * 12) / 60) + 1);
                sendEQ((byte) 6, param);
                Util.doSleep(5);
            }
            if (eq2[5] != mEqData[5]) {
                param = (byte) (((eq2[5] * 12) / 60) + 1);
                sendEQ((byte) 5, param);
                Util.doSleep(5);
            }
            mEqData = eq2;
        }
    }

    public void startConnect() {

        super.startConnect();

        if (CarUtil.getCarEQ() == 1) {
            byte[] buf = new byte[]{(byte) 0x90, 0x02, 0x17, 0};
            sendDataToCanbox(buf, buf.length);
            mHandler.removeMessages(UPDATE_EQ);
            mHandler.sendEmptyMessageDelayed(UPDATE_EQ, 100);
            McuManager mcu = McuManager.getInstance();
            if (mcu != null) {
                mcu.setAudio(0x6, 0x2);
            }
        }
    }

    public void stopConnect() {

        mHandler.removeMessages(UPDATE_EQ);
        byte[] buf = new byte[]{(byte) 0x84, 0x02, 0x9, 0x0};
        sendDataToCanbox(buf, buf.length);
        super.stopConnect();
    }
}
