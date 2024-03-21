package com.zhuchao.android.car.cartype.raise;

import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class HaiMaFuLaiMeiRaise extends Canbox{

	public HaiMaFuLaiMeiRaise(){
		mIdAC = 0x23;
		buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
		buildCmdRadarBack((byte) 0x24, (byte) 0x0, (byte)3);
		buildCmdAngle((byte) 0x30, (byte) 0x4, 0x1800);
		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdKey = 0x21;
		MAP_KEYS = KEYS_WHEEL;	
		mIdKey2 = 0x22;
		MAP_KEYS2 = KEYS_WHEEL2;		

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x31, 0x40 };

	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D },

			{ 0x4, KEY_NEXTSONG },
			{ 0x3, KEY_PREVIOUSSONG }, 

			{ 0x6, MyCmd.Keycode.MUTE }, 
			{ 0x7, MyCmd.Keycode.MODLE }, 
			{ 0x8, MyCmd.Keycode.BT }, 
			{ 0x9, MyCmd.Keycode.BT_DIAL }, 
			{ 0xa, MyCmd.Keycode.BT_HANG }, 
			{ 0xb, MyCmd.Keycode.SPEECH }, 
			{ 0xc, MyCmd.Keycode.BACK }, 
			{ 0xd, MyCmd.Keycode.POWER }, 
			{ 0xe, MyCmd.Keycode.HOME }, 
	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x2, MyCmd.Keycode.PREVIOUS },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x4, MyCmd.Keycode.SETUP },
		{ 0x5, MyCmd.Keycode.EQ },
		{ 0x6, MyCmd.Keycode.BACK },
		{ 0x7, MyCmd.Keycode.RADIO },
		{ 0x8, MyCmd.Keycode.DVD },
		{ 0x9, MyCmd.Keycode.MUTE },
		{ 0xa, MyCmd.Keycode.NUMBER1 },
		{ 0xb, MyCmd.Keycode.NUMBER2 },
		{ 0xc, MyCmd.Keycode.NUMBER3 },
		{ 0xd, MyCmd.Keycode.NUMBER4 },
		{ 0xe, MyCmd.Keycode.NUMBER5 },
		{ 0xf, MyCmd.Keycode.NUMBER6 },
		{ 0x10, MyCmd.Keycode.NUMBER7 },
		{ 0x11, MyCmd.Keycode.NUMBER8 },
		{ 0x12, MyCmd.Keycode.NUMBER9 },
		{ 0x13, MyCmd.Keycode.NUMBER0 },
		{ 0x14, MyCmd.Keycode.DVD },
		{ 0x15, MyCmd.Keycode.EJECT },
		{ 0x16, MyCmd.Keycode.SETUP },
		{ 0x17, MyCmd.Keycode.TIME_SETTING },
		{ 0x18, MyCmd.Keycode.RADIO },
		{ 0x19, MyCmd.Keycode.AS },
		{ 0x20, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x21, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x22, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x23, MyCmd.Keycode.KEY_SEEK_NEXT},
		{ 0x24, MyCmd.Keycode.KEY_SEEK_PREV },
		{ 0x25, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x26, MyCmd.Keycode.PREVIOUS },
		{ 0x27, MyCmd.Keycode.NEXT },
		{ 0x28, MyCmd.Keycode.AUX_IN },
		{ 0x29, MyCmd.Keycode.ROLL_NEXT },
		{ 0x30, MyCmd.Keycode.ROLL_PREV },
		{ 0x31, MyCmd.Keycode.MODLE },
		{ 0x32, MyCmd.Keycode.BT_DIAL },
		{ 0x33, MyCmd.Keycode.BT_HANG },
		{ 0x36, MyCmd.Keycode.HOME },
		{ 0x34, MyCmd.Keycode.AS },
		{ 0x35, MyCmd.Keycode.SETUP },
		{ 0x37, MyCmd.Keycode.EASY_CONNECT },
	};




	
	public int getAngleValue(byte[] data) {

		int angle;
		int max;

		angle = ((data[2] & 0xff) | (((data[3] & 0x7f)) << 8));
		if ((data[3] & 0x80) == 0){
			angle = -angle;
		}
		max = 0x1800;

		angle = ((angle * 3000) / max);
		if (angle > -50 && angle < 50) {
			angle = 50;
		}

		return angle;
	}



	private int getACPrivTemp(byte data) {
		if ((data & 0xff) == 0x1e) {
			data = (byte) 0xff;
		} else if ((data & 0xff) == 0) {
			data = (byte) 0;
		} else {
			data = (byte)(35 + (data));
		}
		return data & 0xff;
	}


	public void parseACInfo(byte[] data)
	{			

		byte[]	airData = new byte[8];
		airData[0] = (byte) ((data[2] & 0xe8) | ((data[2] & 0x02) >> 1) | ((data[2] & 0x01) << 1));

		
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

		airData[1] |= (byte) (((data[4] & 0xff)));
		if (CarUtil.getModelId() == 9||CarUtil.getManaId() == 75){
			airData[2] = (byte) getACPrivTemp(data[6]);
		} else {
			airData[7] |= 0x40;
			airData[2] = (byte) ((data[5] & 0xff));
		}
		airData[3] = (byte)0xfa;
//		airData[4] = (byte) (((data[8] & 0x80)>>4));
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
