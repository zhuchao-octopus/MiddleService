package com.zhuchao.android.car.cartype.hiworld;

import android.content.Context;
import android.provider.Settings;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;

public class BenzB200Hiworld extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x61};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN},

            {0x3, MyCmd.Keycode.MUTE},


            {0x5, MyCmd.Keycode.BT_DIAL},

            {0x6, MyCmd.Keycode.BT_HANG}, {0xa, MyCmd.Keycode.MODLE},

            {0xe, MyCmd.Keycode.NEXT}, {0xd, MyCmd.Keycode.PREVIOUS},


            {0xf, MyCmd.Keycode.PLAY_PAUSE}, {0x10, MyCmd.Keycode.BACK}, {0x16, MyCmd.Keycode.PREVIOUS}, {0x17, MyCmd.Keycode.NEXT}, {0x18, MyCmd.Keycode.SPEECH},

    };
    private String mVersion8 = "";
    private String mVersion32 = "";


    public BenzB200Hiworld() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        buildCmdDoor((byte) 0x73, (byte) 0x2, (byte) 0xf8, (byte) 0x07);
        buildCmdAngle((byte) 0x72, (byte) 0x0, 35);
        // buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x73;

        buildCmdKey((byte) 0x72, (byte) 1, (byte) 4, (byte) 0, KEYS_WHEEL);
        //	buildCmdKey((byte) 0x74, (byte) 4, (byte) 2, (byte) 2, KEYS_WHEEL2);

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    @Override
    public void stopConnect() {

    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0xd3, 0x0, 0x0};
        if (CarUtil.getModelId() == 25) {
            cmd[2] = 1;
        } else {
            return null;
        }
        return cmd;
    }

    @Override
    public int getAngleValue2(byte[] data) {
        int angle = 0;
        if ((data[6] & 0xff) > 0 && (data[6] & 0xff) <= 35) {
            angle = (data[6] & 0xff);
        } else if ((data[7] & 0xff) > 0 && (data[7] & 0xff) <= 35) {
            angle = -(data[7] & 0xff);
        }

        return angle;

    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub

        if ((data & 0xff) < 30) {
            data = 0;
        } else {

        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[10];

        airData[0] = (byte) (((data[2] & 0x40) << 1) | ((data[2] & 0x0c) << 0) | ((data[2] & 0x10) << 1) | ((data[3] & 0x40) << 0) | ((data[3] & 0x20) >> 5) | ((data[3] & 0x10) >> 3));

        //		if (((data[2] & 0x10) == 0)) {
        //			airData[0] |= 0x20;
        //		}


        airData[1] = (byte) (((data[6] & 0x40) >> 1) | ((data[6] & 0x20) << 1) | ((data[6] & 0x10) << 3) | ((data[6] & 0x0f) << 0));

        airData[6] = (byte) (((data[7] & 0x40) >> 1) | ((data[7] & 0x20) << 1) | ((data[7] & 0x10) << 3) | ((data[7] & 0x0f) << 0));

        airData[2] = data[4];
        airData[3] = data[5];

        airData[5] |= 0x88;
        super.parseACInfo(airData);
    }

    private String parseVersionString(byte[] data) {

        int len = 0;
        if (len == 0) {
            len = data.length - 3;
            if (data[data.length - 2] == 0) {
                len--;
            }
        }
        byte[] version;
        version = new byte[len];
        Util.byteArrayCopy(version, data, 0, 2, version.length);
        return (new String(version));

    }

    public void parseCanboxData(byte[] data, int len) {
        switch (data[0]) {

            case 0x72:
                if (data.length > 15) {
                    mRadar[0] = radarChangeStyle(data[8], 7, 0);
                    mRadar[1] = radarChangeStyle(data[9], 7, 0);
                    mRadar[2] = radarChangeStyle(data[10], 7, 0);
                    mRadar[3] = radarChangeStyle(data[11], 7, 0);
                    mRadar[4] = radarChangeStyle(data[12], 7, 0);
                    mRadar[5] = radarChangeStyle(data[13], 7, 0);
                    mRadar[6] = radarChangeStyle(data[14], 7, 0);
                    mRadar[7] = radarChangeStyle(data[15], 7, 0);
                    parseRadar();
                }
                super.parseCanboxData(data, len);
                break;
            case (byte) 0xf0:
                mVersion32 = parseVersionString(data);
                mVersion = mVersion8 + " v32:" + mVersion32;
                break;
            case (byte) 0xf1:
                mVersion8 = parseVersionString(data);
                mVersion = mVersion8 + " v32:" + mVersion32;
                break;
            default:
                super.parseCanboxData(data, len);
        }
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

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{0x0a, (byte) 0xcb, 0, h, m, 0, 0, ampm, y, mon, d, 0};

        sendDataToCanbox(buf, buf.length);

    }
}
