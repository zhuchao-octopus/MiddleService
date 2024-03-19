package com.my.cartype.ods;

import java.util.Date;

import android.provider.Settings;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class FutianOD extends Canbox{

	public FutianOD(){
		mIdAC = 0x24;
		buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte)4);
		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte)4);
		buildCmdAngle((byte) 0x29, (byte) 0x3, 0x15c2);
		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;	
		mIdKey2 = 0x21;
		MAP_KEYS2 = KEYS_WHEEL2;		
		

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = { 0x40	};
	

	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D },

			{ 0x3, MyCmd.Keycode.NEXT },
			{ 0x4, MyCmd.Keycode.PREVIOUS }, 


			{ 0x6, MyCmd.Keycode.MUTE },
			{ 0x7, KEY_SOURCE },
			{ 0x9, MyCmd.Keycode.BT_DIAL },
			{ 0xa, MyCmd.Keycode.BT_HANG}, 


	};
	
	private final static byte KEYS_WHEEL2[][] = {

			{ 0x1, MyCmd.Keycode.PREVIOUS },
			{ 0x2, MyCmd.Keycode.NEXT },

			{ 0x6, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x7, MyCmd.Keycode.VOLUME_UP },
			{ 0x8, MyCmd.Keycode.POWER },

			{ 0x10, MyCmd.Keycode.PLAY_PAUSE },


	};
	


	
	public int getAngleValue2(byte[] data) {


		short angle = (short) ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));


		return angle;
	}





	@Override
	public int getACTemp(byte data) {
		if ((data & 0xff) == 0x1f) {
			data = (byte) 0xff;
		} else if ((data & 0xff) == 0) {
		} else {
				data = (byte) (35 + (data - 0x1));
			
		}
		return data & 0xff;
	}


	public void parseACInfo(byte[] data)
	{			

		byte[]	airData = new byte[8];
		airData[0] = (byte) (((data[2] & 0xe0)));

		switch ((data[3] & 0xff)) {
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
		}
		

		airData[1] |= (byte) (data[4] & 0x0f);
		
		airData[2] = data[5];
		airData[3] = data[6];
		


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

		byte ampm = 1;
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		if ("12".equals(strTimeFormat)) {
			ampm = 0;
		}
		
		byte []buf = new byte[] { (byte) 0xc9, 0x03, m,  h,
				ampm };
		
		sendDataToCanbox(buf, buf.length);
	}


}
