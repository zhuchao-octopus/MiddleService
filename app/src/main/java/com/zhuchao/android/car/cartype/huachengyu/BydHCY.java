package com.zhuchao.android.car.cartype.huachengyu;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Calendar;


public class BydHCY extends Canbox {

    public BydHCY() {

        buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarFront((byte) 0x1d, (byte) 0x0, (byte) 0x4);
        buildCmdRadarBack((byte) 0x1e, (byte) 0x0, (byte) 0x4);
        buildCmdAngle((byte) 0x29, (byte) 0x0, 0x1e77);
        buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        //		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        //		buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x28;
        //		mIdKey = 0x20;

        buildCmdKey((byte) 0x20, (byte) 5, (byte) 2, (byte) 0, KEYS_WHEEL);

        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {
            0x7, 0x14, 0x17, 0x19, 0x26, 0x27, 0x43, 0x32, 0x33, 0x34, 0x35
    };

    private byte[] getCarTypeCmd() {
        byte[] cmd = null;
        //		if (CarUtil.getCatelId() == 14) {
        cmd = new byte[]{(byte) 0x26, 0x01, 0};
        switch (CarUtil.getModelId()) {
            case 1:
                cmd[2] = 1;
                break;
            case 11:
                cmd[2] = 8;
                break;
            case 13:
                cmd[2] = 2;
                break;
            case 14:
                cmd[2] = 3;
                break;
            case 15:
                cmd[2] = 4;
                break;
            case 16:
                cmd[2] = 5;
                break;
            case 17:
                cmd[2] = 6;
                break;
            case 18:
                cmd[2] = 7;
                break;
        }
        //		}
        return cmd;
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x4, MyCmd.Keycode.VOLUME_DOWN}, {0x7, MyCmd.Keycode.PREVIOUS}, {0x8, MyCmd.Keycode.KEY_SEEK_PREV}, {0xa, MyCmd.Keycode.NEXT},
            {0xb, MyCmd.Keycode.KEY_SEEK_NEXT}, {0xc, MyCmd.Keycode.BT}, {0xd, MyCmd.Keycode.BT}, {0xe, MyCmd.Keycode.EQ}, {0xf, MyCmd.Keycode.EQ}, {0x10, MyCmd.Keycode.MODLE},
            {0x11, MyCmd.Keycode.MODLE}, {0x13, MyCmd.Keycode.SPEECH}, {0x14, MyCmd.Keycode.SPEECH},


            {0x20, MyCmd.Keycode.MUTE}, {0x30, MyCmd.Keycode.POWER}, {0x31, MyCmd.Keycode.BACK}, {0x35, MyCmd.Keycode.MUTE}, {0x36, MyCmd.Keycode.HOME},


    };


    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {
        } else if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else if (((data & 0xff) >= 0x1) && ((data & 0xff) <= 0x1d)) {
            data = (byte) (35 + (data & 0xff));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[12];

        airData[0] = (byte) (data[2] & 0xef);

        airData[1] = data[3];

        airData[4] = (byte) (((data[2] & 0x10) >> 2) | ((data[6] & 0x04) << 1));


        airData[2] = data[4];
        airData[3] = data[5];

        airData[9] = (byte) (((data[9] & 0x80)));

        airData[10] = (byte) getACTemp(data[8]);

        airData[11] = (byte) (((data[9] & 0x7f)));

        airData[5] |= 0x80;


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

        int w = c.get(Calendar.DAY_OF_WEEK);
        byte[] buf = new byte[]{(byte) 0x76, 0x07, y, mon, d, h, m, s, 0};

        buf[8] = (byte) (0x80 | w);
        sendDataToCanbox(buf, buf.length);
        Util.doSleep(50);

        buf[8] = (byte) (0x40 | w);
        sendDataToCanbox(buf, buf.length);

    }

    public boolean isSupportCompass() {
        return true;
    }

    public void updateCompass(int compass, double altitude) {

        byte[] buf = new byte[]{
                (byte) (0xca), 4, 0, 0, 0, 0
        };

        int a = (int) (altitude * 10);
        int altitudePoint = a % 10;
        a = a / 10;

        buf[2] = (byte) (compass & 0xff);
        buf[3] = (byte) (((compass & 0xf00) >> 8) | (((altitudePoint) & 0xf) << 4));

        buf[4] = (byte) (a & 0xff);
        buf[5] = (byte) ((a & 0xf00) >> 8);

        buf[5] |= 0xc0;

        sendDataToCanbox(buf, buf.length);
    }

}
