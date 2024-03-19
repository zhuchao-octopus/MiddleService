package com.my.cartype.daojun;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class Accord7DaoJun extends Canbox{

	public Accord7DaoJun(){
		buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdOutTemp((byte) 0x1, (byte) 0x10);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdAC = 0x1;
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;	
	}
	
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.BT_HANG },
	};
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0x72, 0x09, 0x4, 0x2, 0xf, 0x1, 0x3, 
				(byte)0xef, (byte)0xf7, (byte)0xc3, (byte)0xfb };
		switch(CarUtil.getModelId()){
		case 55:
			cmd [4] = 0x2;
			cmd [9] = (byte)0xf7;
			break;
		case 56:
			cmd [4] = 0x1;
			cmd [9] = (byte)0xfb;
			break;
		}
		return cmd;
	}
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 0){
			data = (byte)0xfa;
		} else if ((data&0xff) == 0xf){
			data = (byte)0xff;
		} else if ((data&0xff) == 1){
			data = 0;
		} else {
			data = (byte)(38 + (((data&0xff)-2)*2));
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) (((data[3] & 0x40) >> 2) 
				
				| ((data[5] & 0x04) << 3)
				| ((data[5] & 0x08) >> 1)
				| ((data[5] & 0x10) >> 3)
				| ((data[5] & 0x20) >> 5));		
		
		if ((data[5] & 0x03) == 0x02){
			airData[0] |= (byte) (0x40) ;
		}
		
		airData[4] = (byte) (((data[6] & 0x01) << 2));	
		
		switch((data[3] & 0x38)>>3){
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

		airData[1] |= (byte) (data[3] & 0x07);
		

		airData[2] = (byte) (data[4] & 0x0f);
		airData[3] = (byte) ((data[4] & 0xf0)>>4);
		

		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}
	public void setMediaSrc(int source, byte type, byte []b){
	}

	public void setMediaSrc(int source) {
	}
	

	

	
	public int getOutTemp(byte[] data) {//
		if ((data[2] < -40) || (data[2] > 85)){
			return 0;
		}
		return data[2]*10;
	}

}
