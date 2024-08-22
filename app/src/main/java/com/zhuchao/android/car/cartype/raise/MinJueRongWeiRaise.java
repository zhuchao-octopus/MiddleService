package com.zhuchao.android.car.cartype.raise;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.utils.AppConfig;
import com.common.utils.MyCmd;
import com.common.utils.UtilSystem;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;


public class MinJueRongWeiRaise extends Canbox {

    public MinJueRongWeiRaise() {
        buildCmdRepeatSendCarType(getCarTypeCmd());
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xf8, (byte) 0x02);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x7);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x7);
        buildCmdAngle((byte) 0x29, (byte) 0x0, 0x2198);
        // buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdOutTemp((byte) 0x27, (byte) 0x10);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);

        buildBrake((byte) 0x24, (byte) 0x3, (byte) 0x2, (byte) 0);
        mIdAC = 0x21;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x39, 0x40, 0x41, 0x42, 0x52, 0x53, 0x54, 0x60, 0x61, 0x62, 0x63, 0x64};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.PREVIOUS}, {0x4, MyCmd.Keycode.NEXT}, {0x6, MyCmd.Keycode.MULT_MUTE_AND_BT},
            {0x7, MyCmd.Keycode.MODLE},
            //		{ 0x8, MyCmd.Keycode },
            {0x9, MyCmd.Keycode.BT}, {0x10, MyCmd.Keycode.SPEECH}, {0x11, MyCmd.Keycode.MENU}, {0x12, MyCmd.Keycode.BACK}, {0x13, MyCmd.Keycode.HOME}, {0x14, MyCmd.Keycode.CANBOX_OPEN_AC_VIEW},
            {0x15, MyCmd.Keycode.KEY_CAR_SETTING}, {0x16, MyCmd.Keycode.MUTE}, {0x17, MyCmd.Keycode.KEY_360}, {0x1f, MyCmd.Keycode.NUMBER_STAR}, {0x20, MyCmd.Keycode.ROLL_PREV},
            {0x21, MyCmd.Keycode.ROLL_NEXT}, {0x32, MyCmd.Keycode.NAVIGATION}, {(byte) 0x80, MyCmd.Keycode.POWER}, {(byte) 0x81, MyCmd.Keycode.VOLUME_ROLL_UP},
            {(byte) 0x82, MyCmd.Keycode.VOLUME_ROLL_DOWN},
    };

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0xee, 0x02, 0, 0};
        switch (CarUtil.getModelId()) {
            case 36:
                cmd[2] = 0x50;
                cmd[3] = 0;
                break;
            case 3:
                cmd[2] = 0x50;
                cmd[3] = 1;
                break;
            case 31:
                cmd[2] = 0x50;
                cmd[3] = 2;
                break;
            case 8:
                cmd[2] = 0x50;
                cmd[3] = 3;
                break;
            case 34:
                cmd[2] = 0x50;
                cmd[3] = 4;
                break;
            case 28:
                cmd[2] = 0x50;
                cmd[3] = 5;
                break;
            case 49:
                cmd[2] = 0x50;
                cmd[3] = 6;
                break;
            case 18:
                cmd[2] = 0x51;
                cmd[3] = 1;
                break;
            case 22: {
                cmd[2] = 0x51;
                switch (CarUtil.getCarTypeConfig()) {
                    case 0:
                        cmd[3] = 2;
                        break;
                    case 1:
                        cmd[3] = 3;
                        break;
                    default:
                        return null;
                }
            }
            break;
            case 32:
                cmd[2] = 0x51;
                cmd[3] = 4;
                break;
            case 33:
                cmd[2] = 0x51;
                cmd[3] = 5;
                break;
            case 9:
                cmd[2] = 0x51;
                cmd[3] = 6;
                break;
            case 51: {
                cmd[2] = 0x51;
                if (CarUtil.getCarTypeConfig() == 2) {
                    cmd[3] = 9;
                } else {
                    cmd[3] = 7;
                }
            }
            break;
            case 50:
                cmd[2] = 0x51;
                cmd[3] = 8;
                break;
            default:
                return null;
        }
        return cmd;
    }

    @Override
    public int getAngleValue(byte[] data) {

        int angle = ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));

        angle = angle - 0x8000;
        int max = 0x9fa6 - 0x8000;
        angle = ((angle * 3000) / max);

        if (angle > -50 && angle < 50) {
            angle = 50;
        }

        return angle;


    }

    private int getACTempPriv(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {
            data = (byte) 0;
        } else if ((data & 0xff) == 0x0f) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0xff) {
            data = (byte) 0xfa;
        } else if ((data & 0xff) == 0x10) {
            data = (byte) 32;
        } else {
            data = (byte) (34 + (data - 1) * 2);
        }
        return data;
    }

    byte[] airData = new byte[12];

    public void parseACInfoEx(byte[] data) {
        switch ((data[4] & 0xf)) {
            case 0:
                airData[11] = (byte) (0x40);
                break;
            case 1:
                airData[11] = (byte) (0x60);
                break;
            case 2:
                airData[11] = (byte) (0x20);
                break;
            case 0xf:
                airData[11] = (byte) (0x10);
                break;
            default:
                airData[11] = 0;
                break;
        }

        airData[11] |= (byte) (data[2] & 0x0f);


        airData[10] = (byte) getACTempPriv(data[3]);


        airData[9] = (byte) ((data[5] & 0x80) | ((data[5] & 0x40) >> 5));
        airData[0] |= 0x80;
        super.parseACInfoRear(airData);
    }

    public void parseACInfo(byte[] data) {


        airData[0] = (byte) ((data[2] & 0xd0) | ((data[2] & 0x08) >> 1) | ((data[5] & 0x80) >> 6) | ((data[5] & 0x40) >> 6));

        if ((data[2] & 0x06) == 0) {
            airData[0] |= 0x20;
        }

        airData[9] = 0;
        airData[1] = 0;
        switch ((data[3] & 0xf0) >> 4) {
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
            case 0xf:
                airData[9] = (byte) (0x1);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) (data[3] & 0x0f);


        airData[2] = (byte) getACTempPriv((data[4]));
        airData[3] = (byte) getACTempPriv((data[6]));


        airData[4] = (byte) (((data[7] & 0x80) >> 4) | ((data[2] & 0x04) << 5) | ((data[5] & 0x06) >> 1) | ((data[5] & 0x18) << 1));

        airData[7] = (byte) (((data[7] & 0x40) << 1) | ((data[5] & 0x20) >> 5));

        if (airData[1] == 0) {
            //			Util.zeroBuf(airData);
        }
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }

    private byte mDriverMode = 0;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mContext != null) {
                Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
                i.putExtra("buf", (byte[]) msg.obj);
                mContext.sendBroadcast(i);
            }
        }
    };

    public void parseCanboxData(byte[] data, int len) {
        switch (data[0]) {
            case 0x11:
                parseACInfoEx(data);
                break;
            case 0x24:
                if (CarUtil.getModelId() == 18 || CarUtil.getModelId() == 32) {

                } else {
                    super.parseCanboxData(data, len);
                }
                break;
            case 0x53:
                if ("com.canboxsetting/com.canboxsetting.MainActivity".equals(AppConfig.getTopActivity())) {
                    super.parseCanboxData(data, len);

                } else {

                    if (data[2] < 4 && mDriverMode != data[2]) {// no super sport
                        // now
                        if (!"com.canboxsetting/com.canboxsetting.MainActivity".equals(AppConfig.getTopActivity())) {
                            UtilSystem.doRunActivity(mContext, "com.canboxsetting", "com.canboxsetting.MainActivity");
                            mHandler.removeMessages(0);
                            mHandler.sendMessageDelayed(mHandler.obtainMessage(0, data), 800);
                        } else {
                            super.parseCanboxData(data, len);
                        }
                        mDriverMode = data[2];
                    }
                }
                break;
            default:
                super.parseCanboxData(data, len);
                break;
        }
    }

    public int getOutTemp(byte[] data) {//
        int t = ((data[2] & 0x7f)) * 10;
        if ((data[2] & 0x80) != 0) {
            t = -t;
        }
        return t;
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
            h |= 0x80;
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
        }

        byte m = (byte) curDate.getMinutes();
        //		byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();

        byte[] buf = new byte[]{(byte) 0xa6, 0x05, y, mon, d, h, m};
        sendDataToCanbox(buf, buf.length);
    }
}
