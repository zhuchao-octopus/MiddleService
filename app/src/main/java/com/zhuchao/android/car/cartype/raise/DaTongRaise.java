package com.zhuchao.android.car.cartype.raise;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;


public class DaTongRaise extends Canbox {

    public DaTongRaise() {
        buildCmdRepeatSendCarType(getCarTypeCmd());
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarFront((byte) 0x26, (byte) 0x1, (byte) 0x6);
        buildCmdRadarBack((byte) 0x27, (byte) 0x1, (byte) 0x6);
        buildCmdAngle((byte) 0x30, (byte) 0x3, 0x1900);
        buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdOutTemp((byte) 0x36, (byte) 0x1);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x23;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x51, 0x25};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x6, MyCmd.Keycode.MUTE},


            {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.BT}, {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG}, {0x10, MyCmd.Keycode.SPEECH}, {0x11, MyCmd.Keycode.NUMBER_STAR},
            {0x12, MyCmd.Keycode.MULT_MUTE_AND_HANG}, {0x13, MyCmd.Keycode.BT},

            {0x20, MyCmd.Keycode.HOME},
            //		{ 0x21, MyCmd.Keycode.c },
            {0x22, MyCmd.Keycode.AUDIO}, {0x23, MyCmd.Keycode.AUDIO}, {0x24, MyCmd.Keycode.AUDIO}, {0x25, MyCmd.Keycode.AUX_IN}, {0x26, MyCmd.Keycode.RADIO}, {0x27, MyCmd.Keycode.BACK},
            {0x28, MyCmd.Keycode.MENU}, {0x29, MyCmd.Keycode.SETUP}, {0x2a, MyCmd.Keycode.PLAY_PAUSE}, {0x2b, MyCmd.Keycode.PREVIOUS}, {0x2c, MyCmd.Keycode.NEXT}, {0x2d, MyCmd.Keycode.AUDIO},
            {0x2e, MyCmd.Keycode.ROLL_NEXT}, {0x2f, MyCmd.Keycode.ROLL_PREV}, {(byte) 0x80, MyCmd.Keycode.POWER},
    };

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0x22, 0x02, 0x52, 0};
        switch (CarUtil.getModelId()) {
            case 26:
                cmd[3] = 0;
                break;
            case 25:
                cmd[3] = 1;
                break;
            case 24:
                cmd[3] = 2;
                break;
            case 38:
                cmd[3] = 3;
                break;
            case 39:
                cmd[3] = 4;
                break;
            default:
                return null;
        }
        return cmd;
    }
    //	@Override
    //	public int getAngleValue(byte[] data) {
    //
    //		int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));
    //
    //
    //		return angle;
    //
    //
    //	}

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {
            data = (byte) 0;
        } else if ((data & 0xff) == 0x12) {
            data = (byte) 0xff;
        } else {
            data = (byte) (36 + (((data & 0xff) - 3) * 2));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[10];

        airData[0] = (byte) ((data[2] & 0xe8) | ((data[2] & 0x1) << 1) | ((data[2] & 0x02) >> 1));

        switch ((data[3] & 0xff)) {
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
            case 5:
                airData[1] = (byte) (0x80);
                break;
            case 6:
                airData[9] = (byte) (0x1);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);


        airData[2] = (byte) (data[5] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        //		if (airData[1] == 0) {
        //			Util.zeroBuf(airData);
        //		}
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
        if (mContext == null) {
            return;
        }
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        if ("12".equals(strTimeFormat)) {
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
        }

        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();

        byte[] buf = new byte[]{(byte) 0xa6, 0x06, y, mon, d, h, m, s};
        sendDataToCanbox(buf, buf.length);
    }

}
