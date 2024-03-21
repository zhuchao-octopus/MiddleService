package com.zhuchao.android.car.cartype.raise;

import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class YueXiangV7 extends Canbox{

	public YueXiangV7(){
		mIdAC = 0x23;
		buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
		buildCmdAngle((byte) 0x30, (byte) 0x4, 5400);
		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdKey = 0x21;
		MAP_KEYS = KEYS_WHEEL;	
		mIdKey2 = 0x22;
		MAP_KEYS2 = KEYS_WHEEL2;			

	}
	

	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D },


			{ 0x6, MyCmd.Keycode.MUTE },
			{ 0x7, KEY_SOURCE },
			
			{ 0xC, KEY_NEXTSONG },
			{ 0xB, KEY_PREVIOUSSONG }, 
	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x5, MyCmd.Keycode.EQ },
		{ 0x21, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x22, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x29, MyCmd.Keycode.PREVIOUS },
		{ 0x30, MyCmd.Keycode.NEXT },
		{ 0x35, MyCmd.Keycode.SETUP },
		{ 0x36, MyCmd.Keycode.NAVIGATION },
		{ 0x37, MyCmd.Keycode.AUDIO },
		{ 0x38, MyCmd.Keycode.KEY_AM },
		{ 0x39, MyCmd.Keycode.KEY_FM },
		{ 0x40, MyCmd.Keycode.BACKLIGHT_OFF },
		{ 0x41, MyCmd.Keycode.AS },
//		{ 0x42, MyCmd.Keycode.KEY_TURN_A },
		{ 0x43, MyCmd.Keycode.ROLL_NEXT },
		{ 0x44, MyCmd.Keycode.ROLL_PREV },
		{ 0x45, MyCmd.Keycode.BT },
		{ 0x46, MyCmd.Keycode.HOME },
		{ 0x47, MyCmd.Keycode.EASY_CONNECT },
	};







	public void parseACInfo(byte[] data) {

		byte[] airData = new byte[8];
		airData[0] = data[2];
		switch ((data[3] & 0xf) >> 0) {
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
		super.parseACInfo(airData);
	}
	

	
	public void setMediaMoreInfo(int source, int play, int total, int time, int total_time){

	}
	public void setMediaSrc(int source, byte type, byte []b){
		
	}
	public void setMediaSrc(int source){//default is simple box
		
	}
	



}
