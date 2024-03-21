package com.zhuchao.android.car.cartype.raise;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class VolvoRaise extends Canbox{

	public VolvoRaise(){
		buildCmdDoor((byte) 0x7d, (byte) 0x1, (byte) 0xfc, (byte) 0x03,
				(byte) 0x5);
//		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
//		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
//		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
//		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
//		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdAC = 0x25;
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;
		mIdKey2 = 0x21;
		MAP_KEYS2 = KEYS_WHEEL2;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x7d, 0x7e	};
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x4, MyCmd.Keycode.PREVIOUS },
		{ 0x5, MyCmd.Keycode.BT },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x8, MyCmd.Keycode.SPEECH },
		{ 0x9, MyCmd.Keycode.BT_DIAL },
		{ 0xa, MyCmd.Keycode.BT_HANG },
		{ 0xb, MyCmd.Keycode.PLAY_PAUSE },
		{ 0xc, MyCmd.Keycode.BACK },
		{ 0xd, MyCmd.Keycode.ROLL_PREV },
		{ 0xe, MyCmd.Keycode.ROLL_NEXT },
	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.NUMBER1 },
		{ 0x2, MyCmd.Keycode.NUMBER2 },
		{ 0x3, MyCmd.Keycode.NUMBER3 },
		{ 0x4, MyCmd.Keycode.NUMBER4 },
		{ 0x5, MyCmd.Keycode.NUMBER5 },
		{ 0x6, MyCmd.Keycode.NUMBER6 },
		{ 0x7, MyCmd.Keycode.NUMBER7 },
		{ 0x8, MyCmd.Keycode.NUMBER8 },
		{ 0x9, MyCmd.Keycode.NUMBER9 },
		{ 0xa, MyCmd.Keycode.NUMBER0 },
		{ 0xb, MyCmd.Keycode.NUMBER_POUND },
		{ 0xc, MyCmd.Keycode.NUMBER_STAR },
		{ 0x10, MyCmd.Keycode.RADIO },
		{ 0x11, MyCmd.Keycode.AUDIO },
		{ 0x12, MyCmd.Keycode.BT },
//		{ 0x13, MyCmd.Keycode. },
		{ 0x14, MyCmd.Keycode.PREVIOUS },
		{ 0x15, MyCmd.Keycode.NEXT },
		{ 0x16, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x17, MyCmd.Keycode.BACK },
		{ 0x18, MyCmd.Keycode.POWER },
		{ 0x19, MyCmd.Keycode.EQ },
		{ 0x1a, MyCmd.Keycode.NAVIGATION },
//		{ 0x1b, MyCmd.Keycode },
		{ 0x1c, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x1d, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x1e, MyCmd.Keycode.ROLL_PREV },
		{ 0x1f, MyCmd.Keycode.ROLL_NEXT },
//		{ 0x20, MyCmd.Keycode },
//		{ 0x80, MyCmd.Keycode },
	};


	@Override
	public int getACTemp(byte data, int unit) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0) {
		} else if ((data & 0xff) == 0xfe) {
			data = (byte) 0xff;
		} else {
			if (unit == 0) {
				data = (byte) (32 + ((data & 0xff) - 1));
			}
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0xf0) 
				| ((data[2] & 0x0e) >> 1));				
		


		airData[1] = data[3];
		

		airData[2] = (byte) (data[4] & 0xff);
		airData[3] = (byte) (data[5] & 0xff);

		if ((((data[4] & 0xff) >= 0x3c) && ((data[4] & 0xff) <= 0x57))
				|| (((data[5] & 0xff) >= 0x3c) && ((data[5] & 0xff) <= 0x57))) {
			airData[5] = 0x1;
		}


		airData[4] = (byte) (((data[6] & 0x0c) >> 2) 
				| ((data[6] & 0x03) << 4));		
		
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
		short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
		return t;
	}

}
