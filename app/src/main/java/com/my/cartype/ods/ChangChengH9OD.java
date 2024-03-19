package com.my.cartype.ods;

import java.util.Calendar;

import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class ChangChengH9OD extends Canbox{

	public ChangChengH9OD(){
		mIdAC = 0x23;
		buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
		buildCmdRadarFront((byte) 0x27, (byte) 0x1, (byte) 0x4);
		buildCmdRadarBack((byte) 0x26, (byte) 0x1, (byte) 0x4);
		buildCmdAngle((byte) 0x30, (byte) 0x4, 0x157c);

		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		buildCmdEQ((byte) 0x37, (byte) 0x2, 6);
		mIdKey = 0x21;
		MAP_KEYS = KEYS_WHEEL;			

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	
	private final static byte IDS_TO_CANBOXSETTING[] = { 0x35, 0x23, 0x28,
			0x31, 0x34, 0x36, 0x38, 0x39, 0x3f, 0x37 };

	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D },

			{ 0x7, KEY_SOURCE },
			{ 0x9, MyCmd.Keycode.BT_DIAL }, 
			{ 0xa, MyCmd.Keycode.BT_HANG },	
			{ 0xC, KEY_NEXTSONG },
			{ 0xB, KEY_PREVIOUSSONG }, 	
			{ 0xD, MyCmd.Keycode.SPEECH },
			{ 0xE, MyCmd.Keycode.MUTE },
			

			{ 0x10, MyCmd.Keycode.RADIO },
			{ 0x11, MyCmd.Keycode.POWER },
			{ 0x12, MyCmd.Keycode.VOLUME_ROLL_UP },
			{ 0x13, MyCmd.Keycode.VOLUME_ROLL_DOWN },
			{ 0x14, MyCmd.Keycode.BT },
			{ 0x15, MyCmd.Keycode.NAVIGATION },
			{ 0x16, MyCmd.Keycode.EQ },
			{ 0x17, MyCmd.Keycode.BACK },
			{ 0x18, MyCmd.Keycode.MENU },
			{ 0x19, MyCmd.Keycode.HOME },
			{ 0x1a, MyCmd.Keycode.ROLL_PREV },
			{ 0x1b, MyCmd.Keycode.ROLL_PREV },
			{ 0x20, MyCmd.Keycode.KEY_AIR_CONTROL },
	};
	
	

	
	private byte getAngelStyle() {
		return 0;
	}
	
	
	public int getAngleValue(byte[] data) {

		short a;		
		int angle;
		int max;
		
		if (getAngelStyle() == 0){
			a = (short)((data[3] & 0xff) | (((data[2] & 0xff)) << 8));
			angle = -a;
			max = 5500;
		} else {
			a = (short) ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));
			angle = a / 20;
			max = 510;
			if ((data[3] & 0x1) != 0) {
				angle = -angle;
			}
		}
		
		angle = ((angle * 3000) / max);
		if (angle > -50 && angle < 50) {
			angle = 50;
		}
		
		return angle;
	}


	
	public void parseCanboxData(byte[] data, int len) {
		switch(data[0]){
		case 0x24:
			parseACInfoRear(data);
			break;
		default:
			super.parseCanboxData(data, len);
		}
	}
	



	private int getACTempPriv(byte data) {//
		if ((data & 0xff) >= 0x70 && (data & 0xff) <= 0x90) {
			data = (byte) (32 + (data - 0x74));
		} else {

		}
		return data & 0xff;
	}

	private byte[]	airData = new byte[12];
	public void parseACInfo(byte[] data)
	{			

		airData[0] = (byte) ((data[2] & 0xec) | ((data[2] & 0x02) >> 1) | ((data[2] & 0x01) << 1));
		
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
		case 6:
			airData[1] = (byte) (0xc0);
			break;
		case 7:
			airData[1] = (byte) (0xe0);
			break;
		default:
			airData[1] = 0;
			break;
		}
		
		airData[1] |= (byte) (data[4] & 0x0f);
		
		airData[2] = (byte) getACTempPriv(data[5]);
		airData[3] = (byte) getACTempPriv(data[6]);
	

		airData[4] = (byte) ((data[8] & 0x33));


		airData[7] = (byte) (
				((data[2] & 0x10) << 3));
		
		airData[8] = (byte) (
				((data[9] & 0x30) >> 0) | 
				((data[9] & 0x03) << 2));
		
//		if ((data[5] & 0xff) > 0 && (data[5] & 0xff) < 0x70) {
//			airData[7] |= 0x40;
//		}
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}
	

	public void parseACInfoRear(byte[] data)
	{			

		airData[9] = (byte) ((data[2] & 0x80));

		
		
		switch((data[3] & 0xff)){
		case 1:
			airData[11] = (byte) (0x40);
			break;
		case 2:
			airData[11] = (byte) (0x60);
			break;
		case 3:
			airData[11] = (byte) (0x20);
			break;
		default:
			airData[11] = 0;
			break;
		}
		

		airData[11] |= (byte) (((data[4] & 0x0f) << 0));
		

		airData[9] = (byte) (
				((data[2] & 0x08) >> 2));
		
//		airData[1] |= (byte) (data[4] & 0x0f);
		
		airData[10] = (byte) getACTempPriv(data[5]);	
	
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}
	
	
	public void setMediaMoreInfo(int source, int play, int total, int time, int total_time){
		byte min = (byte)((time/60)%60);
		byte sec = (byte)((time)%60);
//		++play;
		byte []data ;
		
		if (MyCmd.SOURCE_DVD != source) {
			data = new byte[] { (byte) 0xc3, 0x6, (byte) (total & 0xFF),
					(byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF),
					(byte) ((play >> 8) & 0xFF), min, sec };
		} else {
			data = new byte[] { (byte) 0xc3, 0x6, (byte) (1 & 0xFF),
					(byte) ((play ) & 0xFF), (byte) (total & 0xFF),
					(byte) ((0 ) & 0xFF), min, sec };
		}
		sendDataToCanbox(data, data.length);
	}
	public void setMediaSrc(int source, byte type, byte []b){
		setMediaSrc(0);
		if (b[0] != 0x10){
			b[0] += 1;
		}
		byte []data = new byte[]{(byte)0xc2, 0x4, b[0],b[1],b[2], 0};
		sendDataToCanbox(data, data.length);
	}
	public void setMediaSrc(int source){//default is simple box
		byte s = 0;
		byte mediaType = 0;
		switch (source){
		case 0:
			s = 1;
			mediaType = 1;
			break;
		case 1:
			s = 2;
			mediaType = 0x10;
			break;
		case MyCmd.SOURCE_IPOD:
			s = 6;
			mediaType = 0x12;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x09;
			mediaType = 0x11;
			break;
		case MyCmd.SOURCE_AUX:
			s = 0x07;
			mediaType = 0x30;
			byte []data2 = new byte[]{(byte)0xc3, 0x6, 0,0,0,0,0,0};
			sendDataToCanbox(data2, data2.length);
			break;
		case MyCmd.SOURCE_DTV:
			s = 0x0A;
			mediaType = 0x30;
			byte []data3 = new byte[]{(byte)0xc3, 0x6, 0,0,0,0,0,0};
			sendDataToCanbox(data3, data3.length);
			break;
		case MyCmd.SOURCE_BT:
			s = 0x0b;
			mediaType = 0x30;
			break;
		default:
			s = 0x00;
			mediaType = 0x0;
			break;
		}
		byte []data;
		
		data = new byte[]{(byte)0xc0, 0x2, s, mediaType};		
		
		sendDataToCanbox(data, data.length);
	}
	
	private void sendACControl(){
		
	}
	public void doCanboxFunctionKey(byte key){
		switch(key){
		case MyCmd.Keycode.CANBOX_AC_WIND_UP:
			Log.d("fcck", "!!!");
			break;
		case MyCmd.Keycode.CANBOX_AC_WIND_DOWN:
			Log.d("fcck", "!!!");
			break;
		}
	}

	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret =  (40 << 16) | (21 << 8) | 21;
			
			byte[] buf = new byte[] { (byte) 0x90, 0x2, 0x37, 0 };
			sendDataToCanbox(buf, buf.length);
		} else {
			byte[] buf = new byte[] { (byte) 0x86, 0x2, 0x0, (byte) data };
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				buf[2] = 2;
				break;
			case EQ_CMD_SET_MIDDLE:
				buf[2] = 3;
				break;
			case EQ_CMD_SET_LOW:
				buf[2] = 4;
				break;
			case EQ_CMD_SET_ZONE_FR:
				buf[2] = 6;
				break;
			case EQ_CMD_SET_ZONE_LR:
				buf[2] = 5;
				break;
			case EQ_CMD_SET_VOLUME:
				buf[2] = 1;
				break;
			default:
				return 0;
			}

			sendDataToCanbox(buf, buf.length);
		}
		return ret;
	}

	public boolean isSupportCompass() {
		return true;
	}	
			
	public void updateCompass(int compass) {
		int direction = compassAngleToDirect(compass);

		byte[] buf = new byte[] { (byte) (0x83), 0x3, 0x15,
				(byte) (direction & 0xff), 0 };

		sendDataToCanbox(buf, buf.length);
	}
	
	public int getUpdateTime() {
		return 60000;
	}

	public void updateTime() {

		Calendar c = Calendar.getInstance();

		byte h = (byte) c.get(Calendar.HOUR_OF_DAY);
		h = fixTimeHour(h);

		byte m = (byte) c.get(Calendar.MINUTE);
		byte[] buf = new byte[] { (byte) 0x83, 0x03, 0x03, h, m };

		sendDataToCanbox(buf, buf.length);


	}
	
}
