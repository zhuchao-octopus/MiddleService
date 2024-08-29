package com.zhuchao.android.car.cartype.hiworld;

import android.provider.Settings;

import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;

public class DongFeng008Hiworld extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x48, 0x13, 0x32, 0x61, 0x1a, (byte) 0xed};
    private final static byte[][] KEYS_WHEEL2 = {{0x1, MyCmd.Keycode.POWER}, {0x10, MyCmd.Keycode.PLAY_PAUSE}, {0x2b, MyCmd.Keycode.HOME}, {0x2c, MyCmd.Keycode.MODLE}, {0x2f, MyCmd.Keycode.MENU}, {0x39, MyCmd.Keycode.KEY_DISPLAY},};
    private final static byte[][] KEYS_WHEEL3 = {{0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0},};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH}, {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG},


            {0x8, MyCmd.Keycode.PREVIOUS}, {0x9, MyCmd.Keycode.NEXT},

            {0xc, MyCmd.Keycode.MODLE},};

    public DongFeng008Hiworld() {
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
        buildCmdAngle((byte) 0x1a, (byte) 0x0, 540);
        // buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);

        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);


        buildCmdKey((byte) 0x21, (byte) 0, (byte) 2, (byte) 0, KEYS_WHEEL2);

        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;


        buildCmdRepeatSendCarType(getCarTypeCmd());
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x0, 0x26};
        switch (CarUtil.getModelId()) {
            case 9:
                cmd[2] = 0xa;
                break;
            case 46:
                cmd[2] = 0xb;
                break;
            case 47:
                if (CarUtil.getCarTypeConfig() == 2) {
                    cmd[2] = 0xd;
                } else {
                    cmd[2] = 0xc;
                }
                break;
            case 48:
                cmd[2] = 0xe;
                break;
            default:
                cmd = null;
                break;
        }
        return cmd;
    }

    @Override
    public int getAngleValue2(byte[] data) {
        // TODO Auto-generated method stub

        short angle = (short) ((data[9] & 0xff) | (((data[8] & 0xff)) << 8));

        return -angle;
    }

    @Override
    public void stopConnect() {

    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[10];

        airData[0] = (byte) (((data[2] & 0x40) << 1) | ((data[2] & 0x01) << 6) | ((data[3] & 0x10) << 1) | ((data[3] & 0x08) << 0) | ((data[4] & 0x10) >> 3));


        switch ((data[6] & 0xff)) {
            case 1:
                airData[1] = (byte) (0x40);
                break;
            case 2:
                airData[1] = (byte) (0x60);
                break;
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 4:
                airData[1] = (byte) (0xa0);
                break;
        }

        airData[1] |= (byte) (data[7] & 0x0f);

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
    }

    public int getUpdateTime() {
        return 1000;
    }

    public void updateTime() {
        if (mContext == null) {
            return;
        }
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);


        if ("12".equals(strTimeFormat)) {
        } else {
            ampm = 1;
        }

        byte format = 2;

        String date_foramt = SettingProperties.getProperty(mContext, SettingProperties.KEY_DATE_FORMAT);
        if (date_foramt != null) {
            if ("dd/MM/yyyy".equals(date_foramt)) {
                format = 1;
            } else if ("MM/dd/yyyy".equals(date_foramt)) {
                format = 3;
            }
        }


        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{0x0a, (byte) 0xcb, 0, h, m, s, 0, ampm, y, mon, d, format};

        sendDataToCanbox(buf, buf.length);

    }
}
