package com.zhuchao.android.car.cartype.simple;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

public class IvecoSimple extends Canbox {

	public IvecoSimple() {
		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;
	}

	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x4, MyCmd.Keycode.PREVIOUS },
		{ 0x5, MyCmd.Keycode.BT },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x8, MyCmd.Keycode.SPEECH },
		
	};

	@Override
	public int getAngleValue(byte[] data) {

		int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

		angle = ((angle * 3000) / 540);

		if (angle > -50 && angle < 50) {
			angle = 50;
		}
		
		return angle;

	}

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
	}

	public void setMediaSrc(int source) {
	}

	public int getOutTemp(byte[] data) {//
		short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
		return t;
	}

}
