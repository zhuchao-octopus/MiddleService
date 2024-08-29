package com.zhuchao.android.car.cartype.simple;

import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.common.utils.AppConfig;
import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;


public class CarHondaDASimple extends Canbox {

    private final static int[][] KEYS_WHEEL = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG}, {0x7, KEY_SOURCE}, {0x8, KEY_MIC}, {0x9, KEY_BT_DIAL}, {0xa, MyCmd.Keycode.MULT_BACK_AND_HANG},

            {0x14, KEY_NEXTSONG}, {0x13, KEY_PREVIOUSSONG}, {0x16, KEY_PLAYPAUSE},

            {0x17, KEY_MENU}, {0x18, KEY_HOME},

            {(byte) 0x81, AK_KEYPAD_VOLUME_A}, {(byte) 0x82, AK_KEYPAD_VOLUME_D}, {(byte) 0x86, KEY_MUTE},

    };
    private final static byte[] RADAR_CHANGE = new byte[]{1, 5, 7, 9, 11, 14};
    private final byte mSource = 0x6;
    byte[] mAirData = new byte[8];
    String mName = null;
    String mArtist = null;
    String mAlbum = null;
    private byte[][] mKeyPannel;
    private boolean mRadarSwitch = false;
    private int mDoorStatus = 0;
    private byte[] mEq = null;
    private byte mVolume;
    private int mRightCameraSwitch = 0;

    public CarHondaDASimple() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x0, 0x0, 0x0, 0x1});
    }

    // public void setMediaMoreInfo(int source, int play, int total, int time,
    // int total_time) {
    // // byte min = (byte) ((time / 60) % 60);
    // // byte sec = (byte) ((time) % 60);
    // // ++play;
    // // byte[] data = new byte[] { (byte) 0xa3, 0x1, (byte) (total & 0xFF),
    // // (byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF),
    // // (byte) ((play >> 8) & 0xFF), min, sec };
    // // sendDataToCanbox(data, data.length);
    // }
    //
    // public void setMediaSrc(int source, byte type, byte[] b) {
    // if (b[0] == 0x3) {
    // b[0] = 5;
    // } else {
    // b[0] = 1;
    // }
    // byte[] data = new byte[] { (byte) 0x9a, 0x5, 8, b[0], b[1], b[2], 0 };
    // sendDataToCanbox(data, data.length);
    // }
    //
    // public void setMediaSrc(int source) {// default is simple box
    // switch (source) {
    // case MyCmd.SOURCE_DVD:
    // mSource = 0x2;
    // break;
    // case MyCmd.SOURCE_RADIO:
    // mSource = 0x1;
    // break;
    // case MyCmd.SOURCE_AUX:
    // mSource = 0x4;
    // break;
    // case MyCmd.SOURCE_BT:
    // mSource = 0x7;
    // break;
    // default:
    // mSource = 0x6;
    // break;
    // }
    //
    // byte[] data = new byte[] { (byte) 0x99, 0x2, mSource, mVolume };
    // sendDataToCanbox(data, data.length);
    // }

    private void parseWheelKey(byte[] data) {
        if (doKeyStudy(data[2], data[3])) {
            return;
        }
        int key = 0;
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

    private void parsePannelKey(byte[] data) {
        byte key = 0;
        if (mKeyPannel == null) {
            return;
        }
        for (int i = 0; i < mKeyPannel.length; ++i) {
            if (mKeyPannel[i][0] == data[2]) {
                key = mKeyPannel[i][1];
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
        //		if (data[4] >= 0x1f){
        //			data[4] = (byte)0xff;
        //		} else if(data[4] > 0){
        //			data[4] = (byte)((17.5f + (0.5f * data[4]))*2);
        //		}
        //		if (data[5] >= 0x1f){
        //			data[5] = (byte)0xff;
        //		} else if(data[5] > 0){
        //			data[5] = (byte)((17.5f + (0.5f * data[5]))*2);
        //		}

        byte[] airData = new byte[8];
        airData[0] = (byte) ((data[2] & 0xec) | ((data[6] & 0x80) >> 6) | ((data[6] & 0x04) >> 2));
        airData[7] = (byte) ((data[2] & 0x10) << 3);


        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);


        airData[4] = (byte) ((data[2] & 0x1) << 3);

        airData[5] = (byte) ((data[6] & 0x1));

        boolean airControl = false;
        if (airData[0] != (byte) (mAirData[0] & 0xff) || airData[1] != (byte) (mAirData[1] & 0xff) || airData[2] != (byte) (mAirData[2] & 0xff) || airData[3] != (byte) (mAirData[3] & 0xff) || airData[4] != (byte) (mAirData[4] & 0xff) || airData[7] != (byte) (mAirData[7] & 0xff)) {
            airControl = true;

            mAirData = airData;

        }

        int msg = CANBOX_HIDE_AIR;
        if (/*(data[2] & 0x80) != 0 && */((data[3] & 0x10) != 0)) {
            msg = CANBOX_RETURN_AIR;
        }

        if (airControl) {
            Handler handler = getHandler("CanService");
            if (null != handler) {
                handler.sendMessage(handler.obtainMessage(msg, airData));
            }
        }

        if ((data[6] & 0x40) != 0) {

            if (isShowAir()) {

                try {
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.putExtra(MyCmd.EXTRA_COMMON_CMD, 1);
                    it.setClassName("com.canboxsetting", "com.canboxsetting.CanAirControlActivity");
                    it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(it);
                } catch (Exception e) {
                    // Log.e(TAG, e.getMessage());
                }
            } else {
                sendCanboxInfo("com.canboxsetting", data);
            }

        } else {
            sendCanboxInfo("com.canboxsetting", data);
        }
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data);
                break;
            }
            //		case 0x02: {
            //			parsePannelKey(data);
            //
            //		}
            //			break;
            case 0x21: {
                if (data[1] == 9) {
                    parseACInfo(data, len);
                }
            }
            break;
            case 0x22: // Radar back
            {
                byteArrayCopy(mRadar, data, 0, 2, 4);
                for (int i = 0; i < 4; ++i) {
                    if (mRadar[i] != 1) {
                        mRadar[i] *= 2;
                    }
                }

                if (mRadarSwitch) {
                    boolean zero = Util.isZero(mRadar);
                    if (!zero) {
                        RadarManager.start(mContext);
                        checkHideRadarEx(5000);
                    }
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                }
                //			checkHideRadar();
            }
            break;
            case 0x23: // Radar front
            {
                byteArrayCopy(mRadar, data, 4, 2, 4);
                for (int i = 4; i < 8; ++i) {
                    if (mRadar[i] != 1) {
                        mRadar[i] *= 2;
                    }
                }
                if (mRadarSwitch) {
                    boolean zero = Util.isZero(mRadar);
                    if (!zero) {
                        RadarManager.start(mContext);
                        checkHideRadarEx(5000);
                    }
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                }
                //			checkHideRadar();
            }

            break;
            case 0x25: // Radar status
            {

                mRadarSwitch = (data[2] & 0xc) != 0;

                //			byteArrayCopy(mRadar, data, 0, 2, 4);
                if (mRadarSwitch) {
                    checkHideRadarEx(5000);
                    //				RadarManager.start(mContext);
                    Handler handler = getHandler(RadarManager.TAG);
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                    }
                } else {
                    RadarManager.stop();
                }
            }
            break;
            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu
                    // ma

                    int angle = -(((a * 3000) / 4608));

                    if (angle > -50 && angle < 0) {
                        angle = -50;
                    } else if (angle > 0 && angle < 50) {
                        angle = 50;
                    }
                    // Log.e("1", ""+(data[2] & 0xff));
                    // Log.e("2", ""+(data[3] & 0xff));
                    // Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 100));
                }
            }
            break;
            case 0x24: {
                int door = (data[2] & 0xfc);

                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((door & 0x04) << 3));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
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
            case (byte) 0xd1:
                if (CarUtil.getRightCameraExist()) {
                    if ((data[3] & 0x80) != 0) {
                        doRightCameraSwitch(1);
                    } else {
                        if ((mRightCameraSwitch & 0x80) != 0) {
                            doRightCameraSwitch(0);
                        } else {
                            if ((data[3] & 0x40) != 0) {
                                if ((mRightCameraSwitch & 0x40) == 0) {
                                    toggleCameraSwitch();
                                }
                            } else {

                            }
                        }
                    }

                    mRightCameraSwitch = data[3];
                }
                //			}
                break;
            case 0x32:
            case 0x33:
            case (byte) 0xd0:
                sendCanboxInfo("com.canboxsetting", data);
                break;
            //		case 0x8:
            //		case 0x9:
            //			sendCanboxInfo("com.android.car.bt", data);
            //			break;
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

    public void sendEqToCanbox(byte[] eq) {
        if (eq != null && eq.length >= 13) {
            if (mEq == null) {
                mEq = new byte[eq.length];
                Util.byteArrayCopy(mEq, eq, 0, 0, eq.length);
                return;
            }

            byte type = 0;
            if (eq[0] != mEq[0]) {
                type = 2;
            } else if (eq[1] != mEq[1]) {
                type = 1;
            } else if (eq[2] != mEq[2] || eq[3] != mEq[3] || eq[4] != mEq[4] || eq[5] != mEq[5]) {
                type = 3;
            } else {
                type = 4;
            }

            byte[] buf = new byte[10];
            int i;
            buf[0] = (byte) 0x98;
            buf[1] = 0x8;
            buf[2] = type;

            i = (eq[0] * 180 / 14);
            if (i % 10 > 0) {
                i /= 10;
                ++i;
            } else {
                i /= 10;
            }

            buf[4] = (byte) i;
            i = (eq[1] * 180 / 14);

            if (i % 10 > 0) {
                i /= 10;
                ++i;
            } else {
                i /= 10;
            }

            buf[3] = (byte) i;

            buf[5] = (byte) ((eq[2] + eq[3] + eq[4] + eq[5]) / 4);
            buf[6] = (byte) ((eq[6] + eq[7] + eq[8] + eq[9] + eq[10]) / 5);
            buf[7] = eq[11];

            switch (eq[12]) {
                case 4:
                    buf[9] = 1;
                    break;
                case 3:
                    buf[9] = 5;
                    break;
                case 5:
                    buf[9] = 3;
                    break;
                case 2:
                    buf[9] = 2;
                    break;
                case 1:
                    buf[9] = 4;
                    break;
                default:
                    buf[9] = 0;
                    break;
            }

            sendDataToCanbox(buf, buf.length);

            Util.byteArrayCopy(mEq, eq, 0, 0, eq.length);
        }
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        //		++play;
        byte[] data;

        if (MyCmd.SOURCE_DVD != source) {

            ++play;
            data = new byte[]{(byte) 0xc3, 0x6, (byte) (total & 0xFF), (byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF), (byte) ((play >> 8) & 0xFF), min, sec};
        } else {
            data = new byte[]{(byte) 0xc3, 0x6, (byte) (1 & 0xFF), (byte) ((play) & 0xFF), (byte) (total & 0xFF), (byte) ((0) & 0xFF), min, sec};
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
                s = 0x08;
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
        //		if (s == 0xb || s == 0x7) {
        //			data = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0,
        //					0 };
        //		} else {
        data = new byte[]{(byte) 0xc0, 0x2, s, mediaType};
        //		}

        sendDataToCanbox(data, data.length);
    }

    public void setVolume(int volume) {

        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    public void updateCanboxKeySettings() {
        //		if (CarUtil.getKeyType()==2) {
        //			mKeyPannel = KEYS_PANNEL_GL8;
        //		} else if (CarUtil.getKeyType()==1) {
        //			mKeyPannel = KEYS_PANNEL_ENVISION_L;
        //		} else {
        //			mKeyPannel = KEYS_PANNEL_NORMAL;
        //		}
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

            int len = num_len + 3;
            if (len > 33) {
                len = 33;
            }
            byte[] data = new byte[len];

            data[0] = (byte) 0xcb;
            data[1] = (byte) (len - 2);
            data[2] = index;
            System.arraycopy(n, 0 + (n.length - num_len), data, 3, num_len);
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("CarHondaDASimple", "sendId3" + e);
        }
    }

    public void setPhone(int status, String num) {
        sendId3((byte) 0x1, num);
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

            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }


            h |= 0x80;
        }

        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        byte[] buf = new byte[]{(byte) 0xc6, 0x04, 0x50, h, m, s};
        sendDataToCanbox(buf, buf.length);
    }

    public int getUpdateTime() {
        return 60000;
    }

    private void toggleCameraSwitch() {
        String top = AppConfig.getTopActivity();
        Intent it = new Intent(Intent.ACTION_VIEW);
        if ((top != null && top.contains("com.android.car.frontcamera.SideCameraActivity"))) {
            it.putExtra("finish", 1);
        } else {
            it.putExtra("style", 1);
            it.putExtra("camera", MyCmd.CAMERA_SOURCE_DTV_CVBS);
        }

        try {
            it.setClassName("com.car.ui", "com.android.car.frontcamera.SideCameraActivity");
            it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

            mContext.startActivity(it);
        } catch (Exception e) {
            //			Log.e(TAG, e.getMessage());
        }
    }

    private void doRightCameraSwitch(int s) {

        String top = AppConfig.getTopActivity();

        Intent it = new Intent(Intent.ACTION_VIEW);
        boolean topIsCamera = top != null && top.contains("com.android.car.frontcamera.SideCameraActivity");
        /*|| ((mRightCameraSwitch & 0xC0) != 0)*/

        if (s == 0) {
            if (topIsCamera) {
                it.putExtra("finish", 1);
            }
        } else if (s == 1) {
            if (!topIsCamera) {
                topIsCamera = true;
                it.putExtra("style", 1);
                it.putExtra("camera", MyCmd.CAMERA_SOURCE_DTV_CVBS);
            }
        }
        //		else if (s == 2){
        //
        //			if (topIsCamera){
        //				it.putExtra("finish", 1);
        //			} else {
        //				it.putExtra("style", 1);
        //			}
        //			topIsCamera = true;
        //		}

        if (topIsCamera) {
            try {
                it.setClassName("com.car.ui", "com.android.car.frontcamera.SideCameraActivity");
                it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

                mContext.startActivity(it);
            } catch (Exception e) {
                //				Log.e(TAG, e.getMessage());
            }
        }
    }
}
