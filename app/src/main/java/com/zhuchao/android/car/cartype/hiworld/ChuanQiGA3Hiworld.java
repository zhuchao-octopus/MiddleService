package com.zhuchao.android.car.cartype.hiworld;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

public class ChuanQiGA3Hiworld extends Canbox {

	public ChuanQiGA3Hiworld() {
		buildCmdAngle((byte) 0x72, (byte) 0x0, 780);
		buildCmdRadarBack((byte) 0x72, (byte) 0x0, (byte) 0xfe);
		buildCmdRadarFront((byte) 0x72, (byte) 0x0, (byte) 0xfe);

		buildCmdRadarBackEx((byte) 6);
		buildCmdRadarFrontEx((byte) 10);
		
		buildCmdVersion((byte) 0xf0, (byte) 0x0);
		mIdAC = 0x73;

		buildCmdKey((byte) 0x72, (byte) 5, (byte) 4, (byte) 0, KEYS_WHEEL);
	}



	private final static byte[][] KEYS_WHEEL = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x3, MyCmd.Keycode.MUTE },

			{ 0x5, MyCmd.Keycode.BT_DIAL },

			{ 0x6, MyCmd.Keycode.BT_HANG },

			{ 0x9, MyCmd.Keycode.PREVIOUS },
			{ 0x8, MyCmd.Keycode.NEXT },

			{ 0xa, MyCmd.Keycode.MODLE },
			

			{ 0xd, MyCmd.Keycode.PREVIOUS },
			{ 0xe, MyCmd.Keycode.NEXT },
			{ 0xf, MyCmd.Keycode.PLAY_PAUSE },

	};	
	


	@Override
	public int getAngleValue2(byte[] data) {
		int angle = (((data[6] & 0x7f) << 8) | (data[7] & 0xff));
		if ((data[6] & 0x80) != 0){
			angle = -angle;
		}
		return angle/10;
	}
	
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0xff) {

		} else if ((data & 0xff) == 1) {
			data = 0;
		}  else if ((data & 0xff) == 0) {
			data = (byte)0xfa;
		} else {
			if ((data & 0x80) == 0) {
				data = (byte) ((data & 0x7f) * 2);
			} else {
				data = (byte) (((data & 0x7f) * 2) + 1);
			}
		}
		return data;
	}

	public void parseACInfo(byte[] data) {

		byte[] airData = new byte[8];

		airData[0] = (byte) (((data[2] & 0x40) << 1)
				| ((data[2] & 0x0c) << 0) 
				| ((data[3] & 0x10) >> 3) 
				| ((data[3] & 0x20) >> 5));
		
		if ((data[3] & 0xc0) == 0x40) {
			airData[0] |= (byte) 0x40;
		} else if ((data[3] & 0xc0) == 0x80) {
			airData[7] = (byte) 0x01;
		}
		
		if ((data[2] & 0x30) == 0x10) {
			airData[0] |= (byte) 0x20;
		} else if ((data[2] & 0x30) == 0x30) {
			airData[4] = (byte) 0x80;
		}

		airData[1] = (byte) (((data[6] & 0x40) >> 1)
				| ((data[6] & 0x20) << 1) 
				| ((data[6] & 0x10) << 3) 
				| ((data[6] & 0x0f) << 0));		


		airData[2] = data[4];
		airData[3] = data[5];

	//	airData[5] |= 0x80;
		super.parseACInfo(airData);
	}


	public void startConnect() {

		copyLcdInfo(mLcdInfo, "");
		mLcdInfo[0] = 0xd;
		mLcdInfo[1] = (byte)0xd2;
		mLcdInfo[2] = 0;
		sendDataToCanbox(mLcdInfo, mLcdInfo.length);
	}

	public void stopConnect() {
		copyLcdInfo(mLcdInfo, "");
		mLcdInfo[0] = 0xd;
		mLcdInfo[1] = (byte)0xd2;
		mLcdInfo[2] = 0;
		sendDataToCanbox(mLcdInfo, mLcdInfo.length);
	}
	
	private void copyLcdInfo(byte[] lcd, String s) {
		byte[] b = s.getBytes();

		for (int i = 0; i < lcd.length - 3 ; ++i) {
			if (i < b.length){
				lcd[i + 3] = b[i];
			} else {
				lcd[i + 3] = 0;
			}
		}
	}	
	
	private final byte[] mLcdInfo = new byte[15];
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		switch (source) {
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			break;
		}

		String s = String.format("%02d:%02d:%02d %d", time/3600, time/60, time%60,  play,
				Locale.ENGLISH);
		copyLcdInfo(mLcdInfo, s);
		sendDataToCanbox(mLcdInfo, mLcdInfo.length);
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
		if (source == MyCmd.SOURCE_RADIO) {
			String s;
			if (b[0] >= 0x10) { // am
				s = String.format("%d 0KHz", (freq), Locale.ENGLISH);
				copyLcdInfo(mLcdInfo, s);
				mLcdInfo[2] = 0x4;
			} else {

				if (freq < 10000) {
					s = String.format("%d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				} else {
					s = String.format("%d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				}
				copyLcdInfo(mLcdInfo, s);
				mLcdInfo[2] = 0x1;
			}
		}

		sendDataToCanbox(mLcdInfo, mLcdInfo.length);		
	}

	public void setMediaSrc(int source) {// default is simple box
		String s = "";
		switch (source) {
		case MyCmd.SOURCE_RADIO: {
		}
			return;
		case MyCmd.SOURCE_DVD: {
			mLcdInfo[2] = 0x7;
		}
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO: {
			mLcdInfo[2] = 0xd;
		}
			break;
		case MyCmd.SOURCE_AUX:
			mLcdInfo[3] = 0xc;
			break;
		case MyCmd.SOURCE_BT_MUSIC:
			mLcdInfo[3] = 0xa;
			break;
		}
		copyLcdInfo(mLcdInfo, s);
		sendDataToCanbox(mLcdInfo, mLcdInfo.length);
	}


	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword2(data, len);
	}

}
