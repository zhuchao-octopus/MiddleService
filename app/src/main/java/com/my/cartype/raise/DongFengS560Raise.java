package com.my.cartype.raise;

import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;

public class DongFengS560Raise extends Canbox {

	public DongFengS560Raise() {
		buildCmdDoor((byte) 0x22, (byte) 0x1, (byte) 0xf8, (byte) 0x02);
//		buildCmdRadarFront((byte) 0x26, (byte) 0x0, (byte) 0xa);
//		buildCmdRadarBack((byte) 0x25, (byte) 0x0, (byte) 0xa);

		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdAC = 0x23;
		mIdKey = 0x21;
		MAP_KEYS = KEYS_WHEEL;

		setVoiceSupportRaise();
	}

	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN }, 
		{ 0x3, MyCmd.Keycode.PREVIOUS },
		{ 0x4, MyCmd.Keycode.NEXT },
		{ 0x5, MyCmd.Keycode.MUTE },
		{ 0x6, MyCmd.Keycode.SPEECH },
		{ 0x7, MyCmd.Keycode.MODLE },

		{ (byte)0x81, MyCmd.Keycode.POWER },
		{ (byte)0x82, MyCmd.Keycode.HOME },
		{ (byte)0x83, MyCmd.Keycode.EQ },
		{ (byte)0x84, MyCmd.Keycode.AS },
		{ (byte)0x85, MyCmd.Keycode.RADIO },
		{ (byte)0x86, MyCmd.Keycode.AUDIO },
		{ (byte)0x87, MyCmd.Keycode.PREVIOUS },
		{ (byte)0x88, MyCmd.Keycode.PLAY_PAUSE },
		{ (byte)0x89, MyCmd.Keycode.NEXT },
		{ (byte)0xf1, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ (byte)0xf2, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ (byte)0xf3, MyCmd.Keycode.ROLL_PREV },
		{ (byte)0xf4, MyCmd.Keycode.ROLL_NEXT },
		 };



	public void parseACInfo(byte[] data) {

		byte[] airData = new byte[8];

		airData[0] = (byte) ((data[2] & 0xe0) );

		switch ((data[3] & 0xff)) {
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
		case 6:
			airData[0] |= (byte) (0x01);
			break;
		default:
			airData[1] = 0;
			break;
		}

		airData[1] |= (byte) (data[4] & 0x0f);

		airData[2] = (byte) (data[5] & 0xff);
		airData[3] = airData[2];
		
		airData[7] = 0x40;
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
	}

	public void setMediaSrc(int source) {
	}





}
