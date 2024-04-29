package com.zhuchao.android.car.cartype.hiworld;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

public class SaiOu3Hiworld extends Canbox {

    public SaiOu3Hiworld() {

        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);

        buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0xfe);

        buildCmdVersion((byte) 0xf0, (byte) 0x0);

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);


    }


    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN},

            {0x3, MyCmd.Keycode.MUTE},


            {0x5, MyCmd.Keycode.BT_DIAL},

            {0x6, MyCmd.Keycode.BT_HANG},

            {0x9, MyCmd.Keycode.NEXT}, {0x8, MyCmd.Keycode.PREVIOUS}, {0xa, MyCmd.Keycode.MODLE},


    };


    public void startConnect() {

    }


    @Override
    public void stopConnect() {

    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {// default is simple box

    }


}
