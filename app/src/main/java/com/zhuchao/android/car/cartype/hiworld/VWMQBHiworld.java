package com.zhuchao.android.car.cartype.hiworld;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.fbase.MMLog;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class VWMQBHiworld extends Canbox {
    private final String TAG = "VWMQBHiworld";

    public VWMQBHiworld() {
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
        buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
        buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0xf0);
        mIdAC = 0x31;
        mIdKey = 0x040211;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
        MMLog.d(TAG, "NEW VWMQBHiworld CANBOX");
    }

    public void startConnect() {

    }

    @Override
    public void stopConnect() {

    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x35, 0x45, 0x46, 0x47, 0x48, 0x49, 0x67, 0x68, 0x69, 0x64, 0x76, 0x1f, 0x1e, 0x75, 0x77, 0x74, 0x36, 0x48, (byte) 0x85, (byte) 0x87, (byte) 0x88, (byte) 0xc1, (byte) 0xf0, (byte) 0xc2, (byte) 0x1f, (byte) 0xe8,};

    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH}, {0x5, MyCmd.Keycode.BT_HANG},
            //		{ 0x6, MyCmd.Keycode },
            //		{ 0x7, MyCmd.Keycode },
            {0x8, MyCmd.Keycode.NEXT}, {0x9, MyCmd.Keycode.PREVIOUS}, {0xa, MyCmd.Keycode.MODLE}, {0xb, MyCmd.Keycode.MODLE}, {0xc, MyCmd.Keycode.MODLE},
            //		{ 0xd, MyCmd.Keycode },
    };

    @Override
    public int getAngleValue(byte[] data) {
        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));
        return angle;
    }

    @Override
    public int getACTemp(byte data, int unit) {//

        if (unit == 1) {
            if ((data & 0xff) == 0xff) {

            } else if ((data & 0xff) == 0xfe) {
                data = 0;
            } else {
                data = (byte) ((data & 0xff) / 2);
            }
        } else {
            if ((data & 0xff) == 0xff) {

            } else if ((data & 0xff) == 0xfe) {
                data = 0;
            } else {
                // data =
            }
        }
        return data;
    }

    public void parseACInfo(byte[] data) {
        byte[] airData = new byte[14];
        airData[0] = (byte) ((data[2] & 0x08) | ((data[2] & 0x40) << 1) | ((data[3] & 0x40) >> 0) | ((data[3] & 0x10) << 1));
        airData[4] = (byte) (((data[2] & 0x20) >> 3) | ((data[3] & 0x80) >> 4) | ((data[4] & 0x03) << 4) | ((data[4] & 0x0c) >> 2));
        //		airData[5] = (byte) (((data[3] & 0x20) >> 5));
        if (((data[3] & 0x20) == 0)) {
            airData[5] = 0x1;
        }

        airData[7] = (byte) (((data[2] & 0x04) << 5));
        airData[8] = (byte) (((data[4] & 0x30) >> 0) | ((data[4] & 0xc0) >> 4));
        airData[12] = (byte) (((data[5] & 0x10) >> 4));
        airData[13] = (byte) (((data[3] & 0x0f)));

        switch ((data[6] & 0xff)) {
            case 1:
                airData[9] = (byte) (0x1);
                break;
            case 2:
                airData[0] |= (byte) (0x2);
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
        airData[2] = data[8];
        airData[3] = data[9];

        switch ((data[11] & 0x0f)) {
            case 3:
                airData[11] = (byte) (0x20);
                break;
            case 5:
                airData[11] |= (byte) (0x60);
                break;
            case 6:
                airData[11] = (byte) (0x40);
                break;
        }

        airData[11] |= (byte) (((data[11] & 0xf0) >> 4));
        airData[9] |= (byte) (((data[2] & 0x02) >> 0));
        airData[10] = (byte) getACTemp(data[12], airData[5] & 0x1);
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    @SuppressLint("DefaultLocale")
    public void setMediaSrc(int source, byte type, byte[] b) {
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        if (source == MyCmd.SOURCE_RADIO) {
            String s;
            if (b[0] >= 0x10) { // am
                s = " " + freq + " KHz";
            } else {
                s = String.format("%d.%02d", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                if (freq < 10000) {
                    s = " " + s + " MHz";
                }
            }
            setSongName(s);
        }

    }

    public void setMediaSrc(int source) {
        if (source != MyCmd.SOURCE_MUSIC) {
            if (source != MyCmd.SOURCE_RADIO) {
                setSongName(" ");
            }
            setSongAritst(" ");
            setSongAlbum(" ");
        }
    }

    public void sendId3(byte index, String num, int data_len, int reserve) {

        try {
            if (num == null) {
                num = " ";
            }
            byte[] n = num.getBytes(StandardCharsets.UTF_8);

            int num_len = n.length;

            if (num_len >= (data_len - reserve)) {
                num_len = (data_len - reserve);
            }
            byte[] data;

            int len = data_len + 2;

            data = new byte[len];

            data[0] = (byte) (data_len);
            data[1] = index;
            for (int i = 0; i < num_len; ++i) {
                data[2 + reserve + i] = n[i];

            }

            sendDataToCanbox(data, data.length);
        } catch (Exception ignored) {
        }
    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }

    public void setSongName(String s) {
        sendId3((byte) 0x91, s, 0x1a, 2);
    }

    public void setSongAritst(String s) {
        sendId3((byte) 0x92, s, 0x19, 1);
    }

    public void setSongAlbum(String s) {
        sendId3((byte) 0x93, s, 0x19, 1);
    }


    public void setPhoneEx(int status, String num, String name) {
        switch (status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:
            case HFP_INFO_CONNECTING:
            case HFP_INFO_CONNECTED:
                name = null;
                num = null;

                break;
            case HFP_INFO_CALLED:
            case HFP_INFO_INCOMING:
            case HFP_INFO_CALLING:

                break;
        }

        if (num == null) {
            num = " ";
        }

        if (name == null) {
            name = " ";
        }

        sendId3((byte) 0x95, num, 0x19, 1);
        sendId3((byte) 0x96, name, 0x19, 1);
    }


    public int getOutTemp(byte[] data) {//
        short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
        return t;
    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
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

        byte format = 2;

        String date_foramt = SettingProperties.getProperty(mContext, SettingProperties.KEY_DATE_FORMAT);
        if (date_foramt != null) {
            if ("dd/MM/yyyy".equals(date_foramt)) {
                format = 1;
            } else if ("MM/dd/yyyy".equals(date_foramt)) {
                format = 3;
            }
        }

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

        Calendar now = Calendar.getInstance();
        int zone = now.getTimeZone().getDefault().getRawOffset();
        zone = zone / 3600000;
        //		int offsetInMillis = now.getTimeZone().getOffset(now.getTimeInMillis());
        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        //		Log.d("cccc", ""+curDate.getYear());
        byte y = (byte) (curDate.getYear() - 100 + 208);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{0x0a, (byte) 0xcb, 1, h, m, 0, (byte) zone, ampm, y, mon, d, format};
        sendDataToCanbox(buf, buf.length);
    }

    @Override
    public void touchInReverse(int x, int y, int w, int h) {
        super.touchInReverse(x, y, w, h);
        MMLog.d(TAG, "touchInReverse x:" + x + " y:" + y + " w:" + w + " h:" + h);
    }

    @Override
    public void touchInReverseEx(int x, int y, int w, int h, int down) {
        super.touchInReverseEx(x, y, w, h, down);
        MMLog.d(TAG, "touchInReverse x:" + x + " y:" + y + " w:" + w + " h:" + h + " down:" + down);
    }
}
