package com.zhuchao.android.car.cartype.binarytek;

import android.content.Intent;
import android.provider.Settings;

import com.common.utils.AppConfig;
import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;

public class GuanZhiBNR extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x32, (byte) 0xd2,};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x5, MyCmd.Keycode.MUTE}, {0x3, MyCmd.Keycode.MULT_NEXT_AND_HANG}, {0x4, MyCmd.Keycode.MULT_PREV_AND_RECEIVE},};

    public GuanZhiBNR() {
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x4);

        buildCmdAngle((byte) 0x29, (byte) 0x5, 0x1200);
        buildCmdVersion((byte) 0x30, (byte) 0x0);

        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        // ++play;
        byte[] data;

        if (MyCmd.SOURCE_DVD != source) {
            data = new byte[]{(byte) 0xc3, 0x6, (byte) (total & 0xFF), (byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF), (byte) ((play >> 8) & 0xFF), min, sec};
        } else {
            data = new byte[]{(byte) 0xc3, 0x6, (byte) (1 & 0xFF), (byte) ((play) & 0xFF), (byte) (total & 0xFF), (byte) ((0) & 0xFF), min, sec};
        }
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        byte[] data = new byte[]{(byte) 0xc2, 0x4, b[0], b[1], b[2], 0};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source) {// default is simple box
        byte s = 0;
        byte mediaType = 0;
        switch (source) {
            case 0:
                s = 1;
                mediaType = 1;
                break;
            case 1:
                s = 2;
                mediaType = 0x10;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 6;
                mediaType = 0x12;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x09;
                mediaType = 0x11;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                mediaType = 0x30;
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                mediaType = 0x30;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0b;
                mediaType = 0x30;
                break;
            default:
                s = 0x00;
                mediaType = 0x0;
                break;
        }
        byte[] data;

        data = new byte[]{(byte) 0xc0, 0x2, s, mediaType};

        sendDataToCanbox(data, data.length);
    }

    public void setVolume(int volume) {

        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);

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

        if ("12".equals(strTimeFormat)) {
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
            h |= 0x80;
        } else {
            ampm = 1;
        }

        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        // Log.d("cccc", ""+curDate.getYear());
        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{(byte) 0xc6, 0x07, 0x50, h, m, s, y, mon, d};

        sendDataToCanbox(buf, buf.length);
    }

    private void doRightCameraSwitch(int s) {

        String top = AppConfig.getTopActivity();

        Intent it = new Intent(Intent.ACTION_VIEW);
        boolean topIsCamera = top != null && top.contains("com.android.car.frontcamera.SideCameraActivity");
        /* || ((mRightCameraSwitch & 0xC0) != 0) */

        if (s == 0) {
            if (topIsCamera) {
                it.putExtra("finish", 1);
            }
        } else if (s == 1) {
            if (!topIsCamera) {
                topIsCamera = true;
                it.putExtra("style", 1);
                it.putExtra("camera", MyCmd.CAMERA_SOURCE_DTV_CVBS);
            }
        }
        // else if (s == 2){
        //
        // if (topIsCamera){
        // it.putExtra("finish", 1);
        // } else {
        // it.putExtra("style", 1);
        // }
        // topIsCamera = true;
        // }

        if (topIsCamera) {
            try {
                it.setClassName("com.car.ui", "com.android.car.frontcamera.SideCameraActivity");
                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

                mContext.startActivity(it);
            } catch (Exception e) {
                // Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == (byte) 0xd1) {
            doRightCameraSwitch((data[3] & 0x80) >> 7);
        } else {
            super.parseCanboxData(data, len);
        }
    }

}
