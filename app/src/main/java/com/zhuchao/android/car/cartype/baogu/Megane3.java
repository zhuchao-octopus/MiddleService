package com.zhuchao.android.car.cartype.baogu;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class Megane3 extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG}, {0x5, KEY_BT}, {0x6, KEY_MUTE}, {0x7, KEY_SOURCE}, {0x8, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x9, MyCmd.Keycode.KEY_SEEK_PREV},

            {0xa, MyCmd.Keycode.RADIO}, {0xb, KEY_GPS},

    };


    public Megane3() {

        mIdKey = 0x20;

        MAP_KEYS = KEYS_WHEEL;


        buildCmdDoor((byte) 0xd0, (byte) 0x0, (byte) 0x1f, (byte) 0x2);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {//default is simple box

    }

}
