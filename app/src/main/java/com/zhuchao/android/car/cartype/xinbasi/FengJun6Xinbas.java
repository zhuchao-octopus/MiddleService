package com.zhuchao.android.car.cartype.xinbasi;

import java.util.Date;
import java.util.Locale;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.R;

public class FengJun6Xinbas extends Canbox{

	public FengJun6Xinbas(){


		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdAC = 0x2;
		mIdKey = 0x1;
		MAP_KEYS = KEYS_WHEEL;
	
	}


	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x4, MyCmd.Keycode.PREVIOUS },

		
		{ 0xb, MyCmd.Keycode.MUTE },
		{ 0x9, MyCmd.Keycode.BT_DIAL },
		{ 0xa, MyCmd.Keycode.BT_HANG },



	};


	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0xff) {

		} else if ((data & 0xff) == 0x0) {

		} else if ((data & 0xff) > 0xf || (data & 0xff) < 0) {

			data = (byte) 0xfa;
		} else {
			data = (byte) (36 + (((data & 0xff) - 1) * 2));

		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0xea));				
		
	


		airData[1] = (byte) ((data[3] & 0xff));		
		

		airData[2] = data[4];
		airData[3] = data[5];


		airData[5] |= 0x80;
		
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {		

		
		
	}
	@Override
	public void startConnect() {
		// TODO Auto-generated method stub
		super.startConnect();
		byte [] mData = new byte[] { (byte) 0xff, 0x1, (byte) 0x7f};
		sendDataToCanbox(mData, mData.length);
	}
	public void setMediaSrc(int source, byte type, byte []b){

		int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
		if (source == MyCmd.SOURCE_RADIO) {
			String s;
			byte[] mData = new byte[]{(byte) 0x82, 0x2, 0x20, 0};
			if (b[0] >= 0x10) { // am

				if (freq < 1000) {
					s = String.format("AM %d 0KHz", (freq), Locale.ENGLISH);
				} else {
					s = String.format("AM %d 0KHz", (freq), Locale.ENGLISH);
				}

				type = 4;
				mData[2] = 0;
			} else {

				if (freq < 10000) {
					s = String.format("FM %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				} else {
					s = String.format("FM %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				}
				type = 1;


			}
			sendLcdInfo(s);

			sendDataToCanbox(mData, mData.length);
		}
	}

	public void sendLcdInfo(String num) {

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

			data[0] = (byte) (0x83);
			data[1] = (byte) 12;
			System.arraycopy(n, 0, data, 2, num_len);

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

		}
	}
	
	public void setMediaSrc(int source) {
		if (source!=MyCmd.SOURCE_RADIO){
			sendLcdInfo("");
		}
		
	}

}
