package com.zhuchao.android.car.cartype.ods;

import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.OSProManager;


public class JiangHuaiOD extends Canbox{

	public JiangHuaiOD(){

		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x4);
		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x4);
		buildCmdAngle((byte) 0x30, (byte) 0x0, 12000);

		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdAC = 0x11;
		mIdKey = 0x21;
		if (CarUtil.getModelId() == 5 || CarUtil.getModelId() == 16
				|| CarUtil.getModelId() == 13) {
			MAP_KEYS = KEYS_WHEEL_R3;
		} else {
			MAP_KEYS = KEYS_WHEEL;
		}
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x38, 0x39,0x40	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0xa7, 0x2, 0x11, 0 };// x80
		if (CarUtil.getModelId() == 13) {
			if (CarUtil.getCarTypeConfig() == 0) {
				cmd[3] = 0;
			} else {
				cmd[3] = 0x1;
			}
		} else {
			return null;
		}
		return cmd;
	}
	private static final byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x9, MyCmd.Keycode.BT_DIAL },
		{ 0xa, MyCmd.Keycode.MULT_MUTE_AND_HANG },
		{ 0xb, MyCmd.Keycode.PREVIOUS },
		{ 0xc, MyCmd.Keycode.NEXT },
		{ 0x12, MyCmd.Keycode.SPEECH },
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
		{ (byte)0x8b, MyCmd.Keycode.SETUP },
	};
	
	private static final byte[][] KEYS_WHEEL_R3 = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x9, MyCmd.Keycode.BT_DIAL },
		{ 0xa, MyCmd.Keycode.MULT_MUTE_AND_HANG },
		{ 0xb, MyCmd.Keycode.PREVIOUS },
		{ 0xc, MyCmd.Keycode.NEXT },
		{ 0x12, MyCmd.Keycode.SPEECH },
		{ (byte)0x80, MyCmd.Keycode.POWER },
		{ (byte)0x81, MyCmd.Keycode.HOME },
		{ (byte)0x82, MyCmd.Keycode.BACK },
		{ (byte)0x83, MyCmd.Keycode.AUDIO },
		{ (byte)0x84, MyCmd.Keycode.RADIO },
		{ (byte)0x85, MyCmd.Keycode.BT },
		{ (byte)0x86, MyCmd.Keycode.NAVIGATION },
		{ (byte)0x87, MyCmd.Keycode.MUTE },
		{ (byte)0x88, MyCmd.Keycode.KEY_DISPLAY },
		{ (byte)0x89, MyCmd.Keycode.BT },
		{ (byte)0x8a, MyCmd.Keycode.NEXT },
		{ (byte)0x8b, MyCmd.Keycode.SETUP },
	};

	@Override
	public int getAngleValue2(byte[] data) {

		int angle = (short) ((data[3] & 0xff) | (((data[2] & 0x7f)) << 8));

		if (((data[2] & 0x80)) != 0) {
			angle = -angle;
		}
		return angle;
	}
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 30){
			data = (byte)0xff;
		} else if ((data&0xff) == 0){

		} else if ((data&0xff) >= 1 && (data&0xff) <= 29){
			data = (byte)(34 + (data&0xff));
		} else {
			data = (byte)0xfa;
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[9];
		
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
		
		if ((data[4] & 0x0f) != 0){
			airData[0] |= 0x80;
		} 
		
		if (data[1] == 8){
			airData[4] = (byte) (data[8] & 0x33);
			airData[8] = (byte) ((data[9] & 0x30) 
					| ((data[9] & 0x03) << 2));	
		}
		
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
	

	
	@Override
	public void setVolume(int volume) {

		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
		sendDataToCanbox(data, data.length);
	}
	
	public int getOutTemp(byte[] data) {//
		short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
		return t;
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



		byte m = (byte) curDate.getMinutes();

		int year = curDate.getYear() + 1900;
		byte y = (byte) (year & 0xff);
		byte mon = (byte)( ((((curDate.getMonth() + 1)) & 0xff) << 4) | ((year & 0xf00)>>8));
		byte d = (byte) curDate.getDate();


		byte[] buf = new byte[] { (byte) 0x82, 0x06, y, mon, d, h, m, 0};
		sendDataToCanbox(buf, buf.length);
	}
	

	private final static int AUTO_W = 1024;
	private final static int AUTO_H = 600;

	public void touchInReverseEx(int x, int y, int w, int h, int down) {
		if (CarUtil.getModelId() == 13) {
			if (w == 0) {
				w = AUTO_W;
			}
			if (h == 0) {
				h = AUTO_H;
			}
			int x1 = (x * AUTO_W) / w;
			int y1 = (y * AUTO_H) / h;
			byte[] buf;

			buf = new byte[] { (byte) 0xa8, 0x6, (byte) down,
					(byte) ((x1 & 0xff00) >> 8), (byte) (x1 & 0xff),
					(byte) ((y1 & 0xff00) >> 8), (byte) (y1 & 0xff), 0 };

			sendDataToCanbox(buf, buf.length);
		}
	}
}
