package com.my.cartype.baogu;

import java.util.Date;

import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;
import android.util.Log;


public class DaChengE20 extends Canbox{

	public DaChengE20(){
//		buildCmdDoor((byte) 0x41, (byte) 0x2, (byte) 0xfc, (byte) 0x02);
//		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 15);
//		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 32);
//		buildCmdAngle((byte) 0x28, (byte) 0x0, 0x2198);
		buildCmdOutTemp((byte) 0x27, (byte) 0x10);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdAC = 0x21;
		mIdKey = 0x20;

		buildBrake((byte) 0x41, (byte) 0x3, (byte) 0x20, (byte) 0x1f);
		
		if (CarUtil.getModelId() == 15 || CarUtil.getModelId() == 11) {
			KEYS_WHEEL[11][1] = MyCmd.Keycode.VIDEO;
		}
				
		MAP_KEYS = KEYS_WHEEL;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}

	private final static byte IDS_TO_CANBOXSETTING[] = { 0x24, 0x40, 0x41, 0x38, 0x39, 0x47	};
	
	private final static byte KEYS_WHEEL[][] = { 
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D },
			{ 0x3, KEY_NEXTSONG },
			{ 0x4, KEY_PREVIOUSSONG }, 
			{ 0x5, KEY_BT }, 
			{ 0x6, KEY_MUTE },
			{ 0x7, KEY_SOURCE }, 
			{ 0x8, KEY_MIC },

			{ 0x13, KEY_NEXTSONG },
			{ 0x14, KEY_PREVIOUSSONG }, 
	};
	

	
	@Override
	public int getAngleValue(byte[] data) {

		int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

		angle =   0x1ea0 - angle;
		int max = (0x1ea0 - 0x0884);

		angle = ((angle * 3000) / max);
		if (angle > -50 && angle < 50) {
			angle = 50;
		}
		
		return angle;
		
		
	}

	public byte getACTempPriv(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0) {
			data = (byte) 0;
		} else if ((data & 0xff) == 0x1f) {
			data = (byte) 0xff;
		} else {
			data = (byte) (35 + (data&0xff));
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];

		airData[0] = data[2];		
		airData[1] = data[3];	
		
		

		
		airData[2] = data[7];


		airData[4] = (byte) (((data[6] & 0x08) >> 1));		
		
		airData[7] |= 0x40;
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub

		switch (data[0]) {
		case 0x41:
			switch (data[2]) {
			case 1: {
				if (mDoorStatus != (byte) (data[3] & 0x1F)) {
					mDoorStatus = (byte) (data[3] & 0x1F);
					Handler handler = getHandler("CanService");
					if (null != handler) {
						handler.sendMessage(handler.obtainMessage(
								CANBOX_DOOR_STATUS, mDoorStatus, 0));

					}
				}
			}
				break;
			}
		}

		
		if (mContext != null) {
			Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
			i.putExtra("buf", data);
			mContext.sendBroadcast(i);
		}

		super.parseCanboxData(data, len);
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
		if(s==0xb||s==0x7){
			data = new byte[]{(byte)0xc0, 0x8, s, mediaType,0,0,0,0,0,0};
		}else{
			data = new byte[]{(byte)0xc0, 0x2, s, mediaType};
		}
		
		sendDataToCanbox(data, data.length);
	}

	public void setVolume(int volume) {

		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
		sendDataToCanbox(data, data.length);
	}

	

	public int getUpdateTime() {
		return 60000;
	}
	public void updateTime() {
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);


		byte m = (byte) curDate.getMinutes();
		byte s = (byte) curDate.getSeconds();

		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte)( curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();
		byte []buf = new byte[] { (byte) 0xa6, 0x06, y, mon, d, h,
				m, s };
		
		sendDataToCanbox(buf, buf.length);
	}
}
