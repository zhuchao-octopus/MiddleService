package com.zhuchao.android.car.cartype.hiworld;

import java.util.Locale;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class Jeep002Hiworld extends Canbox{

	public Jeep002Hiworld(){
		buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
		buildCmdAngle((byte) 0x11, (byte) 0x0, 540);

		buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 2, (byte) 6, (byte) 4);
		buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 2, (byte) 4,
				(byte) 4);
		buildCmdRadarFrontEx((byte) 4);
		
		buildCmdEQ((byte) 0xa6, (byte) 0x1, 6);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);		

		
		buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
		buildCmdKey((byte) 0x21, (byte) 2,  KEYS_WHEEL2);
		mIdAC = 0x31;
		mIdKey3 = 0x021122;
		MAP_KEYS3 = KEYS_WHEEL3;
		
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	public void startConnect() {

	}

	@Override
	public int getAngleValue2(byte[] data) {
		int angle = (short) (((data[8] & 0xff) << 8) | (data[9] & 0xff));
		return -angle;
	}
	
	@Override
	public void stopConnect() {

	}
	private final static byte[] IDS_TO_CANBOXSETTING = {
		0x32, 0x43, 0x62, 0x60, (byte) 0xae, (byte) 0xc2, (byte) 0xa6, 
		(byte) 0xc1, (byte) 0x9c, (byte) 0xa5, 0x45,0x31 };
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.MUTE },
		{ 0x4, MyCmd.Keycode.SPEECH },
		{ 0x5, MyCmd.Keycode.BT_DIAL },
		{ 0x6, MyCmd.Keycode.BT_HANG },
		{ 0x9, MyCmd.Keycode.NEXT },
		{ 0x8, MyCmd.Keycode.PREVIOUS },
		{ 0xc, MyCmd.Keycode.MODLE },
	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x6, MyCmd.Keycode.BACK },
		{ 0x9, MyCmd.Keycode.MUTE },
		{ 0x31, MyCmd.Keycode.BACKLIGHT_OFF },
		{ 0x32, MyCmd.Keycode.PLAY_PAUSE },
	};
	private final static byte[][] KEYS_WHEEL3 = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
		{ 0x3, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x13, MyCmd.Keycode.ROLL_PREV , 0},
	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x2, (byte) 0x24, 0x01, 0 };
		switch(CarUtil.getModelId()){
		case 1:
		case 13:
		case 18:
			switch(CarUtil.getCarTypeConfig()){
			case 0:
				cmd[2] = 1;
				break;
			case 1:
				cmd[2] = 2;
				break;
			default:
				cmd[2] = 3;
				break;
			}
			break;
		case 2:
		case 8:
		case 19:
			cmd[2] = 4;
			break;
		case 20:
			cmd[2] = 5;
			break;
		case 21:
			cmd[2] = 6;
			break;
		default:
			return null;
		}
		return cmd;
	}

	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 0){
			data = (byte)0xfa;
		} else if ((data&0xff) == 0xff){

		} else if ((data&0xff) == 0xfe){
			data = 0;
		} else {
			//data = 
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[13];
		
		airData[0] = (byte) ((data[2] & 0x08)
				| ((data[2] & 0x40) << 1)
				| ((data[3] & 0x40) >> 0)
				| ((data[3] & 0x10) << 1)
				| ((data[4] & 0x20) >> 5)
				| ((data[4] & 0x10) >> 3));		
		

		airData[4] = (byte) ( ((data[2] & 0x20) >> 3)
				| ((data[4] & 0x03) << 4)
				| ((data[4] & 0x0c) >> 2));	
		

		airData[7] = (byte) ( ((data[2] & 0x04) << 5));	
//		airData[12] = (byte) ( ((data[3] & 0x20) >> 5));	
		

//		airData[8] = (byte) ( ((data[5] & 0x03) << 4)
//				| ((data[5] & 0x0c) >> 0));		
		
		switch((data[6] & 0xff)){
		case 1:
			airData[9] = (byte) (0x1);
			break;
//		case 2:
//			airData[1] = (byte) (0x80);
//			break;
		case 3:
			airData[1] = (byte) (0x20);
			break;
		case 5:
			airData[1] = (byte) (0x60);
			break;
		case 6:
			airData[1] = (byte) (0x40);
			break;
		case 0xb:
			airData[1] = (byte) (0x80);
			break;
		case 0xc:
			airData[1] = (byte) (0xa0);
			break;
//		case 0xd:
//			airData[1] = (byte) (0xc0);
//			break;
//		case 0xe:
//			airData[1] = (byte) (0xe0);
//			break;
		default:
			airData[1] = 0;
			break;
		}
		if ((data[7] & 0xff) <= 7) {
			airData[1] |= (byte) (data[7] & 0x0f);
		}

		airData[2] = (byte) (data[8] & 0xff);
		airData[3] = (byte) (data[9] & 0xff);
		


		airData[5] |= 0x80;
		
		super.parseACInfo(airData);
	}	
	
	public void parseCanboxData(byte[] data, int len) {
		//		case 0x22:
		//			if (data[3] == 0) {
		//				return;
		//			} else if (data[3] < 0) {
		//				data[3] = (byte) (-data[3]);
		//				data[2] += 0x10;
		//			}
		//			parseWheelKey(mIdKey3, data, MAP_KEYS3);
		//			break;
		super.parseCanboxData(data, len);
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

		String s = String.format("%02d:%02d:%02d   %d/%d", time / 3600,
				time / 60, time % 60, play, total, Locale.ENGLISH);
		sendLcdInfo(type, s, false);

		
	}
	public void setMediaSrc(int source, byte type, byte []b){
		int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
		if (source == MyCmd.SOURCE_RADIO) {
			String s;

			if (b[0] >= 0x10) { // am

				s = String.format("%d KHz", (freq), Locale.ENGLISH);
				type = 4;
			} else {
				s = String.format("%d.%02d MHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);

				type = 1;
			}

			sendLcdInfo(type, s, false);
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
			byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

			int num_len = n.length;
			if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
				num_len -= 2;
			}
			if (num_len >= (36)) {
				num_len = (36);
			}
			byte[] data;

			int len = 36 + 3;

			data = new byte[len];

			data[0] = (byte) (37);
			data[1] = (byte) 0x95;
			data[2] = index;
			
			for (int i = 0; i < num_len; ++i) {
//				data[3 + i] = n[i];
				
				if (i % 2 == 0) {
					data[3 + i] = n[i + 3];
				} else {
					data[3 + i] = n[i + 1];
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
			ret = (0x26 << 16) | (19 << 8) | 19;			
			byte[] buf = new byte[] { 0x2, (byte) 0xa, 0x1, (byte) 0x6a };

			sendDataToCanbox(buf, buf.length);
		} else {
			byte[] buf = new byte[] { 0x2, (byte) 0xad, 0x0, (byte) (data-9) };
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				buf[2] = 6;
				break;
			case EQ_CMD_SET_MIDDLE:
				buf[2] = 5;
				break;
			case EQ_CMD_SET_LOW:
				buf[2] = 4;
				break;
			case EQ_CMD_SET_ZONE_FR:
				buf[2] = 3;
				break;
			case EQ_CMD_SET_ZONE_LR:
				buf[2] = 2;
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

		mEQData[0] = (byte) (buf[7] + 9);
		mEQData[1] = (byte) (buf[6] + 9);
		mEQData[2] = (byte) (buf[5] + 9);
		mEQData[3] = (byte) (buf[4] + 9);
		mEQData[4] = (byte) (buf[3] + 9);
		mEQData[5] = buf[2];
		if (mEQData != null) {
			returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
		}
	}
}
