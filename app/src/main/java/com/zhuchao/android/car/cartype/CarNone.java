package com.zhuchao.android.car.cartype;

import com.zhuchao.android.car.canbox.Canbox;

public class CarNone extends Canbox {

    public CarNone() {

        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, (byte) 0xff, 0x0, 0x0, 0x0});
    }

    public void startConnect() {
    }

    public void stopConnect() {
    }
}
