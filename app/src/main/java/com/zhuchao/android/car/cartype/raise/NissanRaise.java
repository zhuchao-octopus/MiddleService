package com.zhuchao.android.car.cartype.raise;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.view.Nissian360ButtonView;

import java.util.Date;
import java.util.Locale;

public class NissanRaise extends Canbox {

    private final static byte[][] KEYS_WHEEL2 = {{0x1, MyCmd.Keycode.KEY_AM}, {0x2, MyCmd.Keycode.KEY_FM}, {0x3, MyCmd.Keycode.KEY_FM}, {0x9, KEY_FM},

            {0x4, KEY_DVD}, {0x5, KEY_MEDIA}, {0x6, KEY_MEDIA}, {0xa, KEY_MEDIA},

            {0x7, MyCmd.Keycode.BT_MUSIC}, {0x8, MyCmd.Keycode.AUX_IN},

            {0xe, MyCmd.Keycode.KEY_TV}, {0x10, MyCmd.Keycode.ALL_APP},

            {0x11, KEY_BT_DIAL}, {0x12, KEY_BT_HANG},

    };
    private final static byte[][] KEYS_WHEEL3 = {
            // { 0x1, MyCmd.Keycode.KEY_AM },
            // { 0x2, MyCmd.Keycode.KEY_FM },
            {0x1, KEY_BT_DIAL}, {0x2, KEY_BT_HANG}, {0x3, KEY_BT_HANG},

    };
    private final static int HIDE_RADAR = 0;
    private final static int SET_EQ_STEP = 1;
    private final byte[][] KEYS_WHEEL = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x4, KEY_NEXTSONG}, {0x3, KEY_PREVIOUSSONG}, {0x7, KEY_SOURCE}, {0x9, KEY_BT_DIAL}, {0xA, KEY_BT_HANG}, {0x15, KEY_BACK}, {0x16, KEY_PLAYPAUSE}, {(byte) 0x87, KEY_POWER}, {(byte) 0x72, KEY_GPS}, {(byte) 0x73, KEY_MENU},

            {(byte) 0x71, KEY_BACK},

            {(byte) 0x74, MyCmd.Keycode.KEY_TURN_A}, {(byte) 0x75, MyCmd.Keycode.KEY_TURN_D}, {0x70, KEY_PLAYPAUSE}, {0x61, KEY_NEXTSONG}, {0x62, KEY_NEXTSONG}, {0x63, KEY_NEXTSONG}, {0x64, KEY_NEXTSONG},

            {0x60, KEY_PREVIOUSSONG}, {0x65, KEY_PREVIOUSSONG}, {0x66, KEY_PREVIOUSSONG}, {0x67, KEY_PREVIOUSSONG},


            {0x24, MyCmd.Keycode.KEY_FM}, {0x26, MyCmd.Keycode.KEY_AM}, {0x25, MyCmd.Keycode.DVD}, {0x27, MyCmd.Keycode.NUMBER1}, {0x28, MyCmd.Keycode.NUMBER2}, {0x29, MyCmd.Keycode.NUMBER3}, {0x2a, MyCmd.Keycode.NUMBER4}, {0x2b, MyCmd.Keycode.NUMBER5}, {0x2c, MyCmd.Keycode.NUMBER6}, {0x2d, MyCmd.Keycode.VOLUME_DOWN}, {0x2e, MyCmd.Keycode.VOLUME_UP}, {0x2f, MyCmd.Keycode.KEY_TURN_D}, {0x30, MyCmd.Keycode.KEY_TURN_A}, {0x31, MyCmd.Keycode.KEY_SEEK_PREV}, {0x32, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x33, MyCmd.Keycode.MUTE},


            {0x40, MyCmd.Keycode.AUX_IN}, {0x42, MyCmd.Keycode.KEYAMS_RPT}, {0x43, MyCmd.Keycode.DVD}, {0x44, MyCmd.Keycode.HOME}, {0x45, MyCmd.Keycode.NAVIGATION}, {0x47, MyCmd.Keycode.NAVIGATION}, {0x4a, MyCmd.Keycode.BRIGHTNESS}, {0x4e, MyCmd.Keycode.BRIGHTNESS}, {0x4d, MyCmd.Keycode.SETUP}, {0x4f, MyCmd.Keycode.SPEECH}, {0x50, MyCmd.Keycode.AUDIO}, {0x51, MyCmd.Keycode.EJECT}, {0x52, MyCmd.Keycode.NAVIGATION},


    };
    byte[] data;
    String mName = null;
    String mArtist = null;
    String mAlbum = null;
    byte[] mEqData = new byte[6];
    Nissian360ButtonView mNissian360ButtonView;
    byte[] mEQBuf = new byte[]{5, 0, 5, 5, 5, 0};
    private int mKey;
    private int mDoorStatus;
    private int mPhoneStatus = HFP_INFO_INITIAL;

    public NissanRaise() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});
        updateCanboxKeySettings();

        mIdAC = 0x21;
        setVoiceSupportRaise();
        changeKeyType();
    }

    public void updateCanboxKeySettings() {
        if (CarUtil.getCarEQ() == 1) {
            CarUtil.mIsNeedSendEQ = true;
            CarUtil.setMcuEQZoneUsed(1);
        }
    }

    private void changeKeyType() {
        int type = 0;
        if (CarUtil.getModelId() == 2 || CarUtil.getModelId() == 5 || CarUtil.getModelId() == 22 || CarUtil.getModelId() == 52) {
            type = 1;
        }

        if (type == 1) {
            for (int i = 0; i < KEYS_WHEEL.length; ++i) {
                if (KEYS_WHEEL[i][0] == 0x9) {
                    KEYS_WHEEL[i][1] = MyCmd.Keycode.SPEECH;
                } else if (KEYS_WHEEL[i][0] == 0xa) {
                    KEYS_WHEEL[i][1] = MyCmd.Keycode.BT;
                }
            }
        }
    }

    public int getLongKey(int key) {
        int ret = 0;
        switch (key) {
            case MyCmd.Keycode.NEXT:
                if (CarUtil.getKeyType() == 6) {
                    mKeyDown = 0;
                    sendAVMKey();
                } else {
                    ret = MyCmd.Keycode.KEY_SEEK_NEXT;
                }
                break;
            case MyCmd.Keycode.PREVIOUS:
                if (CarUtil.getKeyType() == 5) {
                    mKeyDown = 0;
                    sendAVMKey();
                } else {
                    ret = MyCmd.Keycode.KEY_SEEK_PREV;
                }
                break;
            case MyCmd.Keycode.MODLE:
                // ret = MyCmd.Keycode.KEY_MIC;
                if (CarUtil.getKeyType() == 3) {
                    mKeyDown = 0;
                    sendAVMKey();
                }
                break;
            case KEY_BT_DIAL:
            case KEY_BT_HANG:
                // ret = MyCmd.Keycode.KEY_MIC;
                if (CarUtil.getKeyType() == 4) {
                    mKeyDown = 0;
                    sendAVMKey();
                }
                break;
        }
        return ret;
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

        if (data[3] == 0) {
            if (mKey != 0) {

                if (CarUtil.getKeyType() == 1 && mKey == MyCmd.Keycode.PLAY_PAUSE) {
                    sendAVMKey();
                } else {
                    if (CarUtil.getKeyType() == 2) {
                        if (mKey == KEY_BT_HANG) {
                            mKey = KEY_BT;
                        }
                    }
                    doKey(mKey, data[3]);
                }
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
        udpateLang();

        if (isShowButton()) {
            mNissian360ButtonView = Nissian360ButtonView.getInstance(mContext);
            mNissian360ButtonView.showMicButton();
        } else {
            mNissian360ButtonView = null;
        }

    }

    public void show360Button() {
        if (isShowButton()) {
            hide360Button();
            if (CarUtil.getCarType2() == 0) {
                if (mNissian360ButtonView != null) {
                    mNissian360ButtonView.showMicButton();
                }
            }
        }
    }    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_RADAR:
                    RadarManager.stop();
                    break;
                case SET_EQ_STEP:
                    sendEQCmd((byte) msg.arg1, msg.arg2);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void hide360Button() {
        if (mNissian360ButtonView != null) {
            mNissian360ButtonView.hideMicButton();
        }
    }

    public void startConnect() {
        super.startConnect();
        if (mContext != null) {
            if (isShowButton()) {
                mNissian360ButtonView = Nissian360ButtonView.getInstance(mContext);
                mNissian360ButtonView.showMicButton();
            } else {
                mNissian360ButtonView = null;
            }
        }
    }

    public void sendAVMKey() {

        byte[] data;// = new byte[] { (byte) 0xc6, 0x2, 0x2, 0x1 };

        data = new byte[]{(byte) 0xc7, 0x1, 0x1};

        sendDataToCanbox(data, data.length);
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

    private void parseWheelKey3(byte[] data) {
        if (doKeyStudy(2, data[2], 1)) {
            doKeyStudy(2, data[2], 0);
            return;
        }

        byte key = 0;
        for (int i = 0; i < KEYS_WHEEL3.length; ++i) {
            if (KEYS_WHEEL3[i][0] == data[2]) {
                key = KEYS_WHEEL3[i][1];
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

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data);
            }

            break;
            case 0x40: {
                parseWheelKey2(data);
            }
            break;
            case 0x50: {
                parseWheelKey3(data);
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
            case 0x28: {
                int door = (data[2] & 0xfe);
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
            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = ((data[3] & 0xff) | ((data[2] & 0x7f) << 8));
                    angle = ((angle * 3000) / 5400);

                    if ((data[2] & 0x80) != 0) {
                        angle = -angle;
                    }
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
            case 0x60:
                doCarTimeSet(data);
                break;
            case (byte) 0x94:
                byte[] version = new byte[]{data[2], data[3], data[4], data[5]};
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_NISSIAN_UI_DATA, version));
                }
                break;
            case 0x27:
            case 0x68:
            case 0x6a:
                sendCanboxInfo("com.canboxsetting", data);
                returnDriveData(data);
                break;
            case (byte) 0x93:
                returnEQData(data);
            case (byte) 0x95:
            case 0x3:
            case 0x4:
            case 0x5:
            case 0x6:
            case 0x7:
            case 0x8:
                sendCanboxInfo("com.canboxsetting", data);
                break;
            default:
                super.parseCanboxData(data, len);
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

    // public void setPhone(int status, String num) {
    // sendId3((byte)0x1, num);
    // }

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

    public void updateTime() {
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        byte format;
        if ("12".equals(strTimeFormat)) {
            //			if (h >= 12) {
            //				ampm |= 0x80;
            //			}

            //			ampm |= 0x40;

            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }

            //			h |= 0x80;
        } else {
            ampm = 1;
        }

        byte m = (byte) curDate.getMinutes();

        byte[] buf = new byte[]{(byte) 0xc8, 0x03, m, h, ampm};
        sendDataToCanbox(buf, buf.length);
    }

    public void udpateLang() {
        int lang = -1;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("zh")) {
                lang = 0;
            } else {
                lang = 1;
            }

        }
        if (lang != -1) {
            byte[] buf = {(byte) 0x83, 0x2, 0x31, (byte) lang};
            sendDataToCanbox(buf, buf.length);
        }
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

    private void sendEQ(byte cmd, byte param) {
        byte[] data = new byte[]{(byte) 0x84, 0x2, cmd, param};
        sendDataToCanbox(data, data.length);
    }

    public void sendEqToCanbox(byte[] eq) {
        if (eq != null && eq.length >= 11) {

            byte[] data = new byte[]{(byte) 0x84, 0x2, 0, 0};

            byte[] eq2 = new byte[6];

            eq2[0] = eq[0];
            eq2[1] = eq[1];

            eq2[3] = (byte) (eq[2] + eq[3] + eq[4]);
            eq2[4] = (byte) (eq[5] + eq[6] + eq[7]);
            eq2[5] = (byte) (eq[8] + eq[9] + eq[10]);
            switch (eq[12]) {
                case 2:
                    eq2[2] = 3;

                    break;
                case 3:
                    eq2[2] = 1;

                    break;
                case 5:
                    eq2[2] = 0;

                    break;
                case 4:
                    eq2[2] = 2;

                    break;
                default:
                    eq2[2] = 4;
                    break;
            }

            byte param;

            if (eq2[0] != mEqData[0]) {
                param = (eq2[0]);
                sendEQ((byte) 1, param);
                Util.doSleep(5);
            }
            if (eq2[1] != mEqData[1]) {
                param = (eq2[1]);
                sendEQ((byte) 2, param);
                Util.doSleep(5);
            }
            // if (eq2[2] != mEqData[2]) {
            // param = eq2[2];
            // sendEQ((byte) 3, param);
            // Util.doSleep(5);
            // }

            if (eq2[3] != mEqData[3]) {
                param = (byte) ((eq2[3] * 11) / 45);
                sendEQ((byte) 4, param);
                Util.doSleep(5);
            }
            if (eq2[4] != mEqData[4]) {
                param = (byte) ((eq2[4] * 11) / 45);
                sendEQ((byte) 6, param);
                Util.doSleep(5);
            }
            if (eq2[5] != mEqData[5]) {
                param = (byte) ((eq2[5] * 11) / 45);
                sendEQ((byte) 5, param);
                Util.doSleep(5);
            }
            mEqData = eq2;
        }
    }

    private void returnDriveData(byte[] buf) {
        if (mRequestDriveData > 0) {
            boolean update = true;
            if (buf[0] == 0x24) {
                if ((buf[3] & 0x8) != 0) {
                    mDriveData[10] = 3;
                } else {
                    if ((buf[3] & 0x1) != 0) {
                        mDriveData[10] = 1;
                    } else {
                        mDriveData[10] = 4;
                    }
                }

                if ((buf[3] & 0x4) != 0) {
                    mDriveData[14] = 1;
                } else {
                    mDriveData[14] = 0;
                }
            } else {
                update = false;
            }
            if (update) {
                returnDriveData();
            }
        }
    }

    private void doCarTimeSet(byte[] data) {
        //		switch (data[2]) {
        //		case 0x11:
        //		case 0x12:
        //		case 0x13:
        //		case 0x14:
        //		case 0x15: {
        //			UtilSystem.doRunActivity(mContext, "com.android.deskclock",
        //					"com.android.deskclock.DeskClock");
        //
        //			Calendar c = Calendar.getInstance();
        //
        //			c.set(Calendar.HOUR_OF_DAY, hour);
        //			c.set(Calendar.MINUTE, minute);
        //			long when = c.getTimeInMillis();
        //
        //			if (when / 1000 < Integer.MAX_VALUE) {
        //				SystemClock.setCurrentTimeMillis(when);
        //			}
        //
        //			long now = Calendar.getInstance().getTimeInMillis();
        //
        //			switch (data[2]) {
        //			case 0x11:
        //			case 0x12:
        //			case 0x13:
        //			case 0x14:
        //			case 0x15:
        //
        //				break;
        //			}
        //			break;
        //		}
        //		}
    }

    //	private final static int TOUCH_MAX = 0x400;
    //	private int widthScreen = 0;
    //	private int heightScreen = 0;
    //
    //	private void sendTouch(int x, int y) {
    //		if (mContext != null) {
    //			int i = SettingProperties.getIntProperty(mContext,
    //					SettingProperties.KEY_CANBOX_TOUCH_PANNEL);
    //			if (i == 0) {
    //				if (widthScreen == 0 || heightScreen == 0) {
    //
    //					DisplayManager displayManager = (DisplayManager) mContext
    //							.getSystemService(Context.DISPLAY_SERVICE);
    //					Display[] display = displayManager.getDisplays();
    //					if (display.length > 0) {
    //						widthScreen = display[0].getWidth();
    //						heightScreen = display[0].getHeight();
    //					}
    //
    //				} else {
    //					x = x * widthScreen / TOUCH_MAX;
    //					y = y * widthScreen / TOUCH_MAX;
    //					GlobalDef.sendInputTap(x, y);
    //				}
    //			}
    //		}
    //	}

    private boolean isShowButton() {

        boolean ret = false;
        if (CarUtil.getKeyType() != 0 && CarUtil.getKeyType() != 2) {
            if (CarUtil.getCarType2() == 0) {
                ret = true;
            }
        }

        return ret;
    }

    private void sendEQCmd(byte id, int step) {
        byte data;
        if (step == 0) {
            return;
        } else if (step > 0) {
            data = 0x21;
            step--;
        } else {
            data = 0x31;
            step++;
        }
        byte[] buf = new byte[]{(byte) 0x83, 0x2, id, data};
        sendDataToCanbox(buf, buf.length);

        mHandler.removeMessages(SET_EQ_STEP);
        if (step != 0) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(SET_EQ_STEP, id, step), 200);
        }
    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (40 << 16) | (11 << 8) | 11;
        } else {
            byte id;
            int step;
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    id = 0x23;
                    step = data - mEQBuf[0];
                    break;
                case EQ_CMD_SET_LOW:
                    id = 0x22;
                    step = data - mEQBuf[2];
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    id = 0x25;
                    step = data - mEQBuf[3];
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    id = 0x24;
                    step = data - mEQBuf[4];
                    break;
                case EQ_CMD_SET_VOLUME:
                    id = 0x21;
                    step = data - mEQBuf[5];
                    break;
                default:
                    return 0;
            }

            sendEQCmd(id, step);

        }
        return ret;
    }

    private void returnEQData(byte[] buf) {
        mEQBuf[0] = (byte) (buf[4] + 5);
        mEQBuf[2] = (byte) (buf[3] + 5);
        mEQBuf[3] = (byte) (buf[6] + 5);
        mEQBuf[4] = (byte) (buf[5] + 5);
        mEQBuf[5] = buf[2];
        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQBuf);
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        data = (byte) (((data & 0xfc) >> 2));

        if ((data & 0xff) == 0) {
            data = (byte) 0xfa;
        } else if ((data & 0xff) >= 1 && (data & 0xff) <= 0xf) {
            data = 0;
        } else if ((data & 0xff) >= 0x31 && (data & 0xff) <= 0x3f) {
            data = (byte) 0xff;
        } else {
            data = (byte) (32 + (((data & 0xff) - 16)));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {


        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0x10) | ((data[2] & 0x04) << 3) | ((data[2] & 0x02) << 5) | ((data[2] & 0x01) << 7) | ((data[3] & 0x04) >> 1) | ((data[3] & 0x02) >> 1) | ((data[3] & 0x01) << 2));


        airData[4] = (byte) (((data[3] & 0x08) >> 1));

        switch ((data[2] & 0xf0) >> 5) {
            case 1:
                airData[1] = (byte) (0x40);
                break;
            case 2:
                airData[1] = (byte) (0x60);
                break;
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 4:
                airData[1] = (byte) (0xa0);
                break;
            case 5:
                airData[1] = (byte) (0x80);
                break;
            case 6:
                airData[1] = (byte) (0xc0);
                break;
            case 7:
                airData[1] = (byte) (0xe0);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) ((data[3] & 0xf0) >> 4);


        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public int getUpdateTime() {
        return 60000;
    }




}
