package com.zhuchao.android.car.cartype.raise;

import android.os.Handler;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.canbox.WarningMsgManager;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;

public class PetgeoRaise extends Canbox {

    public PetgeoRaise() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x1, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x1, 0x1, 0x2, 0x1
        });

        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        // updateCanboxSettings();
    }

    public void startConnect() {// default is simple box
        byte[] buf = new byte[]{0x4, (byte) 0x8f, 0x38};
        updateTime();
        Util.doSleep(20);
        sendDataToCanbox(buf, buf.length);
        Util.doSleep(20);
        buf[2] = (byte) 0x7f;
        sendDataToCanbox(buf, buf.length);
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{0x6, (byte) 0xa7, 0x03, 3, 0};
        switch (CarUtil.getModelId()) {
            case 5:
                cmd[2] = 0;
                break;
            case 8:
            case 22:
            case 21:
                cmd[2] = 1;
                break;
            case 13:
                cmd[2] = 2;
                break;
            case 40:
                cmd[2] = 4;
                break;
            case 0x50:
                cmd[2] = 0x50;
                break;
            default:
                cmd[2] = 3;
                break;
        }

        switch (CarUtil.getCarTypeConfig()) {
            case 0:
                cmd[3] = 1;
                break;
            case 1:
                cmd[3] = 2;
                break;
            case 2:
                cmd[3] = 3;
                break;
        }
        return cmd;
    }

    private final static byte[][] KEYS_WHEEL = {

            {0x2, KEY_HOME}, {0x3, KEY_PREVIOUSSONG}, {0x4, KEY_NEXTSONG}, {0x7, KEY_PLAYPAUSE}, {0x8, KEY_BACK}, {0x10, KEY_SOURCE}, {0x11, KEY_SOURCE}, {0x12, KEY_SEEK_NEXT}, {0x13, KEY_SEEK_PREV},
            {0x14, AK_KEYPAD_VOLUME_A}, {0x15, AK_KEYPAD_VOLUME_D}, {0x16, KEY_MUTE}, {0x17, KEY_PREVIOUSSONG}, {0x18, KEY_NEXTSONG}, {0x1F, KEY_MIC}, {0x50, KEY_BT},
            {0x20, MyCmd.Keycode.KEY_CAR_INFO},

            {0x21, KEY_MENU}, {0x23, KEY_BT},

            {(byte) 0x17, KEY_PREVIOUSSONG}, {(byte) 0x18, KEY_NEXTSONG}, {0x19, AK_KEYPAD_VOLUME_A}, {0x1a, AK_KEYPAD_VOLUME_D}, {0x22, MyCmd.Keycode.KEY_MEM_INFO},

            {(byte) 0x91, KEY_NUM_1}, {(byte) 0x92, KEY_NUM_2}, {(byte) 0x93, KEY_NUM_3}, {(byte) 0x94, KEY_NUM_4}, {(byte) 0x95, KEY_NUM_5}, {(byte) 0x96, KEY_NUM_6},

            {(byte) 0x44, KEY_NUM_1}, {(byte) 0x45, KEY_NUM_2}, {(byte) 0x46, KEY_NUM_3}, {(byte) 0x47, KEY_NUM_4}, {(byte) 0x48, KEY_NUM_5}, {(byte) 0x49, KEY_NUM_6},

            {(byte) 0x97, KEY_PREVIOUSSONG}, {(byte) 0x98, KEY_NEXTSONG}, {(byte) 0x99, KEY_FM}, {(byte) 0x9a, MyCmd.Keycode.BRIGHTNESS}, {(byte) 0x9b, KEY_HOME}, {(byte) 0x9c, KEY_PREVIOUSSONG},
            {(byte) 0x9d, KEY_NEXTSONG}, {(byte) 0x9e, KEY_MENU}, {(byte) 0x9f, MyCmd.Keycode.MODLE},

            {(byte) 0xa0, MyCmd.Keycode.MULT_SOURCE_AND_BT}, {(byte) 0xa1, KEY_MEDIA}, {(byte) 0xa2, KEY_PLAYPAUSE}, {(byte) 0xa3, KEY_BACK}, {(byte) 0xa4, KEY_MUTE},
            // { (byte)0xa5, KEY_MODE },
            {(byte) 0xa6, KEY_HOME},

            {(byte) 0xb0, KEY_BT_DIAL}, {(byte) 0xb1, KEY_BT_HANG}, {(byte) 0xb2, KEY_BACK}, {(byte) 0xb3, KEY_GPS}, {(byte) 0xb4, MyCmd.Keycode.RDS_TA_SWITCH}, {(byte) 0xb5, KEY_FM},
            {(byte) 0xb6, KEY_MEDIA}, {(byte) 0xb7, KEY_SET}, {(byte) 0xb8, MyCmd.Keycode.KEY_RECENT_APPS}, {(byte) 0xb9, KEY_PREVIOUSSONG}, {(byte) 0xba, KEY_NEXTSONG}, {(byte) 0xbb, KEY_SEEK_PREV},
            {(byte) 0xbc, KEY_SEEK_NEXT}, {(byte) 0xbd, KEY_PLAYPAUSE}, {(byte) 0xbe, MyCmd.Keycode.ROLL_NEXT}, {(byte) 0xbf, MyCmd.Keycode.ROLL_PREV},

            {(byte) 0x81, MyCmd.Keycode.VOLUME_UP}, {(byte) 0x82, MyCmd.Keycode.VOLUME_DOWN},


            {(byte) 0x29, MyCmd.Keycode.SPEECH}, {(byte) 0x2a, MyCmd.Keycode.KEY_CAR_SETTING}, {(byte) 0x30, MyCmd.Keycode.MULT_SOURCE_AND_BT}, {(byte) 0x33, MyCmd.Keycode.RADIO},
            {(byte) 0x2b, MyCmd.Keycode.NAVIGATION}, {(byte) 0x2c, MyCmd.Keycode.RADIO}, {(byte) 0x2d, MyCmd.Keycode.BT}, {(byte) 0x2e, MyCmd.Keycode.SETUP},
            {(byte) 0x2f, MyCmd.Keycode.KEY_AIR_CONTROL}, {(byte) 0x32, MyCmd.Keycode.NAVIGATION}, {(byte) 0x35, MyCmd.Keycode.NAVIGATION},

            {(byte) 0x34, MyCmd.Keycode.SETUP},

            {(byte) 0x36, MyCmd.Keycode.AUDIO},

            {(byte) 0x38, KEY_PREVIOUSSONG}, {(byte) 0x39, KEY_NEXTSONG},

            {(byte) 0x40, KEY_PREVIOUSSONG}, {(byte) 0x41, KEY_NEXTSONG}, {(byte) 0x42, KEY_PREVIOUSSONG}, {(byte) 0x43, KEY_NEXTSONG},


            {(byte) 0x58, KEY_PREVIOUSSONG}, {(byte) 0x59, KEY_NEXTSONG}, {(byte) 0x57, KEY_PREVIOUSSONG}, {(byte) 0x56, KEY_NEXTSONG}, {(byte) 0x5c, KEY_PLAYPAUSE},


            {(byte) 0x4a, KEY_MODE}, {(byte) 0x52, KEY_EQ},

            {(byte) 0x53, MyCmd.Keycode.RDS_TA_SWITCH},

            {(byte) 0x54, MyCmd.Keycode.DARK},

            {(byte) 0x5a, KEY_MENU}, {(byte) 0x5b, KEY_MENU},


            {(byte) 0x5d, KEY_MUTE},

            {(byte) 0x55, KEY_EJECT}, {(byte) 0x5e, KEY_BACK},


            {(byte) 0x80, KEY_POWER},

            {0x30, MyCmd.Keycode.BT_DIAL}, {0x31, MyCmd.Keycode.BT_HANG}, {0x51, MyCmd.Keycode.SETUP}, {0x61, MyCmd.Keycode.BACK},

    };

    int mKey;

    private void parseWheelKey(byte[] data, int len) {
        //		if (data[2] == (byte) 0xa5) {
        //			if (data[3] == 1) {
        //				Handler handler = getHandler("CanService");
        //				if (null != handler) {
        //					handler.sendMessage(handler.obtainMessage(
        //							CANBOX_RETURN_AIR, airData));
        //				}
        //			}
        //		}
        //		else {


        int key = 0;
        for (int i = 0; i < KEYS_WHEEL.length; ++i) {
            if (KEYS_WHEEL[i][0] == data[2]) {
                key = KEYS_WHEEL[i][1];
                break;
            }
        }

        if (data.length == 4) {
            if (data[2] == 0) {
                if (doKeyStudy(0, 0)) {
                    return;
                }
                if (mKey != 0) {
                    doKey(mKey, 0);
                    mKey = 0;
                }
            } else {
                if (doKeyStudy(data[2], 1)) {
                    return;
                }
                if (key != 0) {
                    doKey(key, 1);
                    mKey = key;
                }
            }
        } else {
            if (doKeyStudy(data[2], data[3])) {
                return;
            }
            if (data[3] == 0) {
                if (mKey != 0) {
                    doKey(mKey, data[3]);
                    mKey = 0;
                }
            } else {
                if (key != 0) {
                    doKey(key, data[3]);
                    mKey = key;
                }
            }
        }

        //		}
    }

    byte[] airData = new byte[8];

    private void parseACInfo(byte[] data, int len) {
        int windMode = 0;
        boolean airControl = false;


        //
        //			if (data[4] >= 0x7f) {
        //				data[4] = (byte) 0xff;
        //			} else if (data[4] >= 0x1f && data[4] <= 0x3B) {
        //				data[4] = (byte) ((15.5f + (0.5f * (data[4] - 0x1f))) * 2);
        //			} else {
        //				data[4] = 0;
        //			}
        //
        //			if (data[5] >= 0x7f) {
        //				data[5] = (byte) 0xff;
        //			} else if (data[5] >= 0x1f && data[5] <= 0x3B) {
        //				data[5] = (byte) ((15.5f + (0.5f * (data[5] - 0x1f))) * 2);
        //			} else {
        //				data[5] = 0;
        //			}


        airData[0] = (byte) (data[2] & 0xef);
        airData[0] |= (byte) (((data[6] & 0x80) >> 6));


        byte wind = (byte) (data[3] & 0x0f);
        if (wind == 0) {
            airData[0] |= 0x8;
        } else {
            --wind;
        }
        airData[1] = (byte) (data[3] & 0xf0);
        airData[1] |= wind;

        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        airData[4] = (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x08) >> 1) | ((data[6] & 0x04) << 1));

        airData[4] |= ((data[2] & 0x10) << 3);

        airData[5] = (byte) (((data[6] & 0x1)));


        //		airData[6] = (byte) (((data[10] & 0xe0)));
        if (airData[6] != 0) {
            windMode = 1;
        }

        //		if((data[6] & 0x2)==0){
        //			airData[7] = 0x40;
        //		} else {
        //			airData[7] = 0x0;
        //		}


        ////		if (airControl) {
        //			Handler handler = getHandler("CanService");
        //			if (null != handler) {
        //				handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR,
        //						windMode, 0, airData));
        //			}
        ////		}
        airData[5] |= 0x80;
        super.parseACInfo(airData);
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

    int mRadarSwitch;

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        //		--len;
        //		byte[] data = new byte[len];
        //		byteArrayCopy(d, data, 0, 1, len);

        switch (data[1]) {
            case 0x2: {
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
                mRadarSwitch = data[2];
                if (data[2] == 0x2) {
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
                } else {
                    RadarManager.stop();
                }
            }
            break;
            case 0x30: // Radar front
            {
                // byteArrayCopy(mRadar, data, 4, 2, 4);

                mRadarLeft[0] = getRadarData(data[3]);
                mRadarLeft[1] = getRadarData(data[4]);
                //			mRadarLeft[2] = getRadarData(data[4]);
                mRadarLeft[3] = getRadarData(data[5]);


                mRadarRight[0] = getRadarData(data[6]);
                mRadarRight[1] = getRadarData(data[7]);
                //			mRadarRight[2] = getRadarData(data[7]);
                mRadarRight[3] = getRadarData(data[8]);

                //			int[] radarColor = new int[4];


                if (mRadarSwitch == 0x2) {
                    boolean zero = Util.isZero(mRadar);
                    if (!zero) {
                        RadarManager.start(mContext);
                        checkHideRadarEx(2000);
                    }
                    Handler handler = getHandler(RadarManager.TAG);
                    if (null != handler) {
                        //					checkHideRadar();
                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_LEFT));


                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_RIGHT));
                    }
                }
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
                    // ma

                    int angle = ((a * 3000) / 5450);

                    if (angle > -50 && angle < 50) {
                        angle = 50;
                    }
                    // Log.e("1", ""+(data[2] & 0xff));
                    // Log.e("2", ""+(data[3] & 0xff));
                    // Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
                }
            }
            break;
            case 0x33:
            case 0x34:
            case 0x35:
            case 0x3B:
            case 0x41:
            case 0x39:

                sendCanboxInfo("com.canboxsetting", data);
                break;

            case 0x7f: {
                int v_len = len - 2;
                byte[] version = new byte[v_len];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
            case 0x71: {
                // byte[] version = new byte[9];
                // Util.byteArrayCopy(version, data, 0, 2, version.length);
                String date = ((data[4] & 0xf0) >> 4) + String.valueOf((data[4] & 0xf) >> 0) + "-" + ((data[5] & 0xf0) >> 4) + ((data[5] & 0xf) >> 0) + "-" + ((data[6] & 0xf0) >> 4) + ((data[6] & 0xf) >> 0);
                mVersionEx = data[2] + " " + data[3] + " " + date + "v" + data[8] + data[9] + data[10];
                //			Log.d("ff", ""+mVersion);
                break;
            }
        }
    }

    private int showWarningMsg = -1;

    public void updateCanboxSettings() {

        showWarningMsg = Settings.System.getInt(mContext.getContentResolver(), SystemConfig.SHOW_FOCUS_CAR_WARNING_MSG, 0);
        if (showWarningMsg != 0) {
            WarningMsgManager.stop();
        }
    }

    private int mDoorStatus = 0;

    public void setReverseRadaVol(byte param) {
        //		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x0, param };
        //		sendDataToCanbox(data, data.length);
    }

    public void setParkCarMode(byte param) {
        //		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x2, param };
        //		sendDataToCanbox(data, data.length);
    }

    public void requestInfo(byte param) {
        //		byte[] data = new byte[] { (byte) 0x90, 0x2, param, 0 };
        //		sendDataToCanbox(data, data.length);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        //		++play;
        byte[] data;

        byte s = 0;

        switch (source) {

            case MyCmd.SOURCE_DVD:
                s = 2;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x08;
                break;
        }

        if (MyCmd.SOURCE_DVD == source) {

        } else {
            ++play;

        }

        data = new byte[]{
                0x9, (byte) 0xc0, s, 1, (byte) ((total & 0xff00) >> 8), (byte) (total & 0xff), (byte) ((play & 0xff00) >> 8), (byte) (play & 0xff)
        };

        sendDataToCanbox(data, data.length);

    }

    private final int mSource = MyCmd.SOURCE_NONE;
    private final int mBaud = 0;

    public void setVolume(int volume) {

        byte[] data = new byte[]{0x4, (byte) 0xc4, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        if (b[0] >= 0x10) {
            b[0] = 0x10;
        } else {
            b[0] = 0;
        }
        byte[] data = new byte[]{0x9, (byte) 0xc0, 1, 1, b[0], b[2], b[1], 0};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source) {
        byte s = 0;
        switch (source) {
            case 0:
                return;
            case 1:
                s = 2;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 6;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x08;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0b;
                break;
            case MyCmd.SOURCE_AV_OFF:
                s = 0x0;
                break;
            default:
                s = 0xc;
                break;
        }
        byte[] data;


        data = new byte[]{0x9, (byte) 0xc0, s, 1, 0, 0, 0, 0};

        sendDataToCanbox(data, data.length);

    }

    String mName = null;
    String mArtist = null;
    String mAlbum = null;

    public void setPhone(int status, String num) {
        sendId3((byte) 0x1, num);
    }

    public void sendId3(byte index, String num) {

        //		try {
        //			if (num == null) {
        //				num = "";
        //			}
        //			byte[] n = num.getBytes();
        //
        //			int num_len = n.length;
        //			if((n[0]&0xff) == 0xff && (n[1]&0xff) == 0xfe){
        //				num_len-=2;
        //			}
        //
        //			int len = n.length + 3;
        //			if (len > 0x21) {
        //				len = 0x21;
        //			}
        //			byte[] data = new byte[len];
        //
        //			data[0] = (byte) 0xcb;
        //			data[1] = (byte) (len - 2);
        //			data[2] = index;
        //			for (int i = 0; i < (len - 3); ++i) {
        //				data[3 + i] = n[i+(n.length-num_len)];
        //			}
        //			sendDataToCanbox(data, data.length);
        //		} catch (Exception e) {
        //
        //			Log.d("PSASimple", "sendId3"+e);
        //		}
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

    private byte Sum(byte[] data, int len) {
        byte sum = 0;

        for (int i = 0; i < len; ++i) {
            sum += data[i];
        }
        return (byte) (sum & 0xFF);
    }

    public void sendDataToCanbox(byte[] data, int len) {
        byte[] send = new byte[len + 3];
        send[0] = (byte) (len + 2);
        send[1] = (byte) 0xfd;
        send[len + 2] = Sum(data, len);
        byteArrayCopy(send, data, 2, 0, len);
        sendCommonDataToCanbox(send);
    }

    public void stopConnect() {// default is simple box

    }

    public void updateTime() {
        if (mContext == null) {
            return;
        }
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);


        byte m = (byte) curDate.getMinutes();


        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();

        byte[] buf = new byte[]{0x8, (byte) 0xa6, y, mon, d, h, m};
        sendDataToCanbox(buf, buf.length);
    }

    public int getUpdateTime() {
        return 60000;
    }
}
