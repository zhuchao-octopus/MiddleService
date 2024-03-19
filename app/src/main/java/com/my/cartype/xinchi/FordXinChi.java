package com.my.cartype.xinchi;

import java.util.Date;

import android.os.Handler;
import android.provider.Settings;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;


public class FordXinChi extends Canbox{

	public FordXinChi(){
		buildCmdDoor((byte) 0x4, (byte) 0x2, (byte) 0xfc, (byte) 0x03);
//		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
//		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
		buildCmdAngle((byte) 0x09, (byte) 0x0, 380);
		buildCmdEQ((byte) 0x70, (byte) 0xff, 0);
//		buildCmdOutTemp((byte) 0x1a, (byte) 0x10);
		buildCmdVersion((byte) 0x71, (byte) 0x0);
		mIdAC = 0x5;
		mIdKey = 0x1;
		MAP_KEYS = KEYS_WHEEL;
		mIdKey2 = 0x2;
		MAP_KEYS2 = KEYS_WHEEL2;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = { 0x3,0x7,0x21,0x22,0x23,0x40,0x50,0x60	};
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.MULT_SEEK_PRE_HANG },
		{ 0x4, MyCmd.Keycode.MULT_SEEK_NEXT_RECV },
		{ 0x5, MyCmd.Keycode.MODLE },
		{ 0x6, MyCmd.Keycode.SPEECH },
		{ 0x7, MyCmd.Keycode.MUTE },
		{ 0x8, MyCmd.Keycode.BT },
		{ 0x9, MyCmd.Keycode.PREVIOUS },
		{ 0xa, MyCmd.Keycode.NEXT },
		{ 0xb, MyCmd.Keycode.PREVIOUS },
		{ 0xc, MyCmd.Keycode.NEXT },
		{ 0xd, MyCmd.Keycode.PLAY_PAUSE },
	};

	private final static byte KEYS_WHEEL2[][] = {
		{ 0x9, MyCmd.Keycode.POWER },
		{ 0xa, MyCmd.Keycode.MODLE },
		{ 0xe, MyCmd.Keycode.KEY_SEEK_PREV },
		{ 0xf, MyCmd.Keycode.KEY_SEEK_NEXT },
		{ 0x2f, MyCmd.Keycode.KEY_DISPLAY },
		{ 0x30, MyCmd.Keycode.EQ },
		{ 0x31, MyCmd.Keycode.KEY_TURN_A },
		{ 0x32, MyCmd.Keycode.KEY_TURN_D },
		{ 0x33, MyCmd.Keycode.EJECT },
		
		
		{ 0x50, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x51, MyCmd.Keycode.VOLUME_ROLL_DOWN },
	};
	
	@Override
	public void startConnect() {
		// TODO Auto-generated method stub
		super.startConnect();		

		byte[] buf = new byte[] {(byte) 0x90, 0x2, 0x4, 0 };
		sendDataToCanbox(buf, buf.length);
	}

	@Override
	public int getAngleValue(byte[] data) {

		int angle = (data[2] & 0xff);

		angle = 0x80 - angle;
//		if ((data[3] & 0x08) != 0){
//			angle =-angle;
//		}
		
		angle = ((angle * 3000) / 0x80);
		if (angle > -50 && angle < 50) {
			angle = 50;
		}
		
		return angle;
		
		
	}

	@Override
	public int getACTemp(byte data, int unit) {
		// TODO Auto-generated method stub
		if (((data & 0xff) == 0xfe) || ((data & 0xff) == 0xff)) {
			data = (byte) 0xfa;
		} else if ((data & 0xff) == 0xfd) {
			data = (byte) 0xff;
		} else {
			if (unit == 0) {
			//	data = (byte) (35 + data);
			} else {
				//data = (byte) (35 + data);
			}
		}
		return data;
	}

	byte[]	airData = new byte[13];
	public void parseACInfo(byte[] data)
	{
		

		
		airData[0] = (byte) ((data[2] & 0xd0) 
				| ((data[2] & 0x04) << 3)
				| ((data[2] & 0x20) >> 3)
				| ((data[3] & 0x80) >> 6)
				| ((data[2] & 0x02) >> 1));	
		

		airData[1] = (byte) (
				 ((data[4] & 0x40) << 1)
				| ((data[4] & 0x10) << 2)
				| ((data[4] & 0x20) >> 0));	


		airData[12] = (byte)(((data[3] & 0x10) << 1) 
				| ((data[3] & 0x20) >> 5));	
		if ((airData[12] & 0x20) == 0){
			airData[1] |= (byte)(data[3]&0xf); 
		}


		airData[4] &= ~0xf7;
		airData[4] |= (byte) (((data[3] & 0x40) >> 4));
		airData[5] = (byte) (((data[4] & 0x08) << 1)
				| ((data[4] & 0x01) << 0));	
		airData[7] = (byte) (((data[2] & 0x01) << 5));	
		

		airData[9] &= ~0x01;
		airData[9] |= (byte) (((data[4] & 0x80) >> 7));	
		
		airData[2] = (byte) (data[5] & 0xff);
		airData[3] = (byte) (data[6] & 0xff);
		
		airData[8] = 0;
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
		
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	

	public void parseACInfoEx(byte[] data){

		airData[9] &= ~0x80;
		airData[9] |= (byte) ((data[2] & 0x80));	
		

		airData[4] &= ~0x08;
		airData[4] |= (byte) (((data[2] & 0x20) >> 2));
		

		airData[11] = (byte) ((data[2] & 0x0f)
				| ((data[3] & 0x80) >> 2)
				| ((data[3] & 0x40) << 0));	
		

		if (((data[4]&0xff) == 0) || ((data[4]&0xff) == 0xf)){
			airData[10] = (byte)0xfa;
		} else if (((data[4] & 0xff) > 0) || ((data[4] & 0xff) < 9)) {
			airData[10] = (byte) ((data[4] & 0xff) | 0xf0);
		}
		
		airData[5] |= 0x80;		

		super.parseACInfoRear(airData);
	}
	private byte getRadarData(byte i) {	


		return (byte)(i*11/7);
	}
	
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x2a: // Radar back
		{
			byte radar;
			boolean show = false;
			radar = getRadarData(data[4]);
			if (mRadar[0] != radar){
				mRadar[0] = radar;
				show = true;
			}
			radar = getRadarData(data[5]);
			if (mRadar[1] != radar) {
				mRadar[1] = radar;
				show = true;
			}
			radar = getRadarData(data[9]);
			if (mRadar[2] != radar) {
				mRadar[2] = radar;
				show = true;
			}
			radar = getRadarData(data[8]);
			if (mRadar[3] != radar) {
				mRadar[3] = radar;
				show = true;
			}
			
			radar = getRadarData(data[2]);
			if (mRadarLeft[2] != radar) {
				mRadarLeft[2] = radar;
				show = true;
			}
			radar = getRadarData(data[3]);
			if (mRadarLeft[3] != radar) {
				mRadarLeft[3] = radar;
				show = true;
			}
			
			radar = getRadarData(data[6]);
			if (mRadarRight[2] != radar) {
				mRadarRight[2] = radar;
				show = true;
			}
			radar = getRadarData(data[7]);
			if (mRadarRight[3] != radar) {
				mRadarRight[3] = radar;
				show = true;
			}

			// byteArrayCopy(mRadar, data, 0, 2, 4);
			boolean zero = Util.isZero(mRadar);
			boolean zero2 = Util.isZero(mRadarLeft);
			boolean zero3 = Util.isZero(mRadarRight);
			if (!zero || !zero2 || !zero3) {
				RadarManager.start(mContext);
				checkHideRadarEx(5000);
			}
			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {				
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
				if (!zero2) {
					handler.sendMessage(handler
							.obtainMessage(CANBOX_RADAR_LEFT));
				}

				if (!zero3) {
					handler.sendMessage(handler
							.obtainMessage(CANBOX_RADAR_RIGHT));
				}
			}
		}
			break;
		case 0x2b: // Radar back
		{
			byte radar;
			boolean show = false;
			radar = getRadarData(data[4]);
			if (mRadar[4] != radar){
				mRadar[4] = radar;
				show = true;
			}
			radar = getRadarData(data[5]);
			if (mRadar[5] != radar) {
				mRadar[5] = radar;
				show = true;
			}
			radar = getRadarData(data[9]);
			if (mRadar[6] != radar) {
				mRadar[6] = radar;
				show = true;
			}
			radar = getRadarData(data[8]);
			if (mRadar[7] != radar) {
				mRadar[7] = radar;
				show = true;
			}
			
			radar = getRadarData(data[2]);
			if (mRadarLeft[1] != radar) {
				mRadarLeft[1] = radar;
				show = true;
			}
			radar = getRadarData(data[3]);
			if (mRadarLeft[0] != radar) {
				mRadarLeft[0] = radar;
				show = true;
			}
			
			radar = getRadarData(data[6]);
			if (mRadarRight[0] != radar) {
				mRadarRight[0] = radar;
				show = true;
			}
			radar = getRadarData(data[7]);
			if (mRadarRight[1] != radar) {
				mRadarRight[1] = radar;
				show = true;
			}

			// byteArrayCopy(mRadar, data, 0, 2, 4);
			boolean zero = Util.isZero(mRadar);
			boolean zero2 = Util.isZero(mRadarLeft);
			boolean zero3 = Util.isZero(mRadarRight);
			if (!zero || !zero2 || !zero3) {
				RadarManager.start(mContext);
				checkHideRadarEx(5000);
			}
			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {				
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
				if (!zero2) {
					handler.sendMessage(handler
							.obtainMessage(CANBOX_RADAR_LEFT));
				}

				if (!zero3) {
					handler.sendMessage(handler
							.obtainMessage(CANBOX_RADAR_RIGHT));
				}
			}
		}
			break;
		case 6:
			parseACInfoEx(data);
			break;
		default:
			super.parseCanboxData(data, len);
		}
	}
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}
	public void setMediaSrc(int source, byte type, byte []b){
	}

	public void setMediaSrc(int source) {
	}
	

	

	
	public int getOutTemp(byte[] data) {//
		int t = data[3]*10;
		return t;
	}


	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (30 << 16) | (15 << 8) | 15;

			byte[] buf = new byte[] { (byte) 0xf1, 0x1, 0x70, 0 };
			sendDataToCanbox(buf, buf.length);
		} else {
			if (mEQData == null) {
				mEQData = new byte[6];
			}
			byte[] buf = new byte[] { (byte) 0x93, 0x7,
					mEQData[5],	mEQData[4],mEQData[3],
					mEQData[2],mEQData[1],mEQData[0],0 };
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				buf[7] = (byte) (data + 3);
				break;
			case EQ_CMD_SET_MIDDLE:
				buf[6] = (byte) (data + 3);
				break;
			case EQ_CMD_SET_LOW:
				buf[5] = (byte) (data + 3);
				break;
			case EQ_CMD_SET_ZONE_FR:
				buf[4] = (byte) (data + 3);
				break;
			case EQ_CMD_SET_ZONE_LR:
				buf[3] = (byte) (data + 3);
				break;
			case EQ_CMD_SET_VOLUME:
				buf[2] = (byte) (data);
				break;
			default:
				return 0;
			}

			sendDataToCanbox(buf, buf.length);
		}
		return ret;
	}
	
	public void parseEQ(int id, byte[] buf) {
		
		if (mEQData == null) {
			mEQData = new byte[6];
		}
		
		mEQData[0] = (byte) (buf[7] & 0xff);
		mEQData[1] = (byte) (buf[6] & 0xff);	
		mEQData[2] = (byte) (buf[5] & 0xff);	
		mEQData[3] = (byte) (buf[4] & 0xff);
		mEQData[4] = (byte) (buf[3]& 0xff);

		if ((buf[2] & 0x80) != 0) {
			buf[2] = 0;
		}
		
		mEQData[5] = buf[2];

		mEQData[0] -= 3;
		mEQData[1] -= 3;
		mEQData[2] -= 3;
		mEQData[3] -= 3;
		mEQData[4] -= 3;

		
		super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
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

		byte []buf = new byte[] { (byte) 0x98, 0x04, ampm, h,
				m, s };
		
		sendDataToCanbox(buf, buf.length);
	}
	
	public boolean isSupportCompass() {
		return true;
	}	
			
	public void updateCompass(int compass) {
		int direction = compassAngleToDirect(compass);

		byte[] buf = new byte[] { (byte) (0x9b), 0x1, (byte) (direction & 0xff) };

		sendDataToCanbox(buf, buf.length);
	}
}
