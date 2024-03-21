package com.zhuchao.android.car.cartype.hiworld;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

public class Infiniti001Hiworld extends Canbox {

	public Infiniti001Hiworld() {
		buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
		buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
		buildCmdEQ((byte) 0xa6, (byte) 0xff, 0);
		buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x4);
		buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 0x4);
		buildCmdRadarFrontEx((byte)0x4);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);
		mIdAC = 0x31;

		buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);
		buildCmdKey((byte) 0x21, (byte) 0, (byte) 2, (byte) 0, KEYS_WHEEL2);

		mIdKey3 = 0x021122;
		MAP_KEYS3 = KEYS_WHEEL3;
		
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
	}


	@Override
	public void stopConnect() {

	}

	private final static byte[] IDS_TO_CANBOXSETTING = { 0x16, 0x17, 0x35,
			0x48, 0x32, 0x61, (byte) 0xe8, (byte) 0xa6, 0x62, (byte) 0xa9,
			(byte) 0xa8 };

	private final static byte[][] KEYS_WHEEL = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x4, MyCmd.Keycode.SPEECH },

			{ 0x5, MyCmd.Keycode.BT_DIAL },

			

			{ 0x8, MyCmd.Keycode.PREVIOUS },
			{ 0x9, MyCmd.Keycode.NEXT },

			{ 0xc, MyCmd.Keycode.MODLE },
			{ 0xf, MyCmd.Keycode.PLAY_PAUSE },
			{ 0x10, MyCmd.Keycode.BACK },

	};
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x6, MyCmd.Keycode.BACK },
		{ 0xa, MyCmd.Keycode.NUMBER1 },
		{ 0xb, MyCmd.Keycode.NUMBER2 },
		{ 0xc, MyCmd.Keycode.NUMBER3 },
		{ 0xd, MyCmd.Keycode.NUMBER4 },
		{ 0xe, MyCmd.Keycode.NUMBER5 },
		{ 0xf, MyCmd.Keycode.NUMBER6 },
		{ 0x11, MyCmd.Keycode.EJECT },
		{ 0x12, MyCmd.Keycode.SETUP },
		{ 0x16, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x17, MyCmd.Keycode.PREVIOUS },
		{ 0x18, MyCmd.Keycode.NEXT },
		{ 0x19, MyCmd.Keycode.PREVIOUS },
		{ 0x1a, MyCmd.Keycode.NEXT },
		{ 0x1b, MyCmd.Keycode.PREVIOUS },
		{ 0x1c, MyCmd.Keycode.NEXT },
		{ 0x1d, MyCmd.Keycode.PREVIOUS },
		{ 0x1e, MyCmd.Keycode.NEXT },
		{ 0x20, MyCmd.Keycode.NAVIGATION },
		{ 0x21, MyCmd.Keycode.NAVIGATION },
		{ 0x25, MyCmd.Keycode.NAVIGATION },
		{ 0x2c, MyCmd.Keycode.MODLE },
		{ 0x31, MyCmd.Keycode.KEY_DISPLAY },
		{ 0x37, MyCmd.Keycode.SETUP },
		{ 0x39, MyCmd.Keycode.KEY_DISPLAY },
		{ 0x3f, MyCmd.Keycode.SPEECH },
		{ 0x40, MyCmd.Keycode.EQ },
		{ 0x41, MyCmd.Keycode.AS },
		{ 0x43, MyCmd.Keycode.KEYAMS_RPT },
		{ 0x45, MyCmd.Keycode.VOLUME_UP },
		{ 0x46, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x4b, MyCmd.Keycode.RADIO },
		{ 0x4d, MyCmd.Keycode.KEY_SEEK_PREV },
		{ 0x4e, MyCmd.Keycode.KEY_SEEK_NEXT },
		{ 0x56, MyCmd.Keycode.DVD },
		{ 0x57, MyCmd.Keycode.KEY_TURN_A },
		{ 0x58, MyCmd.Keycode.KEY_TURN_D },
	};

	
	private final static byte[][] KEYS_WHEEL3 = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x2, (byte) 0x24, 0x0, 0xc };
		switch (CarUtil.getModelId()) {
		case 14:
			cmd[2] = 5;
			break;
		case 11:
			cmd[2] = 6;
			break;
		case 19:
			cmd[2] = 7;
			break;
		case 16:
			cmd[2] = 8;
			break;
		}
		return cmd;
	}

	@Override
	public int getAngleValue2(byte[] data) {
		int angle = (short) (((data[8] & 0xff) << 8) | (data[9] & 0xff));
		return -angle;
	}
	
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0xff || (data & 0xff) == 0xfa) {

		} else if ((data & 0xff) == 0xfe) {
			data = 0;
		} else {

		}
		return data;
	}

	public void parseACInfo(byte[] data) {

		byte[] airData = new byte[13];

		airData[0] = (byte) (((data[2] & 0x40) << 1)
				| ((data[2] & 0x08) << 0) 
				| ((data[3] & 0x10) << 1) 
				| ((data[3] & 0x44) << 0) 
				| ((data[4] & 0x20) >> 5) | 
				((data[4] & 0x10) >> 3));
	


		airData[4] = (byte) (((data[2] & 0x03) << 0)
				| ((data[4] & 0x03) << 4) 
				| ((data[3] & 0x02) << 2) 
				| ((data[3] & 0x44) << 0) 
				);
		
		if (((data[3] & 0x18) == 0x10)) {
			airData[4] |= 0x80;
		} else if (((data[3] & 0x18) == 0x08)) {
			airData[0] |= 0x20;
		} 

		airData[8] = (byte) (((data[2] & 0x04) >> 2)
				| ((data[4] & 0x04) >> 1) 
				);
		
		airData[9] = (byte) (((data[4] & 0x4) << 4)
				| ((data[2] & 0x4) >> 0) 
				| ((data[4] & 0x80) >> 2) 
				| ((data[3] & 0x80) >> 3) 
				| ((data[3] & 0x01) << 1) 
				| ((data[2] & 0x10) << 3) 
				);
		
		switch ((data[6] & 0x0f)) {
		case 3:
			airData[1] = (byte) (0x20);
			break;
		case 5:
			airData[1] = (byte) (0x60);
			break;
		case 6:
			airData[1] = (byte) (0x40);
			break;
		case 0xc:
			airData[1] = (byte) (0xa0);
			break;
		}
		
		switch ((data[6] & 0xf0)>>4) {
		case 3:
			airData[6] = (byte) (0x20);
			break;
		case 5:
			airData[6] = (byte) (0x60);
			break;
		case 6:
			airData[6] = (byte) (0x40);
			break;
		case 0xc:
			airData[6] = (byte) (0xa0);
			break;
		}

		switch ((data[10] & 0x0f)) {
		case 1:
			airData[11] = (byte) (0x10);
			break;
		case 2:
			airData[11] = (byte) (0x20);
			break;
		case 3:
			airData[11] = (byte) (0x60);
			break;
		case 4:
			airData[11] = (byte) (0x40);
			break;
		}
		
		airData[1] |= (byte) (data[7] & 0x0f);

		airData[11] |= (byte) (data[11] & 0x0f);

		airData[2] = data[8];
		airData[3] = data[9];
		
		airData[10] = (byte)getACTemp(data[12], data[5]&0x1);

		airData[5] |= 0x88;
		super.parseACInfo(airData);
	}


	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		byte type = 7;
		switch (source) {
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			type = 0xd;
			break;
		}

		String s = String.format("%03d %02d%02d", play, time/60, time%60, Locale.ENGLISH);
		sendLcdInfo(type, s, false);

		
	}
	public void setMediaSrc(int source, byte type, byte []b){
		int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
		if (source == MyCmd.SOURCE_RADIO) {
			String s;

			if (b[0] >= 0x10) { // am

				if (freq < 1000) {
					s = String.format("000 %d 0KHz", (freq), Locale.ENGLISH);
				} else {
					s = String.format("00 %d 0KHz", (freq), Locale.ENGLISH);
				}

				type = 4;
			} else {

				if (freq < 10000) {
					s = String.format("00  %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				} else {
					s = String.format("00 %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				}
				type = 1;
			}

			sendLcdInfo(type, s, false);
		}
	}

	public void setMediaSrc(int source) {
		byte s;
		switch (source) {
//		case MyCmd.SOURCE_RADIO:
//			return;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0xd;
			break;
		case MyCmd.SOURCE_BT:
			s = (byte)0xa;
			break;
		case MyCmd.SOURCE_AUX:
			s = (byte)0xc;
			break;
		case MyCmd.SOURCE_AV_OFF:
			s = 0;
		default:
			return;
		}

		sendLcdInfo(s, null, false);
	}		

	public void sendLcdInfo(byte index, String num, boolean end) {

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = num.getBytes();

			int num_len = n.length;

			if (num_len >= (12)) {
				num_len = (12);
			}
			byte[] data;

			int len = 12 + 3;

			data = new byte[len];

			data[0] = (byte) (13);
			data[1] = (byte) 0x91;
			data[2] = index;
			if (!end) {
				System.arraycopy(n, 0, data, 3, num_len);
			} else {
				for (int i = 0; i < num_len; ++i) {
					data[data.length - i - 1] = n[num_len - i - 1];
				}
			}

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

		}
	}

	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword1(data, len);
	}

	public void startConnect() {
		byte[] buf = new byte[] { 0x3, (byte) 0x6a, 0x5, 1, (byte) 0xf0 };

		sendDataToCanbox(buf, buf.length);
	}

	byte[] mEQBuf = new byte[] { 5, 0, 5, 5, 5, 0 };
	
	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (40 << 16) | (11 << 8) | 11;

			byte[] buf = new byte[] { 0x3, (byte) 0x6a, (byte) 0x5, (byte) 0x1, (byte) 0xa6 };
			sendDataToCanbox(buf, buf.length);
		} else {
			byte id;
			int step;
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				id = 0x6;
				step = data - mEQBuf[0];
				break;
			case EQ_CMD_SET_MIDDLE:
				id = 0x5;
				step = data - mEQBuf[1];
				break;
			case EQ_CMD_SET_LOW:
				id = 0x4;
				step = data - mEQBuf[2];
				break;
			case EQ_CMD_SET_ZONE_FR:
				id = 0x3;
				step = data - mEQBuf[3];
				break;
			case EQ_CMD_SET_ZONE_LR:
				id = 0x2;
				step = data - mEQBuf[4];
				break;
			case EQ_CMD_SET_VOLUME:
				id = 0x1;
				step = data - mEQBuf[0];
				break;
			default:
				return 0;
			}
			
			sendEQCmd(id,step);
		}
		return ret;
	}
	
	private void sendEQCmd(byte id, int step) {
		byte data;
		if (step == 0) {
			return;
		} else if (step > 0) {
			data = 1;
			step--;
		} else {
			data = -1;
			step++;
		}
		byte[] buf = new byte[] { 0x2, (byte) 0xad, id, data };
		sendDataToCanbox(buf, buf.length);

		mHandler.removeMessages(SET_EQ_STEP);
		if (step != 0) {
			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(SET_EQ_STEP, id, step), 200);
		}
	}
	
	private final static int SET_EQ_STEP = 1;
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == SET_EQ_STEP) {
				sendEQCmd((byte) msg.arg1, msg.arg2);
			}
			super.handleMessage(msg);
		}
	};
	
	public void parseEQ(int id, byte[] buf) {
		
		mEQBuf[0] = (byte) (buf[7] + 5);
		mEQBuf[1] = (byte) (buf[6] + 5);
		mEQBuf[2] = (byte) (buf[5] + 5);
		mEQBuf[3] = (byte) (buf[4] + 5);
		mEQBuf[4] = (byte) (buf[3] + 5);
		mEQBuf[5] = (byte) (buf[2] + 5);

		
		super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQBuf);
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
		byte format = 0;
		byte ampm = 0;
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);


		if ("12".equals(strTimeFormat)) {	
			if (h > 12){
				ampm = 1;
			}
		} else {
			format = 1;
		}

		byte m = (byte) curDate.getMinutes();

		byte[] buf = new byte[] { 0x0a, (byte) 0xcb, 0, 0, 0, h, m, format, ampm,
				 0, 0, 0 };

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
			if (locale.equals("zh")) {
				if (!"CN".equals(Locale.getDefault().getCountry())) {
					lang = 0x3;
				} else {
					lang = 2;
				}
			} else if (locale.equals("ko")) {
				lang = 0x11;
			} else if (locale.equals("ru")) {
				lang = 0x12;
			} else {
				lang = 1;
			}

		}
		if (lang != -1) {
			byte[] buf = { 0x2, (byte) 0x9a, 0x1, (byte) lang };
			sendDataToCanbox(buf, buf.length);
		}
	}
}
