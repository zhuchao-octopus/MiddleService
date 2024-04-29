package com.zhuchao.android.car.cartype.simple;

import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Locale;

public class AudiA3Simple extends Canbox {

    public AudiA3Simple() {

        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x3, 0x4, 0x3, 0x0
        });

        data[0] = 12;
    }

    public void startConnect() {
        data[1] = 0x53;
        data[3] = 0;
        sendDataToCanbox(data, data.length);
    }

    @Override
    public void stopConnect() {
        data[1] = 0;
        data[3] = 0;
        sendDataToCanbox(data, data.length);
    }


    //	public void sendDataToCanbox(byte[] data, int len) {
    //		byte[] send = new byte[len + 4];
    //		send[0] = (byte) 0x55;
    //		send[1] = (byte) 0xaa;
    //		send[2] = (byte) (((len - 1) & 0x1f) | (data[0] << 5));
    //		byteArrayCopy(send, data, 3, 1, len - 1);
    //
    //		data[0] = send[2];
    //		send[send.length - 1] = Sum(data, len);
    //		sendCommonDataToCanbox(send);
    //	}

    public void sendDataToCanbox(byte[] data, int len) {
        byte[] send = new byte[len + 4];
        send[0] = (byte) (len + 3);
        send[1] = (byte) 0x55;
        send[2] = (byte) 0xaa;
        byteArrayCopy(send, data, 3, 0, len);

        send[send.length - 1] = Sum(data, len);
        //		Log.d("cde", "sendCmd:" + Util.byteArrayToHex(send));
        sendCommonDataToCanbox(send);
    }


    private byte Sum(byte[] data, int len) {
        int l = (data[0] & 0x1f);
        int id = ((data[0] & 0xe0)); //?is diff with pro

        int sum = (l);// & id);

        for (int i = 1; i < len; ++i) {
            sum += data[i] & 0xff;
        }


        return (byte) ((sum & 0xFF) - 1);
    }

    private final static byte[][] KEYS_WHEEL = {

            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x3, KEY_BT}, {0x4, KEY_MUTE}, {0x5, KEY_PREVIOUSSONG}, {0x6, KEY_NEXTSONG}, {0x7, KEY_SOURCE}, {0x13, KEY_PREVIOUSSONG}, {0x14, KEY_NEXTSONG}

    };

    private void parseWheelKey(byte bkey) {

        if (doKeyStudy(bkey, 1)) {
            doKeyStudy(bkey, 0);
            return;
        }

        int key = 0;
        if (bkey == 0) {
            return;
        }

        for (int i = 0; i < KEYS_WHEEL.length; ++i) {
            if (KEYS_WHEEL[i][0] == bkey) {
                key = KEYS_WHEEL[i][1];
                break;
            }
        }

        if (key != 0) {
            doKey(key, 1);
            doKey(0, 0);
        }

    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if ((data[1] & 0xe) >> 5 == 0x0) {
            parseWheelKey(data[4]);
        }
    }

    private final byte[] data = new byte[13];

    public void setVolume(int volume) {

        int i1 = volume % 10;
        int i2 = (volume / 10) % 10;
        data[2] = (byte) ((i2 << 4) | i1);

        sendDataToCanbox(data, data.length);

    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);

        byte hour = (byte) ((time / 3600) % 60);

        String s = String.format(Locale.ENGLISH, "%02d:%02d:%02d", hour, min, sec);

        byte[] c = s.getBytes();
        data[3] = 8;
        for (int i = 0; i < 8 && i < c.length; ++i) {
            data[i + 4] = c[i];
        }

        //		for (int i = c.length; i < 8; ++i) {
        //			data[i+4] = 0;
        //		}

        if (MyCmd.SOURCE_DVD != source) {

            data[1] = 0x55;
        } else {

            data[1] = 0x22;
        }
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        String s = "";
        int freq;
        if (b[0] >= 0x10) { //am

            b[0] = (byte) (0x4 + b[0]);

            freq = (((b[2] & 0xff) << 8) | (b[1] & 0xff));
            s += freq + "KHZ";

        } else {
            b[0] = (byte) (0x10 + b[0] + 1);

            freq = (((b[2] & 0xff) << 8) | (b[1] & 0xff));

            s = (freq / 100) + ".";
            //			if ((freq % 100) < 10) {
            //				s += "0";
            //			}
            s += (freq % 100) / 10;

            s += "MHZ";
        }

        byte[] c = s.getBytes();
        data[1] = b[0];
        data[3] = 8;
        for (int i = 0; i < 8 && i < c.length; ++i) {
            data[i + 4] = c[i];
        }

        for (int i = c.length; i < 8; ++i) {
            data[i + 4] = 0;
        }


        Log.d("cde", type + ":sendCmd:" + Util.byteArrayToHex(b));

        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source) {//default is simple box

        byte mediaType = 0x53;
        switch (source) {
            case 0:
                mediaType = 0x10;
                break;
            case 1:
                mediaType = 0x22;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                mediaType = 0x55;
                break;
            case MyCmd.SOURCE_AUX:
                mediaType = 0x53;
                break;
            case MyCmd.SOURCE_DTV:
                mediaType = 0x51;
                break;
            case MyCmd.SOURCE_BT:
                mediaType = 0x40;
                break;
            default:
                mediaType = 0x53;
                break;
        }

        data[1] = mediaType;
        data[3] = 0;
        sendDataToCanbox(data, data.length);
    }

}
