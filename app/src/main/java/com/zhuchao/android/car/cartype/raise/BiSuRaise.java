package com.zhuchao.android.car.cartype.raise;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class BiSuRaise extends Canbox {

    public BiSuRaise() {
        mIdAC = 0x26;
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 4);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 4);
        buildCmdAngle((byte) 0x29, (byte) 0x3, 0x16cd);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x21;
        MAP_KEYS2 = KEYS_WHEEL2;

    }


    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG},

            {0x5, MyCmd.Keycode.MUTE}, {0x6, KEY_SOURCE},
    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG},

            {0x5, MyCmd.Keycode.MUTE}, {0x6, MyCmd.Keycode.PLAY_PAUSE}, {0x7, MyCmd.Keycode.HOME}, {0x8, MyCmd.Keycode.AUDIO}, {0x9, MyCmd.Keycode.RADIO}, {0xa, MyCmd.Keycode.BT_MUSIC},
            {0xb, MyCmd.Keycode.BT_DIAL}, {0xc, MyCmd.Keycode.BT_HANG}, {0xd, MyCmd.Keycode.BACK}, {0xe, MyCmd.Keycode.POWER}, {0xf, MyCmd.Keycode.SETUP}, {0x10, MyCmd.Keycode.EASY_CONNECT},
            {0x11, MyCmd.Keycode.NAVIGATION},

    };


    //	public int getAngleValue(byte[] data) {
    //
    //		int angle;
    //		int max;
    //
    //		angle = ((data[3] & 0xff) | (((data[2] & 0x7f)) << 8));
    //		if ((data[2] & 0x80) != 0){
    //			angle = -angle;
    //		}
    //		max = 0x21c;
    //
    //		angle = ((angle * 3000) / max);
    //		if (angle > -50 && angle < 50) {
    //			angle = 50;
    //		}
    //
    //		return angle;
    //	}


    @Override
    public int getACTemp(byte data) {
        if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else {
            data = (byte) (36 + (data - 0x1));

        }
        return data & 0xff;
    }


    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[8];
        airData[0] = (byte) (((data[2] & 0xef)));
        airData[1] = (byte) ((data[3] & 0xff));
        airData[2] = data[4];
        airData[3] = data[5];


        airData[7] = (byte) (((data[2] & 0x10) << 1));
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {//default is simple box

    }


}
