package com.zhuchao.android.car.cartype.binarytek;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.utils.AppConfig;
import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.OSProManager;

import java.util.Date;


public class VWMQBBNR extends Canbox {

    public VWMQBBNR() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG}, {0x5, KEY_BT}, {0x6, KEY_MUTE}, {0x7, KEY_SOURCE}, {0x8, KEY_MIC},

            {0x13, KEY_NEXTSONG}, {0x14, KEY_PREVIOUSSONG},
    };

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
            doKey(key, data[3]);
        } else {
            if (data[3] == 0) {
                doKey(0, 0);
            }
        }
    }

    private final static byte[][] KEYS_WHEEL2 = {
            {0x2, KEY_NEXTSONG}, {0x1, KEY_PREVIOUSSONG}, {0x3, MyCmd.Keycode.FAST_F}, {0x4, MyCmd.Keycode.FAST_R}, {0x11, MyCmd.Keycode.BT_DIAL}, {0x12, MyCmd.Keycode.BT_HANG}, {0x14, KEY_HOME},
            {0x17, KEY_MIC}, {0x19, MyCmd.Keycode.KEY_BT_VOICE_SPEAKER}, {0x18, MyCmd.Keycode.KEY_BT_VOICE_PHONE}, {0x30, KEY_BACK},

    };

    private void parseWheelKey2(byte[] data) {
        if (doKeyStudy(1, data[2], 1)) {
            doKeyStudy(1, data[2], 0);
            return;
        }

        byte key = 0;
        for (int i = 0; i < KEYS_WHEEL2.length; ++i) {
            if (KEYS_WHEEL2[i][0] == data[2]) {
                key = KEYS_WHEEL2[i][1];
                break;
            }
        }

        if (key != 0) {
            doKey(key, 1);
            doKey(key, 0);
        }
    }


    byte mAirKey = 0;
    byte[] mAirData = new byte[8];

    private void parseACInfo(byte[] data, int len) {

        if (!isShowAir()) {
            sendCanboxInfo("com.canboxsetting", data);
            //return;
        }

        if (data[4] >= 0x1f) {
            data[4] = (byte) 0xff;
        } else if (data[4] > 0) {
            if ((data[6] & 0x1) == 0) {
                data[4] = (byte) ((15.5f + (0.5f * data[4])) * 2);
                if (CarUtil.getCarType() == 1) {
                    data[4] -= 4;
                }
            } else {
                data[4] = (byte) ((59 + (data[4] & 0xff)));
            }
        }
        if (data[5] >= 0x1f) {
            data[5] = (byte) 0xff;
        } else if (data[5] > 0) {
            if ((data[6] & 0x1) == 0) {
                data[5] = (byte) ((15.5f + (0.5f * data[5])) * 2);
                if (CarUtil.getCarType() == 1) {
                    data[5] -= 4;
                }
            } else {
                data[5] = (byte) ((59 + (data[5] & 0xff)));
            }
        }
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


        airData[5] |= 0x80;

        int msg = CANBOX_HIDE_AIR;
        //		if (/*(data[2] & 0x80) != 0 && */((data[3] & 0x10) != 0)) {
        //			msg = CANBOX_RETURN_AIR;
        //		}
        byte key = (byte) (data[8] & 0x4);

        if ((data[2] & 0x80) != 0) {
            if (!Util.isBufEquals(airData, mAirData)) {
                Util.byteArrayCopy(mAirData, airData, 0, 0, airData.length);
                msg = CANBOX_RETURN_AIR;
            }
        }

        if ((key != mAirKey)) {
            mAirKey = key;
            if (key != 0) {
                if (msg == CANBOX_HIDE_AIR) {

                    doKey(MyCmd.Keycode.KEY_AIR_CONTROL);
                    msg = CANBOX_RETURN_AIR;
                }
            }
        }

        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(msg, airData));
        }
    }

    private final int[] mRadarColor = new int[8];

    private int getRadarColor(int i) {
        int color = Color.GREEN;
        switch (i) {
            case 0x1:
                color = Color.WHITE;
                break;
            case 0x2:
                color = Color.YELLOW;
                break;
            case 0x3:
                color = Color.RED;
                break;

        }
        return color;
    }

    private byte getRadarData(byte i) {
        byte data = 0;
        if (i > 0x0 && i <= 0xf) {
            data = 1;
        } else if (i >= 0x10 && i <= 0x1e) {
            data = 2;
        } else if (i >= 0x1f && i <= 0x2d) {
            data = 3;
        } else if (i >= 0x2e && i <= 0x3c) {
            data = 4;
        } else if (i >= 0x3d && i <= 0x4b) {
            data = 5;
        } else if (i >= 0x4c && i <= 0x5a) {
            data = 6;
        } else if (i >= 0x5b && i <= 0x69) {
            data = 7;
        } else if (i >= 0x6a && i <= 0x78) {
            data = 8;
        } else if (i >= 0x79 && i <= 0x87) {
            data = 9;
        } else if (i >= 0x88 && i <= 0x96) {
            data = 10;
        } else if (i >= 0x97 && i <= 0xa5) {
            data = 11;
        }
        return data;
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data);
            }

            break;
            case 0x2f: {
                parseWheelKey2(data);
            }
            break;
            case 0x21: {
                parseACInfo(data, len);
            }
            break;
            case 0x22: // Radar back
            {
                // byteArrayCopy(mRadar, data, 0, 2, 4);

                mRadar[0] = getRadarData(data[2]);
                mRadar[1] = getRadarData(data[3]);
                mRadar[2] = getRadarData(data[4]);
                mRadar[3] = getRadarData(data[5]);

                mRadarColor[0] = getRadarColor((data[6] & 0xf0) >> 4);
                mRadarColor[1] = getRadarColor((data[6] & 0xf) >> 0);
                mRadarColor[2] = getRadarColor((data[7] & 0xf0) >> 4);
                mRadarColor[3] = getRadarColor((data[7] & 0xf) >> 0);

                if (!Util.isZero(mRadar)) {
                    RadarManager.start(mContext);
                    checkHideRadar();
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK, mRadarColor));
                }
            }
            break;
            case 0x30: {
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
            case 0x23: // Radar front
            {
                // byteArrayCopy(mRadar, data, 4, 2, 4);

                mRadar[4] = getRadarData(data[2]);
                mRadar[5] = getRadarData(data[3]);
                mRadar[6] = getRadarData(data[4]);
                mRadar[7] = getRadarData(data[5]);

                mRadarColor[5] = getRadarColor((data[6] & 0xf0) >> 4);
                mRadarColor[4] = getRadarColor((data[6] & 0xf) >> 0);
                mRadarColor[7] = getRadarColor((data[7] & 0xf0) >> 4);
                mRadarColor[6] = getRadarColor((data[7] & 0xf) >> 0);

                if (!Util.isZero(mRadar)) {
                    RadarManager.start(mContext);
                    checkHideRadar();
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {

                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT, mRadarColor));
                }
            }
            break;
            case 0x32: // Radar front
            {
                // byteArrayCopy(mRadar, data, 4, 2, 4);

                mRadarLeft[0] = getRadarData(data[2]);
                mRadarLeft[1] = getRadarData(data[3]);
                mRadarLeft[2] = getRadarData(data[4]);
                mRadarLeft[3] = getRadarData(data[5]);

                int[] radarColor = new int[4];

                radarColor[1] = getRadarColor((data[6] & 0xf0) >> 4);
                radarColor[0] = getRadarColor((data[6] & 0xf) >> 0);
                radarColor[3] = getRadarColor((data[7] & 0xf0) >> 4);
                radarColor[2] = getRadarColor((data[7] & 0xf) >> 0);

                if (mRadarSwitch == 1) {
                    //				RadarManager.start(mContext);
                    Handler handler = getHandler(RadarManager.TAG);
                    if (null != handler) {
                        //					checkHideRadar();
                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_LEFT, radarColor));
                    }
                }
            }
            break;
            case 0x33: // Radar front
            {
                // byteArrayCopy(mRadar, data, 4, 2, 4);

                mRadarRight[0] = getRadarData(data[2]);
                mRadarRight[1] = getRadarData(data[3]);
                mRadarRight[2] = getRadarData(data[4]);
                mRadarRight[3] = getRadarData(data[5]);

                int[] radarColor = new int[4];

                radarColor[0] = getRadarColor((data[6] & 0xf0) >> 4);
                radarColor[1] = getRadarColor((data[6] & 0xf) >> 0);
                radarColor[2] = getRadarColor((data[7] & 0xf0) >> 4);
                radarColor[3] = getRadarColor((data[7] & 0xf) >> 0);

                if (mRadarSwitch == 1) {
                    //				RadarManager.start(mContext);
                    Handler handler = getHandler(RadarManager.TAG);
                    if (null != handler) {
                        //					checkHideRadar();
                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_RIGHT, radarColor));
                    }
                }
            }
            break;
            case 0x24: {
                //			if ((data[2] & 0x1) != 0) {
                int door = (data[2] & 0xfc);
                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((door & 0x4) << 3));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }
                //			}

            }
            break;

            case 0x25: // Radar status
            {

                if ((data[2] & 0x2) != 0) {
                    mRadarSwitch = 1;
                } else {
                    RadarManager.stop();
                    mRadarSwitch = 0;
                }

                // byte[] status = new byte[2];
                // status[0] = data[2];
                // status[1] = data[3];
                //
                // Handler handler = getHandler("Reverse");
                // if (null != handler) {
                // handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_STATUS,
                // status));
                // }
            }
            break;
            case 0x27: {

                int t = (short) ((data[3] & 0xff) | ((data[4] & 0xff) << 8));
                int temp = t;
                mUnit = data[2];
                updateOutDoorTemp(temp);
            }
            break;

            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = ((data[2] & 0xff) | ((data[3]) << 8));
                    angle = -(angle * 300 / 0x4dce);
                    //				if (angle > -5 && angle < 5) {
                    //					angle = 5;
                    //				}
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));
                }
            }
            break;

            case 0x40:
                if (data[2] == (byte) 0x40) {
                    if ((data[3] & 0x2) != 0) {
                        OSProManager.simulationReverse((byte) 1);
                    } else {
                        OSProManager.simulationReverse((byte) 0);
                    }

                    sendToReverseUI(data[2] & 0xff, (data[3] & 0xff) << 8 | data[6] & 0xff);
                } else if (data[2] == (byte) 0xB0) {
                    sendToReverseUI(data[2] & 0xff, (data[4] & 0xff) | (data[5] & 0xff) << 8 | (data[6] & 0xff) << 16);
                }
                break;
            case 0x68:
                if (((data[2] & 0xf) != 0) || (data[3] == 2) || (data[3] == 3) || (data[3] == 4)) {
                    if (!"com.canboxsetting/com.canboxsetting.TPMSActivity".equals(AppConfig.getTopActivity())) {

                        try {
                            Intent it = new Intent(Intent.ACTION_VIEW);
                            it.setClassName("com.canboxsetting", "com.canboxsetting.TPMSActivity");
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(it);
                            mHandler.sendMessageDelayed(mHandler.obtainMessage(DEALY_SEND_TPMS, data), 1000);
                        } catch (Exception e) {
                            // Log.e(TAG, ""+e);
                        }
                    }
                }
                break;
            case 0x51:

                returnEQData(data);
                break;
        }

        if (data[0] == 0x40 || data[0] == 0x41 || data[0] == 0x50 || data[0] == 0x63 || data[0] == 0x62 || data[0] == 0x60 || data[0] == 0x25 || data[0] == 0x16 || data[0] == 0x65 || data[0] == 0x68 || data[0] == 0x66 || data[0] == 0x51 || data[0] == 0x52) {
            sendCanboxInfo("com.canboxsetting", data);
        }
    }

    private byte[] mBuf;
    private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
    private int mUnit = 0;

    @SuppressLint("DefaultLocale")
    public void updateOutDoorTemp(int temp) {

        if (temp == CarUtil.INVALID_OUT_DOOR_TEMP) {
            if (mTempOutDoor != CarUtil.INVALID_OUT_DOOR_TEMP) {
                temp = mTempOutDoor;
            } else {
                return;
            }
        }
        mTempOutDoor = temp;

        if (CarUtil.mTempUnit == 2) {
            if (mUnit != 1) {
                float t = temp / 10.0f;
                temp = (int) (((t) * 1.8f + 32) * 10);
            }
            mUnit = 1;
        } else if (CarUtil.mTempUnit == 1) {
            if (mUnit == 1) {
                temp = (int) ((((float) temp / 10.0f) - 32) / 1.8f) * 10;
            }
            mUnit = 0;
        }

        String s = "";
        // if (temp >= -58 && temp <= 171) {
        if (mUnit != 1) {

            s = String.format("%d.%d%s", temp / 10, (temp % 10) >= 0 ? (temp % 10) : -(temp % 10), mContext.getResources().getString(R.string.temp_unic_centigrade));

        } else {
            s = String.format("%d%s", temp / 10, mContext.getResources().getString(R.string.temp_unic_fahrenheit));
        }

        if (s.length() > 1) {
            GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
        }

    }

    private byte mRadarSwitch = 0;
    private int mDoorStatus = 0;

    public void setReverseRadaVol(byte param) {
        byte[] data = new byte[]{(byte) 0xc6, 0x2, 0x0, param};
        sendDataToCanbox(data, data.length);
    }

    public void setParkCarMode(byte param) {
        byte[] data = new byte[]{(byte) 0xc6, 0x2, 0x2, param};
        sendDataToCanbox(data, data.length);
    }

    public void requestInfo(byte param) {
        byte[] data = new byte[]{(byte) 0x90, 0x2, param, 0};
        sendDataToCanbox(data, data.length);
    }

    private byte[] mData = new byte[]{
            (byte) 0xc0, 0x8, 0, 0, 0, 0, 0, 0, 0, 0
    };

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte h = (byte) ((time / 3600));
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        // ++play;

        byte s = 0;
        byte s2 = 0;
        switch (source) {
            case MyCmd.SOURCE_DVD:
                s = 0x2;
                s2 = 0x10;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                ++play;
                s = 8;
                s2 = 0x11;
                break;
            case MyCmd.SOURCE_BT:
                s = 0xb;
                s2 = 0x10;
                break;
            default:
                s = 0x07;
                s2 = 0x30;
                break;
        }

        if (MyCmd.SOURCE_DVD == source) {
            mData = new byte[]{
                    (byte) 0xc0, 0x8, s, s2, 0, (byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec
            };

        } else {
            mData = new byte[]{
                    (byte) 0xc0, 0x8, s, s2, 0, 0, (byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), min, sec
            };
        }

        // if (mPhoneStatus < HFP_INFO_CALLED) {

        sendDataToCanbox(mData, mData.length);
        // }
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        mData = new byte[]{
                (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0, 0, 0
        };
        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source) {// default is simple box
        byte s = 0;
        byte mediaType = 0;
        switch (source) {
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
            case 0:
                return;
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
                mediaType = 0x40;
                break;
            default:
                s = 0x00;
                mediaType = 0x0;
                break;
        }

        // if (s == 0xb || s == 0x7) {
        // data = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0,
        // 0 };
        // } else {
        mData = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0};
        // }

        sendDataToCanbox(mData, mData.length);
    }

    public void setVolume(int volume) {

        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

    private final static int HIDE_RADAR = 0;
    private final static int DEALY_SEND_TPMS = 1;
    private final static int REQUEST_STEER_ANGLE = 2;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_RADAR:
                    RadarManager.stop();
                    break;
                case DEALY_SEND_TPMS:
                    try {
                        sendCanboxInfo("com.canboxsetting", (byte[]) (msg.obj));
                    } catch (Exception e) {

                    }
                    break;
                case REQUEST_STEER_ANGLE:
                    requestSteerAngle();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void requestSteerAngle() {
        byte[] data = new byte[]{(byte) 0x90, 0x2, 0x29, 0};
        sendDataToCanbox(data, data.length);
        mHandler.removeMessages(REQUEST_STEER_ANGLE);
        mHandler.sendEmptyMessageDelayed(REQUEST_STEER_ANGLE, 2000);
    }

    public void setPhone(int status, String num) {// default is simple box
        switch (status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:
            case HFP_INFO_CONNECTING:
            case HFP_INFO_CONNECTED:
                status = 0;
                break;
            case HFP_INFO_CALLED:
                status = 2;
                break;
            case HFP_INFO_INCOMING:
                status = 1;
                break;
            case HFP_INFO_CALLING:
                status = 4;
                break;
        }
        byte[] data = new byte[4];// {(byte)0xc5, 0x1, (byte)status};
        data[0] = (byte) 0xc5;
        data[1] = (byte) (2);
        data[2] = (byte) 0;
        data[3] = (byte) status;

        sendDataToCanbox(data, data.length);

        if (num == null) {
            num = " ";
        }
        byte[] n = num.getBytes();
        data = new byte[n.length + 4];// {(byte)0xc5, 0x1, (byte)status};
        data[0] = (byte) 0xcA;
        data[1] = (byte) (n.length + 2);
        data[2] = (byte) 1;
        data[3] = (byte) 3;
        byteArrayCopy(data, n, 4, 0, n.length);
        Util.doSleep(100);
        sendDataToCanbox(data, data.length);

    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
        //requestSteerAngle();//test for pg
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

        byte format = 1;

        String date_foramt = SettingProperties.getProperty(mContext, SettingProperties.KEY_DATE_FORMAT);
        if (date_foramt != null) {
            if ("dd/MM/yyyy".equals(date_foramt)) {
                format = 0;
            } else if ("MM/dd/yyyy".equals(date_foramt)) {
                format = 2;
            }
        }

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

        //		Log.d("cccc", ""+curDate.getYear());
        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{
                (byte) 0xa6, 0x07, y, mon, d, h, m, s, format
        };

        sendDataToCanbox(buf, buf.length);
    }

    public void sendId3(byte index, String num) {

        try {
            if (num == null) {
                num = "";
            }
            byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

            int num_len = n.length;

            if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
                num_len -= 2;
            }

            if (num_len > 43) {
                num_len = 43;
            }
            byte[] data;
            if (num_len == 0) {
                data = new byte[4];

                data[0] = index;
                data[1] = 2;
                data[2] = 0x10;
                data[3] = 0;
            } else {

                int len = num_len + 3;

                data = new byte[len];

                data[0] = index;
                data[1] = (byte) (num_len + 1);
                data[2] = 0x10;
                for (int i = 0; i < num_len && i < (data[1]); ++i) {
                    //					data[3 + i] = n[i];

                    if (i % 2 == 0) {
                        data[3 + i] = n[i + 3];
                    } else {
                        data[3 + i] = n[i + 1];
                    }

                }
            }
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("Nissan2013Simple", "sendId3" + e);
        }
    }

    String mName = null;
    String mArtist = null;
    String mAlbum = null;


    public void setSongName(String s) {
        sendId3((byte) 0x70, s);
        mName = s;
    }

    public void setSongAritst(String s) {
        sendId3((byte) 0x71, s);
        mArtist = s;
    }

    public void setSongAlbum(String s) {
        sendId3((byte) 0x72, s);
        mAlbum = s;
    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (19 << 8) | 19;

            byte[] buf = new byte[]{(byte) 0x90, 0x2, 0x51, 0};
            sendDataToCanbox(buf, buf.length);
        } else {
            byte[] buf = new byte[]{(byte) 0xa8, 0x2, 0x0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    buf[2] = 1;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    buf[2] = 2;
                    break;
                case EQ_CMD_SET_LOW:
                    buf[2] = 3;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    buf[2] = 4;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    buf[2] = 5;
                    break;
                default:
                    return 0;
            }

            sendDataToCanbox(buf, buf.length);
        }
        return ret;
    }

    private void returnEQData(byte[] buf) {
        byte[] data = new byte[5];
        data[0] = buf[3];
        data[1] = buf[4];
        data[2] = buf[5];
        data[3] = buf[6];
        data[4] = buf[7];
        super.returnEQData(EQ_CMD_SET_ALL_DATA, data);
    }

    private int mReverseStatus = 0;

    private void sendToReverseUI(int data1, int data2) {
        mReverseStatus = data2;
        Handler handler = getHandler("Reverse");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(CANBOX_VW_RAISE_UI_DATA, data1, data2));
        }
    }

    public int getReverseViewID() {
        sendToReverseUI(0x40, mReverseStatus);

        return 46;
    }

    public void sendReverseCmd(int cmd) {
        byte[] buf = new byte[]{
                (byte) ((cmd & 0xff0000) >> 16), 0x2, (byte) ((cmd & 0xff00) >> 8), (byte) ((cmd & 0xff) >> 0)
        };

        sendDataToCanbox(buf, buf.length);
    }

    public boolean isSupportCompass() {
        return true;
    }

    public void updateCompass(int compass) {
        int direction = compassAngleToDirect(compass);

        byte[] buf = new byte[]{
                (byte) (0xa7), 0x3, (byte) (direction & 0xff), (byte) ((compass & 0xff) >> 0), (byte) ((compass & 0xff00) >> 8)
        };

        sendDataToCanbox(buf, buf.length);
    }

}
