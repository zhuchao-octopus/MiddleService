package com.zhuchao.android.car.cartype.raise;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class HaiMaRaise extends Canbox {

    public HaiMaRaise() {
        mIdAC = 0x23;
        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
        buildCmdRadarBack((byte) 0x24, (byte) 0x0, (byte) 3);
        buildCmdAngle((byte) 0x30, (byte) 0x4, 0x1800);
        buildCmdOutTemp((byte) 0x23, (byte) 0x0);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x22;
        MAP_KEYS2 = KEYS_WHEEL2;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x31};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG},

            {0x5, MyCmd.Keycode.BT}, {0x6, MyCmd.Keycode.MUTE}, {0x7, KEY_SOURCE}, {0x8, MyCmd.Keycode.SPEECH}, {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG}, {0xC, KEY_NEXTSONG},
            {0xB, KEY_PREVIOUSSONG}, {0xe, KEY_NEXTSONG}, {0xd, KEY_PREVIOUSSONG}, {0xf, MyCmd.Keycode.PLAY_PAUSE}, {0x17, MyCmd.Keycode.HOME}, {0x18, MyCmd.Keycode.BACKLIGHT_OFF},
    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.SETUP}, {0x5, MyCmd.Keycode.EQ}, {0x6, MyCmd.Keycode.BACK},
            {0x7, MyCmd.Keycode.RADIO}, {0x8, MyCmd.Keycode.DVD}, {0x9, MyCmd.Keycode.MUTE}, {0xa, MyCmd.Keycode.NUMBER1}, {0xb, MyCmd.Keycode.NUMBER2}, {0xc, MyCmd.Keycode.NUMBER3},
            {0xd, MyCmd.Keycode.NUMBER4}, {0xe, MyCmd.Keycode.NUMBER5}, {0xf, MyCmd.Keycode.NUMBER6}, {0x10, MyCmd.Keycode.NUMBER7}, {0x11, MyCmd.Keycode.NUMBER8}, {0x12, MyCmd.Keycode.NUMBER9},
            {0x13, MyCmd.Keycode.NUMBER0}, {0x14, MyCmd.Keycode.DVD}, {0x15, MyCmd.Keycode.EJECT}, {0x16, MyCmd.Keycode.SETUP}, {0x17, MyCmd.Keycode.TIME_SETTING}, {0x18, MyCmd.Keycode.RADIO},
            {0x19, MyCmd.Keycode.AS}, {0x20, MyCmd.Keycode.PLAY_PAUSE}, {0x21, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x22, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x23, MyCmd.Keycode.KEY_SEEK_NEXT},
            {0x24, MyCmd.Keycode.KEY_SEEK_PREV}, {0x25, MyCmd.Keycode.PLAY_PAUSE}, {0x26, MyCmd.Keycode.PREVIOUS}, {0x27, MyCmd.Keycode.NEXT}, {0x28, MyCmd.Keycode.AUX_IN},
            {0x29, MyCmd.Keycode.ROLL_NEXT}, {0x30, MyCmd.Keycode.ROLL_PREV}, {0x31, MyCmd.Keycode.MODLE}, {0x32, MyCmd.Keycode.BT_DIAL}, {0x33, MyCmd.Keycode.BT_HANG}, {0x36, MyCmd.Keycode.HOME},
            {0x34, MyCmd.Keycode.AS}, {0x35, MyCmd.Keycode.SETUP}, {0x37, MyCmd.Keycode.EASY_CONNECT},
    };


    public int getAngleValue(byte[] data) {

        int angle;
        int max;

        angle = ((data[2] & 0xff) | (((data[3] & 0x7f)) << 8));
        if ((data[3] & 0x80) == 0) {
            angle = -angle;
        }
        max = 0x1800;

        angle = ((angle * 3000) / max);
        if (angle > -50 && angle < 50) {
            angle = 50;
        }

        return angle;
    }


    @Override
    public int getACTemp(byte data, int unit) {
        if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else {
            if (unit == 0) {
                data = (byte) (32 + (data - 0x1));
            } else {
                data = (byte) (59 + data);
            }
        }
        return data & 0xff;
    }


    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[8];
        airData[0] = (byte) ((data[2] & 0xe8));
        airData[1] = data[3];
        airData[2] = data[4];
        airData[3] = data[5];


        airData[0] |= (byte) (((data[6] & 0x80) >> 6) | ((data[6] & 0x40) >> 6));

        airData[5] = (byte) ((data[6] & 0x01));

        airData[4] = (byte) (((data[8] & 0x80) >> 4));
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {//default is simple box

    }


}
