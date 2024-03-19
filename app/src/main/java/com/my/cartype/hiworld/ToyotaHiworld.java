package com.my.cartype.hiworld;

import java.util.Date;
import java.util.Locale;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;


public class ToyotaHiworld extends Canbox{

	public ToyotaHiworld(){
		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x11, (byte) 0x1, (byte) 0xfc, (byte) 0x6);
		buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 4, (byte) 4, (byte) 3);
		buildCmdRadarFrontEx((byte) 4, (byte)1);
		buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 4, (byte) 4, (byte) 3);
		buildCmdRadarBackEx((byte) 0, (byte)1);
		buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
		buildCmdEQ((byte) 0xa6, (byte) 0x1, 6);
		buildCmdOutTemp((byte) 0x31, (byte) 0x10);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);
		mIdAC = 0x82;
		mIdKey = 0x040111;
		MAP_KEYS = KEYS_WHEEL;
		mIdKey2 = 0x21;
		MAP_KEYS2 = KEYS_WHEEL2;
		mIdKey3 = 0x021122;
		MAP_KEYS3 = KEYS_WHEEL3;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}


	@Override
	public void stopConnect() {

	}
	private final static byte IDS_TO_CANBOXSETTING[] = { 0x12, 0x13, 0x16,
			0x17, 0x1f, 0x48, 0x62, 0x32, (byte)0xa6 };
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x4, MyCmd.Keycode.SPEECH },
		{ 0x5, MyCmd.Keycode.BT_DIAL },
		{ 0x6, MyCmd.Keycode.BT_HANG },
		{ 0x9, MyCmd.Keycode.NEXT },
		{ 0x8, MyCmd.Keycode.PREVIOUS },
		{ 0xc, MyCmd.Keycode.MODLE },

		{ 0xe, MyCmd.Keycode.NEXT },
		{ 0xd, MyCmd.Keycode.PREVIOUS },
		
		{ 0xf, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x10, MyCmd.Keycode.BACK },
//		{ 0xd, MyCmd.Keycode },
	};
	
	private final static byte KEYS_WHEEL2[][] = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x6, MyCmd.Keycode.BACK },
		{ 0x20, MyCmd.Keycode.NAVIGATION },
		{ 0x2a, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x2b, MyCmd.Keycode.HOME },
		{ 0x2c, MyCmd.Keycode.MODLE },
		{ 0x2f, MyCmd.Keycode.MENU },
		{ 0x30, MyCmd.Keycode.BT },
		{ 0x33, MyCmd.Keycode.RADIO },
		{ 0x39, MyCmd.Keycode.KEY_DISPLAY },
		

		{ 0x4b, MyCmd.Keycode.RADIO },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x2, MyCmd.Keycode.PREVIOUS },
		{ 0x28, MyCmd.Keycode.BT },
	};
	
	private final static byte KEYS_WHEEL3[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
	};
	
	public void parseCanboxData(byte[] data, int len) {
		switch (data[0]) {
		case 0x22:
//			if (data[3] < 0) {
//				data[3] = (byte) (-data[3]);
//				data[2] += 0x10;
//			}
			parseWheelKey(mIdKey3, data, MAP_KEYS3);
			break;
		case 0x31:
			parseACInfo2(data);
		default:
			super.parseCanboxData(data, len);
		}	
	}

	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x2, 0x24, 0, 0x01 };
		switch (CarUtil.getModelId()) {
		case 9:
			cmd[2] = (byte)0x81;
			break;
		case 0:
			cmd[2] = (byte)0x82;
			break;
		case 75:
			cmd[2] = (byte)0x83;
			break;
		case 76:
			cmd[2] = (byte)0x84;
			break;
		case 31:
			cmd[2] = (byte)0x85;
			break;
		case 56:
			cmd[2] = (byte)0x86;
			break;
		case 34:
			cmd[2] = (byte)0x87;
			break;
		case 67:
			cmd[2] = (byte)0x88;
			break;
		case 46:
			cmd[2] = (byte)0x89;
			break;
		case 68:
			cmd[2] = (byte)0x8a;
			break;
		case 69:
			cmd[2] = (byte)0x8b;
			break;
		case 30:
			cmd[2] = (byte)0x8c;
			break;
		case 22:
			cmd[2] = (byte)0x8d;
			break;
		case 35:
			cmd[2] = (byte)0x8e;
			break;
		case 73:
			cmd[2] = (byte)0x8f;
			break;
		case 7:
			cmd[2] = (byte)0x90;
			break;
		case 57:
			cmd[2] = (byte)0x91;
			break;
		case 79:
			cmd[2] = (byte)0x92;
			break;
		case 87:
			cmd[2] = (byte)0x93;
			break;
		case 85:
			cmd[2] = (byte)0x94;
			break;
		case 82:
			cmd[2] = (byte)0x95;
			break;
		case 88:
			cmd[2] = (byte)0x96;
			break;
		case 89:
			cmd[2] = (byte)0x97;
			break;
		default:
			return null;
		}
		return cmd;
	}
	@Override
	public int getAngleValue2(byte[] data) {

		short angle = (short) ((data[9] & 0xff)
				| ((data[8] & 0xff) << 8));
		
		return -angle;
		
		
	}

	private int getACTemp1(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0) {
			data = (byte) 0xfa;
		} else if ((data & 0xff) == 1) {
			data = 0;
		}  else if ((data & 0xff) == 0xff) {
			//data = 0;
		} else {
			data = (byte)((data&0xff) - 80);
		}
		return data&0xff;
	}
	
	private int getACTemp2(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0xfe) {
			data = 0;
		}  else if ((data & 0xff) == 0xff) {
			//data = 0;
		} else {
			
		}
		return data&0xff;
	}
	
//	private byte[]	airData = new byte[12];
	public void parseACInfo(byte[] data)
	{		
		byte[]	airData = new byte[12];
		airData[0] = (byte) ((data[2] & 0x08) 
				| ((data[2] & 0x40) << 1)
				| ((data[3] & 0x20) >> 5)
				| ((data[3] & 0x08) << 2)
				| ((data[3] & 0x44) >> 0)
				| ((data[3] & 0x10) >> 3));				
		
		airData[4] = (byte) (
				 ((data[3] & 0x80) >> 5));	

		airData[7] = (byte) (
				((data[3] & 0x02) >> 1));	
		

		airData[1] = (byte) (
				((data[6] & 0x10) << 3)
				| ((data[6] & 0x20) << 1)
				| ((data[6] & 0x40) >> 1)
				| ((data[6] & 0x0f) >> 0));			



		airData[2] = (byte) getACTemp1(data[4] );
		airData[3] = (byte) getACTemp1(data[5] );
		

		switch((data[7] & 0xff)){
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
		airData[11] |= (byte) (data[8] & 0x0f);
		

		airData[10] = (byte)getACTemp(data[9]);
		
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	

	public void parseACInfo2(byte[] data)
	{		
		byte[]	airData = new byte[12];
		airData[0] = (byte) ((data[2] & 0x08) 
				| ((data[2] & 0x40) << 1)
				| ((data[3] & 0x10) << 1)
				| ((data[3] & 0x44) >> 0)
				| ((data[4] & 0x10) >> 3)
				| ((data[4] & 0x20) >> 5));				
					
		
		airData[4] = (byte) (
				 ((data[3] & 0x80) >> 4)
				 |((data[2] & 0x20) >> 3)
				 |((data[3] & 0x08) << 4)
				 |((data[4] & 0x03) << 4)
				 |((data[4] & 0x0c) >> 2));	


		
		airData[8] = (byte) (
				 ((data[5] & 0xc0) >> 4)
				 |((data[5] & 0x30) >> 0));	
		
//		airData[7] = (byte) (
//				((data[3] & 0x02) >> 1));	
		

		switch ((data[6] & 0xf)) {
		case 1:
			airData[9] = (byte) (0x1);
			break;
		case 3:
			airData[1] = (byte) (0x20);
			break;
		case 5:
			airData[1] = (byte) (0x60);
			break;
		case 6:
			airData[1] = (byte) (0x40);
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
		
		airData[1] |= (byte) (
				((data[7] & 0x0f) >> 0));	
		


		
		switch ((data[10] & 0xf)) {
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

		airData[11] |= (byte) (data[12] & 0x0f);
		
		airData[2] = (byte) getACTemp2(data[8]);
		airData[3] = (byte) getACTemp2(data[9]);	

		

		airData[10] = (byte)getACTemp2(data[12]);
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}
	public void setMediaSrc(int source, byte type, byte []b){
		int freq = (int) ((b[1] & 0xff) | ((b[2] & 0xff) << 8));
		switch (source) {
		case MyCmd.SOURCE_RADIO: {
			String s;
			if (b[0] >= 0x10) { // am
				s = "" + freq;
				type = 4;
			} else {

				s = String.format("%d.%d", (freq) / 100, (freq/10) % 10, Locale.ENGLISH);
//				if (freq < 10000) {
//					s = "" + s;
//				}
				type = 1;
			}
			sendLcdInfo(type, s, true);
		}
			break;
		}
	}

	public void setMediaSrc(int source) {
		byte s;
		switch (source) {
//		case MyCmd.SOURCE_RADIO:
//			return;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0xd;
			break;
		case MyCmd.SOURCE_BT:
			s = (byte)0x85;
			break;
		default:
			return;
		}

		sendLcdInfo(s, null, false);
	}		

	@Override
	public void setVolume(int volume) {
		String s = volume + "";
		sendLcdInfo((byte) 0x20, s, false);
	}

	public void sendLcdInfo(byte index, String num, boolean end) {

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = num.getBytes();

			int num_len = n.length;

			if (num_len >= (12)) {
				num_len = (12);
			}
			byte[] data;

			int len = 12 + 3;

			data = new byte[len];

			data[0] = (byte) (13);
			data[1] = (byte) 0x91;
			data[2] = (byte) index;
			if (!end) {
				for (int i = 0; i < num_len; ++i) {
					data[3 + i] = n[i];
				}
			} else {
				for (int i = 0; i < num_len; ++i) {
					data[data.length - i - 1] = n[num_len - i - 1];
				}
			}

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

		}
	}
	
	public void sendId3(byte index, String num) {

		try {
			if (num == null) {
				num = "";
			}

			byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

			int num_len = n.length;
			if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
				num_len -= 2;
			}

			if (num_len >= (0x20)) {
				num_len = (0x20);
			}
			byte[] data;

			int len = 0x22;

			data = new byte[len];

			data[0] = (byte) (0x20);
			data[1] = (byte) index;
			for (int i = 0; i < num_len; ++i) {
//				if (i % 2 == 0) {
//					data[2 + i] = n[i + 3];
//				} else {
//					data[2 + i] = n[i + 1];
//				}
				data[2 + i] = n[i + 2];
			}

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

		}
	}
	
	public void setSongName(String s) {
		sendId3((byte) 0x92, s);
	}

	public void setSongAritst(String s) {
		sendId3((byte) 0x93, s);
	}

	public void setSongAlbum(String s) {
		sendId3((byte) 0x94, s);
	}
	public int getOutTemp(byte[] data) {//		
		return ((data[13])*5 - 400);
	}
	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword1(data, len);
	}
	private Handler mHandlerRepeat = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				sendEQCmd(msg.arg1, msg.arg2);
				break;
			}
			super.handleMessage(msg);
		}
	};
	byte[] mEQCmdBuf = new byte[] { 0x2, (byte) 0xad, 0x0, 0x0 };

	private void sendEQCmd(int style, int step) {
		mHandlerRepeat.removeMessages(0);
		if (step == 0) {
			return;
		}
		
		if (style == 1) {
			if (step < 0) {
				mEQCmdBuf[3] = -1;
				++step;
			} else {
				mEQCmdBuf[3] = 1;
				--step;
			}
		} else {
			if (step > 0) {
				--step;
				mEQCmdBuf[3]++;
			} else {
				++step;
				if (mEQCmdBuf[3]>0){
					mEQCmdBuf[3]--;
				} else {
					return;
				}
			}
		}
		
		sendDataToCanbox(mEQCmdBuf, mEQCmdBuf.length);
		if (step != 0) {
			mHandlerRepeat.sendMessageDelayed(mHandlerRepeat.obtainMessage(0, style, step), 100);
		}
		
	}
	
	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (63 << 16) | (15 << 8) | 11;

			byte[] buf = new byte[] { 0x3, (byte) 0x6a, 0x5, (byte) 0xa6, 0 };
			sendDataToCanbox(buf, buf.length);
		} else {
			mHandlerRepeat.removeMessages(0);
			if (mEQData == null){
				mEQData = new byte[6];
			}
			
			int step = 0;
			int style = 0;
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				mEQCmdBuf[2] = 6;
				mEQCmdBuf[3] = mEQData[0];
				step = data - mEQData[0];
				break;
			case EQ_CMD_SET_MIDDLE:
				mEQCmdBuf[2] = 5;
				mEQCmdBuf[3] = mEQData[1];
				step = data - mEQData[1];
				break;
			case EQ_CMD_SET_LOW:
				mEQCmdBuf[2] = 4;
				mEQCmdBuf[3] = mEQData[2];
				step = data - mEQData[2];
				break;
			case EQ_CMD_SET_ZONE_FR:
				mEQCmdBuf[2] = 3;
				mEQCmdBuf[3] = mEQData[3];
				step = data - mEQData[3];
				break;
			case EQ_CMD_SET_ZONE_LR:
				mEQCmdBuf[2] = 2;
				mEQCmdBuf[3] = mEQData[4];
				step = data - mEQData[4];
				break;
			case EQ_CMD_SET_VOLUME:
				mEQCmdBuf[2] = 1;
				step = data - mEQData[5];
				style = 1;
				break;
			default:
				return 0;
			}

			sendEQCmd( style, step);
		}
		return ret;
	}
	public void startConnect() {
		
	}
//	private void returnEQData(byte[] buf) {
//		byte[] data = new byte[6];
//
//		data[0] = (byte) (buf[3] & 0x0f);
//		data[1] = (byte) ((buf[4] & 0xf0) >> 4);
//		data[2] = (byte) ((buf[3] & 0xf0) >> 4);
//		data[3] = (byte) ((buf[2] & 0xf0) >> 4);
//		data[4] = (byte) (buf[2] & 0x0f);
//
//		data[5] = buf[5];
//
//		data[0] -= 2;
//		data[1] -= 2;
//		data[2] -= 2;
//		
//		super.returnEQData(EQ_CMD_SET_ALL_DATA, data);
//	}

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


		if ("12".equals(strTimeFormat)) {		
		} else {
			ampm = 1;
		}


		

		byte m = (byte) curDate.getMinutes();

		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();
		byte[] buf = new byte[] { 0x0a, (byte) 0xcb, 0, h, m, 0, 0, ampm,
				 y, mon, d, 0 };

		sendDataToCanbox(buf, buf.length);

	}
}
