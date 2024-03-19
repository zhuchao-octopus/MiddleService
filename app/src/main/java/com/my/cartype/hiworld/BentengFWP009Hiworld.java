package com.my.cartype.hiworld;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;

public class BentengFWP009Hiworld extends Canbox {

	public BentengFWP009Hiworld() {
		buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);
		mIdAC = 0x31;

		buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);

		
	}


	@Override
	public void stopConnect() {

	}
	private final static byte KEYS_WHEEL[][] = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x3, MyCmd.Keycode.MUTE },		

			{ 0xd, MyCmd.Keycode.PREVIOUS },
			{ 0xe, MyCmd.Keycode.NEXT },

			{ 0xc, MyCmd.Keycode.MODLE },

	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x2, (byte) 0x24, 0xc, 0xf };
	
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
				| ((data[2] & 0x08) << 0) 
				| ((data[3] & 0x10) << 1) 
				| ((data[3] & 0x40) << 0) 
				| ((data[4] & 0x20) >> 5) | 
				((data[4] & 0x10) >> 3));

//		if (((data[3] & 0x10) == 0)) {
//			airData[0] |= 0x20;
//		}

		airData[4] = (byte) (((data[3] & 0x1) << 7));
//		airData[7] = (byte) 0x40;

		switch ((data[6] & 0xff)) {
		case 1:
			airData[9] = (byte) (0x1);
			break;
		case 2:
			// airData[1] = (byte) (0x20);
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
		case 0xb:
			airData[1] = (byte) (0x80);
			break;
		case 0xc:
			airData[1] = (byte) (0xa0);
			break;
		case 0xd:
			airData[1] = (byte) (0xc0);
			break;
		case 0xe:
			airData[1] = (byte) (0xe0);
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
		byte type = 7;
		switch (source) {
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			type = 0xd;
			break;
		}

		String s = String.format("%03d  %03d", play, total, Locale.ENGLISH);
		sendLcdInfo(type, s, false);

		
	}
	public void setMediaSrc(int source, byte type, byte []b){
		int freq = (int) ((b[1] & 0xff) | ((b[2] & 0xff) << 8));
		switch (source) {
		case MyCmd.SOURCE_RADIO: {
			String s;
			
			if (b[0] >= 0x10) { // am
				
				if (freq < 1000){
					s = String.format("10 %d ", (freq), Locale.ENGLISH);
				} else {
					s = String.format("10 %d ", (freq), Locale.ENGLISH);
				}
				
				type = 4;
			} else {
				
				if (freq < 10000){
					s = String.format("00  %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				} else {
					s = String.format("00 %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				}
				type = 1;
			}
			
			sendLcdInfo(type, s, false);
		}
			break;
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
			data[1] = (byte) 0xe1;
			data[2] = (byte) index;
			if (!end) {
				for (int i = 0; i < num_len; ++i) {
					data[3 + i] = n[i];
				}
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
	
}
