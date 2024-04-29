package com.zhuchao.android.car.cartype.daojun;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Calendar;


public class BydG6DaoJun extends Canbox {

    public BydG6DaoJun() {

        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        //		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

        buildCmdAngle((byte) 0x33, (byte) 0x0, 5011);
        mIdAC = 0xb1;
        mIdKey = 0x6;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x6};

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0x72, 0x02, 1, 1};
        return cmd;
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x2, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x3, MyCmd.Keycode.ROLL_NEXT}, {0x4, MyCmd.Keycode.ROLL_PREV}, {0x5, MyCmd.Keycode.POWER},
            {0x24, MyCmd.Keycode.PLAY_PAUSE},


    };

    @Override
    public int getAngleValue2(byte[] data) {
        // TODO Auto-generated method stub
        int angle = (data[2] & 0xff) | ((data[3] & 0xff) << 8);
        angle = angle - 8000;
        return angle;

    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if (((data & 0xff)) == 0) {
            data = (byte) 0xfa;
        } else if (((data & 0xff)) == 0x11) {
            data = (byte) 0xff;
        } else if (((data & 0xff)) == 1) {
            data = 0;
        } else {
            data = (byte) (36 + (data - 2) * 2);

        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[10];

        switch ((data[3] & 0x38) >> 3) {
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


        airData[1] |= (byte) ((data[3] & 0x07) | ((data[8] & 0x80) >> 3));


        airData[0] = (byte) (((data[5] & 0x10) >> 2)

                | ((data[5] & 0x20) >> 5) | ((data[6] & 0x01) << 4));

        if (((data[5] & 0x03)) == 0x02) {
            airData[0] |= (byte) (0x40);
        }


        if (((data[5] & 0x0c) >> 2) == 0x01) {
            airData[0] |= (byte) (0x20);
        } else if (((data[5] & 0x0c) >> 2) == 0x03) {
            airData[4] = (byte) (0x80);
        }

        airData[2] = (byte) (((data[4] & 0x0f)) | ((data[3] & 0x40) >> 2));
        airData[3] = (byte) (((data[4] & 0xf0) >> 4) | ((data[5] & 0x40) >> 2));


        airData[0] |= 0x80;

        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {

        mLcdInfo[2] = 1;
        if (b[0] >= 0x10) {
            mLcdInfo[4] = 2;
        } else {
            mLcdInfo[4] = 1;
        }

        mLcdInfo[5] = b[2];
        mLcdInfo[6] = b[1];


        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }

    public void setMediaSrc(int source) {//default is simple box

        switch (source) {
            case MyCmd.SOURCE_RADIO:
                mLcdInfo[2] = 1;
                return;
            case MyCmd.SOURCE_DVD:
                mLcdInfo[2] = 2;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                mLcdInfo[2] = 5;
                break;
            case MyCmd.SOURCE_AUX:
                mLcdInfo[2] = 4;
                break;
            case MyCmd.SOURCE_DTV:
                mLcdInfo[2] = 3;
                break;
            default:
                mLcdInfo[2] = 4;
                break;
        }

        mLcdInfo[4] = 0;
        mLcdInfo[5] = 0;
        mLcdInfo[6] = 0;
        mLcdInfo[7] = 0;
        mLcdInfo[8] = 0;
        mLcdInfo[9] = 0;

        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }

    byte[] mLcdInfo = new byte[]{(byte) 0x77, 0x8, 0, 0, 0, 0, 0, 0, 0, 0};

    public void setVolume(int volume) {

        mLcdInfo[3] = (byte) volume;
        sendDataToCanbox(mLcdInfo, mLcdInfo.length);


    }


    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {
        Calendar c = Calendar.getInstance();

        byte h = (byte) c.get(Calendar.HOUR_OF_DAY);
        h = fixTimeHour(h);

        byte m = (byte) c.get(Calendar.MINUTE);
        m |= 0x80;

        byte y = (byte) (c.get(Calendar.YEAR) - 2000);
        y |= 0x80;
        byte mon = (byte) (c.get(Calendar.MONTH) + 1);
        byte d = (byte) c.get(Calendar.DAY_OF_MONTH);

        d |= 0x80;
        byte w = (byte) (c.get(Calendar.DAY_OF_WEEK) - 1);

        byte[] buf = new byte[]{(byte) 0x73, 8, 0, 0, w, m, h, d, mon, y};

        sendDataToCanbox(buf, buf.length);

    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        byte[] buf = new byte[]{
                (byte) 0xf5, 8, 1, (byte) 0x81, 1, (byte) 0x80, 0, (byte) 0x80, 0, (byte) 0x80
        };

        sendDataToCanbox(buf, buf.length);
        buf[0] = (byte) 0xb4;
        buf[1] = 1;
        sendDataToCanbox(buf, buf.length);
        buf[1] = 2;
        sendDataToCanbox(buf, buf.length);
        buf[1] = 3;
        sendDataToCanbox(buf, buf.length);
        buf[1] = 4;
        sendDataToCanbox(buf, buf.length);
        buf[1] = 5;
        sendDataToCanbox(buf, buf.length);
        buf[1] = 6;
        sendDataToCanbox(buf, buf.length);
    }

    @Override
    public void stopConnect() {
        byte[] buf = new byte[]{
                (byte) 0xf5, 8, 1, (byte) 0x81, 0x10, (byte) 0x80, 0, (byte) 0x80, 0, (byte) 0x80
        };

        sendDataToCanbox(buf, buf.length);
    }
}
