package com.zhuchao.android.car;

import com.zhuchao.android.car.service.MyCarService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;


public class GlobalBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "GlobalBroadcastReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
			Log.d(TAG, "Got Intent.ACTION_BOOT_COMPLETED");
			if(MyCarService.mThis == null){
				Intent it = new Intent(Intent.ACTION_RUN);
				it.setClass(context, MyCarService.class);
				context.startService(it);
			}
			///it = new Intent(Intent.ACTION_RUN);
			///it.setClass(context, VideoRecordService.class);
			///context.startService(it);
		}
	}

}
