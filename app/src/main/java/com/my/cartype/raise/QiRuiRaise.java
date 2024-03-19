package com.my.cartype.raise;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class QiRuiRaise extends Canbox{

	public QiRuiRaise(){
		mIdAC = 0x21;
		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 4);
		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 4);
		buildCmdAngle((byte) 0x29, (byte) 0x0, 540);
		buildCmdOutTemp((byte) 0x10, (byte) 0x10);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdKey = 0x20;
		if (CarUtil.getCarTypeConfig() == 2) {
			for (int i = 0; i < KEYS_WHEEL.length; ++i) {
				if (KEYS_WHEEL[i][0] == 0x28) {
					KEYS_WHEEL[i][1] = MyCmd.Keycode.NAVIGATION;
				}
			}
		}
		MAP_KEYS = KEYS_WHEEL;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = { 0x40	};
	

	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0xee, 0x02, 0x4, 0 };
		switch(CarUtil.getModelId()){
		case 7:
		case 15:
		case 16:
			cmd [3] = 1;
			break;
		case 9:
			cmd [3] = 3;
			break;
		case 11:
			cmd [3] = 4;
			break;
		case 12:
			cmd [3] = 5;
			break;
		case 17:
			cmd [3] = 6;
			break;
		case 18:
			cmd [3] = 7;
			break;
		case 21:
			cmd [3] = 8;
			break;
		case 22:
			cmd [3] = 9;
			break;
		case 23:
			cmd [3] = 0xa;
			break;
		case 24:
			cmd [3] = 0xb;
			break;
		case 25:
			cmd [3] = 0xd;
			break;
		case 26:
			cmd [3] = 0xc;
			break;
		case 27:
			cmd [3] = 0xe;
			break;
		default:
			return null;
		}
		return cmd;
	}
	
	private static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode .VOLUME_DOWN}, 
			{ 0x3, MyCmd.Keycode.KEY_TURN_A },
			{ 0x4, MyCmd.Keycode.KEY_TURN_D }, 
			{ 0x5, MyCmd.Keycode.BT_DIAL }, 
			{ 0x6, MyCmd.Keycode.BT_HANG },
			{ 0x7, MyCmd.Keycode.MODLE },
			{ 0x8, MyCmd.Keycode.SPEECH },

			{ 0x10, MyCmd.Keycode.KEY_FM },
			{ 0x11, MyCmd.Keycode.KEY_AM },
			{ 0x12, MyCmd.Keycode.POWER },
			{ 0x13, MyCmd.Keycode.PREVIOUS },
			{ 0x14, MyCmd.Keycode.NEXT },
			{ 0x15, MyCmd.Keycode.MUTE },
			{ 0x16, MyCmd.Keycode.KEY_RADIO_SCAN },
			{ 0x17, MyCmd.Keycode.MODLE },
			{ 0x18, MyCmd.Keycode.AS },
			{ 0x19, MyCmd.Keycode.SETUP },
			{ 0x20, MyCmd.Keycode.VOLUME_ROLL_UP },
			{ 0x21, MyCmd.Keycode.VOLUME_ROLL_DOWN },
			{ 0x22, MyCmd.Keycode.EQ },
			{ 0x23, MyCmd.Keycode.MENU },
			{ 0x24, MyCmd.Keycode.BT },
			{ 0x25, MyCmd.Keycode.RADIO },
			{ 0x26, MyCmd.Keycode.HOME },
			{ 0x27, MyCmd.Keycode.EASY_CONNECT },
			{ 0x28, MyCmd.Keycode.BT },
			{ 0x29, MyCmd.Keycode.NAVIGATION },
			{ 0x2a, MyCmd.Keycode.BACKLIGHT_OFF },
			{ 0x2b, MyCmd.Keycode.PLAY_PAUSE },
			{ 0x2c, MyCmd.Keycode.ROLL_NEXT },
			{ 0x2d, MyCmd.Keycode.ROLL_PREV },
			{ 0x2e, MyCmd.Keycode.VOLUME_UP },
			{ 0x2f, MyCmd.Keycode.VOLUME_DOWN },
	};

	@Override
	public int getAngleValue(byte[] data) {
		int angle = ((data[2] & 0xff) | ((data[3] & 0xff) << 8));
		
		int max = 0x2200;

		angle -= 0x8000;
		angle = ((angle * 3000) / max);

		
		return angle;
	}
	@Override
	public int getACTemp(byte data) {
		
		if ((data & 0xff) >= 1 && (data & 0xff) <= 32) {
			data = (byte) (36 + (data-1));
		}
		return data & 0xff;
	}
	public void parseACInfo(byte[] data)
	{			
		byte[]	airData = new byte[8];
		airData[0] = (byte) (((data[2] & 0x4c))
				| ((data[2] & 0x10) << 1)
				| ((data[2] & 0x02) >> 1));
		airData[0] |= 0x80;
		
		
		switch((data[3] & 0xff)){
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
		case 6:
			airData[0] |= 0x02;
			break;
		default:
			airData[1] = 0;
			break;
		}
		airData[1] |= (byte) (data[4] & 0x0f);


		airData[2] = data[5];
		airData[3] = data[6];
		airData[4] = (byte) ((data[2] & 0x80) >> 5);

		airData[7] = (byte) ((data[2] & 0x01) << 5);
		if ((data[4] & 0x0f) == 0) {
//			Util.zeroBuf(airData);
		}
		super.parseACInfo(airData);
	}

	private byte[] mData = new byte[] { (byte) 0xc0, 0x8, 0, 0, 0, 0, 0, 0, 0,
			0 };

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		byte h = (byte) ((time / 3600));
		byte min = (byte) ((time / 60) % 60);
		byte sec = (byte) ((time) % 60);
		// ++play;

		byte s = 0;
		byte s2 = 0;
		switch (source) {
		case MyCmd.SOURCE_DVD:
			s = 0x2;
			s2 = 0x10;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			s = 8;
			s2 = 0x11;
			break;
		case MyCmd.SOURCE_BT:
			s = 0xb;
			s2 = 0x10;
			break;
		default:
			s = 0x07;
			s2 = 0x30;
			break;
		}

		if (MyCmd.SOURCE_DVD == source) {
			mData = new byte[] { (byte) 0xc0, 0x8, s, s2, 0,
					(byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec };

		} else {
			mData = new byte[] { (byte) 0xc0, 0x8, s, s2, 0, 0, 
					(byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8),
					 min, sec };
		}

		// if (mPhoneStatus < HFP_INFO_CALLED) {

		sendDataToCanbox(mData, mData.length);
		// }
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		setMediaSrc(0);
		if (b[0] != 0x10) {
			b[0] += 1;
		}
		mData = new byte[] { (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0,
				0, 0 };
		sendDataToCanbox(mData, mData.length);
	}

	public void setMediaSrc(int source) {// default is simple box
		byte s = 0;
		byte mediaType = 0;
		switch (source) {
		case 1:
			s = 2;
			mediaType = 0x10;
			break;
		case MyCmd.SOURCE_IPOD:
			s = 6;
			mediaType = 0x12;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
		case 0:
			return;
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
			mediaType = 0x40;
			break;
		default:
			s = 0x00;
			mediaType = 0x0;
			break;
		}

		// if (s == 0xb || s == 0x7) {
		// data = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0,
		// 0 };
		// } else {
		mData = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0 };
		// }

		sendDataToCanbox(mData, mData.length);
	}

	
	public boolean requestAngleData() {
		byte[] data3 = new byte[] { (byte) 0x90, 0x2, 0x29, 0 };
		sendDataToCanbox(data3, data3.length);
		return true;
	}
	
	public int getOutTemp(byte[] data) {//
		int t = ((data[3] & 0xff));
		if (t != 0xff) {
			t = t * 5 - 550;
			return t;
		}		
		return CarUtil.CLEAR_OUT_DOOR_TEMP;
	}

	public int getOutTempUnit(byte[] data) {//
		return data[2] & 0x1;
	}
	
	public void sendId3(byte index, String num) {

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = num.getBytes("UTF-8");
			
			int num_len = n.length;
			if((n[0]&0xff) == 0xff && (n[1]&0xff) == 0xfe){
				num_len-=2;
			}
			
			int len = n.length + 3;
			if (len > 0x21) {
				len = 0x21;
			}
			byte[] data = new byte[len];
			switch(index){
			case 1:
				data[0] = (byte) 0x70;
				break;
			case 2:
				data[0] = (byte) 0x71;
				break;
			}
			data[1] = (byte) (len - 2);
			data[2] = 0x12;
			for (int i = 0; i < (len - 3); ++i) {
				data[3 + i] = n[i+(n.length-num_len)];
			}
			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

			Log.d("PSASimple", "sendId3"+e);
		}
	}
	
	public void setSongName(String s) {
		sendId3((byte)1, s);
	}

	public void setSongAritst(String s) {
		sendId3((byte)2, s);
	}
	
	public void setPhone(int phone_status, String num) {// default is simple box
		
		
		 if (mPhoneStatus != HFP_INFO_CALLING
				 && phone_status == HFP_INFO_CALLING){
			 mCallingTime = 0;
			 mHandler.removeMessages(0);
			 mHandler.sendEmptyMessageDelayed(0, 1000);
		 } else  if (phone_status!= HFP_INFO_CALLING){
			 mCallingTime = 0;
			 mHandler.removeMessages(0);
		 }
		 mPhoneStatus  = phone_status;
		byte status = 0;
		switch (phone_status) {
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
		if (mPhoneStatus == HFP_INFO_CALLING) {
			mCallingTime = 0;
		}
	}

	int mCallingTime;
	int mPhoneStatus;

	public void clear() {
		super.clear();
		mHandler.removeMessages(0);
		mPhoneStatus = 0;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (mPhoneStatus == HFP_INFO_CALLING) {
					++mCallingTime;
					byte[] data = new byte[] { (byte) 0xc3, 4, 0, 0, 0, 0 };
					data[4] = (byte) (((mCallingTime / 3600)));
					data[3] = (byte) (((mCallingTime % 3600) / 60));
					data[2] = (byte) ((mCallingTime % 3600) % 60);
					sendDataToCanbox(data, data.length);
					mHandler.sendEmptyMessageDelayed(0, 1000);
				}
				break;

			}
			super.handleMessage(msg);
		}
	};

	
	public void udpateVoiceControl(int data) {
		

//		Log.d("ffck", "udpateVoiceControl:"+Integer.toHexString(data));
		
		int cmd = (data & 0xff);
		byte param = (byte)((int)((data & 0xff00) >> 8));
		switch(cmd){
		case 0x7:
			cmd = 0x40;
			break;
		case 0x8:
			cmd = 0x41;
			break;
		case 0x10:
			cmd = 0x42;
			break;
		case 0x14:
			cmd = 0x43;
			break;
		case 0x9:			
			cmd = 0x44;
			if (param == 0){
				param = (byte)0x81;
			} else {
				param = (byte)0x80;
			}
			break;
		case 0xa:			
			cmd = 0x44;
			break;
		}
		
		byte[] buf = new byte[] { (byte) 0xef, 0x3, 0x7d, (byte) cmd,
				(byte) (param) };
		sendDataToCanbox(buf, buf.length);
	}
	
	public void requestVersion(){
		byte[] buf = new byte[] { (byte) 0x90, 0x2, 0x30, 0x0 };
		sendDataToCanbox(buf, buf.length);
	}
}
