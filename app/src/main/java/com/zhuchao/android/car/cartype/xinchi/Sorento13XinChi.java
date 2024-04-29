package com.zhuchao.android.car.cartype.xinchi;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.zhuchao.android.car.canbox.Canbox;

public class Sorento13XinChi extends Canbox {

    public Sorento13XinChi() {
        buildCmdEQ((byte) 0x70, (byte) 0xff, 0);
        buildCmdVersion((byte) 0x71, (byte) 0x0);
        //		buildCmdOutTemp(5);
        mIdAC = 0x5;
        mIdKey = 0x4;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.MODLE}, {0x6, MyCmd.Keycode.VOLUME_UP}, {0x5, MyCmd.Keycode.VOLUME_DOWN},
            {0x7, MyCmd.Keycode.HOME}, {0x8, MyCmd.Keycode.NAVIGATION}, {0x9, MyCmd.Keycode.BT}, {0xa, MyCmd.Keycode.KEY_DISPLAY}, {0xb, MyCmd.Keycode.BACK}, {0xc, MyCmd.Keycode.SPEECH},
    };


    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {

        } else if ((data & 0xff) == 0x7f) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0xff) {
            data = (byte) 0xfa;
        } else {
            //data = (byte) (35 + data);
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];


        airData[0] = (byte) ((data[2] & 0xd0) | ((data[2] & 0x04) << 3) | ((data[2] & 0x01) << 1) | ((data[3] & 0x40) >> 4));

        airData[1] = (byte) ((data[3] & 0xf) | ((data[4] & 0x20) >> 0) | ((data[4] & 0x10) << 2) | ((data[4] & 0x40) << 1));


        airData[2] = data[5];
        airData[3] = data[6];


        //		airData[4] = (byte) (((data[6] & 0x40) >> 3));
        //airData[7] = (byte) (((data[6] & 0x80) >> 3));


        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (39 << 16) | (19 << 8) | 19;

            super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
            byte[] buf = new byte[]{(byte) 0x90, 0x2, 0x31, 0};
            sendDataToCanbox(buf, buf.length);
        } else {
            if (mEQData == null) {
                mEQData = new byte[6];
            }
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

            byte[] buf = new byte[]{
                    (byte) 0x93, 0x7, mEQData[5], (byte) (mEQData[4] + 1), (byte) (mEQData[3] + 1), (byte) (mEQData[2] + 1), (byte) (mEQData[1] + 1), (byte) (mEQData[0] + 1), 0
            };
            sendDataToCanbox(buf, buf.length);


            //			super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
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
        mEQData[5] = (byte) (buf[2] & 0x7f);

        mEQData[0] -= 1;
        mEQData[1] -= 1;
        mEQData[2] -= 1;
        mEQData[3] -= 1;
        mEQData[4] -= 1;


        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        super.startConnect();
        if (mEQData == null) {
            mEQData = new byte[6];
        }
        startEQ();
    }

    @Override
    public void stopConnect() {
        // TODO Auto-generated method stub
        String value = mEQData[0] + "," + mEQData[1] + "," + mEQData[2] + "," + mEQData[3] + "," + mEQData[4] + "," + mEQData[5];
        SystemConfig.setProperty(mContext, SystemConfig.CANBOX_EQ_VOLUME, value);
        stopEQ();
        super.stopConnect();
    }


    private void stopEQ() {
        byte[] buf = new byte[]{
                (byte) 0x93, 0x7, (byte) 0x80, (byte) (mEQData[4] + 1), (byte) (mEQData[3] + 1), (byte) (mEQData[2] + 1), (byte) (mEQData[1] + 1), (byte) (mEQData[0] + 1), 0
        };
        sendDataToCanbox(buf, buf.length);
    }

    private void startEQ() {

        String s = SystemConfig.getProperty(mContext, SystemConfig.CANBOX_EQ_VOLUME);
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

                    byte[] buf = new byte[]{
                            (byte) 0x93, 0x7, mEQData[5], (byte) (mEQData[4] + 1), (byte) (mEQData[3] + 1), (byte) (mEQData[2] + 1), (byte) (mEQData[1] + 1), (byte) (mEQData[0] + 1), 0
                    };
                    sendDataToCanbox(buf, buf.length);

                } catch (Exception e) {

                }
            }
        }

    }
}
