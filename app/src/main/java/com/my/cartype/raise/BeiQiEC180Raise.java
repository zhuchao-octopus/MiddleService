package com.my.cartype.raise;

import java.util.Date;

import android.provider.Settings;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class BeiQiEC180Raise extends Canbox{

	public BeiQiEC180Raise(){
		mSupportRaise0x7d = true;
		mIdAC = 0x23;
		buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdRadarFront((byte) 0x28, (byte) 0x0, (byte) 0x4);
		buildCmdRadarBack((byte) 0x27, (byte) 0x0, (byte) 0x4);
//		buildCmdAngle((byte) 0x7d, (byte) 0x4, 540);
//		buildCmdOutTemp((byte) 0x21, (byte) 0x0);
		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdKey = 0x21;
		MAP_KEYS = KEYS_WHEEL;			

		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}
	
	private final static byte IDS_TO_CANBOXSETTING[] = {0x37, 0x39, 0x40, 0x41, 0x42, 0x43	};

	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0xee, 0x02, 1, 0 };
		if (CarUtil.getManaId() == 13) { //od
			switch (CarUtil.getModelId()) {
			case 31:
				if (CarUtil.getCarTypeConfig() == 0){
					cmd[3] = 1;
				} else {
					cmd[3] = 2;
				}
				break;
			case 32:
				if (CarUtil.getCarTypeConfig() == 0){
					cmd[3] = 3;
				} else {
					cmd[3] = 4;
				}
				break;
			case 33:
				cmd[3] = 5;
				break;
			default:
				cmd[3] = 0x6;	
				break;
			}
		} else {
			switch (CarUtil.getModelId()) {
			case 28:
				cmd[3] = 1;
				break;
			default:
				return null;
			}
		}
		return cmd;
	}
	
	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, 
			{ 0x3, KEY_NEXTSONG },
			{ 0x4, KEY_PREVIOUSSONG }, 
			{ 0x5, MyCmd.Keycode.BT_DIAL },
			{ 0x6, MyCmd.Keycode.MULT_MUTE_AND_HANG },
			{ 0x7, KEY_SOURCE },

			{ 0x8, MyCmd.Keycode.POWER },
			{ 0x9, MyCmd.Keycode.MUTE },
			{ 0xa, MyCmd.Keycode.HOME },
			{ 0xb, MyCmd.Keycode.NAVIGATION },
			{ 0xc, MyCmd.Keycode.RADIO },
			{ 0xd, MyCmd.Keycode.AUDIO },
			{ 0xe, MyCmd.Keycode.KEY_SEEK_NEXT },
			{ 0xf, MyCmd.Keycode.KEY_SEEK_PREV },
//			{ 0x14, MyCmd.Keycode.BT },
//			{ 0x15, MyCmd.Keycode },
			{ 0x16, MyCmd.Keycode.SPEECH },
//			{ 0x17, MyCmd.Keycode },
			
//this for od pro
			{ 0x10, MyCmd.Keycode.BACK },
			{ 0x11, MyCmd.Keycode.KEY_AIR_CONTROL },
	};	
	

	
	private int getAcType() {

		if (CarUtil.getManaId() == 13) { // od

		} else {
			if (CarUtil.getModelId() == 26) {
				return 2;
			} else if (CarUtil.getModelId() == 28) {
				return 1;
			}
		}
		return 0;
	}	


	
	public int getACTemp(byte data) {//
		if (data >= 1 && data <= 0xf) {
			if (getAcType() == 0) {
				data = (byte) (34 + (data) * 2);
			} else if (getAcType() == 1) {
				data = (byte) (32 + (data ) * 2);
			}
		}
		return data & 0xff;
	}

	byte[]	airData = new byte[12];
	public void parseACInfo(byte[] data)
	{	
		
		
		airData[0] = (byte) ((data[2] & 0xf0)
				| ((data[2] & 0x08) >> 3));		
		
		switch ((data[3] & 0xff)) {
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
			airData[1] = (byte) (0x80);
			break;
		}
		
		airData[1] |= (byte) (data[4] & 0x0f);
		
		
				
		airData[5] = (byte) ((data[2] & 0x01) << 4);
		
		
//		airData[2] = (byte) getACTemp(data[4], airData[5] & 0x1);
		airData[3] = (byte) 0xfa;

		airData[2] = data[5];
		if (getAcType() == 2) {
			airData[7] |= 0x40;
		}
		airData[5] |= 0x80;
		

		airData[9] = data[6];
		airData[10] = data[7];
		airData[11] = data[8];
		super.parseACInfo(airData);
	}
	
	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		
	}

	public void setMediaSrc(int source, byte type, byte[] b) {

		byte[] data = new byte[] { (byte) 0xc0, 0x5, 1, 1, b[0], b[1], b[2] };
		sendDataToCanbox(data, data.length);
	}

	public void setMediaSrc(int source){//default is simple box
		byte s = 0;
		byte mediaType = 0;
		switch (source){
		case 0:
			return;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0x08;
			mediaType = (byte)0xff;
			break;
		case MyCmd.SOURCE_AUX:
			s = 0x0c;
			mediaType = 0x30;
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
		byte []data;		
		data = new byte[]{(byte)0xc0, 0x2, s, mediaType};		
		
		sendDataToCanbox(data, data.length);
	}
	
	public int getUpdateTime() {
		return 60000;
	}
	
	public void updateTime() {
		if (mContext == null) {
			return;
		}
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);
		byte format = 0;
		if ("12".equals(strTimeFormat)) {
			format = 1;
		}

		byte m = (byte) curDate.getMinutes();

		byte y = (byte) (curDate.getYear() - 100);
		byte mon = (byte) (curDate.getMonth() + 1);
		byte d = (byte) curDate.getDate();

		byte[] buf = new byte[] { (byte) 0x84, 0x06, y, mon, d, h, m, format };
		sendDataToCanbox(buf, buf.length);
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
				data[2] = 0x10;
				data[3] = 0;
			} else {
				
				int len = num_len + 3;

				data = new byte[len];

				data[0] = (byte) index;
				data[1] = (byte) (num_len + 1);
				data[2] = 0x10;
				for (int i = 0; i < num_len && i < (data[1]); ++i) {
					data[3 + i] = n[i + 2];		
				}
			}
			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

			Log.d("Nissan2013Simple", "sendId3" + e);
		}
	}




	public void setSongName(String s) {
		sendId3((byte) 0x70, s);
	
	}

	public void setSongAritst(String s) {
		sendId3((byte) 0x71, s);
	}

	public void setSongAlbum(String s) {
		sendId3((byte) 0x72, s);
	}
}
