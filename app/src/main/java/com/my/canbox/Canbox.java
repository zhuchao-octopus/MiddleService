package com.my.canbox;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import com.car.hardware.Mcu;
import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.ProtocolAk47;
import com.common.util.Util;
import com.my.GlobalDef;
import com.my.cartype.CarUtil;
import com.my.manager.McuManager;
import com.my.out.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Canbox {
    static {
        // System.loadLibrary("Canbox");
    }

    public Canbox() {

        String mCanboxType = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAN_BOX);
        if (mCanboxType == null) {
            mCanboxType = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_CAN_BOX);
        }

        if (mCanboxType != null) {
            String[] ss = mCanboxType.split(",");
            String mcuBaud = null;
            String mcuConfig = null;

            for (int i = 1; i < ss.length; ++i) {
                if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_MCU_BAUD)) {
                    mcuBaud = ss[i].substring(1);
                } else if (ss[i]
                        .startsWith(MachineConfig.KEY_SUB_CANBOX_MCU_CONFIG)) {
                    mcuConfig = ss[i].substring(1);
                }
            }

            if (mcuBaud != null && mcuConfig != null) {
                try {

                    byte[] data = new byte[]{0x05, 0x01, 0x2, 0x3, 0x0, 0x0};
                    data[2] = (byte) (Integer.parseInt(mcuBaud.substring(0, 2),
                            16));
                    data[3] = (byte) (Integer.parseInt(mcuBaud.substring(2, 4),
                            16));
                    data[4] = (byte) (Integer.parseInt(mcuBaud.substring(4, 6),
                            16));
                    data[5] = (byte) (Integer.parseInt(mcuBaud.substring(6, 8),
                            16));
                    sendCmd(CANBOX_WRITE_MCU_DATA, 0, data);
                    data[1] = 0x2;
                    data[2] = (byte) (Integer.parseInt(mcuConfig.substring(0, 2),
                            16));
                    data[3] = (byte) (Integer.parseInt(mcuConfig.substring(2, 4),
                            16));
                    data[4] = (byte) (Integer.parseInt(mcuConfig.substring(4, 6),
                            16));
                    data[5] = (byte) (Integer.parseInt(mcuConfig.substring(6, 8),
                            16));
                    sendCmd(CANBOX_WRITE_MCU_DATA, 0, data);
                } catch (Exception e) {

                }
            }
        }
        startRepeatSendLcdMsg(false);
    }

    public Context mContext;

    public void setContext(Context c) {
        mContext = c;
        registerListener();
    }

    public final static String TAG = "Canbox";

    public String mVersion;
    public String mVersionEx;

    public static final int HFP_INFO_INITIAL = 0;
    public static final int HFP_INFO_READY = 1;
    public static final int HFP_INFO_CONNECTING = 2;
    public static final int HFP_INFO_CONNECTED = 3;
    public static final int HFP_INFO_CALLED = 4;
    public static final int HFP_INFO_INCOMING = 5;
    public static final int HFP_INFO_CALLING = 6;

    public static final int CANBOX_OPEN = 0x00;
    public static final int CANBOX_CLOSE = 0x01;

    public static final int CANBOX_SET_KEY = 0x02;
    public static final int CANBOX_WRITE_COMMON_DATA = 0x03;
    public static final int CANBOX_WRITE_MCU_DATA = 0x04;

    public static final int REVSRSE_OPEN = 0x08;

    public static final int CANBOX_RETURN_AIR = 0x81;
    public static final int CANBOX_RADAR_FRONT = 0x82;
    public static final int CANBOX_RADAR_BACK = 0x83;
    public static final int CANBOX_RADAR_STATUS = 0x84;
    public static final int CANBOX_DOOR_STATUS = 0x85;
    public static final int CANBOX_STEER_ANGLE = 0x86;
    public static final int CANBOX_OUT_DOOR_TEMP = 0x87;

    public static final int CANBOX_RADAR_SWITCH = 0x88;

    public static final int CANBOX_HIDE_RADAR = 0x89;
    public static final int CANBOX_HIDE_AIR = 0x8a;

    public static final int CANBOX_RADAR_LEFT = 0x8b;
    public static final int CANBOX_RADAR_RIGHT = 0x8c;

    public static final int CANBOX_NISSIAN_UI_DATA = 0x8d;

    public static final int CANBOX_NISSIAN_REQUEST_INFO = 0x8e;

    public static final int CANBOX_HY_UI_DATA = 0x8f;

    public static final int CANBOX_HY_REQUEST_INFO = 0x90;

    public static final int CANBOX_DACIA_UI_DATA = 0x91;

    public static final int CANBOX_SUBARU_UI_DATA = 0x92;

    public static final int CANBOX_MAZDA_RAISE_UI_DATA = 0x93;
    public static final int CANBOX_VW_RAISE_UI_DATA = 0x94;

    public final static int RADAR_DISTANCE_WANRING = 4;
    public final static int RADAR_DISTANCE_NORMAL = 7;
    public final static int RADAR_DISTANCE_LONG = 9;

    /*
     * key
     */
    public final static int KEY_MIC = MyCmd.Keycode.KEY_MIC; // SOURCE
    public final static int KEY_SOURCE = MyCmd.Keycode.MODLE; // SOURCE
    public final static int KEY_MUTE = MyCmd.Keycode.MUTE; // MUTE
    public final static int KEY_EJECT = MyCmd.Keycode.EJECT; // EJECT
    public final static int KEY_TS = 4;// TS
    public final static int KEY_BL_LEVEL = 5;// BACKLIGHT LEVEL
    public final static int KEY_AUX = MyCmd.Keycode.AUX_IN;// AUX
    public final static int KEY_NEXTSONG = MyCmd.Keycode.NEXT; // NEXTSONG
    public final static int KEY_PREVIOUSSONG = MyCmd.Keycode.PREVIOUS;// PREVIOUSSONG
    public final static int KEY_GPS = MyCmd.Keycode.NAVIGATION; // GPS
    public final static int KEY_FM = MyCmd.Keycode.RADIO; // FM
    public final static int KEY_DVD = MyCmd.Keycode.DVD; // DVD
    public final static int KEY_DTV = 12; // DTV
    public final static int KEY_IPOD = 13; // IPOD
    public final static int KEY_BT = MyCmd.Keycode.BT;// BT
    public final static int KEY_SCW = 15; // ^>
    public final static int KEY_SCCW = 16; // <^
    public final static int KEY_TV = 17; // TV
    public final static int KEY_EQ = MyCmd.Keycode.EQ;// EQ

    public final static int KEY_BT_DIAL = MyCmd.Keycode.BT_DIAL; // BT dial
    public final static int KEY_BT_HANG = MyCmd.Keycode.BT_HANG; // BT hang
    public final static int KEY_MODE = MyCmd.Keycode.MODLE; // MODE
    public final static int KEY_POWER_OFF = MyCmd.Keycode.POWER_OFF; // Power
    // off

    public final static int KEY_PLAYPAUSE = MyCmd.Keycode.PLAY_PAUSE;//

    public final static int KEY_CH_UP = MyCmd.Keycode.CH_UP;//
    public final static int KEY_CH_DOWN = MyCmd.Keycode.CH_DOWN; //

    public final static int KEY_SEEK_NEXT = MyCmd.Keycode.KEY_SEEK_NEXT;//
    public final static int KEY_SEEK_PREV = MyCmd.Keycode.KEY_SEEK_PREV; //

    public final static int KEY_APP = MyCmd.Keycode.ALL_APP; // APP
    public final static int KEY_SET = MyCmd.Keycode.SETUP; // SET
    public final static int KEY_UP = MyCmd.Keycode.UP;
    public final static int KEY_DOWN = MyCmd.Keycode.DOWN;
    public final static int KEY_LEFT = MyCmd.Keycode.LEFT;
    public final static int KEY_RIGHT = MyCmd.Keycode.RIGHT;
    public final static int KEY_ENTER = MyCmd.Keycode.ENTER;

    public final static int KEY_NUM_1 = MyCmd.Keycode.NUMBER1;
    public final static int KEY_NUM_2 = MyCmd.Keycode.NUMBER2;
    public final static int KEY_NUM_3 = MyCmd.Keycode.NUMBER3;
    public final static int KEY_NUM_4 = MyCmd.Keycode.NUMBER4;
    public final static int KEY_NUM_5 = MyCmd.Keycode.NUMBER5;
    public final static int KEY_NUM_6 = MyCmd.Keycode.NUMBER6;
    public final static int KEY_POWER = MyCmd.Keycode.POWER;

    public final static int KEY_NUM_7 = MyCmd.Keycode.NUMBER7;
    public final static int KEY_NUM_8 = MyCmd.Keycode.NUMBER8;
    public final static int KEY_NUM_9 = MyCmd.Keycode.NUMBER9;
    public final static int KEY_NUM_0 = MyCmd.Keycode.NUMBER0;

    public final static int KEY_NUM_J = MyCmd.Keycode.NUMBER_POUND;
    public final static int KEY_NUM_X = MyCmd.Keycode.NUMBER_STAR;

    public static final int AK_KEYPAD_FF = MyCmd.Keycode.FAST_F;
    public static final int AK_KEYPAD_FR = MyCmd.Keycode.FAST_R;

    public static final int AK_KEYPAD_VOLUME_A = MyCmd.Keycode.VOLUME_UP;
    public static final int AK_KEYPAD_VOLUME_D = MyCmd.Keycode.VOLUME_DOWN;
    public static final int AK_KEYPAD_POWER_KEY_FATE = MyCmd.Keycode.POWER;

    public static final int AK_KEYPAD_MUTE_FAKE = MyCmd.Keycode.MUTE;
    // > 100 is phy
    public final static int KEY_PHY_BUTTON_KEY = 100;

    public final static int KEY_HOME = MyCmd.Keycode.HOME;
    public final static int KEY_HOMEPAGE = MyCmd.Keycode.HOME;
    public final static int KEY_MENU = MyCmd.Keycode.MENU;
    public final static int KEY_BACK = MyCmd.Keycode.BACK;

    public final static int KEY_MEDIA = MyCmd.Keycode.AUDIO;

    public byte[] mRadar = new byte[8];
    public byte[] mRadarFontEx = new byte[2];

    public byte[] mRadarLeft = new byte[4];
    public byte[] mRadarRight = new byte[4];

    /*
     * new eq set
     */
    // //

    public final static int CMD_GROUP_EQ = 0x100;
    public final static int CMD_GROUP_AC = 0x200;


    public final static int AC_CMD_REQUEST_INFO = 1;
    // sub cmd <= 0xff
    public final static int EQ_CMD_SET_HIGH = 1;
    public final static int EQ_CMD_SET_MIDDLE = 2;
    public final static int EQ_CMD_SET_LOW = 3;
    public final static int EQ_CMD_SET_ZONE_FR = 4;
    public final static int EQ_CMD_SET_ZONE_LR = 5;
    public final static int EQ_CMD_SET_VOLUME = 6;
    public final static int EQ_CMD_SET_OTHER = 7;

    public final static int EQ_CMD_SET_ALL_DATA = 0xf0;
    public final static int EQ_REQUEST_ALL_MAX = 0xff;

    /*
     *
EQ_REQUEST_ALL_MAX

mEQMax = (data & 0xff);
mZoneMax = ((data & 0xff00) >> 8);
mVolumeMax = ((data & 0xff0000) >> 16);
*/
    public int doCmd(int cmd, int data) {
        int ret = 0;
        switch (cmd & 0xff00) {
            case CMD_GROUP_EQ:
                ret = doEQCmd(cmd & 0xff, data);
                if (ret > 0) {
                    if (mContext != null) {
                        Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
                        i.putExtra(MyCmd.EXTRA_COMMON_CMD, EQ_REQUEST_ALL_MAX
                                | CMD_GROUP_EQ);
                        i.putExtra(MyCmd.EXTRA_COMMON_DATA, ret);
                        i.setPackage("com.canboxsetting");
                        mContext.sendBroadcast(i);
                    }

                }
                break;
            case CMD_GROUP_AC:
                if ((cmd & 0xff) == AC_CMD_REQUEST_INFO) {
                    if (!returnACDataToCanboxSetting()) {
                        Handler handler = getHandler("CanService");
                        if (null != handler) {
                            handler.sendMessage(handler.obtainMessage(CMD_GROUP_AC));
                        }
                    }
                }
                break;
        }
        return ret;
    }

    public int doEQCmd(int cmd, int data) {
        return 0;
    }

    public boolean returnACDataToCanboxSetting() {
        return false;
    }

    /*

    EQ_CMD_SET_ALL_DATA
    0 高音
    1 中音
    2 低音
    3 前后
    4 左右
    5 音量

     * */
    public void returnEQData(int cmd, byte[] buf) {
        if (mContext != null) {
            Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
            i.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd | CMD_GROUP_EQ);
            i.putExtra("buf", buf);
            i.setPackage("com.canboxsetting");
            mContext.sendBroadcast(i);
        }
    }

    // private native final int nativeSendCommand(int cmd, int param1,
    // byte[] param2);

    private Mcu mMcu;

    public int sendCanboxData(byte[] buf) {
        int can_len = buf.length;
        byte[] protocol = new byte[2 + can_len];
        protocol[0] = ProtocolAk47.TYPE_CAN_SEND;
        protocol[1] = 0x1;
        Util.byteArrayCopy(protocol, buf, 2, 0, can_len);
        sendMcuData(protocol);
        return -1;
    }

    private int sendMcuData(byte[] buf) {
        if (mMcu == null) {
            mMcu = Mcu.getInstance();
        }
        return mMcu.sendCmd(buf);
    }

    private final int nativeSendCommand(int cmd, int param1, byte[] param2) {
        if (CANBOX_WRITE_COMMON_DATA == cmd) {
            return McuManager.sendCanboxData(param2);
        } else if (CANBOX_WRITE_MCU_DATA == cmd) {
            return sendMcuData(param2);
        }
        return 0;
    }

    public void sendCmd(int cmd, int param1, byte[] param2) {
        nativeSendCommand(cmd, param1, param2);
    }

    public void sendCmd(int cmd) {
        nativeSendCommand(cmd, 0, null);
    }

    public void sendCmd(int cmd, int param) {
        nativeSendCommand(cmd, param, null);
    }

    private static HashMap mHandlerMap = new HashMap();

    public static void addHandler(String kTag, Handler vHandler) {
        if (!mHandlerMap.containsKey(kTag)) {
            mHandlerMap.put(kTag, vHandler);
        }
    }

    public static void removeHandler(String kTag) {
        if (mHandlerMap.containsKey(kTag)) {
            mHandlerMap.remove(kTag);
        }
    }

    public static Handler getHandler(String kTag) {
        return (Handler) mHandlerMap.get(kTag);
    }

    // virtual function


    public void setReverseRadaVol(byte param) {
    }

    public void setParkCarMode(byte param) {
    }

    public void requestInfo(byte param) {
    }

    public void setMediaSrcASC(byte[] b, int len) {
    }

    public void setMediaMoreInfo(int source, int play, int total, int time,
                                 int total_time) {
    }

    public void setVolume(int volume) {
    }

    public void setPhone(int status, String num) {
    }

    public void setPhoneEx(int status, String num, String name) {
    }

    public void setSongName(String s) {
    }

    public void setSongAritst(String s) {
    }

    public void setSongAlbum(String s) {
    }

    public void clearID3() {
        setSongName("");
        setSongAritst("");
        setSongAlbum("");
    }

    public void startConnect() {// default is simple box
        byte[] data = new byte[]{(byte) 0x81, 0x1, 1};
        sendDataToCanbox(data, data.length);
        requestVersion();
        updateTime();
        //requestDriveData(1);// test
        Log.d(TAG, "startConnect()");
    }

    public void stopConnect() {// default is simple box
        byte[] data = new byte[]{(byte) 0x81, 0x1, 0};
        sendDataToCanbox(data, data.length);
        mRequestDriveData = 0;
    }

    public boolean requestAngleData() {
        return false;
    }

    public void requestVersion() {

    }
    // public void poweroff() {
    //
    // }

    public void setMediaSrc(int source) {// default is simple box
        byte s;
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
            // case 0x82:
            // case 0x83:
            // break;
            default:
                s = 0x0c;
                mediaType = 0x30;
                break;
        }
        byte[] data = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0,
                0, 0};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {// default is
        // simple box
        // byte []data = new byte[]{(byte)0xc0, 0x8, 1, type, b[0], b[1], b[2],
        // b[3], b[4], b[5]};
        // sendDataToCanbox(data, data.length);
    }

    public void sendCommonDataToCanbox(byte[] data) {
        nativeSendCommand(CANBOX_WRITE_COMMON_DATA, 0, data);
    }

    public byte simpleSum(byte[] data, int len) {
        byte sum = 0;
        for (int i = 0; i < len; ++i) {
            sum += data[i];
        }
        return (byte) (sum ^ 0xFF);
    }

    public byte hiworldSum1(byte[] data, int len) {
        byte sum = 0;
        for (int i = 0; i < len; ++i) {
            sum += data[i];
        }
        sum = (byte) ((sum & 0xFF) - 1);
        return sum;
    }

//	public byte hiworldSum2(byte[] data, int len) {
//		byte sum = 0;
//		for (int i = 0; i < len; ++i) {
//			sum += data[i];
//		}
//		sum = (byte) ((sum & 0xFF) - 1);
//		return sum;
//	}


    public void sendDataToCanboxHiword1(byte[] data, int len) {
        // canbox
        if ((data[0] & 0xff) > (data.length - 2)) { //wrong pro
            Log.e(TAG, "sendDataToCanboxHiword1 wrong pro:");
            return;
        }

        byte[] send = new byte[len + 4];
        send[0] = (byte) (len + 3);
        send[1] = (byte) 0x5a;
        send[2] = (byte) 0xa5;
        send[len + 3] = hiworldSum1(data, len);
        byteArrayCopy(send, data, 3, 0, len);
        sendCmd(CANBOX_WRITE_COMMON_DATA, 0, send);
    }

    public void sendDataToCanboxHiword2(byte[] data, int len) {
        // canbox
        if ((data[0] & 0xff) > (data.length - 2)) { //wrong pro
            Log.e(TAG, "sendDataToCanboxHiword2 wrong pro:");
            return;
        }
        byte[] send = new byte[len + 4];
        send[0] = (byte) (len + 3);
        send[1] = (byte) 0xaa;
        send[2] = (byte) 0x55;
        send[len + 3] = hiworldSum1(data, len);
        byteArrayCopy(send, data, 3, 0, len);
        sendCmd(CANBOX_WRITE_COMMON_DATA, 0, send);
    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        // canbox
        byte[] send = new byte[len + 3];
        send[0] = (byte) (len + 2);
        send[1] = 0x2e;
        send[len + 2] = simpleSum(data, len);
        byteArrayCopy(send, data, 2, 0, len);
        nativeSendCommand(CANBOX_WRITE_COMMON_DATA, 0, send);
    }

    public int mKeyDown = 0;
    public long mClickTime = 0;
    public boolean longClick = false;

    public static final int LONG_CLICK_TIME = 1000;

    public int getLongKey(int key) {
        int ret = 0;
        switch (key) {
            case MyCmd.Keycode.MULT_NEXT_AND_HANG:
            case MyCmd.Keycode.NEXT:
                ret = MyCmd.Keycode.KEY_SEEK_NEXT;
                break;
            case MyCmd.Keycode.MULT_PREV_AND_RECEIVE:
            case MyCmd.Keycode.PREVIOUS:
                ret = MyCmd.Keycode.KEY_SEEK_PREV;
                break;
            case MyCmd.Keycode.BT_DIAL:
                ret = MyCmd.Keycode.KEY_MIC;
                break;
            case MyCmd.Keycode.BT:
                ret = MyCmd.Keycode.BT_HANG;
                break;
            case MyCmd.Keycode.MULT_SPEECH_AND_BT:
                ret = MyCmd.Keycode.BT;
                break;
            case MyCmd.Keycode.MULT_OK_AND_POWER:
            case MyCmd.Keycode.MULT_MUTE_AND_POWER:
                ret = MyCmd.Keycode.POWER;
                break;
            case MyCmd.Keycode.MULT_SPEECH_MODE:
                ret = MyCmd.Keycode.MODLE;
                break;
        }
        return ret;
    }

    // protected void doKey(int value, int status) { // value 0 -> key up
    //
    // if (CarUtil.getChangeKey() == 1) {
    // value = changeKey(value);
    // }
    //
    // switch (status) {
    // case 0:
    // break;
    // case 1:
    // // if(mKeyDown!=value){
    // mKeyDown = value;
    // mClickTime = System.currentTimeMillis();
    // doKey(value);
    // // }
    // break;
    // case 2:
    // mKeyDown = value;
    // // longClick = true; //no do longClickNow
    // break;
    // case 3:
    // mKeyDown = value;
    // break;
    // }
    //
    // if (status == 0 && mKeyDown != 0) {
    // if (value != 0 && value != mKeyDown) {
    // doKey(value);
    // }
    // // if (longClick) {
    // // // sendCmd(CANBOX_SET_KEY, value | 0x80000000);
    // // } else {
    // // if (CarUtil.getChangeKey() == 1) {
    // // value = changeKey(value);
    // // }
    // // doKey(value);
    // //
    // // mKeyDown = 0;
    // // }
    // // longClick = false;
    // } else if (mKeyDown == AK_KEYPAD_VOLUME_A
    // || mKeyDown == AK_KEYPAD_VOLUME_D) {
    // // volme for long press
    // if ((System.currentTimeMillis() - mClickTime) > 500) {
    // doKey(value);
    // longClick = true;
    // }
    // }
    // }


    protected void doKey(int value, int status) { // all long key to this. is
        // good?

        // Log.d("Nissan2013Simple", "doKey:"+value);
        // if (CarUtil.getChangeKey() == 1) {
        value = changeKey(value);
        // }

        switch (status) {
            case 0:
                if (mKeyDown != 0) {
                    if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
                        int ret = getLongKey(value);
                        if (ret != 0) {
                            mKeyDown = ret;
                        }
                    }
                    doKey(mKeyDown);
                    mKeyDown = 0;
                    longClick = false;
                }
                break;
            case 1:
                mKeyDown = value;
                mClickTime = System.currentTimeMillis();
                longClick = false;
                break;
            case 2:
                if (mKeyDown != 0) {
                    if (value == AK_KEYPAD_VOLUME_A || value == AK_KEYPAD_VOLUME_D) {
                        if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
                            doKey(value);
                        }
                        // mKeyDown = 0;
                    } else {
                        if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
                            if (mKeyDown != 0) {
                                longClick = true;
                                int ret = getLongKey(value);
                                if (ret != 0) {
                                    doKey(ret);
                                    mKeyDown = 0;
                                }
                            }
                        }
                    }
                }
                break;
        }

    }

    public int changeKey(int value) {
        if ((CarUtil.getChangeKey() & 0x1) != 0) {
            switch (value) {
                case KEY_NEXTSONG:
                    value = KEY_PREVIOUSSONG;
                    break;
                case KEY_PREVIOUSSONG:
                    value = KEY_NEXTSONG;
                    break;
                case MyCmd.Keycode.KEY_SEEK_NEXT:
                    value = MyCmd.Keycode.KEY_SEEK_PREV;
                    break;
                case MyCmd.Keycode.KEY_SEEK_PREV:
                    value = MyCmd.Keycode.KEY_SEEK_NEXT;
                    break;
            }
        }

        if ((CarUtil.getChangeKey() & 0x2) != 0) {
            switch (value) {
                case AK_KEYPAD_VOLUME_A:
                    value = AK_KEYPAD_VOLUME_D;
                    break;
                case AK_KEYPAD_VOLUME_D:
                    value = AK_KEYPAD_VOLUME_A;
                    break;
            }
        }
        return value;
    }

    protected void doKey(int value) {
        if (mContext != null) {
            BroadcastUtil.sendToCarService(mContext,
                    MyCmd.Cmd.APP_REQUEST_SEND_KEY, value);
            Util.setFileValue("/sys/class/ak/source/beep", "2");
        }
    }

    public void byteArrayCopy(byte[] dest, byte[] src, int dStart, int sStart,
                              int len) {
        for (int i = 0; i < len; i++) {
            dest[i + dStart] = src[sStart + i];
        }
    }

    protected void sendCanboxInfo(byte[] buf) {
        if (mContext != null) {
            Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
            i.putExtra("buf", buf);
            mContext.sendBroadcast(i);
        }
    }

    protected boolean sendCanboxAir(byte[] buf) {
//		if (!AppConfig.getTopActivity().contains("com.canboxsetting.CanAirControlActivity")){
//			return false;
//		}
        if (mContext != null) {
            Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
            i.putExtra("buf", buf);
            i.putExtra(MyCmd.EXTRA_COMMON_CMD, "ac");
            mContext.sendBroadcast(i);
        }
        return false;
    }

    protected boolean sendSeatHeat(byte[] buf) {
        if (!AppConfig.getTopActivity().contains("com.canboxsetting.SeatHeatActivity")) {
            return false;
        }
        if (mContext != null) {
            Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
            i.putExtra("buf", buf);
            i.putExtra(MyCmd.EXTRA_COMMON_CMD, "ac");
            mContext.sendBroadcast(i);
        }
        return true;
    }


    protected void sendCanboxInfo(String packageName, byte[] buf) {
        if (!GlobalDef.mTopIsNeedCanboxInfo && packageName != null) {
            return;
        }

        if (mContext != null) {
            Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
            i.putExtra("buf", buf);
            if (packageName != null) {
                i.setPackage(packageName);
            }
            mContext.sendBroadcast(i);
        }
    }

    public int getReturnType() {
        return -1;
    }

    public void updateCanboxSettings() {

    }

    public void updateTime() {

    }

//	public void updateCompass(int direction, int compass) {
//
//	}

    public void updateCompass(int compass) {

    }

    public void updateCompass(int compass, double altitude) {

    }

    private int mCarSpeed = 0;
    private final static int SPEED_CLOSE_RADAR = 25;

    public void updateExtRadar(int speed) {
        //use can speed now for pro 290.
        Log.d(TAG, "updateExtRadar:" + speed);

//		if ((mCarSpeed < SPEED_CLOSE_RADAR && speed >= SPEED_CLOSE_RADAR)
//				|| (mCarSpeed >= SPEED_CLOSE_RADAR && speed < SPEED_CLOSE_RADAR)) {
//
//			String cmd = "AT#RS";
//
//			mCarSpeed = speed;
//			if (speed >= SPEED_CLOSE_RADAR) {
//				cmd += "1";
//			} else {
//				cmd += "0";
//			}
//			Util.setFileValue(CarUtil.PG, cmd);
//
//		}

    }

    public boolean isSupportCompass() {
        return false;
    }

    public int compassAngleToDirect(int compass) {
        int direct = 0;
        if (compass >= 22 && compass < 67) {
            direct = 1;
        } else if (compass >= 67 && compass < 112) {
            direct = 2;
        } else if (compass >= 112 && compass < 157) {
            direct = 3;
        } else if (compass >= 157 && compass < 202) {
            direct = 4;
        } else if (compass >= 202 && compass < 247) {
            direct = 5;
        } else if (compass >= 247 && compass < 292) {
            direct = 6;
        } else if (compass >= 292 && compass < 337) {
            direct = 7;
        }
        return direct;
    }

    public int compassAngleToDirect16(int compass) {
        int direct = 0;
        if (compass >= 12 && compass < 35) {
            direct = 1;
        } else if (compass >= 35 && compass < 57) {
            direct = 2;
        } else if (compass >= 57 && compass < 79) {
            direct = 3;
        } else if (compass >= 79 && compass < 101) {
            direct = 4;
        } else if (compass >= 101 && compass < 124) {
            direct = 5;
        } else if (compass >= 124 && compass < 146) {
            direct = 6;
        } else if (compass >= 146 && compass < 169) {
            direct = 7;
        } else if (compass >= 169 && compass < 192) {
            direct = 8;
        } else if (compass >= 192 && compass < 214) {
            direct = 9;
        } else if (compass >= 214 && compass < 237) {
            direct = 10;
        } else if (compass >= 237 && compass < 259) {
            direct = 11;
        } else if (compass >= 259 && compass < 280) {
            direct = 12;
        } else if (compass >= 280 && compass < 302) {
            direct = 13;
        } else if (compass >= 302 && compass < 325) {
            direct = 14;
        } else if (compass >= 325 && compass < 348) {
            direct = 15;
        }

        return direct;
    }

    public int compassAngleToDirectStep(int compass, int step) {
        float f = 360.0f / step;
        float start = (-f / 2);

        for (int i = 0; i < step; ++i) {
            if ((compass >= start) && (compass < (start + (f * i)))) {
                return i;
            }
        }
        return 0;
    }

    public int getUpdateTime() {
        return Integer.MAX_VALUE;
    }

    public void sendEqToCanbox(byte[] buf) {

    }

    public void updateCanboxExData() {

    }

    public void setCanboxLED(int type, int status) {

    }


    public void udpateLang() {

    }

    public void udpateSet(int data) {

    }

    private int mSupportVoiceControl = 0;
    public final static int VOICE_RAISE = 1;

    public void setVoiceSupportRaise() {
        mSupportVoiceControl = VOICE_RAISE;
    }

    public void udpateVoiceControl(int data) {
        if (mSupportVoiceControl == VOICE_RAISE) {
            int cmd = (data & 0xff);
            byte param = (byte) ((int) ((data & 0xff00) >> 8));
            switch (cmd) {
                case 0x7:
                    cmd = 0x40;
                    break;
                case 0x8:
                    cmd = 0x41;
                    break;
                case 0x10:
                    cmd = 0x42;
                    break;
                case 0x14:
                    cmd = 0x43;
                    break;
                case 0x9:
                    cmd = 0x44;
                    if (param == 0) {
                        param = (byte) 0x81;
                    } else {
                        param = (byte) 0x80;
                    }
                    break;
                case 0xa:
                    cmd = 0x44;
                    break;
            }

            byte[] buf = new byte[]{(byte) 0xef, 0x3, 0x7d, (byte) cmd,
                    (byte) (param)};
            sendDataToCanbox(buf, buf.length);
        }
    }

    public void updateCallLog(Object obj) {

    }

    public void touchInReverse(int x, int y, int w, int h) {

    }

    public void touchInReverseEx(int x, int y, int w, int h, int down) {

    }

    public void notifyReverse(int status) {

    }

    public void updateScreenSaveNeedData(boolean b) {

    }

    public int mWindMaxStep = 0;

    public void hideRadar() {

    }

    private final static int HIDE_RADAR = 0;
    private final static int REPEAT_SEND_LCD = 1;
    private final static int REPEAT_SEND_CAR_TYPE = 100;
    private final static int REPEAT_SEND_ROLL_KEY = 101;
    private Handler mHandlerRadar = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_RADAR:
                    RadarManager.stop();
                    hideRadar();
                    break;
                case REPEAT_SEND_CAR_TYPE:
                    autoUpdateCarType();
                    break;
                case REPEAT_SEND_ROLL_KEY:
                    doKeyRoll(msg.arg1, msg.arg2);
                    break;
                case REPEAT_SEND_LCD:
                    repeatSendLcdMsg();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void autoUpdateCarType() {
        if (mRepeatSendCarType != null) {
            sendDataToCanbox(mRepeatSendCarType, mRepeatSendCarType.length);
            if (mRepeatCmdTime > 0) {
                --mRepeatCmdTime;
            }
            if (mRepeatCmdTime == -1 || mRepeatCmdTime > 0) {
                mHandlerRadar.removeMessages(REPEAT_SEND_CAR_TYPE);
                mHandlerRadar.sendEmptyMessageDelayed(REPEAT_SEND_CAR_TYPE,
                        1000);
            }
        }
    }

    public void checkHideRadarEx(int time) {
        mHandlerRadar.removeMessages(HIDE_RADAR);
        mHandlerRadar.sendEmptyMessageDelayed(HIDE_RADAR, time);
    }

    public void checkHideRadarEx() {
        mHandlerRadar.removeMessages(HIDE_RADAR);
        mHandlerRadar.sendEmptyMessageDelayed(HIDE_RADAR, 5000);
    }

    public boolean isBufEqual(byte[] data, byte[] data2) {
        if (data.length != data2.length) {
            return false;
        }
        for (int i = 0; i < data.length; ++i) {
            if (data[i] != data2[i]) {
                return false;
            }
        }
        return true;
    }

    // for canbox study key

    private int mKeyDownMap = 0;

    protected boolean doKeyStudy(int groundId, int value, int status) {

        value = (value & 0xff | (groundId << 8));

        if (mMapKeyStudy != null) {
            if (status == 0) {
                if (mKeyDownMap != 0) {
                    Intent it = new Intent(MyCmd.BROADCAST_RETURN_CANKEY_STUDY);
                    it.putExtra(MyCmd.EXTRA_COMMON_CMD,
                            MyCmd.Cmd.TOUCH_STUDY_KEY);
                    it.putExtra(MyCmd.EXTRA_COMMON_DATA, mKeyDownMap);
                    mContext.sendBroadcast(it);
                }
                mKeyDownMap = 0;
            } else if (status == 1) {
                mKeyDownMap = value;
            }
            return true;
        } else if (mMapKey == null) {
            return false;
        } else {
            switch (status) {
                case 0:
                    if (mKeyDownMap != 0) {
                        if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
                            if (((mKeyDownMap & 0xff00) >> 8) != 0) {
                                mKeyDownMap = ((mKeyDownMap & 0xff00) >> 8);
                            }
                        }
                        doKey(mKeyDownMap & 0xff);
                        mKeyDownMap = 0;
                        longClick = false;
                        return true;
                    }
                    return false;
                case 1:
                    int studyKey = getStudyKey(value);
                    if (studyKey == 0) {
                        return false;
                    }
                    if (mKeyDownMap == 0 || mKeyDownMap != studyKey) {
                        mKeyDownMap = studyKey;

                        mClickTime = System.currentTimeMillis();
                        longClick = false;
                    }
                    return true;
                case 2:
                    if (mKeyDownMap != 0) {
                        if ((mKeyDownMap & 0xff) == AK_KEYPAD_VOLUME_A
                                || (mKeyDownMap & 0xff) == AK_KEYPAD_VOLUME_D) {
                            doKey((mKeyDownMap & 0xff));
                        } else {
                            if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
                                if (((mKeyDownMap & 0xff00) >> 8) != 0) {
                                    mKeyDownMap = ((mKeyDownMap & 0xff00) >> 8);
                                    doKey(mKeyDownMap & 0xff);
                                    longClick = false;
                                    mKeyDownMap = 0;
                                }
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }

            }
            return false;
        }
    }

    protected boolean doKeyStudy(int value, int status) {
        return doKeyStudy(0, value, status);
    }

    private int getStudyKey(int key) {
        for (int i = 0; i < mMapKey.size(); ++i) {
            if (((mMapKey.get(i) & 0xffff)) == key) {
                return ((mMapKey.get(i) & 0xffff0000)) >> 16;
            }
        }
        return 0;
    }

    private final static String SYSTEM_CONFIG = MyCmd.VENDOR_DIR
            + ".canbox_key_mapping_";
    private ArrayList<Integer> mMapKey = null;
    private ArrayList<Integer> mMapKeyStudy = null;

    private String getKeyMapFile() {
        return SYSTEM_CONFIG + CarUtil.getCanboxType();
    }

    private void saveMapping() {
        String value = "";
        if (mMapKey != null && mMapKey.size() > 0) {
            for (int i = 0; i < mMapKey.size(); ++i) {
                if (i != 0) {
                    value += ",";
                }
                value += mMapKey.get(i);
            }
        }

        File file = new File(getKeyMapFile());
        try {
            if (!file.exists()) {
                file.createNewFile();
                ///FileUtils.setPermissions(SYSTEM_CONFIG, FileUtils.S_IRWXU  | FileUtils.S_IRWXG | FileUtils.S_IRWXO, -1, -1);
                ///FileUtils.setPermissions(SYSTEM_CONFIG, FileUtils.S_IRWXU  | FileUtils.S_IRWXG | FileUtils.S_IRWXO, -1, -1);

                Util.sudoExec("chmod:666:" + getKeyMapFile());
            } else {

            }

            FileOutputStream is = new FileOutputStream(file);
            DataOutputStream dis = new DataOutputStream(is);

            dis.write(value.getBytes());
            dis.flush();
            is.flush();
            dis.close();
            is.close();
            Util.sudoExecNoCheck("sync");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void intKeyStudyMap() {
        String s = Util.getFileString(getKeyMapFile());
        if (s != null) {
            if (mMapKey == null) {
                mMapKey = new ArrayList<Integer>();
            } else {
                mMapKey.clear();
            }
            String[] ss = s.split(",");
            for (int i = 0; i < ss.length; ++i) {
                try {
                    int v = Integer.valueOf(ss[i]);

                    mMapKey.add(v);
                } catch (Exception e) {

                }
            }
        }

        if (mMapKey != null && mMapKey.size() <= 0) {
            mMapKey = null;
        }
    }

    private BroadcastReceiver mReceiver = null;

    private void registerListener() {
        if (mReceiver == null) {
            intKeyStudyMap();
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(MyCmd.BROADCAST_SET_CANKEY_STUDY)) {

                        int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);
                        int data = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA,
                                0);
                        int data2 = intent.getIntExtra(
                                MyCmd.EXTRA_COMMON_DATA2, 0);
                        try {
                            doKeyStudyMsg(cmd, data, data2);
                        } catch (Exception e) {

                        }
                    }

                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(MyCmd.BROADCAST_SET_CANKEY_STUDY);

            mContext.registerReceiver(mReceiver, iFilter);
        }
    }

    private void setStudyKey(int key, int keyStudy) {
        int i = 0;
        int code = key | (keyStudy << 16);
        for (i = 0; i < mMapKeyStudy.size(); ++i) {
            if (((mMapKeyStudy.get(i) & 0xffff)) == key) {
                mMapKeyStudy.set(i, code);
                break;
            }
        }
        if (i >= mMapKeyStudy.size()) {
            mMapKeyStudy.add(code);
        }
    }

    private void doKeyStudyMsg(int cmd, int key, int keyStudy) {
        switch (cmd) {
            case MyCmd.Cmd.TOUCH_STUDY_START: {
                if (mMapKey != null) {
                    mMapKeyStudy = (ArrayList<Integer>) mMapKey.clone();
                } else {
                    mMapKeyStudy = new ArrayList<Integer>();
                }

                Intent it = new Intent(MyCmd.BROADCAST_RETURN_CANKEY_STUDY);
                it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
                it.putExtra(MyCmd.EXTRA_COMMON_DATA, mMapKeyStudy);
                mContext.sendBroadcast(it);
            }
            break;
            case MyCmd.Cmd.TOUCH_STUDY_KEY: {
                setStudyKey(key, keyStudy);
                Intent it = new Intent(MyCmd.BROADCAST_RETURN_CANKEY_STUDY);
                it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.TOUCH_STUDY_START);
                it.putExtra(MyCmd.EXTRA_COMMON_DATA, mMapKeyStudy);
                mContext.sendBroadcast(it);
                // mPopertiesStudy.setProperty(key + "", keyStudy + "");
                // it = new Intent(MyCmd.BROADCAST_RETURN_CANKEY_STUDY);
                // it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.TOUCH_STUDY_START);
                // it.putExtra(MyCmd.EXTRA_COMMON_DATA, mPopertiesStudy);
                // mContext.sendBroadcast(it);
            }
            break;
            case MyCmd.Cmd.TOUCH_STUDY_CLEAR:
                mMapKeyStudy.clear();
                break;
            case MyCmd.Cmd.TOUCH_STUDY_END:

                mMapKey = mMapKeyStudy;
                saveMapping();

                mMapKeyStudy = null;
                break;
            case MyCmd.Cmd.STUDY_QUIT_WITHOUT_SAVE:
                mMapKeyStudy = null;
                break;
            default:
                mMapKeyStudy = null;
                return;
        }
    }

    private void unregisterListener() {
        if (mContext != null) {
            mContext.unregisterReceiver(mReceiver);
        }
    }

    public void clear() {
        unregisterListener();
        stopConnect();
        mHandlerRadar.removeMessages(REPEAT_SEND_CAR_TYPE);
        mRepeatSendCarType = null;
    }

    public void sendSutdyKeyCanCode(byte key) {

    }

    public boolean isShowAir() {
        // if (isAirtContolCar()) {
        if ("com.canboxsetting/com.canboxsetting.CanAirControlActivity"
                .equals(AppConfig.getTopActivity())) {
            return false;
        }
        // }
        return true;
    }

//	public boolean isShowAirEx() {
//		// if (isAirtContolCar()) {
////		if ("com.canboxsetting/com.canboxsetting.CanAirControlActivity"
////				.equals(AppConfig.getTopActivity())) {
//			return false;
////		}
//		// }
////		return true;
//	}

    /*
     * 0 单位
     *
     * Bit0: 单位units ==0 美制/英制US/Imperial ==1 公制Metric Bit2- Bit1: Fuel*
     * Consumption/油耗 00b L/100KM 01b KM/L 10b MPG（US） 11b MPG（UK）③V1.19.000
     * Bit3: Distance/距离 ==0 km ==1 mi Bit4: Temperature/温度 ==0 摄氏度 ==1 华氏度
     * Bit6-Bit5 Pressure/压力 00b psi 01b kPa 10b bar Bit7 Speed ==0 km/h ==1 MPH
     *
     * 0xff 为无效值
     *
     * 低字节在前 1-2车速, 3-4转速, 5-7总里程, 显示值= distance *0.1 (如456KM，显示45.6KM) 8-9续航里程,
     * 10 档位 bit0~3: 0:未知 1:r 2:n 3:p 4:d bit4:手刹状态 0：手刹放下 1:手刹拉起 11 报警项 安全带
     * bit0~1: 0:未知 1, 扣 2, 未扣 。 12 油量 13 水温 x-40
     *
     * 14 灯光
     *
     * Bit7: 倒车灯 0：关 1：开 Bit6: 刹车灯 0：关 1：开 Bit5: 右转向灯 0：关 1：开 Bit4: 左转向灯 0：关 1：开
     * Bit3: 警示灯（双闪灯）0：关 1：开 Bit2: 近光灯 0：关 1：开 Bit1: 远光灯 0：关 1：开 Bit0: 示宽灯 0：关
     * 1：开
     */
    private final static int NUM_DRIVE_DATA = 15;
    public byte[] mDriveData;// = new byte[NUM_DRIVE_DATA];

    public void returnDriveData() {
        if (mRequestDriveData > 0) {
            if (mContext != null) {
                Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
                i.putExtra(MyCmd.EXTRA_COMMON_CMD,
                        MyCmd.Cmd.CANBOX_RETURN_DRIVE_DATA);
                i.putExtra(MyCmd.EXTRA_COMMON_DATA, mDriveData);
                mContext.sendBroadcast(i);
                Log.d(TAG, "returnDriveData:" + Util.byte2HexStr(mDriveData));
            }
        }
    }

    public int mRequestDriveData = 0;

    public void requestDriveData(int on) {
        if (on == 0x1) {
            mRequestDriveData = 1;
            if (mDriveData == null) {
                mDriveData = new byte[NUM_DRIVE_DATA];
                for (int i = 0; i < mDriveData.length; ++i) {
                    mDriveData[i] = -1;
                }
            }
            returnDriveData();
        } else {
            mRequestDriveData = 0;
        }
    }


    public boolean isNeedRepeatCmd() { //pg is repeat in bin
        return true;
    }

    /*==============new version ========================*/

    /*
     *byte 1: ID
     *byte 2: style
     *byte 3: mask id door. max num in Radar.
     *byte 4: index in array.only door need*/

    public boolean mSupportRaise0x7d = false;
    public int mIdAC = 0;
    public int mIdRadarFront = 0;
    public int mIdRadarBack = 0;
    public int mIdRadarFrontEx = 0;
    public int mIdRadarBackEx = 0;
    public int mIdAngle = 0;
    public int mIdDoor = 0;
    public int mIdEQ = 0;
    public int mIdOutTemp = 0;
    public int mIdKey = 0;
    public int mIdKey2 = 0;
    public int mIdKey3 = 0;
    public int mIdVersion = 0;
    public int mIdBrake = 0;

    public byte MAP_KEYS[][];
    public byte MAP_KEYS2[][];
    public byte MAP_KEYS3[][];

    public byte IDS_TO_CANBOXSETTINGS[];

    public final static int RADAR_STEP_MAX = 11;

    public void buildBrake(byte cmd, byte arrayIndex, byte mask, byte style) {
        mIdBrake = (cmd & 0xff) | ((arrayIndex & 0xff) << 8)
                | ((style & 0xff) << 24) | ((mask & 0xff) << 16);
    }

    public void buildCmdKey(byte cmd, byte style, byte index, byte arrayIndex, byte[][] maps) {
        switch (arrayIndex) {
            case 2:
                mIdKey2 = (cmd & 0xff) | ((style & 0xff) << 8) | ((index & 0xff) << 16);
                MAP_KEYS2 = maps;
                break;
            case 3:
                mIdKey3 = (cmd & 0xff) | ((style & 0xff) << 8) | ((index & 0xff) << 16);
                MAP_KEYS3 = maps;
                break;
            default:
                mIdKey = (cmd & 0xff) | ((style & 0xff) << 8) | ((index & 0xff) << 16);
                MAP_KEYS = maps;
                break;
        }
    }

    public void buildCmdKey(byte cmd, byte arrayIndex, byte[][] maps) {
        buildCmdKey(cmd, (byte) 0, (byte) 0, arrayIndex, maps);
    }

    public void buildCmdVersion(byte cmd) {
        mIdVersion = ((cmd & 0xff) & 0xff);
    }

    public void buildCmdVersion(byte cmd, byte style) {
        mIdVersion = (cmd & 0xff) | ((style & 0xff) << 8);
    }

    public void buildCmdOutTemp(byte cmd, byte style) {
        mIdOutTemp = (cmd & 0xff) | ((style & 0xff) << 8);
    }

    public void buildCmdEQ(byte cmd, byte style, int len) {
        mIdEQ = (cmd & 0xff) | ((style & 0xff) << 8) | ((len & 0xff) << 16);
    }

    public void buildCmdKey(byte cmd, byte keys[][], int index) {
        switch (index) {
            case 0:
                mIdKey = cmd;
                MAP_KEYS = keys;
                break;
        }
    }

    //左正 右负
    public void buildCmdAngle(byte cmd, byte style, int max) {
        mIdAngle = (cmd & 0xff) | ((style & 0xff) << 8)
                | ((max & 0xffff) << 16);
    }

    public void buildCmdDoor(byte cmd, byte style, byte mask, byte dataIndex) {
        mIdDoor = (cmd & 0xff) | ((style & 0xff) << 8) | ((mask & 0xff) << 16)
                | ((dataIndex & 0xff) << 24);
    }

    public void buildCmdDoor(byte cmd, byte style, byte mask, byte dataIndex, byte subCmd) {
        mIdDoor = (cmd & 0xff) | ((style & 0xff) << 8) | ((mask & 0xff) << 16)
                | ((dataIndex & 0xf) << 24) | ((dataIndex & 0xf) << 28);
    }

    public int buildCmdRadar(byte cmd, byte style, byte max, byte num) {
        return ((cmd & 0xff) | ((style & 0xff) << 8) | ((max & 0xff) << 16) | ((num & 0xff) << 24));
    }

    public void buildCmdRadarFront(byte cmd, byte style, byte max, byte num) {
        mIdRadarFront = buildCmdRadar(cmd, style, max, num);
    }

    public void buildCmdRadarFront(byte cmd, byte style, byte max) {
        buildCmdRadarFront(cmd, style, max, (byte) 4);
    }

    public void buildCmdRadarBack(byte cmd, byte style, byte max, byte num) {
        mIdRadarBack = buildCmdRadar(cmd, style, max, num);
    }

    public void buildCmdRadarBackEx(byte arrayIndex) {
        mIdRadarBackEx = (arrayIndex & 0xff);
    }

    public void buildCmdRadarFrontEx(byte arrayIndex) {
        mIdRadarFrontEx = (arrayIndex & 0xff);
    }

    public void buildCmdRadarBackEx(byte arrayIndex, byte style2) {
        mIdRadarBackEx = (arrayIndex & 0xff) | ((style2 & 0xff) << 8);
    }

    public void buildCmdRadarFrontEx(byte arrayIndex, byte style2) {
        mIdRadarFrontEx = (arrayIndex & 0xff) | ((style2 & 0xff) << 8);
    }

    public void buildCmdRadarBack(byte cmd, byte style, byte max) {
        buildCmdRadarBack(cmd, style, max, (byte) 4);
    }

    public void buildCmdRadarBack(byte cmd, byte style, byte max, byte midMax,
                                  byte num) {
        buildCmdRadarBack(cmd, (byte) (style & 0x7 | (midMax << 3)), max,
                (byte) num);
    }

    public void buildCmdRadarFront(byte cmd, byte style, byte max, byte midMax,
                                   byte num) {
        buildCmdRadarFront(cmd, (byte) (style & 0x7 | (midMax << 3)), max,
                (byte) num);
    }

    public void buildCmdRepeatSendCarType(byte[] cmd) {
        buildCmdRepeatSendCarType(cmd, -1);
    }

    public void buildCmdRepeatSendCarType(byte[] cmd, int time) {
        if (cmd != null) {
            mRepeatSendCarType = cmd;
            mRepeatCmdTime = time;
            autoUpdateCarType();
        }
    }

    private byte[] mRepeatSendCarType;
    private int mRepeatCmdTime = -1;

    public void parseCanboxData(byte[] data, int len) {
        if ((data[0] & 0xff) == (mIdAC & 0xff)) {
            parseACInfo(data);
        } else if ((data[0] & 0xff) == (mIdDoor & 0xff)) {
            parseDoor(data);
        }

        if ((data[0] & 0xff) == (mIdKey & 0xff)) {
            parseWheelKey(mIdKey, data, MAP_KEYS);
        } else if ((data[0] & 0xff) == (mIdKey2 & 0xff)) {
            parseWheelKey(mIdKey2, data, MAP_KEYS2);
        } else if ((data[0] & 0xff) == (mIdKey3 & 0xff)) {
            parseWheelKey(mIdKey3, data, MAP_KEYS3);
        } else if ((data[0] & 0xff) == (mIdEQ & 0xff)) {
            parseEQ(mIdEQ, data);
        } else if ((data[0] & 0xff) == (mIdVersion & 0xff)) {
            parseVersion(mIdVersion, data);
        }

        if ((data[0] & 0xff) == (mIdRadarFront & 0xff)) {
            parseRadarFront(mIdRadarFront, data);
        }
        if ((data[0] & 0xff) == (mIdRadarBack & 0xff)) {
            parseRadarBack(mIdRadarBack, data);
        }

        if ((data[0] & 0xff) == (mIdAngle & 0xff)) {
            parseAngle(mIdAngle, data);
        }

        if ((data[0] & 0xff) == (mIdBrake & 0xff)) {
            parseBrake(mIdBrake, data);
        }

        if ((data[0] & 0xff) == (mIdOutTemp & 0xff)) {
            parseOutTemp(data);
        }

        if (IDS_TO_CANBOXSETTINGS != null) {
            for (byte b : IDS_TO_CANBOXSETTINGS) {
                if (data[0] == b) {
                    sendCanboxInfo("com.canboxsetting", data);
                    break;
                }
            }
        }

        if (mSupportRaise0x7d && (data[0] & 0xff) == (0x7d)) {
            parse0x7D(data);
        }

    }

    private void parse0x7D(byte[] data) {
        switch (data[2]) {

            case 5:
                byte door = doorChangeStyle1(data[3]);
                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS,
                                mDoorStatus, 0));

                    }
                }
                break;
            case 8:// angle
            {

                int a = (short) ((data[3] & 0xff) | (((data[4] & 0xff)) << 8));
                int angle = -a;

                angle = ((angle * 3000) / 540);
                if (angle > -50 && angle < 50) {
                    angle = 50;
                }

                // 右转<0
                Log.d(TAG, ":" + angle);

                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
                            angle, 100));
                }

                break;
            }
        }
    }

    public byte[] mEQData = null;

    public void parseEQ(int id, byte[] buf) {
        int style = ((id & 0xff00) >> 8);
        int len = ((id & 0xff0000) >> 16);
//		byte[] data = null;
        switch (style) {
            case 0:
                mEQData = new byte[len];
                mEQData[0] = buf[8];
                mEQData[1] = buf[7];
                mEQData[2] = buf[6];
                mEQData[3] = buf[5];
                mEQData[4] = buf[4];
                if (len > 5) {
                    mEQData[5] = buf[3];
                }
                break;
            case 1:
                mEQData = new byte[len];
                mEQData[0] = buf[7];
                mEQData[1] = buf[6];
                mEQData[2] = buf[5];
                mEQData[3] = buf[4];
                mEQData[4] = buf[3];
                mEQData[5] = buf[2];
                break;
            case 2:
                mEQData = new byte[len];
                mEQData[0] = buf[3];
                mEQData[1] = buf[4];
                mEQData[2] = buf[5];
                mEQData[3] = buf[7];
                mEQData[4] = buf[6];
                mEQData[5] = buf[2];
                break;
        }
        if (mEQData != null) {
            returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
        }
    }

    public int getAngleValue(byte[] data) {
        return Integer.MAX_VALUE;
    }

    public int getAngleValue2(byte[] data) {
        return Integer.MAX_VALUE;
    }

    private void parseAngle(int id, byte[] data) {
        int style = ((id & 0xff00) >> 8);
        int max = (id & 0xffff0000) >> 16;

        int angle = getAngleValue(data);
        if (angle == Integer.MAX_VALUE) {
            angle = getAngleValue2(data);
            if (angle == Integer.MAX_VALUE) {
                short a;
                switch (style) {
                    case 0:
                        a = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));
                        angle = a;
                        break;
                    case 1:
                        angle = ((data[3] & 0xff) | (((data[2] & 0x7f)) << 8));
                        if ((data[2] & 0x80) != 0) {
                            angle = -angle;
                        }
                        break;
                    case 3:
                        a = (short) ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));
                        angle = a;
                        break;
                    case 4:
                        a = (short) ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));
                        angle = -a;
                        break;
                    case 5:
                        a = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));
                        angle = -a;
                        break;
                    case 6:
                        angle = data[2];
                        break;
                }
            }
            angle = ((angle * 3000) / max);
            if (angle > -50 && angle < 50) {
                angle = 50;
            }
        }
        //右转<0
        Log.d(TAG, ":" + angle);

        Handler handler = getHandler("Reverse");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
                    angle, 100));
        }
    }

    public void parseRadar() {
        if (!Util.isZero(mRadar)) {
            RadarManager.start(mContext);
            checkHideRadarEx();
        }
        Handler handler = getHandler(RadarManager.TAG);
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
        }
    }

    private void parseRadar(int id, byte[] data, int startIndex, int arrayIndex, int style2) {
        int style = ((id & 0x700) >> 8);
        int max = ((id & 0xff0000) >> 16);
        int num = ((id & 0xff000000) >> 24);
        int midMax = ((id & 0xf800) >> 11);

        if (midMax == 0) {
            midMax = max;
        }

        mRadar[startIndex + 0] = radarChangeStyle(data[arrayIndex + 2], max,
                style);
        if (num == 4) {
            mRadar[startIndex + 1] = radarChangeStyle(data[arrayIndex + 3],
                    midMax, style);
            mRadar[startIndex + 2] = radarChangeStyle(data[arrayIndex + 4],
                    midMax, style);
            mRadar[startIndex + 3] = radarChangeStyle(data[arrayIndex + 5],
                    max, style);
        } else if (num == 3) {
            if (style2 == 0) {
                mRadar[startIndex + 1] = radarChangeStyle(data[arrayIndex + 3],
                        midMax, style);
                mRadar[startIndex + 2] = radarChangeStyle(data[arrayIndex + 3],
                        midMax, style);
                mRadar[startIndex + 3] = radarChangeStyle(data[arrayIndex + 4],
                        max, style);
            } else if (style2 == 1) {
                mRadar[startIndex + 1] = radarChangeStyle(data[arrayIndex + 3],
                        midMax, style);
                mRadar[startIndex + 2] = radarChangeStyle(data[arrayIndex + 3],
                        midMax, style);
                mRadar[startIndex + 3] = radarChangeStyle(data[arrayIndex + 5],
                        max, style);
            }

        } else if (num == 2) {
            if (style2 == 0) {
                mRadar[startIndex + 1] = 0;
                mRadar[startIndex + 2] = 0;
                mRadar[startIndex + 3] = radarChangeStyle(data[arrayIndex + 3],
                        max, style);
            } else if (style2 == 1) {
                mRadar[startIndex + 1] = radarChangeStyle(data[arrayIndex + 3],
                        max, style);
            }
        } else if (num == 1) {
            mRadar[startIndex + 1] = mRadar[startIndex + 0];
            mRadar[startIndex + 2] = mRadar[startIndex + 0];
            mRadar[startIndex + 3] = mRadar[startIndex + 0];
        }

        parseRadar();
    }

    public void parseRadarBack(int id, byte[] data) {
        parseRadar(id, data, 0, (mIdRadarBackEx & 0xff), (mIdRadarBackEx & 0xff00) >> 8);
    }

    public void parseRadarFront(int id, byte[] data) {
        parseRadar(id, data, 4, (mIdRadarFrontEx & 0xff), (mIdRadarBackEx & 0xff00) >> 8);
    }

    public byte radarChangeStyle(byte data, int max, int style) {
        byte ret = 0;
        int step = (max * 100) / RADAR_STEP_MAX;

        switch (style) {
            case 0://越小越近
                if ((data & 0xff) > 0 && (data & 0xff) <= max) {
                    ret = (byte) (((((data & 0xff) - 1) * 100) / step) + 1);

                } else {
                    ret = 0;
                }
                break;
            case 1:
                if ((data & 0xff) > 0 && (data & 0xff) <= max) {
                    data = (byte) (max - (data & 0xff) + 1);
                    ret = (byte) (((((data & 0xff) - 1) * 100) / step) + 1);

                } else {
                    ret = 0;
                }
                break;
        }
        return ret;
    }

    public void parseVersion(int id, byte[] data) {
        int style = ((id & 0xff00) >> 8);
        int len = ((id & 0xff0000) >> 16);
        if (len == 0) {
            len = data.length - 3;
            if (data[data.length - 2] == 0) {
                len--;
            }
        }
        byte[] version;
        switch (style) {
            case 0:
                version = new byte[len];
                Util.byteArrayCopy(version, data, 0, 2, version.length);
                mVersion = (new String(version));
                break;
        }

        Util.setProperty("canbox_version", mVersion);
        Log.d(TAG, "version:" + mVersion);
    }

    public void parseBrake(int id, byte[] data) {
        if (CarUtil.mSettingCanboxBrake != 0) {
            int data_index = ((id & 0xff00) >> 8);
            int mask = ((id & 0xff0000) >> 16);
            int style = ((id & 0xff000000) >> 24);

            int v = -1;
            if (style > 0xF) {
                style = ((id & 0x0f000000) >> 24);
                switch (style) {
                    case 0xf:
                        int subCmd = ((id & 0xf0000000) >> 28);
                        if (subCmd == (data[2] & 0xff)) {
                            if (data_index < data.length) {
                                v = data[data_index] & mask;

                            }
                        }
                        break;
                }
            } else {
                switch (style) {
                    case 0:
                        if (data_index < data.length) {
                            v = data[data_index] & mask;
                        }
                        break;
                }
            }
            if (v != -1) {
                CarUtil.updateCanboxBrake(v == 0 ? 0 : 1);
            }
            Log.d(TAG, "parseBrake:" + v);
        }
    }

    public byte mDoorStatus;

    public void parseDoor(byte[] data) {
        int style = ((mIdDoor & 0xff00) >> 8);
        byte mask = (byte) ((mIdDoor & 0xff0000) >> 16);
        byte index = (byte) ((mIdDoor & 0x0f000000) >> 24);
        byte subCmd = (byte) ((mIdDoor & 0xf0000000) >> 28);

        if (subCmd != 0 && index > 2) {
            if (subCmd != data[2]) {
                return;
            }
        }

        if (mask == 0) {
            mask = (byte) 0xff;
        }
        byte door = (byte) (data[index] & mask);
        switch (style) {
            case 1:
                door = doorChangeStyle1(door);
                break;
            case 2:
                door = doorChangeStyle2(door);
                break;
            case 3:
                door = doorChangeStyle3(door);
                break;
            case 4:
                door = doorChangeStyle4(door);
                break;
        }

        if (mDoorStatus != door) {
            mDoorStatus = door;
            Handler handler = getHandler("CanService");
            if (null != handler) {
                handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS,
                        mDoorStatus, 0));

            }
        }
    }


    public byte doorChangeStyle1(byte door) {
		/*Bit7
：右前门
Bit6
：左前门
Bit5
：右后门
Bit4
：左后门
Bit3
：后尾箱
Bit2
：引擎盖*/
        return (byte) (((door & 0x40) >> 6) | ((door & 0x80) >> 6)
                | ((door & 0x10) >> 2) | ((door & 0x20) >> 2)
                | ((door & 0x08) << 1) | ((door & 0x4) << 3));
    }

    public byte doorChangeStyle2(byte door) {
		/*
Bit7:左前门
Bit6:右前门
Bit5:左后门
Bit4:右后门
Bit3:尾箱*/
        return (byte) (((door & 0x40) >> 5) | ((door & 0x80) >> 7)
                | ((door & 0x10) >> 1) | ((door & 0x20) >> 3)
                | ((door & 0x08) << 1) | ((door & 0x4) << 3));
    }


    public byte doorChangeStyle3(byte door) {
		/*Bit7
：右前门
Bit6
：左前门
Bit5
：左后门
Bit4
：右后门
Bit3
：后尾箱
Bit2
：引擎盖*/
        return (byte) (((door & 0x40) >> 6) | ((door & 0x80) >> 6)
                | ((door & 0x10) >> 1) | ((door & 0x20) >> 3)
                | ((door & 0x08) << 1) | ((door & 0x4) << 3));
    }

    public byte doorChangeStyle4(byte door) {
		/*Bit5
：左前门
Bit4
：右前门
Bit3
：左后门
Bit2
：右后门
Bit1
：后尾箱
Bit0
：引擎盖*/
        return (byte) (((door & 0x20) >> 5) | ((door & 0x10) >> 3)
                | ((door & 0x08) >> 1) | ((door & 0x04) << 1)
                | ((door & 0x02) << 3) | ((door & 0x1) << 5));
    }

    public void parseWheelKey(int id, byte[] data, byte keys[][]) {
        int style = ((id & 0xff00) >> 8);
        int index;
        int key;
        switch (style) {
            case 0:
                if (data[1] == 2) {
                    parseWheelKey0(data, keys);
                } else if (data[1] == 1) {
                    index = 2;
                    key = data[index] & 0xff;
                    parseWheelKey1(key, key == 0 ? 0 : 1, keys);
                } else {
                    parseWheelKey0(data, keys);
                }
                break;
            case 1:
                index = ((id & 0xff0000) >> 16);
                key = data[index] & 0xff;
                parseWheelKey1(key, key == 0 ? 0 : 1, keys);
                break;
            case 2:
                index = ((id & 0xff0000) >> 16);
                key = data[index] & 0xff;
                parseWheelKey1(key, data[index + 1], keys);
                break;
            case 5:
                index = ((id & 0xff0000) >> 16);
                key = data[index] & 0xff;
                parseWheelKeyOneMsg(key, keys);
                break;
            case 6:
                index = ((id & 0xff0000) >> 16);
                if (data[index + 1] != 0) {
                    key = data[index] & 0xff;
                    parseWheelKeyOneMsg(key, keys);
                }
                break;
            case 3:
                index = ((id & 0xff0000) >> 16);
                key = data[index] & 0xff;
                if (data[index + 1] < 0) {
                    key += 0x10;
                    data[index + 1] = (byte) -data[index + 1];
                }
                if (data[index + 1] > 0) {
                    parseWheelKey1(key, data[index + 1], keys);
                }
                break;
            case 4:
                index = ((id & 0xff0000) >> 16);
                key = data[index] & 0xff;
                parseWheelKeyAllRoll(key, data[index + 1], keys);
                break;
            case 0x10:
                index = ((id & 0xff0000) >> 16);
                key = data[index] & 0xff;
                parseWheelKey0x10(key, data[index + 1], keys);
                break;
            case 0x11:
                index = ((id & 0xff0000) >> 16);
                key = data[index] & 0xff;
                parseWheelKey0x11(key, data[index + 1], keys);
                break;
        }
    }

    private final static byte ROLL_KEYS[][] = {
            {MyCmd.Keycode.SMART_CW, MyCmd.Keycode.NEXT},
            {MyCmd.Keycode.SMART_CCW, MyCmd.Keycode.PREVIOUS},
            {MyCmd.Keycode.ROLL_NEXT, MyCmd.Keycode.NEXT},
            {MyCmd.Keycode.ROLL_PREV, MyCmd.Keycode.PREVIOUS},
            {MyCmd.Keycode.VOLUME_ROLL_UP, MyCmd.Keycode.VOLUME_UP},
            {MyCmd.Keycode.VOLUME_ROLL_DOWN, MyCmd.Keycode.VOLUME_DOWN},
    };

    private void doKeyRoll(int key, int step) {
        mHandlerRadar.removeMessages(REPEAT_SEND_ROLL_KEY);
        if (step > 0) {
            doKey(key, 1);
            doKey(key, 0);
            Util.setFileValue("/sys/class/ak/source/beep", "2");
        }
        --step;
        if (step > 0) {
            mHandlerRadar.sendMessageDelayed(mHandlerRadar.obtainMessage(
                    REPEAT_SEND_ROLL_KEY, key, step), 30);
        }
    }

    private int isRollKey(byte key) {
        for (byte[] b : ROLL_KEYS) {
            if (key == b[0]) {
                return b[1] & 0xff;
            }
        }
        return Integer.MAX_VALUE;
    }

    private boolean isVolumeKey(int key) {
        if (key == MyCmd.Keycode.VOLUME_UP || MyCmd.Keycode.VOLUME_DOWN == key) {
            return true;
        }
        return false;
    }

    public void parseWheelKey0(byte[] data, byte keys[][]) {
        if (doKeyStudy(data[2], data[3])) {
            return;
        }
        byte key = 0;
        for (int i = 0; i < keys.length; ++i) {
            if (keys[i][0] == data[2]) {
                key = keys[i][1];
                break;
            }
        }

        if (key != 0) {
            int rollKey = isRollKey(key);
            if (rollKey == Integer.MAX_VALUE) {
                if (isVolumeKey(key)) {
                    doKeyRoll(key, data[3]);
                } else {
                    doKey(key, data[3]);
                }
            } else if (data[3] > 0) {
                doKeyRoll(rollKey, data[3]);
            }
        } else {
            if (data[3] == 0) {
                doKey(0, 0);
            }
        }
    }

    public void parseWheelKey1(int keyCan, int down, byte keys[][]) {
        if (doKeyStudy(keyCan, down)) {
            return;
        }
        byte key = 0;
        for (int i = 0; i < keys.length; ++i) {
            if (keys[i][0] == keyCan) {
                key = keys[i][1];
                break;
            }
        }

        if (key != 0) {
            int rollKey = isRollKey(key);
            if (rollKey == Integer.MAX_VALUE) {
                doKey(key, down);
            } else {
                doKeyRoll(rollKey, down);
            }
        } else {
            if (down == 0) {
                doKey(0, 0);
            }
        }
    }

    private void parseWheelKeyOneMsg(int keyCan, byte keys[][]) {
        parseWheelKey1(keyCan, 1, keys);
        Util.doSleep(50);
        parseWheelKey1(keyCan, 0, keys);
    }

    private void parseWheelKeyAllRoll(int keyCan, int down, byte keys[][]) {
        if (doKeyStudy(keyCan, down)) {
            return;
        }
        byte key = 0;
        for (int i = 0; i < keys.length; ++i) {
            if (keys[i][0] == keyCan) {
                key = keys[i][1];
                break;
            }
        }

        if (key != 0) {
            doKeyRoll(key, down);
        } else {
            if (down == 0) {
                doKey(0, 0);
            }
        }
    }

    private void parseWheelKey0x10(int keyCan, int down, byte keys[][]) {
        if (doKeyStudy(keyCan, down)) {
            return;
        }
        byte key = 0;
        for (int i = 0; i < keys.length; ++i) {
            if (keys[i][0] == keyCan) {
                key = keys[i][1];
                break;
            }
        }

        if (key != 0) {
            int rollKey = isRollKey(key);
            if (rollKey == Integer.MAX_VALUE) {
                doKey(key, down);
            } else {
                doKeyRoll(rollKey, down);
            }
        } else {
            if (down == 0) {
                doKey(0, 0);
            }
        }
    }

    private void parseWheelKey0x11(int keyCan, int down, byte keys[][]) {
        if (doKeyStudy(keyCan, down)) {
            return;
        }
        byte key = 0;
        byte org = 0;
        int step = 0;
        for (int i = 0; i < keys.length; ++i) {
            if (keys[i][0] == keyCan) {
                key = keys[i][1];
                if (keys[i].length > 2) {
                    org = keys[i][2];
                    keys[i][2] = (byte) down;
                }

                if (down == org) {
                    return;
                } else if (down > org) {
                    step = down - org;
                } else {
                    step = org - down;
                    key = 0;
                    keyCan += 0x10;
                    for (i = 0; i < keys.length; ++i) {
                        if (keys[i][0] == keyCan) {
                            key = keys[i][1];
                            break;
                        }
                    }
                }
                break;
            }
        }

        if (key != 0) {
            int rollKey = isRollKey(key);
            if (rollKey == Integer.MAX_VALUE) {
                doKey(key, down);
            } else {
                doKeyRoll(rollKey, step);
            }
        } else {
            if (down == 0) {
                doKey(0, 0);
            }
        }
    }

    public int getACTemp(byte data) {//
        return Integer.MAX_VALUE;
    }

    public int getACTemp(byte data, int unit) {//
        int temp = getACTemp(data);
        if (temp != Integer.MAX_VALUE) {
            return temp;
        }
        return Integer.MAX_VALUE;
    }

    public int getTempUnit() {//
        return Integer.MAX_VALUE;
    }

    private byte[] mAirData = null;

    public void updateCommonAirData(byte[] data) {
        if (mAirData != null && data != null) {
            for (int i = 0; (i < data.length) && (i < mAirData.length); ++i) {
                mAirData[i] = data[i];
            }
        }
    }

    public void parseACInfo(byte[] dataIn) {
        byte[] data = new byte[dataIn.length];
        Util.byteArrayCopy(data, dataIn, 0, 0, dataIn.length);

        if (mAirData == null) {
            mAirData = new byte[data.length];
        }

        int temp = getACTemp(data[2], data[5] & 0x1);

        if (temp != Integer.MAX_VALUE) {
            data[2] = (byte) temp;
            data[3] = (byte) getACTemp(data[3], data[5] & 0x1);
        }

        int msg = 0;
        if ((data[0] & 0x80) == 0) {
            msg = CANBOX_HIDE_AIR;
        }

        if (!Arrays.equals(mAirData, data)) {
            Util.byteArrayCopy(mAirData, data, 0, 0, data.length);
            msg = CANBOX_RETURN_AIR;
        }

        if (!sendCanboxAir(data) && msg != 0) {
            Handler handler = getHandler("CanService");
            if (null != handler) {
                handler.sendMessage(handler.obtainMessage(msg,
                        data));
            }
        }
    }

    public void parseACInfoRear(byte[] data) {
        if (mAirData == null) {
            mAirData = new byte[data.length];
        }

        int msg = 0;
        if ((data[0] & 0x80) == 0) {
            msg = CANBOX_HIDE_AIR;
        }

        if (!Arrays.equals(mAirData, data)) {
            Util.byteArrayCopy(mAirData, data, 0, 0, data.length);
            msg = CANBOX_RETURN_AIR;
        }

        if (!sendCanboxAir(data) && msg != 0) {
            Handler handler = getHandler("CanService");
            if (null != handler) {
                handler.sendMessage(handler.obtainMessage(msg,
                        data));
            }
        }
    }


    public void parseSeatHeat() {
//		if (AppConfig.getTopActivity().contains("com.canboxsetting.SeatHeatActivity")){
//			return false;
//		}
    }

    private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
    private int mUnit = 0;

    public int getOutTemp(byte[] data) {//
        return CarUtil.INVALID_OUT_DOOR_TEMP;
    }

    public int getOutTempUnit(byte[] data) {//
        return 0;
    }

    public void parseOutTemp(byte[] data) {
        int temp = getOutTemp(data);
        mUnit = getOutTempUnit(data);
        if (temp == Integer.MAX_VALUE) {
            int style = ((mIdOutTemp & 0xff00) >> 8);
            switch (style) {
                case 0:
                    temp = ((data[2] & 0xff) - 40) * 10;
                    break;
                case 1:
                    temp = (data[2] & 0x7f) * 10;
                    if ((data[2] & 0x80) != 0) {
                        temp = -temp;
                    }

                    break;
            }
        }
        updateOutDoorTemp(temp);
    }

    public void updateOutDoorTemp(int temp) {
        String s = "";
        if (temp == CarUtil.CLEAR_OUT_DOOR_TEMP) {
            s = " ";
        } else {
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


            if (mUnit != 1) {
                if (temp < 0) {
                    s = "-";
                    temp = -temp;
                }
                s = s + String.format(
                        "%d.%d%s",
                        temp / 10,
                        (temp % 10) >= 0 ? (temp % 10) : -(temp % 10),
                        mContext.getResources().getString(
                                R.string.temp_unic_centigrade));

            } else {
                s = String.format("%d%s", temp / 10, mContext.getResources()
                        .getString(R.string.temp_unic_fahrenheit));
            }
        }
        if (s.length() > 1) {
            GlobalDef.sendByCarServiceToSystemUI(mContext,
                    "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
        }
    }

    // reverse
    public int getReverseViewID() {
        return Integer.MAX_VALUE;
    }

    public void sendReverseCmd(int cmd) {

    }

    //
    public void doCanboxFunctionKey(byte key) {
        switch (key) {
            case MyCmd.Keycode.CANBOX_OPEN_AC_VIEW:
                break;
        }
    }

    public byte fixTimeHour(byte h) {
        return (byte) ((h + CarUtil.getTimeAddOrMinus1() + 24) % 24);
    }

    //util


    public byte make_bit(byte b, int mask, int s, int d) {
        int move = d - s;
        if (move < 0) {
            b = (byte) ((b & mask) >> move);
        } else {
            b = (byte) ((b & mask) << move);
        }
        return b;
    }

    public byte[] getBytesUnicodeLittleEndian(String num) {
        byte[] n = null;
        try {
            n = num.getBytes("Unicode");
            if ((n[0] & 0xff) == 0xfe && (n[1] & 0xff) == 0xff) {
                byte b;
                for (int i = 0; i < n.length; i += 2) {
                    b = n[i];
                    n[i] = n[i + 1];
                    n[i + 1] = b;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "getBytesUnicodeLittleEndian" + e);
        }
        return n;
    }

    public byte[] getBytesUnicodeBigEndian(String num) {
        byte[] n = null;
        try {
            n = num.getBytes("Unicode");
            if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
                byte b;
                for (int i = 0; i < n.length; i += 2) {
                    b = n[i];
                    n[i] = n[i + 1];
                    n[i + 1] = b;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "getBytesUnicodeLittleEndian" + e);
        }
        return n;
    }

    public void startRepeatSendLcdMsg(boolean b) {
        mHandlerRadar.removeMessages(REPEAT_SEND_LCD);
        if (b) {
            mHandlerRadar.sendEmptyMessageDelayed(REPEAT_SEND_LCD, 1000);
        }
    }

    public void repeatSendLcdMsg() {
        startRepeatSendLcdMsg(true);
    }

    //ac set
    public final static int MASK_AC_MAX = 0x0402;

    public void setACData(byte[] airData, byte[] buf, int index, int bit,
                          int mask) {
        int ac_index = (mask & 0xff00) >> 8;
        int ac_mask = (mask & 0xff);
        int shift = ac_mask - bit;

        airData[ac_index] &= ~(0x1 << ac_mask);
        if (shift > 0) {
            airData[ac_index] |= ((buf[index] & (0x1 << bit)) << shift);
        } else {
            shift = -shift;
            airData[ac_index] |= ((buf[index] & (0x1 << bit)) >> shift);
        }

    }
}
