package com.zhuchao.android.car.cartype.changyuantong;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Locale;


public class Mazda6ChangYuanTong extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.NEXT}, {0x4, MyCmd.Keycode.PREVIOUS}, {0x7, MyCmd.Keycode.MODLE}, {0x13, MyCmd.Keycode.MUTE}, {0x14, MyCmd.Keycode.BT_DIAL}, {0x15, MyCmd.Keycode.BT_HANG}, {0x20, MyCmd.Keycode.MULT_MUTE_AND_POWER}, {0x21, MyCmd.Keycode.PLAY_PAUSE}, {0x22, MyCmd.Keycode.BT}, {0x23, MyCmd.Keycode.AUDIO}, {0x24, MyCmd.Keycode.NAVIGATION}, {0x25, MyCmd.Keycode.RADIO},};
    private final byte[] mLcdInfo = new byte[14];

    public Mazda6ChangYuanTong() {
        buildCmdDoor((byte) 0x24, (byte) 0x0, (byte) 0x3f, (byte) 0x02);

        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x2;
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        //		if ((data&0xff) <= 0xa){
        //			data = (byte)0xfa;
        //		} else if ((data&0xff) == 0x10){
        //			data = (byte)0;
        //		} else {
        //			//data =
        //		}
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[9];

        airData[0] = (byte) (((data[2] & 0xd0)) | ((data[2] & 0x08) >> 2) | ((data[2] & 0x04) >> 2) | ((data[2] & 0x02) << 1));

        if ((data[2] & 0x1) == 0) {
            airData[0] |= 0x20;
        }


        airData[1] = (byte) (((data[3] & 0xf)) | ((data[3] & 0x70) << 1));


        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);


        //		airData[4] = (byte) (
        //				 ((data[5] & 0x03) << 4)
        //					| ((data[5] & 0x30) >> 4));


        airData[7] = (byte) (((data[2] & 0x20) >> 5));
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    private void sendPoint(byte b) {
        byte[] buf = new byte[]{(byte) 0x82, 0x2, b, 0};
        sendDataToCanbox(buf, buf.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        if (source == MyCmd.SOURCE_RADIO) {
            String s;
            if (b[0] >= 0x10) { // am
                if (freq < 1000) {
                    s = String.format("AM       %d", (freq), Locale.ENGLISH);
                } else {
                    s = String.format("AM      %d", (freq), Locale.ENGLISH);
                }
                sendPoint((byte) 0);
            } else {
                if (freq < 10000) {
                    s = String.format("FM      %d", (freq), Locale.ENGLISH);
                } else {
                    s = String.format("FM     %d", (freq), Locale.ENGLISH);
                }

                sendPoint((byte) 0x20);
            }
            mLcdInfo[0] = (byte) 0x83;
            mLcdInfo[1] = 0xc;

            copyLcdInfo(mLcdInfo, s);

            sendDataToCanbox(mLcdInfo, mLcdInfo.length);
        }

    }

    private void copyLcdInfo(byte[] lcd, String s) {
        byte[] b = s.getBytes();

        for (int i = 0; i < lcd.length - 3; ++i) {
            if (i < b.length) {
                lcd[i + 2] = b[i];
            } else {
                lcd[i + 2] = 0x20;
            }
        }
    }

    public void setMediaSrc(int source) {
    }


    public int getOutTemp(byte[] data) {//
        short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
        return t;
    }

}
