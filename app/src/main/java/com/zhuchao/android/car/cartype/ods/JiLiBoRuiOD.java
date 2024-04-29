package com.zhuchao.android.car.cartype.ods;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class JiLiBoRuiOD extends Canbox {

    public JiLiBoRuiOD() {
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

        buildCmdAngle((byte) 0x30, (byte) 0x0, 0x1545);


        buildCmdVersion((byte) 0x7f, (byte) 0x0);

        mIdAC = 0x23;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x21;
        MAP_KEYS2 = KEYS_WHEEL2;

    }


    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x6, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE},
            {0x8, MyCmd.Keycode.BT}, {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG},
    };


    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.AUDIO}, {0x2, MyCmd.Keycode.RADIO}, {0x3, MyCmd.Keycode.BT}, {0x4, MyCmd.Keycode.SETUP},

            {0x6, MyCmd.Keycode.HOME}, {0x7, MyCmd.Keycode.BACK}, {0x8, MyCmd.Keycode.NAVIGATION}, {0x9, MyCmd.Keycode.KEY_360}, {0xa, MyCmd.Keycode.PREVIOUS}, {0xb, MyCmd.Keycode.NEXT},
            {0xc, MyCmd.Keycode.PREVIOUS}, {0xd, MyCmd.Keycode.NEXT}, {0xe, MyCmd.Keycode.PLAY_PAUSE}, {0x11, MyCmd.Keycode.ROLL_PREV}, {0x12, MyCmd.Keycode.ROLL_NEXT},

    };

    @Override
    public int getAngleValue2(byte[] data) {

        int angle = (short) ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));


        return angle;


    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {
        } else if ((data & 0xff) == 0xff) {

        } else if ((data & 0xff) == 0xfe) {

        } else {
            data = (byte) (34 + (data & 0xff));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xe0) | ((data[2] & 0x01) << 1) | ((data[2] & 0x02) >> 1));

        switch ((data[3] & 0xff)) {
            case 1:
                airData[1] = (byte) (0x40);
                break;
            case 2:
                airData[1] = (byte) (0x60);
                break;
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 4:
                airData[1] = (byte) (0xa0);
                break;
            case 5:
                airData[1] = (byte) (0x80);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);


        airData[2] = (byte) (data[5] & 0xff);

        airData[4] |= (byte) ((data[2] & 0x10) >> 2);

        //		airData[7] |= 0x40;
        //		airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0x40) {
        } else {
            super.parseCanboxData(data, len);
        }
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


    public int getOutTemp(byte[] data) {//
        short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
        return t;
    }

}
