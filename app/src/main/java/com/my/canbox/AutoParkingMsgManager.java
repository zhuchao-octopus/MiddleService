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
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

import com.my.cartype.CarUtil;
import com.my.manager.OSProManager;
import com.my.out.R;
import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;

public class AutoParkingMsgManager {
	public final static String TAG = "AutoParkingMsgManager";
	private static WindowManager mWindowManager;
	private static WindowManager.LayoutParams mLayoutParams;
	private static View mView;
	private static TextView mTextWarn;
	private static TextView mTextWarn1;
	private static ImageView mImageWarn;

	public static boolean isShow = false;

	public static void init(Context context) {
		if (mView == null) {
			mView = ((LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.parking_msg, null);

			mWindowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			mLayoutParams = new WindowManager.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0,
					LayoutParams.TYPE_SYSTEM_ERROR,
					LayoutParams.FLAG_LAYOUT_IN_SCREEN
							| LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.RGBA_8888);

			// mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;

			mView.setOnClickListener(
					new OnClickListener() {
						public void onClick(View v) {
							stop();
						}
					});

			mTextWarn = (TextView) mView.findViewById(R.id.warning_text);
			mTextWarn1 = (TextView) mView.findViewById(R.id.warning_text2);
			mImageWarn = (ImageView) mView.findViewById(R.id.waring_image);

		}

	}

//	OnClickListener mOnClickListener = new OnClickListener() {
//		public void onClick(View v) {
//			stop();
//		}
//	};
	
	public static void start(Context context) {
		if (!isShow) {
			init(context);
			mWindowManager.addView(mView, mLayoutParams);
			// mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

			isShow = true;
		}

	}

	public static void updateView(Context context, int drawable, int string,
			int string2) {
		if (isShow) {
			if (mTextWarn != null) {
				mTextWarn.setText(context.getResources().getString(string));
			}
			if (mTextWarn1 != null) {
				if(string2!=0){
					mTextWarn1.setVisibility(View.VISIBLE);
					mTextWarn1.setText(context.getResources().getString(string2));
				} else {
					mTextWarn1.setVisibility(View.GONE);
				}
			}
			if (mImageWarn != null) { 				if (drawable != 0) {
					mImageWarn.setBackground(context.getResources()
							.getDrawable(drawable));
					mImageWarn.setVisibility(View.VISIBLE);
				} else {
					mImageWarn.setVisibility(View.GONE);
				}

			}

		}
	}

	public static void stop() {
		if (isShow) {

			// mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			mWindowManager.removeView(mView);

			isShow = false;
		}
	}
}
