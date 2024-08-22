package com.zhuchao.android.car.cartype.hiworld;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.nio.charset.StandardCharsets;
import java.util.Locale;


public class HYHiworld extends Canbox {

    public HYHiworld() {
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
        buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 4);
        buildCmdRadarFrontEx((byte) 4);
        buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 4);
        buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
        buildCmdEQ((byte) 0xa6, (byte) 0x0, 6);
        buildCmdOutTemp((byte) 0x31, (byte) 0x0);
        buildCmdVersion((byte) 0xf0);
        mIdAC = 0x31;
        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x040211;
        MAP_KEYS2 = KEYS_WHEEL2;
        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }


    @Override
    public void stopConnect() {

    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x61, (byte) 0xa6, 0x32};

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH}, {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG},
            //		{ 0x7, MyCmd.Keycode },
            {0x9, MyCmd.Keycode.NEXT}, {0x8, MyCmd.Keycode.PREVIOUS}, {0xa, MyCmd.Keycode.MODLE}, {0xb, MyCmd.Keycode.MODLE}, {0x20, MyCmd.Keycode.KEY_SEEK_PREV}, {0x21, MyCmd.Keycode.KEY_SEEK_NEXT},
            {0x45, MyCmd.Keycode.BT},
            //		{ 0x2, MyCmd.Keycode },
            //		{ 0x2, MyCmd.Keycode },
    };

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT}, {0x6, MyCmd.Keycode.BACK}, {0x16, MyCmd.Keycode.PLAY_PAUSE}, {0x24, MyCmd.Keycode.AUDIO},
            {0x28, MyCmd.Keycode.BT}, {0x2b, MyCmd.Keycode.HOME}, {0x2f, MyCmd.Keycode.MENU}, {0x35, MyCmd.Keycode.KEY_DISPLAY}, {0x36, MyCmd.Keycode.SETUP}, {0x37, MyCmd.Keycode.AS},
            //		{ 0x38, MyCmd.Keycode. },
            {0x39, MyCmd.Keycode.NAVIGATION}, {0x47, MyCmd.Keycode.KEY_FM}, {0x48, MyCmd.Keycode.KEY_AM},
            //		{ 0x49, MyCmd.Keycode. },
            //		{ 0x4a, MyCmd.Keycode. },
            {0x4b, MyCmd.Keycode.RADIO}, {0x5b, MyCmd.Keycode.PREVIOUS}, {0x5c, MyCmd.Keycode.NEXT},
    };

    private final static byte[][] KEYS_WHEEL3 = {
            {0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0},
    };

    public void parseCanboxData(byte[] data, int len) {
        if (data[0] == 0x22) {
            parseWheelKey(mIdKey3, data, MAP_KEYS3);
        } else {
            super.parseCanboxData(data, len);
        }
    }

    @Override
    public int getAngleValue2(byte[] data) {

        short angle = (short) ((data[9] & 0xff) | ((data[8] & 0xff) << 8));

        return -angle;


    }

    @Override
    public int getACTemp(byte data) {//

        if ((data & 0xff) == 0xff) {

        } else if ((data & 0xff) == 0xfe) {
            data = 0;
        } else {
            // data =
        }

        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[14];

        airData[0] = (byte) ((data[2] & 0x08) | ((data[2] & 0x40) << 1) | ((data[2] & 0x01) << 6) | ((data[3] & 0x10) << 1));


        airData[7] = (byte) (((data[2] & 0x04) << 5) | ((data[4] & 0x10) << 1));

        airData[5] = (byte) (((data[3] & 0x20)));

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

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

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
        } catch (Exception e) {

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
        int t = ((data[13] & 0xff)) * 5 - 400;
        return t;
    }

    public void startConnect() {

    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            int volMax = 0x2d;
            switch (CarUtil.getModelId()) {
                case 0:
                case 2:
                    volMax = 0x1e;
                    break;
                case 1:
                    if (CarUtil.getCarTypeConfig() == 2) {
                        volMax = 0x23;
                    }
                    break;
            }
            ret = (volMax << 16) | (21 << 8) | 21;

        } else {
            byte[] buf = new byte[]{0x2, (byte) 0xad, 0x0, (byte) data};
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

            if (EQ_CMD_SET_VOLUME != cmd) {
                buf[3] += 6;
            }

            sendDataToCanbox(buf, buf.length);
        }
        return ret;
    }

    public void parseEQ(int id, byte[] buf) {
        byte[] data = new byte[6];
        data[0] = (byte) (buf[7] - 6);
        data[1] = (byte) (buf[6] - 6);
        data[2] = (byte) (buf[5] - 6);
        data[3] = (byte) (buf[4] - 6);
        data[4] = (byte) (buf[3] - 6);
        data[5] = buf[2];
        super.returnEQData(EQ_CMD_SET_ALL_DATA, data);
    }
}
