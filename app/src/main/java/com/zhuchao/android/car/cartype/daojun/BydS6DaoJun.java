package com.zhuchao.android.car.cartype.daojun;

import java.util.Calendar;
import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class BydS6DaoJun extends Canbox{

	public BydS6DaoJun(){
		buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
		buildCmdDoor((byte) 0x4, (byte) 0x0, (byte) 0x3f, (byte) 0x03);

		buildCmdVersion((byte) 0x30, (byte) 0x0);

		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
		buildCmdRadarBack((byte) 0x3, (byte) 0x0, (byte) 0x3);
		buildCmdRadarFront((byte) 0x3, (byte) 0x0, (byte) 0x3);
		buildCmdRadarBackEx((byte) 0x4);
		
		mIdAC = 0x2;
		mIdKey = 0x6;
		MAP_KEYS = KEYS_WHEEL;	
		mIdKey2 = 0x1;
		MAP_KEYS2 = KEYS_WHEEL2;	
	}
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0x85, 0x02, 1,1 };	
		return cmd;
	}
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.KEY_TURN_A },
		{ 0x4, MyCmd.Keycode.KEY_TURN_D },
		{ 0x5, MyCmd.Keycode.POWER },
		{ 0x6, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x7, MyCmd.Keycode.MUTE },
		{ 0x8, MyCmd.Keycode.NAVIGATION },
		{ 0x9, MyCmd.Keycode.RADIO },
	};
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.PREVIOUS },
		{ 0x4, MyCmd.Keycode.NEXT },
		{ 0x5, MyCmd.Keycode.MODLE },
		{ 0x24, MyCmd.Keycode.BT_DIAL },
		{ 0x25, MyCmd.Keycode.BT_HANG },
	};
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if (((data&0x7e)>>1) == 0){
			data = (byte)0xfa;
		} else if (((data&0x7e)>>1) == 63){
			data = (byte)0xff;
		} else if (((data&0x7e)>>1) == 1){
			data = 0;
		} else {
			data = (byte)((((data&0x7e)>>1)*2) + (((data & 0x1) != 0)?1:0));
			
			
			
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[14];
		
		airData[0] = (byte) (((data[5] & 0x10) >> 3) 				
				| ((data[5] & 0x20) >> 3)
				| ((data[5] & 0x40) >> 2)
				| ((data[5] & 0x80) >> 7));		
		
		if (((data[5] & 0x0c)>>2) == 0x01) {
			airData[0] |= (byte) (0x20);
		} else if (((data[5] & 0x0c)>>2) == 0x03) {
			airData[4] |= (byte) (0x80);
		}
		
		if ((data[5] & 0x03) == 0x02) {
			airData[0] |= (byte) (0x40);
		} else if ((data[5] & 0x03) == 0x03) {
			airData[0] |= (byte) (0x40);
			airData[0] |= (byte) (0x10);
		}
		airData[0] |= 0x80;
		airData[4] = (byte) (((data[6] & 0x01) << 2));	
		
		switch((data[2] & 0xf0)>>4){
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
			airData[9] = (byte) (0x01);
			airData[1] = 0;
			break;
		default:
			airData[1] = 0;
			break;
		}

		airData[1] |= (byte) ((data[2] & 0x07) 

				| ((data[6] & 0x04) << 2));
		

		airData[2] = data[3];
		airData[3] = data[4];
		airData[13] = data[6];

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
		byte s = (byte) c.get(Calendar.SECOND);

		byte y = (byte) (c.get(Calendar.YEAR) - 2000);
		byte mon = (byte) (c.get(Calendar.MONTH) + 1);
		byte d = (byte) c.get(Calendar.DAY_OF_MONTH);

		byte w = (byte)(c.get(Calendar.DAY_OF_WEEK) - 1);
		
		byte[] buf = new byte[] { (byte) 0x84, 7, y, mon, d, h, m, s, w };

		sendDataToCanbox(buf, buf.length);


	}


}
