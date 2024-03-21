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
import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;


public class FiatEGEARaise extends Canbox {

	public FiatEGEARaise() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });
	}

	private final static byte[][] KEYS_WHEEL = { { 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, { 0x3, KEY_NEXTSONG },
			{ 0x4, KEY_PREVIOUSSONG }, { 0x5, KEY_BT },

			{ 0x6, KEY_MUTE }, { 0x7, KEY_SOURCE },
			{ 0x8, MyCmd.Keycode.PLAY_PAUSE }, { 0x9, KEY_BT_DIAL },
			{ 0xA, KEY_BT_HANG }, { 0x10, KEY_MIC }, { 0x11, KEY_FM },
			{ 0x12, KEY_MIC },

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
			doKey(key, data[3]);
		} else {
			if (data[3] == 0) {
				doKey(0, 0);
			}
		}
	}

	private final static byte[][] KEYS_WHEEL2 = { { 0x2, KEY_NEXTSONG },
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

		if (data[4] >= 0x1f) {
			data[4] = (byte) 0xff;
		} else if (data[4] > 0) {
			data[4] = (byte) ((17.5 + ((0.5f * data[4]))) * 2);
		}

		if (data[5] >= 0x1f) {
			data[5] = (byte) 0xff;
		} else if (data[5] > 0) {
			data[5] = (byte) ((17.5 + ((0.5f * data[5]))) * 2);
		}

		byte[] airData = new byte[8];

		airData[0] = (byte) (data[2] & 0xef);
		airData[0] |= (byte) (((data[6] & 0x40) >> 6));

		airData[1] = (byte) (data[3] & 0xff);
		airData[2] = (byte) (data[4] & 0xff);
		airData[3] = (byte) (data[5] & 0xff);

		airData[4] = (byte) (data[6] & 0xff);
		// airData[4] |= (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x8) >>
		// 1) | ((data[2] & 0x1) << 3));

		// airData[5] = (byte) ((data[6] & 0x1));

		airData[7] = (byte) (((data[6] & 0x10) >> 4));
		airData[7] |= (byte) (((data[6] & 0x80) >> 2));

		int msg = CANBOX_HIDE_AIR;
		if ((data[3] & 0x10) != 0 || (airData[0] & 0x80) != 0) {
			msg = CANBOX_RETURN_AIR;
		}

		Handler handler = getHandler("CanService");
		if (null != handler) {
			handler.sendMessage(handler.obtainMessage(msg, airData));
		}
	}

	private byte getRadarData(byte i) {

		return i;
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
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
			}
		}
			break;
		case 0x25: {
			if ((data[2] & 0x08) == 0) {
				mHandler.removeMessages(HIDE_RADAR);
				RadarManager.stop();
			}
		}
			break;
		case 0x30: {
			byte[] version = new byte[10];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		case 0x24: {
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
		}

			break;

		case 0x28: {

			int t = data[2] & 0x7f;
			if ((data[2] & 0x80) != 0) {
				t = -t;
			}
			updateOutDoorTemp(t * 10);

			// }
		}
			break;
		case 0x15: {

			int t = (data[2] & 0xff);
			int temp = -40 + t;
			updateOutDoorTemp(temp * 10);

			// }
		}
			break;

		case 0x29: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				int angle = ((data[3] & 0xff) | ((data[2]) << 8));
				angle = -(angle * 300 / 540);
				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle, 10));
			}
		}
			break;
		case 0x40:
		case 0x50:
			sendCanboxInfo("com.canboxsetting", data);
			break;
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

		byte s = 0;
		switch (source) {
		case MyCmd.SOURCE_DVD:
			s = 0x3;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			s = 4;
			break;
		default:
			s = 0x07;
			break;
		}

		if (mPhoneStatus < HFP_INFO_CALLED) {
			data = new byte[] { (byte) 0xc0, 0x6, s,
					(byte) ((total & 0xff00) >> 8),
					(byte) ((total & 0xff) >> 0),
					(byte) ((play & 0xff00) >> 8), (byte) ((play & 0xff) >> 0),
					1 };
			sendDataToCanbox(data, data.length);
		}
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		byte s;
		if (b[0] >= 0x10) {
			b[0] -= 0x10;
			s = 2;
		} else {
			b[0] = 1;
			s = 1;
		}
		// int freq = (b[1]&0xff) | ((b[2]&0xff)<<8)
		data = new byte[] { (byte) 0xc0, 0x6, s, b[2], b[1], (byte) (b[3] + 1),
				b[0], 0 };
		sendDataToCanbox(data, data.length);
	}

	public void setMediaSrc(int source) {// default is simple box
		byte s;
		switch (source) {
		case MyCmd.SOURCE_AUX:
			s = 7;
			break;
		case MyCmd.SOURCE_BT:
			s = 5;
			break;
		default:
			s = 0x0c;
			break;
		}
		byte[] data = new byte[] { (byte) 0xc0, 0x2, s, 0 };
		sendDataToCanbox(data, data.length);
	}

	public void sendId3(byte index, String num) {

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

			int num_len = n.length;
			if (num_len >= 70) {
				num_len = 70;
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
			byte[] data = new byte[len];

			data[0] = (byte) 0xca;
			data[1] = (byte) (len - 2);
			data[2] = index;
			data[3] = 0x10;

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
				data[4 + i] = n2[i];
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

	// public void setSongName(String s) {
	// sendId3((byte) 0x2, s);
	// mName = s;
	// }
	//
	// public void setSongAritst(String s) {
	// sendId3((byte) 0x4, s);
	// mArtist = s;
	// }
	//
	// public void setSongAlbum(String s) {
	// sendId3((byte) 0x3, s);
	// mAlbum = s;
	// }

	private int mPhoneStatus = HFP_INFO_INITIAL;

	public void setPhone(int status, String num) {// default is simple box

		mPhoneStatus = status;
		int s = 0;
		switch (status) {
		case HFP_INFO_INITIAL:
		case HFP_INFO_READY:
		case HFP_INFO_CONNECTING:
		case HFP_INFO_CONNECTED:
			s = 0;
			break;
		case HFP_INFO_CALLED:
			s = 3;
			break;
		case HFP_INFO_INCOMING:
			s = 1;
			break;
		case HFP_INFO_CALLING:
			s = 4;
			break;
		}
		if (status >= HFP_INFO_CONNECTED) {
			s |= 0x10;
		}

		// if (status != 0) {
		byte[] data2;

		if (num == null) {
			num = " ";
		}

		data2 = new byte[] { (byte) 0xc5, 0x2, 0x55, (byte) s };
		sendDataToCanbox(data2, data2.length);

		Util.doSleep(50);
		sendId3((byte) 0x3, num);
		// byte[] n = num.getBytes();
		// data2 = new byte[36];// {(byte)0xc5, 0x1, (byte)status};
		// data2[0] = (byte) 0xca;
		// data2[1] = 34;
		// data2[2] = 3;
		// data2[3] = 0x10;
		//
		// int num_len = n.length;
		// if (num_len > 31) {
		// num_len = 31;
		// }
		// byteArrayCopy(data2, n, 4, 0, num_len);
		// data2[num_len + 4] = (byte) 0xff;
		//
		// sendDataToCanbox(data2, data2.length);

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
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == HIDE_RADAR) {
				RadarManager.stop();
			}
			super.handleMessage(msg);
		}
	};

	public void setContext(Context c) {
		super.setContext(c);
		updateTime();
		udpateLang();
	}

	private int DecimaltoBCD(int dVal) {
		return ((dVal % 10) & 0x0F) | (((dVal / 10) << 4) & 0xF0);
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

		if ("12".equals(strTimeFormat)) {
			if (h > 12) {
				h -= 12;
			} else if (h == 0) {
				h = 12;
			}
		}

		byte m = (byte) curDate.getMinutes();
		byte s = (byte) curDate.getSeconds();

		int y = ((curDate.getYear() + 1900));
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();

		byte[] buf = new byte[] { (byte) 0xa7, 0x07,
				(byte) DecimaltoBCD(y / 100), (byte) DecimaltoBCD(y % 100),
				(byte) DecimaltoBCD(mon), (byte) DecimaltoBCD(d),
				(byte) DecimaltoBCD(h), (byte) DecimaltoBCD(m),
				(byte) DecimaltoBCD(s) };
		sendDataToCanbox(buf, buf.length);
	}	


	public void udpateLang() {
		int lang = -1;
		String locale = Locale.getDefault().getLanguage();
		if (locale != null) {
			if (locale.equals("en")) {				
				lang = 2;				
			} else if (locale.equals("it")) {
				lang = 0;
			}else if (locale.equals("tr")) {
				lang = 9;
			} else if (locale.equals("fr")) {
				lang = 4;
			} else if (locale.equals("de")) {
				lang = 1;
			} else if (locale.equals("es")) {
				lang = 3;
			} else if (locale.equals("nl")) {
				lang = 7;
			} else if (locale.equals("pl")) {
				lang = 6;
			} else if (locale.equals("pt")) {				
				lang = 5;
			} else if (locale.equals("es")) {
				lang = 4;
			}

		}
		if (lang != -1) {
			byte[] buf = { (byte) 0x80, 0x2, 0x0, (byte) lang };
			sendDataToCanbox(buf, buf.length);
		}
	}

	public int getUpdateTime() {
		return 60000;
	}
}
