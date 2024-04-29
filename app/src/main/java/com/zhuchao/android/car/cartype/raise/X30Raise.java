package com.zhuchao.android.car.cartype.raise;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;


public class X30Raise extends Canbox {

    public X30Raise() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x4, KEY_NEXTSONG}, {0x3, KEY_PREVIOUSSONG},

            {0x6, KEY_MUTE}, {0x7, KEY_SOURCE}, {0x9, KEY_BT_DIAL}, {0xA, KEY_BT_HANG},

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
            {0x1, KEY_POWER}, {0x2, AK_KEYPAD_VOLUME_A}, {0x3, AK_KEYPAD_VOLUME_D}, {0x4, KEY_MUTE}, {0x5, MyCmd.Keycode.AS}, {0x6, MyCmd.Keycode.KEY_SEEK_PREV}, {0x7, MyCmd.Keycode.KEY_SEEK_NEXT},
            {0x8, MyCmd.Keycode.PLAY_PAUSE}, {0x9, MyCmd.Keycode.KEY_TURN_A}, {0xa, MyCmd.Keycode.KEY_TURN_D},

            {0xa, MyCmd.Keycode.NUMBER1}, {0xb, MyCmd.Keycode.NUMBER2}, {0xc, MyCmd.Keycode.NUMBER3}, {0xd, MyCmd.Keycode.NUMBER4}, {0xe, MyCmd.Keycode.NUMBER5}, {0xf, MyCmd.Keycode.NUMBER6},
            {0x10, MyCmd.Keycode.NUMBER1},

            {0x11, MyCmd.Keycode.KEY_SEEK_PREV}, {0x12, MyCmd.Keycode.KEY_SEEK_NEXT},

            {0x13, KEY_FM}, {0x14, KEY_MEDIA}, {0x15, MyCmd.Keycode.KEY_SHUFFLE}, {0x16, MyCmd.Keycode.KEY_REPEAT},

            {0x17, KEY_GPS}, {0x18, KEY_MEDIA}, {0x19, KEY_MIC}, {0x1a, KEY_BT}, {0x1b, MyCmd.Keycode.KEY_RADIO_SCAN}, {0x1c, MyCmd.Keycode.SCREEN_BRIGHTNESS},

            {0x1d, MyCmd.Keycode.BT_DIAL}, {0x1e, MyCmd.Keycode.BT_HANG}, {0x1F, KEY_BACK}, {0x20, KEY_HOME}, {0x21, KEY_HOME},

            {0x22, KEY_MEDIA},

    };

    private final static byte[][] KEYS_WHEEL2_B50 = {
            {0x1, KEY_POWER}, {0x2, AK_KEYPAD_VOLUME_A}, {0x3, AK_KEYPAD_VOLUME_D}, {0x4, KEY_MUTE}, {0x5, KEY_BACK}, {0x6, MyCmd.Keycode.KEY_SEEK_PREV}, {0x7, MyCmd.Keycode.KEY_SEEK_NEXT},
            {0x8, MyCmd.Keycode.PLAY_PAUSE}, {0x9, MyCmd.Keycode.KEY_TURN_A}, {0xa, MyCmd.Keycode.KEY_TURN_D},

            {0xa, MyCmd.Keycode.NUMBER1}, {0xb, MyCmd.Keycode.NUMBER2}, {0xc, MyCmd.Keycode.NUMBER3}, {0xd, MyCmd.Keycode.NUMBER4}, {0xe, MyCmd.Keycode.NUMBER5}, {0xf, MyCmd.Keycode.NUMBER6},
            {0x10, MyCmd.Keycode.NUMBER1},

            {0x11, MyCmd.Keycode.BT_DIAL}, {0x12, MyCmd.Keycode.BT_HANG},

            {0x13, KEY_FM}, {0x14, KEY_BT}, {0x15, KEY_MODE}, {0x16, KEY_MIC},

            {0x17, KEY_GPS}, {0x18, KEY_MEDIA}, {0x19, KEY_MIC}, {0x1a, KEY_BT}, {0x1b, MyCmd.Keycode.KEY_RADIO_SCAN}, {0x1c, MyCmd.Keycode.SCREEN_BRIGHTNESS},

            {0x1d, MyCmd.Keycode.BT_DIAL}, {0x1e, MyCmd.Keycode.BT_HANG}, {0x1F, KEY_BACK}, {0x20, KEY_HOME}, {0x21, KEY_HOME},

            {0x22, KEY_MEDIA},

    };

    private void parseWheelKey2(byte[] data) {
        byte[][] keys;
        if (CarUtil.getKeyType() == 1) {
            keys = KEYS_WHEEL2_B50;
        } else {
            keys = KEYS_WHEEL2;
        }

        byte key = 0;
        for (int i = 0; i < keys.length; ++i) {
            if (keys[i][0] == data[2]) {
                key = keys[i][1];
                break;
            }
        }

        if (key != 0) {
            doKey(key, 1);
            doKey(key, 0);
        }
    }

    private void parseACInfo(byte[] data, int len) {

        if (!isShowAir()) {
            sendCanboxInfo("com.canboxsetting", data);
            //	return;
        }

        byte[] airData = new byte[9];
        if ((data[5] & 0x80) != 0) {
            if ((data[5] & 0xff) == 0xF1) {
                data[5] = 0;
            } else if ((data[5] & 0xff) == 0xF2) {
                data[5] = (byte) 0xff;
            } else if ((data[5] & 0x1f) >= 0) {
                data[5] = (byte) ((18.0f + (0.5f * (data[5] & 0x1f))) * 2);
            }
        } else {
            airData[8] = 0x40;
        }

        if ((data[6] & 0x80) != 0) {
            if ((data[6] & 0xff) == 0xF1) {
                data[6] = 0;
            } else if ((data[6] & 0xff) == 0xF2) {
                data[6] = (byte) 0xff;
            } else if ((data[6] & 0x1f) >= 0) {
                data[6] = (byte) ((18.0f + (0.5f * (data[6] & 0x1f))) * 2);
            }
        } else {
            airData[8] = 0x40;
        }

        airData[0] = (byte) (data[2] & 0x4c);
        airData[0] |= (byte) (((data[2] & 0x10) << 1));
        airData[0] |= (byte) (((data[2] & 0x02) >> 1));

        airData[7] = (byte) (((data[2] & 0x80) >> 7));
        airData[1] = 0;
        if ((data[3] & 0xff) == 1) {
            airData[1] = 0x40;
        } else if ((data[3] & 0xff) == 2) {
            airData[1] = 0x60;
        } else if ((data[3] & 0xff) == 3) {
            airData[1] = 0x20;
        } else if ((data[3] & 0xff) == 4) {
            airData[1] = (byte) 0xA0;
        } else if ((data[3] & 0xff) == 5) {
            airData[1] = (byte) 0x80;
        }

        airData[1] |= (data[4] & 0xff);

        airData[2] = (byte) (data[5] & 0xff);
        airData[3] = (byte) (data[6] & 0xff);

        int msg = CANBOX_RETURN_AIR;
        if ((data[4] & 0xff) != 0) {
            msg = CANBOX_RETURN_AIR;
        }

        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(msg, airData));
        }
    }

    private byte getRadarData(byte i) {
        byte data = 0;
        switch (i) {
            case 0:
                data = 0;
                break;
            case 1:
                data = 1;
                break;
            case 2:
                data = 4;
                break;
            case 3:
                data = 7;
                break;
            case 4:
                data = 11;
                break;
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
            case 0x24: {
                parseWheelKey2(data);
            }

            break;

            case 0x21: {
                parseACInfo(data, len);
            }
            break;
            case 0x22: // Radar back
            {

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
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                }
            }
            break;
            case 0x23: // Radar back
            {

                mRadar[4] = getRadarData(data[2]);
                mRadar[5] = getRadarData(data[3]);
                mRadar[6] = getRadarData(data[4]);
                mRadar[7] = getRadarData(data[5]);

                boolean zero = Util.isZero(mRadar);
                if (!zero) {
                    RadarManager.start(mContext);
                    checkHideRadar();
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                }
            }
            break;
            case 0x25: {
                if ((data[2] & 0x08) == 0) {
                    mHandler.removeMessages(HIDE_RADAR);
                    RadarManager.stop();
                }
            }
            break;
            case 0x7F: {
                byte[] version = new byte[data[1] - 1];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
            case 0x28: {
                int door = (data[2] & 0xfc);
                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((door & 0x4) << 3));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }

                if ((data[3] & 0xff) != 0xff && (data[3] & 0xff) != 0xfe) {
                    int t = (short) ((data[3] & 0xff));
                    int temp = (t - 40) * 10;
                    updateOutDoorTemp(temp);
                }

            }
            break;

            case 0x30: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = ((data[3] & 0xff) | ((data[2] & 0x7f) << 8));

                    angle = (angle * 300 / 5400);
                    if (((data[2] & 0x80) != 0)) {
                        angle = -angle;
                    }
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));
                }
            }
            break;

            case 0x40:
                if (data[2] == (byte) 0xa0) {
                    if ((data[3] & 0x10) != 0) {
                        try {
                            Intent it = new Intent(Intent.ACTION_VIEW);
                            it.setClassName("com.canboxsetting", "com.canboxsetting.MainActivity");
                            it.putExtra(MyCmd.EXTRA_COMMON_CMD, 1);
                            it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(it);
                        } catch (Exception e) {
                            // Log.e(TAG, e.getMessage());
                        }
                    }
                }
                break;
            case 0x52:
                sendCanboxInfo("com.canboxsetting", data);
                break;
        }

    }

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

    private final byte mRadarSwitch = 0;
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

    byte[] data;

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        // byte h = (byte) ((time / 3600));
        // byte min = (byte) ((time / 60) % 60);
        // byte sec = (byte) ((time) % 60);
        // // ++play;
        //
        // byte s = 0;
        // byte s2 = 0;
        // switch (source) {
        // case MyCmd.SOURCE_DVD:
        // s = 0x2;
        // s2 = 0x10;
        // break;
        // case MyCmd.SOURCE_MUSIC:
        // case MyCmd.SOURCE_VIDEO:
        // ++play;
        // s = 8;
        // s2 = 0x11;
        // break;
        // case MyCmd.SOURCE_BT:
        // s = 0xb;
        // s2 = 0x10;
        // break;
        // default:
        // s = 0x07;
        // s2 = 0x30;
        // break;
        // }
        //
        // // if (MyCmd.SOURCE_DVD == source) {
        // // data = new byte[] { (byte) 0xc0, 0x8, s, s2, 0,
        // // (byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec };
        // //
        // // } else {
        // data = new byte[] { (byte) 0xc0, 0x8, s, s2,
        // (byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), 2,
        // h, min, sec };
        // // }
        //
        // if (mPhoneStatus < HFP_INFO_CALLED) {
        //
        // sendDataToCanbox(data, data.length);
        // }
    }

    private byte[] mData;

    public void setMediaSrc(int source) {
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
                s = 0x08;
                mediaType = 0x11;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                mediaType = 0x30;
                byte[] data2 = new byte[]{(byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0};
                sendDataToCanbox(data2, data2.length);
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                mediaType = 0x30;
                byte[] data3 = new byte[]{(byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0};
                sendDataToCanbox(data3, data3.length);
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

        mData = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0};
        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        // setMediaSrc(0);
        // if (b[0] != 0x10) {
        // b[0] += 1;
        // }
        // data = new byte[] { (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0,
        // 0,
        // 0 };
        // sendDataToCanbox(data, data.length);
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
            int len = 36;
            // if (len > 31) {
            // len = 31;
            // }
            byte[] data = new byte[len];
            if (index == 2) {
                data[0] = (byte) 0x70;
            } else {
                data[0] = (byte) 0x71;
            }

            data[1] = (byte) (len - 2);
            data[2] = 0x10;
            for (int i = 0; i < num_len && i < (len - 3); ++i) {
                Log.d("Accord2013Simple", i + ":" + n.length + ":" + num_len);
                data[3 + i] = n[i + (n.length - num_len)];
            }
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("Accord2013Simple", "sendId3" + e);
        }
    }

    String mName = null;
    String mArtist = null;
    String mAlbum = null;

    // public void setPhone(int status, String num) {
    // sendId3((byte)0x1, num);
    // }

    public void setSongName(String s) {
        sendId3((byte) 0x2, s);
        mName = s;
    }

    public void setSongAritst(String s) {
        sendId3((byte) 0x4, s);
        mArtist = s;
    }

    public void setSongAlbum(String s) {
        // sendId3((byte) 0x3, s);
        // mAlbum = s;
    }

    private int mPhoneStatus = HFP_INFO_INITIAL;

    public void setPhone(int phone_status, String num) {// default is simple box

        int status = 0;
        switch (phone_status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:
            case HFP_INFO_CONNECTING:
            case HFP_INFO_CONNECTED:
                status = 5;
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

        // if (status != 0) {
        byte[] data2;

        if (num == null) {
            num = " ";
        }

        data2 = new byte[]{(byte) 0xc5, 0x2, 0x0, (byte) status};

        sendDataToCanbox(data2, data2.length);
        Util.doSleep(20);

        try {
            if (num == null) {
                num = "";
            }
            byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

            int num_len = n.length;
            if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
                num_len -= 2;
            }
            int len = num_len + 4;
            // if (len > 31) {
            // len = 31;
            // }
            byte[] data = new byte[len];
            data[0] = (byte) 0xCA;

            data[1] = (byte) (len - 2);
            data[2] = 0x4;
            data[3] = 0x10;
            for (int i = 0; i < num_len && i < (len - 4); ++i) {
                Log.d("Accord2013Simple", i + ":" + n.length + ":" + num_len);
                data[4 + i] = n[i + (n.length - num_len)];
            }
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("Accord2013Simple", "sendId3" + e);
        }
        if (mPhoneStatus > HFP_INFO_CONNECTED && phone_status == HFP_INFO_CONNECTED) {
            if (mData != null) {

                sendDataToCanbox(mData, mData.length);
            }
        }

        mPhoneStatus = phone_status;
    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
    }

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

        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();

        byte[] buf = new byte[]{(byte) 0xa6, 0x07, y, mon, d, h, m, s, 0};
        // sendDataToCanbox(buf, buf.length);
    }

    public void setVolume(int volume) {
        if (volume == 0) {
            volume |= 0x80;
        }
        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

    private final static int HIDE_RADAR = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_RADAR) {
                RadarManager.stop();
            }
            super.handleMessage(msg);
        }
    };


    //
    // public void setPhone(int status, String num) {// default is simple box
    // switch (status) {
    // case HFP_INFO_INITIAL:
    // case HFP_INFO_READY:
    // case HFP_INFO_CONNECTING:
    // case HFP_INFO_CONNECTED:
    // status = 0;
    // break;
    // case HFP_INFO_CALLED:
    // status = 2;
    // break;
    // case HFP_INFO_INCOMING:
    // status = 1;
    // break;
    // case HFP_INFO_CALLING:
    // status = 4;
    // break;
    // }
    // byte[] data = new byte[4];// {(byte)0xc5, 0x1, (byte)status};
    // data[0] = (byte) 0xc5;
    // data[1] = (byte) (2);
    // data[2] = (byte) 0;
    // data[3] = (byte) status;
    //
    // sendDataToCanbox(data, data.length);
    //
    // if (num == null) {
    // num = " ";
    // }
    // byte[] n = num.getBytes();
    // data = new byte[n.length + 4];// {(byte)0xc5, 0x1, (byte)status};
    // data[0] = (byte) 0xcA;
    // data[1] = (byte) (n.length + 2);
    // data[2] = (byte) 1;
    // data[3] = (byte) 3;
    // byteArrayCopy(data, n, 4, 0, n.length);
    // Util.doSleep(100);
    // sendDataToCanbox(data, data.length);
    //
    // }

    public int getUpdateTime() {
        return 60000;
    }
}
