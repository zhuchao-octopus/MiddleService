package com.zhuchao.android.car.cartype.hiworld;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;

public class BMW001Hiworld extends Canbox {

    public BMW001Hiworld() {
        buildCmdDoor((byte) 0x73, (byte) 0x3, (byte) 0xfc, (byte) 0x09);
        buildCmdAngle((byte) 0x72, (byte) 0x0, 140);
        buildCmdRadarBack((byte) 0x72, (byte) 0x0, (byte) 0xfe);
        buildCmdRadarFront((byte) 0x72, (byte) 0x0, (byte) 0xfe);
        buildCmdRadarFrontEx((byte) 10);
        buildCmdRadarBackEx((byte) 6);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);

        buildCmdKey((byte) 0x72, (byte) 1, (byte) 4, (byte) 0, KEYS_WHEEL);

    }


    @Override
    public void stopConnect() {

    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE},

            {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG},

            {0x9, MyCmd.Keycode.PREVIOUS}, {0x8, MyCmd.Keycode.NEXT},

            {0xa, MyCmd.Keycode.MODLE},

    };

    @Override
    public int getAngleValue2(byte[] data) {
        int angle = data[6] & 0xff;
        if (angle == 0) {
            angle = -(data[7] & 0xff);
        }

        return angle;
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {

    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword2(data, len);
    }

    public void startConnect() {
    }


    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {
        if (mContext == null) {
            return;
        }
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);


        byte m = (byte) curDate.getMinutes();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{
                0x0a, (byte) 0xcb, 0, h, m, 0, 0, 0, y, mon, d, 0
        };

        sendDataToCanbox(buf, buf.length);

    }
}
