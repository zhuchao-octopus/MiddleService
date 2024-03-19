package com.my.cartype.ods;

import java.util.Date;

import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class ZhidouOD extends Canbox{

	public ZhidouOD(){
		mIdAC = 0x25;
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte)4);
		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte)4);
		buildCmdAngle((byte) 0x29, (byte) 0x3, 7800);
		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;	
		mIdKey2 = 0x21;
		MAP_KEYS2 = KEYS_WHEEL2;		

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = { 0x31	};
	

	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D },

			{ 0x3, MyCmd.Keycode.NEXT },
			{ 0x4, MyCmd.Keycode.PREVIOUS }, 


			{ 0x5, MyCmd.Keycode.BT_DIAL },
			{ 0x6, MyCmd.Keycode.MUTE },
			{ 0x7, KEY_SOURCE },
			{ 0x8, MyCmd.Keycode.BT_HANG}, 
			
			{ 0x14, MyCmd.Keycode.SPEECH },
	};
	
	private final static byte KEYS_WHEEL2[][] = {

			{ 0x1, MyCmd.Keycode.HOME },
			{ 0x2, MyCmd.Keycode.MUTE },
			{ 0x3, MyCmd.Keycode.AUX_IN },
			{ 0x4, MyCmd.Keycode.NAVIGATION },
			{ 0x5, MyCmd.Keycode.PLAY_PAUSE },
			{ 0x6, MyCmd.Keycode.BT_DIAL },
			{ 0x7, MyCmd.Keycode.PREVIOUS },
			{ 0x8, MyCmd.Keycode.NEXT },
			{ 0x9, MyCmd.Keycode.POWER },
			{ 0x10, MyCmd.Keycode.VOLUME_ROLL_UP },
			{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN },
			{ 0x12, MyCmd.Keycode.RADIO },
			{ 0x13, MyCmd.Keycode.BT },
			{ 0x14, MyCmd.Keycode.BT_HANG },
			{ 0x15, MyCmd.Keycode.SETUP },
			{ 0x16, MyCmd.Keycode.EASY_CONNECT },

	};
	


	
	public int getAngleValue2(byte[] data) {


		short angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));


		return angle;
	}





	@Override
	public int getACTemp(byte data, int unit) {
		if ((data & 0xff) == 0x1f) {
			data = (byte) 0xff;
		} else if ((data & 0xff) == 0) {
		} else {
			if (unit == 0){
				data = (byte) (32 + (data - 0x1));
			} else {
				data = (byte) (60 + (data - 0x1));
			}
		}
		return data & 0xff;
	}


	public void parseACInfo(byte[] data)
	{			

		byte[]	airData = new byte[8];
		airData[0] = (byte) (((data[2] & 0xef)));
		airData[1] = (byte) ((data[3] & 0xff));
		airData[2] = data[4];
		airData[3] = data[5];
		

		airData[5] = (byte) ((data[6] & 0x01));
		airData[7] = (byte) (((data[2] & 0x10)<<1));
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}
	

	
	public void setMediaMoreInfo(int source, int play, int total, int time, int total_time){
		
	}
	public void setMediaSrc(int source, byte type, byte []b){
		
	}
	public void setMediaSrc(int source){//default is simple box
		
	}
	
	public int getUpdateTime() {
		return 60000;
	}
	public void updateTime() {
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);


		byte m = (byte) curDate.getMinutes();


		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte)( curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();
		byte []buf = new byte[] { (byte) 0x82, 0x06, y, mon, d, h,m, 0 };
		//byte []buf = new byte[] { (byte) 0xC9, 0x06, m, h, d, mon,y, 0 };
		sendDataToCanbox(buf, buf.length);
	}


}
