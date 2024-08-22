package com.zhuchao.android.car.cartype.binarytek;

import android.content.Context;
import android.provider.Settings;

import com.common.utils.MachineConfig;
import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;
import java.util.Locale;


public class HyBNR extends Canbox {

    public HyBNR() {
        buildCmdDoor((byte) 0x24, (byte) 0x0, (byte) 0x3f, (byte) 0x03);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x3);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x3);
        buildCmdAngle((byte) 0x26, (byte) 0x6, 100);
        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x21;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;

        if (CarUtil.getModelId() == 6) {
            switch (CarUtil.getCarTypeConfig()) {
                case 2:
                    KEYS_WHEEL[20][1] = MyCmd.Keycode.SETUP;
                    break;
                case 1:
                    KEYS_WHEEL[18][1] = MyCmd.Keycode.AUDIO;
                    KEYS_WHEEL[19][1] = MyCmd.Keycode.KEY_DISPLAY;
                    KEYS_WHEEL[23][1] = MyCmd.Keycode.HOME;
                    KEYS_WHEEL[24][1] = MyCmd.Keycode.RADIO;
                    break;
            }
        }
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x5, MyCmd.Keycode.BT}, {0x6, MyCmd.Keycode.MUTE},
            {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.SPEECH}, {0x9, MyCmd.Keycode.KEY_DISPLAY}, {0xa, MyCmd.Keycode.HOME}, {0xb, MyCmd.Keycode.MENU}, {0xc, MyCmd.Keycode.BACK},
            {0xd, MyCmd.Keycode.NEXT}, {0xe, MyCmd.Keycode.PREVIOUS}, {0xf, MyCmd.Keycode.POWER}, {0x10, MyCmd.Keycode.BT_HANG}, {0x12, MyCmd.Keycode.SPEECH}, {0x30, MyCmd.Keycode.KEY_DISPLAY},
            {0x31, MyCmd.Keycode.NAVIGATION}, {0x32, MyCmd.Keycode.NAVIGATION}, {0x33, MyCmd.Keycode.NAVIGATION}, {0x34, MyCmd.Keycode.SETUP}, {0x35, MyCmd.Keycode.PLAY_PAUSE},
            {0x36, MyCmd.Keycode.RADIO}, {0x37, MyCmd.Keycode.AUDIO}, {0x38, MyCmd.Keycode.BT}, {0x39, MyCmd.Keycode.KEY_SEEK_PREV}, {0x3a, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x3b, MyCmd.Keycode.POWER},
            {0x3c, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x3d, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x3e, MyCmd.Keycode.ROLL_NEXT}, {0x3f, MyCmd.Keycode.ROLL_PREV},

            {(byte) 0x81, MyCmd.Keycode.VOLUME_ROLL_UP}, {(byte) 0x82, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {(byte) 0x83, MyCmd.Keycode.ROLL_NEXT}, {(byte) 0x84, MyCmd.Keycode.ROLL_PREV},
            {(byte) 0x85, MyCmd.Keycode.ROLL_NEXT}, {(byte) 0x86, MyCmd.Keycode.ROLL_PREV}, {(byte) 0x87, MyCmd.Keycode.POWER}, {(byte) 0x88, MyCmd.Keycode.MODLE}, {(byte) 0x89, MyCmd.Keycode.MUTE},
            {(byte) 0x8a, MyCmd.Keycode.VOLUME_ROLL_UP}, {(byte) 0x8b, MyCmd.Keycode.VOLUME_ROLL_DOWN},
    };

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x1e) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0x0) {

        } else {
            data = (byte) (31 + (data & 0xff));
        }
        return data;
    }

    private final byte[] airDataEx = new byte[8];

    public void parseACInfo(byte[] data) {

        airDataEx[0] = (byte) ((data[2] & 0xec) | ((data[2] & 0x1) << 1) | ((data[6] & 0x04) >> 2));


        airDataEx[1] = (byte) (data[3] & 0xff);


        airDataEx[2] = data[4];
        airDataEx[3] = data[5];


        airDataEx[5] |= 0x80;

        super.parseACInfo(airDataEx);
    }

    public void parseACInfoEx(byte[] data) {

        airDataEx[4] = (byte) ((data[3] & 0x3) | ((data[2] & 0x3) << 4));
        super.parseACInfo(airDataEx);
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0x2a) {
            parseACInfoEx(data);
        } else {
            super.parseCanboxData(data, len);
        }
    }

    private byte[] mData = new byte[]{
            (byte) 0xc0, 0x8, 0, 0, 0, 0, 0, 0, 0, 0
    };

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte h = (byte) ((time / 3600));
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        // ++play;

        byte s = 0;
        byte s2 = 0;
        switch (source) {
            case MyCmd.SOURCE_DVD:
                s = 0x2;
                s2 = 0x10;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                ++play;
                s = 8;
                s2 = 0x13;
                break;
            case MyCmd.SOURCE_BT:
                s = 0xb;
                s2 = 0x10;
                break;
            default:
                s = 0x07;
                s2 = 0x30;
                break;
        }

        if (MyCmd.SOURCE_DVD == source) {
            mData = new byte[]{
                    (byte) 0xc0, 0x8, s, s2, 0, (byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec
            };

        } else {
            mData = new byte[]{
                    (byte) 0xc0, 0x8, s, s2, (byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), 0, h, min, sec
            };
        }

        // if (mPhoneStatus < HFP_INFO_CALLED) {

        sendDataToCanbox(mData, mData.length);
        // }
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        mData = new byte[]{
                (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0, 0, 0
        };
        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source) {// default is simple box

        if (source == MyCmd.SOURCE_RADIO) {
            return;
        }
        byte s = 0;
        byte mediaType = 0;
        switch (source) {
            case 0:
                s = 1;
                mediaType = 1;
                break;
            case 1:
                s = 2;
                mediaType = 0x10;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 6;
                mediaType = 0x12;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x08;
                mediaType = 0x12;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                mediaType = 0x30;
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                mediaType = 0x30;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0b;
                mediaType = 0x30;
                break;
            default:
                s = 0x00;
                mediaType = 0x0;
                break;
        }

        // if (s == 0xb || s == 0x7) {
        // data = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0,
        // 0 };
        // } else {
        mData = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0};
        // }

        sendDataToCanbox(mData, mData.length);
    }

    private void setEQVolume(int volume) {

        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    private void startEQ() {
        String s = MachineConfig.getProperty(SettingProperties.CANBOX_EQ_VOLUME);
        if (s != null) {
            String[] ss = s.split(",");
            if (ss != null && ss.length > 5) {
                try {
                    mEQData[0] = Byte.valueOf(ss[0]);
                    mEQData[1] = Byte.valueOf(ss[1]);
                    mEQData[2] = Byte.valueOf(ss[2]);
                    mEQData[3] = Byte.valueOf(ss[3]);
                    mEQData[4] = Byte.valueOf(ss[4]);
                    mEQData[5] = Byte.valueOf(ss[5]);


                } catch (Exception ignored) {
                }
            }
        }

        setEQVolume(mEQData[5]);
        powerEQ(true);

    }

    private void powerEQ(boolean power) {
        byte[] buf = new byte[]{(byte) 0xc7, 0x6, 0, mEQData[3], mEQData[4], mEQData[0], mEQData[1], mEQData[2]};


        sendDataToCanbox(buf, buf.length);
    }

    private void stopEQ() {
        powerEQ(false);
    }

    public void startConnect() {
        super.startConnect();
        startEQ();
    }

    private final int mVolume = 30;

    public void stopConnect() {
        stopEQ();
        super.stopConnect();
    }

    byte[] mEQData = new byte[]{10, 10, 10, 10, 10, 30};

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            int vMax = 35;
            if (CarUtil.getModelId() == 1) {
                vMax = 45;
            }
            ret = (vMax << 16) | (21 << 8) | 21;
            returnEQData();
        } else {
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    mEQData[0] = (byte) data;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    mEQData[1] = (byte) data;
                    break;
                case EQ_CMD_SET_LOW:
                    mEQData[2] = (byte) data;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    mEQData[3] = (byte) data;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    mEQData[4] = (byte) data;
                    break;
                case EQ_CMD_SET_VOLUME:
                    mEQData[5] = (byte) data;
                    break;
                default:
                    return 0;
            }

            byte[] buf;// = new byte[] { (byte) 0x6, 0x2, 0x0, (byte) data };
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                case EQ_CMD_SET_MIDDLE:
                case EQ_CMD_SET_LOW:
                case EQ_CMD_SET_ZONE_FR:
                case EQ_CMD_SET_ZONE_LR:
                    buf = new byte[]{(byte) 0xc7, 0x6, 0, mEQData[3], mEQData[4], mEQData[0], mEQData[1], mEQData[2]};
                    break;
                case EQ_CMD_SET_VOLUME:
                    buf = new byte[]{(byte) 0xc4, 0x1, mEQData[5]};
                    break;
                default:
                    return 0;
            }

            String value = mEQData[0] + "," + mEQData[1] + "," + mEQData[2] + "," + mEQData[3] + "," + mEQData[4] + "," + mEQData[5];
            MachineConfig.setProperty(SettingProperties.CANBOX_EQ_VOLUME, value);


            sendDataToCanbox(buf, buf.length);
            returnEQData();
        }
        return ret;
    }

    private void returnEQData() {
        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
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

        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        if ("12".equals(strTimeFormat)) {
            if (h > 12) {
                h -= 12;
                ampm |= 0x80;
            } else if (h == 0) {
                h = 12;
            }
            h |= 0x80;
        } else {
        }

        h = fixTimeHour(h);


        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();

        byte[] buf = new byte[]{(byte) 0xc6, 0x8, 0x01, y, mon, d, h, m, s, ampm};
        sendDataToCanbox(buf, buf.length);
    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
        udpateLang();
    }


    public void udpateLang() {
        int lang = 2;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("en")) {
                lang = 2;
            } else if (locale.equals("zh")) {
                lang = 1;
            } else if (locale.equals("ko")) {
                lang = 3;
            }

        }
        if (lang != -1) {
            byte[] buf = {(byte) 0xc6, 0x8, 0x2, (byte) lang, 0, 0, 0, 0, 0, 0};
            sendDataToCanbox(buf, buf.length);
        }
    }
}
