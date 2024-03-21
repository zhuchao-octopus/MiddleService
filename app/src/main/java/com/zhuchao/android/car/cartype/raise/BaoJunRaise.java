package com.zhuchao.android.car.cartype.raise;

import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.nio.charset.StandardCharsets;


public class BaoJunRaise extends Canbox{

	public BaoJunRaise(){

		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdRadarFront((byte) 0x27, (byte) 0x0, (byte) 0x4);
		buildCmdRadarBack((byte) 0x26, (byte) 0x0, (byte) 0x4);
		buildCmdAngle((byte) 0x30, (byte) 0x5, 0x2198);
		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdAC = 0x20;
		mIdKey = 0x21;
		MAP_KEYS = KEYS_WHEEL;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x52	};
	
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0xee, 0x02, 0, 0 };
		if (CarUtil.getModelId() == 48) {
			cmd[2] = 4;
			cmd[3] = 1;
		} else {
			return null;
		}
		return cmd;
	}
	
	private final static byte[][] KEYS_WHEEL = {

		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, 

			{ 0x6, KEY_MUTE },
			{ 0x7, KEY_SOURCE },
			{ 0x9, MyCmd.Keycode.BT_DIAL },
			{ 0xa, MyCmd.Keycode.BT_HANG },

			{ 0xc, KEY_NEXTSONG },
			{ 0xb, KEY_PREVIOUSSONG }, 
			{ 0xd, KEY_MIC },
			{ 0xe, MyCmd.Keycode.BT },
			

			{ 0x20, MyCmd.Keycode.POWER},
			{ 0x21, MyCmd.Keycode.VOLUME_ROLL_UP},
			{ 0x22, MyCmd.Keycode.VOLUME_ROLL_DOWN},
			{ 0x23, MyCmd.Keycode.PLAY_PAUSE},
			{ 0x24, MyCmd.Keycode.ROLL_NEXT},
			{ 0x25, MyCmd.Keycode.ROLL_PREV},
			{ 0x26, MyCmd.Keycode.KEY_AM},
			{ 0x27, MyCmd.Keycode.KEY_FM},
			{ 0x28, MyCmd.Keycode.AUDIO},
			{ 0x29, MyCmd.Keycode.BT},
			{ 0x2a, MyCmd.Keycode.KEY_DISPLAY},
			{ 0x2b, MyCmd.Keycode.SETUP},
			{ 0x2c, MyCmd.Keycode.MENU},
			{ 0x2d, MyCmd.Keycode.HOME},
			{ 0x2e, MyCmd.Keycode.PREVIOUS},
			{ 0x2f, MyCmd.Keycode.NEXT},
			{ 0x30, MyCmd.Keycode.NAVIGATION},
			{ 0x31, MyCmd.Keycode.BACK},
			{ 0x32, MyCmd.Keycode.KEY_RADIO_SCAN},
	};

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
	public void parseACInfo(byte[] data) {
		byte[] airData = new byte[8];

		airData[0] = (byte) ((data[2] & 0x4c) 
				| ((data[2] & 0x10) << 1) 
				| ((data[2] & 0x02) >> 1));

		switch ((data[3] & 0xff)) {
		case 1:
			airData[1] = (byte) (0x40);
			break;
		case 2:
			airData[1] = (byte) (0x60);
			break;
		case 3:
			airData[1] = (byte) (0x20);
			break;
		case 4:
			airData[1] = (byte) (0xa0);
			break;
		case 5:
			airData[1] = (byte) (0x80);
			break;
		default:
			airData[1] = 0;
			break;
		}

		airData[1] |= (byte) (data[4] & 0x0f);

		airData[2] = (byte) (data[5] & 0xff);
		airData[3] = (byte) (data[6] & 0xff);
		
		if (airData[1] == 0) {
//			Util.zeroBuf(airData);
		}
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

//		switch (source) {
//		case MyCmd.SOURCE_MUSIC:
//		case MyCmd.SOURCE_VIDEO:
//			++play;
//			break;
//		}
//		if (mPhoneStatus < HFP_INFO_CALLED) {
//			byte[] data = new byte[] { (byte) 0xc0, 0x6, 8,
//					(byte) ((total & 0xff00) >> 8),
//					(byte) ((total & 0xff) >> 0),
//					(byte) ((play & 0xff00) >> 8), (byte) ((play & 0xff) >> 0),
//					1 };
//			sendDataToCanbox(data, data.length);
//		}
	}

	public void sendId3(byte index, String num) {

		try {
			if (num == null) {
				num = "";
			}
			byte[] n;// = num.getBytes();

			n = num.getBytes(StandardCharsets.UTF_8);
			int num_len = n.length;
			if (num_len >= 25) {
				num_len = 25;
			}
			int len = num_len + 6;
			byte[] data = new byte[len];

			data[0] = (byte) 0xc0;
			data[1] = (byte) (len - 2);
			data[2] = 8;
			data[3] = 0x12;
			data[4] = index;
			data[5] = (byte) num_len;

			//				Log.d("Accord2013Simple", i + ":" + n.length + ":" + num_len);
			System.arraycopy(n, 0, data, 6, num_len);
			
			sendDataToCanbox(data, data.length);
		} catch (Exception e) {


		}
	}
	public void setSongName(String s) {
		sendId3((byte) 0x1, s);
	}

	public void setSongAritst(String s) {
		sendId3((byte) 0x2, s);
	}
	public void setMediaSrc(int source, byte type, byte[] b) {
		byte[] data = new byte[] { (byte) 0xc0, 0x5, 0x1, b[0], b[1], b[2],
				(byte) (b[3] + 1), };
		sendDataToCanbox(data, data.length);
	}

	int mSource = MyCmd.SOURCE_NONE;

	public void setMediaSrc(int source) {// default is simple box
		byte s;
		switch (source) {
		case MyCmd.SOURCE_AUX:
			s = 7;
			break;
		case MyCmd.SOURCE_BT:
			s = 0xb;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x8;
			break;
		default:
			return;
		}
		if (mSource != source) {
			mSource = source;
			sendId3((byte) 0, "");
		}
		byte[] data = new byte[] { (byte) 0xc0, 0x2, s, 0 };
		sendDataToCanbox(data, data.length);
	}

	private int mPhoneStatus = HFP_INFO_INITIAL;

	public void setPhone(int status, String num) {// default is simple box

		mPhoneStatus = status;

		switch (status) {
		case HFP_INFO_INITIAL:
		case HFP_INFO_READY:
		case HFP_INFO_CONNECTING:
		case HFP_INFO_CONNECTED:
			status = 0;
			break;
		case HFP_INFO_CALLED:
			status = 3;
			break;
		case HFP_INFO_INCOMING:
			status = 1;
			break;
		case HFP_INFO_CALLING:
			status = 2;
			break;
		}

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = num.getBytes();

			n = num.getBytes(StandardCharsets.UTF_8);
			int num_len = n.length;
			if (num_len >= 25) {
				num_len = 25;
			}
			int len = num_len + 7;
			byte[] data = new byte[len];

			data[0] = (byte) 0xc0;
			data[1] = (byte) (len - 2);
			data[2] = 5;
			data[3] = (byte)status;
			data[4] = 0x12;
			data[5] = 1;
			data[6] = (byte) num_len;

			//				Log.d("Accord2013Simple", i + ":" + n.length + ":" + num_len);
			System.arraycopy(n, 0, data, 7, num_len);
			
			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

			Log.d("Accord2013Simple", "sendId3" + e);
		}

	}
}
