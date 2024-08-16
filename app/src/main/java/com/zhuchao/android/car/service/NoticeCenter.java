package com.zhuchao.android.car.service;

import static com.zhuchao.android.fbase.FileUtils.EmptyString;
import static com.zhuchao.android.fbase.FileUtils.NotEmptyString;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.zhuchao.android.TPlatform;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.TAppUtils;
import com.zhuchao.android.fbase.TTask;
import com.zhuchao.android.fbase.TTaskInterface;
import com.zhuchao.android.fbase.ThreadUtils;
import com.zhuchao.android.fbase.eventinterface.InvokeInterface;
import com.zhuchao.android.fbase.eventinterface.TRequestEventInterface;
import com.zhuchao.android.net.NetworkInformation;
import com.zhuchao.android.net.TNetUtils;
import com.zhuchao.android.session.TNetTask;
import com.zhuchao.android.session.TTaskManager;
import com.zhuchao.android.session.TTaskQueue;
import com.zhuchao.android.session.TWatchManService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/*第一种方式：通过StartService启动Service
 通过startService启动后，service会一直无限期运行下去，只有外部调用了stopService()或stopSelf()方法时，该Service才会停止运行并销毁。
 要创建一个这样的Service，你需要让该类继承Service类，然后重写以下方法：
 onCreate()
 1.如果service没被创建过，调用startService()后会执行onCreate()回调；
 2.如果service已处于运行中，调用startService()不会执行onCreate()方法,多次执行startService()不会重复调用onCreate().
 onStartCommand()如果多次执行了Context的startService()方法，那么Service的onStartCommand()方法也会相应的多次调用。
 onBind()Service中的onBind()方法是抽象方法，Service类本身就是抽象类，所以onBind()方法是必须重写的，即使我们用不到。
 onDestory()在销毁的时候会执行Service该方法。
 */

public class NoticeCenter extends Service implements TNetUtils.NetworkStatusListener {
    private static final String TAG = "NoticeCenter";
    private final static String Action_OCTOPUS_HELLO = "octopus.intent.action.ACTION_WATCHMAN_HELLO";
    private final static String Action_HELLO = "android.intent.action.ACTION_WATCHMAN_HELLO";
    private final static String Action_UPDATE_NET_STATUS = "android.intent.action.UPDATE_NET_STATUS";
    private final static String Action_GET_RUNNING_TASK = "android.intent.action.GET_RUNNING_TASK";
    private final static String Action_WATCHMAN_SWITCH_ONOFF = "android.intent.action.WATCHMAN_SWITCH_ONOFF";
    private final static String Action_SystemShutdown = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
    private final static String Action_SystemReboot = "android.intent.action.ACTION_REQUEST_REBOOT";
    private final static String Action_SilentInstall = "android.intent.action.SILENT_INSTALL_PACKAGE";
    private final static String Action_SilentInstallComplete = "android.intent.action.SILENT_INSTALL_PACKAGE_COMPLETE";
    private final static String Action_SilentUninstall = "android.intent.action.SILENT_UNINSTALL_PACKAGE";
    private final static String Action_SilentClose = "android.intent.action.SILENT_CLOSE_PACKAGE";
    private final static String Action_SetAudioOutputChannel = "android.intent.action.SET_AUDIO_OUTPUT_CHANNEL";
    private final static String Action_SetAudioInputChannel = "android.intent.action.SET_AUDIO_INPUT_CHANNEL";

    private final static String Action_SilentInstall1 = "android.intent.action.SILENT_INSTALL_PACKAGE1";
    private final static String Action_SilentInstall2 = "android.intent.action.SILENT_INSTALL_PACKAGE2";

    private final TTaskQueue tTaskQueue = new TTaskQueue();
    private TNetUtils tNetUtils = null;
    private NetworkInformation networkInformation = null;
    private String pName = null;//"A40I";
    //private String pModel = null;//"A40I";
    private String pBrand = null;//"TianPu";
    private String pCustomer = null;//"TianPu";

    private boolean installedDeleteFile = false;
    private boolean installedReboot = false;
    private boolean watchManSwitchOnOff = true;

    private String VERSION_NAME = "1.0.0";
    private NotificationManager notificationManager;
    private static final String NOTIFICATION_ID = "channelId";
    private static final String NOTIFICATION_NAME = "channelId";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public NoticeCenter() {
        //MMLog.i(TAG, "TWatchManService construct with no parameters.");//1 first call
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        //.setSmallIcon(R.drawable.ic_launcher)
        //.setContentTitle("测试服务")
        //.setContentText("我正在运行");
        //设置Notification的ChannelID,否则不能正常显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_ID);
        }
        return builder.build();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void start() {
        ThreadUtils.runThread(new Runnable() {
            @Override
            public void run() {
                try {
                    VERSION_NAME = TAppUtils.getAppVersionName(NoticeCenter.this, NoticeCenter.this.getPackageName());
                    ///tTaskManager = new TTaskManager(TWatchManService.this);
                    tNetUtils = new TNetUtils(NoticeCenter.this);
                    tNetUtils.registerNetStatusCallback(NoticeCenter.this);
                    registerUserEventReceiver();
                    MMLog.d(TAG, "NoticeCenter version:" + VERSION_NAME + ", " + getFWVersionName() + " starting...");//2 first call
                    //TPlatform.SetSystemProperty("WatchMan.Service","true");//导致错误
                } catch (Exception e) {
                    //e.printStackTrace();
                    MMLog.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MMLog.setLogOnOff(false);
        //MMLog.d(TAG, "onCreate()");//2 second call
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //创建NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_ID, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        //startForeground(1, getNotification());
        start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
        //MMLog.d(TAG, "onStartCommand()");//3 call
    }

    @Override
    public IBinder onBind(Intent intent) {
        /// TODO: Return the communication channel to the service.
        ///throw new UnsupportedOperationException("Not yet implemented");
        ///MMLog.d(TAG, "WatchManService on bind");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //MMLog.d(TAG, "onDestroy()");
        try {
            unRegisterUserEventReceiver();
            tNetUtils.free();
        } catch (Exception e) {
            ///e.printStackTrace();
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerUserEventReceiver() {
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Action_HELLO);//测试
            intentFilter.addAction(Action_UPDATE_NET_STATUS);//测试
            intentFilter.addAction(Action_GET_RUNNING_TASK);
            intentFilter.addAction(Action_WATCHMAN_SWITCH_ONOFF);

            intentFilter.addAction(Action_SystemShutdown);//关机
            intentFilter.addAction(Action_SystemReboot);//重启

            intentFilter.addAction(Action_SilentInstall);//静默安装
            intentFilter.addAction(Action_SilentUninstall);//静默反安装
            intentFilter.addAction(Action_SilentClose);//静默结束
            intentFilter.addAction(Action_SetAudioOutputChannel);
            intentFilter.addAction(Action_SetAudioInputChannel);

            intentFilter.addAction(Action_SilentInstall1);//静默安装
            intentFilter.addAction(Action_SilentInstall2);//静默安装
            intentFilter.addAction(Action_SilentInstallComplete);//静默安装后删除文件

            registerReceiver(UserEventReceiver, intentFilter);
            //MMLog.d(TAG, "Register user event listener successfully.");
        } catch (Exception e) {
            MMLog.e(TAG, "Register user event listener failed!" + e.toString());
        }
    }

    public void unRegisterUserEventReceiver() {
        try {
            unregisterReceiver(UserEventReceiver);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private final BroadcastReceiver UserEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            final String action = intent.getAction();
            MMLog.d(TAG, "user event intent.Action = " + action);
            switch (Objects.requireNonNull(action)) {
                case Action_HELLO:
                    MMLog.setLogOnOff(true);
                    MMLog.log(TAG, "Hello it is ready! version:" + VERSION_NAME + ", " + getFWVersionName() + " SwitchOnOff=" + watchManSwitchOnOff);
                    if (networkInformation != null) MMLog.d(TAG, "HOST:" + networkInformation.toString());
                    else MMLog.d(TAG, "sorry!! networkInformation = null");

                    tTaskQueue.printQueue();

                    if (intent.getExtras() != null) {
                        pName = intent.getExtras().getString("pName", null);
                        ///pModel = intent.getExtras().getString("pModel", null);
                        pBrand = intent.getExtras().getString("pBrand", null);
                        pCustomer = intent.getExtras().getString("pCustomer", null);
                    }
                    break;
                case Action_UPDATE_NET_STATUS://
                    if (networkInformation != null) MMLog.d(TAG, "HOST:" + networkInformation.toString());
                    else MMLog.d(TAG, "sorry!! networkInformation = null");
                    session_jhz_test_update_session(true);
                    break;
                case Action_GET_RUNNING_TASK:
                    Action_GETRUNNINGTASK();
                    break;
                case Action_WATCHMAN_SWITCH_ONOFF:
                    //Action_WATCHMAN_SWITCH_ONOFF();
                    watchManSwitchOnOff = !watchManSwitchOnOff;
                    MMLog.i(TAG, "watchManSwitchOnOff = " + watchManSwitchOnOff);
                    break;
            }
            if (!watchManSwitchOnOff) {
                MMLog.i(TAG, "watchManSwitchOnOff = false");
                return;
            }
            ////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////
            switch (action) {
                case Action_SystemShutdown:
                    Action_SystemShutdown();
                    break;
                case Action_SystemReboot:
                    Action_SystemReboot();
                    break;
                case Action_SilentInstall:
                    if (intent.getExtras() != null) {
                        String apkFilePathName = intent.getExtras().getString("apkFilePathName");
                        boolean installedAutoStart = intent.getExtras().getBoolean("installedAutoStart", false);
                        //MMLog.i(TAG, "Silent to install " + apkFilePath);
                        installedDeleteFile = intent.getExtras().getBoolean("installedDeleteFile", false);
                        installedReboot = intent.getExtras().getBoolean("installedReboot", false);
                        if (EmptyString(apkFilePathName)) return;
                        tTaskQueue.setMaxConcurrencyCount(1);
                        TTask tTask = TTaskManager.getSingleTaskFor("Silent install " + apkFilePathName);
                        tTask.reset();
                        tTask.invoke(new InvokeInterface() {
                            @Override
                            public void CALLTODO(String tag) {
                                Action_SilentInstallAction(apkFilePathName, installedAutoStart);
                            }
                        });

                        tTaskQueue.addTTask(tTask).startWork();
                        ///if(!tTaskQueue.isEmpty())
                        ///tTaskQueue.startWork();
                        ///tTask.startAgain();
                    }
                    break;
                case Action_SilentInstall1:
                    if (intent.getExtras() != null) {
                        String apkFilePath = intent.getExtras().getString("apkFilePathName");
                        boolean installedAutoStart = intent.getExtras().getBoolean("installedAutoStart", false);
                        Action_SilentInstallAction1(apkFilePath, installedAutoStart);
                    }
                    break;
                case Action_SilentInstall2:
                    if (intent.getExtras() != null) {
                        String apkFilePath = intent.getExtras().getString("apkFilePathName");
                        boolean installedAutoStart = intent.getExtras().getBoolean("installedAutoStart", false);
                        Action_SilentInstallAction2(apkFilePath, installedAutoStart);
                    }
                    break;
                case Action_SilentInstallComplete:
                    if (intent.getExtras() != null) {
                        String apkFilePathName = intent.getExtras().getString("apkFilePathName");
                        Action_SilentInstallComplete(apkFilePathName);
                    }
                    break;
                case Action_SilentUninstall:
                    if (intent.getExtras() != null) {
                        String packageName = intent.getExtras().getString("packageName");
                        String packageName2 = intent.getExtras().getString("uninstall_pkg");
                        if (NotEmptyString(packageName)) Action_SilentUnInstallAction(packageName);
                        else if (NotEmptyString(packageName2)) Action_SilentUnInstallAction(packageName2);
                        else MMLog.log(TAG, "uninstall package name = null");
                    }
                    break;
                case Action_SilentClose:
                    if (intent.getExtras() != null) {
                        String packageName = intent.getExtras().getString("packageName");
                        if (EmptyString(packageName)) packageName = intent.getExtras().getString("close_pkg");
                        Action_SilentCLOSEAction(packageName);
                    }
                    break;
                case Action_SetAudioOutputChannel:
                    if (intent.getExtras() != null) {
                        String channel = intent.getExtras().getString("channel");
                        Action_SetAudioOutputChannel(channel);
                    }
                    break;
                case Action_SetAudioInputChannel:
                    if (intent.getExtras() != null) {
                        String channel = intent.getExtras().getString("channel");
                        Action_SetAudioInputChannel(channel);
                    }
                    break;
                default:
                    ;
                    break;
            }
        }
    };

    private void Action_GETRUNNINGTASK() {
        TAppUtils.getRunningProcess(this).print();
    }

    private void Action_SilentInstallComplete(String apkFilePath) {
        if (installedDeleteFile) {
            boolean b = FileUtils.deleteFile(apkFilePath);
            if (b) MMLog.i(TAG, "delete file successfully! ---> " + apkFilePath);
            else MMLog.i(TAG, "delete file failed! ---> " + apkFilePath);
        }

        ///MMLog.i(TAG, "Silent install installedReboot=" + installedReboot);
        ///MMLog.i(TAG, "Silent install successfully  ->" + apkFilePath);

        if (installedReboot) {
            Action_SystemReboot();
        }
    }

    private void Action_SetAudioOutputChannel(String channel) {
        TPlatform.setAudioOutputPolicy("device.audio.output.policy", channel);
        MMLog.log(TAG, "AudioOutputPolicy--->" + TPlatform.GetAudioOutputPolicy());
    }

    private void Action_SetAudioInputChannel(String channel) {
        TPlatform.setAudioInputPolicy("device.audio.input.policy", channel);
        MMLog.log(TAG, "AudioInputPolicy--->" + TPlatform.GetAudioInputPolicy());
    }

    private void Action_SilentUnInstallAction(String packageName) {
        //uninstall(packageName);
        TAppUtils.uninstallApk(packageName);
    }

    private void Action_SilentCLOSEAction(String packageName) {
        //killAppProcess(packageName);
        //killAssignPkg(packageName);
        TAppUtils.killApplication(this, packageName);
    }

    private void Action_SystemShutdown() {
        ///TPlatform.sendKeyCode(KeyEvent.KEYCODE_F9);
        ///TPlatform.sendKeyEvent(KeyEvent.KEYCODE_F9);
        TPlatform.ExecConsoleCommand("reboot -p");
    }

    private void Action_SystemReboot() {
        TPlatform.ExecConsoleCommand("reboot");
    }

    private String Action_SystemGetDeviceUUID() {
        return TPlatform.GetCPUSerialCode();
    }

    private synchronized void Action_SilentInstallAction(String apkFilePathName, boolean autostart) {
        if (EmptyString(apkFilePathName)) {
            MMLog.log(TAG, "file is not exists! --->" + apkFilePathName);
            return;
        }
        if (!apkFilePathName.toLowerCase().endsWith(".apk")) {
            MMLog.log(TAG, "file is not a valid apk file! --->" + apkFilePathName);
            return;
        }
        if (!FileUtils.existFile(apkFilePathName)) {
            MMLog.log(TAG, "file does not exists! --->" + apkFilePathName);
            return;
        }

        boolean b = TAppUtils.installSilent(this, apkFilePathName);
        if (!b) MMLog.log(TAG, "Silent install failed! ->" + apkFilePathName);
        else MMLog.log(TAG, "Silent install successfully! ->" + apkFilePathName);

        if (autostart) {
            PackageInfo packageInfo = TAppUtils.getPackageInfo(this, apkFilePathName);
            if (packageInfo != null) TAppUtils.startApp(this, packageInfo.packageName);
        }
    }

    private synchronized void Action_SilentInstallAction1(String filePath, boolean autostart) {
        TPlatform.ExecConsoleCommand("pm install -r " + filePath);
        if (autostart) {
            PackageInfo packageInfo = TAppUtils.getPackageInfo(this, filePath);
            if (packageInfo != null) TAppUtils.startApp(this, packageInfo.packageName);
        }
    }

    private synchronized void Action_SilentInstallAction2(String filePath, boolean autostart) {
        String ret = TPlatform.ExecShellCommand("pm", "install", "-f", filePath);
        MMLog.log(TAG, ret);
        if (autostart) {
            PackageInfo packageInfo = TAppUtils.getPackageInfo(this, filePath);
            if (packageInfo != null) TAppUtils.startApp(this, packageInfo.packageName);
        }
    }

    private String getFWVersionName() {
        //读取固件的MODEL 那么getString("ro.product.model");
        return Build.MODEL + "," + Build.MANUFACTURER + "," + Build.BRAND + "," + Build.DEVICE + "," + Build.VERSION.SDK_INT + "," + Build.VERSION.RELEASE + "," + VERSION_NAME; //wms version
    }

    private String getRequestJSON() {
        JSONObject jsonObj = new JSONObject();
        //networkInformation.getMAC(), networkInformation.getInternetIP(), networkInformation.regionToJson()
        try {
            jsonObj.put("name", pName); //不推送pName
            jsonObj.put("brand", pBrand);
            jsonObj.put("customer", pCustomer);
            if (networkInformation != null) {
                jsonObj.put("mac", networkInformation.getMAC());
                if (NotEmptyString(networkInformation.getInternetIP())) {
                    jsonObj.put("ip", networkInformation.getInternetIP());
                    jsonObj.put("region", networkInformation.toJson());
                } else {
                    jsonObj.put("ip", null);
                    jsonObj.put("region", null);
                }
            }
            jsonObj.put("appVersion", VERSION_NAME);//VERSION_NAME
            jsonObj.put("fwVersion", getFWVersionName());
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return jsonObj.toString();
    }

    private void session_jhz_test_update_session(boolean startAgainFlag) {
        TTaskInterface tTask = TTaskManager.getObjectByName(DataID.SESSION_UPDATE_JHZ_TEST_UPDATE_NAME);
        if (tTask == null) {
            MMLog.i(TAG, "NOT FOUND TASK SESSION_UPDATE_JHZ_TEST_UPDATE_NAME!!");
            return;
        }
        //if (EmptyString(properties.getString("product_name", null))) return;
        ///if (!tTask.isWorking()) {
        ///((TRequestEventInterface) (tTask)).setRequestParameter(getRequestJSON());
        ///if (startAgainFlag || tTask.isTimeOut(24 * 60 * 60 * 1000)) ((TTaskInterface) (tTask)).startAgain();
        ///else ((TTaskInterface) (tTask)).start();
        ///}
    }

    private void doNetStatusChangedFunction() {
        if (networkInformation != null) {
            session_jhz_test_update_session(false);
        }
    }

    @Override
    public void onNetStatusChanged(NetworkInformation networkInformation) {
        if (tNetUtils != null && tNetUtils.isAvailable()) {
            this.networkInformation = networkInformation;
            if (networkInformation.getAction() == NetworkInformation.NetworkInformation_onCONNECTIVITY) doNetStatusChangedFunction();
        }
    }

    public void test() {
        //KeyEvent.KEYCODE_Z
    }
}