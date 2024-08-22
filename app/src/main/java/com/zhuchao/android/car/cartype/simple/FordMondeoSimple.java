package com.zhuchao.android.car.cartype.simple;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;


public class FordMondeoSimple extends Canbox {

    public FordMondeoSimple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x4, KEY_NEXTSONG}, {0x3, KEY_PREVIOUSSONG}, {0x5, KEY_BT}, {0x6, KEY_MUTE}, {0x7, KEY_SOURCE}, {0x8, KEY_MIC}, {0xa, KEY_BT_HANG},
            {0xb, KEY_MIC},

    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x0, KEY_FM}, {0x1, KEY_DVD}, {0x2, KEY_AUX},

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


    private void parseACInfo(byte[] data, int len) {

        if (data[4] >= 0x39) {
            data[4] = (byte) 0xff;
        } else if (data[4] <= 0x1f) {
            data[4] = 0;
        }

        if (data[5] >= 0x39) {
            data[5] = (byte) 0xff;
        } else if (data[5] <= 0x1f) {
            data[5] = 0;
        }

        byte[] airData = new byte[8];

        airData[0] = (byte) (data[2] & 0xff);
        airData[0] |= (byte) (((data[6] & 0x40) >> 6));

        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        airData[7] = (byte) ((data[6] & 0x80) >> 2);
        //		airData[4] |= (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x8) >> 1) | ((data[2] & 0x1) << 3));
        //
        //		airData[5] = (byte) ((data[6] & 0x1));
        //
        //		airData[7] = (byte) (((data[6] & 0x10) >> 4));

        int msg = CANBOX_HIDE_AIR;
        if ((data[3] & 0x10) != 0) {
            msg = CANBOX_RETURN_AIR;
        }

        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(msg, airData));
        }
    }


    private byte getRadarData(byte i) {
        byte data = 0;
        if (i > 0x0 && i <= 0x1) {
            data = 1;
        } else if (i >= 0x2 && i <= 0x7) {
            if (i <= 0x3) {
                data = 2;
            } else if (i <= 0x5) {
                data = 3;
            } else {
                data = 4;
            }

        } else if (i >= 0x8 && i <= 0xd) {
            if (i <= 0xa) {
                data = 5;
            } else {
                data = 6;
            }
        } else if (i >= 0xe && i <= 0x13) {
            if (i <= 0x10) {
                data = 7;
            } else {
                data = 8;
            }
        } else if (i >= 0x14 && i <= 0x19) {
            if (i <= 0x16) {
                data = 9;
            } else {
                data = 10;
            }
        } else if (i >= 0x1a && i <= 0x1f) {
            if (i <= 0x1c) {
                data = 11;
            } else {
                data = 12;
            }
        }
        return data;
    }

    @SuppressLint("DefaultLocale")
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

            case 0x24: {
                if ((data[2] & 0x1) != 0) {
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
            case 0x26: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    // Log.e("1", ""+(data[2] & 0xff));
                    // Log.e("2", ""+(data[3] & 0xff));
                    // Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, (-((data[2] & 0xff) | ((data[3]) << 8))) / 10, 0));
                }
            }
            break;
            case 0x27: {

                int temp = ((data[3] & 0xff) | ((data[4] & 0xff) << 8));
                String s = "";
                if (temp >= -58 && temp <= 171) {
                    if (data[2] != 1) {


                        s = String.format("%d.%d%s", temp / 10, (temp % 10) >= 0 ? (temp % 10) : -(temp % 10), mContext.getResources().getString(R.string.temp_unic_centigrade));

                    } else {
                        s = String.format("%d%s", temp / 10, mContext.getResources().getString(R.string.temp_unic_fahrenheit));
                    }

                    if (s.length() > 1) {
                        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
                    }
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

            case 0x30: {
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }

            //		case 0x40:
            //		case 0x50:
            //		case 0x63:
            //
            //			sendCanboxInfo("com.canboxsetting", data);
            //			break;
        }

        if (data[0] == 0x40) {
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

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        // ++play;
        byte[] data;

        if (MyCmd.SOURCE_DVD != source) {
            //			data = new byte[] { (byte) 0xc3, 0x6, (byte) (total & 0xFF),
            //					(byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF),
            //					(byte) ((play >> 8) & 0xFF), min, sec };

            //			data = new byte[] { (byte) 0xc0, 0x2, 0x7, 0x0 };
        } else {
            data = new byte[]{
                    (byte) 0xc3, 0x6, (byte) (1 & 0xFF), (byte) ((play) & 0xFF), (byte) (total & 0xFF), (byte) ((0) & 0xFF), min, sec
            };
            sendDataToCanbox(data, data.length);
        }

    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        byte[] data = new byte[]{(byte) 0xc2, 0x4, b[0], b[1], b[2], 0};
        sendDataToCanbox(data, data.length);
    }

    private int mSource = MyCmd.SOURCE_NONE;

    public void setMediaSrc(int source) {// default is simple box
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
            default:
                s = 0x07;
                mediaType = 0x30;
                break;
        }
        byte[] data;
        if (mSource != source) {
            mSource = source;
            data = new byte[]{(byte) 0xc0, 0x2, 0, 0};
            sendDataToCanbox(data, data.length);
            // Util.doSleep(10);
        }
        data = new byte[]{(byte) 0xc0, 0x2, s, mediaType};

        sendDataToCanbox(data, data.length);
    }

    public void setVolume(int volume) {

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

    public void setPhone(int status, String num) {// default is simple box
        switch (status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:
            case HFP_INFO_CONNECTING:
            case HFP_INFO_CONNECTED:
                status = 0;
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
        byte[] data = new byte[4];// {(byte)0xc5, 0x1, (byte)status};
        data[0] = (byte) 0xc5;
        data[1] = (byte) (2);
        data[2] = (byte) 0;
        data[3] = (byte) status;

        sendDataToCanbox(data, data.length);

        if (num == null) {
            num = " ";
        }
        byte[] n = num.getBytes();
        data = new byte[n.length + 4];// {(byte)0xc5, 0x1, (byte)status};
        data[0] = (byte) 0xcA;
        data[1] = (byte) (n.length + 2);
        data[2] = (byte) 1;
        data[3] = (byte) 3;
        byteArrayCopy(data, n, 4, 0, n.length);
        Util.doSleep(100);
        sendDataToCanbox(data, data.length);

    }
}
