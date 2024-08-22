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
import com.zhuchao.android.car.canbox.RadarManager;

import java.util.Date;


public class RamFIATSimple extends Canbox {

    public RamFIATSimple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });


    }

    public void setContext(Context c) {
        super.setContext(c);
        updateTime();
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG}, {0x6, KEY_MUTE}, {0x7, KEY_SOURCE}, {0x8, KEY_MIC}, {0x9, KEY_BT_DIAL},
            {0xa, KEY_BT_HANG}, {0x12, KEY_PLAYPAUSE},

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

    private void parseACInfo(byte[] data, int len) {

        if ((data[6] & 0x2) != 0) {
            switch ((data[4] & 0xc0) >> 6) {
                case 1:
                    data[4] = 36;
                    break;
                case 2:
                    data[4] = 56;
                    break;
                default:
                    data[4] = 52;
                    break;
            }

            switch ((data[5] & 0xc0) >> 6) {
                case 1:
                    data[5] = 36;
                    break;
                case 2:
                    data[5] = 56;
                    break;
                default:
                    data[5] = 52;
                    break;
            }
        }


        byte[] airData = new byte[8];

        airData[0] = (byte) (data[2] & 0xe0);
        airData[0] |= (byte) (((data[8] & 0x1) << 1) | ((data[6] & 0x40) >> 6));

        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        airData[4] = (byte) ((data[6] & 0x8) >> 1);

        airData[5] = (byte) ((data[6] & 0x1));

        airData[7] = (byte) (((data[6] & 0x10) >> 4));

        int msg = CANBOX_HIDE_AIR;
        if ((data[2] & 0x80) != 0) {
            msg = CANBOX_RETURN_AIR;
        }

        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(msg, airData));
        }
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
            case 0x21: {
                parseACInfo(data, len);
            }
            break;

            case 0x24: {
                //			if ((data[2] & 0x1) != 0) {
                int door = (data[2] & 0xfc);
                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((door & 0x4) << 3));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }
                //			}

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

            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = ((data[2] & 0xff) | ((data[3]) << 8));
                    angle = -(angle * 300 / 3000);
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

        }

        if (data[0] == 0x40) {
            sendCanboxInfo("com.canboxsetting", data);
        }
    }

    private int mDoorStatus = 0;


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        // ++play;
        byte[] data;

        if (MyCmd.SOURCE_DVD != source) {
            data = new byte[]{
                    (byte) 0xc3, 0x6, (byte) (total & 0xFF), (byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF), (byte) ((play >> 8) & 0xFF), min, sec
            };
        } else {
            ++play;
            data = new byte[]{
                    (byte) 0xc3, 0x6, (byte) (1 & 0xFF), (byte) ((play) & 0xFF), (byte) (total & 0xFF), (byte) ((0) & 0xFF), min, sec
            };
        }
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        byte[] data = new byte[]{(byte) 0xc2, 0x4, b[0], b[1], b[2], 0};
        sendDataToCanbox(data, data.length);
    }

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
            case MyCmd.SOURCE_IPOD:
                s = 6;
                mediaType = 0x12;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x09;
                mediaType = 0x11;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                mediaType = 0x30;
                byte[] data2 = new byte[]{(byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0};
                sendDataToCanbox(data2, data2.length);
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                mediaType = 0x30;
                byte[] data3 = new byte[]{(byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0};
                sendDataToCanbox(data3, data3.length);
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0b;
                mediaType = 0x30;
                break;
            default:
                s = 0x00;
                mediaType = 0x0;
                break;
        }
        byte[] data;
        if (s == 0xb || s == 0x7) {
            data = new byte[]{
                    (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0
            };
        } else {
            data = new byte[]{(byte) 0xc0, 0x2, s, mediaType};
        }

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

    public void updateTime() {
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        if ("12".equals(strTimeFormat)) {
            if (h >= 12) {
                ampm |= 0x80;
            }

            //			if (h > 12) {
            //				h -= 12;
            //			} else if (h == 0) {
            //				h = 12;
            //			}


            h |= 0x80;
        }

        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();


        byte y = (byte) curDate.getYear();
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{
                (byte) 0xc7, 0x06, y, mon, d, h, m, s
        };
        sendDataToCanbox(buf, buf.length);
    }

    public int getUpdateTime() {
        return 60000;
    }
}
