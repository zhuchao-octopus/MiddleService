package com.zhuchao.android.car.cartype.raise;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class QiChengRaise extends Canbox{

	public QiChengRaise(){
		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdAC = 0x21;
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;	
	}
	
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x4, MyCmd.Keycode.PREVIOUS },
		{ 0x5, MyCmd.Keycode.BT_DIAL },
		{ 0x6, MyCmd.Keycode.BT_HANG },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x8, MyCmd.Keycode.SPEECH },
		{ 0xa, MyCmd.Keycode.KEY_360 },
		{ 0xb, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x12, MyCmd.Keycode.POWER },
		{ 0x13, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x14, MyCmd.Keycode.ROLL_NEXT },
		{ 0x15, MyCmd.Keycode.ROLL_PREV },
		{ 0x20, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x21, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x22, MyCmd.Keycode.BACK },
		{ 0x23, MyCmd.Keycode.MENU },
		{ 0x24, MyCmd.Keycode.KEY_DISPLAY},
		{ 0x25, MyCmd.Keycode.AUDIO },
		{ 0x26, MyCmd.Keycode.HOME },
		{ 0x27, MyCmd.Keycode.NAVIGATION },
		{ 0x28, MyCmd.Keycode.MUTE },
		{ 0x29, MyCmd.Keycode.SETUP },
		{ 0x2a, MyCmd.Keycode.KEY_RADIO_SCAN },
		{ 0x2b, MyCmd.Keycode.RADIO },
		{ 0x2c, MyCmd.Keycode.EQ },
	};
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0xee, 0x02, 0x1, 0 };
		switch(CarUtil.getModelId()){
		case 2:
			cmd [3] = 0;
			break;
		case 3:
			cmd [3] = 1;
			break;
		case 4:
			cmd [3] = 2;
			break;
		case 7:
			cmd [3] = 3;
			break;
		case 8:
			cmd [3] = 4;
			break;
		default:
			return null;
		}
		return cmd;
	}
	

	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0x44) 
				| ((data[2] & 0x10) << 1)
				| ((data[2] & 0x02) >> 1));				
		
		switch((data[3] & 0xff)){
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

		airData[1] |= (byte) (data[4] & 0x0f);
		

		airData[2] = (byte) (data[5] & 0xff);
		airData[3] = (byte) (data[6] & 0xff);

		airData[4] = (byte) (((data[2] & 0x80) >> 5));	
		airData[7] = 0x40;
		
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
