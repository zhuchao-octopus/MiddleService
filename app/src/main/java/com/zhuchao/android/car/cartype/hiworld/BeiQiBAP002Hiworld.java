package com.zhuchao.android.car.cartype.hiworld;


import com.common.util.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

public class BeiQiBAP002Hiworld extends Canbox {

	public BeiQiBAP002Hiworld() {
		buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
		buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
		buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x6);
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
	private final static byte[][] KEYS_WHEEL2 = {
			{ 0x1, MyCmd.Keycode.POWER },
			{ 0x2, MyCmd.Keycode.PREVIOUS },
			{ 0x3, MyCmd.Keycode.NEXT },
			{ 0x6, MyCmd.Keycode.BACK },

			{ 0x9, MyCmd.Keycode.MUTE },
			{ 0x12, MyCmd.Keycode.SETUP },
			{ 0x16, MyCmd.Keycode.PREVIOUS },
			{ 0x17, MyCmd.Keycode.NEXT },
			{ 0x18, MyCmd.Keycode.PREVIOUS },
			{ 0x19, MyCmd.Keycode.NEXT},
			{ 0x1a, MyCmd.Keycode.PREVIOUS },
			{ 0x1b, MyCmd.Keycode.NEXT},
			{ 0x1c, MyCmd.Keycode.PREVIOUS },
			{ 0x1d, MyCmd.Keycode.NEXT },
			{ 0x1e, MyCmd.Keycode.NEXT },
			{ 0x20, MyCmd.Keycode.NAVIGATION },
			
			{ 0x24, MyCmd.Keycode.AUDIO },


			{ 0x2b, MyCmd.Keycode.HOME },
			{ 0x2f, MyCmd.Keycode.MENU },

			{ 0x33, MyCmd.Keycode.RADIO },
			{ 0x3e, MyCmd.Keycode.EASY_CONNECT },
			{ 0x45, MyCmd.Keycode.VOLUME_UP },
			{ 0x46, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x4c, MyCmd.Keycode.BT },
	};


	private final static byte[][] KEYS_WHEEL = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x3, MyCmd.Keycode.MUTE },

			{ 0x5, MyCmd.Keycode.BT_DIAL },

			{ 0x6, MyCmd.Keycode.BT_HANG },

			{ 0x8, MyCmd.Keycode.PREVIOUS },
			{ 0x9, MyCmd.Keycode.NEXT },

			{ 0xb, MyCmd.Keycode.MODLE },

	};
	
	private final static byte[][] KEYS_WHEEL3 = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x2, (byte) 0x24, 0x0, 0xc };
		switch (CarUtil.getModelId()) {
		case 14:
			cmd[2] = 5;
			break;
		case 11:
			cmd[2] = 6;
			break;
		case 19:
			cmd[2] = 7;
			break;
		case 16:
			cmd[2] = 8;
			break;
		}
		return cmd;
	}

	@Override
	public int getAngleValue2(byte[] data) {
		int angle = (short) (((data[9] & 0xff) << 8) | (data[8] & 0xff));
		return -angle;
	}
	
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0xff || (data & 0xff) == 0xfa) {

		} else if ((data & 0xff) == 0xfe) {
			data = 0;
		} else {

		}
		return data;
	}

	public void parseACInfo(byte[] data) {

		byte[] airData = new byte[10];

		airData[0] = (byte) (((data[2] & 0x40) << 1)
				| ((data[3] & 0x44) << 0) 
				| ((data[3] & 0x10) << 1) 
				| ((data[4] & 0x20) >> 5) | 
				((data[4] & 0x10) >> 3));

//		if (((data[3] & 0x10) == 0)) {
//			airData[0] |= 0x20;
//		}

		airData[4] = (byte) (((data[2] & 0x20) >> 3));

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

	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword1(data, len);
	}

	public void startConnect() {
		byte[] buf = new byte[] { 0x3, (byte) 0x6a, 0x5, 1, (byte) 0xf0 };

		sendDataToCanbox(buf, buf.length);
	}
	
}
