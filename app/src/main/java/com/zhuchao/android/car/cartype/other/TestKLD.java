package com.zhuchao.android.car.cartype.other;


import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.common.util.shell.ShellUtils;
import com.rockchip.car.recorder.utils.SystemProperties;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.hardware.Mcu;
import com.zhuchao.android.car.manager.AutoIlluminManager;
import com.zhuchao.android.car.manager.McuManager;
import com.zhuchao.android.car.ui.BacklightPanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;

public class TestKLD extends Canbox {

    private final McuManager mMcuManager;

    public final static String TAG = "KLD";

    public TestKLD() {
        // sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
        // 0x0, 0x0 });
        // sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
        // 0x5, 0x0 });

        buildCmdKey((byte) 0x20, (byte) 5, (byte) 2, (byte) 0, KEYS_WHEEL);
        buildCmdKey((byte) 0x21, (byte) 5, (byte) 2, (byte) 2, KEYS_WHEEL2);

        mMcuManager = McuManager.getInstance();


    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.RADIO}, {0x2, MyCmd.Keycode.AUDIO}, {0x3, MyCmd.Keycode.VIDEO}, {0x4, MyCmd.Keycode.BT}, {0x5, MyCmd.Keycode.AUX_IN}, {0x6, MyCmd.Keycode.NAVIGATION},
            {0x7, MyCmd.Keycode.KEY_F_CAMERA},

            {0x8, MyCmd.Keycode.KEY_CAMERA}, {0x9, MyCmd.Keycode.SPEECH}, {0xa, MyCmd.Keycode.SETUP}, {0xb, MyCmd.Keycode.DVD}, {0xc, MyCmd.Keycode.EASY_CONNECT}, {0xd, MyCmd.Keycode.DVR},

            {0x10, MyCmd.Keycode.PAUSE}, {0x11, MyCmd.Keycode.PLAY}, {0x12, MyCmd.Keycode.PREVIOUS}, {0x13, MyCmd.Keycode.NEXT},

    };

    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.PREVIOUS}, {0x4, MyCmd.Keycode.NEXT}, {0x5, MyCmd.Keycode.BT}, {0x6, MyCmd.Keycode.MUTE},
            {0x7, MyCmd.Keycode.MODLE}, {0x8, MyCmd.Keycode.SPEECH}, {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG}, {0xb, MyCmd.Keycode.BACK}, {0xc, MyCmd.Keycode.HOME},

    };

    @Override
    public void setContext(Context c) {
        // TODO Auto-generated method stub
        super.setContext(c);
        initGpsCompass();

        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        //		mHandlerTest.sendEmptyMessageDelayed(0, 12000);
        //		Log.d("ddck", "setContext !!!!!!!!!!!test!!!!!!!!:");
    }

    private static final int MSG_CHECK_RM_DATA = 8;
    private static final int MSG_CHECK_SYNC = 9;

    private static final String MCU_RECOVERY_FILE = "/sys/class/ak/source/factory";

    private final Handler mHandlerTest = new Handler() { //test
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    connectWifi("TP-LINK_47", "ak47ak47");//tt
                    break;
                case MSG_CHECK_RM_DATA:
                    if (ShellUtils.getAkdRunStatus()) {
                        mHandlerTest.removeMessages(MSG_CHECK_RM_DATA);
                        mHandlerTest.sendEmptyMessageDelayed(MSG_CHECK_RM_DATA, 500);
                    } else {
                        Util.sudoExec("sync");
                        mHandlerTest.removeMessages(MSG_CHECK_RM_DATA);
                        mHandlerTest.sendEmptyMessageDelayed(MSG_CHECK_SYNC, 500);
                    }
                    break;
                case MSG_CHECK_SYNC:
                    if (ShellUtils.getAkdRunStatus()) {
                        mHandlerTest.removeMessages(MSG_CHECK_RM_DATA);
                        mHandlerTest.sendEmptyMessageDelayed(MSG_CHECK_SYNC, 100);
                    } else {
                        Log.d("reset", "mcu reset");
                        Util.setFileValue(MCU_RECOVERY_FILE, new byte[]{
                                0x55, (byte) 0xaa, 0x00
                        });
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private byte mStatus = 0;
    private byte mAccStatus = 1;

    @Override
    public void stopConnect() {
        // TODO Auto-generated method stub
        //		mMcuManager.setAutoTest(0x1b, 0);
        //		GlobalDef.setTestingEx(false);
    }

    @Override
    public void startConnect() {
        //		mMcuManager.setAutoTest(0x1b, 1);
        //		GlobalDef.setTestingEx(true);
    }

    private void reportStatus() {
        byte[] send = new byte[]{0x78, 0x1, mStatus};
        sendDataToCanbox(send, send.length);
    }

    private int mReturnID = 0;
    private final static int[] RET_CMD2 = new int[]{
            0x010c, 0x0104, 0x0202, 0x0405, 0x0302, 0x0106, 0x0403, 0x0108
    };
    private final static int[] RET_CMD3 = new int[]{0x11000, 0x11001};

    public void parseVersion(int id, byte[] data) { // this for mcu pro

        boolean send = false;
        int c;

        Log.e("fccd", "!!!!!!!!!!!!" + data[0]);
        switch (data[0]) {
            case 0x9:
                switch (data[1]) {
                    case 1:
                        mSWCMode1 = data[2];
                        mSWCMode2 = data[2];
                        return;
                    case 2:
                        switch (data[2]) {
                            case 1:
                                mSWCMode1 = 3;
                                return;
                            case 2:
                                mSWCMode2 = 3;
                                return;
                        }
                        return;
                }
                return;
            case 0xb:
                switch (data[1]) {
                    case 1:
                        mPannelMode = data[2];
                        return;
                    case 2:
                        mPannelMode = 3;
                        return;
                }
                return;
        }
        // c = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8)
        // | ((data[2] & 0xff) << 0);
        // for (int i = 0; i < RET_CMD1.length; ++i) {
        // if (RET_CMD1[i] == c) {
        // send = true;
        // break;
        // }
        // }

        c = ((data[0] & 0xff) << 8) | ((data[1] & 0xff) << 0);
        for (int i = 0; i < RET_CMD2.length; ++i) {
            if (RET_CMD2[i] == c && mReturnID == c) {
                send = true;
                mReturnID = 0;
                break;
            }
        }

        if (!send) {
            c = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | ((data[2] & 0xff) << 0);
            for (int i = 0; i < RET_CMD3.length; ++i) {
                if (RET_CMD3[i] == c && mReturnID == c) {
                    send = true;
                    mReturnID = 0;
                    break;
                }
            }
        }

        if (send) {
            byte[] buf = new byte[data.length + 1];
            buf[0] = data[0];
            buf[1] = (byte) (buf.length - 2);
            Util.byteArrayCopy(buf, data, 2, 1, data.length - 1);
            sendDataToCanbox(buf, buf.length);
        }

        if (data[0] == 0x1) {
            if (data[1] == 0x10 || data[2] == 0x0) {
                mAccStatus = data[3];
            }
        }
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        byte[] send;

        if ((data[0] & 0xff) < 0x20) {

            byte[] buf = new byte[data.length - 2];
            buf[0] = data[0];
            Util.byteArrayCopy(buf, data, 1, 2, buf.length - 1);

            sendMcuData(buf);


            int cmd =/* ((data[0] & 0xff) << 8) |*/ (data[0] & 0xff);

            if (cmd == 0x02) {
                mReturnID = 0x0202;
            } else if (cmd == 0x03) {
                mReturnID = 0x0302;
            } else {
                cmd = ((data[0] & 0xff) << 16) | ((data[2] & 0xff) << 8) | (data[3] & 0xff);
                if (cmd == 0x040602) {
                    mReturnID = 0x0403;
                } else if (cmd == 0x040604) {
                    mReturnID = 0x0405;
                } else if (cmd == 0x011200) {
                    mReturnID = 0x010c;
                } else if (cmd == 0x010903) {
                    mReturnID = 0x0104;
                } else if (cmd == 0x010a00) {
                    mReturnID = 0x0106;
                } else if (cmd == 0x010107) {
                    mReturnID = 0x0108;
                }
            }
        } else {
            switch (data[0] & 0xff) {
                case 0x78:
                    switch (data[2]) {
                        case 1:
                            if ((data[3] & 0xff) == 0xec) {
                                mStatus = data[4];
                                if (mStatus == 1) {
                                    //							mMcuManager.setAutoTest(0x1b, 1);
                                    setTestGPS();
                                    GlobalDefinition.setTestingEx(true);
                                } else {
                                    GlobalDefinition.setTestingEx(false);
                                    //							mMcuManager.setAutoTest(0x1b, 0);
                                }
                            }
                        case 2:
                            sendToCan(0x78, mStatus);
                            break;
                    }
                    break;
                case 0x30:
                    if ((data[2] & 0xff) == 1) {
                        reset();
                    }
                    break;
                case 0x40:
                    mSnLen = data[1];
                    for (int i = 0; i < mSN.length && i < mSnLen; ++i) {
                        mSN[i] = data[2 + i];
                    }
                    break;
                case 0x80:
                    if ((data[2] & 0xff) == 2) {
                        if ((data[3] & 0xff) == 1) {
                            testGPSSound();
                        } else {
                            stopGPSSound();
                        }
                    }
                    break;
                case 0x50:
                    if ((data[2] & 0xff) == 1) {
                        int index = ((data[3] & 0xff) << 8 | (data[4] & 0xff));
                        BroadcastUtil.sendKey(mContext, MyCmd.makeEx2Key(MyCmd.Keycode.EX_KEY2_PLAY_MUSIC_SONG, index));
                    }
                    break;
                case 0x60://backlight
                    switch (data[2] & 0xff) {
                        case 1:
                            BacklightPanel.setBrightness(data[3] & 0xff, 0);
                            break;
                        case 2:
                            int value = SystemConfig.getIntProperty(mContext, AutoIlluminManager.DAY_BRIGHTNESS);
                            sendToCan(0x60, 0x2, (byte) value);
                            break;
                    }
                    break;
                case 0x70://wifi
                    switch (data[2] & 0xff) {
                        case 1:
                            statWifi(data[3] == 1);
                            break;
                        case 2:
                            sendToCan(0x70, 0x2, (byte) (mWifiManager.isWifiEnabled() ? 1 : 0));
                            break;
                        case 3:
                            sendToCan(0x70, 0x3, (byte) (isWifiConnected() ? 3 : 1));
                            break;
                        case 4:
                            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                            //					Log.d("kkfk", "" + wifiInfo.getRssi());
                            sendToCan(0x70, 0x4, (byte) (-wifiInfo.getRssi()));
                            break;
                        case 5:
                            int name_len = data[3] & 0xff;
                            int psw_len = data[4] & 0xff;
                            byte[] name = new byte[name_len];
                            byte[] psw = new byte[psw_len];

                            Util.byteArrayCopy(name, data, 0, 5, name_len);
                            Util.byteArrayCopy(psw, data, 0, 5 + name_len, psw_len);

                            connectWifi(new String(name), new String(psw));// tt
                            break;
                        case 6:
                            name_len = data[3] & 0xff;
                            name = new byte[name_len];
                            Util.byteArrayCopy(name, data, 0, 4, name_len);
                            connectionTest(new String(name));

                            break;
                        case 7:
                            sendToCan(0x70, 0x7, mNetStatus);
                            break;
                    }
                    break;
                case 0x90:// query
                    switch (data[2] & 0xff) {
                        case 1:
                            sendToCan(0x1, 0x10, 0x0, mAccStatus);
                            break;
                        case 2:
                            int reverse = Util.getFileValue("/sys/class/gpio-detection/car-reverse/status");
                            sendToCan(0x1, 0x10, 0x1, reverse);
                            break;
                        case 3:
                            sendToCan(0x90, 0x3, getSource());
                            break;
                        case 4:
                            send = new byte[]{
                                    (byte) 0x90, 0x5, 0x4, (byte) ((mMusicTime & 0xff00) >> 8), (byte) ((mMusicTime & 0xff) >> 0), (byte) ((mMusicPlayCur & 0xff00) >> 8),
                                    (byte) ((mMusicPlayCur & 0xff) >> 0)
                            };
                            sendDataToCanbox(send, send.length);
                            break;
                        case 5:
                            mReturnTAG |= RETURN_BT_NAME;
                            Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_BT);
                            it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.BT_REQUEST_A2DP_INFO);
                            mContext.sendBroadcast(it);
                            break;
                        case 0xb:
                            mReturnTAG |= RETURN_BT_MAC;
                            it = new Intent(MyCmd.BROADCAST_CMD_TO_BT);
                            it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.BT_REQUEST_A2DP_INFO);
                            mContext.sendBroadcast(it);
                            break;
                        case 6:
                            sendToCan(0x90, 0x6, (byte) mPhoneStatus);
                            break;
                        case 7:
                            sendToCan(0x90, 0x7, mGpsPos);
                            break;
                        case 8:
                            send = new byte[]{
                                    (byte) 0x90, 0x9, 0x8, (byte) ((mLatitude & 0xff000000) >> 24), (byte) ((mLatitude & 0xff0000) >> 16), (byte) ((mLatitude & 0xff00) >> 8),
                                    (byte) ((mLatitude & 0xff) >> 0), (byte) ((mLongtitude & 0xff000000) >> 24), (byte) ((mLongtitude & 0xff0000) >> 16), (byte) ((mLongtitude & 0xff00) >> 8),
                                    (byte) ((mLongtitude & 0xff) >> 0)
                            };
                            sendDataToCanbox(send, send.length);
                            break;
                        case 9:
                            send = new byte[52];
                            Util.bytes2HexString(send);
                            send[0] = (byte) 0x90;
                            send[1] = (byte) GPS_SHOW_NUM * 4 + 2;
                            send[2] = (byte) 49;
                            send[3] = (byte) mInUse;
                            for (int i = 0; i < GPS_SHOW_NUM; ++i) {
                                if (mGpsSatellite[i] != null) {
                                    int snr = ((int) mGpsSatellite[i].getSnr());
                                    send[i * 4 + 4] = (byte) (snr & 0xff000000 >> 24);
                                    send[i * 4 + 5] = (byte) (snr & 0xff0000 >> 16);
                                    send[i * 4 + 6] = (byte) (snr & 0xff00 >> 8);
                                    send[i * 4 + 7] = (byte) (snr & 0xff >> 0);
                                }
                            }
                            sendDataToCanbox(send, send.length);
                            break;
                        case 0xa:
                            send = new byte[mSnLen + 3];
                            send[0] = (byte) 0x90;
                            send[1] = (byte) mSnLen;
                            send[2] = (byte) 0xa;
                            Util.byteArrayCopy(send, mSN, 3, 0, mSnLen);
                            sendDataToCanbox(send, send.length);
                            break;
                        case 0xc:
                            sendToCan(0x90, 0xc, mSWCMode1, mSWCMode2);
                            break;
                        case 0xd:
                            sendToCan(0x90, 0xd, mPannelMode);
                            break;
                        case 0xe:
                            byte stat = 0;
                            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                                stat = 1;
                            }
                            sendToCan(0x90, 0xe, stat);
                            break;
                        case 0x10:
                            sendOSVersion();
                            break;
                    }
                    break;
                default:
                    if (data[0] == 0x20) {
                        if (data[2] == 8) {
                            mKeyBack = 1;
                        } else {
                            mKeyBack = 0;
                        }
                    }
                    super.parseCanboxData(data, len);
                    break;
            }
        }
    }

    private boolean isWifiConnected() {

        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetworkInfo = connectivityManager

                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        Log.d(TAG, "isWifiConnected:" + wifiNetworkInfo.isConnected());
        return wifiNetworkInfo.isConnected();

    }

    private byte mSWCMode1 = -1;
    private byte mSWCMode2 = -1;
    private byte mPannelMode = -1;
    private byte mKeyBack = 0;

    private void reset() {

        Util.sudoExec("rm:-r:/data/");

        mHandlerTest.removeMessages(MSG_CHECK_RM_DATA);
        mHandlerTest.sendEmptyMessageDelayed(MSG_CHECK_RM_DATA, 2000);

        Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.EXTRA_COMMON_CMD);
        mContext.sendBroadcast(it);// for some app
        // reset itself
    }

    private int mSnLen = 0;
    private final byte[] mSN = new byte[64];

    private String mBTName;
    private String mBTMac;

    private final static int RETURN_BT_NAME = 1;
    private final static int RETURN_BT_MAC = 2;
    private int mReturnTAG = 0;

    public void sendBTName(String s, String mac) {
        if (s == null) {
            s = "unknow";
        }
        if (mac == null) {
            mac = "unknow";
        }
        mBTName = s;
        mBTMac = mac;

        if ((mReturnTAG & RETURN_BT_NAME) != 0) {
            byte[] buf = s.getBytes();
            byte[] send = new byte[buf.length + 3];
            send[0] = (byte) 0x90;
            send[1] = (byte) (buf.length + 1);
            send[2] = (byte) 0x5;

            Util.byteArrayCopy(send, buf, 3, 0, buf.length);

            sendDataToCanbox(send, send.length);

            mReturnTAG &= ~RETURN_BT_NAME;
        }

        if ((mReturnTAG & RETURN_BT_MAC) != 0) {
            byte[] buf = mac.getBytes();
            byte[] send = new byte[buf.length + 3];
            send[0] = (byte) 0x90;
            send[1] = (byte) (buf.length + 1);
            send[2] = (byte) 0xb;

            Util.byteArrayCopy(send, buf, 3, 0, buf.length);

            sendDataToCanbox(send, send.length);

            mReturnTAG &= ~RETURN_BT_MAC;
        }
    }

    private void sendToCan(int d0, int d1) {
        byte[] send = new byte[]{(byte) d0, 0x1, (byte) d1};
        sendDataToCanbox(send, send.length);
    }

    private void sendToCan(int d0, int d1, int d2) {
        byte[] send = new byte[]{(byte) d0, 0x2, (byte) d1, (byte) d2};
        sendDataToCanbox(send, send.length);
    }

    private void sendToCan(int d0, int d1, int d2, int d3) {
        byte[] send = new byte[]{
                (byte) d0, 0x3, (byte) d1, (byte) d2, (byte) d3
        };
        sendDataToCanbox(send, send.length);
    }

    private Mcu mMcu;

    private int sendMcuData(byte[] buf) {
        if (mMcu == null) {
            mMcu = Mcu.getInstance();
        }
        return mMcu.sendCmd(buf);
    }

    private int mMusicPlayCur;
    private int mMusicTime;

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        mMusicTime = time;
        mMusicPlayCur = play + 1;
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }

    private int mPhoneStatus = 0;

    public void setPhone(int status, String num) {
        mPhoneStatus = status;
        sendToCan(0x90, 0x6, (byte) mPhoneStatus);
    }


    private LocationManager mLocationManager = null;
    private MyLocationListener mLocationListener = null;

    private void initGpsCompass() {

        try {
            if (mLocationManager == null) {
                mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            }
            Log.d(TAG, "initGpsCompass:" + mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));

            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (mLocationListener != null) {
                    mLocationManager.removeUpdates(mLocationListener);
                }
                mLocationListener = new MyLocationListener();
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            }
        } catch (Exception e) {
            Log.d("kkcd", String.valueOf(e));
        }

        // mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SAVE_TIME, 1);
        // doUpdateGpsTime();
    }

    private int mLatitude;
    private int mLongtitude;
    private byte mGpsPos = 0;

    public class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            mLatitude = (int) location.getLatitude();
            mLongtitude = (int) location.getLongitude();

            if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                mGpsPos = 0;
            } else {
                mGpsPos = 1;
            }

        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private int mInUse = 0;
    final static int GPS_SHOW_NUM = 12;
    GpsSatellite[] mGpsSatellite = new GpsSatellite[GPS_SHOW_NUM];
    GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {

            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.e(TAG, "GPS_EVENT_FIRST_FIX");
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    // 获取当前状态
                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    // 获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    // 创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    for (int i = 0; i < GPS_SHOW_NUM; ++i) {
                        mGpsSatellite[i] = null;
                    }

                    while (iters.hasNext() && count <= maxSatellites) { // get in
                        // used
                        // number
                        GpsSatellite s = iters.next();
                        if (s.usedInFix()) {
                            if (count >= GPS_SHOW_NUM) break;
                            mGpsSatellite[count] = s;

                            // Log.e(TAG, "1count:" + count + ":" + s.getSnr() + ":"
                            // + s.getPrn());
                            count++;
                        }
                    }
                    mInUse = count;
                    break;
            }
        }
    };
    //wifi

    private WifiManager mWifiManager;

    private void statWifi(boolean start) {
        try {
            Log.d(TAG, "statWifi !!" + start);
            mWifiManager.setWifiEnabled(start);

        } catch (Exception e) {
            Log.d(TAG, "statWifi err" + e);
        }
    }

    private static final String SYSTEM_VERSION_FILE = "/system/ak47_update_hold.txt";

    private void sendOSVersion() {
        try {
            String s = readLine(SYSTEM_VERSION_FILE);
            if (s == null) {
                s = " ";
            }
            byte[] buf = s.getBytes();
            byte[] send = new byte[buf.length + 3];
            send[0] = (byte) 0x90;
            send[1] = (byte) (buf.length + 1);
            send[2] = (byte) 0x10;

            Util.byteArrayCopy(send, buf, 3, 0, buf.length);

            sendDataToCanbox(send, send.length);
        } catch (Exception e) {

        }
    }

    private String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    public WifiConfiguration createWifiConfig(String ssid, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";


        config.preSharedKey = "\"" + password + "\"";
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;


        return config;

    }

    public boolean addNetWork(WifiConfiguration config) {
        WifiInfo wifiinfo = mWifiManager.getConnectionInfo();

        Log.d(TAG, "addNetWork result:" + wifiinfo);
        if (null != wifiinfo) {
            mWifiManager.disableNetwork(wifiinfo.getNetworkId());
        }

        boolean result;

        Log.d(TAG, "addNetWork result:" + config.networkId);

        if (config.networkId > 0) {
            result = mWifiManager.enableNetwork(config.networkId, true);
            mWifiManager.updateNetwork(config);
        } else {

            int i = mWifiManager.addNetwork(config);
            result = false;

            Log.d(TAG, "addNetWork result:" + i);

            if (i > 0) {

                mWifiManager.saveConfiguration();
                mWifiManager.enableNetwork(i, true);

                result = true;
            }
        }
        Log.d(TAG, " result:" + result);
        return result;

    }

    public static WifiConfiguration createWifiInfo(String ssid, String password) {

        WifiConfiguration config = new WifiConfiguration();

        config.allowedAuthAlgorithms.clear();

        config.allowedGroupCiphers.clear();

        config.allowedKeyManagement.clear();

        config.allowedPairwiseCiphers.clear();

        config.allowedProtocols.clear();

        config.SSID = "\"" + ssid + "\"";

        //    	if(TextUtils.isEmpty(password)) {
        //
        //    	config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        //
        //    	Log.i(TAG, "password is ''");
        //
        //    	return config;
        //
        //    	}

        config.preSharedKey = "\"" + password + "\"";

        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);

        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        config.status = WifiConfiguration.Status.ENABLED;

        return config;

    }

	/*ActionListener mActionListener = new ActionListener() {
		public void onSuccess() {
			Log.d(TAG, "connectWifi onSuccess:");
		};

		public void onFailure(int arg0) {
			Log.d(TAG, "connectWifi onFailure:" + arg0);
		};
	};*/

    private void connectWifi(String name, String psw) {
        statWifi(true);
        Log.d("ddck", "connectWifi:" + name + ":" + psw);
        //WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        //wifiManager.connect(createWifiInfo(name, psw), mActionListener);
    }

    private int mNetStatus = 0;

    public boolean connectionTest(String urlAddress) {

        Log.i(TAG, "connectionTest " + urlAddress);
        mNetStatus = 0;
        isNetWorkAvailableOfDNS(urlAddress, new Comparable<Boolean>() {

            @Override
            public int compareTo(Boolean available) {
                if (available) {
                    mNetStatus = 1;
                    // TODO 设备访问Internet正常
                } else {
                    // TODO 设备无法访问Internet
                }
                return 0;
            }

        });

        return true;

        //	        String logPrefix = "connectionTest " + urlAddress;
        //	        mNetStatus = 0;
        //	        try {
        //	            URL url = new URL(urlAddress);
        //	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //	            try {
        //	                connection.setRequestMethod("HEAD");
        //	                connection.setConnectTimeout(500);
        //	                connection.setReadTimeout(500);
        //	                int code = connection.getResponseCode();
        //	                Log.d(TAG, logPrefix + " success! " + code);
        //	                mNetStatus = 1;
        //	                return true;
        //	            } catch (Exception e) {
        //		            Log.w(TAG, "connection fail " + e.toString());
        //
        //		        } finally {
        //	                connection.disconnect();
        //	            }
        //	            return false;
        //	        } catch (MalformedURLException e) {
        //	            Log.w(TAG, "urlAddress " + urlAddress + " is a malformed URL!");
        //	            return false;
        //	        } catch (UnknownHostException e) {
        //	            Log.w(TAG, logPrefix + " UnknownHostException");
        //	            return false;
        //	        } catch (IOException e) {
        //	            Log.w(TAG, logPrefix + " exception", e);
        //	            return false;
        //	        }
    }


    /**
     * 检查互联网地址是否可以访问-使用DNS解析
     *
     * @param hostname 要检查的域名或IP
     * @param callback 检查结果回调（是否可以解析成功）{@see java.lang.Comparable<T>}
     */
    public static void isNetWorkAvailableOfDNS(final String hostname, final Comparable<Boolean> callback) {
        final Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (callback != null) {
                    callback.compareTo(msg.arg1 == 0);
                }
            }

        };
        new Thread(new Runnable() {

            @Override
            public void run() {
                Message msg = new Message();
                try {
                    DNSParse parse = new DNSParse(hostname);
                    Thread thread = new Thread(parse);
                    thread.start();
                    thread.join(3 * 1000); // 设置等待DNS解析线程响应时间为3秒
                    InetAddress resCode = parse.get(); // 获取解析到的IP地址
                    msg.arg1 = resCode == null ? -1 : 0;
                } catch (Exception e) {
                    msg.arg1 = -1;
                    e.printStackTrace();
                } finally {
                    handler.sendMessage(msg);
                }
            }

        }).start();
    }

    /**
     * DNS解析线程
     */
    private static class DNSParse implements Runnable {
        private final String hostname;
        private InetAddress address;

        public DNSParse(String hostname) {
            this.hostname = hostname;
        }

        public void run() {
            try {
                set(InetAddress.getByName(hostname));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public synchronized void set(InetAddress address) {
            this.address = address;
        }

        public synchronized InetAddress get() {
            return address;
        }
    }


    private final static String GPS_SOUND1 = "/system/media/audio/ringtones/ANDROMEDA.ogg";
    private final static String GPS_SOUND2 = "/product/media/audio/ringtones/ANDROMEDA.ogg";

    private MediaPlayer mMediaPlayer;

    private void testGPSSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }

        SystemProperties.set("ak.af.navi.uid", "1000");

        // Uri.fromFile(path));
        try {
            String file = GPS_SOUND1;
            File f = new File(GPS_SOUND1);
            if (!f.exists()) {
                file = GPS_SOUND2;
            }
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(file);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            Log.d("ccef", ":" + e);
        }
    }

    private void stopGPSSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    private int getSource() {
        String top = AppConfig.getTopActivity();
        Log.d(TAG, "getSource()" + top);
        int ret = 0;
        if (top != null) {
            String[] ss = top.split("/");
            if ("com.car.ui/com.zhuchao.android.car.radio.RadioActivity".equals(top)) {
                ret = 1;
            } else if ("com.car.ui/com.zhuchao.android.car.audio.MusicActivity".equals(top)) {
                ret = 2;
            } else if ("com.car.ui/com.zhuchao.android.car.dvd.DVDPlayer".equals(top)) {
                ret = 0xb;
            } else if ("com.car.ui/com.zhuchao.android.car.tv.TVActivity".equals(top)) {
                ret = 1;
            } else if ("com.car.ui/com.zhuchao.android.car.video.VideoActivity".equals(top)) {
                ret = 3;
            } else if ("com.car.ui/com.zhuchao.android.car.auxplayer.AUXPlayer".equals(top)) {
                ret = 5;
            } else if ("com.car.ui/com.zhuchao.android.car.frontcamera.FrontCameraActivity".equals(top)) {
                ret = 7;
            } else if ("com.car.ui/com.zhuchao.android.car.frontcamera.BackCameraActivity".equals(top)) {
                ret = 8;
            } else if ("com.zhuchao.android.car.dvr/com.zhuchao.android.car.dvr.MainActivity".equals(top)) {
                ret = 0xd;
            } else if ("com.zhuchao.android.car.bt/com.zhuchao.android.car.bt.ATBluetoothActivity".equals(top)) {
                ret = 4;
            } else if (top.contains("net.easyconn")) {
                ret = 0xc;
            } else if ("com.android.settings/com.android.settings.Settings".equals(top)) {
                ret = 0xa;
            } else if (AppConfig.isGpsApp(mContext, ss[0])) {
                ret = 6;
            }
        }
        return ret;
    }

    private void setTestGPS() {

        String s = null;//MachineConfig.getPropertyReadOnly(MachineConfig.KEY_DEFAULT_GPS);
        if (s == null) {
            s = "com.google.android.apps.maps/com.google.android.maps.MapsActivity";
        }

        String[] ss = s.split("/");
        if (ss.length > 1) {
            SystemConfig.setProperty(mContext, MachineConfig.KEY_GPS_PACKAGE, ss[0]);
            SystemConfig.setProperty(mContext, MachineConfig.KEY_GPS_CLASS, ss[1]);
        }

    }
}
