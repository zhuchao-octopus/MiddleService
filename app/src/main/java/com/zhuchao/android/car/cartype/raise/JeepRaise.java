package com.zhuchao.android.car.cartype.raise;

import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class JeepRaise extends Canbox{

	public JeepRaise(){
		mIdAC = 0x5;
		buildCmdDoor((byte) 0xa, (byte) 0x2, (byte) 0xf8, (byte) 0x03);
		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x5);
		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x5);
		if (isAnglesStyle0()){
			buildCmdAngle((byte) 0x9, (byte) 0x3, 540);
		} else {
			buildCmdAngle((byte) 0x9, (byte) 0x4, 540);
		}
		buildCmdEQ((byte) 0x31, (byte) 0x1, 6);
//		buildCmdOutTemp((byte) 0x5, (byte) 0x0);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdKey = 0x1;
		MAP_KEYS = KEYS_WHEEL;

		mIdKey2 = 0x4;
		MAP_KEYS2 = KEYS_WHEEL2;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;	

		mLcdInfo[0] = (byte)0x90;
		mLcdInfo[1] = 0x1f;
	}
	
//	public int getOutTemp(byte[] data) {//
//		int t = ((data[9] & 0xff) - 40) * 10;
//		return t;
//	}
	
	public int getAngleValue(byte[] data) {
		short a = (short) ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));
		int angle = a;
		if (isAnglesStyle0()) {
			angle = ((angle * 3000) / 540);
		} else {
			if (angle == 0x700) {
				angle = 0;
			} else if (angle > 0x700) {
				angle -= 0x700;
				angle = ((angle * 3000) / ((0xbaf - 0x700)));
			} else {
				angle -= 0x700;
				angle = ((angle * 3000) / ((0x700 - 0x268)));
			}
		}

		if (angle > -50 && angle < 50) {
			angle = 50;
		}

		return angle;
	}
	
	
	private boolean isAnglesStyle0(){
		return CarUtil.getModelId() != 4 && CarUtil.getModelId() != 15;
	}
	private final static byte[] IDS_TO_CANBOXSETTING = { 0xa, 6, 7, 0x19, 0x10, 0x11	};
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x6, KEY_MUTE },
		{ 0x11, AK_KEYPAD_VOLUME_A },
			{ 0x12, AK_KEYPAD_VOLUME_D }, 
//			{ 0x14, KEY_NEXTSONG },
//			{ 0x13, KEY_PREVIOUSSONG }, 
			{ 0x15, KEY_SOURCE },
			{ 0x16, KEY_BACK },
			{ 0x17, KEY_MIC },
			{ 0x18, KEY_BT }, 
			{ 0x1a, KEY_SOURCE },
			{ 0x1b, KEY_FM },
			{ 0x1c, MyCmd.Keycode.KEY_SEEK_NEXT },
			{ 0x1d, MyCmd.Keycode.KEY_SEEK_PREV },
//			{ 0x1f, KEY_NEXTSONG },
//			{ 0x1e, KEY_PREVIOUSSONG }, 
//			{ 0x20, MyCmd.Keycode.PLAY_PAUSE },
	};
	
	private final static byte[][] KEYS_WHEEL2 = {

		{ 0x1, MyCmd.Keycode.BACKLIGHT_OFF },
		{ 0x2, MyCmd.Keycode.BACK },
		{ 0x3, MyCmd.Keycode.MUTE },
		{ 0x4, MyCmd.Keycode.HOME },
		{ 0x5, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x6, MyCmd.Keycode.VOLUME_UP },
		{ 0x7, MyCmd.Keycode.KEY_SEEK_PREV },
		{ 0x8, MyCmd.Keycode.KEY_SEEK_NEXT },
		{ 0x9, MyCmd.Keycode.POWER },
		{ 0xa, MyCmd.Keycode.POWER },
//		{ 0xb, MyCmd.Keycode.BACKLIGHT_OFF },
		
		
		{ 0x6, KEY_MUTE },
		{ 0x11, AK_KEYPAD_VOLUME_A },
			{ 0x12, AK_KEYPAD_VOLUME_D }, 
//			{ 0x14, KEY_NEXTSONG },
//			{ 0x13, KEY_PREVIOUSSONG }, 
			{ 0x15, KEY_SOURCE },
			{ 0x16, KEY_BACK },
			{ 0x17, KEY_MIC },
			{ 0x18, KEY_BT }, 
			{ 0x1a, KEY_SOURCE },
			{ 0x1b, KEY_FM },
			{ 0x1c, MyCmd.Keycode.KEY_SEEK_NEXT },
			{ 0x1d, MyCmd.Keycode.KEY_SEEK_PREV },
//			{ 0x1f, KEY_NEXTSONG },
//			{ 0x1e, KEY_PREVIOUSSONG }, 
//			{ 0x20, MyCmd.Keycode.PLAY_PAUSE },
	};
	
	private int getTempStyle() {
		if (CarUtil.getModelId() == 7 || CarUtil.getModelId() == 12) {
			return 1;
		} else if (CarUtil.getModelId() == 1 || 
				CarUtil.getModelId() == 6 ||
				CarUtil.getModelId() == 18) {
			return 0;
		}
		return 2;
	}
	
	public int getACTemp(byte data) {//
		if ((data & 0xff) == 0x7f) {
			data = (byte) 0xff;
		} else if (data == 0) {
			data = 0;
		} else {
			switch (getTempStyle()) {
			case 0:
				data = (byte) (28 + (data - 0xe) * 2);
				break;
			case 1:
				data = (byte) (32 + (data - 0x10));
				break;
			case 2:
				data = (byte) (32 + (data - 0x1));
				break;
			}
		}
		return (data & 0xff);
	}
	public void parseACInfo(byte[] data)
	{
	
		
		byte[]	airData = new byte[13];

		airData[2] = data[5];
		airData[3] = data[6];
		
		airData[0] = (byte) ((data[2] & 0xd0) | 
				((data[2] & 0x04)<<3)|
				((data[2] & 0x02)>>1)|
				((data[2] & 0x01)<<1));	

		airData[4] = (byte) ((data[2] & 0x08) <<4);
		airData[4] |= (byte) ((data[2] & 0x20) >>3);
		airData[7] = (byte) ((data[3] & 0xc0) << 1);


		airData[1] = (byte) (data[3] & 0x0f);
		airData[1] |= (byte) ((data[4] & 0x40) <<1);
		airData[1] |= (byte) ((data[4] & 0x20) <<0);
		airData[1] |= (byte) ((data[4] & 0x10) <<2);
		
		airData[9] = (byte) ((data[4] & 0x80) >>6);

		airData[4] |= (byte) ((data[7] & 0xc0) >>2);
		airData[4] |= (byte) ((data[7] & 0x0c) >>2);
		
		airData[8] = (byte) (data[7] & 0x30);
		airData[8] |= (byte) ((data[7] & 0x03) <<2);

		airData[12] = (byte) ((data[8] & 0x80) >>7);
		
		airData[5] = (byte) ((CarUtil.mTempUnit & 0x03) << 3);
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	
	private final byte[] mLcdInfo = new byte[33];

	public void sendLcdInfo() {
		sendDataToCanbox(mLcdInfo, mLcdInfo.length);
	}

	public void parseCanboxData(byte[] data, int len) {		
		super.parseCanboxData(data, len);
		if (data[0] == 0x7) {
			if ((data[12] & 0x10) != 0) {
				CarUtil.mTempUnit = 2;
			} else {
				CarUtil.mTempUnit = 0;
			}
		}
	}
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
//		byte min = (byte) ((time / 60) % 60);
//		byte sec = (byte) ((time) % 60);
//		
//		byte[] data;
//
//		if (MyCmd.SOURCE_DVD != source) {
//			++play;
//			data = new byte[] { (byte) 0xc3, 0x6, (byte) (play & 0xFF),
//					(byte) ((play >> 8) & 0xFF), (byte) (total & 0xFF),
//					(byte) ((total >> 8) & 0xFF), min, sec };
//		} else {
//			data = new byte[] { (byte) 0xc3, 0x6, (byte) (1 & 0xFF),
//					(byte) ((play) & 0xFF), (byte) (total & 0xFF),
//					(byte) ((0) & 0xFF), min, sec };
//		}
//		sendDataToCanbox(data, data.length);
	}
	
	private void copyLcdInfo(int index, String s) {
		try {
			mLcdInfo[2] = (byte) index;
			byte[] buf = getBytesUnicodeLittleEndian(s); //del 0xff 0xfe

			int num_len = buf.length;
			int start = 0;
			if ((buf[0] & 0xff) == 0xff && (buf[1] & 0xff) == 0xfe) {
				num_len -= 2;
				start = 2;
			}

			for (int i = 0; i < (mLcdInfo.length - 3); ++i) {
				if (i < num_len) {
					if (i % 2 == 0) {
						mLcdInfo[3 + i] = buf[i + start + 1];
					} else {
						mLcdInfo[3 + i] = buf[i + start - 1];
					}

				} else {
					mLcdInfo[3 + i] = 0;
				}
			}
		} catch (Exception e) {
			// Log.d("ffck", "ee"+e);
		}

		sendLcdInfo();
	}

	public void setSongName(String s) {
		copyLcdInfo(4, s);
		sendLcdInfo();
	}
	
	public void setMediaSrc(int source, byte type, byte[] b) {

		int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
		Log.e("ffc", freq + ":" + b[1] + ":" + b[2] + ":");
		if (source == MyCmd.SOURCE_RADIO) {
			String s;
			if (b[0] == 16) { // am
				s = "AM " + freq + " KHz";
				copyLcdInfo(2, s);
			} else {

				s = "FM " + String.format("%d.%02d", (freq) / 100, (freq) % 100) + " MHz";
				copyLcdInfo(1, s);
			}
		}

	}

	public void setMediaSrc(int source) {// default is simple box
		byte s = 0;
		String text;

		switch (source) {
		case MyCmd.SOURCE_RADIO:
			s = 1;
			return;
		case MyCmd.SOURCE_DVD:
			s = 3;
			text = "DVD";
//			return;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x04;
			return;
		case MyCmd.SOURCE_BT:
			s = 0x05;
			text = "BT_MUSIC";
			break;
		case MyCmd.SOURCE_AUX:
		default:
			s = 0x07;
			text = "AUX";
			break;
		}

		copyLcdInfo(s, text);
		sendLcdInfo();
	}
	
	private final void zeroBuf(byte[] buf, int start) {
		if (buf != null) {
			for (int i = start; i < buf.length; ++i) {
				buf[i] = 0;
			}
		}
	}
	
	public void setVolume(int volume) {		
		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
		sendDataToCanbox(data, data.length);	
	}
	
	public boolean requestAngleData() {
		byte[] data3 = new byte[] { (byte) 0xf1, 0x1, 0x9 };
		sendDataToCanbox(data3, data3.length);
		return true;
	}	


	public void setPhone(int phone_status, String num) {// default is simple box

		byte status = 0;
		switch (phone_status) {
		case HFP_INFO_INITIAL:
		case HFP_INFO_READY:
		case HFP_INFO_CONNECTING:
			status = 6;
			break;
		case HFP_INFO_CONNECTED:
			status = 0;
			break;
		case HFP_INFO_CALLED:
			status = 2;
			break;
		case HFP_INFO_INCOMING:
			status = 1;
			break;
		case HFP_INFO_CALLING:
			status = 4;
			break;
		}
		if (num == null) {
			num = "";
		}

		byte[] n = num.getBytes();
		int num_len = n.length;
		int len = num_len + 4;
		byte[] data = new byte[len];
		data[0] = (byte) 0xc5;
		data[1] = (byte) (len - 2);
		data[2] = status;
		data[3] = 0x1;
		for (int i = 0; i < num_len && i < (len - 4); ++i) {
			data[4 + i] = n[i];
		}
		sendDataToCanbox(data, data.length);
	}

	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (39 << 16) | (19 << 8) | 19;

			byte[] buf = new byte[] { (byte) 0x90, 0x2,  (byte) 0x93, 0 };
			sendDataToCanbox(buf, buf.length);
		} else {
			byte[] buf = new byte[] { (byte) 0x93, 0x2, 0x0, (byte) data };
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				buf[2] = 6;
				break;
			case EQ_CMD_SET_MIDDLE:
				buf[2] = 5;
				break;
			case EQ_CMD_SET_LOW:
				buf[2] = 4;
				break;
			case EQ_CMD_SET_ZONE_FR:
				buf[2] = 3;
				break;
			case EQ_CMD_SET_ZONE_LR:
				buf[2] = 2;
				break;
			case EQ_CMD_SET_VOLUME:
				buf[2] = 1;
				break;
			default:
				return 0;
			}

			sendDataToCanbox(buf, buf.length);
		}
		return ret;
	}
	
	
	public int getUpdateTime() {
		return 60000;
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
			h |= 0x80;
		}

		byte m = (byte) curDate.getMinutes();
		byte s = (byte) curDate.getSeconds();

		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();

		byte[] buf = new byte[] { (byte) 0xc6, 0x07, 0x50,   h, m , s, y, mon, d};
		sendDataToCanbox(buf, buf.length);
	}
	
	public void setContext(Context c) {
		super.setContext(c);
		udpateLang();
	}
	
	public void udpateLang() {
		int lang = -1;
		String locale = Locale.getDefault().getLanguage();
		if (locale != null) {
			if (locale.equals("en")) {
				if ("GB".equals(Locale.getDefault().getCountry())) {
					lang = 0xe;
				} else {
					lang = 1;
				}
			} else if (locale.equals("it")) {
				lang = 0x6;
			} else if (locale.equals("tr")) {
				lang = 0xb;
			} else if (locale.equals("fr")) {
				lang = 0x2;
			} else if (locale.equals("de")) {
				lang = 0x5;
			} else if (locale.equals("es")) {
				lang = 0x4;
			} else if (locale.equals("nl")) {
				lang = 0x7;
			} else if (locale.equals("pt")) {
				if ("BR".equals(Locale.getDefault().getCountry())) {
					lang = 0x3;
				} else {
					lang = 0xa;
				}
			} else if (locale.equals("es")) {
				lang = 0x4;
			} else if (locale.equals("pl")) {
				lang = 0x8;
			} else if (locale.equals("zh")) {
				lang = 0x9;
			} else if (locale.equals("ar")) {
				lang = 0xc;
			} else if (locale.equals("ru")) {
				lang = 0xd;
			}
		}
		if (lang != -1) {
			byte[] buf = { (byte) 0x97, 0x2, 0x53, (byte) lang };
			sendDataToCanbox(buf, buf.length);
		}
	}
}
