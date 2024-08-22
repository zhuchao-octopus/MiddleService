package com.zhuchao.android.car.cartype.raise;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class ChangChengFengJun6Raise extends Canbox {

    public ChangChengFengJun6Raise() {
        mIdAC = 0x23;
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        buildCmdOutTemp((byte) 0x23, (byte) 0x0);
        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;
    }


    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x6, KEY_MUTE}, {0x7, KEY_SOURCE}, {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG},

            {0xd, KEY_NEXTSONG}, {0xe, KEY_PREVIOUSSONG},


    };

    public int getOutTemp(byte[] data) {//
        int t = CarUtil.CLEAR_OUT_DOOR_TEMP;
        if (data.length > 6) {

            t = (((data[7] & 0x7f))) * 10;
            if ((data[7] & 0x80) != 0) {
                t = -t;
            }
        }
        return t;
    }

    @Override
    public int getACTemp(byte data) {
        if (data == (byte) 0x1f) {
            data = (byte) 0xff;
        } else if (data == 0) {
            data = 0;
        } else if (data >= 1 && data <= 0xf) {
            data = (byte) (34 + (data) * 2);
        } else {
            data = (byte) 0xfa;
        }

        return data & 0xff;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[8];
        airData[0] = (byte) ((data[2] & 0xc8));

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
            case 6:
                airData[1] = (byte) (0xc0);
                break;
            case 7:
                airData[1] = (byte) (0xe0);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);

        airData[2] = data[5];
        airData[3] = data[6];


        airData[5] |= 0x80;

        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {// default is simple box

    }

}
