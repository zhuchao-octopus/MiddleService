package com.zhuchao.android.car.cartype.hiworld;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class Ford003Hiworld extends Canbox {

    public Ford003Hiworld() {
        buildCmdDoor((byte) 0x73, (byte) 0x2, (byte) 0xf8, (byte) 0x09);

        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;
        buildCmdKey((byte) 0x72, (byte) 5, (byte) 4, (byte) 0, KEYS_WHEEL);


    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.DVD}, {0x2, MyCmd.Keycode.RADIO}, {0x3, MyCmd.Keycode.AUX_IN}, {0x4, MyCmd.Keycode.MUTE}, {0x5, MyCmd.Keycode.MENU}, {0x6, MyCmd.Keycode.EJECT},
            {0x7, MyCmd.Keycode.SETUP}, {0x8, MyCmd.Keycode.PREVIOUS}, {0x9, MyCmd.Keycode.NEXT}, {0xa, MyCmd.Keycode.PREVIOUS}, {0xb, MyCmd.Keycode.NEXT}, {0xc, MyCmd.Keycode.PLAY_PAUSE},
            {0xd, MyCmd.Keycode.KEY_SEEK_PREV}, {0xe, MyCmd.Keycode.KEY_SEEK_NEXT}, {0xf, MyCmd.Keycode.POWER}, {0x10, MyCmd.Keycode.AS}, {0x11, MyCmd.Keycode.AUDIO}, {0x12, MyCmd.Keycode.NUMBER1},
            {0x13, MyCmd.Keycode.NUMBER2}, {0x14, MyCmd.Keycode.NUMBER3}, {0x15, MyCmd.Keycode.NUMBER4}, {0x16, MyCmd.Keycode.NUMBER5}, {0x17, MyCmd.Keycode.NUMBER6}, {0x18, MyCmd.Keycode.NUMBER7},
            {0x19, MyCmd.Keycode.NUMBER8}, {0x1a, MyCmd.Keycode.NUMBER9}, {0x1b, MyCmd.Keycode.NUMBER0}, {0x1c, MyCmd.Keycode.NUMBER_STAR}, {0x1d, MyCmd.Keycode.NUMBER_POUND},
            //		{ 0x1e, MyCmd.Keycode },
            //		{ 0x1f, MyCmd.Keycode },
            //		{ 0x20, MyCmd.Keycode },
            //		{ 0x21, MyCmd.Keycode },
            {0x22, MyCmd.Keycode.VOLUME_UP}, {0x23, MyCmd.Keycode.VOLUME_DOWN},

    };


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

        airData[0] = (byte) (((data[2] & 0x40) << 1) | ((data[3] & 0x08) >> 0) | ((data[3] & 0x80) >> 6) | ((data[3] & 0x01) << 6));

        if ((data[3] & 0x10) == 0) {
            airData[0] |= 0x20;
        }

        switch ((data[6] & 0xff)) {
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 6:
                airData[1] = (byte) (0x40);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[7] & 0x0f);


        airData[2] = (byte) (data[8] & 0xff);
        airData[3] = airData[2];


        airData[5] |= 0x80;

        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {


    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {// default is simple box

    }


    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }

    public void startConnect() {

    }

    public void stopConnect() {

    }

}
