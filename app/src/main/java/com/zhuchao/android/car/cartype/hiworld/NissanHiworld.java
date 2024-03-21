package com.zhuchao.android.car.cartype.hiworld;

import java.util.Date;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class NissanHiworld extends Canbox{

	public NissanHiworld(){
		buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x04);
		buildCmdRadarFront((byte) 0x41, (byte) 0x1, (byte) 3, (byte) 4,
				(byte) 4);
		buildCmdRadarFrontEx((byte) 4);
		buildCmdRadarBack((byte) 0x41, (byte) 0x1, (byte) 3, (byte) 4, (byte) 4);
		buildCmdAngle((byte) 0x72, (byte) 0x0, 500);
		buildCmdEQ((byte) 0xa6, (byte) 0x1, 6);
//		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
//		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdAC = 0x31;
		mIdKey = 0x040172;
		MAP_KEYS = KEYS_WHEEL;
		mIdKey2 = 0x1074;
		MAP_KEYS2 = KEYS_WHEEL2;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}


	@Override
	public void stopConnect() {

	}
	private byte[] getCarTypeCmd() {
		switch(CarUtil.getModelId()){
		case 2:
		case 5:
			KEYS_WHEEL[4][1] = MyCmd.Keycode.BT;
			break;
		case 7:
			KEYS_WHEEL[4][1] = MyCmd.Keycode.MULT_MUTE_AND_HANG;
			KEYS_WHEEL[6][1] = MyCmd.Keycode.NEXT;
			KEYS_WHEEL[7][1] = MyCmd.Keycode.PREVIOUS;
			break;	
		case 9:
		case 24:
			KEYS_WHEEL[3][1] = MyCmd.Keycode.SPEECH;
			KEYS_WHEEL[4][1] = MyCmd.Keycode.BT;
			KEYS_WHEEL[6][1] = MyCmd.Keycode.NEXT;
			KEYS_WHEEL[7][1] = MyCmd.Keycode.PREVIOUS;
			break;
		case 35:
			KEYS_WHEEL[3][1] = MyCmd.Keycode.MULT_SPEECH_AND_BT;
			KEYS_WHEEL[4][1] = MyCmd.Keycode.BT;
			KEYS_WHEEL[6][1] = MyCmd.Keycode.NEXT;
			KEYS_WHEEL[7][1] = MyCmd.Keycode.PREVIOUS;
			break;		
			
		}
		
		byte[] cmd = new byte[] { 0x2, (byte) 0x24, 0x00, 0x4 };
		switch(CarUtil.getModelId()){
		case 13:
			cmd[2] = 0x1;
			break;
		case 11:
			cmd[2] =(byte) 0xa1;
			break;
		case 36:
			cmd[2] =(byte) 0xa2;
			break;
		case 12:
			cmd[2] =(byte) 0x5;
			break;
		case 5:
			cmd[2] =(byte) 0xb;
			break;
		case 7:
			cmd[2] =(byte) 0xa3;
			break;
		case 37:
			cmd[2] =(byte) 0xa4;
			break;
		case 10:
			cmd[2] =(byte) 0xa5;
			break;
		case 35:
			cmd[2] =(byte) 0xa6;
			break;
		case 38:
			cmd[2] =(byte) 0xa7;
			break;
			

		case 1:
		case 2:
		case 3:
		case 4:
		case 48:
		case 49:
		case 15:
			cmd[2] =(byte) 0xf;
			break;
		case 47:
			cmd[2] =(byte) 0x10;
			break;
		case 46:
			cmd[2] =(byte) 0x11;
			break;
		case 45:
			cmd[2] =(byte) 0x12;
			break;
		case 24:
			cmd[2] =(byte) 0x13;
			break;
		case 44:
			cmd[2] =(byte) 0x14;
			break;

		default:
			return null;
			
		}
		
		return cmd;
	}
	
	private final static byte[] IDS_TO_CANBOXSETTING = {
		0x72, 0x12, (byte)0xa6};
	

	private final byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x4, MyCmd.Keycode.SPEECH },
		{ 0x5, MyCmd.Keycode.BT_DIAL },
		{ 0x6, MyCmd.Keycode.BT_HANG },
		{ 0xa, MyCmd.Keycode.MODLE },
		{ 0xd, MyCmd.Keycode.PREVIOUS },
		{ 0xe, MyCmd.Keycode.NEXT },
		{ 0xf, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x10, MyCmd.Keycode.BACK },
	};
	
	private static final byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.PREVIOUS, 0 },
		{ 0x2, MyCmd.Keycode.NEXT, 0 },
		{ 0x3, MyCmd.Keycode.PREVIOUS, 0 },
		{ 0x4, MyCmd.Keycode.NEXT, 0 },
		{ 0x5, MyCmd.Keycode.PREVIOUS, 0 },
		{ 0x6, MyCmd.Keycode.NEXT, 0 },
		{ 0x7, MyCmd.Keycode.PREVIOUS, 0 },
		{ 0x8, MyCmd.Keycode.NEXT, 0 },
		{ 0x9, MyCmd.Keycode.PLAY_PAUSE, 0 },
		{ 0xa, MyCmd.Keycode.BACK, 0 },
		{ 0xb, MyCmd.Keycode.MENU, 0 },
		{ 0xc, MyCmd.Keycode.NAVIGATION, 0 },
	};


	@Override
	public int getAngleValue2(byte[] data) {

		short angle = (short) ((data[7] & 0xff)
				| ((data[6] & 0xff) << 8));
		
		return -angle;
		
		
	}
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 0){
			data = (byte)0xfa;
		} else if ((data&0xff) == 0xff){

		} else if ((data&0xff) == 0xfe){
			data = 0;
		} else {
			//data = 
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[13];
		
		airData[0] = (byte) ((data[2] & 0x08) 
				| ((data[2] & 0x48) << 1)
				| ((data[2] & 0x01) << 6)
				| ((data[3] & 0x40) << 0)
				| ((data[3] & 0x10) << 1)
				| ((data[3] & 0x04) << 0)
				| ((data[4] & 0x20) >> 5)
				| ((data[4] & 0x10) >> 3));				
		
		airData[4] = (byte) (
				 ((data[4] & 0x03) << 4)
					| ((data[4] & 0x0c) >> 2)
					| ((data[2] & 0x20) >> 3)
					| ((data[3] & 0x80) >> 4)
					| ((data[3] & 0x08) << 4));		
		

		airData[8] = (byte) (
				 ((data[5] & 0x30) << 0)
				| ((data[5] & 0xc0) >> 4));		

		airData[7] = (byte) (
				 ((data[2] & 0x04) << 5));		
		
		airData[5] = 0x8;
		
		switch((data[6] & 0x0f)){
//		case 2:
//			airData[1] = (byte) (0x80);
//			break;
		case 3:
			airData[1] = (byte) (0x20);
			break;
		case 5:
			airData[1] = (byte) (0x60);
			break;
		case 6:
			airData[1] = (byte) (0x40);
			break;
		case 0xb:
			airData[1] = (byte) (0x80);
			break;
		case 0xc:
			airData[1] = (byte) (0xa0);
			break;
		case 0xd:
			airData[1] = (byte) (0xc0);
			break;
		case 0xe:
			airData[1] = (byte) (0xe0);
			break;
		}
		
		switch((data[6] & 0xf0) >> 4){
		case 3:
			airData[6] = (byte) (0x20);
			break;
		case 5:
			airData[6] = (byte) (0x60);
			break;
		case 6:
			airData[6] = (byte) (0x40);
			break;
		case 0xb:
			airData[6] = (byte) (0x80);
			break;
		case 0xc:
			airData[6] = (byte) (0xa0);
			break;
		case 0xd:
			airData[6] = (byte) (0xc0);
			break;
		case 0xe:
			airData[6] = (byte) (0xe0);
			break;
		}

		airData[1] |= (byte) (data[7] & 0x0f);
		

		airData[2] = (byte) (data[8] & 0xff);
		airData[3] = (byte) (data[9] & 0xff);
		
		switch((data[10])){
		case 1:
			airData[11] = (byte) (0x20);
			break;
		case 2:
			airData[11] = (byte) (0x40);
			break;
		case 3:
			airData[11] = (byte) (0x60);
			break;
		}		

		airData[11] |= (byte) (data[10] & 0x0f);

		airData[10] = (byte) getACTemp(data[12]);
		

		airData[12] = (byte) (
				 ((data[4] & 0x80) >> 7));	
		
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}
	public void setMediaSrc(int source, byte type, byte []b){
	}

	public void setMediaSrc(int source) {
	}
	

	

	
	public int getOutTemp(byte[] data) {//
		short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
		return t;
	}
	public void startConnect() {
		
	}
	
	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword2(data, len);
	}
	
	private final Handler mHandlerRepeat = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				sendEQCmd(msg.arg2);
			}
			super.handleMessage(msg);
		}
	};
	byte[] mEQCmdBuf = new byte[] { 0x2, (byte) 0xad, 0x0, 0x0 };

	private void sendEQCmd(int step) {
		mHandlerRepeat.removeMessages(0);
		if (step == 0) {
			return;
		}

		if (step < 0) {
			mEQCmdBuf[3] = -1;
			++step;
		} else {
			mEQCmdBuf[3] = 1;
			--step;
		}

		sendDataToCanbox(mEQCmdBuf, mEQCmdBuf.length);
		if (step != 0) {
			mHandlerRepeat.sendMessageDelayed(
					mHandlerRepeat.obtainMessage(0, 0, step), 100);
		}

	}
	
	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			
			ret = (40 << 16) | (11 << 8) | 11;
			
		} else {
			byte step = 0;
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				mEQCmdBuf[2] = 6;
				step = 0; 
				break;
			case EQ_CMD_SET_MIDDLE:
				mEQCmdBuf[2] = 5;
				step = 1; 
				break;
			case EQ_CMD_SET_LOW:
				mEQCmdBuf[2] = 4;
				step = 2; 
				break;
			case EQ_CMD_SET_ZONE_FR:
				mEQCmdBuf[2] = 3;
				step = 3; 
				break;
			case EQ_CMD_SET_ZONE_LR:
				mEQCmdBuf[2] = 2;
				step = 4; 
				break;
			case EQ_CMD_SET_VOLUME:
				mEQCmdBuf[2] = 1;
				step = 5; 
				break;
			default:
				return 0;
			}
			
			if (mEQData != null){
				step = (byte)(data - mEQData[step]);
			}			
			if (step != 0) {
				sendEQCmd(step);
			}
		}
		return ret;
	}
	
	public void parseEQ(int id, byte[] buf) {
		if (mEQData == null){
			mEQData = new byte[6];
		}
		mEQData[0] = (byte) (buf[7] + 5);
		mEQData[1] = (byte) (buf[6] + 5);
		mEQData[2] = (byte) (buf[5] + 5);
		mEQData[3] = (byte) (buf[4] + 5);
		mEQData[4] = (byte) (buf[3] + 5);
		mEQData[5] = buf[2];
		super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
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
		byte ampm = 0;
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		byte Am = 0;
		if ("12".equals(strTimeFormat)) {	
			if (h>12){
				Am = 1;
			}
		} else {
			ampm = 1;
		}
		
		

		byte m = (byte) curDate.getMinutes();


		byte[] buf = new byte[] { 0x0a, (byte) 0xcb, 0, 0, 0, h, m, ampm,
				Am, 0, 0, 0 };

		sendDataToCanbox(buf, buf.length);

	}
}
