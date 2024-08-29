package com.zhuchao.android.car.cartype.ods;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

public class BMWNbtEvo extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {3};
    private final static byte[][] KEYS_WHEEL = {

            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x5, MyCmd.Keycode.BT}, {0x6, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.SPEECH}, {0x9, MyCmd.Keycode.NAVIGATION}, {0x29, MyCmd.Keycode.ROLL_PREV}, {0x2a, MyCmd.Keycode.ROLL_NEXT}, {0x2b, MyCmd.Keycode.PREVIOUS}, {0x2c, MyCmd.Keycode.NEXT}, {0x2d, MyCmd.Keycode.PREVIOUS}, {0x2e, MyCmd.Keycode.NEXT}, {0x2f, MyCmd.Keycode.PLAY_PAUSE}, {0x30, MyCmd.Keycode.AUDIO}, {0x31, MyCmd.Keycode.RADIO}, {0x32, MyCmd.Keycode.HOME}, {0x33, MyCmd.Keycode.BT}, {0x34, MyCmd.Keycode.NAVIGATION}, {0x35, MyCmd.Keycode.BACK}, {0x36, MyCmd.Keycode.SETUP},


            {0xa, KEY_NUM_1}, {0xb, KEY_NUM_2}, {0xc, KEY_NUM_3}, {0xd, KEY_NUM_4}, {0xe, KEY_NUM_5}, {0xf, KEY_NUM_6},

            {0x2a, KEY_NUM_X}, {0x2b, KEY_NUM_J},


    };


    public BMWNbtEvo() {

        buildCmdVersion((byte) 0x30, (byte) 0x0);

        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
        buildCmdAngle((byte) 0x29, (byte) 0x5, 0x1518);

        buildCmdOutTemp((byte) 0x3, (byte) 0x10);

        mIdAC = 0x21;

        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {

        } else if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else {
            data = (byte) (31 + (data));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xef));
        airData[1] = (byte) ((data[3] & 0xef));


        airData[4] = (byte) (((data[2] & 0x10) >> 2) | ((data[7] & 0x33) >> 0));


        airData[2] = (byte) (data[5] & 0xff);
        airData[3] = (byte) (data[6] & 0xff);


        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0x24) {
            if ((data[2] & 0x1) == 0) {
                data[2] = 0;
            }
        }
        super.parseCanboxData(data, len);
    }

    public int getOutTemp(byte[] data) {//
        short t = 0;
        if (data.length > 3) {

            t = (short) (((data[3] & 0xff) | ((data[2] & 0xff) << 8)));
        }
        return t * 5;
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }


    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {

    }
}
