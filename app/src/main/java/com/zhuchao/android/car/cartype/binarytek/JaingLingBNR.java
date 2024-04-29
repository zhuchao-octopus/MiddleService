package com.zhuchao.android.car.cartype.binarytek;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class JaingLingBNR extends Canbox {

    public JaingLingBNR() {
        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarBack((byte) 0x24, (byte) 0x0, (byte) 0x4);
        buildCmdRadarBackEx((byte) 0x1);
        buildCmdAngle((byte) 0x30, (byte) 0x0, 0x15e0);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x23;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x6, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.BT}, {0xb, MyCmd.Keycode.PREVIOUS},
            {0xc, MyCmd.Keycode.NEXT},
    };

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x1e) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0x0) {

        } else {
            data = (byte) (31 + (data & 0xff));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xea));

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


        airData[2] = data[5];
        airData[3] = data[5];


        airData[5] |= 0x80;

        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


    public int getOutTemp(byte[] data) {//
        short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
        return t;
    }

}
