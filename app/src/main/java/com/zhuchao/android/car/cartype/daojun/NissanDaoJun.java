package com.zhuchao.android.car.cartype.daojun;

import java.util.Date;

import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class NissanDaoJun extends Canbox{

	public NissanDaoJun(){
		buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdEQ((byte) 0x31, (byte) 0xff, 0);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdAC = 0x2a;

		buildCmdKey((byte) 0x11, (byte) 1, (byte) 2, (byte) 0, KEYS_WHEEL);
		buildCmdKey((byte) 0x20, (byte) 0, (byte) 2, (byte) 2, KEYS_WHEEL2);
		
	}
	
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x2, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x3, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x4, MyCmd.Keycode.KEY_AM },
		{ 0x5, MyCmd.Keycode.KEY_FM },
		{ 0x6, MyCmd.Keycode.MODLE },
		{ 0x7, MyCmd.Keycode.DVD },
		{ 0x8, MyCmd.Keycode.NEXT },
		{ 0x9, MyCmd.Keycode.PREVIOUS },
		{ 0x10, MyCmd.Keycode.MUTE },
		{ 0x11, MyCmd.Keycode.NEXT },
		{ 0x12, MyCmd.Keycode.PREVIOUS },
		{ 0x13, MyCmd.Keycode.PREVIOUS },
		{ 0x14, MyCmd.Keycode.FAST_R },
		{ 0x15, MyCmd.Keycode.KEY_SHUFFLE },
		{ 0x16, MyCmd.Keycode.NEXT },
		{ 0x17, MyCmd.Keycode.FAST_F },
		{ 0x18, MyCmd.Keycode.KEY_REPEAT },
//		{ 0x19, MyCmd.Keycode.NAVIGATION },
//		{ 0x20, MyCmd.Keycode.AUDIO },
//		{ 0x21, MyCmd.Keycode. },
//		{ 0x22, MyCmd.Keycode. },
		{ 0x23, MyCmd.Keycode.AUDIO },
		{ 0x24, MyCmd.Keycode.MODLE },
//		{ 0x25, MyCmd.Keycode. },
	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.PREVIOUS },
		{ 0x4, MyCmd.Keycode.NEXT },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x9, MyCmd.Keycode.BT_DIAL },
		{ 0xa, MyCmd.Keycode.BT_HANG },
		{ 0x15, MyCmd.Keycode.BACK },
		{ 0x16, MyCmd.Keycode.PLAY_PAUSE },
		{ (byte)0x87, MyCmd.Keycode.POWER },

	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0x72, 0x09, 0x7, 0x3, 0x1, 0x1, 0x3, 
				(byte)0xe3, (byte)0xf3, (byte)0xfb, (byte)0xfb };
		if (CarUtil.getCarTypeConfig() == 0) {
			cmd[4] = 0x2;
			cmd[9] = (byte) 0xf7;
		}
		return cmd;
	}
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 0xfe){
			data = (byte)0;
		} else {
			
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) (((data[2] & 0xf7) >> 0));		
		airData[1] = data[3];

		airData[4] = (byte) (((data[6] & 0x33) >> 0));	
		

		airData[2] = data[4];
		airData[3] = data[5];
		

		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}
	public void setMediaSrc(int source, byte type, byte []b){
	}

	public void setMediaSrc(int source) {
	}
	
	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (63 << 16) | (15 << 8) | 11;
			if (mEQData == null) {
				mEQData = new byte[6];
			}
			byte[] buf = new byte[] { (byte) 0x90, 0x1, 0x31 };
			sendDataToCanbox(buf, buf.length);
		} else {

		}
		return ret;
	}

	public void parseEQ(int cmd, byte[] buf) {
	
		mEQData[0] = (byte) (buf[3] & 0xf);
		mEQData[2] = (byte) ((buf[3] & 0xf0) >> 4);
		mEQData[3] = (byte) (buf[2] & 0xf);
		mEQData[4] = (byte) ((buf[2] & 0xf0) >> 4);
		mEQData[5] = buf[5];
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
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		byte s = 0;
		if ("12".equals(strTimeFormat)) {
			if (h > 12) {
				h -= 12;
				s |= 0x80;
			} else if (h == 0) {
				h = 12;
				s |= 0x40;
			}
		}

		byte m = (byte) curDate.getMinutes();

		byte[] buf = new byte[] { (byte) 0xc6, 0x04, 1, h, m, s };
		sendDataToCanbox(buf, buf.length);
	}
}
