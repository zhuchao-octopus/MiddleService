package com.zhuchao.android.car.cartype.raise;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;

public class CarX80 extends Canbox {

    private final static int HIDE_RADAR = 0;
    private final byte mDoorStatus = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_RADAR) {
                RadarManager.stop();
            }
        }
    };

    public CarX80() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x1, 0x1, 0x2, 0x0});

    }

    private void parseWheelKey(byte[] data, int len) {

        if (doKeyStudy(data[3], (data[3] == 0) ? 0 : 1)) {
            return;
        }

        switch (data[3]) {
            case 0x0:
                doKey(0, 0);
                break;
            case 0x14:
                doKey(AK_KEYPAD_VOLUME_A, 1); // vol+
                break;
            case 0x15:
                doKey(AK_KEYPAD_VOLUME_D, 1);// vol-
                break;
            case 0x13:
                doKey(KEY_PREVIOUSSONG, 1);
                break;
            case 0x12:
                doKey(KEY_NEXTSONG, 1);
                break;
            case 0x30:
                doKey(KEY_BT_DIAL, 1);
                break;
            case 0x31:
                doKey(KEY_BT_HANG, 1);
                break;
            case 0x16:
                doKey(AK_KEYPAD_MUTE_FAKE, 1);// mute
                break;
            case 0x11:
                doKey(KEY_MODE, 1);
                break;
        }
    }

    private void parseACInfo(byte[] data, int len) {

        if (len < 8) {
            return;
        }
        byte[] airData = new byte[8];
        if ((data[5] & 0x80) != 0) {
            //			Handler handler = getHandler("CanService");
            //			handler.sendMessage(handler.obtainMessage(CANBOX_OUT_DOOR_TEMP, data[1], 0));
            airData[2] = (byte) 0xfb;
        } else {
            if ((data[3] & 0xff) == 0xff) {
                airData[2] = (byte) 0xff;
            } else if ((data[3] & 0xff) == 0) {
                airData[2] = (byte) 0;
            } else {
                airData[2] = (byte) (((data[3] & 0xff) * 2) + ((((data[6] & 0xf0) >> 4) == 0x05) ? 1 : 0));
            }

            if ((data[8] & 0xff) == 0xff) {
                airData[3] = (byte) 0xff;
            } else if ((data[8] & 0xff) == 0) {
                airData[3] = (byte) 0;
            } else {
                airData[3] = (byte) (((data[8] & 0xff) * 2) + (((data[6] & 0xf) == 0x05) ? 1 : 0));
            }
        }


        airData[0] = (byte) (((data[4] & 0x80) >> 2) | ((data[4] & 0x04) << 1) | ((data[4] & 0x01) << 6));
        airData[1] = (byte) (((data[4] & 0x40) << 1) | ((data[4] & 0x10) << 1) | ((data[4] & 0x08) << 3));
        airData[1] |= (data[5] & 0x0F);

        //		airData[3] = 0;
        //		airData[4] = (byte) (data[6] & 0xff);
        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
        }
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == (byte) 0xfd) {
            switch (data[2]) {
                case 0x30: {

                    int angle = ((data[4] & 0xff) | ((data[3] & 0x7f) << 8));
                    if ((data[3] & 0x80) != 0) {
                        angle = -angle;
                    }

                    angle = ((angle * 3000) / 5400);
                    if (angle > -50 && angle < 50) {
                        angle = 50;
                    }

                    // 右转<0
                    Log.d(TAG, ":" + angle);

                    Handler handler = getHandler("Reverse");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));

                    }
                }
                break;
                case 0x12:
                    parseWheelKey(data, len);
                    break;
                case 0x10:
                    parseACInfo(data, len);
                    break;
                case 0x32:
                    if (data[3] == 0) {
                        for (int i = 0; i < 4; ++i) {
                            switch (data[5 + i]) {
                                case 0:
                                    mRadar[i] = 1;
                                    break;
                                case 1:
                                    mRadar[i] = 6;
                                    break;
                                case 2:
                                    mRadar[i] = 0xa;
                                    break;
                                case 3:
                                    mRadar[i] = 0;
                                    break;
                            }
                        }
                    } else if (data[3] == 1) {
                        for (int i = 0; i < 4; ++i) {
                            if (data[5 + i] == 0xff || data[5 + i] >= 15) {
                                mRadar[i] = 0;
                            } else {
                                mRadar[i] = data[5 + i];
                            }
                        }
                    }

                    boolean zero = Util.isZero(mRadar);
                    if (!zero) {
                        RadarManager.start(mContext);
                        checkHideRadar();
                    }
                    Handler handler = getHandler(RadarManager.TAG);
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                    }

                    break;
            }
        }
    }

    //	public void setReverseRadaVol(byte param) {
    //		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x0, param };
    //		sendDataToCanbox(data, data.length);
    //	}
    //
    //	public void setParkCarMode(byte param) {
    //		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x2, param };
    //		sendDataToCanbox(data, data.length);
    //	}
    //
    //	public void requestInfo(byte param) {
    //		byte[] data = new byte[] { (byte) 0x90, 0x2, param, 0 };
    //		sendDataToCanbox(data, data.length);
    //	}
    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        // if (mRadarSwitch != 1) {
        // int i;
        // for (i = 0; i < mRadar.length; ++i) {
        // if(mRadar[i]!=0){
        // break;
        // }
        // }
        // if (i >= mRadar.length) {
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 5000);
        // }

        // }
    }

    public void setMediaSrc(int source) {
    }

    @Override
    public void startConnect() {
    }

    @Override
    public void stopConnect() {
    }
}
