package com.zhuchao.android.car.cartype.hiworld;

import android.content.Context;
import android.provider.Settings;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;
import java.util.Locale;

public class BentengFWP008Hiworld extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.MULT_SPEECH_AND_BT},

            {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG},

            {0x8, MyCmd.Keycode.PREVIOUS}, {0x9, MyCmd.Keycode.NEXT},

            {0xa, MyCmd.Keycode.MODLE}, {0xc, MyCmd.Keycode.MODLE}, {0xd, MyCmd.Keycode.PREVIOUS}, {0xe, MyCmd.Keycode.NEXT},

    };
    private final static byte[][] KEYS_WHEEL2 = {{0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT}, {0x9, MyCmd.Keycode.MUTE}, {0xa, MyCmd.Keycode.NUMBER1}, {0xb, MyCmd.Keycode.NUMBER2}, {0xc, MyCmd.Keycode.NUMBER3}, {0xd, MyCmd.Keycode.NUMBER4}, {0xe, MyCmd.Keycode.NUMBER5}, {0xf, MyCmd.Keycode.NUMBER6}, {0x15, MyCmd.Keycode.AS}, {0x16, MyCmd.Keycode.PLAY_PAUSE}, {0x24, MyCmd.Keycode.AUDIO}, {0x28, MyCmd.Keycode.BT}, {0x33, MyCmd.Keycode.RADIO},

            {0x39, MyCmd.Keycode.KEY_DISPLAY}, {0x3e, MyCmd.Keycode.EASY_CONNECT}, {0x3f, MyCmd.Keycode.SPEECH}, {0x43, MyCmd.Keycode.AS},};
    private final static byte[][] KEYS_WHEEL3 = {{0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0},};

    public BentengFWP008Hiworld() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
        buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
        buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x3);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 0, (byte) 2, (byte) 0, KEYS_WHEEL2);

        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;

    }

    @Override
    public void stopConnect() {

    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x9, 0xf};//x80
        if (CarUtil.getModelId() == 15) {
            cmd[2] = 0x11;
        }
        return cmd;
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

        //		if (((data[3] & 0x10) == 0)) {
        //			airData[0] |= 0x20;
        //		}

        airData[4] = (byte) (((data[4] & 0x3) << 4) | ((data[4] & 0xc) >> 2));

        airData[8] = (byte) (((data[5] & 0x3) << 4) | ((data[5] & 0xc) >> 0));


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
        airData[3] = data[9];

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte type = 7;
        switch (source) {
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                ++play;
                type = 0xd;
                break;
        }

        String s = String.format("%03d      %03d", play, total, Locale.ENGLISH);
        sendLcdInfo(type, s, false);


    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        if (source == MyCmd.SOURCE_RADIO) {
            String s;

            if (b[0] >= 0x10) { // am

                if (freq < 1000) {
                    s = String.format("000 %d 0KHz", (freq), Locale.ENGLISH);
                } else {
                    s = String.format("00 %d 0KHz", (freq), Locale.ENGLISH);
                }

                type = 4;
            } else {

                if (freq < 10000) {
                    s = String.format("00  %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                } else {
                    s = String.format("00 %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                }
                type = 1;
            }

            sendLcdInfo(type, s, false);
        }
    }

    public void setMediaSrc(int source) {
        byte s;
        switch (source) {
            //		case MyCmd.SOURCE_RADIO:
            //			return;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0xd;
                break;
            case MyCmd.SOURCE_BT:
                s = (byte) 0xa;
                break;
            case MyCmd.SOURCE_AUX:
                s = (byte) 0xc;
                break;
            case MyCmd.SOURCE_AV_OFF:
                s = 0;
            default:
                return;
        }

        sendLcdInfo(s, null, false);
    }

    public void sendLcdInfo(byte index, String num, boolean end) {

        try {
            if (num == null) {
                num = "";
            }
            byte[] n = num.getBytes();

            int num_len = n.length;

            if (num_len >= (12)) {
                num_len = (12);
            }
            byte[] data;

            int len = 12 + 3;

            data = new byte[len];

            data[0] = (byte) (13);
            data[1] = (byte) 0x91;
            data[2] = index;
            if (!end) {
                System.arraycopy(n, 0, data, 3, num_len);
            } else {
                for (int i = 0; i < num_len; ++i) {
                    data[data.length - i - 1] = n[num_len - i - 1];
                }
            }

            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

        }
    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }

    public void startConnect() {
        byte[] buf = new byte[]{0x3, (byte) 0x6a, 0x5, 1, (byte) 0xf0};

        sendDataToCanbox(buf, buf.length);
    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
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

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{0x0a, (byte) 0xcb, 0, h, m, 0, 0, ampm, y, mon, d, 0};

        sendDataToCanbox(buf, buf.length);

    }
}
