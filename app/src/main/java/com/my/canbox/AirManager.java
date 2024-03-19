package com.my.canbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import android.app.Activity;
import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.my.GlobalDef;
import com.my.cartype.CarUtil;
import com.my.manager.OSProManager;
import com.my.out.R;
import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;

public class AirManager {

	private static WindowManager mWindowManager;
	private static WindowManager.LayoutParams mLayoutParams;
	private static View mView;

	public static boolean isShow = false;

	private static Presentation mPresentation = null;

	private static AirUI mUI;
	public static boolean mShowScreen1 = false;

	public static void reinit(Context context) {
		stop();
		mUI = null;
		mView = null;
		init(context);
	}

	private static void init(Context context) {
		if (mView == null) {

			mView = ((LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.air_toyota_screen1, null);

			mLayoutParams = new WindowManager.LayoutParams(

			LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0,
					LayoutParams.TYPE_SYSTEM_ERROR,
					LayoutParams.FLAG_LAYOUT_NO_LIMITS
							| LayoutParams.FLAG_LAYOUT_IN_SCREEN,
					PixelFormat.RGBA_8888);

			String s = MachineConfig
					.getPropertyOnce(MachineConfig.KEY_SCREEN1_VIEW);
			//s = "1"; // test
			if (s != null) {
				mShowScreen1 = true;
				DisplayManager displayManager = (DisplayManager) context
						.getSystemService(Context.DISPLAY_SERVICE);
				Display[] display = displayManager.getDisplays();

				if (display.length > 1) {
					mPresentation = new Presentation(context, display[1],
							R.style.TranslucentTheme2);
					mPresentation.getWindow().setType(
							(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
					mPresentation.setContentView(mView);

					// mPresentation = new Presentation(getApplicationContext(),
					// display[1], R.style.TranslucentTheme);
					WindowManager.LayoutParams lp = mPresentation.getWindow()
							.getAttributes();
					lp.alpha = 0.98f;
					// lp.width = LayoutParams.WRAP_CONTENT;
					// lp.height = LayoutParams.WRAP_CONTENT;

					mPresentation.getWindow().setGravity(Gravity.BOTTOM);

				}
			} else {
				mPresentation = null;
			}

			if (mPresentation == null) {
				mWindowManager = (WindowManager) context
						.getSystemService(Context.WINDOW_SERVICE);

			}

			mView.findViewById(R.id.air_control_back).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub

							stop();
						}
					});
		}

	}

	private static final int HIDE = 0;
	private static Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE:
				stop();
				break;
			}

		}
	};

	public static void startAll(Context context) {
		AirUI.mStyle = AirUI.STYLE_ALL;
		start(context, null);
	}

	public static void start(Context context, byte[] airData) {
		if (mView == null) {
			init(context);
		}

		// mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		if (mUI == null) {
			mUI = AirUI.getInstanse(context,
					mView.findViewById(R.id.screen1_main), 0);

		}

		mUI.setAirData(airData);

		if (airData == null || (airData[0] & 0x80) != 0) {
			if (!isShow) {

				if (mPresentation != null) {
					mPresentation.show();
				} else {
					mWindowManager.addView(mView, mLayoutParams);
				}

				mUI.onCreate();
				isShow = true;

				mUI.onResume();

			}
			prepareHide();
		} else {
			if (isShow) {
				stop();
			}
		}
	}
	
	public static boolean mSetByUI = false;
	public static void sendAirFunchtion(int i){
		if(mUI!=null){
			mSetByUI = true;
			mUI.sendAirFunchtion(i);
		}
	}

	public static void prepareHide() {
		// if(mStyle != AirUI.STYLE_ALL){
		mHandler.removeMessages(HIDE);
		mHandler.sendEmptyMessageDelayed(HIDE, 5500);
		// }
	}

	public static void stop() {
		if (isShow) {
			AirUI.mStyle = 0;
			if (mPresentation != null) {
				mPresentation.dismiss();
			} else {
				mWindowManager.removeView(mView);
			}
			mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

			mUI.onPause();
			mUI.onDestroy();

			isShow = false;
		}
	}
}
