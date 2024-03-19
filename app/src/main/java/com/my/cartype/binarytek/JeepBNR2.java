package com.my.cartype.binarytek;

import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.GlobalDef;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.manager.McuManager;
import com.my.out.R;

public class JeepBNR2 extends Canbox {

	public JeepBNR2() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });
		updateCanboxKeySettings();

		buildCmdEQ((byte) 0x17, (byte) 0x1, 6);
	}

	byte[] mCmdEQ = new byte[] { (byte) 0x84, 0x02, 0x1, 0x1 };

	private void sendEQ(boolean on) {
		mHandler.removeMessages(SEND_REPEAT_EQ);
		if (on) {			
				mCmdEQ[3] = 0x1;
				mHandler.sendEmptyMessageDelayed(SEND_REPEAT_EQ, 1000);
			
		} else {
			mCmdEQ[3] = 0x0;
		}
		sendDataToCanbox(mCmdEQ, mCmdEQ.length);
	}
	
	private byte mKeyPannel[][];
	private final static byte KEYS_WHEEL[][] = { { 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D },

			{ 0x3, KEY_NEXTSONG }, { 0x4, KEY_PREVIOUSSONG },
			{ 0x5, KEY_MUTE },//for od Ferrari
			{ 0x6, KEY_MUTE }, { 0x13, KEY_MUTE },

			{ 0x7, KEY_SOURCE }, { 0x8, KEY_MIC }, { 0x9, KEY_BT_DIAL },
			{ 0xa, KEY_BT_HANG },

			{ 0x12, KEY_PLAYPAUSE }, { 0x14, MyCmd.Keycode.DARK },

			{ 0x15, KEY_BACK }, { 0x16, KEY_MUTE },

			{ 0x1B, KEY_POWER },
			
			{ 0x17, AK_KEYPAD_VOLUME_A }, 
			{ 0x18, AK_KEYPAD_VOLUME_D },
			
			{ 0x19, MyCmd.Keycode.ROLL_NEXT }, 
			{ 0x1a, MyCmd.Keycode.ROLL_PREV },

	};

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

			if (((data[2] & 0xff) == 0x17) || ((data[2] & 0xff) == 0x18)) {
				int step = data[3] & 0xff;
				// step = 0x10;
				if (step <= 0){
					return;
				}
				if (step <= 8 ) {
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
				doKey(key, 1);
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
	}

	private boolean isOneKey(byte b) {
		if (((b & 0xff) == 0x19) || ((b & 0xff) == 0x1a)) {
			return true;
		}

		return false;
	}
	
	private final static int SHOW_VOLUME_STEP = 1;
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
	

	private boolean isAirtContolCar() {
		if (CarUtil.getCarType() == 0 || CarUtil.getCarType() == 1
				|| CarUtil.getCarType() == 4 || CarUtil.getCarType() == 6
				|| CarUtil.getCarType() == 7) {
			return true;
		}
		return false;
	}



	byte[] mAirData = new byte[8];

	private void parseACInfo(byte[] data, int len) {
		if (!isShowAir()) {
			sendCanboxInfo("com.canboxsetting", data);
		//	return;
		}
		// if (data[4] >= 0x1f){
		// data[4] = (byte)0xff;
		// } else if(data[4] > 0){
		// data[4] = (byte)((17.5f + (0.5f * data[4]))*2);
		// }
		// if (data[5] >= 0x1f){
		// data[5] = (byte)0xff;
		// } else if(data[5] > 0){
		// data[5] = (byte)((17.5f + (0.5f * data[5]))*2);
		// }

		byte[] airData = new byte[8];

		airData[0] = (byte) ((data[2] & 0xec) | ((data[6] & 0x80) >> 6) | ((data[6] & 0x40) >> 6));

		airData[7] = (byte)( ((data[6] & 0x10) >> 4)
				|((data[6] & 0x02) << 5));

		if ((data[6] & 0x02) == 0){

			airData[2] = (byte) (data[4] & 0xff);
			airData[3] = (byte) (data[5] & 0xff);
		} else {
			airData[2] = (byte) (data[4] & 0x3f);
			airData[3] = (byte) (data[5] & 0x3f);
		}
		airData[1] = (byte) (data[3] & 0xff);

		airData[4] = (byte) (((data[7] & 0x33) << 0) |

		((data[6] & 0x8) >> 1) |

		((data[6] & 0x20) << 2));

		airData[5] = (byte) ((data[6] & 0x1));

		boolean airControl = false;
		if (airData[0] != (byte) (mAirData[0] & 0xff)
				|| airData[1] != (byte) (mAirData[1] & 0xff)
				|| airData[2] != (byte) (mAirData[2] & 0xff)
				|| airData[3] != (byte) (mAirData[3] & 0xff)
				|| airData[4] != (byte) (mAirData[4] & 0xff)
				|| airData[5] != (byte) (mAirData[5] & 0xff)
				|| airData[6] != (byte) (mAirData[6] & 0xff)
				|| airData[7] != (byte) (mAirData[7] & 0xff)) {
			airControl = true;

			mAirData = airData;

		}

		if (airControl) {
			Handler handler = getHandler("CanService");
			if (null != handler) {

				int msg = CANBOX_HIDE_AIR;
				if (/* (data[2] & 0x80) != 0 && */((data[3] & 0x10) != 0)) {
					msg = CANBOX_RETURN_AIR;
				}

				handler.sendMessage(handler.obtainMessage(msg, airData));
			}
		}
	}

	private byte getRadarData(byte i) {
		byte data = 0;
		switch (i) {
		case 0:
			data = 0;
			break;
		case 1:
			data = 3;
			break;
		case 2:
			data = 8;
			break;
		}
		return data;
	}

	private byte getRadarData2(byte i) {
		byte data = 0;
		switch (i) {
		case 0:
			data = 0;
			break;
		case 1:
			data = 1;
			break;
		case 2:
			data = 3;
			break;
		case 3:
			data = 5;
			break;
		case 4:
			data = 7;
			break;
		case 5:
			data = 9;
			break;
		case 6:
			data = 11;
			break;
		}
		return data;
	}

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x17:
			super.parseCanboxData(data, len);
			return;
		case 0x20: {
			parseWheelKey(data);
			break;
		}
		case 0x21: {
			parseACInfo(data, len);
		}
			break;
		case 0x27: {
			int temp = (data[2] & 0xff);
			temp = -395 + (temp * 5);
			// temp -= 40;
			String s = "";
			if (temp >= -390 && temp <= 880) {
				if (data[3] != 1) {

					s = String.format(
							"%d.%d%s",
							temp / 10,
							(temp % 10) >= 0 ? (temp % 10) : -(temp % 10),
							mContext.getResources().getString(
									R.string.temp_unic_centigrade));

				} else {
					temp = (temp * 18 + 3200);
					temp /= 10;

					s = String.format(
							"%d.%d%s",
							temp / 10,
							(temp % 10) >= 0 ? (temp % 10) : -(temp % 10),
							mContext.getResources().getString(
									R.string.temp_unic_fahrenheit));

				}

			}
			GlobalDef.sendByCarServiceToSystemUI(mContext,
					"com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);

		}
			break;

		case 0x22: // Radar back
		{

			mRadar[0] = getRadarData(data[2]);
			mRadar[1] = getRadarData2(data[3]);
			mRadar[2] = getRadarData2(data[4]);
			mRadar[3] = getRadarData(data[5]);

			if (mRadar[0] != 0 || mRadar[1] != 0 || mRadar[2] != 0
					|| mRadar[3] != 0) {
				boolean zero = Util.isZero(mRadar);
				if (!zero){
					RadarManager.start(mContext);
					checkHideRadarEx(2000);
				}
				Handler handler = getHandler(RadarManager.TAG);
				if (null != handler) {
					handler.sendMessage(handler
							.obtainMessage(CANBOX_RADAR_BACK));
				}
			}
		}
			break;
		case 0x23: // Radar back
		{

			mRadar[4] = getRadarData(data[2]);
			mRadar[5] = getRadarData2(data[3]);
			mRadar[6] = getRadarData2(data[4]);
			mRadar[7] = getRadarData(data[5]);
			if (mRadar[4] != 0 || mRadar[5] != 0 || mRadar[6] != 0
					|| mRadar[7] != 0) {
				boolean zero = Util.isZero(mRadar);
				if (!zero) {
					RadarManager.start(mContext);
					checkHideRadarEx(2000);
				}
				Handler handler = getHandler(RadarManager.TAG);
				if (null != handler) {
					handler.sendMessage(handler
							.obtainMessage(CANBOX_RADAR_FRONT));
				}
			}
		}
			break;

		case 0x29: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu
				// ma

				int angle = -(((a * 3000) / 4600));

				if (angle > -50 && angle < 0) {
					angle = -50;
				} else if (angle > 0 && angle < 50) {
					angle = 50;
				}
				// Log.e("1", ""+(data[2] & 0xff));
				// Log.e("2", ""+(data[3] & 0xff));
				// Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle, 100));
			}
		}
			break;
		case 0x24: {
			int door = (data[2] & 0xfc);

			door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6)
					| ((door & 0x10) >> 2) | ((door & 0x20) >> 2)
					| ((door & 0x08) << 1) | ((door & 0x04) << 3));

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
//		case 0x17:
//			if (CarUtil.getCarEQ() == 0) {
//				if ((data[9] & 0x80) != 0) {
//					CarUtil.mIsNeedSendEQ = true;
//					sendEQ(true);
//					// McuManager mcu = McuManager.getInstanse();
//					// if (mcu != null) {
//					// mcu.setAudio(0x6, 0x2);
//					// }
//				} else {
//					CarUtil.mIsNeedSendEQ = false;
//				}
//			}
//			break;
		case 0x7:
		case 0x40:
		case 0x42:
		case 0x43:
		case 0x44:
		case 0x28:
			sendCanboxInfo("com.canboxsetting", data);
			break;
		case 0x30: {
			byte[] version = new byte[16];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		}
		
		returnDriveData(data);
	}

	private int mDoorStatus = 0;

	public void setReverseRadaVol(byte param) {
		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x0, param };
		sendDataToCanbox(data, data.length);
	}

	public void setParkCarMode(byte param) {
		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x2, param };
		sendDataToCanbox(data, data.length);
	}

	public void requestInfo(byte param) {
		byte[] data = new byte[] { (byte) 0x90, 0x2, param, 0 };
		sendDataToCanbox(data, data.length);
	}

	// public void setMediaMoreInfo(int source, int play, int total, int time,
	// int total_time) {
	// // byte min = (byte) ((time / 60) % 60);
	// // byte sec = (byte) ((time) % 60);
	// // ++play;
	// // byte[] data = new byte[] { (byte) 0xa3, 0x1, (byte) (total & 0xFF),
	// // (byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF),
	// // (byte) ((play >> 8) & 0xFF), min, sec };
	// // sendDataToCanbox(data, data.length);
	// }
	//
	// public void setMediaSrc(int source, byte type, byte[] b) {
	// if (b[0] == 0x3) {
	// b[0] = 5;
	// } else {
	// b[0] = 1;
	// }
	// byte[] data = new byte[] { (byte) 0x9a, 0x5, 8, b[0], b[1], b[2], 0 };
	// sendDataToCanbox(data, data.length);
	// }
	//
	// public void setMediaSrc(int source) {// default is simple box
	// switch (source) {
	// case MyCmd.SOURCE_DVD:
	// mSource = 0x2;
	// break;
	// case MyCmd.SOURCE_RADIO:
	// mSource = 0x1;
	// break;
	// case MyCmd.SOURCE_AUX:
	// mSource = 0x4;
	// break;
	// case MyCmd.SOURCE_BT:
	// mSource = 0x7;
	// break;
	// default:
	// mSource = 0x6;
	// break;
	// }
	//
	// byte[] data = new byte[] { (byte) 0x99, 0x2, mSource, mVolume };
	// sendDataToCanbox(data, data.length);
	// }

	private byte mEq[] = null;

	// private byte eqSwitch(byte e){
	// switch(e){
	//
	// }
	// }
//	public void sendEqToCanbox(byte[] eq) {
//		// Log.d("abcd", "sendEqToCanbox");
//		if (eq != null && eq.length >= 11) {
//			byte[] buf = new byte[4];
//			int i;
//			buf[0] = (byte) 0x84;
//			buf[1] = 0x2;
//
//			// buf[2] = 0x2;
//			// buf[3] = eq[0];
//			// sendDataToCanbox(buf, buf.length);
//			// Util.doSleep(1);
//
//			buf[2] = 0x3;
//			buf[3] = (byte) (eq[0] + 3);
//			sendDataToCanbox(buf, buf.length);
//			Util.doSleep(1);
//
//			buf[2] = 0x4;
//			buf[3] = (byte) (eq[1] + 3);
//			sendDataToCanbox(buf, buf.length);
//			Util.doSleep(1);
//
//			buf[2] = 0x5;
//			buf[3] = (byte) (((eq[2] + eq[3] + eq[4]) / 3));
//
//			sendDataToCanbox(buf, buf.length);
//			Util.doSleep(1);
//
//			buf[2] = 0x6;
//			buf[3] = (byte) (((eq[8] + eq[9] + eq[10]) / 3));
//
//			sendDataToCanbox(buf, buf.length);
//			Util.doSleep(1);
//
//			buf[2] = 0x7;
//			buf[3] = (byte) (((eq[5] + eq[6] + eq[7]) / 3));
//
//			sendDataToCanbox(buf, buf.length);
//
//		}
//	}

	// private byte mVolume;
	// private byte mSource = 0x6;

	private byte[] mData = new byte[] { (byte) 0xc0, 0x8, 0, 0, 0, 0, 0, 0, 0,
			0 };

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {



	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		if (b[0] != 0x10) {
			b[0] += 1;
		}
		mData = new byte[] { (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0,
				0, 0 };
		sendDataToCanbox(mData, mData.length);
	}

	public void setMediaSrc(int source) {// default is simple box

		switch (source) {
		case 0:
			return;
		}

		mData = new byte[] { (byte) 0xc0, 0x8, 0x7, 0, 0, 0, 0, 0, 0, 0 };
		// }

		sendDataToCanbox(mData, mData.length);
	}

	public void setVolume(int volume) {

		byte[] data = new byte[] { (byte) 0x84, 0x2, 0x2, (byte) volume };
		sendDataToCanbox(data, data.length);
	}

	public void updateCanboxKeySettings() {
		// if (CarUtil.getKeyType()==2) {
		// mKeyPannel = KEYS_PANNEL_GL8;
		// } else if (CarUtil.getKeyType()==1) {
		// mKeyPannel = KEYS_PANNEL_ENVISION_L;
		// } else {
		// mKeyPannel = KEYS_PANNEL_NORMAL;
		// }

		if (CarUtil.getCarEQ() == 1) {
			CarUtil.mIsNeedSendEQ = true;
		}
	}

	private int mPreBtStatus = 0;

	public void setPhoneEx(int status_in, String num, String name) {

//		byte[] c = new byte[] {  0, 0, 0, 0, 0, 0 };
//		int status = 0;
//		if (name != null){
//			num = name + " " + num;
//		}
//		if (status_in >= HFP_INFO_CONNECTED) {
//			switch (status_in) {
//			case HFP_INFO_CALLED:
//				status = 0x42;
//				break;
//			case HFP_INFO_INCOMING:
//				status = 0x41;
//				break;
//			case HFP_INFO_CALLING:
//				if (mPreBtStatus == 0) {
//					mPreBtStatus = 0x41;
//				}
//				status = mPreBtStatus;
//				break;
//			default:
//				if (mPreBtStatus == 0x41 || mPreBtStatus == 0x42){
//					status = 0x43;
//				} else {
//					if (mData[2] == 0){
//						num = "Connect";
//						status = 5;
//					}
////					status = 0;
//				}
//				break;
//			}
//		} else {
//			status = 0;
//		}
//
//		mPreBtStatus = status;
//
//		if (status == 0x43 || status == 0) {
//			mHandler.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					sendDataToCanbox(mData, mData.length);
//				}
//			}, 50);
//		} else {
//
//			mHandler.removeMessages(SEND_MEDIA_TEXT);
//			if (num != null) {
//				try {
//					
//					byte[] data;
//					if (CarUtil.getKeyType() == 0) {
//
//						byte[] n = num.getBytes();
//
//						int num_len = n.length;						
//
//						data = new byte[num_len + 3];
//						data[0] = 0x70;
//						data[1] = (byte) (num_len + 1);
//						data[2] = 0x1;
//						for (int i = 0; i < num_len; ++i) {
//							data[i + 3] = n[i];		
//						}						
//					} else {
//						byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe
//
//						int num_len = n.length;
//						if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
//							num_len -= 2;
//						}
//
//						data = new byte[num_len + 3];
//						data[0] = 0x70;
//						data[1] = (byte) (num_len + 1);
//						data[2] = 0x10;
//						for (int i = 0; i < num_len; ++i) {
//							if (i % 2 == 0) {
//								data[i + 3] = n[i + (n.length - num_len) + 1];
//							} else {
//								data[i + 3] = n[i + (n.length - num_len) - 1];
//							}
//						}
//						
//					}
//					
//					mHandler.sendMessageDelayed(
//							mHandler.obtainMessage(SEND_MEDIA_TEXT, data),
//							30);
//				} catch (Exception e) {
//
//				}
//			}
//		}
//
//		if (status != 0) {
//			byte[] data = new byte[] { (byte) 0xc0, 0x8, (byte) status,
//					(byte) 0xff, c[0], c[1],c[2],c[3],c[4],c[5], };
//			sendDataToCanbox(data, data.length);
//			
//			
//		}

	}

	private void checkHideRadar() {
		mHandler.removeMessages(HIDE_RADAR);
		mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
	}

	private final static int HIDE_RADAR = 0;
	private final static int SEND_MEDIA_TEXT = 10;
	private final static int SEND_REPEAT_EQ = 11;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_RADAR:
				RadarManager.stop();
				break;
			case SHOW_VOLUME_STEP:
				doKeyStep(msg.arg1, msg.arg2);
				break;
			case SEND_MEDIA_TEXT:
				if (msg.obj != null) {
					byte[] buf = (byte[]) msg.obj;
					sendDataToCanbox(buf, buf.length);
				}
				break;
			case SEND_REPEAT_EQ:
				sendEQ(true);
				break;
			}
			super.handleMessage(msg);
		}
	};

	public void setContext(Context c) {
		super.setContext(c);
		udpateLang();
	}

//	public void udpateLang() {
//		int lang = -1;
//		String locale = Locale.getDefault().getLanguage();
//		if (locale != null) {
//			if (locale.equals("en")) {
////				if ("GB".equals(Locale.getDefault().getCountry())) {
////					lang = 0xb;
////				} else {
//					lang = 1;
////				}
//			} else if (locale.equals("zh")) {
//				lang = 0xa;
//			} else if (locale.equals("it")) {
//				lang = 5;
//			} else if (locale.equals("ru")) {
//				lang = 0xd;
//			} else if (locale.equals("tr")) {
//				lang = 9;
//			} else if (locale.equals("fr")) {
//				lang = 2;
//			} else if (locale.equals("de")) {
//				lang = 3;
//			} else if (locale.equals("es")) {
//				lang = 4;
//			} else if (locale.equals("nl")) {
//				lang = 6;
//			} else if (locale.equals("pl")) {
//				lang = 7;
//			} else if (locale.equals("pt")) {
//				if ("BR".equals(Locale.getDefault().getCountry())) {
//					lang = 0xc;
//				} else {
//					lang = 8;
//				}
//			} else if (locale.equals("es")) {
//				lang = 4;
//			}
//
//		}
//		if (lang != -1) {
//			byte[] buf = { (byte) 0xc6, 0x2, 0x0, (byte) lang };
//			sendDataToCanbox(buf, buf.length);
//		}
//	}

	public void updateTime() {
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		if ("12".equals(strTimeFormat)) {

			if (h > 12) {
				h -= 12;
			} else if (h == 0) {
				h = 12;
			}

			h |= 0x80;
		}

		byte m = (byte) curDate.getMinutes();
		byte s = (byte) curDate.getSeconds();

		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();

		byte[] buf = new byte[] { (byte) 0xc7, 0x06, y, mon, d, h, m, s };
		sendDataToCanbox(buf, buf.length);
	}
	
	public void startConnect() {
		super.startConnect();
		byte cmd = -1;
		if (CarUtil.getCarType2() == 1) {
			cmd = 0;
		} else if (CarUtil.getCarType2() == 2) {
			cmd = 1;
		}
		if (cmd != -1) {
			byte[] buf = new byte[] { (byte) 0xca, 0x01, cmd };
			sendDataToCanbox(buf, buf.length);
		}
//		if (CarUtil.getCarEQ() == 1 || CarUtil.isShowEQ()) {
			sendEQ(true);
//		}
	}
	
	@Override
	public void stopConnect() {
		// TODO Auto-generated method stub
		sendEQ(false);
		super.stopConnect();
	}
	
	private void returnDriveData(byte[] buf) {
		if (mRequestDriveData > 0) {
			boolean update = true;
			switch (buf[0]) {
			case 0x28: {
				mDriveData[1] = buf[2];
				mDriveData[2] = buf[3];
				mDriveData[3] = buf[4];
				mDriveData[4] = buf[5];
			}
				break;
			case 0x7: {
				switch (buf[2]) {
				case 0x1:
					mDriveData[8] = buf[4];
					mDriveData[9] = buf[3];
					break;
				case 0x5:
					mDriveData[5] = buf[5];
					mDriveData[6] = buf[4];
					mDriveData[7] = buf[3];
				}
			}
				break;
			case 0x40:
				mDriveData[0] = buf[3];
				break;
			case 0x24:
				if ((buf[3] & 0x2) == 0) {
					mDriveData[10] = 3;
				} else {
					if ((buf[3] & 0x1) != 0) {
						mDriveData[10] = 1;
					} else {
						mDriveData[10] = 4;
					}
				}

				if ((buf[3] & 0x4) != 0) {
					mDriveData[14] = 1;
				} else {
					mDriveData[14] = 0;
				}
				break;
			default:
				update = false;
				break;
			}
			if (update) {
				returnDriveData();
			}
		}
	}
	public int getUpdateTime() {
		return 60000;
	}
	
	public void parseEQ(int id, byte[] buf) {
		byte[] data = new byte[6];
		data[0] = (byte) (buf[7] - 1);
		data[1] = (byte) (buf[8] - 1);
		data[2] = (byte) (buf[6] - 1);
		data[3] = (byte) (buf[4] - 1);
		data[4] = (byte) (buf[5] - 1);
		data[5] = buf[3];
		super.returnEQData(EQ_CMD_SET_ALL_DATA, data);
	}
	
	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (39 << 16) | (19 << 8) | 19;

			byte[] buf = new byte[] { (byte) 0x90, 0x4,  (byte) 0x17, 0, 0, 0 };
			sendDataToCanbox(buf, buf.length);
		} else {
			byte[] buf = new byte[] { (byte) 0x84, 0x2, 0x0, (byte) data };
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				buf[2] = 6;
				buf[3] ++;
				break;
			case EQ_CMD_SET_MIDDLE:
				buf[2] = 7;
				buf[3] ++;
				break;
			case EQ_CMD_SET_LOW:
				buf[2] = 5;
				buf[3] ++;
				break;
			case EQ_CMD_SET_ZONE_FR:
				buf[2] = 3;
				buf[3] ++;
				break;
			case EQ_CMD_SET_ZONE_LR:
				buf[2] = 4;
				buf[3] ++;
				break;
			case EQ_CMD_SET_VOLUME:
				buf[2] = 2;
				break;
			default:
				return 0;
			}

			sendDataToCanbox(buf, buf.length);
		}
		return ret;
	}
}
