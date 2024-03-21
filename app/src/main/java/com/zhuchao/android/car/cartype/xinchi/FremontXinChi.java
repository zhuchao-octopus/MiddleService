package com.zhuchao.android.car.cartype.xinchi;

import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.R;

public class FremontXinChi extends Canbox{

	public FremontXinChi(){
		buildCmdDoor((byte) 0xa, (byte) 0x2, (byte) 0xfc, (byte) 0x03);
//		buildCmdRadarFront((byte) 0xc, (byte) 0x0, (byte) 0x4, (byte) 3);
//		buildCmdRadarBack((byte) 0x9, (byte) 0x0, (byte) 0x4, (byte) 3);
		buildCmdAngle((byte) 0x9, (byte) 0x0, 0x1d0);
		buildCmdEQ((byte) 0x70, (byte) 0x0, 6);
//		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
		buildCmdVersion((byte) 0xf1, (byte) 0x0);
		mIdAC = 0x5;
		mIdKey = 0x1;
		MAP_KEYS = KEYS_WHEEL;
		mIdKey2 = 0x4;
		MAP_KEYS2 = KEYS_WHEEL2;

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x70, 0xa, 0x7 };
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x4, MyCmd.Keycode.PREVIOUS },
		{ 0x5, MyCmd.Keycode.MODLE },
		{ 0x6, MyCmd.Keycode.RADIO },
		{ 0x7, MyCmd.Keycode.SPEECH },
		{ 0x8, MyCmd.Keycode.BT },

	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.POWER },

		{ 0x4, MyCmd.Keycode.NAVIGATION },
		{ 0x5, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x6, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x7, MyCmd.Keycode.ROLL_PREV },
		{ 0x8, MyCmd.Keycode.ROLL_NEXT },
		{ 0x9, MyCmd.Keycode.EJECT },

	};

	@Override
	public int getAngleValue(byte[] data) {	
		int max = 0x80;
		
		int angle = (data[2]&0xff);
		if (angle < 0x80){
			angle = (0x80 - angle);
		} else {
			angle = (0x80 - angle);
		}
		
		angle = angle * 3000 / max;
		return angle;
	}

	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0x0) {
		} else if ((data & 0xff) == 0x7f) {
			data = (byte) 0xff;
		} else if (((data & 0xff) >= 0xe) && ((data & 0xff) <= 0x1e)) {
			data = (byte) (28 + (((data & 0xff) - 0xe) * 2));
		} else if (((data & 0xff) >= 0x3c) && ((data & 0xff) <= 0x54)) {

		} else {
			data = (byte) 0xfa;
		}
		return data;
	}

	private final byte[]	mAirDataEx = new byte[14];
	public void parseACInfo(byte[] data)
	{		
		
		mAirDataEx[0] = (byte) ((data[2] & 0xd0)|
				((data[2] & 0x01) << 1) |
				((data[2] & 0x02) >> 1) |
				((data[2] & 0x04) << 3)  
				);				
		
		mAirDataEx[1] = (byte) ((data[3] & 0x07)|
				((data[4] & 0x40) << 1) |
				((data[4] & 0x20) >> 0) |
				((data[4] & 0x10) << 1)  
				);	
		
		mAirDataEx[4] &= (~0x33);
		mAirDataEx[4] |= (byte) (
				((data[7] & 0xcc) >> 2) 
				);
		
		mAirDataEx[7] = (byte) (
				((data[3] & 0x40) << 1)  
				);	
		
		mAirDataEx[8] = (byte) (
				((data[7] & 0x03) << 2) |
				((data[7] & 0x30) >> 0) 
				);
		
		mAirDataEx[12] = (byte) (
				((data[8] & 0x80) >> 7)  
				);	
		
		if (((data[5] & 0xff) >= 0x3c) && ((data[5] & 0xff) <= 0x54)) {
			mAirDataEx[5] |= 0x1;
		}
		mAirDataEx[2] = data[5];
		mAirDataEx[3] = data[6];



		mAirDataEx[5] |= 0x80;
		
		super.parseACInfo(mAirDataEx);
	}	
	
	@Override
	public void parseACInfoRear(byte[] data) {
		// TODO Auto-generated method stub

		mAirDataEx[4] &= (~0x08);
		mAirDataEx[4] |= (byte) (
				((data[2] & 0x20) >> 2) 
				);
		
		mAirDataEx[9] = (byte) ((data[2] & 0x80) |  
				((data[2] & 0x40) >> 5));
		mAirDataEx[10] =  (byte)getACTemp(data[4]);
		mAirDataEx[11] = (byte) ((data[3] & 0x40) |
				((data[2] & 0x07) << 0) |  
				((data[3] & 0x80) >> 2));
		super.parseACInfo(mAirDataEx);
	}
	
	public void parseCanboxData(byte[] data, int len) {
		if (data[0] == 0x6) {
			parseACInfoRear(data);
		} else {
			super.parseCanboxData(data, len);
		}
	}
	
	
	@Override
	public void startConnect() {
		// TODO Auto-generated method stub
		super.startConnect();
		byte [] mData = new byte[] { (byte) 0xff, 0x1, (byte) 0x7f};
		sendDataToCanbox(mData, mData.length);
	}

	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		switch (source) {
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			break;
		}

		String s = String.format("%03d/%03d %02d:%02d", play, total, time / 60,
				time % 60, Locale.ENGLISH);

		sendId3((byte) 4, s);
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
		if (source == MyCmd.SOURCE_RADIO) {
			String s;
			if (b[0] >= 0x10) { // am
				s = String.format("%d KHz", (freq), Locale.ENGLISH);
				sendId3((byte) 2, s);
			} else {

				if (freq < 10000) {
					s = String.format("FM %d.%dMHZ", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				} else {
					s = String.format("FM %d.%dMHZ", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				}
				sendId3((byte) 1, s);
			}
		}
		
	}

	public void setMediaSrc(int source) {// default is simple box
		String s = "";
		byte index = 4;
		switch (source) {
		case MyCmd.SOURCE_RADIO: {
		}
			return;
		case MyCmd.SOURCE_DVD: {
			index = 3;
		}
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO: {
			index = 4;
		}
			break;
		case MyCmd.SOURCE_AUX:
			index = 7;
			break;
		}
		sendId3(index, s);
	}
	
	public void sendId3(byte index, String num) {

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = getBytesUnicodeLittleEndian(num); // del 0xff 0xfe

			int num_len = n.length;

			if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
				num_len -= 2;
			}

			if (num_len > 30) {
				num_len = 30;
			}
			byte[] data;

			int len = 0x1f + 2;

			data = new byte[len];

			data[0] = (byte) 0x90;
			data[1] = (byte) (0x1f);
			data[2] = index;
			for (int i = 0; i < num_len && i < (data[1]); ++i) {
//				data[3 + i] = n[i + 2];
				
				if (i % 2 == 0) {
					data[i + 3] = n[i + (n.length - num_len) + 1];
				} else {
					data[i + 3] = n[i + (n.length - num_len) - 1];
				}
			}

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

			Log.d("Nissan2013Simple", "sendId3" + e);
		}
	}
	

	private void sendEQCmd(byte id, int step) {

		if (step == 0) {
			return;
		} else if (step > 0) {
			step--;
		} else {
			step++;
		}
		byte[] buf = new byte[] { (byte) 0xa3, 0x2, id, 0 };
		sendDataToCanbox(buf, buf.length);
		
		mHandler.removeMessages(SET_EQ_STEP);
		if (step != 0){
			mHandler.sendMessageDelayed(mHandler.obtainMessage(SET_EQ_STEP, id, step), 200);
		}
	}
	
	private final static int SET_EQ_STEP = 1;
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == SET_EQ_STEP) {
				sendEQCmd((byte) msg.arg1, msg.arg2);
			}
			super.handleMessage(msg);
		}
	};

	byte[] mEQBuf = new byte[] { 5, 0, 5, 5, 5, 0 };
	
	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (38 << 16) | (19 << 8) | 19;

			byte[] buf = new byte[] { (byte) 0xf1, 0x1, 0x70 };
			sendDataToCanbox(buf, buf.length);
		} else {

			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				mEQBuf[0] = (byte)data;
				break;
			case EQ_CMD_SET_MIDDLE:
				mEQBuf[1] = (byte)data;
				break;
			case EQ_CMD_SET_LOW:
				mEQBuf[2] = (byte)data;
				break;
			case EQ_CMD_SET_ZONE_FR:
				mEQBuf[3] = (byte)data;
				break;
			case EQ_CMD_SET_ZONE_LR:
				mEQBuf[4] = (byte)data;
				break;
			case EQ_CMD_SET_VOLUME:
				mEQBuf[5] = (byte)data;
				break;
			default:
				return 0;
			}

			byte[] buf = new byte[] { (byte) 0x93, 0x7, mEQBuf[5],
					(byte) (mEQBuf[4] + 1), (byte) (mEQBuf[3] + 1),
					(byte) (mEQBuf[2] + 1), (byte) (mEQBuf[1] + 1),
					(byte) (mEQBuf[0] + 1), 0 };
			sendDataToCanbox(buf, buf.length);

			super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQBuf);
		}
		return ret;
	}
	
	public void parseEQ(int id, byte[] buf) {


		
		mEQBuf[0] = buf[7];
		mEQBuf[1] = buf[6];
		mEQBuf[2] = buf[5];		
		mEQBuf[3] = buf[4];
		mEQBuf[4] = buf[3];
		mEQBuf[5] = buf[2];

		mEQBuf[0] -= 1;
		mEQBuf[1] -= 1;
		mEQBuf[2] -= 1;
		mEQBuf[3] -= 1;
		mEQBuf[4] -= 1;
		
		super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQBuf);
	}
	

	public int getUpdateTime() {
		return 60000;
	}
	public void updateTime() {
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);
		byte ampm = 0;
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);
		
		if (!"12".equals(strTimeFormat)) {
			ampm = 1;
		} 

		byte m = (byte) curDate.getMinutes();
		byte s = (byte) curDate.getSeconds();

		byte[] buf = new byte[] { (byte) 0x98, 0x04, ampm,  h, m , s};
		
		sendDataToCanbox(buf, buf.length);
	}
	
	public boolean isSupportCompass() {
		return true;
	}	
			
	public void updateCompass(int compass) {
		int direction = compassAngleToDirect(compass);

		byte[] buf = new byte[] { (byte) (0x99), 0x1, (byte) (direction & 0xff) };

		sendDataToCanbox(buf, buf.length);
	}
	

	public void setContext(Context c) {
		super.setContext(c);

		udpateLang();
	}

	public void udpateLang() {
		int lang = -1;
		String locale = Locale.getDefault().getLanguage();
		if (locale != null) {
			if (locale.equals("en")) {
				lang = 1;
			} else if (locale.equals("zh")) {
				lang = 8;
			} else if (locale.equals("de")) {
				lang = 5;
			} else if (locale.equals("it")) {
				lang = 4;
			} else if (locale.equals("fr")) {
				lang = 2;
			} else if (locale.equals("sv")) {
				lang = 5;
			} else if (locale.equals("es")) {
				lang = 3;
			} else if (locale.equals("nl")) {
				lang = 6;
			} if (locale.equals("ru")) {
				lang = 7;
			} 

		}
		if (lang != -1) {
			byte[] buf = { (byte) 0x97, 0x2, 0x53, (byte) lang };
			sendDataToCanbox(buf, buf.length);
		}
	}
}
