package com.zhuchao.android.car.cartype.union;

import com.zhuchao.android.car.GlobalDefinition;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.R;
public class PetgeoScreenUnion extends Canbox {

	public PetgeoScreenUnion() {
//		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x1, 0x3,
//				0x0, 0x0 });
//		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x1, 0x1,
//				0x2, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });
		if (CarUtil.getCarEQ() == 1) {
			CarUtil.mIsNeedSendEQ = true;
			CarUtil.setMcuEQZoneUsed(1);
		}
		
	}

	public void startConnect() {// default is simple box
//		super.startConnect();
//		Util.doSleep(10);
//		byte[] data = new byte[] { (byte) 0x90, 0x4,  0x71 , 0,0,0 };
//		sendDataToCanbox(data, data.length);
	}
	
	private final static byte[][] KEYS_WHEEL = {


		{ (byte) 0x1, KEY_HOME },
		{ (byte) 0x3, KEY_PREVIOUSSONG },
		{ (byte) 0x4, KEY_NEXTSONG },
		
		
		{ 0x7, MyCmd.Keycode.PLAY_PAUSE }, 
		{ 0x8, KEY_BACK },
		{ 0x10, KEY_MUTE },
		{ 0x11, KEY_SOURCE },
		{ 0x12, KEY_SEEK_NEXT },
		{ 0x13, KEY_SEEK_PREV }, 
		{ 0x14, AK_KEYPAD_VOLUME_A },
		{ 0x15, AK_KEYPAD_VOLUME_D },		

		{ 0x16, KEY_MUTE },
		{ 0x17, KEY_PREVIOUSSONG },
		{ 0x18, KEY_NEXTSONG },
		{ 0x20, MyCmd.Keycode.KEY_CAR_INFO },
	
		 
		
		{ 0x30, MyCmd.Keycode.BT_DIAL },
		{ 0x31, MyCmd.Keycode.BT_HANG },
		
		{  (byte)0xc1, MyCmd.Keycode.BT_DIAL },
		{  (byte)0xc0, MyCmd.Keycode.BT_HANG },
		
		{ 0x50, KEY_GPS },
		{ 0x40, KEY_SET },
		

	};

	int mKey;

	private void parseWheelKey(byte[] data, int len) {
		if (doKeyStudy(data[2], (data[2] == 0) ? 0 : 1)) {
			return;
		}	
		int key = 0;
		for (int i = 0; i < KEYS_WHEEL.length; ++i) {
			if (KEYS_WHEEL[i][0] == data[2]) {
				key = KEYS_WHEEL[i][1];
				break;
			}
		}

		if (key != 0) {
			doKey(key, 1);
		} else {
			doKey(0, 0);
		}

	}

	private byte getRadarData(byte i) {
		byte data = 0;
		switch(i){
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
			data = 4;
			break;
		case 4:
			data = 1;
			break;
		}
		return data;
	}

	int mRadarSwitch;
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
//		--len;
//		byte[] data = new byte[len];
//		byteArrayCopy(d, data, 0, 1, len);
		
		switch (data[0]) {
		case 0x2: {
			parseWheelKey(data, len);
		}
			break;
			
		case 0x32: 
		{
			// byteArrayCopy(mRadar, data, 4, 2, 4);

			if (data[2] == 0x2) {
				mRadar[0] = getRadarData(data[3]);
				mRadar[1] = getRadarData(data[4]);
				mRadar[2] = getRadarData(data[4]);
				mRadar[3] = getRadarData(data[5]);

				if (data[1] >= 7) {
					mRadar[4] = getRadarData(data[6]);
					mRadar[5] = getRadarData(data[7]);
					mRadar[6] = getRadarData(data[7]);
					mRadar[7] = getRadarData(data[8]);
				}
				
				boolean zero = Util.isZero(mRadar);
				if (!zero){
					RadarManager.start(mContext);
					checkHideRadarEx(2000);
				}
				Handler handler = getHandler(RadarManager.TAG);
				if (null != handler) {
					checkHideRadar();
					handler.sendMessage(handler.obtainMessage(
							CANBOX_RADAR_BACK, 0, 3));
				}
			}
			
		}
			break;
		case 0x38: {
			int door = (data[2] & 0xf8);
			door = (((door & 0x40) >> 5) | ((door & 0x80) >> 7)
					| ((door & 0x10) >> 1) | ((door & 0x20) >> 3)
					| ((door & 0x08) << 1) | ((door & 0x04) << 3));

			if (mDoorStatus != door) {
				mDoorStatus = door;
				Handler handler = getHandler("CanService");
				if (null != handler) {
					handler.sendMessage(handler.obtainMessage(
							CANBOX_DOOR_STATUS, mDoorStatus, 0));

				}
			}

//			sendCanboxInfo("com.canboxsetting", data);
		}

			break;
		case 0x36: {

			int temp = (data[2] & 0x7f);
			if((data[2] & 0x80)!=0){
				temp = -temp;
			}

			temp-=40;
			String s = String.format(
					"%d%s",
					temp,
					mContext.getResources().getString(
							R.string.temp_unic_centigrade));
			GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui",
					MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);

		}
			break;
	

		case 0x29: {
			
			
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu
																				// ma

				int angle = ((a * 3000) / 5450);

				if (angle > -50 && angle < 50) {
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
		case 0x33:
		case 0x34:
		case 0x35:
		case 0x3B:

			sendCanboxInfo("com.canboxsetting", data);
			break;
			
		case 0x7f: {
			int v_len = len - 2;
			byte[] version = new byte[v_len];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		case 0x71: {
			// byte[] version = new byte[9];
			// Util.byteArrayCopy(version, data, 0, 2, version.length);
			String date = ((data[4] & 0xf0) >> 4) + String.valueOf((data[4] & 0xf) >> 0)
					+ "-" + ((data[5] & 0xf0) >> 4) + ((data[5] & 0xf) >> 0)
					+ "-" + ((data[6] & 0xf0) >> 4) + ((data[6] & 0xf) >> 0);
			mVersionEx = data[2] + " " + data[3] + " "+ date + "v" + data[8]+ data[9] + data[10];
//			Log.d("ff", ""+mVersion);
			break;
		}
		}
	}

	private void checkHideRadar() {
		mHandler.removeMessages(HIDE_RADAR);
		mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
	}

	private final static int HIDE_RADAR = 0;
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == HIDE_RADAR) {
				RadarManager.stop();
			}
			super.handleMessage(msg);
		}
	};

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

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		
		byte min = (byte) ((time / 60) % 60);
		byte sec = (byte) ((time) % 60);
		byte[] data;
		if (play > 0xff) {
			play = 0xff;
		}
		if (total > 0xff) {
			total = 0xff;
		}
		if (MyCmd.SOURCE_DVD == source) {


			data = new byte[] { 0x8, 0x9, 0x1, (byte) total, (byte) play, min, sec };
		} else {
			++play;
			data = new byte[] { 0x8, 0x9, 0xd, (byte) total, (byte) play, min, sec };
		}

		

		sendDataToCanbox(data, data.length);
		
	}

	private final int mSource = MyCmd.SOURCE_NONE;
	private final int mBaud = 0;

	public void setVolume(int volume) {
		byte[] data = new byte[] { 0x5, 0x9, 0xf, (byte)volume };
		sendDataToCanbox(data, data.length);
	}
	
	public void setMediaSrc(int source, byte type, byte[] b) {
		int freq = ((( b[2]&0xff)<<8) | (b[1]&0xff));
		
		if (b[0] == 0x10) {
			b[0]= 3;
		} 

		b[1] = (byte)((freq/100)&0xff);
		b[2] = (byte)((freq%100)&0xff);

		byte[] data = new byte[] { 0x7, 0x9, 0x2, b[0], b[1], b[2] };
		sendDataToCanbox(data, data.length);
	}

	public void setMediaSrc(int source) {
//		byte s = 0;
//		byte mediaType = 0;
//		switch (source) {
//		case 0:
//			s = 1;
//			mediaType = 1;
//			break;
//		case 1:
//			s = 2;
//			mediaType = 0x10;
//			break;
//		case MyCmd.SOURCE_IPOD:
//			s = 6;
//			mediaType = 0x12;
//			break;
//		case MyCmd.SOURCE_MUSIC:
//		case MyCmd.SOURCE_VIDEO:
//			s = 0x09;
//			mediaType = 0x11;
//			break;
//		case MyCmd.SOURCE_AUX:
//			s = 0x07;
//			mediaType = 0x30;
//			byte[] data2 = new byte[] { (byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0 };
//			sendDataToCanbox(data2, data2.length);
//			break;
//		case MyCmd.SOURCE_DTV:
//			s = 0x0A;
//			mediaType = 0x30;
//			byte[] data3 = new byte[] { (byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0 };
//			sendDataToCanbox(data3, data3.length);
//			break;
//		case MyCmd.SOURCE_BT:
//			s = 0x0b;
//			mediaType = 0x12;
//			break;
//		default:
//			s = 0x00;
//			mediaType = 0x0;
//			break;
//		}
//		byte[] data;
//		
//		
//		data = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0 };
//
//		sendDataToCanbox(data, data.length);
//		
//		mSource = source;
	}
	
	
	
	private byte Sum(byte []data, int len){
		byte sum = 0;
		
		for(int i = 0; i < len; ++i){
			sum += data[i];
		}
		return (byte)(sum&0xFF);
	}

	public void sendDataToCanbox(byte[] data, int len) { 
		byte[] send = new byte[len + 3];
		send[0] = (byte)(len+2);
		send[1] = (byte)0xfd;
		send[len+2] = Sum(data, len);
		byteArrayCopy(send, data, 2, 0, len);
		sendCommonDataToCanbox(send);
	}
	
	private byte eqTo19(byte b){
		int ret = ((b&0xff)*20/15);
		ret |= 0x80;
		return (byte)ret;
	}
	public void sendEqToCanbox(byte[] eq) {
		if (eq != null && eq.length >= 11) {
			byte[] buf = new byte[] { 0x9, 0x9, 0x13, 0, 0, 0, 0, 0 };
			
			Log.d("abcd", Util.byte2HexStr(eq));

			buf[3] = (byte) (((eq[2] + eq[3] + eq[4]) / 3)*15/21);
			buf[4] = eq[0];
			buf[5] = eq[1];
			buf[6] = (byte) (((eq[8] + eq[9] + eq[10]) / 3)*15/21);

			if (CarUtil.getCarType() == 1) {
				buf[3] = eqTo19(buf[3]);
				buf[4] = eqTo19(buf[4]);
				buf[5] = eqTo19(buf[5]);
				buf[6] = eqTo19(buf[6]);
			}

			sendDataToCanbox(buf, buf.length);

		}
	}
}
