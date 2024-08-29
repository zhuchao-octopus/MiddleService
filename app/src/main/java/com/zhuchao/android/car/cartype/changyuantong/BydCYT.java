package com.zhuchao.android.car.cartype.changyuantong;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class BydCYT extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x29, 0x2a, 0x2b};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x5, MyCmd.Keycode.BT}, {0x6, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.SPEECH}, {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG}, {0xb, MyCmd.Keycode.KEY_SIDE_CAMERA}, {0xc, MyCmd.Keycode.POWER}, {0xd, MyCmd.Keycode.ROLL_NEXT}, {0xe, MyCmd.Keycode.ROLL_PREV}, {0xf, MyCmd.Keycode.PLAY_PAUSE}, {0x10, MyCmd.Keycode.NAVIGATION}, {0x11, MyCmd.Keycode.RADIO}, {0x12, MyCmd.Keycode.KEY_AIR_CONTROL},};

    public BydCYT() {
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x4);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x4);
        buildCmdAngle((byte) 0x26, (byte) 0x0, 540);

        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x21;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x7f) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0) {

        } else {

        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xef));
        airData[1] = data[3];
        airData[2] = data[4];
        airData[3] = data[5];

        airData[7] = (byte) ((data[2] & 0x10) << 1);

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


}
