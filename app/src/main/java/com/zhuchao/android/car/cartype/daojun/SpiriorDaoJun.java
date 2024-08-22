package com.zhuchao.android.car.cartype.daojun;

import android.os.Handler;
import android.os.Message;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;

public class SpiriorDaoJun extends Canbox {

    public SpiriorDaoJun() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
        // buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
        // buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
        // buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
        buildCmdEQ((byte) 0xa6, (byte) 0x1, 6);
        // buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;


        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);


        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {
            0x16, 0x17, 0x65, 0x66, 0x67, 0x68, 0x69, 0x75, 0x76, (byte) 0x84, (byte) 0x85
    };


    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH}, {0x5, MyCmd.Keycode.BT_DIAL},
            {0x6, MyCmd.Keycode.MULT_BACK_AND_HANG}, {0x8, MyCmd.Keycode.PREVIOUS}, {0x9, MyCmd.Keycode.NEXT}, {0xa, MyCmd.Keycode.MODLE}, {0xb, MyCmd.Keycode.SPEECH},
            {0xc, MyCmd.Keycode.KEY_DISPLAY}, {0xd, MyCmd.Keycode.HOME},


    };

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
            // case 0:
            // cmd [2] = 0x24;
            // break;
            // case 0:
            // cmd [2] = 0x25;
            // break;
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
            data = (byte) ((data & 0xff) * 2);
        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[12];

        airData[0] = (byte) ((data[2] & 0x08) | ((data[2] & 0x40) << 1) | ((data[2] & 0x01) << 6) | ((data[3] & 0x10) << 1) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));

        airData[7] = (byte) (((data[2] & 0x04) << 5));

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

        if ((data[7] & 0xff) >= 0x10) {
            data[7] = (byte) ((data[7] & 0xff) - 0x10 + 0x9);
        }

        airData[1] |= (byte) (data[7] & 0x0f);

        airData[2] = (byte) (data[8] & 0xff);
        airData[3] = (byte) (data[9] & 0xff);

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    byte[] data0x84 = new byte[8];
    byte[] data0x85 = new byte[28];

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case (byte) 0x84:
                Util.byteArrayCopy(data0x84, data, 0, 0, len);
                break;
            case (byte) 0x85:
                Util.byteArrayCopy(data0x85, data, 0, 0, len);
                break;
        }
        super.parseCanboxData(data, len);
    }

    public void setMediaSrc(int source) {
        byte s = 0;
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                s = 1;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0xd;
                break;
            case MyCmd.SOURCE_AV_OFF:
                s = 0;
                break;
            case MyCmd.SOURCE_BT_MUSIC:
                s = (byte) 0x85;
                break;
            default:
                return;
        }
        byte[] buf = new byte[]{0x0d, (byte) 0xe1, s, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        sendDataToCanbox(buf, buf.length);
    }

    public int getOutTemp(byte[] data) {//
        short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
        return t;
    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        if (data[1] == (byte) 0x90) {
            if (data[3] == (byte) 0x84) {
                sendCanboxInfo("com.canboxsetting", data0x84);
            } else if (data[3] == (byte) 0x85) {
                sendCanboxInfo("com.canboxsetting", data0x85);
            }
        }
        super.sendDataToCanboxHiword1(data, len);
    }

    public void startConnect() {

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


        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        //		Log.d("cccc", ""+curDate.getYear());
        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{0x08, (byte) 0xea, y, mon, d, h, m, s, 0, 0};

        sendDataToCanbox(buf, buf.length);
    }
}
