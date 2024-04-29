package com.zhuchao.android.car.cartype.binarytek;

import android.os.Handler;
import android.os.Message;

import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.OBDView;


public class CarOBDBinarytek extends Canbox {

    public CarOBDBinarytek() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub

        int obdId = 0;
        int obdIdLen = 0;

        switch (data[0]) {

            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu
                    // ma

                    int angle = -(((a * 3000) / 0x1640));

                    if (angle > -50 && angle < 0) {
                        angle = -50;
                    } else if (angle > 0 && angle < 50) {
                        angle = 50;
                    }
                    // Log.e("1", ""+(data[2] & 0xff));
                    // Log.e("2", ""+(data[3] & 0xff));
                    // Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
                }
            }
            break;
            case 0x24: {
                int door = (data[2] & 0xfc);

                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((door & 0x04) << 3));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }

                data[2] = (byte) door;//for obd screen saver
            }
            break;
            case 0x7F: {
                byte[] version = new byte[0x10];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
            case 0x27:
            case 0x6A:
            case 0x68: {
                sendCanboxInfo("com.canboxsetting", data);
            }
        }


        switch (data[0]) {

            case 0x24:
                obdId = OBDView.ID_DOOR;
                obdIdLen = 1;
                break;
            case 0x25:
                obdId = OBDView.ID_GEAR_BRAKE_SEAT_LIGHT;
                obdIdLen = 2;
                break;
            case 0x27:
                obdId = OBDView.ID_ENGINEER_TEMP;
                obdIdLen = 1;
                break;
            case 0x68:
                obdId = OBDView.ID_RPM;
                obdIdLen = 2;
                break;
            case 0x6a:
                obdId = OBDView.ID_SPEED;
                obdIdLen = 2;
                break;

        }

        if (obdId != 0 && obdIdLen > 0) {

            byte[] buf = new byte[obdIdLen];
            Util.byteArrayCopy(buf, data, 0, 2, buf.length);
            OBDView.sendCanboxInfo(mContext, obdId, buf);
        }

    }

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            byte[] buf = new byte[]{(byte) 0x89, 0x01, 5};
            sendDataToCanbox(buf, buf.length);
            buf[0] = (byte) 0x8a;
            sendDataToCanbox(buf, buf.length);
            buf[0] = (byte) 0x8b;
            sendDataToCanbox(buf, buf.length);
            super.handleMessage(msg);
        }
    };

    public void updateScreenSaveNeedData(boolean b) {
        if (b) {
            mHandler.sendEmptyMessageDelayed(0, 1);
            mHandler.sendEmptyMessageDelayed(0, 1500);
        } else {
            if (!GlobalDefinition.mTopIsNeedCanboxInfo) {
                byte[] buf = new byte[]{(byte) 0x89, 0x01, 0};
                sendDataToCanbox(buf, buf.length);
                buf[0] = (byte) 0x8a;
                sendDataToCanbox(buf, buf.length);
                buf[0] = (byte) 0x8b;
                sendDataToCanbox(buf, buf.length);
            }
        }
    }

    public void notifyReverse(int status) {
        byte[] buf = new byte[]{(byte) 0x88, 0x01, 0};
        if (status == 1) {
            buf[2] = 3;
        }
        sendDataToCanbox(buf, buf.length);
    }

    private int mDoorStatus = 0;

}
