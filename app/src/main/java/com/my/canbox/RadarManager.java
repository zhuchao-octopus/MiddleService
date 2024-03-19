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
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
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

public class RadarManager {
	public final static String TAG = "RadarManager";
	private static WindowManager mWindowManager;
	private static WindowManager.LayoutParams mLayoutParams;
	private static View mView;

	public static boolean isShow = false;
	public static boolean isAddView = false;

	private static RadarUI mRadarUI;
	
	private static Context mContext;

	public static View init(Context context) {
		mContext = context;
		if (mView == null) {
			mView = ((LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.radar, null);

			mWindowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			mLayoutParams = new WindowManager.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0,
					LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_LAYOUT_NO_LIMITS
							| LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.RGBA_8888);

			mLayoutParams.gravity = Gravity.LEFT;
		
		}
		return mView;
	}

	public static void start(Context context) {
		if (CarUtil.getHideRadarUI()){
			return;
		}
		if (CarUtil.getShowRadarUIOnlyInReverse() && (OSProManager.mReverse != 1)){
			stop();
			return;
		}
		if (!isShow){// && !ReverseManager.isShow) {
			init(context);
			updateView(0);
			if (!isAddView) {
				mWindowManager.addView(mView, mLayoutParams);
				isAddView = true;
			}
			// mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
			if(mRadarUI==null){
			mRadarUI = RadarUI.getInstanse(context,
					mView.findViewById(R.id.screen1_radar), 0);
			}
			mRadarUI.onCreate();
			mRadarUI.onResume();
			isShow = true;
		}

	}
	
	public static void removeView(){
		
	}

	public static void stop() {
		if (isShow) {

			mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			if(isAddView){
				mWindowManager.removeView(mView);
				isAddView = false;
			}
			mRadarUI.onPause();
			mRadarUI.onDestroy();
			isShow = false;
		}
	}
	
	public static void updateView(int type){
		if (mView == null){
			return;
		}
		if (type == 0){
			mLayoutParams.width = LayoutParams.WRAP_CONTENT;
			mLayoutParams.height = LayoutParams.WRAP_CONTENT;
			mLayoutParams.x = 0;
			mLayoutParams.y = 0;
			mView.findViewById(R.id.reverse_left_image)
			.setVisibility(View.VISIBLE);

	
		} else {
			int mScreenHeight = 600;
			if (mContext != null) {
				DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
				//Display[] display = displayManager.getDisplays();
				//DisplayInfo outDisplayInfo = new DisplayInfo();
				//display[0].getDisplayInfo(outDisplayInfo);
                DisplayMetrics displayMetrics= mContext.getResources().getDisplayMetrics();
				mScreenHeight = displayMetrics.heightPixels; //outDisplayInfo.appHeight;
			}

			if (mScreenHeight == 480) {
				mLayoutParams.width = 64;
				mLayoutParams.height = 64;
			} else if (mScreenHeight == 720) {
				mLayoutParams.width = 84;
				mLayoutParams.height = 84;
			}  else if (mScreenHeight == 1080) {
				mLayoutParams.width = 94;
				mLayoutParams.height = 94;
			} else {
				mLayoutParams.width = 74;
				mLayoutParams.height = 74;
			}

			mLayoutParams.y = mScreenHeight - mLayoutParams.height;

			mView.findViewById(R.id.reverse_left_image)
					.setVisibility(View.GONE);
		}
		
		if (isShow){
			mWindowManager.updateViewLayout(mView, mLayoutParams);
		}
	}
}
