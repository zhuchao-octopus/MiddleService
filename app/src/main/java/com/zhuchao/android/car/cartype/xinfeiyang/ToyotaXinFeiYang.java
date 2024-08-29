package com.zhuchao.android.car.cartype.xinfeiyang;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

public class ToyotaXinFeiYang extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x21, 0x22, 0x23, 0x26, 0x27, 0x35};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT}, {0x7, MyCmd.Keycode.MODLE},

            {0x8, MyCmd.Keycode.SPEECH}, {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG},

            {0x13, MyCmd.Keycode.PREVIOUS}, {0x14, MyCmd.Keycode.NEXT},

            {0x15, MyCmd.Keycode.BACK}, {0x16, MyCmd.Keycode.PLAY_PAUSE}, {(byte) 0x88, MyCmd.Keycode.MODLE},


    };
    private final static byte[][] KEYS_WHEEL2 = {{0x0, MyCmd.Keycode.KEY_AM}, {0x1, MyCmd.Keycode.KEY_FM}, {0x2, MyCmd.Keycode.DVD}, {0x3, MyCmd.Keycode.AUDIO}, {0x4, MyCmd.Keycode.BT_MUSIC}, {0x5, MyCmd.Keycode.AUX_IN}, {0x6, MyCmd.Keycode.KEY_TV},

    };
    byte[] airData = new byte[14];
    byte[] mEQBuf = new byte[]{5, 0, 5, 5, 5, 0};
    private byte[] mData = new byte[]{(byte) 0xc0, 0x8, 0, 0, 0, 0, 0, 0, 0, 0};

    public ToyotaXinFeiYang() {

        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdAngle((byte) 0x29, (byte) 0x0, 360);
        buildCmdRadarFront((byte) 0x1d, (byte) 0x0, (byte) 0x4);
        buildCmdRadarBack((byte) 0x1e, (byte) 0x0, (byte) 0x4);
        buildCmdVersion((byte) 0x30, (byte) 0x0);
        buildCmdEQ((byte) 0x31, (byte) 0x0, 6);
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;

        buildCmdKey((byte) 0x36, (byte) 5, (byte) 2, (byte) 2, KEYS_WHEEL2);
        mIdAC = 0x55;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    @Override
    public int getAngleValue2(byte[] data) {


        int angle = ((data[2] & 0xff) | (((data[3] & 0xf)) << 8));

        if (angle != 0) {
            if ((data[3] & 0x80) == 0) {
                angle = angle - 0x1000;
            }
        }

        return angle;

    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x10) {
            data = (byte) 0;
        } else if ((data & 0xff) == 0x50) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0xfe) {
            data = (byte) (32 + ((data & 0xff) - 0x20));
        } else {
            //data =
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        airData[0] = (byte) (((data[6] & 0x08) << 2) | ((data[6] & 0x01) << 4) | ((data[6] & 0x02) << 5) | ((data[6] & 0x04) << 0) | ((data[6] & 0x40) >> 6) | ((data[6] & 0x80) >> 6));

        switch ((data[4] & 0xf0) >> 4) {
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
                airData[1] = (byte) (0xc0);
                break;
            case 6:
                airData[1] = (byte) (0x80);
                break;
            case 7:
                airData[1] = (byte) (0xe0);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);


        airData[2] = (byte) (data[2] & 0xff);
        airData[3] = (byte) (data[3] & 0xff);

        airData[9] &= ~0x08;
        airData[9] |= (byte) ((data[6] & 0x10) >> 1);

        airData[4] = (byte) (((data[5] & 0x30) >> 4) | ((data[5] & 0x03) << 4));

        super.parseACInfo(airData);
    }

    public void parseACInfoRear(byte[] data) {

        airData[9] &= ~0x02;
        airData[9] |= (byte) ((data[6] & 0x01) << 1);
        airData[10] = (byte) getACTemp(data[2]);

        switch ((data[4] & 0xf0) >> 4) {
            case 1:
                airData[11] = (byte) (0x40);
                break;
            case 2:
                airData[11] = (byte) (0x60);
                break;
            case 3:
                airData[11] = (byte) (0x20);
                break;
            default:
                airData[11] = 0;
                break;
        }

        airData[11] |= (byte) (data[4] & 0x0f);

        super.parseACInfoRear(airData);
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0x56) {
            parseACInfoRear(data);
        } else {
            super.parseCanboxData(data, len);
        }
    }

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
                s2 = 0x11;
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
            mData = new byte[]{(byte) 0xc0, 0x8, s, s2, 0, (byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec};

        } else {
            mData = new byte[]{(byte) 0xc0, 0x8, s, s2, 0, 0, (byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), min, sec};
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
        mData = new byte[]{(byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0, 0, 0};
        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source) {// default is simple box
        byte s = 0;
        byte mediaType = 0;
        switch (source) {
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
            case 0:
                return;
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
                mediaType = 0x40;
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

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (63 << 16) | (15 << 8) | 11;

            byte[] mData = new byte[]{(byte) 0x90, 0x2, 0x31, 0};

            sendDataToCanbox(mData, mData.length);
        } else {
            byte id;
            int step;
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    id = 0x5;
                    step = data + 2;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    id = 0x6;
                    step = data + 2;
                    break;
                case EQ_CMD_SET_LOW:
                    id = 0x4;
                    step = data + 2;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    id = 0x1;
                    step = data;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    id = 0x2;
                    step = data;
                    break;
                case EQ_CMD_SET_VOLUME:
                    id = 0x7;
                    step = data;
                    break;
                default:
                    return 0;
            }
            byte[] mData = new byte[]{(byte) 0x84, 0x2, id, (byte) step};

            sendDataToCanbox(mData, mData.length);

        }
        return ret;
    }

    public void parseEQ(int id, byte[] buf) {

        mEQBuf[0] = (byte) ((buf[3] & 0xf) - 2);
        mEQBuf[1] = (byte) (((buf[4] & 0xf0) >> 4) - 2);
        mEQBuf[2] = (byte) (((buf[3] & 0xf0) >> 4) - 2);
        mEQBuf[3] = (byte) (((buf[2] & 0xf0) >> 4));
        mEQBuf[4] = (byte) ((buf[2] & 0xf));
        mEQBuf[5] = buf[5];
        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQBuf);
    }
}
