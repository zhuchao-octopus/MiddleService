package com.my.out;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.common.util.MyCmd;
import com.common.util.UtilSystem;
import com.my.service.MyService;

public class OutApp extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (MyService.mThis == null) {
			Intent it = new Intent(this, MyService.class);
			startService(it);
		} else {

			Intent it = new Intent(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS);
			// it.putExtra(MyCmd.EXTRA_COMMON_DATA, 1);
			sendBroadcast(it);
		}
		// Log.d("ffff", "abc");

		// UtilSystem.doRunActivity(this, getPackageName(),
		// "com.my.canbox.ReverseActivity");
		// it = new Intent(this, VideoRecordService.class);
		// startService(it);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		finish();
	}
}
