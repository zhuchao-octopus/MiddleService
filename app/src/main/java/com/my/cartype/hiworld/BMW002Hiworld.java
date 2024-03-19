package com.my.cartype.hiworld;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.provider.Settings;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;

public class BMW002Hiworld extends Canbox {

	public BMW002Hiworld() {
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xc0, (byte) 0x04);
		buildCmdAngle((byte) 0x81, (byte) 0x0, 0xfe);
		buildCmdRadarBack((byte) 0x81, (byte) 0x0, (byte) 0xfe);
		buildCmdRadarFront((byte) 0x81, (byte) 0x0, (byte) 0xfe);
		buildCmdRadarFrontEx((byte) 10);
		buildCmdRadarBackEx((byte) 6);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);

		buildCmdKey((byte) 0x81, (byte) 5, (byte) 4, (byte) 0, KEYS_WHEEL);
		buildCmdKey((byte) 0x83, (byte) 6, (byte) 2, (byte) 2, KEYS_WHEEL2);
		
		if (CarUtil.getModelId()==5){

			buildCmdKey((byte) 0x11, (byte) 5, (byte) 4, (byte) 3, KEYS_WHEEL_MINI);
		} else {

			buildCmdKey((byte) 0x84, (byte) 5, (byte) 2, (byte) 3, KEYS_WHEEL3);
		}

	}


	@Override
	public void stopConnect() {

	}
	private final static byte KEYS_WHEEL[][] = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN }, 
			
			{ 0x3, MyCmd.Keycode.MUTE },
			{ 0x4, MyCmd.Keycode.SPEECH },

			{ 0x5, MyCmd.Keycode.BT }, 

			{ 0x8, MyCmd.Keycode.PREVIOUS },
			{ 0x9, MyCmd.Keycode.NEXT },

			{ 0xa, MyCmd.Keycode.MODLE },

	};
	
	private final static byte KEYS_WHEEL2[][] = {
		{ 0x1, MyCmd.Keycode.AUDIO },
		{ 0x2, MyCmd.Keycode.HOME }, 
		
		{ 0x3, MyCmd.Keycode.BT },
		{ 0x4, MyCmd.Keycode.BACK },

		{ 0x5, MyCmd.Keycode.MODLE }, 

		{ 0x6, MyCmd.Keycode.PREVIOUS },

		{ 0x7, MyCmd.Keycode.NEXT },
		{ 0x8, MyCmd.Keycode.PLAY_PAUSE },

};
	
	private final static byte KEYS_WHEEL3[][] = {
		{ 0x1, MyCmd.Keycode.RADIO },
		{ 0x6, MyCmd.Keycode.AUDIO },
		{ 0xc, MyCmd.Keycode.AUX_IN },
		{ 0xa, MyCmd.Keycode.BT },
		{ 0xd, MyCmd.Keycode.AUDIO },

};
	
	private final static byte KEYS_WHEEL_MINI[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN }, 

		{ 0x5, MyCmd.Keycode.BT },

		{ 0x9, MyCmd.Keycode.PREVIOUS }, 
		{ 0x8, MyCmd.Keycode.NEXT },

		{ 0x18, MyCmd.Keycode.SPEECH },

};

	@Override
	public int getAngleValue2(byte[] data) {
		int angle = 0;
		if ((data[7] & 0xff) > 0 && (data[7] & 0xff) < 0xff)
		{
			angle = -(data[7] & 0xff);
		}
		else if ((data[6] & 0xff) > 0 && (data[6] & 0xff) < 0xff)
		{
			angle = (data[6] & 0xff);
		}
		
		return angle;
	}

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

	}

	public void setMediaSrc(int source, byte type, byte[] b) {

	}

	public void setMediaSrc(int source) {// default is simple box
		byte s = 0;
		switch (source) {
		case MyCmd.SOURCE_RADIO:
			s = 1;
			break;
		case MyCmd.SOURCE_DVD:
			s = 6;
			break;
		case MyCmd.SOURCE_AV_OFF:
			s = 0x0;
			break;
		default:
			s = 0xc;
			break;
		}
		

		byte[] buf = new byte[] { 0xd, (byte) 0xe1, s, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0 };
		

		sendDataToCanbox(buf, buf.length);
	}

	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword2(data, len);
	}

	public void startConnect() {
		
	}



}
