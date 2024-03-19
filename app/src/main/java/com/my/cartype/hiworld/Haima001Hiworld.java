package com.my.cartype.hiworld;


import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;

public class Haima001Hiworld extends Canbox {

	public Haima001Hiworld() {

	}
	@Override
	public void stopConnect() {

	}

	private final static byte KEYS_WHEEL[][] = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x3, MyCmd.Keycode.MUTE },

			{ 0x5, MyCmd.Keycode.BT_DIAL },
			{ 0x6, MyCmd.Keycode.BT_HANG },


			{ 0x8, MyCmd.Keycode.PREVIOUS },
			{ 0x9, MyCmd.Keycode.NEXT },

			{ 0xa, MyCmd.Keycode.MODLE },

	};
	
	
	@Override
	public void parseCanboxData(byte[] data, int len) {
//		len -= 2;
//		byte[] d = new byte[len];
//		mCanbox.byteArrayCopy(d, data, 0, 2, len);
//		mCanbox.parseCanboxData(d, len);
		switch (data[2]) {
		case (byte) 0xfc:
			if (data[4] == 0x12) {
				int key = data[5] & 0xff;
				parseWheelKey1(key, key == 0 ? 0 : 1, KEYS_WHEEL);
			}
			break;
		case (byte) 0xfd:
			parseACInfo(data);
			break;
		case (byte) 0xf0:
			len = 17; 
			byte[] version = new byte[len];
			Util.byteArrayCopy(version, data, 0, 3, version.length);
			mVersion = (new String(version));
			break;
		}

	}
	


	private byte getACTempPriv(byte data, byte data2) {

		if ((data & 0xff) == 0xff) {

		} else if ((data & 0xff) == 0xfe) {
			data = (byte) 0xfa;
		} else if ((data & 0xff) == 0) {

		} else if ((data & 0xff) >= 16 && (data & 0xff) <= 32) {
			data = (byte) (((data & 0xff) * 2) + ((data2 == 0) ? 0 : 1));
		} else {
			data = (byte) 0xfa;
		}

		return data;
	}


	public void parseACInfo(byte[] data) {

		byte[] airData = new byte[8];

		airData[0] = (byte) (0x80 
				| ((data[4] & 0x40) >> 5)
				| ((data[4] & 0x20) >> 5) 
				| ((data[4] & 0x04) << 1) 
				| ((data[4] & 0x01) << 6)
				);

		if (((data[4] & 0x80) == 0)) {
			airData[0] |= 0x20;
		}


		airData[1] |= (byte) ((data[5] & 0x0f)
				| ((data[4] & 0x10) << 1)
				| ((data[4] & 0x08) << 3)
				);

		airData[2] = getACTempPriv(data[3], (byte)(data[6]&0xf));
		
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
