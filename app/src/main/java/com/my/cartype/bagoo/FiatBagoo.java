package com.my.cartype.bagoo;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.my.GlobalDef;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.manager.McuManager;
import com.my.out.R;


public class FiatBagoo extends Canbox{

	public FiatBagoo(){

		buildCmdDoor((byte) 0x14, (byte) 0x2, (byte) 0xfc, (byte) 0x02);
		buildCmdVersion((byte) 0x71, (byte) 0x0);
		mIdKey = 0x11;
		MAP_KEYS = KEYS_WHEEL;
	}
	
	private final static byte KEYS_WHEEL[][] = {

		{ 0x3, MyCmd.Keycode.MODLE },
		{ 0x4, MyCmd.Keycode.VOLUME_UP },
		{ 0x5, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x8, MyCmd.Keycode.PREVIOUS },
		{ 0x9, MyCmd.Keycode.NEXT },
		{ 0xa, MyCmd.Keycode.NAVIGATION },

	};

	
	public void setMediaMoreInfo(int source, int play, int total, int time, int total_time){
++play;
		mData = new byte[] { (byte) 0x93, 0x6, 4, 0, (byte)(play&0xff), 2, 0,0 };

		sendDataToCanbox(mData, mData.length);
	}
	byte[] mData = null;
	public void setMediaSrc(int source, byte type, byte []b){

		int freq = (( b[2]&0xff) << 8) | (b[1]&0xff);
		if (b[0] < 0x10) {
			b[0] = 0;

			freq = freq/10;
		} else {
			b[0] = 6;
			
		}
//		
//		
		mData = new byte[] { (byte) 0x93, 0x6, 1,  b[0], 0, 0, (byte)((freq&0xff00) >> 8), (byte)(freq&0xff) };
//		byte[] mData = new byte[] { (byte) 0x93, 0x6, 1,  b[0], 0, 0, b[2], b[1] };
		sendDataToCanbox(mData, mData.length);
	}
	
	public void repeatSendLcdMsg(){
		if (mData != null){
			sendDataToCanbox(mData, mData.length);
		} 
		super.repeatSendLcdMsg();
	}
	
	@Override
	public void stopConnect() {
		// TODO Auto-generated method stub
		startRepeatSendLcdMsg(false);
		super.stopConnect();
	}
	@Override
	public void startConnect() {
		// TODO Auto-generated method stub
		super.startConnect();
		startRepeatSendLcdMsg(true);
	}
	
	public void setMediaSrc(int source){//default is simple box
		byte s = 0xe;

		switch (source) {
		case MyCmd.SOURCE_RADIO:
			return;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 5;
			break;
		case MyCmd.SOURCE_AUX:
			s = 0x03;
			break;
		case MyCmd.SOURCE_DVD:
			s = 0x02;
			break;
		case MyCmd.SOURCE_MX51:
		case MyCmd.SOURCE_AV_OFF:
			s = 0x0;
			break;
		}

		mData = new byte[] { (byte) 0x93, 0x6, s, 0, 0, 0, 0,0 };

		sendDataToCanbox(mData, mData.length);
	}
}
