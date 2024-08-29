package com.zhuchao.android.car.cartype.raise;

import android.util.Log;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.nio.charset.StandardCharsets;


public class ZongTaiRaise extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x24, 0x40, 0x41, 0x38, 0x39, 0x47};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x4, MyCmd.Keycode.KEY_SEEK_PREV}, {0x5, MyCmd.Keycode.BT}, {0x6, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.BT_HANG}, {0x9, MyCmd.Keycode.POWER}, {0x10, MyCmd.Keycode.NAVIGATION}, {0x11, MyCmd.Keycode.HOME}, {0x12, MyCmd.Keycode.AUDIO}, {0x13, MyCmd.Keycode.SETUP}, {0x14, MyCmd.Keycode.AUDIO}, {0x15, MyCmd.Keycode.SPEECH}, {0x16, MyCmd.Keycode.BT_DIAL}, {0x17, MyCmd.Keycode.BACK},};
    private byte[] mData = new byte[]{(byte) 0xc0, 0x8, 0, 0, 0, 0, 0, 0, 0, 0};

    public ZongTaiRaise() {
        buildCmdRepeatSendCarType(getCarTypeCmd());
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 15);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 32);
        if (CarUtil.getModelId() == 14) {
            buildCmdAngle((byte) 0x28, (byte) 0x0, 0x2198);
        } else {
            buildCmdAngle((byte) 0x29, (byte) 0x0, 0x2198);
        }
        buildCmdOutTemp((byte) 0x27, (byte) 0x10);
        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x21;
        mIdKey = 0x20;

        if (CarUtil.getModelId() == 15 || CarUtil.getModelId() == 11) {
            KEYS_WHEEL[11][1] = MyCmd.Keycode.VIDEO;
        }

        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0xee, 0x02, (byte) 0x90, 0};
        switch (CarUtil.getModelId()) {
            case 0:
                cmd[3] = 2;
                break;
            case 4:
                cmd[3] = 1;
                break;
            default:
                cmd[3] = 0;
        }
        return cmd;
    }

    @Override
    public int getAngleValue(byte[] data) {
        int angle = ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));
        int max;
        if (CarUtil.getModelId() == 14) {
            angle = 0x1ea0 - angle;
            max = (0x1ea0 - 0x0884);
        } else {

            angle = 0x8000 - angle;
            angle = -angle;
            max = (0xa600 - 0x8000);
        }

        angle = ((angle * 3000) / max);
        if (angle > -50 && angle < 50) {
            angle = 50;
        }

        return angle;


    }

    public byte getACTempPriv(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {
            data = (byte) 0;
        } else if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else {
            data = (byte) (35 + (data & 0xff));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = data[2];

        airData[1] = (byte) (data[3] & 0xef);


        if ((data[4] & 0x80) == 0 && (data[5] & 0x80) == 0) {
            airData[7] = 0x40;
            airData[2] = (byte) (data[4] & 0xff);
            airData[3] = (byte) (data[5] & 0xff);
        } else {
            airData[2] = getACTempPriv((byte) (data[4] & 0x7f));
            airData[3] = getACTempPriv((byte) (data[5] & 0x7f));
        }


        airData[4] = (byte) ((data[7] & 0x33) | ((data[6] & 0x20) << 2) | ((data[6] & 0x08) >> 1) | ((data[6] & 0x40) >> 3));


        airData[7] |= (byte) (((data[6] & 0x80) >> 2) | ((data[6] & 0x10) >> 4));
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {


    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        mData = new byte[]{(byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0, 0, 0};
        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source) {// default is simple box
        byte s = 0;
        byte mediaType = 0;
        switch (source) {
            case 1:
                s = 2;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 6;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 8;
                return;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0b;
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

    public void sendId3(byte index, String num) {

        try {
            if (num == null) {
                num = "";
            }

            byte[] n = num.getBytes(StandardCharsets.UTF_8);

            int num_len = n.length;


            if (num_len > 43) {
                num_len = 43;
            }
            byte[] data;
            if (num_len == 0) {
                data = new byte[4];

                data[0] = index;
                data[1] = 2;
                data[2] = 0x12;
                data[3] = 0;
            } else {

                int len = num_len + 3;

                data = new byte[len];

                data[0] = index;
                data[1] = (byte) (num_len + 1);
                data[2] = 0x12;
                for (int i = 0; i < num_len && i < (data[1]); ++i) {
                    data[3 + i] = n[i];
                }
            }
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("Nissan2013Simple", "sendId3" + e);
        }
    }


    public void setSongName(String s) {
        sendId3((byte) 0x70, s);

    }

    public void setSongAritst(String s) {
        sendId3((byte) 0x71, s);
    }

    @Override
    public void stopConnect() {
        // TODO Auto-generated method stub
        startRepeatSendLcdMsg(false);
        super.stopConnect();
    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        super.startConnect();
        startRepeatSendLcdMsg(true);
    }

    public void repeatSendLcdMsg() {
        if (mData != null) {
            sendDataToCanbox(mData, mData.length);
        }
        super.repeatSendLcdMsg();
    }

    @Override
    public int getOutTempUnit(byte[] data) {
        if ((data[2] & 0x1) != 0) {
            return 1;
        }
        return 0;
    }

    public int getOutTemp(byte[] data) {//
        int t = ((data[3] & 0xff) * 5) - 350;

        return t;
    }

}
