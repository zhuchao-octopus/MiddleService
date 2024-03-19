package com.my.cartype.raise;

import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class HaiMaM8Raise extends Canbox{

	public HaiMaM8Raise(){
		mIdAC = 0x21;
		buildCmdDoor((byte) 0x24, (byte) 0x0, (byte) 0x1f, (byte) 0x3);
		buildCmdRadarBack((byte) 0x22, (byte) 0x1, (byte)3);
		buildCmdAngle((byte) 0x26, (byte) 0x4, 0x1800);
		buildCmdOutTemp((byte) 0x27, (byte) 0x0);
		buildCmdVersion((byte) 0xff, (byte) 0x0);
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;		
		
	}
	

	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D },

			{ 0x3, KEY_NEXTSONG },
			{ 0x4, KEY_PREVIOUSSONG }, 

			{ 0x5, MyCmd.Keycode.BT }, 
			{ 0x6, MyCmd.Keycode.MUTE },
			{ 0x7, KEY_SOURCE },
	};
	

@Override
public void startConnect() {
	// TODO Auto-generated method stub
	super.startConnect();

	byte[] data = new byte[] { (byte) 0xf1, 0x1, 1 };

	sendDataToCanbox(data, data.length);
}
	
	public int getAngleValue(byte[] data) {

		int angle;
		int max;

		angle = ((data[3] & 0xff) | (((data[2] & 0x7f)) << 8));
		if ((data[2] & 0x80) != 0){
			angle = -angle;
		}
		max = 0x21c;

		angle = ((angle * 3000) / max);
		if (angle > -50 && angle < 50) {
			angle = 50;
		}

		return angle;
	}



	@Override
	public int getOutTemp(byte[] data) {
		// TODO Auto-generated method stub
		int t = data[2] & 0x7f;
		if ((data[2] & 0x80) != 0) {
			t = -t;
		}
		return t*10;
	}

	@Override
	public int getACTemp(byte data) {
		if ((data & 0xff) == 0x1f) {
			data = (byte) 0xff;
		} else {
			data = (byte) (36 + (data - 0x1));

		}
		return data & 0xff;
	}


	public void parseACInfo(byte[] data)
	{			

		byte[]	airData = new byte[8];
		airData[0] = (byte) ((data[2] & 0xf7));
		airData[1] = (byte) ((data[3] & 0x0f));
		airData[2] = data[4];
		airData[3] = data[5];
		
		switch((data[3] & 0xf0)>>4){
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
		default:
			airData[1] = 0;
			break;
		}

		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}
	

	
	public void setMediaMoreInfo(int source, int play, int total, int time, int total_time){
		
	}
	public void setMediaSrc(int source, byte type, byte []b){
		
	}
	public void setMediaSrc(int source){//default is simple box
		
	}
	
	


}
