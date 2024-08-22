package com.zhuchao.android.car.cartype.hiworld;

import android.content.Context;
import android.provider.Settings;

import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;

public class BenzMetrisHiworld extends Canbox {

    public BenzMetrisHiworld() {
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
        buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
        buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x7);
        buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 0x7);
        buildCmdRadarFrontEx((byte) 0x4);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 0, (byte) 2, (byte) 0, KEYS_WHEEL2);

        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;


        buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
    }


    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x0, 0x17};
        switch (CarUtil.getModelId()) {
            case 26:
                cmd[2] = 0x9;
                break;
            case 27:
                cmd[2] = 0x19;
                break;
            default:
                return null;
        }
        return cmd;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x32, 0x61, (byte) 0x9b};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH},

            {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG},


            {0x8, MyCmd.Keycode.PREVIOUS}, {0x9, MyCmd.Keycode.NEXT},

            {0xd, MyCmd.Keycode.PREVIOUS}, {0xe, MyCmd.Keycode.NEXT}, {0xf, MyCmd.Keycode.PLAY_PAUSE}, {0x10, MyCmd.Keycode.BACK}, {0x18, MyCmd.Keycode.NAVIGATION}, {0x22, MyCmd.Keycode.HOME},

    };
    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.SETUP}, {0x6, MyCmd.Keycode.BACK},

            {0x28, MyCmd.Keycode.BT}, {0x2b, MyCmd.Keycode.HOME}, {0x2c, MyCmd.Keycode.MODLE}, {0x45, MyCmd.Keycode.VOLUME_UP}, {0x46, MyCmd.Keycode.VOLUME_DOWN},
    };


    private final static byte[][] KEYS_WHEEL3 = {
            {0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0},
    };

    public void parseCanboxData(byte[] data, int len) {
        if ((data[0] & 0xff) == 0xe0) {
            if (data[2] == 0x20) {
                doKey(MyCmd.Keycode.KEY_AM);
            } else if (data[2] == 0x21) {
                doKey(MyCmd.Keycode.KEY_FM);
            }
        } else {
            super.parseCanboxData(data, len);
        }
    }

    @Override
    public int getAngleValue2(byte[] data) {
        int angle = (short) (((data[9] & 0xff) << 8) | (data[8] & 0xff));
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

        airData[0] = (byte) (((data[2] & 0x40) << 1) | ((data[2] & 0x08) << 0) | ((data[2] & 0x01) << 6) | ((data[2] & 0x10) << 1) | ((data[3] & 0x10) << 1) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));

        //		if (((data[3] & 0x10) == 0)) {
        //			airData[0] |= 0x20;
        //		}

        //		airData[4] = (byte) (((data[3] & 0x1) << 7));
        airData[7] = (byte) (((data[2] & 0x04) << 5));

        switch ((data[6] & 0xff)) {

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
        //		byte type = 7;
        //		switch (source) {
        //		case MyCmd.SOURCE_MUSIC:
        //		case MyCmd.SOURCE_VIDEO:
        //			++play;
        //			type = 0xd;
        //			break;
        //		}
        //
        //		String s = String.format("%03d/%03d  %02d:%02d:%02d/%02d:%02d:%02d", total, play,
        //				total_time/3600, total_time/60, total_time%60,time/3600, time/60, time%60,
        //				Locale.ENGLISH);
        //		sendLcdInfo(type, s, true);


    }

    private int mSource;

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
        mSource = source;
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

        byte[] buf = new byte[0x52];
        Util.clearBuf(buf);
        buf[0] = 0x50;
        buf[1] = (byte) 0x94;
        switch (status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:
            case HFP_INFO_CONNECTING:
            case HFP_INFO_CONNECTED:
                name = null;
                num = null;
                break;
            default:
                break;
        }

        try {
            if (num != null) {
                byte[] n = num.getBytes(StandardCharsets.UTF_8);
                int len = n.length;
                if (len > 16) {
                    len = 16;
                }
                System.arraycopy(n, 0, buf, 4, len);
            }

            if (name == null) {
                byte[] n = num.getBytes(StandardCharsets.UTF_8);
                int len = n.length;
                if (len > 16) {
                    len = 16;
                }
                System.arraycopy(n, 0, buf, 24, len);
            }
        } catch (Exception e) {

        }


        sendDataToCanbox(buf, buf.length);

    }

    public void sendId3(byte index, String num, int data_len) {

        try {
            if (num == null) {
                num = " ";
            }
            byte[] n = num.getBytes(StandardCharsets.UTF_8);

            int num_len = n.length;

            if (num_len >= data_len) {
                num_len = data_len;
            }
            byte[] data;

            data = new byte[data_len + 2];

            data[0] = (byte) (data_len);
            data[1] = index;
            //			data[2] = (byte) 1;
            if (num_len >= 0) System.arraycopy(n, 0, data, 2, num_len);

            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

        }
    }


    public void setSongName(String s) {
        sendId3((byte) 0xe3, s, 0x20);
        Util.doSleep(10);
        byte type = 0xd;
        if (mSource == MyCmd.SOURCE_BT_MUSIC) {
            type = 0xa;
        }
        sendLcdInfo(type, s, true);
    }


    public void setSongAritst(String s) {
        sendId3((byte) 0xe2, s, 0x20);
    }

    public void setSongAlbum(String s) {
        //		sendId3((byte) 0xe3, s, 0x20);
    }

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

        String date_foramt = SettingProperties.getProperty(mContext, SettingProperties.KEY_DATE_FORMAT);
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

    public void setContext(Context c) {
        super.setContext(c);

        udpateLang();
    }

    public void udpateLang() {
        int lang = -1;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("zh")) {
                if (!"CN".equals(Locale.getDefault().getCountry())) {
                    lang = 0x1b;
                } else {
                    lang = 2;
                }
            } else if (locale.equals("de")) {
                lang = 3;
            } else if (locale.equals("it")) {
                lang = 4;
            } else if (locale.equals("fr")) {
                lang = 5;
            } else if (locale.equals("sv")) {
                lang = 6;
            } else if (locale.equals("es")) {
                lang = 7;
            } else if (locale.equals("nl")) {
                lang = 8;
            } else if (locale.equals("pt")) {
                lang = 9;
            } else if (locale.equals("nb")) {
                lang = 0xb;
            } else if (locale.equals("fi")) {
                lang = 0xc;
            } else if (locale.equals("da")) {
                lang = 0xd;
            } else if (locale.equals("ar")) {
                lang = 0xf;
            } else if (locale.equals("tr")) {
                lang = 0x10;
            } else if (locale.equals("ko")) {
                lang = 0x11;
            } else if (locale.equals("ru")) {
                lang = 0x12;
            } else if (locale.equals("ro")) {
                lang = 0x14;
            } else if (locale.equals("uk")) {
                lang = 0x16;
            } else if (locale.equals("pl")) {
                lang = 0x17;
            } else if (locale.equals("sk")) {
                lang = 0x18;
            } else if (locale.equals("cs")) {
                lang = 0x19;
            } else if (locale.equals("hu")) {
                lang = 0x1a;
            } else if (locale.equals("sr")) {
                lang = 0x1c;
            } else if (locale.equals("hr")) {
                lang = 0x1f;
            } else if (locale.equals("bg")) {
                lang = 0x20;
            } else if (locale.equals("iw")) {
                lang = 0x22;
            } else {
                lang = 1;
            }

        }
        if (lang != -1) {
            byte[] buf = {0x2, (byte) 0x9a, 0x1, (byte) lang};
            sendDataToCanbox(buf, buf.length);
        }
    }
}
