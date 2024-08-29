package com.zhuchao.android.car.cartype.ods;

import android.os.Handler;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class CadillacKaiLeiDeOD extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.MULT_BACK_AND_HANG}, {0x5, MyCmd.Keycode.BT}, {0x6, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.MULT_SPEECH_AND_BT},};
    private final static byte[][] KEYS_WHEEL2 = {{0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.AUDIO}, {0x3, MyCmd.Keycode.BACK}, {0x4, MyCmd.Keycode.HOME}, {0x5, MyCmd.Keycode.RADIO}, {0x6, MyCmd.Keycode.KEY_SEEK_PREV}, {0x7, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x8, MyCmd.Keycode.SETUP}, {0x9, MyCmd.Keycode.MUTE}, {0xa, MyCmd.Keycode.NAVIGATION}, {0xb, MyCmd.Keycode.PLAY_PAUSE}, {0xd, MyCmd.Keycode.BT_DIAL}, {0xe, MyCmd.Keycode.BT_HANG}, {0x10, MyCmd.Keycode.VOLUME_UP}, {0x11, MyCmd.Keycode.VOLUME_DOWN}, {0x12, MyCmd.Keycode.KEY_TURN_A}, {0x13, MyCmd.Keycode.KEY_TURN_D}, {0x21, MyCmd.Keycode.NUMBER1}, {0x22, MyCmd.Keycode.NUMBER2}, {0x23, MyCmd.Keycode.NUMBER3}, {0x24, MyCmd.Keycode.NUMBER4}, {0x25, MyCmd.Keycode.NUMBER5}, {0x26, MyCmd.Keycode.NUMBER6}, {0x27, MyCmd.Keycode.KEYAMS_RPT}, {0x28, MyCmd.Keycode.AS}, {0x2a, MyCmd.Keycode.SPEECH}, {0x2b, MyCmd.Keycode.NEXT}, {0x2c, MyCmd.Keycode.PREVIOUS},};

    public CadillacKaiLeiDeOD() {
        //		buildCmdDoor((byte) 0x41, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

        buildCmdAngle((byte) 0x26, (byte) 0x0, 540);

        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);

        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x21;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x50;
        MAP_KEYS2 = KEYS_WHEEL2;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {

        } else if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else {
            data = (byte) (36 + (((data & 0xff) - 1)));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];
        airData[0] = data[2];
        airData[1] = data[3];
        airData[2] = data[4];
        airData[3] = data[5];
        airData[4] = data[6];

        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0x41) {
            if (data[2] == 1) {
                if (mDoorStatus != (byte) (data[3] & 0x1F)) {
                    mDoorStatus = (byte) (data[3] & 0x1F);
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }
            }
        }
        super.parseCanboxData(data, len);
    }


}
