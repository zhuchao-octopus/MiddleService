package com.zhuchao.android.car.cartype.hiworld;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;


public class ChangChengHiworldCCP003 extends Canbox {

    public ChangChengHiworldCCP003() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);

        buildCmdAngle((byte) 0x11, (byte) 0x0, 5500);


        buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x3, (byte) 0x7, (byte) 0x4);
        buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 0x3, (byte) 0x5, (byte) 0x4);
        buildCmdRadarFrontEx((byte) 0x4);


        buildCmdEQ((byte) 0xa6, (byte) 0x1, 6);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 2, KEYS_WHEEL2);

        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;

    }

    @Override
    public int getAngleValue2(byte[] data) {
        int angle = (short) (((data[8] & 0xff) << 8) | (data[9] & 0xff));
        return -angle;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x62};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH}, {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG},
            {0x9, MyCmd.Keycode.NEXT}, {0x8, MyCmd.Keycode.PREVIOUS},


            {0xd, MyCmd.Keycode.NEXT}, {0xe, MyCmd.Keycode.PREVIOUS},

            {0xa, MyCmd.Keycode.MODLE}, {0xc, MyCmd.Keycode.MODLE}, {0x2c, MyCmd.Keycode.MODLE},


            {0x67, MyCmd.Keycode.EASY_CONNECT},
    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT}, {0x6, MyCmd.Keycode.BACK}, {0x7, MyCmd.Keycode.RADIO}, {0x9, MyCmd.Keycode.MUTE},
            {0x17, MyCmd.Keycode.PREVIOUS}, {0x18, MyCmd.Keycode.NEXT}, {0x19, MyCmd.Keycode.PREVIOUS}, {0x1a, MyCmd.Keycode.NEXT}, {0x20, MyCmd.Keycode.NAVIGATION}, {0x24, MyCmd.Keycode.AUDIO},
            {0x25, MyCmd.Keycode.NAVIGATION}, {0x2a, MyCmd.Keycode.PLAY_PAUSE}, {0x2c, MyCmd.Keycode.MODLE}, {0x2f, MyCmd.Keycode.HOME}, {0x33, MyCmd.Keycode.RADIO}, {0x34, MyCmd.Keycode.BT_DIAL},
            {0x35, MyCmd.Keycode.BT_HANG}, {0x37, MyCmd.Keycode.SETUP}, {0x40, MyCmd.Keycode.HOME}, {0x41, MyCmd.Keycode.BT}, {0x42, MyCmd.Keycode.EQ},
    };
    private final static byte[][] KEYS_WHEEL3 = {
            {0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0},
    };

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x17, 0x11};
        switch (CarUtil.getModelId()) {
            case 0:
            case 23:
            case 42:
                cmd[2] = 0x1;
                break;
            case 12:
            case 20:
                cmd[2] = 0x2;
                break;
            case 8:
            case 40:
                cmd[2] = 0x3;
                break;
            case 18:
                cmd[2] = 0x4;
                break;
            case 43:
                cmd[2] = 0x5;
                break;
            case 19:
            case 41:
                cmd[2] = 0x7;
                break;
            case 21:
                cmd[2] = 0x8;
                break;
            case 24:
                cmd[2] = 0x9;
                break;
            case 22:
                cmd[2] = 0xa;
                break;
            default:
                return null;
        }
        return cmd;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0xff || (data & 0xff) == 0xfa) {

        } else if ((data & 0xff) == 0xfe) {
            data = 0;
        } else {
            //data =
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[10];

        if (CarUtil.getModelId() == 18) {
            airData[0] = (byte) ((data[2] & 0x08) | ((data[2] & 0x40) << 1) | ((data[3] & 0x44) << 0) | ((data[3] & 0x10) << 1) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));

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
                case 0xc:
                    airData[1] = (byte) (0xa0);
                    break;
            }

            airData[3] = (byte) 0xfa;
        } else {

            airData[0] = (byte) ((data[2] & 0x04) | ((data[2] & 0x40) << 1) | ((data[2] & 0x01) << 6)
                    //				| ((data[3] & 0x10) << 1)
                    | ((data[3] & 0x08) << 1) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));

            if (((data[3] & 0x10) == 0)) {
                airData[0] |= 0x20;
            }

            airData[4] = (byte) (((data[2] & 0x20) >> 3));

            switch ((data[6] & 0xff)) {
                case 3:
                    airData[1] = (byte) (0x20);
                    break;
                case 2:
                    airData[1] = (byte) (0x60);
                    break;
                case 1:
                    airData[1] = (byte) (0x40);
                    break;
                case 4:
                    airData[1] = (byte) (0xa0);
                    break;
            }


            airData[3] = data[9];
        }


        airData[2] = data[8];
        airData[1] |= (byte) (data[7] & 0x0f);

        airData[5] |= 0x80;

        super.parseACInfo(airData);
    }

    public void parseCanboxData(byte[] data, int len) {
        //		case 0x22:
        //			if (data[3] == 0) {
        //				return;
        //			} else if (data[3] < 0) {
        //				data[3] = (byte) (-data[3]);
        //				data[2] += 0x10;
        //			}
        //			parseWheelKey(mIdKey3, data, MAP_KEYS3);
        //			break;
        super.parseCanboxData(data, len);
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


    public void sendId3(byte index, String num, int data_len, int reserve) {

        try {
            if (num == null) {
                num = " ";
            }
            byte[] n = num.getBytes(StandardCharsets.UTF_8);

            int num_len = n.length;

            if (num_len >= (data_len - reserve - 3)) {
                num_len = (data_len - reserve - 3);
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
        sendId3((byte) 0x92, s, 0x20, 0);
    }

    public void setSongAlbum(String s) {
        sendId3((byte) 0x93, s, 0x20, 0);
    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }


    private final Handler mHandlerRepeat = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                sendEQCmd(msg.arg1, msg.arg2);
            }
            super.handleMessage(msg);
        }
    };

    byte[] mEQCmdBuf = new byte[]{0x2, (byte) 0xad, 0x0, 0x0};

    private void sendEQCmd(int style, int step) {
        mHandlerRepeat.removeMessages(0);
        if (step == 0) {
            return;
        }

        if (style == 1) {
            if (step < 0) {
                mEQCmdBuf[3] = -1;
                ++step;
            } else {
                mEQCmdBuf[3] = 1;
                --step;
            }
        } else {
            if (step > 0) {
                --step;
                mEQCmdBuf[3]++;
            } else {
                ++step;
                if (mEQCmdBuf[3] > 0) {
                    mEQCmdBuf[3]--;
                } else {
                    return;
                }
            }
        }

        sendDataToCanbox(mEQCmdBuf, mEQCmdBuf.length);
        if (step != 0) {
            mHandlerRepeat.sendMessageDelayed(mHandlerRepeat.obtainMessage(0, style, step), 100);
        }

    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (39 << 16) | (21 << 8) | 21;

            //			byte[] buf = new byte[] { 0x3, (byte) 0x6a, 0x5, (byte) 0xa6, 0 };
            //			sendDataToCanbox(buf, buf.length);
        } else {
            mHandlerRepeat.removeMessages(0);
            if (mEQData == null) {
                mEQData = new byte[6];
            }

            int step = 0;
            int style = 0;
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    mEQCmdBuf[2] = 6;
                    mEQCmdBuf[3] = mEQData[0];
                    step = data - mEQData[0];
                    break;
                case EQ_CMD_SET_MIDDLE:
                    mEQCmdBuf[2] = 5;
                    mEQCmdBuf[3] = mEQData[1];
                    step = data - mEQData[1];
                    break;
                case EQ_CMD_SET_LOW:
                    mEQCmdBuf[2] = 4;
                    mEQCmdBuf[3] = mEQData[2];
                    step = data - mEQData[2];
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    mEQCmdBuf[2] = 3;
                    mEQCmdBuf[3] = mEQData[3];
                    step = data - mEQData[3];
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    mEQCmdBuf[2] = 2;
                    mEQCmdBuf[3] = mEQData[4];
                    step = data - mEQData[4];
                    break;
                case EQ_CMD_SET_VOLUME:
                    mEQCmdBuf[2] = 1;
                    step = data - mEQData[5];
                    style = 1;
                    break;
                default:
                    return 0;
            }

            sendEQCmd(style, step);
        }
        return ret;
    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
        Util.doSleep(50);
        udpateLang();
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
        //		byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{0x0a, (byte) 0xcb, 0, h, m, 0, 0, ampm, y, mon, d, 0};

        sendDataToCanbox(buf, buf.length);

    }

    public void udpateLang() {
        int lang = -1;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("zh")) {
                lang = 2;
            } else {
                lang = 1;
            }

        }
        if (lang != -1) {
            byte[] buf = {0x2, (byte) 0x9a, 0x1, (byte) lang};
            sendDataToCanbox(buf, buf.length);
        }
    }

    public void startConnect() {

    }

    @Override
    public void stopConnect() {

    }

    public boolean isSupportCompass() {
        return true;
    }

    public void updateCompass(int compass) {
        int direction = (compassAngleToDirect(compass) + 8 - 1) % 8;

        byte[] buf = new byte[]{
                0x13, (byte) (0xe4), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) (direction & 0xff), 0, 0, 0, 0, 0, 0, 0
        };

        sendDataToCanbox(buf, buf.length);
    }

}
