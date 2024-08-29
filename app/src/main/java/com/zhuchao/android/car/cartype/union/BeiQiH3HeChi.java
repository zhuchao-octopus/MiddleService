package com.zhuchao.android.car.cartype.union;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class BeiQiH3HeChi extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS},};

    public BeiQiH3HeChi() {
        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x7);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x7);
        buildCmdAngle((byte) 0x30, (byte) 0x0, 540);


        buildCmdVersion((byte) 0x7f, (byte) 0x0);

        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;
    }

    @Override
    public int getAngleValue2(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));


        return -angle;


    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


}
