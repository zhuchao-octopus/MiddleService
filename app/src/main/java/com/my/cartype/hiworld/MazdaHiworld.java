package com.my.cartype.hiworld;

import java.util.Locale;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class MazdaHiworld extends Canbox{

	public MazdaHiworld(){
		buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
		buildCmdAngle((byte) 0x11, (byte) 0x0, 540);


		buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x3);
		buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 0x3);
		buildCmdRadarFrontEx((byte)0x4);

		buildCmdEQ((byte) 0xa6, (byte) 0x1, 6);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);		

		
		buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
		buildCmdKey((byte) 0x21, (byte) 2,  KEYS_WHEEL2);
		mIdAC = 0x31;
		mIdKey3 = 0x021122;
		MAP_KEYS3 = KEYS_WHEEL3;
		
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	@Override
	public int getAngleValue2(byte[] data) {
		int angle = (short) (((data[8] & 0xff) << 8) | (data[9] & 0xff));
		return -angle;
	}
	public void startConnect() {

	}

	@Override
	public void stopConnect() {

	}
	private final static byte IDS_TO_CANBOXSETTING[] = { (byte) 0x78,
			(byte) 0xae, (byte) 0xa5, (byte) 0x14, (byte) 0x15, (byte) 0x32,
			(byte) 0x79,  };
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.MUTE },
		{ 0x4, MyCmd.Keycode.SPEECH },
		{ 0x5, MyCmd.Keycode.BT_DIAL },
		{ 0x6, MyCmd.Keycode.BT_HANG },
		{ 0x9, MyCmd.Keycode.NEXT },
		{ 0x8, MyCmd.Keycode.PREVIOUS },
		{ 0xa, MyCmd.Keycode.MODLE },

	};
	
	private final static byte KEYS_WHEEL2[][] = {
		{ 0x2, MyCmd.Keycode.PREVIOUS },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x6, MyCmd.Keycode.BACK },
		{ 0x9, MyCmd.Keycode.MULT_MUTE_AND_POWER },
		{ 0x16, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x17, MyCmd.Keycode.PREVIOUS },
		{ 0x18, MyCmd.Keycode.NEXT },
		{ 0x19, MyCmd.Keycode.PREVIOUS },
		{ 0x1a, MyCmd.Keycode.NEXT },
		{ 0x20, MyCmd.Keycode.NAVIGATION },
		{ 0x2b, MyCmd.Keycode.HOME },
		{ 0x2d, MyCmd.Keycode.AUDIO },
//		{ 0x54, MyCmd.Keycode },
		};
	private final static byte KEYS_WHEEL3[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x2, (byte) 0x24, 0x01, 0 };
		switch(CarUtil.getModelId()){
		case 1:
			cmd[2] = 1;
			break;
		case 17:
			cmd[2] = 2;
			break;
		case 3:
			cmd[2] = 3;
			break;
		case 18:
			cmd[2] = 4;
			break;
		case 19:
			cmd[2] = 5;
			break;
		case 20:
			cmd[2] = 6;
			break;
		case 21:
			cmd[2] = 7;
			break;
		case 22:
			cmd[2] = 8;
			break;
		case 12:
			switch(CarUtil.getCarTypeConfig()){
			case 2:
				cmd[2] = 9;
				break;
			default:
				cmd[2] = 0xa;
				break;
			}
			break;
		case 5:
			cmd[2] = 0xb;
			break;
		case 10:
			cmd[2] = 0xc;
			break;
		case 2:
			cmd[2] = 0xd;
			break;
		case 4:
			cmd[2] = 0xe;
			break;
		case 11:
			cmd[2] = 0xf;
			break;
		case 23:
		case 34:
		case 9:
			cmd[2] = 0x10;
			break;
//		case 1:
//			cmd[2] = 0x11;
//			break;
		case 0:
		case 7:
			cmd[2] = 0x12;
			break;
//		case 1:
//			cmd[2] = 0x13;
//			break;
		case 65:
			cmd[2] = 0x14;
			break;
		case 16:
			cmd[2] = 0x15;
			break;
		case 29:
			cmd[2] = 0x16;
			break;
		case 31:
			cmd[2] = 0x17;
			break;
		case 30:
			switch(CarUtil.getCarTypeConfig()){
			case 2:
				cmd[2] = 0x18;
				break;
			default:
				cmd[2] = 0x19;
				break;
			}
			break;
		case 28:
			cmd[2] = 20;
			break;
		default:
			return null;
		}
		return cmd;
	}


	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0xfe) {
			data = 0;
		}  else if ((data & 0xff) == 0xff) {
			//data = 0;
		} else {
			
		}
		return data&0xff;
	}	

	public void parseACInfo(byte[] data)
	{		
		byte[]	airData = new byte[12];
		airData[0] = (byte) (
				((data[2] & 0x08) << 0)
				| ((data[2] & 0x40) << 1)
				| ((data[3] & 0x44) << 0)
				| ((data[3] & 0x10) << 1)
				| ((data[4] & 0x10) >> 3)
				| ((data[4] & 0x20) >> 5));			

		
		airData[4] = (byte) (
				 ((data[3] & 0x08) << 4)
					| ((data[4] & 0x03) << 4)
					| ((data[4] & 0x0c) >> 2)
				);	
		

		airData[7] = (byte) (
				 ((data[3] & 0x80) >> 7)
				);		
		
		airData[1] = (byte) (
				((data[6] & 0x0f) << 0)
				| ((data[6] & 0x40) >> 1)
				| ((data[6] & 0x20) << 1)
				| ((data[6] & 0x10) << 3));				

		airData[6] = (byte) (
				((data[7] & 0x0f) << 0)
				| ((data[7] & 0x40) >> 1)
				| ((data[7] & 0x20) << 1)
				| ((data[7] & 0x10) << 3));	

		airData[2] = data[8];
		airData[3] = data[9];
		


		airData[5] = (byte) (0x8);
		

		
		

		airData[5] |= 0x80;
		
		super.parseACInfo(airData);
	}	
	
	public void parseCanboxData(byte[] data, int len) {
		switch (data[0]) {
		case 0x41:
			byte max = 3;
			byte maxMiddle = 4;
			if (data[13] == 1){
				maxMiddle = max = (byte)0xfe;
			}
			buildCmdRadarBack((byte) 0x41, (byte) 0x0, max, maxMiddle, (byte) 4);
			buildCmdRadarFront((byte) 0x41, (byte) 0x0, max, maxMiddle,
					(byte) 4);
			super.parseCanboxData(data, len);
			break;
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
					s = String.format("000 %d 0KHz", (freq), Locale.ENGLISH);
				} else {
					s = String.format("00 %d 0KHz", (freq), Locale.ENGLISH);
				}
				
				type = 4;
			} else {
				
				if (freq < 10000){
					s = String.format("00  %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				} else {
					s = String.format("00 %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
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
			data[1] = (byte) 0xe1;
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
	
	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword1(data, len);
	}
	

	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {			
			ret = (0x3f << 16) | (17 << 8) | 13;			
			byte[] buf = new byte[] { 0x2, (byte) 0xa, 0x1, (byte) 0x6a };

			sendDataToCanbox(buf, buf.length);
		} else {
			byte[] buf = new byte[] { 0x2, (byte) 0xad, 0x0, (byte) data};
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				buf[2] = 6;
				buf[3] = (byte) (data + 0xa);
				break;
			// case EQ_CMD_SET_MIDDLE:
			// buf[2] = 5;
			// break;
			case EQ_CMD_SET_LOW:
				buf[2] = 4;
				buf[3] = (byte) (data + 0xa);
				break;
			case EQ_CMD_SET_ZONE_FR:
				buf[2] = 3;
				buf[3] = (byte) (data + 8);
				break;
			case EQ_CMD_SET_ZONE_LR:
				buf[2] = 2;
				buf[3] = (byte) (data + 8);
				break;
			case EQ_CMD_SET_VOLUME:
				buf[2] = 1;
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

		mEQData[0] = (byte) (buf[7] - 0xa);
//		mEQData[1] = (byte) (buf[6] + 9);
		mEQData[2] = (byte) (buf[5] - 0xa);
		mEQData[3] = (byte) (buf[4] - 8);
		mEQData[4] = (byte) (buf[3] - 8);
		mEQData[5] = (byte) (buf[2]);
		if (mEQData != null) {
			returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
		}
	}
}
