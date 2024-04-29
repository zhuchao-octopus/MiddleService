package com.zhuchao.android.car.cartype.ods;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;


public class FordQuanXunOD extends Canbox {

    public FordQuanXunOD() {

        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x4);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x4);
        buildCmdAngle((byte) 0x29, (byte) 0x0, 0x2198);
        //		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        //		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x24;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x38, 0x39, 0x41, 0x42};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x6, MyCmd.Keycode.MUTE},
            {0x7, MyCmd.Keycode.MULT_SPEECH_MODE},
    };

    @Override
    public int getAngleValue(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));


        return angle;


    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x40) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0x24) {
            data = 0;
        } else {
            data = (byte) (36 + data - 0x24);
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xec) | ((data[2] & 0x01) << 1) | ((data[2] & 0x02) >> 1));

        switch ((data[3] & 0xff)) {
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
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);


        airData[2] = (byte) (data[5] & 0xff);
        airData[3] = (byte) (data[6] & 0xff);

        airData[5] = (byte) (0x80);

        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }

    public void updateTime() {
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 1;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        if ("12".equals(strTimeFormat)) {

            ampm = 0;

        }

        byte m = (byte) curDate.getMinutes();

        byte[] buf = new byte[]{(byte) 0xc9, 0x03, m, h, ampm};
        sendDataToCanbox(buf, buf.length);

    }


    public int getUpdateTime() {
        return 60000;
    }
}
