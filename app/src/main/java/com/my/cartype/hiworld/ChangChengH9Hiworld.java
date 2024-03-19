package com.my.cartype.hiworld;

import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class ChangChengH9Hiworld extends Canbox{

	public ChangChengH9Hiworld(){
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
//		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
		buildCmdEQ((byte) 0xa6, (byte) 0x1, 6);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);		
		mIdAC = 0x31;
		
		buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
		buildCmdKey((byte) 0x21, (byte) 2,  KEYS_WHEEL2);

		mIdKey3 = 0x021122;
		MAP_KEYS3 = KEYS_WHEEL3;
		
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}

	private final static byte IDS_TO_CANBOXSETTING[] = { 0x32, 0x62 };
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.MUTE },
		{ 0x4, MyCmd.Keycode.SPEECH },
		{ 0x5, MyCmd.Keycode.BT_DIAL },
		{ 0x6, MyCmd.Keycode.BT_HANG },
		{ 0x9, MyCmd.Keycode.NEXT },
		{ 0x8, MyCmd.Keycode.PREVIOUS },
		

		{ 0xd, MyCmd.Keycode.NEXT },
		{ 0xe, MyCmd.Keycode.PREVIOUS },
		
		{ 0xc, MyCmd.Keycode.MODLE },
		{ 0x2c, MyCmd.Keycode.MODLE },
	};
	
	private final static byte KEYS_WHEEL2[][] = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x2, MyCmd.Keycode.PREVIOUS },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x6, MyCmd.Keycode.BACK },
		{ 0x7, MyCmd.Keycode.RADIO },
		{ 0x9, MyCmd.Keycode.MUTE },
		{ 0x17, MyCmd.Keycode.PREVIOUS },
		{ 0x18, MyCmd.Keycode.NEXT },
		{ 0x19, MyCmd.Keycode.PREVIOUS },
		{ 0x1a, MyCmd.Keycode.NEXT },
		{ 0x20, MyCmd.Keycode.NAVIGATION },
		{ 0x24, MyCmd.Keycode.AUDIO },
		{ 0x25, MyCmd.Keycode.BT },
		{ 0x2a, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x2c, MyCmd.Keycode.MODLE },
		{ 0x2f, MyCmd.Keycode.HOME },
		{ 0x33, MyCmd.Keycode.RADIO },
		{ 0x34, MyCmd.Keycode.BT_DIAL },
		{ 0x35, MyCmd.Keycode.BT_HANG },
		{ 0x37, MyCmd.Keycode.SETUP },
//		{ 0x40, MyCmd.Keycode },
//		{ 0x41, MyCmd.Keycode },
		{ 0x42, MyCmd.Keycode.EQ },
		};
	private final static byte KEYS_WHEEL3[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
	};
	
	@Override
	public int getAngleValue(byte[] data) {

		int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

		
		return angle;
		
		
	}
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 0xff || (data&0xff) == 0xfa){

		} else if ((data&0xff) == 0xfe){
			data = 0;
		} else {
			//data = 
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[12];
		


		airData[0] = (byte) ((data[2] & 0x08) 
				| ((data[2] & 0x40) << 1)
				| ((data[3] & 0x44) << 0)
				| ((data[3] & 0x10) << 1)
				| ((data[4] & 0x20) >> 5)
				| ((data[4] & 0x10) >> 3));			
		
		

			airData[4] = (byte) (
					((data[4] & 0x3))
					| ((data[4] & 0xc) << 2)
					);

			switch ((data[6] & 0xff)) {
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
			}


		airData[3] = data[9];		


		airData[2] = data[8];
		airData[1] |= (byte) (data[7] & 0x0f);
		
		//rear
		switch ((data[10] & 0xff)) {
		case 1:
			airData[11] = (byte) (0x10);
			break;
		case 2:
			airData[11] = (byte) (0x20);
			break;
		case 3:
			airData[11] = (byte) (0x60);
			break;
		case 4:
			airData[11] = (byte) (0x40);
			break;
		}

		airData[11] |= (data[11] & 0x0f);
		airData[10] = data[12];
		
		airData[9] = (byte) (
				 ((data[2] & 0x10) << 3)
				| ((data[2] & 0x20) >> 4));		
		
		airData[5] |= 0x80;

		super.parseACInfo(airData);
	}
	
	public void parseCanboxData(byte[] data, int len) {
		switch (data[0]) {
//		case 0x22:
//			if (data[3] == 0) {
//				return;
//			} else if (data[3] < 0) {
//				data[3] = (byte) (-data[3]); 
//				data[2] += 0x10;
//			}
//			parseWheelKey(mIdKey3, data, MAP_KEYS3);
//			break;
		default:
			super.parseCanboxData(data, len);
		}	
	}
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		byte type = 7;
		switch (source) {
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			type = 0xd;
			break;
		}

		String s = String.format("%03d      %03d", play, total, Locale.ENGLISH);
		sendLcdInfo(type, s, false);

		
	}
	public void setMediaSrc(int source, byte type, byte []b){
		int freq = (int) ((b[1] & 0xff) | ((b[2] & 0xff) << 8));
		switch (source) {
		case MyCmd.SOURCE_RADIO: {
			String s;
			
			if (b[0] >= 0x10) { // am
				
				if (freq < 1000){
					s = String.format(" %d KHz", (freq), Locale.ENGLISH);
				} else {
					s = String.format(" %d KHz", (freq), Locale.ENGLISH);
				}
				
				type = 4;
			} else {
				
				if (freq < 10000){
					s = String.format("  %d.%d MHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				} else {
					s = String.format(" %d.%d MHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				}
				type = 1;
			}
			
			sendLcdInfo(type, s, false);
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
			s = (byte)0xa;
			break;
		case MyCmd.SOURCE_AUX:
			s = (byte)0xc;
			break;
		case MyCmd.SOURCE_AV_OFF:
			s = 0;
		default:
			return;
		}

		sendLcdInfo(s, null, false);
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
	

	public void sendId3(byte index, String num, int data_len, int reserve) {

		try {
			if (num == null) {
				num = " ";
			}
			byte[] n = num.getBytes("UTF-8");

			int num_len = n.length;

			if (num_len >= (data_len - reserve -3 )) {
				num_len = (data_len - reserve - 3);
			}
			byte[] data;

			int len = data_len + 2;

			data = new byte[len];

			data[0] = (byte) (data_len);
			data[1] = (byte) index;
			for (int i = 0; i < num_len; ++i) {
				data[2 + reserve + i] = n[i];

			}

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

		}
	}
	
	public void setSongName(String s) {
		sendId3((byte) 0x92, s, 0x20, 0);
	}

	public void setSongAlbum(String s) {
		sendId3((byte) 0x93, s, 0x20, 0);
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
			ret = (39 << 16) | (21 << 8) | 21;			

//			byte[] buf = new byte[] { 0x3, (byte) 0x6a, 0x5, (byte) 0xa6, 0 };
//			sendDataToCanbox(buf, buf.length);
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

	public void setContext(Context c) {
		super.setContext(c);
		updateTime();	
		Util.doSleep(50);
		udpateLang();
	}
	public int getUpdateTime() {
		return 60000;
	}
	public void updateTime() {
		if (mContext == null){
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
			if (h > 12) {
				h -= 12;
			} else if (h == 0) {
				h = 12;
			}
			h |= 0x80;
		} else {
			ampm = 1;
		}

		byte m = (byte) curDate.getMinutes();
//		byte s = (byte) curDate.getSeconds();

		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte)( curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();
		byte []buf = new byte[] {  0x0a, (byte) 0xcb, 0, h, m, 0,  0 , ampm, y, mon, d, 0 };
		
		sendDataToCanbox(buf, buf.length);

	}
	public void udpateLang() {
		int lang = -1;
		String locale = Locale.getDefault().getLanguage();
		if (locale != null) {
			if (locale.equals("zh")) {
				lang = 2;				
			} else {
				lang = 1;
			}

		}
		if (lang != -1) {
			byte[] buf = { 0x2, (byte) 0x9a, 0x1, (byte) lang };
			sendDataToCanbox(buf, buf.length);
		}
	}
	
	public void startConnect() {

	}

	@Override
	public void stopConnect() {

	}
	
	public boolean isSupportCompass() {
		return true;
	}	
			
	public void updateCompass(int compass) {
		int direction = compassAngleToDirect(compass);
		switch(direction){
		case 0:
			direction = 3;
			break;
		case 1:
			direction = 6;
			break;
		case 2:
			direction = 0;
			break;
		case 3:
			direction = 4;
			break;
		case 4:
			direction = 1;
			break;
		case 5:
			direction = 5;
			break;
		case 6:
			direction = 2;
			break;
		case 7:
			direction = 7;
			break;
		}

		byte[] buf = new byte[] { 0x5, (byte) (0xe4), 
				0, 0, 0, 0, 
				(byte) (direction & 0xff),
				};

		sendDataToCanbox(buf, buf.length);
	}
}
