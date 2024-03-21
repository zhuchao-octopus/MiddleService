package com.zhuchao.android.car.cartype.ods;

import java.util.Calendar;
import java.util.Date;

import android.provider.Settings;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class HummerODS extends Canbox{

	public HummerODS(){
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;		
	}
	
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x4, MyCmd.Keycode.KEY_SEEK_PREV },
		{ 0x3, MyCmd.Keycode.KEY_SEEK_NEXT },
		
		{ 0x5, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },
		{ 0x8, MyCmd.Keycode.SPEECH },
		{ 0x9, MyCmd.Keycode.BT },
	};


	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}
	public void setMediaSrc(int source, byte type, byte []b){
	}

	public void setMediaSrc(int source) {
	}


}
