package com.my;

import com.my.service.MyService;

import android.os.Build;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BR extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d("BR", "ACTION_BOOT_COMPLETED");
			if(MyService.mThis==null && Build.VERSION.SDK_INT > 23){
				Intent it = new Intent(Intent.ACTION_RUN);
				it.setClass(context, MyService.class);
				context.startService(it);
			}
//			it = new Intent(Intent.ACTION_RUN);
//			it.setClass(context, VideoRecordService.class);
//			context.startService(it);
		}
	}

}
