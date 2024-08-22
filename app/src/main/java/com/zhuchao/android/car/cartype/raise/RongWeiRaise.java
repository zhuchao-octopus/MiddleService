package com.zhuchao.android.car.cartype.raise;

import android.provider.Settings;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;


public class RongWeiRaise extends Canbox {

    public RongWeiRaise() {
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x9, (byte) 1);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x9, (byte) 1);
        buildCmdAngle((byte) 0x29, (byte) 0x0, 0);
        buildCmdOutTemp((byte) 0x21, (byte) 0x10);
        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x21;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
    }


    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x6, MyCmd.Keycode.MULT_MUTE_AND_BT},
            {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.BACK}, {0x9, MyCmd.Keycode.EQ}, {0xa, MyCmd.Keycode.KEY_TURN_A}, {0xb, MyCmd.Keycode.KEY_TURN_D}, {0xe, MyCmd.Keycode.PREVIOUS},
            {0xf, MyCmd.Keycode.NEXT}, {0x23, MyCmd.Keycode.PLAY_PAUSE}, {0x60, MyCmd.Keycode.SPEECH},
    };

    @Override
    public int getAngleValue(byte[] data) {

        int angle = ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

        angle = angle - 0x8000;
        int max = 0x9fff - 0x8000;
        angle = ((angle * 3000) / max);

        if (angle > -50 && angle < 50) {
            angle = 50;
        }

        return angle;


    }


    private int getACTempPriv(int data) {
        if ((data & 0xff) == 0x0) {
            data = (byte) 0;
        } else if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else {
            data = (byte) (36 + ((data & 0x7f) - 1));
        }
        return data & 0xff;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xff));

        switch ((data[3] & 0xf0)) {
            case 0x80:
                airData[1] = (byte) (0x60);
                break;
            case 0x40:
                airData[1] = (byte) (0x40);
                break;
            case 0x20:
                airData[1] = (byte) (0x20);
                break;
            case 0x10:
                airData[1] = (byte) (0xa0);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[3] & 0x0f);


        airData[3] = (byte) (0xfa);

        if ((data[4] & 0x80) == 0) {
            airData[7] = 0x40;
            airData[2] = data[4];
            if ((data[4] & 0x7f) == 0x1f) {
                airData[2] = (byte) 0xff;
            }
        } else {
            airData[2] = (byte) getACTempPriv(data[4] & 0x7f);
        }

        airData[5] = (byte) ((data[6] & 0x01));
        airData[7] |= (byte) (((data[6] & 0x10) >> 4) | ((data[6] & 0x80) >> 2) | ((data[6] & 0x00)));
        airData[4] |= (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x8) >> 1) | ((data[6] & 0x40) >> 3));
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


    public int getOutTemp(byte[] data) {//
        int t = ((data[7] & 0x7f)) * 10;
        if ((data[7] & 0x80) != 0) {
            t = -t;
        }
        return t;
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
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        if ("12".equals(strTimeFormat)) {
            h |= 0x80;
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
        }

        byte m = (byte) curDate.getMinutes();
        //		byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();

        byte[] buf = new byte[]{(byte) 0xa6, 0x07, y, mon, d, h, m, 0, 0};
        sendDataToCanbox(buf, buf.length);
    }
}
