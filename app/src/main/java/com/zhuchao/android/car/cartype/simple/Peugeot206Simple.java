package com.zhuchao.android.car.cartype.simple;

import com.zhuchao.android.car.GlobalDefinition;


import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.zhuchao.android.car.R;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;

public class Peugeot206Simple extends Canbox {

	public Peugeot206Simple() {
		
		
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x01, 0x2,
				0x3, 0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x02, 0x0,
				0x0, 0x0, 0x1 });

	}

	private final static byte[][] KEYS_WHEEL = {

	{ 0x1, AK_KEYPAD_VOLUME_A }, 
	{ 0x2, AK_KEYPAD_VOLUME_D },
			{ 0x4, KEY_PREVIOUSSONG }, 
			{ 0x3, KEY_NEXTSONG },
			{ 0x5, KEY_HOME },
			{ 0x6, KEY_PREVIOUSSONG }, 
			{ 0x7, KEY_NEXTSONG }, 
//			{ (byte) 0x80, KEY_HOME },

	};

	private void parseWheelKey(byte[] data, int len) {
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
			doKey(key, data[3]);
		}else {
			if (data[3] == 0){
				doKey(0, 0);
			}
		}

	}

	private byte getRadarData(byte i) {
		byte data = 0;
		switch (i) {
		case 1:
			data = 1;
			break;
		case 2:
			data = 3;
			break;
		case 3:
			data = 7;
			break;
		case 4:
			data = 11;
			break;
		}
		return data;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x20: {
			parseWheelKey(data, len);
		}
			break;
		case 0x22: // Radar back
		{
				mRadar[0] = getRadarData(data[2]);
				mRadar[1] = getRadarData(data[3]);
				mRadar[2] = getRadarData(data[4]);
				mRadar[3] = getRadarData(data[5]);


				// byteArrayCopy(mRadar, data, 0, 2, 4);
				boolean zero = Util.isZero(mRadar);
				if (!zero){
					RadarManager.start(mContext);
					checkHideRadarEx(2000);
				}
				Handler handler = getHandler(RadarManager.TAG);
				if (null != handler) {
					checkHideRadar();
					handler.sendMessage(handler.obtainMessage(
							CANBOX_RADAR_BACK, 0, 0));
				}
			
		}
			break;

		case 0x24: {
			if ((data[2] & 0x1) != 0) {
				int door = (data[2] & 0xfe);

				door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6)
						| ((door & 0x10) >> 2) | ((door & 0x20) >> 2)
						| ((door & 0x08) << 1) | ((door & 0x04) << 3));

				if (mDoorStatus != door) {
					mDoorStatus = door;
					Handler handler = getHandler("CanService");
					if (null != handler) {
						handler.sendMessage(handler.obtainMessage(
								CANBOX_DOOR_STATUS, mDoorStatus, 0));

					}
				}
			}
		}

			break;
		case 0x27: {

			int temp = (data[2] & 0x7f);
			
			

			 String s = String.format("%d.%d%s", temp/2,((temp*10/2)%10), mContext.getResources()
					.getString(R.string.temp_unic_centigrade));
			if ((data[2] & 0x80) != 0) {
				s = "-"+s;
			}
			
			GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui",
					MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);

		}
			break;

		case 0x33:

			sendCanboxInfo("com.canboxsetting", data);
			break;
		case 0x30:{
			byte[] version = new byte[16];
			Util.byteArrayCopy(version, data, 0, 2, version.length);

			mVersion = (new String(version));
			// version
			break;
		}
		}
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
	private int mDoorStatus = 0;

}
