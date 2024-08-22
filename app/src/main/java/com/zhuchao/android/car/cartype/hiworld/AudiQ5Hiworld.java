package com.zhuchao.android.car.cartype.hiworld;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

public class AudiQ5Hiworld extends Canbox {

    public AudiQ5Hiworld() {
        buildCmdDoor((byte) 0x73, (byte) 0x2, (byte) 0xf8, (byte) 0x09);
        buildCmdAngle((byte) 0x72, (byte) 0x0, 0xfe);

        buildCmdRadarBack((byte) 0x21, (byte) 0x0, (byte) 0xfe);
        buildCmdRadarFront((byte) 0x72, (byte) 0x0, (byte) 0xfe);
        buildCmdRadarFrontEx((byte) 4);

        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;

        buildCmdKey((byte) 0x1, (byte) 5, (byte) 5, (byte) 0, KEYS_WHEEL);
    }


    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN},

            {0x3, MyCmd.Keycode.MUTE},


            {0x9, MyCmd.Keycode.NEXT}, {0x8, MyCmd.Keycode.PREVIOUS},

            {0xb, MyCmd.Keycode.SPEECH}, {0xc, MyCmd.Keycode.PLAY_PAUSE}, {0x10, MyCmd.Keycode.MODLE}, {0x11, MyCmd.Keycode.NAVIGATION},


    };


    @Override
    public int getAngleValue2(byte[] data) {
        int angle = 0;
        if ((data[6] & 0xff) > 0 && (data[6] & 0xff) <= 0xfe) {
            angle = (data[6] & 0xff);
        } else if ((data[7] & 0xff) > 0 && (data[7] & 0xff) <= 0xfe) {
            angle = -(data[7] & 0xff);
        }

        return angle;

    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub

        if ((data & 0xff) == 0xfe) {
            data = (byte) 0;
        } else if ((data & 0xff) == 0xff) {

        } else {

        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[10];

        airData[0] = (byte) (((data[2] & 0x01) << 7)

        );


        switch ((data[5] & 0xff)) {
            case 1:
                airData[1] = (byte) (0xa0);
                break;
            case 2:
                airData[1] = (byte) (0x80);
                break;
            case 3:
                airData[1] = (byte) (0xc0);
                break;
            case 4:
                airData[1] = (byte) (0x40);
                break;
            case 5:
                airData[1] = (byte) (0x60);
                break;
            case 6:
                airData[1] = (byte) (0x20);
                break;
            case 7:
                airData[1] = (byte) (0xe0);
                break;
            default:
                airData[1] = 0;
                break;
        }
        switch ((data[9] & 0xff)) {
            case 1:
                airData[6] = (byte) (0xa0);
                break;
            case 2:
                airData[6] = (byte) (0x80);
                break;
            case 3:
                airData[6] = (byte) (0xc0);
                break;
            case 4:
                airData[6] = (byte) (0x40);
                break;
            case 5:
                airData[6] = (byte) (0x60);
                break;
            case 6:
                airData[6] = (byte) (0x20);
                break;
            case 7:
                airData[6] = (byte) (0xe0);
                break;
            default:
                airData[6] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);
        airData[2] = data[3];
        airData[3] = data[7];

        if (data[10] >= 3) {
            data[10] = 3;
        }
        if (data[6] >= 3) {
            data[6] = 3;
        }
        airData[4] = (byte) (((data[10] & 0x03) << 0) | ((data[6] & 0x03) << 4));

        airData[5] |= 0x88;
        super.parseACInfo(airData);
    }


    public void startConnect() {

    }

    public void stopConnect() {

    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {


    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {// default is simple box

    }


    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword2(data, len);
    }


}
