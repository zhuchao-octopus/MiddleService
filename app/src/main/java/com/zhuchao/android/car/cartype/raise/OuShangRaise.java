package com.zhuchao.android.car.cartype.raise;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.McuManager;
import com.zhuchao.android.car.R;
import com.common.util.AppConfig;
import com.common.util.MyCmd;
import com.common.util.Util;

import java.util.Date;
import java.util.Locale;


public class OuShangRaise extends Canbox {

	public OuShangRaise() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });

		buildCmdRepeatSendCarType(getCarTypeCmd());

	}

	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0xee, 0x02, 3, 0 };
		byte t = (byte) CarUtil.getCarType();

		if (t != 0) {
			cmd[2] = t;
			cmd[3] = (byte) CarUtil.getCarType2();
			return cmd;
		}
		return null;
	}
	private final static byte[][] KEYS_WHEEL = { { 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, { 0x21, AK_KEYPAD_VOLUME_A },
			{ 0x22, AK_KEYPAD_VOLUME_D },
			{ 0x3, MyCmd.Keycode.MULT_NEXT_AND_HANG },
			{ 0x4, MyCmd.Keycode.MULT_PREV_AND_RECEIVE }, { 0x5, KEY_MUTE },
			{ 0x6, KEY_SOURCE }, { 0x7, MyCmd.Keycode.BT_DIAL },
			{ 0x7, MyCmd.Keycode.BT_HANG }, { (byte) 0x88, KEY_MIC },
			{ 0x9, MyCmd.Keycode.RADIO }, { 0xa, MyCmd.Keycode.POWER },

			{ 0x11, AK_KEYPAD_VOLUME_A }, { 0x12, AK_KEYPAD_VOLUME_D },
			{ 0x13, KEY_HOME }, { 0x14, KEY_MEDIA },

			{ 0x23, MyCmd.Keycode.PLAY_PAUSE },
			{ 0x24, MyCmd.Keycode.KEY_TURN_A },
			{ 0x25, MyCmd.Keycode.KEY_TURN_D },

			{ 0x27, KEY_NEXTSONG }, { 0x26, KEY_PREVIOUSSONG },
			{ 0x28, KEY_SET }, { 0x29, KEY_GPS }, { 0x2A, MyCmd.Keycode.DARK },

			{ 0x2b, MyCmd.Keycode.AS }, { 0x2c, MyCmd.Keycode.KEY_LIST },
			{ 0x2d, MyCmd.Keycode.BT },

			{ 0x2e, KEY_GPS }, { 0x2f, KEY_EQ }, { 0x30, KEY_MENU },
			{ 0x31, MyCmd.Keycode.PLAY_PAUSE },
			

			{ 0x1A, MyCmd.Keycode.NUMBER1 },
			{ 0x1B, MyCmd.Keycode.NUMBER2 },
			{ 0x1C, MyCmd.Keycode.NUMBER3 },
			{ 0x1D, MyCmd.Keycode.NUMBER4 },
			{ 0x1E, MyCmd.Keycode.NUMBER5 },
			{ 0x1F, MyCmd.Keycode.NUMBER6 },

	};

	private void parseWheelKey(byte[] data) {

		if (doKeyStudy(data[2], data[3])) {
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

			if (((data[2] & 0xff) == 0x21) || ((data[2] & 0xff) == 0x22)) {
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
			} else {
				doKey(key, data[3]);
			}
		} else {
			if (data[3] == 0) {
				doKey(0, 0);
			}
		}
	}

	private void parseACInfo(byte[] data, int len) {

		if (data[5] >= 0x1e) {
			data[5] = (byte) 0xff;
		} else if (data[5] > 0) {
			data[5] = (byte) ((17.5f + (0.5f * data[5])) * 2);
		}
		// if (data[5] >= 0x1f) {
		// data[5] = (byte) 0xff;
		// } else if (data[5] > 0) {
		// if ((data[6] & 0x1) == 0) {
		// data[5] = (byte) ((15.5f + (0.5f * data[5])) * 2);
		// } else {
		// data[5] = (byte) ((59 + (data[5] & 0xff)));
		// }
		// }
		byte[] airData = new byte[8];

		airData[0] = (byte) (((data[2] & 0x13) >> 0) | ((data[2] & 0x08) << 3) | ((data[2] & 0x04) << 3));

		airData[1] = (byte) (data[3] & 0xff);
		if ((data[4] & 0xff) == 0) {
			airData[1] |= 0x40;
		} else if ((data[4] & 0xff) == 1) {
			airData[1] |= 0x60;
		} else if ((data[4] & 0xff) == 2) {
			airData[1] |= 0x20;
		} else if ((data[4] & 0xff) == 3) {
			airData[1] |= 0x80;
		} else if ((data[4] & 0xff) == 4) {
			airData[1] |= 0xa0;
		}

		airData[2] = (byte) (data[5] & 0xff);
		airData[3] = (byte) (0xfa);

		airData[4] = (byte) ((data[2] & 0x60) >> 3);


		super.parseACInfo(airData);
	}

	private byte getRadarData(byte i) {
		byte data = 0;
		switch (i) {
		case 0:
			data = 0;
			break;
		case 1:
			data = 11;
			break;
		case 2:
			data = 7;
			break;
		case 3:
			data = 1;
			break;
		}
		return data;
	}
	
	private byte getRadarDataEx(byte i) {
		byte data = 0;
		if (i == 0) {
			data = 1;
		} else if ((i & 0xff) > 150) {
			data = 11;
		} else {
			data = (byte) (2 + (i / 15));
		}
		return data;
	}
	
	private int mShowExRadar = 0;
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x20: {
			parseWheelKey(data);
		}

			break;

		case 0x21: {
			parseACInfo(data, len);
		}
			break;
		case 0x22: // Radar back
		{
			if ((mShowExRadar&0x1)!=0){
				return;
			}
			mRadar[0] = getRadarData(data[2]);
			mRadar[1] = getRadarData(data[3]);
			mRadar[2] = getRadarData(data[4]);
			mRadar[3] = getRadarData(data[5]);

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
		case 0x23: // Radar back
		{
			if ((mShowExRadar&0x2)!=0){
				return;
			}
			mRadar[4] = getRadarData(data[2]);
			mRadar[5] = getRadarData(data[3]);
			mRadar[6] = getRadarData(data[4]);
			mRadar[7] = getRadarData(data[5]);
			boolean zero = Util.isZero(mRadar);
			if (!zero) {
				RadarManager.start(mContext);
				checkHideRadar();
			}
			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
			}
		}
			break;
		case 0x24: // Radar back
		{
			
			mRadarLeft[0] = getRadarData(data[2]);
			mRadarLeft[1] = getRadarData(data[3]);
			mRadarLeft[2] = getRadarData(data[4]);
			mRadarLeft[3] = getRadarData(data[5]);
			
			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_LEFT));
			}
		}
			break;
		case 0x25: // Radar back
		{
			
			mRadarRight[0] = getRadarData(data[2]);
			mRadarRight[1] = getRadarData(data[3]);
			mRadarRight[2] = getRadarData(data[4]);
			mRadarRight[3] = getRadarData(data[5]);
			
			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_RIGHT));
			}
		}
			break;
		case 0x26: // Radar back
		{

			mRadar[0] = getRadarDataEx(data[2]);
			mRadar[1] = getRadarDataEx(data[3]);
			mRadar[2] = getRadarDataEx(data[4]);
			mRadar[3] = getRadarDataEx(data[5]);
			mShowExRadar |= 0x1;
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
		case 0x27: // Radar back
		{

			mRadar[4] = getRadarDataEx(data[2]);
			mRadar[5] = getRadarDataEx(data[3]);
			mRadar[6] = getRadarDataEx(data[4]);
			mRadar[7] = getRadarDataEx(data[5]);

			mShowExRadar |= 0x2;
			boolean zero = Util.isZero(mRadar);
			if (!zero) {
				RadarManager.start(mContext);
				checkHideRadar();
			}
			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
			}
		}
			break;
		case 0x30: {
			if (CarUtil.getProIndex() > -1 || CarUtil.getKeyType() == 3 || CarUtil.getKeyType() == 1) {
				doRightCameraSwitch(data[2] & 0x80);
			}
			break;
		}
		case 0x31: {
			if (CarUtil.getProIndex() > -1  || CarUtil.getKeyType() == 3 || CarUtil.getKeyType() == 2) {
				do360CameraSwitch(data[2] & 0x80);
			}
			break;
		}
		case 0x3a: {
			int door = (data[2] & 0xff);
			door = (((door & 0x20) >> 5) | ((door & 0x10) >> 3)
					| ((door & 0x08) >> 1) | ((door & 0x04) << 1)
					| ((door & 0x02) << 3) | ((door & 0x1) << 5) | ((door & 0x40) << 1));

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

		case 0x29: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				int angle = ((data[3] & 0xff) | ((data[2]) << 8));
				angle = (angle * 300 / 0x157c);
				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle, 10));
			}
		}
			break;

		case 0x40:
			if (data[2] == (byte) 0xa0) {
				if ((data[3] & 0x10) != 0) {
					try {
						Intent it = new Intent(Intent.ACTION_VIEW);
						it.setClassName("com.canboxsetting",
								"com.canboxsetting.MainActivity");
						it.putExtra(MyCmd.EXTRA_COMMON_CMD, 1);
						it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
								| Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startActivity(it);
					} catch (Exception e) {
						// Log.e(TAG, e.getMessage());
					}
				}
			}
			break;
		}

		if (data[0] == 0x52 || data[0] == 0x41 || data[0] == 0x3a
				|| data[0] == 0x38 || data[0] == 0x39) {
			sendCanboxInfo("com.canboxsetting", data);
		}
	}

	private void do360CameraSwitch(int s) {

		String top = AppConfig.getTopActivity();

		Intent it = new Intent(Intent.ACTION_VIEW);
		boolean topIsCamera = top != null && top.contains("com.android.car.frontcamera.FrontCameraActivity");
		if (s == 0) {
			if (topIsCamera) {
				it.putExtra("finish", 1);
			}
		} else {
			if (!topIsCamera) {
				topIsCamera = true;
			}
		}

		if (topIsCamera) {
			try {
				it.setClassName("com.car.ui",
						"com.android.car.frontcamera.FrontCameraActivity");
				it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				it.putExtra("camera", 1);
				mContext.startActivity(it);
			} catch (Exception e) {
				// Log.e(TAG, e.getMessage());
			}
		}
	}
	
	private void doRightCameraSwitch(int s) {

		String top = AppConfig.getTopActivity();

		Intent it = new Intent(Intent.ACTION_VIEW);
		boolean topIsCamera = top != null && top.contains("com.android.car.frontcamera.FrontCameraActivity");
		if (s == 0) {
			if (topIsCamera) {
				it.putExtra("finish", 1);
			}
		} else {
			if (!topIsCamera) {
				topIsCamera = true;
			}
		}

		if (topIsCamera) {
			try {
				it.setClassName("com.car.ui",
						"com.android.car.frontcamera.FrontCameraActivity");
				it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
						| Intent.FLAG_ACTIVITY_NEW_TASK);

				mContext.startActivity(it);
			} catch (Exception e) {
				// Log.e(TAG, e.getMessage());
			}
		}
	}

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

		String s = "";
		// if (temp >= -58 && temp <= 171) {
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

		if (s.length() > 1) {
			GlobalDefinition.sendByCarServiceToSystemUI(mContext,
					"com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
		}

	}

	private final byte mRadarSwitch = 0;
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

	byte[] data;

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		// byte h = (byte) ((time / 3600));
		// byte min = (byte) ((time / 60) % 60);
		// byte sec = (byte) ((time) % 60);
		// ++play;

		byte s = 0;
		switch (source) {
		case MyCmd.SOURCE_DVD:
			s = 0x8;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			s = 8;
			break;
		case MyCmd.SOURCE_BT:
			s = 0xb;
			break;
		default:
			s = 0x07;
			break;
		}
		
		if (CarUtil.getCarType2() == 14){
			data75[2] = 4;
			data75[3] = 0;
			data75[4] = 0;
			data75[5] = 0;		
			data75[6] = 0;			
			sendDataToCanbox(data75, data75.length);
		} else {

		data = new byte[] { (byte) 0xc0, 0xa, s, 0,
				(byte) ((total & 0xFF) >> 8), (byte) (total & 0xFF),

				(byte) ((play & 0xFF) >> 8), (byte) (play & 0xFF),

				(byte) ((total_time & 0xFF) >> 8), (byte) (total_time & 0xFF),

				(byte) ((time & 0xFF) >> 8), (byte) (time & 0xFF), };

		if (mPhoneStatus < HFP_INFO_CALLED) {

			sendDataToCanbox(data, data.length);
		}
		}
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		// if (b[0] >= 0x10) {
		// b[0] = (byte) (b[0] - 0x10);
		// } else {
		// b[0] = (byte) (b[0] + 0x10);
		// }
		if (b[3] < 0 || b[3] > 12) {
			b[3] = 0;
		}
		if (CarUtil.getCarType2() == 14){
			data75[2] = 1;
			if (b[0] >= 0x10) {
				data75[3] = 3;
				
			} else {
				data75[3] = 1;
				int freq = ((b[2] & 0xff) << 8) | (b[1] & 0xff);
				freq = freq/10;

				b[2] =(byte)( (freq & 0xff00) >> 8);
				b[1] =(byte)( (freq & 0xff) >> 0);
			}
			
			if (b[3] < 0 || b[3] > 6) {
				b[3] = 0;
			}
			
			data75[4] = b[2];
			data75[5] = b[1];	
			data75[6] = b[3];		
			sendDataToCanbox(data75, data75.length);
		} else {
			data = new byte[] { (byte) 0xc0, 0x5, 0x1, b[0], b[1], b[2], b[3] };
			sendDataToCanbox(data, data.length);
		}
	}
	byte[] data75 =new byte[] { (byte) 0x75, 0x8,  0, 0, 0, 0, 0, 0, 0, 1 };
	public void setMediaSrc(int source) {// default is simple box
		byte s;
		
		byte[] data ;
		if (CarUtil.getCarType2() == 14){
			switch (source) {
			case MyCmd.SOURCE_AUX:
				s = 5;
				break;
			case MyCmd.SOURCE_BT_MUSIC:
				s = 6;
				break;
			default:
				return;
			}
			data75[2] = s;
			data75[3] = 0;
			data75[4] = 0;
			data75[5] = 0;	
			data75[6] = 0;		
			sendDataToCanbox(data75, data75.length);
		} else {
			if (source == MyCmd.SOURCE_AUX) {
				s = 7;
			} else {
				return;
			}
			data = new byte[] { (byte) 0xc0, 0xa, s, 0, 0, 0, 0, 0, 0, 0, 0,
				0 };	
			sendDataToCanbox(data, data.length);
		}
		
		
	}

	public void sendId3(byte index, String num) {

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = num.getBytes();

//			n = num.getBytes("UTF-8"); // del 0xff 0xfe
			int num_len = n.length;		
			if (num_len >= 70) {
				num_len = 70;
			}
			int len = num_len+7;
			byte[] data = new byte[len];

			data[0] = (byte) 0xc0;
			data[1] = (byte) (len - 2);
			data[2] = 8;
			data[3] = (byte)0xff;
			data[4] = 0x12;
			data[5] = index;
			data[6] = (byte)num_len;
			
			for (int i = 0; i < num_len; ++i) {
				Log.d("Accord2013Simple", i + ":" + n.length + ":" + num_len);
				data[7 + i] = n[i];
			}
			
			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

			Log.d("Accord2013Simple", "sendId3" + e);
		}
	}

	String mName = null;
	String mArtist = null;
	String mAlbum = null;

	// public void setPhone(int status, String num) {
	// sendId3((byte)0x1, num);
	// }

	public void setSongName(String s) {
		sendId3((byte) 0x1, s);
		mName = s;
	}

	public void setSongAritst(String s) {
		sendId3((byte) 0x2, s);
		mArtist = s;
	}

	public void setSongAlbum(String s) {
//		sendId3((byte) 0x3, s);
//		mAlbum = s;
	}

	private int mPhoneStatus = HFP_INFO_INITIAL;

	public void setPhone(int status, String num) {// default is simple box

		mPhoneStatus = status;

		switch (status) {
		case HFP_INFO_INITIAL:
		case HFP_INFO_READY:
		case HFP_INFO_CONNECTING:
		case HFP_INFO_CONNECTED:
			status = 0;
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

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = num.getBytes();

//			n = num.getBytes("UTF-8"); // del 0xff 0xfe
			int num_len = n.length;		
			if (num_len >= 70) {
				num_len = 70;
			}
			int len = num_len+8;
			byte[] data = new byte[len];

			data[0] = (byte) 0xc0;
			data[1] = (byte) (len - 2);
			data[2] = 5;
			data[3] = (byte)status;
			data[4] = 0;
			data[5] = 0;
			data[6] = 1;
			data[7] = (byte)num_len;
			
			for (int i = 0; i < num_len; ++i) {
				Log.d("Accord2013Simple", i + ":" + n.length + ":" + num_len);
				data[8 + i] = n[i];
			}
			
			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

			Log.d("Accord2013Simple", "sendId3" + e);
		}

	}

	public void setVolume(int volume) {
		if (CarUtil.getCarType2() == 14) {
			if (volume == 0) {
				data75[9] = 2;
			} else {
				data75[9] = 1;
			}
			sendDataToCanbox(data75, data75.length);
		}
	}

	private void checkHideRadar() {
		mHandler.removeMessages(HIDE_RADAR);
		mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
	}

	private final static int HIDE_RADAR = 0;
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

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_RADAR:
				mShowExRadar = 0;
				RadarManager.stop();
				break;
			case SHOW_VOLUME_STEP:
				doKeyStep(msg.arg1, msg.arg2);
				break;
			}
			super.handleMessage(msg);
		}
	};
	public void setContext(Context c) {
		super.setContext(c);
		updateTime();
		udpateLang();
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

		byte format = 1;
		if ("12".equals(strTimeFormat)) {
//			if (h > 12) {
//				h -= 12;
//			} else if (h == 0) {
//				h = 12;
//			}
			format = 0;
		}

		byte m = (byte) curDate.getMinutes();


		byte[] buf = new byte[] { (byte) 0xc8, 0x03, m, h, format };
		 sendDataToCanbox(buf, buf.length);
	}
	
	public void udpateLang() {
		int lang = -1;
		String locale = Locale.getDefault().getLanguage();
		if (locale != null) {
			if (locale.equals("en")) {
				lang = 2;
			} else if (locale.equals("zh")) {				
				lang = 1;
			} 
		}
		if (lang != -1) {
			byte[] buf = { (byte) 0x83, 0x2, 0x39, (byte) lang };
			sendDataToCanbox(buf, buf.length);
		}
	}
	public int getUpdateTime() {
		return 60000;
	}
}
