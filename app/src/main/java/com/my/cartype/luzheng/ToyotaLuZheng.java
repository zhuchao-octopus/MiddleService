package com.my.cartype.luzheng;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.GlobalDef;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.manager.OSProManager;
import com.my.out.R;

public class ToyotaLuZheng extends Canbox {
	public ToyotaLuZheng() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });
//		if (CarUtil.getCarEQ() == 1) {
//			CarUtil.mIsNeedSendEQ = true;
//			CarUtil.setMcuEQZoneUsed(1);
//		} else {
//			CarUtil.setMcuEQZoneUsed(0);
//		}
		

		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;	
	}

	@Override
	public void setContext(Context c) {
		// TODO Auto-generated method stub
		super.setContext(c);
		mHandlerSendEQ.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mContext != null) {
					byte[] data = new byte[] { (byte) 0xff, 0x1 };
					Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
					i.putExtra("buf", data);
					mContext.sendBroadcast(i);
				}
			}
		}, 1500);

	}

	public void startConnect() {// default is simple box
		super.startConnect();
//		if (CarUtil.mIsNeedSendEQ) {
			sendEQ((byte) 0x8, (byte) 1);
//		}
//		mHandlerSendEQ.removeMessages(0);
//		if (CarUtil.mIsNeedSendEQ) {
//			// int volume = SystemConfig.getIntProperty2(mContext,
//			// SystemConfig.CANBOX_EQ_VOLUME);
//			// if (volume == -1) {
//			// volume = 45;
//			// }
//			// sendEQ((byte) 0xa, (byte)0);//unmute
//			// setEQVolume(volume);
//			mResetVolume = true;
//			mHandlerSendEQ.sendEmptyMessageDelayed(0, 800);
//		} else {
//			if (CarUtil.getCarEQ() == 0) {
//				sendEQ((byte) 0x8, (byte) 1);
//			}
//		}

		if (CarUtil.getCarType() != 0) {
			byte[] data = new byte[] { (byte) 0xCA, 0x1,
					(byte) (CarUtil.getCarType() - 1) };
			sendDataToCanbox(data, data.length);
		}
		udpateLang();
		if (mContext != null) {
			byte[] data = new byte[] { (byte) 0xff, 0x1 };
			Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
			i.putExtra("buf", data);
			mContext.sendBroadcast(i);
		}
	}

	public void stopConnect() {// default is simple box
		mHandlerSendEQ.removeMessages(0);
		if (CarUtil.mIsNeedSendEQ) {
			sendEQ((byte) 0x8, (byte) 0);
		}
		super.stopConnect();
	}

	private void startEQ() {

	}

	private final static byte KEYS_WHEEL[][] = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.KEY_SEEK_NEXT },
		{ 0x4, MyCmd.Keycode.KEY_SEEK_PREV },
		{ 0x5, MyCmd.Keycode.MODLE },
		{ 0x6, MyCmd.Keycode.MUTE },
		{ 0x7, MyCmd.Keycode.SPEECH },
		{ 0x8, MyCmd.Keycode.BT_DIAL },
		{ 0x9, MyCmd.Keycode.BT_HANG },
		{ 0xa, MyCmd.Keycode.HOME },
		{ 0xb, MyCmd.Keycode.PREVIOUS },
		{ 0xc, MyCmd.Keycode.NEXT },
		{ 0xd, MyCmd.Keycode.PREVIOUS },
		{ 0xe, MyCmd.Keycode.NEXT },
		{ 0xf, MyCmd.Keycode.NAVIGATION },
		{ 0x11, MyCmd.Keycode.AUDIO },
		{ 0x12, MyCmd.Keycode.MENU },
		{ 0x13, MyCmd.Keycode.PREVIOUS },
		{ 0x14, MyCmd.Keycode.NEXT },
		{ 0x15, MyCmd.Keycode.BACK },
		{ 0x16, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x17, MyCmd.Keycode.ROLL_NEXT },
		{ 0x18, MyCmd.Keycode.ROLL_PREV },
		{ (byte)0x81, MyCmd.Keycode.VOLUME_UP },
		{ (byte)0x82, MyCmd.Keycode.VOLUME_DOWN },
		{ (byte)0x84, MyCmd.Keycode.PREVIOUS },
		{ (byte)0x83, MyCmd.Keycode.NEXT },
		{ (byte)0x86, MyCmd.Keycode.PREVIOUS },
		{ (byte)0x85, MyCmd.Keycode.NEXT },
		{ (byte)0x87, MyCmd.Keycode.POWER },
		{ (byte)0x88, MyCmd.Keycode.MODLE },

	};

	
	byte[] airData = new byte[15];



	private byte getACTempPriv(byte data, int unit) {
		if (unit == 0) {
			if (data == 0x1f) {
				data = (byte) 0xff;
			} else if (data == 0) {
			} else if (data >= 0x1 && data <= 0x1d) {
				data = (byte) ((18f + (0.5f * (data - 0x1))) * 2);
			} else if (data >= 0x21 && data <= 0x26) {
				data = (byte) ((15f + (0.5f * (data - 0x21))) * 2);
			} else {
				data = (byte) 0xfa;
			}
		}

		return data;
	}
	

	byte[] dataAir = new byte[13];
	private void parseACInfo(byte[] data, int len) {
		int copy = data.length - 1;
		if (copy > dataAir.length) {
			copy = dataAir.length;
		}
		Util.byteArrayCopy(dataAir, data, 0, 0, copy);

		dataAir[4] = getACTempPriv(dataAir[4], (dataAir[6] & 0x1));
		dataAir[5] = getACTempPriv(dataAir[5], (dataAir[6] & 0x1));
		
		

		airData[0] = (byte) (dataAir[2] & 0xff);
		airData[0] |= (byte) (((dataAir[6] & 0x80) >> 6) | ((dataAir[6] & 0x40) >> 6));

		airData[1] = (byte) (dataAir[3] & 0xff);
		airData[2] = (byte) (dataAir[4] & 0xff);
		airData[3] = (byte) (dataAir[5] & 0xff);


//		airData[4] &= ~0xcc;
//		airData[4] = (byte) ((dataAir[6] & 0x08) >> 1);
//		airData[4] |= (byte) (((dataAir[7] & 0x0c) >> 2) | ((dataAir[7] & 0x3) << 4));

//		airData[4] |= (byte) (((dataAir[6] & 0x20) << 2) | ((dataAir[6] & 0x4) << 1));

		airData[5] = (byte) ((dataAir[6] & 0x01)
				| ((dataAir[2] & 0x01) << 4) );

		airData[7] &= ~0x01;
		airData[7] |= (byte) ((dataAir[6] & 0x10) >> 4);

		airData[9] = (byte) (((dataAir[2] & 0x10) << 1)

				| ((dataAir[6] & 0x80) >> 4)
				| ((dataAir[6] & 0x08) << 1) 
				| ((dataAir[8] & 0x10) >> 3) );
		

		airData[10] = getACTempPriv(dataAir[7], (dataAir[6] & 0x1));
		airData[11] = (byte) (dataAir[8] & 0xef);
		airData[14] = getACTempPriv(dataAir[9], (dataAir[6] & 0x1));
		
		boolean airControl = false;
//		if (/* (dataAir[2] & 0x80) != 0 && */((dataAir[3] & 0x10) != 0)) {
			airControl = true;
//		}
		airData[5] |= 0x80;
		if (!sendCanboxAir(airData)) {
			Handler handler = getHandler("CanService");
			if (airControl && null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR,
						airData));
			}
		}
	}

//	private boolean mResetVolume = true;
	Handler mHandlerSendEQ = new Handler() {
		public void handleMessage(Message msg) {

//			int volume = MachineConfig
//					.getIntProperty2(SystemConfig.CANBOX_EQ_VOLUME);
//			if (volume == -1) {
//				volume = 45;
//			}
//			if (mResetVolume) {
//				sendEQ((byte) 0x8, (byte) 1);
//				sendEQ((byte) 0xa, (byte) 0);// unmute
//				mResetVolume = false;
//			}
//			setEQVolume(volume);
//
//			mHandlerSendEQ.removeMessages(0);
//			mHandlerSendEQ.sendEmptyMessageDelayed(0, 1000);
		}
	};

	private byte[] mAcData = new byte[1];
	private byte[] mAcDataEx = new byte[1];

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x20: {
			super.parseCanboxData(data, len);
		}
			break;	

		case 0x28: {
			// mAcData = new byte[len];
			if (!isBufEqual(data, mAcData)) {
				mAcData = data.clone();
				parseACInfo(data, len);
			}

		}
			break;
	
		case 0x1D: // Radar font
		{
			if (m360Exit) {
				return;
			}
			boolean show = false;
			for (int i = 4; i < 8; ++i) {
				switch (data[2 + i - 4]) {
				case 0:
					if (mRadar[i] != 0) {
						show = true;
						mRadar[i] = 0;
					}
					break;
				case 1:
					if (mRadar[i] != 1) {
						show = true;
						mRadar[i] = 1;
					}
					break;
				case 2:
					if (mRadar[i] != 4) {
						show = true;
						mRadar[i] = 4;
					}
					break;
				case 3:
					if (mRadar[i] != 6) {
						show = true;
						mRadar[i] = 6;
					}
					break;
				case 4:
					if (mRadar[i] != 0xa) {
						show = true;
						mRadar[i] = 0xa;
					}
					break;
				}
			}
			boolean zero = Util.isZero(mRadar);
			if (show && !zero) {
				RadarManager.start(mContext);
				checkHideRadarEx(5000);
			}
			Handler handler = getHandler(RadarManager.TAG);
			if (show && null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
			}
		}
			break;
		case 0x1E: // Radar back
		{
			if (m360Exit) {
				return;
			}
			for (int i = 0; i < 4; ++i) {
				switch (data[2 + i]) {
				case 0:
					mRadar[i] = 0;
					break;
				case 1:
					mRadar[i] = 1;
					break;
				case 2:
					mRadar[i] = 4;
					break;
				case 3:
					mRadar[i] = 6;
					break;
				case 4:
					mRadar[i] = 0xa;
					break;
				}
			}

			if ((data[6] & 0x20) != 0) {
				boolean zero = Util.isZero(mRadar);
				if (!zero) {

					if (CarUtil.getCarType2() == 1
							|| CarUtil.getCarType2() == 2) {
						if (!mIsOpenCamera) {
							do360CameraSwitch(1);
							mIsOpenCamera = true;
						}
					}

					if (CarUtil.getCarType2() != 1) {
						RadarManager.start(mContext);
						checkHideRadarEx(5000);
					}
				}
			} else {
				mIsOpenCamera = false;
				RadarManager.stop();
			}

			// RadarManager.start(mContext);
			Handler handler = getHandler(RadarManager.TAG);
			if (null != handler) {
				handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
			}

			sendCanboxInfo(data);
		}
			break;
		case 0x24: {
			int door = (data[2]);
			door = (((door & 0x40) >> 5) | ((door & 0x80) >> 7)
					| ((door & 0x10) >> 1) | ((door & 0x20) >> 3)
					| ((door & 0x08) << 1) | ((data[3] & 0x80) >> 2));

			if ((data[3] & 0x60) == 0x20) {
				door |= 0x80;
			} else if ((data[3] & 0x60) == 0x60) {

				door |= 0x40;
			}

			if (mDoorStatus != door) {
				mDoorStatus = door;
				Handler handler = getHandler("CanService");
				if (null != handler) {
					handler.sendMessage(handler.obtainMessage(
							CANBOX_DOOR_STATUS, mDoorStatus, 0));

				}
			}
			sendCanboxInfo(data);
		}
			break;
		case 0x35: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				short a = (short) ((data[3] & 0xff) | (((data[2] & 0x0f)) << 8));

				if ((data[2] & 0x8) != 0) {
					a = (short) ((data[3] & 0xff) | (((data[2] & 0x0f) | 0xf0) << 8));
				} else {
					
				}

				int angle = ((a * 3000) / 380);

				if (angle > -50 && angle < 50) {
					angle = 50;
				}

				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle, 100));
			}
		}
			break;

		case 0x30:
			byte[] version = new byte[16];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		case 0x31:
			returnEQData(data);
			break;
		case 0x41: {
			switch (data[2]) {
			case 3:

				updateOutDoorTemp(data[9]);

				break;
			}
		}
			sendCanboxInfo(data);
			break;
		case 0x32:
//			if (CarUtil.getCarEQ() == 0) {
//				if ((data[2] & 0x1) == 0) {
//					if (CarUtil.mIsNeedSendEQ) {
//
//						CarUtil.mIsNeedSendEQ = false;
//						CarUtil.setMcuEQZoneUsed(0);
//
//						mHandlerSendEQ.removeMessages(0);
//					}
//				} else {
//					if (!CarUtil.mIsNeedSendEQ) {
//						CarUtil.mIsNeedSendEQ = true;
//						CarUtil.setMcuEQZoneUsed(1);
//
//						mResetVolume = true;
//						mHandlerSendEQ.removeMessages(0);
//						mHandlerSendEQ.sendEmptyMessageDelayed(0, 1000);
//					}
//				}
//			}

			if ((data[2] & 0x4) != 0) {
				m360Exit = true;
				mIsOpenCamera = false;
				RadarManager.stop();
			} else {
				m360Exit = false;
			}

			if (m360Exit) {
				if ((data[2] & 0x8) != 0) {
					OSProManager.simulationReverse((byte) 1);
				} else {
					OSProManager.simulationReverse((byte) 0);
				}
			}
			break;
		case 0x26:
		case 0x1A:
		case 0x27:
		case 0x21:
		case 0x22:
		case 0x23:
		case 0x25:
		case 0x1f:
		case 0x2a:
		case 0x2b:
			if (data[0] == 0x25) {
				mData0x25 = data;
			} else if (data[0] == 0x1f) {
				mData0x1f = data;
			} else if (data[0] == 0x21) {
				mData0x21 = data;
			} else if (data[0] == 0x22) {
				mData0x22 = data;
			} else if (data[0] == 0x23) {
				mData0x23 = data;
			}
			sendCanboxInfo(data);
			break;
		}
	}

	public void hideRadar() {
		mIsOpenCamera = false;
	}

	private boolean mIsOpenCamera = false;

	private void do360CameraSwitch(int s) {

		String top = AppConfig.getTopActivity();

		Intent it = new Intent(Intent.ACTION_VIEW);
		boolean topIsCamera = false;
		if (top != null
				&& top.contains("com.my.frontcamera.FrontCameraActivity")) {
			topIsCamera = true;
		}
		if (s == 0) {
			if (topIsCamera) {
				it.putExtra("finish", 1);
			}
		} else {
			if (!topIsCamera) {
				topIsCamera = true;
			}
		}

		if (topIsCamera) {
			try {
				it.setClassName("com.car.ui",
						"com.my.frontcamera.FrontCameraActivity");
				it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
						| Intent.FLAG_ACTIVITY_NEW_TASK);

				mContext.startActivity(it);
			} catch (Exception e) {
				// Log.e(TAG, e.getMessage());
			}
		}
	}

	private boolean m360Exit = false;

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
		String s;
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

	private int mDoorStatus;
	private byte[] mData0x21;
	private byte[] mData0x22;
	private byte[] mData0x23;
	private byte[] mData0x25;
	private byte[] mData0x1f;

	public void sendDataToCanbox(byte[] data, int len) {
		if ((data[0] & 0xff) == 0xff) {
			if (data[1] == 0x25) {
				sendCanboxInfo(mData0x25);
			} else if (data[1] == 0x1f) {
				sendCanboxInfo(mData0x1f);
			} else if (data[1] == 0x21) {
				sendCanboxInfo(mData0x21);
			} else if (data[1] == 0x22) {
				sendCanboxInfo(mData0x22);
			} else if (data[1] == 0x23) {
				sendCanboxInfo(mData0x23);
			}
		} else {
			super.sendDataToCanbox(data, len);
		}

	}

	// public void setReverseRadaVol(byte param){
	// byte []data = new byte[]{(byte)0xc6, 0x2, 0x0, param};
	// sendDataToCanbox(data, data.length);
	// }
	// public void setParkCarMode(byte param){
	// byte []data = new byte[]{(byte)0xc6, 0x2, 0x2, param};
	// sendDataToCanbox(data, data.length);
	// }
	// public void requestInfo(byte param){
	// byte []data = new byte[]{(byte)0x90, 0x2, param, 0};
	// sendDataToCanbox(data, data.length);
	// }

	private void sendEQ(byte cmd, byte param) {
		byte[] data = new byte[] { (byte) 0x84, 0x2, cmd, param };
		sendDataToCanbox(data, data.length);
	}

	private void setEQVolume(int volume) {

		byte[] data = new byte[] { (byte) 0x84, 0x2, 0x07, (byte) volume };
		sendDataToCanbox(data, data.length);
	}

	private int mVolume = -1;

	public void setVolume(int volume) {
//		if (CarUtil.mIsNeedSendEQ) {
//			if (volume == 0) {
//				sendEQ((byte) 0xa, (byte) 1);
//			} else if (mVolume == 0) {
//				sendEQ((byte) 0xa, (byte) 0);
//			}
//
//			mVolume = volume;
//
//			// byte[] data = new byte[] { (byte) 0xc4, 0x1, (byte) volume };
//			// sendDataToCanbox(data, data.length);
//		} else {
//			setEQVolume(volume);
//		}
	}

	byte[] mEqData = new byte[6];

	public void sendEqToCanbox(byte[] eq) {
//		if (eq != null && eq.length >= 11) {
//
//			byte[] data = new byte[] { (byte) 0x84, 0x2, 0, 0 };
//
//			byte[] eq2 = new byte[6];
//
//			eq2[0] = eq[0];
//			eq2[1] = eq[1];
//
//			eq2[3] = (byte) (eq[2] + eq[3] + eq[4]);
//			eq2[4] = (byte) (eq[5] + eq[6] + eq[7]);
//			eq2[5] = (byte) (eq[8] + eq[9] + eq[10]);
//			switch (eq[12]) {
//			case 2:
//				eq2[2] = 3;
//
//				break;
//			case 3:
//				eq2[2] = 1;
//
//				break;
//			case 5:
//				eq2[2] = 0;
//
//				break;
//			case 4:
//				eq2[2] = 2;
//
//				break;
//			default:
//				eq2[2] = 4;
//				break;
//			}
//
//			byte param;
//
//			if (eq2[0] != mEqData[0]) {
//				param = (byte) ((eq2[0]));
//				sendEQ((byte) 1, param);
//				Util.doSleep(5);
//			}
//			if (eq2[1] != mEqData[1]) {
//				param = (byte) ((eq2[1]));
//				sendEQ((byte) 2, param);
//				Util.doSleep(5);
//			}
//			// if (eq2[2] != mEqData[2]) {
//			// param = eq2[2];
//			// sendEQ((byte) 3, param);
//			// Util.doSleep(5);
//			// }
//
//			if (eq2[3] != mEqData[3]) {
//				param = (byte) ((eq2[3] * 11) / 45);
//				sendEQ((byte) 4, param);
//				Util.doSleep(5);
//			}
//			if (eq2[4] != mEqData[4]) {
//				param = (byte) ((eq2[4] * 11) / 45);
//				sendEQ((byte) 6, param);
//				Util.doSleep(5);
//			}
//			if (eq2[5] != mEqData[5]) {
//				param = (byte) ((eq2[5] * 11) / 45);
//				sendEQ((byte) 5, param);
//				Util.doSleep(5);
//			}
//			mEqData = eq2;
//		}
	}

	public void udpateLang() {
		int lang = -1;
		String locale = Locale.getDefault().getLanguage();
		if (locale != null) {
			if (locale.equals("en")) {
				if ("US".equals(Locale.getDefault().getCountry())) {
					lang = 2;
				} else if ("GB".equals(Locale.getDefault().getCountry())) {
					lang = 8;
				} else {
					lang = 1;
				}
			} else if (locale.equals("zh")) {
				lang = 0;
			} else if (locale.equals("ru")) {
				lang = 9;
			} else if (locale.equals("tr")) {
				lang = 5;
			} else if (locale.equals("fr")) {
				lang = 4;
			} else if (locale.equals("de")) {
				lang = 6;
			} else if (locale.equals("es")) {
				lang = 3;
			}

		}
		if (lang != -1) {
			byte[] buf = { (byte) 0x83, 0x2, 0x26, (byte) lang };
			sendDataToCanbox(buf, buf.length);
		}
	}

	private final static int BUTTON_AUTO_W = 160;
	private final static int BUTTON_AUTO_H = 120;

	public void touchInReverse(int x, int y, int w, int h) {
		if (!m360Exit) {
			return;
		}
		int button = 0;
		if (OSProManager.mReverse == 1 && OSProManager.mSimulationReverse == 0) {
			if (CarUtil.getKeyType() == 0) {

				if (x > (w - BUTTON_AUTO_W) && y > (h - BUTTON_AUTO_H)) {
					button = 3;
				} else if ((x < (w - BUTTON_AUTO_W))
						&& x > ((w / 2 - BUTTON_AUTO_W))
						&& y > (h - BUTTON_AUTO_H)) {
					button = 7;
				} else if (x < BUTTON_AUTO_W && y > (h - BUTTON_AUTO_H)) {
					button = 4;
				}
			} else {

				if (x < BUTTON_AUTO_W && y > (h - BUTTON_AUTO_H)) {
					button = 4;
				} else if (x > (w - BUTTON_AUTO_W) && (y < BUTTON_AUTO_H)) {
					button = 6;
				} else if (x > (w - BUTTON_AUTO_W) && y > (h - BUTTON_AUTO_H)) {
					button = 3;
				}
			}
		} else {

			if (CarUtil.getKeyType() == 0) {
				if (x < BUTTON_AUTO_W && y > (h - BUTTON_AUTO_H)) {
					button = 4;
				} else if ((x < (w - BUTTON_AUTO_W))
						&& x > ((w / 2 - BUTTON_AUTO_W))
						&& y > (h - BUTTON_AUTO_H)) {
					button = 7;
				}
			} else {

				if (x < BUTTON_AUTO_W && y < BUTTON_AUTO_H) {
					button = 1;
				} else if ((x > (w - BUTTON_AUTO_W)) && y > (h - BUTTON_AUTO_H)) {
					button = 2;
				}

			}

		}

		// Log.d("eed", "!!!!!!!!!!!!:"+button);
		if (button != 0) {
			byte[] buf = { (byte) 0x83, 0x2, 0x21, (byte) button };
			sendDataToCanbox(buf, buf.length);
		}
	}

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

		byte h = (byte) ((time / 3600));
		byte min = (byte) ((time / 60) % 60);
		byte sec = (byte) ((time) % 60);

		byte index = 0;
		switch (source) {
		case MyCmd.SOURCE_DVD:
			index = (byte) 0x3;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			++play;
			index = (byte) 0x83;
			break;
		case MyCmd.SOURCE_BT:
			index = 0xb;
			break;
		}

		if (mPhoneStatus < HFP_INFO_CALLED) {
			data = new byte[0x24];
			data[0] = (byte) 0x99;
			data[1] = (byte) 0x22;
			data[2] = index;
			data[3] = 1;

			try {
				String s = String.format(Locale.ENGLISH, "%02d:%02d %d/%d",
						min, sec, play, total);

				byte[] n = getBytesUnicodeLittleEndian(s); //del 0xff 0xfe
				int head = 0;
				if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
					head = 2;
				}
				for (int j = 0; j < (n.length - head); ++j) {
					data[4 + j] = n[j + head];
				}

				sendDataToCanbox(data, data.length);
			} catch (Exception e) {

			}
		}
	}

	public void setMediaSrc(int source) {// default is simple box
		byte s = 0;
		switch (source) {
		case MyCmd.SOURCE_RADIO:
			s = (byte) 0x81;
			break;
		case MyCmd.SOURCE_DVD:
			s = (byte) 0x83;
			break;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = (byte) 0x83;
			break;
		case MyCmd.SOURCE_AUX:
			s = (byte) 0x87;
			break;
		case MyCmd.SOURCE_BT:
			s = (byte) 0x85;
			break;
		}

		if (source != MyCmd.SOURCE_MUSIC && source != MyCmd.SOURCE_VIDEO) { // clear

			data = new byte[0x24];
			data[0] = (byte) 0x99;
			data[1] = (byte) 0x22;
			data[2] = s;

			for (int i = 0; i < 3; ++i) {
				data[3] = (byte) (2 + i);
				sendDataToCanbox(data, data.length);
				Util.doSleep(100);
			}

			if (s == (byte) 0x87 || s == (byte) 0x85) {
				data[3] = 1;
				sendDataToCanbox(data, data.length);
			}
		}
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
		int freq = (((b[2] & 0xff) << 8) | (b[1] & 0xff));
		String s = "";
		if (b[0] < 0x10) {
			b[0] = (byte) 0x81;

			s = String.format(Locale.ENGLISH, "%d.%02d MHz", freq / 100,
					freq % 100);

		} else {
			b[0] = (byte) 0x82;

			s = String.format(Locale.ENGLISH, "%d KHz", freq);
		}
		data = new byte[0x24];
		data[0] = (byte) 0x99;
		data[1] = (byte) 0x22;
		data[2] = b[0];
		data[3] = 1;

		try {
			byte[] n = getBytesUnicodeLittleEndian(s); //del 0xff 0xfe
			int head = 0;
			if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
				head = 2;
			}
			for (int j = 0; j < (n.length - head); ++j) {
				data[6 + j] = n[j + head];
			}

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

		}

	}

	byte[] data;

	private int mPhoneStatus = -1;

	public void setPhoneEx(int status, String num, String name) {
		if (mPhoneStatus == status) {
			return;
		}

		if (status >= HFP_INFO_CALLED && status <= HFP_INFO_CALLING) {
			sendId3((byte) 1, num);
			Util.doSleep(5);
			sendId3((byte) 2, name);
			Util.doSleep(5);
		} else {
			sendId3((byte) 1, "");
			Util.doSleep(5);
			sendId3((byte) 2, "");
			Util.doSleep(5);
		}

		switch (status) {
		case HFP_INFO_INITIAL:
		case HFP_INFO_READY:
		case HFP_INFO_CONNECTING:
			status = 0;
			break;
		case HFP_INFO_CONNECTED:
			if (mPhoneStatus == 0) {
				status = 5;
			} else {
				status = -1;
			}
			break;
		case HFP_INFO_CALLED:
			status = 3;
			break;
		case HFP_INFO_INCOMING:
			status = 1;
			break;
		case HFP_INFO_CALLING:
			status = 2;
			break;
		}

		mPhoneStatus = status;

		if (status > 0) {
			byte[] data2;

			if (num == null) {
				num = " ";
			}

			data2 = new byte[] { (byte) 0xc0, 0x8, 0x5, 0x40, (byte) status, 0,
					0, 0, 0, 0 };
			sendDataToCanbox(data2, data2.length);
		} else {
			if (data != null) {
				sendDataToCanbox(data, data.length);
			}
		}

	}

	public void sendId3(byte index, String num) {

		if (mPhoneStatus < HFP_INFO_CALLED) {
			data = new byte[0x24];
			data[0] = (byte) 0x99;
			data[1] = (byte) 0x22;
			data[2] = (byte) 0x83;
			data[3] = index;

			try {

				byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe
				int head = 0;
				if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
					head = 2;
				}
				for (int j = 0; j < (n.length - head); ++j) {
					data[4 + j] = n[j + head];
				}

				sendDataToCanbox(data, data.length);
			} catch (Exception e) {

			}

			sendDataToCanbox(data, data.length);
		}
	}

	String mName = null;
	String mArtist = null;
	String mAlbum = null;

	// public void setPhone(int status, String num) {
	// sendId3((byte)0x1, num);
	// }

	public void setSongName(String s) {
		sendId3((byte) 0x2, s);
		mName = s;
	}

	public void setSongAritst(String s) {
		sendId3((byte) 0x3, s);
		mArtist = s;
	}

	public void setSongAlbum(String s) {
		sendId3((byte) 0x4, s);
		mAlbum = s;
	}

	
	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (63 << 16) | (15 << 8) | 11;

			byte[] buf = new byte[] { (byte) 0x90, 0x2, (byte) 0x31, 0 };
			sendDataToCanbox(buf, buf.length);
		} else {
			byte[] buf = new byte[] { (byte) 0x84, 0x2, 0x0, (byte) data };
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				buf[2] = 5;
				buf[3]+=2;
				break;
			case EQ_CMD_SET_MIDDLE:
				buf[2] = 6;
				buf[3]+=2;
				break;
			case EQ_CMD_SET_LOW:
				buf[2] = 4;
				buf[3] += 2;
				break;
			case EQ_CMD_SET_ZONE_FR:
				buf[2] = 1;
				break;
			case EQ_CMD_SET_ZONE_LR:
				buf[2] = 2;
				break;
			case EQ_CMD_SET_VOLUME:
				buf[2] = 7;
				break;
			default:
				return 0;
			}

			sendDataToCanbox(buf, buf.length);
		}
		return ret;
	}

	private void returnEQData(byte[] buf) {
		byte[] data = new byte[6];

		data[0] = (byte) (buf[3] & 0x0f);
		data[1] = (byte) ((buf[4] & 0xf0) >> 4);
		data[2] = (byte) ((buf[3] & 0xf0) >> 4);
		data[3] = (byte) ((buf[2] & 0xf0) >> 4);
		data[4] = (byte) (buf[2] & 0x0f);

		data[5] = buf[5];

		data[0] -= 2;
		data[1] -= 2;
		data[2] -= 2;
		
		super.returnEQData(EQ_CMD_SET_ALL_DATA, data);
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

		if (h > 12) {
			h = (byte) (h - 12);
		}

		byte m = (byte) curDate.getMinutes();

		byte[] buf = new byte[] { (byte) 0x85, 0x06, 0, 0, 0, h, m, 0 };
		sendDataToCanbox(buf, buf.length);
	}
}
