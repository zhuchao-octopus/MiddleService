package com.zhuchao.android.car.cartype.xinfeiyang;

import android.os.Handler;
import android.os.Message;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.R;

public class NissanXinFeiYang extends Canbox{

	public NissanXinFeiYang(){

		buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdAngle((byte) 0x29, (byte) 0x0, 360);
		buildCmdRadarFront((byte) 0x1d, (byte) 0x0, (byte) 0x4);
		buildCmdRadarBack((byte) 0x1e, (byte) 0x0, (byte) 0x4);
		buildCmdVersion((byte) 0x30, (byte) 0x0);
		buildCmdEQ((byte) 0x41, (byte) 0x0, 6);
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;
		mIdKey2 = 0x22;
		MAP_KEYS2 = KEYS_WHEEL2;
		
		mIdAC = 0x55;

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x35,0x6c,0x6d,0x6e,0x6b	};
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0xe2, 0x01, 0 };
		switch(CarUtil.getModelId()){
		case 33:
			if (CarUtil.getCarTypeConfig() == 0){
				cmd [2] = 4;
			}else{
				cmd [2] = 5;
			}
			break;
		case 50:
			if (CarUtil.getCarTypeConfig() == 0){
				cmd [2] = 6;
			}else{
				cmd [2] = 7;
			}
			break;
		case 42:
			if (CarUtil.getCarTypeConfig() == 0){
				cmd [2] = 8;
			}else{
				return null;
			}
			break;
		default:
			return null;
		}
		return cmd;
	}
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x4, MyCmd.Keycode.PREVIOUS },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x7, MyCmd.Keycode.MODLE },

		{ 0x8, MyCmd.Keycode.SPEECH },
		{ 0x9, MyCmd.Keycode.BT_DIAL },
		{ 0xa, MyCmd.Keycode.BT_HANG },	

		{ 0x13, MyCmd.Keycode.PREVIOUS },
		{ 0x14, MyCmd.Keycode.NEXT },

		{ 0x16, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x17, MyCmd.Keycode.MUTE },
		{ 0x18, MyCmd.Keycode.BACK },	


	};

	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x4, MyCmd.Keycode.PREVIOUS },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x5, MyCmd.Keycode.RADIO },
		{ 0x6, MyCmd.Keycode.SETUP },
		{ 0x7, MyCmd.Keycode.AUDIO },
		{ 0x8, MyCmd.Keycode.NEXT },
		{ 0x9, MyCmd.Keycode.PREVIOUS },
		{ 0xa, MyCmd.Keycode.POWER },
		{ 0x11, MyCmd.Keycode.NUMBER1 },
		{ 0x12, MyCmd.Keycode.NUMBER2 },
		{ 0x13, MyCmd.Keycode.NUMBER3 },
		{ 0x14, MyCmd.Keycode.NUMBER4 },
		{ 0x15, MyCmd.Keycode.NUMBER5 },
		{ 0x16, MyCmd.Keycode.NUMBER6 },
		{ 0x20, MyCmd.Keycode.KEY_DISPLAY },
		{ 0x21, MyCmd.Keycode.AUDIO },
		{ 0x22, MyCmd.Keycode.MODLE },
		{ 0x23, MyCmd.Keycode.BT },
		{ 0x24, MyCmd.Keycode.BACK },
		{ 0x25, MyCmd.Keycode.SETUP },
		{ 0x26, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x27, MyCmd.Keycode.PREVIOUS },
		{ 0x28, MyCmd.Keycode.NEXT },
		{ 0x29, MyCmd.Keycode.PREVIOUS },
		{ 0x2a, MyCmd.Keycode.NEXT },
		{ 0x2b, MyCmd.Keycode.ROLL_PREV },
		{ 0x2c, MyCmd.Keycode.ROLL_NEXT },
		{ 0x2d, MyCmd.Keycode.ALL_APP },
		{ 0x2e, MyCmd.Keycode.SETUP },
//		{ 0x2f, MyCmd.Keycode. },
		{ 0x30, MyCmd.Keycode.KEY_AIR_CONTROL },
		{ 0x31, MyCmd.Keycode.AS },
		{ 0x32, MyCmd.Keycode.KEY_REPEAT },
		{ 0x33, MyCmd.Keycode.KEY_CAMERA },
		{ 0x34, MyCmd.Keycode.NAVIGATION },
		{ 0x35, MyCmd.Keycode.SPEECH },

	};
	@Override
	public int getAngleValue2(byte[] data) {


		int angle = ((data[2] & 0xff) | (((data[3] & 0xf)) << 8));

		if (angle != 0) {
			if ((data[3] & 0x80) == 0) {
				angle = angle - 0x1000;
			}
		}
		
		return angle;

	}
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 0x10){
			data = (byte)0;
		} else if ((data&0xff) == 0x50){
			data = (byte)0xff;
		} else if ((data&0xff) == 0xfe){
			data = (byte)(32+((data&0xff)-0x20));
		} else {
			//data = 
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) (
				((data[6] & 0x08) << 2)
				| ((data[6] & 0x01) << 4)
				| ((data[6] & 0x02) << 5)
				| ((data[6] & 0x04) << 0)
				| ((data[6] & 0x40) >> 6)
				| ((data[6] & 0x80) >> 6));				
		
		switch((data[4] & 0xf0)>>4){
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
			airData[1] = (byte) (0xc0);
			break;
		case 6:
			airData[1] = (byte) (0x80);
			break;
		case 7:
			airData[1] = (byte) (0xe0);
			break;
		default:
			airData[1] = 0;
			break;
		}

		airData[1] |= (byte) (data[4] & 0x0f);
		

		airData[2] = (byte) (data[2] & 0xff);
		airData[3] = (byte) (data[3] & 0xff);
		
		if (((data[6] & 0x20) != 0)){

			airData[4] = (byte) (0x80);
		}
		
		if (airData[1] == 0) {
//			Util.zeroBuf(airData);
		}
		super.parseACInfo(airData);
	}		
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		
	}

	public void setMediaSrc(int source, byte type, byte[] b) {

		byte s = 0;
		byte[] mData;

		mData = new byte[] { (byte) 0x91, 0x2, 0, 1 };
		
		if (b[0] < 0x10) {
			s = 0x10;
			mData[3] = 2;
 		} else {
 			
 		}
		

		sendDataToCanbox(mData, mData.length);

		mData = new byte[] { (byte) 0x91, 0x2, 1, s };

		sendDataToCanbox(mData, mData.length);

		mData = new byte[] { (byte) 0x91, 0x4, 2, 0, b[2], b[1] };

		sendDataToCanbox(mData, mData.length);

	}

	public void setMediaSrc(int source) {
		
		byte s = 0;
		switch (source) {
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x05;
			break;
		default:
			return;
		}

		byte []mData = new byte[] { (byte) 0x91, 0x2, 0 , s};		

		sendDataToCanbox(mData, mData.length);
		
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

	private void sendEQCmd(byte id, int step) {
		byte data;
		if (step == 0) {
			return;
		} else if (step > 0) {
			data = 0x1;
			step--;
		} else {
			data = 0x0;
			step++;
		}
		byte[] buf = new byte[] { (byte) 0x84, 0x2, id, data };
		sendDataToCanbox(buf, buf.length);
		
		mHandler.removeMessages(SET_EQ_STEP);
		if (step != 0){
			mHandler.sendMessageDelayed(mHandler.obtainMessage(SET_EQ_STEP, id, step), 200);
		}
	}
	byte[] mEQBuf = new byte[] { 5, 0, 5, 5, 5, 0 };
	
	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (40 << 16)|(11 << 8) | 11;
			

			byte []mData = new byte[] { (byte) 0x90, 0x2, 0x41, 0};		

			sendDataToCanbox(mData, mData.length);
		} else {
			byte id;
			int step;
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				id = 0x5;
				step = data - mEQBuf[0];
				break;
			case EQ_CMD_SET_LOW:
				id = 0x4;
				step = data - mEQBuf[2];
				break;
			case EQ_CMD_SET_ZONE_FR:
				id = 0x1;
				step = data - mEQBuf[3];
				break;
			case EQ_CMD_SET_ZONE_LR:
				id = 0x2;
				step = data - mEQBuf[4];
				break;
			case EQ_CMD_SET_VOLUME:
				id = 0x7;
				step = data - mEQBuf[5];
				break;
			default:
				return 0;
			}
			
			sendEQCmd(id,step);
			
		}
		return ret;
	}
	public void parseEQ(int id, byte[] buf) {		
		
		mEQBuf[0] = (byte) (buf[4] - 5);
		mEQBuf[1] = (byte) (buf[3] - 5);
		mEQBuf[2] = (byte) (buf[2] - 5);
		mEQBuf[3] = (byte) (buf[5] - 5);
		mEQBuf[4] = (byte) (buf[6] - 5);
		mEQBuf[5] = buf[7];
		super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQBuf);
	}
}
