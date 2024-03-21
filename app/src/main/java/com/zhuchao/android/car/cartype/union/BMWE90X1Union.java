package com.zhuchao.android.car.cartype.union;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.zhuchao.android.car.R;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;


public class BMWE90X1Union extends Canbox {

	public BMWE90X1Union() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });
		
		
	}

	public void startConnect() {// default is simple box

		super.startConnect();
		Util.doSleep(10);
		byte[] data = new byte[] { (byte) 0xf1, 0x1,  0x71  };
		sendDataToCanbox(data, data.length);
	}
	
	private final static byte[][] KEYS_WHEEL = {
			{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D },
			{ 0x5, KEY_NEXTSONG },
			{ 0x6, KEY_PREVIOUSSONG },
			{ 0x8, KEY_BT }, 
			{ 0x7, KEY_MUTE },
			{ 0x3, KEY_SOURCE },
			{ 0x4, KEY_HOME },

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
			doKey(key, data[3]);
		}else {
			if (data[3] == 0){
				doKey(0, 0);
			}
		}
	}

	private void parseACInfo(byte[] data, int len) {
		
		if (data[4] >= 0x1f) {
			data[4] = (byte) 0xff;
		} else if (data[4] > 0) {
			if ((data[6] & 0x1) == 0) {
				data[4] = (byte) ((15.5f + (0.5f * data[4])) * 2);
			} else {
				data[4] = (byte) ((59 + (data[4] & 0xff)));
			}
		}
		if (data[5] >= 0x1f) {
			data[5] = (byte) 0xff;
		} else if (data[5] > 0) {
			if ((data[6] & 0x1) == 0) {
				data[5] = (byte) ((15.5f + (0.5f * data[5])) * 2);
			} else {
				data[5] = (byte) ((59 + (data[5] & 0xff)));
			}
		}
		byte[] airData = new byte[8];

		airData[0] = (byte) (data[2] & 0xf);
		airData[0] |= (byte) (((data[6] & 0x40) >> 6));

		airData[1] = (byte) (data[3] & 0xff);
		airData[2] = (byte) (data[4] & 0xff);
		airData[3] = (byte) (data[5] & 0xff);

		airData[4] = (byte) (data[7] & 0xff);
		airData[4] |= (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x8) >> 1) | ((data[2] & 0x1) << 3));

		airData[5] = (byte) ((data[6] & 0x1));

		airData[7] = (byte) (((data[6] & 0x10) >> 4));

		int msg = CANBOX_HIDE_AIR;
		if ((data[3] & 0x10) != 0) {
			msg = CANBOX_RETURN_AIR;
		}

		Handler handler = getHandler("CanService");
		if (null != handler) {
			handler.sendMessage(handler.obtainMessage(msg, airData));
		}
	}

	private byte getRadarData(byte i) {
		
		
		byte data = 0;
		if (i > 0 && i <= 0xa) {
			data = (byte) (0xb - i);
		}

		return data;
	}

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x06: {
			parseWheelKey(data);
		}
			break;
		case 0x1c: // Radar 
		{

			mRadar[0] = getRadarData(data[2]);
			mRadar[1] = getRadarData(data[3]);
			mRadar[2] = getRadarData(data[4]);
			mRadar[3] = getRadarData(data[5]);
			mRadar[4] = getRadarData(data[6]);
			mRadar[5] = getRadarData(data[7]);
			mRadar[6] = getRadarData(data[8]);
			mRadar[7] = getRadarData(data[9]);

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

		case 0x08: {
				int door = (data[2] & 0xf8);
				door = (((door & 0x80) >> 7) | ((door & 0x40) >> 5)
						| ((door & 0x20) >> 3) | ((door & 0x10) >> 1)
						| ((door & 0x08) << 1));

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
		case 0x71: {
			// byte[] version = new byte[9];
			// Util.byteArrayCopy(version, data, 0, 2, version.length);
			String date = ((data[4] & 0xf0) >> 4) + String.valueOf((data[4] & 0xf) >> 0)
					+ "-" + ((data[5] & 0xf0) >> 4) + ((data[5] & 0xf) >> 0)
					+ "-" + ((data[6] & 0xf0) >> 4) + ((data[6] & 0xf) >> 0);
			mVersion = data[2] + " " + data[3] + " "+ date + "v" + data[8]+ data[9] + data[10];
//			Log.d("ff", ""+mVersion);
			break;
		}
		case 0x24: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				int angle = data[2]&0xff;
				if (angle >= 0 && angle <= 0x32) {
					angle = ((angle * 300) / 50);
				} else if (angle >= 0x80 && angle <= 0xb2) {
					angle = -(((angle - 0x80) * 300) / 50);
				} 
				
				if(angle > -5 && angle < 5){
					angle = 5;
				}
				// Log.e("1", ""+(data[2] & 0xff));
				// Log.e("2", ""+(data[3] & 0xff));
				// Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle,
						10));
			}
		}
			break;
		case 0x03: {

			short t = (short)((data[3] & 0xff) | ((data[2] & 0xff) << 8));//bu ma
			mTempOutDoor = t;
			updateOutDoorTemp(mTempOutDoor);
		}
			sendCanboxInfo("com.canboxsetting", data);
			break;
		case 0x4:
			if(mTempUnit!=data[5]){
				mTempUnit = data[5];
				if(mTempOutDoor!=CarUtil.INVALID_OUT_DOOR_TEMP){
					updateOutDoorTemp(mTempOutDoor);
				}
			}
			sendCanboxInfo("com.canboxsetting", data);
			break;
		case 0x7:
			sendCanboxInfo("com.canboxsetting", data);
			break;
		}
		
//		if (data[0] == 0x40 | data[0] == 0x50 | data[0] == 0x63
//				| data[0] == 0x21 | data[0] == 0x25) {
//			sendCanboxInfo("com.canboxsetting", data);
//		}
	}
	private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
	@SuppressLint("DefaultLocale")
	public void updateOutDoorTemp(int temp){
		if (temp == CarUtil.INVALID_OUT_DOOR_TEMP) {
			if (mTempOutDoor != CarUtil.INVALID_OUT_DOOR_TEMP) {
				temp = mTempOutDoor;
			} else {
				return;
			}
		}
		
		if (CarUtil.mTempUnit == 2) {
			temp = (int) ((temp) * 1.8f + 32);
			mTempUnit = 2;
		} else if (CarUtil.mTempUnit == 1) {
			mTempUnit = 0;
		}
		
		String s = "";
//		if (temp >= -58 && temp <= 171) {
			if (mTempUnit == 0) {
				temp=(temp*10)/2;
				s = String.format(
						"%d.%d%s",
						temp / 10,
						(temp % 10)>=0?(temp % 10):-(temp % 10),
						mContext.getResources().getString(
								R.string.temp_unic_centigrade));

			} else {
				s = String.format(
						"%d%s",
						temp ,
						mContext.getResources().getString(
								R.string.temp_unic_fahrenheit));
			}

			if (s.length() > 1) {
				GlobalDefinition.sendByCarServiceToSystemUI(mContext,
						"com.android.systemui",
						MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
			}
//		}
	}
	private byte mTempUnit = 0;

	private int mDoorStatus = 0;

	public void setReverseRadaVol(byte param) {
		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x0, param };
		sendDataToCanbox(data, data.length);
	}

	public void setParkCarMode(byte param) {


	}

	public void requestInfo(byte param) {
		
	}

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		
	}

	public void setMediaSrc(int source) {// default is simple box
		
	}

	public void setVolume(int volume) {

		
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

	public void setPhone(int status, String num) {// default is simple box
		

	}
	

	public int getUpdateTime() {
		return 60000;
	}

	public void updateTime() {

		Calendar c = Calendar.getInstance();

		byte h = (byte) c.get(Calendar.HOUR_OF_DAY);
		h = fixTimeHour(h);

		byte m = (byte) c.get(Calendar.MINUTE);
//		byte s = (byte) c.get(Calendar.SECOND);

		byte y = (byte) (c.get(Calendar.YEAR) - 2000);
		byte mon = (byte) (c.get(Calendar.MONTH) + 1);
		byte d = (byte) c.get(Calendar.DAY_OF_MONTH);

		byte[] buf = new byte[] { (byte) 0x83, 0x05,  h,m,  d, mon, y };

		sendDataToCanbox(buf, buf.length);

	}
	
}
