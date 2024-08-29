package com.zhuchao.android.car.cartype.xinchi;

import android.provider.Settings;

import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;

public class Accord8XinChi extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x3, 0x4, 0x5, 0x7, 0x8, 0xa, 0xb, 0xe};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.SPEECH}, {0x9, MyCmd.Keycode.BT}, {0xa, MyCmd.Keycode.MULT_BACK_AND_HANG},

    };

    public Accord8XinChi() {
        buildCmdDoor((byte) 0x11, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        //		buildCmdRadarFront((byte) 0xc, (byte) 0x0, (byte) 0x4, (byte) 3);
        //		buildCmdRadarBack((byte) 0x9, (byte) 0x0, (byte) 0x4, (byte) 3);
        buildCmdAngle((byte) 0xf, (byte) 0x0, 0x1d0);
        buildCmdEQ((byte) 0x10, (byte) 0x0, 6);
        //		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x2;
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    //	@Override
    //	public int getAngleValue(byte[] data) {
    //
    //		int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));
    //
    //		return angle;
    //	}
    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0xff) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0x1) {
            data = 0;
        } else if ((data & 0xff) == 0) {
            data = (byte) 0xfa;
        } else {
            data = (byte) (data & 0xff);
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
        //		airData[5] |= ((data[6] & 0x01));


        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte[] mData = new byte[]{(byte) 0x88, 0x4, 4, 0, 0, 0};
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
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        byte format = 0;

        String date_foramt = SettingProperties.getProperty(mContext, SettingProperties.KEY_DATE_FORMAT);
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
        byte[] buf = new byte[]{(byte) 0x87, 0x06, y, mon, d, format, h, m};

        sendDataToCanbox(buf, buf.length);
    }

    public void setVolume(int volume) {

        byte[] data = new byte[]{(byte) 0x85, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (21 << 8) | 21;
        }
        return ret;
    }

    public void parseEQ(int id, byte[] buf) {

        if (mEQData == null) {
            mEQData = new byte[6];
        }

        switch ((buf[2] & 0xe0) >> 5) {
            case 2:
                mEQData[0] = (byte) (buf[2] & 0x1f);
                break;
            case 1:
                mEQData[1] = (byte) (buf[2] & 0x1f);
                break;
            case 5:
                mEQData[2] = (byte) (buf[2] & 0x1f);
                break;
            case 3:
                mEQData[3] = (byte) (buf[2] & 0x1f);
                break;
            case 4:
                mEQData[4] = (byte) (buf[2] & 0x1f);
                break;
        }

        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
    }
}
