package com.my.cartype.luzheng;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class SubaruHaoZheng extends Canbox{

	public SubaruHaoZheng(){
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

		buildCmdEQ((byte) 0x31, (byte) 0x0, 6);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdAC = 0x28;
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;	

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = {0x35,0x38,0x62};
	
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x8, MyCmd.Keycode.SPEECH },
		{ 0x9, MyCmd.Keycode.BT_DIAL },
		{ 0xa, MyCmd.Keycode.BT_HANG },
		
		{ 0x13, MyCmd.Keycode.KEY_SEEK_NEXT },
		{ 0x14, MyCmd.Keycode.KEY_SEEK_PREV },
		{ 0x15, MyCmd.Keycode.NAVIGATION },
		{ 0x16, MyCmd.Keycode.NAVIGATION },
		{ 0x17, MyCmd.Keycode.AUDIO },
		{ 0x18, MyCmd.Keycode.HOME },		

		{ (byte)0x87, MyCmd.Keycode.MUTE },
	};


	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 0x7f){
			data = (byte)0xfa;
		} else {
			//data = 
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
				

		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time, int total_time){
		
	}
	public void setMediaSrc(int source, byte type, byte []b){
		
	}

	public void setMediaSrc(int source) {// default is simple box
		
	}
	


}
