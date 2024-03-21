package com.zhuchao.android.car.cartype.daojun;

import android.util.Log;

import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class Sorento13DaoJun extends Canbox{

	public Sorento13DaoJun(){
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdAC = 0x21;	
	}
	
	


	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0) {

		} else if ((data & 0xff) == 0x1e) {
			data = (byte) 0xff;
		} else {
			data = (byte) (34 + (data & 0xff));
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];



		airData[0] = data[2];	
		airData[1] = data[3];	
		airData[2] = data[4];	
		airData[3] = data[5];	
		
		airData[4] = (byte) (
				((data[6] & 0xf8)));	
		
		

	
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
	
	public void setVolume(int volume) {

		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
		sendDataToCanbox(data, data.length);
	}
}
