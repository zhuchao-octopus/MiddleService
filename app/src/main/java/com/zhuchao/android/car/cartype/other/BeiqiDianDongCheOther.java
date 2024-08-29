package com.zhuchao.android.car.cartype.other;

import android.content.SharedPreferences;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class BeiqiDianDongCheOther extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x40};
    private static final byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.KEY_TURN_A}, {0x4, MyCmd.Keycode.KEY_TURN_D}, {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.SPEECH},

            {0x10, MyCmd.Keycode.KEY_FM}, {0x11, MyCmd.Keycode.KEY_AM}, {0x12, MyCmd.Keycode.POWER}, {0x13, MyCmd.Keycode.PREVIOUS}, {0x14, MyCmd.Keycode.NEXT}, {0x15, MyCmd.Keycode.MUTE}, {0x16, MyCmd.Keycode.KEY_RADIO_SCAN}, {0x17, MyCmd.Keycode.MODLE}, {0x18, MyCmd.Keycode.AS}, {0x19, MyCmd.Keycode.SETUP}, {0x20, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x21, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x22, MyCmd.Keycode.EQ}, {0x23, MyCmd.Keycode.MENU}, {0x24, MyCmd.Keycode.BT}, {0x25, MyCmd.Keycode.RADIO}, {0x26, MyCmd.Keycode.HOME}, {0x27, MyCmd.Keycode.EASY_CONNECT}, {0x28, MyCmd.Keycode.BT}, {0x29, MyCmd.Keycode.NAVIGATION}, {0x2a, MyCmd.Keycode.BACKLIGHT_OFF}, {0x2b, MyCmd.Keycode.PLAY_PAUSE}, {0x2c, MyCmd.Keycode.ROLL_NEXT}, {0x2d, MyCmd.Keycode.ROLL_PREV}, {0x2e, MyCmd.Keycode.VOLUME_UP}, {0x2f, MyCmd.Keycode.VOLUME_DOWN},};
    private final static String AC_DATA = "pro290_ac_data";
    private final static String AC_KEY = "pro290_ac_key";
    private final byte[] mAirDataSave = new byte[5];

    public BeiqiDianDongCheOther() {
        mIdAC = 0x21;
        buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 4);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 4);
        buildCmdAngle((byte) 0x29, (byte) 0x0, 540);
        buildCmdOutTemp((byte) 0x10, (byte) 0x10);
        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdKey = 0x20;
        if (CarUtil.getCarTypeConfig() == 2) {
            for (int i = 0; i < KEYS_WHEEL.length; ++i) {
                if (KEYS_WHEEL[i][0] == 0x28) {
                    KEYS_WHEEL[i][1] = MyCmd.Keycode.NAVIGATION;
                }
            }
        }
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0xee, 0x02, 0x4, 0};
        if (CarUtil.getModelId() == 36) {
            cmd[3] = 1;
        } else {
            return null;
        }
        return cmd;
    }

    @Override
    public int getAngleValue(byte[] data) {
        int angle = (data[2] | (data[3] << 8));

        int max = 0;
        if (angle > 0x1f00) {
            max = 0x2200;
        }

        angle -= 0x8000;
        angle = ((angle * 3000) / max);


        return angle;
    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        super.startConnect();
        Util.doSleep(10);
        //	sendSaveACData();


        Util.doSleep(10);
        String vehicle_version = Util.getFileString("/system/etc/vehicle_version");

        if (vehicle_version != null) {
            byte[] s = vehicle_version.getBytes();

            byte[] data = new byte[s.length + 2];

            data[0] = 0x30;
            data[1] = (byte) (data.length - 1);
            System.arraycopy(s, 0, data, 2, s.length);

            sendDataToCanbox(data, data.length);
        }
    }

    @Override
    public void stopConnect() {
        // TODO Auto-generated method stub
        super.stopConnect();
        saveACData(mAirDataSave);
    }

    @Override
    public int getACTemp(byte data) {

        if ((data & 0xff) >= 1 && (data & 0xff) <= 32) {
            data = (byte) (36 + (data - 1));
        }
        return data & 0xff;
    }

    public void parseACInfoInner(byte[] data) {
        byte[] airData = new byte[8];
        airData[0] = (byte) (((data[2] & 0x4c)) | ((data[2] & 0x10) << 1) | ((data[2] & 0x02) >> 1));
        airData[0] |= 0x80;


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
                airData[0] |= 0x02;
                break;
            default:
                airData[1] = 0;
                break;
        }
        airData[1] |= (byte) (data[4] & 0x0f);


        airData[2] = data[5];
        airData[3] = data[6];
        airData[4] = (byte) ((data[2] & 0x80) >> 5);

        airData[7] = (byte) ((data[2] & 0x01) << 5);

        updateCommonAirData(airData);
    }

    public void parseACInfo(byte[] data) {
        if (data.length >= 7) {
            Util.byteArrayCopy(mAirDataSave, data, 0, 2, mAirDataSave.length);
        }
        byte[] airData = new byte[8];
        airData[0] = (byte) (((data[2] & 0x4c)) | ((data[2] & 0x10) << 1) | ((data[2] & 0x02) >> 1));
        airData[0] |= 0x80;


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
                airData[0] |= 0x02;
                break;
            default:
                airData[1] = 0;
                break;
        }
        airData[1] |= (byte) (data[4] & 0x0f);


        airData[2] = data[5];
        airData[3] = data[6];
        airData[4] = (byte) ((data[2] & 0x80) >> 5);

        airData[7] = (byte) ((data[2] & 0x01) << 5);
        if ((data[4] & 0x0f) == 0) {
            //			Util.zeroBuf(airData);
        }
        super.parseACInfo(airData);
    }

    private void saveACData(byte[] data) {
        if (mContext != null) {
            SharedPreferences.Editor sharedata = mContext.getSharedPreferences(AC_DATA, 0).edit();

            String value = "";
            for (int i = 0; i < data.length; ++i) {
                value += data[i] + ",";
            }
            sharedata.putString(AC_KEY, value);
            sharedata.commit();
        }
    }

    private void sendSaveACData() {
        if (mContext != null) {
            SharedPreferences sharedata = mContext.getSharedPreferences(AC_DATA, 0);
            String s = sharedata.getString(AC_KEY, null);
            if (s != null) {
                String[] ss = s.split(",");
                if (ss != null && ss.length >= 5) {
                    for (int i = 0; i < ss.length; ++i) {
                        mAirDataSave[i] = Byte.valueOf(ss[i]);
                    }


                    byte[] data = new byte[]{(byte) 0x21, 0x5, 0, 0, 0, 0, 0};
                    Util.byteArrayCopy(data, mAirDataSave, 2, 0, 5);
                    parseACInfoInner(data);
                    sendDataToCanbox(data, data.length);
                }
            }
        }
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {


    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {// default is simple box

    }


    public boolean requestAngleData() {
        byte[] data3 = new byte[]{(byte) 0x90, 0x2, 0x29, 0};
        sendDataToCanbox(data3, data3.length);
        return true;
    }

    public int getOutTemp(byte[] data) {//
        int t = ((data[3] & 0xff));
        if (t != 0xff) {
            t = t * 5 - 550;
            return t;
        }
        return CarUtil.CLEAR_OUT_DOOR_TEMP;
    }

    public int getOutTempUnit(byte[] data) {//
        return data[2] & 0x1;
    }


    public void udpateVoiceControl(int data) {


        //		Log.d("ffck", "udpateVoiceControl:"+Integer.toHexString(data));

        int cmd = (data & 0xff);
        byte param = (byte) ((data & 0xff00) >> 8);
        switch (cmd) {
            case 0x7:
                cmd = 0x40;
                doKey(MyCmd.Keycode.KEY_AIR_CONTROL);
                break;
            case 0x8:
                cmd = 0x41;
                break;
            case 0x10:
                cmd = 0x42;
                break;
            case 0x14:
                cmd = 0x43;
                break;
            case 0x9:
                cmd = 0x44;
                if (param == 0) {
                    param = (byte) 0x81;
                } else {
                    param = (byte) 0x80;
                }
                break;
            case 0xa:
                cmd = 0x44;
                break;
        }

        byte[] buf = new byte[]{(byte) 0xef, 0x3, 0x7d, (byte) cmd, param};
        sendDataToCanbox(buf, buf.length);
    }

    public void requestVersion() {
        byte[] buf = new byte[]{(byte) 0x90, 0x2, 0x30, 0x0};
        sendDataToCanbox(buf, buf.length);
    }
}
