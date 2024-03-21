package com.zhuchao.android.car;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;

import com.common.util.Util;
import com.zhuchao.android.car.service.MyCarService;

public class PackageChangedReceiver extends BroadcastReceiver {

    public static void deleteDir(String dirPath) {
        try {
            File file = new File(dirPath);
            if (file.isFile()) {
                file.delete();
            } else {
                File[] files = file.listFiles();
                if (files == null) {
                    file.delete();
                } else {
                    for (File value : files) {
                        deleteDir(value.getAbsolutePath());
                    }
                    file.delete();
                }
            }
        } catch (Exception e) {
            Log.i("MyCarService", "deleteDir " + dirPath + " error");
            e.printStackTrace();
        }
    }

    @SuppressLint("SdCardPath")
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String packageName = intent.getData().getSchemeSpecificPart();

        MyCarService.updatePackageList();
        //		Log.d("dd", "onReceive:" + intent.getAction() + ":" );

        if (packageName != null) {
            if (packageName.contains("org.prowl.torque")) {
                if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
                    DisplayMetrics dm = context.getResources().getDisplayMetrics();
                    if (dm != null) {
                        if ((1280 == dm.widthPixels) && (720 == dm.heightPixels)) {
                            Log.i("MyCarService", "## fixup torque data");
                            if (Util.isAndroidP() || Util.isAndroidQ()) {
                                Util.copyFolder("/oem/ak_param/torque_data", "/sdcard/.torque");
                            } else {
                                Util.copyFolder("/system/etc/torque_data", "/sdcard/.torque");
                            }
                        }
                    }
                } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
                    Log.i("MyCarService", "## delete torque data");
                    deleteDir("/sdcard/.torque");
                }
            } /*else if (packageName.contains("com.zjinnova.zlink")) {
				Log.i("PackageChangedReceiver", "com.zjinnova.zlink start service z-link");
				Util.setProperty("ctl.start", "z-link");
			}*/
        }
    }
}
