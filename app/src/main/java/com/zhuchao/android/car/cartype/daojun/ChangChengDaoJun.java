package com.zhuchao.android.car.cartype.daojun;

import android.provider.Settings;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Calendar;

public class ChangChengDaoJun extends Canbox {

    public ChangChengDaoJun() {
        buildCmdDoor((byte) 0x3, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

        buildCmdRadarBack((byte) 0x9, (byte) 0x0, (byte) 0xe);
        buildCmdRadarBackEx((byte) 0x5);
        buildCmdRadarFront((byte) 0x9, (byte) 0x0, (byte) 0xe);
        buildCmdRadarFrontEx((byte) 0x1);
        buildCmdEQ((byte) 0x31, (byte) 0x0, 6);
        buildCmdAngle((byte) 0x6, (byte) 0x0, 12165);
        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x2;
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x21;
        MAP_KEYS2 = KEYS_WHEEL2;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x4, 0x10, 0x11, 0x13};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT}, {0x5, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE},
            {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG}, {0xb, MyCmd.Keycode.SPEECH}, {0x10, MyCmd.Keycode.KEY_AIR_CONTROL}, {0x11, MyCmd.Keycode.KEY_AIR_CONTROL},
    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x2, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x3, MyCmd.Keycode.ROLL_NEXT}, {0x4, MyCmd.Keycode.ROLL_PREV}, {0x5, MyCmd.Keycode.POWER},
            {0x6, MyCmd.Keycode.PLAY_PAUSE}, {0x7, MyCmd.Keycode.EJECT}, {0x8, MyCmd.Keycode.NAVIGATION}, {0x9, MyCmd.Keycode.HOME}, {0xa, MyCmd.Keycode.BACK}, {0xb, MyCmd.Keycode.BT},
            {0xc, MyCmd.Keycode.AUDIO}, {0xd, MyCmd.Keycode.RADIO},
    };


    private final byte[] airData = new byte[12];

    private void parseSeatHeat(byte[] data) {
        airData[4] = (byte) (((data[2] & 0x0f) << 4) | ((data[4] & 0x0f) >> 0));

        airData[8] = (byte) (((data[2] & 0xf0) << 0) | ((data[4] & 0xf0) >> 2));

        //		if (!sendSeatHeat(airData)) {
        super.parseACInfo(airData);
        //		}
    }

    public void parseCanboxData(byte[] data, int len) {
        if (data[0] == 0x13) {
            parseSeatHeat(data);
            super.parseCanboxData(data, len);
        } else {
            super.parseCanboxData(data, len);
        }
    }

    @Override
    public int getAngleValue2(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

        return -angle;

    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {

        } else if ((data & 0xff) == 0x20) {
            data = (byte) 0xff;
        } else if ((data & 0xff) >= 1 && (data & 0xff) <= 0x1f) {
            data = (byte) (32 + (((data & 0xff))));
        } else {
            data = (byte) 0xfa;
        }
        return data;
    }


    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[8];
        airData[0] = (byte) ((data[2] & 0xef));
        airData[1] = (byte) ((data[3] & 0xef));


        airData[2] = data[4];
        airData[3] = data[5];


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
            ret = (40 << 16) | (21 << 8) | 21;

            byte[] buf = new byte[]{(byte) 0x90, 0x1, 0x31};
            sendDataToCanbox(buf, buf.length);

        } else {
            byte[] buf = new byte[]{(byte) 0x84, 0x2, 0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    mEQData[2] = 3;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    mEQData[2] = 4;
                    break;
                case EQ_CMD_SET_LOW:
                    mEQData[2] = 5;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    mEQData[2] = 1;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    mEQData[2] = 2;
                    break;
                case EQ_CMD_SET_VOLUME:
                    mEQData[2] = 8;
                    break;
            }

            sendDataToCanbox(buf, buf.length);
        }

        return ret;
    }

    public void parseEQ(int cmd, byte[] buf) {

        mEQData[0] = buf[5];
        mEQData[1] = buf[4];
        mEQData[2] = buf[3];
        mEQData[3] = buf[8];
        mEQData[4] = buf[9];
        mEQData[5] = buf[2];

        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
    }


    public boolean isSupportCompass() {
        return true;
    }

    private int compassAngleToDirectPriv(int compass) {
        int direct = 0x30;
        if (compass >= 22 && compass < 67) {
            direct = 0x60;
        } else if (compass >= 67 && compass < 112) {
            direct = 0;
        } else if (compass >= 112 && compass < 157) {
            direct = 0x40;
        } else if (compass >= 157 && compass < 202) {
            direct = 0x10;
        } else if (compass >= 202 && compass < 247) {
            direct = 0x50;
        } else if (compass >= 247 && compass < 292) {
            direct = 0x20;
        } else if (compass >= 292 && compass < 337) {
            direct = 0x70;
        }
        return direct;
    }

    public void updateCompass(int compass) {
        int direction = compassAngleToDirectPriv(compass);

        byte[] buf = new byte[]{
                (byte) (0xc9), 0x3, (byte) (direction & 0xff), 0, 0
        };

        sendDataToCanbox(buf, buf.length);
    }

    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {

        Calendar c = Calendar.getInstance();

        byte h = (byte) c.get(Calendar.HOUR_OF_DAY);
        h = fixTimeHour(h);


        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        if ("12".equals(strTimeFormat)) {
            h |= 0x80;
        }

        byte m = (byte) c.get(Calendar.MINUTE);
        byte[] buf = new byte[]{(byte) 0xe1, 0x02, h, m};

        sendDataToCanbox(buf, buf.length);


    }
}
