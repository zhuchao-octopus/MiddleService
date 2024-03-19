package com.my.cartype.bagoo;

import java.util.Calendar;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.GlobalDef;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.out.R;

public class CarPSABagoo extends Canbox {

	public CarPSABagoo() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });

		if (CarUtil.getCarEQ() == 1) {
			CarUtil.mIsNeedSendEQ = true;
		}
	}

	private void parseWheelKey(byte[] data, int len) {
		if (doKeyStudy(data[2], data[3])){
			return;
		}
		
		switch (data[2]) {
		case 0x00: {
			doKey(0, 0);
		}
			break;
		case 0x6c:
		case 0x2: {
			// doKey(KEY_MENU, data[3]); // vol+
			doKey(KEY_HOME, data[3]); // vol+
		}
			break;
		case 0x74:
		case 0x19:
		case (byte) 0x84:
		case 0x7: {
			doKey(MyCmd.Keycode.ENTER, data[3]); // vol+
		}
			break;
		case 0x17:
		case 0x03:
		case 0x72: {
			doKey(KEY_PREVIOUSSONG, data[3]);
		}
			break;
		case 0x73:
		case 0x18:
		case 0x04: {
			doKey(KEY_NEXTSONG, data[3]);
		}
			break;
		case 0x68:
		case 0x08: // R
		case (byte) 0x8d: {
			doKey(KEY_BACK, data[3]);
		}
			break;
		case 0x6b:
		case 0x1a:
		case 0x10: // src
		{
			doKey(KEY_MODE, data[3]);
		}
			break;

		case (byte) 0x8c:
		case 0x11: // tel
		{
			doKey(KEY_BT, data[3]);
		}
			break;
		case 0x13:
		case 0x78:
			doKey(MyCmd.Keycode.KEY_SEEK_PREV, data[3]);
			break;
		case 0x77:
		case 0x12:
			doKey(MyCmd.Keycode.KEY_SEEK_NEXT, data[3]);
			break;
		case 0x75:
		case 0x58:
		case 0x14: {
			doKey(AK_KEYPAD_VOLUME_A, data[3]); // vol+
			// vol+
		}
			break;
		case 0x76:
		case 0x59:
		case 0x15: {
			doKey(AK_KEYPAD_VOLUME_D, data[3]);
			// vol-
		}
			break;
		case 0x6a:
		case 0x16: // mute
		{
			doKey(AK_KEYPAD_MUTE_FAKE, data[3]);// mute
		}
			break;
		case (byte) 0x85:
		case (byte) 0xc1:
		case 0x30: // HANGUP
		{
			doKey(KEY_BT_DIAL, data[3]);
		}
			break;
		case (byte) 0x86:
		case (byte) 0xc0:
		case 0x31: // HANGUP
		{
			doKey(KEY_BT_HANG, data[3]);
		}
			break;
		case 0x60: // POwer
		{
			doKey(KEY_POWER, data[3]);
		}
			break;
		case 0x61: // POwer
		{
			doKey(KEY_NUM_1, data[3]);
		}
			break;
		case 0x62: // POwer
		{
			doKey(KEY_NUM_2, data[3]);
		}
			break;
		case 0x63: // POwer
		{
			doKey(KEY_NUM_3, data[3]);
		}
			break;
		case 0x64: // POwer
		{
			doKey(KEY_NUM_4, data[3]);
		}
			break;
		case 0x65: // POwer
		{
			doKey(KEY_NUM_5, data[3]);
		}
			break;
		case 0x66: // POwer
		{
			doKey(KEY_NUM_6, data[3]);
		}
			break;

		case 0x67: // POwer
		{
			doKey(MyCmd.Keycode.EJECT, data[3]);
		}
			break;

		case (byte) 0x88:
		case 0x6e:
			doKey(MyCmd.Keycode.AUDIO, data[3]);
			break;
		case (byte) 0x87:
		case 0x6f:
			doKey(MyCmd.Keycode.RADIO, data[3]);
			break;

		case 0x70:
			doKey(MyCmd.Keycode.FAST_R, data[3]);
			break;
		case 0x71:
			doKey(MyCmd.Keycode.FAST_F, data[3]);
			break;

		case (byte) 0x80:
			doKey(MyCmd.Keycode.LEFT, data[3]);
			break;
		case (byte) 0x81:
			doKey(MyCmd.Keycode.RIGHT, data[3]);
			break;
		case (byte) 0x82:
			doKey(MyCmd.Keycode.UP, data[3]);
			break;
		case (byte) 0x83:
			doKey(MyCmd.Keycode.DOWN, data[3]);
			break;

		case (byte) 0x89:

			doKey(MyCmd.Keycode.NAVIGATION, data[3]);
			break;

		case (byte) 0x69:
			doKey(MyCmd.Keycode.DARK, data[3]);
			break;

		case (byte) 0x20:
			doKey(MyCmd.Keycode.KEY_CAR_INFO, data[3]);
			break;
		case (byte) 0x52:
			doKey(MyCmd.Keycode.KEY_MEM_INFO, data[3]);
			break;
		case (byte) 0x40:
			doKey(MyCmd.Keycode.KEY_CHECK, data[3]);
			break;
		// case (byte) 0x50:
		case (byte) 0x8b:
			doKey(MyCmd.Keycode.SETUP, data[3]);
			break;
		}
	}

	byte[] airData = new byte[8];
	byte eco = 0;

	private void parseACInfo(byte[] data, int len) {

		airData[0] = (byte) ((data[2] & 0x80)
				| (data[2] & 0x40)
				| ((data[2] & 0x1) << 5) 
				| ((data[2] & 0x20) >> 1)
				| ((data[2] & 0x10) >> 1) 
				| ((data[2] & 0x8) >> 2) 
				| ((data[2] & 0x4) >> 2));

		airData[1] = 0;
		if (((data[3] & 0xf0) == 0x40) ||
				((data[3] & 0xf0) == 0x60)
				|| ((data[3] & 0xf0) == 0x70) 
				|| ((data[3] & 0xf0) == 0x80)) {
			airData[1] |= 0x80;
		}

		if (((data[3] & 0xf0) == 0x30) 
				|| ((data[3] & 0xf0) == 0x50)
				|| ((data[3] & 0xf0) == 0x70) 
				|| ((data[3] & 0xf0) == 0x80)) {
			airData[1] |= 0x40;
		}

		if (((data[3] & 0xf0) == 0x20) 
				|| ((data[3] & 0xf0) == 0x50)
				|| ((data[3] & 0xf0) == 0x60) 
				|| ((data[3] & 0xf0) == 0x80)) {
			airData[1] |= 0x20;
		}

		byte b = (byte) (data[4] & 0xf);
		if (b == 0xf) {
			b = 0;
		}
		airData[1] |= b;

		airData[6] = 0;
		if (((data[3] & 0xf) == 0x4) || ((data[3] & 0xf) == 0x6)
				|| ((data[3] & 0xf) == 0x7) || ((data[3] & 0xf) == 0x8)) {
			airData[6] |= 0x80;
		}

		if (((data[3] & 0xf) == 0x3) || ((data[3] & 0xf) == 0x5)
				|| ((data[3] & 0xf) == 0x7) || ((data[3] & 0xf) == 0x8)) {
			airData[6] |= 0x40;
		}

		if (((data[3] & 0xf) == 0x2) || ((data[3] & 0xf) == 0x5)
				|| ((data[3] & 0xf) == 0x6) || ((data[3] & 0xf) == 0x8)) {
			airData[6] |= 0x20;
		}

		if (data[5] == 0xd) {
			airData[2] = 0;
		} else if (data[5] == 0x3f) {
			airData[2] = (byte) 0xff;
		} else {
			airData[2] = (byte) (2 * (((data[5] & 0xff) * 0.5) + 9.5));
		}

		if (data[6] == 0xd) {
			airData[3] = 0;
		} else if (data[6] == 0x3f) {
			airData[3] = (byte) 0xff;
		} else {
			airData[3] = (byte) (2 * (((data[6] & 0xff) * 0.5) + 9.5));
		}

		airData[4] = 0;
		if ((data[2] & 0x3) == 0) {
			airData[4] |= 0x80;
		}
		airData[4] |= ((data[7] & 0x40) >> 4);

		airData[7] = 0;
		if ((data[2] & 0x30) == 0x30) {
			airData[7] |= 0x10;
		} else {

		}

		airData[7] |= ((data[7] & 0x80) >> 4);

		if ((data[7] & 0x3) == 0) {
			airData[7] |= 0x2;
		} else if ((data[7] & 0x3) == 0x2) {
			airData[7] |= 0x4;
		}

		airData[7] |= (eco & 0x1);
		Handler handler = getHandler("CanService");
		if (null != handler) {
			handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, 1, 0,
					airData));
		}
	}

	private boolean mRequestOurdoorTemp = false;

	public void startConnect() {// default is simple box

		super.startConnect();
		startConnect2();
	}

	public void startConnect2() {// default is simple box

		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });
		// super.startConnect();
		mHandler.removeMessages(0);
		mHandler.sendEmptyMessageDelayed(0, 2000);
		if (!mRequestOurdoorTemp) {
			// mRequestOurdoorTemp = true;

			byte[] data = new byte[] { (byte) 0xf1, 0x1, 0x36 };
			sendDataToCanbox(data, data.length);
		}

		Util.doSleep(10);
		byte[] data = new byte[] { (byte) 0xf1, 0x1, 0x71 };
		sendDataToCanbox(data, data.length);
	}

	public void stopConnect() {// default is simple box
		super.stopConnect();
		mHandler.removeMessages(0);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				startConnect2();
				break;
			}
			super.handleMessage(msg);
		}
	};

	private final static byte[] RADAR_CHANGE = new byte[] { 1, 5, 7, 9, 11, 14 };

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x02: {
			if (data[1] == 1) {
				data[3] = 1;
			}
			parseWheelKey(data, len);

		}
			break;
		case 0x41: {
			parseACInfo(data, len);
		}
			break;
		case 0x71: {
			// byte[] version = new byte[9];
			// Util.byteArrayCopy(version, data, 0, 2, version.length);
			String date = ((data[4] & 0xf0) >> 4) + ""+ ((data[4] & 0xf) >> 0)
					+ "-" + ((data[5] & 0xf0) >> 4) + ""+ ((data[5] & 0xf) >> 0)
					+ "-" + ((data[6] & 0xf0) >> 4) + ""+ ((data[6] & 0xf) >> 0);
			mVersion = data[2] + " " + data[3] + " "+ date + "v" + data[8]+ ""
					+ data[9] + ""+ data[10];
			
			break;
		}
		case 0x32: // Radar ll
		{

			if ((data[2] == 1) || (data[2] == 2)) {
				try {
					mRadar[0] = RADAR_CHANGE[data[3]];
					mRadar[1] = RADAR_CHANGE[data[4]];
					mRadar[2] = RADAR_CHANGE[data[4]];
					mRadar[3] = RADAR_CHANGE[data[5]];

					mRadar[4] = RADAR_CHANGE[data[6]];
					mRadar[5] = RADAR_CHANGE[data[7]];
					mRadar[6] = RADAR_CHANGE[data[7]];
					mRadar[7] = RADAR_CHANGE[data[8]];

				} catch (Exception e) {
					// Log.d("ee","e");
				}

				boolean zero = Util.isZero(mRadar);
				if (!zero) {
					RadarManager.start(mContext);
					checkHideRadarEx(5000);
				}

				Handler handler = getHandler(RadarManager.TAG);

				if (null != handler) {
					handler.sendMessage(handler
							.obtainMessage(CANBOX_RADAR_BACK));
				}

			} else {
				RadarManager.stop();
			}

		}
			break;
		// case 0x23: // Radar front
		// {
		// byteArrayCopy(mRadar, data, 4, 2, 4);
		// RadarManager.start(mContext);
		// Handler handler = getHandler(RadarManager.TAG);
		// if (null != handler) {
		// handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
		// }
		// }
		// break;
		// case 0x25: // Radar status
		// {
		// byte[] status = new byte[2];
		// status[0] = data[2];
		// status[1] = data[3];
		//
		// Handler handler = getHandler("Reverse");
		// if (null != handler) {
		// handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_STATUS,
		// status));
		// }
		// }
		// break;
		case 0x62: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				// Log.e("1", ""+(data[2] & 0xff));
				// Log.e("2", ""+(data[3] & 0xff));
				// Log.e("3", ""+((data[3] << 8) | (data[2] & 0xff)));
				int angle = (0x80 - (int) (data[2] & 0xff)) / 4;
				// Log.d(TAG,"angle+"+angle);
				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle, 0));
			}
		}
			break;
		case 0x38: {
			int door = (data[2] & 0xf8);
			door = (((door & 0x40) >> 5) | ((door & 0x80) >> 7)
					| ((door & 0x10) >> 1) | ((door & 0x20) >> 3) | ((door & 0x08) << 1));

			if (mDoorStatus != door) {
				mDoorStatus = door;
				Handler handler = getHandler("CanService");
				if (null != handler) {
					handler.sendMessage(handler.obtainMessage(
							CANBOX_DOOR_STATUS, mDoorStatus, 0));

				}
			}

			if (eco != (byte) (data[2] & 0x1)) {
				eco = (byte) (data[2] & 0x1);
				airData[7] &= ~0x1;
				airData[7] |= (eco & 0x1);
//				Handler handler = getHandler("CanService");
//				if (null != handler) {
//					handler.sendMessage(handler.obtainMessage(
//							CANBOX_RETURN_AIR, 1, 0, airData));
//				}
			}

			sendCanboxInfo("com.canboxsetting", data);

		}
			break;
		case 0x36:
			updateOutDoorTemp(data[2]);
			break;
		case 0x60:
			// Calendar c = Calendar.getInstance();
			//
			// c.set(Calendar.YEAR, data[3] + 2000);
			// c.set(Calendar.MONTH, data[4] - 1);
			// c.set(Calendar.DAY_OF_MONTH, data[5]);
			// c.set(Calendar.HOUR_OF_DAY, data[6]);
			// c.set(Calendar.MINUTE, data[7]);
			// c.set(Calendar.SECOND, data[8]);
			//
			// long when = c.getTimeInMillis();
			//
			// try {
			// SystemClock.setCurrentTimeMillis(when);
			//
			// } catch (Exception e) {
			//
			// }
			break;
		case 0x33:
		case 0x34:
		case 0x35:
		case 0x61:
		case 0x49:
		case 0x47:
			sendCanboxInfo("com.canboxsetting", data);
			break;
		}
	}

	private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
	public void updateOutDoorTemp(int temp) {

		if (temp == CarUtil.INVALID_OUT_DOOR_TEMP) {
			if (mTempOutDoor != CarUtil.INVALID_OUT_DOOR_TEMP) {
				temp = mTempOutDoor;
			} else {
				return;
			}
		}
		mTempOutDoor = temp;

		String s = "";
		
		if ((temp & 0x80) != 0) {
			temp = -(temp & 0x7f);
		} else {
			temp = (temp & 0x7f);
		}		
		
		if (CarUtil.mTempUnit == 2) {
			temp = (int) ((temp) * 1.8f + 32);
			s = ""
					+ temp
					+ mContext.getResources().getString(
							R.string.temp_unic_fahrenheit);
		} else {
			s = ""
					+ temp
					+ mContext.getResources().getString(
							R.string.temp_unic_centigrade);
		}
		GlobalDef.sendByCarServiceToSystemUI(mContext, "com.android.systemui",
				MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
	}
	
	private int mDoorStatus = 0;

	public void setReverseRadaVol(byte param) {
		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x0, param };
		sendDataToCanbox(data, data.length);
	}

	public void setParkCarMode(byte param) {
		byte[] data = new byte[] { (byte) 0xc6, 0x2, 0x2, param };
		sendDataToCanbox(data, data.length);
	}

	public void requestInfo(byte param) {
		byte[] data = new byte[] { (byte) 0x90, 0x2, param, 0 };
		sendDataToCanbox(data, data.length);
	}

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
		// byte min = (byte) ((time / 60) % 60);
		// byte sec = (byte) ((time) % 60);
		// ++play;
		// byte[] data = new byte[] { (byte) 0xa3, 0x1, (byte) (total & 0xFF),
		// (byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF),
		// (byte) ((play >> 8) & 0xFF), min, sec };
		// sendDataToCanbox(data, data.length);
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		if (b[0] == 0x3) {
			b[0] = 5;
		} else {
			b[0] = 1;
		}
		byte[] data = new byte[] { (byte) 0x9a, 0x5, 8, b[0], b[1], b[2], 0 };
		sendDataToCanbox(data, data.length);
	}

	public void setMediaSrc(int source) {// default is simple box
		switch (source) {
		case MyCmd.SOURCE_DVD:
			mSource = 0x2;
			break;
		case MyCmd.SOURCE_RADIO:
			mSource = 0x1;
			break;
		case MyCmd.SOURCE_AUX:
			mSource = 0x4;
			break;
		case MyCmd.SOURCE_BT:
			mSource = 0x7;
			break;
		default:
			mSource = 0x6;
			break;
		}

		byte[] data = new byte[] { (byte) 0x99, 0x2, mSource, mVolume };
		sendDataToCanbox(data, data.length);
	}

	private byte mEq[] = null;

	public void sendEqToCanbox(byte[] eq) {
		if (eq != null && eq.length >= 13) {
			if (mEq == null) {
				mEq = new byte[eq.length];
				Util.byteArrayCopy(mEq, eq, 0, 0, eq.length);
				return;
			}

			byte type = 0;
			if (eq[0] != mEq[0]) {
				type = 2;
			} else if (eq[1] != mEq[1]) {
				type = 1;
			} else if (eq[2] != mEq[2] || eq[3] != mEq[3] || eq[4] != mEq[4]
					|| eq[5] != mEq[5]) {
				type = 3;
			} else {
				type = 4;
			}

			byte[] buf = new byte[10];
			int i;
			buf[0] = (byte) 0x98;
			buf[1] = 0x8;
			buf[2] = type;

			i = (eq[0] * 180 / 14);
			if (i % 10 > 0) {
				i /= 10;
				++i;
			} else {
				i /= 10;
			}

			buf[4] = (byte) i;
			i = (eq[1] * 180 / 14);

			if (i % 10 > 0) {
				i /= 10;
				++i;
			} else {
				i /= 10;
			}

			buf[3] = (byte) i;

			buf[5] = (byte) ((eq[2] + eq[3] + eq[4] + eq[5]) / 4);
			buf[6] = (byte) ((eq[6] + eq[7] + eq[8] + eq[9] + eq[10]) / 5);
			buf[7] = eq[11];

			switch (eq[12]) {
			case 4:
				buf[9] = 1;
				break;
			case 3:
				buf[9] = 5;
				break;
			case 5:
				buf[9] = 3;
				break;
			case 2:
				buf[9] = 2;
				break;
			case 1:
				buf[9] = 4;
				break;
			default:
				buf[9] = 0;
				break;
			}

			sendDataToCanbox(buf, buf.length);

			Util.byteArrayCopy(mEq, eq, 0, 0, eq.length);
		}
	}

	private byte mVolume;
	private byte mSource = 0x6;

	public void setVolume(int volume) {
		mVolume = (byte) volume;
		if (mVolume == 0) {
			mVolume |= 0x80;

		}
		byte[] data = new byte[] { (byte) 0x99, 0x2, mSource, (byte) volume };
		sendDataToCanbox(data, data.length);
	}
}
