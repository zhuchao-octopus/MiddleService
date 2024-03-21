package com.zhuchao.android.car.cartype.raise;

import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

public class DongFengFengShenAX7Raise extends Canbox {

	public DongFengFengShenAX7Raise() {
		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
//		buildCmdRadarFront((byte) 0x26, (byte) 0x0, (byte) 0xa);
//		buildCmdRadarBack((byte) 0x25, (byte) 0x0, (byte) 0xa);
		buildCmdAngle((byte) 0x30, (byte) 0x0, 0x2198);
		buildCmdOutTemp((byte) 0x36, (byte) 0x1);
		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdAC = 0x23;
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;
		mIdKey2 = 0x21;
		MAP_KEYS2 = KEYS_WHEEL2;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;

		setVoiceSupportRaise();
//		mRadarBack2 = buildCmdRadar((byte) 0x24, (byte) 0x0, (byte) 255,
//				(byte) 2);
	}

//	private int mRadarBack2;
	private final static byte[] IDS_TO_CANBOXSETTING = { (byte) 0x29, 0x27 };

	private final static byte[][] KEYS_WHEEL = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN }, 
			{ 0x6, MyCmd.Keycode.MUTE },
			{ 0x7, MyCmd.Keycode.MODLE },
			{ 0x8, MyCmd.Keycode.BACK },
			{ 0xb, MyCmd.Keycode.MULT_PREV_AND_RECEIVE },
			{ 0xc, MyCmd.Keycode.MULT_NEXT_AND_HANG },
			{ 0xd, MyCmd.Keycode.SPEECH },
			 };

	private final static byte[][] KEYS_WHEEL2 = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN }, 
			{ 0x6, MyCmd.Keycode.MUTE }, 
			{ 0x7, MyCmd.Keycode.MODLE },
			{ 0x9, MyCmd.Keycode.BT_DIAL },
			{ 0xa, MyCmd.Keycode.BT_HANG }, 
			{ 0x20, MyCmd.Keycode.KEY_RADIO_SCAN }, 
			{ 0x21, MyCmd.Keycode.RADIO }, 
			{ 0x22, MyCmd.Keycode.AS }, 
			{ 0x23, MyCmd.Keycode.PREVIOUS }, 
			{ 0x24, MyCmd.Keycode.NEXT }, 
			{ 0x2b, MyCmd.Keycode.VOLUME_ROLL_UP }, 
			{ 0x2c, MyCmd.Keycode.VOLUME_ROLL_DOWN }, 
			{ 0x2d, MyCmd.Keycode.POWER }, 
			{ 0x2f, MyCmd.Keycode.EQ }, 
			{ 0x30, MyCmd.Keycode.SETUP }, 
			{ 0x32, MyCmd.Keycode.EASY_CONNECT }, 
			{ 0x33, MyCmd.Keycode.AUDIO }, 
			{ 0x34, MyCmd.Keycode.BACK }, 
			{ 0x35, MyCmd.Keycode.HOME }, 
			{ 0x36, MyCmd.Keycode.NAVIGATION }, 
			{ 0x37, MyCmd.Keycode.NAVIGATION }, 
			{ 0x38, MyCmd.Keycode.NAVIGATION }, 
			{ 0x39, MyCmd.Keycode.KEY_DISPLAY }, 
			
	};

	private byte[] getCarTypeCmd() {
		if (CarUtil.getCatelId() == 25) {
			byte[] cmd = new byte[] { (byte) 0x85, 0x01, 0 };
			switch (CarUtil.getModelId()) {
			case 2:
				if (CarUtil.getCarTypeConfig() == 1) {
					cmd[2] = 8;
				}
				break;
			case 29:
				if (CarUtil.getCarTypeConfig() == 0) {
					cmd[2] = 9;
				}
				break;
			case 11:
				cmd[2] = 1;
				break;
			case 12:
				cmd[2] = 2;
				break;
			}
			return cmd;
		}
		return null;
	}

	@Override
	public int getAngleValue(byte[] data) {

		int angle = ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));

		int max = (0x2ad0 - 0x1568);
		if (CarUtil.getCatelId() == 25 && CarUtil.getModelId() == 10) {
			max = (0x35f6 - 0x1e8a);
			angle = angle - 0x1e8a;
		} else {
			angle = angle - 0x1568;
		}
		
		angle = ((angle * 3000) / max);
		if (angle > -50 && angle < 50) {
			angle = 50;
		}

		return angle;

	}

	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0x40) {
			data = (byte) 0;
		} else if ((data & 0xff) == 0x60) {
			data = (byte) 0xff;
		} else if ((data & 0xff) >= 0x41 && (data & 0xff) <= 0x5f) {
			data = (byte) (33 + ((data & 0xff) - 0x41));
		} else {
			data = (byte) 0xfa;
		}
		return data;
	}

	public void parseACInfo(byte[] data) {

		byte[] airData = new byte[8];

		airData[0] = (byte) ((data[2] & 0xe8) 
				| ((data[2] & 0x02) >> 1) );

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
		default:
			airData[1] = 0;
			break;
		}

		airData[1] |= (byte) (data[4] & 0x0f);

		airData[2] = (byte) (data[5] & 0xff);
		airData[3] = airData[2];
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

//	@Override
//	public void parseCanboxData(byte[] data, int len) {
//		// TODO Auto-generated method stub
//		switch (data[0]) {
//		case 0x24:
//			parseRadarBack(mRadarBack2, data);
//			break;
//		default:
//			super.parseCanboxData(data, len);
//
//		}
//	}





}
