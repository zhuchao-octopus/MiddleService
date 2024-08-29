package com.zhuchao.android.car.cartype.hiworld;

import android.provider.Settings;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;

public class TataTAP001Hiworld extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x61};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH},

            {0x8, MyCmd.Keycode.PREVIOUS}, {0x9, MyCmd.Keycode.NEXT},

            {0xc, MyCmd.Keycode.MODLE},

    };
    private final static byte[][] KEYS_WHEEL2 = {{0x2, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT}, {0x6, MyCmd.Keycode.BACK}, {0x9, MyCmd.Keycode.MUTE}, {0x2b, MyCmd.Keycode.HOME}, {0x28, MyCmd.Keycode.BT}, {0x37, MyCmd.Keycode.SETUP}, {0x54, MyCmd.Keycode.NAVIGATION},};
    private final static byte[][] KEYS_WHEEL3 = {{0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0},};

    public TataTAP001Hiworld() {
        //		buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
        buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
        buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x6);
        //		buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 0x4);
        //		buildCmdRadarFrontEx((byte)0x4);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 0, (byte) 2, (byte) 0, KEYS_WHEEL2);

        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    @Override
    public void stopConnect() {

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


        airData[4] = (byte) (((data[3] & 0x1) << 7));
        airData[7] = (byte) (((data[3] & 0x80) >> 7));

        if (((data[3] & 0x18) == 0x10)) {
            airData[4] |= 0x80;
        } else if (((data[3] & 0x18) == 0x08)) {
            airData[0] |= 0x20;
        }


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

        airData[2] = data[8];
        airData[3] = airData[2];

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


    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {
        if (mContext == null) {
            return;
        }
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);


        if ("12".equals(strTimeFormat)) {
        } else {
            ampm = 1;
        }


        byte m = (byte) curDate.getMinutes();

        byte[] buf = new byte[]{0x0a, (byte) 0xcb, 0, h, m, 0, 0, ampm, 0, 0, 0, 0};

        sendDataToCanbox(buf, buf.length);

    }

}
