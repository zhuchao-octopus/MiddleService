package com.my.cartype.ods;

import java.util.Date;

import com.common.util.MyCmd;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;

public class YeMaOD extends Canbox {

	public YeMaOD() {

		buildCmdVersion((byte) 0x30, (byte) 0x0);
		
		buildCmdDoor((byte) 0x15, (byte) 0x0, (byte) 0x3f, (byte) 0x03);
		buildCmdRadarFront((byte) 0x14, (byte) 0x0, (byte) 0x4);
		buildCmdRadarBack((byte) 0x13, (byte) 0x0, (byte) 0x4);
		buildCmdAngle((byte) 0x16, (byte) 0x5, 0x1580);

		

		mIdKey = 0x21;
		if (CarUtil.getCarTypeConfig() == 2){
			MAP_KEYS = KEYS_WHEEL_HIGH;
		} else {
			MAP_KEYS = KEYS_WHEEL;
		}


		mIdKey2 = 0x11;
		MAP_KEYS2 = KEYS_WHEEL2;
		mIdKey3 = 0x12;
		MAP_KEYS3 = KEYS_WHEEL3;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = {0x1a};

	private final static byte KEYS_WHEEL[][] = {
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x13, MyCmd.Keycode.MUTE },
		{ 0x14, MyCmd.Keycode.PREVIOUS },
		{ 0x15, MyCmd.Keycode.NEXT },
		{ 0x16, MyCmd.Keycode.RADIO },
		{ 0x17, MyCmd.Keycode.POWER },
		{ 0x1b, MyCmd.Keycode.SETUP },
		{ 0x1e, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x1f, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x22, MyCmd.Keycode.ROLL_PREV },
		{ 0x23, MyCmd.Keycode.ROLL_NEXT },
		{ 0x24, MyCmd.Keycode.AS },
		
	};
	
	private final static byte KEYS_WHEEL_HIGH[][] = {
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x13, MyCmd.Keycode.AS },
		{ 0x14, MyCmd.Keycode.NAVIGATION },
		{ 0x15, MyCmd.Keycode.BT },
		{ 0x16, MyCmd.Keycode.RADIO },
		{ 0x17, MyCmd.Keycode.POWER },
		{ 0x1b, MyCmd.Keycode.MUTE },
		{ 0x1e, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x1f, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x22, MyCmd.Keycode.ROLL_PREV },
		{ 0x23, MyCmd.Keycode.ROLL_NEXT },
		{ 0x24, MyCmd.Keycode.AS },
		
	};
	
	private final static byte KEYS_WHEEL2[][] = {

		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x4, MyCmd.Keycode.NEXT },
		{ 0x3, MyCmd.Keycode.PREVIOUS },
		{ 0x6, MyCmd.Keycode.BT },
		{ 0x8, MyCmd.Keycode.MUTE },
		{ 0x5, MyCmd.Keycode.MODLE },
		{ 0x7, MyCmd.Keycode.SPEECH },


	};
	
	private final static byte KEYS_WHEEL3[][] = {

		{ 0x1, MyCmd.Keycode.AS },
		{ 0x2, MyCmd.Keycode.RADIO },
		{ 0x3, MyCmd.Keycode.MODLE },
		{ 0x4, MyCmd.Keycode.MUTE },
		{ 0x5, MyCmd.Keycode.NAVIGATION },
		{ 0x6, MyCmd.Keycode.POWER },
		{ 0x7, MyCmd.Keycode.KEYAMS_RPT },
		{ 0x8, MyCmd.Keycode.BT },
		{ 0x9, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0xa, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0xb, MyCmd.Keycode.ROLL_NEXT },
		{ 0xc, MyCmd.Keycode.ROLL_PREV },
		{ 0xd, MyCmd.Keycode.ROLL_PREV },
		{ 0xe, MyCmd.Keycode.ROLL_NEXT },
		{ 0xf, MyCmd.Keycode.BACK },
		{ 0x10, MyCmd.Keycode.HOME },
		{ 0x11, MyCmd.Keycode.SETUP },
		{ 0x12, MyCmd.Keycode.AUDIO },
		{ 0x13, MyCmd.Keycode.MODLE },


	};

	@Override
	public int getAngleValue2(byte[] data) {
		// TODO Auto-generated method stub
		int angle = (short) ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));
		angle = angle - 0x1e50;
		return -angle;		
	}
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 0){
			
		} else if ((data&0xff) == 0x1f){
			data = (byte)0xff;
		} else {
			data = (byte)(31 + (data));
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0xef));	
		airData[1] = (byte) ((data[3] & 0xef));		
		

		airData[4] = (byte) (((data[2] & 0x10) >> 2)
				| ((data[7] & 0x33) >> 0));			
		
		

		airData[2] = (byte) (data[5] & 0xff);
		airData[3] = (byte) (data[6] & 0xff);
		

		

		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x24:
			if ((data[2] & 0x1) == 0) {
				data[2] = 0;
			}
			break;
		}
		super.parseCanboxData(data, len);
	}

	public int getOutTemp(byte[] data) {//
		short t = 0;
		if (data.length > 3) {

			t = (short) (((data[3] & 0xff) | ((data[2] & 0xff) << 8)));
		}
		return t * 5;
	}
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}



	public void setMediaSrc(int source, byte type, byte[] b) {

	}

	public void setMediaSrc(int source) {

	}
	
	public int getUpdateTime() {
		return 60000;
	}

	public void updateTime() {
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);

		byte m = (byte) curDate.getMinutes();

		byte[] buf = new byte[] { (byte) 0x85, 0x02, h, m };

		sendDataToCanbox(buf, buf.length);
	}
}
