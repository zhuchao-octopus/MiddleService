package com.zhuchao.android.car.cartype.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.utils.AppConfig;
import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.OSProManager;


public class DaciaSimple extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x38};
    private final static byte[][] KEYS_WHEEL1 = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG}, {0x6, KEY_MUTE}, {0x7, KEY_SOURCE},

            {0x8, MyCmd.Keycode.KEY_MIC}, {0x9, KEY_BT},

            {0x13, KEY_SEEK_NEXT}, {0x14, KEY_SEEK_PREV},

            {0x16, KEY_PLAYPAUSE},

    };
    private final static byte[][] KEYS_WHEEL2 = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG},
            //			{ 0x6, KEY_MUTE },
            {0x7, KEY_SOURCE},

            {0x8, MyCmd.Keycode.KEY_MIC}, {0x9, KEY_MUTE},

            {0x13, KEY_SEEK_NEXT}, {0x14, KEY_SEEK_PREV},

            {0x16, MyCmd.Keycode.MULT_NEXT_AND_HANG}, {6, MyCmd.Keycode.MULT_PREV_AND_RECEIVE},

    };
    private final static int HIDE_RADAR = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_RADAR) {
                RadarManager.stop();
            }
            super.handleMessage(msg);
        }
    };
    byte[] mAirData = new byte[8];
    int mDoorStatus;
    private byte[][] KEYS_WHEEL = KEYS_WHEEL1;

    public DaciaSimple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});

        if (CarUtil.getModelId() == 23) {
            KEYS_WHEEL = KEYS_WHEEL2;
        }

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private void parseWheelKey(byte[] data) {
        if (doKeyStudy(data[2], data[3])) {
            return;
        }
        byte key = 0;
        for (int i = 0; i < KEYS_WHEEL.length; ++i) {
            if (KEYS_WHEEL[i][0] == data[2]) {
                key = KEYS_WHEEL[i][1];
                break;
            }
        }

        if (key != 0) {
            if (data[2] == 3 || data[2] == 4) {
                doKey(key, 1);
                // Util.doSleep(20);
                doKey(key, 0);
            } else {
                doKey(key, data[3]);
            }

        } else {
            if (data[3] == 0) {
                doKey(0, 0);
            }
        }
    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
    }

    private byte getRadarData(byte i) {
        byte data = 0;
        switch (i) {
            case 0:
                data = 0;
                break;
            case 4:
                data = 1;
                break;
            case 3:
                data = 4;
                break;
            case 2:
                data = 7;
                break;
            case 1:
                data = 11;
                break;
        }
        return data;
    }

    private void parseACInfo(byte[] data, int len) {

        // if (data[4] >= 0x1f) {
        // data[4] = (byte) 0xff;
        // } else if (data[4] > 0) {
        // if ((data[6] & 0x1) == 0) {
        // data[4] = (byte) ((15.5f + (0.5f * data[4])) * 2);
        // } else {
        // data[4] = (byte) ((59 + (data[4] & 0xff)));
        // }
        // }
        // if (data[5] >= 0x1f) {
        // data[5] = (byte) 0xff;
        // } else if (data[5] > 0) {
        // if ((data[6] & 0x1) == 0) {
        // data[5] = (byte) ((15.5f + (0.5f * data[5])) * 2);
        // } else {
        // data[5] = (byte) ((59 + (data[5] & 0xff)));
        // }
        // }
        byte[] airData = new byte[8];

        airData[0] = (byte) (data[2] & 0xef);
        airData[0] |= (byte) (((data[6] & 0x40) >> 6));

        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        airData[4] = (byte) (data[7] & 0xff);
        airData[4] |= (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x8) >> 1) | ((data[2] & 0x1) << 3));

        airData[5] = (byte) ((data[6] & 0x1));

        airData[7] = (byte) (((data[6] & 0x10) >> 4));
        airData[7] |= (byte) (((data[6] & 0x80) >> 2));

        int msg = CANBOX_RETURN_AIR;
        if ((data[3] & 0x10) == 0) {
            // msg = CANBOX_RETURN_AIR;
            airData[0] &= ~0x80;
        }

        boolean airControl = false;
        if (airData[0] != mAirData[0] || airData[1] != mAirData[1] || airData[2] != mAirData[2] || airData[3] != mAirData[3] || airData[4] != mAirData[4] || airData[5] != mAirData[5] || airData[6] != mAirData[6] || airData[7] != mAirData[7]) {
            airControl = true;

            mAirData = airData;
        }

        if (airControl) {
            Handler handler = getHandler("CanService");
            if (null != handler) {
                handler.sendMessage(handler.obtainMessage(msg, airData));
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data);
            }

            break;

            case 0x21: {
                parseACInfo(data, len);
            }
            break;
            case 0x24: {
                if ((data[2] & 0x1) != 0) {
                    int door = (data[2] & 0xfc);
                    door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((door & 0x4) << 3));

                    if (mDoorStatus != door) {
                        mDoorStatus = door;
                        Handler handler = getHandler("CanService");
                        if (null != handler) {
                            handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                        }
                    }
                }

            }
            break;

            case 0x27: {

                int t = (data[2] & 0x7f);
                int temp = t * 5;
                if ((data[2] & 0x80) != 0) {
                    temp = -temp;
                }
                String s = "";
                // if (temp >= -58 && temp <= 171) {
                if ((data[3] & 0x1) != 1) {

                    s = String.format("%d.%d%s", temp / 10, (temp % 10) >= 0 ? (temp % 10) : -(temp % 10), mContext.getResources().getString(R.string.temp_unic_centigrade));

                } else {
                    s = String.format("%d%s", temp / 10, mContext.getResources().getString(R.string.temp_unic_fahrenheit));
                }

                if (s.length() > 1) {
                    GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
                }
                // }
            }
            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = -(short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));
                    Log.d("abc", ":" + angle);
                    if (angle >= -0x1700 && angle <= 0x1700) {


                        angle = ((angle * 3000) / 5888);


                        //				if ((data[2] & 0x80) != 0) {
                        //					angle = -angle;
                        //				}
                        if (angle > -50 && angle < 50) {
                            angle = 50;
                        }

                        handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
                    }
                }
            }
            break;

            case 0x22: // Radar back
            {
                // byteArrayCopy(mRadar, data, 0, 2, 4);

                mRadar[0] = getRadarData(data[2]);
                mRadar[1] = getRadarData(data[3]);
                mRadar[2] = getRadarData(data[4]);
                mRadar[3] = getRadarData(data[5]);
                boolean zero = Util.isZero(mRadar);
                if (!zero) {
                    RadarManager.start(mContext);
                    checkHideRadar();
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendEmptyMessage(CANBOX_RADAR_BACK);
                }
            }
            break;
            case (byte) 0x94:
                if (data[2] == 0x1) {
                    OSProManager.simulationReverse((byte) 1);
                } else {
                    OSProManager.simulationReverse((byte) 0);
                }

                Handler handler = getHandler("Reverse");
                if (null != handler) {

                    handler.sendMessage(handler.obtainMessage(CANBOX_DACIA_UI_DATA, (data[3] & 0xf), 0));
                }


                CarUtil.m360UI = ((data[2] & 0xff) << 8 | (data[3] & 0xf));
                break;

            case 0x30: {
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
            default:
                super.parseCanboxData(data, len);
        }
    }

    private void do360CameraSwitch(int s, int data) {

        String top = AppConfig.getTopActivity();

        Intent it = new Intent(Intent.ACTION_VIEW);
        boolean topIsCamera = top != null && top.contains("com.android.car.frontcamera.BackCameraActivity");
        if (s == 0) {
            if (topIsCamera) {
                topIsCamera = true;
                it.putExtra("finish", 1);
            }
        } else {
            //			if (!topIsCamera){
            topIsCamera = true;
            it.putExtra("data", data | 0x80);
            //			}
        }

        if (topIsCamera) {
            try {
                it.setClassName("com.car.ui", "com.android.car.frontcamera.BackCameraActivity");
                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

                mContext.startActivity(it);
            } catch (Exception e) {
                //				Log.e(TAG, e.getMessage());
            }
        }
    }

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source) {// default is simple box

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    protected void doKey(int value, int status) { // value 0 -> key up

        //		Log.d("Nissan2013Simple", "doKey:" + value);
        //		if (CarUtil.getChangeKey() == 1) {
        value = changeKey(value);
        //		}

        switch (status) {
            case 0:
                if (mKeyDown != 0) {
                    if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
                        int ret = getLongKey(value);
                        if (ret != 0) {
                            mKeyDown = ret;
                        }
                    }
                    doKey(mKeyDown);
                    mKeyDown = 0;
                    longClick = false;
                }
                break;
            case 1:
                mKeyDown = value;
                mClickTime = System.currentTimeMillis();
                longClick = false;
                break;
            case 2:
                if (value == AK_KEYPAD_VOLUME_A || value == AK_KEYPAD_VOLUME_D) {
                    doKey(value);
                    mKeyDown = 0;
                } else {
                    if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
                        if (mKeyDown != 0) {
                            longClick = true;
                            int ret = getLongKey(value);
                            if (ret != 0) {
                                doKey(ret);
                                mKeyDown = 0;
                            }
                        }
                    }
                }
                break;
        }

    }

    public int getUpdateTime() {
        return 60000;
    }
}
