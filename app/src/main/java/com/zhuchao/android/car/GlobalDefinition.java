package com.zhuchao.android.car;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UserHandle;
import android.util.Log;

import com.common.utils.AppConfig;
import com.common.utils.MachineConfig;
import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.common.utils.Util;
import com.common.utils.UtilSystem;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.hardware.BackTrack;
import com.zhuchao.android.car.manager.McuManager;

import java.io.File;

public class GlobalDefinition {

    public static final String TAG = "GlobalConstant";
    public static final String BRIGHTNESS_SCREEN1 = "/sys/class/backlight/ak-backlight/aux_bkl_lvl";//0~20 ?
    public static final String BRIGHTNESS_CVBS_701 = "/sys/class/misc/mst701/device/b";  //0~244
    public static final String BRIGHTNESS_CVBS_7181 = "/sys/devices/virtual/ak/source/cvbs_brightness";  //0~244
    public static final String BRIGHTNESS_CONTRAST = "/sys/class/ak/source/cvbs_contrast";  //1~244
    public static final int CVBS_DEFALUT_BRIGHTNESS_701 = 140;
    public static final int CVBS_DEFALUT_BRIGHTNESS_7181 = 128;
    public static final int CVBS_DEFALUT_CONTRAST = 128;
    public static final String SCREEN_8600_MAIN = "/sys/class/misc/ak-lcd/device/main_area";
    public static final String SCREEN_8600_ICON = "/sys/class/misc/ak-lcd/device/icon";
    public static final String SCREEN_8600_ANIM = "/sys/class/misc/ak-lcd/device/anim";
    public static final int SPEED_TO_QUIT_FRONT_CAMERA = 20;
    private static final String DVD_CHECK = "/sys/module/ak_dvd/parameters/alive";
    public static long mSystemBootStartTime = 0;
    public static boolean mIsTesting = false;
    public static boolean mTopIsNeedCanboxInfo = false;
    public static boolean mTopIsNoNeedBrakeControl = false;
    public static boolean mIsUSBDvd = false; //
    public static String BRIGHTNESS_CVBS = BRIGHTNESS_CVBS_701;  //1~244
    public static int mMediaInfoToastBackground = 0;
    public static int CVBS_DEFALUT_BRIGHTNESS = CVBS_DEFALUT_BRIGHTNESS_701;
    public static int mPannelKeyType = 0;
    public static boolean mRudder = false;
    public static int mReverseBrightness = 0;
    public static int mReverseContrast = 0;
    public static int mTouchKeyType = 0;
    public static int mMcuBatteryCell = 0;
    public static String mSystemUI = null;
    public static int mSettingDoorVoice = 0;
    public static int mSettingRadarFrontCamera = 0;
    public static int mSettingGPSBrake = 0;
    public static float mGPSSpeed = -1;
    public static int mModeKeyDelayTime = 2000;
    public static int mScreenSaverStyle = 0;
    //for 8600
    public static boolean mIs8600 = false;
    public static int mAutoFrontCameraStatus = 0;
    public static int mPreGPSBrake = 0;
    private static boolean mIsTestingEx = false;

    ////for 7.1
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static int mScreen1Source = MyCmd.SOURCE_NONE;
    private static WakeLock mWakeLock;

    public static Context getContext() {
        return mContext;
    }

    public static void init(Context c) {
        mContext = c;
        mSystemUI = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);
        if (Util.isRKSystem()) {
            BRIGHTNESS_CVBS = BRIGHTNESS_CVBS_7181;
            CVBS_DEFALUT_BRIGHTNESS = CVBS_DEFALUT_BRIGHTNESS_7181;

            File f = new File(SCREEN_8600_MAIN);
            if (f.exists()) {
                mIs8600 = true;
            }
        }


        mMediaInfoToastBackground = MachineConfig.getPropertyIntReadOnly(SettingProperties.MEDIA_INFO_TOAST_BACKGROUND);
        CarUtil.mTempUnit = SettingProperties.getIntProperty(c, SettingProperties.CANBOX_TEMP_UNIT);
        mScreenSaverStyle = SettingProperties.getIntProperty(c, SettingProperties.KEY_SCREEN_SAVE_STYLE);
        mMcuBatteryCell = SettingProperties.getIntProperty(c, SettingProperties.KEY_CAR_CELL);
        mModeKeyDelayTime = MachineConfig.getPropertyIntReadOnly(MachineConfig.KEY_MODE_DELAY_TIME);
        if (mModeKeyDelayTime <= 0 || mModeKeyDelayTime > 5000) {
            mModeKeyDelayTime = 2000;
        }

        BackTrack.reloadConfig(c);
        initGPSSpeedSettings(c);
    }

    public static void initGPSSpeedSettings(Context c) {
        mSettingGPSBrake = MachineConfig.getPropertyIntReadOnly(SettingProperties.GPS_BRAKE);
        if (mSettingGPSBrake == 1) {
            mSettingGPSBrake = SettingProperties.getIntProperty(c, SettingProperties.GPS_BRAKE);
            if (mSettingGPSBrake == 1) {
                mSettingGPSBrake = 15;
            } else {
                mSettingGPSBrake = 0;
            }
        }

        mSettingDoorVoice = SettingProperties.getIntProperty(c, SettingProperties.CANBOX_DOOR_VOICE);
        mSettingRadarFrontCamera = SettingProperties.getIntProperty(c, SettingProperties.CANBOX_FRONT_RADAR_OPEN_CAMERA);
    }

    public static int getScreen1Source() {
        return mScreen1Source;
    }

    public static void setScreen1Source(int i) {
        mScreen1Source = i;
    }

    // no screen2 now by allen
    public static int getScreenNum(Context context) {
        //		DisplayManager displayManager = (DisplayManager) context
        //				.getSystemService(Context.DISPLAY_SERVICE);
        //		Display[] display = displayManager.getDisplays();
        return 1;//display.length;
    }

    public static void sendByCarServiceToSystemUI(Context context, String packageName, int cmd) {
        Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND_SYSTEM_UI);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        if (packageName != null) {
            it.setPackage(packageName);
        }

        if (Util.isAndroidQ() || Util.isAndroidR()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.sendBroadcastAsUser(it, UserHandle.getUserHandleForUid(-1)); //UserHandle.ALL
            }
        } else {
            context.sendBroadcast(it);
        }
    }

    public static void sendByCarServiceToSystemUI(Context context, String packageName, int cmd, String s) {
        if (context != null) {
            if (cmd == MyCmd.Cmd.SET_OUT_DOOR_TEMP) {
                if (CarUtil.isHideOurDoorTemp()) {
                    s = "";
                }
            }
            Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND_SYSTEM_UI);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
            it.putExtra(MyCmd.EXTRA_COMMON_DATA, s);
            if (packageName != null) {
                it.setPackage(packageName);
            }
            if (Util.isAndroidQ() || Util.isAndroidR()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.sendBroadcastAsUser(it, UserHandle.getUserHandleForUid(-1));//UserHandle.ALL
                }
            } else {
                context.sendBroadcast(it);
            }
        }
    }

    public static void sendByCarServiceToCarUI(Context context, String packageName, int cmd) {
        if (context != null) {
            Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND_SYSTEM_UI);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
            if (packageName != null) {
                it.setPackage(packageName);
            }
            if (Util.isAndroidQ() || Util.isAndroidR()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.sendBroadcastAsUser(it, UserHandle.getUserHandleForUid(-1));//UserHandle.ALL
                }
            } else {
                context.sendBroadcast(it);
            }
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    public static void wakeLock() {
        if (mContext != null) {
            if (mWakeLock == null) {
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                // mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                // | PowerManager.SCREEN_DIM_WAKE_LOCK
                // | PowerManager.ON_AFTER_RELEASE, TAG);
                mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
                mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
            }
        }
    }

    public static void wakeRelease() {
        if (null != mWakeLock) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    public static void wakeLockOnce() {
        if (mContext != null) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            WakeLock mWakeLockOne = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
            mWakeLockOne.acquire(10 * 60 * 1000L /*10 minutes*/);
            mWakeLockOne.release();
        }
    }

    public static void setSmallLcd(String s) {
        Log.d(TAG, mIs8600 + "setSmallLcd:" + s);
        if (mIs8600) {
            Util.setFileValue(SCREEN_8600_MAIN, s);
        }
    }

    public static void setSmallLcdIcon(String s) {
        Log.d(TAG, mIs8600 + "setSmallLcdIcon:" + s);
        if (mIs8600) {
            Util.setFileValue(SCREEN_8600_ICON, s);
        }
    }

    public static void setSmallLcdAinm(String s) {
        Log.d(TAG, mIs8600 + "setSmallLcdAinm:" + s);
        if (mIs8600) {
            Util.setFileValue(SCREEN_8600_ANIM, s);
        }
    }

    public static void updateLcd(int source, String s) {
        if (mIs8600) {
            McuManager mcu = McuManager.getInstance();
            if (mcu != null) {
                mcu.updateSmallLcd(source, s);
            }
        }
    }

    public static boolean isOwerAppControlTop() {
        String top = AppConfig.getTopActivity();
        return top != null && top.contains("com.car.ui");
    }

    public static void makeSureDVDExist(boolean exist) {
        //		Log.d(TAG, "makeSureDVDExist:"+exist+":"+GlobalDef.mIsUSBDvd+":"+AppConfig.isHidePackage("com.zhuchao.android.car.dvd.DVDPlayer"));
        if (!exist) {
            String s = Util.getFileString(DVD_CHECK);
            if ("Y".equals(s)) {
                exist = true;
            }
        }

        if (exist) {
            if (GlobalDefinition.mIsUSBDvd) {
                GlobalDefinition.mIsUSBDvd = false;
                MachineConfig.setProperty(MachineConfig.KEY_USB_DVD, "0");
            }

            if (AppConfig.isHidePackage("com.zhuchao.android.car.dvd.DVDPlayer")) {
                delHideApp(AppConfig.HIDE_APP_DVD);
            }
        }
    }

    private static void delHideApp(String show) {
        String value = MachineConfig.getProperty(MachineConfig.KEY_APP_HIDE);
        if (value != null) {
            String[] ss = value.split(",");

            value = "";
            String s;
            for (int i = 0; i < ss.length; i++) {
                s = ss[i];
                if (!show.equals(s)) {
                    if (value.length() > 1) {
                        value += ",";
                    }
                    value += s;
                }
            }
            MachineConfig.setProperty(MachineConfig.KEY_APP_HIDE, value);
            // MachineConfig.notifyAll(mActivity);

            Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_APP_HIDE);
            if (Util.isAndroidQ() || Util.isAndroidR()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mContext.sendBroadcastAsUser(it, UserHandle.getUserHandleForUid(-1)); //UserHandle.ALL
                }
            } else {
                mContext.sendBroadcast(it);
            }
        }
    }

    public static void makeUSBDVDExist() {
        if (!GlobalDefinition.mIsUSBDvd) {
            GlobalDefinition.mIsUSBDvd = true;
            MachineConfig.setProperty(MachineConfig.KEY_USB_DVD, "1");

            Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_APP_HIDE);
            if (Util.isAndroidQ() || Util.isAndroidR()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mContext.sendBroadcastAsUser(it, UserHandle.getUserHandleForUid(-1)); //UserHandle.ALL
                }
            } else {
                mContext.sendBroadcast(it);
            }
        }
    }

    public static void autoCloseFrontByGpsSpeed() {
        if (mAutoFrontCameraStatus == 1 && AppConfig.CAR_UI_FRONT_CAMERA.equals(AppConfig.getTopActivity())) {
            Intent it = new Intent(Intent.ACTION_VIEW);
            it.setClassName("com.car.ui", "com.zhuchao.android.car.frontcamera.FrontCameraActivity");
            it.putExtra("finish", 1);
            it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(it);
            mAutoFrontCameraStatus = 0;
        }
    }

    public static void autoOpenFrontByGpsSpeed() {
        if (AppConfig.isHidePackage("com.zhuchao.android.car.frontcamera.FrontCameraActivity")) {
            return;
        }

        if (!AppConfig.CAR_UI_FRONT_CAMERA.equals(AppConfig.getTopActivity())) {
            if (GlobalDefinition.mGPSSpeed < GlobalDefinition.SPEED_TO_QUIT_FRONT_CAMERA) {
                UtilSystem.doRunActivity(GlobalDefinition.getContext(), "com.car.ui", "com.zhuchao.android.car.frontcamera.FrontCameraActivity");
                mAutoFrontCameraStatus = 1;
            }
        }
    }

    public static void beep(int i) {
        Log.d("aac", "beep=" + i);
        Util.setFileValue("/sys/class/ak/source/beep", i);
    }


    public static void sendInputTap(int x, int y) {
        String s = "input tap " + x + " " + y;
        Util.sudoExec(s);
    }

    public static boolean getTestingEx() {
        return mIsTestingEx;
    }

    public static void setTestingEx(boolean b) {
        mIsTestingEx = b;
    }
}
