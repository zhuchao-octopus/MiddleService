package com.my.cartype.xinbasi;

import java.util.Date;
import java.util.Locale;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class X80Xinbas extends Canbox{

	public X80Xinbas(){


		buildCmdRadarBack((byte) 0x4, (byte) 0x0, (byte) 0x3, (byte) 0x3, (byte) 0x3);
		buildCmdRadarBackEx((byte)1);

		buildCmdAngle((byte) 0x5, (byte) 0x0, 0x1600);
		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdAC = 0x3;
		mIdKey = 0x2;
		MAP_KEYS = KEYS_WHEEL;
	
	}


	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.MODLE },
		{ 0x4, MyCmd.Keycode.MUTE },
		{ 0x5, MyCmd.Keycode.NEXT },
		{ 0x6, MyCmd.Keycode.PREVIOUS },

		
		{ 0x7, MyCmd.Keycode.BT_DIAL },
		{ 0x8, MyCmd.Keycode.BT_HANG },



	};
	@Override
	public int getAngleValue2(byte[] data) {

		int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));
		
		return -angle;
	}

	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0xff) {

		} else if ((data & 0xff) == 0x0) {

		} else if ((data & 0xff) > 0x3f || (data & 0xff) < 0x25) {
			data = (byte) 0xfa;
		} else {
			data = (byte) (37 + (((data & 0xff) - 0x25)));
		}
		return data;
	}
	
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0x90)

				| ((data[2] & 0x20) << 1)
				| ((data[2] & 0x0c) >> 2)
				| ((data[2] & 0x02) << 1)				
				);	

		if ((data[2] & 0x01) == 0){
			airData[0] |=  0x20;
		}
		
		airData[1] = (byte) ((data[3] & 0xff));		
		

		airData[2] = data[4];
		airData[3] = data[5];


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

}
