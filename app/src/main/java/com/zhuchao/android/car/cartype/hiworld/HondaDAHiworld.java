package com.zhuchao.android.car.cartype.hiworld;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class HondaDAHiworld extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x11, 0x12, 0x16, 0x17, 0x32, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x75, (byte) 0x84, (byte) 0x85, (byte) 0x87, (byte) 0x89, (byte) 0xa4, (byte) 0xe8};
    private final static byte[][] KEYS_WHEEL = {{0x45, MyCmd.Keycode.VOLUME_UP}, {0x46, MyCmd.Keycode.VOLUME_DOWN}, {0x57, MyCmd.Keycode.KEY_TURN_A}, {0x58, MyCmd.Keycode.KEY_TURN_D}, {0x5b, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x5c, MyCmd.Keycode.KEY_SEEK_PREV}, {0x5d, MyCmd.Keycode.NEXT}, {0x5e, MyCmd.Keycode.PREVIOUS},

    };
    private final static byte[][] KEYS_WHEEL2 = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x4, MyCmd.Keycode.SPEECH}, {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG}, {0x8, MyCmd.Keycode.PREVIOUS}, {0x9, MyCmd.Keycode.NEXT}, {0xa, MyCmd.Keycode.MUTE}, {0xb, MyCmd.Keycode.MODLE}, {0xc, MyCmd.Keycode.MODLE}, {0xd, MyCmd.Keycode.PREVIOUS}, {0xe, MyCmd.Keycode.NEXT}, {0xf, MyCmd.Keycode.PLAY_PAUSE}, {0x22, MyCmd.Keycode.ALL_APP}, {0x23, MyCmd.Keycode.KEY_FM}, {0x24, MyCmd.Keycode.KEY_AM}, {0x25, MyCmd.Keycode.AUDIO}, {0x26, MyCmd.Keycode.AUDIO}, {0x27, MyCmd.Keycode.AUDIO},

    };
    byte[] mEQCmdBuf = new byte[]{0x2, (byte) 0xad, 0x0, 0x0};

    public HondaDAHiworld() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);

        buildCmdRadarFront((byte) 0x41, (byte) 0x1, (byte) 4);
        buildCmdRadarFrontEx((byte) 4);
        buildCmdRadarBack((byte) 0x41, (byte) 0x1, (byte) 4);
        buildCmdAngle((byte) 0x11, (byte) 0x0, 0x1450);

        buildCmdEQ((byte) 0xa6, (byte) 0x1, 6);
        //		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;
        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;

        mIdKey2 = 0x040211;
        MAP_KEYS2 = KEYS_WHEEL2;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    @Override
    public void stopConnect() {

    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x01, 0x2};
        switch (CarUtil.getModelId()) {
            case 0:
            case 28:
                cmd[2] = 0x1d;
                break;
            case 29:
            case 8:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x1d;
                } else {
                    cmd[2] = 0x1f;
                }
                break;
            case 20:
                cmd[2] = 0x22;
                break;
            case 22:
                cmd[2] = 0x23;
                break;
            case 66:
                cmd[2] = 0x26;
                break;
            case 67:
                cmd[2] = 0x27;
                break;
            case 58:
                cmd[2] = 0x28;
                break;
            case 27:
                cmd[2] = 0x29;
                break;
            case 60:
                cmd[2] = 0x2a;
                break;
            case 61:
                cmd[2] = 0x2b;
                break;
            case 62:
                cmd[2] = 0x2c;
                break;
            case 63:
                cmd[2] = 0x2d;
                break;
            case 64:
                cmd[2] = 0x2e;
                break;
            case 65:
                cmd[2] = 0x2f;
                break;
            case 57:
                cmd[2] = 0x30;
                break;
            case 68:
                cmd[2] = 0x1d;
                break;
        }
        return cmd;
    }

    @Override
    public int getAngleValue2(byte[] data) {

        int angle = ((data[9] & 0xff) | ((data[8] & 0x7f) << 8));
        if ((data[8] & 0x80) != 0) {
            angle = -angle;
        }
        return -angle;


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


        byte[] airData = new byte[12];

        airData[0] = (byte) ((data[2] & 0x08) | ((data[2] & 0x40) << 1) | ((data[2] & 0x01) << 6) | ((data[3] & 0x10) << 1) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));

        airData[4] = (byte) (((data[4] & 0x03) << 4) | ((data[4] & 0x0c) >> 2));

        airData[7] = (byte) (((data[2] & 0x04) << 5));

        airData[5] = 0x8;

        switch ((data[6] & 0x0f)) {
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 4:
                airData[1] = (byte) (0xa0);
                break;
            case 5:
                airData[1] = (byte) (0x60);
                break;
            case 6:
                airData[1] = (byte) (0x40);
                break;
            case 7:
                airData[1] = (byte) (0xc0);
                break;
            case 0xa:
                airData[1] = (byte) (0xe0);
                break;
        }

        switch ((data[6] & 0xf0) >> 4) {
            case 3:
                airData[6] = (byte) (0x20);
                break;
            case 4:
                airData[6] = (byte) (0xa0);
                break;
            case 5:
                airData[6] = (byte) (0x60);
                break;
            case 6:
                airData[6] = (byte) (0x40);
                break;
            case 7:
                airData[6] = (byte) (0xc0);
                break;
            case 0xa:
                airData[6] = (byte) (0xe0);
                break;
        }

        airData[1] |= (byte) (data[7] & 0x0f);


        airData[2] = (byte) (data[8] & 0xff);
        airData[3] = (byte) (data[9] & 0xff);


        airData[11] = (byte) (data[10] & 0x0f);

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
        String s;

        if (CarUtil.getModelId() == 68 || CarUtil.getModelId() == 8) {
            s = String.format("%03d %02d%02d", play, time / 60, time % 60, Locale.ENGLISH);
        } else {
            s = String.format("%03d %02d  ", play, total, Locale.ENGLISH);
        }
        sendLcdInfo(type, s, false);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        if (source == MyCmd.SOURCE_RADIO) {
            String s;

            if (b[0] >= 0x10) { // am

                if (freq < 1000) {
                    s = String.format("10 %d ", (freq), Locale.ENGLISH);
                } else {
                    s = String.format("10 %d ", (freq), Locale.ENGLISH);
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

    public int getOutTemp(byte[] data) {//
        short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
        return t;
    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }

    public void startConnect() {

    }

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

    }    private final Handler mHandlerRepeat = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                sendEQCmd(msg.arg1, msg.arg2);
            }
            super.handleMessage(msg);
        }
    };

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {

            ret = (0x28 << 16) | (0xc << 8) | 0x12;

        } else {
            byte step = 0;
            int style = 0;
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    mEQCmdBuf[2] = 6;
                    step = 0;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    mEQCmdBuf[2] = 5;
                    step = 1;
                    break;
                case EQ_CMD_SET_LOW:
                    mEQCmdBuf[2] = 4;
                    step = 2;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    mEQCmdBuf[2] = 3;
                    step = 3;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    mEQCmdBuf[2] = 2;
                    step = 4;
                    break;
                case EQ_CMD_SET_VOLUME:
                    mEQCmdBuf[2] = 1;
                    step = 5;
                    style = 1;
                    break;
                default:
                    return 0;
            }

            if (mEQData != null) {
                step = (byte) (data - mEQData[step]);
            }
            if (step != 0) {
                sendEQCmd(style, step);
            }
        }
        return ret;
    }

    public void parseEQ(int id, byte[] buf) {
        if (mEQData == null) {
            mEQData = new byte[6];
        }
        mEQData[0] = buf[7];
        mEQData[1] = buf[6];
        mEQData[2] = buf[5];
        mEQData[3] = buf[4];
        mEQData[4] = buf[3];
        mEQData[5] = buf[2];
        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
    }

    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);

        Calendar now = Calendar.getInstance();

        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf;

        //		if (CarUtil.getModelId() == 68 || CarUtil.getModelId() == 8) {


        buf = new byte[]{0x03, (byte) 0xb5, h, m, s};
        sendDataToCanbox(buf, buf.length);
        Util.doSleep(10);
        buf = new byte[]{0x0a, (byte) 0xcb, 0, h, m, s, 0, 0, y, mon, d, 0};
        sendDataToCanbox(buf, buf.length);

    }

    public void sendId3UTF(byte index, String num, int data_len) {

        try {
            if (num == null) {
                num = " ";
            }
            byte[] n = num.getBytes(StandardCharsets.UTF_8);

            int num_len = n.length;

            if (num_len >= (data_len)) {
                num_len = (data_len);
            }
            byte[] data;

            data = new byte[data_len + 2];

            data[0] = (byte) (data_len);
            data[1] = index;
            if (num_len >= 0) System.arraycopy(n, 0, data, 2, num_len);

            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

        }
    }

    public void sendId3Unicode(byte index, String num, int data_len, int reserve) {

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

        if (CarUtil.getModelId() == 29 || (CarUtil.getModelId() == 8 && CarUtil.getCarTypeConfig() == 2)) {
            sendId3Unicode((byte) 0xe4, s, 0x20, 0);
        } else if (CarUtil.getModelId() == 20 || CarUtil.getModelId() == 26 || CarUtil.getModelId() == 19 || CarUtil.getModelId() == 59) {
            sendId3UTF((byte) 0xe4, s, 0x20);
        } else {
            sendId3GBK((byte) 0xe4, s, 0x20, 0);
        }

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

    public void setContext(Context c) {
        super.setContext(c);
        udpateLang();
    }

    public boolean isSupportCompass() {
        return true;
    }

    public void updateCompass(int compass) {
        int direction = compassAngleToDirect16(compass);
        switch (direction) {
            case 0:
                direction = 7;
                break;
            case 1:
                direction = 16;
                break;
            case 2:
                direction = 8;
                break;
            case 3:
                direction = 15;
                break;
            case 4:
                direction = 1;
                break;
            case 5:
                direction = 9;
                break;
            case 6:
                direction = 2;
                break;
            case 7:
                direction = 10;
                break;
            case 8:
                direction = 3;
                break;
            case 9:
                direction = 12;
                break;
            case 10:
                direction = 4;
                break;
            case 11:
                direction = 11;
                break;
            case 12:
                direction = 5;
                break;
            case 13:
                direction = 13;
                break;
            case 14:
                direction = 6;
                break;
            case 15:
                direction = 14;
                break;
        }
        byte[] buf = new byte[]{0x13, (byte) (0x94), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) (direction & 0xff), 0, 0, 0, 0, 0, 0, 0};

        sendDataToCanbox(buf, buf.length);
    }




}
