package com.zhuchao.android.car.cartype.xinchi;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

public class LC100XinChi extends Canbox {

    public LC100XinChi() {
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

        buildCmdRadarFront((byte) 0x19, (byte) 0x0, (byte) 0x4);
        buildCmdRadarBack((byte) 0x1e, (byte) 0x0, (byte) 0x4);

        buildCmdAngle((byte) 0x29, (byte) 0x0, 380);
        buildCmdEQ((byte) 0x31, (byte) 0xff, 0);
        buildCmdOutTemp((byte) 0x1a, (byte) 0x10);
        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x28;
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x11;
        MAP_KEYS2 = KEYS_WHEEL2;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {
            0x21, 0x22, 0x23, 0x1c, 0x1d, 0x24, 0x5a, 0x65, 0x10
    };

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x5, MyCmd.Keycode.MODLE}, {0x6, MyCmd.Keycode.BT_DIAL},
            {0x7, MyCmd.Keycode.BT_HANG},


            {0x8, MyCmd.Keycode.SPEECH}, {0x9, MyCmd.Keycode.MUTE}, {0xa, MyCmd.Keycode.BACK}, {0xb, MyCmd.Keycode.HOME}, {0xc, MyCmd.Keycode.PLAY_PAUSE}, {0xd, MyCmd.Keycode.BT},
    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x3, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x4, MyCmd.Keycode.KEY_AM}, {0x5, MyCmd.Keycode.KEY_FM},
            {0x6, MyCmd.Keycode.MODLE}, {0x7, MyCmd.Keycode.AUDIO}, {0x8, MyCmd.Keycode.KEY_TURN_A}, {0x9, MyCmd.Keycode.KEY_TURN_D}, {0x10, MyCmd.Keycode.NEXT}, {0x11, MyCmd.Keycode.PREVIOUS},
            {0x12, MyCmd.Keycode.AS}, {0x20, MyCmd.Keycode.HOME}, {0x21, MyCmd.Keycode.NAVIGATION},


            {0x22, MyCmd.Keycode.BACK}, {0x23, MyCmd.Keycode.HOME}, {0x24, MyCmd.Keycode.PLAY_PAUSE}, {0x25, MyCmd.Keycode.PREVIOUS}, {0x26, MyCmd.Keycode.NEXT}, {0x27, MyCmd.Keycode.PREVIOUS},
            {0x28, MyCmd.Keycode.NEXT}, {0x29, MyCmd.Keycode.AUDIO},

    };

    @Override
    public int getAngleValue(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0x07)) << 8));

        if ((data[3] & 0x08) != 0) {
            angle = -angle;
        }

        angle = ((angle * 3000) / 380);
        if (angle > -50 && angle < 50) {
            angle = 50;
        }

        return angle;


    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {

        } else if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else {
            data = (byte) (35 + data);
        }
        return data;
    }

    byte[] airData = new byte[12];

    public void parseACInfo(byte[] data) {


        airData[0] = data[2];
        airData[1] = data[3];


        airData[2] = data[4];
        airData[3] = data[5];


        //		airData[4] = (byte) (((data[6] & 0x40) >> 3));
        //airData[7] = (byte) (((data[6] & 0x80) >> 3));


        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    private void parseACInfoEx(byte[] data) {


        airData[9] &= ~0x02;
        airData[9] |= (byte) (((data[2] & 0x00) >> 0) | ((data[2] & 0x10) >> 3));
        airData[10] = (byte) getACTemp(data[3]);

        airData[11] = data[4];


        super.parseACInfoRear(airData);
    }

    private void parseACInfoEx2(byte[] data) {
        airData[9] &= ~0x38;
        airData[9] |= (byte) (((data[2] & 0x08) >> 0) | ((data[2] & 0x04) << 2) | ((data[2] & 0x02) << 4));


        airData[5] &= ~0x10;
        airData[5] |= (byte) (((data[2] & 0x01) << 4));


        super.parseACInfo(airData);
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x1b:
                parseACInfoEx(data);
                break;
            case 0x1a:
                parseACInfoEx2(data);
                break;
            default:
                super.parseCanboxData(data, len);
        }

    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


    public int getOutTemp(byte[] data) {//
        int t = data[3] * 10;
        return t;
    }


    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (63 << 16) | (15 << 8) | 11;

            byte[] buf = new byte[]{(byte) 0x90, 0x2, 0x31, 0};
            sendDataToCanbox(buf, buf.length);
        } else {
            byte[] buf = new byte[]{(byte) 0x84, 0x2, 0x0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    buf[2] = 5;
                    buf[3] = (byte) (buf[3] + 2);
                    break;
                case EQ_CMD_SET_MIDDLE:
                    buf[2] = 6;
                    buf[3] = (byte) (buf[3] + 2);
                    break;
                case EQ_CMD_SET_LOW:
                    buf[2] = 4;
                    buf[3] = (byte) (buf[3] + 2);
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    buf[2] = 1;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    buf[2] = 2;
                    break;
                case EQ_CMD_SET_VOLUME:
                    buf[2] = 7;
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

        mEQData[0] = (byte) (buf[3] & 0x0f);
        mEQData[1] = (byte) ((buf[4] & 0xf0) >> 4);
        mEQData[2] = (byte) ((buf[3] & 0xf0) >> 4);
        mEQData[3] = (byte) ((buf[2] & 0xf0) >> 4);
        mEQData[4] = (byte) (buf[2] & 0x0f);

        mEQData[5] = buf[5];

        mEQData[0] -= 2;
        mEQData[1] -= 2;
        mEQData[2] -= 2;


        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
    }

    public void setVolume(int volume) {
        byte[] buf = new byte[]{(byte) 0x8f, 0x3, 1, (byte) volume, 0};
        sendDataToCanbox(buf, buf.length);
    }

    @Override
    public void sendDataToCanbox(byte[] data, int len) {
        // TODO Auto-generated method stub
        super.sendDataToCanbox(data, len);
    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        super.startConnect();
        byte[] buf = new byte[]{(byte) 0x8f, 0x3, 0x2, 4, 0};
        sendDataToCanbox(buf, buf.length);

    }

    @Override
    public void stopConnect() {
        // TODO Auto-generated method stub
        //		byte[] buf = new byte[] { (byte) 0x8f, 0x2, mVolume, 4 };
        //		sendDataToCanbox(buf, buf.length);

        super.stopConnect();

    }
}
