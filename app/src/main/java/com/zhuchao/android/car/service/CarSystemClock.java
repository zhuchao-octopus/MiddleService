package com.zhuchao.android.car.service;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.fbase.MMLog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

public class CarSystemClock {
    private static long mMillis;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static void setCurrentTimeMillis(long millis, Context c) {
        Intent i = new Intent(MyCmd.BROADCAST_UPDATE_TIME);
        i.putExtra(MyCmd.EXTRA_COMMON_CMD, 1);
        c.sendBroadcast(i);
        mMillis = millis;
        mContext = c;
        mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 2500);
    }

    public static Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        public void handleMessage(@NonNull Message msg) {
            MMLog.d("CarSystemClock", "do setCurrentTimeMillis:" + mMillis);
            SystemClock.setCurrentTimeMillis(mMillis);
            Intent i = new Intent(MyCmd.BROADCAST_UPDATE_TIME);
            i.putExtra(MyCmd.EXTRA_COMMON_CMD, 2);
            mContext.sendBroadcast(i);
            super.handleMessage(msg);
        }
    };

    public static long currentTimeMillis() {
        return mMillis;
    }
}
