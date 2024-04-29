package com.zhuchao.android.car.cartype.daojun;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;


public class FiestaDaojun extends Canbox {

    public FiestaDaojun() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });


        buildCmdAngle((byte) 0x26, (byte) 0x0, 22016);
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL2;
    }


    @Override
    public int getAngleValue(byte[] data) {

        int max;
        int angle;
        if (CarUtil.getModelId() == 51) {
            angle = (((data[3] & 0xff) << 8) | (data[2] & 0xff));
            angle = 31901 - angle;
            max = 5168;
        } else {
            angle = (((data[2] & 0xff) << 8) | (data[3] & 0xff));
            angle = 0x8000 - angle;
            max = 43520 - 0x8000;
            if (angle > 0) {
                max = 0x8000 - 22045;
            }
        }

        angle = ((angle * 3000) / max);
        if (angle > -50 && angle < 50) {
            angle = 50;
        }

        return -angle;


    }

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.DVD}, {0x3, MyCmd.Keycode.RADIO}, {0x4, MyCmd.Keycode.AUX_IN}, {0x5, MyCmd.Keycode.MUTE},//yihu
            {0x8, MyCmd.Keycode.EJECT}, {0x9, MyCmd.Keycode.AS}, {0xa, MyCmd.Keycode.AUDIO}, {0xb, MyCmd.Keycode.PREVIOUS}, {0xc, MyCmd.Keycode.NEXT}, {0xd, MyCmd.Keycode.PLAY_PAUSE},
            {0xe, MyCmd.Keycode.PREVIOUS}, {0xf, MyCmd.Keycode.NEXT}, {0x10, MyCmd.Keycode.PREVIOUS}, {0x11, MyCmd.Keycode.NEXT}, {0x12, MyCmd.Keycode.PREVIOUS}, {0x13, MyCmd.Keycode.NEXT},
            {0x14, MyCmd.Keycode.PREVIOUS}, {0x15, MyCmd.Keycode.NEXT},

            {0x16, MyCmd.Keycode.NUMBER0}, {0x17, MyCmd.Keycode.NUMBER1}, {0x18, MyCmd.Keycode.NUMBER2}, {0x19, MyCmd.Keycode.NUMBER3}, {0x1a, MyCmd.Keycode.NUMBER4}, {0x1b, MyCmd.Keycode.NUMBER5},
            {0x1c, MyCmd.Keycode.NUMBER6}, {0x1d, MyCmd.Keycode.NUMBER7}, {0x1e, MyCmd.Keycode.NUMBER8}, {0x1f, MyCmd.Keycode.NUMBER9}, {0x20, MyCmd.Keycode.NUMBER_STAR},
            {0x21, MyCmd.Keycode.NUMBER_POUND}, {(byte) 0x23, MyCmd.Keycode.VOLUME_ROLL_UP}, {(byte) 0x22, MyCmd.Keycode.VOLUME_ROLL_DOWN},


    };

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.MULT_NEXT_AND_HANG}, {0x4, MyCmd.Keycode.MULT_PREV_AND_RECEIVE}, {0x5, MyCmd.Keycode.SPEECH},

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

    private final byte[] mAirData = new byte[8];

    private void parseACInfo(byte[] data, int len) {

        if (data[4] == 0x7f) {
            data[4] = (byte) 0xff;
        }
        if (data[5] == 0x7f) {
            data[5] = (byte) 0xff;
        }

        byte[] airData = new byte[8];

        airData[0] = (byte) (data[2] & 0xef);
        airData[0] |= (byte) (((data[6] & 0x40) >> 6));

        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        //		airData[4] = (byte) (data[7] & 0xff);
        airData[4] |= (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x8) >> 1) | ((data[2] & 0x1) << 3) | ((data[6] & 0x4) << 0));

        airData[5] = (byte) ((data[6] & 0x1));

        airData[7] = (byte) (((data[6] & 0x10) >> 4));
        airData[7] |= (byte) (((data[6] & 0x80) >> 2));

        if (Util.isBufEquals(mAirData, airData)) {
            return;
        } else {
            Util.byteArrayCopy(mAirData, airData, 0, 0, mAirData.length);
        }

        //		int msg = CANBOX_HIDE_AIR;
        //		if ((data[3] & 0x10) != 0) {
        //			msg = CANBOX_RETURN_AIR;
        //		}

        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
        }
    }

    private byte getRadarData(byte i) {
        byte data = 0;
        if (i > 0 && i < 31) {
            data = (byte) ((i & 0xff) / 3);
        } else if (i == 31) {
            data = 11;
        }
        return data;
    }

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
            case 0x25: {
                if ((data[2] & 0x08) == 0) {
                    mHandler.removeMessages(HIDE_RADAR);
                    RadarManager.stop();
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
            case 0x24: {
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
            break;

            case 0x66: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = getAngleValue(data);
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
                }
            }
            break;

            case 0x65:
            case 0x16:
                sendCanboxInfo("com.canboxsetting", data);
                break;
            default:
                super.parseCanboxData(data, len);
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
            if (mUnit != 1) {
                float t = temp / 10.0f;
                temp = (int) (((t) * 1.8f + 32) * 10);
            }
            mUnit = 1;
        } else if (CarUtil.mTempUnit == 1) {
            if (mUnit == 1) {
                temp = (int) ((((float) temp / 10.0f) - 32) / 1.8f) * 10;
            }
            mUnit = 0;
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


    byte[] data;

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
            data = new byte[]{
                    (byte) 0xc0, 0x8, s, s2, 0, (byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec
            };

        } else {
            data = new byte[]{
                    (byte) 0xc0, 0x8, s, s2, (byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), 0, h, min, sec
            };
        }

        if (mPhoneStatus < HFP_INFO_CALLED) {

            sendDataToCanbox(data, data.length);
        }
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        data = new byte[]{
                (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0, 0, 0
        };
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
            //			if (len > 31) {
            //				len = 31;
            //			}
            byte[] data = new byte[len];

            data[0] = (byte) 0xcb;
            data[1] = (byte) (len - 2);
            data[2] = index;
            data[3] = 0x2;
            for (int i = 0; i < num_len && i < (len - 4); ++i) {
                Log.d("Accord2013Simple", i + ":" + n.length + ":" + num_len);
                data[4 + i] = n[i + (n.length - num_len)];
            }
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("Accord2013Simple", "sendId3" + e);
        }
    }

    String mName = null;
    String mArtist = null;
    String mAlbum = null;
    //	public void setPhone(int status, String num) {
    //		sendId3((byte)0x1, num);
    //	}

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

    private int mPhoneStatus = HFP_INFO_INITIAL;

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

        //		if (status != 0) {
        byte[] data2;

        if (num == null) {
            num = " ";
        }


        data2 = new byte[]{(byte) 0xc0, 0x8, 0x5, 0x40, 0, 0, 0, 0, 0, 0};


        sendDataToCanbox(data2, data2.length);
        Util.doSleep(50);

        byte[] n = num.getBytes();
        data2 = new byte[36];// {(byte)0xc5, 0x1, (byte)status};
        data2[0] = (byte) 0xcb;
        data2[1] = 34;
        data2[2] = 0x1;
        data2[3] = 0x1;

        int num_len = n.length;
        if (num_len > 31) {
            num_len = 31;
        }
        byteArrayCopy(data2, n, 4, 0, num_len);
        data2[num_len + 4] = (byte) 0xff;

        sendDataToCanbox(data2, data2.length);
        //		} else {
        //			if (data != null) {
        //				sendDataToCanbox(data, data.length);
        //			}
        //		}

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
    //
    // public void setPhone(int status, String num) {// default is simple box
    // switch (status) {
    // case HFP_INFO_INITIAL:
    // case HFP_INFO_READY:
    // case HFP_INFO_CONNECTING:
    // case HFP_INFO_CONNECTED:
    // status = 0;
    // break;
    // case HFP_INFO_CALLED:
    // status = 2;
    // break;
    // case HFP_INFO_INCOMING:
    // status = 1;
    // break;
    // case HFP_INFO_CALLING:
    // status = 4;
    // break;
    // }
    // byte[] data = new byte[4];// {(byte)0xc5, 0x1, (byte)status};
    // data[0] = (byte) 0xc5;
    // data[1] = (byte) (2);
    // data[2] = (byte) 0;
    // data[3] = (byte) status;
    //
    // sendDataToCanbox(data, data.length);
    //
    // if (num == null) {
    // num = " ";
    // }
    // byte[] n = num.getBytes();
    // data = new byte[n.length + 4];// {(byte)0xc5, 0x1, (byte)status};
    // data[0] = (byte) 0xcA;
    // data[1] = (byte) (n.length + 2);
    // data[2] = (byte) 1;
    // data[3] = (byte) 3;
    // byteArrayCopy(data, n, 4, 0, n.length);
    // Util.doSleep(100);
    // sendDataToCanbox(data, data.length);
    //
    // }
}
