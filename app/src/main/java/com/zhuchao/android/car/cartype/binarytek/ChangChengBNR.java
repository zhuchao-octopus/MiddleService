package com.zhuchao.android.car.cartype.binarytek;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Calendar;


public class ChangChengBNR extends Canbox {

    public ChangChengBNR() {
        mIdAC = 0x2;
        buildCmdDoor((byte) 0x3, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
        buildCmdRadarFront((byte) 0x9, (byte) 0x0, (byte) 0xe);
        buildCmdRadarBack((byte) 0x9, (byte) 0x0, (byte) 0xe);
        buildCmdRadarFrontEx((byte) 1);
        buildCmdRadarBackEx((byte) 5);
        buildCmdAngle((byte) 0x6, (byte) 0x0, 0x1500);

        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        buildCmdEQ((byte) 0x31, (byte) 0x2, 6);
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }


    private final static byte[] IDS_TO_CANBOXSETTING = {
            0x10, 0x11, 0x13, 0x4,
    };

    private final static byte[][] KEYS_WHEEL = {

            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG}, {0x5, MyCmd.Keycode.MUTE}, {0x7, KEY_SOURCE}, {0x9, MyCmd.Keycode.BT_DIAL},
            {0xa, MyCmd.Keycode.BT_HANG}, {0xb, MyCmd.Keycode.SPEECH}, {0x20, MyCmd.Keycode.KEY_AIR_CONTROL}, {0x21, MyCmd.Keycode.KEY_AIR_CONTROL},
    };


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

    private int getACTempPriv(byte data) {//

        //		if (CarUtil.getModelId() != 0x10) {
        //			return (byte) (0xfa);
        //		}

        if ((data & 0xff) == 0) {

        } else if ((data & 0xff) == 0x20) {
            data = (byte) (0xff);
        } else if ((data & 0xff) >= 1 && (data & 0xff) <= 0x1f) {
            data = (byte) (32 + (data));
        } else {
            data = (byte) (0xfa);
        }
        return data & 0xff;
    }

    private final byte[] airData = new byte[12];

    public void parseACInfo(byte[] data) {

        airData[0] = (byte) ((data[2] & 0xef));


        airData[1] = (byte) ((data[3] & 0xef) | ((data[6] & 0x80) >> 3));

        airData[2] = (byte) getACTempPriv(data[4]);
        airData[3] = (byte) getACTempPriv(data[5]);

        if (data.length > 9) {
            airData[9] = (byte) ((data[8] & 0x80) | ((data[8] & 0x40) >> 5));
            airData[11] = (byte) (((data[8] & 0x30) << 1) | (data[8] & 0xf));
            airData[10] = (byte) getACTempPriv(data[9]);
        }

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {//default is simple box


    }

    public void parseEQ(int id, byte[] buf) {
        byte[] data = new byte[6];
        data[0] = buf[5];
        data[1] = buf[4];
        data[2] = buf[3];
        data[3] = buf[8];
        data[4] = buf[9];
        data[5] = buf[2];
        super.returnEQData(EQ_CMD_SET_ALL_DATA, data);
    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (40 << 16) | (21 << 8) | 21;

            byte[] buf = new byte[]{(byte) 0x83, 0x2, 0x31, 0};
            sendDataToCanbox(buf, buf.length);
        } else {
            byte[] buf = new byte[]{(byte) 0x84, 0x2, 0x0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    buf[2] = 3;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    buf[2] = 4;
                    break;
                case EQ_CMD_SET_LOW:
                    buf[2] = 5;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    buf[2] = 1;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    buf[2] = 2;
                    break;
                case EQ_CMD_SET_VOLUME:
                    buf[2] = 8;
                    break;
                default:
                    return 0;
            }

            sendDataToCanbox(buf, buf.length);
        }
        return ret;
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
