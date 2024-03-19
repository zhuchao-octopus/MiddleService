package com.my.cartype.binarytek;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;


public class ChuanQiBNR extends Canbox{

	public ChuanQiBNR(){
		mIdAC = 0x10;
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
		buildCmdRadarFront((byte) 0x33, (byte) 0x1, (byte) 0x7);
		buildCmdRadarBack((byte) 0x32, (byte) 0x1, (byte) 0);
		buildCmdAngle((byte) 0x31, (byte) 0x4, 0x157c);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		buildCmdEQ((byte) 0x37, (byte) 0x2, 6);


		buildCmdKey((byte) 0x12, (byte) 1, (byte) 2, (byte) 1, KEYS_WHEEL);	
		buildCmdKey((byte) 0x50, (byte) 5, (byte) 2, (byte) 2, KEYS_WHEEL2);	

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	
	
	private final static byte IDS_TO_CANBOXSETTING[] = { 0x52, 0x6b, 0x6c, 0x6d, 0x6e};

	private final static byte KEYS_WHEEL[][] = {

		{ 0x11, MyCmd.Keycode.MODLE},
		{ 0x12, MyCmd.Keycode.KEY_SEEK_NEXT},
		{ 0x13, MyCmd.Keycode.KEY_SEEK_PREV},
		{ 0x14, MyCmd.Keycode.VOLUME_UP},
		{ 0x15, MyCmd.Keycode.VOLUME_DOWN},
		{ 0x16, MyCmd.Keycode.MUTE},
		{ 0x17, MyCmd.Keycode.PLAY_PAUSE},
		{ 0x18, MyCmd.Keycode.BACK},
		{ 0x30, MyCmd.Keycode.BT_DIAL},
		{ 0x31, MyCmd.Keycode.BT_HANG},
		{ 0x32, MyCmd.Keycode.SPEECH},
	};
	
	
	private final static byte KEYS_WHEEL2[][] = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x2, MyCmd.Keycode.EJECT },
		{ 0x3, MyCmd.Keycode.HOME },
		{ 0x4, MyCmd.Keycode.MODLE },
		{ 0x5, MyCmd.Keycode.RADIO },
		{ 0x6, MyCmd.Keycode.PREVIOUS },
		{ 0x7, MyCmd.Keycode.NEXT },
		{ 0x8, MyCmd.Keycode.SETUP },
		{ 0x9, MyCmd.Keycode.MUTE },
		{ 0xa, MyCmd.Keycode.NAVIGATION },
		{ 0xb, MyCmd.Keycode.PLAY_PAUSE },
		{ 0xc, MyCmd.Keycode.BACK },
		{ 0xd, MyCmd.Keycode.BT_DIAL },
		{ 0xe, MyCmd.Keycode.SPEECH },
		{ 0x10, MyCmd.Keycode.VOLUME_UP },
		{ 0x11, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x12, MyCmd.Keycode.KEY_TURN_A },
		{ 0x13, MyCmd.Keycode.KEY_TURN_D },
		{ 0x21, MyCmd.Keycode.NUMBER1 },
		{ 0x22, MyCmd.Keycode.NUMBER2 },
		{ 0x23, MyCmd.Keycode.NUMBER3 },
		{ 0x24, MyCmd.Keycode.NUMBER4 },
		{ 0x25, MyCmd.Keycode.NUMBER5 },
		{ 0x26, MyCmd.Keycode.NUMBER6 },
		{ 0x27, MyCmd.Keycode.KEY_RADIO_PS },
		{ 0x28, MyCmd.Keycode.AS },
		{ 0x29, MyCmd.Keycode.BT_DIAL },
		{ 0x2a, MyCmd.Keycode.SPEECH },
		{ 0x2b, MyCmd.Keycode.BACKLIGHT_OFF },
		{ 0x2c, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x2d, MyCmd.Keycode.KEY_SHUFFLE },
		{ 0x2e, MyCmd.Keycode.KEY_REPEAT },
	};
	
	private byte radarChangeStylePriv(byte data) {
		byte ret = 0;		
		
		if (data >= 0x19 && data <= 0x7f) {
			int step = ((0x7f - 0x19) * 100) / RADAR_STEP_MAX;
			ret = (byte) ((((data - 1) * 100) / step) + 1);
		}

		return ret;
	}
	
	public void parseRadarBack(int id, byte[] data) {
		int max;
		switch (data[2] & 0xff) {
		case 1:
			mRadar[0] = radarChangeStylePriv(data[4]);
			mRadar[1] = radarChangeStylePriv(data[5]);
			mRadar[2] = radarChangeStylePriv(data[6]);
			mRadar[3] = radarChangeStylePriv(data[7]);
			break;
		case 0:
		default:
			max = data[3];
			mRadar[0] = radarChangeStyle((byte) (data[4] + 1), max, 0);
			mRadar[1] = radarChangeStyle((byte) (data[5] + 1), max, 0);
			mRadar[2] = radarChangeStyle((byte) (data[6] + 1), max, 0);
			mRadar[3] = radarChangeStyle((byte) (data[7] + 1), max, 0);
			break;
		}
		parseRadar();
	}
	
	public void parseRadarFront(int id, byte[] data) {
		int max;
		switch (data[2] & 0xff) {
		case 1:
			mRadar[4] = radarChangeStylePriv(data[4]);
			mRadar[5] = radarChangeStylePriv(data[5]);
			mRadar[6] = radarChangeStylePriv(data[6]);
			mRadar[7] = radarChangeStylePriv(data[7]);
			break;
		case 0:
		default:
			max = data[3];
			mRadar[4] = radarChangeStyle((byte) (data[4] + 1), max, 0);
			mRadar[5] = radarChangeStyle((byte) (data[5] + 1), max, 0);
			mRadar[6] = radarChangeStyle((byte) (data[6] + 1), max, 0);
			mRadar[7] = radarChangeStyle((byte) (data[7] + 1), max, 0);
			break;
		}
		parseRadar();
	}
	

	

	
	public int getAngleValue(byte[] data) {

		int angle;
		int max;

		angle = ((data[3] & 0xff) | (((data[2] & 0xff)) << 8));
		angle = 0x1e00 - angle;
		max = 0x1300;

		angle = ((angle * 3000) / max);
		if (angle > -50 && angle < 50) {
			angle = 50;
		}

		return angle;
	}

	private void parseSeatHeat(byte[] data) {
		if (data[2] == 0x16 || data[2] == 0x17) {
			if (data[2] == 0x16) {
				airData[4] &= ~0x30;
				airData[4] |= ((data[3] & 0xf) << 4);
			} else if (data[2] == 0x17) {
				airData[4] &= ~0x3;
				airData[4] |= ((data[3] & 0xf) << 0);
			}

			if (!sendSeatHeat(airData)) {
				super.parseACInfo(airData);
			}
		}
	}
	
	public void parseCanboxData(byte[] data, int len) {
		switch(data[0]){
		case 0x52:
			parseSeatHeat(data);
			break;
		}
		super.parseCanboxData(data, len);
	}
	
	public int getOutTemp(byte[] data) {//
		int t = CarUtil.CLEAR_OUT_DOOR_TEMP;
		if (data.length > 6) {
			
			t = -400 + (((data[7] & 0xff))*5);
		}
		return t;
	}
//	private int getACStyle() {
//		if (CarUtil.getModelId() == 10) {
//			return false;
//		}
//		return true;
//	}

	private int getACTempPriv(byte data) {//
		if ((data & 0xff) >= 0x3 && (data & 0xff) <= 0x37) {
			data = (byte) (37 + (((data & 0xff) - 0x3)/2));
		} else if ((data & 0xff) == 0x39) {
			data = (byte) 0xff;
		} else if ((data & 0xff) == 0x1) {
			data = (byte) 0x0;
		} else {
			data = (byte) 0xfa;
		}
		return data & 0xff;
	}

	private byte[]	airData = new byte[10];
	public void parseACInfo(byte[] data)
	{			

		airData[0] = (byte) (
				 ((data[3] & 0x40) >> 5)
				| ((data[3] & 0x20) >> 5)
				| ((data[4] & 0x40) >> 4)
				| ((data[3] & 0x04) << 1)
				| ((data[3] & 0x01) << 6));
		
		if (((data[3] & 0x80) == 0)){
			airData[0] |= 0x20; 
		}
		
		airData[1] = (byte) (((data[3] & 0x08) << 3)
				| ((data[3] & 0x10) << 1)
				| ((data[4] & 0xf) << 0));
		
		
		airData[1] |= (byte) (data[4] & 0x0f);
		

		airData[4] = (byte) ((data[4] & 0x80)>>5);
		
		if (data[2] == 0) {
			airData[2] = (byte) (getACTempPriv(data[5]));
			airData[3] = (byte) (getACTempPriv(data[6]));
		} else {
			airData[2] = data[2];
			airData[3] = data[2];
			airData[7] |= 0x40;
		}

		if ((data[4] & 0x0f) == 0) {
//			Util.zeroBuf(airData);
		}
		super.parseACInfo(airData);
	}
	

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		byte s = 0;
		switch (source) {
		case MyCmd.SOURCE_DVD:
			s = 0x10;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			s = 0x08;
			break;
		}

		if (mPhoneStatus < HFP_INFO_CALLED) {
			mLcdInfo = new byte[] { (byte) 0xc0, 0xa, s, 0,
					(byte) ((total & 0xff00) >> 8),
					(byte) ((total & 0xff) >> 0),
					(byte) ((play & 0xff00) >> 8), (byte) ((play & 0xff) >> 0),
					(byte) ((total_time & 0xff00) >> 8),
					(byte) ((total_time & 0xff) >> 0),
					(byte) ((time & 0xff00) >> 8), (byte) ((time & 0xff) >> 0), };
			sendDataToCanbox(mLcdInfo, mLcdInfo.length);
		}
	}
	byte[] mLcdInfo;
	public void setMediaSrc(int source, byte type, byte[] b) {
		mLcdInfo = new byte[] { (byte) 0xc0, 0x5,0x1, b[0], b[1], b[2], (byte) (b[3] + 1)};
		sendDataToCanbox(mLcdInfo, mLcdInfo.length);
	}

	public void setMediaSrc(int source) {// default is simple box
		byte s = -1;
		switch (source) {
		case MyCmd.SOURCE_BT:
			s = 0xb;
			break;
		case MyCmd.SOURCE_AV_OFF:
			s = 0;
			break;
		case MyCmd.SOURCE_AUX:
			s = 0x07;
			break;
		case MyCmd.SOURCE_MX51:
			s = 0x08;
			break;
		}
		if (s != -1) {
			byte[] data = new byte[] { (byte) 0xc0, 0x2, s, 0 };
			sendDataToCanbox(data, data.length);
		}
	}


	private int mPhoneStatus = HFP_INFO_INITIAL;

	private byte[] mLcdBTInfo;

	private int mCallingTime;
	
	public void setPhone(int status, String num) {// default is simple box

		 if (mPhoneStatus != HFP_INFO_CALLING
				 && status == HFP_INFO_CALLING){
			 mCallingTime = 0;
			 mHandler.removeMessages(0);
			 mHandler.sendEmptyMessageDelayed(0, 1000);
		 } else  if (status!= HFP_INFO_CALLING){
			 mCallingTime = 0;
			 mHandler.removeMessages(0);
		 }
		 
		mPhoneStatus = status;
		int s = 0;
		switch (status) {
		case HFP_INFO_INITIAL:
		case HFP_INFO_READY:
		case HFP_INFO_CONNECTING:
		case HFP_INFO_CONNECTED:
			s = 0;
			break;
		case HFP_INFO_CALLED:
			s = 3;
			break;
		case HFP_INFO_INCOMING:
			s = 1;
			break;
		case HFP_INFO_CALLING:
			s = 2;
			break;
		}
//		if (status >= HFP_INFO_CONNECTED) {
//			s |= 0x10;
//		}

		byte[] data2;

		if (num == null) {
			num = " ";
		}

		if (s == 0){
			if (mLcdInfo != null) {
				sendDataToCanbox(mLcdInfo, mLcdInfo.length);
			}
		} else {
			byte[] n = num.getBytes();
			int num_len = n.length;
			if (num_len > 31) {
				num_len = 31;
			}
			 
			 data2 = new byte[num_len + 8];
			 data2[0] = (byte) 0xc0;
			 data2[1] = (byte)(data2.length - 2);
			 data2[2] = 5;
			 data2[3] = (byte)s;
			 data2[4] = (byte)((mCallingTime&0xff00)>>8);
			 data2[5] = (byte)(mCallingTime&0xff);
			 data2[6] = (byte)1;
			 data2[7] = (byte)num_len;			
			
			 byteArrayCopy(data2, n, 8, 0, num_len);
//			 data2[num_len + 4] = (byte) 0xff;
			
			 sendDataToCanbox(data2, data2.length);
			 if (mPhoneStatus == HFP_INFO_CALLING){
				 mCallingTime = 0;
				 mLcdBTInfo = data2.clone();
			 }
		}
	}
	public void clear(){
		super.clear();
		mHandler.removeMessages(0);
		mPhoneStatus = 0;
	}
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (mPhoneStatus == HFP_INFO_CALLING && mLcdBTInfo != null){
					++mCallingTime;
					mLcdBTInfo[4] = (byte)((mCallingTime&0xff00)>>8);
					mLcdBTInfo[5] = (byte)(mCallingTime&0xff);
					sendDataToCanbox(mLcdBTInfo, mLcdBTInfo.length);
					mHandler.sendEmptyMessageDelayed(0, 1000);
				 }
				break;
			
			}
			super.handleMessage(msg);
		}
	};
	
	public boolean isSupportCompass() {
		return true;
	}	
			
	public void updateCompass(int compass) {		
		int direction = compassAngleToDirectStep(compass, 32);
		
		byte[] buf = new byte[] { (byte) (0x86), 0x2, (byte) (direction&0xff), 0};

		sendDataToCanbox(buf, buf.length);
	}
}
