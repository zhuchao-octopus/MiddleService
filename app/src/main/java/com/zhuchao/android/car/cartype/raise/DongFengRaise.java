package com.zhuchao.android.car.cartype.raise;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;

public class DongFengRaise extends Canbox {

    public DongFengRaise() {
        buildCmdRepeatSendCarType(getCarTypeCmd());
        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        if (CarUtil.getModelId() == 0 || CarUtil.getModelId() == 1 || CarUtil.getModelId() == 15 || CarUtil.getModelId() == 8) {
            buildCmdRadarBack((byte) 0x24, (byte) 0x1, (byte) 120, (byte) 2);
        } else {

            buildCmdRadarBack((byte) 0x25, (byte) 0x0, (byte) 0xa);
        }
        buildCmdRadarFront((byte) 0x26, (byte) 0x0, (byte) 0xa);
        buildCmdAngle((byte) 0x30, (byte) 0x0, 0x2198);
        buildCmdOutTemp((byte) 0x36, (byte) 0x1);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x23;
        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;

        mRadarBack2 = buildCmdRadar((byte) 0x24, (byte) 0x0, (byte) 255, (byte) 2);
        setVoiceSupportRaise();
    }

    private final int mRadarBack2;
    private final static byte[] IDS_TO_CANBOXSETTING = {(byte) 0x29, 0x27};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x5, MyCmd.Keycode.BT}, {0x6, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.SPEECH},
            {0xb, MyCmd.Keycode.KEY_SEEK_PREV}, {0xc, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x32, MyCmd.Keycode.NAVIGATION}, {(byte) 0x80, MyCmd.Keycode.RADIO}, {(byte) 0x81, MyCmd.Keycode.HOME},
            {(byte) 0x82, MyCmd.Keycode.BACK}, {(byte) 0x83, MyCmd.Keycode.AUDIO}, {(byte) 0x84, MyCmd.Keycode.KEY_DISPLAY}, {(byte) 0x85, MyCmd.Keycode.PREVIOUS}, {(byte) 0x86, MyCmd.Keycode.KEY_FM},
            {(byte) 0x87, MyCmd.Keycode.POWER}, {(byte) 0x88, MyCmd.Keycode.NEXT}, {(byte) 0x89, MyCmd.Keycode.KEY_AM}, {(byte) 0x8a, MyCmd.Keycode.VIDEO}, {(byte) 0x8b, MyCmd.Keycode.BT},
            {(byte) 0x8c, MyCmd.Keycode.SETUP}, {(byte) 0x8d, MyCmd.Keycode.EQ}, {(byte) 0x8e, MyCmd.Keycode.KEY_360}, {(byte) 0x8f, MyCmd.Keycode.AS}, {(byte) 0x90, MyCmd.Keycode.AUDIO},
            {(byte) 0x91, MyCmd.Keycode.MENU}, {(byte) 0x92, MyCmd.Keycode.NEXT}, {(byte) 0x93, MyCmd.Keycode.PREVIOUS}, {(byte) 0x94, MyCmd.Keycode.KEY_SEEK_NEXT},
            {(byte) 0x95, MyCmd.Keycode.KEY_SEEK_PREV}, {(byte) 0x96, MyCmd.Keycode.PLAY_PAUSE}, {(byte) 0x97, MyCmd.Keycode.MUTE}, {(byte) 0x98, MyCmd.Keycode.EQ},
            {(byte) 0x99, MyCmd.Keycode.PLAY_PAUSE}, {(byte) 0xf2, MyCmd.Keycode.VOLUME_ROLL_UP}, {(byte) 0xf1, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {(byte) 0xf3, MyCmd.Keycode.ROLL_PREV},
            {(byte) 0xf4, MyCmd.Keycode.ROLL_NEXT},
    };

    private byte[] getCarTypeCmd() {
        if (CarUtil.getCatelId() == 40) {
            byte[] cmd = new byte[]{(byte) 0xee, 0x02, (byte) 0xa0, 0};
            switch (CarUtil.getModelId()) {
                case 2:
                    cmd[3] = 1;
                    break;
                case 3:
                    cmd[3] = 0;
                    break;
                case 6:
                    cmd[3] = 2;
                    break;
                case 7:
                    cmd[3] = 3;
                    break;
                default:
                    return null;
            }
            return cmd;
        } else {
            byte[] cmd = new byte[]{(byte) 0xee, 0x02, (byte) 0xa1, 0};
            switch (CarUtil.getModelId()) {
                case 36:
                    cmd[3] = 1;
                    break;
                case 37:
                    cmd[3] = 2;
                    break;
                case 38:
                    cmd[3] = 3;
                    break;
                case 39:
                    cmd[3] = 4;
                    break;
                default:
                    return null;
            }
            return cmd;
        }
    }

    @Override
    public int getAngleValue(byte[] data) {

        int angle = ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));

        int max = (0x2ad0 - 0x1568);
        if (CarUtil.getCatelId() == 28 || CarUtil.getModelId() == 10) {
            max = (0x35f6 - 0x1e8a);
            angle = angle - 0x1e8a;
        } else {
            angle = angle - 0x1568;
        }

        angle = ((angle * 3000) / max);
        if (angle > -50 && angle < 50) {
            angle = 50;
        }

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

        airData[4] = (byte) (((data[6] & 0x08) >> 1) | ((data[2] & 0x04) << 1));
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
