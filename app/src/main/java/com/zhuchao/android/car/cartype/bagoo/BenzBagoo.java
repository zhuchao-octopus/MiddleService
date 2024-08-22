package com.zhuchao.android.car.cartype.bagoo;


import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;

public class BenzBagoo extends Canbox {

    public BenzBagoo() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });

        // updateCanboxSettings();
    }

    private final static byte[][] KEYS_WHEEL = {

            {0x12, KEY_SEEK_NEXT}, {0x13, KEY_SEEK_PREV},

            {0x14, AK_KEYPAD_VOLUME_A}, {0x15, AK_KEYPAD_VOLUME_D},

            {0x50, KEY_BT_DIAL}, {0x51, KEY_BT_HANG},

    };

    private void parseWheelKey(byte[] data, int len) {

        if (doKeyStudy(data[2], (data[2] == 0) ? 0 : 1)) {
            return;
        }

        int key = 0;
        for (int i = 0; i < KEYS_WHEEL.length; ++i) {
            if (KEYS_WHEEL[i][0] == data[2]) {
                key = KEYS_WHEEL[i][1];
                break;
            }
        }

        if (key != 0) {
            doKey(key, 1);
        } else {
            doKey(key, 0);
        }

    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0x1) {
            parseWheelKey(data, len);
        }
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);


        if (MyCmd.SOURCE_DVD == source) {

        } else {
            ++play;


        }
        data[3] = (byte) 0xff;
        data[4] = (byte) 0xff;
        data[5] = (byte) ((play & 0xff00) >> 8);
        data[6] = (byte) ((play & 0xff) >> 0);
        data[7] = (byte) 0xff;
        data[9] = min;
        data[10] = sec;
        sendDataToCanbox(data, data.length);

    }

    private int mSource = MyCmd.SOURCE_NONE;
    private final int mBaud = 0;

    public void setVolume(int volume) {

        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    byte[] data = new byte[11];

    public void setMediaSrc(int source, byte type, byte[] b) {
        // setMediaSrc(0);
        if (b[0] == 0x10) {
            b[0] = 0x20;
        } else {
            b[0] = 0x10;
        }

        //		Util.clearBuf(data);
        //		data[0] = (byte) 0x82;
        //		data[1] = 0x9;
        //		data[2] = 0x1;
        data[3] = (byte) 0xff;
        data[4] = b[0];
        data[5] = b[2];
        data[6] = b[1];

        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source) {
        byte s = 0;

        Util.clearBuf(data);

        switch (source) {
            case MyCmd.SOURCE_RADIO:
                s = 1;
                break;
            case MyCmd.SOURCE_DVD:
                s = 2;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 5;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x03;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x09;
                break;
            default:
                s = 0x0f;
                break;
        }

        data[0] = (byte) 0x82;
        data[1] = 0x9;

        data[2] = s;
        sendDataToCanbox(data, data.length);

        mSource = source;
    }

    public void setPhone(int status, String num) {

    }

}
