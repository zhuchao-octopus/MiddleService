package com.zhuchao.android.car.cartype.hiworld;

import java.util.Locale;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class ShangQiSAP001Hiworld extends Canbox{

	public ShangQiSAP001Hiworld(){
		buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
		buildCmdDoor((byte) 0x73, (byte) 0x2, (byte) 0xf8, (byte) 0x09);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);		
		
		buildCmdKey((byte) 0x72, (byte) 1, (byte) 4, (byte) 0, KEYS_WHEEL);

	}



	@Override
	public void stopConnect() {

	}
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },

		{ 0x3, MyCmd.Keycode.MUTE },
		{ 0x4, MyCmd.Keycode.SPEECH },

		{ 0x5, MyCmd.Keycode.BT_DIAL },

		{ 0x6, MyCmd.Keycode.BT_HANG },
		
		{ 0xa, MyCmd.Keycode.MODLE },
		{ 0x9, MyCmd.Keycode.NEXT },
		{ 0x8, MyCmd.Keycode.PREVIOUS },

		{ 0x10, MyCmd.Keycode.BACK },
		{ 0xf, MyCmd.Keycode.SPEECH },
		{ 0x17, MyCmd.Keycode.NAVIGATION },
	};


	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x2, (byte) 0x24, 0x0, 0xd };
		switch(CarUtil.getModelId()){
		case 13:
			cmd[2] = 2;
			break;
		case 14:
			cmd[2] = 4;
			break;
		default:
			return null;
		}
		return cmd;
	}
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

	}
	public void setMediaSrc(int source, byte type, byte []b){
		
	}

	public void setMediaSrc(int source) {

	}		

	



	
	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword2(data, len);
	}public void startConnect() {
		
	}
}
