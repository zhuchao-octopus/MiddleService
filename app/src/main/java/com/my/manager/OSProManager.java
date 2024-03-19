package com.my.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

import com.my.GlobalDef;
import com.my.canbox.Canbox;
import com.my.canbox.ReverseManager;
import com.my.cartype.CarUtil;
import com.my.manager.key.TouchKeyEvent;
import com.my.out.R;
import com.car.autotest.AutoTest;
import com.car.hardware.Mcu;
import com.common.util.AKProperty;
import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Kernel;
import com.common.util.MyCmd;
import com.common.util.ProtocolAk47;
import com.common.util.Util;
import com.common.util.UtilSystem;

import android.app.Instrumentation;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.Toast;

public class OSProManager {
	private final static String TAG = "OSProManager";

	private Mcu mMcu;

	private Context mContext;

	private static OSProManager mThis;
	private McuManager mMcuManager;

	public static OSProManager getInstanse(Context c) {
		if (mThis == null) {
			mThis = new OSProManager();
			mThis.init(c);
		}
		return mThis;
	}

	public void init(Context c) {
		mMcu = Mcu.getInstance();
		mContext = c;
		mTouchKeyEvent = new TouchKeyEvent(c);
		mMcuManager = McuManager.getInstanse();
		mMcu.setOsHandler(mOsHandler);
		registerListener();
		mOsHandler.sendEmptyMessageDelayed(MSG_NOTIFY_APP_READY, 1500);
		String s = MachineConfig.getProperty(MachineConfig.KEY_NO_REVERSE);
		if (s != null && s.equals(MachineConfig.VALUE_ON)) {
			mNoReverse = true;
		}
		initTouchMode();
		mSwitchToFrontCameraTime = MachineConfig.getPropertyInt(MachineConfig.KEY_SWITCH_TO_FRONT_CAMER);
	}

	public void initTouchMode(){
		if(mTouchKeyEvent.isExistStudyData()){
			sendTouchMode(1);
		} else {
			sendTouchMode(0);
		}
	}
	
	public static boolean mNoReverse = false;
	public static int mSwitchToFrontCameraTime = 0;
	
	private TouchKeyEvent mTouchKeyEvent;

	public static void clearReverse() {
		if (mThis != null) {
			if (mReverse == 1) {
				mThis.doReverse(0, false);
			}
		}
	}
	public static void checktartReverseAfterSleep() {
		if (mThis != null) {
			int reverse = Util.getFileValue("/sys/class/gpio-detection/car-reverse/status");
//			Log.d("add", "checktartReverseAfterSleep:"+reverse);
			if (reverse == 1) {
				mThis.mOsHandler.sendMessageDelayed(mThis.mOsHandler.obtainMessage(
						MSG_DELAY_DO_REVERSE, 1, 0), 200);
			}
		}
	}

	public static int mReverse = 0;
	// 内核与APP通讯协议
	/*
	 * 
	 * #define AK_PRO_GROUPID_COMMON 1 #define AK_PRO_GROUPID_TOUCH_KEY 2
	 * 
	 * 
	 * #define AK_PRO_SUBID_COMMON_REVERSE 1 #define AK_PRO_SUBID_TOUCH_KEY_XY 1
	 */
	public static final int AK_PRO_GROUPID_COMMON = 1;
	public static final int AK_PRO_GROUPID_TOUCH_KEY = 2;

	public static final int AK_PRO_SUBID_COMMON_REVERSE = 1;
	public static final int AK_PRO_SUBID_TOUCH_KEY_XY = 1;
	public static final int AK_PRO_SUBID_TOUCH_KEY_FIX_KEY = 2;

	public static Handler mHandlerReverse;
	private long mReverseStartTime = 0;
	private long mReverseStopTime = 0;
	private static final int REVERSE_WAIT_TIME = 1000;
	private boolean mLastIsCameraApp = false;

	boolean mSwitchToFrontCamera = false;

	private void doReverseSwitchToFront(boolean front) {

		mOsHandler.removeMessages(MSG_DELAY_SWITCH_TO_FRONT_CAMERA);
		mOsHandler.removeMessages(MSG_DELAY_DO_REVERSE);

		mOsHandler.removeMessages(MSG_CHECK_FRONT_CAMERA_SIGNAL);
		if (front) {
			mOsHandler.sendEmptyMessageDelayed(
					MSG_DELAY_SWITCH_TO_FRONT_CAMERA,
					(mSwitchToFrontCameraTime * 1000)+800);
			mOsHandler.sendEmptyMessageDelayed(MSG_CHECK_FRONT_CAMERA_SIGNAL,
					1200);

			mSwitchToFrontCamera = true;
		} else {

			mSwitchToFrontCamera = false;
		}
		ReverseManager.switchToFrontCamera(front);

	}

	private boolean isFocus3Top(String top) {
		if (CarUtil.getCanboxType() != null
				&& (CarUtil.getCanboxType().equals(
						MachineConfig.VALUE_CANBOX_FORD_SIMPLE) || CarUtil
						.getCanboxType()
						.equals(MachineConfig.VALUE_CANBOX_FORD_EXPLORER_SIMPLE))) {
			if (CarUtil.getCarType() == 2 || CarUtil.getCarType() == 3) {
				if ("com.canboxsetting/com.focussync.MainActivity".equals(top)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void doReverse(int status, boolean delay) {
		Util.setProperty(AKProperty.REVERSE, ""+status);
		if (GlobalDef.mIsTesting) {
			if (status == 1) {
				byte[] data = new byte[] { AutoTest.REVERSE_RETURN };
				AutoTest.parseTestData(data, data.length);
				return;
			}
		}
		
		if (GlobalDef.getTestingEx()) {
			Canbox canbox = CarUtil.getCanboxInstance();
			if (canbox != null) {
				try {
					byte[] param = new byte[] { 0x1, 0x10, 0x1, (byte) status };
					canbox.parseVersion(0, param);
				} catch (Exception e) {

				}
			}
		}
		
		Log.e("allen", "reverse : " + status);
		mOsHandler.removeMessages(MSG_NOTIFY_APP_REVERSE_STOP);
		mOsHandler.removeMessages(MSG_DELAY_SWITCH_TO_FRONT_CAMERA);		
		mOsHandler.removeMessages(MSG_DELAY_DO_REVERSE);
		
		if (mNoReverse || (status == 1 && mMcuManager.mPowerOffAcc)) {
			Log.e(TAG, "lock reverse : " + status);
			return;
		}
		
		mReverse = status;
		if (status == 1) {
			String top = AppConfig.getTopActivity();
			if (mLastIsCameraApp && Util.isRKSystem()) {
				// long delay = REVERSE_WAIT_TIME- (SystemClock.uptimeMillis() -
				// mReverseStopTime);
				// if (delay> 0) {
				// mOsHandler.sendMessageDelayed(mOsHandler
				// .obtainMessage(MSG_DELAY_DO_REVERSE,
				// protocol), (delay));
				// Log.d("allen", "send MSG_DELAY_DO_REVERSE start:"+delay);
				// return;
				// } else {
				// mReverseStopTime = 0;
				// mReverseStartTime = SystemClock.uptimeMillis();
				// }
			}

			BroadcastUtil.sendByCarService(mContext, Util.isRKSystem() ? null : AppConfig.PACKAGE_CAR_UI,
					MyCmd.Cmd.REVERSE_STATUS, status);

			mMcuManager.setCameraSource(MyCmd.SOURCE_REVERSE);
			mMcuManager.lockKey(McuManager.LOCK_KEY_ALL);
			mMcuManager.resetScreen0Status();
			// Log.e("allen", "reverse : " + protocol[2]);
			if (GlobalDef.getScreen1Source() == MyCmd.SOURCE_AUX
					|| GlobalDef.getScreen1Source() == MyCmd.SOURCE_DVD
					|| AppConfig.CAR_UI_FRONT_CAMERA.equals(top)
					|| AppConfig.CAR_UI_AUX_IN.equals(top)
					|| AppConfig.CAR_UI_DVD.equals(top)
					|| isFocus3Top(top)) {
				mLastIsCameraApp = true;
				ReverseManager.start(mContext, 50);
			} else {
				mLastIsCameraApp = false;
				ReverseManager.start(mContext, 0);
			}

			// UtilSystem.doRunActivity(mContext,
			// mContext.getPackageName(),
			// "com.my.canbox.ReverseActivity");
//			if (!mMcuManager.getBacklightStatus()) {
				mOsHandler.removeMessages(MSG_RECOVER_BACKLIGHT);
				mOsHandler.removeMessages(MSG_RESET_BACKLIGHT);
				mOsHandler.sendEmptyMessageDelayed(MSG_RESET_BACKLIGHT, 250);
//			}
				
			GlobalDef.wakeLock();
		} else {
			GlobalDef.wakeRelease();
			if (Util.isRKSystem()) {
				mSwitchToFrontCamera = false;

				//ww+isPX6() to fixed px6 auxin and reverse switching display blue-screen
				if (!(Util.isRK356X() || Util.isPX6() || Util.isPX30() || (Util.isPX5() && (Util.isAndroidP() || Util.isAndroidQ() || Util.isAndroidR()))) && mLastIsCameraApp && !delay) {
					mOsHandler.sendMessageDelayed(mOsHandler.obtainMessage(
							MSG_DELAY_DO_REVERSE, status, 0), 1000);
					return;

					// long delay = REVERSE_WAIT_TIME-
					// (SystemClock.uptimeMillis() - mReverseStartTime);
					// if (delay>0) {
					// mOsHandler.sendMessageDelayed(mOsHandler
					// .obtainMessage(MSG_DELAY_DO_REVERSE,
					// protocol), delay);
					// Log.d("allen", "send MSG_DELAY_DO_REVERSE stop"+delay);
					// return;
					// } else {
					// mReverseStartTime = 0;
					// mReverseStopTime = SystemClock.uptimeMillis();
					// }
				}
			}
			mOsHandler
					.sendEmptyMessageDelayed(MSG_NOTIFY_APP_REVERSE_STOP, 200);

			mOsHandler.removeMessages(MSG_RECOVER_REVERSE_OPEN_APP);
			mOsHandler.sendEmptyMessageDelayed(MSG_RECOVER_REVERSE_OPEN_APP,
					400);
			mMcuManager.lockKey(0);
			mMcuManager.recoverScreen0Status();
			if (!mMcuManager.getBacklightRecoverStatus()) {
				ReverseManager.stop();
			} else {
				mOsHandler.removeMessages(MSG_RESET_BACKLIGHT);
				mOsHandler.removeMessages(MSG_RECOVER_BACKLIGHT);
				mOsHandler.sendEmptyMessageDelayed(MSG_RECOVER_BACKLIGHT, 200);
			}

			mMcuManager.recoverBacklightStatus(0);
			if (mHandlerReverse != null) {
				mHandlerReverse.sendEmptyMessage(status);
			}
		}

	}

	private void doOsData(byte[] protocol) {
		// for (int i = 0; i < protocol.length; ++i) {
		// Log.d(TAG, i + "=" + protocol[i]);
		// }

		byte groupId = protocol[0];
		byte subId = protocol[1];
		switch (groupId) {
		case AK_PRO_GROUPID_COMMON:
			switch (subId) {
			case AK_PRO_SUBID_COMMON_REVERSE:
				if (protocol[2] == 1){
					mSimulationReverse = 0;
				}
				if (mSwitchToFrontCameraTime != 0 && protocol[2] == 0){
					doReverseSwitchToFront(true);
				} else {
					if (!mSwitchToFrontCamera){
						doReverse(protocol[2], false);
					} else {
						doReverseSwitchToFront(false);
					}
					
				}
				break;
			}
			break;
		case AK_PRO_GROUPID_TOUCH_KEY:
			switch (subId) {
			case AK_PRO_SUBID_TOUCH_KEY_XY:
				mTouchKeyEvent.process(protocol);
				break;
			case AK_PRO_SUBID_TOUCH_KEY_FIX_KEY:
				// protocol[3]=protocol[10];//test
				mTouchKeyEvent.processTouchFixKey(protocol);
				break;
			}
			break;
		}
	}

	private final static int MSG_RESET_BACKLIGHT = 0x10000;
	private final static int MSG_RECOVER_BACKLIGHT = 0x10001;
	private final static int MSG_NOTIFY_APP_REVERSE_STOP = 0x10002;

	private final static int MSG_RECOVER_REVERSE_OPEN_APP = 0x10003;

	private final static int MSG_DELAY_DO_REVERSE = 0x10004;
	
	private final static int MSG_DELAY_SWITCH_TO_FRONT_CAMERA = 0x10005;


	private final static int MSG_CHECK_FRONT_CAMERA_SIGNAL = 0x10006;
	
	private final static int MSG_NOTIFY_APP_READY = 0x10100; // handler the
																// kernel
																// reverse
	private Handler mOsHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Mcu.MSG_RECEIVE_OS_DATA:
				byte[] potocol = (byte[]) msg.obj;
				doOsData(potocol);
				break;
			case MSG_RESET_BACKLIGHT:
				mMcuManager.resetBacklightStatus(0);
				break;
			case MSG_RECOVER_BACKLIGHT:
				ReverseManager.stop();
				break;
			case MSG_NOTIFY_APP_REVERSE_STOP:
				BroadcastUtil.sendByCarService(mContext,
						Util.isRKSystem() ? null : AppConfig.PACKAGE_CAR_UI, MyCmd.Cmd.REVERSE_STATUS,
						msg.arg1);
				break;
			case MSG_NOTIFY_APP_READY:

				appReady();
				break;
			case MSG_RECOVER_REVERSE_OPEN_APP:
				mMcuManager.resetReverseOpenApp();
				break;
			case MSG_DELAY_DO_REVERSE:
				// if (msg.obj != null) {
				// doOsData((byte[]) msg.obj);
				doReverse(msg.arg1, true);
				// }
				break;
			case MSG_DELAY_SWITCH_TO_FRONT_CAMERA:
				doReverse(0, false);
				break;
			case MSG_CHECK_FRONT_CAMERA_SIGNAL:
				String source;

				// if(Util.isPX5()){
				if(Util.isRKSystem()){
					source = readLine("/sys/class/ak/source/cvbs_status");
				} else {
					source = readLine("/sys/class/misc/mst701/device/lock");
				}
				// } else {
				// source = readLine("/sys/class/misc/mst701/device/lock");
				// }
			//to do better future
			//	if (source == null || !source.equals("1")) {
			//		doReverse(0, false);
			//	}
				break;
			default:
				break;
			}
		}
	};

	private String readLine(String path) {

		File file = new File(path);

		String source = null;
		if (file.exists()) {
			BufferedReader buf;

			try {
				FileReader fr = new FileReader(file);
				buf = new BufferedReader(fr);
				source = buf.readLine();
				buf.close();
				fr.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return source;
	}
	
	private void appReady() {
//		Log.d("allen" , "appReady!!!!2500");
		mMcu.sendKernelCmd(new byte[] { 0x2, 0x1, 0x2 });
	}

	public void sendTouchMode(int mode){
		mMcu.sendKernelCmd(new byte[] { 0x2, 0x2, (byte)mode });
	}
	// key study
	private void doTouchStudy(int cmd, int data) {
		if (mTouchKeyEvent != null && mTouchKeyEvent.mTouchKeyProcessor != null) {
			switch (cmd) {
			case MyCmd.Cmd.TOUCH_STUDY_START:
				sendTouchMode(1);
				mTouchKeyEvent.mTouchKeyProcessor.setTouchCurrentMode(true);
				break;
			case MyCmd.Cmd.TOUCH_STUDY_KEY:
				mTouchKeyEvent.mTouchKeyProcessor
						.setTouchCurrentStudyKeycode(data);
				break;
			case MyCmd.Cmd.TOUCH_STUDY_CLEAR:
				mTouchKeyEvent.mTouchKeyProcessor
						.setTouchClearStudyKeycode((byte) data);
				break;
			case MyCmd.Cmd.TOUCH_STUDY_END:
				mTouchKeyEvent.mTouchKeyProcessor.setTouchCurrentMode(false);
				if (data == 1) {
					mTouchKeyEvent.mTouchKeyProcessor.saveStudyKey();
				}
				initTouchMode();
				break;
			default:
				return;
			}

		}
	}

	private BroadcastReceiver mReceiver = null;

	private void registerListener() {
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					Log.d(TAG, "registerListener" + action);
					if (action.equals(MyCmd.BROADCAST_SET_TOUCH_STUDY)) {

						int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);
						int data = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA,
								0);
						doTouchStudy(cmd, data);
					}

				}
			};
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(MyCmd.BROADCAST_SET_TOUCH_STUDY);

			mContext.registerReceiver(mReceiver, iFilter);
		}
	}

	public static int mSimulationReverse = 0;
	
	public void doSimulationReverse(byte reverse) { // only canbox can use
		Log.d("aced", "doSimulationReverse:");
		
		if (mSwitchToFrontCameraTime != 0 && reverse == 0){
			doReverseSwitchToFront(true);
		} else {
			if (!mSwitchToFrontCamera){
				doReverse(reverse, false);
			} else {
				doReverseSwitchToFront(false);
			}
			
		}
	}

	public static void simulationReverse(byte reverse) { // only canbox can use
		if (mThis != null && mThis.mOsHandler != null) {
			Log.d("aced", "simulationReverse:" + reverse + ":"
					+ mSimulationReverse + ":" + mReverse);
			if (reverse == 1) {				
				if (mReverse == 1) {
					return;
				}		
				mSimulationReverse = 1;
			} else {
				if (mSimulationReverse == 0 || mReverse == 0) {
					return;
				}
				mSimulationReverse = 0;
			}
			mThis.doSimulationReverse(reverse);
		}
	}
}
