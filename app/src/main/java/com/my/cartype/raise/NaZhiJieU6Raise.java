package com.my.cartype.raise;

import com.common.util.MyCmd;
import com.my.canbox.Canbox;

public class NaZhiJieU6Raise extends Canbox {

	public NaZhiJieU6Raise() {
		mSupportRaise0x7d = true;
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
	}

	private final static byte IDS_TO_CANBOXSETTING[] = { 0x53, 0x25 };


	private final static byte KEYS_WHEEL[][] = { 
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D },
			{ 0x3, KEY_NEXTSONG },
			{ 0x4, KEY_PREVIOUSSONG }, 
			{ 0x5, KEY_MUTE },
			{ 0x6, KEY_SOURCE }, 			

			{ 0x7, MyCmd.Keycode.BT_DIAL },
			{ 0x8, MyCmd.Keycode.BT_HANG }, 
			{ 0x9, KEY_MIC },
	};
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
	}

	public void setMediaSrc(int source) {
	}

}
