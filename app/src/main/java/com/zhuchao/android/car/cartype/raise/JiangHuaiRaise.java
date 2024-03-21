package com.zhuchao.android.car.cartype.raise;

import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class JiangHuaiRaise extends Canbox{

	public JiangHuaiRaise(){
		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdRadarFront((byte) 0x23, (byte) 0x1, (byte) 0x4);
		buildCmdRadarBack((byte) 0x22, (byte) 0x1, (byte) 0x4);
		buildCmdAngle((byte) 0x30, (byte) 0x0, 0x2198);
		buildCmdOutTemp((byte) 0x11, (byte) 0x10);
		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdAC = 0x11;
		mIdKey = 0x21;
		
		switch(CarUtil.getModelId()){
		case 1:
		case 6:
			if (CarUtil.getCarTypeConfig()==2){
				MAP_KEYS = KEYS_WHEEL_S2_HIGH;
			} else {
				MAP_KEYS = KEYS_WHEEL;
			}
			break;
		case 5:
		case 8:
		case 9:
			MAP_KEYS = KEYS_WHEEL_IEV6E;
			break;
		default:
			MAP_KEYS = KEYS_WHEEL;
			break;
		}
		

		mIdKey2 = 2;
		MAP_KEYS2 = KEYS_WHEEL_IEV6E_LOW;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;	
		checkIfNeedInfo();
	}
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0xee, 0x05, 0x53, 0 };
		switch(CarUtil.getModelId()){
		case 20:
			cmd [3] = 1;
			break;
		case 21:
			cmd [3] = 2;
			break;
		default:
			return null;
		}
		return cmd;
	}
	
	private boolean mNeedInfo = false;
	private void checkIfNeedInfo(){
		switch(CarUtil.getModelId()){
		case 9:
		case 20:
		case 21:
			mNeedInfo = true;
			break;
		}
	}
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x38,  0x39, 0x3a	};
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0xb, MyCmd.Keycode.PREVIOUS },
		{ 0xc, MyCmd.Keycode.NEXT },
		{ 0x12, MyCmd.Keycode.SPEECH },

		{ (byte)0x8b, MyCmd.Keycode.VOLUME_UP },
		{ (byte)0x8c, MyCmd.Keycode.VOLUME_DOWN },
//		{ (byte)0x8d, MyCmd.Keycode },
		{ (byte)0x8e, MyCmd.Keycode.EASY_CONNECT },
		

		{ 0x9, MyCmd.Keycode.MULT_SPEECH_AND_BT },
		{ 0xa, MyCmd.Keycode.MULT_MUTE_AND_HANG },
		
		{ (byte)0x80, MyCmd.Keycode.POWER },
		{ (byte)0x81, MyCmd.Keycode.RADIO },
		{ (byte)0x82, MyCmd.Keycode.PREVIOUS },
		{ (byte)0x83, MyCmd.Keycode.NEXT },
		{ (byte)0x84, MyCmd.Keycode.BACK },
		{ (byte)0x85, MyCmd.Keycode.BT },
		{ (byte)0x86, MyCmd.Keycode.NAVIGATION },
		{ (byte)0x87, MyCmd.Keycode.AUDIO },
		{ (byte)0x88, MyCmd.Keycode.SETUP },
		{ (byte)0x89, MyCmd.Keycode.BT },
		{ (byte)0x8a, MyCmd.Keycode.NEXT },
	};
	
	private final static byte[][] KEYS_WHEEL_IEV6E = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0xb, MyCmd.Keycode.PREVIOUS },
		{ 0xc, MyCmd.Keycode.NEXT },
		{ 0x12, MyCmd.Keycode.SPEECH },

		{ (byte)0x8b, MyCmd.Keycode.VOLUME_UP },
		{ (byte)0x8c, MyCmd.Keycode.VOLUME_DOWN },
//		{ (byte)0x8d, MyCmd.Keycode },
		{ (byte)0x8e, MyCmd.Keycode.EASY_CONNECT },
		

		{ 0x9, MyCmd.Keycode.MULT_SPEECH_AND_BT },
		{ 0xa, MyCmd.Keycode.MULT_MUTE_AND_HANG },
		
		{ (byte)0x80, MyCmd.Keycode.POWER },
		{ (byte)0x81, MyCmd.Keycode.MENU },
		{ (byte)0x82, MyCmd.Keycode.BACK },
		{ (byte)0x83, MyCmd.Keycode.AUDIO },
		{ (byte)0x84, MyCmd.Keycode.RADIO },
		{ (byte)0x85, MyCmd.Keycode.PREVIOUS },
		{ (byte)0x86, MyCmd.Keycode.NAVIGATION },
		{ (byte)0x87, MyCmd.Keycode.MUTE },
		{ (byte)0x88, MyCmd.Keycode.KEY_DISPLAY },
		{ (byte)0x89, MyCmd.Keycode.BT },
		{ (byte)0x8a, MyCmd.Keycode.NEXT },
	};
	
	private final static byte[][] KEYS_WHEEL_S2_HIGH = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0xb, MyCmd.Keycode.PREVIOUS },
		{ 0xc, MyCmd.Keycode.NEXT },
		{ 0x12, MyCmd.Keycode.SPEECH },

		

		{ 0x9, MyCmd.Keycode.BT_DIAL },
		{ 0xa, MyCmd.Keycode.BT_HANG },

	};
	
	private final static byte[][] KEYS_WHEEL_IEV6E_LOW = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x2, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x3, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x4, MyCmd.Keycode.SETUP },
		{ 0x5, MyCmd.Keycode.MODLE },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.AS },
		{ 0x8, MyCmd.Keycode.PREVIOUS },
		{ 0x9, MyCmd.Keycode.NEXT },
		{ 0xa, MyCmd.Keycode.MODLE },
//		{ 0xb, MyCmd.Keycode.AUDIO },

	};
	
	@Override
	public int getAngleValue(byte[] data) {

		int angle = ((data[3] & 0xff) | (((data[2] & 0x7f)) << 8));

		
		angle = ((angle * 3000) / 12000);
		
		if ((data[2] & 0x80) != 0) {
			angle = -angle;
		}

		if (angle > -50 && angle < 50) {
			angle = 50;
		}
		return angle;		
	}
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) > 0 && (data & 0xff) < 30) {
			data = (byte) (35 + ((data&0xff) - 1));
		} else if ((data & 0xff) == 30) {
			data = (byte) 0xff;
		} else if ((data & 0xff) == 0) {
			data = 0;
		} else {
			data = (byte) 0xfa;
		}
		return data;
	}
	
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0x48) 
				| ((data[2] & 0x10) << 1)
				| ((data[2] & 0x02) >> 1));				
		
		switch((data[3] & 0xff)){
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
		airData[3] = (byte) (data[6] & 0xff);
		

		super.parseACInfo(airData);
	}	
	private final byte []mLcdInfo = new byte[]{(byte)0x75, 0x8, 0,0,0,0,0,0,0,1};
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		byte min = (byte) ((time / 60) % 60);
		byte sec = (byte) ((time) % 60);
		

		mLcdInfo[2] = 3;
		mLcdInfo[7] = min;
		mLcdInfo[8] = sec;
		
		sendDataToCanbox(mLcdInfo, mLcdInfo.length);
	}
	public void setMediaSrc(int source, byte type, byte []b){
		setMediaSrc(0);
		if (b[0] >= 0x10){
			b[0] = 3;
		} else {
			b[0] = 1;
		}
		mLcdInfo[2] = 1;
		mLcdInfo[3] = b[0];
		mLcdInfo[4] = b[2];
		mLcdInfo[5] = b[1];

		
		sendDataToCanbox(mLcdInfo, mLcdInfo.length);
	}

	@Override
	public void setVolume(int volume) {
		// TODO Auto-generated method stub
		if (volume == 0) {
			mLcdInfo[9] = 2;
		} else {
			mLcdInfo[9] = 1;
		}
		sendDataToCanbox(mLcdInfo, mLcdInfo.length);
	}
	public void setMediaSrc(int source) {// default is simple box
		byte s = 0;
	
		switch (source) {
		case MyCmd.SOURCE_RADIO:
			return;
		case MyCmd.SOURCE_DVD:
			s = 2;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x03;
			break;
		case MyCmd.SOURCE_AUX:
			s = 0x05;
			break;
		case MyCmd.SOURCE_BT:
			s = 0x6;
			break;
		}
		mLcdInfo[2] = s;


		mLcdInfo[3] = 0;
		mLcdInfo[4] = 0;
		mLcdInfo[5] = 0;
		mLcdInfo[6] = 0;
		mLcdInfo[7] = 0;
		mLcdInfo[8] = 0;
		sendDataToCanbox(mLcdInfo, mLcdInfo.length);
	}

	
	public int getOutTemp(byte[] data) {//
		if (((data[7] & 0xff) == 0xff) || ((data[7] & 0xff) == 0xfe)){
			return CarUtil.INVALID_OUT_DOOR_TEMP;
		} else {
			
			return ((data[7] & 0xff) - 40)*10;
		}
		
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

		h = fixTimeHour(h);
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		if ("12".equals(strTimeFormat)) {
			h |= 0x80;
			if (h > 12) {
				h -= 12;
			} else if (h == 0) {
				h = 12;
			}
		}

		int year = curDate.getYear() + 1900;
		
		byte m = (byte) curDate.getMinutes();
//		byte s = (byte) curDate.getSeconds();

		byte y = (byte) (year&0xff) ;
		byte mon = (byte) (((curDate.getMonth() + 1)&0x0f) | ((year&0xf00))>>4);
		byte d = (byte) curDate.getDate();

		byte[] buf = new byte[] { (byte) 0x82, 0x06, y, mon, d,  h, m , 0};
		sendDataToCanbox(buf, buf.length);
	}

}
