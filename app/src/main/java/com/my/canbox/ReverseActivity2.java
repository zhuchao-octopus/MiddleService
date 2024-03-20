package com.my.canbox;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.my.out.R;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;

public class ReverseActivity2 extends Activity {

	ReverseUI mRadioUI;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.back);
		Log.d("aa", "onCreate");
		mRadioUI = mRadioUI.getInstance(this, findViewById(R.id.screen1_main),
				0);

		mRadioUI.onCreate();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mRadioUI != null)
			mRadioUI.onResume();
	}

	protected void onPause() {
		super.onPause();
		if (mRadioUI != null)
			mRadioUI.onPause();

	}

	protected void onDestroy() {
		super.onDestroy();
		if (mRadioUI != null)
			mRadioUI.onDestroy();

		BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
	}

}
