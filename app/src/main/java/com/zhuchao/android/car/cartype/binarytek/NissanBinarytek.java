package com.zhuchao.android.car.cartype.binarytek;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;


public class NissanBinarytek extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x4, KEY_NEXTSONG}, {0x3, KEY_PREVIOUSSONG}, {0x7, KEY_SOURCE}, {0x9, KEY_BT_DIAL}, {0xA, KEY_BT_HANG}, {0x15, KEY_BACK}, {0x16, KEY_PLAYPAUSE}, {(byte) 0x87, KEY_POWER},


            {(byte) 0x72, KEY_GPS}, {(byte) 0x73, KEY_HOME},

            {(byte) 0x71, KEY_BACK},

            {(byte) 0x74, AK_KEYPAD_VOLUME_A}, {(byte) 0x75, AK_KEYPAD_VOLUME_D},


            //			{ 0x70, KEY_PLAYPAUSE },
            //			{ 0x61, KEY_NEXTSONG },
            //			{ 0x62, KEY_NEXTSONG },
            //			{ 0x63, KEY_NEXTSONG },
            //			{ 0x64, KEY_NEXTSONG },
            //
            //			{ 0x60, KEY_PREVIOUSSONG },
            //			{ 0x65, KEY_PREVIOUSSONG },
            //			{ 0x66, KEY_PREVIOUSSONG },
            //			{ 0x67, KEY_PREVIOUSSONG },

    };
    private final static byte[][] KEYS_WHEEL_SIMAA_SPECAIL_CAR_APP = {

            //		{ 0x70, KEY_PLAYPAUSE },
            {0x63, KEY_NEXTSONG},

            {0x60, KEY_PREVIOUSSONG},

    };
    private final static byte[][] KEYS_WHEEL_SIMAA_SPECAIL = {

            //		{ 0x70, KEY_ENTER },
            {0x61, KEY_RIGHT}, {0x62, KEY_ENTER}, {0x63, KEY_DOWN}, {0x64, KEY_ENTER},

            {0x60, KEY_UP}, {0x65, KEY_LEFT}, {0x66, KEY_ENTER}, {0x67, KEY_ENTER},

    };
    private final static byte[][] KEYS_WHEEL2 = {{0x1, MyCmd.Keycode.KEY_AM},

            {0x2, MyCmd.Keycode.KEY_FM}, {0x3, MyCmd.Keycode.KEY_FM},

            {0x9, KEY_FM},

            {0x4, KEY_DVD}, {0x5, KEY_MEDIA}, {0x6, KEY_MEDIA}, {0xa, KEY_MEDIA},

            {0x7, MyCmd.Keycode.BT_MUSIC}, {0x8, MyCmd.Keycode.AUX_IN},

            {0xe, MyCmd.Keycode.KEY_TV}, {0x10, MyCmd.Keycode.ALL_APP},

            {0x11, KEY_BT_DIAL}, {0x12, KEY_BT_HANG},

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
    byte[] data;
    String mName = null;
    String mArtist = null;
    String mAlbum = null;
    private int mKey;
    private int mDoorStatus = 0;
    private int mPhoneStatus = HFP_INFO_INITIAL;

    public NissanBinarytek() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});
    }

    private void parseWheelKey(byte[] data) {

        if (doKeyStudy(data[2], data[3])) {
            return;
        }

        byte key = 0;

        if (CarUtil.getCarType() == 2) {
            if (data[2] == 0x9) {
                key = KEY_MIC;
            } else if (data[2] == 0xa) {
                key = KEY_BT;
            }
        }
        if (CarUtil.getCarType() == 1) {
            if (data[2] == 0xa) {
                key = KEY_BT;
            }
        }

        if (key == 0) {
            for (int i = 0; i < KEYS_WHEEL.length; ++i) {
                if (KEYS_WHEEL[i][0] == data[2]) {
                    key = KEYS_WHEEL[i][1];
                    break;
                }
            }

            if (key == 0) {
                byte[][] keys;
                if (GlobalDefinition.isOwerAppControlTop()) {
                    keys = KEYS_WHEEL_SIMAA_SPECAIL_CAR_APP;
                } else {
                    keys = KEYS_WHEEL_SIMAA_SPECAIL;
                }

                for (int i = 0; i < keys.length; ++i) {
                    if (keys[i][0] == data[2]) {
                        key = keys[i][1];
                        break;
                    }
                }
            }
        }


        if (data[3] == 0) {
            if (mKey != 0) {
                doKey(mKey, data[3]);
                mKey = 0;
            }
        } else {
            if (key != 0) {
                doKey(key, data[3]);
                mKey = key;
            }
        }

    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
    }

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

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

    private void parseACInfo(byte[] data, int len) {

        if ((data[3] & 0x10) == 0) {
            return;
        }

        if (data[4] >= 0x7f) {
            data[4] = (byte) 0xff;
        } else if (data[4] > 0) {
            //			data[4] = (byte) ((17.5f + (0.5f * data[4])) * 2);
        }
        if (data[5] >= 0x7f) {
            data[5] = (byte) 0xff;
        } else if (data[5] > 0) {
            //			data[5] = (byte) ((17.5f + (0.5f * data[5])) * 2);
        }

        byte[] airData = new byte[8];
        airData[0] = (byte) ((data[2] & 0xef) | ((data[2] & 0x10) >> 4));
        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);
        airData[4] = (byte) (data[6] & 0xff);
        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
        }
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data);
            }

            case 0x21: {
                parseACInfo(data, len);
            }
            break;
            case 0x24: {
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
            break;
            case 0x2f: {
                parseWheelKey2(data);
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

            case 0x23: // Radar front
            {
                // byteArrayCopy(mRadar, data, 4, 2, 4);

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
            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu
                    // ma

                    int angle = -((a * 3000) / 5696);

                    if (angle > -50 && angle < 50) {
                        angle = 50;
                    }
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
                }
            }
            break;
            case 0x30: {
                int v_len = len - 2;
                byte[] version = new byte[v_len];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
        }
    }

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
            data = new byte[]{(byte) 0xc0, 0x8, s, s2, 0, (byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec};

        } else {
            data = new byte[]{(byte) 0xc0, 0x8, s, s2, 0, 0, (byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), min, sec};
        }

        if (mPhoneStatus < HFP_INFO_CALLED) {

            sendDataToCanbox(data, data.length);
        }
    }

    public void setMediaSrc(int source) {// default is simple box
        byte s;
        byte mediaType = 0;
        switch (source) {
            case 0:
                s = 1;
                // mediaType = 1;
                break;
            case 1:
                s = 2;
                mediaType = 0x10;
                break;
            // case 0x82:
            // case 0x83:
            // break;
            default:
                s = 0x0c;
                mediaType = 0x30;
                break;
        }
        byte[] data = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0};
        if (mediaType != 0) {
            sendDataToCanbox(data, data.length);
        }
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        // setMediaSrc(0);
        // if (b[0] != 0x10) {
        // b[0] += 1;
        // }
        if (b[3] >= 0 & b[3] <= 30) {
            b[3]++;
        } else {
            b[3] = 0;
        }
        data = new byte[]{(byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], b[3], 0, 0};
        sendDataToCanbox(data, data.length);
    }

    // public void setPhone(int status, String num) {
    // sendId3((byte)0x1, num);
    // }

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

            data[0] = (byte) 0xcb;
            data[1] = (byte) (len - 2);
            data[2] = index;
            data[3] = 0x2;
            for (int i = 0; i < num_len && i < (len - 4); ++i) {
                data[4 + i] = n[i + (n.length - num_len)];
            }
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("Nissan2013Simple", "sendId3" + e);
        }
    }

    public void setSongName(String s) {
        sendId3((byte) 0x2, s);
        mName = s;
    }

    public void setSongAritst(String s) {
        sendId3((byte) 0x4, s);
        mArtist = s;
    }

    public void setSongAlbum(String s) {
        sendId3((byte) 0x3, s);
        mAlbum = s;
    }

    public void setPhone(int status, String num) {// default is simple box

        mPhoneStatus = status;

        switch (status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:
            case HFP_INFO_CONNECTING:
            case HFP_INFO_CALLED:
            case HFP_INFO_CONNECTED:
                status = 0;
                break;
            case HFP_INFO_INCOMING:
                status = 1;
                break;
            case HFP_INFO_CALLING:
                status = 2;
                break;
        }

        byte[] data2;

        if (num == null) {
            num = " ";
        }

        byte[] n = num.getBytes();
        data2 = new byte[n.length + 4];// {(byte)0xc5, 0x1, (byte)status};
        data2[0] = (byte) 0xc5;
        data2[1] = ((byte) (n.length + 2));
        data2[2] = (byte) status;
        data2[3] = 0x1;

        int num_len = n.length;
        if (num_len > 31) {
            num_len = 31;
        }
        byteArrayCopy(data2, n, 4, 0, num_len);

        sendDataToCanbox(data2, data2.length);

    }

    public void setVolume(int volume) {

        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
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

    public void updateTime() {
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        byte format;
        if ("12".equals(strTimeFormat)) {
            if (h >= 12) {
                ampm |= 0x80;
            }

            ampm |= 0x40;

            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }

        }

        byte m = (byte) curDate.getMinutes();

        byte[] buf = new byte[]{(byte) 0xc6, 0x04, 0x01, h, m, ampm};
        sendDataToCanbox(buf, buf.length);
    }
}
