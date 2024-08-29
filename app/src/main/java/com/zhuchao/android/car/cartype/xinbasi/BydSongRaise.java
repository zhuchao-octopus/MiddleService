package com.zhuchao.android.car.cartype.xinbasi;

import android.content.Intent;
import android.util.Log;

import com.common.utils.AppConfig;
import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Date;

public class BydSongRaise extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0xb, 0xd, 0xe, 0x10, 0x11};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.PREVIOUS}, {0x4, MyCmd.Keycode.NEXT}, {0x6, MyCmd.Keycode.MUTE}, {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.BT_DIAL}, {0x9, MyCmd.Keycode.BT_HANG}, {0xa, MyCmd.Keycode.SPEECH},};

    public BydSongRaise() {
        buildCmdDoor((byte) 0x8, (byte) 0x2, (byte) 0xfc, (byte) 0x02);
        buildCmdRadarFront((byte) 0x4, (byte) 0x0, (byte) 4);
        buildCmdRadarFrontEx((byte) 0x4);
        buildCmdRadarBack((byte) 0x4, (byte) 0x0, (byte) 4);
        buildCmdAngle((byte) 0xa, (byte) 0x0, 2560);

        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x3;
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    @Override
    public int getAngleValue2(byte[] data) {

        int angle = (short) ((data[4] & 0xff) | (((data[3] & 0xff)) << 8));

        if ((data[2] & 0x1) == 0) {
            angle = -angle;
        }
        return angle;


    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 33) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 17) {
            data = 0;
        } else if ((data & 0xff) > 17 && (data & 0xff) < 33) {
            data = (byte) (34 + (((data & 0xff) - 17) * 2));
        } else {
            data = (byte) 0xfa;
        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xc0) | ((data[2] & 0x20) >> 1) | ((data[2] & 0x10) >> 2) | ((data[2] & 0x08) >> 3) | ((data[3] & 0x10) << 1) | ((data[3] & 0x20) >> 4));


        airData[1] = (byte) ((data[3] & 0x07) | ((data[2] & 0x01) << 7) | ((data[2] & 0x02) << 5) | ((data[2] & 0x04) << 3));


        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        airData[5] |= 0x80;

        super.parseACInfo(airData);
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x12:
                doRightCameraSwitch(data[2] & 0x1);
                break;
            case 0xc:
                doBackCameraSwitch(data[2] & 0x80);
                break;
            default:
                super.parseCanboxData(data, len);
        }
    }


    private void doBackCameraSwitch(int s) {

        String top = AppConfig.getTopActivity();

        Intent it = new Intent(Intent.ACTION_VIEW);
        boolean topIsCamera = top != null && top.contains("com.android.car.frontcamera.BackCameraActivity");
        if (s == 0) {
            if (topIsCamera) {
                it.putExtra("finish", 1);
            }
        } else {
            if (!topIsCamera) {
                topIsCamera = true;
            }
        }

        if (topIsCamera) {
            try {
                it.setClassName("com.car.ui", "com.android.car.frontcamera.BackCameraActivity");
                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

                mContext.startActivity(it);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void doRightCameraSwitch(int s) {

        String top = AppConfig.getTopActivity();

        Intent it = new Intent(Intent.ACTION_VIEW);
        boolean topIsCamera = top != null && top.contains("com.android.car.frontcamera.SideCameraActivity");
        /*|| ((mRightCameraSwitch & 0xC0) != 0)*/

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


        if (topIsCamera) {
            try {
                it.setClassName("com.car.ui", "com.android.car.frontcamera.SideCameraActivity");
                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

                mContext.startActivity(it);
            } catch (Exception e) {
                //				Log.e(TAG, e.getMessage());
            }
        }
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
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


        byte m = (byte) curDate.getMinutes();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{(byte) 0x87, 0x05, y, mon, d, h, m};

        sendDataToCanbox(buf, buf.length);
    }


}
