package com.zhuchao.android.car.cartype.hiworld;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

public class ChangAnCNP002Hiworld extends Canbox {

    public ChangAnCNP002Hiworld() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
        buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
        buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x3);
        buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 0x3);
        buildCmdRadarFrontEx((byte) 0x4);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 0, (byte) 2, (byte) 0, KEYS_WHEEL2);


        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }


    @Override
    public void stopConnect() {

    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x48, (byte) 0x87, (byte) 0x78};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH},

            {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG},


            {0x8, MyCmd.Keycode.MULT_PREV_AND_HANG}, {0x9, MyCmd.Keycode.MULT_NEXT_AND_RECEIVE},

            {0xc, MyCmd.Keycode.MODLE}, {0x18, MyCmd.Keycode.NAVIGATION},


            {0xd, MyCmd.Keycode.PREVIOUS}, {0xe, MyCmd.Keycode.NEXT},

    };
    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT}, {0x2b, MyCmd.Keycode.HOME}, {0x2d, MyCmd.Keycode.AUDIO}, {0x45, MyCmd.Keycode.VOLUME_UP},
            {0x46, MyCmd.Keycode.VOLUME_DOWN},

            {0x9, MyCmd.Keycode.MUTE}, {0x33, MyCmd.Keycode.RADIO},
    };


    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x0, 0x13};
        switch (CarUtil.getModelId()) {
            case 1:
                cmd[2] = 1;
                break;
            case 2:
                cmd[2] = 2;
                break;
            case 14:
            case 15:
            case 31:
                cmd[2] = 3;
                break;
            case 4:
            case 32:
                cmd[2] = 4;
                break;
            case 33:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 6;
                } else {
                    cmd[2] = 5;
                }
                break;
            case 8:
                cmd[2] = 7;
                break;
            case 29:
                cmd[2] = 8;
                break;
            case 16:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0xa;
                } else {
                    cmd[2] = 9;
                }
                break;
            case 17:
                cmd[2] = 0xb;
                break;
            case 21:
                if (CarUtil.getCarTypeConfig() == 2) {
                    cmd[2] = 0xe;
                } else {
                    cmd[2] = 0xd;
                }
                break;
            case 26:
                cmd[2] = 0xf;
                break;
        }
        return cmd;
    }

    @Override
    public int getAngleValue2(byte[] data) {
        int angle = (short) (((data[9] & 0xff) << 8) | (data[8] & 0xff));
        return -angle;
    }

    private int getACTempPriv(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 31) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 1) {
            data = 0;
        } else if ((data & 0xff) >= 2 && (data & 0xff) <= 29) {
            data = (byte) (34 + (data & 0xff));
        } else {
            data = (byte) 0xfa;
        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[10];

        airData[0] = (byte) (((data[2] & 0x40) << 1) | ((data[2] & 0x08) << 0) | ((data[2] & 0x01) << 6) | ((data[3] & 0x10) << 1) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));

        //		if (((data[3] & 0x10) == 0)) {
        //			airData[0] |= 0x20;
        //		}

        airData[4] = (byte) (((data[2] & 0x20) >> 3) | ((data[2] & 0x10) >> 1));
        airData[7] = (byte) (((data[2] & 0x04) << 5));

        switch ((data[6] & 0xff)) {
            case 2:
                airData[1] = (byte) (0x80);
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
            case 0xc:
                airData[1] = (byte) (0xa0);
                break;
        }

        airData[1] |= (byte) (data[7] & 0x0f);

        //		if (data[8] >= 1 && data[8] <= 16) {
        //			airData[7] |= 0x40;
        //		}


        if (CarUtil.getModelId() == 15) {
            airData[7] |= 0x40;
            airData[2] = data[8];
        } else {
            airData[2] = (byte) getACTempPriv(data[8]);
        }

        airData[3] = (byte) 0xfa;

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
        byte[] buf = new byte[]{0x3, (byte) 0x6a, 0x5, 1, (byte) 0xf0};

        sendDataToCanbox(buf, buf.length);
    }

}
