package com.zhuchao.android.car.cartype.daojun;

import java.util.Calendar;
import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class BydF6DaoJun extends Canbox{

	public BydF6DaoJun(){
	
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		
		mIdAC = 0x1;
	}
	
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if (((data & 0xff)) == 0) {
			data = (byte) 0xfa;
		} else if (((data & 0xff)) == 0xf) {
			data = (byte) 0xff;
		} else if (((data & 0xff)) == 1) {
			data = 0;
		} else {
			data = (byte) (38 + (data - 2) * 2);

		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[10];
		
		if ((data[3] & 0x80) == 0) {
			switch ((data[3] & 0x38) >> 3) {
			case 1:
				airData[1] = (byte) (0x40);
				break;
			case 2:
				airData[1] = (byte) (0x60);
				break;
			case 3:
				airData[1] = (byte) (0x20);
				break;
			case 4:
				airData[1] = (byte) (0xa0);
				break;
			case 5:
				airData[1] = (byte) (0x80);
				break;
			default:
				airData[1] = 0;
				break;
			}
		} else {
			airData[9] = 1;
		}

		airData[1] |= (byte) ((data[3] & 0x07)
				| ((data[5] & 0x40)>>2));
		


		airData[0] = (byte) (((data[3] & 0x40) >> 2) 					
				| ((data[5] & 0x04) << 3)
				| ((data[5] & 0x10) >> 3)
				| ((data[5] & 0x20) >> 5)
				| ((data[5] & 0x08) >> 1));		
		
		if (((data[5] & 0x03)) == 0x02) {
			airData[0] |= (byte) (0x40);
		} else if (((data[5] & 0x03)) == 0x03) {
			airData[0] |= (byte) (0x40);
			airData[0] |= (byte) (0x10);
		}
		

		airData[0] |= 0x80;

		airData[2] = (byte) (((data[4] & 0x0f)));	
		airData[3] = (byte) (((data[4] & 0xf0) >> 4));	


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
