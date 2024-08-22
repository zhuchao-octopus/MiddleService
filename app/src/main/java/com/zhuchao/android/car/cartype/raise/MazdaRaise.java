package com.zhuchao.android.car.cartype.raise;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Message;
import android.view.Display;

import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;

import java.util.Locale;


public class MazdaRaise extends Canbox {

    public MazdaRaise() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });

    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x4, KEY_NEXTSONG}, {0x3, KEY_PREVIOUSSONG}, {0x6, KEY_SOURCE}, {0x5, KEY_MUTE}, {0x9, KEY_BT_DIAL}, {0xa, KEY_BT_HANG},

            {0x10, MyCmd.Keycode.MULT_SPEECH_AND_BT},

    };

    private final static byte[][] KEYS_PANNEL = {
            {0x1, MyCmd.Keycode.NAVIGATION}, {0x3, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x4, KEY_MUTE},

            {0x5, MyCmd.Keycode.HOME}, {0x6, MyCmd.Keycode.BACK}, {0x7, MyCmd.Keycode.RADIO}, {0x8, MyCmd.Keycode.AUDIO},


            {0xa, KEY_NEXTSONG}, {0x9, KEY_PREVIOUSSONG}, {0xc, KEY_NEXTSONG}, {0xb, KEY_PREVIOUSSONG}, {0x11, KEY_NEXTSONG}, {0x10, KEY_PREVIOUSSONG},

            {0xd, MyCmd.Keycode.KEY_TURN_D}, {0xe, MyCmd.Keycode.KEY_TURN_A}, {0xf, MyCmd.Keycode.PLAY_PAUSE},

    };

    public void udpateLang() {
        int lang = -1;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("zh")) {
                lang = 1;
            } else {
                lang = 0;
            }

        }
        if (lang != -1) {
            byte[] buf = {(byte) 0x83, 0x2, 0x12, (byte) lang};
            sendDataToCanbox(buf, buf.length);
        }
    }

    private boolean isOneKey(byte b) {
        return ((b & 0xff) == 0x9) || ((b & 0xff) == 0xa) || ((b & 0xff) == 0xb) || ((b & 0xff) == 0xc) || ((b & 0xff) == 0xd) || ((b & 0xff) == 0xe) || ((b & 0xff) == 0x2) || ((b & 0xff) == 0x3);
    }

    private void parsePannelKey(byte[] data) {

        if (doKeyStudy(data[2], data[3])) {
            return;
        }

        byte key = 0;
        for (int i = 0; i < KEYS_PANNEL.length; ++i) {
            if (KEYS_PANNEL[i][0] == data[2]) {
                key = KEYS_PANNEL[i][1];
                break;
            }
        }

        if (key != 0) {
            if (isOneKey(data[2])) {
                doKey(key, data[3]);
                Util.doSleep(10);
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

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
        udpateLang();
    }

    public void setMediaSrc(int source) {
        switch (source) {
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
            case MyCmd.SOURCE_RADIO:
                byte[] data = new byte[]{(byte) 0xa1, 0x2, 4, 3};
                sendDataToCanbox(data, data.length);
                break;
        }
    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        super.startConnect();

        byte[] data = new byte[]{(byte) 0xa1, 0x2, 4, 3};
        sendDataToCanbox(data, data.length);
    }

    @Override
    public void stopConnect() {
        // TODO Auto-generated method stub

        //		byte[] data = new byte[] { (byte) 0xa1, 0x2, 4, 0 };
        //		sendDataToCanbox(data, data.length);

        super.stopConnect();
    }

    public void setMediaSrc(int source, byte type, byte[] b) {

    }

    private void sendAVMKey() {
        byte[] data = new byte[]{(byte) 0xc6, 0x2, 0x2, 0x1};
        sendDataToCanbox(data, data.length);
    }

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, KEY_FM}, {0x2, KEY_FM}, {0x3, KEY_FM}, {0x9, KEY_FM},

            {0x4, KEY_DVD}, {0x5, KEY_MEDIA}, {0x6, KEY_MEDIA}, {0xa, KEY_MEDIA},

            {0x7, MyCmd.Keycode.BT_MUSIC}, {0x8, MyCmd.Keycode.AUX_IN},

            {0xe, MyCmd.Keycode.KEY_TV}, {0x10, MyCmd.Keycode.ALL_APP},

            {0x11, KEY_BT_DIAL}, {0x12, KEY_BT_HANG},
    };

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

    private final static int HIDE_RADAR = 0;
    private final static int KEY_VOL = 1;
    private final static int SHOW_VOLUME_STEP = 2;

    private void doKeyStep(int key, int step) {
        mHandler.removeMessages(SHOW_VOLUME_STEP);
        doKey(key, 1);
        doKey(key, 0);
        --step;
        if (step > 0) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW_VOLUME_STEP, key, step), 30);
        }
    }

    private int mVolStep = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_RADAR:
                    RadarManager.stop();
                    break;
                case KEY_VOL:
                    if (mVolStep < 30) {
                        mVolStep++;
                        doKey(mKeyDown);
                        mHandler.sendEmptyMessageDelayed(KEY_VOL, 200);
                    }
                    break;
                case SHOW_VOLUME_STEP:
                    doKeyStep(msg.arg1, msg.arg2);
                    break;
            }
            super.handleMessage(msg);
        }
    };

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

    private int mDoorStatus = 0;

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x21: {
                parseWheelKey(data);
            }

            break;
            case 0x22: {
                parsePannelKey(data);
            }

            break;
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
                sendCanboxInfo("com.canboxsetting", data);
            }
            break;

            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = ((data[3] & 0xff) | ((data[2]) << 8));
                    angle = -(angle * 300 / 540);
                    if (angle == 0) {
                        angle = 5;
                    }
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));
                }
            }
            break;
            case 0x23: {

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

            case 0x24: {

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
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                }
            }
            break;
            case 0x7F: {
                byte[] version = new byte[0x10];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
            case 0x25:
                sendTouch(((data[2] & 0xff) << 8) | (data[3] & 0xff), ((data[4] & 0xff) << 8) | (data[5] & 0xff));
                break;
            case 0x26:
            case 0x40:
            case 0x41:
            case 0x50:
            case 0x51:
            case 0x74:
            case 0x60:
            case 0x61:
            case 0x62:
            case 0x71:
            case 0x72:
            case 0x73: {
                sendCanboxInfo("com.canboxsetting", data);
            }
            break;
            case 0x70:
                returnEQData(data);
                break;

        }
    }

    // byte[] data = new byte[6];
    //
    // public void setMediaMoreInfo(int source, int play, int total, int time,
    // int total_time) {
    // // byte min = (byte) ((time / 60) % 60);
    // // byte sec = (byte) ((time) % 60);
    // // ++play;
    // int s = 4;
    // if (MyCmd.SOURCE_DVD == source) {
    // s = 6;
    // } else {
    // ++play;
    // }
    //
    // data[0] = (byte) 0x82;
    // data[1] = 0x4;
    // data[2] = 4;
    //
    // data[3] = (byte) ((play >> 8) & 0xff);
    // data[4] = (byte) ((play) & 0xff);
    // data[5] = 0;
    //
    // sendDataToCanbox(data, data.length);
    // }
    //
    // public void setMediaSrc(int source, byte type, byte[] b) {
    // // setMediaSrc(0);
    // // int freq = (((b[2]&0xff)<< 8) );
    // // int dd = (b[1] & 0xff);
    // // freq = freq | dd;
    //
    // int freq = (((b[2] & 0xff) << 8)) | (b[1] & 0xff);
    //
    // if (b[0] == 0x10) {
    // b[0] = 2;
    // } else {
    // // b[0] = 0x10;
    // freq = freq / 10;
    // b[0] = 1;
    // }
    //
    // // Util.clearBuf(data);
    // data[0] = (byte) 0x82;
    // data[1] = 0x4;
    // data[2] = b[0];
    //
    // data[3] = (byte) ((freq >> 8) & 0xff);
    // data[4] = (byte) ((freq) & 0xff);
    // data[5] = 0;
    // sendDataToCanbox(data, data.length);
    // }
    //
    // public void setMediaSrc(int source) {
    // byte s = 0;
    //
    // Util.clearBuf(data);
    //
    // switch (source) {
    // case MyCmd.SOURCE_RADIO:
    // s = 1;
    // break;
    // case MyCmd.SOURCE_DVD:
    // s = 6;
    // break;
    // case MyCmd.SOURCE_IPOD:
    // s = 5;
    // break;
    // case MyCmd.SOURCE_MUSIC:
    // case MyCmd.SOURCE_VIDEO:
    // s = 0x04;
    // break;
    // case MyCmd.SOURCE_AUX:
    // s = 0x09;
    // break;
    // case MyCmd.SOURCE_DTV:
    // s = 0x0b;
    // break;
    // case MyCmd.SOURCE_BT:
    // s = 0x0a;
    // break;
    // default:
    // s = 0x0f;
    // break;
    // }
    //
    // data[0] = (byte) 0x82;
    // data[1] = 0x4;
    //
    // data[2] = s;
    // sendDataToCanbox(data, data.length);
    //
    // // mSource = source;
    // }

    // public void updateTime() {
    // Date curDate = new Date(System.currentTimeMillis());
    // byte h = (byte) curDate.getHours();
    //
    // byte ampm = 0;
    // String strTimeFormat = Settings.System.getString(
    // mContext.getContentResolver(),
    // android.provider.Settings.System.TIME_12_24);
    //
    // byte format;
    // if ("12".equals(strTimeFormat)) {
    // if (h >= 12) {
    // ampm |= 0x80;
    // }
    //
    // ampm |= 0x40;
    //
    // if (h > 12) {
    // h -= 12;
    // } else if (h == 0) {
    // h = 12;
    // }
    //
    // h |= 0x80;
    // }
    //
    // byte m = (byte) curDate.getMinutes();
    //
    // byte[] buf = new byte[] { (byte) 0x83, 0x02, h, m };
    // sendDataToCanbox(buf, buf.length);
    // }

    protected void doKey(int value, int status) { // value 0 -> key up

        // Log.d("Mazda3", "doKey:" + value);
        // if (CarUtil.getChangeKey() == 1) {
        value = changeKey(value);
        // }

        switch (status) {
            case 0:
                mHandler.removeMessages(KEY_VOL);
                if (mKeyDown != 0) {
                    if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
                        if (mKeyDown == AK_KEYPAD_VOLUME_A || mKeyDown == AK_KEYPAD_VOLUME_D) {
                            mKeyDown = 0;
                        } else {
                            int ret = getLongKey(value);
                            if (ret != 0) {
                                mKeyDown = ret;
                            }
                        }
                    }
                    if (mKeyDown != 0) {
                        doKey(mKeyDown);
                    }
                    mKeyDown = 0;
                    longClick = false;
                }
                break;
            case 1:
                mKeyDown = value;
                mClickTime = System.currentTimeMillis();
                longClick = false;

                mHandler.removeMessages(KEY_VOL);
                if (value == AK_KEYPAD_VOLUME_A || value == AK_KEYPAD_VOLUME_D) {
                    mVolStep = 0;
                    mHandler.sendEmptyMessageDelayed(KEY_VOL, LONG_CLICK_TIME);
                }
                break;
            // case 2:
            // if (value == AK_KEYPAD_VOLUME_A || value == AK_KEYPAD_VOLUME_D) {
            // doKey(value);
            // mKeyDown = 0;
            // } else {
            // if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
            // if (mKeyDown != 0) {
            // longClick = true;
            // int ret = getLongKey(value);
            // if (ret != 0) {
            // doKey(ret);
            // mKeyDown = 0;
            // }
            // }
            // }
            // }
            // break;
        }

    }

    private final static int TOUCH_MAX = 0x400;
    private int widthScreen = 0;
    private int heightScreen = 0;

    private void sendTouch(int x, int y) {
        if (mContext != null) {
            int i = SettingProperties.getIntProperty(mContext, SettingProperties.KEY_CANBOX_TOUCH_PANNEL);
            if (i == 0) {
                if (widthScreen == 0 || heightScreen == 0) {

                    DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
                    Display[] display = displayManager.getDisplays();
                    if (display.length > 0) {
                        widthScreen = display[0].getWidth();
                        heightScreen = display[0].getHeight();
                    }

                } else {
                    x = x * widthScreen / TOUCH_MAX;
                    y = y * widthScreen / TOUCH_MAX;
                    GlobalDefinition.sendInputTap(x, y);
                }
            }
        }
    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (63 << 16) | (17 << 8) | 13;

            byte[] buf = new byte[]{(byte) 0x90, 0x2, 0x70, 0};
            sendDataToCanbox(buf, buf.length);
        } else {
            byte[] buf = new byte[]{(byte) 0xa0, 0x2, 0x0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    buf[2] = 2;
                    buf[3] = getEQSendData(mEQBuf[0], (byte) data);
                    break;
                case EQ_CMD_SET_LOW:
                    buf[2] = 1;
                    buf[3] = getEQSendData(mEQBuf[2], (byte) data);
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    buf[2] = 3;
                    buf[3] = getEQSendData(mEQBuf[3], (byte) data);
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    buf[2] = 4;
                    buf[3] = getEQSendData(mEQBuf[4], (byte) data);
                    break;
                case EQ_CMD_SET_VOLUME:
                    buf[2] = 6;
                    buf[3] = getEQSendData(mEQBuf[5], (byte) data);
                    break;
                default:
                    return 0;
            }

            sendDataToCanbox(buf, buf.length);
        }
        return ret;
    }

    private byte getEQSendData(byte old_v, byte new_v) {
        int step = new_v - old_v;
        if (step < 0) {
            step = (0x80) | (-step);
        }
        return (byte) step;
    }


    byte[] mEQBuf = new byte[6];

    private void returnEQData(byte[] buf) {
        //		byte[] data = new byte[5];
        mEQBuf[0] = buf[3];
        mEQBuf[2] = buf[2];
        mEQBuf[3] = buf[4];
        mEQBuf[4] = buf[5];
        mEQBuf[5] = buf[7];
        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQBuf);
    }

    public int getUpdateTime() {
        return 60000;
    }
}
