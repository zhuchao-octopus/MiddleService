package com.my.cartype.simple;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.GlobalDef;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.out.R;

public class GMCSimple extends Canbox {

	public GMCSimple() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x0, 0x0,
				0x0, 0x1 });
	}

	private final static byte KEYS_WHEEL[][] = { { 0x1, AK_KEYPAD_VOLUME_A },
			{ 0x2, AK_KEYPAD_VOLUME_D },

			{ 0x7, KEY_SOURCE }, { 0x9, KEY_BT },

			{ 0x12, KEY_PLAYPAUSE }, { 0x13, KEY_PREVIOUSSONG },
			{ 0x14, KEY_NEXTSONG },

			{ 0x18, KEY_HOME },

	};

	private void parseWheelKey(byte[] data) {
		if (doKeyStudy(data[2], data[3])){
			return;
		}
		byte key = 0;
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

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x20: {
			parseWheelKey(data);
		}
			break;

		case 0x24: {
			int door = (data[2] & 0xfc);
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

		case 0x27: {

			int temp = 0;
			String s = "";

			if ((data[3] & 0x1) == 0) {
				temp = data[2];
				s = temp
						+ mContext.getResources().getString(
								R.string.temp_unic_centigrade);

			} else {

				temp = (int) (data[2] & 0xff);
				if ((data[3] & 0x2) != 0) {
					temp = -temp;
				}

				s = temp
						+ mContext.getResources().getString(
								R.string.temp_unic_fahrenheit);

			}

			GlobalDef.sendByCarServiceToSystemUI(mContext, "com.android.systemui",
					MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
		}
			break;
		case 0x29: {
			Handler handler = getHandler("Reverse");
			if (null != handler) {
				short angle = (short) ((data[2] & 0xff) | ((data[3]) << 8));

				angle = (short) (angle * 300 / 8500);
				angle = (short) (0 - angle);

				handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE,
						angle, 10));
			}
		}
			break;
		case (byte) 0x39:
			showSOS(data[2] & 0x1);
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

	private int mDoorStatus = 0;

	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
	private View mView;
	public boolean isAddView = false;

	private void showSOS(int show) {

		if (show != 0) {

			if (mView == null) {
				mView = ((LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.sos_layout, null);

				mWindowManager = (WindowManager) mContext
						.getSystemService(Context.WINDOW_SERVICE);
				mLayoutParams = new WindowManager.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
						0, 0, LayoutParams.TYPE_SYSTEM_ERROR,
						LayoutParams.FLAG_LAYOUT_IN_SCREEN
								| LayoutParams.FLAG_NOT_FOCUSABLE,
						PixelFormat.RGBA_8888);

				// mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;

			}
			if (mView != null) {
				if (!isAddView) {
					mWindowManager.addView(mView, mLayoutParams);
					isAddView = true;

					BroadcastUtil.sendToCarService(mContext,
							MyCmd.Cmd.CANBOX_PHONE_STATUS, 1);

				}
			}
		} else {
			if (isAddView) {
				mWindowManager.removeView(mView);
				isAddView = false;

				BroadcastUtil.sendToCarService(mContext,
						MyCmd.Cmd.CANBOX_PHONE_STATUS, 0);
			}
		}
	}

}
