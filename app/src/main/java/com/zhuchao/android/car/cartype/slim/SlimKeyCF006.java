package com.zhuchao.android.car.cartype.slim;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.common.util.AppConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.fbase.ByteUtils;
import com.zhuchao.android.fbase.MMLog;

import java.util.Date;
import java.util.Objects;

public class SlimKeyCF006 extends Canbox {
    private final String TAG = "SlimKeyCF006";

    public SlimKeyCF006() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x4, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x06, 0x05, 0x02, 0x01});
        ///sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x10, 0x1});
        MMLog.d(TAG, "NEW SlimKeyCF006 CAN BOX.");
    }

    private final static byte[][] KEYS_WHEEL_NORMAL = {{0x20, KEY_NUM_0}, {0x21, KEY_NUM_1}, {0x22, KEY_NUM_2}, {0x23, KEY_NUM_3}, {0x24, KEY_NUM_4}, {0x25, KEY_NUM_5}, {0x26, KEY_NUM_6}, {0x27, KEY_NUM_7}, {0x28, KEY_NUM_8}, {0x29, KEY_NUM_9}, {0x2a, KEY_NUM_X}, {0x2b, KEY_NUM_J}, {0x33, KEY_FM}, {0x34, KEY_AUX}, {0x35, KEY_DVD}, {0x36, KEY_AUX}, {0x37, KEY_HOME}, {0x38, KEY_EQ}, {0x39, KEY_BT}, {0x3d, MyCmd.Keycode.TIME_SETTING}, {0x3f, KEY_POWER}, {0x48, KEY_PLAYPAUSE}, {0x49, MyCmd.Keycode.KEY_TURN_D}, {0x4a, MyCmd.Keycode.KEY_TURN_A}, {0x4b, KEY_PREVIOUSSONG}, {0x4c, KEY_NEXTSONG}, {0x52, MyCmd.Keycode.MULT_PREV_AND_RECEIVE}, {0x53, MyCmd.Keycode.MULT_NEXT_AND_HANG}, {0x54, KEY_EJECT}, {0x56, MyCmd.Keycode.RDS_TA_SWITCH}, {0x57, KEY_GPS}, {0x59, KEY_EQ}, {0x5a, KEY_MUTE},};

    private final byte[][] KEYS_WHEEL = KEYS_WHEEL_NORMAL;

    byte[] airData = new byte[10];
    private byte mOutDoorTempUnit;

    private void parseWheelKey(byte[] data, int len) {
        if (doKeyStudy(data[2], data[3])) {
            return;
        }

        switch (data[2]) {
            case 0x0:
                doKey(0, 0);
                break;
            case 0x1:
                doKey(AK_KEYPAD_VOLUME_A, data[3]); // vol+
                break;
            case 0x2:
                doKey(AK_KEYPAD_VOLUME_D, data[3]);// vol-
                break;
            case 0x3:
                doKey(MyCmd.Keycode.MULT_NEXT_AND_HANG, data[3]);
                break;
            case 0x4:
                doKey(MyCmd.Keycode.MULT_PREV_AND_RECEIVE, data[3]);
                break;
            case 0x5:
            case 0x09:
            case 0x0A:
                doKey(KEY_BT, data[3]);
                break;
            case 0x6:
                doKey(AK_KEYPAD_MUTE_FAKE, data[3]);// mute
                break;
            case 0x7:
                doKey(KEY_MODE, data[3]);
                break;
            case 0xe:
                doKey(KEY_PREVIOUSSONG, data[3]);
                break;
            case 0xf:
                doKey(KEY_NEXTSONG, data[3]);
                break;
            case 0x10:
                doKey(MyCmd.Keycode.KEY_TURN_D, data[3]);
                break;
            case 0x11:
                doKey(MyCmd.Keycode.KEY_TURN_A, data[3]);
                break;
            case 0x12:
                doKey(KEY_PLAYPAUSE, data[3]);
                break;
            default:
                byte key = 0;
                for (byte[] bytes : KEYS_WHEEL) {
                    if (bytes[0] == data[2]) {
                        key = bytes[1];
                        break;
                    }
                }

                if (key != 0) {
                    doKey(key, data[3]);
                    if (data[2] == 0x60 || data[2] == 0x61 || data[2] == (byte) 0xf1 || data[2] == (byte) 0xf0) {
                        Util.doSleep(1);
                        doKey(0, 0);
                    }
                } else {
                    if (data[3] == 0) {
                        doKey(0, 0);
                    }
                }
                break;
        }
    }

    public void parseACInfo(byte[] data) {
        ///if ((data[3] == 0x01) && (data[4] == 0x01)) //显示与影藏
        airData[0] = (byte) (airData[0] | (0x80));
        ///else
        ///airData[0] = (byte) (airData[0] & (0x7F));

        if ((data[3] == 0x05) && (data[4] == 0x01)) //AC
        {
            airData[0] = (byte) (airData[0] | (0x40));
            airData[0] = (byte) (airData[0] | (0x10));
        }
        if ((data[3] == 0x05) && (data[4] == 0x00)) //AC
        {
            airData[0] = (byte) (airData[0] & (0xBF));
        }

        if ((data[3] == 0x06) && (data[4] == 0x01)) //AC MAX
            airData[4] = (byte) (airData[4] | (0x04));

        if ((data[3] == 0x06) && (data[4] == 0x00)) //AC MAX
            airData[4] = (byte) (airData[4] & (0xFB));

        if (data[4] == 0x02) //外循环
            airData[0] = (byte) (airData[0] | (0x20));
        else airData[0] = (byte) (airData[0] & (0xDF));

        //        super.parseACInfo(airData);
        boolean isAirActivity = "com.canboxsetting/com.canboxsetting.CanAirControlActivity".equals(AppConfig.getTopActivity());
        if (isAirActivity) {
            Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
            i.putExtra("buf", data);
            i.putExtra("bufStr", ByteUtils.BuffToHexStr(data));
            i.putExtra(MyCmd.EXTRA_COMMON_CMD, "ac");
            mContext.sendBroadcast(i);
        } else {
            ///Intent it = new Intent(Intent.ACTION_VIEW);
            ///it.setClassName("com.canboxsetting", "com.canboxsetting.CanAirControlActivity");
            ///it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            ///mContext.startActivity(it);
            super.parseACInfo(airData);
            getHandler("CanService").postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ("com.canboxsetting/com.canboxsetting.CanAirControlActivity".equals(AppConfig.getTopActivity())) {
                        Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
                        i.putExtra("buf", data);
                        i.putExtra("bufStr", ByteUtils.BuffToHexStr(data));
                        i.putExtra(MyCmd.EXTRA_COMMON_CMD, "ac");
                        mContext.sendBroadcast(i);
                    }
                }
            }, 300);
        }
        //MMLog.d(TAG, "sendCanboxAir buf = " + ByteUtils.BuffToHexStr(data) + "   isAirActivity = " + isAirActivity);
    }

    public void updateOutDoorTemp(int temp) {
        if ((temp < -40) || (temp > 86)) {
            return;
        }

        int t = temp;

        if (CarUtil.mTempUnit == 2) {
            mOutDoorTempUnit |= 0x40;
        } else if (CarUtil.mTempUnit == 1) {
            mOutDoorTempUnit = 0;
        }

        String unit = mContext.getResources().getString(R.string.temp_unic_centigrade);
        if ((mOutDoorTempUnit & 0x40) != 0) {
            unit = mContext.getResources().getString(R.string.temp_unic_fahrenheit);
            t = (t * 18 + 320) / 10;
        }
        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, t + unit);
    }

    private byte getRadarData(byte i) {
        byte data = 0;
        if (i == 0x1) {
            data = 1;
        } else if (i >= 0x2 && i <= 0x3) {
            data = 2;
        } else if (i >= 0x4 && i <= 0x5) {
            data = 3;
        } else if (i >= 0x6 && i <= 0x7) {
            data = 4;
        } else if (i >= 0x8 && i <= 0x9) {
            data = 5;
        } else if (i >= 0xa && i <= 0xb) {
            data = 6;
        } else if (i >= 0xc && i <= 0xd) {
            data = 7;
        } else if (i >= 0xe && i <= 0xf) {
            data = 8;
        } else if (i >= 0x10 && i <= 0x11) {
            data = 9;
        } else if (i >= 0x12 && i <= 0x13) {
            data = 10;
        } else if (i >= 0x14 && i <= 0x15) {
            data = 11;
        } else if (i >= 0x16 && i <= 0x17) {
            data = 13;
        } else if (i >= 0x18 && i <= 0x19) {
            data = 14;
        } else if (i >= 0x1a && i <= 0x1b) {
            data = 15;
        } else if (i >= 0x1c && i <= 0x1f) {
            data = 16;
        }
        return data;
    }

    private int mSource = MyCmd.SOURCE_NONE;

    public void setMediaSrc(int source) {
        mSource = source;
    }

    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();
        h = fixTimeHour(h);
        byte m = (byte) curDate.getMinutes();
        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        //byte []buf = new byte[] { (byte) 0x82, 0x06, y, mon, d, h,m, 0 };
        byte[] buf = new byte[]{(byte) 0xC9, 0x06, m, h, d, mon, y, 0};
        sendDataToCanbox(buf, buf.length);
    }

    private final static int HIDE_RADAR = 0;
    private final static int DEALY_SEND_TPMS = 1;

    private final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_RADAR:
                    RadarManager.stop();
                    break;
                case DEALY_SEND_TPMS:
                    try {
                        sendCanboxInfo("com.canboxsetting", (byte[]) (msg.obj));
                    } catch (Exception ignored) {
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void parseCanboxData(byte[] data, int len) {
        byte radar;
        boolean show = false;
        Handler handler = null;

        MMLog.d(TAG, "CanboxData data=" + ByteUtils.BuffToHexStr(data) + "length=" + len);

        //if (data[3] < 0x06) {
        //parseACInfo(data);
        //}
        //else if (data[3] == 0x07)
        {
            //byte[] datas = new byte[]{(byte) 0x90, 0x4, 0x71, 0, 0, 0};
            sendDataToCanbox(data, data.length - 1);
        }
    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        //byte[] data = new byte[]{(byte) 0x81, 0x1, 1};
        //sendDataToCanbox(data, data.length);
        MMLog.d(TAG, "SlimKeyCF006 startConnect()");
    }

    @Override
    public void stopConnect() {
        // TODO Auto-generated method stub
        ///byte[] data = new byte[]{(byte) 0x81, 0x1, 0};
        ///sendDataToCanbox(data, data.length);
        Log.d(TAG, "SlimKeyCF006 stopConnect()");
    }

    @Override
    public void setContext(Context c) {
        super.setContext(c);
    }

    @Override
    public void touchInReverse(int x, int y, int w, int h) {
        super.touchInReverse(x, y, w, h);
        ///MMLog.d(TAG, "touchInReverse x:" + x + " y:" + y + " w:" + w + " h:" + h);
    }

    @Override
    public void touchInReverseEx(int x, int y, int w, int h, int down) {
        super.touchInReverseEx(x, y, w, h, down);
        ///MMLog.d(TAG, "touchInReverseEx x:" + x + " y:" + y + " w:" + w + " h:" + h + " down:" + down);
    }
}
