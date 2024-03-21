package com.zhuchao.android.car.cartype.daojun;

import java.util.Calendar;
import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class JinBeiDaoJun extends Canbox{

	public JinBeiDaoJun(){

		buildCmdVersion((byte) 0x30, (byte) 0x0);
		
		mIdAC = 0x21;
	}	

	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[10];
		

		airData[0] = data[2];
		airData[1] = data[3];
		airData[2] = data[4];
		airData[3] = (byte)0xfa;
		

		airData[6] = data[8];

		airData[7] |= 0x40;

	

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
