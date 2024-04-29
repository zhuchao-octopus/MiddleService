package com.zhuchao.android.car.cartype.ods;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

public class LuxgenOD extends Canbox {

    public LuxgenOD() {

        buildCmdVersion((byte) 0x7f, (byte) 0x0);

        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        //		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
        //		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
        //		buildCmdAngle((byte) 0x29, (byte) 0x5, 0x1518);


        mIdAC = 0x11;

        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {3};


    private final static byte[][] KEYS_WHEEL = {

            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0xc, MyCmd.Keycode.NEXT}, {0xb, MyCmd.Keycode.PREVIOUS},

            {0x6, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE}, {0x9, MyCmd.Keycode.MULT_SPEECH_AND_BT}, {0xa, MyCmd.Keycode.MULT_MUTE_AND_HANG}, {0x12, MyCmd.Keycode.SPEECH},
            {(byte) 0x80, MyCmd.Keycode.POWER}, {(byte) 0x81, MyCmd.Keycode.RADIO}, {(byte) 0x84, MyCmd.Keycode.BACK}, {(byte) 0x85, MyCmd.Keycode.BT}, {(byte) 0x86, MyCmd.Keycode.NAVIGATION},
            {(byte) 0x87, MyCmd.Keycode.AUDIO}, {(byte) 0x88, MyCmd.Keycode.SETUP}, {(byte) 0x8b, MyCmd.Keycode.SETUP}, {(byte) 0x8c, MyCmd.Keycode.PLAY_PAUSE}, {(byte) 0x8e, MyCmd.Keycode.HOME},
            {0x30, MyCmd.Keycode.VOLUME_UP}, {0x31, MyCmd.Keycode.VOLUME_DOWN}, {0x32, MyCmd.Keycode.PREVIOUS}, {0x33, MyCmd.Keycode.NEXT}, {0x34, MyCmd.Keycode.PREVIOUS}, {0x35, MyCmd.Keycode.NEXT},
            {0x36, MyCmd.Keycode.PREVIOUS}, {0x37, MyCmd.Keycode.NEXT}, {0x38, MyCmd.Keycode.PLAY_PAUSE}, {0x40, MyCmd.Keycode.PREVIOUS}, {0x41, MyCmd.Keycode.NEXT}, {0x42, MyCmd.Keycode.PREVIOUS},
            {0x43, MyCmd.Keycode.NEXT}, {0x44, MyCmd.Keycode.PREVIOUS}, {0x45, MyCmd.Keycode.NEXT}, {0x46, MyCmd.Keycode.PREVIOUS}, {0x47, MyCmd.Keycode.NEXT}, {0x48, MyCmd.Keycode.PREVIOUS},
            {0x49, MyCmd.Keycode.NEXT},


    };

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {

        } else if ((data & 0xff) == 0xff) {
            data = (byte) 0xfa;
        } else if ((data & 0xff) == 30) {
            data = (byte) 0xff;
        } else {
            data = (byte) (34 + (data));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0x48) | ((data[2] & 0x10) << 1) | ((data[2] & 0x02) >> 1));

        switch ((data[3] & 0xff)) {
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 2:
                airData[1] = (byte) (0x60);
                break;
            case 1:
                airData[1] = (byte) (0x40);
                break;
            case 4:
                airData[1] = (byte) (0xa0);
                break;
            case 5:
                airData[1] = (byte) (0x80);
                break;
        }

        airData[1] |= (byte) ((data[4] & 0xf));


        //		airData[4] = (byte) (((data[2] & 0x10) >> 2)
        //				| ((data[7] & 0x33) >> 0));


        airData[2] = (byte) (data[5] & 0xff);
        airData[3] = (byte) (data[6] & 0xff);


        //		airData[5] |= 0x80;
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
