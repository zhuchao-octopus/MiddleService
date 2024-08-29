package com.zhuchao.android.car.cartype.hiworld;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class ShangQiSAP003Hiworld extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {(byte) 0x87};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN},

            {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH},

            {0x5, MyCmd.Keycode.MULT_MUTE_AND_BT},

            {0x6, MyCmd.Keycode.BT_HANG},

            {0xb, MyCmd.Keycode.MODLE}, {0x9, MyCmd.Keycode.NEXT}, {0x8, MyCmd.Keycode.PREVIOUS},

    };
    private final static byte[][] KEYS_WHEEL2 = {{0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT}, {0x6, MyCmd.Keycode.BACK}, {0x9, MyCmd.Keycode.MUTE}, {0x20, MyCmd.Keycode.NAVIGATION}, {0x2b, MyCmd.Keycode.HOME}, {0x2c, MyCmd.Keycode.MODLE}, {0x2f, MyCmd.Keycode.MENU},

    };
    private final static byte[][] KEYS_WHEEL3 = {{0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0},};

    public ShangQiSAP003Hiworld() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
        //		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
        //		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 2, KEYS_WHEEL2);

        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    @Override
    public void stopConnect() {

    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x0, 0x0};
        switch (CarUtil.getModelId()) {
            case 0:
                cmd[2] = 1;
                break;
            case 6:
                cmd[2] = 2;
                break;
            case 7:
                cmd[2] = 5;
                break;
            case 1:
                cmd[2] = 3;
                break;
            case 10:
                cmd[2] = 4;
                break;
            case 11:
                cmd[2] = 6;
                break;
            default:
                return null;
        }
        return cmd;
    }

    @Override
    public int getAngleValue(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));


        return angle;


    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0xff || (data & 0xff) == 0xfa) {

        } else if ((data & 0xff) == 0xfe) {
            data = 0;
        } else {

        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[10];

        airData[0] = (byte) (((data[2] & 0x40) << 1) | ((data[2] & 0x01) << 6) | ((data[3] & 0x08) << 1) | ((data[3] & 0x04) << 0) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));

        if (((data[3] & 0x10) == 0)) {
            airData[0] |= 0x20;
        }

        airData[4] = (byte) (((data[2] & 0x20) >> 3));

        switch ((data[6] & 0xff)) {
            case 1:
                airData[9] = (byte) (0x1);
                break;
            case 2:
                //			airData[1] = (byte) (0x20);
                break;
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 5:
                airData[1] = (byte) (0x60);
                break;
            case 6:
                airData[1] = (byte) (0x40);
                break;
            case 0xb:
                airData[1] = (byte) (0x80);
                break;
            case 0xc:
                airData[1] = (byte) (0xa0);
                break;
            case 0xd:
                airData[1] = (byte) (0xc0);
                break;
            case 0xe:
                airData[1] = (byte) (0xe0);
                break;
        }

        airData[1] |= (byte) (data[7] & 0x0f);

        airData[2] = data[8];
        airData[3] = data[9];

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {

    }


    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }

    public void startConnect() {

    }
}
