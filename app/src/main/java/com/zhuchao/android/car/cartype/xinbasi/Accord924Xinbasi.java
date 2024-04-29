package com.zhuchao.android.car.cartype.xinbasi;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;

public class Accord924Xinbasi extends Canbox {

    public Accord924Xinbasi() {
        buildCmdDoor((byte) 0x2, (byte) 0x2, (byte) 0xfc, (byte) 0x02);
        //		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
        //		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
        buildCmdAngle((byte) 0x5, (byte) 0x0, 0x1200);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0xc;
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {
            0x3, 0x4, 0x7, 0x8, 0xa, 0xb
    };

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x5, MyCmd.Keycode.MODLE}, {0x6, MyCmd.Keycode.SPEECH},
            {0x7, MyCmd.Keycode.BT_DIAL}, {0x8, MyCmd.Keycode.BT_HANG},

            {0x17, MyCmd.Keycode.HOME}, {0x18, MyCmd.Keycode.MODLE},


            {0x18, MyCmd.Keycode.KEY_SIDE_CAMERA},
    };

    @Override
    public int getAngleValue(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

        return angle;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0x7f) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0x0) {
            data = 0;
        } else if ((data & 0xff) > 0x3f || (data & 0xff) < 0x1f) {
            data = (byte) 0xfa;
        } else {

        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xed) | ((data[6] & 0x80) >> 6));


        airData[1] = data[3];


        airData[2] = data[4];
        airData[3] = data[5];

        airData[5] |= 0x80;
        airData[5] |= ((data[6] & 0x01));

        if ((data[3] & 0x10) == 0) {
            airData[0] &= ~0x80;
        }

        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte[] mData = new byte[]{
                (byte) 0x82, 0x4, 4, 0, 0, 0
        };
        sendDataToCanbox(mData, mData.length);

    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        super.startConnect();
        byte[] mData = new byte[]{(byte) 0xff, 0x1, (byte) 0x7f};
        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {

        //		setMediaSrc(0);
        //		if (b[0] < 0x10) {
        ////			if(b[0] == 3){
        //				b[0] = 1;
        ////			}
        //		} else {
        //			b[0] = 3;
        //		}
        //		byte [] mData = new byte[] { (byte) 0x83, 0x4, 1, b[0], b[2], b[1]};
        //		sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source) {

        byte s = 0x4;
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                s = 1;
                break;
            case MyCmd.SOURCE_IPOD:
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 4;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x03;
                break;
            case MyCmd.SOURCE_DVD:
                s = 0x02;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x06;
                break;
            case MyCmd.SOURCE_AV_OFF:
                s = 0x0;
                break;
        }

        byte[] mData = new byte[]{(byte) 0x82, 0x1, s};

        sendDataToCanbox(mData, mData.length);

    }

    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        byte format = 0;

        String date_foramt = SystemConfig.getProperty(mContext, SystemConfig.KEY_DATE_FORMAT);
        if (date_foramt != null) {
            if ("dd/MM/yyyy".equals(date_foramt)) {
                format = 0;
            } else if ("MM/dd/yyyy".equals(date_foramt)) {
                format = 2;
            }
        }

        if ("12".equals(strTimeFormat)) {
            ampm = 1;
        }

        byte m = (byte) curDate.getMinutes();

        //		Log.d("cccc", ""+curDate.getYear());
        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{
                (byte) 0x88, 0x06, y, mon, d, format, h, m
        };

        sendDataToCanbox(buf, buf.length);
    }
}
