package com.my.cartype.xinbasi;

import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class HyXinbasi extends Canbox{

	public HyXinbasi(){
		buildCmdDoor((byte) 0x8, (byte) 0x2, (byte) 0xfc, (byte) 0x02);
		buildCmdRadarFront((byte) 0x4, (byte) 0x1, (byte) 0x3);
		buildCmdRadarFrontEx((byte) 4);
		buildCmdRadarBack((byte) 0x4, (byte) 0x1, (byte) 0x3);
		buildCmdAngle((byte) 0x5, (byte) 0x0, 0x1200);
		buildCmdVersion((byte) 0x6, (byte) 0x0);
		mIdAC = 0x3;
		mIdKey = 0x1;
		MAP_KEYS = KEYS_WHEEL;
		mIdKey2 = 0x5;
		MAP_KEYS2 = KEYS_WHEEL2;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = { 0x3, 0x4, 0x7, 0x8,
			0xa, 0xb };
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x4, MyCmd.Keycode.NEXT },
		{ 0x3, MyCmd.Keycode.PREVIOUS },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0xa, MyCmd.Keycode.SPEECH },

		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x8, MyCmd.Keycode.BT_DIAL },
		{ 0x9, MyCmd.Keycode.BT_HANG },

	};
	
	private final static byte KEYS_WHEEL2[][] = {
		{ 0x1, MyCmd.Keycode.RADIO },
		{ 0x2, MyCmd.Keycode.AUDIO },
		{ 0x3, MyCmd.Keycode.BT },
		{ 0x4, MyCmd.Keycode.KEY_SEEK_NEXT },
		{ 0x5, MyCmd.Keycode.KEY_SEEK_PREV },
		{ 0x6, MyCmd.Keycode.NAVIGATION },
		{ 0x7, MyCmd.Keycode.NAVIGATION },
		{ 0x8, MyCmd.Keycode.SETUP },
		{ 0x9, MyCmd.Keycode.PLAY_PAUSE },
		{ 0xa, MyCmd.Keycode.POWER },
		{ 0xb, MyCmd.Keycode.MODLE },
		{ 0xc, MyCmd.Keycode.MUTE },
		{ 0xd, MyCmd.Keycode.PREVIOUS },
		{ 0xe, MyCmd.Keycode.NEXT },
		{ 0xf, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x10, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x11, MyCmd.Keycode.ROLL_NEXT },
		{ 0x12, MyCmd.Keycode.ROLL_PREV },
		{ 0x13, MyCmd.Keycode.KEY_DISPLAY },
		{ 0x14, MyCmd.Keycode.HOME },
//		{ 0x15, MyCmd.Keycode. },


	};

	@Override
	public int getAngleValue(byte[] data) {

		int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));
		
		return angle;
	}
	@Override
	public int getACTemp(byte data, int unit) {//
		// TODO Auto-generated method stub
		if ((data&0xff) == 0x7f){
			data = (byte)0xfa;
		} else {
			if (unit == 0){
				if ((data&0xff) == 0x22){
					data = 0;
				} else if ((data&0xff) == 0x40){
					data = (byte)0xff;
				} else {

				}
			} else {
				data = (byte)((data&0xff) + 28);
			}
		}
		
		
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0xc0) 
				| ((data[2] & 0x20) >> 1) );				
		

		airData[1] = (byte) ((data[3] & 0x0f) 
				| ((data[2] & 0x01) << 7) 
				| ((data[2] & 0x02) << 5) 
				| ((data[2] & 0x04) << 3)  );
		


		airData[7] = (byte) (
				((data[2] & 0x10) << 3) );
		

		airData[5] = (byte) (
				((data[3] & 0x40) >> 6) );
		
		airData[2] = data[4];
		airData[3] = data[5];

		airData[5] |= 0x80;
	
		
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		byte[] mData = new byte[] { (byte) 0x84, 0x5, 8, (byte) 0x20,
				(byte) (play & 0xff),
				(byte) (((play & 0xf00) >> 2) | ((time % 60) & 0x3f)),
				(byte) (time / 60) };
		sendDataToCanbox(mData, mData.length);

	}
	@Override
	public void startConnect() {
		// TODO Auto-generated method stub
		super.startConnect();
		byte [] mData = new byte[] { (byte) 0xff, 0x1, (byte) 0x7f};
		sendDataToCanbox(mData, mData.length);
	}

	public void setMediaSrc(int source, byte type, byte[] b) {

		if (b[0] < 0x10) {
			b[0] = 0;
		} else {
			b[0] = 3;
		}
		byte[] mData = new byte[] { (byte) 0x84, 0x5, 1, 0, b[0], b[2], b[1] };
		sendDataToCanbox(mData, mData.length);
	}

	public void setMediaSrc(int source) {

		byte s = 0xe;
		byte s1 = 0;
		switch (source) {
		case MyCmd.SOURCE_RADIO:
			return;
		case MyCmd.SOURCE_IPOD:
			s = 6;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 8;
			break;
		case MyCmd.SOURCE_AUX:
			s = 0x07;
			break;
		case MyCmd.SOURCE_DVD:
			s = 0x02;
			break;
		case MyCmd.SOURCE_BT:
			s = 0x06;
			break;
		case MyCmd.SOURCE_AV_OFF:
			s = 0x0;
			s1 = 0x40;
			break;
		}

		byte[] mData = new byte[] { (byte) 0x84, 0x5, s, s1, 0, 0, 0 };

		sendDataToCanbox(mData, mData.length);

	}

	public int getUpdateTime() {
		return 60000;
	}
	public void updateTime() {
		if (mContext == null) {
			return;
		}
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		byte ampm = 0;
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		if (!"12".equals(strTimeFormat)) {
			ampm |= 0x80;
		}
		
		h = fixTimeHour(h);
	

		byte m = (byte) curDate.getMinutes();

		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();

		byte[] buf = new byte[] { (byte) 0x86, 0x6, y, mon, d,  h, m , ampm};
		sendDataToCanbox(buf, buf.length);
	}
}
