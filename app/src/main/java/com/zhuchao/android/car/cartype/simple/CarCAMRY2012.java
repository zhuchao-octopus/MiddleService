package com.zhuchao.android.car.cartype.simple;

import android.os.Handler;

import com.zhuchao.android.car.canbox.Canbox;

public class CarCAMRY2012 extends Canbox{

	private void parseWheelKey(byte[] data, int len)
	{
		if (doKeyStudy(data[2], data[3])){
			return;
		}
		switch(data[2])
		{
		case 0x00:
		{
			doKey(0,0);
		}break;
		case 0x01:
		{
			//vol+
		}break;
		case 0x02:
		{
			//vol-
		}break;
		case 0x03:
		{
			doKey(KEY_NEXTSONG,data[3]);
		}break;
		case 0x04:
		{
			doKey(KEY_PREVIOUSSONG,data[3]);
		}break;
		case 0x07:        //src
		{
			doKey(KEY_SOURCE,data[3]);
		}break;
		case 0x08:         //speech
		{
			
		}break;
		case 0x09:            //pickup
		{
			
		}break;
		case 0x0a:          //hangup
		{
			
		}break;
		case 0x13:          //speech hold
		{
			doKey(KEY_UP, data[3]);
		}break;
		case 0x14:        //pickup hold
		{
			doKey(KEY_DOWN, data[3]);
		}break;
		case 0x15:        //hangup hold
		{
			doKey(KEY_BACK, data[3]);
		}break;
		case 0x16:
		{
			doKey(KEY_ENTER, data[3]);
		}break;
		}
	}
	private void parseACInfo(byte[] data, int len)
	{
		if (data[4] >= 0x1f){
			data[4] = (byte)0xff;
		} else if(data[4] > 0){
			data[4] = (byte)((17.5f + (0.5f * data[4]))*2);
		}
		if (data[5] >= 0x1f){
			data[5] = (byte)0xff;
		} else if(data[5] > 0){
			data[5] = (byte)((17.5f + (0.5f * data[5]))*2);
		}
		
		byte[]	airData = new byte[8];
		airData[0] = (byte) (data[2] & 0xff);
		airData[1] = (byte) (data[3] & 0xff);
		airData[2] = (byte) (data[4] & 0xff);
		airData[3] = (byte) (data[5] & 0xff);
		airData[4] = (byte) (data[6] & 0xff);
		Handler handler = getHandler("CanService");
		if(null != handler)
		{
			handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
		}
	}
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch(data[0])
		{
		case 0x20:
		{
			parseWheelKey(data,len);
		}break;
		case 0x28:
		{
			parseACInfo(data, len);
		}break;
		}
	}

}
