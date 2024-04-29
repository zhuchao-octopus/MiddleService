package com.zhuchao.android.car.cartype.raise;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class LiFanRaise extends Canbox {

    public LiFanRaise() {
        mIdAC = 0x3;
        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
        buildCmdRadarBack((byte) 0x22, (byte) 0x1, (byte) 0x3, (byte) 0x2);
        buildCmdAngle((byte) 0x30, (byte) 0x4, 0x157c);
        buildCmdOutTemp((byte) 0x3, (byte) 0x0);
        buildCmdVersion((byte) 0x7F, (byte) 0x0);
        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x2;
        MAP_KEYS2 = KEYS_WHEEL2;
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0xc, KEY_NEXTSONG}, {0xb, KEY_PREVIOUSSONG}, {0x5, KEY_MUTE}, {0x7, KEY_SOURCE}, {0x9, MyCmd.Keycode.BT_DIAL},
            {0xa, MyCmd.Keycode.BT_HANG},
    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, KEY_NEXTSONG}, {0x3, KEY_PREVIOUSSONG}, {0x5, KEY_EQ}, {0x7, KEY_FM}, {0x9, KEY_MUTE}, {0xa, MyCmd.Keycode.NUMBER1}, {0xb, MyCmd.Keycode.NUMBER2},
            {0xc, MyCmd.Keycode.NUMBER3}, {0xd, MyCmd.Keycode.NUMBER4}, {0xe, MyCmd.Keycode.NUMBER5}, {0xf, MyCmd.Keycode.NUMBER6}, {0x11, MyCmd.Keycode.EJECT}, {0x16, MyCmd.Keycode.HOME},
            {0x17, AK_KEYPAD_VOLUME_A}, {0x18, AK_KEYPAD_VOLUME_D}, {0x21, KEY_GPS}, {0x21, KEY_SOURCE}, {0x22, MyCmd.Keycode.AS},
    };

    private int getACTempPriv(byte data) {//
        if ((data & 0xff) == 0x80) {
            data = 0;
        } else if ((data & 0xff) == 0x9d) {
            data = (byte) 0xff;
        } else {
            data = (byte) (36 + (data - 0x80));
        }
        return data & 0xff;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[10];

        airData[0] = (byte) ((data[2] & 0xe8) | ((data[2] & 0x10) >> 4));

        airData[1] = (byte) ((data[2] & 0x07) | ((data[3] & 0x80) >> 4));
        switch (data[3] & 0x7f) {
            case 1:
                airData[9] |= 0x01;
                break;
            case 2:
            case 7:
                airData[1] |= 0x80;
                break;
            case 3:
                airData[1] |= 0x20;
                break;
            case 4:
                airData[1] |= 0x60;
                break;
            case 5:
                airData[1] |= 0x40;
                break;
            case 6:
                airData[1] |= 0xc0;
                break;
            case 8:
                airData[1] |= 0xa0;
                break;
            case 9:
                airData[1] |= 0xe0;
                break;
        }

        airData[2] = data[4];
        airData[3] = data[5];

        airData[4] = (byte) (data[6] & 0xff);
        if ((data[4] & 0x80) == 0) {
            airData[7] = 0x40;
            if ((airData[2] & 0xff) > 9) {
                airData[2] = 0;
            }
            if ((airData[3] & 0xff) > 9) {
                airData[3] = 0;
            }
        } else {
            airData[2] = (byte) getACTempPriv(airData[2]);
            airData[3] = (byte) getACTempPriv(airData[3]);
        }
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {// default is simple box

    }


    public int getOutTemp(byte[] data) {//
        // short t = data[7];
        if ((data[7] & 0xff) == 0xff) {
            return CarUtil.CLEAR_OUT_DOOR_TEMP;
        } else {
            return data[7] * 10;
        }
    }

}
