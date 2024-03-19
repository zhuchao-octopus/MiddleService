package com.my.cartype.raise;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class KeyPannel1 extends Canbox {

	public KeyPannel1() {
		
		buildCmdKey((byte) 0x21, (byte) 5, (byte) 2, (byte) 2, KEYS_WHEEL);
	
	}

	private final static byte KEYS_WHEEL[][] = { 
		{ 0x6, MyCmd.Keycode.VOLUME_UP },
		{ 0x7, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT },
		{ 0x1, MyCmd.Keycode.ROLL_PREV },
		{ 0x4, MyCmd.Keycode.AUDIO },
		{ 0x9, MyCmd.Keycode.HOME },
		{ 0xd, MyCmd.Keycode.NAVIGATION },
		{ 0xa, MyCmd.Keycode.BACK },
		{ 0xe, MyCmd.Keycode.RADIO },

	
	
	
	};

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
	}

	public void setMediaSrc(int source) {
	}

}
