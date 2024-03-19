package com.my.cartype.xinfeiyang;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.GlobalDef;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.out.R;

public class RenaultXinFeiYang extends Canbox {

	public RenaultXinFeiYang() {

		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdAngle((byte) 0x29, (byte) 0x0, 360);
		buildCmdVersion((byte) 0x30, (byte) 0x0);

		buildCmdRadarBack((byte) 0x1e, (byte) 0x0, (byte) 0x4);
		buildCmdRadarFront((byte) 0x1d, (byte) 0x0, (byte) 0x4);
		mIdAC = 0x55;
		
		mIdKey = 0x20;		
		MAP_KEYS = KEYS_WHEEL;		
	}

	private final static byte KEYS_WHEEL[][] = { 
			{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, 
			{ 0x3, MyCmd.Keycode.ROLL_PREV },
			{ 0x4, MyCmd.Keycode.ROLL_NEXT },


			{ 0x9, KEY_BT },
			{ 0xb, KEY_BT },

			{ 0x6, KEY_MIC }, 

			{ 0x16, KEY_PLAYPAUSE },
			
			{ 0x17, KEY_MUTE }, 
			{(byte)0x88, KEY_SOURCE },


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
	


	private byte[] mData = new byte[] { (byte) 0xc0, 0x8, 0, 0, 0, 0, 0, 0, 0,
			0 };

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		byte h = (byte) ((time / 3600));
		byte min = (byte) ((time / 60) % 60);
		byte sec = (byte) ((time) % 60);
		// ++play;

		byte s = 0;
		byte s2 = 0;
		switch (source) {
		case MyCmd.SOURCE_DVD:
			s = 0x2;
			s2 = 0x10;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			s = 8;
			s2 = 0x11;
			break;
		case MyCmd.SOURCE_BT:
			s = 0xb;
			s2 = 0x10;
			break;
		default:
			s = 0x07;
			s2 = 0x30;
			break;
		}

		if (MyCmd.SOURCE_DVD == source) {
			mData = new byte[] { (byte) 0xc0, 0x8, s, s2, 0,
					(byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec };

		} else {
			mData = new byte[] { (byte) 0xc0, 0x8, s, s2, 0, 0, 
					(byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8),
					 min, sec };
		}

		// if (mPhoneStatus < HFP_INFO_CALLED) {

		sendDataToCanbox(mData, mData.length);
		// }
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		setMediaSrc(0);
		if (b[0] != 0x10) {
			b[0] += 1;
		}
		mData = new byte[] { (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0,
				0, 0 };
		sendDataToCanbox(mData, mData.length);
	}

	public void setMediaSrc(int source) {// default is simple box
		byte s = 0;
		byte mediaType = 0;
		switch (source) {
		case 1:
			s = 2;
			mediaType = 0x10;
			break;
		case MyCmd.SOURCE_IPOD:
			s = 6;
			mediaType = 0x12;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
		case 0:
			return;
		case MyCmd.SOURCE_AUX:
			s = 0x07;
			mediaType = 0x30;
			break;
		case MyCmd.SOURCE_DTV:
			s = 0x0A;
			mediaType = 0x30;
			break;
		case MyCmd.SOURCE_BT:
			s = 0x0b;
			mediaType = 0x40;
			break;
		default:
			s = 0x00;
			mediaType = 0x0;
			break;
		}

		// if (s == 0xb || s == 0x7) {
		// data = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0,
		// 0 };
		// } else {
		mData = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0 };
		// }

		sendDataToCanbox(mData, mData.length);
	}
}
