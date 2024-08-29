package com.zhuchao.android.car.cartype.raise;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.common.utils.BroadcastUtil;
import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;


public class KadjarRaise extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, MyCmd.Keycode.ROLL_PREV}, {0x4, MyCmd.Keycode.ROLL_NEXT},

            {0x6, KEY_MUTE}, {0x7, KEY_SOURCE},

            {0x9, KEY_BT_DIAL}, {0xa, KEY_BT_HANG},

            {0x12, KEY_MIC}, {0x15, KEY_BACK}, {0x16, KEY_PLAYPAUSE},

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
    public boolean isAddView = false;
    byte[] data;
    private int mOutTemp = -1;
    private int mDoorStatus = 0;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mView;
    private int mSource = MyCmd.SOURCE_NONE;
    private int mPhoneStatus = HFP_INFO_INITIAL;

    public KadjarRaise() {

        buildCmdRepeatSendCarType(getCarTypeCmd());
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});

        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0xee, 0x02, 0x10, 0};
        switch (CarUtil.getModelId()) {
            case 3:
            case 5:
            case 14:
                switch (CarUtil.getCarTypeConfig()) {
                    case 0:
                        cmd[3] = 0;
                        break;
                    case 1:
                        cmd[3] = 1;
                        break;
                    case 2:
                        cmd[3] = 2;
                        break;
                }
                break;
            default:
                return null;
        }
        return cmd;
    }

    private void parseACInfo(byte[] data, int len) {

        byte[] airData = new byte[10];

        if ((data[5] & 0xff) == 0xfe) {
            data[5] = (byte) 0;
        } else if (data[5] == 0) {
            airData[5] |= 0x2;
        }

        if ((data[6] & 0xff) == 0xfe) {
            data[6] = (byte) 0;
        } else if (data[6] == 0) {
            airData[5] |= 0x4;
        }

        airData[0] = (byte) (data[2] & 0x6c);
        airData[0] |= (byte) (((data[2] & 0x10) << 1) | ((data[2] & 0x2) >> 1) | ((data[2] & 0x1) << 1));

        airData[1] = (byte) (((data[3] & 0x1) << 7) | ((data[3] & 0x2) << 5) | ((data[3] & 0x4) << 3));

        if (((data[4] & 0xf) << 0) == 0xf) {
            airData[9] = 0x1;
        } else {
            airData[1] |= ((data[4] & 0xf) << 0);
        }

        airData[2] = (byte) (data[5] & 0xff);
        airData[3] = (byte) (data[6] & 0xff);


        airData[4] = (byte) (data[2] & 0x80);

        airData[7] = (byte) ((data[3] & 0x80) >> 7);

        if ((data[3] & 0x40) != 0) {
            airData[7] |= 0x2;
        } else if ((data[3] & 0x20) != 0) {
            airData[7] |= 0x4;
        }

        super.parseACInfo(airData);
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

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                //			parseWheelKey(data);
                super.parseCanboxData(data, len);
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

                boolean zero = Util.isZero(mRadar);
                if (!zero) {
                    RadarManager.start(mContext);
                    checkHideRadarEx(2000);
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    checkHideRadar();
                    handler.sendEmptyMessage(CANBOX_RADAR_BACK);
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
                    checkHideRadarEx(2000);
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    checkHideRadar();
                    handler.sendEmptyMessage(CANBOX_RADAR_FRONT);
                }
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

                int temp = (data[3] & 0xff);
                String s = "";
                if (temp != mOutTemp) {
                    mOutTemp = temp;
                    if (temp == 0xff) {
                        s = "--" + mContext.getResources().getString(R.string.temp_unic_centigrade);
                    } else if (temp != 0xfe) {
                        temp = temp - 40;
                        s = String.format("%d%s", temp, mContext.getResources().getString(R.string.temp_unic_centigrade));
                    }

                    GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);

                }

            }
            break;

            case 0x25: // Radar status
            {
                if ((data[2] & 0x1) != 0) {
                    RadarManager.stop();
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
            case 0x30: {
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

            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = ((data[2] & 0xff) | ((data[3]) << 8));
                    angle = -(angle * 300 / 0x4dce);
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));
                }
            }
            break;
            case (byte) 0x91:
                showSOS(data[2] & 0x1);
                break;
            case 0x71:
            case 0x61:
            case (byte) 0x81:
                // case 0x63:
                //
                sendCanboxInfo("com.canboxsetting", data);
                break;

            case (byte) 0x7f: {
                byte[] version = new byte[data.length - 2];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }

        }

        // if (data[0] == 0x40) {
        // sendCanboxInfo("com.canboxsetting", data);
        // }
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte h = (byte) ((time / 3600));
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        // ++play;

        byte s = 0;
        switch (source) {
            case MyCmd.SOURCE_DVD:
                s = 0x10;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                ++play;
                s = 8;
                break;
            case MyCmd.SOURCE_BT:
                s = 0xb;
                break;
            default:
                s = 0x07;
                break;
        }

        data = new byte[]{(byte) 0xc0, 0x8, s, (byte) ((total & 0xFF00) >> 8), (byte) (total & 0xFF), (byte) ((play & 0xFF00) >> 8), (byte) ((play) & 0xFF), h, min, sec};

        if (mPhoneStatus < HFP_INFO_CALLED) {

            sendDataToCanbox(data, data.length);
        }
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        data = new byte[]{(byte) 0xc0, 0x5, 0x1, b[0], b[1], b[2], 0};
        sendDataToCanbox(data, data.length);
    }

    private void showSOS(int show) {

        if (show != 0) {

            if (mView == null) {
                mView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.sos_layout, null);

                mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                mLayoutParams = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.RGBA_8888);

                // mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;

            }
            if (mView != null) {
                if (!isAddView) {
                    mWindowManager.addView(mView, mLayoutParams);
                    isAddView = true;

                    BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.CANBOX_PHONE_STATUS, 1);

                }
            }
        } else {
            if (isAddView) {
                mWindowManager.removeView(mView);
                isAddView = false;

                BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.CANBOX_PHONE_STATUS, 0);
            }
        }
    }

    public void setMediaSrc(int source) {
        byte s = 0;
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                s = 1;
                break;
            case MyCmd.SOURCE_DVD:
                s = 0x10;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 8;
                break;
            case MyCmd.SOURCE_BT:
                s = 0xb;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x7;
                break;
            default:
                s = 0x0;
                break;
        }

        if (mSource != source) {
            mSource = source;
            if (source == MyCmd.SOURCE_BT) {

                data = new byte[]{(byte) 0xc0, 0x8, s, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            } else {

                data = new byte[]{(byte) 0xc0, 0x8, s, 0, 0, 0, 0, 0, 0, 0};
            }
            sendDataToCanbox(data, data.length);
        }
    }

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

    public void setPhone(int status, String num) {// default is simple box

        mPhoneStatus = status;

        switch (status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:
            case HFP_INFO_CONNECTING:
            case HFP_INFO_CONNECTED:
                status = 0;
                break;
            case HFP_INFO_CALLED:
                status = 3;
                break;
            case HFP_INFO_INCOMING:
                status = 1;
                break;
            case HFP_INFO_CALLING:
                status = 4;
                break;
        }

        if (status != 0) {
            byte[] data2;

            if (num == null) {
                num = " ";
            }
            byte[] n = num.getBytes();
            data2 = new byte[n.length + 5];// {(byte)0xc5, 0x1, (byte)status};
            data2[0] = (byte) 0xc0;
            data2[1] = (byte) (n.length + 3);
            data2[2] = 5;
            data2[3] = (byte) status;
            data2[4] = 1;
            byteArrayCopy(data2, n, 5, 0, n.length);

            sendDataToCanbox(data2, data2.length);
        } else {
            if (data != null) {
                sendDataToCanbox(data, data.length);
            }
        }

    }
}
