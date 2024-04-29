package com.zhuchao.android.car.cartype.hiworld;

import android.content.Context;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;


public class ChangChengH2Hiworld extends Canbox {

    public ChangChengH2Hiworld() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
        //		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
        //		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 2, KEYS_WHEEL2);

        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    public void startConnect() {

    }

    @Override
    public void stopConnect() {

    }

    private final static byte[] IDS_TO_CANBOXSETTING = {
            (byte) 0x32, (byte) 0xf2
    };

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN},

            {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG},

            {0xc, MyCmd.Keycode.MODLE}, {0xd, MyCmd.Keycode.NEXT}, {0xe, MyCmd.Keycode.PREVIOUS},

    };

    private final byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.PREVIOUS}, {0x3, MyCmd.Keycode.NEXT},


            {0x2c, MyCmd.Keycode.MODLE}, {0x3f, MyCmd.Keycode.HOME}, {0x43, MyCmd.Keycode.MUTE}, {0x47, MyCmd.Keycode.BT_DIAL}, {0x48, MyCmd.Keycode.BT_HANG},

            {0x49, MyCmd.Keycode.RADIO}, {0x4a, MyCmd.Keycode.SETUP}, {0x4b, MyCmd.Keycode.NAVIGATION},
    };
    private final static byte[][] KEYS_WHEEL3 = {
            {0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0},
    };

    private byte[] getCarTypeCmd() {
        if (CarUtil.getModelId() == 23) {
            KEYS_WHEEL2[10][2] = MyCmd.Keycode.BT;
        }
        return null;
    }

    @Override
    public int getAngleValue(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));


        return angle;


    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0xff) {

        } else if ((data & 0xff) == 0xfe) {
            data = 0;
        } else if (data >= 1 && data <= 7) {
            //data = (byte) ((data & 0xff) * 2);
        } else {
            data = (byte) ((data & 0xff) * 2);
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[10];

        airData[0] = (byte) ((data[2] & 0x04) | ((data[2] & 0x40) << 1) | ((data[2] & 0x01) << 6)
                //				| ((data[3] & 0x10) << 1)
                | ((data[3] & 0x08) << 1) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));

        if (((data[3] & 0x10) == 0)) {
            airData[0] |= 0x20;
        }

        airData[4] = (byte) (((data[2] & 0x20) >> 3));

        switch ((data[6] & 0xff)) {
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 2:
            case 5:
                airData[1] = (byte) (0x60);
                break;
            case 1:
            case 6:
                airData[1] = (byte) (0x40);
                break;
            case 4:
                airData[1] = (byte) (0xa0);
                break;
            case 7:
                airData[1] = (byte) (0xc0);
                break;
            case 8:
                airData[1] = (byte) (0xe0);
                break;
        }

        airData[1] |= (byte) (data[7] & 0x0f);


        airData[2] = data[8];
        if (data[8] >= 1 && data[8] <= 7) {
            airData[7] = 0x40;
            airData[3] = data[8];
        } else {
            airData[3] = data[9];
        }

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void parseCanboxData(byte[] data, int len) {
        //		case 0x22:
        //			if (data[3] == 0) {
        //				return;
        //			} else if (data[3] < 0) {
        //				data[3] = (byte) (-data[3]);
        //				data[2] += 0x10;
        //			}
        //			parseWheelKey(mIdKey3, data, MAP_KEYS3);
        //			break;
        super.parseCanboxData(data, len);
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

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
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
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);


        if ("12".equals(strTimeFormat)) {
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
            h |= 0x80;
        } else {
            ampm = 1;
        }

        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        byte[] buf = new byte[]{0x03, (byte) 0xb5, h, m, s};

        sendDataToCanbox(buf, buf.length);

    }
}
