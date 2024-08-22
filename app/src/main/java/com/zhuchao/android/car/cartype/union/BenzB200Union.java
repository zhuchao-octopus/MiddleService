package com.zhuchao.android.car.cartype.union;

import android.os.Handler;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;


public class BenzB200Union extends Canbox {

    public BenzB200Union() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x14, AK_KEYPAD_VOLUME_A}, {0x15, AK_KEYPAD_VOLUME_D},

            {0x13, MyCmd.Keycode.KEY_SEEK_PREV}, {0x12, MyCmd.Keycode.KEY_SEEK_NEXT},

            {0x16, KEY_MUTE}, {0x17, MyCmd.Keycode.SPEECH}, {0x50, KEY_BT_DIAL}, {0x51, KEY_BT_HANG}, {0x52, KEY_MODE}, {0x53, KEY_HOME},

    };

    public void stopConnect() {// default is simple box
        setMediaSrc(MyCmd.SOURCE_NONE);
        super.stopConnect();
    }

    private void parseWheelKey(byte[] data) {
        if (doKeyStudy(data[2], 1)) {
            doKeyStudy(data[2], 0);
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
            doKey(key, 1);
            doKey(key, 0);
        }
    }

    private void parseACInfo(byte[] data, int len) {

        byte[] airData = new byte[9];

        //		if ((data[5] & 0xff) == 0xfe) {
        //			data[5] = (byte) 0;
        //		} else if (data[5] == 0) {
        //			airData[5] |= 0x2;
        //		}
        //
        //		if ((data[6] & 0xff) == 0xfe) {
        //			data[6] = (byte) 0;
        //		} else if (data[6] == 0) {
        //			airData[5] |= 0x4;
        //		}

        airData[0] = (byte) (data[2] & 0xf0);
        airData[0] |= (byte) (((data[2] & 0x08) >> 3) | ((data[2] & 0x1) << 1));


        airData[1] = (byte) (data[3] & 0xff);

        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);


        airData[4] = (byte) (((data[2] & 0x02) << 2));
        airData[7] = (byte) ((data[2] & 0x04) << 5);

        airData[8] = (byte) ((data[6] & 0x80));


        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
        }
    }

    private int mDoorStatus;

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x1: {
                parseWheelKey(data);
            }
            break;

            case 0x21: {
                parseACInfo(data, len);
            }
            break;
            case 0x22: {
                int door = (data[2] & 0x3f);
                // door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6)
                // | ((door & 0x10) >> 2) | ((door & 0x20) >> 2)
                // | ((door & 0x08) << 1) | ((door & 0x4) << 3));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }
            }
            break;

            case 0x24: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = (data[2] & 0xff);

                    if (angle > 0 && angle <= 0x32) {

                    } else if (angle > 0x80 && angle <= 0xb2) {
                        angle = -(angle - 0x80);
                    } else {
                        angle = 0;
                    }

                    angle = angle * 300 / 50;

                    if (angle > -5 && angle < 5) {
                        angle = 5;
                    }

                    // angle = (short) (0 - angle);

                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));
                }
            }
            break;

        }

    }


    private int mSource = MyCmd.SOURCE_NONE;
    private final byte[] data = new byte[11];

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        // ++play;

        if (MyCmd.SOURCE_DVD != source) {
            ++play;
        }

        data[5] = (byte) ((play & 0xff00) >> 8);
        data[6] = (byte) ((play & 0xff) >> 0);

        data[9] = min;
        data[10] = sec;

        if (mPhoneStatus < HFP_INFO_CALLED) {
            sendDataToCanbox(data, data.length);
        }
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) { //fm
            b[0] = 0x11;
        } else {
            b[0] = 0x21;
        }
        //		data = new byte[] { (byte) 0xc0, 0x5, 0x1, b[0], b[1], b[2], 0 };
        data[4] = b[0];
        data[5] = b[2];
        data[6] = b[1];
        sendDataToCanbox(data, data.length);
    }


    public void setMediaSrc(int source) {
        byte s = 0;
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                s = 1;
                break;
            case MyCmd.SOURCE_DVD:
                s = 0x2;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 3;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x9;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x7;
                break;
            case MyCmd.SOURCE_NONE:
                s = 0;
                break;
            default:
                s = 0xf;
                break;
        }

        if (mSource != source) {
            mSource = source;
            Util.zeroBuf(data);
            data[0] = (byte) 0x82;
            data[1] = 9;
            data[2] = s;

            sendDataToCanbox(data, data.length);
        }
    }


    private int mPhoneStatus = HFP_INFO_INITIAL;

    public void setPhone(int status, String num) {// default is simple box

        mPhoneStatus = status;


        byte[] dataNum = null;
        if (status > HFP_INFO_CONNECTED) {
            data[8] |= 0x04;

            int len = 0;
            if (num != null) {

                byte[] n = num.getBytes();

                len = n.length;
                if (len > 12) {
                    len = 12;
                }

                dataNum = new byte[3 + len];

                dataNum[0] = (byte) 0x83;
                dataNum[1] = (byte) (len + 1);
                dataNum[2] = (byte) (len | 0x10);

                byteArrayCopy(dataNum, n, 3, 0, len);

            }
        } else {
            data[8] &= ~0x04;
            dataNum = new byte[]{(byte) 0x83, 0x1, 0x0};
        }

        sendDataToCanbox(data, data.length);
        if (dataNum != null) {
            sendDataToCanbox(dataNum, dataNum.length);
        }
    }

    public void setVolume(int volume) {
        if (volume == 0) {
            data[8] |= 0x08;
        } else {
            data[8] &= ~0x08;
        }

        sendDataToCanbox(data, data.length);
    }
}
