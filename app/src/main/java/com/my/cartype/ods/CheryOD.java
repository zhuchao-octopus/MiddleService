package com.my.cartype.ods;

import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.GlobalDef;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.out.R;

public class CheryOD extends Canbox {

	public CheryOD() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });
	}

	private final static byte KEYS_WHEEL[][] = { 
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, 
			{ 0x3, KEY_NEXTSONG },
			{ 0x4, KEY_PREVIOUSSONG },

			{ 0x5, KEY_BT_DIAL }, 
			{ 0x6, KEY_BT_HANG },

			{ 0x7, KEY_MODE },
			{ 0x8, KEY_MIC },

			{ 0x10, MyCmd.Keycode.KEY_FM }, 
			{ 0x11, MyCmd.Keycode.KEY_AM }, 
			{ 0x12, KEY_POWER },
			{ 0x14, KEY_NEXTSONG },
			{ 0x13, KEY_PREVIOUSSONG },
			{ 0x15, KEY_MUTE },
			{ 0x16, MyCmd.Keycode.KEY_RADIO_SCAN },
			

			{ 0x17, KEY_MODE },
			{ 0x18, MyCmd.Keycode.KEY_RADIO_PS },
			{ 0x19, MyCmd.Keycode.SETUP },
		{ 0x20, AK_KEYPAD_VOLUME_A },
		{ 0x21, AK_KEYPAD_VOLUME_D }, 
			

		{ 0x22, MyCmd.Keycode.EQ },
		{ 0x23, MyCmd.Keycode.MENU },
		{ 0x24, MyCmd.Keycode.BT },
			{ 0x25, MyCmd.Keycode.RADIO },
			{ 0x26, MyCmd.Keycode.HOME },
			{ 0x27, MyCmd.Keycode.BT },
			{ 0x28, MyCmd.Keycode.NAVIGATION },

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
		
		if (data[2] == 0x20 || data[2] == 0x21) {
			if (key != 0) {
//				if (data[3] == 1) {
					doKey(key, 1);
					doKey(0, 0);
//				}
			}
		} else {

			if (key != 0) {
				doKey(key, data[3]);
			} else {
				if (data[3] == 0) {
					doKey(0, 0);
				}
			}
		}
	}

	private void parseACInfo(byte[] data, int len) {

		byte[] airData = new byte[8];

		if ((data[5] & 0xff) == 30) {
			data[5] = (byte) 0xff;
		} else if (data[5] != 0) {
			data[5] = (byte)(36 + data[5] - 1);
		}

		if ((data[6] & 0xff) == 30) {
			data[6] = (byte) 0xff;
		} else if (data[6] != 0) {
			data[6] = (byte)(36 + data[6] - 1);
		}
		

		airData[0] = (byte) (data[2] & 0x6c);
		airData[0] |= (byte) (((data[2] & 0x10) << 1) | ((data[2] & 0x2) >> 1) | ((data[2] & 0x1) << 1));

		airData[1] = 0x10;
		switch(data[3]){
		case 1:
			airData[1] |= 0x40;
			break;
		case 2:
			airData[1] |= 0x40;
			airData[1] |= 0x20;
			break;
		case 3:
			airData[1] |= 0x20;
			break;
		case 4:
			airData[1] |= 0x80;	
			airData[1] |= 0x20;
			break;
		case 5:		
			airData[1] |= 0x80;	
			break;		
		default:
			airData[1] = 0;
			break;
			
		}

		airData[1] |= (data[4] & 0xf);
		if (data[4] == 0) {
			airData[1] = 0;
		}
		

		airData[2] = (byte) (data[5] & 0xff);
		airData[3] = (byte) (data[6] & 0xff);

		// airData[7] = (byte) ((data[6] & 0x80) >> 2);
		
		Handler handler = getHandler("CanService");
		if (null != handler) {
			int msg = CANBOX_HIDE_AIR;
			if (/* (data[2] & 0x80) != 0 && */((airData[1] & 0x10) != 0)) {
				msg = CANBOX_RETURN_AIR;
			}
			handler.sendMessage(handler.obtainMessage(msg, airData));
		}
	}


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

		case 0x24: {
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

		case 0x29: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				int angle = ((data[2] & 0xff) | ((data[3] & 0xff) << 8));

//				if ((data[3] & 0x80) == 0) {
//					angle = -angle;
//				}
				angle = angle-0x8000;

				angle = ((angle * 3000) / 0x2200);

//Log.d("eec", ":"+angle);

				if (angle > -50 && angle < 50) {
					angle = 50;
				}
				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle, 100));
			}
		}
			break;
		case 0x10:

			int temp = (int) (((data[3] & 0xff)) * 5 - 550);
			// int temp = t*5 - 550;
			mUnit = (data[2] & 0x80 >> 7);

			if (mUnit == 1 && CarUtil.mTempUnit != 2) {
//				float t = temp / 10.0f;
//				temp = (int) (((t) * 1.8f + 32) * 10);
			}

			updateOutDoorTemp(temp);
			break;
		case 0x40:
			sendCanboxInfo("com.canboxsetting", data);
			break;
		case (byte)0x30:{
			byte[] version = new byte[data.length-2];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		
		}

		// if (data[0] == 0x40) {
		// sendCanboxInfo("com.canboxsetting", data);
		// }
	}

	private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
	private int mUnit = 0;
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
			if (mUnit != 1){
				float t = temp/10.0f;
				temp = (int)( ((t) * 1.8f + 32)*10);
			}
			mUnit = 1;
		} else if (CarUtil.mTempUnit == 1) {
			if (mUnit == 1){
				temp = (int)((((float)temp/10.0f)-32)/1.8f)*10;
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
			GlobalDef.sendByCarServiceToSystemUI(mContext,
					"com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
		}
		
	}
	
	private int mDoorStatus = 0;

	byte[] data;

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

//		byte h = (byte) ((time / 3600));
//		byte min = (byte) ((time / 60) % 60);
//		byte sec = (byte) ((time) % 60);
//		// ++play;
//
//		byte s = 0;
//		switch (source) {
//		case MyCmd.SOURCE_DVD:
//			s = 0x21;
//			break;
//		case MyCmd.SOURCE_MUSIC:
//		case MyCmd.SOURCE_VIDEO:
//			++play;
//			s = 0x10;
//			break;
//		case MyCmd.SOURCE_BT:
//			s = 0x40;
//			break;
//		default:
//			s = 0x07;
//			break;
//		}
//
//		data = new byte[] { (byte) 0xc0, 0x8, s,
//				(byte) ((total & 0xFF00) >> 8), (byte) (total & 0xFF),
//				(byte) ((play & 0xFF00) >> 8), (byte) ((play) & 0xFF), h, min,
//				sec };
//
//
//		sendDataToCanbox(data, data.length);
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
//		setMediaSrc(0);
//		if (b[0] != 0x10) {
//			b[0] += 1;
//		}

		mHandler.removeMessages(0);
		b[3]++;
	
		data = new byte[] { (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2],  b[3], 0,  0 };
		sendDataToCanbox(data, data.length);
	}

	private int mSource = MyCmd.SOURCE_NONE;

	public void setMediaSrc(int source) {

		mHandler.removeMessages(0);
		byte s = 0;
		byte format = 0;
		switch (source) {
		case MyCmd.SOURCE_RADIO:
			s = 1;
			format = 1;
			break;
		case MyCmd.SOURCE_DVD:
			s = 0x2;
//			format = 0x21;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x9;
//			format = 0x10;
			break;
		case MyCmd.SOURCE_BT:
			s = 0xb;
//			format = 0x40;
			break;
		case MyCmd.SOURCE_AUX:
			s = 0x7;
			format = 0x30;
			break;
		}

		if (mSource != source) {
			mSource = source;
			if (source == MyCmd.SOURCE_BT) {

				data = new byte[] { (byte) 0xc0, 0x8, s, 0, 0, 0, 0,
						(byte) 0xff, (byte) 0xff, (byte) 0xff };
			} else {

				data = new byte[] { (byte) 0xc0, 0x8, s, format, 0, 0, 0, 0, 0, 0 };
			}
			sendDataToCanbox(data, data.length);
		}
	}
	
	public void setVolume(int volume) {
		if (volume == 0) {
//			volume |= 0x80;
		}
		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
		sendDataToCanbox(data, data.length);
		mHandler.removeMessages(0);
		mHandler.sendEmptyMessageDelayed(0, 4000);
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (data != null){
				sendDataToCanbox(data, data.length);
			}
			super.handleMessage(msg);
		}
	};
	
//	public void udpateLang() {
//		int lang = 1;
//		String locale = Locale.getDefault().getLanguage();
//		if (locale != null) {
//			if (locale.equals("en")) {
//				lang = 1;
//			} else if (locale.equals("zh")) {
//				lang = 0;
//			}
//		}
//		if (lang != -1) {
//			byte[] buf = { (byte) 0xc6, 0x2, 0x0, (byte) lang };
//			sendDataToCanbox(buf, buf.length);
//		}
//	}
	
	public void setContext(Context c) {
		super.setContext(c);
		udpateLang();
	}
}
