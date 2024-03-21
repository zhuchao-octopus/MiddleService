package com.zhuchao.android.car.cartype.raise;

import java.util.Date;

import android.os.Handler;
import android.os.Message;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;


public class BMWRaise extends Canbox{

	public BMWRaise(){
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x01, 0x2,
				0x3, 0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x02, 0x0,
				0x0, 0x0, 0x1 });
		

		mIdKey = 0x27;
		MAP_KEYS = KEYS_WHEEL;		
		mIdKey3 = 0x021129;
		MAP_KEYS3 = KEYS_WHEEL3;
		

		buildCmdRepeatSendCarType(getCarTypeCmd());
	}
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0x85, 0x01, 0 };
		switch(CarUtil.getModelId()){
		case 31:
			cmd [2] = 1;
			break;
		case 32:
			cmd [2] = 2;
			break;
		case 33:
			cmd [2] = 3;
			break;
		case 34:
			cmd [2] = 5;
			break;
		case 35:
			cmd [2] = 6;
			break;
		case 36:
			cmd [2] = 7;
			break;
		case 10:
			cmd [2] = 4;
			break;
		default:
			return null;
		}
		return cmd;
	}
	
	private final static byte[][] KEYS_WHEEL3 = {

		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
	};
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.AUDIO },
		{ 0x2, MyCmd.Keycode.HOME },
		{ 0x3, MyCmd.Keycode.BT },
		{ 0x4, MyCmd.Keycode.BACK },
		{ 0x5, MyCmd.Keycode.SETUP },
		{ 0x6, MyCmd.Keycode.PREVIOUS },
		{ 0x7, MyCmd.Keycode.NEXT },
		{ 0x8, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x9, MyCmd.Keycode.PREVIOUS },
		{ 0xa, MyCmd.Keycode.NEXT },
		{ 0xb, MyCmd.Keycode.AUX_IN },
		{ 0xc, MyCmd.Keycode.RADIO },
		{ 0xd, MyCmd.Keycode.NAVIGATION },
		{ 0x11, MyCmd.Keycode.ROLL_PREV },
		{ 0x12, MyCmd.Keycode.ROLL_NEXT },
		{ 0x13, MyCmd.Keycode.PREVIOUS },
		{ 0x14, MyCmd.Keycode.NEXT },
		{ 0x15, MyCmd.Keycode.PREVIOUS },
		{ 0x16, MyCmd.Keycode.NEXT },
	};
	
	private void parseWheelKey(byte[] data, int len)
	{
		if (doKeyStudy(data[2], data[3])){
			return;
		}
		switch (data[2]) {
		case 0x0:
			doKey(0, 0);
			break;
		case 0x1:
			doKey(AK_KEYPAD_VOLUME_A, data[3]); //vol+
			break;
		case 0x2:
			doKey(AK_KEYPAD_VOLUME_D, data[3]);//vol-
			break;
		case 0x3:
			doKey(KEY_NEXTSONG, data[3]);
			break;
		case 0x4:
			doKey(KEY_PREVIOUSSONG, data[3]);
			break;
		case 0x5:
			doKey(KEY_BT, data[3]);
			break;
		case 0x6:
			doKey(AK_KEYPAD_MUTE_FAKE, data[3]);//mute
			break;
		case 0x7:
			doKey(KEY_MODE, data[3]);
			break;	
		case 0x8:
			doKey(KEY_MIC, data[3]);
			break;	
		case 0x9:
			doKey(MyCmd.Keycode.BT_DIAL, data[3]);
			break;	
		case 0xa:
			doKey(MyCmd.Keycode.BT_HANG, data[3]);
			break;	
		case 0xb:
			doKey(KEY_MIC, data[3]);
			break;	

		case 0x16:
			doKey(MyCmd.Keycode.PLAY_PAUSE, data[3]);
			break;	
		case 0x21:
			doKey(MyCmd.Keycode.NUMBER1, data[3]);
			break;	
		case 0x22:
			doKey(MyCmd.Keycode.NUMBER2, data[3]);
			break;	
		case 0x23:
			doKey(MyCmd.Keycode.NUMBER3, data[3]);
			break;	
		case 0x24:
			doKey(MyCmd.Keycode.NUMBER4, data[3]);
			break;	
		case 0x25:
			doKey(MyCmd.Keycode.NUMBER5, data[3]);
			break;	
		case 0x26:
			doKey(MyCmd.Keycode.NUMBER6, data[3]);
			break;		
		case 0x27:
			doKey(MyCmd.Keycode.NUMBER7, data[3]);
			break;	
		case 0x28:
			doKey(MyCmd.Keycode.NUMBER8, data[3]);
			break;
		}
	}
	
	private void parseACInfo(byte[] data, int len)
	{
		if ((data[4]&0xff) == 0xfe){
			data[4] = (byte)0xff;
		} else if(data[4] > 0){
//			data[4] = (byte)((17.5f + (0.5f * data[4]))*2);
		}
		if ((data[5]&0xff) == 0xfe){
			data[5] = (byte)0xff;
		} else if(data[5] > 0){
//			data[5] = (byte)((17.5f + (0.5f * data[5]))*2);
		}
		byte[]	airData = new byte[8];
		airData[0] = (byte) (data[2] & 0xff);
		airData[1] = (byte) (data[3] & 0xff);
		airData[2] = (byte) (data[4] & 0xff);
		airData[3] = (byte) (data[5] & 0xff);
		airData[4] = (byte) (data[6] & 0xff);
		boolean airControl = (data[3] & 0x10) != 0;
		/*(data[2] & 0x80) != 0 && */

		Handler handler = getHandler("CanService");
		if (airControl && null != handler) {		
			handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
		}
	}
	
	private void checkHideRadar() {
		mHandler.removeMessages(HIDE_RADAR);
		mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
	}
	private final static int HIDE_RADAR = 0;
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == HIDE_RADAR) {
				RadarManager.stop();
			}
			super.handleMessage(msg);
		}
	};
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x20: {
			parseWheelKey(data, len);
		}
			break;
		case 0x21: {
			parseACInfo(data, len);
		}
			break;
		case 0x22: // Radar back
		{
			byteArrayCopy(mRadar, data, 0, 2, 4);

			if(!Util.isZero(mRadar)){
				RadarManager.start(mContext);
				checkHideRadar();
			}
			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
			}
		}
			break;
		case 0x23: // Radar front
		{
			byteArrayCopy(mRadar, data, 4, 2, 4);
			if(!Util.isZero(mRadar)){
				RadarManager.start(mContext);
				checkHideRadar();
			}
			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
			}
		}
			break;
		case 0x25: // Radar status
		{
			byte[] status = new byte[2];
			status[0] = data[2];
			status[1] = data[3];

			Handler handler = getHandler("Reverse");
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_STATUS,
						status));
			}
		}
			break;
		case 0x26:
		{
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu
				

//				int angle = (a * 300 / 10000); //old pro is wrong?
				int angle = (a * 300 / 540);
				
				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle, 10));
			}
		}
		break;		
		case 0x30:{
			byte[] version = new byte[16];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		case 0x41:{
			switch(data[2]){
			case 1:{
				if (mDoorStatus != (byte)(data[3] & 0x1F)){
					mDoorStatus = (byte)(data[3] & 0x1F);
					Handler handler = getHandler("CanService");
					if(null != handler)
					{
						handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));
						
					}
				}
			}
				break;
			case 2:
				if(data.length>0xd){
					int i = (short) (((data[9] & 0xff) << 8) | (data[10] & 0xff));
				
				updateOutDoorTemp(i);
				
				}
				break;
			}			
		}
		sendCanboxInfo("com.canboxsetting", data);
			break;
		default:
			super.parseCanboxData(data, len);
			break;
		}
		

	}
	private byte mDoorStatus = 0;
	
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
		
		if (CarUtil.mIsNeedSendEQ) {

			Util.doSleep(1);
			byte[] buf = new byte[4];
			buf[0] = (byte) 0xa0;
			buf[1] = 0x2;
			buf[2] = 0x0;
			buf[3] = (byte) volume;
			
			sendDataToCanbox(buf, buf.length);
		}
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
		byte []buf = new byte[] { (byte) 0xa6, 0x7, 0x10, y, mon, d, h,
				m, s };
		
		sendDataToCanbox(buf, buf.length);
		
//		byte format = 0;
//		String strTimeFormat = Settings.System.getString(
//				mContext.getContentResolver(),
//				android.provider.Settings.System.TIME_12_24);
//
//		if ("12".equals(strTimeFormat)) {
//			if (h>=12){
//				ampm = (byte)0x80;
//			}
//		}
//
//		buf = new byte[] { (byte) 0xa6, 0x2, 0x11, y, mon, d, h,
//				m, s };
//		
//		sendDataToCanbox(buf, buf.length);
	}
}
