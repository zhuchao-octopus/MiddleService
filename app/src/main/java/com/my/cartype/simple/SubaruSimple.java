package com.my.cartype.simple;

import java.util.Date;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.common.util.UtilSystem;
import com.my.GlobalDef;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.manager.McuManager;
import com.my.out.R;

public class SubaruSimple extends Canbox {

	public SubaruSimple() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });

		updateCanboxKeySettings();
	}

	private final static byte KEYS_WHEEL[][] = { { 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, { 0x3, KEY_NEXTSONG },
			{ 0x4, KEY_PREVIOUSSONG }, { 0x7, KEY_SOURCE },
			{ 0x8, KEY_MIC }, { 0x9, KEY_BT_DIAL }, { 0xA, KEY_BT_HANG },
			{ 0x15, KEY_BACK },{ 0x16, KEY_PLAYPAUSE },

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
		} else {
			if (data[3] == 0){
				doKey(0, 0);
			}
		}
	}

	private final static byte KEYS_WHEEL2[][] = { { 0x2, KEY_NEXTSONG },
			{ 0x1, KEY_PREVIOUSSONG }, { 0x3, MyCmd.Keycode.FAST_F },
			{ 0x4, MyCmd.Keycode.FAST_R }, { 0x11, MyCmd.Keycode.BT_DIAL },
			{ 0x12, MyCmd.Keycode.BT_HANG }, { 0x14, KEY_HOME },
			{ 0x17, KEY_MIC }, { 0x19, MyCmd.Keycode.KEY_BT_VOICE_SPEAKER },
			{ 0x18, MyCmd.Keycode.KEY_BT_VOICE_PHONE }, { 0x30, KEY_BACK },

	};

	private void parseWheelKey2(byte[] data) {
		byte key = 0;
		for (int i = 0; i < KEYS_WHEEL2.length; ++i) {
			if (KEYS_WHEEL2[i][0] == data[2]) {
				key = KEYS_WHEEL2[i][1];
				break;
			}
		}

		if (key != 0) {
			doKey(key, 1);
			doKey(key, 0);
		}
	}

	private void parseACInfo(byte[] data, int len) {

		// if (data[4] >= 0x1f) {
		// data[4] = (byte) 0xff;
		// } else if (data[4] > 0) {
		// if ((data[6] & 0x1) == 0) {
		// data[4] = (byte) (((0.5f * data[4])));
		// } else {
		// data[4] = (byte) ((59 + (data[4] & 0xff)));
		// }
		// }
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

		airData[0] = (byte) (data[2] & 0xff);
//		airData[0] |= (byte) (((data[6] & 0x40) >> 6));

		airData[1] = (byte) (data[3] & 0xff);
		airData[2] = (byte) (data[4] & 0xff);
		airData[3] = (byte) (data[5] & 0xff);

//		airData[4] = (byte) (data[7] & 0xff);
		airData[4] |= (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x8) >> 1) | ((data[2] & 0x1) << 3));

		airData[5] = (byte) ((data[6] & 0x1));

		airData[7] = (byte) (((data[6] & 0x10) >> 4));
		airData[7] |= (byte) (((data[6] & 0x80) >> 2));

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
			data = 4;
			break;
		case 4:
			data = 1;
			break;
		}
		return data;
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
		case 0x22: // Radar back
		{

			mRadar[0] = getRadarData(data[2]);
			mRadar[1] = getRadarData(data[3]);
			mRadar[2] = getRadarData(data[4]);
			mRadar[3] = getRadarData(data[5]);

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
		case 0x30: {
			byte[] version = new byte[16];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		case 0x24: {
//			if ((data[2] & 0x1) != 0) {
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
//			}

		}
			break;
		case 0x25:
		{
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_SUBARU_UI_DATA, data[2], 0));
			}
		}
		break;
		case 0x27: {

			int t = (short) ((data[3] & 0xff) | ((data[4] & 0xff) << 8));
			int temp = t;
			mUnit = data[2];
			updateOutDoorTemp(temp);
			
			// }
		}
			break;

		case 0x29: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				short angle = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));
				int angle2 = -(angle * 300 / 0x1300);
				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle2, 10));
			}
		}
			break;
		case 0x31: {
			if (CarUtil.getCarEQ() == 0) {
				if ((data[2] & 0x1) == 1) {
					if (!CarUtil.mIsNeedSendEQ) {
						CarUtil.mIsNeedSendEQ = true;
						CarUtil.setMcuEQZoneUsed(1);
						McuManager mcu = McuManager.getInstanse();
						if (mcu != null) {
							mcu.setAudio(0x6, 0x2);
						}
						startEQ();
					}
				} else {
					if (CarUtil.mIsNeedSendEQ) {
						CarUtil.mIsNeedSendEQ = false;
						CarUtil.setMcuEQZoneUsed(0);
						stopEQ();
					}
				}
			}
		}
			break;
		case (byte)0x95:
			if (data[2] == 0) {
				if (mMuteByGlonass) {
					McuManager mcu = McuManager.getInstanse();
					if (mcu != null && mcu.getMcuVolume() == 0) {
						mcu.setVoulumeMute(1);
					}
					mMuteByGlonass = false;
				}
			} else if (data[2] == 1) {
				McuManager mcu = McuManager.getInstanse();
				if (mcu != null && mcu.getMcuVolume() != 0) {
					mcu.setVoulumeMute(0);
				}
				mMuteByGlonass = true;
			}
			break;
		case 0x40:
			sendCanboxInfo("com.canboxsetting", data);
			break;
		}
		
	}
	private boolean mMuteByGlonass = false;
	
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
	
	private byte mRadarSwitch = 0;
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

		byte h = (byte) ((time / 3600));
		byte min = (byte) ((time / 60) % 60);
		byte sec = (byte) ((time) % 60);
		// ++play;

		byte s = 0;
		byte s2 = 0;
		switch (source) {
		case MyCmd.SOURCE_DVD:
			s = 0x2;
			s2 = 0x10;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			s = 8;
			s2 = 0x11;
			break;
		case MyCmd.SOURCE_BT:
			s = 0xb;
			s2 = 0x10;
			break;
		default:
			s = 0x07;
			s2 = 0x30;
			break;
		}

		if (MyCmd.SOURCE_DVD == source) {
			data = new byte[] { (byte) 0xc0, 0x8, s, s2, 0,
					(byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min,
					sec };

		} else {
			data = new byte[] { (byte) 0xc0, 0x8, s, s2,
					(byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), 0,
					h, min, sec };
		}

		if (mPhoneStatus < HFP_INFO_CALLED) {

			sendDataToCanbox(data, data.length);
		}
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		setMediaSrc(0);
		if (b[0] != 0x10) {
			b[0] += 1;
		}
		data = new byte[] { (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0, 0,
				0 };
		sendDataToCanbox(data, data.length);
	}


	public void sendId3(byte index, String num) {

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

			int num_len = n.length;
			if (num_len >= 80) {
				num_len = 80;
			}

			int len = num_len + 4;
			int head = 0;
			if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
				num_len -= 2;
				head = 2;
			}
			// if (len > 31) {
			// len = 31;
			// }
			if (head>0){
				len = len - head;
			}
			byte[] data = new byte[len];

			data[0] = (byte) 0xc8;
			data[1] = (byte) (len - 2);
			data[2] = 0x10;
			data[3] = index;

			byte[] n2 = new byte[num_len];

			for (int i = 0; i < num_len; ++i) {
				if (i % 2 == 0) {
					n2[i] = n[i + head + 1];
				} else {
					n2[i] = n[i + head - 1];
				}
			}

			for (int i = 0; i < n2.length; ++i) {
				Log.d("Accord2013Simple", i + ":" + n.length + ":" + num_len);
				data[4 + i] = n[i + head];
			}
			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

			Log.d("Accord2013Simple", "sendId3" + e);
		}
	}
	String mName = null;
	String mArtist = null;
	String mAlbum = null;
//	public void setPhone(int status, String num) {
//		sendId3((byte)0x1, num);
//	}
	
	public void setSongName(String s) {
		sendId3((byte)0x3, s);
		mName=s;
	}

	public void setSongAritst(String s) {
		sendId3((byte)0x5, s);
		mArtist = s;
	}

	public void setSongAlbum(String s) {
		sendId3((byte)0x4, s);
		mAlbum = s;
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
		
//		if (status != 0) {
			byte[] data2;

			if (num == null) {
				num = " ";
			}
//			
//
//			
			data2 = new byte[] { (byte) 0xc0, 0x8, 0x5, 0x40, (byte)status, 0, 0, 0, 0, 0 };			
//
//
			sendDataToCanbox(data2, data2.length);

		if (status == 0){
			if (data != null) {
				sendDataToCanbox(data, data.length);
			}
		}
		sendId3((byte)0x1, num);
	}

	public void setVolume(int volume) {

		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
		sendDataToCanbox(data, data.length);
	}

	private void checkHideRadar() {
		mHandler.removeMessages(HIDE_RADAR);
		mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
	}

	private final static int HIDE_RADAR = 0;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_RADAR:
				RadarManager.stop();
				break;
			}
			super.handleMessage(msg);
		}
	};
	//
	// public void setPhone(int status, String num) {// default is simple box
	// switch (status) {
	// case HFP_INFO_INITIAL:
	// case HFP_INFO_READY:
	// case HFP_INFO_CONNECTING:
	// case HFP_INFO_CONNECTED:
	// status = 0;
	// break;
	// case HFP_INFO_CALLED:
	// status = 2;
	// break;
	// case HFP_INFO_INCOMING:
	// status = 1;
	// break;
	// case HFP_INFO_CALLING:
	// status = 4;
	// break;
	// }
	// byte[] data = new byte[4];// {(byte)0xc5, 0x1, (byte)status};
	// data[0] = (byte) 0xc5;
	// data[1] = (byte) (2);
	// data[2] = (byte) 0;
	// data[3] = (byte) status;
	//
	// sendDataToCanbox(data, data.length);
	//
	// if (num == null) {
	// num = " ";
	// }
	// byte[] n = num.getBytes();
	// data = new byte[n.length + 4];// {(byte)0xc5, 0x1, (byte)status};
	// data[0] = (byte) 0xcA;
	// data[1] = (byte) (n.length + 2);
	// data[2] = (byte) 1;
	// data[3] = (byte) 3;
	// byteArrayCopy(data, n, 4, 0, n.length);
	// Util.doSleep(100);
	// sendDataToCanbox(data, data.length);
	//
	// }
	

	public void updateCanboxKeySettings() {
//		if (CarUtil.getCarType2() == 0) {
//			mKeyPannel = KEYS_PANNEL_S_HIDE;
//		} else {
//			mKeyPannel = KEYS_PANNEL_NORMAL;
//		}

		if (CarUtil.getCarEQ() == 1) {
			CarUtil.mIsNeedSendEQ = true;
			CarUtil.setMcuEQZoneUsed(1);
		}
	}
	
	private void startEQ(){
		if (!CarUtil.mIsNeedSendEQ){
			stopEQ();
			return;
		} 

		powerEQ(true); //power on
		mVolume = MachineConfig.getIntProperty2( SystemConfig.CANBOX_EQ_VOLUME);
		if (mVolume == -1){
			if (CarUtil.getCarType2() == 0){
				mVolume = 45;
			} else {
				mVolume = 30;
			}
		}
		setEQVolume(mVolume);
//		mHandler.removeMessages(REPEAT_SEND_EQ);
//		mHandler.sendEmptyMessageDelayed(REPEAT_SEND_EQ, 300);
	}
	
	private void stopEQ(){
//		mHandler.removeMessages(REPEAT_SEND_EQ);
		powerEQ(false); 
	}
	public void startConnect() {
		super.startConnect();
		startEQ();		
	}
	private int mVolume = 30;
	public void stopConnect() {		
		stopEQ();
		super.stopConnect();
	}

	private void powerEQ(boolean power) {
		byte[] data = new byte[] { (byte) 0x84, 0x2, 0x1,
				(byte) (power ? 1 : 0) };
		sendDataToCanbox(data, data.length);
	}
	byte[] mEqData = new byte[6];
	
	private void sendEQ(byte cmd, byte param) {
		byte[] data = new byte[] { (byte) 0x84, 0x2, cmd, param };
		sendDataToCanbox(data, data.length);
	}
	public void sendEqToCanbox(byte[] eq) {
		if (eq != null && eq.length >= 11) {

			byte[] eq2 = new byte[6];

			eq2[0] = eq[0];
			eq2[1] = eq[1];

			eq2[3] = (byte) (eq[2] + eq[3] + eq[4]);
			eq2[4] = (byte) (eq[5] + eq[6] + eq[7]);
			eq2[5] = (byte) (eq[8] + eq[9] + eq[10]);


			byte param;
			
			if (CarUtil.getCarType2() == 0) {
				eq2[0] += 3;
				eq2[1] += 3;

				eq2[3] = (byte)(((eq2[3] *10)/60) + 5);
				eq2[4] = (byte)(((eq2[4] *10)/60) + 5);
				eq2[5] = (byte)(((eq2[5] *10)/60) + 5);
				
			} else {
				if (eq2[0] == 7){
					eq2[0] = 0xa;
				} else {
					eq2[0] = (byte)(((eq2[0] *19)/15) + 1);
				}
				if (eq2[1] == 7){
					eq2[1] = 0xa;
				} else {
					eq2[1] = (byte)(((eq2[1] *19)/15) + 1);
				}
				

				eq2[3] = (byte)(((eq2[3] *19)/60) + 1);
				eq2[4] = (byte)(((eq2[4] *19)/60) + 1);
				eq2[5] = (byte)(((eq2[5] *19)/60) + 1);
			}
			
			if (eq2[0] != mEqData[0]) {
				param = (byte) ((eq2[0]));
				sendEQ((byte) 4, param);
				Util.doSleep(5);
			}
			if (eq2[1] != mEqData[1]) {
				param = (byte) ((eq2[1]));
				sendEQ((byte) 3, param);
				Util.doSleep(5);
			}

			if (eq2[3] != mEqData[3]) {
				param = (byte) ((eq2[3]));
				sendEQ((byte) 5, param);
				Util.doSleep(5);
			}
			if (eq2[4] != mEqData[4]) {
				param = (byte) ((eq2[4]));
				sendEQ((byte) 7, param);
				Util.doSleep(5);
			}
			if (eq2[5] != mEqData[5]) {
				param = (byte) ((eq2[5]));
				sendEQ((byte) 6, param);
				Util.doSleep(5);
			}

			mEqData = eq2;
		}
	}

	private void setEQVolume(int volume) {

		byte[] data = new byte[] { (byte) 0x84, 0x2, 0x2, (byte) volume };
		sendDataToCanbox(data, data.length);
	}
	
	public int getUpdateTime() {
		return 1000;
	}

	public void updateTime() {
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);
		
		byte ampm = 0;
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		if ("12".equals(strTimeFormat)) {
			if (h>=12){
				ampm = (byte)0x80;
			}
		}
		h |= ampm;

		byte m = (byte) curDate.getMinutes();
		byte s = (byte) curDate.getSeconds();

		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();
		byte[] buf = new byte[] { (byte) 0xa6, 0x07, y, mon, d, h, m, s, 1 };

		sendDataToCanbox(buf, buf.length);
	}
}
