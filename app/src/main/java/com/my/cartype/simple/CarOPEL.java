package com.my.cartype.simple;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.my.canbox.AirConditionPanel;
import com.my.canbox.Canbox;
import com.my.canbox.DoorStatusPanel;


public class CarOPEL extends Canbox{

	public CarOPEL() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x01, 0x2,
				0x3, 0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x02, 0x1,
				0x1, 0x1, 0x0 });
		
		
		
	}

	private void parseWheelKey(byte[] data, int len) {
		if (doKeyStudy(data[5], (data[5] == 0) ? 0 : 1)) {
			return;
		}	
		
		if (data[5] == 0x8 || data[5] == 0x9 || data[5] == 0xa) {
			int which = SystemConfig.getIntProperty(mContext,
					MachineConfig.VALUE_CANBOX_OPEL);
			if (which != 0){
				return;
			}
		}
		switch (data[5]) {
		case 0x0:
			doKey(0, 0);
			break;
		case 0x1:
			doKey(AK_KEYPAD_VOLUME_A, 1); // vol+
			break;
		case 0x2:
			doKey(AK_KEYPAD_VOLUME_D, 1);// vol-
			break;
		case 0x5:
			doKey(KEY_PREVIOUSSONG, 1);
			break;
		case 0x6:
			doKey(KEY_NEXTSONG, 1);
			break;
		case 0x3:
			doKey(KEY_BT_DIAL, 1);
			break;
		
		case 0x4:
			doKey(AK_KEYPAD_MUTE_FAKE, 1);// mute
			break;
		case 0x7:
			doKey(KEY_MODE, 1);
			break;

		case 0x8:
			doKey(MyCmd.Keycode.KEY_TURN_A, 1);
			break;
		case 0x9:
			doKey(MyCmd.Keycode.KEY_TURN_D, 1);
			break;
		case 0xa:
			doKey(KEY_HOMEPAGE, 1);
			break;
		}
	}
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case (byte) 0xfd: {
			switch (data[2]) {
			case 0x0:
				parseWheelKey(data, len);
				break;
			
			}
		}
			break;
		}
	}

	private long mSendInfoTime = 0;
	private final static int REPEAT_TIME = 500;
	private int mSendIndex = 0;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {			
			super.handleMessage(msg);
//			Log.d("allen3", "!!!!!!!!");
			if ((mSendIndex % 2) == 0) {
				startConnect();
				mSendIndex = (mSendIndex+1)%2;
			} else {
				if (data != null
						&& (mSendInfoTime != 0 && ((System.currentTimeMillis() - mSendInfoTime) >= REPEAT_TIME))) {
					sendDataToCanbox(data, (byte) data.length);
					mSendIndex = (mSendIndex+1)%2;
				}
			}
			
			mHandler.removeMessages(0);
			if (!mStop) {
				mHandler.sendEmptyMessageDelayed(0, REPEAT_TIME/2);
			}
		}
	};
	
	private byte mDoorStatus = 0;
	private boolean mStop = false;
	public void startConnect(){//default is simple box
		byte []data = new byte[]{0x05, 0x04, 0x0};
		sendDataToCanbox(data, (byte)data.length);


		mHandler.removeMessages(0);		
		mHandler.sendEmptyMessageDelayed(0, 300);
	}
	public void stopConnect(){//default is simple box
		mStop = true;
		mHandler.removeMessages(0);
		byte []data = new byte[]{0x05, 0x04, 0x1};
		sendDataToCanbox(data, (byte)data.length);
	}
	
	private int Sum(byte []data, int len){
		int sum = 0;
		
		for(int i = 0; i < len; ++i){
			sum += (data[i]&0xff);
		}
		return (int)(sum&0xFFFF);
	}

	public void sendDataToCanbox(byte[] data, int len) { 	

		byte[] send = new byte[len + 4];
		send[0] = (byte)(len+3);
		send[1] = (byte)0xfd;
		send[len+2] = (byte)((Sum(data, len) >> 8) & 0xFF);
		send[len+3] = (byte)((Sum(data, len) >> 0) & 0xFF);
		byteArrayCopy(send, data, 2, 0, len);
		sendCommonDataToCanbox(send);
	}
	byte []data;
	public void setMediaMoreInfo(int source, int play, int total, int time, int total_time){
		
		data = new byte[]{0x10, 0x00, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
				0x20, 0x20, 0x20, 0x20};

		
		int seconds = time % 60;
		int minutes = (time / 60) % 60;
		int hours = time / 3600;
		switch (source){
		case MyCmd.SOURCE_DVD:{
			data[2] = 'D';
			data[3] = 'V';
			data[4] = 'D';
			
			data[6] = (byte)('0' + (hours%10));
			data[7] = ':';
			data[8] = (byte)('0' + (minutes/10));
			data[9] = (byte)('0' + (minutes%10));
			data[10] = ':';
			data[11] = (byte)('0' + (seconds/10));
			data[12] = (byte)('0' + (seconds%10));
			
			
			break;
		}case MyCmd.SOURCE_MUSIC:{
			data[2] = 'M';
			data[3] = 'U';
			data[4] = 'S';
			data[5] = 'I';
			data[6] = 'C';
			
//			if ((play/100) != 0){
//				data[8] = (byte)('0'+(play/100));
//			}
//			if ((play/10) != 0){
//				data[9] = (byte)('0'+((play%100)/10));
//			}
//			data[10] = (byte)('0'+(play%10));
			
			data[8] = (byte)('0' + (minutes/10));
			data[9] = (byte)('0' + (minutes%10));
			data[10] = ':';
			data[11] = (byte)('0' + (seconds/10));
			data[12] = (byte)('0' + (seconds%10));
			
			break;
		}case MyCmd.SOURCE_VIDEO:{
			data[2] = 'V';
			data[3] = 'D';
			data[4] = 'O';
			
			data[6] = (byte)('0' + (hours%10));
			data[7] = ':';
			data[8] = (byte)('0' + (minutes/10));
			data[9] = (byte)('0' + (minutes%10));
			data[10] = ':';
			data[11] = (byte)('0' + (seconds/10));
			data[12] = (byte)('0' + (seconds%10));
			break;
		}case MyCmd.SOURCE_IPOD:{
			data[2] = 'i';
			data[3] = 'P';
			data[4] = 'o';
			data[5] = 'd';
			break;
		}		
		}
		
		mSendInfoTime = System.currentTimeMillis();
		sendDataToCanbox(data, (byte)data.length);
//		mHandler.removeMessages(1);
//		if (!mStop) {
//			mHandler.sendEmptyMessageDelayed(1, 300);
//		}
	}
	public void setMediaSrc(int source){//default is simple box
		data = new byte[]{0x10, 0x00, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
				0x20, 0x20, 0x20, 0x20};
		

		switch (source){
		case MyCmd.SOURCE_DVD:{
			data[2] = 'D';
			data[3] = 'V';
			data[4] = 'D';
			break;
		}case MyCmd.SOURCE_MUSIC:{
			data[2] = 'M';
			data[3] = 'U';
			data[4] = 'S';
			data[5] = 'I';
			data[6] = 'C';
			break;
		}case MyCmd.SOURCE_VIDEO:{
			data[2] = 'V';
			data[3] = 'I';
			data[4] = 'D';
			data[5] = 'E';
			data[6] = 'O';
			break;
		}case MyCmd.SOURCE_IPOD:{
			data[2] = 'i';
			data[3] = 'P';
			data[4] = 'o';
			data[5] = 'd';
			break;
		}case MyCmd.SOURCE_DTV:{
			data[2] = 'D';
			data[3] = 'T';
			data[4] = 'V';
			break;
		}
		case MyCmd.SOURCE_AUX:{
			data[2] = 'A';
			data[3] = 'U';
			data[4] = 'X';
			break;
		}
		case MyCmd.SOURCE_BT:{
			data[2] = 'A';
			data[3] = '2';
			data[4] = 'D';
			data[4] = 'P';
			break;
		}
		}
		mSendInfoTime = System.currentTimeMillis();
		sendDataToCanbox(data, (byte)data.length);
//		mHandler.removeMessages(1);
//		if (!mStop) {
//			mHandler.sendEmptyMessageDelayed(1, 300);
//		}
	}

	
	public void setMediaSrcASC(byte []b){
		
	}
	public void setMediaSrc(int source, byte type, byte []b){//default is simple box
		data = null;
		
		int freq = (int)((b[1] & 0xff) | ((b[2] & 0xff) << 8));
//		Log.e("", ""+freq+":"+b[1]+":"+b[2]+":");
		switch (source){
		case MyCmd.SOURCE_RADIO:{
			data = new byte[]{0x10, 0x00, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
					0x20, 0x20, 0x20, 0x20};
			
			if (b[0] == 16){ //am
				data[2] = 'A';
				data[3] = 'M';
				data[4] = 0x20;
				
				data[6] = (byte) ('0' + (freq / 1000));
				if (data[6] == '0'){
					data[6] = 0x20;
				}
			
				data[7] = (byte) ('0' + ((freq % 1000)/ 100));
				data[8] = (byte) ('0' + ((freq % 100) / 10));
				data[9] = (byte) ('0' + ((freq % 10) / 1));
				
				data[11] = 'K';
				data[12] = 'H';
				data[13] = 'z';
			} else {
				data[2] = 'F';
				data[3] = 'M';
				if (b[0] == 0) {					
					data[4] = '1';
				} else if (b[0] == 1) {
					data[4] = '2';
				} else if (b[0] == 2) {
					data[4] = '3';
				}
				if ((freq / 10000) != 0) {
					data[6] = (byte) ('0' + (freq / 10000));
				}
				data[7] = (byte) ('0' + ((freq % 10000)/ 1000));
				data[8] = (byte) ('0' + ((freq % 1000) / 100));
				data[9] = 0x2e;
				data[10] = (byte) ('0' + ((freq % 100) / 10));
				data[11] = 'M';
				data[12] = 'H';
				data[13] = 'z';
			}
			
			
		}
			break;
		}
////		byte []data = new byte[]{0xf, 0x07, 0x46, 0x4d, 0x31, 0x20, 0x31, 0x30, 0x35, 0x2e,
////				0x32, 0x4d, 0x48,
////				0x7a};
		if (data != null){
			mSendInfoTime = System.currentTimeMillis();
			sendDataToCanbox(data, (byte)data.length);
		}
//		mHandler.removeMessages(1);
//		if (!mStop) {
//			mHandler.sendEmptyMessageDelayed(1, 300);
//		}
	}
	
	/*	public void setVolume(int vol){
	byte []data = new byte[]{0x10, 0x00, 'V', 'O', 'L', ':', 0x20, 0x20, 0x20, 0x20,
			0x20, 0x20, 0x20, 0x20};

	data[6] = (byte)(0x30+((vol/10)%10));
	data[7] = (byte)(0x30+((vol)%10));
	
	sendDataToCanbox(data, (byte)data.length);
}*/
}
