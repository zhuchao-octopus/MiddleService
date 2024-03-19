package com.my.cartype.ods;

import com.common.util.MyCmd;
import com.my.canbox.Canbox;

public class OpelOD extends Canbox {

	public OpelOD() {

		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		
		buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
//		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
//		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
//		buildCmdAngle((byte) 0x29, (byte) 0x5, 0x1518);

//		buildCmdOutTemp((byte) 0x3, (byte) 0x10);
		
		mIdAC = 0x11;

		mIdKey = 0x21;
		MAP_KEYS = KEYS_WHEEL;

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = {(byte)0xc0,0x38,0x39};

	
	private final static byte KEYS_WHEEL[][] = {

		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.MODLE },

		{ 0x9, MyCmd.Keycode.BT_DIAL },		
		
		{ 0xa, MyCmd.Keycode.BT_HANG }, 
		{ 0xb, MyCmd.Keycode.PREVIOUS}, 
		{ 0xc, MyCmd.Keycode.NEXT },
		{ 0x12, MyCmd.Keycode.SPEECH },


	};

	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data&0xff) == 0){
			
		} else if ((data&0xff) == 30){
			data = (byte)0xff;
		} else {
			data = (byte)(34 + (data));
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) (((data[2] & 0x48))
				| ((data[2] & 0x10) << 1)
				| ((data[2] & 0x02) >> 1));	
		
		
		switch (data[3]) {
		case 1:
			airData[1] = (byte) 0x40;
			break;
		case 2:
			airData[1] = (byte) 0x60;
			break;
		case 3:
			airData[1] = (byte) 0x20;
			break;
		case 4:
			airData[1] = (byte) 0xa0;
			break;
		case 5:
			airData[1] = (byte) 0x80;
			break;
		}
		airData[1] |= (byte) ((data[4] & 0x0f));	
		
		

		airData[2] = (byte) (data[5] & 0xff);
		airData[3] = (byte) (data[6] & 0xff);
		
		if (data[4]>0) {
			airData[0] |= 0x80;
		}
		

		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	



	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}



	public void setMediaSrc(int source, byte type, byte[] b) {

	}

	public void setMediaSrc(int source) {

	}
}
