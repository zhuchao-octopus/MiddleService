package com.my.cartype.hiworld;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;

public class IVT001Hiworld extends Canbox {
	public IVT001Hiworld() {
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
		buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);

		buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);
	}


	@Override
	public void stopConnect() {

	}
	private final static byte KEYS_WHEEL[][] = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x3, MyCmd.Keycode.MUTE },
			{ 0x4, MyCmd.Keycode.SPEECH },

			{ 0x5, MyCmd.Keycode.BT },

			

			{ 0x9, MyCmd.Keycode.PREVIOUS },
			{ 0x8, MyCmd.Keycode.NEXT },

			{ 0xb, MyCmd.Keycode.MODLE },

	};
	@Override
	public int getAngleValue2(byte[] data) {
		int angle = (short) (((data[8] & 0xff) << 8) | (data[9] & 0xff));
		return -angle;
	}
	
	public void startConnect() {
		byte[] buf = new byte[] { 0x3, (byte) 0x6a, 0x5, 1, (byte) 0xf0 };

		sendDataToCanbox(buf, buf.length);
	}
	
}
