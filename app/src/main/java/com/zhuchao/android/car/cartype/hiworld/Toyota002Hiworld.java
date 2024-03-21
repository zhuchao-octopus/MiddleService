package com.zhuchao.android.car.cartype.hiworld;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.common.util.AppConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class Toyota002Hiworld extends Canbox{

	public Toyota002Hiworld(){
		buildCmdDoor((byte) 0x1a, (byte) 0x2, (byte) 0xfc, (byte) 0x03);
		//buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
		//buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
		buildCmdAngle((byte) 0x1a, (byte) 0x0, 540);
		buildCmdEQ((byte) 0x87, (byte) 0xff, 0);
		buildCmdOutTemp((byte) 0x1a, (byte) 0x10);
		buildCmdVersion((byte) 0xf5, (byte) 0x0);
		

		buildCmdKey((byte) 0x81, (byte) 1, (byte) 6, (byte) 2, KEYS_WHEEL);
		buildCmdKey((byte) 0x21, (byte) 0, (byte) 2, (byte) 0, KEYS_WHEEL2);
		

		mIdKey3 = 0x021122;
		MAP_KEYS3 = KEYS_WHEEL3;
		
		mIdAC = 0x82;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}

	private final static byte[] IDS_TO_CANBOXSETTING = {(byte) 0x83,(byte) 0x84, (byte)0x85,(byte)0x86, };
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x4, MyCmd.Keycode.SPEECH },
		{ 0x5, MyCmd.Keycode.BT_DIAL },
		{ 0x6, MyCmd.Keycode.BT_HANG },
		{ 0x8, MyCmd.Keycode.PREVIOUS },
		{ 0x9, MyCmd.Keycode.NEXT },
		{ 0xc, MyCmd.Keycode.MODLE },
		{ 0x20, MyCmd.Keycode.KEY_AIR_CONTROL },
	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x20, MyCmd.Keycode.NAVIGATION },
		{ 0x24, MyCmd.Keycode.AUDIO },
		{ 0x2f, MyCmd.Keycode.HOME },
		{ 0x30, MyCmd.Keycode.BT },		
		{ 0x39, MyCmd.Keycode.KEY_DISPLAY },
		{ 0x42, MyCmd.Keycode.EQ },
	};
	
	private final static byte[][] KEYS_WHEEL3 = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
	};
	
	@Override
	public int getAngleValue2(byte[] data) {
		// TODO Auto-generated method stub
		
		short angle = (short) ((data[9] & 0xff)
				| (((data[8] & 0xff)) << 8));
		
		return -angle;
	}
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0) {
			data = (byte) 0xfa;
		} else if ((data & 0xff) == 0x1) {
			data = (byte) 0;
		} else if ((data & 0xff) == 0xff) {

		} else {

		}
		return data;
	}

	
	public void parseACInfo(byte[] data)
	{

		byte[]	airData = new byte[13];
		airData[0] = (byte) ((data[2] & 0x0c) 
				| ((data[2] & 0x40) << 1)
				| ((data[6] & 0x02) >> 0)
				| ((data[6] & 0x08) >> 3)
				| ((data[6] & 0x04) << 4));		
	
		if (((data[2]&0x30)>>4) == 2){
			airData[0] |= 0x20;
		} else {
			
		}
			
			
		switch ((data[5] & 0xf0) >> 4) {
		case 1:
			airData[1] = (byte) (0x40);
			break;
		case 2:
			airData[1] = (byte) (0x20);
			break;
		case 3:
			airData[1] = (byte) (0x60);
			break;
		case 4:
			airData[1] = (byte) (0xa0);
			break;
		}

		airData[1] |= (byte) ((data[5] & 0xf));
	

		airData[2] = data[3];
		airData[3] = data[4];
		

		airData[9] = (byte) (
				((data[2] & 0x02) << 3)
				| ((data[2] & 0x01) << 5)
				| ((data[6] & 0x01) << 3));	
		

		airData[12] = (byte) (
				((data[6] & 0x10) << 2));	
		
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	
//	private void parseACInfoEx(byte[] data)
//	{
//
//		
//
//		airData[9] = (byte)(				
//				((data[2]&0x00)>>0) |
//				((data[2]&0x10)>>3)
//				);	
//		airData[10] = (byte)getACTemp(data[3]);	
//
//		airData[11] = data[4];	
//
//	
//		super.parseACInfoRear(airData);
//	}	
	
	int mPreSource = -1;
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		if (data[0] == (byte) 0x83) {
			Intent it = null;
			String top = AppConfig.getTopActivity();
			int s = data[2] & 0xf;
			if (s != mPreSource) {
				mPreSource = s;
				if ((data[2] & 0xf) == 0) {
					if (!"com.canboxsetting/com.canboxsetting.RadioActivity".equals(top)) {
						it = new Intent(Intent.ACTION_VIEW);
						it.setClassName("com.canboxsetting", "com.canboxsetting.RadioActivity");
						it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);

					}

				} else if ((data[2] & 0xf) == 1) {
					if (!"com.canboxsetting/com.canboxsetting.JeepCarCDPlayerActivity".equals(top)) {
						it = new Intent(Intent.ACTION_VIEW);
						it.setClassName("com.canboxsetting", "com.canboxsetting.JeepCarCDPlayerActivity");
						it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
					}
				}
				//				else {
				//
				//					byte[] buf = new byte[] { 0x2, (byte) 0xf3, 0x1, 5 };
				//					sendDataToCanbox(buf, buf.length);
				//				}
			}

			if (it != null) {
				try {
					mContext.startActivity(it);
				} catch (Exception e) {
					// Log.e(TAG, e.getMessage());
				}
			}
		} else {
			super.parseCanboxData(data, len);
		}

	}
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}
	public void setMediaSrc(int source, byte type, byte []b){
	}

	public void setMediaSrc(int source) {
		switch (source) {
		case MyCmd.SOURCE_AV_OFF: {

			byte[] buf = new byte[] { 0x2, (byte) 0xf3, 0x1, 0 };
			sendDataToCanbox(buf, buf.length);
		}
			break;
		case MyCmd.SOURCE_AUX:
			break;
		default:
			byte[] buf = new byte[] { 0x2, (byte) 0xf3, 0x1, 5 };
			sendDataToCanbox(buf, buf.length);
			break;
		}
	}

	

	
	public int getOutTemp(byte[] data) {//
		int t = data[3]*10;
		return t;
	}


	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (63 << 16) | (31 << 8) | 31;

			byte[] buf = new byte[] { 0x2, (byte) 0x6a, (byte) 0x87, (byte) 0xff };
			sendDataToCanbox(buf, buf.length);
		} else {
			byte[] buf = new byte[] {  0x2, (byte) 0xf0, 0, (byte) data };
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
				buf[3] = (byte)(data - mEQData[0]);
				
//				int step = data - mEQData[0];
//				sendEQCmd(1, step);
				break;
			default:
				return 0;
			}

			sendDataToCanbox(buf, buf.length);
		}
		return ret;
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
	
	public void parseEQ(int id, byte[] buf) {
		
		if (mEQData == null) {
			mEQData = new byte[6];
		}
		
		mEQData[0] = buf[7];
		mEQData[1] = buf[6];		
		mEQData[2] = buf[5];		
		mEQData[3] = buf[4];	
		mEQData[4] = buf[3];
		mEQData[5] = buf[2];

		
		super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
	}
	public void setVolume(int volume) {
		byte[] buf = new byte[] { (byte) 0x8f, 0x3, 1, (byte)volume, 0 };
		sendDataToCanbox(buf, buf.length);
	}
	
	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword1(data, len);
	}
	

	@Override
	public void startConnect() {
		
	}
	
	@Override
	public void stopConnect() {
		
	}
	
	
}
