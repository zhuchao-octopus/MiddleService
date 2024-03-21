package com.zhuchao.android.car.cartype.hiworld;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class QiRuiHiworld extends Canbox{

	public QiRuiHiworld(){
		buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xf8, (byte) 0x04);
		buildCmdAngle((byte) 0x11, (byte) 0x0, 540);


		buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0x3);
		buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 0x3);
		buildCmdRadarFrontEx((byte)0x4);
		
		buildCmdVersion((byte) 0xf0, (byte) 0x0);		
		mIdAC = 0x31;
		
		buildCmdKey((byte) 0x11, (byte) 2, (byte) 4, (byte) 0, KEYS_WHEEL);
		buildCmdKey((byte) 0x21, (byte) 2,  KEYS_WHEEL2);

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
	private final static byte[] IDS_TO_CANBOXSETTING = { (byte) 0x87,
			(byte) 0xe8, (byte) 0x32, 0x26 };
	
	private final static byte[][] KEYS_WHEEL = {
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
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x25, MyCmd.Keycode.NAVIGATION },
		{ 0x2b, MyCmd.Keycode.HOME },
		{ 0x37, MyCmd.Keycode.SETUP },
		{ 0x43, MyCmd.Keycode.BT },
		{ 0x45, MyCmd.Keycode.VOLUME_UP },
		{ 0x46, MyCmd.Keycode.VOLUME_DOWN },
		};
	private final static byte[][] KEYS_WHEEL3 = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { 0x2, (byte) 0x24, 0x0, 0x35 };
		switch(CarUtil.getModelId()){
		case 14:
			cmd[2] = 0x12;
			break;
		case 3:
		case 20:
		case 24:
			cmd[2] = 0xb;
			break;
		case 1:
		case 13:
		case 15:
			cmd[2] = 0xe;
			break;
		case 4:
		case 22:
			cmd[2] = 0x13;
			break;
		case 5:
		case 11:
		case 18:
		case 23:
		case 10:
		case 16:
			cmd[2] = 0x14;
			break;
		case 6:
			cmd[2] = 0x15;
			break;
		case 8:
			cmd[2] = 0x16;
			break;
		default:
			return null;
		}
		return cmd;
	}
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 0xff){

		} else if ((data&0xff) == 0xfe){
			data = 0;
		} else {
			//data = 
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[10];
		
		airData[0] = (byte) ((data[2] & 0x08) 
				| ((data[2] & 0x40) << 1)
				| ((data[3] & 0x44) >> 0)
				| ((data[3] & 0x10) << 1)
				| ((data[4] & 0x20) >> 5)
				| ((data[4] & 0x10) >> 3));				
		

		airData[4] = (byte) (((data[2] & 0x20) >> 3));	
		
		switch((data[6] & 0xff)){
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
		default:
			airData[1] = 0;
			break;
		}

		airData[1] |= (byte) (data[7] & 0x0f);
		

		airData[2] = data[8];
		airData[3] = data[9];

		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	
	public void parseCanboxData(byte[] data, int len) {
		if (data[0] == 0x41) {
			byte max = 3;
			byte maxMiddle = 4;
			if (data[13] == 1) {
				maxMiddle = max = (byte) 0xfe;
			}
			buildCmdRadarBack((byte) 0x41, (byte) 0x0, max, maxMiddle, (byte) 4);
			buildCmdRadarFront((byte) 0x41, (byte) 0x0, max, maxMiddle, (byte) 4);
			super.parseCanboxData(data, len);
		} else {
			super.parseCanboxData(data, len);
		}
	}
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
//		byte type = 7;
//		switch (source) {
//		case MyCmd.SOURCE_MUSIC:
//		case MyCmd.SOURCE_VIDEO:
//			++play;
//			type = 0xd;
//			break;
//		}
//
//		String s = String.format("%03d      %03d", play, total, Locale.ENGLISH);
//		sendLcdInfo(type, s, false);

		
	}
	public void setMediaSrc(int source, byte type, byte []b){
		int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
		if (source == MyCmd.SOURCE_RADIO) {
			String s;

			if (b[0] >= 0x10) { // am

				if (freq < 1000) {
					s = String.format("000 %d 0KHz", (freq), Locale.ENGLISH);
				} else {
					s = String.format("00 %d 0KHz", (freq), Locale.ENGLISH);
				}

				type = 4;
			} else {

				if (freq < 10000) {
					s = String.format("00  %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				} else {
					s = String.format("00 %d.%dMHz", (freq) / 100, (freq) % 100, Locale.ENGLISH);
				}
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
			data[2] = index;
			if (!end) {
				System.arraycopy(n, 0, data, 3, num_len);
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
			byte[] n = num.getBytes(StandardCharsets.UTF_8);

			int num_len = n.length;

			if (num_len >= (data_len - reserve -3 )) {
				num_len = (data_len - reserve - 3);
			}
			byte[] data;

			int len = data_len + 2;

			data = new byte[len];

			data[0] = (byte) (data_len);
			data[1] = index;
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
}
