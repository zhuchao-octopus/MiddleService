package com.zhuchao.android.car.cartype.hiworld;

import android.content.Context;
import android.provider.Settings;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;
import java.util.Locale;


public class FordHiworld extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x32, 0x13, 0x34, 0x67, (byte) 0xE8, 0x68, (byte) 0x85, 0x61, (byte) 0xA6};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH}, {0x5, MyCmd.Keycode.MULT_NEXT_AND_HANG}, {0x6, MyCmd.Keycode.MULT_PREV_AND_RECEIVE},
            //		{ 0x7, MyCmd.Keycode },
            {0x8, MyCmd.Keycode.NEXT}, {0x9, MyCmd.Keycode.PREVIOUS}, {0xb, MyCmd.Keycode.MENU}, {0xc, MyCmd.Keycode.KEY_DISPLAY}, {0xd, MyCmd.Keycode.PREVIOUS}, {0xe, MyCmd.Keycode.NEXT}, {0xf, MyCmd.Keycode.PLAY_PAUSE}, {0x17, MyCmd.Keycode.NAVIGATION}, {0x2d, MyCmd.Keycode.AUDIO}, {0x37, MyCmd.Keycode.SETUP}, {0x62, MyCmd.Keycode.PAUSE}, {0x65, MyCmd.Keycode.EJECT},

    };
    private final static byte[][] KEYS_WHEEL2 = {{0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.KEY_SEEK_PREV}, {0x4, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x5, MyCmd.Keycode.EQ}, {0x6, MyCmd.Keycode.BACK}, {0x9, MyCmd.Keycode.MUTE}, {0xa, MyCmd.Keycode.NUMBER1}, {0xb, MyCmd.Keycode.NUMBER2}, {0xc, MyCmd.Keycode.NUMBER3}, {0xd, MyCmd.Keycode.NUMBER4}, {0xe, MyCmd.Keycode.NUMBER5}, {0xf, MyCmd.Keycode.NUMBER6},

            {0x10, MyCmd.Keycode.PLAY_PAUSE},

            {0x11, MyCmd.Keycode.EJECT}, {0x12, MyCmd.Keycode.SETUP}, {0x13, MyCmd.Keycode.TIME_SETTING}, {0x17, MyCmd.Keycode.PREVIOUS}, {0x18, MyCmd.Keycode.NEXT}, {0x19, MyCmd.Keycode.PREVIOUS}, {0x1a, MyCmd.Keycode.NEXT}, {0x1f, MyCmd.Keycode.AUX_IN},


            {0x20, MyCmd.Keycode.NAVIGATION}, {0x21, MyCmd.Keycode.NAVIGATION}, {0x24, MyCmd.Keycode.AUDIO}, {0x28, MyCmd.Keycode.BT}, {0x2a, MyCmd.Keycode.PLAY_PAUSE}, {0x2c, MyCmd.Keycode.MODLE}, {0x2d, MyCmd.Keycode.RADIO}, {0x2e, MyCmd.Keycode.AS}, {0x30, MyCmd.Keycode.NUMBER7}, {0x31, MyCmd.Keycode.NUMBER8}, {0x32, MyCmd.Keycode.NUMBER9}, {0x33, MyCmd.Keycode.NUMBER0}, {0x34, MyCmd.Keycode.NUMBER_STAR}, {0x35, MyCmd.Keycode.NUMBER_POUND},

            //		{ 0x36, MyCmd.Keycode },
            //		{ 0x37, MyCmd.Keycode },
            //		{ 0x38, MyCmd.Keycode },
            //		{ 0x39, MyCmd.Keycode },
            {0x3a, MyCmd.Keycode.DVD}, {0x3b, MyCmd.Keycode.AUDIO}, {0x3c, MyCmd.Keycode.KEY_TURN_A}, {0x3d, MyCmd.Keycode.KEY_TURN_D}, {0x3e, MyCmd.Keycode.AS}, {0x3f, MyCmd.Keycode.HOME}, {0x40, MyCmd.Keycode.KEY_DISPLAY},


            {0x2b, MyCmd.Keycode.HOME}, {0x39, MyCmd.Keycode.POWER}, {0x4b, MyCmd.Keycode.RADIO},};
    private final static byte[][] KEYS_WHEEL3 = {{0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0}, {0x3, MyCmd.Keycode.ROLL_NEXT, 0}, {0x13, MyCmd.Keycode.ROLL_PREV, 0},};
    private final byte[] mLcdInfo = new byte[16];
    private final boolean mShowVolume = false;

    public FordHiworld() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);

        buildCmdAngle((byte) 0x11, (byte) 0x0, 540);


        buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x7);
        buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 0x7);
        buildCmdRadarFrontEx((byte) 0x4);

        buildCmdEQ((byte) 0xa6, (byte) 0x1, 6);
        //		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;
        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 2, KEYS_WHEEL2);

        //		mIdKey2 = 0x040211;
        //		MAP_KEYS2 = KEYS_WHEEL2;


        mIdKey3 = 0x021022;
        MAP_KEYS3 = KEYS_WHEEL3;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    @Override
    public int getAngleValue2(byte[] data) {
        int angle = (short) (((data[8] & 0xff) << 8) | (data[9] & 0xff));
        return -angle;
    }

    public void parseCanboxData(byte[] data, int len) {
        if (data[0] == 0x22) {
            if (data[3] == 0) {
                return;
            } else if (data[3] < 0) {
                data[3] = (byte) (-data[3]);
                data[2] += 0x10;
            }
            parseWheelKey(mIdKey3, data, MAP_KEYS3);
        } else {
            super.parseCanboxData(data, len);
        }
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0x2, 0x24, 0x20, 0x3};
        switch (CarUtil.getModelId()) {
            case 1:
            case 5:
                cmd[2] = 0x9;
                break;
            case 11:
                cmd[2] = 0x14;
                break;
            case 18:
                cmd[2] = 0x16;
                break;
            case 15:
                cmd[2] = 0x1e;
                break;
            case 21:
                cmd[2] = 0x22;
                break;
            case 26:
                cmd[2] = 0x14;
                break;
            case 32:
                cmd[2] = 0x24;
                break;
            case 38:
                cmd[2] = 0x26;
                break;
            case 39:
                cmd[2] = 0x27;
                break;
            case 40:
                cmd[2] = 0x25;
                break;
            case 42:
                cmd[2] = 0x1d;
                break;
            case 51:
                cmd[2] = 0x15;
                break;
            case 10:
            case 19:
            case 27:
            case 28:
            case 29:
                cmd[2] = 0x13;
                break;
        }
        return cmd;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {
            data = (byte) 0xfa;
        } else if ((data & 0xff) == 0xff) {

        } else if ((data & 0xff) == 0xfe) {
            data = 0;
        } else {
            //data =
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[13];

        airData[0] = (byte) ((data[2] & 0x04) | ((data[2] & 0x40) << 1) | ((data[3] & 0x08) >> 0) | ((data[3] & 0x10) << 1) | ((data[3] & 0x01) << 6) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));


        airData[4] = (byte) (((data[3] & 0x40) >> 4) | ((data[4] & 0x03) << 4) | ((data[4] & 0x0c) >> 2) | ((data[10] & 0x80) >> 4));


        airData[5] = (byte) (((data[3] & 0x02) << 3));
        airData[12] = (byte) (((data[3] & 0x20) >> 5));


        airData[8] = (byte) (((data[5] & 0x03) << 4) | ((data[5] & 0x0c) >> 0));

        switch ((data[6] & 0xff)) {
            case 1:
                airData[9] = (byte) (0x1);
                break;
            case 2:
                airData[1] = (byte) (0x80);
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
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[7] & 0x0f);


        airData[2] = (byte) (data[8] & 0xff);
        airData[3] = (byte) (data[9] & 0xff);


        if (data[12] == 0) {
            airData[10] = (byte) 0xfa;
        } else {
            airData[10] = data[12];
        }

        airData[11] = (byte) (data[11] & 0xff);


        airData[5] |= 0x80;

        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        switch (source) {
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                ++play;
                break;
        }

        String s = String.format("%03d      %03d", play, total, Locale.ENGLISH);
        copyLcdInfo(mLcdInfo, s);
        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        if (source == MyCmd.SOURCE_RADIO) {
            String s;
            if (b[0] >= 0x10) { // am
                s = String.format("00 %d 0KHz", (freq), Locale.ENGLISH);
                copyLcdInfo(mLcdInfo, s);
                mLcdInfo[2] = 0x4;
            } else {

                if (freq < 10000) {
                    s = String.format("00  %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                } else {
                    s = String.format("00 %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                }
                copyLcdInfo(mLcdInfo, s);
                mLcdInfo[2] = 0x1;
            }
        }

        if (!mShowVolume) {
            sendDataToCanbox(mLcdInfo, mLcdInfo.length);
        }
    }

    public void setMediaSrc(int source) {// default is simple box
        String s = "";
        switch (source) {
            case MyCmd.SOURCE_RADIO: {
            }
            return;
            case MyCmd.SOURCE_DVD: {
                mLcdInfo[2] = 0x7;
            }
            break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO: {
                mLcdInfo[2] = 0xd;
            }
            break;
            case MyCmd.SOURCE_AUX:
                mLcdInfo[3] = 0xc;
                break;
            case MyCmd.SOURCE_BT_MUSIC:
                mLcdInfo[3] = 0xa;
                break;
        }
        copyLcdInfo(mLcdInfo, s);
        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }

    private void copyLcdInfo(byte[] lcd, String s) {
        byte[] b = s.getBytes();

        for (int i = 0; i < lcd.length - 4; ++i) {
            if (i < b.length) {
                lcd[i + 4] = b[i];
            } else {
                lcd[i + 4] = 0;
            }
        }
    }

    public int getOutTemp(byte[] data) {//
        short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
        return t;
    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }

    public void startConnect() {

        copyLcdInfo(mLcdInfo, "");
        mLcdInfo[0] = 0xe;
        mLcdInfo[1] = (byte) 0x91;
        mLcdInfo[2] = 0;
        //		mLcdInfo[3] |= 0x2;
        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }

    public void stopConnect() {
        copyLcdInfo(mLcdInfo, "");
        mLcdInfo[0] = 0xe;
        mLcdInfo[1] = (byte) 0x91;
        mLcdInfo[2] = 0;
        mLcdInfo[3] &= ~0x2;

        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
        udpateLang();
    }

    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {
        //		Log.d("fk", ">>updateTime");
        if (mContext == null) {
            return;
        }
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);


        if ("12".equals(strTimeFormat)) {
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
            h |= 0x80;
        } else {
            ampm = 1;
        }

        byte m = (byte) curDate.getMinutes();
        //		byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{0x0a, (byte) 0xcb, 1, h, m, 0, 0, ampm, y, mon, d, 0};

        sendDataToCanbox(buf, buf.length);

    }


    public void udpateLang() {
        int lang = -1;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("zh")) {
                if ("CN".equals(Locale.getDefault().getCountry())) {
                    lang = 2;
                } else {
                    lang = 3;
                }
            } else {
                lang = 1;
            }

        }
        if (lang != -1) {
            byte[] buf = {0x2, (byte) 0x9a, 0x1, (byte) lang};
            sendDataToCanbox(buf, buf.length);
        }
    }

    public void sendId3(byte index, String num) {

        try {
            if (num == null) {
                num = "";
            }

            byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

            int num_len = n.length;
            if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
                num_len -= 2;
            }

            if (num_len >= (0x20)) {
                num_len = (0x20);
            }
            byte[] data;

            int len = 0x22;

            data = new byte[len];

            data[0] = (byte) (0x20);
            data[1] = index;
            for (int i = 0; i < num_len; ++i) {
                if (i % 2 == 0) {
                    data[2 + i] = n[i + 3];
                } else {
                    data[2 + i] = n[i + 1];
                }
                //				data[2 + i] = n[i + 2];
            }

            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

        }
    }

    public void setSongName(String s) {
        sendId3((byte) 0x92, s);
    }

    public void setSongAritst(String s) {
        sendId3((byte) 0x96, s);
    }

    public void setSongAlbum(String s) {
        sendId3((byte) 0x93, s);
    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (30 << 16) | (15 << 8) | 15;
        } else {
            byte[] buf = new byte[]{0x2, (byte) 0xad, 0x0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    buf[2] = 6;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    buf[2] = 5;
                    break;
                case EQ_CMD_SET_LOW:
                    buf[2] = 4;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    buf[2] = 3;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    buf[2] = 2;
                    break;
                case EQ_CMD_SET_VOLUME:
                    buf[2] = 1;
                    break;
                default:
                    return 0;
            }

            sendDataToCanbox(buf, buf.length);
        }
        return ret;
    }

    public void parseEQ(int id, byte[] buf) {
        buf[2] = (byte) (buf[2] & 0x3f);
        super.parseEQ(id, buf);
    }
}
