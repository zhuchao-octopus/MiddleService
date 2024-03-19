package com.my.cartype.luzheng;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class HondaHaoZheng extends Canbox{

	public HondaHaoZheng(){
		buildCmdDoor((byte) 0x8, (byte) 0x2, (byte) 0xfc, (byte) 0x02);
		buildCmdRadarFront((byte) 0x63, (byte) 0x0, (byte) 0x4);
		buildCmdRadarBack((byte) 0x63, (byte) 0x0, (byte) 0x4);
		buildCmdRadarBackEx((byte) 5);
		buildCmdRadarFrontEx((byte) 1);
		buildCmdAngle((byte) 0x64, (byte) 0x0, 0x1200);
		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
		buildCmdOutTemp((byte) 0x62, (byte) 0x10);
		buildCmdVersion((byte) 0x71, (byte) 0x0);
		mIdAC = 0x62;
		mIdKey = 0x6;
		MAP_KEYS = KEYS_WHEEL;	
	}
	
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.MODLE },
		{ 0x4, MyCmd.Keycode.MUTE },
		{ 0x5, MyCmd.Keycode.NEXT },
		{ 0x6, MyCmd.Keycode.PREVIOUS },
		{ 0x7, MyCmd.Keycode.SPEECH },
		{ 0x8, MyCmd.Keycode.BT },
	};

//	private void parseRadar(byte[] data) {
//
//		mRadar[3] = radarChangeStyle(data[3], 4, 0);
//
//		mRadar[4] = radarChangeStyle(data[4], 4, 0);
//		mRadar[5] = radarChangeStyle(data[5], 4, 0);
//		mRadar[6] = radarChangeStyle(data[6], 4, 0);
//
//		mRadar[0] = radarChangeStyle(data[7], 4, 0);
//
//		mRadar[1] = radarChangeStyle(data[8], 4, 0);
//		mRadar[2] = radarChangeStyle(data[9], 4, 0);
//		mRadar[3] = radarChangeStyle(data[10], 4, 0);
//		parseRadar();
//	}
	
//	@Override
//	public void parseCanboxData(byte[] data, int len) {
//		// TODO Auto-generated method stub
//		switch(data[0]){
//		case 0x63:
//			parseRadar(data);
//			break;
//		default:
//			super.parseCanboxData(data, len);	
//		}
//	}
	@Override
	public int getAngleValue(byte[] data) {

		int angle = (short) -((data[3] & 0xff) | (((data[2] & 0xff)) << 8));

		angle = ((angle * 3000) / 0x1200);
		return angle;
		
		
	}
	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 0){
			data = (byte)0xfa;
		} else if ((data&0xff) == 0xff){

		} else if ((data&0xff) == 0xfe){
			data = 0;
		} else {
			//data = 
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0xc0) 
				| ((data[2] & 0x20) >> 1)
				| ((data[2] & 0x0c) >> 2));	
		
		if (((data[2] & 0x01) == 0)){
			airData[0] |= 0x20;
		}
		
		airData[1] = (byte) ((data[3] & 0xef));	
		airData[1] |= (byte) (data[4] & 0x0f);
		

		airData[2] = (byte) (data[5] & 0xff);
		airData[3] = (byte) (data[6] & 0xff);



		airData[5] = (byte)((data[10] & 0x1));

		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time, int total_time){
		byte min = (byte)((time/60)%60);
		byte sec = (byte)((time)%60);
//		++play;
		byte []data ;
		
		if (MyCmd.SOURCE_DVD != source) {
			data = new byte[] { (byte) 0xc3, 0x6, (byte) (total & 0xFF),
					(byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF),
					(byte) ((play >> 8) & 0xFF), min, sec };
		} else {
			data = new byte[] { (byte) 0xc3, 0x6, (byte) (1 & 0xFF),
					(byte) ((play ) & 0xFF), (byte) (total & 0xFF),
					(byte) ((0 ) & 0xFF), min, sec };
		}
		sendDataToCanbox(data, data.length);
	}
	public void setMediaSrc(int source, byte type, byte []b){
		setMediaSrc(0);
		if (b[0] != 0x10){
			b[0] += 1;
		}
		byte []data = new byte[]{(byte)0xc2, 0x4, b[0],b[1],b[2], 0};
		sendDataToCanbox(data, data.length);
	}

	public void setMediaSrc(int source) {// default is simple box
		byte s = 0;
		byte mediaType = 0;
		switch (source) {
		case 0:
			s = 1;
			mediaType = 1;
			break;
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
			s = 0x09;
			mediaType = 0x11;
			break;
		case MyCmd.SOURCE_AUX:
			s = 0x07;
			mediaType = 0x30;
			break;
		case MyCmd.SOURCE_DTV:
			s = 0x03;
			mediaType = 0x22;
			break;
		case MyCmd.SOURCE_BT:
			s = 0x0b;
			mediaType = 0x30;
			break;
		default:
			s = 0x00;
			mediaType = 0x0;
			break;
		}
		byte[] data;

		data = new byte[] { (byte) 0xc0, 0x2, s, mediaType };

		sendDataToCanbox(data, data.length);
	}
	

	public void setVolume(int volume) {
		
		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
		sendDataToCanbox(data, data.length);

	}

	
	public int getOutTemp(byte[] data) {//
		short t = (short) ((data[9] & 0xff) | ((data[8] & 0xff) << 8));
		t = (short)(t*10/2);
		return t;
	}

}
