package com.zhuchao.android.car.cartype.ods;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Calendar;


public class SkyworthET5 extends Canbox {

    public SkyworthET5() {
        mIdAC = 0x28;
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
        buildCmdRadarBack((byte) 0x1e, (byte) 0x0, (byte) 4);
        buildCmdRadarFront((byte) 0x1d, (byte) 0x0, (byte) 4);
        buildCmdAngle((byte) 0x29, (byte) 0x3, 7799);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;


        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x27};


    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x3, MyCmd.Keycode.PREVIOUS}, {0x4, MyCmd.Keycode.NEXT},


            {0x5, MyCmd.Keycode.MUTE}, {0x6, MyCmd.Keycode.SPEECH},

            {0x7, KEY_SOURCE}, {0x8, MyCmd.Keycode.BT},
    };


    public int getAngleValue2(byte[] data) {

        int angle;

        angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));


        return angle;
    }


    @Override
    public int getACTemp(byte data) {
        if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0) {
        } else {
            data = (byte) (36 + (data - 0x1));

        }
        return data & 0xff;
    }


    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[12];
        airData[0] = (byte) (((data[2] & 0xef)));
        airData[1] = (byte) ((data[3] & 0xff));
        airData[2] = data[4];
        airData[3] = data[5];


        airData[10] = data[7];
        airData[11] = data[8];

        setACData(airData, data, 2, 4, MASK_AC_MAX);

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {//default is simple box

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

}
