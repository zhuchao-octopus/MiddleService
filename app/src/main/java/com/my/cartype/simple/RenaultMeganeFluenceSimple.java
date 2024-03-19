package com.my.cartype.simple;

import android.os.Handler;
import android.util.Log;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.my.GlobalDef;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.out.R;

public class RenaultMeganeFluenceSimple extends Canbox {

	public RenaultMeganeFluenceSimple() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x01, 0x2,
				0x3, 0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x02, 0x1,
				0x1, 0x2, 0x0 });

	}

	private void parseWheelKey(byte[] data, int len) {
		if (doKeyStudy(data[2], data[3])) {
			return;
		}
		switch (data[2]) {
		case 0x0:
			doKey(0, 0);
			break;
		case 0x1:
			doKey(AK_KEYPAD_VOLUME_A, data[3]); // vol+
			break;
		case 0x2:
			doKey(AK_KEYPAD_VOLUME_D, data[3]);// vol-
			break;
		case 0x4:
			doKey(KEY_PREVIOUSSONG, data[3]);
			break;
		case 0x3:
			doKey(KEY_NEXTSONG, data[3]);
			break;
		case 0x5:
			doKey(KEY_BT, data[3]);
			break;			
		case 0x6:
			doKey(AK_KEYPAD_MUTE_FAKE, data[3]);// mute
			break;
		case 0x7:
			doKey(KEY_MODE, data[3]);
			break;
		case 0x8:
			doKey(MyCmd.Keycode.KEY_SEEK_NEXT, data[3]);
			break;
		case 0x9:
			doKey(MyCmd.Keycode.KEY_SEEK_PREV, data[3]);
			break;
		case 0xa:
			doKey(KEY_HOME, data[3]);
			break;
		case 0xb:
			doKey(KEY_GPS, data[3]);
			break;
		}
	}


	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[1]) {
		case 0x20:
			parseWheelKey(data, len);
			break;
		case (byte)0xd0: {
//			if ((data[2] & 0x1) != 0) {
				int door = (data[2] & 0x1f);
//				door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6)
//						| ((door & 0x10) >> 2) | ((door & 0x20) >> 2)
//						| ((door & 0x08) << 1) | ((door & 0x4) << 3));

//				if (mDoorStatus != door) {
//					mDoorStatus = door;
					Handler handler = getHandler("CanService");
					if (null != handler) {
						handler.sendMessage(handler.obtainMessage(
								CANBOX_DOOR_STATUS, door, 0));

					}
//				}
//			}

		}
		case (byte)0xa0: {

			int temp = (data[3] & 0xff);
			String s = "";
			if (temp >= 0 && temp <= 59) {
				if (data[2] == 1) {
					temp = -temp;
				}
				
				s = temp+mContext.getResources().getString(
						R.string.temp_unic_centigrade);

				if (s.length() > 1) {
					GlobalDef.sendByCarServiceToSystemUI(mContext,
							"com.android.systemui",
							MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
				}
			}
		}
			break;
		}

	}

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		
	}

	public void setMediaSrc(int source) {// default is simple box
		
	}

	public void setVolume(int volume) {

		
	}
	
//	private byte mDoorStatus = 0;

//	public void setReverseRadaVol(byte param) {
//		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x0, param };
//		sendDataToCanbox(data, data.length);
//	}
//
//	public void setParkCarMode(byte param) {
//		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x2, param };
//		sendDataToCanbox(data, data.length);
//	}
//
//	public void requestInfo(byte param) {
//		byte[] data = new byte[] { (byte) 0x90, 0x2, param, 0 };
//		sendDataToCanbox(data, data.length);
//	}

}
