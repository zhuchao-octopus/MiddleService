package com.zhuchao.android.car.cartype.xinbasi;

import java.util.Date;
import java.util.Locale;

import android.provider.Settings;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.R;
public class NissanXinbas extends Canbox {

	public NissanXinbas() {

		buildCmdVersion((byte) 0x7f, (byte) 0x0);
		mIdKey = 0x1;
		MAP_KEYS = KEYS_WHEEL;
	}

	private final static byte[][] KEYS_WHEEL = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN }, { 0x3, MyCmd.Keycode.NEXT },
			{ 0x4, MyCmd.Keycode.PREVIOUS }, { 0x6, MyCmd.Keycode.MUTE },
			{ 0x7, MyCmd.Keycode.MODLE },

			{ 0x9, MyCmd.Keycode.BT_DIAL }, { 0xa, MyCmd.Keycode.BT_HANG },

			{ 0x15, MyCmd.Keycode.BACK }, { 0x16, MyCmd.Keycode.PLAY_PAUSE },
			{ (byte) 0x87, MyCmd.Keycode.POWER },

	};

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		if (data[0] == 2) {
			if (data[2] == 1) {
				switch (data[3]) {
					case 1:
						doKey(MyCmd.Keycode.KEY_AM);
						break;
					case 2:
					case 3:
						doKey(MyCmd.Keycode.KEY_FM);
						break;
					case 4:
						doKey(MyCmd.Keycode.DVD);
						break;
					case 5:
					case 6:
						doKey(MyCmd.Keycode.AUDIO);
						break;
					case 7:
						doKey(MyCmd.Keycode.BT_MUSIC);
						break;
					case 8:
						doKey(MyCmd.Keycode.AUX_IN);
						break;
				}
			} else if (data[2] == 2) {
				switch (data[3]) {
					case 1:
						doKey(MyCmd.Keycode.BT_DIAL);
						break;
					case 2:
						doKey(MyCmd.Keycode.BT_HANG);
						break;
				}
			}
		}
		super.parseCanboxData(data, len);
	}

	@Override
	public void setVolume(int volume) {

		byte[] data = new byte[] { (byte) 0x85, 0x1, (byte) volume };
		sendDataToCanbox(data, data.length);
	}

	private byte[] mData = new byte[] { (byte) 0x82, 0x8, 0, 0, 0, 0, 0, 0, 0,
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
			mData = new byte[] { (byte) 0x82, 0x8, s, s2, 0,
					(byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec };

		} else {
			mData = new byte[] { (byte) 0x82, 0x8, s, s2, 0, 0,
					(byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), min,
					sec };
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
		mData = new byte[] { (byte) 0x82, 0x8, 0x1, 0x1, b[0], b[1], b[2], 0,
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
		mData = new byte[] { (byte) 0x82, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0 };
		// }

		sendDataToCanbox(mData, mData.length);
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

				data[0] = index;
				data[1] = 2;
				data[2] = 0x10;
				data[3] = 0;
			} else {
				
				int len = num_len + 3;

				data = new byte[len];

				data[0] = index;
				data[1] = (byte) (num_len + 1);
				data[2] = 0x2;
				for (int i = 0; i < num_len && i < (data[1]); ++i) {
//					data[3 + i] = n[i];
					
					if (i % 2 == 0) {
						data[3 + i] = n[i + 3];
					} else {
						data[3 + i] = n[i + 1];
					}
					
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
//		sendId3((byte) 0x83, s);
//		mName = s;
	}

	public void setSongAritst(String s) {
//		sendId3((byte) 0x84, s);
//		mArtist = s;
	}


}
