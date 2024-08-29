package com.zhuchao.android.car.cartype.hiworld;

import android.content.Intent;
import android.util.Log;

import com.common.utils.AppConfig;
import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

public class WeiChaiJMP001Hiworld extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MUTE}, {0x4, MyCmd.Keycode.SPEECH},

            {0x5, MyCmd.Keycode.BT_DIAL}, {0x6, MyCmd.Keycode.BT_HANG},


            {0x8, MyCmd.Keycode.PREVIOUS}, {0x9, MyCmd.Keycode.NEXT},

            {0xc, MyCmd.Keycode.MODLE},

    };
    private final static byte[][] KEYS_WHEEL2 = {{0x1, MyCmd.Keycode.POWER}, {0x6, MyCmd.Keycode.BACK}, {0x16, MyCmd.Keycode.PLAY_PAUSE}, {0x17, MyCmd.Keycode.PREVIOUS}, {0x18, MyCmd.Keycode.NEXT}, {0x19, MyCmd.Keycode.PREVIOUS}, {0x1a, MyCmd.Keycode.NEXT}, {0x2b, MyCmd.Keycode.HOME}, {0x41, MyCmd.Keycode.NAVIGATION}, {0x45, MyCmd.Keycode.VOLUME_UP}, {0x46, MyCmd.Keycode.VOLUME_DOWN},};
    private final static byte[][] KEYS_WHEEL3 = {{0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0}, {0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0}, {0x2, MyCmd.Keycode.ROLL_NEXT, 0}, {0x12, MyCmd.Keycode.ROLL_PREV, 0},};

    public WeiChaiJMP001Hiworld() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
        buildCmdDoor((byte) 0x1a, (byte) 0x2, (byte) 0xfc, (byte) 0x03);
        buildCmdAngle((byte) 0x1a, (byte) 0x0, 540);
        buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x3);
        buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 0x3);
        buildCmdRadarFrontEx((byte) 0x4);
        buildCmdVersion((byte) 0xf0, (byte) 0x0);
        mIdAC = 0x31;

        buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 0, (byte) 2, (byte) 0, KEYS_WHEEL2);

        mIdKey3 = 0x021122;
        MAP_KEYS3 = KEYS_WHEEL3;

    }

    @Override
    public void stopConnect() {

    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x2, (byte) 0x24, 0x0, 0x31};
        switch (CarUtil.getModelId()) {
            case 9:
                cmd[2] = 1;
                break;
            case 13:
                switch (CarUtil.getCarTypeConfig()) {
                    case 0:
                        cmd[2] = 3;
                        break;
                    case 1:
                        cmd[2] = 2;
                        break;
                    case 2:
                        cmd[2] = 1;
                        break;
                }
                break;

        }
        return cmd;
    }

    @Override
    public int getAngleValue2(byte[] data) {
        int angle = (short) (((data[8] & 0xff) << 8) | (data[9] & 0xff));
        return -angle;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0xff || (data & 0xff) == 0xfa) {

        } else if ((data & 0xff) == 0xfe) {
            data = 0;
        } else {

        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[10];

        airData[0] = (byte) (((data[2] & 0x40) << 1) | ((data[2] & 0x01) << 6) | ((data[3] & 0x10) << 1) | ((data[4] & 0x20) >> 5) | ((data[4] & 0x10) >> 3));

        //		if (((data[3] & 0x10) == 0)) {
        //			airData[0] |= 0x20;
        //		}

        airData[4] = (byte) (((data[3] & 0x1) << 7));
        airData[7] = (byte) (((data[3] & 0x80) >> 7));

        switch ((data[6] & 0xff)) {
            case 1:
                airData[9] = (byte) (0x1);
                break;
            case 2:
                // airData[1] = (byte) (0x20);
                break;
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 5:
                airData[1] = (byte) (0x60);
                break;
            case 6:
                airData[1] = (byte) (0x40);
                break;
            case 0xb:
                airData[1] = (byte) (0x80);
                break;
            case 0xc:
                airData[1] = (byte) (0xa0);
                break;
            case 0xd:
                airData[1] = (byte) (0xc0);
                break;
            case 0xe:
                airData[1] = (byte) (0xe0);
                break;
        }

        airData[1] |= (byte) (data[7] & 0x0f);

        airData[2] = data[8];
        airData[3] = (byte) 0xfa;

        airData[5] |= 0x80;
        airData[7] |= 0x40;
        super.parseACInfo(airData);
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case (byte) 0xe8:
                if (CarUtil.getModelId() == 13 && CarUtil.getCarTypeConfig() == 1) {
                    doRightCameraSwitch(data[4]);
                }
                break;
            case (byte) 0x1a:
                if (CarUtil.getModelId() == 13 && CarUtil.getCarTypeConfig() == 2) {
                    doBackCameraSwitch(data[10]);
                }
                super.parseCanboxData(data, len);
                break;
            default:
                super.parseCanboxData(data, len);
                break;
        }
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    public void setMediaSrc(int source) {

    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }

    public void startConnect() {
        byte[] buf = new byte[]{0x3, (byte) 0x6a, 0x5, 1, (byte) 0xf0};

        sendDataToCanbox(buf, buf.length);
    }

    private void doRightCameraSwitch(int s) {

        String top = AppConfig.getTopActivity();

        Intent it = new Intent(Intent.ACTION_VIEW);
        boolean topIsCamera = top != null && top.contains("com.android.car.frontcamera.FrontCameraActivity");
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
                it.setClassName("com.car.ui", "com.android.car.frontcamera.FrontCameraActivity");
                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

                mContext.startActivity(it);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
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

    public void udpateVoiceControl(int data) {

        byte[] buf = new byte[]{0x6, (byte) 0x6f, 0x14, 0, 0, 0};

        int param = ((data & 0xff00) >> 8);
        if (param == 0) {
            param = 2;
        }
        switch (data & 0xff) {
            case 0x1:
                if (param == 1) {
                    buf[3] = 0x11;
                    buf[4] = 0x11;
                    buf[5] = 0x10;
                } else {

                }
                break;
            case 0x2:
                buf[3] = (byte) ((param & 0x3) << 4);
                break;
            case 0x3:
                buf[3] = (byte) ((param & 0x3) << 0);
                break;
            case 0x4:
                buf[4] = (byte) ((param & 0x3) << 4);
                break;
            case 0x5:
                buf[4] = (byte) ((param & 0x3) << 0);
                break;
            case 0x6:
                buf[5] = (byte) ((param & 0x3) << 4);
                break;
        }

        sendDataToCanbox(buf, buf.length);
    }
}
