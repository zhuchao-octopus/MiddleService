package com.zhuchao.android.car.cartype.simple;

import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.canbox.SosManager;
import com.zhuchao.android.car.cartype.CarUtil;

import com.zhuchao.android.car.view.Nissian360ButtonView;

public class Nissan2013Simple extends Canbox {

	public Nissan2013Simple() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });


		buildCmdAngle((byte) 0x29, (byte) 0x0, 5400);
		
	}

	private final static int[][] KEYS_WHEEL = { { 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D }, { 0x4, KEY_NEXTSONG },
			{ 0x3, KEY_PREVIOUSSONG }, { 0x7, KEY_SOURCE },
			{ 0x9, KEY_BT_DIAL }, { 0xA, KEY_BT_HANG }, { 0x15, KEY_BACK },
			{ 0x16, KEY_PLAYPAUSE }, { (byte) 0x87, KEY_POWER, },

			{ 0x22, KEY_NEXTSONG }, { 0x21, KEY_PREVIOUSSONG },
			{ 0x23, KEY_NEXTSONG }, { 0x24, KEY_PREVIOUSSONG },
			{ 0x40, MyCmd.Keycode.DARK }, { 0x41, KEY_MUTE },

			{ 0x42, MyCmd.Keycode.BACKLIGHT_ON },
			{ 0x43, MyCmd.Keycode.BACKLIGHT_OFF },

	};

	private final static byte[][] KEYS_WHEEL_IXB = { { 0x22, KEY_NEXTSONG },
			{ 0x21, KEY_PREVIOUSSONG }, { 0x23, KEY_NEXTSONG },
			{ 0x24, KEY_PREVIOUSSONG }, { 0x40, MyCmd.Keycode.DARK },
			{ 0x41, KEY_MUTE }

	};

	private void parseWheelKeyIXB(byte[] data) {
		byte key = 0;
		for (int i = 0; i < KEYS_WHEEL_IXB.length; ++i) {
			if (KEYS_WHEEL2[i][0] == data[2]) {
				key = KEYS_WHEEL_IXB[i][1];
				break;
			}
		}

		if (key != 0) {
			doKey(key, 1);
			doKey(key, 0);
		}
	}

	private void parseWheelKey(byte[] data) {
		if (doKeyStudy(data[2], data[3])) {
			return;
		}
		int key = 0;
		for (int i = 0; i < KEYS_WHEEL.length; ++i) {
			if (KEYS_WHEEL[i][0] == data[2]) {
				key = KEYS_WHEEL[i][1];
				break;
			}
		}

		if (key != 0) {
			if (CarUtil.getKeyType() == 1 && data[2] == 0x16) {
				if (data[3] == 0) {
					sendAVMKey();
				}
			} else {
				if (CarUtil.getKeyType() == 2) {
					if (key == KEY_BT_HANG) {
						key = KEY_BT;
					}
				}
				doKey(key, data[3]);
			}
		} else {
			if (data[3] == 0) {
				doKey(0, 0);
			}
		}
	}

	Nissian360ButtonView mNissian360ButtonView;

	private boolean isShowButton() {

		boolean ret = false;
		if (CarUtil.getKeyType() != 0 && CarUtil.getKeyType() != 2) {
			if (CarUtil.getCarType2() == 0) {
				ret = true;
			}
		}

		return ret;
	}

	public void setContext(Context c) {
		super.setContext(c);
		updateTime();
		udpateLang();
		if (isShowButton()) {
			mNissian360ButtonView = Nissian360ButtonView.getInstance(mContext);
			mNissian360ButtonView.showMicButton();
		} else {
			mNissian360ButtonView = null;
		}

		mAVMCarType = SystemConfig.getIntProperty(mContext,
				MachineConfig.VALUE_CANBOX_NISSAN2013);
	}

	public void startConnect() {
		super.startConnect();
		if (mContext != null) {
			if (isShowButton()) {
				mNissian360ButtonView = Nissian360ButtonView
						.getInstance(mContext);
				mNissian360ButtonView.showMicButton();
			} else {
				mNissian360ButtonView = null;
			}
		}

		startRepeatSendLcdMsg(true);
	}

	public void stopConnect() {
		super.stopConnect();
		hide360Button();
		startRepeatSendLcdMsg(false);
	}
	public void repeatSendLcdMsg(){
		if (mData0xc0 != null){
			sendDataToCanbox(mData0xc0, mData0xc0.length);
		} 
		super.repeatSendLcdMsg();
	}
	public void show360Button() {
		if (isShowButton()) {
			hide360Button();
			if (CarUtil.getCarType2() == 0) {
				if (mNissian360ButtonView != null) {
					mNissian360ButtonView.showMicButton();
				}
			}
		}
	}

	public void hide360Button() {
		if (mNissian360ButtonView != null) {
			mNissian360ButtonView.hideMicButton();
		}
	}

	private int mAVMCarType = 0;

	public void sendAVMKey() {

		byte[] data;// = new byte[] { (byte) 0xc6, 0x2, 0x2, 0x1 };
		if (mAVMCarType == 0) {
			data = new byte[] { (byte) 0xc6, 0x2, 0x2, 0x1 };
		} else {
			data = new byte[] { (byte) 0xc7, 0x1, 0x1 };
		}
		sendDataToCanbox(data, data.length);
	}

	public void sendLangKey() {
		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x2, 0x2 };
		sendDataToCanbox(data, data.length);
	}

	private final static byte[][] KEYS_WHEEL2 = {

	{ 0x1, MyCmd.Keycode.KEY_AM }, { 0x2, MyCmd.Keycode.KEY_FM },
			{ 0x3, MyCmd.Keycode.KEY_FM }, { 0x9, KEY_FM }, { 0x9, KEY_FM },

			{ 0x4, KEY_DVD }, { 0x5, KEY_MEDIA }, { 0x6, KEY_MEDIA },
			{ 0xa, KEY_MEDIA },

			{ 0x7, MyCmd.Keycode.BT_MUSIC }, { 0x8, MyCmd.Keycode.AUX_IN },

			{ 0xe, MyCmd.Keycode.KEY_TV }, { 0x10, MyCmd.Keycode.ALL_APP },

			{ 0x11, KEY_BT_DIAL }, { 0x12, KEY_BT_HANG }, };

	private void parseWheelKey2(byte[] data) {
		if (doKeyStudy(1, data[2], 1)) {
			doKeyStudy(1, data[2], 0);
			return;
		}

		byte key = 0;
		for (int i = 0; i < KEYS_WHEEL2.length; ++i) {
			if (KEYS_WHEEL2[i][0] == data[2]) {
				key = KEYS_WHEEL2[i][1];
				break;
			}
		}

		if (key != 0) {
			doKey(key, 1);
			doKey(key, 0);
		}
	}

	private void do1050SosCustomCmd(byte[] data) {
		if (MachineConfig.VALUE_SYSTEM_UI22_1050.equals(GlobalDefinition.mSystemUI)
				|| MachineConfig.VALUE_SYSTEM_UI_1050_2
						.equals(GlobalDefinition.mSystemUI)
				|| CarUtil.getCarType() == 5) {
			switch (data[2]) {
			case 0x23:
				if (data[3] == 1) {
					SosManager.start(mContext, 1);
				}
				break;
			case 0x24:
			case 0x26:
				if (data[3] == 1) {
					SosManager.stop();
				}
				break;
			case 0x25:
				if (data[3] == 1) {
					SosManager.start(mContext, 2);
				}
				break;
			}
		}
	}

	private int mDoorStatus;

	private byte getRadarData(byte i) {
		byte data = 0;
		switch (i) {
		case 0:
			data = 0;
			break;
		case 4:
			data = 1;
			break;
		case 3:
			data = 4;
			break;
		case 2:
			data = 7;
			break;
		case 1:
			data = 11;
			break;
		}
		return data;
	}

	private void checkHideRadar() {
		mHandler.removeMessages(HIDE_RADAR);
		mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
	}

	private final static int HIDE_RADAR = 0;
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == HIDE_RADAR) {
				RadarManager.stop();
			}
			super.handleMessage(msg);
		}
	};


	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x20: {
			if (data[1] == 1) { // ixb
				parseWheelKeyIXB(data);
			} else {
				parseWheelKey(data);
				do1050SosCustomCmd(data);
			}
		}
			break;

		case 0x22: // Radar back
		{

			mRadar[0] = getRadarData(data[2]);
			mRadar[1] = getRadarData(data[3]);
			mRadar[2] = getRadarData(data[4]);
			mRadar[3] = getRadarData(data[5]);

			boolean zero = Util.isZero(mRadar);
			if (!zero){
				RadarManager.start(mContext);
				checkHideRadar();
			}
			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {				
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
			}
		}
			break;

		case 0x23: // Radar back
		{

			mRadar[4] = getRadarData(data[2]);
			mRadar[5] = getRadarData(data[3]);
			mRadar[6] = getRadarData(data[4]);
			mRadar[7] = getRadarData(data[5]);

			boolean zero = Util.isZero(mRadar);
			if (!zero){
				RadarManager.start(mContext);
				checkHideRadar();
			}
			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {				
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
			}
		}
			break;

		case 0x28: {
			int door = (data[2] & 0xfe);
			door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6)
					| ((door & 0x10) >> 2) | ((door & 0x20) >> 2)
					| ((door & 0x08) << 1) | ((door & 0x4) << 3));

			if (mDoorStatus != door) {
				mDoorStatus = door;
				Handler handler = getHandler("CanService");
				if (null != handler) {
					handler.sendMessage(handler.obtainMessage(
							CANBOX_DOOR_STATUS, mDoorStatus, 0));

				}
			}

		}
			break;

		case 0x2f: {
			parseWheelKey2(data);
		}
			break;
		case 0x30: {
			byte[] version = new byte[16];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		case 0x60:
			int DATA_ID = ((data[3] & 0xff) | ((data[2] & 0xff) << 8));
			int Param_1 = ((data[7] & 0xff) | ((data[6] & 0xff) << 8)
					| ((data[5] & 0xff) << 16) | ((data[4] & 0xff) << 24));
			int Param_2 = ((data[11] & 0xff) | ((data[10] & 0xff) << 8)
					| ((data[9] & 0xff) << 16) | ((data[8] & 0xff) << 24));
			int Param_3 = ((data[15] & 0xff) | ((data[14] & 0xff) << 8)
					| ((data[13] & 0xff) << 16) | ((data[12] & 0xff) << 24));

			Log.d("ixb", "0x60:" + DATA_ID + ":" + Param_1 + ":" + Param_2
					+ ":" + Param_3 + ":");
			Intent i = new Intent("com.esensetech.BR_EV_CANDATA");
			i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			i.putExtra("EV_Extra_Data_ID", DATA_ID);
			i.putExtra("EV_Extra_Param_1", Param_1);
			i.putExtra("EV_Extra_Param_2", Param_2);
			i.putExtra("EV_Extra_Param_3", Param_3);
			mContext.sendBroadcast(i);
			break;
		case (byte) 0x94:
			byte[] version = new byte[] { data[2], data[3], data[4], data[5] };
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(
						CANBOX_NISSIAN_UI_DATA, version));
			}
			break;
		default:
			super.parseCanboxData(data, len);
		}
		

		returnDriveData(data);
	}

	byte[] mData0xc0;

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
			return;//0xff 0x70 0x71
//			break;
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
			mData0xc0 = new byte[] { (byte) 0xc0, 0x8, s, s2, 0,
					(byte) ((play) & 0xFF), (byte) (total & 0xFF), h, min, sec };

		} else {
			mData0xc0 = new byte[] { (byte) 0xc0, 0x8, s, s2,
					(byte) ((play) & 0xFF), (byte) ((play & 0xFF00) >> 8), 0,
					h, min, sec };
		}

		if (mPhoneStatus < HFP_INFO_CALLED) {

			sendDataToCanbox(mData0xc0, mData0xc0.length);
		}
	}

	public void setMediaSrc(int source) {// default is simple box
		byte s;
		byte mediaType = 0;
		switch (source) {
		case 0:
			s = 1;
			// mediaType = 1;
			break;
		case 1:
			s = 2;
			mediaType = 0x10;
			break;
		case MyCmd.SOURCE_BT:
			s = 0xb;
			mediaType = 0x30;
			break;		
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 8;
			mediaType = (byte) 0xff;
			break;
		default:
			s = 0x0c;
			mediaType = 0x30;
			break;
		}
		mData0xc0 = new byte[] { (byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0,
				0, 0 };
		if (mediaType != 0) {
			sendDataToCanbox(mData0xc0, mData0xc0.length);
		}
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		// setMediaSrc(0);
		// if (b[0] != 0x10) {
		// b[0] += 1;
		// }
		if (b[3] >= 0 & b[3] <= 30) {
			b[3]++;
		} else {
			b[3] = 0;
		}
		mData0xc0 = new byte[] { (byte) 0xc0, 0x8, 0x1, 0x1, b[0], b[1], b[2], b[3],
				0, 0 };
		sendDataToCanbox(mData0xc0, mData0xc0.length);
	}

	public void sendId3(byte index, String num) {

		try {
			if (num == null) {
				num = " ";
			}
			byte[] n = getBytesUnicodeLittleEndian(num); // del 0xff 0xfe

			int num_len = n.length;
			byte[] data;

			if (num_len > 2) {
				if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
					num_len -= 2;
				}
			}
			int len = num_len + 3;
			
			data = new byte[len];

			data[0] = index;
			data[1] = (byte) (num_len+1);
			data[2] = 0x11;
			for (int i = 0; i < num_len && i < (data.length - 3); ++i) {
				data[3 + i] = n[i+2];
			}

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

			Log.d("Nissan2013Simple", "sendId3" + e);
		}
	}

	String mName = null;
	String mArtist = null;
	String mAlbum = null;

	// public void setPhone(int status, String num) {
	// sendId3((byte)0x1, num);
	// }

	public void setSongName(String s) {
		sendId3((byte) 0x70, s);
		mName = s;
	}

	public void setSongAritst(String s) {
		sendId3((byte) 0x71, s);
		mArtist = s;
	}

//	public void setSongAlbum(String s) {
//		sendId3((byte) 0x3, s);
//		mAlbum = s;
//	}

	private int mPhoneStatus = HFP_INFO_INITIAL;

	public void setPhoneEx(int status, String num, String name) {

		mPhoneStatus = status;

		switch (status) {
		case HFP_INFO_INITIAL:
		case HFP_INFO_READY:
		case HFP_INFO_CONNECTING:
			status = 0;
			break;
		case HFP_INFO_CONNECTED:
			status = 2;
			break;
		case HFP_INFO_CALLED:
		case HFP_INFO_INCOMING:
		case HFP_INFO_CALLING:
			status = 1;
			break;
		}

		if (num == null) {
			num = " ";
		}

		// data2 = new byte[] { (byte) 0xc0, 0x8, 0x5, 0x40, 0, 0, 0, 0, 0, 0 };
		//
		// sendDataToCanbox(data2, data2.length);
		// Util.doSleep(50);

		byte[] n = num.getBytes(); // del 0xff 0xfe

		int num_len = n.length;
		byte[] data;

		int len = num_len + 4;

		data = new byte[len];

		data[0] = (byte) 0xc5;
		data[1] = (byte) (num_len + 2);
		data[2] = (byte) status;
		data[3] = 0x1;
		for (int i = 0; i < num_len && i < (data.length - 4); ++i) {
			data[4 + i] = n[i];
		}

		sendDataToCanbox(data, data.length);

	}

	public void setVolume(int volume) {

		byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
		sendDataToCanbox(data, data.length);
	}

	public void updateTime() {
		Date curDate = new Date(System.currentTimeMillis());
		byte h = (byte) curDate.getHours();

		h = fixTimeHour(h);
		byte ampm = 0;
		String strTimeFormat = Settings.System.getString(
				mContext.getContentResolver(),
				android.provider.Settings.System.TIME_12_24);

		byte format;
		if ("12".equals(strTimeFormat)) {
			if (h >= 12) {
				ampm |= 0x80;
			}

			ampm |= 0x40;

			if (h > 12) {
				h -= 12;
			} else if (h == 0) {
				h = 12;
			}

			h |= 0x80;
		}

		byte m = (byte) curDate.getMinutes();

		byte[] buf = new byte[] { (byte) 0xc6, 0x04, 0x01, h, m, ampm };
		sendDataToCanbox(buf, buf.length);
	}

	public int getLongKey(int key) {
		int ret = 0;
		switch (key) {
		case MyCmd.Keycode.NEXT:
			if (CarUtil.getKeyType() == 6) {
				mKeyDown = 0;
				sendAVMKey();
			} else {
				ret = MyCmd.Keycode.KEY_SEEK_NEXT;
			}
			break;
		case MyCmd.Keycode.PREVIOUS:
			if (CarUtil.getKeyType() == 5) {
				mKeyDown = 0;
				sendAVMKey();
			} else {
				ret = MyCmd.Keycode.KEY_SEEK_PREV;
			}
			break;
		case MyCmd.Keycode.MODLE:
			// ret = MyCmd.Keycode.KEY_MIC;
			if (CarUtil.getKeyType() == 3) {
				mKeyDown = 0;
				sendAVMKey();
			}
			break;
		case KEY_BT_DIAL:
		case KEY_BT_HANG:
			// ret = MyCmd.Keycode.KEY_MIC;
			if (CarUtil.getKeyType() == 4) {
				mKeyDown = 0;
				sendAVMKey();
			}
			break;
		}
		return ret;
	}

	protected void doKey(int value, int status) { // value 0 -> key up

		// Log.d("Nissan2013Simple", "doKey:" + value);
		// if (CarUtil.getChangeKey() == 1) {
		value = changeKey(value);
		// }

		switch (status) {
		case 0:
			if (mKeyDown != 0) {
				if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
					int ret = getLongKey(value);
					if (ret != 0) {
						mKeyDown = ret;
					}
				}
				doKey(mKeyDown);
				mKeyDown = 0;
				longClick = false;
			}
			break;
		case 1:
			mKeyDown = value;
			mClickTime = System.currentTimeMillis();
			longClick = false;
			break;
		case 2:
			if (value == AK_KEYPAD_VOLUME_A || value == AK_KEYPAD_VOLUME_D) {
				doKey(value);
				mKeyDown = 0;
			} else {
				if ((System.currentTimeMillis() - mClickTime) > LONG_CLICK_TIME) {
					if (mKeyDown != 0) {
						longClick = true;
						int ret = getLongKey(value);
						if (ret != 0) {
							doKey(ret);
							mKeyDown = 0;
						}
					}
				}
			}
			break;
		}

	}

	public void udpateLang() {
		int lang = -1;
		String locale = Locale.getDefault().getLanguage();
		if (locale != null) {
			if (locale.equals("en")) {
				lang = 1;
			} else if (locale.equals("zh")) {
				if ("CN".equals(Locale.getDefault().getCountry())) {
					lang = 0;
				} else {
					lang = 0x16;
				}
			} else if (locale.equals("de")) {
				lang = 2;
			} else if (locale.equals("fr")) {
				lang = 3;
			} else if (locale.equals("es")) {
				lang = 4;
			} else if (locale.equals("it")) {
				lang = 5;
			} else if (locale.equals("nl")) {
				lang = 6;
			} else if (locale.equals("pt")) {
				lang = 7;
			} else if (locale.equals("tr")) {
				lang = 8;
			} else if (locale.equals("ru")) {
				lang = 9;
			} else if (locale.equals("uk")) {
				lang = 0xa;
			} else if (locale.equals("da")) {
				lang = 0xb;
			} else if (locale.equals("sv")) {
				lang = 0xc;
			} else if (locale.equals("fi")) {
				lang = 0xd;
			} else if (locale.equals("nb")) {
				lang = 0xe;
			} else if (locale.equals("fa")) {
				lang = 0xf;
			} else if (locale.equals("sk")) {
				lang = 0x10;
			} else if (locale.equals("cs")) {
				lang = 0x11;
			} else if (locale.equals("hu")) {
				lang = 0x12;
			} else if (locale.equals("el")) {
				lang = 0x13;
			} else if (locale.equals("ko")) {
				lang = 0x14;
			} else if (locale.equals("jp")) {
				lang = 0x15;
			}

		}
		if (lang != -1) {
			byte[] buf = { (byte) 0x83, 0x2, 0x31, (byte) lang };
			sendDataToCanbox(buf, buf.length);
		}
	}
	

	private void returnDriveData(byte[] buf) {
		if (mRequestDriveData > 0) {
			boolean update = true;
			if (buf[0] == 0x28) {
				if ((buf[3] & 0x2) == 0) {
					mDriveData[10] = 3;
				} else {
					if ((buf[3] & 0x1) != 0) {
						mDriveData[10] = 1;
					} else {
						mDriveData[10] = 4;
					}
				}

				if ((buf[3] & 0x4) != 0) {
					mDriveData[14] = 1;
				} else {
					mDriveData[14] = 0;
				}
			} else {
				update = false;
			}
			if (update) {
				returnDriveData();
			}
		}
	}
	public int getUpdateTime() {
		return 60000;
	}
	
	@Override
	public int getAngleValue2(byte[] data) {

		int angle = (short) ((data[3] & 0xff) | (((data[2] & 0x7f)) << 8));

		if ((data[2] & 0x80) != 0) {
			angle = -angle;
		}
		return angle;

	}

}
