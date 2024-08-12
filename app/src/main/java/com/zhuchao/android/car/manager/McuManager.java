package com.zhuchao.android.car.manager;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.Kernel;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.ProtocolAk47;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.common.util.UtilCarKey;
import com.common.util.UtilSystem;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.autotest.AutoTest;
import com.zhuchao.android.car.canbox.CanService;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.OBDView;
import com.zhuchao.android.car.canbox.ReverseManager;
import com.zhuchao.android.car.canbox.ReverseUI;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.hardware.Mcu;
import com.zhuchao.android.car.service.MyCarService;
import com.zhuchao.android.car.ui.BacklightPanel;
import com.zhuchao.android.car.ui.VolumePanel;
import com.zhuchao.android.car.view.Nissian360ButtonView;
import com.zhuchao.android.fbase.ByteUtils;
import com.zhuchao.android.fbase.MMLog;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class McuManager {
    private final static String TAG = "McuManager";
    private Mcu mMcu;
    public static String mMcuVersion;
    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static McuManager mThis;
    private CanService mCanService;
    // private static final long SLEEP_LONG_TIME_TO_REBOOT = 1000; //test
    private static final long SLEEP_LONG_TIME_TO_REBOOT = 10 * 24 * 3600 * 1000;
    private long mSystemStartTime = 0;
    private int DEFAULT_VOLUME = 12;
    public static final int MAX_VOLUME = 30;

    public static McuManager getInstance(Context c) {
        if (mThis == null && c != null) {
            mThis = new McuManager();
            mThis.init(c);
        }
        return mThis;
    }

    public static McuManager getInstance() {
        return mThis;
    }

    public void init(Context c) {
        mMcu = Mcu.getInstance();
        mContext = c;
        mMcu.sendCmd(Mcu.MCU_OPEN);
        mMcu.setHandler(mMcuHandler);

        doPowerOnDelay();
        mCanService = CanService.getInstance(mContext);
        initEQIndepend();

        String s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_BT_MUSIC_INSIDE);
        if (MachineConfig.VALUE_ON.equals(s)) {
            mBtMusicInBTapk = true;
        }

        mSystemStartTime = SystemClock.uptimeMillis();
        updateSmallLcd(mAppSource, null);
        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SYSTEMUI_STATUS_BAR_VISIBLE); //for carsh....
        /// GlobalDef.makeSureDVDExist(false);
        /// setMcuSceen0(0);
        doLockPowerKey(true);
        registerReceiver();

        sendAccState(mContext, 1);
        startZlink();
    }

    // some data store this
    private byte mMute = 0;
    private byte mVolume = -1;

    // private byte mBrake = 0;
    private byte mRearL = 0;
    private byte mRearR = 0;
    private byte mScreen0 = 0;
    public byte mBrake = -1;
    public byte mBrakeSwitch = 0;

    private void doMcuData(byte[] param) {
        ///if (!DebugMessage.updateText(param, true)) {
        ///}
        if(param[0] != ProtocolAk47.TYPE_RDS_RECEIVE)
           MMLog.i(TAG, "DoMcuData:" + ByteUtils.BuffToHexStr(param));//Util.byteArrayToHex(param) +","+

        if (GlobalDefinition.getTestingEx()) {
            Canbox canbox = CarUtil.getCanboxInstance();
            if (canbox != null) {
                try {
                    canbox.parseVersion(0, param);
                } catch (Exception ignored) {
                }
            }
        }

        if (mRealPowerOff) {
            MMLog.d(TAG, "mRealPowerOff:" + true);
            return;
        }
        try {
            switch (param[0]) {
                case ProtocolAk47.TYPE_SECRET_RECEIVE:
                    switch (param[1]) {
                        case 0x4:
                            mBrakeSwitch = param[2];
                            setBrakeProp();
                            break;
                        case 0xa:
                            Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
                            it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.MCU_RETURN_LIGHT_DECTECT);
                            it.putExtra(MyCmd.EXTRA_COMMON_DATA, param[2]);
                            mContext.sendBroadcast(it);
                            break;
                    }
                    break;
                case ProtocolAk47.TYPE_COMMON_RECEIVE:
                    switch (param[1]) {
                        case ProtocolAk47.RECEVE_COMMON_KEY_INFO:
                            doKey((param[2] & 0xff));
                            break;
                        case ProtocolAk47.TYPE_SECRET_RECEIVE:

                            if (mBrake != param[2]) {
                                mBrake = param[2];
                                setBrakeProp();
                            }

                            if (mBrakeSwitch != 0) {
                                int brake = param[2];
                                if (brake == 0) {
                                    if (GlobalDefinition.mPreGPSBrake == 1) {
                                        brake = 1;
                                    } else if (CarUtil.mPreCanboxBrake == 1) {
                                        brake = 1;
                                    }
                                }
                                BroadcastUtil.sendByCarService(mContext, MyCmd.Cmd.MCU_BRAK_CAR_STATUS, brake);
                            }
                            if (GlobalDefinition.mIsTesting) {
                                if (mBrake == 1) {
                                    byte[] data = new byte[]{AutoTest.BRAKE_RETURN};
                                    AutoTest.parseTestData(data, data.length);
                                }
                            }
                            break;
                        case 3:
                            mRearL = param[2];
                            doRearSet();
                            break;
                        case 4:
                            if (Util.isRKSystem()) {
                                boolean on = param[3] == 4 || param[3] == 1;
                                CarUtil.setCanboxLED(CarUtil.CANBOX_LED_ILL, on ? 0 : 1);
                                if (GlobalDefinition.mIsTesting) {
                                    if (on) {
                                        byte[] data = new byte[]{AutoTest.ILL_RETURN};
                                        AutoTest.parseTestData(data, data.length);
                                    }
                                }
                                if (!mPowerOffFate) {
                                    Intent it = new Intent(MyCmd.BROADCAST_ILL_STATUS);
                                    it.putExtra(MyCmd.EXTRA_COMMON_DATA, on);
                                    mContext.sendBroadcast(it);
                                    BacklightPanel.doIllSwitch(on);
                                    sendIllToSuding(on);
                                }
                            }
                            break;
                        case 9:
                            mRearR = param[2];
                            doRearSet();
                            break;
                        case 8:
                            if (param[2] == 0x20) {//battery

                                if (GlobalDefinition.mMcuBatteryCell == 1) {
                                    Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND_SYSTEM_UI);
                                    it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.MCU_BATTERY);
                                    it.putExtra(MyCmd.EXTRA_COMMON_DATA, param[3]);
                                    it.setPackage("com.android.systemui");
                                    mContext.sendBroadcast(it);
                                }

                                if (OBDView.mNeedData) {
                                    Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
                                    it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.MCU_BATTERY);
                                    it.putExtra(MyCmd.EXTRA_COMMON_DATA, param[3]);
                                    mContext.sendBroadcast(it);
                                }
                            } else if (param[2] == 0x5) {
                                Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
                                it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.MCU_RETURN_VOLTAGE_PROTECT);
                                it.putExtra(MyCmd.EXTRA_COMMON_DATA, param[3]);
                                mContext.sendBroadcast(it);
                            } else if (param[2] == 0x10) {
                                Canbox canbox = CarUtil.getCanboxInstance();
                                if (canbox != null) {
                                    canbox.stopConnect();
                                }
                            } else if (param[2] == 0x11) {
                                Canbox canbox = CarUtil.getCanboxInstance();
                                if (canbox != null) {
                                    canbox.startConnect();
                                }
                            } else {
                                doAccPower(param[2]);
                            }

                            // doRealPowerOff();
                            break;
                        case 0x10:
                            if (GlobalDefinition.mIsTesting) {
                                if (param[2] == 0 && param[3] == 0) {
                                    byte[] data = new byte[]{AutoTest.ACC_RETURN};
                                    AutoTest.parseTestData(data, data.length);
                                }
                            }
                            break;
                        case 0xd:
                            mTinyRam[0] = param[2];
                            mTinyRam[1] = param[3];
                            mTinyRam[2] = param[4];
                            mTinyRam[3] = param[5];
                            mPowerKeySwitch = (mTinyRam[2] & 0x1) != 0;
                            break;
                        case 0xe:
                            updateSaveTime(((param[2] & 0xff) << 24) | ((param[3] & 0xff) << 16) | ((param[4] & 0xff) << 8) | ((param[5] & 0xff) << 0));
                            break;
                        case 0x14:
                            updateSaveTimeEx(param);
                            break;
                        case 0xf:
                            // mScreen0 = param[2];
                            break;
                        case 0x11:
                            mSupportPowerKey = true;
                            break;
                    }
                    break;
                case ProtocolAk47.TYPE_RADIO_RECEIVE:
                    if (param[1] == 0x2) {
                        if (GlobalDefinition.mIsTesting) {
                            MMLog.d("test_radio", GlobalDefinition.mIsTesting + "doMcuData:" + Util.byteArrayToHex(param));
                            byte[] cmd = new byte[]{AutoTest.RADIO_RETURN, param[6]};
                            AutoTest.parseTestData(cmd, cmd.length);
                        }
                    }
                case ProtocolAk47.TYPE_RDS_RECEIVE:

                    //				BroadcastUtil.sendByCarService(mContext,
                    //						AppConfig.PACKAGE_CAR_UI,
                    //						MyCmd.Cmd.MCU_RADIO_RECEIVE_DATA, param);
                    //				BroadcastUtil.sendByCarService(mContext,
                    //						AppConfig.getLauncherPackage(),
                    //						MyCmd.Cmd.MCU_RADIO_RECEIVE_DATA, param);

                    Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
                    it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.MCU_RADIO_RECEIVE_DATA);
                    it.putExtra(MyCmd.EXTRA_COMMON_DATA, param);
                    mContext.sendBroadcast(it);

                    break;

                case ProtocolAk47.TYPE_AUDIO_RECEIVE:
                    switch (param[1]) {
                        case ProtocolAk47.RECEVE_AUDIO_VOLUME_INFO:
                            if (mVolume != -1) {
                                if (!mPowerOffFate || (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON)) {
                                    if (!mHideVolumeUI) {
                                        BroadcastUtil.sendByCarService(mContext, mContext.getPackageName(), MyCmd.Cmd.MCU_AUDIO_RECEIVE_DATA, param);
                                    } else {
                                        VolumePanel.mCurrentVolume = param[2];
                                    }

                                    if (mHideVolumeUI) {
                                        Intent itVolume = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
                                        itVolume.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.RETURN_CURRENT_VOLUME);
                                        itVolume.putExtra(MyCmd.EXTRA_COMMON_DATA, param[2] & 0xff);
                                        mContext.sendBroadcast(itVolume);
                                    }
                                } else {
                                    VolumePanel.mCurrentVolume = param[2];
                                }

                                mHandleModeKey.post(new Runnable() {
                                    public void run() {
                                        updateSmallLcd(SOURCE_VOL_8600, null);
                                    }
                                });

                            } else {
                                VolumePanel.mCurrentVolume = param[2];
                            }

                            CarUtil.sendVolumeToCanbox(param[2]);

                            mVolume = param[2];
                            break;
                        case ProtocolAk47.RECEVE_AUDIO_MUTE_INFO:
                            mMute = (byte) (param[2] & 0x10);
                            break;
                        case 0xc:
                        case 0xe:
                        case 0xf:
                        case 0x10:
                        case 0x11:
                            BroadcastUtil.sendByCarService(mContext, AppConfig.PACKAGE_EQ, MyCmd.Cmd.MCU_AUDIO_RECEIVE_DATA, param);
                            mLoud = param[2];
                            break;
                        case 0xd: // spectrum
                            BroadcastUtil.sendByCarService(mContext, "com.android.deskclock", MyCmd.Cmd.RETURN_AUDIO_SPECTRUM, param);
                            break;
                        case ProtocolAk47.RECEVE_AUDIO_EQ_INFO:
                            if (!mDsp) {
                                mLoud = param[6];
                            }
                            BroadcastUtil.sendByCarService(mContext, AppConfig.PACKAGE_EQ, MyCmd.Cmd.MCU_AUDIO_RECEIVE_DATA, param);

                            BroadcastUtil.sendByCarService(mContext, AppConfig.getCarAppPackageName(mContext), MyCmd.Cmd.MCU_AUDIO_RECEIVE_DATA, param);

                            saveEQIndepend(param[2]);
                            if (CarUtil.mIsNeedSendEQ) {

                                byte[] data = (new byte[]{param[7], param[8], param[9], param[10], param[11], param[12], param[13], param[14], param[15], param[16], param[17], param[6], param[2]});
                                mMcuHandler.removeMessages(MSG_SEND_EQ_TO_CANBOX);
                                mMcuHandler.sendMessageDelayed(mMcuHandler.obtainMessage(MSG_SEND_EQ_TO_CANBOX, data), 300);

                                // CarUtil.sendEqToCanbox(new byte[] { param[7],
                                // param[8],
                                // param[9], param[10], param[11], param[12],
                                // param[13], param[14], param[15], param[16],
                                // param[17], param[6], param[2] });
                            }
                            break;
                        case 0xb:
                            updateDSP(param[2]);
                            break;
                    }

                    break;
                case ProtocolAk47.TYPE_CAN_RECEIVE:
                    // BroadcastUtil.sendByCarService(mContext,
                    // AppConfig.PACKAGE_CAR_UI,
                    // MyCmd.Cmd.MCU_CANBOX_RECEIVE_DATA, param);
                    if (!GlobalDefinition.mIsTesting) {
                        mCanService.canboxDataParser(param, param.length);
                    } else {
                        AutoTest.parseTestData(param, param.length);
                    }
                    break;
                case ProtocolAk47.TYPE_DVD_RECEIVE:
                    switch (param[1]) {
                        case 1:
                            mDVDPower = param[2];
                            break;
                        case 2:
                            doDvdStatusChange(param[2]);
                            break;

                    }
                    BroadcastUtil.sendByCarService(mContext, AppConfig.getCarAppPackageName(mContext), MyCmd.Cmd.MCU_DVD_RECEIVE_DATA, param);
                    break;
                case ProtocolAk47.TYPE_SETTINGS_RECEIVE:
                    if (param[1] == 0x11) {
                        int vcom = param[2] & 0xff;
                        if (param.length > 3) {
                            vcom = vcom | ((param[3] & 0xff) << 8);
                        }

                        BroadcastUtil.sendByCarService(mContext, "com.android.car.filemanager", MyCmd.Cmd.SET_VCOM, vcom);
                    }
                    break;
                case ProtocolAk47.TYPE_SWC_RECEIVE:
                    // case ProtocolAk47.TYPE_PANEL_KEY_SEND:
                    if (param[1] == 2 && GlobalDefinition.mIsTesting) {
                        byte[] data = new byte[]{AutoTest.SWC_RETURN};
                        AutoTest.parseTestData(data, data.length);
                    }
                    break;
            }
        } catch (Exception e) {
            MMLog.d(TAG, "doMcuData exception:" + Util.byte2HexStr(param));
        }
    }

    private boolean mDsp = false;
    private boolean mNodiskDvdPowerOn = false;

    private void doRearSet() {
        // if (mRearL == MyCmd.SOURCE_DVD || mRearR == MyCmd.SOURCE_DVD) {
        // if (mDVDStatus != DVD_STATUS_DISK_INSIDE
        // && mDVDStatus != DVD_STATUS_IN_ING) {
        // mNodiskDvdPowerOn = true;
        // }
        // dvdPowerOn();
        // } else {
        // if (mAppSource != MyCmd.SOURCE_DVD) {
        // // dvdPowerOff();
        // prepareDvdPowerOff();
        // }
        // }
    }

    private void doLockPowerKey(boolean time) {
        MMLog.d(TAG, "doLockPowerKey:" + time);
        mMcuHandler.removeMessages(MSG_UNLOCK_POWERKEY);
        if (time) {
            mMcuHandler.sendEmptyMessageDelayed(MSG_UNLOCK_POWERKEY, TIME_LOCK_POWER_KEY);
        }
        mLockPower = true;
    }

    private boolean mLockPower = false;
    private final static int MSG_UNLOCK_POWERKEY = 1001005;
    private final static int TIME_LOCK_POWER_KEY = 3000;
    private final Handler mMcuHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UNLOCK_POWERKEY:
                    mLockPower = false;
                    break;
                case Mcu.MSG_RECEIVE_MCU_DATA:
                    byte[] protocol = (byte[]) msg.obj;
                    doMcuData(protocol);
                    break;
                case MSG_DVD_POWER_OFF:
                    dvdPowerOff();
                    break;
                case MSG_DVD_EJECT:
                    doKeyEject();
                    break;
                case MSG_POWER_OFF_FAIL:
                    mRealPowerOff = false;
                    break;
                case MSG_DELAY_SET_ARM_LVDS:
                    Util.setFileValue(ARM_LVDS, msg.arg1);
                    break;
                case MSG_DELAY_SET_TV_LVDS:
                    Util.setFileValue(DTV_LVDS, msg.arg1);
                    break;
                case MSG_DELAY_SET_MCU_LVDS:
                    setMcuSceen0(msg.arg1);
                    break;
                case MSG_SEND_EQ_TO_CANBOX:
                    if (msg.obj != null) {
                        byte[] data = (byte[]) msg.obj;
                        CarUtil.sendEqToCanbox(data);
                    }
                    break;
                case MSG_FIRST_RUN_POWERON:
                    mLockKey = 0;
                    MMLog.d(TAG, "MSG_FIRST_RUN_POWERON:" + mAppSource + ":" + msg.arg1 + ":" + AppConfig.getTopActivity());
                    if (mAppSource == MyCmd.SOURCE_NONE) {
                        if (msg.arg1 < 40 && !AppConfig.getTopActivity().contains("com.android.settings.FallbackHome")) {
                            doPowerOn();
                            mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) 0x16, (byte) 0x0));
                        } else {
                            if (msg.arg1 < 40) {
                                mMcuHandler.removeMessages(MSG_FIRST_RUN_POWERON);
                                mMcuHandler.sendMessageDelayed(mMcuHandler.obtainMessage(MSG_FIRST_RUN_POWERON, msg.arg1 + 1, 0), 500);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    // common

    public int setMcuBTType(int i) {
        return mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_SETTINGS_SEND, (byte) 0x11, (byte) i));
    }

    public int setMcuBacklight(int i) {
        return mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_COMMON_SEND, ProtocolAk47.SEND_COMMON_SUB_BRIGHTNESS, (byte) i, (byte) i));//for kld px5&& 3566 pg
    }

    public int setMcuSource(int i) {
        return mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, ProtocolAk47.SEND_COMMON_SUB_FRONT_SOURCE, (byte) i));
    }

    public int setMcuSceen0(int i) {
        return mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, ProtocolAk47.SEND_COMMON_SUB_SCREEN0, (byte) i));
    }

    public void setVolume(int i) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_AUDIO_SEND, ProtocolAk47.SEND_VOLUME_SUB_VOLUME_CONTROL, ProtocolAk47.SEND_VOLUME_SUB_DATA1_SET_VOLUME, (byte) i));
    }

    public void setEQZoneUsed(int i) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_AUDIO_SEND, (byte) 0x2, (byte) 0x50, (byte) i));
    }

    public void setVoulumeMute(int i) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_AUDIO_SEND, ProtocolAk47.SEND_VOLUME_SUB_VOLUME_CONTROL, (byte) 1, (byte) i));
    }

    public void setVoulumeIncrease(boolean b) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_AUDIO_SEND, ProtocolAk47.SEND_VOLUME_SUB_VOLUME_CONTROL, (byte) (b ? 2 : 3), (byte) 0));
    }

    public void setVoulumeAutoMuteTime(int time) { // time is 100ms
        mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_AUDIO_SEND, ProtocolAk47.SEND_VOLUME_SUB_VOLUME_CONTROL, (byte) 0x4, (byte) time));
    }

    public void setMcuBtPhoneStatus(int i) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, ProtocolAk47.SEND_COMMON_SUB_BLUETOOTH, (byte) i));
    }

    // radio
    public void setRadio(int subId, int param1, int param2) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_RADIO_SEND, (byte) subId, (byte) param1, (byte) param2));
    }

    public void setRadio(int subId, int param1) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_RADIO_SEND, (byte) subId, (byte) param1));
    }

    public void setRadioRds(int subId, int param1, int param2) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_RDS_SEND, (byte) subId, (byte) param1, (byte) param2));
    }

    public void setRadioRds(int subId, int param1) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_RDS_SEND, (byte) subId, (byte) param1));
    }

    public void setAudio(int subId, int param1) {
        if (subId != 0xd) {
            mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_AUDIO_SEND, (byte) subId, (byte) param1));
        } else {
            byte[] protocol = new byte[]{ProtocolAk47.TYPE_AUDIO_SEND, (byte) subId, (byte) ((param1 & 0xff) << 0), (byte) ((param1 & 0xff00) >> 8), (byte) ((param1 & 0xff0000) >> 16), (byte) ((param1 & 0xff000000) >> 24)};
            mMcu.sendCmd(protocol);
        }
    }

    public void setAudio(int subId, int param1, int param2) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_AUDIO_SEND, (byte) subId, (byte) param1, (byte) param2));
    }

    public void setAudio(int subId, int param1, int param2, int param3) {
        byte[] protocol = new byte[]{ProtocolAk47.TYPE_AUDIO_SEND, (byte) subId, (byte) param1, (byte) param2, (byte) param3};
        mMcu.sendCmd(protocol);
    }

    public void setAudioGain(byte[] audio) {
        try {
            byte[] data = new byte[9];
            data[0] = ProtocolAk47.TYPE_AUDIO_SEND;
            data[1] = 0xc;
            data[2] = audio[0];
            data[3] = audio[1];
            data[4] = audio[2];
            data[5] = audio[3];
            data[6] = audio[4];
            data[7] = audio[5];
            data[8] = audio[6];

            mMcu.sendCmd(data);
        } catch (Exception ignored) {
        }
    }

    public void setSpectrumSwitch(byte i) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_AUDIO_SEND, (byte) 0xf, i));
    }

    public void queryVoulume() {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_AUDIO_SEND, (byte) 0x6, (byte) 0x4));
    }

    // dvd
    public int setDvd(int subId, int param1, int param2) {
        return mMcu.sendCmd(ProtocolAk47.generateProtocol1((byte) subId, (byte) param1, (byte) param2));
    }

    public void setDvd(int subId, int param1) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_DVD_SEND, (byte) subId, (byte) param1));
    }

    // dtv
    public void setTV(int param) {
        int subId = (param & 0xff000000) >> 24;
        if (subId == 0x3) {
            mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_TV_SEND, (byte) subId, (byte) ((param & 0xff00) >> 8), (byte) (param & 0xff)));
        } else {
            mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_TV_SEND, (byte) subId, (byte) (param & 0xff)));
        }
    }

    public void setTVEx(int param) {

        byte[] data = new byte[6];
        data[0] = ProtocolAk47.TYPE_TV_SEND;
        data[1] = 0x5;
        data[2] = (byte) ((param & 0xff000000) >> 24);
        data[3] = (byte) ((param & 0xff0000) >> 16);
        data[4] = (byte) ((param & 0xff00) >> 8);
        data[5] = (byte) ((param & 0xff) >> 0);

        mMcu.sendCmd(data);
    }

    // public void setDvd(int subId, int param1) {
    // mMcu.sendCmd(ProtocolAk47.generateProtocol1(
    // ProtocolAk47.TYPE_COMMON_SEND, (byte) subId, (byte) param1));
    // }

    public void setVCOM(int subId, int param1) {
        if (subId == 0) {
            mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_SETTINGS_SEND, (byte) 0x10, (byte) subId, (byte) param1));
        } else if (subId == 0x10) {
            mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_SETTINGS_SEND, (byte) 0x10, (byte) subId, (byte) param1));
        } else {
            mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_SETTINGS_SEND, (byte) 0x10, (byte) subId));
        }

    }

    public void setRadioAntPower(int power) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_RADIO_SEND, (byte) 0x5, (byte) power));

    }

    public void setAutoTest(int subId, int param1) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) subId, (byte) param1));
    }

    public void setSwcKeyStudy(int param1) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_SWC_SEND, (byte) 0x1, (byte) param1));
    }

    public void setSystemReadyWatchDog() {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, ProtocolAk47.SEND_COMMON_SUB_POWER, (byte) 0xff));
    }

    public void clearSystemReadyWatchDog() {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, ProtocolAk47.SEND_COMMON_SUB_POWER, (byte) 0xfe));
    }

    public static final int FRONT_CAMERA_POWER_USER1 = 1;
    private int mFCPowerUser = 0;

    public void setFrontCamerPower(byte data, int user) {
        //		Log.d("ffck", "setFrontCamerPower:" + data + ":" + user);
        if (data == 0) {
            mFCPowerUser &= ~user;
            if (mFCPowerUser == 0) {
                mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) 0x1c, data));
            }
        } else {
            mFCPowerUser |= user;
            mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) 0x1c, data));
        }
    }

    // do key
    public final static int LOCK_KEY_ALL = 1;
    public final static int LOCK_KEY_EXCEPTION_POWER = 2;
    private int mLockKey = LOCK_KEY_ALL;

    public void lockKey(int lock) {
        mLockKey = lock;
    }

    private final static int LOCK_KEY_TIME = 900;
    private long mStartPlayTime = 0;

    private void lockKeyTime() {
        mStartPlayTime = SystemClock.uptimeMillis();
    }

    private boolean isLockKeyTime() {
        if (((SystemClock.uptimeMillis() - mStartPlayTime) <= 0)) {
            mStartPlayTime = 0;
            return false;
        }

        if ((SystemClock.uptimeMillis() - mStartPlayTime) < LOCK_KEY_TIME) {
            Log.d(TAG, "lock!");
            return true;
        }
        return false;
    }

    private boolean isLockKeyTime(int key) {
        if (isNeedLockKeyTime(key)) {
            return isLockKeyTime();
        }
        return false;
    }

    private void lockKeyTime(int key) {
        if (isNeedLockKeyTime(key)) {
            lockKeyTime();
        }
    }

    private boolean isNeedLockKeyTime(int key) {
        switch (key) {
            case MyCmd.Keycode.HOME:
            case MyCmd.Keycode.AUDIO:
            case MyCmd.Keycode.KEY_TV:
            case MyCmd.Keycode.KEY_CAR_INFO:
            case MyCmd.Keycode.KEY_MEM_INFO:
            case MyCmd.Keycode.KEY_RECENT_APPS:
            case MyCmd.Keycode.EQ:
            case MyCmd.Keycode.VIDEO:
            case MyCmd.Keycode.BT_MUSIC:
            case MyCmd.Keycode.SETUP:
            case MyCmd.Keycode.AUX_IN:
            case MyCmd.Keycode.NAVIGATION:
            case MyCmd.Keycode.TIME_SETTING:
            case MyCmd.Keycode.RADIO:
            case MyCmd.Keycode.ALL_APP:
            case MyCmd.Keycode.KEY_MIC:
                return true;
        }
        return false;
    }

    private int dealMultiKey(int key) {

        if (key == MyCmd.Keycode.MULT_NEXT_AND_HANG) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT_HANG;
            } else {
                key = MyCmd.Keycode.NEXT;
            }
        } else if (key == MyCmd.Keycode.MULT_PREV_AND_RECEIVE) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT_DIAL;
            } else {
                key = MyCmd.Keycode.PREVIOUS;
            }
        }
        if (key == MyCmd.Keycode.MULT_NEXT_AND_RECEIVE) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT_DIAL;
            } else {
                key = MyCmd.Keycode.NEXT;
            }
        } else if (key == MyCmd.Keycode.MULT_PREV_AND_HANG) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT_HANG;
            } else {
                key = MyCmd.Keycode.PREVIOUS;
            }
        } else if (key == MyCmd.Keycode.MULT_SOURCE_AND_BT) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT;
            } else {
                key = MyCmd.Keycode.MODLE;
            }
        } else if (key == MyCmd.Keycode.KEY_JOY_HOME) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT_DIAL;
            } else {
                key = MyCmd.Keycode.HOME;
            }
        } else if (key == MyCmd.Keycode.KEY_JOY_BACK) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT_HANG;
            } else {
                key = MyCmd.Keycode.BACK;
            }
        } else if (key == MyCmd.Keycode.MULT_BACK_AND_HANG) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT_HANG;
            } else {
                key = MyCmd.Keycode.BACK;
            }
        } else if (key == MyCmd.Keycode.MULT_SPEECH_AND_BT) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT;
            } else {
                key = MyCmd.Keycode.SPEECH;
            }
        } else if (key == MyCmd.Keycode.MULT_MUTE_AND_HANG) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT_HANG;
            } else {
                key = MyCmd.Keycode.MUTE;
            }
        } else if (key == MyCmd.Keycode.MULT_MUTE_AND_BT) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT;
            } else {
                key = MyCmd.Keycode.MUTE;
            }
        } else if (key == MyCmd.Keycode.MULT_SEEK_PRE_HANG) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT_HANG;
            } else {
                key = MyCmd.Keycode.KEY_SEEK_PREV;
            }
        } else if (key == MyCmd.Keycode.MULT_SEEK_NEXT_RECV) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT_DIAL;
            } else {
                key = MyCmd.Keycode.KEY_SEEK_NEXT;
            }
        } else if (key == MyCmd.Keycode.MULT_MODE_AND_RECV) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                key = MyCmd.Keycode.BT_DIAL;
            } else {
                key = MyCmd.Keycode.MODLE;
            }
        }

        return key;
    }

    private int doKeyType(int key) {
        if (GlobalDefinition.mPannelKeyType == 1) {
            if (key == MyCmd.Keycode.UP || key == MyCmd.Keycode.DOWN || key == MyCmd.Keycode.LEFT || key == MyCmd.Keycode.RIGHT || key == MyCmd.Keycode.ENTER) {
                String s = AppConfig.getTopActivity();
                if (AppConfig.CAR_UI_DVD.equals(s) || AppConfig.CAR_UI_AUDIO.equals(s) || AppConfig.CAR_UI_VIDEO.equals(s) || AppConfig.CAR_UI_RADIO.equals(s) || AppConfig.CAR_UI_BT_MUSIC.equals(s)) {

                    switch (key) {
                        case MyCmd.Keycode.UP:
                        case MyCmd.Keycode.LEFT:
                            key = MyCmd.Keycode.PREVIOUS;
                            break;
                        case MyCmd.Keycode.DOWN:
                        case MyCmd.Keycode.RIGHT:
                            key = MyCmd.Keycode.NEXT;
                            break;
                        case MyCmd.Keycode.ENTER:
                            key = MyCmd.Keycode.PLAY_PAUSE;
                            break;
                    }
                }
            }
        }
        return key;
    }

    private void checkWakeLock(int key) {
        switch (key) {
            case MyCmd.Keycode.HOME:
            case MyCmd.Keycode.BACK:
            case MyCmd.Keycode.MENU:
            case MyCmd.Keycode.AUDIO:
            case MyCmd.Keycode.KEY_TV:
            case MyCmd.Keycode.KEY_CAR_INFO:
            case MyCmd.Keycode.KEY_CAR_SETTING:
            case MyCmd.Keycode.KEY_MEM_INFO:
            case MyCmd.Keycode.KEY_RECENT_APPS:
            case MyCmd.Keycode.EQ:
            case MyCmd.Keycode.KEY_EQ_SEL:
            case MyCmd.Keycode.VIDEO:
            case MyCmd.Keycode.BT_MUSIC:
            case MyCmd.Keycode.SETUP:
            case MyCmd.Keycode.AUX_IN:
            case MyCmd.Keycode.NAVIGATION:
            case MyCmd.Keycode.AS:
            case MyCmd.Keycode.KEY_AM:
            case MyCmd.Keycode.KEY_FM:
            case MyCmd.Keycode.TIME_SETTING:
            case MyCmd.Keycode.RADIO:
            case MyCmd.Keycode.KEY_RADIO_ONLY:
            case MyCmd.Keycode.ALL_APP:
            case MyCmd.Keycode.KEY_MIC:
            case MyCmd.Keycode.POWER:
            case MyCmd.Keycode.KEY_LOUDNESS:
            case MyCmd.Keycode.KEY_CUSTOM_APP:
            case MyCmd.Keycode.DVD:
                GlobalDefinition.wakeLockOnce();
                break;
        }
    }

    public int doKey(int key) {

        int ret = 0;

        if (mLockPower) {
            //if (key == MyCmd.Keycode.POWER) {
            Log.d(TAG, "doKeyPower lock");
            return 0;
            //}
        }

        if (key >= MyCmd.Keycode.CANBOX_FUNCTION_START && key <= MyCmd.Keycode.CANBOX_FUNCTION_END) {
            Canbox canbox = CarUtil.getCanboxInstance();
            if (canbox != null) {
                canbox.doCanboxFunctionKey((byte) key);
            }
        }

        key = doKeyType(key);
        key = dealMultiKey(key);

        //	Log.d(TAG, ">>doKey:" + key);
        if ((mLockKey == LOCK_KEY_ALL) || (mLockKey == LOCK_KEY_EXCEPTION_POWER && key != MyCmd.Keycode.POWER && key != (MyCmd.Keycode.POWER_ON & 0xff))) {
            if (MyCmd.Keycode.BACK == key) {
                ReverseManager.toggleFrontCmaera();
            }

            if (MyCmd.Keycode.IXB_360_DISPLAY != key) {
                if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON && (key == MyCmd.Keycode.BT || key == MyCmd.Keycode.BT_DIAL || key == MyCmd.Keycode.BT_HANG || key == MyCmd.Keycode.VOLUME_DOWN || key == MyCmd.Keycode.VOLUME_UP)) {
                } else {
                    //	Log.d(TAG, "doKey: lock by" + key);
                    return -1;
                }
            }
        }

        Log.d(TAG, "doKey:" + key);
        // checkWakeLock(key);
        GlobalDefinition.wakeLockOnce();
        if (GlobalDefinition.mIsTesting) {
            Log.d(TAG, "doKey: GlobalDef.mIsTesting" + GlobalDefinition.mIsTesting);
            return 0;
        }

        if (!mBacklightStatus && !mPowerOffFate) {
            if (key != MyCmd.Keycode.BRIGHTNESS) {
                doBacklight(true);
                return 0;
            }
        }

        if (isLockKeyTime(key)) {
            return 0;
        }
        lockKeyTime(key);

        switch ((byte) key) {
            case MyCmd.Keycode.MUTE:
            case MyCmd.Keycode.MULT_MUTE_AND_POWER:
                doKeyMute();
                break;
            case MyCmd.Keycode.VOLUME_DOWN:
                //mContext.sendBroadcast(new Intent(
                //MyCmd.BROADCAST_ACC_DELAY_POWER_OFF));
                //MyService.testGPSSpeedp(10);
                setVoulumeIncrease(false);
                break;
            case MyCmd.Keycode.VOLUME_UP:
                //			MyService.testGPSSpeedp(40);
                setVoulumeIncrease(true);
                break;
            case MyCmd.Keycode.HOME:
                // killAllNoSystemProcess();
                Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);
                break;
            case MyCmd.Keycode.BACK:
                // doAccPowerOnFate();
                if (!ReverseManager.toggleFrontCmaera()) {
                    Kernel.doKeyEvent(Kernel.KEY_BACK);
                }
                break;
            case MyCmd.Keycode.MENU:
                if (AppConfig.CAR_DTV.equals(AppConfig.getTopActivity())) {
                    ret = key;
                } else {
                    Kernel.doKeyEvent(Kernel.KEY_MENU);
                }
                break;
            case MyCmd.Keycode.AUDIO:
                UtilCarKey.doKeyAudio(mContext);
                break;
            case MyCmd.Keycode.KEY_TV:
                UtilCarKey.doKeyTV(mContext);
                break;
            case MyCmd.Keycode.DVR:
                UtilCarKey.doKeyDVR(mContext);
                break;
            case MyCmd.Keycode.EASY_CONNECT:
                UtilSystem.doRunActivity(mContext, "net.easyconn", "net.easyconn.ui.Sv04MainActivity");
                break;
            case MyCmd.Keycode.KEY_F_CAMERA:
                UtilSystem.doRunActivity(mContext, "com.car.ui", "com.android.car.frontcamera.FrontCameraActivity");
                break;
            case MyCmd.Keycode.KEY_CAMERA:
                UtilSystem.doRunActivity(mContext, "com.car.ui", "com.android.car.frontcamera.BackCameraActivity");
                break;
            case MyCmd.Keycode.KEY_SIDE_CAMERA:
                UtilSystem.doRunActivity(mContext, "com.car.ui", "com.android.car.frontcamera.SideCameraActivity");
                break;
            case MyCmd.Keycode.KEY_CAR_INFO:
                try {
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setClassName("com.canboxsetting", "com.canboxsetting.CarInfoActivity");
                    it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    it.putExtra(MyCmd.EXTRA_COMMON_ID, 1);
                    mContext.startActivity(it);
                } catch (Exception e) {
                    Log.e(TAG, String.valueOf(e));
                }
                break;
            case MyCmd.Keycode.KEY_CAR_SETTING:
                UtilSystem.doRunActivity(mContext, "com.canboxsetting", "com.canboxsetting.MainActivity");
                break;
            case MyCmd.Keycode.KEY_MEM_INFO:
                UtilSystem.doRunActivity(mContext, "android.settings.INTERNAL_STORAGE_SETTINGS");
                break;
            case MyCmd.Keycode.KEY_RECENT_APPS:
                GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SHOW_RECENT_APPS);
                break;
            case MyCmd.Keycode.KEY_SCREENT_SHOT:
                GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.DO_SCREEN_SHOT);
                break;
            case MyCmd.Keycode.KEY_SPLIT_WINDOW:
                GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SYSTEMUI_LONG_PRESS_RECENT);
                break;
            case MyCmd.Keycode.KEY_SPEED:
            case MyCmd.Keycode.SPEED_UP:
                GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SYSTEMUI_SPEED_UP);
                break;
            case MyCmd.Keycode.KEY_CHECK:
                // UtilSystem.doRunActivity(mContext,
                // "android.settings.DEVICE_INFO_SETTINGS");
                break;
            // case MyCmd.Keycode.BT_DIAL: //bt apk do this key now
            // case MyCmd.Keycode.BT:
            // UtilCarKey.doKeyBT(mContext);
            // ret = key;
            // break;
            case MyCmd.Keycode.EQ:
                try {
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setClassName("com.eqset", "com.eqset.EQActivity");
                    it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    it.putExtra("switch", 1);
                    mContext.startActivity(it);
                } catch (Exception e) {
                    Log.e(TAG, String.valueOf(e));
                }

                break;
            //		case MyCmd.Keycode.KEY_CAMERA:
            //			try {
            //				Intent it = new Intent(Intent.ACTION_VIEW);
            //				it.setClassName("com.car.ui", "com.android.car.frontcamera.FrontCameraActivity");
            //				it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            //						| Intent.FLAG_ACTIVITY_NEW_TASK);
            //				mContext.startActivity(it);
            //			} catch (Exception e) {
            //				Log.e(TAG, e.getMessage());
            //			}

            //			break;
            case MyCmd.Keycode.KEY_EQ_SEL:

                if (!AppConfig.CAR_EQ.equals(AppConfig.getTopActivity())) {
                    UtilSystem.doRunActivity(mContext, "com.eqset", "com.eqset.EQActivity");
                } else {
                    BroadcastUtil.sendByCarService(mContext, AppConfig.PACKAGE_EQ, MyCmd.Cmd.EQ_QUIT);
                }
                break;
            case MyCmd.Keycode.KEY_EQ_MODE:
                swtichEQMode();
                break;
            case MyCmd.Keycode.VIDEO:
                UtilCarKey.doKeyVideo(mContext);
                break;
            case MyCmd.Keycode.BT_MUSIC:
                if (!mBtMusicInBTapk) {
                    UtilCarKey.doKeyBTMusic(mContext);
                } else {
                    UtilCarKey.doKeyBTMusicInBT(mContext);
                }
                break;
            case MyCmd.Keycode.SETUP:
                UtilCarKey.doKeySet(mContext);
                break;
            case MyCmd.Keycode.AUX_IN:
                UtilCarKey.doKeyAuxIn(mContext);
                break;
            case MyCmd.Keycode.NAVIGATION:
                //UtilCarKey.doKeyGps(mContext);
                break;
            case MyCmd.Keycode.AS:
                UtilCarKey.doKeyRadio(mContext);
                ret = key;
                break;
            case MyCmd.Keycode.KEY_AM: {
                try {
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setClassName(AppConfig.getCarAppPackageName(mContext), "com.android.car.radio.RadioActivity");
                    it.putExtra("amfm", (byte) 3);
                    it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(it);
                } catch (Exception e) {
                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                }
            }
            break;
            case MyCmd.Keycode.KEY_FM: {
                try {
                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setClassName(AppConfig.getCarAppPackageName(mContext), "com.android.car.radio.RadioActivity");
                    it.putExtra("amfm", (byte) 0);
                    it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(it);
                } catch (Exception e) {
                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                }
            }
            break;
            case MyCmd.Keycode.TIME_SETTING:
                //			UtilSystem.doRunActivity(mContext, Settings.ACTION_DATE_SETTINGS);
                UtilSystem.doRunActivity(mContext, "com.android.deskclock", "com.android.deskclock.DeskClock");
                break;
            case MyCmd.Keycode.RADIO:
                if (!UtilCarKey.doKeyRadio(mContext)) {
                    ret = key;
                }
                break;
            case MyCmd.Keycode.KEY_RADIO_ONLY:
                UtilCarKey.doKeyRadio(mContext);
                break;
            case MyCmd.Keycode.ALL_APP: {

                int delay = 0;
                // if (!"com.android.launcher/com.android.launcher2.Launcher"
                // .equals(AppConfig.getTopActivity())) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.putExtra("allApp", 1);
                mContext.startActivity(intent);
                //this for old method, no used in new method.
                delay = 800;
                // }
                mHandleModeKey.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BroadcastUtil.sendByCarService(mContext, "com.android.launcher", MyCmd.Cmd.LAUNCHER_SHOW_ALL_APP);
                    }
                }, delay);
            }
            break;
            case MyCmd.Keycode.KEY_MIC:
            case MyCmd.Keycode.SPEECH:
            case MyCmd.Keycode.MULT_SPEECH_MODE:
                UtilCarKey.doKeyMic(mContext);
                break;
            case MyCmd.Keycode.KEY_SPEECH_CARPLAY:
                doSpeechCarPlay();
                break;
            case MyCmd.Keycode.POWER:
                doKeyPower();
                break;
            case MyCmd.Keycode.POWER_ON:
                doKeyPower(false);
                break;
            case MyCmd.Keycode.POWER_OFF:
                doKeyPower(true);
                break;
            case MyCmd.Keycode.MODLE:
                doKeyMode();
                break;
            case MyCmd.Keycode.SCREEN_BRIGHTNESS:
            case MyCmd.Keycode.BRIGHTNESS:
                doKeyBrightness();
                break;
            case MyCmd.Keycode.DARK:
                doKeyBacklight();
                break;
            case MyCmd.Keycode.BACKLIGHT_ON:
                doBacklight(true);
                break;
            case MyCmd.Keycode.KEY_DISPLAY:
            case MyCmd.Keycode.BACKLIGHT_OFF:
                doBacklight(false);
                break;
            case MyCmd.Keycode.KEY_LOUDNESS:
                doKeyLoudness();

                break;
            case MyCmd.Keycode.KEY_CUSTOM_APP:
                doKeyCustomApp();
                break;
            case MyCmd.Keycode.DVD:
                if (!GlobalDefinition.mIsUSBDvd) {
                    UtilCarKey.doKeyDVD(mContext);
                } else {
                    if (!(AppConfig.getTopActivity().contains(AppConfig.USB_DVD))) {
                        UtilSystem.doRunActivity(mContext, "com.car.dvdplayer", AppConfig.USB_DVD);
                    }
                }
                break;
            case MyCmd.Keycode.EJECT:
                // Toast.makeText(mContext, R.string.key_not_support,
                // Toast.LENGTH_SHORT).show();
                // doKeyEject();
                //			if (!GlobalDef.mIsUSBDvd) {
                prepareKeyEject();
                //			} else {
                BroadcastUtil.sendKey(mContext, "com.car.dvdplayer", key);
                //			}

                break;
            case MyCmd.Keycode.UP:
            case MyCmd.Keycode.DOWN:
            case MyCmd.Keycode.LEFT:
            case MyCmd.Keycode.RIGHT:
            case MyCmd.Keycode.ENTER:
                if (AppConfig.CAR_DTV.equals(AppConfig.getTopActivity())) {
                    ret = key;
                } else {
                    doKernelKey(key);
                }
                break;
            // case MyCmd.Keycode.KEY_DVD_UP:
            // if (!AppConfig.CAR_UI_DVD.equals(AppConfig.getTopActivity())) {
            // key = MyCmd.Keycode.NEXT;
            // }
            // ret = key;
            // break;
            // case MyCmd.Keycode.KEY_DVD_DOWN:
            // if (!AppConfig.CAR_UI_DVD.equals(AppConfig.getTopActivity())) {
            // key = MyCmd.Keycode.PREVIOUS;
            // }
            // ret = key;
            // break;
            // case MyCmd.Keycode.KEY_DVD_LEFT:
            // if (!AppConfig.CAR_UI_DVD.equals(AppConfig.getTopActivity())) {
            // key = MyCmd.Keycode.KEY_TURN_D;
            // }
            // ret = key;
            // break;
            // case MyCmd.Keycode.KEY_DVD_RIGHT:
            // if (!AppConfig.CAR_UI_DVD.equals(AppConfig.getTopActivity())) {
            // key = MyCmd.Keycode.KEY_TURN_A;
            // }
            // ret = key;
            // break;
            case MyCmd.Keycode.KEY_JOY_UP:
            case MyCmd.Keycode.KEY_JOY_DOWN:
            case MyCmd.Keycode.KEY_JOY_LEFT:
            case MyCmd.Keycode.KEY_JOY_RIGHT:
            case MyCmd.Keycode.KEY_JOY_ENTER:
            case MyCmd.Keycode.KEY_JOY_ROLL_LEFT:
            case MyCmd.Keycode.KEY_JOY_ROLL_RIGHT:
                ret = key = doJoyKey(key);
                break;
            case MyCmd.Keycode.IXB_VOICE: {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.MEDIA_MIC_BUTTON");
                mContext.sendBroadcast(intent);
            }
            break;
            case MyCmd.Keycode.IXB_360_DISPLAY: {
                CarUtil.sendDataToCanbox(new byte[]{(byte) 0xcc, 0x1, 0x1});
            }
            break;
            case MyCmd.Keycode.NISSIAN_360:
                Nissian360ButtonView.send360Key();
                break;
            case MyCmd.Keycode.DVD_FORCE_EJECT:
                setDvd(1, 0xf);
                break;
            case MyCmd.Keycode.KEY_360:
                doKeyCommon360();
                break;
            case MyCmd.Keycode.KEY_AIR_CONTROL:
                toggleAC(0);
                break;
            case MyCmd.Keycode.CANBOX_OPEN_AC_VIEW:
                toggleAC(1);
                break;
            /// case MyCmd.Keycode.CANBOX_AC_OFF:
            ///  	toggleAC(2);
            ///		break;
            default:
                ret = key;
                break;
        }

        if (ret != 0) {// if it is not car application, we send the Andoird next
            // song to it.
            if (!isOwerAppControlSource()) {
                switch (key) {
                    case MyCmd.Keycode.CH_UP:
                    case MyCmd.Keycode.KEY_SEEK_NEXT:
                    case MyCmd.Keycode.KEY_TURN_A:
                    case MyCmd.Keycode.NEXT:
                    case MyCmd.Keycode.KEY_DVD_UP:
                    case MyCmd.Keycode.KEY_DVD_RIGHT:
                        key = (byte) Kernel.KEY_NEXTSONG;
                        ret = 0;
                        break;
                    case MyCmd.Keycode.CH_DOWN:
                    case MyCmd.Keycode.KEY_SEEK_PREV:
                    case MyCmd.Keycode.KEY_TURN_D:
                    case MyCmd.Keycode.PREVIOUS:
                    case MyCmd.Keycode.KEY_DVD_DOWN:
                    case MyCmd.Keycode.KEY_DVD_LEFT:
                        key = (byte) Kernel.KEY_PREVIOUSSONG;
                        ret = 0;
                        break;
                    case MyCmd.Keycode.PLAY_PAUSE:
                        key = (byte) Kernel.KEY_PLAYPAUSE;
                        ret = 0;
                        break;
                }
            }

            if (ret == 0) {
                Kernel.doKeyEventEx(key, 50);
            }
        }

        if (ret != 0) {
            // BroadcastUtil.sendKey(mContext, key);
            broadcasKeyToApplication(key);
        }
        return ret;
    }

    private void toggleAC(int action) {
        if (CarUtil.isShowAC()) {
            boolean top = "com.canboxsetting/com.canboxsetting.CanAirControlActivity".equals(AppConfig.getTopActivity());

            if ((top && action == 1) || (!top && action == 2)) {
                return;
            }

            Intent it = new Intent(Intent.ACTION_VIEW);
            it.setClassName("com.canboxsetting", "com.canboxsetting.CanAirControlActivity");
            it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

            if ((action == 0 && top) || action == 2) {
                it.putExtra("finish", 1);
            }

            try {
                mContext.startActivity(it);
            } catch (Exception e) {
                Log.e(TAG, String.valueOf(e));
            }

        }
    }

    private void doKeyCommon360() {
        Log.d(TAG, "doKeyCommon360 to do!!:");
    }

    private void broadcasKeyToApplication(int key) {
        boolean send = false;
        switch (key) {
            case MyCmd.Keycode.NUMBER0:
            case MyCmd.Keycode.NUMBER1:
            case MyCmd.Keycode.NUMBER2:
            case MyCmd.Keycode.NUMBER3:
            case MyCmd.Keycode.NUMBER4:
            case MyCmd.Keycode.NUMBER5:
            case MyCmd.Keycode.NUMBER6:
            case MyCmd.Keycode.NUMBER7:
            case MyCmd.Keycode.NUMBER8:
            case MyCmd.Keycode.NUMBER9:
                if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                    BroadcastUtil.sendKey(mContext, "com.android.car.bt", key);
                    send = true;
                }
                break;

        }
        if (!send) {
            if (mAppSource == MyCmd.SOURCE_DAB && isDABControlKey(key)) {
                BroadcastUtil.sendKey(mContext, "com.car.dabplayer", key);
            } else {
                BroadcastUtil.sendKey(mContext, key);
            }

        }
    }

    private boolean isDABControlKey(int key) {
        boolean ret = false;
        switch (key) {
            case MyCmd.Keycode.FAST_F:
            case MyCmd.Keycode.KEY_SEEK_NEXT:
            case MyCmd.Keycode.KEY_TURN_A:
            case MyCmd.Keycode.CH_UP:
            case MyCmd.Keycode.NEXT:
            case MyCmd.Keycode.FAST_R:
            case MyCmd.Keycode.KEY_SEEK_PREV:
            case MyCmd.Keycode.KEY_TURN_D:
            case MyCmd.Keycode.CH_DOWN:
            case MyCmd.Keycode.PREVIOUS:
            case MyCmd.Keycode.AS:
            case MyCmd.Keycode.KEY_RADIO_PS:
            case MyCmd.Keycode.KEY_RADIO_SCAN:
                ret = true;
                break;
        }
        return ret;
    }

    private boolean isOwerAppControlSource() {
        return mAppSource == MyCmd.SOURCE_RADIO || mAppSource == MyCmd.SOURCE_DVD || mAppSource == MyCmd.SOURCE_AUX || mAppSource == MyCmd.SOURCE_DTV || mAppSource == MyCmd.SOURCE_DTV_CVBS || mAppSource == MyCmd.SOURCE_BT_MUSIC || mAppSource == MyCmd.SOURCE_MUSIC || mAppSource == MyCmd.SOURCE_VIDEO || mAppSource == MyCmd.SOURCE_DVR || mAppSource == MyCmd.SOURCE_DAB || mAppSource == MyCmd.SOURCE_USBDVD;
    }

    private final static String[] USE_JOY_APPLICATION = {"com.car.ui", "com.android.car.bt",
            /*"net.easyconn", "com.android.launcher"*/};

    private boolean isJoyApplicationTop() {
        if (Util.isRKSystem()) {
            return false;
        } else {
            String top = AppConfig.getTopActivity();
            if (top != null) {
                for (String s : USE_JOY_APPLICATION) {
                    if (top.startsWith(s)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int doJoyKey(int key) {
        int ret = 0;

        switch (key) {
            case MyCmd.Keycode.KEY_JOY_LEFT:
                setVoulumeIncrease(false);
                break;
            case MyCmd.Keycode.KEY_JOY_RIGHT:
                setVoulumeIncrease(true);
                break;
            case MyCmd.Keycode.KEY_JOY_UP:
                if (isOwerAppControlSource()) {
                    ret = key = MyCmd.Keycode.KEY_SEEK_PREV;
                }
                break;
            case MyCmd.Keycode.KEY_JOY_DOWN:
                if (isOwerAppControlSource()) {
                    ret = key = MyCmd.Keycode.KEY_SEEK_NEXT;
                }
                break;
            case MyCmd.Keycode.KEY_JOY_ENTER:
                // if(isJoyApplicationTop()){
                // ret = key;
                // } else {
                doKernelKey(MyCmd.Keycode.ENTER);
                // }
                break;
            case MyCmd.Keycode.KEY_JOY_ROLL_LEFT:
                if (!VolumePanel.mShown) {
                    if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {

                        Kernel.doKeyEvent(Kernel.KEY_LEFTSHIFT, 1);
                        Kernel.doKeyEvent(Kernel.KEY_TAB);
                        Kernel.doKeyEvent(Kernel.KEY_LEFTSHIFT, 0);

                    } else {
                        if (isJoyApplicationTop()) {
                            // ret = key;
                            Kernel.doKeyEvent(Kernel.KEY_F7);
                        } else {
                            Kernel.doKeyEvent(Kernel.KEY_LEFTSHIFT, 1);
                            Kernel.doKeyEvent(Kernel.KEY_TAB);
                            Kernel.doKeyEvent(Kernel.KEY_LEFTSHIFT, 0);
                        }
                    }
                } else {
                    setVoulumeIncrease(false);
                }
                break;
            case MyCmd.Keycode.KEY_JOY_ROLL_RIGHT:
                if (!VolumePanel.mShown) {
                    if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {

                        Kernel.doKeyEvent(Kernel.KEY_TAB);

                    } else {
                        if (isJoyApplicationTop()) {
                            // ret = key;
                            Kernel.doKeyEvent(Kernel.KEY_F8);
                        } else {
                            Kernel.doKeyEvent(Kernel.KEY_TAB);
                        }
                    }
                } else {
                    setVoulumeIncrease(true);
                }
                break;
        }

        if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
            switch (key) {
                case MyCmd.Keycode.KEY_JOY_UP:
                case MyCmd.Keycode.KEY_JOY_DOWN:
                case MyCmd.Keycode.KEY_JOY_ENTER:
                    BroadcastUtil.sendKey(mContext, "com.android.car.bt", key);
                    break;

            }
        }
        return ret;
    }

    private void doKernelKey(int key) {

        switch (key) {
            case MyCmd.Keycode.UP:
                key = Kernel.KEY_UP;
                break;
            case MyCmd.Keycode.DOWN:
                key = Kernel.KEY_DOWN;
                break;
            case MyCmd.Keycode.LEFT:
                key = Kernel.KEY_LEFT;
                break;
            case MyCmd.Keycode.RIGHT:
                key = Kernel.KEY_RIGHT;
                break;
            case MyCmd.Keycode.ENTER:
                key = Kernel.KEY_ENTER;
                break;
        }
        Kernel.doKeyEvent(key);
    }

    private final byte mMuteVolume = 0;

    private void doKeyMute() {

        if (mMute == 0 || mVolume > 0) {
            setVoulumeMute(0);
        } else {
            setVoulumeMute(1);
        }
    }

    private boolean mDvdEjecting = false;

    private void doKeyEject() {
        setDvd(1, 3);
        dvdPowerOff();
        mDvdEjecting = false;
    }

    private void prepareKeyEject() {
        if (mDVDStatus == DVD_STATUS_DISK_IN_ENTRY) {
            setDvd(1, 4);
        } else {
            if (mDVDPower != 0) {
                Util.setFileValue("/sys/class/misc/ak-dvd/device/data", "0x0212");
                Util.doSleep(5);
                Util.setFileValue("/sys/class/misc/ak-dvd/device/data", "0x0212");

                Util.doSleep(5);
                Util.setFileValue("/sys/class/misc/ak-dvd/device/data", "0x0300"); // save play status

            }
            if (!mDvdEjecting) {
                mDvdEjecting = true;
                mMcuHandler.sendEmptyMessageDelayed(MSG_DVD_EJECT, TIME_DVD_EJECT);
            }
        }
    }

    // mode key start
    private Toast mToastModeKey;
    // private Timer mTimerModeKey;
    private TextView mTextViewMode;

    static class mModeKeyConfig {
        public int key;
        public int text;

        public mModeKeyConfig(byte k, int t) {
            // source = s;
            key = k;
            text = t;
        }
    }

    @SuppressLint("InflateParams")
    private Toast makeModeToast() {
        Toast t = new Toast(mContext);

        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.source_key, null);
        mTextViewMode = v.findViewById(R.id.mode_text);

        t.setView(v);
        t.setDuration(Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        return t;
    }

    private final List<mModeKeyConfig> mListModeKey = new ArrayList<mModeKeyConfig>();

    private boolean initModeKeyContent() {
        if (mToastModeKey != null) {
            String s = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_MODE_KEY_CONTENT);
            if (s != null) {
                if (s.contains("radio")) {
                    mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.KEY_RADIO_ONLY, R.string.button_text_radio));
                }
                if (s.contains("navi")) {
                    //mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.NAVIGATION,R.string.button_text_navi));
                }
                if (s.contains("bt")) {
                    mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.BT, R.string.button_text_bt));
                }
                if (s.contains("audio")) {
                    mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.AUDIO, R.string.button_text_music));
                }
                if (s.contains("video")) {
                    mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.VIDEO, R.string.button_text_video));
                }
                if (s.contains("bt_music")) {
                    mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.BT_MUSIC, R.string.button_text_bt_music));
                }
                if (s.contains("aux")) {
                    mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.AUX_IN, R.string.button_text_aux));
                }

                if (s.contains("dvd")) {
                    AppConfig.updateHideAppConfig();
                    if (!GlobalDefinition.mIsUSBDvd) {
                        if (!AppConfig.isHidePackage("com.android.car.dvd.DVDPlayer")) {
                            mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.DVD, R.string.button_text_dvd));
                        }
                    } else {
                        mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.DVD, R.string.button_text_dvd));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void initModeKeyToast(boolean force) {
        if (mToastModeKey == null || force) {

            mToastModeKey = makeModeToast();
            // init mode key list
            mListModeKey.clear();
            if (!initModeKeyContent()) {
                mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.KEY_RADIO_ONLY, R.string.button_text_radio));

                //mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.NAVIGATION,R.string.button_text_navi));

                mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.BT, R.string.button_text_bt));

                mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.AUDIO, R.string.button_text_music));

                mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.VIDEO, R.string.button_text_video));

                AppConfig.updateHideAppConfig();
                if (!GlobalDefinition.mIsUSBDvd) {
                    if (!AppConfig.isHidePackage("com.android.car.dvd.DVDPlayer")) {
                        mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.DVD, R.string.button_text_dvd));
                    }
                } else {
                    mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.DVD, R.string.button_text_dvd));
                }

                mListModeKey.add(new mModeKeyConfig(MyCmd.Keycode.AUX_IN, R.string.button_text_aux));

                // mListModeKey.add(new mModeKeyConfig(MyCmd.SOURCE_DTV,
                // AkKeypad.MKP_KEY_DTV, R.string.button_text_tv));
            }
        }
    }

    private final static int MSG_AFTER_FATE_POWEROFF = 1;
    private final static int MSG_ANDROID_POWER_OFF = 2;
    private final static int MSG_ACC_POWEROFF = 3;

    private final static int MSG_ACC_POWEROFF_PX5 = 4;

    private final static int MSG_ACC_POWERON_PX5 = 6;

    private final static int MSG_RECOVER_BT = 5;


    private final static int MSG_UPDATE_8600_LCD = 10;
    private final static int MSG_UPDATE_8600_LCD_TIME = 11;

    private final Handler mHandleModeKey = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        @Override
        public void handleMessage(Message msg) {
            MMLog.d(TAG, "mHandleModeKey:" + msg.what);
            switch (msg.what) {
                case 0:
                    doKey(mListModeKey.get(mModeIndex).key);
                    break;
                case MSG_AFTER_FATE_POWEROFF:
                    mLockKey = LOCK_KEY_EXCEPTION_POWER;
                    if (!mRealPowerOff) {
                        if (!ReverseManager.isShow) {
                            doBacklight(false);
                        } else {
                            mBacklightRecover = true;
                        }
                    }

                    requestAudioFocus();
                    if (mPowerOffDialog != null) {
                        mPowerOffDialog.dismiss();
                        mPowerOffDialog = null;
                    }
                    break;
                case MSG_ANDROID_POWER_OFF:
                    doAndroidPoweroff();
                    break;
                case MSG_ACC_POWEROFF:
                    if (Util.isRKSystem()) {
                        doAndroidPoweroff();
                        // mHandleModeKey.removeMessages(MSG_ANDROID_POWER_OFF);
                        // mHandleModeKey.sendEmptyMessageDelayed(
                        // MSG_ANDROID_POWER_OFF, 100);

                        // OSProManager.clearReverse();
                        // killAllNoSystemProcess();
                        // saveNetStatus();
                        //
                        // Util.setFileValue("/sys/class/ak/source/cam_ch", -1);
                    }
                    break;
                case MSG_RECOVER_BT:
                    MMLog.d(TAG, "MSG_RECOVER_BT");
                    Intent it = new Intent(Intent.ACTION_RUN);
                    try {
                        it.setClassName("com.android.car.bt", "com.android.car.bt.ATBluetoothService");
                        mContext.startService(it);
                    } catch (Exception e) {
                        MMLog.d(TAG, "start ATBluetoothService err");
                    }
                    break;
                case MSG_ACC_POWEROFF_PX5:
                    doAccPowerOffFatePx5();
                    break;
                case MSG_ACC_POWERON_PX5:
                    doAccPowerOnFate();
                    break;
                case MSG_UPDATE_8600_LCD:
                    mLcdShowVol = false;
                    recoverSmallLcd();
                    break;
                case MSG_UPDATE_8600_LCD_TIME:
                    updateSmallLcdTime();
                    break;
            }
        }
    };

    private int mModeIndex = -1;

    private void doKeyMode() {
        initModeKeyToast(false);

        if (!mListModeKey.isEmpty()) {
            mModeIndex = (mModeIndex + 1) % mListModeKey.size();
            int nTextId = mListModeKey.get(mModeIndex).text;
            if (mToastModeKey != null) {
                mToastModeKey.cancel();
            }
            if (nTextId != 0) {
                mToastModeKey = makeModeToast();
                mTextViewMode.setText(nTextId);
                mToastModeKey.show();
            }
            clearKeyMode();
            mHandleModeKey.sendMessageDelayed(mHandleModeKey.obtainMessage(0), GlobalDefinition.mModeKeyDelayTime);
        }
    }

    private void clearKeyMode() {
        mHandleModeKey.removeMessages(0);
    }

    // mode key end
    private boolean mBacklightStatus = true;
    private boolean mBacklightRecover = false;
    private boolean mRealPowerOff = false;

    /*
     * 4.1.8、(SubID 0x08) Power 参数 数值 描述 SubID 0x08 Power 电源相关信息 Param0 0x00
     * Power off MCU 发送关机命令给主机，主机收到此命令后，做好关机准备，MCU 3秒后关闭主机供电。 0x01 Reserved。
     * 0x02 进入ACC延时关机 0x03 退出ACC延时关机 0x04 返回ACC延时关机超时值
     */
    private void doAccPower(int param) {
        mStartPlayTime = 0;
        switch (param) {
            case 0:
                doRealPowerOff();
                break;
            case 2:
                if (Util.isRKSystem()) {
                    if ((SystemClock.uptimeMillis() - GlobalDefinition.mSystemBootStartTime) > 5000) {
                        mTagAccPower = 1;
                        doAccPowerOffFate();
                        doLockPowerKey(false);
                    } else {
                        Log.d(TAG, "early acc off");
                    }
                } else {
                    doAccPowerOffFate();
                    doLockPowerKey(false);
                }
                break;
            case 3:
                doAccPowerOnFirst();

                break;
        }
    }

    /*
     * laite 发送系统ACC状态￥ I:acc on:acc_off
     */
    public static final String ACC_STATE = "acc_state";

    public void sendAccState(Context context, int state) { //suding use
        Settings.System.putInt(context.getContentResolver(), ACC_STATE, state);
        context.getContentResolver().notifyChange(Settings.System.getUriFor(ACC_STATE), null);
    }

    private static final String COMMAND_REQ_NIGHT_MODE_CMD = "REQ_NIGHT_MODE_CMD";
    private static final String COMMAND_REQ_DAY_MODE_CMD = "REQ_DAY_MODE_CMD";

    public static final String CL_NIGHT_MODE = "carletter_night_mode";

    public void sendIllToSuding(boolean state) { //suding use
        Intent it = new Intent();
        it.setAction("com.zjinnova.zlink");

        if (state) {
            it.putExtra("command", COMMAND_REQ_NIGHT_MODE_CMD);
        } else {
            it.putExtra("command", COMMAND_REQ_DAY_MODE_CMD);
        }
        it.setPackage("com.suding.speedplay");
        Log.d(TAG, "sendIllToSuding！！:" + state);
        mContext.sendBroadcast(it);
        //laite used
        Settings.System.putInt(mContext.getContentResolver(), CL_NIGHT_MODE, state ? 1 : 0);
        mContext.getContentResolver().notifyChange(Settings.System.getUriFor(CL_NIGHT_MODE), null);
    }

    private int mTagAccPower = 0;

    private void startZlink() {
        Log.d(TAG, "startZlink！！ && laite:" + mContext);
        if (mContext != null) {
            Intent it;
            it = new Intent("com.zjinnova.zlink");
            it.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            it.putExtra("command", "ACTION_ENTER");
            mContext.sendBroadcast(it);

            try {
                it = new Intent();
                ComponentName componentName = new ComponentName("com.carletter.car", "com.carletter.car.service.CarletterService");
                it.setComponent(componentName);
                mContext.startService(it);
            } catch (Exception e) {
                Log.d(TAG, "start laite err");
            }
        }
    }

    private void doAccPowerOnFirst() {

        mHandleModeKey.removeMessages(MSG_ACC_POWEROFF);
        mHandleModeKey.removeMessages(MSG_ACC_POWEROFF_PX5);
        if (mPowerOffAcc) {
            mPowerOffAcc = false;
            doLockPowerKey(true);
            mContext.sendBroadcast(new Intent(MyCmd.BROADCAST_ACC_DELAY_POWER_ON));

            sendAccState(mContext, 1);
            startZlink();
            if (Util.isRKSystem()) {
                recoverNetStatus();
                // restoreAllUsbStorage();
                // recoverBtStatus();
                // MyService.reinitGpsTime();
            }
            mHandleModeKey.removeMessages(MSG_ACC_POWERON_PX5);
            mHandleModeKey.sendEmptyMessageDelayed(MSG_ACC_POWERON_PX5, 200);

            notifyCarPlay(mContext);
        }
    }

    private void notifyCarPlay(Context context) {
        try {
            Intent intent = new Intent("com.zjinnova.zlink");
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("command", "ACTION_ENTER");
            //intent.setPackage("com.zjinnova.zlink");
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAccPowerOnFate() {
        Log.d(TAG, "doAccPowerOnFate");
        // mHandleModeKey.removeMessages(MSG_ACC_POWEROFF);
        // if (mPowerOffAcc) {
        // mPowerOffAcc = false;
        doPowerOffFate(false, false, false);
        // if (Util.isPX5()) {
        // recoverNetStatus();
        restoreAllUsbStorage();
        OSProManager.checktartReverseAfterSleep();
        // recoverBtStatus();
        // }
        // mContext.sendBroadcast(new Intent(
        // MyCmd.BROADCAST_ACC_DELAY_POWER_ON));

        // }

        CanService.startConnectEx();
    }

    private void doAccPowerOffFate() {
        Log.d(TAG, "doAccPowerOffFate:" + mPowerOffAcc + "run time:" + (SystemClock.uptimeMillis() - mSystemStartTime));
        if (!mPowerOffAcc) {
            mPowerOffAcc = true;
            doPowerOffFate(true, false, (!mPowerKeySwitch));

            mContext.sendBroadcast(new Intent(MyCmd.BROADCAST_ACC_DELAY_POWER_OFF));
            sendAccState(mContext, 0);

            mHandleModeKey.removeMessages(MSG_ACC_POWEROFF_PX5);
            mHandleModeKey.sendEmptyMessageDelayed(MSG_ACC_POWEROFF_PX5, 500);

        }
    }

    private final static int TIME_DELAY_REAL_POWEROFF = 2000;

    private void doAccPowerOffFatePx5() {
        saveData(SAVE_DATA_TIME, (System.currentTimeMillis() + TIME_DELAY_REAL_POWEROFF));
        storeEQIndepend();

        resetDefaultVolume();
        OSProManager.clearReverse();
        if (Util.isRKSystem()) {
            ReverseUI.checkCamera0IfFacing = false;
            saveNetStatus();
            ejectAllUsbStorage();
            powerOffUSB();
            killAllNoSystemProcess();
            Util.setFileValue("/sys/class/ak/source/cam_ch", -1);

            if (mSystemStartTime != 0 && ((SystemClock.uptimeMillis() - mSystemStartTime) > SLEEP_LONG_TIME_TO_REBOOT)) {
                Log.d(TAG, "doAccPowerOffFate: run too much");
                mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) 0x16, (byte) 0x2));
            }
            mHandleModeKey.removeMessages(MSG_ACC_POWEROFF);
            mHandleModeKey.sendEmptyMessageDelayed(MSG_ACC_POWEROFF, TIME_DELAY_REAL_POWEROFF);
        }
    }

    private void doRealPowerOff() {

        saveData(SAVE_DATA_TIME, (System.currentTimeMillis() + 2000));
        storeEQIndepend();

        Log.d(TAG, "doRealPowerOff:" + System.currentTimeMillis());
        if (!mPowerOffFate) {
            doPowerOffFate(true, false, !mPowerKeySwitch);
        }
        mContext.sendBroadcast(new Intent(MyCmd.BROADCAST_REAL_POWER_OFF));

        Util.doSleep(200);

        mRealPowerOff = true;
        mMcuHandler.sendEmptyMessageDelayed(MSG_POWER_OFF_FAIL, 3000);

        doBacklight(true); // mcu will show the true backlight

        //		updateTimeToMcu();
        Util.sudoExecNoCheck("sync");
        // for(int i = 0 ;i<90000000;++i){
        // Util.doSleep(100);
        // Log.d("allen","f");
        // }
    }

    public boolean mPowerOffFate = false;
    public boolean mPowerOffAcc = false;

    private static AlertDialog sConfirmDialog;

    private boolean mPowerKeySwitch = false;

    private void doKeyPower() {
        // try {
        // Intent i = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // mContext.startActivity(i);
        // } catch (Exception e) {
        //
        // }
        if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON && !mPowerOffFate) {
            return;
        }

        mPowerKeySwitch = !mPowerOffFate;
        sendPowerKeySwitchMcu((!mPowerOffFate) ? 0x1 : 0x0);
        if (!mPowerOffFate) {
            doBacklight(true);
            //			BroadcastUtil.sendByCarService(mContext,
            //					AppConfig.PACKAGE_CAR_UI, MyCmd.Cmd.RETURN_POWERKEY_STATUS,
            //					0);
        }
        doPowerOffFate(!mPowerOffFate, true, true);
    }

    private void doKeyPower(boolean on) {
        Log.d(TAG, on + ":" + mPowerOffFate);
        if (on != mPowerOffFate) {
            if (!on) {
                doBacklight(false);
            }
            mPowerKeySwitch = !mPowerOffFate;
            sendPowerKeySwitchMcu((!mPowerOffFate) ? 0x1 : 0x0);
            doPowerOffFate(!mPowerOffFate, true, true);
        }
    }

    private void doKeyPowerAutoOff() {
        mPowerKeySwitch = true;
        doBacklight(false);
        sendPowerKeySwitchMcu(1);
        doPowerOffFate(true, false, false);
    }

    private void sendPowerKeySwitchMcu(int b) {//0x80 query
        Log.d(TAG, "sendPowerKeySwitchMcu:" + b);
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) 0x1d, (byte) b));
    }

    ProgressDialog mPowerOffDialog;

    private void doPowerOnTemp(boolean on) {

    }

    private void doPowerOffFate(boolean poweroff, boolean ui, boolean updateTinyRam) {
        mPowerOffFate = poweroff;

        mHandleModeKey.removeMessages(MSG_AFTER_FATE_POWEROFF);
        Log.d("ffcd", "doPowerOffFate:" + poweroff + updateTinyRam);
        if (mPowerOffFate) {
            // requestAudioFocus();
            //			doBacklight(true);
            mLockKey = LOCK_KEY_ALL;

            Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);
            if (ui) {
                if (mPowerOffDialog == null) {
                    mPowerOffDialog = new ProgressDialog(mContext);
                    //mPowerOffDialog.setTitle(mContext.getText(com.android.internal.R.string.power_off));
                    //mPowerOffDialog.setMessage(mContext.getText(com.android.internal.R.string.shutdown_progress));
                    mPowerOffDialog.setIndeterminate(true);
                    mPowerOffDialog.setCancelable(false);
                    mPowerOffDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

                }
                mPowerOffDialog.show();
            }

            // boolean gps = AppConfig.isGpsApp(AppConfig.getTopActivity());
            if (updateTinyRam) {
                int topActivitySource = getTopActivitySource();

                mTinyRam[0] = (byte) (mAppSource + 1);

                mTinyRam[1] = 0;
                mTinyRam[2] = 0;

                Log.d("Mcu", mPowerKeySwitch + "tinyram set mAppSource:" + mAppSource);
                if (topActivitySource == MyCmd.SOURCE_GPS) {
                    mTinyRam[1] |= 0x1;
                } else if (topActivitySource == MyCmd.SOURCE_OTHERS_APPS) {
                    mTinyRam[1] |= 0x4;
                }

                if (GlobalDefinition.getScreenNum(mContext) > 1) {
                    if (topActivitySource != mAppSource) {
                        mTinyRam[1] |= 0x2;
                    }
                }

                if (mPowerKeySwitch) {
                    mTinyRam[2] |= 0x1;
                }

                Util.setFileValue(mTinyRam, MCU_TINY_RAM);
            }

            Log.d(TAG, "doPowerOffFate:" + mAppSource);
            if (mAppSource == MyCmd.SOURCE_DVD) {
                // if (mRearL != MyCmd.SOURCE_DVD && mRearR !=
                // MyCmd.SOURCE_DVD){
                prepareDvdPowerOff();
                // }
            }
            setSource(MyCmd.SOURCE_AV_OFF);

            mHandleModeKey.sendEmptyMessageDelayed(MSG_AFTER_FATE_POWEROFF, 2000);
            mContext.sendBroadcast(new Intent(MyCmd.BROADCAST_POWER_OFF));
            mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) 0x16, (byte) 0x1));

            dvdPowerOff();
            CarUtil.setCanboxLED(CarUtil.CANBOX_LED_ALL, 0);
            setSmallLcdEx("OFF");

            resetDefaultVolume();
        } else {
            CarUtil.setCanboxLED(CarUtil.CANBOX_LED_ALL, 1);
            recoverSmallLcd();
            doBacklight(true);

            mMcu.sendCmd(new byte[]{0x1, 0x10, 0x1}); // query rear l

            mMcu.sendCmd(new byte[]{0x1, 0x10, 0x2}); // query rear r

            mMcu.sendCmd(new byte[]{0xc, 0x1, 0x4}); // query brake switch
            mMcu.sendCmd(new byte[]{0x1, 0x12, 0x0}); // query brake
            setDvd(2, 0); // query dvd info
            queryIll();

            mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) 0x16, (byte) 0x0));
            doPowerOn();

            mContext.sendBroadcast(new Intent(MyCmd.BROADCAST_POWER_ON));
            abandonAudioFocus();
            if (mPowerOffDialog != null) {
                mPowerOffDialog.dismiss();
                mPowerOffDialog = null;
            }
        }
    }

    private boolean mBtMusicInBTapk = false;

    private int getTopActivitySource() {
        int ret = MyCmd.SOURCE_NONE;
        String s = AppConfig.getTopActivity();
        if (s != null) {
            if (AppConfig.isGpsApp(mContext, s)) {
                ret = MyCmd.SOURCE_GPS;
            } else if (AppConfig.CAR_UI_AUDIO.equals(s)) {
                ret = MyCmd.SOURCE_MUSIC;
            } else if (AppConfig.CAR_UI_VIDEO.equals(s)) {
                ret = MyCmd.SOURCE_VIDEO;
            } else if (AppConfig.CAR_UI_DVD.equals(s)) {
                ret = MyCmd.SOURCE_DVD;
            } else if (AppConfig.CAR_UI_BT_MUSIC.equals(s)) {
                ret = MyCmd.SOURCE_BT_MUSIC;
            } else if (AppConfig.CAR_UI_AUX_IN.equals(s)) {
                ret = MyCmd.SOURCE_AUX;
            } else if (AppConfig.CAR_UI_RADIO.equals(s)) {
                ret = MyCmd.SOURCE_RADIO;
            } else if (s.contains("com.car.dvdplayer")) {
                ret = MyCmd.SOURCE_USBDVD;
            } else if (AppConfig.CAR_BT.equals(s)) {
                if (mBtMusicInBTapk) {
                    ret = MyCmd.SOURCE_BT_MUSIC;
                }
            } else if (is3AppNeedTop(s)) {
                ret = MyCmd.SOURCE_OTHERS_APPS;
            }
        }
        return ret;
    }

    private boolean is3AppNeedTop(String s) {
        return false;
    }

    private void initMcuDisplay() {
        String s = MachineConfig.getProperty(MachineConfig.KEY_SCREEN_W);
        if ("800".equals(s)) {
            mMcu.sendCmd(new byte[]{0x1, 0x18, 0x1});
        } else {
            mMcu.sendCmd(new byte[]{0x1, 0x18, 0x0});
        }
    }

    public void queryMcuRtc() {
        mMcu.sendCmd(new byte[]{0x1, 0x15, 0x0}); // query mcu rtc
    }

    public void queryIll() {
        mMcu.sendCmd(new byte[]{0x1, 0x9, 0x3}); //
    }

    public void queryBattery() {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, ProtocolAk47.SEND_COMMON_SUB_POWER, (byte) 0x20));
    }

    private void doPowerOnDelay() {
        Log.d(TAG, "doPowerOnDelay");
        initMcuDisplay();

        mMcu.sendCmd(new byte[]{0x1, 0x13, 0x1, 0, 0, 0, 0}); // query mcu
        // tiny ram
        // mMcu.sendCmd(new
        // byte[] {
        // 0x1,
        // 0x15, 0x0
        // }); //
        // query mcu
        // rtc

        mMcu.sendCmd(new byte[]{0x1, 0x10, 0x1}); // query rear l

        mMcu.sendCmd(new byte[]{0x1, 0x10, 0x2}); // query rear r

        mMcu.sendCmd(new byte[]{0xc, 0x1, 0x4}); // query brake switch
        mMcu.sendCmd(new byte[]{0x1, 0x12, 0x0}); // query brake

        sendPowerKeySwitchMcu(0x80);
        // mMcu.sendCmd(new byte[] { 0xa, 0x6, 0x3 }); // query ill
        queryIll();

        queryVoulume();
        queryBattery();
        setDvd(2, 0); // query dvd info
        // readTinyRam();
        //		mMcuHandler.postDelayed(new Runnable() {
        //			public void run() {
        //				mLockKey = 0;
        //				Log.d(TAG, "doPowerOnDelay2:" + mAppSource);
        //				if (mAppSource == MyCmd.SOURCE_NONE) {
        //					doPowerOn();
        //					mMcu.sendCmd(ProtocolAk47.generateProtocol1(
        //							ProtocolAk47.TYPE_COMMON_SEND, (byte) 0x16,
        //							(byte) 0x0));
        //				}
        //			}
        //		}, 4000);

        mMcuHandler.removeMessages(MSG_FIRST_RUN_POWERON);
        mMcuHandler.sendEmptyMessageDelayed(MSG_FIRST_RUN_POWERON, 4000);
    }

    private void lockTouch(int lock) {
        Util.setFileValue("/sys/class/ak/source/touch_lock", lock);
    }

    private boolean mSupportPowerKey = false; //for old mcu

    public void doPowerOn() {
        byte key = 0;

        // readTinyRam();
        String pacageName = AppConfig.getCarAppPackageName(mContext);

        String name = null;
        mLockKey = 0;
        int source = mTinyRam[0] - 1;

        if (mPowerKeySwitch) {
            if (mSupportPowerKey) {
                mAppSource = source;
                doKeyPowerAutoOff();
                mTagAccPower = 2;
                // BroadcastUtil.sendByCarService(mContext,
                // AppConfig.PACKAGE_CAR_UI, MyCmd.Cmd.RETURN_POWERKEY_STATUS,
                // 1);
                return;
            }
            mPowerKeySwitch = false;
        }
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                name = "com.android.car.radio.RadioActivity";
                break;
            case MyCmd.SOURCE_MUSIC:
                name = "com.android.car.audio.MusicActivity";
                break;
            case MyCmd.SOURCE_USBDVD:
                pacageName = "com.car.dvdplayer";
                name = "com.car.dvdplayer.DVDPlayerActivity";
                break;
            case MyCmd.SOURCE_VIDEO:
                name = "com.android.car.video.VideoActivity";
                break;
            case MyCmd.SOURCE_BT_MUSIC:
                if (!mBtMusicInBTapk) {
                    name = "com.android.car.btmusic.BTMusicActivity";
                } else {
                    pacageName = "com.android.car.bt";
                    name = "com.android.car.bt.ATBluetoothActivity";
                }
                break;
            case MyCmd.SOURCE_AUX:
                name = "com.android.car.auxplayer.AUXPlayer";
                break;
            case MyCmd.SOURCE_DVD:
                // setDvd(2, 0);
                name = "com.android.car.dvd.DVDPlayer";
                break;
            case MyCmd.SOURCE_DTV:
            case MyCmd.SOURCE_DTV_CVBS:
                name = "com.android.car.tv.TVActivity";
                break;
            case MyCmd.SOURCE_DAB:
                pacageName = "com.car.dabplayer";
                name = "com.car.dabplayer.DABActivity";
                break;
        }

        Log.d(TAG, "doPowerOn:" + mAppSource);
        // if (key != 0) {
        // if (source == MyCmd.SOURCE_BT_MUSIC) {//fix bt_music bug . is not
        // good...
        // setSource(source);
        // }
        // doKey(key);
        // }

        // if((mTinyRam[1] & 0x1)!=0){
        // UtilCarKey.doKeyGps(mContext);
        // }
        boolean showScreen1 = false;
        if (GlobalDefinition.getScreenNum(mContext) > 1) {
            if ((mTinyRam[1] & 0x2) != 0) {
                showScreen1 = true;
            }
        }
        if (name != null) {
            //			if (source == MyCmd.SOURCE_BT_MUSIC) {// fix bt_music bug . is not
            // good...
            setSource(source);
            //			}

            if (!showScreen1) {
                try {
                    final Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setClassName(pacageName, name);
                    if ((mTinyRam[1] & 0x1) != 0) {
                        it.putExtra("gps", 1);
                    }
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    if (source == MyCmd.SOURCE_BT_MUSIC && mBtMusicInBTapk) {
                        it.putExtra("music", 1);
                    }
                    if (mTagAccPower == 1) {
                        it.putExtra("first_poweron", 1);
                        mTagAccPower = 0;
                    } else if (mTagAccPower == 2) {
                        it.putExtra("first_poweron", 3);
                        mTagAccPower = 0;
                    } else {
                        it.putExtra("first_poweron", 2);
                    }

                    if (!mBtMusicInBTapk || !name.equals("com.android.car.bt.ATBluetoothActivity")) {
                        mContext.startActivity(it);
                    } else {
                        mMcuHandler.postDelayed(new Runnable() {
                            public void run() {
                                Log.d("abcd", "ffffffdoPowerOn:" + mAppSource);
                                mContext.startActivity(it);
                            }
                        }, 3000);
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            } else {
                if ((mTinyRam[1] & 0x1) != 0) {
                    UtilCarKey.doKeyGps(mContext);
                }
            }

        } else {
            if ((mTinyRam[1] & 0x1) != 0) {
                UtilCarKey.doKeyGps(mContext);
            }
        }

        if (showScreen1) {
            BroadcastUtil.sendByCarService(mContext, MyCmd.Cmd.REQUEST_SCREEN1_SHOW, source);
        }

    }

    private byte mLoud = 0;
    private Toast mToastLoud;

    private void doKeyLoudness() {
        // Log.d(TAG, "doKeyLoudness:" + mLoud);
        if (mLoud == 0) {
            mLoud = 1;
        } else {
            mLoud = 0;
        }
        if (!mDsp) {
            setAudio(0x2, 0x4, mLoud);
        } else {
            setAudio(0xd, 0x0, mLoud);
        }
        int id;
        if (mLoud == 1) {
            id = R.string.on;
        } else {
            id = R.string.off;
        }
        if (mToastLoud != null) {
            mToastLoud.cancel();
        }
        mToastLoud = Toast.makeText(mContext, "Loud " + mContext.getResources().getString(id), Toast.LENGTH_SHORT);
        mToastLoud.show();
    }

    private void doKeyBrightness() {
        // UtilSystem.doRunActivity(mContext, "com.android.systemui",
        // "com.android.systemui.settings.BrightnessDialog");
        // BroadcastUtil.sendByCarService(mContext, "com.android.systemui",
        // MyCmd.Cmd.BACKLIGHT_STEP);
        Intent it = new Intent(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, 1);
        mContext.sendBroadcast(it);
    }

    private void doKeyBacklight() {
        doBacklight(!mBacklightStatus);
    }

    private void doBacklight(boolean Backlight) {
        // if(Backlight){
        // return;
        // }
        mBacklightStatus = Backlight;

        if (Util.isRKSystem()) {
            if (Backlight) {
                setMcuBacklight(1);
            } else {
                setMcuBacklight(0);
            }
        }

        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", Backlight ? MyCmd.Cmd.BACKLIGHT_ON : MyCmd.Cmd.BACKLIGHT_OFF);
        // if(Backlight){
        //
        // lockTouch(0);
        // }else{
        //
        // lockTouch(1);
        // }
    }

    public void setBacklight(boolean Backlight) {
        if (Backlight && mPowerOffFate) {
            return;
        }
        doBacklight(Backlight);
    }

    // source control

    private static final String MCU_TINY_RAM = "/sys/class/ak/source/tiny_ram";
    private final byte[] mTinyRam = new byte[4];

    /*
     * byte 0: source
     *
     * byte 1: bit 0: is gps bit 1: is screen1 source need show
     */

    private void readTinyRam() {
        File file = new File(MCU_TINY_RAM);
        if (file.exists()) {
            try {
                FileInputStream is = new FileInputStream(file);
                DataInputStream dis = new DataInputStream(is);

                dis.read(mTinyRam, 0, mTinyRam.length);
                dis.close();
                is.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private int mMcuSource = MyCmd.SOURCE_NONE;
    private static int mAppSource = MyCmd.SOURCE_NONE;
    private static int mAppRecover = MyCmd.SOURCE_NONE;

    public int getSource() {
        return mAppSource;
    }

    public int setSource(int index) {

        int ret = 0;

        int mOldSource = mAppSource;
        Log.d(TAG, "setSource:" + index);

        if (mAppSource == MyCmd.SOURCE_DTV && index != MyCmd.SOURCE_DTV) {
            setScreen0(0);
        }

        if (index == MyCmd.SOURCE_DVD) {
            // dvdPowerOn();// power on dvd
            // BroadcastUtil.sendByCarService(mContext,
            // AppConfig.PACKAGE_CAR_UI,
            // MyCmd.Cmd.MCU_DVD_RECEIVE_DATA, s);
            if (/* mDVDStatus == DVD_STATUS_IN_ING || */mDVDStatus == DVD_STATUS_DISK_INSIDE) {
                dvdPowerOn();
            }

        } else if (mAppSource == MyCmd.SOURCE_DVD && index != MyCmd.SOURCE_OTHERS_APPS && index != MyCmd.SOURCE_CANBOX_PHONE) {
            // prepareDvdPowerOff();
            // if (mRearL != MyCmd.SOURCE_DVD && mRearR != MyCmd.SOURCE_DVD) {
            prepareDvdPowerOff();
            // }
        }

        if (mAppSource != MyCmd.SOURCE_BT) {
            mAppRecover = mAppSource;
        }
        if (index != MyCmd.SOURCE_REVERSE) {
            mAppSource = index;
            mCanService.setSource(index);
        }

        if (index == MyCmd.SOURCE_VIDEO || index == MyCmd.SOURCE_MUSIC || index == MyCmd.SOURCE_USBDVD || index == MyCmd.SOURCE_VIDEO_FILEMANAGE || index == MyCmd.SOURCE_MUSIC_FILEMANAGE || index == MyCmd.SOURCE_OTHERS_APPS || index == MyCmd.SOURCE_DVR) {
            index = MyCmd.SOURCE_MX51;
        } else if (index == MyCmd.SOURCE_CANBOX_MEDIA) {
            index = MyCmd.SOURCE_AUX;
        } else if (index == MyCmd.SOURCE_CANBOX_PHONE) {
            index = CarUtil.getCanboxPhoneSource();
        }

        updateArmSound(index != MyCmd.SOURCE_MX51);

        if (index != MyCmd.SOURCE_REVERSE) {// reverse is set by CarApp
            if (index == MyCmd.SOURCE_DTV) {
                setScreen0(1);
            }
            ret = setMcuSource((byte) index);

            // setCameraSource(index);
            mMcuSource = index;
            setEQIndepend(index);
        }

        if (mAppSource != MyCmd.SOURCE_NONE/*
         * && mAppSource !=
         * MyCmd.SOURCE_AV_OFF
         */) {
            clearKeyMode();
            BroadcastUtil.sendByCarService(mContext, MyCmd.Cmd.SOURCE_CHANGE, mAppSource, mOldSource);

        }
        updateAutoVideoOut(index);

        updateSmallLcd(mAppSource, null);

        return ret;
    }

    private final static int[] mEQIndex = {MyCmd.SOURCE_NONE, MyCmd.SOURCE_RADIO, MyCmd.SOURCE_DVD, MyCmd.SOURCE_AUX, MyCmd.SOURCE_MX51, MyCmd.SOURCE_BT};

    private final int[] mEQValue = new int[mEQIndex.length];

    private int mEQindpenOn = 0; // default is close
    private SettingsObserver mSettingsObserver;

    /**
     * Observer to watch for changes to the setting
     */
    private class SettingsObserver extends ContentObserver {

        private final Context mContext;

        SettingsObserver(Handler handler, Context context) {
            super(handler);
            mContext = context;
        }

        void startObserving() {
            ContentResolver resolver = mContext.getContentResolver();

            resolver.registerContentObserver(Settings.System.getUriFor(SystemConfig.KEY_EQ_INDEPEND_SWITCH), false, this);
        }

        void stopObserving() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            initEQIndepend();
        }
    }

    private void initEQIndepend() {

        try {
            mEQindpenOn = Settings.Global.getInt(mContext.getContentResolver(), SystemConfig.KEY_EQ_INDEPEND_SWITCH);
        } catch (Exception snfe) {
            mEQindpenOn = 0;
        }

        if (mSettingsObserver == null) {
            mSettingsObserver = new SettingsObserver(new Handler(), mContext);
            mSettingsObserver.startObserving();
        }

        // mEQindpenOn = 1;
        if (mEQindpenOn == 1) {
            String s = SystemConfig.getProperty(mContext, SystemConfig.KEY_EQ_INDEPEND);
            if (s != null) {
                String[] ss = s.split(",");
                for (int i = 0; i < mEQValue.length; ++i) {
                    if (i < ss.length) {
                        try {
                            mEQValue[i] = Integer.valueOf(ss[i]);
                        } catch (Exception e) {
                            mEQValue[i] = 0;
                        }
                    } else {
                        mEQValue[i] = 0;
                    }
                }
            }
        }
    }

    private static final int EQ_MODE_USER = 0;
    private static final int EQ_MODE_NORMAL = 1;
    private static final int EQ_MODE_JAZZ = 2;
    private static final int EQ_MODE_POP = 3;
    private static final int EQ_MODE_CLASSICAL = 4;
    private static final int EQ_MODE_DBB = 5;
    private static final int EQ_MODE_NEWS = 6;

    private void swtichEQMode() {
        int i = 0;
        for (i = 0; i < EQ_MODE.length; ++i) {
            if (EQ_MODE[i] == mCurEQMode) {
                break;
            }
        }

        if (i < EQ_MODE.length) {
            i = (i + 1) % EQ_MODE.length;
            mCurEQMode = EQ_MODE[i];
            // sendEQCommand( EQ_MODE);

            int text_id = 0;
            switch (mCurEQMode) {
                case EQ_MODE_USER:
                    text_id = R.string.eq_text_user;
                    break;
                case EQ_MODE_JAZZ:
                    text_id = R.string.eq_text_jazz;
                    break;
                case EQ_MODE_CLASSICAL:
                    text_id = R.string.eq_text_classic;
                    break;
                case EQ_MODE_POP:
                    text_id = R.string.eq_text_pop;
                    break;
                case EQ_MODE_DBB:
                    text_id = R.string.eq_text_rock;
                    break;
                case EQ_MODE_NORMAL:
                    text_id = R.string.eq_text_normal;
                    break;
            }

            String s = "EQ: " + mContext.getString(text_id);
            if (mToastEQ != null) {
                mToastEQ.cancel();
            }
            mToastEQ = Toast.makeText(mContext, s, Toast.LENGTH_SHORT);
            mToastEQ.show();

            setAudio(0x2, 0x0, mCurEQMode);
        }
    }

    private Toast mToastEQ;
    private final static byte[] EQ_MODE = {EQ_MODE_CLASSICAL, EQ_MODE_POP, EQ_MODE_DBB, EQ_MODE_JAZZ, EQ_MODE_USER, EQ_MODE_NORMAL};

    private int mCurEQMode = 0;

    private void saveEQIndepend(int value) {
        mCurEQMode = value;
        if (mEQindpenOn == 1) {
            for (int i = 0; i < mEQIndex.length; ++i) {
                if (mMcuSource == mEQIndex[i]) {
                    mEQValue[i] = value;
                    break;
                }
            }
        }
        // storeEQIndepend();
    }

    private void storeEQIndepend() {
        if (mEQindpenOn == 1) {
            String s = "";

            for (int i = 0; i < mEQValue.length; ++i) {
                s += mEQValue[i];
                if (i < (mEQValue.length - 1)) {
                    s += ",";
                }
            }
            SystemConfig.setProperty(mContext, SystemConfig.KEY_EQ_INDEPEND, s);
        }
    }

    private void setEQIndepend(int index) {
        if (mEQindpenOn == 1) {
            for (int i = 0; i < mEQIndex.length; ++i) {
                if (index == mEQIndex[i]) {
                    Log.d("allen1", "setEQIndepend" + mEQValue[i]);
                    setAudio(0x2, 0x0, mEQValue[i]);
                    break;
                }
            }
        }
    }

    // DVD control
    private int mDVDPower = 0;
    private int mDVDStatus = 0;
    private final static int DVD_POWER_ON = 1;
    private final static int DVD_POWER_OFF = 0;
    private final static int DVD_STATUS_UNKNOW = 0;
    private final static int DVD_STATUS_DISK_INSIDE = 1;
    private final static int DVD_STATUS_DISK_IN_ENTRY = 2;
    private final static int DVD_STATUS_IN_ING = 3;
    private final static int DVD_STATUS_OUT_ING = 4;
    private final static int DVD_STATUS_NO_DISK = 5;

    private final static int MSG_DVD_POWER_OFF = 1000;
    private final static int TIME_DVD_POWER_OFF = 600;

    private final static int MSG_DVD_EJECT = 1001;
    private final static int MSG_SEND_EQ_TO_CANBOX = 1002;
    private final static int TIME_DVD_EJECT = 800;

    private final static int MSG_POWER_OFF_FAIL = 1001000;
    private final static int MSG_DELAY_SET_ARM_LVDS = 1001001;
    private final static int MSG_DELAY_SET_TV_LVDS = 1001002;
    private final static int MSG_DELAY_SET_MCU_LVDS = 1001003;


    private final static int MSG_FIRST_RUN_POWERON = 1001004;


    private void doDvdStatusChange(byte s) {
        if (s == DVD_STATUS_DISK_INSIDE || s == DVD_STATUS_DISK_IN_ENTRY || s == DVD_STATUS_IN_ING || s == DVD_STATUS_OUT_ING) {
            GlobalDefinition.makeSureDVDExist(true);
        }
        if (s == DVD_STATUS_DISK_INSIDE || s == DVD_STATUS_DISK_IN_ENTRY || s == DVD_STATUS_IN_ING) {
            CarUtil.setCanboxLED(CarUtil.CANBOX_LED_DISC, 0);
        } else {
            CarUtil.setCanboxLED(CarUtil.CANBOX_LED_DISC, 1);
        }

        if (mDVDStatus != s) {
            // BroadcastUtil.sendByCarService(mContext,
            // AppConfig.PACKAGE_CAR_UI,
            // MyCmd.Cmd.MCU_DVD_RECEIVE_DATA, s);
            if (s == DVD_STATUS_DISK_INSIDE) {

                if (!ReverseManager.isShow) {
                    if (mDVDStatus == DVD_STATUS_IN_ING) {
                        UtilCarKey.doKeyDVD(mContext);
                    }
                } else {
                    reverseOpenApp = MyCmd.SOURCE_DVD;
                }

                if (mNodiskDvdPowerOn) {
                    mNodiskDvdPowerOn = false;
                    dvdPowerOff();
                }
            }

            if (/* s == DVD_STATUS_IN_ING || */s == DVD_STATUS_DISK_INSIDE && (AppConfig.CAR_UI_DVD.equals(AppConfig.getTopActivity()) || mDVDStatus == DVD_STATUS_IN_ING)) {
                dvdPowerOn();
            } else {
                Log.d(TAG, "doDvdStatusChange:" + s + ":" + mDVDStatus + ":" + AppConfig.getTopActivity());
            }

            if ((s == DVD_STATUS_DISK_INSIDE) && (mDVDStatus == DVD_STATUS_IN_ING)) {
                if (mPowerOffFate) {
                    doPowerOffFate(false, false, false);
                } else {
                    if (!mBacklightStatus) {
                        doBacklight(true);
                    }
                }
                if (mBacklightRecover) {
                    mBacklightRecover = false;
                }
            }


        }
        mDVDStatus = s;

    }

    private void dvdPowerOn() {

        mMcuHandler.removeMessages(MSG_DVD_POWER_OFF);
        setDvd(1, 1);
    }

    private void dvdPowerOff() {
        setDvd(1, 2);
    }

    private void prepareDvdPowerOff() {
        // if (mDVDPower == DVD_POWER_ON) {

        Util.setFileValue("/sys/class/misc/ak-dvd/device/data", "0x0300"); // save
        // play
        // status

        mMcuHandler.removeMessages(MSG_DVD_POWER_OFF);
        mMcuHandler.sendEmptyMessageDelayed(MSG_DVD_POWER_OFF, TIME_DVD_POWER_OFF);
        // }

    }

    // DVD control end
    public void updateArmSound(boolean recover) {
        if (recover) {
            int armSound = Util.getFileValue("/sys/class/ak/source/arm_sound");

            if (armSound == 1) {
                Util.setFileValue("/sys/class/ak/source/arm_sound", 0);
            }
        }
    }

    private final int mCameraChannel = 1;

    public int getCameraIndex() {
        Log.d("aa", String.valueOf(mCameraChannel));
        return mCameraChannel;
    }

    public void setCameraSource(int source) {
        if (ReverseManager.isShow && source != MyCmd.SOURCE_REVERSE) {

            Log.d(TAG, "setCameraSource: is in reverse, can't set other camera source");
            return;
        }
        switch (source) {
            case MyCmd.SOURCE_AUX:
                source = MyCmd.CAMERA_SOURCE_AUX;
                break;
            case MyCmd.SOURCE_REVERSE:
                if (CarUtil.isFocusSync3Reverse()) {
                    source = MyCmd.CAMERA_SOURCE_AUX;
                } else {
                    source = MyCmd.CAMERA_SOURCE_REVERSE;
                }
                break;
            case MyCmd.SOURCE_DVD:
                source = MyCmd.CAMERA_SOURCE_DVD;
                break;
            case MyCmd.SOURCE_DTV_CVBS:
                source = MyCmd.CAMERA_SOURCE_DTV_CVBS;
                break;
            default:
                source = -1;
                break;
        }
        // Log.d("allen1","setCameraSource:"+source);
        // mCameraChannel = Util
        // .getFileValue("/sys/class/misc/mst701/device/source");
        if (source != -1 /* && mCameraChannel != source */) {

            if (Util.isRKSystem()) {
                Util.setFileValue("/sys/class/ak/source/cam_ch", source);
            } else {
                Util.setFileValue("/sys/class/misc/mst701/device/source", source);
            }
        }

        // if (source != -1 && s!=source) {
        //
        // }

    }

    public boolean getBacklightStatus() {
        return mBacklightStatus;
    }

    public boolean getBacklightRecoverStatus() {
        return mBacklightRecover;
    }

    private final static String DTV_LVDS = "/sys/class/misc/lt8619b/device/lvds_out";
    private final static String ARM_LVDS = "/sys/class/graphics/fb0/blank";
    private final static int SCREEN0_ARM = 0;
    private final static int SCREEN0_TV = 1;

    private byte mScreen0Recover = -1;

    public void resetScreen0Status() {
        if (mScreen0 == SCREEN0_TV) {
            mScreen0Recover = mScreen0;
            setScreen0(SCREEN0_ARM);
        }
    }

    public void recoverScreen0Status() {
        if (mScreen0Recover != -1) {
            setScreen0(SCREEN0_TV);
            mScreen0Recover = -1;
        }
    }

    private void doSetSceen0() {

    }

    // private long mSetScreen0Time = 0;
    public void setScreen0(int index) { // for hdmi tv
        if ((mScreen0 == index)/*
         * &&(System.currentTimeMillis() -
         * mSetScreen0Time)<800
         */) {
            Log.d(TAG, "setScreen0 same");
            return;
        }

        // if(index == SCREEN0_TV &&
        // !AppConfig.CAR_DTV.equals(AppConfig.getTopActivity())){
        // Log.d(TAG, "TV is not top");
        // return;
        // //setScreen0(SCREEN0_TV);
        // }
        // mSetScreen0Time = System.currentTimeMillis();
        mScreen0 = (byte) index;
        mMcuHandler.removeMessages(MSG_DELAY_SET_ARM_LVDS);
        mMcuHandler.removeMessages(MSG_DELAY_SET_MCU_LVDS);
        mMcuHandler.removeMessages(MSG_DELAY_SET_TV_LVDS);
        if (index == 0) {
            mMcuHandler.sendMessageDelayed(mMcuHandler.obtainMessage(MSG_DELAY_SET_ARM_LVDS, 0, 0), 200);
            mMcuHandler.sendMessageDelayed(mMcuHandler.obtainMessage(MSG_DELAY_SET_MCU_LVDS, index, 0), 400);
            mMcuHandler.sendMessageDelayed(mMcuHandler.obtainMessage(MSG_DELAY_SET_TV_LVDS, 0, 0), 600);
            // Util.setFileValue(ARM_LVDS, "0");
            // Util.doSleep(500);
            // setMcuSceen0(index);
            // Util.doSleep(500);
            // Util.setFileValue(DTV_LVDS, "0");
        } else {

            mMcuHandler.sendMessageDelayed(mMcuHandler.obtainMessage(MSG_DELAY_SET_TV_LVDS, 1, 0), 600);
            mMcuHandler.sendMessageDelayed(mMcuHandler.obtainMessage(MSG_DELAY_SET_MCU_LVDS, index, 0), 400);
            mMcuHandler.sendMessageDelayed(mMcuHandler.obtainMessage(MSG_DELAY_SET_ARM_LVDS, 4, 0), 200);

            // Util.setFileValue(DTV_LVDS, "1");
            // Util.doSleep(500);
            // setMcuSceen0(index);
            //
            // Util.doSleep(500);
            // Util.setFileValue(ARM_LVDS, "4");
        }
    }

    private final int[] mRecoverDevice = new int[]{0, 0, 0};

    public void resetBacklightStatus(int index) {
        mRecoverDevice[index] = 1;
        if (!mBacklightStatus) {

            mBacklightRecover = true;
            doBacklight(true);

        }
    }

    public void recoverBacklightStatus(int index) {
        mRecoverDevice[index] = 0;
        if (mBacklightRecover) {

            if (mRecoverDevice[0] == 0 && mRecoverDevice[1] == 0 && mRecoverDevice[2] == 0) {
                mBacklightRecover = false;
                doBacklight(false);
            }
        }
    }

    private int mBtPhoneStatus = MyCmd.PhoneStatus.PHONE_OFF;

    public void setBtPhoneStatus(int i) {
        if (i == MyCmd.PhoneStatus.PHONE_ON) {
            resetBacklightStatus(1);

            resetScreen0Status();
            mBtPhoneStatus = i;
            updateSmallLcd(MyCmd.SOURCE_BT_PHONE, null);
        } else if (i == MyCmd.PhoneStatus.PHONE_OFF) {
            recoverBacklightStatus(1);
            mBtPhoneStatus = i;

            recoverScreen0Status();
            recoverSmallLcd();
            mBtOneTimeMute = false;
        } else if (i == MyCmd.PhoneStatus.PHONE_MUTE_ONE_TIME) {
            if (mBtOneTimeMute) {
                Log.d(TAG, "mBtOneTimeMute!! return");
                return;
            }
            mBtOneTimeMute = true;
        } else if (MyCmd.PhoneStatus.PHONE_IVT_CRASH_MUTE == i) {
            if (mAppSource == MyCmd.SOURCE_BT_MUSIC) {
                mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_AUDIO_SEND, ProtocolAk47.SEND_VOLUME_SUB_VOLUME_CONTROL, (byte) 4, (byte) 10));
            }
            return;
        } else if (MyCmd.PhoneStatus.PHONE_GOC_MUTE == i) {
            if (mAppSource == MyCmd.SOURCE_BT_MUSIC || mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_AUDIO_SEND, ProtocolAk47.SEND_VOLUME_SUB_VOLUME_CONTROL, (byte) 4, (byte) 10));
            }
            return;
        } else if (MyCmd.PhoneStatus.PHONE_GOC_UNMUTE == i) {
            if (mAppSource == MyCmd.SOURCE_BT_MUSIC || mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                mMcu.sendCmd(ProtocolAk47.generateProtocol2(ProtocolAk47.TYPE_AUDIO_SEND, ProtocolAk47.SEND_VOLUME_SUB_VOLUME_CONTROL, (byte) 4, (byte) 1));
            }
            return;
        }
        clearKeyMode();
        setMcuBtPhoneStatus(i);

    }

    private boolean mBtOneTimeMute = false;

    private int mSourceRecoverByCanbox = MyCmd.SOURCE_NONE;
    private int mCanboxPhoneStatus = MyCmd.PhoneStatus.PHONE_OFF;

    public void setCanboxPhoneStatus(int i) {
        if (i == MyCmd.PhoneStatus.PHONE_ON || i == MyCmd.PhoneStatus.PHONE_CARPLAY_ON) {
            resetBacklightStatus(2);

            if (mAppSource != MyCmd.SOURCE_CANBOX_PHONE) {
                mSourceRecoverByCanbox = mAppSource;
                setSource(MyCmd.SOURCE_CANBOX_PHONE);
            }

            requestAudioFocus();

        } else if (i == MyCmd.PhoneStatus.PHONE_OFF || i == MyCmd.PhoneStatus.PHONE_CARPLAY_OFF) {
            recoverBacklightStatus(2);

            if (mSourceRecoverByCanbox != MyCmd.SOURCE_NONE) {
                setSource(mSourceRecoverByCanbox);
                mSourceRecoverByCanbox = mAppSource;
            }

            abandonAudioFocus();
        }
        mCanboxPhoneStatus = i;
        clearKeyMode();

    }

    public void queryCurrentSource(int id) {
        Intent it = new Intent(MyCmd.BROADCAST_CAR_SERVICE_SEND);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.RETURN_CURRENT_SOURCE);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, mAppSource);
        it.putExtra(MyCmd.EXTRA_COMMON_ID, id);
        mContext.sendBroadcast(it);

        // BroadcastUtil.sendByCarService(mContext,
        // MyCmd.Cmd.RETURN_CURRENT_SOURCE, mAppSource);
    }

    // canbox
    public static int sendCanboxData(byte[] buf) {
        if (mThis != null) {
            int can_len = buf.length - 1;
            byte[] protocol = new byte[2 + can_len];
            protocol[0] = ProtocolAk47.TYPE_CAN_SEND;
            protocol[1] = 0x3;
            Util.byteArrayCopy(protocol, buf, 2, 1, can_len);
            return mThis.mMcu.sendCmd(protocol);
        }
        return -1;
    }

    // save time

    private static final String SAVE_DATA = "CarService";
    private static final String SAVE_DATA_TIME = "time";

    private void saveData(String s, long v) {
        SharedPreferences.Editor shareData = mContext.getSharedPreferences(SAVE_DATA, 0).edit();
        shareData.putLong(s, v);
        shareData.commit();
    }

    private long getData(String s) {
        SharedPreferences shareData = mContext.getSharedPreferences(SAVE_DATA, 0);
        return shareData.getLong(s, 0);
    }

    private void updateSaveTime(int mcuTime) {
        // if (mcuTime != 0) {

        long t = getData(SAVE_DATA_TIME);
        Log.d(TAG, mNewSaveTime + ":updateSaveTime:" + t + ":" + mcuTime);
        if (mNewSaveTime) {
            return;
        }

        if (t != 0) {
            t = (mcuTime * 1000L) + t;

            // Log.d(TAG, "updateSaveTime2:" + t + ":" + mcuTime);
            boolean ret = false;
            try {
                ret = SystemClock.setCurrentTimeMillis(t);
            } catch (Exception e) {
                // Log.d(TAG, "err !!!!!!!!! updateSaveTime:" + ret);
            }
        }
    }

    private void requestAudioFocus() {

        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private final OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
        }
    };

    public void abandonAudioFocus() {
        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
    }

    private int reverseOpenApp = MyCmd.SOURCE_NONE;

    public void resetReverseOpenApp() {
        if (reverseOpenApp != MyCmd.SOURCE_NONE) {
            if (reverseOpenApp == MyCmd.SOURCE_DVD) {
                UtilCarKey.doKeyDVD(mContext);
            }
            reverseOpenApp = MyCmd.SOURCE_NONE;
        }
    }

    public void setAccPowerDelayTime(int time) {

        byte[] protocol = new byte[6];
        protocol[0] = 0x1;
        protocol[1] = 0x1;
        protocol[2] = (byte) 0x6;
        protocol[3] = (byte) ((time & 0xff00) >> 8);
        protocol[4] = (byte) (time & 0xff);
        protocol[5] = (byte) ((time & 0xff00) >> 16);

        mMcu.sendCmd(protocol);
    }

    public void setVoltageProtect(int v) {

        byte[] protocol = new byte[6];
        if ((v & 0xff00) != 0) {
            protocol = new byte[3];
            protocol[2] = (byte) ((v & 0xff00) >> 8);
        } else {
            protocol = new byte[4];
            protocol[2] = 0x10;
            protocol[3] = (byte) (v & 0xff);
        }

        protocol[0] = 0x1;
        protocol[1] = 0x1;

        mMcu.sendCmd(protocol);
    }

    public void setLightDectect(int v) {

        byte[] protocol = new byte[6];


        protocol[0] = 0xc;
        protocol[1] = 0xa;
        protocol[2] = (byte) (v & 0xff);

        mMcu.sendCmd(protocol);
    }

    private void doAndroidPoweroff() {
        Log.d(TAG, "doAndroidPoweroff:");
        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.ANDROID_POWEROFF);

        // PowerManager pm =
        // (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        // pm.goToSleep(200);
    }

    private final static String[] KILL_WHITELIST = {"com.ak.speechrecog", "com.oes.unitapp", "com.txznet.txz", "com.txznet.smartadapter", "com.aispeech.hotwords", "com.dogen.usbcameradogen"};

    private static boolean checkWhiteList(String app) {
        if (app != null) {
            for (int i = 0; i < KILL_WHITELIST.length && KILL_WHITELIST[i] != null; i++) {
                if (KILL_WHITELIST[i].equals(app)) return true;
            }
        }
        return false;
    }


    private final static String[] KILL_SYSTEM_APP = {"com.android.chrome", "com.android.email", "com.google.android.gm", "com.google.android.apps.maps", "com.android.vending", "com.carletter.car"};

    private void killAllNoSystemProcess() {
        try {
            getAvailMemory(mContext);
            PackageManager mPackageManager = mContext.getPackageManager();
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            final List<ResolveInfo> apps = mPackageManager.queryIntentActivities(mainIntent, 0);

            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> mRunningProcess = am.getRunningAppProcesses();

            for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess) {
                int kill = -1;

                for (ResolveInfo appInfo : apps) {
                    boolean flag = false;
                    if ((appInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                        flag = true;
                    } else if ((appInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        flag = true;
                    }

                    if (amProcess.processName.startsWith(appInfo.activityInfo.packageName)) {
                        if (flag) {
                            kill = 1;
                        } else {
                            kill = 0;
                        }
                        break;
                    }
                }

                if (kill == -1) {
                    if (amProcess.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                        kill = 1;
                    }
                }
                Log.d(TAG, amProcess.processName + "<<kill:" + kill + ":" + amProcess.importance);
                if (kill > 0 && !checkWhiteList(amProcess.processName)) {
                    //am.forceStopPackage(amProcess.processName);
                }
            }

            for (String s : KILL_SYSTEM_APP) {
                Log.d(TAG, "kill system app:" + s);
                //am.forceStopPackage(s);
            }
            // Log.d(TAG, "kill bt:");
            // am.forceStopPackage("com.android.car.bt");

            // Util.setProperty("ctl.stop", "ivt_blueletd");
            getAvailMemory(mContext);
        } catch (Exception e) {
            Log.d(TAG, "killAllNoSystemProcess:" + e);
        }

    }

    // 获取可用内存大小
    private long getAvailMemory(Context context) {
        // 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        Log.d(TAG, "可用内存---->>>" + mi.availMem / (1024 * 1024));
        return mi.availMem / (1024 * 1024);
    }

    private ConnectivityManager mConnectivityManager;

    // private WifiManager mWifiManager;
    // private boolean saveWifiStatus = false;
    // private boolean saveConnectDataStatus = false;
    private void saveNetStatus() {
        // mWifiManager = (WifiManager) mContext
        // .getSystemService(Context.WIFI_SERVICE);
        try {
            mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            //mConnectivityManager.setAirplaneMode(true);
            Log.d(TAG, "saveNetStatus:");
        } catch (Exception e) {

        }
        // if (mConnectivityManager.getMobileDataEnabled()) {
        //
        // saveConnectDataStatus = true;
        // }
        //
        //
        // if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING
        // || mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
        //
        // // saveWifiStatus = true;
        // // mWifiManager.setWifiEnabled(false);
        //
        // }

    }

    public void recoverBtStatus() {
        Util.setProperty("ctl.start", "ivt_blueletd");
        mHandleModeKey.removeMessages(MSG_RECOVER_BT);
        mHandleModeKey.sendEmptyMessageDelayed(MSG_RECOVER_BT, 1500);
    }

    public void recoverNetStatus() {
        // if (saveWifiStatus && mWifiManager != null) {
        // mWifiManager.setWifiEnabled(true);
        // saveWifiStatus = false;
        // }
        //
        // if (saveConnectDataStatus && mConnectivityManager != null) {
        //
        // saveConnectDataStatus = false;
        // }
        Log.d(TAG, "recoverNetStatus:");
        try {
            if (mConnectivityManager == null) {
                mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            }
            //mConnectivityManager.setAirplaneMode(false);
        } catch (Exception e) {

        }
    }

    private void resetDefaultVolume() {
        if (MachineConfig.VALUE_SYSTEM_UI_KLD7_1992.equals(GlobalDefinition.mSystemUI)) {
            return;
        }

        int i = SystemConfig.getIntProperty2(mContext, SystemConfig.KEY_DEFAULT_RESET_VOLUME_LEVEL);
        if (i != -1) {
            DEFAULT_VOLUME = i;
        }
        Log.d(TAG, "resetDefaultVolume:" + mVolume + "to:" + DEFAULT_VOLUME);
        if (DEFAULT_VOLUME > 0 && mVolume > DEFAULT_VOLUME) {
            setVolume(DEFAULT_VOLUME);
        }
    }

    private final ArrayList<String> mUSBUmount = new ArrayList<String>();

    private void restoreAllUsbStorage() {
        Log.i("abcd", "restoreAllUsbStorage" + mUSBUmount.size());
        for (String s : mUSBUmount) {
            remountUSB(s);
        }
    }

    private void remountUSB(String id) {

        Intent intent = new Intent();

        intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageMountReceiver");
        intent.putExtra("android.os.storage.extra.VOLUME_ID", id);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        //
        //
        mContext.sendBroadcast(intent);

        // StorageManager storageManager = (StorageManager) mContext
        // .getSystemService(Context.STORAGE_SERVICE);
        // try {
        // Log.i("FFFK", "## mount ++");
        // // storageManager.mount(volID);
        // Log.i("FFFK", "## mount --");
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
    }

    private void powerOffUSB() {
        Util.setFileValue("/sys/class/ak/source/usb_pwr", 0);
    }

    private void ejectAllUsbStorage() {
        Log.d(TAG, "ejectAllUsbStorage");
        mUSBUmount.clear();
        // mUSBUmount = null;
        StorageManager storageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod("getVolumes", paramClasses);
            Object[] params = {};
            List<Object> VolumeInfo = (List<Object>) getVolumeList.invoke(storageManager, params);

            if (VolumeInfo != null) {
                for (Object volumeinfo : VolumeInfo) {

                    Method getPath = volumeinfo.getClass().getMethod("getPath");

                    File path = (File) getPath.invoke(volumeinfo, new Object[0]);

                    Method getId = volumeinfo.getClass().getMethod("getId");

                    String id = (String) getId.invoke(volumeinfo, new Object[0]);

                    Method getDisk = volumeinfo.getClass().getMethod("getDisk");

                    Object diskinfo = getDisk.invoke(volumeinfo);
                    if (diskinfo != null) {
                        Method isSd = diskinfo.getClass().getMethod("isSd");
                        boolean bIsSd = (Boolean) isSd.invoke(diskinfo, new Object[0]);
                        if (bIsSd && Util.isPX6()) {
                            String strPath = (path == null) ? null : path.getAbsolutePath();
                            if (strPath != null && strPath.startsWith("/storage/MediaCard") && id != null && id.startsWith("public:8,")) {
                                Log.w("abcd", "MediaCard " + id + " is actually a UDISK");
                                bIsSd = false;
                            }
                        }
                        if (!bIsSd) {
                            Log.d("abcd", (path == null ? "path is null," : path.toString()) + " ejectAllUsbStorage " + id);
                            // if (path.toString().endsWith("USBdisk3")
                            // || path.toString().endsWith("USBdisk4")) {
                            mUSBUmount.add(id);
                            Log.d("abcd", (path == null ? "path is null," : path.toString()) + " umount!!!");
                            // try {
                            // Log.i("FFFK", "## mount ++");
                            // storageManager.unmount(id);
                            // Log.i("FFFK", "## mount --");
                            // } catch (Exception e) {
                            // e.printStackTrace();
                            // }

                            Intent intent = new Intent();

                            intent.setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageUnmountReceiver");
                            intent.putExtra("android.os.storage.extra.VOLUME_ID", id);
                            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                            //
                            //
                            mContext.sendBroadcast(intent);
                            // }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doKeyCustomApp() {
        boolean ret = false;

        String pc = SystemConfig.getProperty(mContext, SystemConfig.KEY_CUSTOM_APP);
        if (pc != null) {
            String[] ss = pc.split("/");
            Intent it = new Intent();
            try {

                it.setClassName(ss[0], ss[1]);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(it);

                ret = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!ret) {
            Toast.makeText(mContext, "Fail!", Toast.LENGTH_LONG).show();
            // Toast.makeText(context, errStiring,
            // Toast.LENGTH_SHORT).show();

            // com.android.car.factory.intent.action.GeneralSettings
            // try {
            // Intent it = new Intent();
            // it.setAction("com.android.car.factory.intent.action.GeneralSettings");
            // it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // it.putExtra("key_custom_app", 1);
            // mContext.startActivity(it);
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
        }
    }

    public void setBrakeProp() {
        if (mBrakeSwitch != 0) {
            if (mBrake != -1) {
                if (mBrake == 0 && GlobalDefinition.mPreGPSBrake != 1 && CarUtil.mPreCanboxBrake != 1) {
                    if (!GlobalDefinition.mTopIsNoNeedBrakeControl) {
                        Util.setProperty("ak.codec.disable_video_out", "1");
                    }
                    MyCarService.doSaveDriver();
                } else {
                    Util.setProperty("ak.codec.disable_video_out", "0");
                }
            }
        } else {
            if (mBrake != -1) {
                Util.setProperty("ak.codec.disable_video_out", "0");
                MyCarService.doSaveDriver();
            }
        }
    }

    public int getDVDStatus() {
        return mDVDStatus;
    }

    public int getMcuVolume() {
        return mVolume;
    }

    private boolean mHideVolumeUI = false;

    public void setMcuVolumeUI(boolean hide) {
        mHideVolumeUI = hide;
    }

    private void updateDSP(int i) {

        int dsp = SystemConfig.getIntProperty(mContext, SystemConfig.KEY_DSP);

        if (i == 1) {
            mDsp = true;
        }

        if (dsp != i) {
            SystemConfig.setIntProperty(mContext, SystemConfig.KEY_DSP, i);

            Intent it = new Intent(MyCmd.BROADCAST_MACHINECONFIG_UPDATE);
            it.putExtra(MyCmd.EXTRA_COMMON_CMD, MachineConfig.KEY_APP_HIDE);
            mContext.sendBroadcast(it);

            if (i == 1) {

                // Settings.Global.putInt(mContext.getContentResolver(),
                // Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);
                // Settings.System.putInt(mContext.getContentResolver(),
                // Settings.System.SCREEN_OFF_TIMEOUT, 30000);

                // SystemConfig.setIntProperty(mContext,
                // SystemConfig.KEY_DSP_SCREEN_SAVER, 2);

            } else {
                // if (GlobalDef.mSystemUI != null
                // && (MachineConfig.VALUE_SYSTEM_UI20_RM10_1
                // .equals(GlobalDef.mSystemUI)
                // || MachineConfig.VALUE_SYSTEM_UI21_RM10_2
                // .equals(GlobalDef.mSystemUI) ||
                // MachineConfig.VALUE_SYSTEM_UI21_RM12
                // .equals(GlobalDef.mSystemUI))) {
                // Settings.Global.putInt(mContext.getContentResolver(),
                // Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);
                // Settings.System.putInt(mContext.getContentResolver(),
                // Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                // Log.d(TAG, "ScreenSaver STAY_ON_WHILE_PLUGGED_IN");
                // } else {
                // Settings.System.putInt(mContext.getContentResolver(),
                // Settings.System.SCREEN_OFF_TIMEOUT,
                // Integer.MAX_VALUE);
                // Log.d(TAG, "ScreenSaver SCREEN_OFF_TIMEOUT");
                // }
            }
            // updateDSPScreenSaver(time);
        }
    }

    public void updateDSPScreenSaver(int time) {

        try {
            if (time == 0) {
                // if (GlobalDef.mSystemUI != null
                // && (MachineConfig.VALUE_SYSTEM_UI20_RM10_1
                // .equals(GlobalDef.mSystemUI)
                // || MachineConfig.VALUE_SYSTEM_UI21_RM10_2
                // .equals(GlobalDef.mSystemUI) ||
                // MachineConfig.VALUE_SYSTEM_UI21_RM12
                // .equals(GlobalDef.mSystemUI))) {
                // Settings.Global.putInt(mContext.getContentResolver(),
                // Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);
                // Settings.System.putInt(mContext.getContentResolver(),
                // Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                // } else {
                Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
                // }
            } else {
                Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);
                Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, time);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        Log.d(TAG, "updateDSPScreenSaver:" + time);
    }

    private void updateAutoVideoOut(int index) {
        if (AppConfig.isHidePackage("com.android.car.videoout.VideoOutActivity")) {

            if (index == MyCmd.SOURCE_DVD || index == MyCmd.SOURCE_AUX) {
                mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) 0x3, (byte) index));
                mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) 0xF, (byte) index));
            } else {
                mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) 0x3, (byte) 0x7));
                mMcu.sendCmd(ProtocolAk47.generateProtocol1(ProtocolAk47.TYPE_COMMON_SEND, (byte) 0xF, (byte) 0x7));
            }
        }
    }

    public void updateVideoOUTDvd() {

    }

    // 8600
    private void recoverSmallLcd() {
        if (GlobalDefinition.mIs8600) {
            if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                GlobalDefinition.setSmallLcd("BT");
                GlobalDefinition.setSmallLcdIcon("0");
            } else {
                setSmallLcdEx(mPreLcd);
                GlobalDefinition.setSmallLcdIcon(mPreIcon);
            }

        }
    }

    private String mPreLcd = "";
    private String mPreIcon = "0";
    private boolean mLcdShowVol = false;
    private int mLcdSource = -3;
    private final static int SOURCE_VOL_8600 = 0xfe;

    public void updateSmallLcd(int source_in, String str) {
        if (GlobalDefinition.mIs8600) {
            String s = null;
            int source = (source_in & 0xff);
            if (str == null) {
                if (mBtPhoneStatus == MyCmd.PhoneStatus.PHONE_ON) {
                    if (source == SOURCE_VOL_8600) {
                        s = "VOL " + mVolume;
                        mLcdShowVol = true;
                        mHandleModeKey.removeMessages(MSG_UPDATE_8600_LCD);
                        mHandleModeKey.sendEmptyMessageDelayed(MSG_UPDATE_8600_LCD, 3000);
                    } else {
                        s = "BT";
                    }
                } else {
                    if (mLcdSource == source && source != SOURCE_VOL_8600) {
                        return;
                    }
                    mLcdSource = source;

                    switch (source) {
                        case MyCmd.SOURCE_RADIO:
                            s = "";
                            break;
                        case MyCmd.SOURCE_MUSIC:
                            s = "MP3";
                            break;
                        case MyCmd.SOURCE_AUX:
                        case MyCmd.SOURCE_VIDEO:
                            s = "AV";
                            break;
                        case MyCmd.SOURCE_BT_MUSIC:
                            s = "A2DP";
                            break;
                        case MyCmd.SOURCE_DTV_CVBS:
                            s = "TV";
                            break;
                        case MyCmd.SOURCE_DVD:
                            s = "DVD";
                            break;
                        case SOURCE_VOL_8600:
                            s = "VOL " + mVolume;
                            mLcdShowVol = true;
                            mHandleModeKey.removeMessages(MSG_UPDATE_8600_LCD);
                            mHandleModeKey.sendEmptyMessageDelayed(MSG_UPDATE_8600_LCD, 3000);
                            break;
                        default:
                            s = "OFF";
                            break;
                    }

                    if (source != SOURCE_VOL_8600) {
                        mPreLcd = s;
                    }
                }
                if (s != null) {
                    setSmallLcdEx(s);
                }
                //always ainm now
                //				if ("OFF".equals(s)) {
                //					GlobalDef.setSmallLcdAinm("0");
                //				} else {
                GlobalDefinition.setSmallLcdAinm("1");
                //				}

                String icon = "0";
                if (MyCmd.SOURCE_MUSIC == source) {
                    mPreIcon = icon = "0x00000800";
                    //					GlobalDef.setSmallLcdIcon("0x00000800");
                } else if (MyCmd.SOURCE_VIDEO == source) {
                    mPreIcon = icon = "0x00002000";
                    //					GlobalDef.setSmallLcdIcon("0x00002000");
                } else if (MyCmd.SOURCE_RADIO == source) {
                    mPreIcon = mPreRadioIcon;
                } else if (mBtPhoneStatus != MyCmd.PhoneStatus.PHONE_ON && SOURCE_VOL_8600 != source) {
                    mPreIcon = icon;
                }
                if (!"OFF".equals(s)) {
                    GlobalDefinition.setSmallLcdIcon(icon);
                }
            } else {
                if (mBtPhoneStatus != MyCmd.PhoneStatus.PHONE_ON && !mLcdShowVol) {
                    if (mAppSource == source) {
                        GlobalDefinition.setSmallLcd(str);
                        if (MyCmd.SOURCE_RADIO == source) {
                            String icon = "0";
                            if ((source_in & 0xff00) != 0) {
                                icon = "0x00080000";
                            }
                            mPreRadioIcon = mPreIcon = icon;
                            GlobalDefinition.setSmallLcdIcon(icon);
                        } else if (MyCmd.SOURCE_MUSIC == source || MyCmd.SOURCE_VIDEO == source || MyCmd.SOURCE_DVD == source) {
                            String icon;
                            if (MyCmd.SOURCE_MUSIC == source) {
                                icon = "0x00080800";
                            } else if (MyCmd.SOURCE_VIDEO == source) {
                                icon = "0x00082000";
                            } else {
                                icon = "0x00080000";
                            }
                            mPreIcon = icon;
                            GlobalDefinition.setSmallLcdIcon(icon);
                        }
                    }
                }
                if (mAppSource == source) {
                    mPreLcd = str;
                }
            }
        }
    }

    private String mPreRadioIcon = "0";

    private boolean m8600flash = true;

    private void updateSmallLcdTime() {
        Log.d("ccf", "updateSmallLcdTime:" + mPreLcd);
        if ("OFF".equals(mPreLcd) && mBtPhoneStatus != MyCmd.PhoneStatus.PHONE_ON && !mLcdShowVol) { // off set time
            Date curDate = new Date(System.currentTimeMillis());
            byte h = (byte) curDate.getHours();

            String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

            if ("12".equals(strTimeFormat)) {

                if (h > 12) {
                    h -= 12;
                } else if (h == 0) {
                    h = 12;
                }
                //				h |= 0x80;
            }

            byte m = (byte) curDate.getMinutes();
            //			byte s = (byte) curDate.getSeconds();

            String str = String.format(Locale.ENGLISH, "    %02d%02d", h, m);

            GlobalDefinition.setSmallLcd(str);

            if (m8600flash) {
                GlobalDefinition.setSmallLcdIcon("0x00080000");
            } else {
                GlobalDefinition.setSmallLcdIcon("0");
            }

            m8600flash = !m8600flash;
            mHandleModeKey.removeMessages(MSG_UPDATE_8600_LCD_TIME);
            mHandleModeKey.sendEmptyMessageDelayed(MSG_UPDATE_8600_LCD_TIME, 1000);
        }
    }

    private void startUpdateSmallLcdTime() {
        updateSmallLcdTime();
        mHandleModeKey.removeMessages(MSG_UPDATE_8600_LCD_TIME);
        mHandleModeKey.sendEmptyMessageDelayed(MSG_UPDATE_8600_LCD_TIME, 1000);
    }

    private void setSmallLcdEx(String str) {
        if ("OFF".equals(str)) { //off setto

            startUpdateSmallLcdTime();

            if (!mPowerOffFate) {
                GlobalDefinition.setSmallLcdAinm("1");
            } else {
                //				GlobalDef.setSmallLcd("        ");
                //				GlobalDef.setSmallLcdIcon("0");
                GlobalDefinition.setSmallLcdAinm("0");
                //				mHandleModeKey.removeMessages(MSG_UPDATE_8600_LCD_TIME);
            }
        } else {
            GlobalDefinition.setSmallLcd(str);
        }

    }

    public void setKeepAcc(int i) {
        mMcu.sendCmd(ProtocolAk47.generateProtocol1((byte) 0xc, (byte) 0x9, (byte) i));
    }

    /////////
    private boolean mNewSaveTime = false;

    private void updateSaveTimeEx(byte[] mcuTime) {
        mNewSaveTime = true;
        Log.d(TAG, "dupdateSaveTimeEx::" + (2000 + mcuTime[2]) + ":" + mcuTime[3] + ":" + mcuTime[4] + ":" + mcuTime[6] + ":" + mcuTime[7] + ":" + mcuTime[8]);

        if (mcuTime[2] == 0) {//first time
            updateTimeToMcu();
            return;
        }
        Calendar c = Calendar.getInstance();

        c.set(2000 + mcuTime[2], mcuTime[3], mcuTime[4], mcuTime[6], mcuTime[7], mcuTime[8]);

        long when = c.getTimeInMillis();

        try {
            SystemClock.setCurrentTimeMillis(when);
        } catch (Exception e) {
            Log.d(TAG, "updateSaveTimeEx:: err" + e);
        }


    }

    private void updateTimeToMcu() {

        Calendar c = Calendar.getInstance();

        byte h = (byte) c.get(Calendar.HOUR_OF_DAY);
        byte m = (byte) c.get(Calendar.MINUTE);
        byte s = (byte) c.get(Calendar.SECOND);
        byte y = (byte) (c.get(Calendar.YEAR) - 2000);
        byte mon = (byte) (c.get(Calendar.MONTH));
        byte d = (byte) c.get(Calendar.DAY_OF_MONTH);
        byte w = (byte) (c.get(Calendar.DAY_OF_WEEK));

        if (w == 1) {
            w = 7;
        } else {
            --w;
        }

        byte[] protocol = new byte[9];
        protocol[0] = 0x1;
        protocol[1] = 0x1f;
        protocol[2] = y;
        protocol[3] = mon;
        protocol[4] = d;
        protocol[5] = w;
        protocol[6] = h;
        protocol[7] = m;
        protocol[8] = s;

        mMcu.sendCmd(protocol);
    }

    private BroadcastReceiver mBroadcastReceiver;

    private void unregisterReceiver() {
        if (mBroadcastReceiver != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void registerReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
                        updateTimeToMcu();
                    }
                }
            };
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private final static String ZLINK_BROAST = "com.zjinnova.zlink";

    private void sendKeyToZlink(int code) {
        Log.d(TAG, "sendKeyToZlink::" + code);

        Intent it = new Intent(ZLINK_BROAST);
        it.putExtra("command", "REQ_SPEC_FUNC_CMD");
        it.putExtra("specFuncCode", code);

        mContext.sendBroadcast(it);
    }

    public static final String CL_CARPLAY_CONTROL = "carletter_carplay_control";

    private void sendKeyToLaite(int keycode) {
        // laite used
        Log.d("eefk", "sendKeyToLaite:" + keycode);
        Settings.System.putInt(mContext.getContentResolver(), CL_CARPLAY_CONTROL, keycode);
        mContext.getContentResolver().notifyChange(Settings.System.getUriFor(CL_CARPLAY_CONTROL), null);
    }

    private void doSpeechCarPlay() { //suding
        String car_play = Util.getProperty("car_play_connect");
        Log.d(TAG, "car_play::" + car_play);
        if ("1".equals(car_play)) {
            //			String top = AppConfig.getTopActivity();
            //			if (top != null && top.contains("com.suding.speedplay")) {
            sendKeyToZlink(1500);
            //			}
        } else {
            UtilSystem.doRunActivity(mContext, "com.suding.speedplay", "com.suding.speedplay.ui.MainActivity");
        }
    }

    private void doSpeechCarPlayLaite() { // laite

        String top = AppConfig.getTopActivity();
        Log.d("eefk", "doSpeechCarPlayLaite:" + top);
        if (top != null && top.contains("com.carletter.car")) {
            sendKeyToLaite(0x250);
        } else {
            UtilSystem.doRunActivity(mContext, "com.carletter.car", "com.carletter.car.ui.CarletterActivity");
        }
    }
}
