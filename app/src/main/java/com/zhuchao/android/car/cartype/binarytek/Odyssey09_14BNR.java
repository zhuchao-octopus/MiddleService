package com.zhuchao.android.car.cartype.binarytek;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class Odyssey09_14BNR extends Canbox {

    public Odyssey09_14BNR() {
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x4);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x4);
        buildCmdAngle((byte) 0x29, (byte) 0x0, 0x1200);


        buildCmdVersion((byte) 0x30, (byte) 0x0);

        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;

        mIdKey2 = 0x78;
        MAP_KEYS2 = KEYS_WHEEL2;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c};


    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.SPEECH},
            {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.MULT_BACK_AND_HANG}, {0x17, MyCmd.Keycode.HOME}, {0x18, MyCmd.Keycode.KEY_DISPLAY},
    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x11, MyCmd.Keycode.KEY_CAR_INFO}, {0x12, MyCmd.Keycode.KEY_CAR_INFO}, {0x13, MyCmd.Keycode.KEY_CAR_INFO}, {0x14, MyCmd.Keycode.KEY_CAR_INFO}, {0x15, MyCmd.Keycode.HOME},
            {0x16, MyCmd.Keycode.PLAY_PAUSE}, {0x18, MyCmd.Keycode.BACK}, {0x19, MyCmd.Keycode.PLAY_PAUSE}, {0x1a, MyCmd.Keycode.EQ}, {0x1d, MyCmd.Keycode.KEY_SEEK_NEXT},
            {0x1e, MyCmd.Keycode.KEY_SEEK_PREV},

    };

    @Override
    public int getAngleValue2(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

        return -angle;

    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {
            data = (byte) 0xfa;
        } else if ((data & 0xff) == 0xff) {

        } else if ((data & 0xff) == 0xfe) {
            data = 0;
        } else {
            //data =
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0x48) | ((data[2] & 0x10) << 1) | ((data[2] & 0x02) >> 1));

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
        airData[3] = (byte) (data[6] & 0xff);

        if (airData[1] == 0) {
            //			Util.zeroBuf(airData);
        }
        super.parseACInfo(airData);
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
