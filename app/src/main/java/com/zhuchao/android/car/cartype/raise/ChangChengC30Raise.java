package com.zhuchao.android.car.cartype.raise;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class ChangChengC30Raise extends Canbox {

    public ChangChengC30Raise() {
        mIdAC = 0x23;
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;
    }


    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x6, KEY_MUTE}, {0x7, KEY_SOURCE}, {0x8, KEY_MIC}, {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG},

            {0xc, KEY_NEXTSONG}, {0xb, KEY_PREVIOUSSONG},

            {0x20, MyCmd.Keycode.AS}, {0x21, MyCmd.Keycode.RADIO}, {0x22, MyCmd.Keycode.HOME}, {0x23, MyCmd.Keycode.KEY_SEEK_PREV}, {0x24, MyCmd.Keycode.KEY_SEEK_NEXT},
            {0x25, MyCmd.Keycode.KEY_SHUFFLE}, {0x26, MyCmd.Keycode.KEY_REPEAT}, {0x27, MyCmd.Keycode.BT_HANG}, {0x28, MyCmd.Keycode.BT_DIAL}, {0x29, MyCmd.Keycode.PREVIOUS},
            {0x2a, MyCmd.Keycode.NEXT}, {0x2b, MyCmd.Keycode.VOLUME_UP}, {0x2c, MyCmd.Keycode.VOLUME_DOWN}, {0x2d, MyCmd.Keycode.POWER}, {0x2e, MyCmd.Keycode.EJECT}, {0x2f, MyCmd.Keycode.EQ},
            {0x30, MyCmd.Keycode.SETUP},

    };


    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[8];
        airData[0] = (byte) ((data[2] & 0xe0) | ((data[2] & 0x02) >> 1));

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
        airData[3] = airData[2];


        airData[7] |= 0x40;
        airData[5] |= 0x80;

        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {// default is simple box

    }

}
