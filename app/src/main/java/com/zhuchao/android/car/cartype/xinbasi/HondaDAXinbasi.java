package com.zhuchao.android.car.cartype.xinbasi;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;

public class HondaDAXinbasi extends Canbox {

    public HondaDAXinbasi() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
        buildCmdDoor((byte) 0x3, (byte) 0x1, (byte) 0xf8, (byte) 0x02);
        //		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
        //		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
        buildCmdAngle((byte) 0x6, (byte) 0x4, 0x1400);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x5};


    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0x82, 0x02, 0, 0};

        if (CarUtil.getRightCameraExist()) {
            cmd[3] = 1;
        }

        switch (CarUtil.getModelId()) {
            case 14: //key
                KEYS_WHEEL[11][1] = MyCmd.Keycode.MUTE;
                break;
            case 5:
            case 13:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 1;
                }
                break;
        }
        return cmd;
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0x9) {
            mRadar[0] = radarChangeStyle(data[3], 4, 1);
            mRadar[1] = radarChangeStyle(data[4], 4, 1);
            mRadar[2] = radarChangeStyle(data[4], 4, 1);
            mRadar[3] = radarChangeStyle(data[5], 4, 1);
            mRadar[4] = radarChangeStyle(data[6], 4, 1);
            mRadar[5] = radarChangeStyle(data[7], 4, 1);
            mRadar[6] = radarChangeStyle(data[7], 4, 1);
            mRadar[7] = radarChangeStyle(data[8], 4, 1);
            parseRadar();
        } else {
            super.parseCanboxData(data, len);
        }
    }

    private static final byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.SPEECH},
            {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.MULT_BACK_AND_HANG},


            {0xb, MyCmd.Keycode.SPEECH}, {0xc, MyCmd.Keycode.BT_DIAL}, {0xd, MyCmd.Keycode.MULT_BACK_AND_HANG},

            {0x17, MyCmd.Keycode.HOME}, {0x18, MyCmd.Keycode.MODLE},

            {0x19, MyCmd.Keycode.MUTE},
    };

    //	@Override
    //	public int getAngleValue(byte[] data) {
    //
    //		int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));
    //
    //		return angle;
    //	}

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte[] mData = new byte[]{
                (byte) 0x82, 0x4, 4, 0, 0, 0
        };
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
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        if ("12".equals(strTimeFormat)) {
            h |= 0x80;
        }

        byte m = (byte) curDate.getMinutes();

        byte[] buf = new byte[]{(byte) 0x85, 0x07, 0, 0, 0, h, m, 0, 0};

        sendDataToCanbox(buf, buf.length);
    }
}
