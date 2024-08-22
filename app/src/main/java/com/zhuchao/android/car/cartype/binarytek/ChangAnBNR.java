package com.zhuchao.android.car.cartype.binarytek;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class ChangAnBNR extends Canbox {

    public ChangAnBNR() {
        buildCmdRepeatSendCarType(getCarTypeCmd());
        buildCmdDoor((byte) 0x3a, (byte) 0x4, (byte) 0x3f, (byte) 0x02);
        buildCmdRadarFront((byte) 0x23, (byte) 0x1, (byte) 0x3);
        buildCmdRadarBack((byte) 0x22, (byte) 0x1, (byte) 0x3);
        buildCmdAngle((byte) 0x29, (byte) 0x0, 0x19c0);

        buildCmdVersion((byte) 0xff, (byte) 0x0);
        mIdAC = 0x21;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {(byte) 0xd2, 0x13, 0x3a, 0x50, 0x66, 0x68, (byte) 0xd2, 0xa};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.PREVIOUS}, {0x4, MyCmd.Keycode.NEXT}, {0x5, MyCmd.Keycode.MUTE}, {0x6, MyCmd.Keycode.MODLE},
            {0x7, MyCmd.Keycode.BT_DIAL}, {0x8, MyCmd.Keycode.BT_HANG}, {0x9, MyCmd.Keycode.SPEECH},
    };

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0xe2, 0x01, 0};
        switch (CarUtil.getModelId()) {
            case 1:
                switch (CarUtil.getCarTypeConfig()) {
                    case 0:
                        cmd[2] = 0;
                        break;
                    case 1:
                        cmd[2] = 1;
                        break;
                    case 2:
                        cmd[2] = 2;
                        break;
                }
                break;
            case 3:
            case 6:
            case 35:
                cmd[2] = 4;
                break;
            case 34:
                cmd[2] = 5;
                break;
            case 5:
                switch (CarUtil.getCarTypeConfig()) {
                    case 0:
                        cmd[2] = (byte) 0x80;
                        break;
                    case 1:
                        cmd[2] = (byte) 0x81;
                        break;
                    case 2:
                        cmd[2] = (byte) 0x82;
                        break;
                }
                break;
            default:
                return null;
        }

        return cmd;
    }

    @Override
    public int getAngleValue2(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));


        return angle;


    }

    private final byte[] airData = new byte[12];

    private void parseSeatHeat(byte[] data) {

        airData[4] &= ~0x33;

        airData[4] |= (byte) (((data[2] & 0x03) << 4) | ((data[3] & 0x03) >> 0));

        airData[8] = (byte) (((data[2] & 0x30) << 0) | ((data[3] & 0x30) >> 2));

        super.parseACInfo(airData);

    }

    public void parseCanboxData(byte[] data, int len) {
        if (data[0] == 0x13) {
            parseSeatHeat(data);
            super.parseCanboxData(data, len);
        } else {
            super.parseCanboxData(data, len);
        }
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((CarUtil.getCarTypeConfig()) != 0) {
            if ((data & 0xff) == 0) {

            } else if ((data & 0xff) == 0xff) {

            } else if ((data & 0xff) >= 1 && (data & 0xff) <= 0x1d) {
                data = (byte) (36 + ((data & 0xff) - 1));
            } else {
                // data =
            }
        }

        return data;
    }

    public void parseACInfo(byte[] data) {

        airData[0] = (byte) ((data[2] & 0x13) | ((data[2] & 0x04) << 3) | ((data[2] & 0x08) << 3) | ((data[2] & 0x20) >> 3));


        switch ((data[4] & 0xff)) {
            case 0:
                airData[1] = (byte) (0x40);
                break;
            case 1:
                airData[1] = (byte) (0x60);
                break;
            case 2:
                airData[1] = (byte) (0x20);
                break;
            case 3:
                airData[1] = (byte) (0x80);
                break;
            case 4:
                airData[1] = (byte) (0xa0);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[3] & 0x0f);


        airData[2] = (byte) (data[5] & 0xff);
        airData[3] = (byte) (data[6] & 0xff);


        airData[4] &= ~0x4;
        airData[4] |= (byte) (((data[2] & 0x40) >> 4));

        if ((data[3] & 0x0f) > 0) {
            airData[0] |= (byte) (0x80);
        }

        if ((CarUtil.getCarTypeConfig()) == 0) {
            airData[7] = (byte) (0x40);
        }

        airData[5] = (byte) (0x80);
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


}
