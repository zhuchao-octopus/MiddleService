package com.my.manager;

import java.util.Calendar;

import com.my.out.R;
import com.car.hardware.Mcu;
import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.Kernel;
import com.common.util.MyCmd;
import com.common.util.ProtocolAk47;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.common.util.UtilCarKey;

import android.os.PowerManager;
import android.app.Instrumentation;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.Toast;

import android.os.UserHandle;
public class AutoIlluminManager {
	private final static String TAG = "AutoIlluminManager";

    private static final int MSG_UPDATE_AUTO_ILLUMIN = 1;
    
	private static final int UPDATE_AUTO_ILLUMIN_TIME = 60000;	// 60 second
	
	private static final String MCU_ILL_CTRL_NODE = "/sys/class/ak/source/illuminsw";
	private static final int ILLUMIN_MODE_BY_CAR = 0;	// deteminated by car ill signal
	private static final int ILLUMIN_MODE_NIGHT = 1;	// always dim screen and LED on
	private static final int ILLUMIN_MODE_DAY = 2;		// always not dim screen and LED ff

	private final static String AUTO_ILL_ENABLE = "auto_ill_enable";
	private final static String AUTO_ILL_START_HOUR = "auto_ill_start_hour";
	private final static String AUTO_ILL_START_MINUTE = "auto_ill_start_minute";
	private final static String AUTO_ILL_STOP_HOUR = "auto_ill_stop_hour";
	private final static String AUTO_ILL_STOP_MINUTE = "auto_ill_stop_minute";

	private Context mContext;
	
	private static SettingsObserver mSettingsObserver;

	private static AutoIlluminManager mThis;
	
	public static final String NIGHT_BRIGHTNESS = "night_brightness";
	public static final String DAY_BRIGHTNESS = "day_brightness";
		
	public static AutoIlluminManager getInstanse(Context c) {
		if (mThis == null && c != null) {
			mThis = new AutoIlluminManager();
			mThis.init(c);
		}
		return mThis;
	}

	public void init(Context c) {
		mContext = c;
		mSettingsObserver = new SettingsObserver(new Handler(), mContext);
		mSettingsObserver.startObserving();
		mAutoIlluminHandler.sendEmptyMessageDelayed(MSG_UPDATE_AUTO_ILLUMIN, 1);	
		
		int value = SystemConfig.getIntProperty(mContext, DAY_BRIGHTNESS);
		if(value != 0){
			PowerManager pm = (PowerManager) mContext
					.getSystemService(Context.POWER_SERVICE);
			
			try {
				Log.v(TAG, "AutoIlluminManager: recover:" + value);
				//pm.setBacklightBrightness(value);
				com.my.ui.BacklightPanel.setBacklightBrightness(c, value);
				
				///Settings.System.putIntForUser(mContext.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, value,UserHandle.USER_CURRENT);
				
                ///SystemConfig.setIntProperty(mContext, AutoIlluminManager.DAY_BRIGHTNESS, -1);
			} catch (Exception e) {
				Log.v(TAG, "doIllSwitch: err" + e);
			}
		}
	}

	private Handler mAutoIlluminHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_AUTO_ILLUMIN:
				updateIlluminMode();
				mAutoIlluminHandler.sendEmptyMessageDelayed(MSG_UPDATE_AUTO_ILLUMIN,
						UPDATE_AUTO_ILLUMIN_TIME);
				break;
			default:
				break;
			}
		}
	};
	
	public void updateIlluminMode() {
		boolean autoIlluminEnable = 
				(0!=Settings.Global.getInt(mContext.getContentResolver(), AUTO_ILL_ENABLE, 0));

		int autoIlluminMode = getIlluminMode();
		
		if (!autoIlluminEnable) {
			if (autoIlluminMode!=ILLUMIN_MODE_BY_CAR) {
				setIlluminMode(ILLUMIN_MODE_BY_CAR);
			}
			return;
		}
		
		final Calendar now = Calendar.getInstance();

		int illStartHour = Settings.Global.getInt(mContext.getContentResolver(), AUTO_ILL_START_HOUR, 19);
		int illStartMinute = Settings.Global.getInt(mContext.getContentResolver(), AUTO_ILL_START_MINUTE, 0);
		Calendar start = (Calendar) now.clone();
		start.set(Calendar.HOUR_OF_DAY, illStartHour);
		start.set(Calendar.MINUTE, illStartMinute);
		
		int illStopHour = Settings.Global.getInt(mContext.getContentResolver(), AUTO_ILL_STOP_HOUR, 7);
		int illStopMinute = Settings.Global.getInt(mContext.getContentResolver(), AUTO_ILL_STOP_MINUTE, 0);
		Calendar stop = (Calendar) now.clone();
		stop.set(Calendar.HOUR_OF_DAY, illStopHour);
		stop.set(Calendar.MINUTE, illStopMinute);
				
		if (start.before(stop)) {
			// start time is before stop time
			if (now.before(start)) {
				setIlluminMode(ILLUMIN_MODE_BY_CAR);
			} else if (now.after(stop)) {
				setIlluminMode(ILLUMIN_MODE_BY_CAR);
			} else {
				setIlluminMode(ILLUMIN_MODE_NIGHT);
			}
		} else if (start.after(stop)) {
			// start time is after stop time
			if (now.before(stop)) {
				setIlluminMode(ILLUMIN_MODE_NIGHT);
			} else if (now.after(start)) {
				setIlluminMode(ILLUMIN_MODE_NIGHT);
			} else {
				setIlluminMode(ILLUMIN_MODE_BY_CAR);
			}
		}

	}
	
	private int getIlluminMode() {
		int mode = Util.getFileValue(MCU_ILL_CTRL_NODE);
		if ( (mode > ILLUMIN_MODE_DAY) || 
				(mode < ILLUMIN_MODE_BY_CAR) ) {
			mode = ILLUMIN_MODE_BY_CAR;
		}
		return mode;
	}
	private void setIlluminMode(int mode) {
		if ( (mode > ILLUMIN_MODE_DAY) || 
				(mode < ILLUMIN_MODE_BY_CAR) ) {
			mode = ILLUMIN_MODE_BY_CAR;
		}
		Util.setFileValue(MCU_ILL_CTRL_NODE, mode);		
	}

    /** Observer to watch for changes to the setting */
    private class SettingsObserver extends ContentObserver {

        private Context mContext;

        SettingsObserver(Handler handler, Context context) {
            super(handler);
            mContext = context;
        }

        void startObserving() {
            ContentResolver resolver = mContext.getContentResolver();
            
            resolver.registerContentObserver(Settings.Global
                    .getUriFor(AUTO_ILL_ENABLE), false, this);
            resolver.registerContentObserver(Settings.Global
                    .getUriFor(AUTO_ILL_START_HOUR), false, this);
            resolver.registerContentObserver(Settings.Global
                    .getUriFor(AUTO_ILL_START_MINUTE), false, this);
            resolver.registerContentObserver(Settings.Global
                    .getUriFor(AUTO_ILL_STOP_HOUR), false, this);
            resolver.registerContentObserver(Settings.Global
                    .getUriFor(AUTO_ILL_STOP_MINUTE), false, this);            
        }

        void stopObserving() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateIlluminMode();
        }
    }
    
    public static void updateIlluminModeEx(){
    	if (mThis!=null){
    		mThis.updateIlluminMode();
    	}
    }
}
