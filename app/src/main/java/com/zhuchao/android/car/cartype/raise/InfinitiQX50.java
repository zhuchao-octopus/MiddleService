package com.zhuchao.android.car.cartype.raise;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class InfinitiQX50 extends Canbox {

    public InfinitiQX50() {
        //		buildCmdRepeatSendCarType(getCarTypeCmd());
        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xf0, (byte) 0x02);
        //		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
        //		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
        //		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
        //		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        //		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x21;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x24;
        MAP_KEYS2 = KEYS_WHEEL2;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x71, 0x72, 0x29, 0x27,};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x4, MyCmd.Keycode.NEXT}, {0x3, MyCmd.Keycode.PREVIOUS},


            {0x7, MyCmd.Keycode.MODLE}, {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG},


            {0x12, MyCmd.Keycode.SPEECH}, {0x15, MyCmd.Keycode.PLAY_PAUSE}, {0x16, MyCmd.Keycode.SPEED_UP},

    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x3, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x4, MyCmd.Keycode.KEY_RADIO_PS}, {0x5, MyCmd.Keycode.KEY_RADIO_SCAN},
            {0x6, MyCmd.Keycode.KEY_SEEK_PREV}, {0x7, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x8, MyCmd.Keycode.AUDIO}, {0xa, MyCmd.Keycode.ROLL_PREV}, {0x9, MyCmd.Keycode.ROLL_NEXT},
            {0xb, MyCmd.Keycode.NUMBER1}, {0xc, MyCmd.Keycode.NUMBER2}, {0xd, MyCmd.Keycode.NUMBER3}, {0xe, MyCmd.Keycode.NUMBER4}, {0xf, MyCmd.Keycode.NUMBER5}, {0x10, MyCmd.Keycode.NUMBER6},
    };

    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x00) {
            data = (byte) 0xfa;
        } else {
            //			data = (byte)(37 + ((data&0xff) - 0x25));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0x4c) | ((data[2] & 0x02) >> 1) | ((data[2] & 0x10) << 1));


        airData[4] = (byte) (((data[2] & 0x20) << 2));


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
        airData[3] = data[6];

        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


}
