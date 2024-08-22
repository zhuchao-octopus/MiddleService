package com.zhuchao.android.car.cartype.luzheng;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;
import java.util.Locale;


public class LandRoverHaozheng extends Canbox {

    public LandRoverHaozheng() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });

        if (CarUtil.getModelId() == 1) {
            buildCmdRadarFront((byte) 0x5, (byte) 0x1, (byte) 0x1f);
            buildCmdRadarBack((byte) 0x4, (byte) 0x1, (byte) 0x1f);
        } else {
            buildCmdRadarFront((byte) 0x5, (byte) 0x0, (byte) 0xa);
            buildCmdRadarBack((byte) 0x4, (byte) 0x0, (byte) 0xa);
        }


        mIdKey = (byte) 0xa8;
        MAP_KEYS = KEYS_WHEEL2;
    }

    private final static byte[][] KEYS_WHEEL = {

            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG}, {0x5, KEY_MODE}, {0x6, KEY_MUTE}, {0x7, KEY_BT}, {0x8, KEY_BT_DIAL}, {0x9, KEY_BT_HANG},

            {0xb, KEY_NEXTSONG}, {0xa, KEY_PREVIOUSSONG}, {0xd, KEY_PLAYPAUSE}, {0x19, KEY_MIC},


            {0x11, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x12, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x13, MyCmd.Keycode.ROLL_NEXT}, {0x14, MyCmd.Keycode.ROLL_PREV}, {0x16, MyCmd.Keycode.NAVIGATION},
            {0x17, MyCmd.Keycode.AUDIO}, {0x18, MyCmd.Keycode.POWER}, {0x1b, MyCmd.Keycode.HOME}, {0x20, MyCmd.Keycode.SETUP}, {0x21, MyCmd.Keycode.EQ}, {0x22, MyCmd.Keycode.EJECT},


    };

    private final static byte[][] KEYS_WHEEL2 = {

            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x3, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x4, MyCmd.Keycode.RADIO}, {0x5, MyCmd.Keycode.AUDIO}, {0x6, MyCmd.Keycode.BT},
            //		{ 0x7, MyCmd.Keycode. },
            {0x8, MyCmd.Keycode.NAVIGATION}, {0x9, MyCmd.Keycode.KEY_CAR_INFO}, {0xa, MyCmd.Keycode.AS}, {0xb, MyCmd.Keycode.SETUP}, {0xc, MyCmd.Keycode.PLAY_PAUSE}, {0xd, MyCmd.Keycode.ROLL_NEXT},
            {0xe, MyCmd.Keycode.ROLL_PREV},


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


    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x2: {
                parseWheelKey(data);
            }

            break;
            case 0x6: {
                int door = (data[2] & 0xfc);
                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }

            }
            break;

            case 0xa: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = ((data[3] & 0xff) | ((data[4]) << 8));
                    if (data[2] == 0) {
                        angle = -angle;
                    }
                    angle = (angle * 300 / 480);
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));
                }
            }
            break;
            case 0x35:
                int temp = ((data[8] & 0xff) * 5 - 400);
                updateOutDoorTemp(temp);

                sendCanboxInfo("com.canboxsetting", data);
                break;
            case 0x7f: {
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
            case 0x38:
                mUnit = ((data[3] & 0x40) >> 6);
                sendCanboxInfo("com.canboxsetting", data);
                break;
            case 0x37:
                sendCanboxInfo("com.canboxsetting", data);
                break;
            default:
                super.parseCanboxData(data, len);
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
            // if (mUnit != 1){
            float t = temp / 10.0f;
            temp = (int) (((t) * 1.8f + 32) * 10);
            // }
            mUnit = 1;
        } else if (CarUtil.mTempUnit == 1) {
            // if (mUnit == 1){
            // temp = (int)((((float)temp/10.0f)-32)/1.8f)*10;
            // }
            mUnit = 0;
        } else {
            if (mUnit == 1) {
                float t = temp / 10.0f;
                temp = (int) (((t) * 1.8f + 32) * 10);
            }
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

    private int mDoorStatus = 0;

    byte mAmpm = -1;

    public void updateTime() {
        if (mContext == null) {
            return;
        }
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        byte ampm = 0;
        if ("12".equals(strTimeFormat)) {
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
            ampm = 1;
        }

        byte m = (byte) curDate.getMinutes();
        // byte s = (byte) curDate.getSeconds();

        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();

        byte[] buf = new byte[]{(byte) 0xc6, 0x06, 1, y, mon, d, h, m};
        sendDataToCanbox(buf, buf.length);
        if (mAmpm != ampm) {
            Util.doSleep(1);
            mAmpm = ampm;
            buf = new byte[]{(byte) 0xc6, 0x06, 0, ampm, 0, 0, 0, 0};
            sendDataToCanbox(buf, buf.length);
        }
    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
        udpateLang();
    }

    private void sendId3(String num) {

        try {
            if (num == null) {
                num = "";
            }
            byte[] n = num.getBytes();

            int num_len = n.length;
            if (num_len > 16) {
                num_len = 16;
            }
            byte[] data = new byte[18];
            data[0] = (byte) 0xc0;
            data[1] = 16;


            for (int i = 0; i < 16; ++i) {
                if (i < num_len) {
                    data[2 + i] = n[i];
                } else {
                    data[2 + i] = 0;
                }

            }

            sendDataToCanbox(data, data.length);
        } catch (Exception e) {
            Log.d("eef", String.valueOf(e));
        }
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte h = (byte) ((time / 3600));
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);


        byte[] data = new byte[15];
        data[0] = (byte) (data.length - 2);
        data[1] = (byte) 0xe1;
        for (int i = 3; i < 15; ++i) {
            data[i] = 0x20;
        }

        if (MyCmd.SOURCE_DVD == source) {
            data[2] = 7;
        } else {
            data[2] = 0xd;
            ++play;
        }

        String s;

        switch (source) {
            case 1:
                s = "DVD ";
                break;
            case MyCmd.SOURCE_MUSIC:
                s = "Music ";
                break;
            case MyCmd.SOURCE_VIDEO:
                s = "Video ";
                break;
            default:
                s = "";
                break;
        }

        s += play + " ";

        if (h != 0) {
            if (h < 10) {
                s += "0";
            }
            s += h + ":";
        }
        if (min < 10) {
            s += "0";
        }
        s += min + ":";
        if (sec < 10) {
            s += "0";
        }
        s += sec;


        sendId3(s);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        String s;
        if (b[0] != 0x10) {
            s = "FM ";
        } else {
            s = "AM ";
        }
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);

        if (b[0] != 0x10) {
            //			data[2] = 1;

            s += (freq / 100) + ".";
            if ((freq % 100) < 10) {
                s += "0";
            }
            s += (freq % 100);
            s += " MHZ";

            //			int start = 6;
            //			if (freq < 10000) {
            //				start = 7;
            //			}
            //			for (int i = 0; (i + start) < data.length && i < s.length(); ++i) {
            //				data[i + start] = (byte) s.charAt(i);
            //			}
        } else {
            //			data[2] = 4;

            s += freq + " KHZ";
            //			int start = 8;
            //			if (freq < 1000) {
            //				start = 9;
            //			}
            //			for (int i = 0; (i + start) < data.length && i < s.length(); ++i) {
            //				data[i + start] = (byte) s.charAt(i);
            //			}

        }

        sendId3(s);
    }

    //	private int mSource;
    public void setMediaSrc(int source) {// default is simple box
        //		mSource = source;
        if (source == MyCmd.SOURCE_RADIO) {
            return;
        }
        String s;
        switch (source) {
            case 1:
                s = "DVD";
                break;
            case MyCmd.SOURCE_AUX:
                s = "AUX";
                break;
            case MyCmd.SOURCE_DTV:
                s = "TV";
                break;
            case MyCmd.SOURCE_BT:
                s = "BT";
                break;
            default:
                s = "";
                break;
        }

        sendId3(s);
    }


    public void udpateLang() {
        int lang = -1;
        String locale = Locale.getDefault().getLanguage();
        if (locale != null) {
            if (locale.equals("en")) {
                if ("US".equals(Locale.getDefault().getCountry())) {
                    lang = 1;
                } else {
                    lang = 0;
                }
            } else if (locale.equals("it")) {
                lang = 2;
            } else if (locale.equals("ne")) {
                lang = 6;
            } else if (locale.equals("sv")) {
                lang = 7;
            } else if (locale.equals("fr")) {
                lang = 5;
            } else if (locale.equals("de")) {
                lang = 4;
            } else if (locale.equals("es")) {
                lang = 3;
            } else if (locale.equals("da")) {
                lang = 8;
            } else if (locale.equals("nb")) {
                lang = 9;
            } else if (locale.equals("fi")) {
                lang = 0xa;
            } else if (locale.equals("pt")) {
                lang = 0xb;
            } else {
                lang = 0;
            }

        }
        if (lang != -1) {
            byte[] buf = {(byte) 0xc6, 0x2, 0xc, (byte) lang};
            sendDataToCanbox(buf, buf.length);
        }
    }

    public int getUpdateTime() {
        return 60000;
    }
}
