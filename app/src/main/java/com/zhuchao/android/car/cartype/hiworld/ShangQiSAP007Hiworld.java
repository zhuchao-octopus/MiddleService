package com.zhuchao.android.car.cartype.hiworld;

import java.util.Date;
import java.util.Locale;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

public class ShangQiSAP007Hiworld extends Canbox {

	public ShangQiSAP007Hiworld() {
		buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
		buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
		// buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);
		mIdAC = 0x31;

		buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
		buildCmdKey((byte) 0x21, (byte) 2, KEYS_WHEEL2);

		mIdKey3 = 0x021122;
		MAP_KEYS3 = KEYS_WHEEL3;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
	}

	@Override
	public int getAngleValue2(byte[] data) {
		int angle = (short) (((data[9] & 0xff) << 8) | (data[8] & 0xff));
		return -angle;
	}
	
	@Override
	public void stopConnect() {

	}

	private final static byte[] IDS_TO_CANBOXSETTING = { 0x14, 0x15, 0x32,
			0x34, 0x35, 0x3f, 0x48, 0x66 };

	private final static byte[][] KEYS_WHEEL = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN },

			{ 0x3, MyCmd.Keycode.MUTE },
			{ 0x4, MyCmd.Keycode.SPEECH },

			{ 0x5, MyCmd.Keycode.BT_DIAL },

			{ 0x6, MyCmd.Keycode.BT_HANG },

			{ 0xa, MyCmd.Keycode.MODLE }, 
			{ 0xb, MyCmd.Keycode.MODLE }, 
			
			{ 0x9, MyCmd.Keycode.NEXT },
			{ 0x8, MyCmd.Keycode.PREVIOUS },

			{ 0x40, MyCmd.Keycode.BT},
	};

	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.POWER },
			{ 0x2, MyCmd.Keycode.PREVIOUS },
			{ 0x3, MyCmd.Keycode.NEXT },
			{ 0x6, MyCmd.Keycode.BACK }, 
			{ 0x9, MyCmd.Keycode.MUTE },
			{ 0x20, MyCmd.Keycode.NAVIGATION }, 
			{ 0x2b, MyCmd.Keycode.HOME },
			{ 0x2c, MyCmd.Keycode.MODLE },
			{ 0x2d, MyCmd.Keycode.AUDIO },


			{ 0x2f, MyCmd.Keycode.MENU },
			
			
			{ 0x37, MyCmd.Keycode.SETUP },
			{ 0x45, MyCmd.Keycode.VOLUME_UP },
			{ 0x46, MyCmd.Keycode.VOLUME_DOWN },

	};
	private final static byte[][] KEYS_WHEEL3 = {
			{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
			{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 }, };


	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x2, (byte) 0x24, 0x0, 0xd };
		switch(CarUtil.getModelId()){
		case 53:
			cmd[2] = 0x10;
			break;
		case 54:
			cmd[2] = 0x11;
			break;
		case 55:
			cmd[2] = 0x12;
			break;
		case 56:
			cmd[2] = 0x13;
			break;
		case 57:
			cmd[2] = 0x14;
			break;
		case 58:
			cmd[2] = 0x15;
			break;
		case 59:
			cmd[2] = 0x16;
			break;
		case 60:
			cmd[2] = 0x17;
			break;
		case 61:
			if (CarUtil.getCarTypeConfig() == 0){
				cmd[2] = 0x18;
			} else {
				return null;
			}
			break;
		case 62:
			cmd[2] = 0x1a;
			break;
		case 63:
			cmd[2] = 0x1b;
			break;
		case 64:
			cmd[2] = 0x1c;
			break;
		case 65:
			cmd[2] = 0x1d;
			break;
		case 66:
			cmd[2] = 0x1e;
			break;
		default:
			return null;
		}
		return cmd;
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

		byte[] airData = new byte[10];

		airData[0] = (byte) (((data[2] & 0x40) << 1) 
				| ((data[2] & 0x0c) << 0)
				| ((data[3] & 0x40) << 0) 
				| ((data[4] & 0x20) >> 5) 
				| ((data[4] & 0x10) >> 3));

		if (((data[3] & 0x30) == 0x10)) {
			airData[0] |= 0x20;
		} else if (((data[3] & 0x30) == 0x20)) {
			airData[4] = (byte)0x80;
		}

		airData[4] |= (byte) (((data[2] & 0x20) >> 3));
		airData[4] |= (byte) (((data[4] & 0x03) << 4));
		airData[4] |= (byte) (((data[4] & 0x0c) >> 2));

		switch ((data[6] & 0xff)) {
		case 2:
			airData[1] = (byte) (0x80);
			break;
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

		airData[1] |= (byte) (data[7] & 0x0f);

		airData[2] = data[8];
		airData[3] = data[9];

		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

	}

	public void setMediaSrc(int source, byte type, byte[] b) {

	}

	public void setMediaSrc(int source) {

	}

	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword1(data, len);
	}
	public void startConnect() {
		updateTime();
		udpateLang();
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
		byte ampm = 0;
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);


		if ("12".equals(strTimeFormat)) {		
		} else {
			ampm = 1;
		}

	

		byte m = (byte) curDate.getMinutes();

		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();
		byte[] buf = new byte[] { 0x0a, (byte) 0xcb, 0, h, m, 0, 0, ampm,
				 y, mon, d,0 };

		sendDataToCanbox(buf, buf.length);

	}

	public void udpateLang() {
		int lang = -1;
		String locale = Locale.getDefault().getLanguage();
		if (locale != null) {
			if (locale.equals("zh")) {
				lang = 2;
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
