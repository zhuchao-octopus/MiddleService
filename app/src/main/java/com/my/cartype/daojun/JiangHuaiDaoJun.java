package com.my.cartype.daojun;

import java.util.Calendar;
import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class JiangHuaiDaoJun extends Canbox{

	public JiangHuaiDaoJun(){

		buildCmdVersion((byte) 0x30, (byte) 0x0);


		mIdKey = 0x2;
		MAP_KEYS = KEYS_WHEEL;		
	}


	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x2, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x3, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x4, MyCmd.Keycode.SETUP },
		{ 0x5, MyCmd.Keycode.MODLE },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.RADIO },
		{ 0x8, MyCmd.Keycode.PREVIOUS },
		{ 0x9, MyCmd.Keycode.NEXT },
		{ 0xa, MyCmd.Keycode.MODLE },
		{ 0xb, MyCmd.Keycode.NAVIGATION },
		
	};

	
	private byte[] mData = new byte[] { (byte) 0x75, 0x8, 0, 0, 0, 0, 0, 0, 0,
			0 };

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		mData[2] = 3;
		mData[4] = 0;
		mData[5] = 0;
		mData[6] = 0;
		mData[7] = (byte) (time / 60);
		mData[8] = (byte) (time % 60);

		sendDataToCanbox(mData, mData.length);

	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		setMediaSrc(0);
		if (b[0] < 0x10) {
			int freq = ((b[2]&0xff)<<8) | (b[1]&0xff);
			freq = freq/10;
			

			b[1] = (byte) (freq & 0xff);
			b[2] = (byte) ((freq >> 8) & 0xff);
			
			mData[3] = 1;
		} else {
			mData[3] = 3;
		}
		mData[2] = 1;
		mData[4] = b[2];
		mData[5] = b[1];
		mData[6] = 0;
		mData[7] = 0;
		mData[8] = 0;
		sendDataToCanbox(mData, mData.length);
	}

	public void setMediaSrc(int source) {// default is simple box
		byte s = 0;
		switch (source) {
		case MyCmd.SOURCE_RADIO:
			return;
		case MyCmd.SOURCE_DVD:
			s = 2;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 3;
			break;
		case MyCmd.SOURCE_AUX:
			s = 0x05;
			break;
		case MyCmd.SOURCE_BT:
			s = 0x04;
			break;
		case MyCmd.SOURCE_AV_OFF:
			s = 0x00;
			break;
		}

		mData[2] = s;
		mData[4] = 0;
		mData[5] = 0;
		mData[6] = 0;
		mData[7] = 0;
		mData[8] = 0;


		sendDataToCanbox(mData, mData.length);
	}
	


}
