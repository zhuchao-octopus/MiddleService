package com.zhuchao.android.car.cartype.xinbasi;

import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.util.AppConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.McuManager;
import com.zhuchao.android.car.R;

public class HaferH3Xinbas extends Canbox {

	public HaferH3Xinbas() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });
		updateCanboxKeySettings();
	}

	public void startConnect() {
		super.startConnect();
//		byte[] data = new byte[] { (byte) 0xff, 0x1, (byte) 0x7f };
//		sendDataToCanbox(data, data.length);
		mRepeatConnectCount--;
		if (mRepeatConnectCount > 0) {
			mHandler.sendEmptyMessageDelayed(CONNECT_REPEAT, 500);
		}
		updateTime();
	}
	public void stopConnect() {
		super.stopConnect();
		mHandler.removeMessages(CONNECT_REPEAT);
		mRepeatConnectCount = 50000;
	}
	
	private byte[][] KEYS_WHEEL;
	private final static byte[][] KEYS_WHEEL_17 = {
			{ 0x1, AK_KEYPAD_VOLUME_A }, { 0x2, AK_KEYPAD_VOLUME_D },
			{ 0x3, KEY_NEXTSONG }, { 0x4, KEY_PREVIOUSSONG },
			{ 0x7, KEY_SOURCE }, { 0x8, KEY_MUTE }, { 0x9, KEY_BT_DIAL },
			{ 0xa, MyCmd.Keycode.SPEECH },

			{ 0x20, KEY_POWER }, { 0x21, MyCmd.Keycode.KEY_SEEK_PREV },
			{ 0x22, MyCmd.Keycode.KEY_SEEK_NEXT }, { 0x23, KEY_BT_DIAL },
			{ 0x24, KEY_BT_HANG }, { 0x25, KEY_FM }, { 0x26, KEY_MUTE },
			{ 0x27, KEY_MUTE }, { 0x28, KEY_SOURCE },

			{ 0x29, KEY_HOME }, { 0x2a, KEY_GPS },
			{ 0x2b, MyCmd.Keycode.BRIGHTNESS }, { 0x2c, MyCmd.Keycode.HOME },
			{ 0x2d, MyCmd.Keycode.KEY_RADIO_PS }, { 0x2e, MyCmd.Keycode.BT },

			{ (byte) 0xf0, AK_KEYPAD_VOLUME_D },
			{ (byte) 0xf1, AK_KEYPAD_VOLUME_A }, };

	private final static byte[][] KEYS_WHEEL_NORMAL = {
			{ 0x1, AK_KEYPAD_VOLUME_A }, { 0x2, AK_KEYPAD_VOLUME_D },
			{ 0x3, KEY_NEXTSONG }, { 0x4, KEY_PREVIOUSSONG },
			{ 0x7, KEY_SOURCE }, { 0x8, KEY_MUTE }, { 0x9, KEY_BT_DIAL },
			{ 0xa, KEY_BT_HANG },

			{ 0x20, KEY_POWER }, { 0x21, MyCmd.Keycode.KEY_SEEK_PREV },
			{ 0x22, MyCmd.Keycode.KEY_SEEK_NEXT }, { 0x23, KEY_BT_DIAL },
			{ 0x24, KEY_BT_HANG }, { 0x25, KEY_FM }, { 0x26, KEY_SET },
			{ 0x27, KEY_MUTE }, { 0x28, KEY_SOURCE },

			{ 0x29, KEY_HOME }, { 0x2a, KEY_GPS },
			{ 0x2b, MyCmd.Keycode.BRIGHTNESS },
			{ 0x2c, MyCmd.Keycode.KEY_RADIO_SCAN },
			{ 0x2d, MyCmd.Keycode.KEY_RADIO_PS }, { 0x2e, MyCmd.Keycode.BT },

			{ (byte) 0xf0, AK_KEYPAD_VOLUME_A },
			{ (byte) 0xf1, AK_KEYPAD_VOLUME_D }, };

	public void updateCanboxKeySettings() {
		if (CarUtil.getKeyType() == 1) {
			KEYS_WHEEL = KEYS_WHEEL_17;
		} else {
			KEYS_WHEEL = KEYS_WHEEL_NORMAL;
		}
	}

	private boolean isOneKey(byte b) {
		return ((b & 0xff) == 0xf0) || ((b & 0xff) == 0xf1) || ((b & 0xff) == 0x0a);
	}

	private void parseWheelKey(byte[] data) {
		if (doKeyStudy(data[2], data[3])){
			return;
		}
		byte key = 0;
		for (int i = 0; i < KEYS_WHEEL.length; ++i) {
			if (KEYS_WHEEL[i][0] == data[2]) {
				key = KEYS_WHEEL[i][1];
				break;
			}
		}

		if (key != 0) {

			if (isOneKey(data[2])) {
				int step = data[3] & 0xff;
				// step = 0x10;
				if (step > 0 && step <= 8) {
					doKeyStep(key, step);
				} else {
					McuManager mcu = McuManager.getInstance();
					if (mcu != null) {
						int v = mcu.getMcuVolume();
						switch (key) {
						case AK_KEYPAD_VOLUME_A:
							// if (v >= McuManager.MAX_VOLUME) {
							// return;
							// }
							v += step;
							break;
						case AK_KEYPAD_VOLUME_D:
							// if (v <= 0) {
							// return;
							// }
							v -= step;
							break;
						}

						if (v < 0) {
							v = 0;
						} else if (v > McuManager.MAX_VOLUME) {
							v = McuManager.MAX_VOLUME;
						}
						mcu.setVolume(v);
						Util.setFileValue("/sys/class/ak/source/beep", "2");
					}
				}
			} else if (isOneKey(data[2])) {
				doKey(key, data[3]);
				Util.doSleep(10);
				doKey(key, 0);
			} else {
				doKey(key, data[3]);
			}
		} else {
			if (data[3] == 0) {
				doKey(0, 0);
			}
		}

//		if (key != 0) {
//			if (data[2] < 0) {
//				doKey(key, 1);
//				Util.doSleep(10);
//				doKey(key, 0);
//			} else {
//				doKey(key, data[3]);
//			}
//		} else {
//			if (data[3] == 0) {
//				doKey(0, 0);
//			}
//		}
	}

	public void setContext(Context c) {
		super.setContext(c);
		updateTime();
		udpateLang();
	}



	private final static int HIDE_RADAR = 0;
	private final static int KEY_VOL = 1;
	private final static int SHOW_VOLUME_STEP = 2;

	private final static int CONNECT_REPEAT = 0x80;
	private void doKeyStep(int key, int step) {
		mHandler.removeMessages(SHOW_VOLUME_STEP);
		doKey(key, 1);
		doKey(key, 0);
		--step;

		if (step > 0) {
			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(SHOW_VOLUME_STEP, key, step), 30);
		}
	}

	private int mVolStep = 0;
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CONNECT_REPEAT:
				startConnect();
				break;
//			case HIDE_RADAR:
//				RadarManager.stop();
//				break;
			case KEY_VOL:
				if (mVolStep < 30) {
					mVolStep++;
					doKey(mKeyDown);
					mHandler.sendEmptyMessageDelayed(KEY_VOL, 200);
				}
				break;
			case SHOW_VOLUME_STEP:
				doKeyStep(msg.arg1, msg.arg2);
				break;
			}
			super.handleMessage(msg);
		}
	};


	private int mDoorStatus = 0;

	private boolean mAirStep = false;
	private void parseACInfo(byte[] data, int len)
	{
//		if (data[4] >= 0x24 && data[4] <= 0x40){
//			data[4] = (byte)((18f + (0.5f * data[4]))*2);
//		} 
		
//		if (data[5] >= 0x24 && data[5] <= 0x40){
//			data[5] = (byte)((18f + (0.5f * data[5]))*2);
//		} 
		if ((data[4]&0xff) >= 1 && (data[4]&0xff) <= 7){
			data[4] =(byte)(0xf0+(data[4]&0xff));
			mAirStep = true;
		} else {
			mAirStep = false;
		}

		if ((data[5]&0xff) >= 1 && (data[5]&0xff) <= 7){
			data[5] =(byte)(0xf0+(data[5]&0xff));
			mAirStep = true;
		} else {
			mAirStep = false;
		}

		if (mAirStep){
			if (data[4] == 0){
				data[4] = (byte)0xfa;
			}
			if (data[5] == 0){
				data[5] = (byte)0xfa;
			}
		}
		
		
		
		byte[]	airData = new byte[8];
		airData[0] = (byte) (data[2] & 0xff);
		airData[1] = (byte) (data[3] & 0xff);
		airData[2] = (byte) (data[4] & 0xff);
		airData[3] = (byte) (data[5] & 0xff);
	//	airData[4] = (byte) (data[6] & 0x7f);
		airData[5] = (byte) (data[6] & 0x01);
		boolean airControl = (data[6] & 0x80) != 0;
		/*(data[2] & 0x80) != 0 && */

		Handler handler = getHandler("CanService");
		if (airControl && null != handler) {		
			handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
		}
		
		int temp = data[7]&0xff;
		if ((data[6] & 0x1) != 0) {
			temp |= 0x100;
		}
		if ((data[6] & 0x2) != 0) {
			temp |= 0x200;
		}
		updateOutDoorTemp(temp);
	}

	private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
	public void updateOutDoorTemp(int temp) {

		
		if (temp == CarUtil.INVALID_OUT_DOOR_TEMP) {
			if (mTempOutDoor != CarUtil.INVALID_OUT_DOOR_TEMP) {
				temp = mTempOutDoor;
			} else {
				return;
			}
		}
		mTempOutDoor = temp;
		String s;
		
		int unit = (temp & 0x100);
		int point = (temp & 0x200); 
		int ne = temp & 0x80;
		temp = temp & 0x7f;
		if (ne != 0){
			temp = -temp;
		}
		if (CarUtil.mTempUnit == 2 || unit != 0) {			
			temp = (int) ((temp) * 1.8f + 32);
			s = temp
					+ mContext.getResources().getString(
							R.string.temp_unic_fahrenheit);
		} else {
			s = temp + ((point!=0)?".5":"")
					+ mContext.getResources().getString(
							R.string.temp_unic_centigrade);
		}

		GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui",
				MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);

		
	}
	
	
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x1: {
			parseWheelKey(data);
		}

			break;
		case 0x2: {
			parseACInfo(data, len);
		}
			break;
		case 0x3: {
			int door = (data[2] & 0xfc);
			door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6)
					| ((door & 0x10) >> 2) | ((door & 0x20) >> 2)
					| ((door & 0x08) << 1) | ((door & 0x4) << 3));

			if (mDoorStatus != door) {
				mDoorStatus = door;
				Handler handler = getHandler("CanService");
				if (null != handler) {
					handler.sendMessage(handler.obtainMessage(
							CANBOX_DOOR_STATUS, mDoorStatus, 0));

				}
			}
			
			doRightCameraSwitch (data[3] & 0x20);
				

		}
			break;

		case 0x6: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				short angle = (short) ((data[2] & 0xff) | ((data[3]) << 8));
				angle = (short) (angle * 300 / 0x1500);
				if (angle == 0) {
					angle = 5;
				}
				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle, 10));
			}
		}
			break;
		// case 0x8:
		// {
		//
		// mRadar[0] = getRadarData(data[6]);
		// mRadar[1] = getRadarData(data[7]);
		// mRadar[2] = getRadarData(data[7]);
		// mRadar[3] = getRadarData(data[8]);
		//
		//
		// mRadar[4] = getRadarData(data[3]);
		// mRadar[5] = getRadarData(data[4]);
		// mRadar[6] = getRadarData(data[4]);
		// mRadar[7] = getRadarData(data[5]);
		//
		// RadarManager.start(mContext);
		// Handler handler = getHandler(RadarManager.TAG);
		// if (null != handler) {
		// checkHideRadar();
		// handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
		// }
		// }
		// break;
		case 0x7F: {
			mHandler.removeMessages(CONNECT_REPEAT);
			mRepeatConnectCount = 0;
			byte[] version = new byte[0x10];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		case 0x4:
		case 0x5: {
			sendCanboxInfo("com.canboxsetting", data);
		}
			break;

		}
	}
	

	private void doRightCameraSwitch(int s) {
		
		String top = AppConfig.getTopActivity();

		Intent it = new Intent(Intent.ACTION_VIEW);
		boolean topIsCamera = top != null && top.contains("com.android.car.frontcamera.SideCameraActivity");
		if (s == 0) {
			if (topIsCamera){
				it.putExtra("finish", 1);
			}
		} else {
			if (!topIsCamera){	
				topIsCamera = true;
				it.putExtra("style", 1);
			}
		}
		
		if (topIsCamera) {
			try {
				it.setClassName("com.car.ui",
						"com.android.car.frontcamera.SideCameraActivity");
				it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
						| Intent.FLAG_ACTIVITY_NEW_TASK);

				mContext.startActivity(it);
			} catch (Exception e) {
//				Log.e(TAG, e.getMessage());
			}
		}
	}
	
	private int mRepeatConnectCount = 50000; 


	protected void doKey(int value, int status) { // value 0 -> key up

		// Log.d("Mazda3", "doKey:" + value);
		// if (CarUtil.getChangeKey() == 1) {
		value = changeKey(value);
		// }

		switch (status) {
		case 0:
			mHandler.removeMessages(KEY_VOL);
			if (mKeyDown != 0) {
				if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
					if (mKeyDown == AK_KEYPAD_VOLUME_A
							|| mKeyDown == AK_KEYPAD_VOLUME_D) {
						mKeyDown = 0;
					} else {
						int ret = getLongKey(value);
						if (ret != 0) {
							mKeyDown = ret;
						}
					}
				}
				if (mKeyDown != 0) {
					doKey(mKeyDown);
				}
				mKeyDown = 0;
				longClick = false;
			}
			break;
		case 1:
			mKeyDown = value;
			mClickTime = System.currentTimeMillis();
			longClick = false;

			mHandler.removeMessages(KEY_VOL);
			if (value == AK_KEYPAD_VOLUME_A || value == AK_KEYPAD_VOLUME_D) {
				mVolStep = 0;
				mHandler.sendEmptyMessageDelayed(KEY_VOL, LONG_CLICK_TIME);
			}
			break;
		// case 2:
		// if (value == AK_KEYPAD_VOLUME_A || value == AK_KEYPAD_VOLUME_D) {
		// doKey(value);
		// mKeyDown = 0;
		// } else {
		// if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
		// if (mKeyDown != 0) {
		// longClick = true;
		// int ret = getLongKey(value);
		// if (ret != 0) {
		// doKey(ret);
		// mKeyDown = 0;
		// }
		// }
		// }
		// }
		// break;
		}

	}	

	
	public void updateTime() {
		if (mContext == null) {
			return;
		}
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);
		byte ampm = 1;
		if ("12".equals(strTimeFormat)) {

			if (h > 12) {
				h -= 12;
			} else if (h == 0) {
				h = 12;
			}

//			h |= 0x80;
			ampm = 0;
		}

		byte m = (byte) curDate.getMinutes();
//		byte s = (byte) curDate.getSeconds();

		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();

		byte[] buf = new byte[] { (byte) 0x84, 0x06, y, mon, d, ampm, h, m };
		sendDataToCanbox(buf, buf.length);
	}
	
	
	public void udpateLang() {
		int lang = -1;
		String locale = Locale.getDefault().getLanguage();
		if (locale != null) {
			 if (locale.equals("zh")) {
				lang = 0;
			} else {
				lang = 1;
			}

		}
		if (lang != -1) {
			byte[] buf = { (byte) 0x82, 0x2, 0x4, (byte) lang };
			sendDataToCanbox(buf, buf.length);
		}
	}
	public int getUpdateTime() {
		return 60000;
	}
}
