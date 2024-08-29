package com.zhuchao.android.car.cartype.simple;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.canbox.WarningMsgManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.fbase.ByteUtils;
import com.zhuchao.android.fbase.MMLog;

public class PSASimple extends Canbox {

    private final static byte[][] KEYS_WHEEL_NORMAL = {

            {0x2, KEY_HOME}, {0x3, KEY_PREVIOUSSONG}, {0x4, KEY_NEXTSONG}, {0x7, KEY_PLAYPAUSE}, {0x8, KEY_BACK}, {0x10, KEY_SOURCE}, {0x11, KEY_SOURCE}, {0x12, KEY_SEEK_NEXT}, {0x13, KEY_SEEK_PREV}, {0x14, AK_KEYPAD_VOLUME_A}, {0x15, AK_KEYPAD_VOLUME_D}, {0x16, KEY_MUTE}, {0x1F, KEY_MIC}, {0x50, KEY_BT}, {0x20, MyCmd.Keycode.KEY_CAR_INFO}, {0x21, KEY_MODE}, {0x23, KEY_BT}, {0x30, MyCmd.Keycode.MULT_SOURCE_AND_BT},

            {(byte) 0x17, MyCmd.Keycode.ROLL_NEXT}, {(byte) 0x18, MyCmd.Keycode.ROLL_PREV},

            {0x19, AK_KEYPAD_VOLUME_A}, {0x1a, AK_KEYPAD_VOLUME_D}, {0x22, KEY_EJECT}, {(byte) 0x91, KEY_NUM_1}, {(byte) 0x92, KEY_NUM_2}, {(byte) 0x93, KEY_NUM_3}, {(byte) 0x94, KEY_NUM_4}, {(byte) 0x95, KEY_NUM_5}, {(byte) 0x96, KEY_NUM_6}, {(byte) 0x97, KEY_PREVIOUSSONG}, {(byte) 0x98, KEY_NEXTSONG}, {(byte) 0x99, KEY_FM}, {(byte) 0x9a, MyCmd.Keycode.BRIGHTNESS}, {(byte) 0x9b, KEY_HOME}, {(byte) 0x9c, KEY_PREVIOUSSONG}, {(byte) 0x9d, KEY_NEXTSONG}, {(byte) 0x9e, KEY_MODE}, {(byte) 0x9f, MyCmd.Keycode.KEY_RECENT_APPS},

            {(byte) 0xa0, MyCmd.Keycode.MULT_SOURCE_AND_BT}, {(byte) 0xa1, KEY_MEDIA}, {(byte) 0xa2, KEY_PLAYPAUSE}, {(byte) 0xa3, KEY_BACK}, {(byte) 0xa4, KEY_MUTE},
            // { (byte)0xa5, KEY_MODE },
            {(byte) 0xa6, KEY_HOME},

            {(byte) 0xb0, KEY_BT_DIAL}, {(byte) 0xb1, KEY_BT_HANG}, {(byte) 0xb2, KEY_BACK}, {(byte) 0xb3, KEY_GPS}, {(byte) 0xb4, MyCmd.Keycode.RDS_TA_SWITCH}, {(byte) 0xb5, KEY_FM}, {(byte) 0xb6, KEY_MEDIA}, {(byte) 0xb7, KEY_SET}, {(byte) 0xb8, MyCmd.Keycode.KEY_RECENT_APPS}, {(byte) 0xb9, KEY_PREVIOUSSONG}, {(byte) 0xba, KEY_NEXTSONG}, {(byte) 0xbb, KEY_SEEK_PREV}, {(byte) 0xbc, KEY_SEEK_NEXT}, {(byte) 0xbd, KEY_PLAYPAUSE}, {(byte) 0xbe, MyCmd.Keycode.VOLUME_ROLL_UP}, {(byte) 0xbf, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {(byte) 0x81, MyCmd.Keycode.VOLUME_ROLL_UP}, {(byte) 0x82, MyCmd.Keycode.VOLUME_ROLL_DOWN},

            {(byte) 0xa5, MyCmd.Keycode.KEY_AIR_CONTROL},

    };
    private final byte[][] KEYS_WHEEL = KEYS_WHEEL_NORMAL;
    private final int mBaud = 0;
    int caneq = 0;
    byte[] airData = new byte[8];

    //	private void intKeyType() {
    //		switch (CarUtil.getModelId()) {
    //		case 2:
    //			cmd[3] = 1;
    //			break;
    //		case 3:
    //			cmd[3] = 0;
    //			break;
    //		case 6:
    //			cmd[3] = 2;
    //			break;
    //		}
    //	}
    String mName = null;
    String mArtist = null;
    String mAlbum = null;
    byte[] mEqData = new byte[8];
    private int showWarningMsg = -1;
    private int mDoorStatus = 0;
    private int mSource = MyCmd.SOURCE_NONE;

    public PSASimple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});

        buildBrake((byte) 0x38, (byte) 0x4, (byte) 0x2, (byte) 0);
        buildCmdRepeatSendCarType(getCarTypeCmd(), 1);
        // updateCanboxSettings();
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = null;
        if (CarUtil.getManaId() == 1) {
            cmd = new byte[]{(byte) 0xca, 0x01, 0};
            switch (CarUtil.getModelId()) {
                case 9:
                    cmd[2] = 1;
                    break;
                case 65:
                    cmd[2] = 2;
                    break;
                default:
                    return null;
            }
        }
        return cmd;
    }

    @Override
    public void stopConnect() {
        // TODO Auto-generated method stub
        if (CarUtil.getCarEQ() == 1 || caneq == 1 || CarUtil.getCanboxProVersion() >= 3) {
            sendEQ((byte) 1, (byte) 0);
        }
        super.stopConnect();
    }

    public void startConnect() {// default is simple box
        caneq = SettingProperties.getIntProperty(mContext, SettingProperties.KEY_CANBOX_EQ);
        if (CarUtil.getCarEQ() == 1 || caneq == 1 || CarUtil.getCanboxProVersion() >= 3) {
            CarUtil.mIsNeedSendEQ = true;
            Util.doSleep(10);
            sendEQ((byte) 1, (byte) 1);
        }

        super.startConnect();
        Util.doSleep(10);
        byte[] data = new byte[]{(byte) 0x90, 0x4, 0x71, 0, 0, 0};
        sendDataToCanbox(data, data.length);
        Util.doSleep(10);
        data = new byte[]{(byte) 0x90, 0x4, 0x17, 0, 0, 0};
        sendDataToCanbox(data, data.length);

        /*switch (CarUtil.getAirCondition()) {
            case 4:
                MMLog.d(TAG, "getAirCondition==4");
                break;
            case 5:
                MMLog.d(TAG, "getAirCondition==5");
                break;
        }*/
    }

    private void parseWheelKey(byte[] data, int len) {
        if (data[2] == (byte) 0xa5) {
            if (data[3] == 1) {
                Handler handler = getHandler("CanService");
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
                }
            }
        }
        // else if (data[2] == 0x17 || data[2] == 0x18 || data[2] == (byte) 0x81
        // || data[2] == (byte) 0x82) {
        //
        // int key = 0;
        // switch (data[2]) {
        // case 0x17:
        // key = KEY_PREVIOUSSONG;
        // break;
        // case 0x18:
        // key = KEY_NEXTSONG;
        // break;
        // case (byte) 0x81:
        // key = AK_KEYPAD_VOLUME_A;
        // break;
        // case (byte) 0x82:
        // key = AK_KEYPAD_VOLUME_D;
        // break;
        // }
        //
        // if (key != 0) {
        // if (data[3] != 0) {
        // doKey(key, 1);
        // doKey(key, 0);
        // } else {
        // // doKey(key, data[3]);
        // }
        // }
        // }
        else {
            super.parseWheelKey(0, data, KEYS_WHEEL);
            //			if (doKeyStudy(data[2], data[3])) {
            //				return;
            //			}
            //
            //			int key = 0;
            //			for (int i = 0; i < KEYS_WHEEL.length; ++i) {
            //				if (KEYS_WHEEL[i][0] == data[2]) {
            //					key = KEYS_WHEEL[i][1];
            //					break;
            //				}
            //			}
            //
            //			if (key != 0) {
            //				doKey(key, data[3]);
            //			} else {
            //				if (data[3] == 0) {
            //					doKey(0, 0);
            //				}
            //			}

        }
    }

    private void parseACInfo(byte[] data, int len) {
        boolean airControl = false;
        int windMode = 0;

        // if ((data[6] & 0x2) == 0) {
        // data[4] = (byte)0xfe;
        // data[5] = (byte)0xfe;
        // } else {
        // if (data[4] >= 0x7f) {
        // data[4] = (byte) 0xff;
        // } else if (data[4] >= 0x1f && data[4] <= 0x3B) {
        // data[4] = (byte) ((15.5f + (0.5f * (data[4] - 0x1f))) * 2);
        // } else {
        // data[4] = 0;
        // }
        // if (data[5] >= 0x7f) {
        // data[5] = (byte) 0xff;
        // } else if (data[5] >= 0x1f && data[5] <= 0x3B) {
        // data[5] = (byte) ((15.5f + (0.5f * (data[5] - 0x1f))) * 2);
        // } else {
        // data[5] = 0;
        // }
        // }

        airData[0] = (byte) (data[2] & 0xff);
        airData[0] |= (byte) (((data[6] & 0x80) >> 6));

        byte wind = (byte) (data[3] & 0x0f);
        if (wind == 0) {
            airData[0] |= 0x8;
        } else {
            --wind;
        }
        airData[1] = (byte) (data[3] & 0xf0);
        airData[1] |= wind;

        //if ((CarUtil.getAirCondition() == 4) || CarUtil.getAirCondition() == 5) {
        //    airData[2] = (byte) (data[4] & 0xff);
        //}
        //else
        {
            airData[2] = (byte) (data[4] & 0xff);
            airData[3] = (byte) (data[5] & 0xff);
        }

        airData[4] = (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x08) >> 1) | ((data[6] & 0x04) << 1));
        airData[5] = (byte) (((data[6] & 0x1)));

        if (data[1] > 7) {
            airData[6] = (byte) (((data[10] & 0xe0)));
            if (airData[6] != 0) {
                windMode = 1;
            }
        }

        ///暂时不处理手动空调
        ///	if ((data[6] & 0x2) == 0) {
        ///	   airData[7] = 0x40;
        ///	} else {
        ///	airData[7] = 0x0;
        ///	}

        int msg = CANBOX_HIDE_AIR;
        if ((data[2] & 0x80) != 0) {
            msg = CANBOX_RETURN_AIR;
        }
        // if (airControl) {
        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(msg, windMode, 0, airData));
        }
        // }
    }

    private byte getRadarData(byte i) {
        byte data = 0;
        switch (i) {
            case 0:
                data = 1;
                break;
            case 1:
                data = 5;
                break;
            case 2:
                data = 7;
                break;
            case 3:
                data = 9;
                break;
            case 4:
                data = 11;
                break;
        }
        return data;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void parseCanboxData(byte[] data, int len) {
        MMLog.d(TAG, "CanboxData data=" + ByteUtils.BuffToHexStr(data) + "length=" + len);
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data, len);
            }
            break;
            case 0x21: {
                //sendCanboxInfo("com.canboxsetting", data);
                parseACInfo(data, len);
            }
            break;

            case 0x32: // Radar back
            {
                //if (data[2] == 0x2) {
                mRadar[0] = getRadarData(data[3]);
                mRadar[1] = getRadarData(data[4]);
                mRadar[2] = getRadarData(data[4]);
                mRadar[3] = getRadarData(data[5]);

                mRadar[4] = getRadarData(data[6]);
                mRadar[5] = getRadarData(data[7]);
                mRadar[6] = getRadarData(data[7]);
                mRadar[7] = getRadarData(data[8]);

                // byteArrayCopy(mRadar, data, 0, 2, 4);
                boolean zero = Util.isZero(mRadar);
                if (!zero) {
                    RadarManager.start(mContext);
                    checkHideRadarEx(2000);
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK, 0, 3));
                }
                //} else {
                //	RadarManager.stop();
                //}
            }
            break;

            case 0x38: {
                int door = (data[2] & 0xf8);
                door = (((door & 0x40) >> 5) | ((door & 0x80) >> 7) | ((door & 0x10) >> 1) | ((door & 0x20) >> 3) | ((door & 0x08) << 1) | ((door & 0x04) << 3));

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
            case 0x36: {
                int temp = (data[2] & 0x7f);
                if ((data[2] & 0x80) != 0) {
                    temp = -temp;
                }
                String s = String.format("%d%s", temp, mContext.getResources().getString(R.string.temp_unic_centigrade));
                GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
            }
            break;

            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu
                    int angle = ((a * 3000) / 5450);
                    if (angle > -50 && angle < 50) {
                        angle = 50;
                    }
                    /// Log.e("1", ""+(data[2] & 0xff));
                    /// Log.e("2", ""+(data[3] & 0xff));
                    /// Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
                }
            }
            break;
            case 0x33:
            case 0x34:
            case 0x35:
            case 0x3A:
            case 0x3B:
                sendCanboxInfo("com.canboxsetting", data);
                break;
            case 0x17:
                CarUtil.mIsNeedSendEQ = ((data[2] & 0x80) != 0);
                sendCanboxInfo("com.canboxsetting", data);
                break;
            case 0x30: {
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);
                mVersion = (new String(version));
                break;
            }
            case 0x71: {
                /// byte[] version = new byte[9];
                /// Util.byteArrayCopy(version, data, 0, 2, version.length);
                String date = ((data[4] & 0xf0) >> 4) + String.valueOf((data[4] & 0xf) >> 0) + "-" + ((data[5] & 0xf0) >> 4) + ((data[5] & 0xf) >> 0) + "-" + ((data[6] & 0xf0) >> 4) + ((data[6] & 0xf) >> 0);
                mVersionEx = data[2] + " " + data[3] + " " + date + "v" + data[8] + data[9] + data[10];
                // Log.d("ff", ""+mVersion);
                break;
            }
        }
        super.parseCanboxData(data, len);
    }

    public void updateCanboxSettings() {
        showWarningMsg = Settings.System.getInt(mContext.getContentResolver(), SettingProperties.SHOW_FOCUS_CAR_WARNING_MSG, 0);
        if (showWarningMsg != 0) {
            WarningMsgManager.stop();
        }
    }

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
        byte[] data;

        byte s = 0;
        byte mediaType = 0;

        switch (source) {

            case MyCmd.SOURCE_DVD:
                s = 2;
                mediaType = 0x10;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x08;
                mediaType = 0x13;
                break;
        }

        if (MyCmd.SOURCE_DVD == source) {
            if (play > 0xff) {
                play = 0xff;
            }
            if (total > 0xff) {
                total = 0xff;
            }
            data = new byte[]{(byte) 0xc0, 0x8, s, mediaType, (byte) play, (byte) total, 0, 0, min, sec};
        } else {
            ++play;
            data = new byte[]{(byte) 0xc0, 0x8, s, mediaType, (byte) (play & 0xff), (byte) ((play & 0xff00) >> 8), (byte) (total & 0xff), (byte) ((total & 0xff00) >> 8), min, sec};
        }

        sendDataToCanbox(data, data.length);

    }

    public void setVolume(int volume) {

        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        // setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        byte[] data = new byte[]{(byte) 0xc2, 0x4, b[0], b[1], b[2], 0};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source) {
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
                mediaType = 0x12;
                break;
            default:
                s = 0x00;
                mediaType = 0x0;
                break;
        }
        byte[] data;

        data = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0};

        sendDataToCanbox(data, data.length);

        if (source == MyCmd.SOURCE_MUSIC) {
            Util.doSleep(10);
            setSongName(mName);
            Util.doSleep(10);
            setSongAritst(mArtist);
            Util.doSleep(10);
            setSongAlbum(mAlbum);
        } else if (mSource == MyCmd.SOURCE_MUSIC) {

            Util.doSleep(10);
            sendId3((byte) 0x2, null);
            Util.doSleep(10);
            sendId3((byte) 0x3, null);
            Util.doSleep(10);
            sendId3((byte) 0x4, null);
        }
        mSource = source;
    }

    public void setPhone(int status, String num) {
        sendId3((byte) 0x1, num);
    }

    public void sendId3(byte index, String num) {

        try {
            if (num == null) {
                num = "";
            }
            byte[] n = num.getBytes();

            int num_len = n.length;
            if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
                num_len -= 2;
            }

            int len = n.length + 3;
            if (len > 0x21) {
                len = 0x21;
            }
            byte[] data = new byte[len];

            data[0] = (byte) 0xcb;
            data[1] = (byte) (len - 2);
            data[2] = index;
            System.arraycopy(n, 0 + (n.length - num_len), data, 3, len - 3);
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("PSASimple", "sendId3" + e);
        }
    }

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

    private void sendEQ(byte cmd, byte param) {
        byte[] data = new byte[]{(byte) 0x84, 0x2, cmd, param};
        sendDataToCanbox(data, data.length);
    }

    public void sendEqToCanbox(byte[] eq) {
        //		if (caneq == 1 && eq != null && eq.length >= 12) {
        //
        //			// byte[] data = new byte[] { (byte) 0x84, 0x2, 0, 0 };
        //
        //			byte[] eq2 = new byte[8];
        //
        //			eq2[0] = eq[0];
        //			eq2[1] = eq[1];
        //
        //			eq2[3] = (byte) (eq[2] + eq[3] + eq[4]);
        //			eq2[4] = (byte) (eq[5] + eq[6] + eq[7]);
        //			eq2[5] = (byte) (eq[8] + eq[9] + eq[10]);
        //
        //			eq2[7] = eq[11];
        //			eq2[6] = eq[12];
        //
        //			switch (eq[12]) {
        //			case 2:
        //				eq2[2] = 3;
        //
        //				break;
        //			case 3:
        //				eq2[2] = 1;
        //
        //				break;
        //			case 5:
        //				eq2[2] = 0;
        //
        //				break;
        //			case 4:
        //				eq2[2] = 2;
        //
        //				break;
        //			default:
        //				eq2[2] = 4;
        //				break;
        //			}
        //
        //			byte param;
        //
        //			if (eq2[0] != mEqData[0]) {
        //				param = (byte) (((eq2[0] ) ) + 3);
        //				sendEQ((byte) 3, param);
        //				Util.doSleep(5);
        //			}
        //			if (eq2[1] != mEqData[1]) {
        //				param = (byte) (((eq2[1] ) ) + 3);
        //				sendEQ((byte) 4, param);
        //				Util.doSleep(5);
        //			}
        //
        //			if (eq2[3] != mEqData[3]) {
        //				param = (byte) (((eq2[3] * 12) / 14) + 3);
        //				sendEQ((byte) 5, param);
        //				Util.doSleep(5);
        //			}
        //			if (eq2[4] != mEqData[4]) {
        //				param = (byte) (((eq2[4] * 12) / 14) + 3);
        //				sendEQ((byte) 6, param);
        //				Util.doSleep(5);
        //			}
        //			if (eq2[5] != mEqData[5]) {
        //				param = (byte) (((eq2[5] * 12) / 14) + 3);
        //				sendEQ((byte) 7, param);
        //				Util.doSleep(5);
        //			}
        //
        //			if (eq2[6] != mEqData[6]) {
        //				switch (eq2[6]) {
        //				case 3:
        //					param = 1;
        //					break;
        //				case 4:
        //					param = 2;
        //					break;
        //				case 2:
        //					param = 4;
        //					break;
        //				case 1:
        //					param = 3;
        //					break;
        //				default:
        //					param = eq2[6];
        //					break;
        //				}
        //				sendEQ((byte) 9, param);
        //				Util.doSleep(5);
        //			}
        //
        //			if (eq2[7] != mEqData[7]) {
        //				sendEQ((byte) 0xa, eq2[7]);
        //			}
        //
        //			mEqData = eq2;
        //		}
    }

    public void udpateSet(int data) {
        caneq = data;
        SettingProperties.setIntProperty(mContext, SettingProperties.KEY_CANBOX_EQ, caneq);
    }
}
