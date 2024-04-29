package com.zhuchao.android.car.cartype.hiworld;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Locale;


public class JeepHiworld extends Canbox {

    public JeepHiworld() {
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
        buildCmdAngle((byte) 0x72, (byte) 0x0, 540);

        buildCmdEQ((byte) 0xa6, (byte) 0x1, 6);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);

        buildCmdKey((byte) 0x72, (byte) 1, (byte) 4, (byte) 0, KEYS_WHEEL);

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    public void startConnect() {

    }

    @Override
    public void stopConnect() {

    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x12, 0x32};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG}, {0x7, MyCmd.Keycode.SPEECH},
            {0x8, MyCmd.Keycode.NEXT}, {0x9, MyCmd.Keycode.PREVIOUS}, {0xb, MyCmd.Keycode.MODLE},
    };

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0x85, 0x01, 0};
        if (CarUtil.getModelId() == 0) {
            cmd[2] = 1;
        } else {
            return null;
        }
        return cmd;
    }

    @Override
    public int getAngleValue2(byte[] data) {
        int angle = (short) (((data[6] & 0xff) << 8) | (data[7] & 0xff));
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


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0x48) | ((data[2] & 0x10) << 1) | ((data[2] & 0x02) >> 1));

        switch ((data[3] & 0xff)) {
            case 1:
                airData[1] = (byte) (0x40);
                break;
            case 2:
                airData[1] = (byte) (0x60);
                break;
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 4:
                airData[1] = (byte) (0xa0);
                break;
            case 5:
                airData[1] = (byte) (0x80);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);


        airData[2] = (byte) (data[5] & 0xff);
        airData[3] = (byte) (data[6] & 0xff);

        if (airData[1] == 0) {
            //			Util.zeroBuf(airData);
        }
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

        String s = String.format("%03d      %03d", play, total, Locale.ENGLISH);
        sendLcdInfo(type, s, false);


    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        if (source == MyCmd.SOURCE_RADIO) {
            String s;

            if (b[0] >= 0x10) { // am

                if (freq < 1000) {
                    s = String.format("000 %d 0KHz", (freq), Locale.ENGLISH);
                } else {
                    s = String.format("00 %d 0KHz", (freq), Locale.ENGLISH);
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
            data[1] = (byte) 0xd2;
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

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword2(data, len);
    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (0x26 << 16) | (19 << 8) | 19;
        } else {
            byte[] buf = new byte[]{0x2, (byte) 0xad, 0x0, (byte) (data + 1)};
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

        if (mEQData == null) {
            mEQData = new byte[6];
        }

        mEQData[0] = (byte) (buf[7] - 1);
        mEQData[1] = (byte) (buf[6] - 1);
        mEQData[2] = (byte) (buf[5] - 1);
        mEQData[3] = (byte) (buf[4] - 1);
        mEQData[4] = (byte) (buf[3] - 1);
        mEQData[5] = (byte) (buf[2] - 1);
        if (mEQData != null) {
            returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
        }
    }
}
