package com.my.canbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.my.cartype.CarUtil;
import com.my.manager.OSProManager;
import com.my.out.R;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;

public class ReverseActivity extends Activity {

	private ReverseUI mRadioUI;
	private static ReverseActivity mThis;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.back);
		mRadioUI = ReverseUI.getInstanse(this, findViewById(R.id.screen1_main),0);

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
