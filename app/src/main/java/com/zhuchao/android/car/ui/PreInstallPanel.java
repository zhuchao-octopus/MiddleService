package com.zhuchao.android.car.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageInstaller.SessionCallback;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.common.util.MachineConfig;
import com.common.util.Util;
import com.zhuchao.android.car.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class PreInstallPanel extends Handler {

    private static final String TAG = "PreInstallPanel";
    // private final Toast mToast;
    private final View mView;
    private final Context mContext;
    private TextView mMessage;

    private final static int MSG_SHOW = 0;
    private final static int MSG_HIDE = 1;
    private final static int MSG_INSTALL = 2;

    public boolean mShown = false;

    private WindowManager mWindowManager = null;

    private final WindowManager.LayoutParams mVolumeLayoutParams;

    private final TextView mTextView;

    private final String mDefaultKeyboard;

    private final PackageInstaller mPackageInstaller;

    public PreInstallPanel(Context context) {
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.pre_intall_view, null);

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mVolumeLayoutParams = new WindowManager.LayoutParams();
        mVolumeLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        mVolumeLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        // mVolumeLayoutParams.flags |=
        // WindowManager.LayoutParams.FLAG_FULLSCREEN |
        // WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        mVolumeLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        mVolumeLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mVolumeLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mVolumeLayoutParams.format = PixelFormat.RGBA_8888;
        mTextView = mView.findViewById(R.id.install_apk_status);

        View v = mView.findViewById(R.id.install_quit);
        // v.setVisibility(View.GONE);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doHide();
            }
        });

        mDefaultKeyboard = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_KEYBOARD);
        mPackageInstaller = mContext.getPackageManager().getPackageInstaller();
        try {
            mPackageInstaller.unregisterSessionCallback(mSessionCallback);
        } catch (Exception ignored) {
        }
        mPackageInstaller.registerSessionCallback(mSessionCallback);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SHOW:
                show();
                break;
            case MSG_HIDE:
                doHide();
                break;
            case MSG_INSTALL:
                if (mInstallIndex < mAPK.size()) {
                    APKStatus ak = mAPK.get(mInstallIndex);
                    silentInstall(ak);
                }
                break;
            default:
                break;
        }
    }

    public void show() {
        doShow();
    }

    private void doShow() {
        if (!mShown) {
            mWindowManager.addView(mView, mVolumeLayoutParams);
            mShown = true;
        }
    }

    private void doHide() {
        if (mShown) {
            removeMessages(MSG_HIDE);
            mWindowManager.removeView(mView);
            mShown = false;
            Util.sudoExecNoCheck("sync");
        }

    }

    public static class APKStatus {
        public String mName;
        public String mPackageName;
        public int mIndex;
        public int sessionId;

        public APKStatus(String name, String pName) {
            mName = name;
            mPackageName = pName;
            sessionId = mIndex = 0;

        }
    }

    private final ArrayList<APKStatus> mAPK = new ArrayList<APKStatus>();

    public void addInstallApk(String name) {
        File f = new File(name);
        Log.d(TAG, "addInstallApk " + f.exists());
        if (f.exists()) {
            String packageName = "";
            PackageInfo packageInfo = mContext.getPackageManager().getPackageArchiveInfo(name, PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
            if (packageInfo != null) {
                packageName = packageInfo.packageName;
            }

            APKStatus a = new APKStatus(name, packageName);

            mAPK.add(a);
            updateView();
        }
    }


    public void updateStatus(String name, int status) {

        //		for (int i = 0; i < mAPK.size(); ++i) {
        //			APKStatus apk = mAPK.get(i);
        //		//	Log.d(TAG, name+ ":updateStatus:" + apk.mPackageName);
        //			if (name.equals(apk.mPackageName)) {
        //				apk.mIndex = status;
        //			}
        //		}

        if (mInstallIndex < mAPK.size()) {
            APKStatus apk = mAPK.get(mInstallIndex);
            apk.mIndex = status;
        }

        updateView();
    }

    private void updateView() {
        boolean installFinish = true;
        String s = "";
        for (int i = 0; i < mAPK.size(); ++i) {
            APKStatus apk = mAPK.get(i);

            String name = apk.mName;
            name = name.substring(name.lastIndexOf("/") + 1);

            s += name + "   ";
            if (apk.mIndex == 0) {
                installFinish = false;
                s += mContext.getString(R.string.installing);
            } else if (apk.mIndex == 1) {
                s += "OK";
            } else {
                s += "NG";
            }
            s += "\n";
        }

        //		if (installFinish) { // finish
        //			sendEmptyMessageDelayed(MSG_HIDE, 3000);
        //			mView.findViewById(R.id.install_quit).setVisibility(View.VISIBLE);
        //		} else {
        mTextView.setText(s);

        //		}
    }

    private final static String PRE_APP_PATH = "/mnt/paramter/apk/";
    private final static String[] PRE_APP = {"EsFileExplorer", "Instructions", "AdobeReaderPDF", "trskeyboard"};

    private final static String INSTALL_CONFIG = "/mnt/paramter/apk/install_config.txt";
    private int mInstallIndex = 0;

    public boolean installPreInstallApp() {
        File f = new File(INSTALL_CONFIG);
        if (f.exists()) {

            FileReader fr = null;
            String data = null;
            try {
                fr = new FileReader(INSTALL_CONFIG);
                BufferedReader reader = new BufferedReader(fr);
                data = reader.readLine();
                reader.close();
                fr.close();
            } catch (Exception e) {
            }
            if (data != null) {
                String[] ss = data.split(",");
                for (String s : ss) {
                    addInstallApk(PRE_APP_PATH + s + ".apk");
                }
            }
        }
        int i = 0;

        for (; i < PRE_APP.length; ++i) {
            String s = PRE_APP[i];
            addInstallApk(PRE_APP_PATH + s + ".apk");
            //			silentInstall(PRE_APP_PATH + s + ".apk");
        }

        if (mAPK.size() > 0) {
            sendEmptyMessage(MSG_INSTALL);
            return true;
        } else {
            sendEmptyMessageDelayed(MSG_HIDE, 1000);
        }
        return false;
    }

    /*
        class PackageInstallObserver extends IPackageInstallObserver.Stub {
            public void packageInstalled(String packageName, int returnCode) {
                Log.i(TAG, packageName + " returnCode=" + returnCode +":"+mInstallIndex+":"+mAPK.size());
                updateStatus(packageName, returnCode);
                mInstallIndex++;
                if (mInstallIndex < mAPK.size()) {
                    sendEmptyMessage(MSG_INSTALL);
                } else {
                    post(new Runnable() {
                        public void run() {
                            (mView.findViewById(R.id.install_finish)).setVisibility(View.VISIBLE);
                            (mView.findViewById(R.id.tr_progress)).setVisibility(View.GONE);
                        }
                    });

                    sendEmptyMessageDelayed(MSG_HIDE, 4000);

                }
                if (mDefaultKeyboard != null) {
                    if (mDefaultKeyboard.contains(packageName)) {
                        Log.i(TAG, " update=" + mDefaultKeyboard);
                        InputMethodManager mImm = (InputMethodManager) mContext
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        mImm.setInputMethod(null, mDefaultKeyboard);
                    }
                }
            }

        }


        public void silentInstall(String path) {

            Log.e(TAG, "silentInstall path:" + path);
            File file = new File(path);
            if (!file.exists()) {
                Log.e(TAG, "silentInstall not found path:" + path);
                return;
            }
            Uri uri = Uri.fromFile(file);

            PackageManager pm = mContext.getPackageManager();
            PackageInstallObserver observer = new PackageInstallObserver();
            try {
                pm.installPackage(uri, observer, 0, null);
                Log.e(TAG, "not support");
            } catch (Exception e) {
                Log.e(TAG, "silentInstall fail:" + e);

            }
        }*/
    private static final String ACTION_PREINSTALL_COMPLETE = "com.android.ACTION_PREINSTALL_COMPLETE";

    private void onInstallResult(int sessionId, int returnCode) {
        String packageName = "";
        int i;
        for (i = 0; i < mAPK.size(); i++) {
            APKStatus apk = mAPK.get(i);
            if (sessionId == apk.sessionId) {
                packageName = apk.mPackageName;
                break;
            }
        }
        if (i >= mAPK.size()) {
            Log.e(TAG, "can't find packagename by sessionid:" + sessionId);
            return;
        }

        Log.i(TAG, packageName + " onInstallResult: returnCode=" + returnCode + ":" + mInstallIndex + ":" + mAPK.size());

        updateStatus(packageName, returnCode);
        mInstallIndex++;
        if (mInstallIndex < mAPK.size()) {
            sendEmptyMessage(MSG_INSTALL);
        } else {
            post(new Runnable() {
                public void run() {
                    (mView.findViewById(R.id.install_finish)).setVisibility(View.VISIBLE);
                    (mView.findViewById(R.id.tr_progress)).setVisibility(View.GONE);
                }
            });

            sendEmptyMessageDelayed(MSG_HIDE, 4000);
            Intent intent = new Intent(ACTION_PREINSTALL_COMPLETE);
            intent.putExtra("APKS", mAPK.size());
            mContext.sendBroadcast(intent);
        }
        if (mDefaultKeyboard != null) {
            if (mDefaultKeyboard.contains(packageName)) {
                Log.i(TAG, "update=" + mDefaultKeyboard);
                //				InputMethodManager mImm = (InputMethodManager) mContext
                //						.getSystemService(Context.INPUT_METHOD_SERVICE);
                //				mImm.setInputMethod(null, mDefaultKeyboard);
                Log.i(TAG, "## set ENABLED_INPUT_METHODS & DEFAULT_INPUT_METHOD to " + mDefaultKeyboard);
                try {
                    android.provider.Settings.Secure.putString(mContext.getContentResolver(), android.provider.Settings.Secure.ENABLED_INPUT_METHODS, mDefaultKeyboard);
                    android.provider.Settings.Secure.putString(mContext.getContentResolver(), android.provider.Settings.Secure.DEFAULT_INPUT_METHOD, mDefaultKeyboard);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void silentInstall(final APKStatus apkStatus) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doSilentInstall(apkStatus);
            }
        }).start();
    }

    private static final String BROADCAST_ACTION = "com.android.packageinstaller.ACTION_INSTALL_COMMIT";

    public void doSilentInstall(APKStatus apkStatus) {
        if (apkStatus == null || apkStatus.mName == null) {
            Log.e(TAG, "silentInstall param is null");
            return;
        }
        try {
            String path = apkStatus.mName;
            Log.e(TAG, "silentInstall path:" + path);
            final File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                Log.e(TAG, "silentInstall not found path:" + path);
                return;
            }
            long sizeBytes = 0;
            sizeBytes = file.length();
            InputStream in = new FileInputStream(path);

            int sessionId = mPackageInstaller.createSession(new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL));
            PackageInstaller.Session session = mPackageInstaller.openSession(sessionId);
            apkStatus.sessionId = sessionId;
            OutputStream out = session.openWrite("AKPreInstaller", 0, sizeBytes);
            session.setStagingProgress(0);

            int total = 0;
            byte[] buffer = new byte[1024 * 1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                total += len;
                out.write(buffer, 0, len);
                if (sizeBytes > 0) {
                    float fraction = ((float) len / (float) sizeBytes);
                    session.setStagingProgress(fraction);
                }
            }

            session.fsync(out);
            in.close();
            out.close();

            Log.d(TAG, sessionId + ":" + path + " install success: " + total + " bytes");

            PendingIntent broadCastTest = PendingIntent.getBroadcast(mContext, sessionId, new Intent(BROADCAST_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);

            session.commit(broadCastTest.getIntentSender());
            session.close();
        } catch (Exception e) {
            //				ex.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    private final SessionCallback mSessionCallback = new SessionCallback() {
        @Override
        public void onProgressChanged(int sessionId, float progress) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onProgressChanged " + sessionId + " " + progress);
        }

        @Override
        public void onFinished(int sessionId, boolean success) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onFinished " + sessionId + " " + success);
            onInstallResult(sessionId, success ? 1 : 0);
        }

        @Override
        public void onCreated(int sessionId) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onCreated " + sessionId);
        }

        @Override
        public void onBadgingChanged(int sessionId) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onBadgingChanged " + sessionId);
        }

        @Override
        public void onActiveChanged(int sessionId, boolean active) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onActiveChanged " + sessionId + " " + active);
        }
    };
}
