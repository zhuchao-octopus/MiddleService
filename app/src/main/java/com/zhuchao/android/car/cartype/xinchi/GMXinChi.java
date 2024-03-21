package com.zhuchao.android.car.cartype.xinchi;

import java.util.Date;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.R;

public class GMXinChi extends Canbox{

	public GMXinChi(){
		buildCmdDoor((byte) 0x4, (byte) 0x2, (byte) 0xfc, (byte) 0x03);
		buildCmdRadarFront((byte) 0x2b, (byte) 0x0, (byte) 0x7, (byte) 3);
		buildCmdRadarFrontEx((byte)0, (byte)1);
		buildCmdRadarBack((byte) 0x2a, (byte) 0x0, (byte) 0x7, (byte) 3);
		buildCmdRadarBackEx((byte)0, (byte)1);
		buildCmdAngle((byte) 0x1f, (byte) 0x0, 0x1d0);
		buildCmdEQ((byte) 0x13, (byte) 0x0, 6);
//		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
		buildCmdVersion((byte) 0x71, (byte) 0x0);
		mIdAC = 0xa;
		mIdKey = 0x1;
		MAP_KEYS = KEYS_WHEEL;
		mIdKey2 = 0x2;
		MAP_KEYS2 = KEYS_WHEEL2;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x8, 0x9, 0x7, 0x5, 0x6
		, 0xc, 0x16, 0x17, 0x18, 0x19, 0x70};
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.PREVIOUS },
		{ 0x4, MyCmd.Keycode.NEXT },
		{ 0x5, MyCmd.Keycode.MODLE },
		{ 0x6, MyCmd.Keycode.MULT_SPEECH_AND_BT },
		{ 0x7, MyCmd.Keycode.MULT_MUTE_AND_HANG },


		{ 0x10, MyCmd.Keycode.KEY_SEEK_NEXT },
		{ 0x11, MyCmd.Keycode.KEY_SEEK_PREV },
		{ 0x14, MyCmd.Keycode.NAVIGATION },
		{ 0x15, MyCmd.Keycode.POWER },
	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.NUMBER1 },
		{ 0x2, MyCmd.Keycode.NUMBER2 },
		{ 0x3, MyCmd.Keycode.NUMBER3 },
		{ 0x4, MyCmd.Keycode.NUMBER4 },
		{ 0x5, MyCmd.Keycode.NUMBER5 },
		{ 0x6, MyCmd.Keycode.NUMBER6 },
		{ 0x7, MyCmd.Keycode.RADIO },
		{ 0x8, MyCmd.Keycode.SETUP },
		{ 0x9, MyCmd.Keycode.POWER },
		{ 0xa, MyCmd.Keycode.MODLE },
		{ 0xb, MyCmd.Keycode.RADIO },
		{ 0xc, MyCmd.Keycode.SETUP },
		{ 0xd, MyCmd.Keycode.MUTE },
		{ 0xe, MyCmd.Keycode.PREVIOUS },
		{ 0xf, MyCmd.Keycode.NEXT },
		{ 0x10, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x11, MyCmd.Keycode.BACK },
		{ 0x12, MyCmd.Keycode.EQ },
		{ 0x13, MyCmd.Keycode.EJECT },
		{ 0x14, MyCmd.Keycode.DVD },
		{ 0x15, MyCmd.Keycode.AS },
		{ 0x16, MyCmd.Keycode.TIME_SETTING },
		{ 0x17, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x18, MyCmd.Keycode.HOME },
		{ 0x19, MyCmd.Keycode.PREVIOUS },
		{ 0x1a, MyCmd.Keycode.NEXT },
		{ 0x1b, MyCmd.Keycode.BT },
		{ 0x1c, MyCmd.Keycode.NAVIGATION },
		{ 0x1d, MyCmd.Keycode.NAVIGATION },
		{ 0x1e, MyCmd.Keycode.NAVIGATION },
		{ 0x1f, MyCmd.Keycode.NAVIGATION },
		{ 0x20, MyCmd.Keycode.NAVIGATION },
		{ 0x21, MyCmd.Keycode.NEXT },
		{ 0x22, MyCmd.Keycode.NEXT },
		{ 0x23, MyCmd.Keycode.PREVIOUS },
		{ 0x24, MyCmd.Keycode.PREVIOUS },
		{ 0x25, MyCmd.Keycode.NEXT },
		{ 0x26, MyCmd.Keycode.PREVIOUS },
		{ 0x27, MyCmd.Keycode.HOME },
		{ 0x28, MyCmd.Keycode.DVD },
		{ 0x29, MyCmd.Keycode.AUX_IN },
		{ 0x2a, MyCmd.Keycode.PREVIOUS },
		{ 0x2b, MyCmd.Keycode.NEXT },
		{ 0x2c, MyCmd.Keycode.RADIO },
		{ 0x2d, MyCmd.Keycode.KEY_CAR_INFO },
		{ 0x30, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x31, MyCmd.Keycode.VOLUME_ROLL_DOWN },
		{ 0x32, MyCmd.Keycode.ROLL_NEXT },
		{ 0x33, MyCmd.Keycode.ROLL_PREV },
		{ 0x34, MyCmd.Keycode.ROLL_NEXT },
		{ 0x35, MyCmd.Keycode.ROLL_PREV },

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

		} else if ((data & 0xff) == 0x3f) {
			data = (byte) 0xff;
		} else if (((data & 0xff) >= 0xb) && ((data & 0xff) <= 0x2c)) {
			data = (byte) (30 + ((data & 0xff) - 0xb));
		} else {
			data = (byte) 0xfa;
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[10];
		
		airData[0] = (byte) (
				((data[2] & 0xd0) >> 0) |
				((data[2] & 0x02) >> 1) |
				((data[3] & 0x40) >> 4) |
				((data[3] & 0x20) >> 4) 
				);	

		airData[7] = (byte) (
				 ((data[2] & 0x20) >> 5));	
		
		airData[9] = (byte) (
				 ((data[4] & 0x80) >> 7));	
		
		airData[1] = (byte) (
				 ((data[4] & 0x40) << 1)
				| ((data[4] & 0x10) << 2)
				| ((data[4] & 0x20) >> 0));	

		if ((data[3] & 0x10) == 0){
			airData[1] |= (byte)(data[3]&0xf) ;
		}
		

		airData[2] = data[5];
		airData[3] = data[6];


		if ((data[7] & 0x2) == 0) {
			airData[4] |= (byte) (((data[7] & 0xe0) >> 1));
		} else {
			airData[8] |= (byte) (((data[7] & 0xe0) >> 1));
			
		}
		
		if ((data[7] & 0x1) == 0) {
			airData[4] |= (byte) (((data[7] & 0x1c) >> 2));
		} else {
			airData[8] |= (byte) (((data[7] & 0x1c) << 0));
		}
		
		if ((data[2] & 0x0c) == 0x08){
			airData[4] |= 0x80 ;
		} else if ((data[2] & 0x0c) == 0x04){
			airData[0] |= 0x20 ;
		}
		
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {		

		byte [] mData = new byte[] { (byte) 0x88, 0x4, 4, 0,
				0, 0 };
		sendDataToCanbox(mData, mData.length);
		
	}
	@Override
	public void startConnect() {
		// TODO Auto-generated method stub
		super.startConnect();
		byte [] mData = new byte[] { (byte) 0xff, 0x1, (byte) 0x7f};
		sendDataToCanbox(mData, mData.length);
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
		byte ampm = 0;
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		byte format = 0;
		
		String date_foramt = SystemConfig.getProperty(mContext,
				SystemConfig.KEY_DATE_FORMAT);
		if (date_foramt != null) {
			if ("dd/MM/yyyy".equals(date_foramt)){
				format = 0;
			} else if ("MM/dd/yyyy".equals(date_foramt)){
				format = 2;
			}
		}
		
		if ("12".equals(strTimeFormat)) {
			ampm = 1;
		} 

		byte m = (byte) curDate.getMinutes();

//		Log.d("cccc", ""+curDate.getYear());
		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();
		byte[] buf = new byte[] { (byte) 0x87, 0x06, y, mon, d, format, h, m };
		
		sendDataToCanbox(buf, buf.length);
	}
	
	public void setVolume(int volume) {
		byte[] data = new byte[] { (byte) 0x8f, 0x3, 0x1, (byte) volume, 1 };
		sendDataToCanbox(data, data.length);
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
			ret = (16 << 16)|(11 << 8) | 11;			

			byte[] buf = new byte[] { (byte) 0xf1, 0x1, 0x13 };
			sendDataToCanbox(buf, buf.length);
			
		} else {
			byte id;
			int step;
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				step = data - mEQBuf[0];
				if (step > 0){
					id = 0x5;
				} else {
					id = 0x6;
				}
				break;
			case EQ_CMD_SET_LOW:
				step = data - mEQBuf[2];
				if (step > 0) {
					id = 0x5;
				} else {
					id = 0x6;
				}
				break;
			case EQ_CMD_SET_ZONE_FR:
				step = data - mEQBuf[3];
				if (step > 0) {
					id = 0x5;
				} else {
					id = 0x6;
				}
				break;
			case EQ_CMD_SET_ZONE_LR:
				step = data - mEQBuf[4];
				if (step > 0) {
					id = 0x5;
				} else {
					id = 0x6;
				}
				break;
			case EQ_CMD_SET_VOLUME:
				step = data - mEQBuf[0];
				if (step > 0) {
					id = 0x1;
				} else {
					id = 0x2;
				}
				break;
			default:
				return 0;
			}
			
			sendEQCmd(id,step);
			
		}
		return ret;
	}
	
	public void parseEQ(int id, byte[] buf) {

		mEQBuf[0] = (byte)( buf[4]+5);
		mEQBuf[2] =  (byte)( buf[3]+5);
		mEQBuf[3] =  (byte)( buf[6]+5);
		mEQBuf[4] = (byte)( buf[5]+5);
		mEQBuf[5] = buf[2];
		
		super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQBuf);
	}
}
