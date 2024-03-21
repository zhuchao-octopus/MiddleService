package com.zhuchao.android.car.cartype.daojun;

import java.util.Calendar;
import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class RongWeiI5DaoJun extends Canbox{

	public RongWeiI5DaoJun(){
		buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

		buildCmdVersion((byte) 0x30, (byte) 0x0);

		buildCmdAngle((byte) 0x26, (byte) 0x0, 6446);
		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0x7);
//		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0x4);
		
		mIdAC = 0x3;
		mIdKey = 0x1;
		MAP_KEYS = KEYS_WHEEL;	
		mIdKey2 = 0x27;
		MAP_KEYS2 = KEYS_WHEEL2;		
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
	}

	private final static byte[] IDS_TO_CANBOXSETTING = { 0x6, 0x16 };

	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0x85, 0x02, 1,1 };	
		return cmd;
	}
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.PREVIOUS },
		{ 0x4, MyCmd.Keycode.NEXT },
		{ 0x5, MyCmd.Keycode.MODLE },
		{ 0x6, MyCmd.Keycode.SPEECH },
		{ 0x7, MyCmd.Keycode.MUTE },
		{ 0x24, MyCmd.Keycode.BT },
		{ 0x25, MyCmd.Keycode.HOME },
		
	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x5, MyCmd.Keycode.POWER },
	};

	@Override
	public int getAngleValue(byte[] data) {
		// TODO Auto-generated method stub
		int angle = (data[2] & 0xff) | ((data[3] & 0xff) << 8);
		int max;
		
		if (angle > 32749) {
			max = 40330 - 32749;
		} else {
			max = 32749 - 25164;
		}
		
		angle = angle - 32749;
		
		angle = ((angle * 3000) / max);
		if (angle > -50 && angle < 50) {
			angle = 50;
		}
		
		return angle;

	}
	
	private int getACTempPriv(byte data) {
		// TODO Auto-generated method stub
		if (((data & 0xff)) == 1) {
			data = 0;
		} else if (((data & 0xff)) == 0x0f) {
			data = (byte) 0xff;
		} else if (((data & 0xff)) >= 2 && ((data & 0xff)) <= 0xe) {
			data = (byte) (32 + (data - 0x2)*2);
		} else {
			data = (byte) 0xfa;
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{
		
		byte[]	airData = new byte[10];
		
			switch ((data[3] & 0xf) >> 0) {
			case 1:
				airData[9] = 1;
				break;
			case 2:
				airData[1] = (byte) (0x80);
				break;
			case 3:
				airData[1] = (byte) (0x20);
				break;
			case 4:
				airData[1] = (byte) (0x60);
				break;
			case 5:
				airData[1] = (byte) (0x40);
				break;
			case 6:
				airData[1] = (byte) (0xa0);
				break;
			case 7:
				airData[1] = (byte) (0x80);
				break;
			case 8:
				airData[1] = (byte) (0xa0);
				break;
			case 9:
				airData[1] = (byte) (0xe0);
				break;
			default:
				airData[1] = 0;
				break;
			}
		

		airData[1] |= (byte) ((data[2] & 0x07)
				| (data[3] & 0x10) >> 1);
		


		airData[0] = (byte) (((data[2] & 0x10) >> 4) 					
				| ((data[2] & 0xe0) << 0)
				| ((data[3] & 0x20) >> 3)
				| ((data[2] & 0x80) >> 0)
				| ((data[8] & 0x04) << 2));		



//		airData[9] = (byte) ((data[2] & 0x1));
		if (CarUtil.getModelId() == 32){
			airData[7] |= 0x40;
			airData[2] = data[4];
		} else {
			airData[2] = (byte) getACTempPriv(data[4]);
		}
		
		airData[3] = (byte)0xfa;


		airData[4] = data[6];
		
		
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
		Calendar c = Calendar.getInstance();

		byte h = (byte) c.get(Calendar.HOUR_OF_DAY);
		h = fixTimeHour(h);

		byte m = (byte) c.get(Calendar.MINUTE);

		byte y = (byte) (c.get(Calendar.YEAR) - 2000);
		byte mon = (byte) (c.get(Calendar.MONTH) + 1);
		byte d = (byte) c.get(Calendar.DAY_OF_MONTH);

		
		byte[] buf = new byte[] { (byte) 0x84, 8, y, mon, d, h, m, 0, 0, 0 };

		sendDataToCanbox(buf, buf.length);


	}


}
