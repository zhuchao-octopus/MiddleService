package com.zhuchao.android.car.cartype.hiworld;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

public class FordFDP007Hiworld extends Canbox {

    public FordFDP007Hiworld() {
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
        buildCmdAngle((byte) 0x72, (byte) 0x0, 0xfe);

        buildCmdRadarBack((byte) 0x72, (byte) 0x0, (byte) 0xfe);
        buildCmdRadarFront((byte) 0x72, (byte) 0x0, (byte) 0xfe);
        buildCmdRadarFrontEx((byte) 10);
        buildCmdRadarBackEx((byte) 6);

        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x73;

        buildCmdKey((byte) 0x72, (byte) 5, (byte) 4, (byte) 0, KEYS_WHEEL);

        buildCmdKey((byte) 0x21, (byte) 2, KEYS_WHEEL2);

        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;
    }


    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN},

            {0x3, MyCmd.Keycode.MUTE},


            {0x5, MyCmd.Keycode.BT_DIAL},

            {0x6, MyCmd.Keycode.BT_HANG},

            {0x9, MyCmd.Keycode.NEXT}, {0x8, MyCmd.Keycode.PREVIOUS},


            {0xa, MyCmd.Keycode.MODLE}, {0x20, MyCmd.Keycode.KEY_AIR_CONTROL},

    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT}, {0x11, MyCmd.Keycode.EJECT},


            {0x2b, MyCmd.Keycode.HOME}, {0x39, MyCmd.Keycode.POWER}, {0x4b, MyCmd.Keycode.RADIO},
    };

    private final static byte[][] KEYS_WHEEL3 = {
            {0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0},

    };

    @Override
    public int getAngleValue2(byte[] data) {
        int angle = 0;
        if ((data[6] & 0xff) > 0 && (data[6] & 0xff) <= 0xfe) {
            angle = (data[6] & 0xff);
        } else if ((data[7] & 0xff) > 0 && (data[7] & 0xff) <= 0xfe) {
            angle = -(data[7] & 0xff);
        }

        return angle;

    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub

        if ((data & 0xff) == 0) {
            data = (byte) 0xfa;
        } else if ((data & 0xff) == 1) {
            data = 0;
        } else if ((data & 0xff) == 0xff) {

        } else {
            data = (byte) ((data & 0xff) - 80);
        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[10];

        airData[0] = (byte) (((data[2] & 0x40) << 1) | ((data[2] & 0x0c) << 0)

                | ((data[3] & 0x40) << 0) | ((data[3] & 0x10) >> 3));

        airData[7] = (byte) (((data[3] & 0x10) << 1));

        if ((((data[2] & 0x30) >> 4) == 1)) {
            airData[0] |= 0x20;
        }


        airData[1] = (byte) (((data[6] & 0x40) >> 1) | ((data[6] & 0x20) << 1) | ((data[6] & 0x10) << 3) | ((data[6] & 0x0f) << 0));

        //		airData[6] = (byte) (((data[7] & 0x40) >> 1)
        //				| ((data[7] & 0x20) << 1)
        //				| ((data[7] & 0x10) << 3)
        //				| ((data[7] & 0x0f) << 0) );

        airData[2] = data[4];
        airData[3] = data[5];

        airData[5] |= 0x88;
        super.parseACInfo(airData);
    }


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


    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword2(data, len);
    }


}
