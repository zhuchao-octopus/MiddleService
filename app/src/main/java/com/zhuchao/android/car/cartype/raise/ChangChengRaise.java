package com.zhuchao.android.car.cartype.raise;

import android.util.Log;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Calendar;


public class ChangChengRaise extends Canbox {

    public ChangChengRaise() {
        mIdAC = 0x23;
        buildCmdRepeatSendCarType(getCarTypeCmd(), 1);
        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
        buildCmdRadarFront((byte) 0x27, (byte) 0x1, (byte) 0x7);
        buildCmdRadarBack((byte) 0x26, (byte) 0x1, getBackRadarNum());
        buildCmdAngle((byte) 0x30, (byte) 0x4, 0x157c);
        buildCmdOutTemp((byte) 0x23, (byte) 0x0);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        buildCmdEQ((byte) 0x37, (byte) 0x2, 6);
        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x22;
        MAP_KEYS2 = KEYS_WHEEL2;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0x85, 0x01, 0};
        switch (CarUtil.getModelId()) {
            case 0:
                cmd[2] = 1;
                break;
            case 5:
                cmd[2] = 2;
                break;
            case 6:
                cmd[2] = 3;
                break;
            case 7:
                cmd[2] = 4;
                break;
            case 16:
                cmd[2] = 5;
                break;
            case 13:
                cmd[2] = 6;
                break;
            case 14:
                cmd[2] = 7;
                break;
            case 31:
                cmd[2] = 8;
                break;
            case 10:
                cmd[2] = 9;
                break;
            case 8:
                cmd[2] = 0xa;
                break;
            case 33:
                cmd[2] = 0xb;
                break;
            case 15:
            case 25:
            case 32:
                cmd[2] = 0xc;
                break;
            case 30:
            case 46:
                cmd[2] = 0xd;
                break;
            case 37:
                cmd[2] = 0xe;
                break;
            case 39:
                cmd[2] = 0xf;
                break;
            case 28:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x23;
                } else {
                    cmd[2] = 0x20;
                }
                break;
            case 48:
                cmd[2] = 0x21;
                break;
            case 49:
                cmd[2] = 0x22;
                break;
            case 50:
                cmd[2] = 0x25;
                break;
            default:
                return null;
        }
        return cmd;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {
            0x35, 0x23, 0x28, 0x29, 0x31, 0x34, 0x36, 0x38, 0x39, 0x3f, 0x37
    };

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x6, MyCmd.Keycode.MUTE}, {0x7, KEY_SOURCE}, {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG}, {0xC, KEY_NEXTSONG},
            {0xB, KEY_PREVIOUSSONG}, {0xD, MyCmd.Keycode.SPEECH}, {0xE, MyCmd.Keycode.MUTE},
    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x7, MyCmd.Keycode.RADIO}, {0x9, MyCmd.Keycode.MUTE}, {0x21, MyCmd.Keycode.VOLUME_UP}, {0x22, MyCmd.Keycode.VOLUME_DOWN}, {0x29, MyCmd.Keycode.KEY_SEEK_PREV},
            {0x30, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x31, MyCmd.Keycode.MODLE}, {0x32, MyCmd.Keycode.HOME}, {0x33, MyCmd.Keycode.BT_DIAL}, {0x34, MyCmd.Keycode.BT_HANG}, {0x35, MyCmd.Keycode.SETUP},
            {0x36, MyCmd.Keycode.NAVIGATION}, {0x37, MyCmd.Keycode.CANBOX_AC_WIND_UP}, {0x38, MyCmd.Keycode.CANBOX_AC_WIND_DOWN}, {0x39, MyCmd.Keycode.CANBOX_OPEN_AC_VIEW}, {0x3a, MyCmd.Keycode.BACK},
            {0x3b, MyCmd.Keycode.KEY_REPEAT}, {0x3c, MyCmd.Keycode.EQ}, {0x40, MyCmd.Keycode.PREVIOUS}, {0x41, MyCmd.Keycode.NEXT}, {0x42, MyCmd.Keycode.PREVIOUS}, {0x43, MyCmd.Keycode.NEXT},
            {0x44, MyCmd.Keycode.PLAY_PAUSE}, {0x45, MyCmd.Keycode.PREVIOUS}, {0x46, MyCmd.Keycode.NEXT}, {0x47, MyCmd.Keycode.PREVIOUS}, {0x48, MyCmd.Keycode.NEXT},
            //		{ 0x49, MyCmd.Keycode.SETUP },
    };


    private void parseWheelKey(byte[] data) {
        if (doKeyStudy(data[2], data[3])) {
            return;
        }
        byte key = 0;
        int i;
        for (i = 0; i < KEYS_WHEEL.length; ++i) {
            if (KEYS_WHEEL2[i][0] == data[2]) {
                key = KEYS_WHEEL2[i][1];
                break;
            }
        }

        if (key != 0) {
            doKey(key, data[3]);
            if (i > 7) {
                doKey(key, 0);
            }
        } else {
            doKey(0, 0);
        }
    }

    private byte getBackRadarNum() {
        if (CarUtil.getModelId() == 0 || CarUtil.getModelId() == 6) {
            return 4;
        }
        return 7;
    }

    private byte getAngelStyle() {
        if (CarUtil.getModelId() == 7 || CarUtil.getModelId() == 15 || CarUtil.getModelId() == 25 || CarUtil.getModelId() == 32) {
            return 1;
        }
        return 0;
    }


    public int getAngleValue(byte[] data) {

        short a;
        int angle;
        int max;

        if (getAngelStyle() == 0) {
            a = (short) ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));
            angle = -a;
            max = 5500;
        } else {
            a = (short) ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));
            angle = a / 20;
            max = 510;
            if ((data[3] & 0x1) != 0) {
                angle = -angle;
            }
        }

        angle = ((angle * 3000) / max);
        if (angle > -50 && angle < 50) {
            angle = 50;
        }

        return angle;
    }

    private void parseSeatHeat(byte[] data) {
        airData[4] = (byte) (data[2] & 0x33);
        airData[9] = 0;
        if ((data[2] & 0x4) != 0) {
            airData[9] |= 0x04;
        }
        if ((data[2] & 0x40) != 0) {
            airData[9] |= 0x40;
        }
        if (!sendSeatHeat(airData)) {
            super.parseACInfo(airData);
        }
    }

    public void parseCanboxData(byte[] data, int len) {
        switch (data[0]) {
            case 0x25:
                parseWheelKey(data);
                break;
            case 0x35:
                parseSeatHeat(data);
                break;
            default:
                super.parseCanboxData(data, len);
        }
    }

    public int getOutTemp(byte[] data) {//
        int t = CarUtil.CLEAR_OUT_DOOR_TEMP;
        if (data.length > 6) {

            t = -400 + (((data[7] & 0xff)) * 5);
        }
        return t;
    }
    //	private int getACStyle() {
    //		if (CarUtil.getModelId() == 10) {
    //			return false;
    //		}
    //		return true;
    //	}

    private int getACTempPriv(byte data) {//
        if ((data & 0xff) >= 0x70 && (data & 0xff) <= 0x90) {
            data = (byte) (32 + (data - 0x70));
        } else {

        }
        return data & 0xff;
    }

    private final byte[] airData = new byte[10];

    public void parseACInfo(byte[] data) {

        airData[0] = (byte) ((data[2] & 0xfc) | ((data[2] & 0x02) >> 1) | ((data[2] & 0x01) << 1));

        switch ((data[3] & 0xff)) {
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
            case 6:
                airData[1] = (byte) (0xc0);
                break;
            case 7:
                airData[1] = (byte) (0xe0);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);

        airData[2] = (byte) getACTempPriv(data[5]);
        airData[3] = (byte) getACTempPriv(data[6]);

        if ((data[5] & 0xff) > 0 && (data[5] & 0xff) < 0x70) {
            airData[7] |= 0x40;
        } else {
            airData[7] &= ~0x40;
        }
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        //		++play;
        byte[] data;

        if (MyCmd.SOURCE_DVD != source) {
            data = new byte[]{
                    (byte) 0xc3, 0x6, (byte) (total & 0xFF), (byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF), (byte) ((play >> 8) & 0xFF), min, sec
            };
        } else {
            data = new byte[]{
                    (byte) 0xc3, 0x6, (byte) (1 & 0xFF), (byte) ((play) & 0xFF), (byte) (total & 0xFF), (byte) ((0) & 0xFF), min, sec
            };
        }
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        byte[] data = new byte[]{(byte) 0xc2, 0x4, b[0], b[1], b[2], 0};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source) {//default is simple box
        byte s = 0;
        byte mediaType = 0;
        switch (source) {
            case 0:
                s = 1;
                mediaType = 1;
                break;
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
                s = 0x09;
                mediaType = 0x11;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                mediaType = 0x30;
                byte[] data2 = new byte[]{(byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0};
                sendDataToCanbox(data2, data2.length);
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                mediaType = 0x30;
                byte[] data3 = new byte[]{(byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0};
                sendDataToCanbox(data3, data3.length);
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0b;
                mediaType = 0x30;
                break;
            default:
                s = 0x00;
                mediaType = 0x0;
                break;
        }
        byte[] data;

        data = new byte[]{(byte) 0xc0, 0x2, s, mediaType};

        sendDataToCanbox(data, data.length);
    }

    private void sendACControl() {

    }

    public void doCanboxFunctionKey(byte key) {
        switch (key) {
            case MyCmd.Keycode.CANBOX_AC_WIND_UP:
                Log.d("fcck", "!!!");
                break;
            case MyCmd.Keycode.CANBOX_AC_WIND_DOWN:
                Log.d("fcck", "!!!");
                break;
        }
    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (40 << 16) | (21 << 8) | 21;

            byte[] buf = new byte[]{(byte) 0x90, 0x2, 0x37, 0};
            sendDataToCanbox(buf, buf.length);
        } else {
            byte[] buf = new byte[]{(byte) 0x86, 0x2, 0x0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    buf[2] = 2;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    buf[2] = 3;
                    break;
                case EQ_CMD_SET_LOW:
                    buf[2] = 4;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    buf[2] = 6;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    buf[2] = 5;
                    break;
                case EQ_CMD_SET_VOLUME:
                    buf[2] = 1;
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

    public void updateCompass(int compass) {
        int direction = compassAngleToDirect(compass);

        byte[] buf = new byte[]{
                (byte) (0x83), 0x3, 0x15, (byte) (direction & 0xff), 0
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

        byte m = (byte) c.get(Calendar.MINUTE);
        byte[] buf = new byte[]{(byte) 0x83, 0x03, 0x03, h, m};

        sendDataToCanbox(buf, buf.length);


    }

}
