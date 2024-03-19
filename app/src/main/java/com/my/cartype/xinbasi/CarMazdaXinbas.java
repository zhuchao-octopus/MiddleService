package com.my.cartype.xinbasi;

import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.common.util.UtilSystem;
import com.my.GlobalDef;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.manager.McuManager;
import com.my.out.R;

public class CarMazdaXinbas extends Canbox {

	public CarMazdaXinbas() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });

	}

	private final static byte KEYS_WHEEL[][] = { { 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, { 0x3, KEY_NEXTSONG },
			{ 0x4, KEY_PREVIOUSSONG }, { 0x7, KEY_SOURCE },{ 0x8, KEY_MUTE },
			{ 0x9, MyCmd.Keycode.KEY_MIC },
			{ 0xa, KEY_BT_DIAL }, { 0xb, KEY_BT_HANG },

			{ 0x20, KEY_MEDIA },
			{ 0x21, KEY_HOME },
			{ 0x22, KEY_GPS },
			{ 0x23, KEY_FM },
			{ 0x24, KEY_UP },
			{ 0x25, KEY_DOWN },
			{ 0x26, KEY_LEFT },
			{ 0x27, KEY_RIGHT },
			{ 0x28, KEY_ENTER },
			
			{ 0x29, KEY_POWER },
			{ 0x2a, KEY_BACK },
			
			{ (byte)0xf0, AK_KEYPAD_VOLUME_A },
			{ (byte)0xf1, AK_KEYPAD_VOLUME_D },
			{ (byte)0xf2, MyCmd.Keycode.CH_UP },
			{ (byte)0xf3, MyCmd.Keycode.CH_DOWN },
			

	};	
	
	public void udpateLang() {
		if (CarUtil.getCarType2() == 1) {
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
				byte[] buf = { (byte) 0x84, 0x2, 0x1f, (byte) lang };
				sendDataToCanbox(buf, buf.length);
			}
		}
	}

	private boolean isOneKey(byte b) {
		if (((b & 0xff) == 0xf0)
				|| ((b & 0xff) == 0xf1) || ((b & 0xff) == 0xf3)
				|| ((b & 0xff) == 0xf2)) {
			return true;
		}

		return false;
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
				if (step > 1 && step <= 8) {
					doKeyStep(key, step);
				} else {
					McuManager mcu = McuManager.getInstanse();
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
						mcu.setVoulume(v);
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
		
		if (key != 0) {
			if (data[2] < 0) {
				doKey(key, 1);
				Util.doSleep(10);
				doKey(key, 0);
			} else {
				doKey(key, data[3]);
			}
		} else {
			if (data[3] == 0){
				doKey(0, 0);
			}
		}
	}

	public void setContext(Context c) {
		super.setContext(c);
		updateTime();
		udpateLang();
	}

	private void sendAVMKey() {
		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x2, 0x1 };
		sendDataToCanbox(data, data.length);
	}

	private final static byte KEYS_WHEEL2[][] = { { 0x1, KEY_FM },
			{ 0x2, KEY_FM }, { 0x3, KEY_FM }, { 0x9, KEY_FM },

			{ 0x4, KEY_DVD }, { 0x5, KEY_MEDIA }, { 0x6, KEY_MEDIA },
			{ 0xa, KEY_MEDIA },

			{ 0x7, MyCmd.Keycode.BT_MUSIC }, { 0x8, MyCmd.Keycode.AUX_IN },

			{ 0xe, MyCmd.Keycode.KEY_TV }, { 0x10, MyCmd.Keycode.ALL_APP },

			{ 0x11, KEY_BT_DIAL }, { 0x12, KEY_BT_HANG }, };


	private void checkHideRadar() {
		mHandler.removeMessages(HIDE_RADAR);
		mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
	}

	private final static int HIDE_RADAR = 0;
	private final static int KEY_VOL = 1;
	private final static int SHOW_VOLUME_STEP = 2;

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
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_RADAR:
				RadarManager.stop();
				break;
			case KEY_VOL:
				if (mVolStep < 30){
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
	private byte getRadarData(byte i) {
		byte data = 0;
		switch (i) {
		case 0:
			data = 0;
			break;
		case 1:
			data = 1;
			break;
		case 2:
			data = 4;
			break;
		case 3:
			data = 7;
			break;
		case 4:
			data = 11;
			break;
		}
		return data;
	}
	private int mDoorStatus = 0;
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x1: {
			parseWheelKey(data);
		}

			break;
		case 0x2: {
				int door = (data[2] & 0xfc);
				door = (((door & 0x40) >> 5) | ((door & 0x80) >> 7)
						| ((door & 0x10) >> 1) | ((door & 0x20) >> 3)
						| ((door & 0x08) << 1) | ((door & 0x4) << 3));

				if (mDoorStatus != door) {
					mDoorStatus = door;
					Handler handler = getHandler("CanService");
					if (null != handler) {
						handler.sendMessage(handler.obtainMessage(
								CANBOX_DOOR_STATUS, mDoorStatus, 0));

					}
				}
			

		}
			break;
			
		case 0x7: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				int angle = ((data[2] & 0xff) | ((data[3]) << 8));
				angle = -(angle * 300 / 0x1200);
				if(angle == 0){
					angle = 5;
				}
				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle, 10));
			}
		}
			break;
			case 0x8: 
			{

				mRadar[0] = getRadarData(data[6]);
				mRadar[1] = getRadarData(data[7]);
				mRadar[2] = getRadarData(data[7]);
				mRadar[3] = getRadarData(data[8]);
				

				mRadar[4] = getRadarData(data[3]);
				mRadar[5] = getRadarData(data[4]);
				mRadar[6] = getRadarData(data[4]);
				mRadar[7] = getRadarData(data[5]);
				
				boolean zero = Util.isZero(mRadar);
				if (!zero){
					RadarManager.start(mContext);
					checkHideRadar();
				}
				Handler handler = getHandler(RadarManager.TAG);
				if (null != handler) {				
					handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
				}
			}
				break;
		case 0x7F: {
			byte[] version = new byte[0x10];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		case 0x4:
		case 0x5:
		case 0x6:
		case 0x9:
		case 0xb:
		case 0xc:
		case 0xd:{
			sendCanboxInfo("com.canboxsetting", data);
			}
			break;
		
		
		}
	}

//	byte[] data = new byte[6];
//
//	public void setMediaMoreInfo(int source, int play, int total, int time,
//			int total_time) {
//		// byte min = (byte) ((time / 60) % 60);
//		// byte sec = (byte) ((time) % 60);
//		// ++play;
//		int s = 4;
//		if (MyCmd.SOURCE_DVD == source) {
//			s = 6;
//		} else {
//			++play;
//		}
//
//		data[0] = (byte) 0x82;
//		data[1] = 0x4;
//		data[2] = 4;
//
//		data[3] = (byte) ((play >> 8) & 0xff);
//		data[4] = (byte) ((play) & 0xff);
//		data[5] = 0;
//
//		sendDataToCanbox(data, data.length);
//	}
//
//	public void setMediaSrc(int source, byte type, byte[] b) {
//		// setMediaSrc(0);
//		// int freq = (((b[2]&0xff)<< 8) );
//		// int dd = (b[1] & 0xff);
//		// freq = freq | dd;
//
//		int freq = (((b[2] & 0xff) << 8)) | (b[1] & 0xff);
//
//		if (b[0] == 0x10) {
//			b[0] = 2;
//		} else {
//			// b[0] = 0x10;
//			freq = freq / 10;
//			b[0] = 1;
//		}
//
//		// Util.clearBuf(data);
//		data[0] = (byte) 0x82;
//		data[1] = 0x4;
//		data[2] = b[0];
//
//		data[3] = (byte) ((freq >> 8) & 0xff);
//		data[4] = (byte) ((freq) & 0xff);
//		data[5] = 0;
//		sendDataToCanbox(data, data.length);
//	}
//
//	public void setMediaSrc(int source) {
//		byte s = 0;
//
//		Util.clearBuf(data);
//
//		switch (source) {
//		case MyCmd.SOURCE_RADIO:
//			s = 1;
//			break;
//		case MyCmd.SOURCE_DVD:
//			s = 6;
//			break;
//		case MyCmd.SOURCE_IPOD:
//			s = 5;
//			break;
//		case MyCmd.SOURCE_MUSIC:
//		case MyCmd.SOURCE_VIDEO:
//			s = 0x04;
//			break;
//		case MyCmd.SOURCE_AUX:
//			s = 0x09;
//			break;
//		case MyCmd.SOURCE_DTV:
//			s = 0x0b;
//			break;
//		case MyCmd.SOURCE_BT:
//			s = 0x0a;
//			break;
//		default:
//			s = 0x0f;
//			break;
//		}
//
//		data[0] = (byte) 0x82;
//		data[1] = 0x4;
//
//		data[2] = s;
//		sendDataToCanbox(data, data.length);
//
//		// mSource = source;
//	}


	protected void doKey(int value, int status) { // value 0 -> key up

//		Log.d("Mazda3", "doKey:" + value);
//		if (CarUtil.getChangeKey() == 1) {
			value = changeKey(value);
//		}

		switch (status) {
		case 0:
			mHandler.removeMessages(KEY_VOL);
			if (mKeyDown != 0) {
				if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
					if (mKeyDown == AK_KEYPAD_VOLUME_A || mKeyDown == AK_KEYPAD_VOLUME_D) {
						mKeyDown = 0;
					} else {
						int ret = getLongKey(value);
						if (ret != 0) {
							mKeyDown = ret;
						}
					}
				}
				if (mKeyDown != 0){
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
//		case 2:
//			if (value == AK_KEYPAD_VOLUME_A || value == AK_KEYPAD_VOLUME_D) {
//				doKey(value);
//				mKeyDown = 0;
//			} else {
//				if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
//					if (mKeyDown != 0) {
//						longClick = true;
//						int ret = getLongKey(value);
//						if (ret != 0) {
//							doKey(ret);
//							mKeyDown = 0;
//						}
//					}
//				}
//			}
//			break;
		}

	}
}
