package com.zhuchao.android.car.autotest;

import com.zhuchao.android.car.canbox.Canbox;


public class McuTest extends Canbox {

    public final static int CMD_ILL = 0x4;

    public McuTest() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});

        sendCmd(0x1, 0x0);
    }

    public void sendCmd(int cmd, int param) {
        byte[] data = new byte[3];
        data[0] = (byte) cmd;
        data[1] = (byte) 1;
        data[2] = (byte) param;

        sendDataToCanbox(data, data.length);

    }

    public void sendCmd(int cmd, int param, int param2) {
        byte[] data = new byte[4];
        data[0] = (byte) cmd;
        data[1] = (byte) 2;
        data[2] = (byte) param;
        data[3] = (byte) param2;

        sendDataToCanbox(data, data.length);

    }

    // public void parseCanboxData(byte[] data, int len) {
    // }

}
