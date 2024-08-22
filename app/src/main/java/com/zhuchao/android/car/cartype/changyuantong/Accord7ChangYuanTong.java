package com.zhuchao.android.car.cartype.changyuantong;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class Accord7ChangYuanTong extends Canbox {

    public Accord7ChangYuanTong() {
        //		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
        //				0x0, 0x0 });
        //		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
        //				0x0, 0x1 });

        buildCmdDoor((byte) 0x24, (byte) 0x0, (byte) 0x3f, (byte) 0x02);
        buildCmdRadarFront((byte) 0x3, (byte) 0x0, (byte) 0x4);
        buildCmdRadarBack((byte) 0x3, (byte) 0x0, (byte) 0x4);
        buildCmdRadarBackEx((byte) 0x4);
        buildCmdAngle((byte) 0x26, (byte) 0x0, 380);
        buildCmdVersion((byte) 0x57, (byte) 0x0);
        mIdAC = 0x55;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;

        if (CarUtil.getModelId() == 69) {
            sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                    0x05, 0x01, 0x0, 0x3, 0x0, 0x0
            });
        }
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.BT_HANG},
    };

    @Override
    public int getAngleValue2(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0x7)) << 8));

        if ((data[3] & 0x8) != 0) {
            angle = -angle;
        }
        return angle;


    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) <= 0xa) {
            data = (byte) 0xfa;
        } else if ((data & 0xff) == 0x10) {
            data = (byte) 0;
        } else {
            //data =
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[9];

        airData[0] = (byte) (0x80 | ((data[6] & 0x01) << 4) | ((data[6] & 0x02) << 5) | ((data[6] & 0x04) << 0) | ((data[6] & 0x20) >> 4) | ((data[6] & 0x40) >> 6) | ((data[6] & 0x80) >> 6));

        if ((data[7] & 0x3) == 1) {
            airData[0] |= 0x20;
        }


        airData[4] = (byte) (((data[7] & 0x08) << 0));

        switch ((data[4] & 0xf0) >> 4) {
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
                airData[1] = (byte) (0xc0);
                break;
            case 6:
                airData[1] = (byte) (0x80);
                break;
            case 7:
                airData[1] = (byte) (0xe0);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);


        airData[2] = (byte) (data[2] & 0xff);
        airData[3] = (byte) (data[3] & 0xff);


        airData[4] = (byte) (((data[5] & 0x03) << 4) | ((data[5] & 0x30) >> 4));


        airData[8] = (byte) (((data[5] & 0x0c) << 2) | ((data[5] & 0xc0) >> 4));


        if (airData[1] == 0) {
            //			Util.zeroBuf(airData);
        }
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
