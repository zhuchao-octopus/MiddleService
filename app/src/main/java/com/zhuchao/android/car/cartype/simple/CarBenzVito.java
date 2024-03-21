package com.zhuchao.android.car.cartype.simple;

import android.os.Handler;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;


public class CarBenzVito extends Canbox {

	public CarBenzVito() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });
	}

	private final static byte[][] KEYS_WHEEL = { { 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, { 0x3, KEY_NEXTSONG },
			{ 0x4, KEY_PREVIOUSSONG }, { 0x6, KEY_MUTE }, 
			{ 0x8, KEY_MIC }, { 0x9, KEY_BT_DIAL }, { 0xA, KEY_BT_HANG },

	};

	private void parseWheelKey(byte[] data) {
		if (doKeyStudy(data[2], data[3])){
			return;
		}
		
		byte key = 0;
		for (int i = 0; i < KEYS_WHEEL.length; ++i) {
			if (KEYS_WHEEL[i][0] == data[2]) {
				key = KEYS_WHEEL[i][1];
				break;
			}
		}

		if (key != 0) {
			doKey(key, data[3]);
		} else {
			if (data[3] == 0){
				doKey(0, 0);
			}
		}
	}

	private final static byte[][] KEYS_WHEEL2 = { { 0x2, KEY_NEXTSONG },
			{ 0x1, KEY_PREVIOUSSONG }, { 0x3, MyCmd.Keycode.FAST_F },
			{ 0x4, MyCmd.Keycode.FAST_R }, { 0x11, MyCmd.Keycode.BT_DIAL },
			{ 0x12, MyCmd.Keycode.BT_HANG }, { 0x14, KEY_HOME },
			{ 0x17, KEY_MIC }, { 0x19, MyCmd.Keycode.KEY_BT_VOICE_SPEAKER },
			{ 0x18, MyCmd.Keycode.KEY_BT_VOICE_PHONE }, { 0x30, KEY_BACK },

	};



	private void parseACInfo(byte[] data, int len) {

		// if (data[4] >= 0x1f) {
		// data[4] = (byte) 0xff;
		// } else if (data[4] > 0) {
		// if ((data[6] & 0x1) == 0) {
		// data[4] = (byte) (((0.5f * data[4])));
		// } else {
		// data[4] = (byte) ((59 + (data[4] & 0xff)));
		// }
		// }
		// if (data[5] >= 0x1f) {
		// data[5] = (byte) 0xff;
		// } else if (data[5] > 0) {
		// if ((data[6] & 0x1) == 0) {
		// data[5] = (byte) ((15.5f + (0.5f * data[5])) * 2);
		// } else {
		// data[5] = (byte) ((59 + (data[5] & 0xff)));
		// }
		// }
		byte[] airData = new byte[8];

		airData[0] = (byte) (data[2] & 0xef);
		airData[0] |= (byte) (((data[6] & 0x40) >> 6));

		airData[1] = (byte) (data[3] & 0xff);
		airData[2] = (byte) (data[4] & 0xff);
		airData[3] = (byte) (data[5] & 0xff);

//		airData[4] = (byte) (data[7] & 0xff);
		airData[4] |= (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x8) >> 1) | ((data[2] & 0x1) << 3));

		airData[5] = (byte) ((data[6] & 0x1));

		airData[7] = (byte) (((data[6] & 0x10) >> 4));
		airData[7] |= (byte) (((data[6] & 0x80) >> 2));

		int msg = CANBOX_HIDE_AIR;
		if ((data[3] & 0x10) != 0) {
			msg = CANBOX_RETURN_AIR;
		}

		Handler handler = getHandler("CanService");
		if (null != handler) {
			handler.sendMessage(handler.obtainMessage(msg, airData));
		}
	}

	private byte getRadarData(byte i) {
		byte data = 0;
		switch (i) {
		case 0:
			data = 0;
			break;
		case 1:
			data = 1;
			break;
		case 2:
			data = 4;
			break;
		case 3:
			data = 7;
			break;
		case 4:
			data = 11;
			break;
		}
		return data;
	}

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x20: {
			parseWheelKey(data);
		}
			break;
		case 0x30: {
			byte[] version = new byte[16];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		case 0x24: {
			if ((data[2] & 0x1) != 0) {
				int door = (data[2] & 0xfc);
				door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6)
						| ((door & 0x10) >> 2) | ((door & 0x20) >> 2)
						| ((door & 0x08) << 1) | ((door & 0x4) << 3));

				if (mDoorStatus != door) {
					mDoorStatus = door;
					Handler handler = getHandler("CanService");
					if (null != handler) {
						handler.sendMessage(handler.obtainMessage(
								CANBOX_DOOR_STATUS, mDoorStatus, 0));

					}
				}
			}

		}
			break;

		case 0x29: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				short angle =(short) ((data[2] & 0xff) | ((data[3]) << 8));
				angle =(short) (angle * 300 / 0x462);
				
				Log.d("acd", String.valueOf(angle));
				if (angle > -5 && angle < 5) {
					angle = 5;
				}

				
				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle, 10));
			}
		}
			break;

		}

	
	}

	private int mDoorStatus = 0;

}
