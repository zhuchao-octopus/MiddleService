package com.my.canbox;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;
import android.view.View;

public class UIBase implements UIInterface{


	protected Context mContext;
	protected View mMainView;

	public boolean mPause = true;
	public int mSource;

	protected int mDisplayIndex; // 0 is main screen, 1 is second screen

	public UIBase(Context context, View view, int displayIndex) {
		mContext = context;
		mMainView = view;
		mDisplayIndex = displayIndex;
	}

	public void onCreate() {
//		mPause = false;
	}
	
	public void onDestroy() {
//		mPause = true;
	}

	public void onPause() {
		mPause = true;
	}

	public void onResume() {
		mPause = false;
	}

	public final static int SCREEN0_HIDE = 0;
	public final static int SCREEN0_SHOW_NORMAL = 1;
	public final static int SCREEN0_SHOW_FULLSCREEN = 2;

	
	public int getScreen0Type() {
		return SCREEN0_HIDE;
	}

}
