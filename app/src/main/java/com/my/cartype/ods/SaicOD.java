package com.my.cartype.ods;

import com.common.util.MyCmd;
import com.my.canbox.Canbox;

public class SaicOD extends Canbox {

	public SaicOD() {

		buildCmdVersion((byte) 0x7f, (byte) 0x0);		

		buildCmdKey((byte)0x21, KEYS_WHEEL, 0);	
	}

	
	private final static byte KEYS_WHEEL[][] = {

		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x4, MyCmd.Keycode.NEXT },
		{ 0x3, MyCmd.Keycode.PREVIOUS },
		
		{ 0x6, MyCmd.Keycode.MULT_MUTE_AND_HANG },
		{ 0x7, MyCmd.Keycode.MULT_MODE_AND_RECV },

		{ 0x10, MyCmd.Keycode.HOME },
		{ 0x11, MyCmd.Keycode.BACK },
		{ 0x12, MyCmd.Keycode.AUDIO },
		{ 0x13, MyCmd.Keycode.MUTE },
		{ 0x14, MyCmd.Keycode.PREVIOUS },
		{ 0x15, MyCmd.Keycode.NEXT },
		{ 0x16, MyCmd.Keycode.RADIO },
		{ 0x17, MyCmd.Keycode.POWER },
		{ 0x18, MyCmd.Keycode.NAVIGATION },
		{ 0x19, MyCmd.Keycode.AUDIO },
		{ 0x1a, MyCmd.Keycode.BT },
		{ 0x1b, MyCmd.Keycode.SETUP },
		{ 0x1c, MyCmd.Keycode.EQ },
		{ 0x1d, MyCmd.Keycode.AUX_IN },
		{ 0x1e, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x1f, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x22, MyCmd.Keycode.ROLL_PREV },
		{ 0x23, MyCmd.Keycode.ROLL_NEXT },		



	};

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}

	public void setMediaSrc(int source, byte type, byte[] b) {

	}

	public void setMediaSrc(int source) {

	}
}
