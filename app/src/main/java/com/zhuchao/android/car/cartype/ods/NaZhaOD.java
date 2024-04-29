package com.zhuchao.android.car.cartype.ods;

import com.zhuchao.android.car.canbox.Canbox;


public class NaZhaOD extends Canbox {

    public NaZhaOD() {
        mIdAC = 0x28;
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xf8, (byte) 0x2);
        buildCmdAngle((byte) 0x29, (byte) 0x3, 7800);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);

    }


    public int getAngleValue2(byte[] data) {


        short angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));


        return angle;
    }


    @Override
    public int getACTemp(byte data) {
        if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0) {
        } else {

            data = (byte) (36 + (data - 0x1));

        }
        return data & 0xff;
    }


    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[8];
        airData[0] = (byte) (((data[2] & 0xef)));
        airData[1] = (byte) ((data[3] & 0xff));
        airData[2] = data[4];
        airData[3] = data[5];


        airData[5] = (byte) ((data[6] & 0x01));
        airData[4] = (byte) (((data[2] & 0x10) >> 2));
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {//default is simple box

    }


}
