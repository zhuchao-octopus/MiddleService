package com.zhuchao.android.car.cartype.ods;

import com.zhuchao.android.car.GlobalDefinition;


import android.os.Handler;

import com.zhuchao.android.car.R;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;

public class VolvoXC60 extends Canbox {

	public VolvoXC60() {

		buildCmdVersion((byte) 0x30, (byte) 0x0);
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2710);
		mIdAC = 0x21;
	}


	private final static byte[][] KEYS_WHEEL_NORMAL = {

	{ 0x20, KEY_NUM_0 }, { 0x21, KEY_NUM_1 }, { 0x22, KEY_NUM_2 },
			{ 0x23, KEY_NUM_3 }, { 0x24, KEY_NUM_4 }, { 0x25, KEY_NUM_5 },
			{ 0x26, KEY_NUM_6 }, { 0x27, KEY_NUM_7 }, { 0x28, KEY_NUM_8 },
			{ 0x29, KEY_NUM_9 }, { 0x2a, KEY_NUM_X },
			{ 0x2b, KEY_NUM_J },

			{ 0x33, KEY_FM },
			{ 0x34, KEY_AUX },
			{ 0x35, KEY_DVD },
			{ 0x36, KEY_AUX },
			{ 0x37, KEY_HOME },
			{ 0x38, KEY_EQ },
			{ 0x39, KEY_BT },
			{ 0x3d, MyCmd.Keycode.TIME_SETTING },
			{ 0x3f, KEY_POWER },

			{ 0x48, KEY_PLAYPAUSE },
			{ 0x49, MyCmd.Keycode.KEY_TURN_D },
			{ 0x4a, MyCmd.Keycode.KEY_TURN_A },
			{ 0x4b, KEY_PREVIOUSSONG },
			{ 0x4c, KEY_NEXTSONG },

			{ 0x52, MyCmd.Keycode.MULT_PREV_AND_RECEIVE },
			{ 0x53, MyCmd.Keycode.MULT_NEXT_AND_HANG },

			{ 0x54, KEY_EJECT },
			{ 0x56, MyCmd.Keycode.RDS_TA_SWITCH },
			{ 0x57, KEY_GPS },
			{ 0x59, KEY_EQ },
			{ 0x5a, KEY_MUTE },
//			{ 0x5b, MyCmd.Keycode.DARK },
//			
//
//			{ 0x62, MyCmd.Keycode.PLAY_PAUSE },
//			{ 0x60, MyCmd.Keycode.KEY_TURN_A },
//			{ 0x61, MyCmd.Keycode.KEY_TURN_D },
//			{ 0x68, MyCmd.Keycode.BACK },
//			{ 0x6A, MyCmd.Keycode.SETUP},
			
			
//			{ 0x6F, MyCmd.Keycode.AUDIO },
			// { 0x5c, 0 },
			// { 0x5d, 0 },
			// { 0x5e, 0 },
			// { 0x5f, 0 },

//			{ (byte) 0x86, KEY_PLAYPAUSE },
//			{ (byte) 0xF0, AK_KEYPAD_VOLUME_A },
//			{ (byte) 0xF1, AK_KEYPAD_VOLUME_D },
//			{ (byte) 0xF2, MyCmd.Keycode.KEY_TURN_A },
//			{ (byte) 0xF3, MyCmd.Keycode.KEY_TURN_D },

	};
	

	private final byte[][] KEYS_WHEEL = KEYS_WHEEL_NORMAL;

	private void parseWheelKey(byte[] data, int len) {
		if (doKeyStudy(data[2], data[3])){
			return;
		}
		
		switch (data[2]) {
		case 0x0:
			doKey(0, 0);
			break;
		case 0x1:
			doKey(AK_KEYPAD_VOLUME_A, data[3]); // vol+
			break;
		case 0x2:
			doKey(AK_KEYPAD_VOLUME_D, data[3]);// vol-
			break;
		case 0x3:
			doKey(MyCmd.Keycode.MULT_NEXT_AND_HANG, data[3]);
			break;
		case 0x4:
			doKey(MyCmd.Keycode.MULT_PREV_AND_RECEIVE, data[3]);
			break;
		case 0x5:
			doKey(KEY_BT, data[3]);
			break;
		case 0x6:
			doKey(AK_KEYPAD_MUTE_FAKE, data[3]);// mute
			break;
		case 0x7:
			doKey(KEY_MODE, data[3]);
			break;
		case 0xe:
			doKey(KEY_PREVIOUSSONG, data[3]);
			break;
		case 0xf:
			doKey(KEY_NEXTSONG, data[3]);
			break;
		case 0x10:
			doKey(MyCmd.Keycode.KEY_TURN_D, data[3]);
			break;
		case 0x11:
			doKey(MyCmd.Keycode.KEY_TURN_A, data[3]);
			break;
		case 0x12:
			doKey(KEY_PLAYPAUSE, data[3]);
			break;
		default:
			byte key = 0;
			for (int i = 0; i < KEYS_WHEEL.length; ++i) {
				if (KEYS_WHEEL[i][0] == data[2]) {
					key = KEYS_WHEEL[i][1];
					break;
				}
			}

			
			if (key != 0) {
				doKey(key, data[3]);
				if (data[2] == 0x60 || data[2] == 0x61 || data[2] == (byte)0xf1|| data[2] == (byte)0xf0){
					Util.doSleep(1);
					doKey(0, 0);
				} 
			} else {
				if (data[3] == 0){
					doKey(0, 0);
				}
			}
			break;
		}
	}

	byte[] airData = new byte[8];

	private byte mOutDoorTempUnit;

	public void parseACInfo(byte[] data) {
		if (data[4] >= 0x7f) {
			data[4] = (byte) 0xff;
		} else if (data[4] >= 0x1f && data[4] <= 0x3B) {
			data[4] = (byte) ((15.5f + (0.5f * (data[4] - 0x1f))) * 2);
		} else {
			data[4] = 0;
		}

		if (data[5] >= 0x7f) {
			data[5] = (byte) 0xff;
		} else if (data[5] >= 0x1f && data[5] <= 0x3B) {
			data[5] = (byte) ((15.5f + (0.5f * (data[5] - 0x1f))) * 2);
		} else {
			data[5] = 0;
		}

		airData[5] = (byte) ((data[6] & 0x40) >> 6);

		data[6] &= ~0x40;

		airData[0] = (byte) (data[2] & 0xff);
		airData[1] = (byte) (data[3] & 0xff);
		airData[2] = (byte) (data[4] & 0xff);
		airData[3] = (byte) (data[5] & 0xff);
		airData[4] &= ~0x04;
		airData[4] |= (byte) (data[6] & 0x04);

		super.parseACInfo(airData);
	}

	private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;

	public void updateOutDoorTemp(int temp) {

		if ((temp < -40) || (temp > 86)) {
			return;
		}
		if (temp == CarUtil.INVALID_OUT_DOOR_TEMP) {
			if (mTempOutDoor != CarUtil.INVALID_OUT_DOOR_TEMP) {
				temp = mTempOutDoor;
			} else {
				return;
			}
		}
		mTempOutDoor = temp;
		int t = temp;
		if (CarUtil.mTempUnit == 2) {
			mOutDoorTempUnit |= 0x40;
		} else if (CarUtil.mTempUnit == 1) {
			mOutDoorTempUnit = 0;
		}

		String unit = mContext.getResources().getString(
				R.string.temp_unic_centigrade);
		if ((mOutDoorTempUnit & 0x40) != 0){
			 unit = mContext.getResources().getString(
						R.string.temp_unic_fahrenheit);
			 t =   (t*18+320) /10;
		}
		GlobalDefinition.sendByCarServiceToSystemUI(
				mContext,
				"com.android.systemui",
				MyCmd.Cmd.SET_OUT_DOOR_TEMP,
				t
						+ unit);
	}
	
	
	private byte getRadarData(byte i) {
		byte data = 0;
		if (i > 0x0 && i <= 0x1) {
			data = 1;
		} else if (i >= 0x2 && i <= 0x3) {
			data = 2;
		} else if (i >= 0x4 && i <= 0x5) {
			data = 3;
		} else if (i >= 0x6 && i <= 0x7) {
			data = 4;
		} else if (i >= 0x8 && i <= 0x9) {
			data = 5;
		} else if (i >= 0xa && i <= 0xb) {
			data = 6;
		} else if (i >= 0xc && i <= 0xd) {
			data = 7;
		} else if (i >= 0xe && i <= 0xf) {
			data = 8;
		} else if (i >= 0x10 && i <= 0x11) {
			data = 9;
		} else if (i >= 0x12 && i <= 0x13) {
			data = 10;
		} else if (i >= 0x14 && i <= 0x15) {
			data = 11;
		} else if (i >= 0x16 && i <= 0x17) {
			data = 13;
		} else if (i >= 0x18 && i <= 0x19) {
			data = 14;
		} else if (i >= 0x1a && i <= 0x1b) {
			data = 15;
		} else if (i >= 0x1c && i <= 0x1f) {
			data = 16;
		}
		return data;
	}

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x20: {
			if (mSource != MyCmd.SOURCE_AUX){
				parseWheelKey(data, len);
			}
		}
			break;
		case 0x6B:
			airData[4] &= ~0x33;
			airData[4] |= (byte) ((data[2] & 0x03) << 4);
			airData[4] |= (byte) ((data[2] & 0x0c) >> 2);
			super.parseACInfo(airData);
			break;
		case 0x22: // Radar back
		{
			byte radar;
			boolean show = false;
			radar = getRadarData(data[2]);
			if (mRadar[0] != radar){
				mRadar[0] = radar;
				show = true;
			}
			radar = getRadarData(data[3]);
			if (mRadar[1] != radar) {
				mRadar[1] = radar;
				show = true;
			}
			radar = getRadarData(data[4]);
			if (mRadar[2] != radar) {
				mRadar[2] = radar;
				show = true;
			}
			radar = getRadarData(data[5]);
			if (mRadar[3] != radar) {
				mRadar[3] = radar;
				show = true;
			}

			// byteArrayCopy(mRadar, data, 0, 2, 4);
			if (show){
				boolean zero = Util.isZero(mRadar);
				if (!zero){
					RadarManager.start(mContext);
					checkHideRadarEx(5000);
				}
			}
			
			Handler handler = getHandler(RadarManager.TAG);
			if (show && null != handler) {				
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
			}
		}
			break;
		case 0x23: // Radar front
		{
			// byteArrayCopy(mRadar, data, 4, 2, 4);
			byte radar;
			boolean show = false;
			radar = getRadarData(data[2]);
			if (mRadar[4] != radar){
				mRadar[4] = radar;
				show = true;
			}
			radar = getRadarData(data[3]);
			if (mRadar[5] != radar) {
				mRadar[5] = radar;
				show = true;
			}
			radar = getRadarData(data[4]);
			if (mRadar[6] != radar) {
				mRadar[6] = radar;
				show = true;
			}
			radar = getRadarData(data[5]);
			if (mRadar[7] != radar) {
				mRadar[7] = radar;
				show = true;
			}

	
			if (show) {
				boolean zero = Util.isZero(mRadar);
				if (!zero) {
					RadarManager.start(mContext);
					checkHideRadarEx(5000);
				}
			}
			Handler handler = getHandler(RadarManager.TAG);
			if (show && null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
			}
		}
			break;
		case 0x25: // Radar status
		{

			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {
				if ((data[2] & 0x8) == 0) {
					handler.sendMessage(handler.obtainMessage(
							CANBOX_RADAR_BACK, 1, 0));
					for (int i = 0; i < 4; ++i) {
						mRadar[i] = 0;
					}
				}
				if ((data[2] & 0x4) == 0) {
					for (int i = 0; i < 4; ++i) {
						mRadar[4 + i] = 0;
					}

					mRadarFontEx[0] = 0;
					mRadarFontEx[1] = 0;
					handler.sendMessage(handler.obtainMessage(
							CANBOX_RADAR_FRONT, 1, 0));
				}

			}
			if ((data[2] & 0xc) != 0) {
				boolean zero = Util.isZero(mRadar);
				if (!zero){
					RadarManager.start(mContext);
					checkHideRadarEx(5000);
				}
			} else {
				RadarManager.stop();
			}
		}
			break;
	default:
		super.parseCanboxData(data, len);

		}
		

	}



	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}



	public void setMediaSrc(int source, byte type, byte[] b) {

	}
	private int mSource = MyCmd.SOURCE_NONE;
	public void setMediaSrc(int source) {
		mSource = source;
	}
}
