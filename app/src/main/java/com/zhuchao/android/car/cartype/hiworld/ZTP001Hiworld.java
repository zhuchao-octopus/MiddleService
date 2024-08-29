package com.zhuchao.android.car.cartype.hiworld;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

public class ZTP001Hiworld extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x37};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN},

            {0x3, MyCmd.Keycode.MUTE},


            {0x5, MyCmd.Keycode.BT_DIAL},

            {0x6, MyCmd.Keycode.BT_HANG},

            {0x9, MyCmd.Keycode.NEXT}, {0x8, MyCmd.Keycode.PREVIOUS},

            {0xc, MyCmd.Keycode.MODLE},


    };


    public ZTP001Hiworld() {

        buildCmdRepeatSendCarType(getCarTypeCmd(), 5);

        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);

        buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);

        mIdAC = 0x31;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x0, 0x20};
        switch (CarUtil.getModelId()) {
            case 0:
                cmd[2] = 1;
                break;
            case 1:
                cmd[2] = 2;
                break;
        }
        return cmd;
    }

    @Override
    public int getAngleValue2(byte[] data) {
        int angle = (short) (((data[8] & 0xff) << 8) | (data[9] & 0xff));
        return -angle;
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

        airData[0] = (byte) (((data[2] & 0x40) << 1) | ((data[2] & 0x08) << 0) | ((data[3] & 0x10) << 1) | ((data[3] & 0x40) << 0) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));

        //		if (((data[3] & 0x10) == 0)) {
        //			airData[0] |= 0x20;
        //		}

        airData[4] = (byte) (((data[3] & 0x1) << 7));
        airData[7] = (byte) (((data[3] & 0x80) >> 7));

        switch ((data[6] & 0xff)) {
            case 1:
                airData[9] = (byte) (0x1);
                break;
            case 2:
                // airData[1] = (byte) (0x20);
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
        airData[1] |= (byte) ((data[2] & 0x80) >> 3);

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

    @Override
    public void stopConnect() {
    }
}
