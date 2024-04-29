package com.zhuchao.android.car.cartype.simple;

import com.zhuchao.android.car.canbox.Canbox;

public class CarCIVIC extends Canbox {

    private void parseWheelKey(byte[] data, int len) {
        if (doKeyStudy(data[2], data[3])) {
            return;
        }
        switch (data[2]) {
            case 0x00: {
                doKey(0, 0);
            }
            break;
            case 0x01: {
                //vol+
            }
            break;
            case 0x02: {
                //vol-
            }
            break;
            case 0x03: {
                doKey(KEY_NEXTSONG, data[3]);
            }
            break;
            case 0x04: {
                doKey(KEY_PREVIOUSSONG, data[3]);
            }
            break;
            case 0x07:        //src
            {
                doKey(KEY_SOURCE, data[3]);
            }
            break;
            case 0x08:         //speech
            {

            }
            break;
            case 0x09:            //pickup
            {

            }
            break;
            case 0x0a:          //hangup
            {

            }
            break;
            case 0x0b:          //speech hold
            {

            }
            break;
            case 0x0c:        //pickup hold
            {

            }
            break;
            case 0x0d:        //hangup hold
            {
            }
            break;
        }
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0x20) {
            parseWheelKey(data, len);
        }
    }

}
