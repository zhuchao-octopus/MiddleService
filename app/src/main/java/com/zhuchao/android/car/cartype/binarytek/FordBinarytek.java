package com.zhuchao.android.car.cartype.binarytek;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;


public class FordBinarytek extends Canbox {

    public FordBinarytek() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x1e);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x1e);
        buildCmdAngle((byte) 0x66, (byte) 0x0, 0x12a0);
        //		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        //		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x21;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {
            0x27, 0x24, 0x28, 0x2a, 0x50, 0x51, 0x52, 0x53, 0xd, 0x68, 0x69, 0x6a, 0x6c, 0x7a, 0x70, 0x71, 0x72, 0x78, 0x79
    };

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MULT_NEXT_AND_HANG}, {0x4, MyCmd.Keycode.MULT_PREV_AND_RECEIVE}, {0x5, MyCmd.Keycode.BT},
            {0x7, MyCmd.Keycode.MODLE}, {0xe, MyCmd.Keycode.PREVIOUS}, {0xf, MyCmd.Keycode.NEXT}, {0x10, MyCmd.Keycode.PREVIOUS}, {0x11, MyCmd.Keycode.NEXT}, {0x12, MyCmd.Keycode.PLAY_PAUSE},
            {0x18, MyCmd.Keycode.SPEECH}, {0x20, MyCmd.Keycode.NUMBER0}, {0x21, MyCmd.Keycode.NUMBER1}, {0x22, MyCmd.Keycode.NUMBER2}, {0x23, MyCmd.Keycode.NUMBER3}, {0x24, MyCmd.Keycode.NUMBER4},
            {0x25, MyCmd.Keycode.NUMBER5}, {0x26, MyCmd.Keycode.NUMBER6}, {0x27, MyCmd.Keycode.NUMBER7}, {0x28, MyCmd.Keycode.NUMBER8}, {0x29, MyCmd.Keycode.NUMBER9},
            {0x2a, MyCmd.Keycode.NUMBER_STAR}, {0x2b, MyCmd.Keycode.NUMBER_POUND}, {0x33, MyCmd.Keycode.AS}, {0x34, MyCmd.Keycode.RADIO}, {0x35, MyCmd.Keycode.DVD}, {0x36, MyCmd.Keycode.AUX_IN},
            {0x37, MyCmd.Keycode.HOME}, {0x38, MyCmd.Keycode.EQ}, {0x39, MyCmd.Keycode.BT}, {0x3d, MyCmd.Keycode.TIME_SETTING}, {0x3f, MyCmd.Keycode.POWER}, {0x48, MyCmd.Keycode.PLAY_PAUSE},
            {0x49, MyCmd.Keycode.PREVIOUS}, {0x4a, MyCmd.Keycode.NEXT}, {0x4b, MyCmd.Keycode.PREVIOUS}, {0x4c, MyCmd.Keycode.NEXT}, {0x52, MyCmd.Keycode.MULT_PREV_AND_RECEIVE},
            {0x53, MyCmd.Keycode.MULT_NEXT_AND_HANG}, {0x54, MyCmd.Keycode.EJECT}, {0x56, MyCmd.Keycode.KEYAMS_RPT}, {0x57, MyCmd.Keycode.SETUP}, {0x59, MyCmd.Keycode.EQ}, {0x5a, MyCmd.Keycode.MUTE},
            {0x5b, MyCmd.Keycode.KEY_DISPLAY}, {0x5c, MyCmd.Keycode.PREVIOUS}, {0x5d, MyCmd.Keycode.PREVIOUS}, {0x5e, MyCmd.Keycode.NEXT}, {0x5f, MyCmd.Keycode.NEXT},
            {(byte) 0xF0, MyCmd.Keycode.VOLUME_ROLL_UP}, {(byte) 0xF1, MyCmd.Keycode.VOLUME_ROLL_DOWN},
    };

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0x8b, 0x01, 0};
        if (CarUtil.getModelId() == 2) {
            if (CarUtil.getCarTypeConfig() == 2) {
                cmd[2] = 0;
            } else if (CarUtil.getCarTypeConfig() == 0) {
                cmd[2] = 1;
            } else {
                return null;
            }
        } else {
            return null;
        }
        return cmd;
    }


    @Override
    public int getACTemp(byte data, int unit) {
        if (unit == 0) {
            if ((data & 0xff) == 0) {
            } else if ((data & 0xff) == 0x7f) {
                data = (byte) 0xff;
            } else if ((data & 0xff) >= 0x1f && (data & 0xff) <= 0x3B) {
                //data = 0;
            } else {
                data = (byte) 0xfa;
            }

        } else {
            if ((data & 0xff) == 0x77) {
                data = 0;
            } else if ((data & 0xff) == 0xab) {
                data = (byte) 0xff;
            } else if ((data & 0xff) > 0x77 && (data & 0xff) < 0xAB) {
                data = (byte) (60 + (((data & 0xff) - 0x78) / 2));
            } else {
                data = (byte) 0xfa;
            }
        }

        return data;
    }


    byte[] airData = new byte[12];

    public void parseACInfo(byte[] data) {

        airData[0] = (byte) ((data[2] & 0xee) | ((data[2] & 0x10) >> 4));


        airData[1] = data[3];


        airData[2] = data[4];
        airData[3] = data[5];

        airData[4] &= ~0x4;
        airData[4] |= (byte) ((data[6] & 0x04));

        airData[5] = (byte) (((data[6] & 0x40) >> 6));

        airData[5] |= 0x80;

        super.parseACInfo(airData);
    }

    public void parseACInfoEx(byte[] data) {

        airData[4] &= ~0x3b;
        airData[4] |= (byte) (((data[2] & 0x03) << 4) | ((data[2] & 0x0c) >> 2) | ((data[4] & 0x80) >> 4));


        airData[7] = (byte) (((data[3] & 0x01) << 5));

        airData[8] = (byte) (((data[2] & 0x30) << 0) | ((data[2] & 0xc0) >> 4));


        airData[9] = (byte) (((data[4] & 0x40) << 1));

        airData[10] = (byte) (((data[5] & 0xff)));
        airData[11] = (byte) (((data[4] & 0x0f)));

        super.parseACInfo(airData);
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0x6b) {
            parseACInfoEx(data);
        }
        super.parseCanboxData(data, len);
    }

    private byte[] mData = new byte[]{
            (byte) 0xc0, 0x8, 0, 0, 0, 0, 0, 0, 0, 0
    };

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte h = (byte) ((time / 3600));
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        // ++play;

        byte s = 0;
        byte s2 = 0;
        switch (source) {
            case MyCmd.SOURCE_DVD:
                s = 0x2;
                s2 = 0x11;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                ++play;
                s = 8;
                s2 = 0x11;
                break;
            default:
                s = 0x07;
                s2 = 0x30;
                break;
        }


        mData = new byte[]{
                (byte) 0xc0, 0x8, s, s2, (byte) ((total) & 0xFF), (byte) ((total & 0xFF00) >> 8), (byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), min, sec
        };


        // if (mPhoneStatus < HFP_INFO_CALLED) {

        sendDataToCanbox(mData, mData.length);
        // }
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        //		if (b[0] != 0x10) {
        b[0] += 1;
        //		}
        mData = new byte[]{
                (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0, 0, 0
        };
        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source) {// default is simple box
        byte s = 0;
        byte mediaType = 0;
        switch (source) {
            case 1:
                s = 2;
                mediaType = 0x10;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 6;
                mediaType = 0x12;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
            case 0:
                return;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                mediaType = 0x30;
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                mediaType = 0x30;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0b;
                mediaType = 0x40;
                break;
            default:
                s = 0x00;
                mediaType = 0x0;
                break;
        }

        // if (s == 0xb || s == 0x7) {
        // data = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0,
        // 0 };
        // } else {
        mData = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0};
        // }

        sendDataToCanbox(mData, mData.length);
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
        byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();

        byte[] buf = new byte[]{(byte) 0x82, 0x06, y, mon, d, h, m, s};
        sendDataToCanbox(buf, buf.length);
    }
}
