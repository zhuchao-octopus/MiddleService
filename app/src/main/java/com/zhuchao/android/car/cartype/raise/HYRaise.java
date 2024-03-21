package com.zhuchao.android.car.cartype.raise;

import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.zhuchao.android.car.R;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.zhuchao.android.car.GlobalDefinition;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.canbox.SosManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.McuManager;
import com.zhuchao.android.car.manager.OSProManager;


public class HYRaise extends Canbox {

	public HYRaise() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x1, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x1, 0x1,
				0x1, 0x0 });
		updateCanboxKeySettings();

		buildCmdRepeatSendCarType(getCarTypeCmd());
	}

	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x6, (byte) 0xee, 0x20, 0 };
		switch(CarUtil.getModelId()){
		case 13:	
		case 35:			
			cmd [3] = 0x1;			
			break;
		case 26:
			if (CarUtil.getCarTypeConfig() == 0) {
				cmd[3] = 0x2;
			}
			else if (CarUtil.getCarTypeConfig() == 1) {
				cmd[3] = 0x3;
			}
			else if (CarUtil.getCarTypeConfig() == 2) {
				cmd[3] = 0x4;
			} else {
				return null;
			}
			break;
		case 0:			
			if (CarUtil.getCarTypeConfig() == 0) {
				cmd[3] = (byte)0x81;
			} else {
				return null;
			}
			break;
		case 39:			
			cmd [3] = (byte)0x82;			
			break;
		case 19:			
			cmd [3] = 0x5;			
			break;
		case 20:			
			cmd [3] = 0x6;			
			break;
		case 21:			
			cmd [3] = 0x7;			
			break;
		case 23:			
			cmd [3] = 0x8;			
			break;
		case 31:			
			cmd [3] = 0x9;			
			break;
		case 27:			
			cmd [3] = 0xa;			
			break;
		case 32:			
			cmd [3] = 0xb;			
			break;
		case 36:			
			cmd [3] = 0xc;			
			break;		
		case 37:
			if (CarUtil.getCarTypeConfig() == 0){
				cmd [3] = 0xd;
			} else {
				return null;
			}
			break;
		default:
			return null;
		}
		return cmd;
	}
	
	public void setContext(Context c) {
		super.setContext(c);
		updateTime();
		udpateLang();
		for (int i = 0; i < buf.length; ++i) {
			buf[i] = 10;
		}
	}

	// private void requestVersion(){
	// byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
	// sendDataToCanbox(data, data.length);
	// }

	public void updateCanboxKeySettings() {

		if (CarUtil.getCarEQ() == 1) {
			CarUtil.mIsNeedSendEQ = true;
			CarUtil.setMcuEQZoneUsed(1);
		}

//		if (CarUtil.getCarType() >= 1 || CarUtil.getCarType() <= 3) {
//			byte[] buf = new byte[] { (byte) 0xca, 0x1,
//					(byte) (CarUtil.getCarType() - 1) };
//			sendDataToCanbox(buf, buf.length);
//		}
	}

	// private byte mKeyPannel[][];

	private final static byte[][] KEYS_PANNEL = {

	{ 0x10, KEY_MUTE }, { 0x11, MyCmd.Keycode.MODLE },
			{ 0x12, MyCmd.Keycode.KEY_SEEK_NEXT },
			{ 0x13, MyCmd.Keycode.KEY_SEEK_PREV },
			{ 0x14, MyCmd.Keycode.VOLUME_UP },
			{ 0x15, MyCmd.Keycode.VOLUME_DOWN },
			{ (byte) 0x84, MyCmd.Keycode.VOLUME_UP },
			{ (byte) 0x85, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x16, MyCmd.Keycode.BT_DIAL },
			{ 0x17, MyCmd.Keycode.BT_HANG },
			{ 0x30, MyCmd.Keycode.SPEECH },
			{ 0x18, MyCmd.Keycode.POWER },
			{ 0x19, MyCmd.Keycode.VOLUME_UP },
			{ 0x1a, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x1b, MyCmd.Keycode.RADIO },
			{ 0x1c, MyCmd.Keycode.AUDIO },
			{ 0x1d, MyCmd.Keycode.BT },
			{ 0x1e, MyCmd.Keycode.DARK },
			{ 0x1f, MyCmd.Keycode.KEY_SEEK_PREV },
			{ 0x20, MyCmd.Keycode.KEY_SEEK_NEXT },
			{ 0x21, MyCmd.Keycode.NAVIGATION },
			{ 0x22, MyCmd.Keycode.NAVIGATION },
			{ 0x23, MyCmd.Keycode.KEY_SEEK_NEXT },
			{ 0x24, MyCmd.Keycode.SETUP },
			{ 0x25, MyCmd.Keycode.PLAY_PAUSE },
			{ 0x26, MyCmd.Keycode.KEY_TURN_A },
			{ 0x27, MyCmd.Keycode.KEY_TURN_D },
			// { 0x28, MyCmd.Keycode. },
			{ 0x29, MyCmd.Keycode.HOME }, { 0x2A, MyCmd.Keycode.KEY_FM },
			{ 0x2B, MyCmd.Keycode.KEY_AM }, { 0x2C, MyCmd.Keycode.SETUP },
			{ 0x2D, MyCmd.Keycode.MENU }, { 0x2E, MyCmd.Keycode.BACK },
			{ 0x2F, MyCmd.Keycode.PREVIOUS },
			{ 0x31, MyCmd.Keycode.NAVIGATION },
			{ 0x32, MyCmd.Keycode.NAVIGATION }, { 0x33, MyCmd.Keycode.BT },
			{ 0x34, MyCmd.Keycode.NEXT },
	// { 0x35, MyCmd.Keycode.SETUP },

	};

	private boolean isOneKey(byte b) {
		return ((b & 0xff) >= 0x81) || ((b & 0xff) == 0x3c) || ((b & 0xff) == 0x3d) || ((b & 0xff) == 0x3e) || ((b & 0xff) == 0x3f);
	}


	private int mKeyDown = 0;

	private void parseWheelKey05(byte[] data, int len) {
		if (data[2] != 0) {
			if (doKeyStudy(data[2], 1)) {
				mKeyDown = data[2];
				return;
			}
		} else {
			if (doKeyStudy(mKeyDown, 0)) {
				return;
			}

			mKeyDown = 0;
		}

		byte key = 0;
		if (KEYS_PANNEL == null) {
			return;
		}
		for (int i = 0; i < KEYS_PANNEL.length; ++i) {
			if (KEYS_PANNEL[i][0] == data[2]) {
				key = KEYS_PANNEL[i][1];
				break;
			}
		}

		if (key != 0) {
			doKey(key, 1);
		} else {
			doKey(0, 0);
		}
	}

	private void parseWheelKey(byte[] data, int len) {
		if (data[0] == 0x5){
			parseWheelKey05(data, len);
		} else {
			parseWheelKey06(data, len);
		}
	}
	private void parseWheelKey06(byte[] data, int len) {

		if (doKeyStudy(data[2], data[3])) {
			return;
		}

		byte key = 0;
		if (KEYS_PANNEL == null) {
			return;
		}
		for (int i = 0; i < KEYS_PANNEL.length; ++i) {
			if (KEYS_PANNEL[i][0] == data[2]) {
				key = KEYS_PANNEL[i][1];
				break;
			}
		}

		if (key != 0) {

			if (((data[2] & 0xff) == 0x19) || ((data[2] & 0xff) == 0x1a)) {
				int step = data[3] & 0xff;
				// step = 0x10;
				if (step <= 8) {
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

	private final byte[] mAirData = new byte[8];

	private void parseACInfo(byte[] data, int len) {

		byte[] airData = new byte[8];

		if (data[2] > 0 && data[2] < 30) {
			data[2] = (byte) ((17f + (0.5f * data[2])) * 2);
		} else if (data[2] >= 62 && data[2] <= 90) {
			airData[5] = (byte) 0x1;
		} else if (data[2] != 0) {
			data[2] = (byte) 0xff;
		}

		if (data[3] > 0 && data[3] < 30) {
			data[3] = (byte) ((17f + (0.5f * data[3])) * 2);
		} else if (data[3] >= 62 && data[3] <= 90) {
			airData[5] = (byte) 0x1;
		} else if (data[3] != 0) {
			data[3] = (byte) 0xff;
		}

		airData[2] = (byte) (data[2] & 0xff);
		airData[3] = (byte) (data[3] & 0xff);
		airData[1] = (byte) ((data[4] & 0x0f) | ((data[5] & 0x40) << 1)
				| ((data[5] & 0x10) << 1) | ((data[5] & 0x08) << 3));

		airData[0] = (byte) (((data[5] & 0x80) >> 2) | ((data[5] & 0x20) >> 5)
				| ((data[5] & 0x04) << 1) | ((data[5] & 0x02) << 1) | ((data[5] & 0x01) << 6));

		if (Util.isBufEquals(airData, mAirData)) {
			return;
		}

		Util.byteArrayCopy(mAirData, airData, 0, 0, airData.length);
		int msg = CANBOX_RETURN_AIR;
//		if (((data[4] & 0x0f) != 0)) {
//			msg = CANBOX_RETURN_AIR;
//		}
		Handler handler = getHandler("CanService");
		if (null != handler) {
			handler.sendMessage(handler.obtainMessage(msg, airData));
		}
	}

	private void do1050SosCustomCmd(byte[] data) {
		if (MachineConfig.VALUE_SYSTEM_UI22_1050.equals(GlobalDefinition.mSystemUI)
				|| MachineConfig.VALUE_SYSTEM_UI_1050_2
						.equals(GlobalDefinition.mSystemUI)
				|| CarUtil.getCarType() == 4) {
			switch (data[2]) {
			case 0x23:
				if (data[3] == 1) {
					SosManager.start(mContext, 1);
				}
				break;
			case 0x24:
			case 0x26:
				if (data[3] == 1) {
					SosManager.stop();
				}
				break;
			case 0x25:
				if (data[3] == 1) {
					SosManager.start(mContext, 2);
				}
				break;
			}
		}
	}

	private byte getRadarData(int i) {
		byte data = 0;
		switch (i) {
		case 3:
			data = RADAR_DISTANCE_WANRING;
			break;
		case 2:
			data = RADAR_DISTANCE_NORMAL;
			break;
		case 1:
			data = RADAR_DISTANCE_LONG;
			break;
		}
		return data;
	}

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[1]) {
		case 0x2: {
			parseWheelKey(data, len);
		}
			break;
		case 0x3: {
			parseACInfo(data, len);
		}
			break;
		case 0x4: // Radar back
		{
			mRadar[0] = getRadarData((data[2] & 0xc0) >> 6);
			mRadar[1] = getRadarData((data[2] & 0x30) >> 4);
			mRadar[2] = getRadarData((data[2] & 0x0c) >> 2);
			mRadar[3] = getRadarData((data[2] & 0x03) >> 0);
			mRadar[4] = getRadarData((data[3] & 0xc0) >> 6);
			mRadar[5] = getRadarData((data[3] & 0x30) >> 4);
			mRadar[6] = getRadarData((data[3] & 0x0c) >> 2);
			mRadar[7] = getRadarData((data[3] & 0x03) >> 0);

			boolean zero = Util.isZero(mRadar);
			if (!zero) {
				RadarManager.start(mContext);
				checkHideRadar();
			}

			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
			}

		}
			break;
		case 0x5: {
			int door = (data[2] & 0x3f);
			// door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6)
			// | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) <<
			// 1));

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
		case 0x7d: {
			switch (data[2]) {
			case 0x5:
				int door = (data[3] & 0xfc);
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
				break;
			case 0x9:
				int temp = (data[4] & 0xff) - 40;
				updateOutDoorTemp(temp * 10);
				break;
			case 0x8: {
				Handler handler = getHandler("Reverse");
				if (null != handler) {
					short a = (short) ((data[3] & 0xff) | ((data[4] & 0xff) << 8));// bu
					// ma

					int angle = -(((a * 3000) / 540));

					if (angle > -50 && angle < 0) {
						angle = -50;
					} else if (angle > 0 && angle < 50) {
						angle = 50;
					}
					handler.sendMessage(handler.obtainMessage(
							CANBOX_STEER_ANGLE, angle, 100));
				}
			}
				break;
			}

		}
			break;

		case 0x7f:
			byte[] version = new byte[data[0] - 3];
			Util.byteArrayCopy(version, data, 0, 3, version.length);

			mVersion = (new String(version));
			break;

		case 0x40: {

			d360[0] = (byte) (((data[2] & 0x01) << 7) | ((data[2] & 0x02) << 5));
			switch (data[3]) {
			case 0:
				break;
			case 1:
				d360[0] |= 0x5;
				break;
			case 2:
				d360[0] |= 0x6;
				break;
			case 3:
				d360[0] |= 0x7;
				break;
			case 4:
				d360[0] |= 0x8;
				break;
			case 5:
				d360[0] |= 0x2;
				break;
			case 6:
				d360[0] |= 0x1;
				break;
			case 7:
				d360[0] |= 0x2;
				break;
			case 8:
				d360[0] |= 0x3;
				break;
			case 9:
				d360[0] |= 0x4;
				break;
			case 0xa:
				d360[0] |= 0x1;
				break;

			}

			Handler handler = getHandler("Reverse");
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_HY_UI_DATA,
						d360));
			}
			byte reverse = 0;
			if ((d360[0] & 0xC0) != 0x0) {
				reverse = 1;
			}
			OSProManager.simulationReverse(reverse);

			break;
		}
		case 0x41:
			d360[1] = (byte) (((data[2] & 0xc0)));
			switch (data[3] & 0xf) {
			case 2:
				d360[1] |= 0x01;
				break;
			case 3:
				d360[1] |= 0x02;
				break;
			case 4:
				d360[1] |= 0x03;
				break;
			}

			switch (data[4] & 0xf) {
			case 2:
				d360[1] |= 0x08;
				break;
			case 3:
				d360[1] |= 0x10;
				break;
			case 4:
				d360[1] |= 0x18;
				break;
			}

			Handler handler = getHandler("Reverse");
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_HY_UI_DATA,
						d360));
			}
			break;
		}

		if (data[1] == 0x7d || data[1] == 0x52|| data[1] == 0x57) {
			sendCanboxInfo("com.canboxsetting", data);
		}
	}

	byte[] d360 = new byte[2];
	private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;

	private int mUnit = 0;

	@SuppressLint("DefaultLocale")
	public void updateOutDoorTemp(int temp) {

		if (temp == CarUtil.INVALID_OUT_DOOR_TEMP) {
			if (mTempOutDoor != CarUtil.INVALID_OUT_DOOR_TEMP) {
				temp = mTempOutDoor;
			} else {
				return;
			}
		}
		mTempOutDoor = temp;

		if (CarUtil.mTempUnit == 2) {
			if (mUnit != 1) {
				float t = temp / 10.0f;
				temp = (int) (((t) * 1.8f + 32) * 10);
			}
			mUnit = 1;
		} else if (CarUtil.mTempUnit == 1) {
			if (mUnit == 1) {
				temp = (int) ((((float) temp / 10.0f) - 32) / 1.8f) * 10;
			}
			mUnit = 0;
		}

		String s = null;
		if (temp != 2550) {
			if (mUnit != 1) {

				s = String.format(
						"%d.%d%s",
						temp / 10,
						(temp % 10) >= 0 ? (temp % 10) : -(temp % 10),
						mContext.getResources().getString(
								R.string.temp_unic_centigrade));

			} else {
				s = String.format("%d%s", temp / 10, mContext.getResources()
						.getString(R.string.temp_unic_fahrenheit));
			}
		} else {
			s = "";
		}

		if (s != null) {
			GlobalDefinition.sendByCarServiceToSystemUI(mContext,
					"com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
		}

	}

	private int mDoorStatus = 0;

	private void checkHideRadar() {
		mHandler.removeMessages(HIDE_RADAR);
		mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
	}

	private final static int HIDE_RADAR = 0;
	private final static int SHOW_VOLUME_STEP = 1;
	private final static int REPEAT_SEND_EQ = 2;
	private final static int REPEAT_SET_CARTYPE = 3;

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

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_RADAR:
				RadarManager.stop();
				break;
			case SHOW_VOLUME_STEP:
				doKeyStep(msg.arg1, msg.arg2);
				break;
			case REPEAT_SET_CARTYPE:
				setCarType();
				break;
			}
			super.handleMessage(msg);
		}
	};

	private void setCarType() {//just for elec car now
//		int t = CarUtil.getCarType();
//		mHandler.removeMessages(REPEAT_SET_CARTYPE);
//		if ((t & 0xff00) != 0) {
//			byte[] data = new byte[] { 0x6, (byte) 0xa9, (byte)0xff, 1 };
//			sendDataToCanbox(data, data.length);
//		}
//		
//		t = (t & 0xff);
//		if (t > 0) {
//
//			byte t2 = 0;
//			if (t == 0x20) {
//				t2 = (byte) CarUtil.getCarType2();
//			}
//			byte[] data = new byte[] { 0x6, (byte) 0xee, (byte)t, t2 };
//			sendDataToCanbox(data, data.length);
//
//			mHandler.sendEmptyMessageDelayed(REPEAT_SET_CARTYPE, 1000);
//		}
		
		//new 
		
		byte[] cmd = new byte[] { 0x6, (byte) 0xa9, (byte) 0xff, 1 };
		switch (CarUtil.getModelId()) {
		case 31:
		case 32:
		case 36:
		case 26:
			sendDataToCanbox(cmd, cmd.length);
			break;
		}
	}



	byte[] buf = new byte[8];


	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		byte h = (byte) ((time / 3600));
		byte min = (byte) ((time / 60) % 60);
		byte sec = (byte) ((time) % 60);

		byte s = 0;
		// byte len = 9;
		switch (source) {
		case MyCmd.SOURCE_DVD:
			s = 0x1;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			s = 0xC;
			break;
		// case MyCmd.SOURCE_BT:
		// s = 0x11;
		// break;
		}

		mData = new byte[] { (byte) 0xa, 0x9, s, (byte) ((play & 0xFF00) >> 8),
				(byte) ((play) & 0xFF), h, min, sec };

		if (mPhoneStatus < HFP_INFO_CALLED) {

			sendDataToCanbox(mData, mData.length);
		}
	}

	byte[] mData;

	private int Sum(byte[] data, int len) {
		int sum = 0;

		for (int i = 0; i < len; ++i) {
			sum += (data[i] & 0xff);
		}
		return sum & 0xFFFF;
	}

	public void sendDataToCanbox(byte[] data, int len) {
		if ((data[0] & 0xff) > 30) {
			Log.e(TAG, "cmd too long will err:" + data[0]);
			return;
		}
		byte[] send = new byte[len + 4];
		send[0] = (byte) (len + 3);
		send[1] = (byte) 0xfd;
		send[len + 2] = (byte) ((Sum(data, len) >> 8) & 0xFF);
		send[len + 3] = (byte) ((Sum(data, len) >> 0) & 0xFF);
		byteArrayCopy(send, data, 2, 0, len);
		sendCommonDataToCanbox(send);
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		if (b[0] >= 0x10) {
			type = 9;
			b[0] = (byte) (3 + (b[0] - 0x10));
		} else {
			type = 2;
		}
		int freq = (b[2] & 0xff) << 8 | (b[1] & 0xff);
		b[1] = (byte) (freq / 100);
		b[2] = (byte) (freq % 100);

		mData = new byte[] { 0x8, 0x9, type, b[0], b[1], b[2] };
		if (mPhoneStatus < HFP_INFO_CALLED) {
			sendDataToCanbox(mData, mData.length);
		}
	}

	private int mSource = MyCmd.SOURCE_NONE;

	public void setMediaSrc(int source) {
		mSource = source;
	}

	private void setEQVolume(int volume) {
		Util.doSleep(2);
		byte[] data = new byte[] { (byte) 0x5, 0x5, (byte) volume };
		sendDataToCanbox(data, data.length);
		Util.doSleep(2);
	}

	private void setPower(int power) {
		Util.doSleep(2);
		byte[] data = new byte[] { (byte) 0x5, 0x4, (byte) power };
		sendDataToCanbox(data, data.length);
		Util.doSleep(2);
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
		byte[] buf = { (byte) 0x6, 0x3, 0x1, (byte) lang };
		sendDataToCanbox(buf, buf.length);
	}

	private void startEQ() {
//		if (!CarUtil.mIsNeedSendEQ) {
//			return;
//		}

		if (CarUtil.getProIndex() <= 0) {
			mEQData[5] = (byte)MachineConfig
					.getIntProperty2(SystemConfig.CANBOX_EQ_VOLUME);
			if (mEQData[5] == -1) {
				mEQData[5] = 32;
			}
			setEQVolume(mEQData[5]);
		} else {
			String s = MachineConfig.getProperty(SystemConfig.CANBOX_EQ_VOLUME);
			if (s != null) {
				String[] ss = s.split(",");
				if (ss != null && ss.length > 5) {
					try {
						mEQData[0] = Byte.valueOf(ss[0]);
						mEQData[1] = Byte.valueOf(ss[1]);
						mEQData[2] = Byte.valueOf(ss[2]);
						mEQData[3] = Byte.valueOf(ss[3]);
						mEQData[4] = Byte.valueOf(ss[4]);
						mEQData[5] = Byte.valueOf(ss[5]);
						
						

						byte[] buf;

						buf = new byte[] { (byte) 0x7, 0x8, mEQData[2],
								mEQData[1], mEQData[0] };
						sendDataToCanbox(buf, buf.length);
						Util.doSleep(20);
						buf = new byte[] { (byte) 0x6, 0x7, mEQData[3],
								mEQData[4] };
						sendDataToCanbox(buf, buf.length);

						Util.doSleep(20);
						buf = new byte[] { (byte) 0x5, 0x5, mEQData[5] };
						sendDataToCanbox(buf, buf.length);
						
						
					} catch (Exception e) {

					}
				}
			}
		}
		// mHandler.removeMessages(REPEAT_SEND_EQ);
		// mHandler.sendEmptyMessageDelayed(REPEAT_SEND_EQ, 300);
	}



	public void startConnect() {

		setPower(0);
		startEQ();
		setCarType();
	}

//	private int mVolume = 32;

	public void stopConnect() {
		setPower(1);
		mHandler.removeMessages(REPEAT_SET_CARTYPE);
	}

	private int mPhoneStatus = HFP_INFO_INITIAL;

	public void setPhone(int status, String num) {// default is simple box

		mPhoneStatus = status;

		switch (status) {
		case HFP_INFO_INITIAL:
		case HFP_INFO_READY:
		case HFP_INFO_CONNECTING:

			status = 5;
			break;
		case HFP_INFO_CONNECTED:
			status = 4;
			break;
		case HFP_INFO_CALLED:
			status = 3;
			break;
		case HFP_INFO_INCOMING:
			status = 1;
			break;
		case HFP_INFO_CALLING:
			status = 2;
			break;
		}

		byte[] data2;

		data2 = new byte[] { (byte) 0x6, 0x9, 0x7, (byte) status };

		sendDataToCanbox(data2, data2.length);

	}

	public void updateTime() {
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);
		byte ampm = 1;
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		if ("12".equals(strTimeFormat)) {

			ampm = 0;

		}

		byte m = (byte) curDate.getMinutes();

		byte[] buf = new byte[] { (byte) 0x7, 0x06, m, h, ampm };
		sendDataToCanbox(buf, buf.length);

	}

	
	byte[] mEQData = new byte[] { 10, 10, 10, 7, 7, 30};

	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (36 << 16) | (15 << 8) | 21;
			returnEQData();
		} else {
			byte[] buf;// = new byte[] { (byte) 0x6, 0x2, 0x0, (byte) data };
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				mEQData[0] = (byte) data;
				break;
			case EQ_CMD_SET_MIDDLE:
				mEQData[1] = (byte) data;
				break;
			case EQ_CMD_SET_LOW:
				mEQData[2] = (byte) data;
				break;
			case EQ_CMD_SET_ZONE_FR:
				mEQData[3] = (byte) data;
				break;
			case EQ_CMD_SET_ZONE_LR:
				mEQData[4] = (byte) data;
				break;
			case EQ_CMD_SET_VOLUME:
				mEQData[5] = (byte) data;
				break;
			default:
				return 0;
			}

			switch (cmd) {
			case EQ_CMD_SET_HIGH:
			case EQ_CMD_SET_MIDDLE:
			case EQ_CMD_SET_LOW:
				buf = new byte[] { (byte) 0x7, 0x8, mEQData[2], mEQData[1],
						mEQData[0] };
				break;
			case EQ_CMD_SET_ZONE_FR:
			case EQ_CMD_SET_ZONE_LR:
				buf = new byte[] { (byte) 0x6, 0x7, mEQData[3], mEQData[4] };
				break;
			case EQ_CMD_SET_VOLUME:
				buf = new byte[] { (byte) 0x5, 0x5, mEQData[5] };
				break;
			default:
				return 0;
			}
			
			String value = mEQData[0]+","+mEQData[1]+","+mEQData[2]+","+mEQData[3]+","+mEQData[4]+","+mEQData[5];
			MachineConfig.setProperty(
					SystemConfig.CANBOX_EQ_VOLUME, value);
			sendDataToCanbox(buf, buf.length);
			returnEQData();
		}
		return ret;
	}

	private void returnEQData() {
		super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
	}
	public int getUpdateTime() {
		return 60000;
	}
	
	public boolean isSupportCompass() {
		return true;
	}	
			
	public void updateCompass(int compass) {		
		int direction = compassAngleToDirect16(compass);
		
		byte[] buf = new byte[] { 0x6, (byte) (0x86), (byte)(direction&0xff), 0};

		sendDataToCanbox(buf, buf.length);
	}
}
