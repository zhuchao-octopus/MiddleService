package com.zhuchao.android.car.cartype.simple;

import android.os.Handler;
import android.util.Log;

import com.zhuchao.android.car.canbox.Canbox;

public class CarToyota2013Low extends Canbox{
	public CarToyota2013Low(){
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x01, 0x2,
				0x3, 0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x02, 0x0,
				0x0, 0x0, 0x1 });
	}
	
	private void parseWheelKey(byte[] data, int len)
	{
		if (doKeyStudy(data[2], data[3])){
			return;
		}
		switch (data[2]) {
		case 0x0:
			doKey(0, 0);
			break;
		case 0x1:
		case (byte)0x81:
			doKey(AK_KEYPAD_VOLUME_A, data[3]); //vol+
			break;
		case (byte)0x82:
		case 0x2:
			doKey(AK_KEYPAD_VOLUME_D, data[3]);//vol-
			break;
		case (byte)0x85:
		case 0x3:
			doKey(KEY_PREVIOUSSONG, data[3]);
			break;
		case (byte)0x86:
		case 0x4:
			doKey(KEY_NEXTSONG, data[3]);
			break;
//		case 0x5:
//			doKey(KEY_BT, data[3]);
//			break;
//		case 0x6:
//			doKey(AK_KEYPAD_MUTE_FAKE, data[3]);//mute
//			break;
		case 0x7:
		case (byte)0x88:
			doKey(KEY_MODE, data[3]);
			break;	
		case 0xa:   //HANGUP
		{
			doKey(KEY_BT_HANG,data[3]);
		}break;
		case 0x13:        //CH+
		case (byte)0x83:
		{
			doKey(KEY_CH_UP,data[3]);
		}break;
		case 0x14:      //CH-
		case (byte)0x84:
		{
			doKey(KEY_CH_DOWN,data[3]);
		}break;
		case (byte)0x87:
		{
			doKey(AK_KEYPAD_MUTE_FAKE, data[3]);//mute
		}break;
		}
	}
	
	private void parseACInfo(byte[] data, int len)
	{
//		if ((data[6] & 0x1) == 0){
//		if (data[4] >= 0x1f){
//			if (data[4] >= 0x20 && data[4] <= 0x23){
//				data[4] = (byte)((16f + (0.5f * (data[4]-0x20)))*2);
//			}else {
//				data[4] = (byte)0xff;
//			}
//			
//		} else if(data[4] > 0){
//			data[4] = (byte)((17.5f + (0.5f * data[4]))*2);
//		}
//		if (data[5] >= 0x1f){
//			if (data[5] >= 0x20 && data[5] <= 0x23){
//				data[5] = (byte)((16f + (0.5f * (data[5]-0x20)))*2);
//			}else {
//				data[5] = (byte)0xff;
//			}
//		} else if(data[5] > 0){
//			data[5] = (byte)((17.5f + (0.5f * data[5]))*2);
//		}
//		} else {
//			
//		}
//		
//		byte[]	airData = new byte[8];
//		airData[0] = (byte) (data[2] & 0xff);
//		airData[1] = (byte) (data[3] & 0xff);
//		airData[2] = (byte) (data[4] & 0xff);
//		airData[3] = (byte) (data[5] & 0xff);
//		airData[4] = (byte) ((data[6] & 0x08) >> 1);
//		airData[5] = (byte) (data[6] & 0x01);
//		
//	
//		
//		Handler handler = getHandler("CanService");
//		if(null != handler)
//		{
//			handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
//		}
	}
	

	private final byte[] mAcData = new byte[1];
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x20: {
			parseWheelKey(data, len);
		}
			break;
		case 0x28: {
			//mAcData = new byte[len];
//			if (!isBufEqual(data, mAcData)){
//				mAcData = data.clone();
//				parseACInfo(data, len);
//			}
			
		}
			break;
		case 0x1E: // Radar back
		{
//			for (int i = 0; i < 4; ++i){
//				switch (data[2+i]){
//				case 0:
//					mRadar[i] = 0;
//					break;
//				case 1:
//					mRadar[i] = 1;
//					break;
//				case 2:
//					mRadar[i] = 4;
//					break;
//				case 3:
//					mRadar[i] = 6;
//					break;
//				case 4:
//					mRadar[i] = 0xa;
//					break;
//				}
//			}
//			Handler handler = getHandler("Reverse");
//			if (null != handler) {
//				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
//			}
		}
			break;
		}
	}
	private final byte mDoorStatus = 0;
	
//	public void setReverseRadaVol(byte param){
//		byte []data = new byte[]{(byte)0xc6, 0x2, 0x0, param};
//		sendDataToCanbox(data, data.length);
//	}
//	public void setParkCarMode(byte param){
//		byte []data = new byte[]{(byte)0xc6, 0x2, 0x2, param};
//		sendDataToCanbox(data, data.length);
//	}
//	public void requestInfo(byte param){
//		byte []data = new byte[]{(byte)0x90, 0x2, param, 0};
//		sendDataToCanbox(data, data.length);
//	}

}
