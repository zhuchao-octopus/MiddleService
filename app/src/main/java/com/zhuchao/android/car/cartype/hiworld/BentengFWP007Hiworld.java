package com.zhuchao.android.car.cartype.hiworld;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

public class BentengFWP007Hiworld extends Canbox {

	public BentengFWP007Hiworld() {
		buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
		buildCmdAngle((byte) 0x11, (byte) 0x0, 10000);
		buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x3);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);
		mIdAC = 0x31;

		buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 2, KEYS_WHEEL);
		buildCmdKey((byte) 0x21, (byte) 0, (byte) 2, (byte) 0, KEYS_WHEEL2);

		mIdKey3 = 0x021122;
		MAP_KEYS3 = KEYS_WHEEL3;
		
	}


	@Override
	public void stopConnect() {

	}
	private final static byte[][] KEYS_WHEEL = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x3, MyCmd.Keycode.MUTE },
		

			{ 0x8, MyCmd.Keycode.PREVIOUS },
			{ 0x9, MyCmd.Keycode.NEXT },

			{ 0xa, MyCmd.Keycode.MODLE },

	};
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x2, MyCmd.Keycode.PREVIOUS },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x9, MyCmd.Keycode.MUTE },
		{ 0xa, MyCmd.Keycode.NUMBER1 },
		{ 0xb, MyCmd.Keycode.NUMBER2 },
		{ 0xc, MyCmd.Keycode.NUMBER3 },
		{ 0xd, MyCmd.Keycode.NUMBER4 },
		{ 0xe, MyCmd.Keycode.NUMBER5 },
		{ 0xf, MyCmd.Keycode.NUMBER6 },
		{ 0x15, MyCmd.Keycode.AS },
		{ 0x16, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x25, MyCmd.Keycode.NAVIGATION },
		{ 0x33, MyCmd.Keycode.RADIO },
		{ 0x34, MyCmd.Keycode.BT_DIAL },
		{ 0x35, MyCmd.Keycode.BT_HANG },
		{ 0x3c, MyCmd.Keycode.KEY_SHUFFLE },
		{ 0x3d, MyCmd.Keycode.AUDIO },
	};

	
	private final static byte[][] KEYS_WHEEL3 = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x2, (byte) 0x24, 0x1, 0x0 };
	
		return cmd;
	}

	@Override
	public int getAngleValue2(byte[] data) {
		int angle = (short) (((data[8] & 0xff) << 8) | (data[9] & 0xff));
		return -angle;
	}
	
//	@Override
//	public int getACTemp(byte data) {
//		// TODO Auto-generated method stub
//		if ((data & 0xff) == 0xff || (data & 0xff) == 0xfa) {
//
//		} else if ((data & 0xff) == 0xfe) {
//			data = 0;
//		} else {
//
//		}
//		return data;
//	}

	public void parseACInfo(byte[] data) {

		byte[] airData = new byte[10];

		airData[0] = (byte) (((data[2] & 0x40) << 1)
				| ((data[2] & 0x08) << 0) 
				| ((data[3] & 0x10) << 1) 
				| ((data[3] & 0x40) << 0) 
				| ((data[4] & 0x20) >> 5) | 
				((data[4] & 0x10) >> 3));

//		if (((data[3] & 0x10) == 0)) {
//			airData[0] |= 0x20;
//		}

		airData[4] = (byte) (((data[3] & 0x1) << 7));
		airData[7] = (byte) 0x40;

		switch ((data[6] & 0xff)) {
		case 1:
			airData[9] = (byte) (0x1);
			break;
		case 2:
			// airData[1] = (byte) (0x20);
			break;
		case 3:
			airData[1] = (byte) (0x20);
			break;
		case 5:
			airData[1] = (byte) (0x60);
			break;
		case 6:
			airData[1] = (byte) (0x40);
			break;
		case 0xb:
			airData[1] = (byte) (0x80);
			break;
		case 0xc:
			airData[1] = (byte) (0xa0);
			break;
		case 0xd:
			airData[1] = (byte) (0xc0);
			break;
		case 0xe:
			airData[1] = (byte) (0xe0);
			break;
		}

		airData[1] |= (byte) (data[7] & 0x0f);

		airData[2] = data[8];
		airData[3] = (byte)0xfa;

//		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

	}

	public void setMediaSrc(int source, byte type, byte[] b) {

	}

	public void setMediaSrc(int source) {

	}

	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword1(data, len);
	}

	public void startConnect() {
		byte[] buf = new byte[] { 0x3, (byte) 0x6a, 0x5, 1, (byte) 0xf0 };

		sendDataToCanbox(buf, buf.length);
	}
	
}
