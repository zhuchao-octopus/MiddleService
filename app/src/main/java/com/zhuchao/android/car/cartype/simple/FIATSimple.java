package com.zhuchao.android.car.cartype.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.common.utils.AppConfig;
import com.common.utils.BroadcastUtil;
import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class FIATSimple extends Canbox {

    public FIATSimple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    @Override
    public void setContext(Context c) {
        // TODO Auto-generated method stub
        super.setContext(c);
        byte[] data = new byte[]{(byte) 0xf1, 0x1, 0x71};
        sendDataToCanbox(data, data.length);
    }

    public void startConnect() {// default is simple box

        super.startConnect();
        Util.doSleep(10);
        byte[] data = new byte[]{(byte) 0xca, 0x1, (byte) CarUtil.getCarType()};
        sendDataToCanbox(data, data.length);

    }

    private final static byte[][] KEYS_WHEEL = {
            {0x4, AK_KEYPAD_VOLUME_A}, {0x5, AK_KEYPAD_VOLUME_D}, {0x9, KEY_NEXTSONG}, {0x8, KEY_PREVIOUSSONG},

            {0x6, KEY_MUTE}, {0x3, KEY_SOURCE}, {0xA, KEY_GPS},

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

    @SuppressLint("DefaultLocale")
    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x11: {
                parseWheelKey(data);
            }
            break;
            case 0x71: {
                // byte[] version = new byte[9];
                // Util.byteArrayCopy(version, data, 0, 2, version.length);
                String date = ((data[4] & 0xf0) >> 4) + String.valueOf((data[4] & 0xf) >> 0) + "-" + ((data[5] & 0xf0) >> 4) + ((data[5] & 0xf) >> 0) + "-" + ((data[6] & 0xf0) >> 4) + ((data[6] & 0xf) >> 0);
                mVersion = data[2] + " " + data[3] + " " + date + "v" + data[8] + data[9] + data[10];
                Log.d("ff", mVersion);
                break;
            }
            case 0x14: {
                int door = (data[2] & 0xf8);
                door = (((door & 0x80) >> 7) | ((door & 0x40) >> 5) | ((door & 0x20) >> 3) | ((door & 0x10) >> 1) | ((door & 0x08) << 1));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }

            }
            break;

            case 0x16: {

                int temp = (data[2] & 0xff);
                temp = -395 + (temp * 5);
                // temp -= 40;
                String s = "";
                if (temp >= -390 && temp <= 880) {
                    if (data[3] != 1) {

                        s = String.format("%d.%d%s", temp / 10, (temp % 10) >= 0 ? (temp % 10) : -(temp % 10), mContext.getResources().getString(R.string.temp_unic_centigrade));

                    } else {
                        temp = (temp * 18 + 3200);
                        temp /= 10;

                        s = String.format("%d.%d%s", temp / 10, (temp % 10) >= 0 ? (temp % 10) : -(temp % 10), mContext.getResources().getString(R.string.temp_unic_fahrenheit));

                    }

                }
                GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
            }
            break;
            case 0x12:
                if ((data[2] & 0xc) != 0) {

                    if (!"com.canboxsetting/com.canboxsetting.CarInfoActivity".equals(AppConfig.getTopActivity())) {

                        try {
                            Intent it = new Intent(Intent.ACTION_VIEW);
                            it.putExtra(MyCmd.EXTRA_COMMON_CMD, 1);
                            it.setClassName("com.canboxsetting", "com.canboxsetting.CarInfoActivity");
                            it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(it);
                        } catch (Exception e) {
                            // Log.e(TAG, e.getMessage());
                        }
                    }
                } else {
                    if ((data[2] & 0x3) != 0) {

                        if (!"com.canboxsetting/com.canboxsetting.CarInfoActivity".equals(AppConfig.getTopActivity())) {

                            try {
                                Intent it = new Intent(Intent.ACTION_VIEW);
                                it.putExtra(MyCmd.EXTRA_COMMON_CMD, 0);
                                it.setClassName("com.canboxsetting", "com.canboxsetting.CarInfoActivity");
                                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(it);
                            } catch (Exception e) {
                                // Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                }

                if ((data[2] & 0xc) != 0) {
                    if ((mUSBBTInfo & 0xc) == 0) {
                        BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.CANBOX_PHONE_STATUS, 1);
                    }
                } else {
                    if ((mUSBBTInfo & 0xc) != 0) {
                        BroadcastUtil.sendToCarService(mContext, MyCmd.Cmd.CANBOX_PHONE_STATUS, 0);
                    }
                }

                mUSBBTInfo = data[2];
                break;
            case 0x30: {
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }

        }

        if (data[0] == 0x12 || data[0] == 0x17) {
            sendCanboxInfo("com.canboxsetting", data);
        }
    }

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

    // private final static int CHECK_PLAYSTATUS = 0;
    // private Handler mHandler = new Handler() {
    // public void handleMessage(Message msg) {
    // switch (msg.what) {
    // case CHECK_PLAYSTATUS:
    // if (mMediaType == 0x2 || mMediaType == 0x4) {
    // mMediaData[1] = 0x0;
    // byte[] data = new byte[] { (byte) 0x93, 0x6, mMediaType, 0,
    // mMediaData[0], mMediaData[1], 0, 0 };
    // sendDataToCanbox(data, data.length);
    // }
    // break;
    // }
    // super.handleMessage(msg);
    // }
    // };

    byte[] mMediaData = new byte[2];
    private int mTime = -1;

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        switch (source) {
            case MyCmd.SOURCE_DVD:
                if (play > 99) {
                    play = 99;
                }
                mMediaType = 0x2;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                if (play > 254) {
                    play = 254;
                }
                mMediaType = 0x4;
                break;
            default:
                source = MyCmd.SOURCE_NONE;
                break;
        }

        if (source != MyCmd.SOURCE_NONE) {
            mMediaData[0] = (byte) (play + 1);

            if (mTime != time) {
                mTime = time;
                mMediaData[1] = 2;
            } else {
                mMediaData[1] = 0;
            }

            byte[] data = new byte[]{
                    (byte) 0x93, 0x6, mMediaType, 0, mMediaData[0], mMediaData[1], 0, 0
            };
            sendDataToCanbox(data, data.length);

            // mHandler.removeMessages(CHECK_PLAYSTATUS);
            // mHandler.sendEmptyMessageDelayed(CHECK_PLAYSTATUS, 1500);
        }
    }

    public void setMediaSrc(int source, byte type, byte[] b) { // only radio use
        // setMediaSrc(0);
        if (source == MyCmd.SOURCE_RADIO) {
            if (b[0] == 0x10) {
                b[0] = 6;
            } else {
                b[0] = 0;
                int value1 = ((b[2] & 0xff) << 8) | ((b[1] & 0xff));
                value1 /= 10;
                b[1] = (byte) (value1 & 0xff);
                b[2] = (byte) ((value1 >> 8) & 0xff);
            }

            mMediaType = 0x1;

            byte[] data = new byte[]{(byte) 0x93, 0x6, 0x1, b[0], 0, 0, b[2], b[1]};
            sendDataToCanbox(data, data.length);
        }

    }

    byte mMediaType = -1;

    public void setMediaSrc(int source) {// default is simple box

        byte mediaType = 0;
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                mediaType = 1;
                mTime = -1;
                break;
            case MyCmd.SOURCE_DVD:
                mediaType = 2;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                mediaType = 0x4;
                break;
            case MyCmd.SOURCE_CANBOX_PHONE:
            case MyCmd.SOURCE_CANBOX_MEDIA:
                mediaType = 0x5;
                break;
            case MyCmd.SOURCE_AUX:
                mediaType = 3;
                mTime = -1;
                break;
            default:
                mTime = -1;
                mediaType = 0xF;
                break;
        }
        //
        if (mMediaType != mediaType) {
            byte[] data = new byte[]{
                    (byte) 0x93, 0x6, mediaType, 0, 0, 0, 0, 0
            };
            sendDataToCanbox(data, data.length);
        }
    }

    private byte mUSBBTInfo = 0;

    public void sendDataToCanbox(byte[] data, int len) {
        if ((data[0] & 0xff) == 0xff) {
            data = new byte[]{(byte) 0x12, 0x1, mUSBBTInfo};
            sendCanboxInfo("com.canboxsetting", data);
        } else {
            super.sendDataToCanbox(data, len);
        }

    }

    public void stopConnect() {// default is simple box
        byte[] data = new byte[]{(byte) 0x92, 0x1, (byte) 0x80};
        sendDataToCanbox(data, data.length);
        super.stopConnect();
    }

}
