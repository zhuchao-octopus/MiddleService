package com.my.cartype.raise;

import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class MitsubishiRaise extends Canbox{

	public MitsubishiRaise(){
		mIdAC = 0x21;

		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdRadarFront((byte) 0x23, (byte) 0x1, (byte) 0x4);
		buildCmdRadarBack((byte) 0x22, (byte) 0x1, (byte) 0x4);
		buildCmdEQ((byte) 0x17, (byte) 0x0, 6);
		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;
	
	}


	private byte[] getCarTypeCmd() {
		
		switch(CarUtil.getModelId()){
		case 2:
		case 5:
			byte[] cmd = new byte[] { (byte) 0x84, 0x02, 0x9, 0x1 };
			return cmd;
		}
		return null;
	}
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, 
			{ 0x4, KEY_NEXTSONG },
			{ 0x3, KEY_PREVIOUSSONG }, 
			{ 0x5, MyCmd.Keycode.KEY_SEEK_PREV }, 
			{ 0x6, MyCmd.Keycode.KEY_SEEK_NEXT },
			{ 0x7, KEY_SOURCE },
			{ 0x8, KEY_MUTE },
			{ 0x9, MyCmd.Keycode.BT_DIAL },
			{ 0xa, MyCmd.Keycode.BT_HANG },
			{ 0x12, MyCmd.Keycode.SPEECH },
			
			{ 0x15, KEY_BACK },
			{ 0x16, MyCmd.Keycode.PLAY_PAUSE },
			

			{ (byte)0x81, MyCmd.Keycode.POWER },
			{ (byte)0x82, MyCmd.Keycode.VOLUME_ROLL_UP },
			{ (byte)0x83, MyCmd.Keycode.VOLUME_ROLL_DOWN },
			{ (byte)0x84, MyCmd.Keycode.PLAY_PAUSE },
			{ (byte)0x85, MyCmd.Keycode.ROLL_NEXT },
			{ (byte)0x86, MyCmd.Keycode.ROLL_PREV },
			{ (byte)0x87, MyCmd.Keycode.RADIO },
			{ (byte)0x88, MyCmd.Keycode.AUDIO },
			{ (byte)0x89, MyCmd.Keycode.NAVIGATION },
			{ (byte)0x8a, MyCmd.Keycode.MENU },
			{ (byte)0x8b, MyCmd.Keycode.HOME },
			{ (byte)0x8c, MyCmd.Keycode.SETUP },
			{ (byte)0x8d, MyCmd.Keycode.SETUP },
			{ (byte)0x8e, MyCmd.Keycode.BACKLIGHT_OFF },
			{ (byte)0x8f, MyCmd.Keycode.ALL_APP },
	};
	
//	public int getACTemp(byte data) {//
//		if ((data & 0xff) >= 0x3 && (data & 0xff) <= 0x37) {
//			data = (byte) (37 + (((data & 0xff) - 0x3)/2));
//		} else if ((data & 0xff) == 0xff) {
//			data = (byte) 0xff;
//		} else if ((data & 0xff) == 0x0) {
//			data = (byte) 0x0;
//		} else {
//			data = (byte) 0xfa;
//		}
//		return data & 0xff;
//	}

	public void parseACInfo(byte[] data)
	{	

		byte[]	airData = new byte[10];
		
		airData[0] = (byte) (((data[3] & 0x80) >> 1)
				| ((data[3] & 0x40) >> 2)
				| ((data[3] & 0x08) >> 3)
				| ((data[4] & 0x04) >> 1)
				| ((data[3] & 0x01) << 5));
		
		airData[1] = (byte) (data[4] & 0xf);
		switch (data[3] & 0x0f) {
		case 0:
			airData[1] |= 0x40;
			break;
		case 1:
			airData[1] |= 0x60;
			break;
		case 2:
			airData[1] |= 0x20;
			break;
		case 3:
			airData[1] |= 0xa0;
			break;
		case 4:
			airData[1] |= 0x90;
			break;
		}

		airData[2] = data[5];
		airData[3] = data[6];

		if ((data[4] & 0x0f) == 0) {
//			Util.zeroBuf(airData);
		}
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

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
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x08;
			break;
		}
		if (s != -1) {
			byte[] data = new byte[] { (byte) 0xc0, 0x2, s, 0 };
			sendDataToCanbox(data, data.length);
		}
	}

	public void sendId3(byte index, String num) {

			try {
				if (num == null){
					num = " ";
				}
				byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe
				int head = 0;
				if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
					head = 2;
				}
				
				int numlen = (n.length - head);
				byte[] data = new byte[numlen + 6];
				data[0] = (byte) 0xc0;
				data[1] = (byte) (numlen + 4);
				data[2] = (byte) 8;
				data[3] = (byte) 0x10;
				data[4] = index;
				data[5] = (byte) numlen;

				for (int j = 0; j < (numlen); ++j) {
					if ((j % 2) == 0) {
						data[6 + j] = n[j + head + 1];
					} else {
						data[6 + j] = n[j + head - 1];
					}
					
				}

				sendDataToCanbox(data, data.length);
			} catch (Exception e) {
				Log.d("ffck", ""+e);
			}
			
		
	}

	public void setSongName(String s) {
		sendId3((byte) 0x1,  s);
	}
	public void setSongAritst(String s) {
		sendId3((byte) 0x2, s);
	}

	

	private int mPhoneStatus = HFP_INFO_INITIAL;


	private int mCallingTime;
	
	public void setPhone(int status, String num) {// default is simple box
	
		 
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


		try {
			if (num == null){
				num = " ";
			}
			byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe
			int head = 0;
			if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
				head = 2;
			}
			
			int numlen = (n.length - head);
			byte[] data = new byte[numlen + 7];
			data[0] = (byte) 0xc0;
			data[1] = (byte) (numlen + 5);
			data[2] = (byte) 5;
			data[3] = (byte) s;
			data[4] = (byte) 0x10;
			data[5] = 2;
			data[6] = (byte) numlen;

			for (int j = 0; j < (numlen); ++j) {
				if ((j % 2) == 0) {
					data[7 + j] = n[j + head + 1];
				} else {
					data[7 + j] = n[j + head - 1];
				}
				
			}

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {
			Log.d("ffck", ""+e);
		}
	}

	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (45 << 16) | (23 << 8) | 11;

			byte[] buf = new byte[] { (byte) 0x90, 0x2, 0x17, 0 };
			sendDataToCanbox(buf, buf.length);
		} else {
			byte[] buf = new byte[] { (byte) 0x84, 0x2, 0x0, (byte) data };
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				buf[3] += 2;
				buf[2] = 5;
				break;
			case EQ_CMD_SET_MIDDLE:
				buf[3] += 2;
				buf[2] = 6;
				break;
			case EQ_CMD_SET_LOW:
				buf[3] += 2;
				buf[2] = 4;
				break;
			case EQ_CMD_SET_ZONE_FR:
				buf[2] = 1;
				break;
			case EQ_CMD_SET_ZONE_LR:
				buf[2] = 2;
				break;
			case EQ_CMD_SET_VOLUME:
				buf[2] = 8;
				break;
			default:
				return 0;
			}

			sendDataToCanbox(buf, buf.length);
		}
		return ret;
	}
	public void parseEQ(int id, byte[] buf) {
		byte[] data = new byte[6];
		data[0] = (byte) (buf[6] - 2);
		data[1] = (byte) (buf[7] - 2);
		data[2] = (byte) (buf[5] - 2);
		data[3] = buf[2];
		data[4] = buf[3];
		data[5] = buf[9];
		super.returnEQData(EQ_CMD_SET_ALL_DATA, data);
	}
	
}
