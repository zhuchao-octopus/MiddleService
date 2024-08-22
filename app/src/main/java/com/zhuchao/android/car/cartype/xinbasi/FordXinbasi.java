package com.zhuchao.android.car.cartype.xinbasi;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

public class FordXinbasi extends Canbox {

    public FordXinbasi() {
        buildCmdDoor((byte) 0xb, (byte) 0x1, (byte) 0xf8, (byte) 0x02);
        //		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
        buildCmdRadarBack((byte) 0x4, (byte) 0x0, (byte) 0x3, (byte) 11, (byte) 0x4);
        buildCmdAngle((byte) 0x5, (byte) 0x0, 0x1200);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x2;
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {
            0x3, 0x4, 0x7, 0x8, 0xa, 0xb
    };

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS},

            {0x7, MyCmd.Keycode.MODLE}, {0xe, MyCmd.Keycode.PREVIOUS}, {0xf, MyCmd.Keycode.NEXT}, {0x10, MyCmd.Keycode.PREVIOUS}, {0x11, MyCmd.Keycode.NEXT}, {0x12, MyCmd.Keycode.PLAY_PAUSE},
            {0x13, MyCmd.Keycode.MUTE}, {0x20, MyCmd.Keycode.NUMBER0}, {0x21, MyCmd.Keycode.NUMBER1}, {0x22, MyCmd.Keycode.NUMBER2}, {0x23, MyCmd.Keycode.NUMBER3}, {0x24, MyCmd.Keycode.NUMBER4},
            {0x25, MyCmd.Keycode.NUMBER5}, {0x26, MyCmd.Keycode.NUMBER6}, {0x27, MyCmd.Keycode.NUMBER7}, {0x28, MyCmd.Keycode.NUMBER8}, {0x29, MyCmd.Keycode.NUMBER9},
            {0x2a, MyCmd.Keycode.NUMBER_STAR}, {0x2b, MyCmd.Keycode.NUMBER_POUND}, {0x33, MyCmd.Keycode.SPEECH}, {0x34, MyCmd.Keycode.RADIO}, {0x35, MyCmd.Keycode.DVD}, {0x36, MyCmd.Keycode.AUX_IN},
            {0x37, MyCmd.Keycode.HOME}, {0x38, MyCmd.Keycode.EQ}, {0x39, MyCmd.Keycode.BT}, {0x3d, MyCmd.Keycode.TIME_SETTING}, {0x3f, MyCmd.Keycode.POWER}, {0x48, MyCmd.Keycode.PLAY_PAUSE},
            {0x49, MyCmd.Keycode.PREVIOUS}, {0x4a, MyCmd.Keycode.NEXT}, {0x4b, MyCmd.Keycode.PREVIOUS}, {0x4c, MyCmd.Keycode.NEXT}, {0x52, MyCmd.Keycode.MULT_PREV_AND_RECEIVE},
            {0x53, MyCmd.Keycode.MULT_NEXT_AND_HANG}, {0x54, MyCmd.Keycode.EJECT}, {0x56, MyCmd.Keycode.RDS_TA_SWITCH}, {0x57, MyCmd.Keycode.SETUP}, {0x59, MyCmd.Keycode.EQ},
            {0x5a, MyCmd.Keycode.MUTE}, {0x5b, MyCmd.Keycode.KEY_DISPLAY}, {0x5c, MyCmd.Keycode.PREVIOUS}, {0x5d, MyCmd.Keycode.PREVIOUS}, {0x5e, MyCmd.Keycode.NEXT}, {0x5f, MyCmd.Keycode.NEXT},
            {(byte) 0xf0, MyCmd.Keycode.VOLUME_ROLL_UP}, {(byte) 0xf1, MyCmd.Keycode.VOLUME_DOWN},
    };

    @Override
    public int getAngleValue(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

        return angle;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0xff) {
            //data = (byte)0xff;
        } else if ((data & 0xff) == 0x0) {
            data = 0;
        } else if ((data & 0xff) == 0x1) {
            data = (byte) 0xfa;
        } else if ((data & 0xff) > 0x3f || (data & 0xff) < 0x1f) {
            //data = (byte)0xfa;
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

        airData[5] = (byte) ((data[6] & 0x40) >> 6);
        airData[5] |= 0x80;
        airData[4] = (byte) ((data[6] & 0x04));

        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {


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

}
