package com.zhuchao.android.car.cartype.ods;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;


public class DongFengXinNengYuanOD extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x31};
    private final static byte[][] KEYS_WHEEL = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS},


            {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.MUTE},

            {0x7, KEY_SOURCE}, {0x8, MyCmd.Keycode.BT_HANG}, {0x9, KEY_SOURCE}, {0xa, MyCmd.Keycode.BT_HANG}, {0x14, MyCmd.Keycode.SPEECH},};

    public DongFengXinNengYuanOD() {
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xf8, (byte) 0x02);
        buildCmdAngle((byte) 0x29, (byte) 0x0, 0x1e77);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    @Override
    public int getAngleValue2(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));


        return angle;


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
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);

        int year = curDate.getYear() + 1900;

        byte m = (byte) curDate.getMinutes();

        byte y = (byte) (year & 0xff);
        byte mon = (byte) (((curDate.getMonth() + 1) << 4) | ((year & 0xf00) >> 8));
        byte d = (byte) curDate.getDate();

        byte[] buf = new byte[]{(byte) 0x82, 0x06, y, mon, d, h, m, 0};

        sendDataToCanbox(buf, buf.length);
    }
}
