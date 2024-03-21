package com.zhuchao.android.car.cartype.binarytek;

import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class RenaultBNR extends Canbox{

	public RenaultBNR(){
		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdRadarFront((byte) 0x23, (byte) 0x1, (byte) 4);
		buildCmdRadarBack((byte) 0x22, (byte) 0x1, (byte) 4);

		buildCmdAngle((byte) 0x30, (byte) 0x0, 5400);

		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdAC = 0x21;
		mIdKey = 0x20;
				
		MAP_KEYS = KEYS_WHEEL;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}

	private final static byte[] IDS_TO_CANBOXSETTING = { 0x71, (byte)0x81,	};
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.ROLL_PREV },
		{ 0x4, MyCmd.Keycode.ROLL_NEXT },

		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },

		{ 0x9, MyCmd.Keycode.BT_DIAL },
		{ 0xa, MyCmd.Keycode.BT_HANG }, 

		{ 0x12, MyCmd.Keycode.SPEECH },
		{ 0x15, MyCmd.Keycode.BACK },
		{ 0x16, MyCmd.Keycode.MODLE },
	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0xe2, 0x01, 0 };
		switch (CarUtil.getModelId()) {
		case 0:
			switch (CarUtil.getCarTypeConfig()) {
			case 0:
				cmd[2] = 2;
				break;
			case 2:
				cmd[2] = 3;
				break;
			default:
				return null;
			}
			break;
		case 3:
			switch (CarUtil.getCarTypeConfig()) {
			case 0:
				cmd[2] = 0;
				break;
			case 1:
				cmd[2] = 1;
				break;
			default:
				cmd[2] = 4;
				break;
			}
			break;
		case 5:
			switch (CarUtil.getCarTypeConfig()) {
			case 0:
				cmd[2] = 5;
				break;
			case 1:
				cmd[2] = 6;
				break;
			default:
				return null;
			}
			break;
		default:
			return null;
		}
		return cmd;
	}
	@Override
	public int getAngleValue2(byte[] data) {
		int angle = ((data[3] & 0xff) | (((data[2] & 0x7f)) << 8));

		if ((data[2] & 0x80)!=0){
			angle = -angle;
		}
		
		return angle;
		
		
	}

	public byte getACTempPriv(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0xfe) {
			data = (byte) 0;
		} else if ((data & 0xff) == 0) {
			data = (byte) 0xfa;
		}  else if ((data & 0xff) == 0xff) {
			data = (byte) 0xff;
		} else {
//			data = (byte) (37 + (((data & 0xff) -3 )/2) );
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{
		
		byte[] airData = new byte[8];

		airData[0] = (byte) ((data[2] & 0xec) 
				| ((data[2] & 0x10) << 1)
				| ((data[2] & 0x02) >> 1) 
				| ((data[2] & 0x01) << 1));

		airData[1] = (byte) ((data[4] & 0x0f) 
				| ((data[3] & 0x01) << 7)
				| ((data[3] & 0x02) << 5) 
				| ((data[3] & 0x04) << 3));

		airData[2] = (byte) (data[5] & 0xff);
		airData[3] = (byte) (data[6] & 0xff);

		airData[4] = (byte) (((data[2] & 0x20) << 2));
//		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	
	private byte[] mData = new byte[] { (byte) 0xc0, 0x8, 0, 0, 0, 0, 0, 0, 0,
			0 };

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		byte h = (byte) ((time / 3600));
		byte min = (byte) ((time / 60) % 60);
		byte sec = (byte) ((time) % 60);
		++play;

		mData = new byte[] { (byte) 0xc0, 0x8, 0x8, (byte) ((total & 0xFF00) >> 8),(byte) ((total) & 0xFF),
				(byte) ((play & 0xFF00) >> 8),(byte) ((play) & 0xFF),
				 h, min, sec };

		// if (mPhoneStatus < HFP_INFO_CALLED) {

		sendDataToCanbox(mData, mData.length);
		// }
	}

	public void setMediaSrc(int source, byte type, byte[] b) {

		if (b[0] < 0x10) {
			b[0] = 0;
		} else {
			b[0] = 0x10;
		}
		mData = new byte[] { (byte) 0xc0, 0x5, 0x1, b[0], b[1], b[2], 0};
		sendDataToCanbox(mData, mData.length);
	}

	public void setMediaSrc(int source) {// default is simple box
		
		if (source == MyCmd.SOURCE_RADIO){
			return;
		}
		byte s = 0;
		switch (source) {
		case 1:
			s = 2;
			break;
		case MyCmd.SOURCE_IPOD:
			s = 6;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x08;
			break;
		case MyCmd.SOURCE_BT:
			s = 0x0b;
			break;
		case MyCmd.SOURCE_AUX:
		default:
			s = 0x07;
			break;
		}

		// if (s == 0xb || s == 0x7) {
		// data = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0,
		// 0 };
		// } else {
		mData = new byte[] { (byte) 0xc0, 0x1, s};
		// }

		sendDataToCanbox(mData, mData.length);
	}
	

	


	public int getUpdateTime() {
		return 60000;
	}
	public void updateTime() {
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);


		

		
		if ("12".equals(strTimeFormat)) {
			if (h > 12) {
				h -= 12;
			} else if (h == 0) {
				h = 12;
			}
			h |= 0x80;
		} else {
		}

		byte m = (byte) curDate.getMinutes();

		byte[] buf = new byte[] { (byte) 0xe1, 0x02, h, m };

		sendDataToCanbox(buf, buf.length);
	}
}
