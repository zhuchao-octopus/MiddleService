package com.zhuchao.android.car.cartype.raise;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class BenzRaise extends Canbox {

    public BenzRaise() {
        mIdAC = 0x21;
        buildCmdDoor((byte) 0x41, (byte) 0x0, (byte) 0x1f, (byte) 0x13);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 7);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 7);
        buildCmdAngle((byte) 0x26, (byte) 0x3, 0x16cd);
        buildCmdVersion((byte) 0x30, (byte) 0x0);
        buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x27;
        MAP_KEYS2 = KEYS_WHEEL2;

    }


    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG},

            {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG}, {0x7, KEY_SOURCE}, {0x8, MyCmd.Keycode.SPEECH}, {0x9, MyCmd.Keycode.BACK}, {0xa, MyCmd.Keycode.MUTE},
    };

    private final static byte[][] KEYS_WHEEL2 = {

            {0x1, MyCmd.Keycode.PREVIOUS}, {0x2, MyCmd.Keycode.NEXT}, {0x3, MyCmd.Keycode.VOLUME_UP}, {0x4, MyCmd.Keycode.VOLUME_DOWN}, {0x5, MyCmd.Keycode.KEY_TURN_D},
            {0x6, MyCmd.Keycode.KEY_TURN_A}, {0x7, MyCmd.Keycode.PLAY_PAUSE},
            //		{ 0x8, MyCmd.Keycode },
            {0x9, MyCmd.Keycode.BACK}, {0xa, MyCmd.Keycode.BACKLIGHT_OFF},
    };


    public int getOutTemp(byte[] data) {//
        int t = CarUtil.CLEAR_OUT_DOOR_TEMP;
        if (data[2] == 0x2) {

            t = (short) (((data[4] & 0xff)) | ((data[3] & 0xff) << 8));
        }
        return t;
    }

    public int getAngleValue(byte[] data) {

        int angle;
        int max;

        angle = ((data[2] & 0xff) | (((data[3] & 0x7f)) << 8));
        if ((data[3] & 0x80) != 0) {
            angle = -angle;
        }
        max = 0x21c;

        angle = ((angle * 3000) / max);
        if (angle > -50 && angle < 50) {
            angle = 50;
        }

        return angle;
    }


    @Override
    public int getACTemp(byte data) {
        if ((data & 0xff) == 0xfe) {
            data = (byte) 0xff;
        } else {
            data = (byte) (30 + (data - 0x1e));

        }
        return data & 0xff;
    }


    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[8];
        airData[0] = (byte) (((data[2] & 0xf7)));
        airData[1] = (byte) ((data[3] & 0xff));
        airData[2] = data[4];
        airData[3] = data[5];


        airData[4] = (byte) (((data[6] & 0x3) << 4) | ((data[6] & 0xc) >> 2));

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }


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
        data = new byte[]{(byte) 0xc0, 0x2, s, mediaType};

        sendDataToCanbox(data, data.length);
    }


}
