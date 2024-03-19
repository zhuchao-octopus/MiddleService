package com.my.cartype.td;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class MitsubishiTD extends Canbox{

	public MitsubishiTD(){
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

		buildCmdAngle((byte) 0x29, (byte) 0x0, 100);
		buildCmdEQ((byte) 0x17, (byte) 0x0, 6);

		buildCmdVersion((byte) 0x30, (byte) 0x0);
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;	
	}
	
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x4, MyCmd.Keycode.PREVIOUS },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x8, MyCmd.Keycode.SPEECH },
		{ 0x9, MyCmd.Keycode.BT_DIAL },
		{ 0x10, MyCmd.Keycode.BT_HANG },
	};
	
	
	@Override
	public int getAngleValue2(byte[] data) {

		int angle = data[2];
		
		return angle;
		
		
	}
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}
	public void setMediaSrc(int source, byte type, byte []b){
	}

	public void setMediaSrc(int source) {
	}
	
	@Override
	public void startConnect() {
		// TODO Auto-generated method stub
		super.startConnect();

		byte[] buf = new byte[] { (byte) 0x84, 0x2, 0x9, 1 };
		sendDataToCanbox(buf, buf.length);
	}
	
	@Override
	public void stopConnect() {
		// TODO Auto-generated method stub

		byte[] buf = new byte[] { (byte) 0x84, 0x2, 0x9, 0 };
		sendDataToCanbox(buf, buf.length);
		
		super.stopConnect();
	}

	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (45 << 16) | (23 << 8) | 11;

			byte[] buf = new byte[] { (byte) 0x90, 0x2, 0x17, 0 };
			sendDataToCanbox(buf, buf.length);
		} else {
			byte[] buf = new byte[] { (byte) 0x84, 0x2, 0, (byte) data };
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				buf[2] = 5;
				buf[3] = (byte)(buf[3] + 2);
				break;
			case EQ_CMD_SET_MIDDLE:
				buf[2] = 6;
				buf[3] = (byte)(buf[3] + 2);
				break;
			case EQ_CMD_SET_LOW:
				buf[2] = 4;
				buf[3] = (byte)(buf[3] + 2);
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
			//buf[3] = (byte) data ;
			sendDataToCanbox(buf, buf.length);
		}
		return ret;
	}

	public void parseEQ(int id, byte[] buf) {
		if (mEQData == null){
			mEQData = new byte[6];
		}
		mEQData[0] = (byte) (buf[6]-2);
		mEQData[1] = (byte) (buf[7]-2);
		mEQData[2] = (byte) (buf[5]-2);
		mEQData[3] = (byte) (buf[2]);
		mEQData[4] = (byte) (buf[3]);
		mEQData[5] = buf[9];
		super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
	}


}
