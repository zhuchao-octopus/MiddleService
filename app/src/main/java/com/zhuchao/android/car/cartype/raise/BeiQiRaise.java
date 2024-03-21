package com.zhuchao.android.car.cartype.raise;

import java.util.Date;

import android.provider.Settings;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class BeiQiRaise extends Canbox{

	public BeiQiRaise(){
		mIdAC = 0x21;
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
		buildCmdAngle((byte) 0x29, (byte) 0x4, 0x157c);
		buildCmdOutTemp((byte) 0x21, (byte) 0x0);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;	

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;	
		buildCmdRepeatSendCarType(getCarTypeCmd());
	}
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0xee, 0x02, (byte) 0xa0, 0 };
		switch(CarUtil.getModelId()){
		case 37:
			cmd [3] = 4;
			break;
		case 38:
			cmd [3] = 2;
			break;
		case 39:
			cmd [3] = 3;
			break;
		default:
			return null;
		}
		return cmd;
	}

	private final static byte[] IDS_TO_CANBOXSETTING = { 0x39, 0x27	};
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, 
			{ 0x3, KEY_NEXTSONG },
			{ 0x4, KEY_PREVIOUSSONG }, 
			{ 0x6, MyCmd.Keycode.MULT_MUTE_AND_HANG },
			{ 0x7, KEY_SOURCE },
			{ 0x8, AK_KEYPAD_VOLUME_A },
			{ 0x9, AK_KEYPAD_VOLUME_D }, 
			{ 0xa, MyCmd.Keycode.POWER },
			{ 0x10, MyCmd.Keycode.MULT_PREV_AND_RECEIVE },
			{ 0x11, MyCmd.Keycode.MULT_NEXT_AND_HANG },
			{ 0x16, MyCmd.Keycode.MUTE },
			{ 0x17, MyCmd.Keycode.HOME },
			{ 0x18, MyCmd.Keycode.BACK },
			{ 0x19, MyCmd.Keycode.NAVIGATION },
			{ 0x20, MyCmd.Keycode.BACKLIGHT_OFF },
			{ 0x21, MyCmd.Keycode.AUDIO },
			{ 0x22, MyCmd.Keycode.SPEECH },
			{ 0x23, MyCmd.Keycode.PLAY_PAUSE },
	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.NAVIGATION },
		{ 0x6, MyCmd.Keycode.BACK },
 
			{ 0x13, MyCmd.Keycode.PLAY_PAUSE },
			{ 0x14, MyCmd.Keycode.BT },
			{ 0x15, MyCmd.Keycode.AUDIO },
			{ 0x16, MyCmd.Keycode.RADIO },
			{ 0x17, MyCmd.Keycode.HOME },
			{ 0x18, MyCmd.Keycode.SETUP },
			{ 0x19, MyCmd.Keycode.MULT_MUTE_AND_POWER },
			

			{ 0xa, KEY_NEXTSONG },
			{ 0x9, KEY_PREVIOUSSONG }, 
			{ 0xc, KEY_NEXTSONG },
			{ 0xb, KEY_PREVIOUSSONG }, 
			{ 0xe, KEY_NEXTSONG },
			{ 0xd, KEY_PREVIOUSSONG }, 
			{ 0x10, KEY_NEXTSONG },
			{ 0xf, KEY_PREVIOUSSONG }, 
			{ 0x12, KEY_NEXTSONG },
			{ 0x11, KEY_PREVIOUSSONG }, 
	};
	

	private void parseWheelKey(byte[] data) {
		if (doKeyStudy(data[2], data[3])){
			return;
		}
		byte key = 0;
		int i;
		for (i = 0; i < KEYS_WHEEL.length; ++i) {
			if (KEYS_WHEEL2[i][0] == data[2]) {
				key = KEYS_WHEEL2[i][1];
				break;
			}
		}

		if (key != 0) {
			doKey(key, data[3]);
			if (i > 7) {
				doKey(key, 0);
			}
		} else {
			doKey(0, 0);
		}
	}
	
	private boolean isAnglesStyle0() {
		return CarUtil.getModelId() == 1 || CarUtil.getModelId() == 10 || CarUtil.getModelId() == 21;
	}
	
	private boolean isManuAC() {
		return CarUtil.getModelId() != 10 && CarUtil.getModelId() != 37 && CarUtil.getModelId() != 38 && CarUtil.getModelId() != 39;
	}
	
	public int getAngleValue(byte[] data) {
		int angle = ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

		int max = 0;
		if (isAnglesStyle0())
		{
			max = (0x32ff - 0x1f00);
			angle = angle - 0x1f00;
		}
		else
		{
			max = (0x358c - 0x1e80);
			angle = angle - 0x1e80;
		}
		
		angle = ((angle * 3000) / max);

		if (angle > -50 && angle < 50) {
			angle = 50;
		}

		return angle;
	}
	
	public void parseCanboxData(byte[] data, int len) {
		if (data[0] == 0x25) {
			parseWheelKey(data);
		} else {
			super.parseCanboxData(data, len);
		}
	}
	
	public int getOutTemp(byte[] data) {//
		int t = CarUtil.CLEAR_OUT_DOOR_TEMP;
		if (data.length > 9) {
			
			t = -400 + (((data[9] & 0xff))*5);
		}
		return t;
	}
	
	public int getACTemp(byte data, int unit) {//
		if (!isManuAC()) {
			if (data > 0 && data <= 0x21) {
				if (unit == 0) {
					data = (byte) (32 + (data - 0x1));
				} else {
					data = (byte) (59 + data);
				}
			}	
		} 
		return data & 0xff;
	}
	
	public void parseACInfo(byte[] data)
	{	
		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0xfe)
				| ((data[6] & 0x40) >> 6));		
		airData[1] = (byte) (data[3] & 0xff);
		
		

		airData[4] = (byte) (((data[6] & 0x08) >> 1) 
				| ((data[6] & 0x20) << 2)
				| ((data[2] & 0x01) << 3));
		airData[4] |= (byte) (data[7] & 0x33);
				
		airData[5] = (byte) ((data[6] & 0x01));
		
		airData[7] = (byte) (((data[6] & 0x80)>>2) 
				| ((data[6] & 0x10) >> 4));
		
//		airData[2] = (byte) getACTemp(data[4], airData[5] & 0x1);
//		airData[3] = (byte) getACTemp(data[5], airData[5] & 0x1);
		
		airData[2] = data[4];
		if (!isManuAC()) {
			airData[3] = data[5];
		}
		
		if (isManuAC()) {
			airData[7] |= 0x40;			
		} 
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}
	
	byte [] mData;
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		
	}
	public void setMediaSrc(int source, byte type, byte []b){
		if (b[0] != 0x10) {
			b[0] += 1;
		}
		mData = new byte[] { (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0,
				0, 0 };
		sendDataToCanbox(mData, mData.length);
	}

	public void setMediaSrc(int source) {// default is simple box
		
		byte s = 0;
		byte mediaType = 0;
		switch (source) {
		case MyCmd.SOURCE_RADIO:
			return;
		case MyCmd.SOURCE_MUSIC:
			s = 0x08;
			mediaType = (byte)0xff;
			break;
		case MyCmd.SOURCE_VIDEO:
			s = 0x08;
			mediaType = 0x0;
			break;
		case MyCmd.SOURCE_BT:
			s = 0x0b;
			break;
		case MyCmd.SOURCE_AV_OFF:
			s = 0x0b;
			break;
		default:
			s = 0x07;
			mediaType = 0x0;
			break;
		}

		mData = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0 };

		sendDataToCanbox(mData, mData.length);
		
	}

	public void sendId3(byte index, String num) {

		try {
			if (num == null) {
				num = "  ";
			}
			byte[] n = getBytesUnicodeLittleEndian(num); // del 0xff 0xfe

			int num_len = n.length;

			if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
				num_len -= 2;
			}

			if (num_len > 43) {
				num_len = 43;
			}
			byte[] data;

			int len = num_len + 3;

			data = new byte[len];

			data[0] = index;
			data[1] = (byte) (num_len + 1);
			data[2] = 0x1;
			for (int i = 0; i < num_len && i < (data[1]); ++i) {
				if (i % 2 == 0) {
					data[3 + i] = n[i + 3];
				} else {
					data[3 + i] = n[i + 1];
				}

			}

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

			Log.d("Nissan2013Simple", "sendId3" + e);
		}
	}
	String mName = null;
	public void setSongName(String s) {
		sendId3((byte) 0x70, s);
		mName = s;
	}

	public void setPhone(int status, String num) {// default is simple box
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
			status = 2;
			break;
		}
		byte[] data;
	
		if (num == null) {
			num = " ";
		}
		byte[] n = num.getBytes();
		data = new byte[n.length + 4];
		data[0] = (byte) 0xc5;
		data[1] = (byte) (n.length + 2);
		data[2] = (byte) status;
		data[3] = (byte) 1;
		byteArrayCopy(data, n, 4, 0, n.length);
		sendDataToCanbox(data, data.length);

	}
	
	@Override
	public int getUpdateTime() {
		// TODO Auto-generated method stub
		return 1000;
	}
	
	public void updateTime() {
		if (mContext == null) {
			return;
		}
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		byte ampm = 1;
		if ("12".equals(strTimeFormat)) {
			ampm = 0;
		}

		byte m = (byte) curDate.getMinutes();
		byte s = (byte) curDate.getSeconds();

		byte y = (byte) (curDate.getYear() - 120);
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();

		byte[] buf = new byte[] { (byte) 0x85, 0x07, y, mon, d, h, m, s, ampm };
		sendDataToCanbox(buf, buf.length);

	}

}
