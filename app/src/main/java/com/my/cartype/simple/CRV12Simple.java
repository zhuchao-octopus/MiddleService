package com.my.cartype.simple;

import java.util.Date;

import android.content.Context;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class CRV12Simple extends Canbox{

	public CRV12Simple(){
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		
		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = { 0x21, (byte)0xd1, (byte)0xd2, (byte)0xd3	};
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.PREVIOUS },
		{ 0x4, MyCmd.Keycode.NEXT },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x8, MyCmd.Keycode.SPEECH },
		{ 0x9, MyCmd.Keycode.BT_DIAL },
		{ 0xa, MyCmd.Keycode.MULT_BACK_AND_HANG },
		{ 0xb, MyCmd.Keycode.SPEECH },
		{ 0xc, MyCmd.Keycode.BT },
		{ 0xd, MyCmd.Keycode.MULT_BACK_AND_HANG },
	};

	@Override
	public int getAngleValue(byte[] data) {

		int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

		
		return angle;
		
		
	}
	

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		byte min = (byte) ((time / 60) % 60);
		byte sec = (byte) ((time) % 60);
//		++play;
		byte[] data;

		if (MyCmd.SOURCE_DVD != source) {

			++play;
			data = new byte[] { (byte) 0xc3, 0x6, (byte) (total & 0xFF),
					(byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF),
					(byte) ((play >> 8) & 0xFF), min, sec };
		} else {
			data = new byte[] { (byte) 0xc3, 0x6, (byte) (1 & 0xFF),
					(byte) ((play) & 0xFF), (byte) (total & 0xFF),
					(byte) ((0) & 0xFF), min, sec };
		}
		sendDataToCanbox(data, data.length);
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		setMediaSrc(0);
		if (b[0] != 0x10) {
			b[0] += 1;
		}
		byte[] data = new byte[] { (byte) 0xc2, 0x4, b[0], b[1], b[2], 0 };
		sendDataToCanbox(data, data.length);
	}

	public void setMediaSrc(int source) {// default is simple box
		byte s = 0;
		byte mediaType = 0;
		switch (source) {
		case 0:
			s = 1;
			mediaType = 1;
			break;
		case 1:
			s = 2;
			mediaType = 0x10;
			break;
		case MyCmd.SOURCE_IPOD:
			s = 6;
			mediaType = 0x12;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x08;
			mediaType = 0x11;
			break;
		case MyCmd.SOURCE_AUX:
			s = 0x07;
			mediaType = 0x30;
			byte[] data2 = new byte[] { (byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0 };
			sendDataToCanbox(data2, data2.length);
			break;
		case MyCmd.SOURCE_DTV:
			s = 0x0A;
			mediaType = 0x30;
			byte[] data3 = new byte[] { (byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0 };
			sendDataToCanbox(data3, data3.length);
			break;
		case MyCmd.SOURCE_BT:
			s = 0x0b;
			mediaType = 0x30;
			break;
		case MyCmd.SOURCE_AV_OFF:
			s = 0x00;
			mediaType = 0x0;
			break;
		default:
			s = 0xc;
			mediaType = 0x0;
			break;
		}
		byte[] data;
//		if (s == 0xb || s == 0x7) {
//			data = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0,
//					0 };
//		} else {
			data = new byte[] { (byte) 0xc0, 0x2, s, mediaType };
//		}

		sendDataToCanbox(data, data.length);
	}

	public void setVolume(int volume) {

		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
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
			ampm = 1;
		}

		byte m = (byte) curDate.getMinutes();

		byte[] buf = new byte[] { (byte) 0xc8, 0x04, 0, ampm, h, m };
		sendDataToCanbox(buf, buf.length);

	}
	public void setContext(Context c) {
		super.setContext(c);
		updateTime();
	}

}
