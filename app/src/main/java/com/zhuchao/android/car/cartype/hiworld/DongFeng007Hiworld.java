package com.zhuchao.android.car.cartype.hiworld;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

public class DongFeng007Hiworld extends Canbox {

    public DongFeng007Hiworld() {

        buildCmdVersion((byte) 0xf0, (byte) 0x0);


        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);

    }


    public void startConnect() {

    }

    @Override
    public void stopConnect() {

    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0xb, MyCmd.Keycode.MODLE},

            {0x8, MyCmd.Keycode.PREVIOUS}, {0x9, MyCmd.Keycode.NEXT},


    };

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {

    }


}
