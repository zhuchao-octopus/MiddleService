package com.zhuchao.android.car.cartype.raise;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;

public class DongFengJingYiX5Raise extends Canbox {

    public DongFengJingYiX5Raise() {
        buildCmdRepeatSendCarType(getCarTypeCmd());
        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        //		buildCmdRadarFront((byte) 0x26, (byte) 0x0, (byte) 0xa);
        //		buildCmdRadarBack((byte) 0x25, (byte) 0x0, (byte) 0xa);
        buildCmdAngle((byte) 0x30, (byte) 0x0, 0x2198);
        buildCmdOutTemp((byte) 0x36, (byte) 0x1);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x23;
        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x22;
        MAP_KEYS2 = KEYS_WHEEL2;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;

        setVoiceSupportRaise();
        //		mRadarBack2 = buildCmdRadar((byte) 0x24, (byte) 0x0, (byte) 255,
        //				(byte) 2);
    }

    //	private int mRadarBack2;
    private final static byte[] IDS_TO_CANBOXSETTING = {(byte) 0x40};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.PREVIOUS}, {0x4, MyCmd.Keycode.NEXT}, {0x9, MyCmd.Keycode.BT}, {0x8, MyCmd.Keycode.SPEECH},
    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.EASY_CONNECT}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.BT}, {0x5, MyCmd.Keycode.KEY_DISPLAY}, {0x6, MyCmd.Keycode.MENU},
            {0x7, MyCmd.Keycode.BACK}, {0x8, MyCmd.Keycode.RADIO}, {0x9, MyCmd.Keycode.AUDIO}, {0xa, MyCmd.Keycode.PLAY}, {0xb, MyCmd.Keycode.ROLL_PREV}, {0xc, MyCmd.Keycode.ROLL_NEXT},
            {0xd, MyCmd.Keycode.EQ}, {0xe, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0xf, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x10, MyCmd.Keycode.SETUP}, {0x11, MyCmd.Keycode.PREVIOUS},
            {0x12, MyCmd.Keycode.NEXT}, {0x13, MyCmd.Keycode.NAVIGATION}, {0x14, MyCmd.Keycode.BT}, {0x15, MyCmd.Keycode.BT_DIAL}, {0x16, MyCmd.Keycode.AUDIO}, {0x17, MyCmd.Keycode.AS},
            {0x18, MyCmd.Keycode.TIME_SETTING}, {0x19, MyCmd.Keycode.EQ}, {0x1a, MyCmd.Keycode.PLAY_PAUSE}, {0x1b, MyCmd.Keycode.KEY_RADIO_SCAN}, {0x1c, MyCmd.Keycode.KEY_REPEAT},
            {0x1d, MyCmd.Keycode.KEY_SHUFFLE}, {0x1e, MyCmd.Keycode.KEY_TURN_D}, {0x1f, MyCmd.Keycode.KEY_TURN_A},

    };

    private byte[] getCarTypeCmd() {
        if (CarUtil.getCatelId() == 25) {
            byte[] cmd = new byte[]{(byte) 0xee, 0x01, 0};
            switch (CarUtil.getModelId()) {
                case 40:
                    cmd[2] = 1;
                    break;
                case 41:
                    cmd[2] = 2;
                    break;
                case 42:
                    cmd[2] = 3;
                    break;
                default:
                    return null;
            }
            return cmd;
        }
        return null;
    }

    @Override
    public int getAngleValue(byte[] data) {

        int angle = ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));

        int max = (0x2ad0 - 0x1568);
        if (CarUtil.getCatelId() == 25 && CarUtil.getModelId() == 10) {
            max = (0x35f6 - 0x1e8a);
            angle = angle - 0x1e8a;
        } else {
            angle = angle - 0x1568;
        }

        angle = ((angle * 3000) / max);
        if (angle > -50 && angle < 50) {
            angle = 50;
        }

        return angle;

    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x0) {
            data = (byte) 0;
        } else if ((data & 0xff) == 0xff) {
            data = (byte) 0xff;
        } else if ((data & 0xff) >= 0x12 && (data & 0xff) <= 0x20) {
            data = (byte) (36 + ((data & 0xff) - 0x12) * 2);
        } else {
            data = (byte) 0xfa;
        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xe8) | ((data[2] & 0x02) >> 1) | ((data[2] & 0x01) << 1));

        switch ((data[3] & 0xff)) {
            case 0:
                airData[1] = (byte) (0x40);
                break;
            case 1:
                airData[1] = (byte) (0x60);
                break;
            case 2:
                airData[1] = (byte) (0x20);
                break;
            case 3:
                airData[1] = (byte) (0xa0);
                break;
            case 4:
                airData[1] = (byte) (0x80);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[4] & 0x0f);

        airData[2] = data[5];
        airData[3] = data[6];
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    @Override
    public void setVolume(int volume) {

        if (volume == 0) {
            mLcdInfo[9] = 2;
        } else {
            mLcdInfo[9] = 1;
        }
        sendDataToCanbox(mLcdInfo, mLcdInfo.length);

    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] >= 0x10) {
            b[0] = 3;
        } else {
            b[0] = 1;
        }

        int freq = (((b[2] & 0xff) << 8) | (b[1] & 0xff)) / 10;

        mLcdInfo[2] = 1;
        mLcdInfo[3] = b[0];
        mLcdInfo[4] = (byte) ((freq & 0xff00) >> 8);
        mLcdInfo[5] = (byte) ((freq & 0xff) >> 0);

        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }

    byte[] mLcdInfo = new byte[]{(byte) 0x75, 0x8, 0, 0, 0, 0, 0, 0, 0, 0};

    public void setMediaSrc(int source) {// default is simple box
        byte s = 0;
        switch (source) {
            case 0:
                return;
            case 1:
                s = 2;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x04;
            case MyCmd.SOURCE_AUX:
                s = 0x05;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x06;
                break;
            default:
                s = 0x00;
                break;
        }
        mLcdInfo[2] = s;
        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }

    //	@Override
    //	public void parseCanboxData(byte[] data, int len) {
    //		// TODO Auto-generated method stub
    //		switch (data[0]) {
    //		case 0x24:
    //			parseRadarBack(mRadarBack2, data);
    //			break;
    //		default:
    //			super.parseCanboxData(data, len);
    //
    //		}
    //	}


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
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        if ("12".equals(strTimeFormat)) {
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
        }


        byte m = (byte) ((curDate.getMinutes() & 0xff));
        byte s = (byte) ((curDate.getSeconds() & 0xff));

        h = (byte) ((h & 0xff));

        byte[] buf = new byte[]{(byte) 0x76, 0x03, h, m, s,};
        sendDataToCanbox(buf, buf.length);
    }


}
