package com.zhuchao.android.car.cartype.ods;

import android.provider.Settings;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;

public class DongFengOD extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {(byte) 0x29, 0x27};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x5, MyCmd.Keycode.BT}, {0x6, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.SPEECH}, {0xb, MyCmd.Keycode.PREVIOUS}, {0xc, MyCmd.Keycode.NEXT},

            {0x10, MyCmd.Keycode.HOME}, {0x11, MyCmd.Keycode.BACK}, {0x12, MyCmd.Keycode.AUDIO}, {0x13, MyCmd.Keycode.KEY_DISPLAY}, {0x14, MyCmd.Keycode.PREVIOUS}, {0x15, MyCmd.Keycode.NEXT}, {0x16, MyCmd.Keycode.KEY_FM}, {0x17, MyCmd.Keycode.POWER}, {0x18, MyCmd.Keycode.KEY_AM}, {0x19, MyCmd.Keycode.AUDIO}, {0x1a, MyCmd.Keycode.BT}, {0x1b, MyCmd.Keycode.SETUP}, {0x1c, MyCmd.Keycode.EQ}, {0x1d, MyCmd.Keycode.PREVIOUS}, {0x1e, MyCmd.Keycode.NEXT}, {0x1f, MyCmd.Keycode.PREVIOUS}, {0x20, MyCmd.Keycode.NEXT}, {0x21, MyCmd.Keycode.PLAY_PAUSE}, {0x22, MyCmd.Keycode.ROLL_PREV}, {0x23, MyCmd.Keycode.ROLL_NEXT}, {0x24, MyCmd.Keycode.KEY_360}, {0x32, MyCmd.Keycode.NAVIGATION},


    };
    private final int mRadarBack2;

    public DongFengOD() {
        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        if (CarUtil.getModelId() == 0 || CarUtil.getModelId() == 1 || CarUtil.getModelId() == 15 || CarUtil.getModelId() == 8) {
            buildCmdRadarBack((byte) 0x24, (byte) 0x1, (byte) 120, (byte) 2);
        } else {

            buildCmdRadarBack((byte) 0x25, (byte) 0x0, (byte) 0xa);
        }
        buildCmdAngle((byte) 0x30, (byte) 0x0, 0x2198);
        buildCmdOutTemp((byte) 0x36, (byte) 0x1);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x23;
        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;

        mRadarBack2 = buildCmdRadar((byte) 0x24, (byte) 0x0, (byte) 255, (byte) 2);
    }

    @Override
    public int getAngleValue(byte[] data) {

        int angle = ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));

        int max = (0x35f6 - 0x1e8a);
        angle = angle - 0x1e8a;

        return angle;

    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x11 || (data & 0xff) == 0x7e) {
            data = (byte) 0;
        } else if ((data & 0xff) == 0x20 || (data & 0xff) == 0x9c) {
            data = (byte) 0xff;
        } else if ((data & 0xff) >= 0x12 && (data & 0xff) <= 0x1e) {
            data = (byte) (36 + ((data & 0xff) - 0x12) * 2);
        } else if ((data & 0xff) >= 0x81 && (data & 0xff) <= 0x9b) {
            data = (byte) (37 + ((data & 0xff) - 0x81));
        } else {
            data = (byte) 0xfa;
        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xe8) | ((data[2] & 0x02) >> 1) | ((data[2] & 0x01) << 1));

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
            case 5:
                airData[1] = (byte) (0x80);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);

        airData[2] = (byte) (data[5] & 0xff);
        airData[3] = airData[2];

        airData[4] = (byte) (((data[2] & 0x04) << 1));
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0x24) {
            parseRadarBack(mRadarBack2, data);
        } else {
            super.parseCanboxData(data, len);
        }
    }

    // public int getOutTemp(byte[] data) {//
    // short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
    // return t;
    // }

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
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
        }

        int year = curDate.getYear() - 100;

        byte m = (byte) ((curDate.getMinutes() & 0xff) << 2);
        byte s = (byte) ((curDate.getSeconds() & 0xff) << 2);

        byte y = (byte) ((((year & 0xff))) << 2);
        byte mon = (byte) ((((curDate.getMonth() + 1)) & 0xff) << 4);
        byte d = (byte) ((curDate.getDate() & 0xff) << 3);
        h = (byte) ((h & 0xff) << 3);

        byte[] buf = new byte[]{(byte) 0xb2, 0x06, s, m, h, d, mon, y,};
        sendDataToCanbox(buf, buf.length);
    }

}
