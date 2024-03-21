package com.zhuchao.android.car.cartype.raise;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class SiWeiRaise extends Canbox{

	public SiWeiRaise(){
		mIdAC = 0x21;
		buildCmdDoor((byte) 0x25, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
		buildCmdAngle((byte) 0x29, (byte) 0x0, 540);
		buildCmdOutTemp((byte) 0x27, (byte) 0x10);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x41, 0x25	};
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, 
			{ 0x4, KEY_NEXTSONG },
			{ 0x3, KEY_PREVIOUSSONG }, 
			{ 0x5, KEY_BT }, 
			{ 0x6, KEY_MUTE },
			{ 0x7, KEY_SOURCE },
			{ 0x8, KEY_MIC },
			{ 0x9, MyCmd.Keycode.BT_DIAL },
			{ 0xa, MyCmd.Keycode.MULT_MUTE_AND_HANG },

			{ (byte)0x81, MyCmd.Keycode.VOLUME_ROLL_DOWN },
			{ (byte)0x82, MyCmd.Keycode.VOLUME_ROLL_UP },
			{ (byte)0x83, MyCmd.Keycode.ROLL_PREV },
			{ (byte)0x84, MyCmd.Keycode.ROLL_NEXT },
			{ (byte)0x85, MyCmd.Keycode.BACK },
			{ (byte)0x86, MyCmd.Keycode.HOME },
			{ (byte)0x87, MyCmd.Keycode.MULT_OK_AND_POWER },
	};
	

	public void parseRadarBack(int id, byte[] data) {
		int max = data[3];
		mRadar[0] = radarChangeStyle(data[4], max, 0);
		mRadar[1] = radarChangeStyle(data[5], max, 0);
		mRadar[2] = radarChangeStyle(data[6], max, 0);
		mRadar[3] = radarChangeStyle(data[7], max, 0);
		parseRadar();
	}


	@Override
	public int getACTemp(byte data) {
		
		if ((data & 0xff) == 0x1) {
			data = (byte) 0x0;
		} else if ((data & 0xff) != 0xff) {
			data = (byte) (30 + (data - 0x1e));

		}
		return data & 0xff;
	}
	
	public void parseACInfo(byte[] data)
	{
		if (data[4] == 0xfe){
			data[4] = (byte)0xff;
		} 
		if (data[5] == 0xfe){
			data[5] = (byte)0xff;
		} 
		
		byte[]	airData = new byte[9];
		
		airData[0] = (byte) ((data[6] & 0xe8) 
				| ((data[6] & 0x01) << 1)
				| ((data[6] & 0x02) >> 1));	
		
		switch((data[5] & 0xff)){
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

		airData[1] |= (byte) (data[2] & 0x7);
		
		airData[2] = (byte) (data[3] & 0xff);
		airData[3] = (byte) (data[4] & 0xff);

		airData[4] = (byte) (data[7] & 0x30);
		airData[8] = (byte) ((data[7] & 0xc0) >> 2);
		airData[5] |= 0x80;
		
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

	}
	public void setMediaSrc(int source, byte type, byte []b){
		setMediaSrc(0);
		
		if (b[0] >= 0x10) { // am
			mLcdInfo[2] = 2;
		} else {
			mLcdInfo[2] = 1;
		}
		
		mLcdInfo[3] = b[1];
		mLcdInfo[4] = b[2];
		if (mPhoneStatus <= HFP_INFO_CONNECTED){
			sendDataToCanbox(mLcdInfo, mLcdInfo.length);
		}
		
	}

	private final byte[] mLcdInfo = new byte[] { (byte) 0xa5, 0x3, 0, 0, 0 };
	public void setMediaSrc(int source) {// default is simple box
		byte s = -1;
	
		switch (source) {
		case MyCmd.SOURCE_RADIO:
			s = 1;
			break;
		case MyCmd.SOURCE_DVD:
			s = 3;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x04;
			break;
		case MyCmd.SOURCE_BT:
			s = 0x06;
			break;
		case MyCmd.SOURCE_AV_OFF:
			s = 0;
			break;
		case MyCmd.SOURCE_AUX:
		default:
			s = 0x08;
			break;
		}

		if (s != -1 && mPhoneStatus <= HFP_INFO_CONNECTED){
			mLcdInfo[2] = s;
			mLcdInfo[3] = 0;
			mLcdInfo[4] = 0;
			sendDataToCanbox(mLcdInfo, mLcdInfo.length);
		}
		
	}
	

	
	public boolean requestAngleData() {
		byte[] data3 = new byte[] { (byte) 0x90, 0x2, 0x29, 0 };
		sendDataToCanbox(data3, data3.length);
		return true;
	}
	
	public int getOutTemp(byte[] data) {//
		int t = data[2];
		return t  * 10;
	}

	private int mPhoneStatus;
	public void setPhone(int phone_status, String num) {// default is simple box
		mPhoneStatus = phone_status;
		byte status = 0;
		switch (phone_status) {
		case HFP_INFO_CALLED:
			status = 0x20;
			break;
		case HFP_INFO_INCOMING:
			status = 0x22;
			break;
		case HFP_INFO_CALLING:
			status = 0x21;
			break;
		}
		if (num == null) {
			num = "";
		}
		if (mPhoneStatus <= HFP_INFO_CONNECTED){
			sendDataToCanbox(mLcdInfo, mLcdInfo.length);
		} else {
			byte[] data = mLcdInfo.clone();
			data[2] = status;
			data[3] = 0;
			data[4] = 0;
			sendDataToCanbox(data, data.length);
		}

	}

}
