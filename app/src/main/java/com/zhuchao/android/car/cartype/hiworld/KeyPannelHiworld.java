package com.zhuchao.android.car.cartype.hiworld;


import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

public class KeyPannelHiworld extends Canbox {

	public KeyPannelHiworld() {
	

		buildCmdKey((byte) 0x21, (byte) 0, (byte) 2, (byte) 0, KEYS_WHEEL2);


		mIdKey3 = 0x021122;
		MAP_KEYS3 = KEYS_WHEEL3;
		
	}
	@Override
	public void stopConnect() {

	}
	private final static byte[][] KEYS_WHEEL2 = {
			{ 0x1, MyCmd.Keycode.POWER },

			{ 0x6, MyCmd.Keycode.BACK },

			{ 0x9, MyCmd.Keycode.MUTE },
			

			{ 0x10, MyCmd.Keycode.PLAY_PAUSE },
			{ 0x20, MyCmd.Keycode.NAVIGATION },
			{ 0x24, MyCmd.Keycode.AUDIO },
			{ 0x2b, MyCmd.Keycode.HOME },
			{ 0x30, MyCmd.Keycode.BT },
			
			{ 0x33, MyCmd.Keycode.RADIO },
			{ 0x34, MyCmd.Keycode.BT_DIAL },
			{ 0x35, MyCmd.Keycode.BT_HANG },
			{ 0x39, MyCmd.Keycode.KEY_DISPLAY },
			{ 0x3b, MyCmd.Keycode.MODLE },
			
			{ 0x42, MyCmd.Keycode.EQ },
			{ 0x4b, MyCmd.Keycode.RADIO },
			{ 0x5f, MyCmd.Keycode.SPEECH },

	};


	
	private final static byte[][] KEYS_WHEEL3 = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
	};
	

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

	}

	public void setMediaSrc(int source, byte type, byte[] b) {

	}

	public void setMediaSrc(int source) {

	}


	public void startConnect() {
	}
	
}
