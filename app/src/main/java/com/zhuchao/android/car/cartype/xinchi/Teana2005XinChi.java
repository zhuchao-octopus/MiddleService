package com.zhuchao.android.car.cartype.xinchi;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;

public class Teana2005XinChi extends Canbox {

    public Teana2005XinChi() {
        buildCmdDoor((byte) 0x24, (byte) 0x2, (byte) 0xfc, (byte) 0x02);
        //		buildCmdRadarFront((byte) 0xc, (byte) 0x0, (byte) 0x4, (byte) 3);
        //		buildCmdRadarBack((byte) 0x9, (byte) 0x0, (byte) 0x4, (byte) 3);
        buildCmdAngle((byte) 0x9, (byte) 0x0, 0x1d0);
        buildCmdEQ((byte) 0x13, (byte) 0x0, 6);
        //		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x28;
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x11;
        MAP_KEYS2 = KEYS_WHEEL2;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0xb, 0x13, 0x30, 0x33};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x4, MyCmd.Keycode.KEY_SEEK_PREV}, {0x5, MyCmd.Keycode.MODLE},
            {0x6, MyCmd.Keycode.BT_DIAL}, {0x7, MyCmd.Keycode.BT_HANG},

    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x3, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x4, MyCmd.Keycode.KEY_AM}, {0x5, MyCmd.Keycode.KEY_FM},
            {0x6, MyCmd.Keycode.MODLE}, {0x7, MyCmd.Keycode.DVD}, {0x8, MyCmd.Keycode.ROLL_NEXT}, {0x9, MyCmd.Keycode.ROLL_PREV},

            {0x10, MyCmd.Keycode.NEXT}, {0x11, MyCmd.Keycode.PREVIOUS}, {0x12, MyCmd.Keycode.AS}, {0x20, MyCmd.Keycode.HOME}, {0x21, MyCmd.Keycode.NAVIGATION},


    };

    @Override
    public int getAngleValue(byte[] data) {
        int max = 0x80;

        int angle = (data[2] & 0xff);
        if (angle < 0x80) {
            angle = (0x80 - angle);
        } else {
            angle = (0x80 - angle);
        }

        angle = angle * 3000 / max;
        return angle;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else if (((data & 0xff) >= 1) && ((data & 0xff) <= 0x1d)) {
            data = (byte) (35 + (data & 0xff));
        } else if ((data & 0xff) == 0) {

        } else {
            data = (byte) 0xfa;
        }
        return data;
    }

    public void parseACInfo(byte[] data) {
        byte[] airData = new byte[8];

        airData[0] = (byte) (data[2] & 0xf7);
        airData[1] = (byte) (data[3] & 0xef);


        airData[2] = data[4];
        airData[3] = data[5];

        //		airData[5] = (byte) (
        //				((data[6] & 0x40) >> 2)
        //				);

        airData[7] = (byte) (((data[6] & 0x80) >> 2));


        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte[] mData = new byte[]{
                (byte) 0x88, 0x4, 4, 0, 0, 0
        };
        sendDataToCanbox(mData, mData.length);

    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        super.startConnect();
        byte[] mData = new byte[]{(byte) 0xff, 0x1, (byte) 0x7f};
        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {


    }

    public void setMediaSrc(int source) {


    }

    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        byte format = 0;

        String date_foramt = SettingProperties.getProperty(mContext, SettingProperties.KEY_DATE_FORMAT);
        if (date_foramt != null) {
            if ("dd/MM/yyyy".equals(date_foramt)) {
                format = 0;
            } else if ("MM/dd/yyyy".equals(date_foramt)) {
                format = 2;
            }
        }

        if ("12".equals(strTimeFormat)) {
            ampm = 1;
        }

        byte m = (byte) curDate.getMinutes();

        //		Log.d("cccc", ""+curDate.getYear());
        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{(byte) 0x87, 0x06, y, mon, d, format, h, m};

        sendDataToCanbox(buf, buf.length);
    }

    public void setVolume(int volume) {
        byte[] data = new byte[]{(byte) 0x8f, 0x3, 0x1, (byte) volume, 1};
        sendDataToCanbox(data, data.length);
    }

    private void sendEQCmd(byte id, int step) {

        if (step == 0) {
            return;
        } else if (step > 0) {
            step--;
        } else {
            step++;
        }
        byte[] buf = new byte[]{(byte) 0xa3, 0x2, id, 0};
        sendDataToCanbox(buf, buf.length);

        mHandler.removeMessages(SET_EQ_STEP);
        if (step != 0) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(SET_EQ_STEP, id, step), 200);
        }
    }

    private final static int SET_EQ_STEP = 1;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == SET_EQ_STEP) {
                sendEQCmd((byte) msg.arg1, msg.arg2);
            }
            super.handleMessage(msg);
        }
    };

    byte[] mEQBuf = new byte[]{5, 0, 5, 5, 5, 0};

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (16 << 16) | (11 << 8) | 11;

            byte[] buf = new byte[]{(byte) 0xf1, 0x1, 0x13};
            sendDataToCanbox(buf, buf.length);

        } else {
            byte id;
            int step;
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    step = data - mEQBuf[0];
                    if (step > 0) {
                        id = 0x5;
                    } else {
                        id = 0x6;
                    }
                    break;
                case EQ_CMD_SET_LOW:
                    step = data - mEQBuf[2];
                    if (step > 0) {
                        id = 0x5;
                    } else {
                        id = 0x6;
                    }
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    step = data - mEQBuf[3];
                    if (step > 0) {
                        id = 0x5;
                    } else {
                        id = 0x6;
                    }
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    step = data - mEQBuf[4];
                    if (step > 0) {
                        id = 0x5;
                    } else {
                        id = 0x6;
                    }
                    break;
                case EQ_CMD_SET_VOLUME:
                    step = data - mEQBuf[0];
                    if (step > 0) {
                        id = 0x1;
                    } else {
                        id = 0x2;
                    }
                    break;
                default:
                    return 0;
            }

            sendEQCmd(id, step);

        }
        return ret;
    }

    public void parseEQ(int id, byte[] buf) {

        mEQBuf[0] = (byte) (buf[4] + 5);
        mEQBuf[2] = (byte) (buf[3] + 5);
        mEQBuf[3] = (byte) (buf[6] + 5);
        mEQBuf[4] = (byte) (buf[5] + 5);
        mEQBuf[5] = buf[2];

        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQBuf);
    }
}
