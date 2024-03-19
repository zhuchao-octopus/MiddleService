package com.my.cartype.binarytek;

import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class ZongTaiBNR extends Canbox{

	public ZongTaiBNR(){
		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdRadarFront((byte) 0x72, (byte) 0x0, (byte) 9);
		buildCmdRadarBack((byte) 0x72, (byte) 0x0, (byte) 9);
		buildCmdRadarBackEx((byte)4);
		buildCmdAngle((byte) 0x29, (byte) 0x0, 0x2198);
		buildCmdOutTemp((byte) 0x27, (byte) 0x10);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdAC = 0x21;
		mIdKey = 0x20;
		
		
				
		MAP_KEYS = KEYS_WHEEL;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}

	private final static byte IDS_TO_CANBOXSETTING[] = { 0x71, 0x70, 0x73, 0x68, 0x6a, 0x74	};
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.KEY_SEEK_NEXT },
		{ 0x4, MyCmd.Keycode.KEY_SEEK_PREV },

		{ 0x5, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0xa, MyCmd.Keycode.BT_HANG },
		{ 0x9, MyCmd.Keycode.BT_DIAL },

		{ 0xb, MyCmd.Keycode.SPEECH },
	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0x85, 0x01, 0 };
		switch(CarUtil.getModelId()){
		case 0:
			cmd [2] = 1;
			break;
		default:
			return null;
		}
		return cmd;
	}
	@Override
	public int getAngleValue(byte[] data) {
		int angle = ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));
		int max;
	

			angle = 0x8000 - angle;
			angle = -angle;
			max = (0xa3c5 - 0x8000);
		
		
		angle = ((angle * 3000) / max);
		if (angle > -50 && angle < 50) {
			angle = 50;
		}
		
		return angle;
		
		
	}

	public byte getACTempPriv(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 1) {
			data = (byte) 0;
		} else if ((data & 0xff) == 0) {
			data = (byte) 0xfa;
		}  else if ((data & 0xff) == 0x29) {
			data = (byte) 0xff;
		} else {
			data = (byte) (37 + (((data & 0xff) -3 )/2) );
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0xe0) 
				| ((data[2] & 0x10) >> 4)
				| ((data[2] & 0x08) >> 2)
				| ((data[2] & 0x04) << 2)
				| ((data[3] & 0x10) >> 2));	
		
		airData[1] = (byte) ((data[3] & 0x07)
				| ((data[3] & 0x80) >> 1)
				| ((data[3] & 0x40) >> 1)
				| ((data[3] & 0x20) << 2));				


		
		if ((data[4] & 0xff) != 2) {
			airData[7] = 0x40;
			airData[2] = (byte) (data[5] & 0xff);
			airData[3] = (byte) (data[8] & 0xff);
		} else {
			airData[2] = getACTempPriv((byte) (data[5] & 0xff));
			airData[3] = getACTempPriv((byte) (data[8] & 0xff));
		}
		
		
		airData[4] = (byte) ((data[7] & 0x33) 
				| ((data[2] & 0x02) << 1));	
		

		airData[7] |= (byte) (((data[2] & 0x01)));	
		
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
	

	


	public int getUpdateTime() {
		return 60000;
	}
	public void updateTime() {
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);


		

		
		if ("12".equals(strTimeFormat)) {
			if (h > 12) {
				h -= 12;
			} else if (h == 0) {
				h = 12;
			}
			h |= 0x80;
		} else {
		}

		byte m = (byte) curDate.getMinutes();

		byte[] buf = new byte[] { (byte) 0xe1, 0x02, h, m };

		sendDataToCanbox(buf, buf.length);
	}
}
