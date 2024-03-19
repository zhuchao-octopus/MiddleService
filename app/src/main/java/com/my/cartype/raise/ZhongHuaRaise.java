package com.my.cartype.raise;

import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class ZhongHuaRaise extends Canbox{

	public ZhongHuaRaise(){
		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x6, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
//		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
//		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
//		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdAC = 0x3;
		mIdKey = 0x2;
		MAP_KEYS = KEYS_WHEEL;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = { 0x4	};
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x4, MyCmd.Keycode.PREVIOUS },
		{ 0x5, MyCmd.Keycode.MODLE },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.BT_DIAL },
		{ 0x8, MyCmd.Keycode.BT_HANG },
		{ 0x9, MyCmd.Keycode.SPEECH },
		{ 0xa, MyCmd.Keycode.BT },
		{ 0x10, MyCmd.Keycode.HOME },
		{ 0x11, MyCmd.Keycode.AUDIO },
		{ 0x12, MyCmd.Keycode.MULT_MUTE_AND_POWER },
		{ 0x13, MyCmd.Keycode.RADIO },
		{ 0x14, MyCmd.Keycode.BT },
		{ 0x15, MyCmd.Keycode.VOLUME_ROLL_UP },
		{ 0x16, MyCmd.Keycode.VOLUME_ROLL_UP },
		};
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0x85, 0x01, 0 };
		switch(CarUtil.getModelId()){
		case 0:
			cmd [2] = 1;
			break;
		default:
			return null;
		}
		return cmd;
	}
	@Override
	public int getAngleValue(byte[] data) {

		int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

		
		return angle;
		
		
	}

	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0) {
			data = (byte) 0x0;
		} else if ((data & 0xff) == 0xe) {
			data = (byte) 0xff;
		} else if (((data & 0xff) >= 1) && ((data & 0xff) <= 0xd)) {
			data = (byte) (32 + (data & 0xff) * 2);
		} else if (((data & 0xff) >= 20) && ((data & 0xff) <= 0x40)) {
			data = (byte) (32 + ((data & 0xff) - 0x20));
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{

		
		byte[]	airData = new byte[8];
		
		airData[0] = (byte) ((data[2] & 0xf3) 
				| ((data[2] & 0x08) >> 1 ));				
		
		airData[1] = data[3];
				

		airData[2] = data[4];
		airData[3] = data[5];
		

		airData[4] = (byte) (((data[7] & 0x80) >> 5 ));	

		airData[5] |= 0x80;
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}
	public void setMediaSrc(int source, byte type, byte []b){
	}

	public void setMediaSrc(int source) {
		
	}	

	


	public void sendId3(byte index, String num) {

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

			int num_len = n.length;
			
			if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
				num_len -= 2;
			}
			
			if (num_len > 43){
				num_len = 43;
			}
			byte[] data;
			if (num_len == 0) {
				data = new byte[4];

				data[0] = (byte) index;
				data[1] = 2;
				data[2] = 0x2;
				data[3] = 0;
			} else {
				
				int len = num_len + 3;

				data = new byte[len];

				data[0] = (byte) index;
				data[1] = (byte) (num_len + 1);
				data[2] = 0x2;
				for (int i = 0; i < num_len && i < (data[1]); ++i) {
					data[3 + i] = n[i+2];					
					
				}
			}
			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

			Log.d("Nissan2013Simple", "sendId3" + e);
		}
	}

	String mName = null;
	String mArtist = null;
	String mAlbum = null;



	public void setSongName(String s) {
		sendId3((byte) 0x70, s);
		mName = s;
	}

	public void setSongAritst(String s) {
		sendId3((byte) 0x71, s);
		mArtist = s;
	}

	public void setSongAlbum(String s) {
		sendId3((byte) 0x72, s);
		mAlbum = s;
	}

}
