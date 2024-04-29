package com.zhuchao.android.car.cartype.hiworld;

import android.content.Context;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.zhuchao.android.car.canbox.Canbox;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;

public class BenzG350Hiworld extends Canbox {

    public BenzG350Hiworld() {
        buildCmdVersion((byte) 0xf0, (byte) 0x0);

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);

    }


    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN},

            {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH},


            {0x5, MyCmd.Keycode.BT_DIAL},

            {0x6, MyCmd.Keycode.BT_HANG},

            {0x9, MyCmd.Keycode.NEXT}, {0x8, MyCmd.Keycode.PREVIOUS},

            {0xc, MyCmd.Keycode.MODLE},

            {0xe, MyCmd.Keycode.NEXT}, {0xd, MyCmd.Keycode.PREVIOUS},


            {0xf, MyCmd.Keycode.PLAY_PAUSE},

            {0x10, MyCmd.Keycode.BACK}, {0x50, MyCmd.Keycode.KEY_DISPLAY},

    };

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte type = 7;
        switch (source) {
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                ++play;
                type = 0xd;
                break;
        }

        String s = String.format("%03d/%03d  %02d:%02d:%02d/%02d:%02d:%02d", total, play, total_time / 3600, total_time / 60, total_time % 60, time / 3600, time / 60, time % 60, Locale.ENGLISH);
        sendLcdInfo(type, s, true);


    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        if (source == MyCmd.SOURCE_RADIO) {
            String s;

            if (b[0] >= 0x10) { // am

                if (freq < 1000) {
                    s = String.format(" %d KHz", (freq), Locale.ENGLISH);
                } else {
                    s = String.format(" %d KHz", (freq), Locale.ENGLISH);
                }

                type = 4;
            } else {

                if (freq < 10000) {
                    s = String.format("  %d.%d MHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                } else {
                    s = String.format(" %d.%d MHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                }
                type = 1;
            }

            sendLcdInfo(type, s, true);
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

        sendLcdInfo(s, null, true);
    }

    public void sendLcdInfo(byte index, String num, boolean power) {

        try {
            if (num == null) {
                num = "";
            }
            byte[] n = num.getBytes();

            int num_len = n.length;

            if (num_len >= (30)) {
                num_len = (30);
            }
            byte[] data;

            int len = 31 + 3;

            data = new byte[len];

            data[0] = (byte) (0x20);
            data[1] = (byte) 0x91;
            data[2] = (byte) (power ? 1 : 0);
            data[3] = index;
            //			if (!end) {
            System.arraycopy(n, 0, data, 4, num_len);
            //			} else {
            //				for (int i = 0; i < num_len; ++i) {
            //					data[data.length - i - 1] = n[num_len - i - 1];
            //				}
            //			}

            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

        }
    }

    public void setPhoneEx(int status, String num, String name) {

        byte[] buf = new byte[]{0x01, (byte) 0x97, 1};

        switch (status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:
            case HFP_INFO_CONNECTING:
            case HFP_INFO_CONNECTED:
                buf[2] = 4;
                name = null;
                num = null;
                break;
            case HFP_INFO_CALLED:
                buf[2] = 6;
                break;
            case HFP_INFO_INCOMING:
                buf[2] = 7;
                break;
            case HFP_INFO_CALLING:
                buf[2] = 8;
                break;
            default:
                break;
        }

        if (num == null) {
            num = " ";
        }

        if (name == null) {
            name = " ";
        }

        sendId3((byte) 0x95, num, 0x20);
        //		sendId3((byte) 0x96, name, 0x19, 1);


        sendDataToCanbox(buf, buf.length);

    }

    public void sendId3(byte index, String num, int data_len) {

        try {
            if (num == null) {
                num = " ";
            }
            byte[] n = num.getBytes(StandardCharsets.UTF_8);

            int num_len = n.length;

            if (num_len >= 30) {
                num_len = 30;
            }
            byte[] data;

            data = new byte[0x5d];

            data[0] = (byte) (0x5b);
            data[1] = index;
            data[2] = (byte) 1;
            System.arraycopy(n, 0, data, 3, num_len);

            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

        }
    }

    public void setSongName(String s) {
        sendId3((byte) 0x92, s, 0x20);
    }

    //	public void setSongAlbum(String s) {
    //		sendId3((byte) 3, s, 0x20);
    //	}
    //
    //	public void setSongAritst(String s) {
    //		sendId3((byte) 2, s, 0x19);
    //	}

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }

    public void startConnect() {

        sendLcdInfo((byte) 0x0, null, true);
    }

    @Override
    public void stopConnect() {
        sendLcdInfo((byte) 0xa, null, false);
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

        byte format = 2;

        String date_foramt = SystemConfig.getProperty(mContext, SystemConfig.KEY_DATE_FORMAT);
        if (date_foramt != null) {
            if ("dd/MM/yyyy".equals(date_foramt)) {
                format = 1;
            } else if ("MM/dd/yyyy".equals(date_foramt)) {
                format = 3;
            }
        }


        byte m = (byte) curDate.getMinutes();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{
                0x0a, (byte) 0xcb, 0, h, m, 0, 0, ampm, y, mon, d, format
        };

        sendDataToCanbox(buf, buf.length);

    }
}
