package com.zhuchao.android.car.cartype.raise;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class AudiRaise extends Canbox{

	public AudiRaise(){
		mIdAC = 0x21;
		buildCmdDoor((byte) 0x41, (byte) 0x0, (byte) 0x1f, (byte) 0x13);
		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
		buildCmdAngle((byte) 0x26, (byte) 0x0, 540);
		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x41, 0x25	};
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, 
			{ 0x3, KEY_NEXTSONG },
			{ 0x4, KEY_PREVIOUSSONG }, 
			{ 0x5, KEY_BT }, 
			{ 0x6, KEY_MUTE },
			{ 0x7, KEY_SOURCE },
			{ 0x8, KEY_MIC },
			{ 0x9, MyCmd.Keycode.BT_DIAL },
			{ 0xa, MyCmd.Keycode.BT_HANG },
			{ 0xb, KEY_BACK },
			{ 0x16, MyCmd.Keycode.PLAY_PAUSE },
	};
	
	public void parseACInfo(byte[] data)
	{
		if (data[4] == 0xfe){
			data[4] = (byte)0xff;
		} 
		if (data[5] == 0xfe){
			data[5] = (byte)0xff;
		} 
		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) (data[2] & 0xff);		
		airData[1] = (byte) (data[3] & 0xff);
		
		airData[2] = (byte) (data[4] & 0xff);
		airData[3] = (byte) (data[5] & 0xff);
		airData[4] = (byte) (data[6] & 0xff);
		
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		byte min = (byte) ((time / 60) % 60);
		byte sec = (byte) ((time) % 60);
		
		byte[] data;

		if (MyCmd.SOURCE_DVD != source) {
			++play;
			data = new byte[] { (byte) 0xc3, 0x6, (byte) (play & 0xFF),
					(byte) ((play >> 8) & 0xFF), (byte) (total & 0xFF),
					(byte) ((total >> 8) & 0xFF), min, sec };
		} else {
			data = new byte[] { (byte) 0xc3, 0x6, (byte) (1 & 0xFF),
					(byte) ((play) & 0xFF), (byte) (total & 0xFF),
					(byte) ((0) & 0xFF), min, sec };
		}
		sendDataToCanbox(data, data.length);
	}
	public void setMediaSrc(int source, byte type, byte []b){
		setMediaSrc(0);
		if (b[0] != 0x10){
			b[0] += 1;
		}
		byte []data = new byte[]{(byte)0xc2, 0x4, b[0],b[1],b[2], 0};
		sendDataToCanbox(data, data.length);
	}

	public void setMediaSrc(int source) {// default is simple box
		byte s = 0;
		byte mediaType = 0;
	
		switch (source) {
		case MyCmd.SOURCE_RADIO:
			s = 1;
			mediaType = 1;
			break;
		case MyCmd.SOURCE_DVD:
			s = 2;
			mediaType = 0x10;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x09;
			mediaType = 0x12;
			break;
		case MyCmd.SOURCE_AUX:
			s = 0x07;
			mediaType = 0x30;
			break;
		case MyCmd.SOURCE_DTV:
			s = 0x0A;
			mediaType = 0x30;
			break;
		case MyCmd.SOURCE_BT:
			s = 0x0b;
			mediaType = 0x30;
			break;
		}
		byte[] data;		
		if ((source == MyCmd.SOURCE_AUX) || (source == MyCmd.SOURCE_DTV) || (source == MyCmd.SOURCE_BT)){//clear 
			data = new byte[] { (byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0 };
			sendDataToCanbox(data, data.length);
			Util.doSleep(1);
		}
		data = new byte[] { (byte) 0xc0, 0x2, s, mediaType };
		sendDataToCanbox(data, data.length);
	}
	
	public void setVolume(int volume) {		
		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
		sendDataToCanbox(data, data.length);	
	}
	
	public boolean requestAngleData() {
		byte[] data3 = new byte[] { (byte) 0x90, 0x2, 0x26, 0 };
		sendDataToCanbox(data3, data3.length);
		return true;
	}
	
	public int getOutTemp(byte[] data) {//
		short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
		return t;
	}

	public void setPhone(int phone_status, String num) {// default is simple box

		byte status = 0;
		switch (phone_status) {
		case HFP_INFO_INITIAL:
		case HFP_INFO_READY:
		case HFP_INFO_CONNECTING:
			status = 6;
			break;
		case HFP_INFO_CONNECTED:
			status = 0;
			break;
		case HFP_INFO_CALLED:
			status = 2;
			break;
		case HFP_INFO_INCOMING:
			status = 1;
			break;
		case HFP_INFO_CALLING:
			status = 4;
			break;
		}
		if (num == null) {
			num = "";
		}

		byte[] n = num.getBytes();
		int num_len = n.length;
		int len = num_len + 4;
		byte[] data = new byte[len];
		data[0] = (byte) 0xc5;
		data[1] = (byte) (len - 2);
		data[2] = status;
		data[3] = 0x1;
		for (int i = 0; i < num_len && i < (len - 4); ++i) {
			data[4 + i] = n[i];
		}
		sendDataToCanbox(data, data.length);
	}

	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (31 << 16) | (19 << 8) | 19;

			byte[] buf = new byte[] { (byte) 0x90, 0x2, 0x51, 0 };
			sendDataToCanbox(buf, buf.length);
		} else {
			byte[] buf = new byte[] { (byte) 0xa0, 0x2, 0x0, (byte) data };
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				buf[2] = 5;
				break;
			case EQ_CMD_SET_MIDDLE:
				buf[2] = 4;
				break;
			case EQ_CMD_SET_LOW:
				buf[2] = 3;
				break;
			case EQ_CMD_SET_ZONE_FR:
				buf[2] = 2;
				break;
			case EQ_CMD_SET_ZONE_LR:
				buf[2] = 1;
				break;
			case EQ_CMD_SET_VOLUME:
				buf[2] = 0;
				break;
			default:
				return 0;
			}

			sendDataToCanbox(buf, buf.length);
		}
		return ret;
	}
}
