package com.my.cartype.ods;

import java.util.Date;
import java.util.Locale;

import com.my.out.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.GlobalDef;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.tts.TextSpeaker;

public class DongNanDX7OD extends Canbox {

	public DongNanDX7OD() {
		// buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		// buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
		// buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
		// buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
		// buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
		// buildCmdOutTemp((byte) 0x41, (byte) 0x10);
		buildCmdVersion((byte) 0x30, (byte) 0x0);

		mIdAC = 0x28;
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;
		mIdKey2 = 0x21;
		MAP_KEYS2 = KEYS_WHEEL2;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
	}

	private final static byte IDS_TO_CANBOXSETTING[] = { 0x24, 0x25, 0x32,
			0x33, 0x40, 0x41,0x34 };

	private final static byte KEYS_WHEEL[][] = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN }, 
			{ 0x4, MyCmd.Keycode.NEXT },
			{ 0x3, MyCmd.Keycode.PREVIOUS }, 
			{ 0x6, MyCmd.Keycode.MUTE },
			{ 0x7, MyCmd.Keycode.MODLE },

			{ 0x8, MyCmd.Keycode.SPEECH }, 
			{ 0x9, MyCmd.Keycode.BT }, };

	private final static byte KEYS_WHEEL2[][] = {

	{ 0x1, MyCmd.Keycode.HOME },
	{ 0x2, MyCmd.Keycode.NAVIGATION },
			{ 0x3, MyCmd.Keycode.AUDIO }, 
			{ 0x4, MyCmd.Keycode.NAVIGATION },
			{ 0x5, MyCmd.Keycode.BACK },
			{ 0x6, MyCmd.Keycode.SETUP },
			{ 0x7, MyCmd.Keycode.PLAY_PAUSE },
			
			{ 0x8, MyCmd.Keycode.ROLL_PREV },
			{ 0x9, MyCmd.Keycode.ROLL_NEXT },
			
			{ 0xa, MyCmd.Keycode.RADIO },
			{ 0xb, MyCmd.Keycode.AUDIO }, 
			{ 0xc, MyCmd.Keycode.NAVIGATION },
			{ 0xd, MyCmd.Keycode.BT },

			{ 0xe, MyCmd.Keycode.MODLE },
			{ 0xf, MyCmd.Keycode.KEY_SEEK_PREV },
			{ 0x10, MyCmd.Keycode.KEY_SEEK_NEXT }, 
			{ 0x11, MyCmd.Keycode.KEY_DISPLAY },
			{ 0x12, MyCmd.Keycode.SETUP },

			{ 0x14, MyCmd.Keycode.NEXT },
			{ 0x13, MyCmd.Keycode.PREVIOUS }, 
			{ 0x15, MyCmd.Keycode.POWER },
			{ 0x16, MyCmd.Keycode.VOLUME_ROLL_DOWN },
			{ 0x17, MyCmd.Keycode.VOLUME_ROLL_UP }, 			
			

			{ 0x18, MyCmd.Keycode.SPEECH }, 
			
	};


	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0) {
		} else if ((data & 0xff) == 0x1f) {
			data = (byte) 0xff;
		} else if (((data & 0xff) >= 0x1) && ((data & 0xff) <= 0x1e)) {
			data = (byte) (35 + (data & 0xff));
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{
		
		byte[]	airData = new byte[12];

		airData[0] = (byte) (((data[2] & 0x40) >> 4) 
				| ((data[2] & 0x10) << 0)
				| ((data[2] & 0x08) << 3)
				| ((data[2] & 0x04) << 3)
				| ((data[2] & 0x03) << 0));	
		
		airData[4] = (byte) (((data[2] & 0x20) >> 3));	
		
		switch (data[4] & 0xff) {
		case 0:
			airData[1] = (byte)0x40;		
			break;
		case 1:
			airData[1] = (byte)0x60;		
			break;
		case 2:
			airData[1] = (byte)0x20;		
			break;
		case 3:
			airData[1] = (byte)0xa0;		
			break;
		}
		airData[1] |= data[3]&0x7;
	
		airData[2] = data[5];
		airData[3] = data[6];	

		airData[0] |= 0x80;
		
		
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
	}

	public void setMediaSrc(int source) {
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

		byte m = (byte) curDate.getMinutes();

		int lang = 0;
		String locale = Locale.getDefault().getLanguage();
		if (locale != null) {
			if (locale.equals("en")) {
				lang = 2;
			} else if (locale.equals("zh")) {
				if ("CN".equals(Locale.getDefault().getCountry())) {
					lang = 0;
				} else {
					lang = 1;
				}
			}
		}

		byte[] buf = new byte[] { (byte) 0x82, 0x03, h, m, (byte) lang };
		sendDataToCanbox(buf, buf.length);
	}

	public int getOutTemp(byte[] data) {//
		short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
		return t;
	}

	public boolean isSupportCompass() {
		return true;
	}

	public void updateCompass(int compass) {
		int direction = compassAngleToDirect(compass);

		byte[] buf = new byte[] { (byte) (0xa7), 0x2,
				(byte) (direction & 0xff), (byte) compass };

		sendDataToCanbox(buf, buf.length);
	}

	private int mSpeed = 0;
	private int mSaft = 0;

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 0x41:
			mSpeed = data[2] & 0xff;
			if (mSpeed > 20 && ((SystemClock.uptimeMillis() - mLastWarning) > 30000)){
				doVoice();
			}
			break;
		case 0x24:
			if (mSaft != (data[3] & 0x18)) {
				mSaft = data[3] & 0x18;
				if (mSaft != 0) {
					doVoice();
				} else {
					clearVoice();
				}
			}
			break;
		}
		super.parseCanboxData(data, len);
	}
	
	private long mLastWarning = 0;

	private Handler mHandlerSaft = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0:
				doVoice();
				break;
			case 1:
				if (msg.arg1 > 0) {
					mHandlerSaft
							.sendMessageDelayed(mHandlerSaft.obtainMessage(1,
									msg.arg1 - 1, 0), 600);
				}
				Util.setFileValue("/sys/class/ak/source/beep", "2");
				break;
			}
			super.handleMessage(msg);
		}
	};

	public void stopConnect() {
		clearVoice();
		super.stopConnect();
	};
	
	@Override
	public void startConnect() {
		// TODO Auto-generated method stub
		super.startConnect();
		

		byte[] data = new byte[] { (byte) 0x90, 0x2, 0x24, 0 };
		sendDataToCanbox(data, data.length);
		
	}

	Toast mToast = null;
	private void clearVoice(){

		mHandlerSaft.removeMessages(0);
		mHandlerSaft.removeMessages(1);
		if (mToast != null){
			mToast.cancel();
			mToast = null;
		}
	}
	
	private void doVoice() {
		mHandlerSaft.removeMessages(0);
		mHandlerSaft.sendEmptyMessageDelayed(0, 30000);
		
		if (mContext != null) {
			if (mSpeed > 20 && mSaft != 0){
				mLastWarning = SystemClock.uptimeMillis(); 
						
//				String s = mContext.getResources().getString(R.string.saft_belt);
//				TextSpeaker.speakDirect(GlobalDef.getContext(), s);
				if (mToast != null){
					mToast.cancel();
					mToast = null;
				}
				if (mToast == null) {
					mToast = Toast.makeText(mContext, R.string.saft_belt,
							Toast.LENGTH_LONG);

					LayoutInflater inflater = (LayoutInflater) mContext
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(R.layout.saft_belt, null);
					// tv.setText(R.string.saft_belt);
					// tv.setCompoundDrawables(mContext.getDrawable(R.drawable.safebelt_icon),
					// null, null, null);
					mToast.setView(view);
				}
				mToast.show();		
				
				mHandlerSaft.removeMessages(1);
				mHandlerSaft.sendMessageDelayed(mHandlerSaft.obtainMessage(1, 4, 0), 500);
			} 
		}

	}

}
