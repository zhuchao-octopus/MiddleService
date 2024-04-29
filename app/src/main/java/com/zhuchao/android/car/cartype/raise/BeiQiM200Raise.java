package com.zhuchao.android.car.cartype.raise;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;


public class BeiQiM200Raise extends Canbox {

    public BeiQiM200Raise() {
        buildCmdVersion((byte) 0xFF, (byte) 0x0);
    }

    public void startConnect() {
        super.startConnect();
        mLcdInfo[0] = (byte) 0x83;
        mLcdInfo[1] = (byte) 0xe;
    }

    public void stopConnect() {

        Util.zeroBuf(mLcdInfo);
        copyLcdInfo(mLcdInfo, "Welcome");
        sendDataToCanbox(mLcdInfo, mLcdInfo.length);


        super.stopConnect();
    }

    public int getUpdateTime() {
        return 1000;
    }

    private final byte[] mLcdInfo = new byte[16];
    private final byte[] mLcdInfoVol = new byte[16];
    private boolean mShowVolume = false;

    public void updateTime() {
        if (mContext == null) {
            return;
        }
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        if ("12".equals(strTimeFormat)) {
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
        }
        byte[] buf;
        if (mShowVolume) {
            buf = mLcdInfoVol;
        } else {
            buf = mLcdInfo;
        }
        buf[11] = h;
        buf[10] = (byte) curDate.getMinutes();
        sendDataToCanbox(buf, buf.length);
    }

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mShowVolume = false;
            }
            super.handleMessage(msg);
        }
    };

    private void copyLcdInfo(byte[] lcd, String s) {
        byte[] b = s.getBytes();

        for (int i = 0; i < 8; ++i) {
            if (i < b.length) {
                lcd[2 + i] = b[i];
            } else {
                lcd[2 + i] = 0;
            }
        }
    }

    public void setVolume(int volume) {

        mShowVolume = true;
        Util.byteArrayCopy(mLcdInfoVol, mLcdInfo, 0, 0, mLcdInfo.length);
        copyLcdInfo(mLcdInfoVol, "VOL " + volume);
        mLcdInfoVol[12] = 0;
        mLcdInfoVol[15] = 0;
        sendDataToCanbox(mLcdInfoVol, mLcdInfoVol.length);
        mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 4000);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        String s = String.format("%02d:%02d:%02d", (time) / 3600, (time) / 60, (time) % 60);
        copyLcdInfo(mLcdInfo, s);

    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        if (source == MyCmd.SOURCE_RADIO) {
            String s;
            if (b[0] >= 0x10) { // am
                s = "    " + freq;
                copyLcdInfo(mLcdInfo, s);
                mLcdInfo[12] = 0x11;
                mLcdInfo[15] = 0x20;
            } else {

                s = "    " + String.format("%d.%02d", (freq) / 100, (freq) % 100);
                if (freq < 10000) {
                    s = " " + s;
                }
                copyLcdInfo(mLcdInfo, s);
                mLcdInfo[12] = 0x21;
                mLcdInfo[15] = 0x10;
            }
        }

        if (!mShowVolume) {
            sendDataToCanbox(mLcdInfo, mLcdInfo.length);
        }
    }

    public void setMediaSrc(int source) {// default is simple box
        String s = "";
        switch (source) {
            case MyCmd.SOURCE_RADIO: {
                //			s = "RADIO";
            }
            break;
            case MyCmd.SOURCE_DVD: {
                mLcdInfo[12] = (byte) 0x80;
                mLcdInfo[15] = 0x0;
                //			s = "CD";
            }
            break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO: {

                mLcdInfo[12] = 0x40;
                mLcdInfo[15] = 0x0;
                //			s = "USB";
            }
            break;
            default:
                s = "AUX";
                break;
        }
        copyLcdInfo(mLcdInfo, s);
        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }


}
