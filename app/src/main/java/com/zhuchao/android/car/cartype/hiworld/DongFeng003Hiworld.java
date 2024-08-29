package com.zhuchao.android.car.cartype.hiworld;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Locale;

public class DongFeng003Hiworld extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH},


            {0x8, MyCmd.Keycode.MULT_PREV_AND_RECEIVE}, {0x9, MyCmd.Keycode.MULT_NEXT_AND_HANG},

            {0xa, MyCmd.Keycode.MODLE},};
    private final static byte[][] KEYS_WHEEL2 = {{0x1, MyCmd.Keycode.POWER},


    };
    private final static byte[][] KEYS_WHEEL3 = {{0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0},};


    public DongFeng003Hiworld() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
        // buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
        // buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 0, (byte) 2, (byte) 0, KEYS_WHEEL2);


        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;

    }

    @Override
    public void stopConnect() {

    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x0, 0x26};
        switch (CarUtil.getModelId()) {
            case 11:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 3;
                }
                break;
            case 5:
                cmd[2] = 4;
                break;
            case 12:
                if (CarUtil.getCarTypeConfig() == 2) {
                    cmd[2] = 0xa;
                } else if (CarUtil.getCarTypeConfig() == 1) {
                    cmd[2] = 0xb;
                }
                break;
            case 23:
                if (CarUtil.getCarTypeConfig() == 2) {
                    cmd[2] = 0xa;
                } else if (CarUtil.getCarTypeConfig() == 1) {
                    cmd[2] = 0xb;
                } else if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x3;
                }
                break;
            default:
                return null;
        }
        return cmd;
    }


    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0xff || (data & 0xff) == 0xfa) {

        } else if ((data & 0xff) == 0xfe) {
            data = 0;
        } else {

        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[10];

        airData[0] = (byte) (((data[2] & 0x40) << 1) | ((data[2] & 0x08) << 0) | ((data[3] & 0x44) << 0) | ((data[3] & 0x10) << 1) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));

        //		if (((data[3] & 0x10) == 0)) {
        //			airData[0] |= 0x20;
        //		}

        airData[4] = (byte) (((data[2] & 0x20) >> 3));

        switch ((data[6] & 0xff)) {
            case 1:
                airData[9] = (byte) (0x1);
                break;
            case 2:
                // airData[1] = (byte) (0x20);
                break;
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 5:
                airData[1] = (byte) (0x60);
                break;
            case 6:
                airData[1] = (byte) (0x40);
                break;
            case 0xb:
                airData[1] = (byte) (0x80);
                break;
            case 0xc:
                airData[1] = (byte) (0xa0);
                break;
            case 0xd:
                airData[1] = (byte) (0xc0);
                break;
            case 0xe:
                airData[1] = (byte) (0xe0);
                break;
        }

        airData[1] |= (byte) (data[7] & 0x0f);

        airData[2] = data[8];
        airData[3] = (byte) 0xfa;

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {

    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }

    public void startConnect() {
        byte[] buf = new byte[]{0x3, (byte) 0x6a, 0x5, 1, (byte) 0xf0};

        sendDataToCanbox(buf, buf.length);
        udpateLang();
    }

    public void udpateLang() {
        int lang = -1;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("zh")) {
                lang = 2;
            } else {
                lang = 1;
            }

        }
        if (lang != -1) {
            byte[] buf = {0x2, (byte) 0x9a, 0x1, (byte) lang};
            sendDataToCanbox(buf, buf.length);
        }
    }
}
