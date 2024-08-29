package com.zhuchao.android.car.cartype.simple;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.common.utils.MachineConfig;
import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.McuManager;


public class ChryslerSimple extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x4, KEY_PREVIOUSSONG}, {0x3, KEY_NEXTSONG},

            {0x5, KEY_MUTE},

            {0x6, KEY_BT}, {0x7, KEY_SOURCE}, {0x8, KEY_MIC}, {0x9, KEY_BT},};
    private final static int UPDATE_EQ = 0;
    private int mDoorStatus = 0;
    private byte[] mEqData = new byte[5];

    public ChryslerSimple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});
        updateCanboxKeySettings();
    }

    public void setContext(Context c) {
        super.setContext(c);

        int volume = MachineConfig.getIntProperty2(SettingProperties.CANBOX_EQ_VOLUME);
        if (volume == -1) {
            volume = 28;
        }
        setEQVolume(volume);
    }

    public void updateCanboxKeySettings() {
        if (CarUtil.getCarEQ() != 2) {
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

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data);
            }
            break;
            // case 0x17:
            // Util.byteArrayCopy(mEqData, data, 0, 3, mEqData.length);
            // break;
            case 0x24: {
                int door = (data[2] & 0xff);
                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1));

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
            case 0x30:
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu
                    // ma

                    int angle = -(((a * 3000) / 1000));

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
        }
    }

    private void sendEQ(byte cmd, byte param) {
        byte[] data = new byte[]{(byte) 0x84, 0x2, cmd, param};
        sendDataToCanbox(data, data.length);
    }    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_EQ) {
                byte[] buf = new byte[]{(byte) 0x84, 0x02, 0x1, 0x1};
                sendDataToCanbox(buf, buf.length);
                mHandler.removeMessages(UPDATE_EQ);
                mHandler.sendEmptyMessageDelayed(UPDATE_EQ, 1000);
            }
            super.handleMessage(msg);
        }
    };

    private byte eqZoneC(byte b) {
        switch (b) {
            case 0:
                b = 1;
                break;
            case 1:
                b = 2;
                break;
            case 2:
                b = 3;
                break;
            case 3:
                b = 5;
                break;
            case 4:
                b = 7;
                break;
            case 5:
                b = 8;
                break;
            case 6:
                b = 9;
                break;
            case 7:
                b = 10;
                break;
            case 8:
                b = 11;
                break;
            case 9:
                b = 12;
                break;
            case 10:
                b = 14;
                break;
            case 11:
                b = 16;
                break;
            case 12:
                b = 17;
                break;
            case 13:
                b = 18;
                break;
            case 14:
                b = 19;
                break;
        }
        return b;
    }

    private byte eqC(int b) {
        b = b / 3;
        return ((byte) b);
    }

    public void sendEqToCanbox(byte[] eq) {
        if (eq != null && eq.length >= 11) {

            byte[] eq2 = new byte[5];

            eq2[0] = eqZoneC(eq[0]);
            eq2[1] = eqZoneC(eq[1]);

            eq2[2] = eqC(eq[2] + eq[3] + eq[4]);
            eq2[3] = eqC(eq[5] + eq[6] + eq[7]);
            eq2[4] = eqC(eq[8] + eq[9] + eq[10]);

            byte param;

            if (eq2[0] != mEqData[0]) {
                param = (eq2[0]);
                sendEQ((byte) 3, param);
                Util.doSleep(5);
            }
            if (eq2[1] != mEqData[1]) {
                param = (eq2[1]);
                sendEQ((byte) 4, param);
                Util.doSleep(5);
            }

            if (eq2[2] != mEqData[2]) {
                param = eq2[2];
                sendEQ((byte) 5, param);
                Util.doSleep(5);
            }
            if (eq2[3] != mEqData[3]) {
                param = eq2[3];
                sendEQ((byte) 7, param);
                Util.doSleep(5);
            }
            if (eq2[4] != mEqData[4]) {
                param = eq2[4];
                sendEQ((byte) 6, param);
                Util.doSleep(5);
            }
            mEqData = eq2;
        }
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte[] data;
        data = new byte[]{(byte) 0xc0, 0x8, 0x10, 0, 0, (byte) play, 0, 0, 0, 0};
        sendDataToCanbox(data, data.length);

    }

    public void setMediaSrc(int source) {// default is simple box
        byte s;
        switch (source) {
            case 0:
                s = 1;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
            case 1:
                s = 0x10;
                break;
            case MyCmd.SOURCE_AUX:
            case MyCmd.SOURCE_AUX_FRONT:
                s = 0x07;
                break;
            default:
                s = 0x00;
                break;
        }
        byte[] data = new byte[]{(byte) 0xc0, 0x8, s, 0, 0, 0, 0, 0, 0, 0};

        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        if (b[3] >= 0 & b[3] <= 30) {
            b[3]++;
        } else {
            b[3] = 0;
        }
        byte[] data = new byte[]{(byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], b[3], 0, 0};
        sendDataToCanbox(data, data.length);
    }

    private void setEQVolume(int volume) {
        byte[] data = new byte[]{(byte) 0x84, 0x02, 0x2, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    public void startConnect() {

        super.startConnect();

        // if (CarUtil.getCarEQ() != 2) {
        byte[] buf = new byte[]{(byte) 0x90, 0x02, 0x17, 0};
        sendDataToCanbox(buf, buf.length);
        mHandler.removeMessages(UPDATE_EQ);
        mHandler.sendEmptyMessageDelayed(UPDATE_EQ, 100);
        // }
        if (CarUtil.getCarEQ() == 1) {
            McuManager mcu = McuManager.getInstance();
            if (mcu != null) {
                mcu.setAudio(0x6, 0x2);
            }
        }
    }

    public void stopConnect() {

        mHandler.removeMessages(UPDATE_EQ);
        byte[] buf = new byte[]{(byte) 0x84, 0x02, 0x1, 0x0};
        sendDataToCanbox(buf, buf.length);
        super.stopConnect();
    }




}
