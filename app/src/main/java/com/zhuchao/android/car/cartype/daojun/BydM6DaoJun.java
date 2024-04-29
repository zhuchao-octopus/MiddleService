package com.zhuchao.android.car.cartype.daojun;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Calendar;


public class BydM6DaoJun extends Canbox {

    public BydM6DaoJun() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

        buildCmdVersion((byte) 0x30, (byte) 0x0);

        buildCmdAngle((byte) 0x26, (byte) 0x0, 6446);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x4);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x4);

        mIdAC = 0x3;
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x6};

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0x85, 0x02, 1, 1};
        return cmd;
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.PREVIOUS}, {0x4, MyCmd.Keycode.NEXT}, {0x5, MyCmd.Keycode.MODLE}, {0x24, MyCmd.Keycode.BT_DIAL},
            {0x26, MyCmd.Keycode.HOME}, {0x27, MyCmd.Keycode.KEY_360}, {0x28, MyCmd.Keycode.KEY_360}, {0x29, MyCmd.Keycode.KEY_360}, {0x30, MyCmd.Keycode.KEY_360}, {0x31, MyCmd.Keycode.KEY_360},

    };

    @Override
    public int getAngleValue2(byte[] data) {
        // TODO Auto-generated method stub
        int angle = (data[2] & 0xff) | ((data[3] & 0xff) << 8);
        angle = angle - 7575;
        return angle;

    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if (((data & 0xff)) == 0) {

        } else if (((data & 0xff)) == 0x1e) {
            data = (byte) 0xff;
        } else if (((data & 0xff)) == 0xff) {
            data = (byte) 0xfa;
        } else {
            data = (byte) (34 + (data - 0x1));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[10];

        switch ((data[3] & 0xf) >> 0) {
            case 1:
                airData[9] = 1;
                break;
            case 2:
                airData[1] = (byte) (0x80);
                break;
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 4:
                airData[1] = (byte) (0x60);
                break;
            case 5:
                airData[1] = (byte) (0x40);
                break;
            case 6:
                airData[1] = (byte) (0xa0);
                break;
            case 7:
                airData[1] = (byte) (0x80);
                break;
            case 8:
                airData[1] = (byte) (0xa0);
                break;
            case 9:
                airData[1] = (byte) (0xe0);
                break;
            default:
                airData[1] = 0;
                break;
        }


        airData[1] |= (byte) ((data[2] & 0x07) | (data[3] & 0x10) >> 1);


        airData[0] = (byte) (((data[2] & 0x10) >> 4) | ((data[2] & 0xe0) << 0) | ((data[3] & 0x20) >> 3) | ((data[8] & 0x04) << 2));


        airData[0] |= 0x80;

        airData[2] = data[4];
        airData[3] = data[5];


        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {
        Calendar c = Calendar.getInstance();

        byte h = (byte) c.get(Calendar.HOUR_OF_DAY);
        h = fixTimeHour(h);

        byte m = (byte) c.get(Calendar.MINUTE);
        byte s = (byte) c.get(Calendar.SECOND);

        byte y = (byte) (c.get(Calendar.YEAR) - 2000);
        byte mon = (byte) (c.get(Calendar.MONTH) + 1);
        byte d = (byte) c.get(Calendar.DAY_OF_MONTH);

        byte w = (byte) (c.get(Calendar.DAY_OF_WEEK) - 1);

        byte[] buf = new byte[]{(byte) 0x84, 7, y, mon, d, h, m, s, w};

        sendDataToCanbox(buf, buf.length);


    }


}
