package com.zhuchao.android.car.service;

import static android.provider.Settings.Secure.TTS_DEFAULT_SYNTH;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.SystemProperties;
import com.common.util.Util;
import com.common.util.UtilSystem;
import com.common.util.UtilSystem.StorageInfo;

import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.autotest.AutoTest;
import com.zhuchao.android.car.canbox.AirManager;
import com.zhuchao.android.car.canbox.CanService;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.DoorStatusPanel;
import com.zhuchao.android.car.canbox.OBDView;
import com.zhuchao.android.car.canbox.ReverseManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.debug.DebugMessage;
import com.zhuchao.android.car.manager.AutoIlluminManager;
import com.zhuchao.android.car.manager.McuManager;
import com.zhuchao.android.car.manager.OSProManager;
import com.zhuchao.android.car.manager.key.JoyKey;
import com.zhuchao.android.car.ui.BacklightPanel;
import com.zhuchao.android.car.ui.PreInstallPanel;
import com.zhuchao.android.car.ui.VolumePanel;
import com.zhuchao.android.car.view.RecentView;
import com.zhuchao.android.fbase.MMLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MyCarService extends Service {

    public static final String TAG = "MyCarService";
    private LocationManager mLocationManager = null;
    private MyLocationListener mLocationListener = null;
    private static final int MSG_DELETE_UPDATE_FILE = 1;

    private static final int MSG_INSTALL_PRE_APP = 2;
    private static final int MSG_UPDATE_TOUCH_CONFIG = 3;
    private static final int MSG_UPDATE_SAVE_TIME = 8;
    private static final int MSG_SAVE_TIME = 9;
    private static final int MSG_WATCH_DOG_ = 10; // mcu
    private static final int MSG_WATCH_DOG_TIME = 1000;
    private static final int MSG_WATCH_DOG_SYSTEM = 11;// system
    private static final int MSG_GPS_TIME = 12;
    private static final int MSG_FIX_GPU_BUG = 13;
    private static final int MSG_UPDATE_SETTINGS = 15;
    private static final int MSG_SET_IOSCHED_CFQ = 14;
    private static final int MSG_INIT_OTHER = 18;
    private static final int MSG_SET_LOWMEMORY_KILLER = 19;
    private static final int MSG_CP_NO_SIGNAL = 20;
    private static final int MSG_REPEAT_INIT_SUDING_ILL = 21;
    private static final int MSG_REPEAT_GPS_SETTINGS = 16;
    private static final String UPDATE_FILE = "ak48_update_guide.txt";
    private static final String UPDATE_FILE_HOLDER = "ak47_update_hold.txt";
    int mDelSdUpdate = 2;

    public static MyCarService mThis;
    private final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REPEAT_INIT_SUDING_ILL:
                    MMLog.d(TAG, "MSG_REPEAT_INIT_SUDING_ILL:" + msg.arg1);
                    if (msg.arg1 > 0) {
                        mMcuManager.queryIll();
                        msg.arg1--;
                        if (msg.arg1 > 0 && msg.arg1 < 10) {
                            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_REPEAT_INIT_SUDING_ILL, msg.arg1, 0), 1000);
                        }
                    }
                    break;
                case MSG_DELETE_UPDATE_FILE:
                    deleteINANDUpdateFile();
                    break;
                case MSG_UPDATE_TOUCH_CONFIG:
                    // updateTouchScreenConfig();
                    break;
                case MSG_UPDATE_SAVE_TIME:
                    updateSaveTime();
                    break;
                case MSG_SAVE_TIME:
                    saveTime();
                    break;
                case MSG_WATCH_DOG_:
                    if (mMcuManager != null) {
                        if (!mIsTestMemory) {
                            mMcuManager.setSystemReadyWatchDog();
                        } else {
                            mMcuManager.clearSystemReadyWatchDog();
                        }
                    }
                    // Log.d("allen", "MSG_WATCH_DOG_");
                    mHandler.sendEmptyMessageDelayed(MSG_WATCH_DOG_, MSG_WATCH_DOG_TIME);
                    break;
                case MSG_INSTALL_PRE_APP:
                    installPreInstallApp();
                    break;
                case MSG_WATCH_DOG_SYSTEM:
                    break;
                case MSG_GPS_TIME:
                    initGpsTime();
                    break;
                case MSG_FIX_GPU_BUG:
                    doHideFixGpu();
                    break;
                case MSG_SET_LOWMEMORY_KILLER:
                    setLowMemoryKiller();
                    break;
                case MSG_UPDATE_SETTINGS:

                    initSettings();
                    break;
                case MSG_REPEAT_GPS_SETTINGS:
                    updatePackageList();
                    break;
                case MSG_SET_IOSCHED_CFQ:
                    MMLog.d(TAG, "set /sys/block/mmcblk0/queue/scheduler cfq!");
                    Util.setFileValue("/sys/block/mmcblk0/queue/scheduler", "cfq");
                    break;
                case MSG_INIT_OTHER:
                    initOther();
                    break;
                case MSG_CP_NO_SIGNAL:
                    cpNoSignal();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate() {
        GlobalDefinition.mSystemBootStartTime = SystemClock.uptimeMillis();
        super.onCreate();
        MMLog.d(TAG, TAG + " onCreate()");
        updateTimeForAndroidP();
        mThis = this;

        printfSystemReleaseVersion();//
        GlobalDefinition.init(this);
        /// initSettings();
        /// Log.d(TAG, ">>>car service onCreate");
        /// updateSaveTime();
        /// initBT();
        new BacklightPanel(this);
        initMcu();

        initBTType();
        /// initGpsTime();
        /// initMediaRouter();
        initVolume();

        registerEventReceiver();
        ///registerMountListener(); // move to filemanager
        initUIService();

        mHandler.sendEmptyMessageDelayed(MSG_INIT_OTHER, 2000);
        mHandler.sendEmptyMessageDelayed(MSG_GPS_TIME, 2000);
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SETTINGS, 2000);
        if (Util.isRKSystem()) {
            mHandler.sendEmptyMessageDelayed(MSG_WATCH_DOG_, MSG_WATCH_DOG_TIME);
            /// mHandler.sendEmptyMessageDelayed(MSG_SET_LOWMEMORY_KILLER, 15000);
        }

        if (Util.isRKSystem()) {
            mHandler.sendEmptyMessageDelayed(MSG_CP_NO_SIGNAL, 15000);
        }

        mHandler.sendEmptyMessageDelayed(MSG_WATCH_DOG_SYSTEM, 5000);
        if (Util.isNexellSystem60()) {
            mHandler.sendEmptyMessageDelayed(MSG_SET_IOSCHED_CFQ, 15000);
        }
        ///mHandler.sendEmptyMessageDelayed(MSG_UPDATE_RADIO, 3000);
        ///doAutoTest(); //test
        ///DebugMessage.start(this);
        ///doUpdateCanbox();
        MMLog.d(TAG, TAG + " onCreate!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        MMLog.d(TAG, TAG + " onDestroy()");
        unregisterListener();
        //unregisterMountListener();
        super.onDestroy();
    }

    private PreInstallPanel mPreInstallPanel;

    private void installPreInstallApp() {
        if (mPreInstallPanel == null) {
            mPreInstallPanel = new PreInstallPanel(mThis);
            //			mPreInstallPanel.show();
            if (mPreInstallPanel.installPreInstallApp()) {
                mPreInstallPanel.show();
            }
        }
    }

    private void delFile(String s) {
        File f = new File(s);
        if (f.exists()) {
            f.delete();
        }
    }

    private void deleteINANDUpdateFile() {
        // Log.e(TAG, "deleteINANDUpdateFile:" + mDelSdUpdate);
        if (mDelSdUpdate <= 0) {
            return;
        }

        if (Util.isNexellSystem60()) {
            deleteINANDUpdateFile(MyCmd.PATH_SDCARD1);
            deleteINANDUpdateFile(MyCmd.PATH_SDCARD2);
        }

        if (Util.isNexellSystem()) {
            delFile(MyCmd.PATH_SDCARD_ + UPDATE_FILE);
            mDelSdUpdate--;
            mHandler.sendEmptyMessageDelayed(MSG_DELETE_UPDATE_FILE, 3000);
        }
    }

    private void deleteINANDUpdateFile(String path) {

        File sdcardDir = new File(path);
        if (sdcardDir.exists()) {
            mDelSdUpdate = 0;

            File f = new File(path + UPDATE_FILE_HOLDER);
            if (!f.exists()) {
                delFile(path + UPDATE_FILE);
            }
        }
    }

    private static final String OTG = "/sys/class/ak/source/otg_id";
    private static final String OTG_60 = "/sys/devices/platform/dwc_otg/otg_mode";
    private static final String SYSTEM_VERSION = "/system/";

    private boolean isFirstBoot() {
        boolean ret = false;
        String SAVE_FIRST_BOOT = "first_boot";
        if (getData(SAVE_FIRST_BOOT) != 1) {
            ret = true;
            saveData(SAVE_FIRST_BOOT, 1);
            Util.sudoExecNoCheck("sync");
        }
        return ret;
    }

    private static final String HOURS_12 = "12";
    private static final String HOURS_24 = "24";

    private void set24Hour(boolean is24Hour) {
        Settings.System.putString(getContentResolver(), Settings.System.TIME_12_24, is24Hour ? HOURS_24 : HOURS_12);
    }

    private void updateTimeForAndroidP() {
        if (Build.VERSION.SDK_INT <= 28) { // Build.VERSION.SDK.contains(Build.P)

            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            if (year < 2021) {
                c.set(2021, 1, 1, 12, 00, 00);
                SystemClock.setCurrentTimeMillis(c.getTimeInMillis());
            }
        }
    }

    private void initParamterMachineConfig() {
        String value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_TIMEZONE);
        MMLog.d(TAG, "initParamterMachineConfig:" + value);
        if (value != null) {
            final AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            try {
                alarm.setTimeZone(value);
            } catch (Exception ignored) {
            }
        }

        int hour12 = MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_TIME12);
        if (hour12 == 1) {
            set24Hour(false);
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_LANGUAGE);

        // Log.d(TAG, "initParamterMachineConfig:"+value);
        if (value != null) {
            String[] ss = value.split(":");
            if (ss.length > 1) {
                Locale l = new Locale(ss[0], ss[1]);
                //try {
                //    LocalePicker.updateLocale(l);
                //} catch (Exception ignored) {
                //}
            }
        }

        // Log.d(TAG, "initParamterMachineConfig:"+mDefaultKeyboard);
        value = MachineConfig.getPropertyReadOnly(SystemConfig.REVERSE_STATIC_TRACK);
        if (MachineConfig.VALUE_ON.equals(value)) {
            Settings.Global.putInt(getContentResolver(), SystemConfig.REVERSE_STATIC_TRACK, 1);
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_USB_DVD);
        if (value != null) {
            MachineConfig.setProperty(MachineConfig.KEY_USB_DVD, value);
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_CAN_BOX_PG_SWITCH);
        if (value != null) {
            MachineConfig.setProperty(MachineConfig.KEY_CAN_BOX_PG_SWITCH, value);
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_APP_HIDE);
        if (value != null) {
            MachineConfig.setProperty(MachineConfig.KEY_APP_HIDE, value);

            value = MachineConfig.getProperty(MachineConfig.KEY_USB_DVD);
            if ("1".equals(value)) {
                AppConfig.updateHideAppConfig();
                if (!AppConfig.isHidePackage("com.android.car.dvd.DVDPlayer")) {
                    MachineConfig.setProperty(MachineConfig.KEY_USB_DVD, "0");
                }
            }

            Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_APP_HIDE);
            sendBroadcast(it);
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_EXTERNAL_BOX);
        if (value != null) {
            MachineConfig.setProperty(MachineConfig.KEY_EXTERNAL_BOX, value);
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_CAN_BOX_SHOW_APP);
        if (value != null) {
            MachineConfig.setProperty(MachineConfig.KEY_CAN_BOX_SHOW_APP, value);
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_CAN_BOX);
        MMLog.d(TAG, "get KEY_CAN_BOX=" + value);
        if (value != null) {
            MachineConfig.setProperty(MachineConfig.KEY_CAN_BOX, value);
            MMLog.d(TAG, "set KEY_CAN_BOX=" + MachineConfig.getProperty(MachineConfig.KEY_CAN_BOX));
            CanService.updateCanboxEx();
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_RUDDER);

        if (value != null) {
            MachineConfig.setProperty(MachineConfig.KEY_RUDDER, value);
            GlobalDefinition.mRudder = MachineConfig.VALUE_ON.equals(value);
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_VIDEO_ON_DRIVING);
        if (value != null) {
            Util.setFileValue("/sys/class/ak/source/reaksw", value);
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.MCU_ILLUM_ACC_NODE);
        MMLog.d(TAG, "MCU_ILLUM_ACC_NODE:" + value);
        if (value != null) {
            Util.setFileValue("/sys/class/ak/source/accillumin", value);
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_MCU_BEEP);
        if (value != null) {
            Util.setFileValue("/sys/class/ak/source/beep", value);
        }

        value = MachineConfig.getPropertyReadOnly("SET_VCOM");

        if (value != null) {
            //Util.setFileValue("/sys/class/ak/source/beep", value);
            int data = -1;
            try {
                data = Integer.parseInt(value);
            } catch (Exception ignored) {
            }
            if (data != -1) {
                mMcuManager.setVCOM(((data & 0xff00) >> 8), (data & 0xff));
            }
        }

        int night = MachineConfig.getPropertyIntReadOnly(AutoIlluminManager.NIGHT_BRIGHTNESS);
        MMLog.d(TAG, "set night:" + night);
        if (night != 0) {
            SystemConfig.setIntProperty(this, AutoIlluminManager.NIGHT_BRIGHTNESS, night);
        }

        // Log.d(TAG, "initParamterMachineConfig:"+mDefaultKeyboard);

        // GlobalDef.mTouchKeyType = MachineConfig
        // .getPropertyIntReadOnly(MachineConfig.KEY_TOUCH_KEY_TYPE);
        // Log.d(TAG, "GlobalDef.mTouchKeyType:"+GlobalDef.mTouchKeyType);
        for (String key : MACHINE_CONFIG_DEFAULT) {
            value = MachineConfig.getPropertyReadOnly(key);
            if (value != null) {
                MachineConfig.setProperty(key, value);
            }
        }

        value = MachineConfig.getPropertyReadOnly(SystemConfig.AUTO_PLAY_MUSIC_DEVICES_MOUNTED);
        //Log.d(TAG,"initParamterMachineConfig SystemConfig.AUTO_PLAY_MUSIC_DEVICES_MOUNTED:" + value);
        if (value != null) {
            Settings.Global.putInt(getContentResolver(), SystemConfig.AUTO_PLAY_MUSIC_DEVICES_MOUNTED, 0);
        }

        // ww+, for ScreenSaver
        try {
            if ((MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDefinition.mSystemUI) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDefinition.mSystemUI) || MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(GlobalDefinition.mSystemUI))) {
                Settings.Global.putInt(getContentResolver(), Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
                MMLog.d(TAG, "ScreenSaver SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE");
            } else {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
                MMLog.d(TAG, "ScreenSaver SCREEN_OFF_TIMEOUT");
            }
        } catch (Exception e) {
            MMLog.e(TAG, e.toString());
        }

        value = MachineConfig.getPropertyReadOnly(SystemConfig.KEY_REVERSE_VOLUME);
        if (value != null) {
            try {
                int mix = Integer.parseInt(value);
                SystemConfig.setIntProperty(this, SystemConfig.KEY_REVERSE_VOLUME, mix);
            } catch (Exception ignored) {
            }
        }

        value = MachineConfig.getPropertyReadOnly(SystemConfig.KEY_NAVI_MIX_SOUND);
        if (value != null) {
            try {
                int mix = Integer.parseInt(value);
                SystemConfig.setIntProperty(this, SystemConfig.KEY_NAVI_MIX_SOUND, mix);
                Util.setFileValue(MCU_NAVI_MIX_NODE, mix);
            } catch (Exception ignored) {
            }
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_RADIO_REGION);

        if (value != null) {
            Util.setFileValue("/sys/class/ak/source/radioregion", value);
        }

        int data;
        data = MachineConfig.getPropertyIntReadOnly(SystemConfig.CANBOX_DOOR_VOICE);
        if (data != 0) {
            SystemConfig.setIntProperty(this, SystemConfig.CANBOX_DOOR_VOICE, data);
        }
        data = MachineConfig.getPropertyIntReadOnly(SystemConfig.CANBOX_FRONT_RADAR_OPEN_CAMERA);
        if (data != 0) {
            SystemConfig.setIntProperty(this, SystemConfig.CANBOX_FRONT_RADAR_OPEN_CAMERA, data);
        }
    }

    private void initDefKeyConfig() {
        String s = MachineConfig.getProperty(MachineConfig.KEY_PANEL_KEY_DEF_CONFIG);
        Log.d(TAG, "initDefKeyConfig:" + s);
        if (s != null) {
            try {
                int value = Integer.parseInt(s);
                GlobalDefinition.mPannelKeyType = (value & 0xff00) >> 8;
                Util.setFileValue("/sys/class/ak/source/panel_key_defcfg", value);
            } catch (Exception ignored) {
            }
        }

        s = MachineConfig.getProperty(MachineConfig.KEY_SWC_KEY_DEF_CONFIG);
        Log.d(TAG, "initDefKeyConfig: KEY_SWC_KEY_DEF_CONFIG:" + s);
        if (s != null) {
            try {
                int value = Integer.parseInt(s);
                Util.setFileValue("/sys/class/ak/source/panel_key_defcfg", value);
            } catch (Exception ignored) {
            }
        }
    }


    private static final String MCU_REVERSE_VOLUME = "/sys/class/ak/source/reverse_volume";
    private static final String MCU_NAVI_MIX_NODE = "/sys/class/ak/source/navi_mix";
    private final static String[] MACHINE_CONFIG_DEFAULT = {
            MachineConfig.KEY_LED_TYPE, MachineConfig.KEY_PANEL_KEY_DEF_CONFIG, MachineConfig.KEY_SWC_KEY_DEF_CONFIG, MachineConfig.KEY_FACTORY_AUDIO_GAIN, MachineConfig.KEY_TPMS_TYPE,
            MachineConfig.KEY_RDS, MachineConfig.KEY_TOUCH3_IDENTIFY
    };

    private void initMcuBootSetting() {
        int index = SystemConfig.getIntProperty2(this, SystemConfig.KEY_REVERSE_VOLUME);

        Log.d(TAG, "initMcuBootSetting KEY_REVERSE_VOLUME.:" + index);
        if (index != -1) {
            Util.setFileValue(MCU_REVERSE_VOLUME, index);
        }

        loadGain(false);
        loadGain(true);

        String s = MachineConfig.getProperty(MachineConfig.KEY_BT_TYPE);
        if (s == null) {
            s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_BT_TYPE);
        }
    }

    public void loadGain(boolean mIsFactory) {
        String value = null;
        if (mIsFactory) value = MachineConfig.getProperty(MachineConfig.KEY_FACTORY_AUDIO_GAIN);
        else value = MachineConfig.getProperty(MachineConfig.KEY_USER_AUDIO_GAIN);

        try {
            if (value != null) {
                int gain_host;
                int gain_radio;
                int gain_dvd;
                int gain_bt;
                int gain_auxin;
                int gain_tv;

                String[] item = value.split(",");
                gain_host = Integer.parseInt(item[0]);
                gain_radio = Integer.parseInt(item[1]);
                gain_dvd = Integer.parseInt(item[2]);
                gain_bt = Integer.parseInt(item[3]);
                gain_auxin = Integer.parseInt(item[4]);
                gain_tv = Integer.parseInt(item[5]);
                byte[] data = new byte[]{(byte) (mIsFactory ? 0x1 : 0x2), (byte) gain_host, (byte) gain_radio, (byte) gain_dvd, (byte) gain_bt, (byte) gain_auxin, (byte) gain_tv};
                doAudioGain(data);
            }
        } catch (Exception ignored) {
        }
    }

    private final static String MIC_CTL = "/sys/class/ak/source/mic_ctrl";

    private void setMicType() {
        int value = SystemConfig.getIntProperty2(this, SystemConfig.KEY_MIC_TYPE);
        if (value == 0 || value == 1) {
            Util.setFileValue(MIC_CTL, value);
        }
        MMLog.d(TAG, "setMicType " + MIC_CTL + " " + value);
    }

    private void initOther() {
        mHandler.sendEmptyMessageDelayed(MSG_DELETE_UPDATE_FILE, 4000);
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TOUCH_CONFIG, 6000);
        /// mHandler.sendEmptyMessageDelayed(MSG_START_DVR, 10000);
        /// Log.e(TAG, "initOther isFisrtBoot:" + isFisrtBoot());

        String value;
        if (isFirstBoot()) {
            MMLog.i(TAG, "initOther isFisrtBoot: true");
            mHandler.sendEmptyMessageDelayed(MSG_INSTALL_PRE_APP, 2800);
            initParamterMachineConfig();
        } else {
            value = MachineConfig.getProperty(MachineConfig.KEY_RUDDER);
            if (value != null) {
                GlobalDefinition.mRudder = MachineConfig.VALUE_ON.equals(value);
            }
        }

        value = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SUDING_MIC_GAIN);
        if (value != null) {
            Util.setProperty(MachineConfig.KEY_SUDING_MIC_GAIN, value);
        }

        AppConfig.updateHideAppConfig();
        initMcuBootSetting();

        updateUSBDvdConfig(false);
        GlobalDefinition.makeSureDVDExist(false);

        if (MachineConfig.getProperty(MachineConfig.KEY_OTG_TEST) != null) {
            if (!Util.isRKSystem()) {
                if (Build.VERSION.SDK_INT > 23) {
                    Util.setFileValue(OTG, 1);
                } else {
                    Util.setFileValue(OTG_60, 1);
                }
            } else {
                if (Build.VERSION.SDK_INT >= 29) {
                    Util.setFileValue("/sys/class/ak/source/otg_id", "peripheral");
                } else {
                    if (Util.isPX6()) {
                        if (Util.isAndroidP()) {
                            Util.setFileValue("/sys/devices/platform/usb0/dwc3_mode", "peripheral");
                        } else if (Util.isAndroidQ()) {
                            Util.setFileValue("/sys/devices/platform/ff770000.syscon/ff770000.syscon:usb2-phy@e450/otg_mode", "peripheral");
                        }
                    } else if (Util.isPX30()) {
                        Util.setFileValue("/sys/devices/platform/ff2c0000.syscon/ff2c0000.syscon:usb2-phy@100/otg_mode", "peripheral");
                    } else {
                        if (Util.isAndroidQ()) {
                            Util.setFileValue("/sys/devices/platform/ff770000.syscon/ff770000.syscon:usb2-phy@700/otg_mode", "peripheral");
                        } else {
                            Util.sudoExec("chmod:666:/sys/bus/platform/drivers/usb20_otg/force_usb_mode");
                            Util.checkAKDRunning();
                            if (Util.isAndroidP() && Util.isRKSystem()) {
                                Util.setFileValue("/sys/bus/platform/drivers/usb20_otg/force_usb_mode", 1);
                                Util.sudoExec("sleep 0.3");
                                Util.checkAKDRunning();
                                Util.setFileValue("/sys/bus/platform/drivers/usb20_otg/force_usb_mode", 2);
                            } else {
                                Util.setFileValue("/sys/bus/platform/drivers/usb20_otg/force_usb_mode", 2);
                            }
                        }
                    }
                }
            }
            Toast.makeText(this, "To usb otg debug", Toast.LENGTH_LONG).show();
        }

        //update radio ant
        int i = MachineConfig.getPropertyInt(MachineConfig.KEY_RADIO_ANT_POWER);
        mMcuManager.setRadioAntPower(i);

        MMLog.d(TAG, "init late");
        new VolumePanel(this);
        // new BacklightPanel(this);
        mJoyKey = new JoyKey(this);
        getSaveDriveSwitch();
        initDefKeyConfig();
        if (Util.isRKSystem()) {
            mMcuManager.recoverNetStatus();
        }
        mMcuManager.queryMcuRtc();
        initGPSSpeedInfo();
        setMicType();
    }

    private void updateUSBDvdConfig(boolean force) {
        String USBDvd;
        if (force) {
            USBDvd = MachineConfig.getPropertyOnce(MachineConfig.KEY_USB_DVD);
        } else {
            USBDvd = MachineConfig.getProperty(MachineConfig.KEY_USB_DVD);
        }

        if ("1".equals(USBDvd)) {
            GlobalDefinition.mIsUSBDvd = true;
            registerMountListener(); // for usb dvd in
        } else {
            unregisterMountListener();
        }
    }

    private void cpNoSignal() {
		/*if (Util.isRKSystem()) {
			File f = new File("/data/camera/no_signal.jpg");
			if (!f.exists()) {
				Util.sudoExec("cp:/system/etc/no_signal:/data/camera/no_signal.jpg");
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Util.sudoExec("chmod:666:/data/camera/no_signal.jpg");
					}
				}, 500);

			}
		}*/
    }

    private void saveTime() {
        // saveData(SAVE_DATA_TIME, System.currentTimeMillis());
        // mHandler.removeMessages(MSG_SAVE_TIME);
        // mHandler.sendEmptyMessageDelayed(MSG_SAVE_TIME, 60000);
    }

    private void updateSaveTime() {/*
     * long t = getData(SAVE_DATA_TIME);
     * Log.d(TAG, "updateSaveTime:" + t + ":" +
     * System.currentTimeMillis()); if
     * (!mUpdateGpsTime && t != 0 && Math.abs(t
     * - System.currentTimeMillis()) > 5000) {
     * try{ SystemClock.setCurrentTimeMillis(t);
     * }catch(Exception e){
     *
     * } }
     * mHandler.removeMessages(MSG_SAVE_TIME);
     * mHandler
     * .sendEmptyMessageDelayed(MSG_SAVE_TIME,
     * 60000);
     */
    }

    private int mGpsInitTime = 10;

    public void doReinitGpsTime() {
        mGpsInitTime = 10;
        initGpsTime();
    }

    public static void reinitGpsTime() {
        if (mThis != null) {
            mThis.doReinitGpsTime();
        }
    }

    private void initGpsTime() {

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        MMLog.d(TAG, "initGpsTime:" + mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));

        mHandler.removeMessages(MSG_GPS_TIME);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (mLocationListener != null) {
                mLocationManager.removeUpdates(mLocationListener);
            }
            mLocationListener = new MyLocationListener();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        } else {
            mGpsInitTime--;
            if (mGpsInitTime > 0) {
                mHandler.sendEmptyMessageDelayed(MSG_GPS_TIME, 2000);
            }
        }

        // mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SAVE_TIME, 1);
        // doUpdateGpsTime();
    }

    //
    // private void doUpdateGpsTime(){
    // LocationManager mLocationManager = (LocationManager)
    // getSystemService(Context.LOCATION_SERVICE);
    // Location location = mLocationManager.getLastLocation();
    // if (location!=null){
    // long timestamp = location.getTime();
    //
    //
    // Log.i(TAG, "UpdateGpsTime:" + timestamp);
    //
    // if (timestamp > 1451581346350L) {// >20160101----
    // try{
    // SystemClock.setCurrentTimeMillis(timestamp);
    // }catch(Exception e){
    //
    // Log.i(TAG, "fail UpdateGpsTime:" + timestamp);
    // }
    //
    //
    // mLocationManager
    // .removeUpdates((LocationListener) mLocationListener);
    // }
    // }
    // }
    private void setHardWarePower(boolean b) {
        if (b) {
            Util.setFileValue("/sys/class/ak/source/bluetoothsw", 1);
            Util.do_exec("start ivt_blueletd");
        }
    }

    private void initBT() {
        /// setHardWarePower(true);
        /// mHandler.postDelayed(new Runnable() {
        /// @Override
        /// public void run() {
        /// try {
        /// Intent it = new Intent(Intent.ACTION_RUN);
        /// it.setClassName("com.android.car.bt", "com.android.car.bt.ATBluetoothService");
        /// startService(it);
        /// } catch (Exception e) {
        /// }
        /// }
        /// }, 500);
        /// mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SAVE_TIME, 1);
    }

    private McuManager mMcuManager;

    private void initMcu() {
        mMcuManager = McuManager.getInstance(this);
        OSProManager mOsManager = OSProManager.getInstanse(this);
        // mMcuManager.setVoulume(5);// test
        // mMcuManager.setSource(7);// test
        AutoIlluminManager mAutoIlluminManager = AutoIlluminManager.getInstanse(this);
    }

    private void initVolume() {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        /// int a = mAudioManager.getStreamVolume(10);
        /// int b = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 11, 11);
        try {
            mAudioManager.setStreamVolume(10, 11, 11);
        } catch (Exception ignored) {
        }
    }

    /// private static final String THIRD_APP_SOUND_FIRST_PATH =
    /// "/sys/class/ak/source/arm_sound_switch";
    private void updateAccPowerOffDelay(String s) {
        if (s == null) {
            s = SystemConfig.getProperty(this, MachineConfig.KEY_ACC_DELAY_OFF);
            if (s == null) {
                s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_ACC_DELAY_OFF);
                if (s != null) {
                    Log.d(TAG, "updateAccPowerOffDelay:" + s);
                    SystemConfig.setProperty(this, MachineConfig.KEY_ACC_DELAY_OFF, s);
                }
            }
        }

        int timeSystem = -1;
        if (s != null) {
            try {
                timeSystem = Integer.parseInt(s);
            } catch (Exception ignored) {
            }
        }

        //check dvr delytime
        int timeDvr = -1;
        s = SystemConfig.getProperty(this, SystemConfig.KEY_ACC_DELAY_OFF_DVR);
        if (s != null) {
            try {
                timeDvr = Integer.parseInt(s);
            } catch (Exception ignored) {
            }
        }

        int time = -1;
        if (timeSystem > timeDvr) {
            time = timeSystem;
        } else {
            time = timeDvr;
        }

        Log.d(TAG, "updateAccPowerOffDelay:" + time + ":" + timeDvr + ":" + timeSystem);

        if (time >= 0 && time <= 0xffffff) {
            mMcuManager.setAccPowerDelayTime(time);
        }
    }

    private void initSettings() {
        // try {
        // if ("1".equals(MachineConfig
        // .getProperty(MachineConfig.KEY_THIRD_APP_SOUND_FIRST))) {
        // Util.setFileValue(THIRD_APP_SOUND_FIRST_PATH, 1);
        // }
        // } catch (Exception e) {
        //
        // }
        updateAccPowerOffDelay(null);

        updatePackageList();

    }

    private static final String ACC_DELAY_POWEROFF = "/sys/class/ak/source/acc_delay_poweroff";
    private static final String PACKAGE_TTS = "com.svox.pico";
    private static final String PACKAGE_IGO = "com.nng.igo";
    private static final String[] CARPLAY_APK = {"com.suding.speedplay", "com.zjinnova.zlink"};

    public static void initGpsSettings() {
        // init carplay id
        int carplay_uid = 0;
        for (ResolveInfo appInfo : apps) {
            for (String packageName : CARPLAY_APK) {
                if (packageName.equals(appInfo.activityInfo.packageName)) {
                    carplay_uid = appInfo.activityInfo.applicationInfo.uid;
                    Log.d(TAG, "initGpsSettings carplay uid" + packageName + carplay_uid);
                    SystemProperties.set("ak.af.carplay.uid", String.valueOf(carplay_uid));
                    SystemProperties.set("ak.af.carplay.package", packageName);
                    break;
                }
            }
        }
        //
        String packageName = SystemConfig.getProperty(mThis, MachineConfig.KEY_GPS_PACKAGE);
        if (packageName == null)
        {
            String s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_DEFAULT_GPS);
            if (s != null) {
                String[] ss = s.split("/");
                if (ss.length > 1) {
                    SystemConfig.setProperty(mThis, MachineConfig.KEY_GPS_PACKAGE, ss[0]);
                    SystemConfig.setProperty(mThis, MachineConfig.KEY_GPS_CLASS, ss[1]);
                }
            }
        }

        if (packageName != null) {
            int uid = 0;
            int tts_uid = 0;
            for (ResolveInfo appInfo : apps) {
                if (packageName.equals(appInfo.activityInfo.packageName)) {
                    uid = appInfo.activityInfo.applicationInfo.uid;
                    break;
                }
            }

            if (uid != 0) {
                SystemProperties.set("ak.af.navi.uid", String.valueOf(uid));
            } else {
                mInitGpsSettingTime--;
                if (mInitGpsSettingTime > 0) {
                    mThis.mHandler.removeMessages(MSG_REPEAT_GPS_SETTINGS);
                    mThis.mHandler.sendEmptyMessageDelayed(MSG_REPEAT_GPS_SETTINGS, 4000);
                }
            }

            if (packageName.startsWith(PACKAGE_IGO)) {
                final Intent intent = new Intent("android.intent.action.TTS_SERVICE", null);
                final List<ResolveInfo> apps1 = mThis.getPackageManager().queryIntentServices(intent, 0);
                String ttsPkgname = Settings.Secure.getString(mThis.getContentResolver(), TTS_DEFAULT_SYNTH);
                if (ttsPkgname != null) {
                    for (ResolveInfo appInfo : apps1) {
                        if (appInfo != null && appInfo.serviceInfo != null) {
                            if (ttsPkgname.equals(appInfo.serviceInfo.packageName)) {
                                if (appInfo.serviceInfo.applicationInfo != null) {
                                    tts_uid = appInfo.serviceInfo.applicationInfo.uid;
                                    //										Log.d(TAG, "found tts uid=" + tts_uid);
                                }
                                break;
                            }
                        }
                    }
                }

                if (tts_uid == 0) {
                    final Intent mainIntent = new Intent("android.intent.action.START_TTS_ENGINE", null);
                    final List<ResolveInfo> apps2 = mThis.getPackageManager().queryIntentActivities(mainIntent, 0);
                    for (ResolveInfo appInfo : apps2) {
                        if (appInfo.activityInfo != null) {
                            if (PACKAGE_TTS.equals(appInfo.activityInfo.packageName)) {
                                tts_uid = appInfo.activityInfo.applicationInfo.uid;

                                break;
                            }
                        }
                    }
                }
            }
            SystemProperties.set("ak.af.tts.uid", String.valueOf(tts_uid));
            Log.d(TAG, packageName + ":set uid:" + uid + "tts uid" + tts_uid);

        }
    }

    private void initUIService() {
        Intent it = new Intent(Intent.ACTION_RUN);
        try {
            it.setClassName("com.car.ui", "com.car.service.UIService");
            startService(it);
            it.setClassName("com.android.car.bt", "com.android.car.bt.ATBluetoothService");
            startService(it);
        } catch (Exception e) {
            Log.d(TAG, "startUIService err");
        }
        Log.d(TAG, "initUIService");
    }

    private void initBTType() {
        String s = MachineConfig.getProperty(MachineConfig.KEY_BT_TYPE);
        if (s == null) {
            s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_BT_TYPE);
        }
        if (s != null) {
            try {
                int t = Integer.parseInt(s);
                t = t & 0xff;
                if (mMcuManager != null) {
                    if (t == MachineConfig.VAULE_BT_TYPE_GOC_8761) {
                        mMcuManager.setMcuBTType(1);
                    } else if (t == MachineConfig.VAULE_BT_TYPE_GOC_RF210) {
                        mMcuManager.setMcuBTType(2);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void setLowMemoryKiller() {
        // Log.d(TAG, "setLowMemoryKiller!!!!!!!!!!");
        // /sys/module/lowmemorykiller/parameters/minfree
        // "6144,7680,9216,10752,12288,15360"
        Util.sudoExec("chmod:666:/sys/module/lowmemorykiller/parameters/minfree");
        Util.doSleep(200);
        Util.setFileValue("/sys/module/lowmemorykiller/parameters/minfree", "6144,7680,9216,10752,12288,15360");
    }

    private void printfSystemReleaseVersion() {
        MMLog.d(TAG, "SystemReleaseVersion:" + Util.getFileString("/system/etc/ak47_release_version"));
    }

    AutoTest mAutoTest;

    private void doAutoTest() {
        mAutoTest = new AutoTest();
        mAutoTest.init(mThis);
    }

    private void saveData(String s, long v) {
        SharedPreferences.Editor shareData = getSharedPreferences(SAVE_DATA, 0).edit();
        shareData.putLong(s, v);
        shareData.apply();
    }

    private long getData(String s) {
        SharedPreferences shareData = getSharedPreferences(SAVE_DATA, 0);
        return shareData.getLong(s, 0);
    }

    private static final String SAVE_DATA = "MyService";
    private static final String SAVE_DATA_TIME = "time";
    private static final String SAVE_DATA_FIRST_SYSTEM_BOOT = "first_system_boot";

    public class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            long timestamp = location.getTime();

            if (timestamp > 1451581346350L) {// >20160101----
                /*
                 * long timestamp2 = System.currentTimeMillis(); mUpdateGpsTime
                 * = true; if ((timestamp2 - timestamp) >= 180000 || (timestamp2
                 * - timestamp) <= -180000) { // try{
                 * CarSystemClock.setCurrentTimeMillis(timestamp, mContext); //
                 * }catch(Exception e){ // // } Log.i(TAG, "true UpdateGpsTime:"
                 * + timestamp); } else {
                 *
                 * Log.i(TAG, "no need UpdateGpsTime:" + (timestamp2 -
                 * timestamp)); }
                 */
                int autoGps = 0;
                try {
                    autoGps = Settings.Global.getInt(getContentResolver(), SystemConfig.GPS_AUTO_UPDATE_TIME);
                } catch (SettingNotFoundException ignored) {
                }

                if (autoGps != 1) {
                    try {
                        SystemClock.setCurrentTimeMillis(timestamp);
                    } catch (Exception ignored) {
                    }
                }

                Log.i(TAG, "true UpdateGpsTime:" + timestamp + ":" + autoGps + ":" + mLocationListener);

                if (mLocationManager != null && mLocationListener != null) {
                    mLocationManager.removeUpdates(mLocationListener);
                    mLocationListener = null;
                }
            }

        }

        public void onProviderDisabled(@NonNull String provider) {

        }

        public void onProviderEnabled(@NonNull String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private void doRadioCmd(Intent intent) {
        int subId = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, -1);
        int param1 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA2, -1);
        int param2 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA3, -1);

        if (param2 != -1) {
            mMcuManager.setRadio(subId, param1, param2);
        } else {
            mMcuManager.setRadio(subId, param1);
        }
    }

    private void doRadioRdsCmd(Intent intent) {
        int subId = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, -1);
        int param1 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA2, -1);
        int param2 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA3, -1);

        if (param2 != -1) {
            mMcuManager.setRadioRds(subId, param1, param2);
        } else {
            mMcuManager.setRadioRds(subId, param1);
        }
    }

    private void doAudioCmd(Intent intent) {
        int subId = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0xffff);
        int param1 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA2, 0xffff);
        int param2 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA3, 0xffff);
        int param3 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA4, 0xffff);

        if (param3 != 0xffff) {
            mMcuManager.setAudio(subId, param1, param2, param3);
        } else if (param2 != 0xffff) {
            mMcuManager.setAudio(subId, param1, param2);
        } else {
            mMcuManager.setAudio(subId, param1);
        }
    }

    private void doAudioGain(byte[] data) {
        mMcuManager.setAudioGain(data);
    }

    private void doDvdCmd(Intent intent) {
        int subId = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, -1);
        int param1 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA2, -1);
        int param2 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA3, -1);
        ///switch (subId) {
        ///    case ProtocolAk47.SEND_COMMON_SUB_DVD_POWER:
        ///        break;
        ///}

        if (param2 != -1) {
            mMcuManager.setDvd(subId, param1, param2);
        } else {
            mMcuManager.setDvd(subId, param1);
        }
    }

    private void doReceiveAppsCmd(Intent intent) {
        int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);
        MMLog.d(TAG, "doReceiveAppsCmd:" + cmd + ":getExtras=" + Objects.requireNonNull(intent.getExtras()).toString());
        switch (cmd) {
            case MyCmd.Cmd.SET_SOURCE:
                mMcuManager.setSource(intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0));
                break;
            case MyCmd.Cmd.SET_SCREEN1_SOURCE:
                GlobalDefinition.setScreen1Source(intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, MyCmd.SOURCE_NONE));
                break;
            case MyCmd.Cmd.QUERY_CURRENT_SOURCE:
                mMcuManager.queryCurrentSource(intent.getIntExtra(MyCmd.EXTRA_COMMON_ID, 0));
                break;
            case MyCmd.Cmd.BACKLIGHT_ON:
                mMcuManager.setBacklight(true);
                break;
            case MyCmd.Cmd.BACKLIGHT_OFF:
                mMcuManager.setBacklight(false);
                break;
            case MyCmd.Cmd.SHOW_MY_RECENT:
                RecentView.toggle(mThis);
                break;
            case MyCmd.Cmd.BT_PHONE_STATUS:
                int data = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mMcuManager.setBtPhoneStatus(data);
                break;
            case MyCmd.Cmd.BT_PHONE_CALLLOG_LIST: {
                Canbox box = CarUtil.getCanboxInstance();
                if (box != null) {
                    //Object obj = intent.getExtra(MyCmd.EXTRA_COMMON_OBJECT);
                    Object obj = intent.getByteArrayExtra(MyCmd.EXTRA_COMMON_OBJECT);//MML
                    box.updateCallLog(obj);
                }
            }
            break;

            case MyCmd.Cmd.CANBOX_RQUEST_DRIVE_DATA: {
                Canbox box = CarUtil.getCanboxInstance();
                int data0 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                if (box != null) {
                    box.requestDriveData(data0);
                }
            }
            break;
            case MyCmd.Cmd.CANBOX_PHONE_STATUS:
                int data1 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mMcuManager.setCanboxPhoneStatus(data1);
                break;
            case MyCmd.Cmd.BT_SEND_HFP_STATUS: {
                Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND_SYSTEM_UI);
                int data2 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);

                it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.BT_BATTERY_SIGNAL);
                it.putExtra(MyCmd.EXTRA_COMMON_DATA, data2);
                it.setPackage("com.android.systemui");
                sendBroadcast(it);
            }
            break;
            case MyCmd.Cmd.SET_SPECTRUM_SCREEN_SAVE:
                int data3 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mMcuManager.updateDSPScreenSaver(data3);
                break;
            case MyCmd.Cmd.SET_AUDIO_GAIN: {
                byte[] gain = intent.getByteArrayExtra(MyCmd.EXTRA_COMMON_OBJECT);
                if (gain != null) {
                    doAudioGain(gain);
                }
            }
            break;
            case MyCmd.Cmd.SET_AUDIO_SPECTRUM: {
                int data4 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mMcuManager.setSpectrumSwitch((byte) data4);
            }
            break;

            case MyCmd.Cmd.SET_OBD_SCREEN_SAVE: {
                int data5 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                OBDView.updateScreenSave(this, data5);
            }
            break;
            case MyCmd.Cmd.MCU_RADIO_SEND_CMD:
                doRadioCmd(intent);
                break;
            case MyCmd.Cmd.MCU_RDS_SEND_CMD:
                doRadioRdsCmd(intent);
                break;
            case MyCmd.Cmd.MCU_AUDIO_SEND_CMD:
                doAudioCmd(intent);
                break;
            case MyCmd.Cmd.MCU_DVD_SEND_CMD:
                doDvdCmd(intent);
                break;
            case MyCmd.Cmd.MCU_AUTO_MUTE:
                mMcuManager.setVoulumeAutoMuteTime(10);
                break;
            case MyCmd.Cmd.MCU_QUERY_VOLUME: {
                int volume = mMcuManager.getMcuVolume();
                Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
                it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.RETURN_CURRENT_VOLUME);
                it.putExtra(MyCmd.EXTRA_COMMON_DATA, volume);
                sendBroadcast(it);

            }
            break;
            case MyCmd.Cmd.MCU_HIDE_VOLUME_UI: {
                mMcuManager.setMcuVolumeUI(intent.getBooleanExtra(MyCmd.EXTRA_COMMON_DATA2, false));
            }
            break;
            case MyCmd.Cmd.MCU_SET_VOLUME:
                int data6 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                if (data6 >= 0) {
                    mMcuManager.setVolume(data6);
                } else if (data6 == -1) {
                    mMcuManager.setVoulumeIncrease(true);
                } else if (data6 == -2) {
                    mMcuManager.setVoulumeIncrease(false);
                } else if (data6 == -3) {
                    mMcuManager.setVoulumeMute(1);
                } else if (data6 == -4) {
                    mMcuManager.setVoulumeMute(0);
                }
                break;
            case MyCmd.Cmd.MCU_SET_VOLTAGE_PROTECT:
                int data7 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mMcuManager.setVoltageProtect((byte) data7);
                break;
            case MyCmd.Cmd.MCU_QUERY_VOLTAGE_PROTECT:
                mMcuManager.setVoltageProtect(0x1100);
                break;
            case MyCmd.Cmd.MCU_SET_LIGHT_DECTECT:
                int data8 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mMcuManager.setLightDectect((byte) data8);
                break;
            case MyCmd.Cmd.MCU_QUERY_LIGHT_DECTECT:
                mMcuManager.setLightDectect((byte) 0xff);
                break;
            case MyCmd.Cmd.APP_REQUEST_SEND_KEY:
                int data9 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                if (!JoyKey.isJoyKey(data9)) {
                    mMcuManager.doKey(data9);
                }
                else
                {
                    boolean down = intent.getBooleanExtra(MyCmd.EXTRA_COMMON_DATA2, true);
                    int data10 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                    if (mJoyKey == null) {
                        mJoyKey = new JoyKey(this);
                    }
                    data10 = mJoyKey.doKey(data10, down);
                    MMLog.d(TAG, down + "22:" + data10);
                    if (data10 != 0) {
                        mMcuManager.doKey(data10);
                    }
                }
                break;
            case MyCmd.Cmd.SET_SCREEN0_SOURCE:
                int data11 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mMcuManager.setScreen0(data11);
                break;
            case MyCmd.Cmd.SHOW_AIR_CONTROL:
                int data12 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                AirManager.startAll(this);
                if (data12 == 0) {
                } else {
                    AirManager.sendAirFunchtion(data12);
                }
                break;
            case MyCmd.Cmd.REQUEST_CANBOX_VERSION:
                int data13 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                showCanboxVersion(data13);
                break;
            case MyCmd.Cmd.SEND_CANBOX_DATA:
                byte[] buf = intent.getByteArrayExtra(MyCmd.EXTRA_COMMON_DATA);
                CarUtil.sendDataToCanbox(buf);
                break;
            case MyCmd.Cmd.SET_DTV_CMD:
                int data14 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mMcuManager.setTV(data14);
                break;
            case MyCmd.Cmd.SET_FRONT_CAMERA_POWER:
                int data15 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mMcuManager.setFrontCamerPower((byte) data15, 0x1);
                break;
            case MyCmd.Cmd.UPDATE_CANBOX:
                doUpdateCanbox(intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA));
                break;
            case MyCmd.Cmd.SEND_UPDATE_CANBOX_SET: {
                Canbox box = CarUtil.getCanboxInstance();
                int data16 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                if (box != null) {
                    box.udpateSet(data16);
                }
            }
            break;
            case MyCmd.Cmd.SET_DTV_CMD_EX:
                // int data2 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA2, 0);
                int data17 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mMcuManager.setTVEx(data17);
                break;
            case MyCmd.Cmd.SET_VCOM:
                int data18 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                mMcuManager.setVCOM(((data18 & 0xff00) >> 8), (data18 & 0xff));
                break;
            case MyCmd.Cmd.AUTO_TEST_SHOW_UI:
                doAutoTest();
                break;
            case MyCmd.Cmd.SHOW_DEBUG_MSG:
                DebugMessage.start(mThis);
                break;
            case MyCmd.Cmd.AUTO_TEST_RESULT:
                int data19 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                if (mAutoTest != null) {
                    int data2 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA2, 0);
                    mAutoTest.testResult(data19, data2);
                }
                break;
            case MyCmd.Cmd.CANBOX_VOICE_CONTROL:
                Canbox box = CarUtil.getCanboxInstance();
                int data20 = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                if (box != null) {
                    MMLog.d(TAG, "CANBOX_VOICE_CONTROL:" + Integer.toHexString(data20).toUpperCase());
                    box.udpateVoiceControl(data20);
                }
                break;
        }
    }

    private JoyKey mJoyKey;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mEmptyView;
    private boolean mShowGpuBug = false;
    private static final String IGO_PATH = "com.navngo.igo.javaclient/com.navngo.igo.javaclient.MainActivity";
    private static final String IGO_PACKAGE = "com.navngo.igo.javaclient";
    private boolean mShowGpuBugOnce = false;
    //private String mPreTopActivity;
    private final static String ZLINK_BROAST = "com.zjinnova.zlink";

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerEventReceiver() {
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE);
        iFilter.addAction(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_ID);
        iFilter.addAction(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_UI);
        iFilter.addAction(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE_CAR_UI);
        iFilter.addAction(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE_CAR_UI_FRAMEWORK);
        iFilter.addAction(MyCmd.BROADCAST_CMD_TO_CAR_SERVICE_BT);

        iFilter.addAction(MyCmd.BROADCAST_ACTIVITY_STATUS);
        iFilter.addAction(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
        iFilter.addAction(Intent.ACTION_LOCALE_CHANGED);

        iFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        iFilter.addAction(ZLINK_BROAST);
        iFilter.addAction("com.carletter.link");
        registerReceiver(mEventReceiver, iFilter);
    }

    private final BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MMLog.d(TAG, "mEventReceiver.onReceive action=" + action);
            if(intent.getExtras() != null)
                MMLog.d(TAG, "mEventReceiver.onReceive getExtras=" + intent.getStringExtra(MyCmd.EXTRA_COMMON_CMD));

            switch (Objects.requireNonNull(action)) {
                case MyCmd.BROADCAST_CMD_TO_CAR_SERVICE:
                case MyCmd.BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_ID:
                case MyCmd.BROADCAST_CMD_TO_CAR_SERVICE_SYSTEM_UI:
                case MyCmd.BROADCAST_CMD_TO_CAR_SERVICE_CAR_UI:
                case MyCmd.BROADCAST_CMD_TO_CAR_SERVICE_CAR_UI_FRAMEWORK:
                case MyCmd.BROADCAST_CMD_TO_CAR_SERVICE_BT:
                    doReceiveAppsCmd(intent);
                    break;
                case MyCmd.BROADCAST_MACHINECONFIG_UPDATE: {
                    String ss = intent.getStringExtra(MyCmd.EXTRA_COMMON_CMD);
                    switch (Objects.requireNonNull(ss)) {
                        case MachineConfig.KEY_CAN_BOX:
                            CanService.updateCanboxEx();
                            break;
                        case MachineConfig.KEY_CAN_BOX_EX:
                            CarUtil.updateCanboxExData();
                            break;
                        case SystemConfig.SHOW_FOCUS_CAR_WARNING_MSG:
                            CanService.updateCanboxSettings();
                            break;
                        case MachineConfig.KEY_SCREEN1_VIEW:
                            ReverseManager.reinit(mThis);
                            break;
                        case MachineConfig.KEY_RUDDER:
                            GlobalDefinition.mRudder = intent.getBooleanExtra(MyCmd.EXTRA_COMMON_DATA, false);
                            break;
                        case MachineConfig.KEY_SAVE_DRIVER_PACKAGE:
                            getSaveDriveConfig();
                            break;
                        case MachineConfig.KEY_SAVE_DRIVER:
                            getSaveDriveSwitch();
                            break;
                        case MachineConfig.KEY_NO_REVERSE:
                            OSProManager.mNoReverse = intent.getBooleanExtra(MyCmd.EXTRA_COMMON_DATA, false);
                            break;
                        case MachineConfig.KEY_SWITCH_TO_FRONT_CAMER:
                            OSProManager.mSwitchToFrontCameraTime = MachineConfig.getPropertyIntOnce(MachineConfig.KEY_SWITCH_TO_FRONT_CAMER);
                            break;
                        case MachineConfig.KEY_ACC_DELAY_OFF:
                            String time = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA);
                            updateAccPowerOffDelay(time);
                            break;
                        case MachineConfig.KEY_APP_HIDE:
                            AppConfig.updateHideAppConfig();
                            if (mMcuManager != null) {
                                mMcuManager.initModeKeyToast(true);
                            }
                            updateUSBDvdConfig(true);
                            break;
                        case MachineConfig.KEY_PANEL_KEY_DEF_CONFIG:
                            int value = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);

                            GlobalDefinition.mPannelKeyType = (value & 0xff00) >> 8;
                            break;
                        case SystemConfig.CANBOX_TEMP_UNIT:
                            String v = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA);
                            try {
                                CarUtil.updateTempUnit(Integer.parseInt(v));
                            } catch (Exception ignored) {
                            }

                            break;
                        case SystemConfig.KEY_LAUNCHER_UI_RM10:
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    killLauncher();
                                }
                            }, 300);
                            break;
                        case SystemConfig.GPS_BRAKE:
                        case SystemConfig.CANBOX_DOOR_VOICE:
                        case SystemConfig.CANBOX_FRONT_RADAR_OPEN_CAMERA:
                            GlobalDefinition.initGPSSpeedSettings(mThis);
                            initGPSSpeedInfo();
                            break;
                        case SystemConfig.KEY_CAR_CELL:
                            GlobalDefinition.mMcuBatteryCell = SystemConfig.getIntProperty(mThis, SystemConfig.KEY_CAR_CELL);
                            if (GlobalDefinition.mMcuBatteryCell == 1) {
                                mMcuManager.queryBattery();
                            }
                            break;
                        case SystemConfig.KEY_SCREEN_SAVE_STYLE:
                            GlobalDefinition.mScreenSaverStyle = SystemConfig.getIntProperty(mThis, SystemConfig.KEY_SCREEN_SAVE_STYLE);
                            break;
                        case MachineConfig.KEY_RADIO_ANT_POWER:
                            int i = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                            mMcuManager.setRadioAntPower(i);
                            break;
                    }
                    break;
                }
                case MyCmd.BROADCAST_ACTIVITY_STATUS: {
                    String s = intent.getStringExtra(MyCmd.EXTRA_COMMON_CMD);
                    if (mFirstRun) {
                        mFirstRun = false;
                    }

                    GlobalDefinition.mTopIsNeedCanboxInfo = (s != null && s.contains("com.canboxsetting"));
                    if ((s != null) && (s.contains("com.zjinnova.zlink") || s.contains("com.suding.speedplay") || s.contains("net.easyconn") || s.contains("com.carletter.car"))) {
                        Util.setProperty("ak.codec.disable_video_out", "0");
                        GlobalDefinition.mTopIsNoNeedBrakeControl = true;
                        sendRudderToSuding();
                    } else {
                        if (GlobalDefinition.mTopIsNoNeedBrakeControl) {
                            GlobalDefinition.mTopIsNoNeedBrakeControl = false;
                            mMcuManager.setBrakeProp();
                        }
                    }

                    if ((s != null) && s.contains("com.carletter.car")) {
                        mTopCarletter = true;
                        sendBroadcast(new Intent("carletter.intent.action.Foreground"));
                        MMLog.d(TAG, "send carletter.intent.action.Foreground!" + mCarletterConnect);

                        if (mCarletterConnect) {
                            mMcuManager.setSource(MyCmd.SOURCE_BT_MUSIC);
                        }
                    } else {
                        if (mTopCarletter) {
                            sendBroadcast(new Intent("carletter.intent.action.Background"));
                            MMLog.d(TAG, "send carletter.intent.action.Background!");
                            mTopCarletter = false;
                        }
                    }

                    MMLog.d(TAG, "BROADCAST_ACTIVITY_STATUS:" + s);
                    if ((s != null) && s.contains("com.antutu.benchmark.full.lite")) {
                        Util.setProperty("use_nuplayer", "true");
                    } else {
                        Util.setProperty("use_nuplayer", "false");
                    }

                    if (!doSaveDriver(s)) {
                        if (isNeedResetArmSound(s)) {
                            Util.setFileValue("/sys/class/ak/source/arm_sound", 1);
                            // mMcuManager.updateArmSound(true);
                        }
                    }

                    try {
                        if (AppConfig.isGpsApp(mThis, s)) {
                            if (mWakeLock == null) {
                                PowerManager pManager = ((PowerManager) getSystemService(POWER_SERVICE));
                                mWakeLock = pManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, getPackageName());
                                mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
                                MMLog.d(TAG, "mWakeLock! acquire");
                            }
                        } else {
                            if (mWakeLock != null) {
                                mWakeLock.release();
                                mWakeLock = null;
                                MMLog.d(TAG, "mWakeLock! release");
                            }
                        }
                    } catch (Exception e) {
                        MMLog.d(TAG, "err mWakeLock! " + e);
                    }

                    GlobalDefinition.sendByCarServiceToSystemUI(mThis, "com.android.systemui", MyCmd.Cmd.SHOW_CUR_APP_NAME);
                    break;
                }
                case Intent.ACTION_LOCALE_CHANGED:
                    mToastSaveDrive = null;
                    initToastSaveDrive();

                    Canbox box = CarUtil.getCanboxInstance();
                    if (box != null) {
                        box.udpateLang();
                    }

                    break;
                case Intent.ACTION_CONFIGURATION_CHANGED:
                    mToastSaveDrive = null;
                    initToastSaveDrive();
                    if (Util.isRKSystem()) {
                        // Util.doSleep(5);
                        if (!(MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDefinition.mSystemUI) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDefinition.mSystemUI) || MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(GlobalDefinition.mSystemUI)/*
									|| MachineConfig.VALUE_SYSTEM_UI16_7099.equals(GlobalDef.mSystemUI)*/)) {
                            killLauncher();
                        }
                    }
                    break;
                case "com.carletter.link":
                    int eventType = intent.getIntExtra("linkMode", 0);
                    String stat = intent.getStringExtra("status");
                    MMLog.d(TAG, "com.carletter.link eventType=" + eventType + ":" + stat);
                    if (eventType == 4) {
                        if ("CONNECTED".equals(stat)) {
                            mCarletterConnect = true;
                            if (mTopCarletter) {
                                mMcuManager.setSource(MyCmd.SOURCE_BT_MUSIC);
                            }
                        } else if ("DISCONNECT".equals(stat)) {
                            mCarletterConnect = false;
                        }
                    }

                    break;
                case ZLINK_BROAST:
                    String status = intent.getStringExtra("status");
                    MMLog.d(TAG, status + "!!!!!!!!!!33:");
                    if ("CONNECTED".equals(status)) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_REPEAT_INIT_SUDING_ILL, 4, 0), 100);
                    }
                    break;
            }
        }
    };

    private final BroadcastReceiver mUSBDeviceEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                if (GlobalDefinition.mIsUSBDvd && isDiscExist(intent)) {
                    GlobalDefinition.makeUSBDVDExist();
                }
            }
        }
    };

    private void registerMountListener() {
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        iFilter.addDataScheme("file");
        registerReceiver(mUSBDeviceEventReceiver, iFilter);
    }

    private void unregisterMountListener() {
        try {
            if (mUSBDeviceEventReceiver != null) {
                unregisterReceiver(mUSBDeviceEventReceiver);
                //mUSBDeviceEventReceiver = null;
            }
        } catch (Exception ignored) {
        }
    }

    private void showCanboxVersion(int data) {
        String v = CarUtil.getCanboxVersion();
        if (data == 1) {
            Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.RETURN_CANBOX_VERSION);
            it.putExtra(MyCmd.EXTRA_COMMON_DATA, v);

            sendBroadcast(it);
        } else {
            Toast.makeText(mThis, "Canbox Version:" + v, Toast.LENGTH_LONG).show();
        }

        Util.setProperty("canbox_version", v);
    }

    private void doGPUBugForIgo(String s) {
        if (mShowGpuBugOnce) return;
        if (IGO_PATH.equals(s)) {
            if (mEmptyView == null) {

                mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                mLayoutParams = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.RGBA_8888);
                mEmptyView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.gpu_bug_for_igo, null);
            }

            doShowFixGpu();
            mShowGpuBugOnce = true;
        } else {
            doHideFixGpu();
        }
    }

    void doHideFixGpu() {
        if (mShowGpuBug && mEmptyView != null) {
            mWindowManager.removeView(mEmptyView);

            mShowGpuBug = false;
        }
    }

    void doShowFixGpu() {
        if (!mShowGpuBug && mEmptyView != null) {
            mShowGpuBug = true;
            mWindowManager.addView(mEmptyView, mLayoutParams);
            mHandler.removeMessages(MSG_FIX_GPU_BUG);
            mHandler.sendEmptyMessageDelayed(MSG_FIX_GPU_BUG, 1600);
        }
    }

    private void sendRudderToSuding() {
        Intent it = new Intent();
        it.setAction("com.zjinnova.zlink");
        if (GlobalDefinition.mRudder) {
            it.putExtra("command", "SET_DRIVER_POS_RIGHT");
        } else {
            it.putExtra("command", "SET_DRIVER_POS_LEFT");
        }

        Log.d(TAG, "sendRudderToSuding:" + GlobalDefinition.mRudder);
        sendBroadcast(it);
    }

    private boolean mTopCarletter = false;
    private boolean mCarletterConnect = false;
    private WakeLock mWakeLock;

    private void killLauncher() {
        MMLog.d(TAG, "killLauncher!");
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            //am.forceStopPackage("com.android.launcher");
            //am.forceStopPackage("com.android.launcher3");
        } catch (Exception e) {
            MMLog.d(TAG, "killLauncher fail!");
        }
    }

    private boolean mFirstRun = true;

    private void unregisterListener() {
        if (mEventReceiver != null) {
            unregisterReceiver(mEventReceiver);
        }
    }

    private boolean isUsbDVD(UsbDevice device) {
        String product = device.getProductName();
        int vid = device.getVendorId();
        int pid = device.getProductId();
        if ((product != null) && product.contains("Sunext") && (0x04fc == vid) && (0x0171 == pid)) {
            Log.i(TAG, "## found Sunext CD-ROM");
            return true;
        } else {
            return false;
        }
    }

    private boolean isDiscExist(Intent intent) {
        if (intent != null) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            return isUsbDVD(device);
        } else {
            UsbManager mUSBManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();
            for (UsbDevice device : deviceList.values()) {
                if (isUsbDVD(device)) return true;
            }
        }
        return false;
    }

    private final String PROC_TOUCH_CONFIG = "/proc/gt9xx_config";
    private final String TOUCH_CONFIG_FILE = "/touch_config.cfg";

    private void updateTouchScreenConfig(String path) {
        Util.doSleep(1000);
        final String configPath = path + TOUCH_CONFIG_FILE;
        File f = new File(configPath);
        if (!f.exists()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = String.format(getResources().getString(R.string.update_touch_config), configPath);
        builder.setTitle(title);
 /*       builder.setPositiveButton(com.android.internal.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                FileReader fr = null;
                boolean ok = false;
                try {
                    fr = new FileReader(configPath);
                    BufferedReader reader = new BufferedReader(fr, 1280);
                    String config = reader.readLine();
                    Util.setFileValue(PROC_TOUCH_CONFIG, config);
                    reader.close();
                    fr.close();
                    ok = true;
                } catch (Exception ignored) {
                }

                if (ok) {
                    Toast.makeText(mThis, "Update Ok!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mThis, "Update Fail!", Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton(com.android.internal.R.string.cancel, null);
*/
        final AlertDialog dialog = builder.create();
        // 在dialog show前添加此代码，表示该dialog属于系统dialog。
        Objects.requireNonNull(dialog.getWindow()).setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

        dialog.show();
    }

    private static List<ResolveInfo> apps;

    public static void updatePackageList() {
        if (mThis != null) {

            PackageManager mPackageManager = mThis.getPackageManager();
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            apps = mPackageManager.queryIntentActivities(mainIntent, 0);

            initGpsSettings();
            checkTestMemory();
        }
    }

    private static int mInitGpsSettingTime = 10;
    private static boolean mIsTestMemory = false;

    private static void checkTestMemory() {
        if (apps != null) {
            for (ResolveInfo rv : apps) {
                if ("com.longsys.testdram".equals(rv.activityInfo.packageName)) {
                    mIsTestMemory = true;
                    return;
                }
            }
        }
        mIsTestMemory = false;
    }

    private int getId(String packageName) {

        if (apps == null) {
            updatePackageList();
        }

        int uid = 0;
        for (ResolveInfo appInfo : apps) {
            if (packageName.contains(appInfo.activityInfo.packageName)) {
                if ((appInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    uid = appInfo.activityInfo.applicationInfo.uid;
                }
                break;
            }
        }

        return uid;
    }

    private static final String YL_APPLICATION = "net.easyconn/net.easyconn.MainActivity";
    private static final String YL_APPLICATION2 = "net.easyconn/net.easyconn.ScreenActivity";

    private boolean isNeedResetArmSound(String packageName) {
        String cmd = Util.getFileString("/sys/class/ak/source/app_snd_uids");
        if (cmd != null) {
            String[] id = cmd.split(":");
            int uid = getId(packageName);

            // Log.d("aa", uid+"===="+cmd);
            if (uid == 0) {
                return false;
            }
            int uid2;
            for (String s : id) {
                try {
                    uid2 = Integer.parseInt(s);
                } catch (Exception e) {
                    uid2 = 0;
                }
                if (uid2 != 0 && (uid == uid2)) {
                    return true;
                }
            }

        }
        return false;
    }

    private final static int LOCK_KEY_TIME = 900;
    private long mStartPlayTime = 0;

    private void lockKey() {
        Log.d(TAG, "lockKey!");
        mStartPlayTime = System.currentTimeMillis();
    }

    private boolean isLockKey() {
        if ((System.currentTimeMillis() - mStartPlayTime) < LOCK_KEY_TIME) {
            Log.d(TAG, "lock!");
            return true;
        }
        return false;
    }

    private MediaRouter mMediaRouter = null;

    private void initMediaRouter() {
        mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
    }

    private final MediaRouter.SimpleCallback mMediaRouterCallback = new MediaRouter.SimpleCallback() {
        @Override
        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
            Log.w(TAG, "onRouteSelected: type=" + type + ", info=" + info);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
            Log.w(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
        }

        @Override
        public void onRoutePresentationDisplayChanged(MediaRouter router, RouteInfo info) {
            Log.w(TAG, "onRoutePresentationDisplayChanged: info=" + info);
        }
    };

    private int mSaveDriveSwitch = 0;
    private final ArrayList<String> mPackageSet = new ArrayList<String>();
    Toast mToastSaveDrive;

    private void initToastSaveDrive() {
        if (mToastSaveDrive == null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.save_drive, null);
            mToastSaveDrive = new Toast(this);

            mToastSaveDrive.setGravity(Gravity.CENTER, 0, 0);
            mToastSaveDrive.setDuration(Toast.LENGTH_LONG);
            mToastSaveDrive.setView(v);
        }
    }

    private boolean doSaveDriver(String s) {
        String pn;
        if (mSaveDriveSwitch > 0) {
            boolean isSaveApp = false;
            if (mSaveDriveSwitch == 1) {
                String[] ss = s.split("/");
                if (ss.length > 0) {
                    pn = ss[0];

                    if (AppConfig.isPackageSaveSpecApp(pn)) {
                        pn = s;
                    }

                    if (isSet(pn)) {
                        isSaveApp = true;
                    }
                }
            } else if (mSaveDriveSwitch == 2) {
                isSaveApp = true;
            }

            if (isSaveApp) {
                if (mMcuManager.mBrake == 0) {
                    Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);
                    // Toast t = Toast.makeText(this,
                    // R.string.warning_driving,
                    // Toast.LENGTH_LONG);
                    initToastSaveDrive();
                    mToastSaveDrive.show();
                }
            }
        }
        return false;
    }


    public static void doSaveDriver() {
        if (mThis != null) {
            String s = AppConfig.getTopActivity();
            mThis.doSaveDriver(s);
        }
    }

    private boolean isSet(String packageName) {
        for (String s : mPackageSet) {
            if (packageName.equals(s)) {
                return true;
            }
        }
        return false;
    }

    private void getSaveDriveConfig() {
        mPackageSet.clear();
        String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_SAVE_DRIVER_PACKAGE);
        if (s != null) {
            String[] ss = s.split(":");
            for (String value : ss) {
                if (value.length() > 1) {
                    mPackageSet.add(value);
                }
            }
        }
    }

    private void getSaveDriveSwitch() {
        mSaveDriveSwitch = MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_SAVE_DRIVER);
        if (mSaveDriveSwitch != 2) {
            String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_SAVE_DRIVER);
            if ("1".equals(s)) {
                mSaveDriveSwitch = 1;
                getSaveDriveConfig();
            } else {
                mSaveDriveSwitch = 0;
            }
        }
    }

    public static AlertDialog mDialogUpdateCanbox;
    private String file = null;

    private void doUpdateCanbox(String manufacturer) {
        List<StorageInfo> ls = UtilSystem.listAllStorage(this);
        File f = null;
        //final String manufacturer = man;
        mDialogUpdateCanbox = null;
        String update = null;
        for (StorageInfo si : ls) {
            if (si.mType == StorageInfo.TYPE_USB || si.mType == StorageInfo.TYPE_SD) {
                file = si.mPath + "/canbox.upde";
                f = new File(file);
                if (f.exists()) {
                    update = file;
                    break;
                }
            }
        }
        MMLog.d(TAG, "doUpdateCanbox:" + update);
        if (update != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String title = String.format(getResources().getString(R.string.update_canbox), update);
            builder.setTitle(title);
             builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     CarUtil.updateCanbox(file, mThis, manufacturer);
                 }
             });

            builder.setNegativeButton("Cancel", null);

            mDialogUpdateCanbox = builder.create();
            // 在dialog show前添加此代码，表示该dialog属于系统dialog。
            Objects.requireNonNull(mDialogUpdateCanbox.getWindow()).setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

            mDialogUpdateCanbox.show();
        } else {
            Toast.makeText(this, "file not found", Toast.LENGTH_LONG).show();
        }
    }

    private LocationListener mGpsBrakeLocationListener = null;

    private void initGPSSpeedInfo() {
        Log.d(TAG, "isNeedGPSSpeed:" + isNeedGPSSpeed() + ":" + GlobalDefinition.mSettingGPSBrake);
        if (GlobalDefinition.mSettingGPSBrake == 0) {
            GlobalDefinition.mPreGPSBrake = 0;
            Util.setFileValue("/sys/class/ak/source/brake_status", GlobalDefinition.mPreGPSBrake);
        }

        if (isNeedGPSSpeed()) {
            if (mGpsBrakeLocationListener == null) {

                mGpsBrakeLocationListener = new LocationListener() {

                    public void onLocationChanged(@NonNull Location location) {
                        GlobalDefinition.mGPSSpeed = (location.getSpeed() * 3.6f);

                        if (GlobalDefinition.mSettingRadarFrontCamera == 1) {
                            if (GlobalDefinition.mGPSSpeed > GlobalDefinition.SPEED_TO_QUIT_FRONT_CAMERA) {
                                GlobalDefinition.autoCloseFrontByGpsSpeed();
                            }
                        }

                        if (GlobalDefinition.mSettingDoorVoice == 1) {
                            if (DoorStatusPanel.mDoorStatus != 0 && (GlobalDefinition.mGPSSpeed > GlobalDefinition.SPEED_TO_QUIT_FRONT_CAMERA)) {
                                DoorStatusPanel.checkDoorOK();
                            }
                        }

                        if (GlobalDefinition.mSettingGPSBrake >= 0) {
                            int brake = 1;
                            if (GlobalDefinition.mGPSSpeed > GlobalDefinition.mSettingGPSBrake) {
                                brake = 0;
                            }
                            Log.d(TAG, "gps speed:" + GlobalDefinition.mGPSSpeed + " brake " + brake + ":" + GlobalDefinition.mPreGPSBrake + ":" + mMcuManager.mBrake);
                            if (GlobalDefinition.mPreGPSBrake != brake) {
                                GlobalDefinition.mPreGPSBrake = brake;
                                if (brake == 0) {
                                    if (mMcuManager.mBrake == 1) {
                                        brake = 1;
                                    }
                                }
                                BroadcastUtil.sendByCarService(mThis, MyCmd.Cmd.MCU_BRAK_CAR_STATUS, brake);

                                Util.setFileValue("/sys/class/ak/source/brake_status", GlobalDefinition.mPreGPSBrake);

                                mMcuManager.setBrakeProp();
                            }
                        }
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(@NonNull String provider) {

                    }

                    public void onProviderDisabled(@NonNull String provider) {
                    }

                };

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mGpsBrakeLocationListener);
            }
        } else {
            if (mGpsBrakeLocationListener != null) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.removeUpdates(mGpsBrakeLocationListener);
                mGpsBrakeLocationListener = null;
            }
        }
    }

    private boolean isNeedGPSSpeed() {
        return GlobalDefinition.mSettingDoorVoice == 1 || GlobalDefinition.mSettingRadarFrontCamera == 1 || GlobalDefinition.mSettingGPSBrake > 0;
    }

    public void testGPSSpeed(int speed) {
        if (mGpsBrakeLocationListener != null) {
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setSpeed(speed / 3.6f);
            mGpsBrakeLocationListener.onLocationChanged(location);
        }
    }

    public static void testGPSSpeed2(int speed) {
        if (mThis != null) {
            mThis.testGPSSpeed(speed);
        }
    }
}
