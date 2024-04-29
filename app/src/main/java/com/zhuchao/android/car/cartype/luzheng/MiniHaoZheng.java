package com.zhuchao.android.car.cartype.luzheng;

import android.os.Handler;
import android.os.Message;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;


public class MiniHaoZheng extends Canbox {

    public MiniHaoZheng() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG}, {0x5, KEY_BT}, {0x6, KEY_MUTE}, {0x7, KEY_SOURCE},

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

    private final static byte[][] KEYS_WHEEL2 = {
            {0x2, KEY_NEXTSONG}, {0x1, KEY_PREVIOUSSONG}, {0x3, MyCmd.Keycode.FAST_F}, {0x4, MyCmd.Keycode.FAST_R}, {0x11, MyCmd.Keycode.BT_DIAL}, {0x12, MyCmd.Keycode.BT_HANG}, {0x14, KEY_HOME},
            {0x17, KEY_MIC}, {0x19, MyCmd.Keycode.KEY_BT_VOICE_SPEAKER}, {0x18, MyCmd.Keycode.KEY_BT_VOICE_PHONE}, {0x30, KEY_BACK},

    };

    private void parseWheelKey2(byte[] data) {
        byte key = 0;
        for (int i = 0; i < KEYS_WHEEL2.length; ++i) {
            if (KEYS_WHEEL2[i][0] == data[2]) {
                key = KEYS_WHEEL2[i][1];
                break;
            }
        }

        if (key != 0) {
            doKey(key, 1);
            doKey(key, 0);
        }
    }

    private void parseACInfo(byte[] data, int len) {

        // if (data[4] >= 0x1f) {
        // data[4] = (byte) 0xff;
        // } else if (data[4] > 0) {
        // if ((data[6] & 0x1) == 0) {
        // data[4] = (byte) (((0.5f * data[4])));
        // } else {
        // data[4] = (byte) ((59 + (data[4] & 0xff)));
        // }
        // }
        // if (data[5] >= 0x1f) {
        // data[5] = (byte) 0xff;
        // } else if (data[5] > 0) {
        // if ((data[6] & 0x1) == 0) {
        // data[5] = (byte) ((15.5f + (0.5f * data[5])) * 2);
        // } else {
        // data[5] = (byte) ((59 + (data[5] & 0xff)));
        // }
        // }
        byte[] airData = new byte[8];

        airData[0] = (byte) (data[2] & 0xef);
        airData[0] |= (byte) (((data[6] & 0x40) >> 6));

        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        //		airData[4] = (byte) (data[7] & 0xff);
        airData[4] |= (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x8) >> 1) | ((data[2] & 0x1) << 3));

        airData[5] = (byte) ((data[6] & 0x1));

        airData[7] = (byte) (((data[6] & 0x10) >> 4));
        airData[7] |= (byte) (((data[6] & 0x80) >> 2));

        int msg = CANBOX_HIDE_AIR;
        if ((data[3] & 0x10) != 0) {
            msg = CANBOX_RETURN_AIR;
        }

        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(msg, airData));
        }
    }

    private byte getRadarData(byte i) {
        byte data = 0;
        switch (i) {
            case 0:
                data = 0;
                break;
            case 1:
                data = 10;
                break;
            case 2:
                data = 8;
                break;
            case 3:
                data = 6;
                break;
            case 4:
                data = 4;
                break;
            case 5:
                data = 2;
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

            case 0x22: // Radar back
            {

                mRadar[0] = getRadarData(data[2]);
                mRadar[1] = getRadarData(data[3]);
                mRadar[2] = getRadarData(data[3]);
                mRadar[3] = getRadarData(data[4]);
                mRadar[4] = getRadarData(data[5]);
                mRadar[5] = getRadarData(data[6]);
                mRadar[6] = getRadarData(data[6]);
                mRadar[7] = getRadarData(data[7]);

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
            case 0x24: {
                if ((data[2] & 0x1) != 0) {
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

            }
            break;

            case 0x23: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = ((data[2] & 0xff));
                    if (angle > 0x20) {
                        angle = angle - 0xff;
                    } else {

                    }
                    angle = -(angle * 300 / 0x20);
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));
                }
            }
            break;

        }

    }


    private int mDoorStatus = 0;


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }


    public void sendId3(byte index, String num) {

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
    //
    // public void setPhone(int status, String num) {// default is simple box
    // switch (status) {
    // case HFP_INFO_INITIAL:
    // case HFP_INFO_READY:
    // case HFP_INFO_CONNECTING:
    // case HFP_INFO_CONNECTED:
    // status = 0;
    // break;
    // case HFP_INFO_CALLED:
    // status = 2;
    // break;
    // case HFP_INFO_INCOMING:
    // status = 1;
    // break;
    // case HFP_INFO_CALLING:
    // status = 4;
    // break;
    // }
    // byte[] data = new byte[4];// {(byte)0xc5, 0x1, (byte)status};
    // data[0] = (byte) 0xc5;
    // data[1] = (byte) (2);
    // data[2] = (byte) 0;
    // data[3] = (byte) status;
    //
    // sendDataToCanbox(data, data.length);
    //
    // if (num == null) {
    // num = " ";
    // }
    // byte[] n = num.getBytes();
    // data = new byte[n.length + 4];// {(byte)0xc5, 0x1, (byte)status};
    // data[0] = (byte) 0xcA;
    // data[1] = (byte) (n.length + 2);
    // data[2] = (byte) 1;
    // data[3] = (byte) 3;
    // byteArrayCopy(data, n, 4, 0, n.length);
    // Util.doSleep(100);
    // sendDataToCanbox(data, data.length);
    //
    // }
}
