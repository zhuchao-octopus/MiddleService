package com.zhuchao.android.car.cartype.simple;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.common.util.UtilSystem;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;


public class BravoUnionSimple extends Canbox {

	public BravoUnionSimple() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });
	}

	private final static byte[][] KEYS_WHEEL = {
		{ 0x2, KEY_BT }, 
		{ 0x3, KEY_MODE }, 
			{ 0x4, AK_KEYPAD_VOLUME_A },
			{ 0x5, AK_KEYPAD_VOLUME_D }, 

			{ 0x6, KEY_MUTE }, 
			
			
			
			
			
			{ 0x9, KEY_NEXTSONG },
			{ 0x8, KEY_PREVIOUSSONG },

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
		}else {
			if (data[3] == 0){
				doKey(0, 0);
			}
		}
	}


	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x1: {
			parseWheelKey(data);
		}
			break;		
		case 0x71: {
			byte[] version = new byte[1];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		}
	}

	byte[] data;

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		byte h = (byte) ((time / 3600));
		byte min = (byte) ((time / 60) % 60);
		byte sec = (byte) ((time) % 60);
		// ++play;

		byte s = 0;
		byte s2 = 0;
		switch (source) {
		case MyCmd.SOURCE_DVD:
			s = 0x2;
			s2 = 0x10;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			s = 8;
			s2 = 0x11;
			break;
		case MyCmd.SOURCE_BT:
			s = 0xb;
			s2 = 0x10;
			break;
		default:
			s = 0x07;
			s2 = 0x30;
			break;
		}

		if (MyCmd.SOURCE_DVD == source) {
			data = new byte[] { (byte) 0xc0, 0x8, s, s2, 0,
					(byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec };

		} else {
			data = new byte[] { (byte) 0xc0, 0x8, s, s2,
					(byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), 0,
					h, min, sec };
		}

		if (mPhoneStatus < HFP_INFO_CALLED) {

			sendDataToCanbox(data, data.length);
		}
	}

	public void setMediaSrc(int source) {// default is simple box
		byte s;
		byte mediaType = 0;
		switch (source) {
		case 0:
			s = 1;
			// mediaType = 1;
			break;
		case 1:
			s = 2;
			mediaType = 0x10;
			break;
		// case 0x82:
		// case 0x83:
		// break;
		default:
			s = 0x0c;
			mediaType = 0x30;
			break;
		}
		byte[] data = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0,
				0, 0 };
		if (mediaType != 0) {
			sendDataToCanbox(data, data.length);
		}
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		// setMediaSrc(0);
		// if (b[0] != 0x10) {
		// b[0] += 1;
		// }
		if (b[3] >= 0 & b[3] <= 30) {
			b[3]++;
		} else {
			b[3] = 0;
		}
		data = new byte[] { (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], b[3],
				0, 0 };
		sendDataToCanbox(data, data.length);
	}

	public void sendId3(byte index, String num) {

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

			int num_len = n.length;
			if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
				num_len -= 2;
			}
			int len = 36;
			// if (len > 31) {
			// len = 31;
			// }
			byte[] data = new byte[len];

			data[0] = (byte) 0xcb;
			data[1] = (byte) (len - 2);
			data[2] = index;
			data[3] = 0x2;
			for (int i = 0; i < num_len && i < (len - 4); ++i) {
				data[4 + i] = n[i + (n.length - num_len)];
			}
			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

			Log.d("Nissan2013Simple", "sendId3" + e);
		}
	}

	String mName = null;
	String mArtist = null;
	String mAlbum = null;

	// public void setPhone(int status, String num) {
	// sendId3((byte)0x1, num);
	// }

	public void setSongName(String s) {
		sendId3((byte) 0x2, s);
		mName = s;
	}

	public void setSongAritst(String s) {
		sendId3((byte) 0x4, s);
		mArtist = s;
	}

	public void setSongAlbum(String s) {
		sendId3((byte) 0x3, s);
		mAlbum = s;
	}

	private int mPhoneStatus = HFP_INFO_INITIAL;

	public void setPhone(int status, String num) {// default is simple box

		mPhoneStatus = status;

		switch (status) {
		case HFP_INFO_INITIAL:
		case HFP_INFO_READY:
		case HFP_INFO_CONNECTING:
		case HFP_INFO_CONNECTED:
			status = 0;
			break;
		case HFP_INFO_CALLED:
			status = 3;
			break;
		case HFP_INFO_INCOMING:
			status = 1;
			break;
		case HFP_INFO_CALLING:
			status = 4;
			break;
		}

		if (status != 0) {
			byte[] data2;

			if (num == null) {
				num = " ";
			}

			data2 = new byte[] { (byte) 0xc0, 0x8, 0x5, 0x40, 0, 0, 0, 0, 0, 0 };

			sendDataToCanbox(data2, data2.length);
			Util.doSleep(50);

			byte[] n = num.getBytes();
			data2 = new byte[36];// {(byte)0xc5, 0x1, (byte)status};
			data2[0] = (byte) 0xcb;
			data2[1] = 34;
			data2[2] = 0x1;
			data2[3] = 0x1;

			int num_len = n.length;
			if (num_len > 31) {
				num_len = 31;
			}
			byteArrayCopy(data2, n, 4, 0, num_len);
			data2[num_len + 4] = (byte) 0xff;

			sendDataToCanbox(data2, data2.length);
		} else {
			if (data != null) {
				sendDataToCanbox(data, data.length);
			}
		}

	}

	public void setVolume(int volume) {

		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
		sendDataToCanbox(data, data.length);
	}

	public void updateTime() {
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);
		byte ampm = 0;
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		byte format;
		if ("12".equals(strTimeFormat)) {
			if (h >= 12) {
				ampm |= 0x80;
			}

			ampm |= 0x40;

			if (h > 12) {
				h -= 12;
			} else if (h == 0) {
				h = 12;
			}

			h |= 0x80;
		}

		byte m = (byte) curDate.getMinutes();

		byte[] buf = new byte[] { (byte) 0xc6, 0x04, 0x01, h, m, ampm };
		sendDataToCanbox(buf, buf.length);
	}

	protected void doKey(int value, int status) { // value 0 -> key up

//		Log.d("Nissan2013Simple", "doKey:"+value);
//		if (CarUtil.getChangeKey() == 1) {
			value = changeKey(value);
//		}

		switch (status) {
		case 0:
			if(mKeyDown != 0){				
				if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME){
					int ret =  (value);
					if (ret != 0) {
						mKeyDown = ret;
					}
				} 
				doKey(mKeyDown);
				mKeyDown = 0;
				longClick = false;
			}
			break;
		case 1:
			mKeyDown = value;
			mClickTime = System.currentTimeMillis();
			longClick = false;
			break;
		case 2:			
			if (value == AK_KEYPAD_VOLUME_A
					|| value == AK_KEYPAD_VOLUME_D) {
				doKey(value);
				mKeyDown = 0;
			} else {
				if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME){
					if (mKeyDown != 0) {
						longClick = true;
						int ret = getLongKey(value);
						if (ret != 0) {
							doKey(ret);
							mKeyDown = 0;
						}
					}
				} 
			}
			break;
		}

	}
	public int getUpdateTime() {
		return 60000;
	}
}
