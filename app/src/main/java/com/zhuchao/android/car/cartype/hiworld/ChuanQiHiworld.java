package com.zhuchao.android.car.cartype.hiworld;

import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class ChuanQiHiworld extends Canbox{

	public ChuanQiHiworld(){
		buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
//		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
//		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);		
		mIdAC = 0x31;
		
		buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
		buildCmdKey((byte) 0x21, (byte) 2,  KEYS_WHEEL2);

		mIdKey3 = 0x021122;
		MAP_KEYS3 = KEYS_WHEEL3;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}


	@Override
	public void stopConnect() {

	}
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x12, 0x32, 0x25,
			0x4f, (byte) 0x96, 0x3e, 0x67 };
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },

		{ 0x3, MyCmd.Keycode.MUTE },
		{ 0x4, MyCmd.Keycode.SPEECH },

		{ 0x5, MyCmd.Keycode.BT_DIAL },

		{ 0x6, MyCmd.Keycode.BT_HANG },
		
		{ 0xa, MyCmd.Keycode.MODLE },
		{ 0x9, MyCmd.Keycode.NEXT },
		{ 0x8, MyCmd.Keycode.PREVIOUS },

	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x2, MyCmd.Keycode.PREVIOUS },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x4, MyCmd.Keycode.RADIO },
		{ 0x9, MyCmd.Keycode.MUTE },
		{ 0xa, MyCmd.Keycode.NUMBER1 },
		{ 0xb, MyCmd.Keycode.NUMBER2 },
		{ 0xc, MyCmd.Keycode.NUMBER3 },
		{ 0xd, MyCmd.Keycode.NUMBER4 },
		{ 0xe, MyCmd.Keycode.NUMBER5 },
		{ 0xf, MyCmd.Keycode.NUMBER6 },
		{ 0x11, MyCmd.Keycode.EJECT },
		{ 0x15, MyCmd.Keycode.ALL_APP },
		{ 0x16, MyCmd.Keycode.RADIO },
		{ 0x25, MyCmd.Keycode.NAVIGATION },
		{ 0x28, MyCmd.Keycode.BT },
		{ 0x2d, MyCmd.Keycode.HOME },
		{ 0x36, MyCmd.Keycode.SETUP },
		{ 0x38, MyCmd.Keycode.MODLE },
		{ 0x39, MyCmd.Keycode.AS },
		{ 0x3f, MyCmd.Keycode.SPEECH },

	};
	private final static byte[][] KEYS_WHEEL3 = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
	};

	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x2, (byte) 0x24, 0x0, 0xe };
		switch(CarUtil.getModelId()){
		case 2:
			cmd[2] = 1;
			break;
		case 3:
		case 4:
			cmd[2] = 2;
			break;
		case 1:
			cmd[2] = 3;
			break;
		case 6:
			cmd[2] = 4;
			break;
		case 7:
		case 9:
			cmd[2] = 6;
			break;
		case 10:
			cmd[2] = 7;
			break;
		case 17:
			cmd[2] = 8;
			break;
		default:
			return null;
		}
		return cmd;
	}
	@Override
	public int getAngleValue(byte[] data) {

		int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

		
		return angle;
		
		
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
				| ((data[2] & 0x01) << 6)
				| ((data[3] & 0x08) << 1) 
				| ((data[4] & 0x20) >> 5) 
				| ((data[4] & 0x10) >> 3));

		if (((data[3] & 0x30) == 0x00)) {
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
	public void setMediaSrc(int source, byte type, byte []b){
		
	}

	public void setMediaSrc(int source) {

	}		
public void startConnect() {
		
	}
	



	
	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword1(data, len);
	}

	public void setContext(Context c) {
		super.setContext(c);
		updateTime();
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
			if (h > 12) {
				h -= 12;
			} else if (h == 0) {
				h = 12;
			}
			h |= 0x80;
		} else {
			ampm = 1;
		}

		byte format = 2;
		
		String date_foramt = SystemConfig.getProperty(mContext,
				SystemConfig.KEY_DATE_FORMAT);
		if (date_foramt != null) {
			if ("dd/MM/yyyy".equals(date_foramt)){
				format = 1;
			} else if ("MM/dd/yyyy".equals(date_foramt)){
				format = 3;
			}
		}
		
		byte m = (byte) curDate.getMinutes();
		// byte s = (byte) curDate.getSeconds();

		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();
		byte[] buf = new byte[] { 0x0a, (byte) 0xcb, 0, h, m, 0, 0, ampm, y,
				mon, d, format };

		sendDataToCanbox(buf, buf.length);

	}
}
