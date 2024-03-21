package com.zhuchao.android.car.ui;

import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;

import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.manager.AutoIlluminManager;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.common.util.AKProperty;

import com.zhuchao.android.car.R;
public class BacklightPanel extends Handler {

	private static final String TAG = "BacklightPanel";
	// private final Toast mToast;
	private View mView;
	private final Context mContext;

	private Presentation mPresentation = null;
	private WindowManager mWindowManager = null;

	private WindowManager.LayoutParams mVolumeLayoutParams;

	private final BacklightUI[] mBacklightUI = new BacklightUI[BacklightUI.MAX_DISPLAY];

	private static BacklightPanel mThis;
	public BacklightPanel(Context context) {
		mContext = context;
		BacklightUI.setHandler(this);

		init();
		registerListener();
		initDefalutScreen1Backlight();
		mThis = this;
	}
	
	private void initDefalutScreen1Backlight(){
		int brightness = SystemConfig.getIntProperty(mContext,
				SystemConfig.KEY_SCREEN1_BACKLIGHT);
		if (brightness != 0) {
			Util.setFileValue(GlobalDefinition.BRIGHTNESS_SCREEN1,
					brightness);
		}
		
	}

	private void initUI(int index) {
		
		if (mBacklightUI[index] == null) {

			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (index == 0) {

				mView = inflater.inflate(R.layout.backlight_layout, null);

				mWindowManager = (WindowManager) mContext
						.getSystemService(Context.WINDOW_SERVICE);
				mVolumeLayoutParams = new WindowManager.LayoutParams();
				mVolumeLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
				mVolumeLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
						| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
				mVolumeLayoutParams.gravity = Gravity.CENTER_HORIZONTAL
						| Gravity.TOP;
				mVolumeLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
				mVolumeLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
				mVolumeLayoutParams.format = PixelFormat.RGBA_8888;

				mBacklightUI[index] = BacklightUI.getInstanse(mContext, mView,
						index);
			} else {
				DisplayManager displayManager = (DisplayManager) mContext
						.getSystemService(Context.DISPLAY_SERVICE);
				Display[] display = displayManager.getDisplays();


				View view = inflater.inflate(R.layout.backlight_screen1_layout, null);
				if (display.length > 1) {
					mPresentation = new Presentation(mContext, display[1],
							R.style.TranslucentTheme);
					mPresentation.getWindow().setType(
							(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
					mPresentation.setContentView(view);

					WindowManager.LayoutParams lp = mPresentation.getWindow()
							.getAttributes();
					lp.alpha = 0.92f;
//					lp.width = LayoutParams.WRAP_CONTENT;
					lp.height = LayoutParams.WRAP_CONTENT;

					mPresentation.getWindow().setGravity(Gravity.TOP);
					
					mBacklightUI[index] = BacklightUI.getInstanse(mContext,
							view, index);
				}

			}

		}
	}

	public void handleMessage(Message msg) {
		switch (msg.what) {
		// case BacklightUI.MSG_SHOW:
		// show();
		// break;
		case BacklightUI.MSG_HIDE:
			doHide(msg.arg1);
			break;
		case BacklightUI.MSG_SAVE_DATA:
			saveSystemBrightness(msg.arg1);
			break;
		default:
			break;
		}
	}

	// private void prepareHide() {
	// removeMessages(BacklightUI.MSG_HIDE);
	// sendEmptyMessageDelayed(BacklightUI.MSG_HIDE, DELAY_HIDE_TIME);
	// }

	private void show(int cmd) {
		doShow(cmd);
		// prepareHide();
	}

	private void doShow(int index) {

		try {
			int type = index;
			if(index<0){
				index = 0;
			}
			
			if (index < 2) {
				initUI(index);
			}
			

			mBacklightUI[index].setType(type);

			if (index == 0) {
				if (mBacklightUI[index].mPause) {
					mWindowManager.addView(mView, mVolumeLayoutParams);
					mBacklightUI[index].onCreate();
				}
				mBacklightUI[index].onResume();
			} else {
				mPresentation.show();
				mBacklightUI[index].onCreate();
				mBacklightUI[index].onResume();
			}

		} catch (Exception e) {

		}

	}

	private void doHide(int index) {
		try {
			if (index == 0) {
				if (!mBacklightUI[index].mPause) {
					mWindowManager.removeView(mView);
					mBacklightUI[index].onPause();
					mBacklightUI[index].onDestroy();
				}
			} else {
				mPresentation.dismiss();

				mBacklightUI[index].onPause();
				mBacklightUI[index].onDestroy();
			}
		} catch (Exception e) {

		}

	}

	private BroadcastReceiver mReceiver = null;

	public void registerListener() {
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					Log.d(TAG, action);
					if (action.equals(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS_SYSTEM_UI)||
							action.equals(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS_SETTINGS)||
							action.equals(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS)||
							action.equals(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS_COMMON)) {
						int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);
						int data = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
//						cmd = 1;
						if (data == 1) {
							doBacklightStep();
						}
						show(cmd);
					}
				}
			};
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS_SYSTEM_UI);
			iFilter.addAction(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS_SETTINGS);
			iFilter.addAction(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS);
			iFilter.addAction(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS_COMMON);
			mContext.registerReceiver(mReceiver, iFilter);
		}
	}

	public void unregisterListener() {
		if (mReceiver != null) {
			mContext.unregisterReceiver(mReceiver);
			mReceiver = null;
		}

	}
	

	private IPowerManager mPower;
	private void init(){ 
		try {
			PowerManager pm = (PowerManager) mContext
					.getSystemService(Context.POWER_SERVICE);
			mPower = IPowerManager.Stub.asInterface(ServiceManager
					.getService("power"));
		} catch (Exception e) {
		}
		
		GlobalDefinition.mReverseBrightness = SystemConfig.getIntProperty(mContext,
				SystemConfig.KEY_REVERSE_BACKLIGHT);
		GlobalDefinition.mReverseContrast = SystemConfig.getIntProperty(mContext,
				SystemConfig.KEY_REVERSE_CONTRAST);
//		Log.d(TAG, GlobalDef.CVBS_DEFALUT_BRIGHTNESS+":"+GlobalDef.mReverseBrightness);
//		if (GlobalDef.mReverseBrightness == 0) {
//			GlobalDef.mReverseBrightness = GlobalDef.CVBS_DEFALUT_BRIGHTNESS;
//		}		
	}
	
	private final static int STEP_NUM = 5;

	private void doBacklightStep() {
		/*try{
		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		int max = pm.getMaximumScreenBrightnessSetting();
		int min = pm.getMinimumScreenBrightnessSetting();
		int step = (max - min) / STEP_NUM;

		int value = Settings.System.getInt(mContext.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,pm.getMaximumScreenBrightnessSetting());
		value = value + step;

		if (value > max) {
			value = (value - max) + min;

		}

		//pm.setBacklightBrightness(value);
		setBacklightBrightness(mContext, value);

		Settings.System.putIntForUser(mContext.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, value,
				UserHandle.USER_CURRENT);
		Log.v(TAG, "doBacklightStep: " + value + ":" + max + ":" + step);
		}catch(Exception e){

			Log.v(TAG, "doBacklightStep: err"+e);
		}
		*/
	}
	
	private final static int DEFAULT_ILL_DOWN_BACKLIGHT = 20;
	private int mSetIll = -1;

	public static void setBacklightBrightness(Context context, int value) {
      if (android.os.Build.VERSION.SDK_INT >= 27) {
          // <uses-permission android:name="android.permission.CONTROL_DISPLAY_BRIGHTNESS" />
          DisplayManager displayManager = context.getSystemService(DisplayManager.class);
          //displayManager.setTemporaryBrightness(value);
          Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
      } else {
      	try{
					PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//					pm.setBacklightBrightness(value);
				}catch(Exception e){
					Log.v(TAG, "setBacklightBrightness: err"+e);
				}
    	}
  }

  void setTemporaryScreenBrightnessSettingOverride(Context context, int value) {
  		try {
	      if (android.os.Build.VERSION.SDK_INT >= 27) {
	          // <uses-permission android:name="android.permission.CONTROL_DISPLAY_BRIGHTNESS" />
	          DisplayManager displayManager = context.getSystemService(DisplayManager.class);
	          //displayManager.setTemporaryBrightness(value);
	      } else {
//	          mPower.setTemporaryScreenBrightnessSettingOverride(value);
	      }
      }catch(Exception e){
				Log.v(TAG, "setTemporaryScreenBrightnessSettingOverride: err"+e);
			}
  }

	
	public void doIllSwitchInner(boolean on) {
		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);

		int value = 0;
		try {
			value = Settings.System.getInt(mContext.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);
		} catch (Settings.SettingNotFoundException e) {
			//throw new RuntimeException(e);
		}

		int night = SystemConfig.getIntProperty2(mContext, AutoIlluminManager.NIGHT_BRIGHTNESS);
		
		
		boolean set = false;

		Log.d(TAG, on+"3doIllSwitch defalut:"+value+":"+night+":");
		
		if(on){						
			if (mSetIll == -1) {
//				mDayBrightness = value;
				SystemConfig.setIntProperty(mContext, AutoIlluminManager.DAY_BRIGHTNESS, value);
				
				if (night != -1) {
					value = night;
				} else {
					value = (value * 60) / 100;
				
					// Log.v(TAG, "1doIllSwitch: " + value);
					if (value < 0) {
						value = 0;
					}
				}
				mSetIll = value;
				set = true;
				
			}
		} else {
			if (mSetIll >= 0) {
				set = true;
//				if(mSetIll != value){
//					value = (value*100)/60;
//				}				
//				if (value > 255){
//					value = 255;
//				}
				mSetIll = -1;
				
				value = SystemConfig.getIntProperty(mContext, AutoIlluminManager.DAY_BRIGHTNESS);
//				SystemConfig.setIntProperty(mContext, AutoIlluminManager.DAY_BRIGHTNESS, -1);
			}
		}
		Util.setProperty(AKProperty.PROPERTY_ILL_STATE, String.valueOf(mSetIll));
		SystemConfig.setIntProperty(mContext, SystemConfig.KEY_ILL_STATE_TRIGGER, mSetIll);

		if (set) {
			try {
				Log.v(TAG, on + "doIllSwitch: " + value);
				//pm.setBacklightBrightness(value);
				setBacklightBrightness(mContext, value);
				
				Settings.System.putInt(mContext.getContentResolver(),
						Settings.System.SCREEN_BRIGHTNESS, value);
			} catch (Exception e) {
				Log.v(TAG, "doIllSwitch: err" + e);
			}
		}
		doHide(0);
	}

//	private int mDayBrightness = -1;
	private void saveSystemBrightness(int brightness){
		final int val = brightness;
		
		AsyncTask.execute(new Runnable() {
			public void run() {
				try {
					Settings.System.putInt(
							mContext.getContentResolver(),
							Settings.System.SCREEN_BRIGHTNESS, val);

					if (mSetIll != -1){
						SystemConfig.setIntProperty(mContext, AutoIlluminManager.NIGHT_BRIGHTNESS, val);
					} else {
						SystemConfig.setIntProperty(mContext, AutoIlluminManager.DAY_BRIGHTNESS, val);
					}
					
				} catch (Exception e) {

				}
				Log.d(TAG, "onProgressChanged22:" + val);
			}
		});
	}

	private void setBrightnessInner(int brightness, int type) {
		if (type == 0) {

			setTemporaryScreenBrightnessSettingOverride(mContext, brightness);
			
			removeMessages(BacklightUI.MSG_SAVE_DATA);
			sendMessageDelayed(obtainMessage(BacklightUI.MSG_SAVE_DATA, brightness, 0), 50);
			
		} else if (type == -1){

			brightness = (((brightness * 20 * 100) / 255) / 100);
			Util.setFileValue(GlobalDefinition.BRIGHTNESS_SCREEN1,
					brightness);
			SystemConfig.setIntProperty(mContext,
					SystemConfig.KEY_SCREEN1_BACKLIGHT, brightness);
		} else if (type == -3 || type == -4){
//			brightness = (((brightness * 20 * 100) / 255) / 100);
			if (brightness <= 1){
				brightness = 1;
			}
			if (brightness >= 254){
				brightness = 254;
			}
			GlobalDefinition.mReverseBrightness = (brightness);
			Util.setFileValue(GlobalDefinition.BRIGHTNESS_CVBS,
					brightness);
			Log.d(TAG, type+":"+brightness);
			SystemConfig.setIntProperty(mContext,
					SystemConfig.KEY_REVERSE_BACKLIGHT, brightness);
		}
	}
	
	private void setContrastInner(int contrast, int type) {
		if (type == -4) {
			if (contrast <= 1){
				contrast = 1;
			}
			if (contrast >= 254){
				contrast = 254;
			}
			
			GlobalDefinition.mReverseContrast = (contrast);
			Util.setFileValue(GlobalDefinition.BRIGHTNESS_CONTRAST, contrast);
			Log.d(TAG, type + ":" + contrast);
			SystemConfig.setIntProperty(mContext,
					SystemConfig.KEY_REVERSE_CONTRAST, contrast);
		}
	}
	
	public static void setBrightness(int brightness, int type) {
		if(mThis!=null){
			mThis.setBrightnessInner(brightness, type);
		}
	}
	
	
	public static void setContrast(int brightness, int type) {
		if(mThis!=null){
			mThis.setContrastInner(brightness, type);
		}
	}
	
	public static void doIllSwitch(boolean on) {
		if(mThis!=null){
			mThis.doIllSwitchInner(on);
		}
	}
}
