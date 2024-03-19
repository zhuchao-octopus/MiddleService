package com.my.out;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;




import com.common.util.MyCmd;
import com.common.util.UtilSystem;
import com.my.service.MyService;

public class VolumeActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent it = new Intent(MyCmd.BROADCAST_START_VOLUMESETTINGS);
		it.setPackage(getPackageName());
		sendBroadcast(it);
		finish();
	}
}
