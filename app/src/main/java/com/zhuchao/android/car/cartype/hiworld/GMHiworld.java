package com.zhuchao.android.car.cartype.hiworld;

import java.util.Locale;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.common.util.AppConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;


public class GMHiworld extends Canbox{

	public GMHiworld(){
		buildCmdRepeatSendCarType(getCarTypeCmd(), 5);
		buildCmdDoor((byte) 0x12, (byte) 0x2, (byte) 0xfc, (byte) 0x4);
		buildCmdRadarFront((byte) 0x41, (byte) 0x0, (byte) 0xff);
		buildCmdRadarFrontEx((byte) 4);
		buildCmdRadarBack((byte) 0x41, (byte) 0x0, (byte) 0xff);
		buildCmdAngle((byte) 0x11, (byte) 0x0, 540);
//		buildCmdEQ((byte) 0xa6, (byte) 0x1, 6);
//		buildCmdOutTemp((byte) 0x31, (byte) 0x10);
		buildCmdVersion((byte) 0xf0, (byte) 0x0);
		mIdAC = 0x31;
		mIdKey = 0x21;
		MAP_KEYS = KEYS_WHEEL;
		buildCmdKey((byte)0x11, (byte)2, (byte)4, (byte)2, KEYS_WHEEL2);
		mIdKey3 = 0x021022;
		MAP_KEYS3 = KEYS_WHEEL3;
		IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;		
	}


	@Override
	public void stopConnect() {

	}
	private final static byte[] IDS_TO_CANBOXSETTING = { 0x32, 0x34, 0x35, 0x45,
			0x55, 0x65, 0x66, 0x67, (byte) 0x85, (byte) 0x90, (byte) 0xb1,
			(byte) 0xb2, (byte) 0xb3, (byte) 0xb4, (byte) 0xbc, (byte) 0xbd,
			(byte) 0xc2, (byte) 0xc3, 0x46, 0x56, 0x68, 0x69, 0x75 , 0x10 , 0x12 };
	
	private final static byte[][] KEYS_WHEEL = {
		{ 0x1, MyCmd.Keycode.POWER },
		{ 0x2, MyCmd.Keycode.PREVIOUS },
		{ 0x3, MyCmd.Keycode.NEXT },
		{ 0x4, MyCmd.Keycode.SETUP },
		{ 0x5, MyCmd.Keycode.EQ },
		{ 0x6, MyCmd.Keycode.BACK },
		{ 0x7, MyCmd.Keycode.RADIO },
		{ 0x8, MyCmd.Keycode.DVD },
		{ 0x9, MyCmd.Keycode.MUTE },
		{ 0xa, MyCmd.Keycode.NUMBER1 },
		{ 0xb, MyCmd.Keycode.NUMBER2 },
		{ 0xc, MyCmd.Keycode.NUMBER3 },
		{ 0xd, MyCmd.Keycode.NUMBER4 },
		{ 0xe, MyCmd.Keycode.NUMBER5 },
		{ 0xf, MyCmd.Keycode.NUMBER6 },
		{ 0x10, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x11, MyCmd.Keycode.EJECT },
		{ 0x12, MyCmd.Keycode.SETUP },
		{ 0x13, MyCmd.Keycode.TIME_SETTING },
		{ 0x14, MyCmd.Keycode.NAVIGATION },
		{ 0x15, MyCmd.Keycode.AS },
		{ 0x16, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x17, MyCmd.Keycode.PREVIOUS },
		{ 0x18, MyCmd.Keycode.NEXT },
		{ 0x19, MyCmd.Keycode.PREVIOUS },
		{ 0x1a, MyCmd.Keycode.NEXT },
		{ 0x1b, MyCmd.Keycode.PREVIOUS },
		{ 0x1c, MyCmd.Keycode.NEXT },
		{ 0x1d, MyCmd.Keycode.PREVIOUS },
		{ 0x1e, MyCmd.Keycode.NEXT },
		{ 0x1f, MyCmd.Keycode.AUX_IN },
		{ 0x20, MyCmd.Keycode.NAVIGATION },
		{ 0x21, MyCmd.Keycode.NAVIGATION },
//		{ 0x22, MyCmd.Keycode. },
		{ 0x23, MyCmd.Keycode.SPEECH },
		{ 0x24, MyCmd.Keycode.AUDIO },
		{ 0x25, MyCmd.Keycode.NAVIGATION },
		{ 0x26, MyCmd.Keycode.AUDIO },
		{ 0x27, MyCmd.Keycode.DVD },
		{ 0x28, MyCmd.Keycode.BT },
//		{ 0x29, MyCmd.Keycode.t },
		{ 0x2a, MyCmd.Keycode.PLAY_PAUSE },
		{ 0x2b, MyCmd.Keycode.HOME },
		{ 0x2c, MyCmd.Keycode.MODLE },
		{ 0x2d, MyCmd.Keycode.MENU },
	};
	
	private final static byte[][] KEYS_WHEEL2 = {
		{ 0x1, MyCmd.Keycode.VOLUME_UP },
		{ 0x2, MyCmd.Keycode.VOLUME_DOWN },
		{ 0x3, MyCmd.Keycode.MUTE },
		{ 0x4, MyCmd.Keycode.SPEECH },
		{ 0x5, MyCmd.Keycode.BT_DIAL },
		{ 0x6, MyCmd.Keycode.BT_HANG  },
		{ 0x9, MyCmd.Keycode.NEXT },
		{ 0x8, MyCmd.Keycode.PREVIOUS },
		{ 0xa, MyCmd.Keycode.MODLE },
	};
	
	private final static byte[][] KEYS_WHEEL3 = {
		{ 0x1, MyCmd.Keycode.VOLUME_ROLL_UP, 0 },
		{ 0x11, MyCmd.Keycode.VOLUME_ROLL_DOWN, 0 },
		{ 0x2, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x12, MyCmd.Keycode.ROLL_PREV , 0},
		{ 0x3, MyCmd.Keycode.ROLL_NEXT , 0},
		{ 0x13, MyCmd.Keycode.ROLL_PREV , 0},
	};
	
	public void parseCanboxData(byte[] data, int len) {
		switch (data[0]) {
		case 0x22:
			if (data[3] == 0) {
				return;
			} else if (data[3] < 0) {
				data[3] = (byte) (-data[3]); 
				data[2] += 0x10;
			}
			parseWheelKey(mIdKey3, data, MAP_KEYS3);
			break;
			
		case (byte) 0xb1:
			int delay = 0;
			switch (data[2] & 0xff) {
			case 1:
			case 2:
			case 3:
				data[2]++;
				break;
			case 4:
				data[2] = 1;
				break;
			}
			if (data[2] != data0x9) {
				if (data[2] == 0x1 || data[2] == 0x2 || data[2] == 0x3 || data[2] == 0x4) {
					String top = AppConfig.getTopActivity();

					Intent it = new Intent(Intent.ACTION_VIEW);
					boolean topIsCamera = top != null && top.contains("com.canboxsetting.AnStartActivity");

					if (!topIsCamera) {
						try {
							it.setClassName("com.canboxsetting",
									"com.canboxsetting.AnStartActivity");
							it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
									| Intent.FLAG_ACTIVITY_NEW_TASK);

							mContext.startActivity(it);
							delay = 800;
						} catch (Exception e) {
							// Log.e(TAG, e.getMessage())
						}
					}
				}
			}
			data0x9 = data[2];
			if (delay == 0){
				sendCanboxInfo("com.canboxsetting", data);
			} else {
				mHandlerRepeat.sendMessageDelayed(mHandlerRepeat.obtainMessage(DELAY_SEND_ANSTART, data), delay);
			}
			break;
		default:
			super.parseCanboxData(data, len);
		}	
	}
	private byte data0x9;
	private byte[] getCarTypeCmd() {
		byte[] cmd = new byte[] { (byte) 0x2, 0x2a, 0, 0 };
		switch (CarUtil.getModelId()) {
		case 0:
		case 2:
		case 29:
		case 4:
		case 7:
		case 12:
			cmd[2] = 1;
			break;
		case 18:
		case 9:
		case 19:
			cmd[2] = 2;
			break;
		case 3:
			cmd[2] = 5;
			break;
		case 13:
		case 20:
		case 31:
			cmd[2] = 6;
			break;
		case 22:
			cmd[2] = 7;
			break;
		case 26:
			cmd[2] = 8;
			break;
		case 48:
			cmd[2] = 9;
			break;
		case 56: //test 21 yinglang
			return null;
		}
		return cmd;
	}
	@Override
	public int getAngleValue2(byte[] data) {

		int angle = (short) ((data[9] & 0xff) | (((data[8] & 0xff)) << 8));

		
		return -angle;
		
		
	}
	
	public int getACTemp(byte data) {
		// TODO Auto-generated method stub
		if ((data & 0xff) == 0xfe) {
			data = 0;
		}  else if ((data & 0xff) == 0xff) {
			//data = 0;
		} else {
			
		}
		return data&0xff;
	}	

	public void parseACInfo(byte[] data)
	{		
		byte[]	airData = new byte[12];
		airData[0] = (byte) (
				((data[2] & 0x10) << 0)
				| ((data[2] & 0x40) << 1)
				| ((data[2] & 0x01) << 6)
				| ((data[3] & 0x10) << 1)
				| ((data[4] & 0x10) >> 3)
				| ((data[4] & 0x20) >> 5));				
		
		if ((data[2] & 0x3) == 1) {
			airData[0] |= 0x40;
		}
		

		airData[5] = (byte) (
				((data[3] & 0x20) >> 0));	
		
		if (((data[2] & 0x0c)>>2) == 1) {
			airData[7] |= 0x80;
		}
		
		airData[4] = (byte) (
				 ((data[3] & 0x08) << 4)
				);	


		
		
		
		

		switch ((data[6] & 0xf)) {
		case 1:
			airData[9] = (byte) (0x1);
			break;
		case 2:
			airData[1] = (byte) (0x80);
			break;
		case 3:
			airData[1] = (byte) (0x20);
			break;
		case 5:
			airData[1] = (byte) (0x60);
			break;
		case 6:
			airData[1] = (byte) (0x40);
			break;
		case 0xb:
			airData[1] = (byte) (0x80);
			break;
		case 0xc:
			airData[1] = (byte) (0xa0);
			break;
		case 0xd:
			airData[1] = (byte) (0xc0);
			break;
		case 0xe:
			airData[1] = (byte) (0xe0);
			break;
		}
		
		if ((data[7] & 0xff) >= 0 && (data[7] & 0xff) <= 8) {
			airData[1] |= (byte) (((data[7] & 0xff) >> 0));
		}		


		airData[2] = data[8];
		airData[3] = data[9];
		
		if ((data[3] & 0x40) == 0) {
			airData[4] |= (byte) (
					 ((data[4] & 0x03) << 4)
					);	
		} else {
			airData[8] = (byte) (
					 ((data[4] & 0x03) << 4)
					);	
		}
		
		if ((data[3] & 0x80) == 0) {
			airData[4] |= (byte) (
					 ((data[4] & 0x0c) >> 2)
					);	
		} else {
			airData[8] |= (byte) (
					 ((data[4] & 0x0c) << 0)
					);	
		}
		
		
		switch ((data[10] & 0xf)) {
		case 1:
			airData[11] = (byte) (0x10);
			break;
		case 2:
			airData[11] = (byte) (0x20);
			break;
		case 3:
			airData[11] = (byte) (0x60);
			break;
		case 4:
			airData[11] = (byte) (0x40);
			break;
		}

		airData[11] |= (byte) (data[11] & 0x0f);
		

		

		airData[10] = (byte)getACTemp(data[12]);
		

		airData[5] |= 0x80;
		
		super.parseACInfo(airData);
	}	
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
//		switch (source) {
//		case MyCmd.SOURCE_MUSIC:
//		case MyCmd.SOURCE_VIDEO:
//			++play;
//			break;
//		}
		String s = String.format("%02d:%02d:%02d", (time) / 3600, (time) / 60, (time) % 60, Locale.ENGLISH);
		sendLcdInfo((byte)0xd, s, false);
	}
	public void setMediaSrc(int source, byte type, byte []b){
		int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
		if (source == MyCmd.SOURCE_RADIO) {
			String s;
			if (b[0] >= 0x10) { // am
				s = freq + " KHz";
				type = 4;
			} else {

				s = String.format("%d.%d MHz", (freq) / 100, (freq / 10) % 10, Locale.ENGLISH);
				type = 1;
			}
			sendLcdInfo(type, s, true);
		}
	}

	public void setMediaSrc(int source) {
		byte s;
		switch (source) {
//		case MyCmd.SOURCE_RADIO:
//			return;
		case MyCmd.SOURCE_MUSIC:
		case MyCmd.SOURCE_VIDEO:
			s = 0xd;
			break;
		case MyCmd.SOURCE_BT:
			s = (byte)0x85;
			break;
		default:
			return;
		}

		sendLcdInfo(s, null, false);
	}		

	@Override
	public void setVolume(int volume) {
//		String s = volume + "";
//		sendLcdInfo((byte) 0x20, s, false);
	}

	public void sendLcdInfo(byte index, String num, boolean end) {

		try {
			if (num == null) {
				num = "";
			}
			byte[] n = num.getBytes();

			int num_len = n.length;

			if (num_len >= (12)) {
				num_len = (12);
			}
			byte[] data;

			int len = 12 + 4;

			data = new byte[len];

			data[0] = (byte) (14);
			data[1] = (byte) 0x91;
			data[2] = index;
			data[3] = (byte) 0;
//			if (!end) {
			System.arraycopy(n, 0, data, 4, num_len);
//			} else {
//				for (int i = 0; i < num_len; ++i) {
//					data[data.length - i - 1] = n[num_len - i - 1];
//				}
//			}

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

		}
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

			if (num_len >= (0x20)) {
				num_len = (0x20);
			}
			byte[] data;

			int len = 0x22;

			data = new byte[len];

			data[0] = (byte) (0x20);
			data[1] = index;
			//				if (i % 2 == 0) {
			//					data[2 + i] = n[i + 3];
			//				} else {
			//					data[2 + i] = n[i + 1];
			//				}
			System.arraycopy(n, 2, data, 2, num_len);

			sendDataToCanbox(data, data.length);
		} catch (Exception e) {

		}
	}
	
//	public void setSongName(String s) {
//		sendLcdInfo((byte)0xd, s, false);
//	}

//	public void setSongAritst(String s) {
//		sendId3((byte) 0x93, s);
//	}
//
//	public void setSongAlbum(String s) {
//		sendId3((byte) 0x94, s);
//	}
	public int getOutTemp(byte[] data) {//		
		return ((data[13])*5 - 400);
	}
	public void sendDataToCanbox(byte[] data, int len) { // default is simple
		super.sendDataToCanboxHiword1(data, len);
	}

	private final static int DELAY_SEND_ANSTART = 1;
	private final Handler mHandlerRepeat = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				sendEQCmd(msg.arg1, msg.arg2);
				break;
				case DELAY_SEND_ANSTART:
					sendCanboxInfo("com.canboxsetting", (byte[])msg.obj);
					break;
			}
			super.handleMessage(msg);
		}
	};
	byte[] mEQCmdBuf = new byte[] { 0x2, (byte) 0xad, 0x0, 0x0 };

	private void sendEQCmd(int style, int step) {
		mHandlerRepeat.removeMessages(0);
		if (step == 0) {
			return;
		}
		
		if (style == 1) {
			if (step < 0) {
				mEQCmdBuf[3] = -1;
				++step;
			} else {
				mEQCmdBuf[3] = 1;
				--step;
			}
		} else {
			if (step > 0) {
				--step;
				mEQCmdBuf[3]++;
			} else {
				++step;
				if (mEQCmdBuf[3]>0){
					mEQCmdBuf[3]--;
				} else {
					return;
				}
			}
		}
		
		sendDataToCanbox(mEQCmdBuf, mEQCmdBuf.length);
		if (step != 0) {
			mHandlerRepeat.sendMessageDelayed(mHandlerRepeat.obtainMessage(0, style, step), 100);
		}
		
	}
	
	public int doEQCmd(int cmd, int data) {
		int ret = 0;
		if (cmd == EQ_REQUEST_ALL_MAX) {
			ret = (63 << 16) | (15 << 8) | 11;

			byte[] buf = new byte[] { 0x3, (byte) 0x6a, 0x5, (byte) 0xa6, 0 };
			sendDataToCanbox(buf, buf.length);
		} else {
			mHandlerRepeat.removeMessages(0);
			if (mEQData == null){
				mEQData = new byte[6];
			}
			
			int step = 0;
			int style = 0;
			switch (cmd) {
			case EQ_CMD_SET_HIGH:
				mEQCmdBuf[2] = 6;
				mEQCmdBuf[3] = mEQData[0];
				step = data - mEQData[0];
				break;
			case EQ_CMD_SET_MIDDLE:
				mEQCmdBuf[2] = 5;
				mEQCmdBuf[3] = mEQData[1];
				step = data - mEQData[1];
				break;
			case EQ_CMD_SET_LOW:
				mEQCmdBuf[2] = 4;
				mEQCmdBuf[3] = mEQData[2];
				step = data - mEQData[2];
				break;
			case EQ_CMD_SET_ZONE_FR:
				mEQCmdBuf[2] = 3;
				mEQCmdBuf[3] = mEQData[3];
				step = data - mEQData[3];
				break;
			case EQ_CMD_SET_ZONE_LR:
				mEQCmdBuf[2] = 2;
				mEQCmdBuf[3] = mEQData[4];
				step = data - mEQData[4];
				break;
			case EQ_CMD_SET_VOLUME:
				mEQCmdBuf[2] = 1;
				step = data - mEQData[5];
				style = 1;
				break;
			default:
				return 0;
			}

			sendEQCmd( style, step);
		}
		return ret;
	}

	public void startConnect() {
		
	}
	
//	private void returnEQData(byte[] buf) {
//		byte[] data = new byte[6];
//
//		data[0] = (byte) (buf[3] & 0x0f);
//		data[1] = (byte) ((buf[4] & 0xf0) >> 4);
//		data[2] = (byte) ((buf[3] & 0xf0) >> 4);
//		data[3] = (byte) ((buf[2] & 0xf0) >> 4);
//		data[4] = (byte) (buf[2] & 0x0f);
//
//		data[5] = buf[5];
//
//		data[0] -= 2;
//		data[1] -= 2;
//		data[2] -= 2;
//		
//		super.returnEQData(EQ_CMD_SET_ALL_DATA, data);
//	}
}
