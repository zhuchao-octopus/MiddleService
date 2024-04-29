package com.zhuchao.android.car.cartype.xinbasi;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;

public class HoldenXinbasi extends Canbox {

    public HoldenXinbasi() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
        buildCmdDoor((byte) 0x3, (byte) 0x1, (byte) 0xf8, (byte) 0x02);
        //		buildCmdRadarFront((byte) 0xf, (byte) 0x0, (byte) 0xa);
        //		buildCmdRadarBack((byte) 0xf, (byte) 0x0, (byte) 0xa);
        buildCmdAngle((byte) 0x10, (byte) 0x4, 0x20);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x3;
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x5};


    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0xf2, 0x01, 0};

        switch (CarUtil.getModelId()) {
            case 0:
                cmd[2] = 1;
                break;
            case 1:
                cmd[2] = 2;
                break;
            default:
                return null;
        }
        return cmd;
    }


    private static final byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x5, MyCmd.Keycode.MODLE},
            {0x6, MyCmd.Keycode.MULT_MUTE_AND_HANG}, {0x7, MyCmd.Keycode.BT_DIAL},

    };

    @Override
    public int getAngleValue2(byte[] data) {
        int angle = data[2] & 0xff;

        if (angle >= 0x80) {
            angle = 0x80 - angle;
        }

        return angle;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0xff) {

        } else if ((data & 0xff) == 0x0) {

        } else if ((data & 0xff) >= 0x1e || (data & 0xff) <= 0x40) {

        } else {
            data = (byte) 0xfa;
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[10];

        airData[0] = (byte) ((data[2] & 0xe0) | ((data[8] & 0x80) >> 3) | ((data[8] & 0x20) >> 5) | ((data[8] & 0x10) >> 2));

        switch ((data[3] & 0xff)) {
            case 1:
                airData[9] = (byte) (0x1);
                break;
            case 2:
                airData[0] |= (byte) (0x02);
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
                airData[1] = (byte) (0xc0);
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
            case 0xa:
                airData[1] = (byte) (0x20);
                airData[0] |= (byte) (0x02);
                break;
            case 0xb:
                airData[1] = (byte) (0x60);
                airData[0] |= (byte) (0x02);
                break;
        }


        airData[1] |= (data[2] & 0xf);


        airData[2] = data[6];
        airData[3] = data[7];

        airData[5] |= 0x80;


        super.parseACInfo(airData);
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0xf) {
            mRadar[0] = radarChangeStyle(data[2], 0xa0, 1);
            mRadar[1] = radarChangeStyle(data[2], 0xa0, 1);
            mRadar[2] = radarChangeStyle(data[2], 0xa0, 1);
            mRadar[3] = radarChangeStyle(data[2], 0xa0, 1);
            mRadar[4] = radarChangeStyle(data[5], 0xa0, 1);
            mRadar[5] = radarChangeStyle(data[5], 0xa0, 1);
            mRadar[6] = radarChangeStyle(data[5], 0xa0, 1);
            mRadar[7] = radarChangeStyle(data[5], 0xa0, 1);

            parseRadar();
        } else {
            super.parseCanboxData(data, len);
        }
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte[] mData = new byte[]{
                (byte) 0x88, 0x5, 8, (byte) ((play & 0xff00) >> 8), (byte) ((play & 0xff)), (byte) ((total & 0xff00) >> 8), (byte) ((total & 0xff))
        };
        sendDataToCanbox(mData, mData.length);

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

        if (b[0] < 0x10) {
            b[0] = 0;
        } else {
            b[0] = 3;
        }
        byte[] mData = new byte[]{(byte) 0x88, 0x5, 1, 0, b[0], b[2], b[1]};
        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source) {

        byte s = 0x8;
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                return;

            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 8;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                break;
            case MyCmd.SOURCE_DVD:
                s = 0x02;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0a;
                break;
            case MyCmd.SOURCE_AV_OFF:
                s = 0x0;
                break;
        }

        byte[] mData = new byte[]{(byte) 0x88, 0x5, s, 0, 0, 0, 0};

        sendDataToCanbox(mData, mData.length);

    }

    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);


        byte m = (byte) curDate.getMinutes();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{
                (byte) 0x89, 0x05, y, mon, d, h, m
        };

        sendDataToCanbox(buf, buf.length);
    }
}
