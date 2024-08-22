package com.zhuchao.android.car.cartype.hiworld;

import android.os.Handler;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Locale;


public class VWHiworld extends Canbox {

    public VWHiworld() {
        mIdAC = 0x73;
        buildCmdDoor((byte) 0x73, (byte) 0x2, (byte) 0xf8, (byte) 0x09);
        buildCmdAngle((byte) 0x72, (byte) 0x0, 140);

        buildCmdRadarBack((byte) 0x72, (byte) 0x0, (byte) 127);
        buildCmdRadarFront((byte) 0x72, (byte) 0x0, (byte) 127);
        buildCmdRadarFrontEx((byte) 10);
        buildCmdRadarBackEx((byte) 6);
        //		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdKey = 0x040172;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;

        mLcdInfo[0] = (byte) 0xd;
        mLcdInfo[1] = (byte) 0xd2;
    }


    @Override
    public int getAngleValue2(byte[] data) {
        int angle = data[7] & 0xff;
        if (angle == 0) {
            angle = data[6] & 0xff;
            angle = -angle;
        }
        return -angle;
    }


    private final byte[] mLcdInfo = new byte[14];
    private final static byte[] IDS_TO_CANBOXSETTING = {0x12, 0x13, 0x72, 0x73};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x9, KEY_NEXTSONG}, {0x8, KEY_PREVIOUSSONG}, {0x3, KEY_MUTE},

            {0x8, KEY_MIC}, {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG}, {0xF, MyCmd.Keycode.SPEECH}, {0xa, MyCmd.Keycode.MODLE},
    };

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {
            data = (byte) 0xfa;
        } else if ((data & 0xff) == 0xff) {

        } else if ((data & 0xff) == 0x1) {
            data = 0;
        } else {
            data = (byte) ((data & 0xff) * 2);
        }
        return data;
    }

    public void parseACInfo(byte[] data) {
        if (data[4] == 0xfe) {
            data[4] = (byte) 0xff;
        }
        if (data[5] == 0xfe) {
            data[5] = (byte) 0xff;
        }

        byte[] airData = new byte[8];

        airData[0] = (byte) (((data[2] & 0x0c)) | ((data[2] & 0x40) << 1) | ((data[2] & 0x01) << 4) | ((data[3] & 0x40)) | ((data[3] & 0x20) >> 5) | ((data[3] & 0x10) >> 3));

        if ((data[2] & 0x30) == 0x10) {
            airData[0] |= 0x20;
        } else if ((data[2] & 0x30) == 0x30) {
            airData[4] = (byte) 0x80;
        }

        airData[4] |= ((data[2] & 0x02) << 1);
        airData[4] |= ((data[3] & 0x03));
        airData[4] |= ((data[3] & 0x0c) << 2);

        airData[1] = (byte) (((data[6] & 0x0f)) | ((data[6] & 0x40) >> 1) | ((data[6] & 0x20) << 1) | ((data[6] & 0x10) << 3));

        airData[6] = (byte) (((data[7] & 0x0f)) | ((data[7] & 0x40) >> 1) | ((data[7] & 0x20) << 1) | ((data[7] & 0x10) << 3));

        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        airData[5] |= 0x88;
        super.parseACInfo(airData);
    }

    public void parseDoor(byte[] data) {

        byte door = (byte) (data[9] & 0xfc);

        door = doorChangeStyle3(door);


        if (mDoorStatus != door) {
            mDoorStatus = door;
            Handler handler = getHandler("CanService");
            if (null != handler) {
                handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

            }
        }
    }

    public void parseCanboxData(byte[] data, int len) {
        if (data[0] == 0x73) {
            parseDoor(data);
        }
        super.parseCanboxData(data, len);
    }

    public void startConnect() {

    }

    public void setVolume(int volume) {

        //		mShowVolume = true;
        //		Util.byteArrayCopy(mLcdInfoVol, mLcdInfo, 0, 0, mLcdInfo.length);
        //		copyLcdInfo(mLcdInfoVol, "VOL " + volume);
        //		mLcdInfoVol[12] = 0;
        //		mLcdInfoVol[15] = 0;
        //		sendDataToCanbox(mLcdInfoVol, mLcdInfoVol.length);
        //		mHandler.removeMessages(0);
        //		mHandler.sendEmptyMessageDelayed(0, 4000);
    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        // canbox
        byte[] send = new byte[len + 4];
        send[0] = (byte) (len + 3);
        send[1] = (byte) 0xaa;
        send[2] = (byte) 0x55;
        send[len + 3] = sum(data, len);
        byteArrayCopy(send, data, 3, 0, len);
        sendCmd(CANBOX_WRITE_COMMON_DATA, 0, send);
    }

    public byte sum(byte[] data, int len) {
        byte sum = 0;
        for (int i = 0; i < len; ++i) {
            sum += data[i];
        }
        sum = (byte) ((sum & 0xFF) - 1);
        return sum;
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        switch (source) {
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                ++play;
                break;
        }

        String s = String.format("%02d:%02d:%02d", (time) / 3600, (time) / 60, (time) % 60);
        copyLcdInfo(mLcdInfo, s);
        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        if (source == MyCmd.SOURCE_RADIO) {
            String s;
            if (b[0] >= 0x10) { // am
                s = String.valueOf(freq);
                copyLcdInfo(mLcdInfo, s);
                mLcdInfo[2] = 0x4;
            } else {

                s = String.format("%d.%02d", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                if (freq < 10000) {
                    s = " " + s;
                }
                copyLcdInfo(mLcdInfo, s);
                mLcdInfo[2] = 0x1;
            }
        }

        if (!mShowVolume) {
            sendDataToCanbox(mLcdInfo, mLcdInfo.length);
        }
    }

    private final boolean mShowVolume = false;

    public void setMediaSrc(int source) {// default is simple box
        String s = "";
        switch (source) {
            case MyCmd.SOURCE_RADIO: {
                //			s = "RADIO";
            }
            return;
            case MyCmd.SOURCE_DVD: {
                mLcdInfo[2] = 0x7;
                //			s = "CD";
            }
            break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO: {
                mLcdInfo[2] = 0xd;
                //			s = "USB";
            }
            break;
            case MyCmd.SOURCE_AUX:
                mLcdInfo[3] = 0xc;
                s = "AUX";
                break;
        }
        copyLcdInfo(mLcdInfo, s);
        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }


    public int getOutTemp(byte[] data) {//
        short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
        return t;
    }

    private void copyLcdInfo(byte[] lcd, String s) {
        byte[] b = s.getBytes();

        for (int i = 0; i < lcd.length - 3 && i < b.length; ++i) {
            lcd[i + 3] = b[i];
        }
    }

    public void stopConnect() {

        Util.zeroBuf(mLcdInfo);
        copyLcdInfo(mLcdInfo, "");
        sendDataToCanbox(mLcdInfo, mLcdInfo.length);

        super.stopConnect();
    }

}
