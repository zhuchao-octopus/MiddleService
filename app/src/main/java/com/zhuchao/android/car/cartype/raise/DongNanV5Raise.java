package com.zhuchao.android.car.cartype.raise;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class DongNanV5Raise extends Canbox{

	public DongNanV5Raise(){
//		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x4);
		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x4);
//		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
//		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
//		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
		buildCmdVersion((byte) 0x30, (byte) 0x0);

		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;
		mIdKey2 = 0x21;
		MAP_KEYS2 = KEYS_WHEEL2;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte[] IDS_TO_CANBOXSETTING = {0x24, 0x25, 0x32, 0x33,
			0x40, 0x41 };

	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x4, MyCmd.Keycode.NEXT },
		{ 0x3, MyCmd.Keycode.PREVIOUS },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },
		

		{ 0x8, MyCmd.Keycode.SPEECH },
		{ 0x9, MyCmd.Keycode.BT },
		
		{ 0x25, MyCmd.Keycode.MULT_MUTE_AND_POWER },
		{ 0x26, MyCmd.Keycode.SETUP },
		{ 0x21, MyCmd.Keycode.RADIO },
		{ 0x22, MyCmd.Keycode.AUDIO },
		{ 0x23, MyCmd.Keycode.NAVIGATION },
		{ 0x24, MyCmd.Keycode.BT },


		};
	

	private final static byte[][] KEYS_WHEEL2 = {

		{ 0x1, MyCmd.Keycode.HOME },
		{ 0x2, MyCmd.Keycode.NAVIGATION },
		{ 0x3, MyCmd.Keycode.AUDIO },
		{ 0x4, MyCmd.Keycode.NAVIGATION },
		{ 0x5, MyCmd.Keycode.BACK },
		{ 0x6, MyCmd.Keycode.SETUP },
		{ 0x7, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x8, MyCmd.Keycode.ROLL_PREV },
		{ 0x9, MyCmd.Keycode.ROLL_NEXT },
		

		{ 0xe, MyCmd.Keycode.MENU },
		{ 0xf, MyCmd.Keycode.AUDIO },
		{ 0x10, MyCmd.Keycode.BACK },
		{ 0x11, MyCmd.Keycode.SETUP },
		{ 0x12, MyCmd.Keycode.NAVIGATION },
		{ 0x13, MyCmd.Keycode.NAVIGATION },
		{ 0x14, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x16, MyCmd.Keycode.ROLL_PREV },
		{ 0x17, MyCmd.Keycode.ROLL_NEXT },
	};

	
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
