package com.zhuchao.android.car.cartype.hiworld;

import com.common.utils.AppConfig;
import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;
import java.util.Locale;


public class Honda003Hiworld extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {(byte) 0xa4};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG}, {0x8, MyCmd.Keycode.NEXT}, {0x9, MyCmd.Keycode.PREVIOUS}, {0xa, MyCmd.Keycode.MODLE}, {0xb, MyCmd.Keycode.SPEECH},

    };
    private final byte[] mLcdInfo = new byte[15];
    private final byte[] mLcdInfoSend = new byte[15];
    private final boolean mShowVolume = false;


    public Honda003Hiworld() {
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);

        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;
        buildCmdKey((byte) 0x72, (byte) 1, (byte) 4, (byte) 0, KEYS_WHEEL);


        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
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


        byte[] airData = new byte[8];

        airData[0] = (byte) (((data[2] & 0x40) << 1) | ((data[3] & 0x08) >> 0) | ((data[3] & 0x80) >> 6) | ((data[3] & 0x01) << 6));

        if ((data[3] & 0x10) == 0) {
            airData[0] |= 0x20;
        }

        switch ((data[6] & 0xff)) {
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 6:
                airData[1] = (byte) (0x40);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[7] & 0x0f);


        airData[2] = (byte) (data[8] & 0xff);
        airData[3] = airData[2];


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
        mLcdInfo[2] = 0xd;
        String s = String.format("%03d %02d%02d", play, time / 60, time % 60, Locale.ENGLISH);
        copyLcdInfo(mLcdInfo, s);
        sendLcdInfo();
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
            sendLcdInfo();
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
                if (!"com.canboxsetting/com.canboxsetting.JeepCarCDPlayerActivity".equals(AppConfig.getTopActivity())) {
                    mLcdInfo[3] = 0xc;
                } else {
                    mLcdInfo[3] = 0x15;
                }
                break;
            case MyCmd.SOURCE_BT_MUSIC:
                mLcdInfo[3] = 0xa;
                break;
        }
        copyLcdInfo(mLcdInfo, s);
        sendLcdInfo();
    }

    private void copyLcdInfo(byte[] lcd, String s) {
        byte[] b = s.getBytes();

        for (int i = 0; i < lcd.length - 3; ++i) {
            if (i < b.length) {
                lcd[i + 3] = b[i];
            } else {
                lcd[i + 3] = 0;
            }
        }
    }

    public void startConnect() {

        copyLcdInfo(mLcdInfo, "");
        mLcdInfo[0] = 0xd;
        mLcdInfo[1] = (byte) 0xe1;
        sendLcdInfo();
    }

    public void stopConnect() {
        copyLcdInfo(mLcdInfo, "");
        mLcdInfo[2] = 0;

        sendLcdInfo();
    }

    private void sendLcdInfo() {
        if (!Util.isBufEquals(mLcdInfo, mLcdInfoSend)) {
            Util.byteArrayCopy(mLcdInfoSend, mLcdInfo, 0, 0, mLcdInfo.length);
            sendDataToCanbox(mLcdInfo, mLcdInfo.length);
        }

    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword2(data, len);
    }


    public boolean isSupportCompass() {
        return true;
    }

    public void updateCompass(int compass) {
        int direction = compassAngleToDirect(compass) + 1;

        byte[] buf = new byte[]{0x1, (byte) (0xb6), (byte) (direction & 0xff)};

        sendDataToCanbox(buf, buf.length);
    }

    public int getUpdateTime() {
        return 1000000;
    }

    public void updateTime() {
        if (mContext == null) {
            return;
        }
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);


        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        byte[] buf = new byte[]{0x03, (byte) 0xb5, h, m, s};

        sendDataToCanbox(buf, buf.length);

    }

}
