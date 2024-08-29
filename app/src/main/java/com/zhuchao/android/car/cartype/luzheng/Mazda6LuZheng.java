package com.zhuchao.android.car.cartype.luzheng;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.manager.McuManager;

import java.util.Locale;

public class Mazda6LuZheng extends Canbox {

    private final static int HIDE_RADAR = 0;
    private final byte mDoorStatus = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_RADAR) {
                RadarManager.stop();
            }
        }
    };
    byte[] airData = new byte[8];

    public Mazda6LuZheng() {


    }

    @Override
    public int getAngleValue2(byte[] data) {

        int angle = (short) ((data[3] & 0xff) | (((data[4] & 0xff)) << 8));

        angle = 0x1e40 - angle;
        return -angle;

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
            case 0x1:
                doKey(MyCmd.Keycode.AS, 1);
                break;
            case 0x3:
                doKey(MyCmd.Keycode.HOME, 1);
                break;
            case 0x5:
                doKey(MyCmd.Keycode.EQ, 1);
                break;
            case 0x7:
                doKey(MyCmd.Keycode.POWER, 1);
                break;
            case 0x2:
                doKey(MyCmd.Keycode.RADIO, 1);
                break;
            case 0x4:
                doKey(MyCmd.Keycode.KEY_RADIO_SCAN, 1);
                break;
            case 0x6:
                doKey(MyCmd.Keycode.EJECT, 1);
                break;
        }
    }

    private void parseACInfo(byte[] data, int len) {

        if (len < 8) {
            return;
        }
        airData[3] = (byte) 0xfa;
        if ((data[5] & 0x80) != 0) {
            // Handler handler = getHandler("CanService");
            // handler.sendMessage(handler.obtainMessage(CANBOX_OUT_DOOR_TEMP,
            // data[1], 0));
            //			airData[2] = (byte) 0xfb;
            return;
        } else {
            if ((data[3] & 0xff) == 0xff) {
                airData[2] = (byte) 0xff;
            } else if ((data[3] & 0xff) == 0) {
                airData[2] = (byte) 0;
            } else {
                airData[2] = (byte) (((data[3] & 0xff) * 2) + ((((data[6] & 0xf)) == 0x05) ? 1 : 0));
            }

            //			if ((data[8] & 0xff) == 0xff) {
            //				airData[3] = (byte) 0xff;
            //			} else if ((data[8] & 0xff) == 0) {
            //				airData[3] = (byte) 0;
            //			} else {
            //				airData[3] = (byte) (((data[8] & 0xff) * 2) + (((data[6] & 0xf) == 0x05) ? 1
            //						: 0));
            //			}
        }

        airData[0] = (byte) (((data[4] & 0x04) << 1) | ((data[4] & 0x01) << 6) | ((data[4] & 0x20) >> 5));
        airData[1] = (byte) (((data[4] & 0x40) << 1) | ((data[4] & 0x10) << 1) | ((data[4] & 0x08) << 3));
        airData[1] |= (data[5] & 0x0F);

        if ((data[4] & 0x80) == 0) {
            airData[0] |= 0x20;
        }

        // airData[3] = 0;
        // airData[4] = (byte) (data[6] & 0xff);
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
                case 0x29: {
                    int angle = getAngleValue2(data);

                    angle = ((angle * 3000) / 0x1630);
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
                case 0x20:
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

    // public void setReverseRadaVol(byte param) {
    // byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x0, param };
    // sendDataToCanbox(data, data.length);
    // }
    //
    // public void setParkCarMode(byte param) {
    // byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x2, param };
    // sendDataToCanbox(data, data.length);
    // }
    //
    // public void requestInfo(byte param) {
    // byte[] data = new byte[] { (byte) 0x90, 0x2, param, 0 };
    // sendDataToCanbox(data, data.length);
    // }
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

    public void setMediaSrc(int source, byte type, byte[] b) {
        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        if (source == MyCmd.SOURCE_RADIO) {
            String s;

            if (b[0] >= 0x10) { // am

                if (freq < 1000) {
                    s = String.format("%d KHz", (freq), Locale.ENGLISH);
                } else {
                    s = String.format("%d KHz", (freq), Locale.ENGLISH);
                }

                type = 4;
            } else {

                if (freq < 10000) {
                    s = String.format("FM %d.%02dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                } else {
                    s = String.format("FM %d.%02dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
                }
                type = 1;
            }

            sendLcdInfo(type, s, true);
        }
    }

    public void sendLcdInfo(byte index, String num, boolean power) {

        try {
            if (num == null) {
                num = "";
            }
            byte[] n = num.getBytes();

            int num_len = n.length;

            if (num_len >= (12)) {
                num_len = (12);
            }
            byte[] data;

            int len = 14;

            data = new byte[len];

            data[0] = (byte) (0xf);
            data[1] = (byte) 0x7;

            System.arraycopy(n, 0, data, 2, num_len);


            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

        }
    }

    @Override
    public void startConnect() {
    }

    @Override
    public void stopConnect() {
    }

    private byte sum(byte[] data, int len) {
        byte sum = 0;
        for (int i = 0; i < len; ++i) {
            sum += data[i];
        }
        return (byte) (sum & 0xFF);
    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple


        byte[] send = new byte[len + 3];
        send[0] = (byte) (len + 2);
        send[1] = (byte) 0xfd;
        send[len + 2] = sum(data, len);
        byteArrayCopy(send, data, 2, 0, len);

        McuManager.sendCanboxData(send);
    }

    public void setMediaSrc(int source) {
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                break;
            case MyCmd.SOURCE_VIDEO:
            case MyCmd.SOURCE_MUSIC:
                sendLcdInfo((byte) 0, "USB", true);
                break;
            default:
                sendLcdInfo((byte) 0, "", true);
                break;
        }
    }

    //	public int getOutTemp(byte[] data) {//
    //		short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
    //		return t;
    //	}

}
