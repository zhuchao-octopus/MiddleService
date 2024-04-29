package com.zhuchao.android.car.cartype.hiworld;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Locale;


public class PSAHiworld extends Canbox {

    public PSAHiworld() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
        //		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
        //		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);

        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 2, KEYS_WHEEL2);

        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    public void startConnect() {

    }

    @Override
    public void stopConnect() {

    }

    private final static byte[] IDS_TO_CANBOXSETTING = {
            (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x71, (byte) 0x72, (byte) 0x76, (byte) 0x79, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x85, (byte) 0x94, (byte) 0xc1, (byte) 0xc2,
    };

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH}, {0x5, MyCmd.Keycode.BT_DIAL}, {0x8, MyCmd.Keycode.NEXT},
            {0x9, MyCmd.Keycode.PREVIOUS}, {0xb, MyCmd.Keycode.MODLE}, {0xa, MyCmd.Keycode.HOME}, {0xd, MyCmd.Keycode.PREVIOUS}, {0xe, MyCmd.Keycode.NEXT}, {0xf, MyCmd.Keycode.PLAY_PAUSE},
            {0x10, MyCmd.Keycode.BACK},
            //		{ 0x11, MyCmd.Keycode },
            //		{ 0x12, MyCmd.Keycode },
            //		{ 0x13, MyCmd.Keycode },
            //		{ 0x14, MyCmd.Keycode },
            //		{ 0x15, MyCmd.Keycode },
            //		{ 0x16, MyCmd.Keycode },
            {0x40, MyCmd.Keycode.BT},
    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x6, MyCmd.Keycode.BACK}, {0x7, MyCmd.Keycode.HOME}, {0x8, MyCmd.Keycode.MODLE}, {0x9, MyCmd.Keycode.MUTE}, {0xa, MyCmd.Keycode.NUMBER1},
            {0xb, MyCmd.Keycode.NUMBER2}, {0xc, MyCmd.Keycode.NUMBER3}, {0xd, MyCmd.Keycode.NUMBER4}, {0xe, MyCmd.Keycode.NUMBER5}, {0xf, MyCmd.Keycode.NUMBER6}, {0x11, MyCmd.Keycode.EJECT},
            {0x16, MyCmd.Keycode.RADIO}, {0x17, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x18, MyCmd.Keycode.KEY_SEEK_PREV}, {0x19, MyCmd.Keycode.KEY_SEEK_PREV}, {0x1a, MyCmd.Keycode.KEY_SEEK_NEXT},
            {0x24, MyCmd.Keycode.PLAY_PAUSE}, {0x25, MyCmd.Keycode.BACK},
            //		{ 0x26, MyCmd.Keycode },
            //		{ 0x27, MyCmd.Keycode. },
            {0x28, MyCmd.Keycode.KEY_LIST}, {0x2a, MyCmd.Keycode.MODLE}, {0x2b, MyCmd.Keycode.BT}, {0x2c, MyCmd.Keycode.MODLE}, {0x2d, MyCmd.Keycode.AUDIO}, {0x2e, MyCmd.Keycode.RADIO},
            {0x2f, MyCmd.Keycode.RADIO}, {0x31, MyCmd.Keycode.AUDIO}, {0x32, MyCmd.Keycode.POWER}, {0x33, MyCmd.Keycode.NAVIGATION}, {0x40, MyCmd.Keycode.NAVIGATION},
    };
    private final static byte[][] KEYS_WHEEL3 = {
            {0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0}, {0x3, MyCmd.Keycode.ROLL_NEXT, 0},
            {0x13, MyCmd.Keycode.ROLL_PREV, 0}, {0x4, MyCmd.Keycode.ROLL_NEXT, 0}, {0x14, MyCmd.Keycode.ROLL_PREV, 0}, {0x5, MyCmd.Keycode.ROLL_NEXT, 0}, {0x15, MyCmd.Keycode.ROLL_PREV, 0},
    };

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x01, 0};
        switch (CarUtil.getModelId()) {
            case 1:
                cmd[2] = 1;
                break;
            case 17:
                cmd[2] = 2;
                break;
            case 3:
                cmd[2] = 3;
                break;
            case 18:
                cmd[2] = 4;
                break;
            case 19:
                cmd[2] = 5;
                break;
            case 20:
                cmd[2] = 6;
                break;
            case 21:
                cmd[2] = 7;
                break;
            case 22:
                cmd[2] = 8;
                break;
            case 12:
                if (CarUtil.getCarTypeConfig() == 2) {
                    cmd[2] = 9;
                } else {
                    cmd[2] = 0xa;
                }
                break;
            case 5:
                cmd[2] = 0xb;
                break;
            case 10:
                cmd[2] = 0xc;
                break;
            case 2:
                cmd[2] = 0xd;
                break;
            case 4:
                cmd[2] = 0xe;
                break;
            case 11:
                cmd[2] = 0xf;
                break;
            case 23:
            case 34:
            case 9:
                cmd[2] = 0x10;
                break;
            //		case 1:
            //			cmd[2] = 0x11;
            //			break;
            case 0:
            case 7:
                cmd[2] = 0x12;
                break;
            //		case 1:
            //			cmd[2] = 0x13;
            //			break;
            case 65:
                cmd[2] = 0x14;
                break;
            case 16:
                cmd[2] = 0x15;
                break;
            case 29:
                cmd[2] = 0x16;
                break;
            case 31:
                cmd[2] = 0x17;
                break;
            case 30:
                if (CarUtil.getCarTypeConfig() == 2) {
                    cmd[2] = 0x18;
                } else {
                    cmd[2] = 0x19;
                }
                break;
            case 28:
                cmd[2] = 20;
                break;
            default:
                return null;
        }
        return cmd;
    }

    @Override
    public int getAngleValue(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));


        return angle;


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


        byte[] airData = new byte[13];

        airData[0] = (byte) ((data[2] & 0x08) | ((data[2] & 0x40) << 1) | ((data[2] & 0x01) << 6) | ((data[3] & 0x10) << 1) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));


        airData[4] = (byte) (((data[2] & 0x20) >> 3) | ((data[3] & 0x08) << 4));

        switch ((data[6] & 0xff)) {
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 5:
                airData[1] = (byte) (0x60);
                break;
            case 6:
                airData[1] = (byte) (0x20);
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


        airData[2] = (byte) (data[8] & 0xff);
        airData[3] = (byte) (data[9] & 0xff);


        airData[12] = (byte) (((data[5] & 0x3) << 3) | ((data[2] & 0x4) << 3));

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    //	public void parseCanboxData(byte[] data, int len) {
    //		switch (data[0]) {
    //		case 0x22:
    //			if (data[3] == 0) {
    //				return;
    //			} else if (data[3] < 0) {
    //				data[3] = (byte) (-data[3]);
    //				data[2] += 0x10;
    //			}
    //			parseWheelKey(mIdKey3, data, MAP_KEYS3);
    //			break;
    //		default:
    //			super.parseCanboxData(data, len);
    //		}
    //	}

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

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }
}
