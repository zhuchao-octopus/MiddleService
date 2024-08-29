package com.zhuchao.android.car.cartype.hiworld;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class TouaregHiworld extends Canbox {

    private final static byte[][] KEYS_WHEEL = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x9, KEY_NEXTSONG}, {0x8, KEY_PREVIOUSSONG}, {0x5, KEY_BT_DIAL}, {0x6, KEY_BT_HANG}, {0x3, KEY_MUTE}, {0xa, KEY_SOURCE},

    };
    private final static byte[][] KEYS_WHEEL2 = {{0x2, KEY_NEXTSONG}, {0x1, KEY_PREVIOUSSONG}, {0x3, MyCmd.Keycode.FAST_F}, {0x4, MyCmd.Keycode.FAST_R}, {0x11, MyCmd.Keycode.BT_DIAL}, {0x12, MyCmd.Keycode.BT_HANG}, {0x14, KEY_HOME}, {0x17, KEY_MIC}, {0x19, MyCmd.Keycode.KEY_BT_VOICE_SPEAKER}, {0x18, MyCmd.Keycode.KEY_BT_VOICE_PHONE}, {0x30, KEY_BACK},

    };
    private final static int HIDE_RADAR = 0;
    private final byte[] mAirData = new byte[8];
    private final int[] mRadarColor = new int[8];
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_RADAR) {
                RadarManager.stop();
            }
            super.handleMessage(msg);
        }
    };
    private byte mKey;
    private byte[] mACOriginalData;
    private byte mRadarSwitch = 0;
    private int mDoorStatus = 0;

    public TouaregHiworld() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x2, 0x0, 0x3, 0x2});
    }

    @Override
    public void stopConnect() {

    }

    private void parseWheelKey(byte data) {
        if (doKeyStudy(data, (data == 0) ? 0 : 1)) {
            return;
        }

        byte key = 0;
        for (int i = 0; i < KEYS_WHEEL.length; ++i) {
            if (KEYS_WHEEL[i][0] == data) {
                key = KEYS_WHEEL[i][1];
                break;
            }
        }

        if (data == 0 && mKey != 0) {
            doKey(key, 0);
            mKey = -1;
        } else if (data != 0 && key != 0) {
            doKey(key, 1);
            mKey = key;
        }
    }

    public boolean returnACDataToCanboxSetting() {
        if (mACOriginalData != null) {
            sendCanboxInfo("com.canboxsetting", mACOriginalData);
            return true;
        }
        return false;
    }

    private void parseACInfo(byte[] data, int len) {

        if (CarUtil.isShowAC()) {
            mACOriginalData = data;
            sendCanboxInfo("com.canboxsetting", data);
            //	return;
        }

        byte[] airData = new byte[8];

        airData[0] = (byte) (((data[3] & 0x40) << 1) | ((data[4] & 0x40) >> 5));
        if ((data[3] & 0x3) == 1) {
            airData[0] |= 0x10;
        } else if ((data[3] & 0x3) == 2) {
            airData[7] |= 0x10;
        } else {
            airData[0] |= 0x08;
        }

        if ((data[4] & 0x8) != 0) {
            airData[4] |= 0x80;
        }

        if ((data[7] & 0xff) == 0x3) {
            airData[1] |= 0x20;
        } else if ((data[7] & 0xff) == 0x5) {
            airData[1] |= 0x60;
        } else if ((data[7] & 0xff) == 0x6) {
            airData[1] |= 0x40;
        } else if ((data[7] & 0xff) == 0xb) {
            airData[1] |= 0x80;
        } else if ((data[7] & 0xff) == 0xc) {
            airData[1] |= 0xa0;
        } else if ((data[7] & 0xff) == 0xd) {
            airData[1] |= 0xc0;
        } else if ((data[7] & 0xff) == 0xe) {
            airData[1] |= 0xe0;
        }

        airData[1] |= (data[8] & 0x0f);

        if ((data[9] & 0xff) == 0xff) {
            airData[2] = data[9];
        } else if ((data[9] & 0xff) == 0xfe) {
            airData[2] = 0;
        } else {
            airData[2] = data[9];
        }

        if ((data[10] & 0xff) == 0xfe) {
            airData[3] = 0;
        } else {
            airData[3] = data[10];
        }
        airData[5] |= 0x80;
        //
        // airData[0] |= (byte) (((data[6] & 0x40) >> 6));
        //
        // airData[1] = (byte) (data[3] & 0xff);
        // airData[2] = (byte) (data[4] & 0xff);
        // airData[3] = (byte) (data[5] & 0xff);
        //
        // airData[4] = (byte) (data[7] & 0xff);
        // airData[4] |= (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x8) >>
        // 1) | ((data[2] & 0x1) << 3));
        //
        // airData[5] = (byte) ((data[6] & 0x1));
        //
        // airData[7] = (byte) (((data[6] & 0x10) >> 4));
        // airData[7] |= (byte) (((data[6] & 0x80) >> 2));
        //
        // int msg = CANBOX_HIDE_AIR;
        // if ((data[3] & 0x10) != 0) {
        // msg = CANBOX_RETURN_AIR;
        // }

        if (Util.isBufEquals(mAirData, airData) || Util.isZero(airData)) {
            return;
        } else {
            Util.byteArrayCopy(mAirData, airData, 0, 0, mAirData.length);
        }

        Handler handler = getHandler("CanService");
        if (null != handler) {
            int msg = CANBOX_HIDE_AIR;
            if (/*(data[2] & 0x80) != 0 && */((data[3] & 0x40) != 0)) {
                msg = CANBOX_RETURN_AIR;
            }
            handler.sendMessage(handler.obtainMessage(msg, airData));
        }
    }

    private int getRadarColor(int i) {
        int color = Color.GREEN;
        switch (i) {
            case 0x1:
                color = Color.WHITE;
                break;
            case 0x2:
                color = Color.YELLOW;
                break;
            case 0x3:
                color = Color.RED;
                break;

        }
        return color;
    }

    private byte getRadarData(byte d) {
        int i = (d & 0xff);
        byte data = 0;

        //		int step = 255 / 11;
        //		for (byte i = 0; i < 11; ++i) {
        //			if (radar >= (step * i)) {
        //				data = (byte) (i + 1);
        //			}
        //		}
        //
        //		return data;

        if (i > 0x0 && i <= 25) {
            data = 1;
        } else if (i >= 25 && i <= 50) {
            data = 2;
        } else if (i >= 50 && i <= 75) {
            data = 3;
        } else if (i >= 75 && i <= 101) {
            data = 4;
        } else if (i >= 101 && i <= 122) {
            data = 5;
        } else if (i >= 122 && i <= 150) {
            data = 6;
        } else if (i >= 150 && i <= 180) {
            data = 7;
        } else if (i >= 180 && i <= 210) {
            data = 8;
        } else if (i >= 210 && i <= 230) {
            data = 9;
        } else if (i >= 230 && i <= 245) {
            data = 10;
        } else if (i >= 245 && i <= 254) {
            data = 11;
        } else {
            data = 0;
        }
        return data;
    }

    private boolean isDataZero(byte[] data, int len) {

        Log.d("TouaregHiworld", data[2] + ":isDataZero:" + len);
        for (int i = 3; i < len; ++i) {
            if (data[i] != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isDataAll0xFF(byte[] data, int start, int len) {

        //		Log.d("TouaregHiworld", data[2]+":isDataZero:"+len);
        for (int i = start; i < (len + start); ++i) {
            if ((data[i] & 0xff) != 0xff) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[2]) {
            case 0x11: {
                parseWheelKey(data[5]);

                //			if ((data[12] & 0xff) != 0) {
                int door = (data[12] & 0xfc);
                door = (((door & 0x40) >> 5) | ((door & 0x80) >> 7) | ((door & 0x10) >> 1) | ((door & 0x20) >> 3) | ((door & 0x08) << 1) | ((door & 0x4) << 3));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }
                //			}

                if ((data[3] & 0x20) == 0) {
                    RadarManager.stop();
                    mRadarSwitch = 0;
                } else {
                    mRadarSwitch = 1;
                }

                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    int angle = ((data[10] & 0xff) | ((data[9]) << 8));
                    angle = -(angle * 300 / 480);
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));

                }

            }
            break;
            case 0x31: {
                if (isDataZero(data, data[1])) {
                    break;
                }
                parseACInfo(data, len);
                sendCanboxInfo("com.canboxsetting", data);
            }
            break;

            case 0x41: // Radar back
            {
                // byteArrayCopy(mRadar, data, 0, 2, 4);
                if (isDataZero(data, data[1])) {
                    break;
                }

                if (isDataAll0xFF(data, 3, 8)) {
                    break;
                }
                mRadar[0] = getRadarData(data[3]);
                mRadar[1] = getRadarData(data[4]);
                mRadar[2] = getRadarData(data[5]);
                mRadar[3] = getRadarData(data[6]);

                mRadar[4] = getRadarData(data[7]);
                mRadar[5] = getRadarData(data[8]);
                mRadar[6] = getRadarData(data[9]);
                mRadar[7] = getRadarData(data[10]);

                // mRadarColor[0] = getRadarColor((data[6] & 0xf0) >> 4);
                // mRadarColor[1] = getRadarColor((data[6] & 0xf) >> 0);
                // mRadarColor[2] = getRadarColor((data[7] & 0xf0) >> 4);
                // mRadarColor[3] = getRadarColor((data[7] & 0xf) >> 0);
                if (mRadarSwitch == 1) {
                    boolean zero = Util.isZero(mRadar);
                    if (!zero) {
                        RadarManager.start(mContext);
                        checkHideRadarEx(2000);
                    }
                    Handler handler = getHandler(RadarManager.TAG);
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                    }
                }
            }
            break;

            //		case 0x30: {
            //			byte[] version = new byte[16];
            //			Util.byteArrayCopy(version, data, 0, 2, version.length);
            //
            //			mVersion = (new String(version));
            //			// version
            //			break;
            //		}

            case (byte) 0xc3:
            case (byte) 0xc1:

            case 0x46:
            case 0x48:
            case 0x68:
            case 0x64:
            case 0x71:

                //
                sendCanboxInfo("com.canboxsetting", data);
                break;
        }

    }

    public void startConnect() {
        byte[] data = new byte[]{0x3, (byte) 0x6a, 0x5, 1, 0};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte h = (byte) ((time / 3600));
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);

        // byte[] data;
        //

        // sendDataToCanbox(data, data.length);

        byte[] data = new byte[15];
        data[0] = (byte) (data.length - 2);
        data[1] = (byte) 0xe1;
        for (int i = 3; i < 15; ++i) {
            data[i] = 0x20;
        }

        if (MyCmd.SOURCE_DVD == source) {
            data[2] = 7;
        } else {
            data[2] = 0xd;
            ++play;
        }

        String s = play + " ";

        if (h != 0) {
            if (h < 10) {
                s += "0";
            }
            s += h + ":";
        }
        if (min < 10) {
            s += "0";
        }
        s += min + ":";
        if (sec < 10) {
            s += "0";
        }
        s += +sec;

        int start = 3;
        for (int i = 0; (i + start) < data.length && i < s.length(); ++i) {
            data[i + start] = (byte) s.charAt(i);
        }

        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        // setMediaSrc(0);

        byte[] data = new byte[15];
        data[0] = (byte) (data.length - 2);
        data[1] = (byte) 0xe1;
        for (int i = 3; i < 15; ++i) {
            data[i] = 0x20;
        }

        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        String s;

        if (b[0] != 0x10) {
            data[2] = 1;

            s = (freq / 100) + ".";
            if ((freq % 100) < 10) {
                s += "0";
            }
            s += (freq % 100);
            s += "MHZ";

            int start = 6;
            if (freq < 10000) {
                start = 7;
            }
            for (int i = 0; (i + start) < data.length && i < s.length(); ++i) {
                data[i + start] = (byte) s.charAt(i);
            }
        } else {
            data[2] = 4;

            s = freq + "KHZ";
            int start = 8;
            if (freq < 1000) {
                start = 9;
            }
            for (int i = 0; (i + start) < data.length && i < s.length(); ++i) {
                data[i + start] = (byte) s.charAt(i);
            }

        }

        sendDataToCanbox(data, data.length);

    }

    public void setMediaSrc(int source) {// default is simple box
        byte s = 0;
        switch (source) {
            case 0:
                s = -1;
                break;
            case 1:
                s = 7;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 6;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x0d;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x0c;
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x08;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0a;
                break;
            default:
                s = 0x00;
                break;
        }

        if (s != -1) {
            byte[] data = new byte[15];
            data[0] = (byte) (data.length - 2);
            data[1] = (byte) 0xe1;
            for (int i = 3; i < 15; ++i) {
                data[i] = 0x20;
            }
            data[2] = s;

            sendDataToCanbox(data, data.length);
        }
    }

    //	public void setVolume(int volume) {
    //
    //		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
    //		sendDataToCanbox(data, data.length);
    //	}

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        super.sendDataToCanboxHiword1(data, len);
    }

    public byte sum(byte[] data, int len) {
        byte sum = 0;
        for (int i = 0; i < len; ++i) {
            sum += data[i];
        }
        sum = (byte) ((sum & 0xFF) - 1);
        return sum;
    }

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
    }

    public void setPhone(int status, String num) {// default is simple box
        //		switch (status) {
        //		case HFP_INFO_INITIAL:
        //		case HFP_INFO_READY:
        //		case HFP_INFO_CONNECTING:
        //		case HFP_INFO_CONNECTED:
        //			status = 0;
        //			break;
        //		case HFP_INFO_CALLED:
        //			status = 2;
        //			break;
        //		case HFP_INFO_INCOMING:
        //			status = 1;
        //			break;
        //		case HFP_INFO_CALLING:
        //			status = 4;
        //			break;
        //		}
        //		byte[] data = new byte[4];// {(byte)0xc5, 0x1, (byte)status};
        //		data[0] = (byte) 0xc5;
        //		data[1] = (byte) (2);
        //		data[2] = (byte) 0;
        //		data[3] = (byte) status;
        //
        //		sendDataToCanbox(data, data.length);
        //
        //		if (num == null) {
        //			num = " ";
        //		}
        //		byte[] n = num.getBytes();
        //		data = new byte[n.length + 4];// {(byte)0xc5, 0x1, (byte)status};
        //		data[0] = (byte) 0xcA;
        //		data[1] = (byte) (n.length + 2);
        //		data[2] = (byte) 1;
        //		data[3] = (byte) 3;
        //		byteArrayCopy(data, n, 4, 0, n.length);
        //		Util.doSleep(100);
        //		sendDataToCanbox(data, data.length);

    }


    public void setContext(Context c) {
        super.setContext(c);
        udpateLang();
    }

    public void udpateLang() {

        if (CarUtil.getManaId() == 15) {
            int lang = -1;
            String locale = Locale.getDefault().getLanguage();
            if (locale != null) {
                if (locale.equals("zh")) {
                    lang = 2;
                } else if (locale.equals("de")) {
                    lang = 3;
                } else if (locale.equals("it")) {
                    lang = 4;
                } else if (locale.equals("fr")) {
                    lang = 5;
                } else if (locale.equals("sv")) {
                    lang = 6;
                } else if (locale.equals("es")) {
                    lang = 7;
                } else if (locale.equals("nl")) {
                    lang = 8;
                } else if (locale.equals("pt")) {
                    lang = 9;
                } else if (locale.equals("jp")) {
                    lang = 0xa;
                } else if (locale.equals("nb")) {
                    lang = 0xb;
                } else if (locale.equals("fi")) {
                    lang = 0xc;
                } else if (locale.equals("da")) {
                    lang = 0xd;
                } else if (locale.equals("tr")) {
                    lang = 0x10;
                } else if (locale.equals("el")) {
                    lang = 0xe;
                } else if (locale.equals("ar")) {
                    lang = 0xf;
                } else {
                    lang = 1;
                }

            }
            if (lang != -1) {
                byte[] buf = {0x2, (byte) 0x9a, 0x1, (byte) lang};
                sendDataToCanbox(buf, buf.length);
            }
        }
    }

    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {
        //		Log.d("fk", ">>updateTime");
        if (mContext == null) {
            return;
        }
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        byte format = 2;

        String date_foramt = SettingProperties.getProperty(mContext, SettingProperties.KEY_DATE_FORMAT);
        if (date_foramt != null) {
            if ("dd/MM/yyyy".equals(date_foramt)) {
                format = 1;
            } else if ("MM/dd/yyyy".equals(date_foramt)) {
                format = 3;
            }
        }

        if (CarUtil.getCarType2() == 1) {
            h = (byte) ((h + 1) % 24);
        } else if (CarUtil.getCarType2() == 2) {
            h = (byte) ((h + 23) % 24);
        } else {
            h = fixTimeHour(h);
        }


        if ("12".equals(strTimeFormat)) {
            if (h > 12) {
                h -= 12;
            } else if (h == 0) {
                h = 12;
            }
            h |= 0x80;
        } else {
            ampm = 1;
        }

        Calendar now = Calendar.getInstance();

        int zone = now.getTimeZone().getDefault().getRawOffset();

        zone = zone / 3600000;

        //		int offsetInMillis = now.getTimeZone().getOffset(now.getTimeInMillis());

        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        //		Log.d("cccc", ""+curDate.getYear());
        byte y = (byte) (curDate.getYear() - 100 + 208);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();

        byte[] buf = new byte[]{0x0a, (byte) 0xcb, 1, h, m, 0, (byte) zone, ampm, y, mon, d, format};

        sendDataToCanbox(buf, buf.length);

    }

}
