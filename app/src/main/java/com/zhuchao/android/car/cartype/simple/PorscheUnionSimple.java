package com.zhuchao.android.car.cartype.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;


public class PorscheUnionSimple extends Canbox {

    private final static byte[][] KEYS_WHEEL = {

            {0x1, KEY_SOURCE}, {0x2, KEY_NEXTSONG}, {0x3, KEY_PREVIOUSSONG}, {0x4, AK_KEYPAD_VOLUME_A}, {0x5, AK_KEYPAD_VOLUME_D}, {0x6, KEY_MUTE}, {0x7, KEY_BT_DIAL}, {0x8, KEY_BT_HANG}, {0x9, KEY_GPS}, {0xa, KEY_FM}

    };
    private final static byte[][] KEYS_WHEEL2 = {{0x1, KEY_FM}, {0x2, KEY_FM}, {0x3, KEY_FM}, {0x9, KEY_FM},

            {0x4, KEY_DVD}, {0x5, KEY_MEDIA}, {0x6, KEY_MEDIA}, {0xa, KEY_MEDIA},

            {0x7, MyCmd.Keycode.BT_MUSIC}, {0x8, MyCmd.Keycode.AUX_IN},

            {0xe, MyCmd.Keycode.KEY_TV}, {0x10, MyCmd.Keycode.ALL_APP},

            {0x11, KEY_BT_DIAL}, {0x12, KEY_BT_HANG},};
    byte[] data = new byte[6];

    public PorscheUnionSimple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});

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
            if (CarUtil.getKeyType() == 1 && data[2] == 0x16) {
                if (data[3] == 0) {
                    sendAVMKey();
                }
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

    public void startConnect() {// default is simple box

        super.startConnect();
        mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 2000);
    }

    public void stopConnect() {// default is simple box
        super.stopConnect();
        mHandler.removeMessages(0);
    }    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                startConnect();
            }
            super.handleMessage(msg);
        }
    };

    private void sendAVMKey() {
        byte[] data = new byte[]{(byte) 0xc6, 0x2, 0x2, 0x1};
        sendDataToCanbox(data, data.length);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x1: {
                parseWheelKey(data);
            }

            break;
            case 0x2: {

                int t = (short) ((data[5] & 0xff) | ((data[4] & 0x7f) << 8));
                int temp = t;
                String s = "";
                if ((data[4] & 0x80) != 0) {
                    temp = -t;
                }
                // if (temp >= -58 && temp <= 171) {
                if (data[2] != 1) {

                    s = String.format("%d.%d%s", temp / 10, (temp % 10) >= 0 ? (temp % 10) : -(temp % 10), mContext.getResources().getString(R.string.temp_unic_centigrade));

                } else {
                    s = String.format("%d%s", temp / 10, mContext.getResources().getString(R.string.temp_unic_fahrenheit));
                }

                if (s.length() > 1) {
                    GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
                }


                sendCanboxInfo("com.canboxsetting", data);
            }
            break;
            case 0x5: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = (data[2] & 0xff);

                    if (angle > 0x80) {
                        angle = 0x80 - angle;
                    } else {
                        // angle = - (angle - 0x80);
                    }

                    angle = (angle * 300 / 50);

                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));
                }
            }
            break;
            case 0x71: {
                byte[] version = new byte[9];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
        }
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        //		byte min = (byte) ((time / 60) % 60);
        //		byte sec = (byte) ((time) % 60);
        // ++play;
        int s = 4;
        if (MyCmd.SOURCE_DVD == source) {
            s = 6;
        } else {
            ++play;
        }

        data[0] = (byte) 0x82;
        data[1] = 0x4;
        data[2] = 4;

        data[3] = (byte) ((play >> 8) & 0xff);
        data[4] = (byte) ((play) & 0xff);
        data[5] = 0;

        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        // setMediaSrc(0);
        // int freq = (((b[2]&0xff)<< 8) );
        // int dd = (b[1] & 0xff);
        // freq = freq | dd;

        int freq = (((b[2] & 0xff) << 8)) | (b[1] & 0xff);

        if (b[0] == 0x10) {
            b[0] = 2;
        } else {
            // b[0] = 0x10;
            freq = freq / 10;
            b[0] = 1;
        }

        // Util.clearBuf(data);
        data[0] = (byte) 0x82;
        data[1] = 0x4;
        data[2] = b[0];

        data[3] = (byte) ((freq >> 8) & 0xff);
        data[4] = (byte) ((freq) & 0xff);
        data[5] = 0;
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source) {
        byte s = 0;

        Util.clearBuf(data);

        switch (source) {
            case MyCmd.SOURCE_RADIO:
                s = 1;
                break;
            case MyCmd.SOURCE_DVD:
                s = 6;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 5;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x04;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x09;
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0b;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0a;
                break;
            default:
                s = 0x0f;
                break;
        }

        data[0] = (byte) 0x82;
        data[1] = 0x4;

        data[2] = s;
        sendDataToCanbox(data, data.length);

        // mSource = source;
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

            h |= 0x80;
        }

        byte m = (byte) curDate.getMinutes();

        byte[] buf = new byte[]{(byte) 0x83, 0x02, h, m};
        sendDataToCanbox(buf, buf.length);
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
