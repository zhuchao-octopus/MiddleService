package com.my.canbox;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.my.manager.OSProManager;
import com.my.out.R;

public class ReverseActivity extends Activity {

	private ReverseUI mRadioUI;
	private static ReverseActivity mThis;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.back);
		mRadioUI = ReverseUI.getInstance(this, findViewById(R.id.screen1_main),
				0);

		mRadioUI.onCreate();

		mThis = this;
		OSProManager.mHandlerReverse = mHandler;
	}

	private static Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (mThis != null) {
					mThis.finish();
				}
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		if (mRadioUI != null)
			mRadioUI.onResume();
	}

	protected void onPause() {
		super.onPause();
		finish();
		if (mRadioUI != null)
			mRadioUI.onPause();

	}

	protected void onDestroy() {
		super.onDestroy();
		if (mRadioUI != null)
			mRadioUI.onDestroy();
		if (mThis == this) {
			mThis = null;

		}
	}

}
