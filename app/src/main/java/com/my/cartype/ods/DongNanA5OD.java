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

public class DongNanA5OD extends Canbox {

	public DongNanA5OD() {
		// buildCmdRepeatSendCarType(getCarTypeCmd());
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		// buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
		// buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
		// buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
		// buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
		// buildCmdOutTemp((byte) 0x41, (byte) 0x10);
		buildCmdVersion((byte) 0x30, (byte) 0x0);

		mIdAC = 0x21;
		mIdKey = 0x20;
		MAP_KEYS = KEYS_WHEEL;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
	}

	private final static byte IDS_TO_CANBOXSETTING[] = { 0x28, 0x27 };

	private final static byte KEYS_WHEEL[][] = {
			{ 0x1, MyCmd.Keycode.VOLUME_UP },
			{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x3, MyCmd.Keycode.NEXT },
			{ 0x4, MyCmd.Keycode.PREVIOUS },
			{ 0x5, MyCmd.Keycode.BT },
			
			{ 0x6, MyCmd.Keycode.MUTE },
			{ 0x7, MyCmd.Keycode.MODLE },
			{ 0x8, MyCmd.Keycode.SPEECH },
			{ 0xa, MyCmd.Keycode.NUMBER1 },
			{ 0xb, MyCmd.Keycode.NUMBER2 },
			{ 0xc, MyCmd.Keycode.NUMBER3 },
			{ 0xd, MyCmd.Keycode.NUMBER4 },
			{ 0xe, MyCmd.Keycode.NUMBER5 },
			{ 0xf, MyCmd.Keycode.NUMBER6 },
			{ 0x10, MyCmd.Keycode.POWER },
			{ 0x14, MyCmd.Keycode.VOLUME_ROLL_UP },
			{ 0x15, MyCmd.Keycode.VOLUME_DOWN },
			{ 0x29, MyCmd.Keycode.ROLL_PREV },
			{ 0x2a, MyCmd.Keycode.ROLL_NEXT },
			{ 0x2b, MyCmd.Keycode.PREVIOUS },
			{ 0x2c, MyCmd.Keycode.NEXT },
			{ 0x2d, MyCmd.Keycode.ROLL_PREV },
			{ 0x2e, MyCmd.Keycode.ROLL_NEXT },
			{ 0x2f, MyCmd.Keycode.PLAY_PAUSE },
			{ 0x30, MyCmd.Keycode.AUDIO },
			{ 0x31, MyCmd.Keycode.RADIO },
			{ 0x32, MyCmd.Keycode.HOME },
			{ 0x33, MyCmd.Keycode.AUX_IN },
			{ 0x34, MyCmd.Keycode.SETUP },
			{ 0x35, MyCmd.Keycode.BACK },
			{ 0x46, MyCmd.Keycode.KEY_REPEAT },
			{ 0x47, MyCmd.Keycode.KEY_SHUFFLE },
			{ 0x48, MyCmd.Keycode.KEYAMS_RPT },
			{ 0x49, MyCmd.Keycode.AS },
			{ 0x4a, MyCmd.Keycode.NAVIGATION },};


	@Override
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0) {
		} else if ((data & 0xff) == 0x1f) {
			data = (byte) 0xff;
		} else if (((data & 0xff) >= 0x1) && ((data & 0xff) <= 0x1e)) {
			data = (byte) (31 + (data & 0xff));
		}
		return data;
	}
	public void parseACInfo(byte[] data)
	{
		
		byte[]	airData = new byte[8];

		airData[0] = (byte) (((data[2] & 0xe3) << 0));	
		
		airData[4] = (byte) (((data[2] & 0x10) >> 2)
				| ((data[7] & 0x33) >> 0));	
		
//		airData[9] = (byte) (((data[7] & 0x44) >> 0));	
		
		airData[1] = data[3];
	
		airData[2] = data[5];
		airData[3] = data[6];	

		
		
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}

	public void setMediaSrc(int source, byte type, byte[] b) {
	}

	public void setMediaSrc(int source) {
	}

}
