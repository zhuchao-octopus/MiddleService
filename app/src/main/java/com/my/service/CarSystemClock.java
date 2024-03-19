package com.my.service;

import com.common.util.MyCmd;
import com.common.util.Util;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class CarSystemClock {
	private static long mMillis;
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

	public static Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.d("CarSystemClock", "do setCurrentTimeMillis:"+mMillis);
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
