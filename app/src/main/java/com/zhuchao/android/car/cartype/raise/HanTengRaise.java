package com.zhuchao.android.car.cartype.raise;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class HanTengRaise extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x60};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x6, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE}, {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG},};
    private final static byte[][] KEYS_WHEEL2 = {{0x2, MyCmd.Keycode.NEXT}, {0x1, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.MODLE}, {0x4, MyCmd.Keycode.AUDIO}, {0x5, MyCmd.Keycode.MUTE}, {0x6, MyCmd.Keycode.VOLUME_DOWN}, {0x7, MyCmd.Keycode.VOLUME_UP}, {0x8, MyCmd.Keycode.MULT_MUTE_AND_POWER}, {0x9, MyCmd.Keycode.HOME}, {0xa, MyCmd.Keycode.BACK}, {0xb, MyCmd.Keycode.SETUP}, {0xc, MyCmd.Keycode.BT}, {0xd, MyCmd.Keycode.NAVIGATION}, {0xe, MyCmd.Keycode.RADIO}, {0x11, MyCmd.Keycode.PLAY_PAUSE},
            //		{ 0x12, MyCmd.Keycode. },
            {0x13, MyCmd.Keycode.KEY_REPEAT}, {0x14, MyCmd.Keycode.KEY_SHUFFLE}, {0x15, MyCmd.Keycode.KEY_TURN_D}, {0x16, MyCmd.Keycode.KEY_TURN_A},

            //this for OD jingyiX5 low config

            {0x31, MyCmd.Keycode.VOLUME_UP}, {0x32, MyCmd.Keycode.VOLUME_DOWN}, {0x33, MyCmd.Keycode.PREVIOUS}, {0x34, MyCmd.Keycode.NEXT}, {0x35, MyCmd.Keycode.KEY_SEEK_PREV}, {0x36, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x37, MyCmd.Keycode.EQ}, {0x38, MyCmd.Keycode.SETUP}, {0x39, MyCmd.Keycode.AS},};

    public HanTengRaise() {
        //		buildCmdRepeatSendCarType(getCarTypeCmd());
        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xf0, (byte) 0x02);
        //		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
        //		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
        //		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
        //		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        //		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x24;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x21;
        MAP_KEYS2 = KEYS_WHEEL2;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    public byte getACTemp1(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x24) {
            data = (byte) 0x0;
        } else if ((data & 0xff) == 0x40) {
            data = (byte) 0xff;
        } else {
            data = (byte) (37 + ((data & 0xff) - 0x25));
        }
        return data;
    }

    public byte getACTemp2(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x24) {
            data = (byte) 0x0;
        } else if ((data & 0xff) == 0x40) {
            data = (byte) 0xff;
        } else {
            data = (byte) (37 + ((data & 0xff) - 0x25));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xe8) | ((data[2] & 0x02) >> 1) | ((data[2] & 0x01) << 1));


        airData[4] = (byte) (((data[9] & 0x80) >> 5) | ((data[7] & 0x33) << 0));

        airData[7] = (byte) (((data[2] & 0x04) << 5));


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
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);


        if ((data[8] & 0x80) != 0) {
            airData[7] |= 0x40;

            airData[2] = getACTemp2((byte) (data[8] & 0x7f));
            airData[3] = airData[2];
        } else {

            airData[2] = getACTemp1((byte) (data[5] & 0x7f));
            airData[3] = getACTemp1((byte) (data[6] & 0x7f));
        }

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


}
