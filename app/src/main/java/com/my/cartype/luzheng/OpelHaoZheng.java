package com.my.cartype.luzheng;

import android.os.Handler;
import android.os.Message;

import com.common.util.AppConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;


public class OpelHaoZheng extends Canbox{

	public OpelHaoZheng(){
		buildCmdDoor((byte) 0x25, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

		buildCmdAngle((byte) 0x24, (byte) 0x0, 0x32);

		buildCmdVersion((byte) 0x71, (byte) 0x0);
		mIdAC = 0x8;
		mIdKey = 0xf;
		MAP_KEYS = KEYS_WHEEL;	
		mIdKey2 = 0xe;
		MAP_KEYS2 = KEYS_WHEEL2;	
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = { 
		0x1, 0x7, 0xb, 0x12, 0x13, 0x14	};
	

	private void setSource() {
		byte[] cmd = new byte[] { (byte) 0x82, 0x01, 0xa };

		if ("com.canboxsetting/com.canboxsetting.CarInfoActivity"
				.equals(AppConfig.getTopActivity())) {
			cmd[2] = 0x9;
		}

		sendDataToCanbox(cmd, cmd.length);
	}
	
	private void startSetSource(){
		mHandlerSetSource.removeMessages(0);
		mHandlerSetSource.sendEmptyMessageDelayed(0, 1100);
	}
	private Handler mHandlerSetSource = new Handler() {
		public void handleMessage(Message msg) {
			setSource();
			startSetSource();
			super.handleMessage(msg);
		}
	};
	
	public void startConnect() {
		startSetSource();
	};
	
	@Override
	public void stopConnect() {
		// TODO Auto-generated method stub

		mHandlerSetSource.removeMessages(0);
		super.stopConnect();
	}
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x4, MyCmd.Keycode.VOLUME_UP },
		{ 0x5, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x2, MyCmd.Keycode.KEY_SEEK_NEXT },
		{ 0x3, MyCmd.Keycode.KEY_SEEK_PREV },

		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x8, MyCmd.Keycode.KEY_TURN_D },
		{ 0x9, MyCmd.Keycode.KEY_TURN_A },
	};
	
	private final static byte KEYS_WHEEL2[][] = {
		{ 0x1, MyCmd.Keycode.HOME },
		{ 0xb, MyCmd.Keycode.PLAY_PAUSE },
		{ 0xe, MyCmd.Keycode.PREVIOUS },
		{ 0xf, MyCmd.Keycode.NEXT },
	};

	@Override
	public int getAngleValue2(byte[] data) {

		int angle = (data[2] & 0xff);

		if (angle >= 0x80){
			angle = 0x80-angle;
		}
		return angle;
		
		
	}
	@Override
	public int getACTemp(byte data, int unit) {
		// TODO Auto-generated method stub // TODO Auto-generated method stub
		if ((data & 0xff) == 0x7f) {
			data = (byte) 0xfa;
		} else {
			// data =
			if (unit == 1) {
				data = (byte) (((data & 0xff) * 18 + 320) / 10);
			}
		}
		return data;
	}
	
	public void parseACInfo(byte[] data)
	{
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0xe0) );	
		
	
		
		airData[1] = (byte) ((data[3] & 0xff));	

		

		airData[2] = (byte) (data[4] & 0xff);
		airData[3] = (byte) (data[5] & 0xff);

		airData[5] |= (byte) (data[6] & 0x01);
		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time, int total_time){

	}
	public void setMediaSrc(int source, byte type, byte []b){

	}

	public void setMediaSrc(int source) {// default is simple box
	}
	

	public void setVolume(int volume) {
		
	}



}
