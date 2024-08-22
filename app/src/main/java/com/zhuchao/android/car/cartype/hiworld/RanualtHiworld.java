package com.zhuchao.android.car.cartype.hiworld;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;
import java.util.Locale;


public class RanualtHiworld extends Canbox {

    public RanualtHiworld() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 2);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
        buildCmdAngle((byte) 0x11, (byte) 0x0, 0x1518);

        buildCmdOutTemp((byte) 0x31, (byte) 0x10);
        buildCmdRadarBack((byte) 0x41, (byte) 0x1, (byte) 0x4);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 2, KEYS_WHEEL2);

        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
        CarUtil.m360UI = 0;
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x0, 0};
        switch (CarUtil.getModelId()) {
            case 9:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x11;
                } else if (CarUtil.getCarTypeConfig() == 1) {
                    cmd[2] = 0x12;
                } else {
                    cmd[2] = 0x13;
                }
                break;
            case 29:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x14;
                } else if (CarUtil.getCarTypeConfig() == 1) {
                    cmd[2] = 0x15;
                } else {
                    cmd[2] = 0x16;
                }
                break;
            case 3:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x17;
                } else if (CarUtil.getCarTypeConfig() == 1) {
                    cmd[2] = 0x18;
                } else {
                    cmd[2] = 0x19;
                }
                break;
            case 35:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x1a;
                } else if (CarUtil.getCarTypeConfig() == 1) {
                    cmd[2] = 0x1b;
                } else {
                    cmd[2] = 0x1c;
                }
                break;
            case 34:
                cmd[2] = 0x1d;
                break;
        }
        return cmd;
    }

    private final static byte[][] KEYS_WHEEL3 = {
            {0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0},
    };

    private final Handler mHandlerSendLcd = new Handler() {
        public void handleMessage(Message msg) {

            if (mLcdType != -1) {
                sendLcdInfo(mLcdType, mLcdString, false);
                startSendLcd();
            }

            super.handleMessage(msg);
        }
    };

    private void startSendLcd() {
        stoptSendLcd();
        //		mHandlerSendLcd.sendEmptyMessageDelayed(0, 1000);
    }

    private void stoptSendLcd() {
        mHandlerSendLcd.removeMessages(0);
    }

    public void startConnect() {
        //startSendLcd();
    }

    @Override
    public void stopConnect() {

    }

    private final static byte[] IDS_TO_CANBOXSETTING = {
            (byte) 0x13, (byte) 0x14, (byte) 0x1b, 0x26, 0x31, 0x61, 0x62
    };

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MULT_MUTE_AND_BT}, {0x4, MyCmd.Keycode.NAVIGATION}, {0x7, MyCmd.Keycode.SPEECH},

            {0x8, MyCmd.Keycode.NEXT}, {0x9, MyCmd.Keycode.PREVIOUS}, {0xd, MyCmd.Keycode.PREVIOUS}, {0xe, MyCmd.Keycode.NEXT}, {0x10, MyCmd.Keycode.MULT_SOURCE_AND_BT}, {0x11, MyCmd.Keycode.MODLE},
            {0x18, MyCmd.Keycode.SPEECH},

    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x4, MyCmd.Keycode.MENU}, {0x6, MyCmd.Keycode.BACK}, {0x9, MyCmd.Keycode.MUTE}, {0x12, MyCmd.Keycode.SETUP}, {0x16, MyCmd.Keycode.PLAY_PAUSE},
            {0x17, MyCmd.Keycode.PREVIOUS}, {0x18, MyCmd.Keycode.NEXT}, {0x19, MyCmd.Keycode.PREVIOUS}, {0x1a, MyCmd.Keycode.NEXT}, {0x20, MyCmd.Keycode.NAVIGATION}, {0x21, MyCmd.Keycode.NAVIGATION},
            {0x25, MyCmd.Keycode.KEY_REPEAT}, {0x2b, MyCmd.Keycode.HOME}, {0x31, MyCmd.Keycode.DARK}, {0x37, MyCmd.Keycode.SETUP}, {0x39, MyCmd.Keycode.BRIGHTNESS}, {0x54, MyCmd.Keycode.NAVIGATION},
            {0x45, MyCmd.Keycode.VOLUME_UP}, {0x46, MyCmd.Keycode.VOLUME_DOWN},


            {0x5b, MyCmd.Keycode.HOME}, {0x5c, MyCmd.Keycode.BACK},
    };

    @Override
    public int getAngleValue(byte[] data) {

        int angle = (short) ((data[9] & 0xff) | (((data[8] & 0x7f)) << 8));
        if ((data[8] & 0x80) != 0) {
            angle = -angle;
        }

        return angle;


    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0xff) {

        } else if ((data & 0xff) == 0xfe) {
            data = 0;
        } else {
            //data =
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[10];

        airData[0] = (byte) ((data[2] & 0x0c) | ((data[2] & 0x40) << 1) | ((data[2] & 0x1) << 6) | ((data[3] & 0x44) >> 0) | ((data[3] & 0x10) << 1) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));


        airData[4] = (byte) (((data[2] & 0x20) >> 3) | ((data[3] & 0x08) << 4));

        switch ((data[6] & 0xff)) {
            case 1:
                airData[9] = (byte) (0x1);
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

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void parseCanboxData(byte[] data, int len) {
        //	case 0x22:
        //		if (data[3] == 0) {
        //			return;
        //		} else if (data[3] < 0) {
        //			data[3] = (byte) (-data[3]);
        //			data[2] += 0x10;
        //		}
        //		parseWheelKey(mIdKey3, data, MAP_KEYS3);
        //		break;
        if (data[0] == (byte) 0xe8) {
            if (CarUtil.getCarTypeConfig() == 2 || CarUtil.getModelId() == 11) {
                int arg = 0;
                switch (data[3] & 0xf) {
                    case 5:
                        arg = 3;
                        break;
                    case 6:
                        arg = 4;
                        break;
                    case 7:
                        arg = 2;
                        break;
                    case 8:
                        arg = 1;
                        break;
                }

                Handler handler = getHandler("Reverse");
                if (null != handler) {

                    handler.sendMessage(handler.obtainMessage(CANBOX_DACIA_UI_DATA, arg, 0));
                }


                CarUtil.m360UI = ((data[5] & 0xff) << 8 | arg);
            } else {
                CarUtil.m360UI = 0;
            }
        } else {
            super.parseCanboxData(data, len);
        }
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte type = 0xd;
        switch (source) {
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                ++play;
                break;
        }

        String s = String.format("00%d %02d%02d   ", play % 10, time / 60, time % 60, Locale.ENGLISH);

        startSendLcd();
        sendLcdInfo(type, s, false);


    }

    @Override
    public void setVolume(int volume) {

        startSendLcd();
        sendLcdInfo((byte) 0x20, String.format("VOL %02d", volume, Locale.ENGLISH), false);
    }

    byte mLcdType = -1;
    String mLcdString;

    public void setMediaSrc(int source, byte type, byte[] b) {
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        if (source == MyCmd.SOURCE_RADIO) {
            String s;

            if (b[0] >= 0x10) { // am

                //				if (freq < 1000){
                //					s = String.format("000 %d 0KHz", (freq), Locale.ENGLISH);
                //				} else {
                s = String.format("00 %d 0KHz", (freq), Locale.ENGLISH);
                //				}

                type = 4;
            } else {

                if (freq < 10000) {
                    s = String.format("00  %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                } else {
                    s = String.format("00 %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                }
                type = 1;
            }

            mLcdType = type;
            mLcdString = s;
            startSendLcd();
            sendLcdInfo(type, s, false);
        }
    }

    public void setMediaSrc(int source) {
        byte s;
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                return;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                return;
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
            data[1] = (byte) 0xe1;
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


    public void sendId3(byte index, String num, int data_len, int reserve) {

        try {
            if (num == null) {
                num = " ";
            }

            byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

            int num_len = n.length;
            if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
                num_len -= 2;
            }

            if (num_len >= (data_len)) {
                num_len = (data_len);
            }
            byte[] data;

            int len = data_len + 2;

            data = new byte[len];

            data[0] = (byte) (data_len);
            data[1] = index;
            for (int i = 0; i < num_len; ++i) {
                if (i % 2 == 0) {
                    data[2 + i] = n[i + 3];
                } else {
                    data[2 + i] = n[i + 1];
                }
            }

            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

        }
    }

    private void sendId3GBK(byte index, String num, int data_len, int reserve) {

        try {
            if (num == null) {
                num = " ";
            }
            byte[] n = num.getBytes("GBK");

            int num_len = n.length;

            if (num_len >= (data_len)) {
                num_len = (data_len);
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
        } catch (Exception e) {

        }
    }

    public void setSongName(String s) {
        if (CarUtil.getModelId() == 34) {
            sendId3GBK((byte) 0xe4, s, 0x20, 0);
        } else {
            sendId3((byte) 0xe4, s, 0x20, 0);
        }
    }

    //	public void setSongAlbum(String s) {
    //		sendId3((byte) 0x93, s, 0x20, 0);
    //	}

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }

    public int getOutTemp(byte[] data) {//
        if (data.length > 13) {
            int t = ((data[13] & 0xff) * 5) - 400;

            return t;
        } else {
            return super.getOutTemp(data);
        }
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

        //		byte format = 2;
        //
        //		String date_foramt = SettingProperties.getProperty(mContext,
        //				SettingProperties.KEY_DATE_FORMAT);
        //		if (date_foramt != null) {
        //			if ("dd/MM/yyyy".equals(date_foramt)){
        //				format = 1;
        //			} else if ("MM/dd/yyyy".equals(date_foramt)){
        //				format = 3;
        //			}
        //		}


        byte m = (byte) curDate.getMinutes();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{
                0x0a, (byte) 0xcb, 0, h, m, 0, 0, ampm, y, mon, d, 1
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

                lang = 2;

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
            } else {
                lang = 1;
            }

        }
        if (lang != -1) {
            byte[] buf = {0x2, (byte) 0x9a, 0x1, (byte) lang};
            sendDataToCanbox(buf, buf.length);
        }
    }

    public void setPhoneEx(int status, String num, String name) {
        switch (status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:

                name = null;
                num = null;
                status = 6;
                break;
            case HFP_INFO_CONNECTING:
            case HFP_INFO_CONNECTED:
                name = null;
                num = null;
                status = 0;
                break;
            case HFP_INFO_CALLED:
                status = 2;
                break;
            case HFP_INFO_INCOMING:
                status = 1;
                break;
            case HFP_INFO_CALLING:
                status = 4;
                break;
        }

        if (num == null) {
            num = " ";
        }

        //		if (name == null) {
        //			name = " ";
        //		}


        try {
            //			if (num == null) {
            //				num = " ";
            //			}

            byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

            int num_len = n.length;
            if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
                num_len -= 2;
            }

            int data_len = 0x1b;
            if (num_len >= (data_len - 2)) {
                num_len = (data_len - 2);
            }
            byte[] data;

            int len = data_len + 2;

            data = new byte[len];

            data[0] = (byte) (data_len);
            data[1] = (byte) 0xcd;
            data[2] = (byte) status;
            data[3] = (byte) 0;
            data[4] = (byte) 0;
            for (int i = 0; i < num_len; ++i) {
                if (i % 2 == 0) {
                    data[5 + i] = n[i + 3];
                } else {
                    data[5 + i] = n[i + 1];
                }
            }

            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

        }

        //		sendId3((byte) 0x95, num, 0x19, 1);
        //		sendId3((byte) 0x96, name, 0x19, 1);

    }
}
