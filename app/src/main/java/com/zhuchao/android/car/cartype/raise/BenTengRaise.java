package com.zhuchao.android.car.cartype.raise;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class BenTengRaise extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x52};
    private final static byte[][] KEYS_WHEEL = {{0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x4, KEY_NEXTSONG}, {0x3, KEY_PREVIOUSSONG},

            {0x6, KEY_MUTE}, {0x7, KEY_SOURCE},

            {0x9, MyCmd.Keycode.BT_DIAL}, {0xa, MyCmd.Keycode.BT_HANG},

    };
    private final byte[][] KEYS_WHEEL2 = {{0x1, MyCmd.Keycode.POWER}, {0x2, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x3, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x4, MyCmd.Keycode.MUTE}, {0x5, MyCmd.Keycode.AS}, {0x6, MyCmd.Keycode.KEY_SEEK_PREV}, {0x7, MyCmd.Keycode.KEY_SEEK_NEXT}, {0x8, MyCmd.Keycode.PLAY_PAUSE}, {0x9, MyCmd.Keycode.KEY_TURN_A}, {0xa, MyCmd.Keycode.KEY_TURN_D}, {0xb, MyCmd.Keycode.NUMBER1}, {0xc, MyCmd.Keycode.NUMBER2}, {0xd, MyCmd.Keycode.NUMBER3}, {0xe, MyCmd.Keycode.NUMBER4}, {0xf, MyCmd.Keycode.NUMBER5}, {0x10, MyCmd.Keycode.NUMBER6}, {0x11, MyCmd.Keycode.PREVIOUS}, {0x12, MyCmd.Keycode.NEXT}, {0x13, MyCmd.Keycode.RADIO}, {0x14, MyCmd.Keycode.AUDIO}, {0x15, MyCmd.Keycode.KEY_SHUFFLE}, {0x16, MyCmd.Keycode.KEY_REPEAT}, {0x17, MyCmd.Keycode.EASY_CONNECT}, {0x18, MyCmd.Keycode.AUDIO}, {0x19, MyCmd.Keycode.SPEECH}, {0x1a, MyCmd.Keycode.BT}, {0x1b, MyCmd.Keycode.KEY_RADIO_SCAN}, {0x1c, MyCmd.Keycode.BACKLIGHT_OFF}, {0x1d, MyCmd.Keycode.BT_DIAL}, {0x1e, MyCmd.Keycode.BT_HANG}, {0x1f, MyCmd.Keycode.BACK}, {0x20, MyCmd.Keycode.HOME}, {0x21, MyCmd.Keycode.EASY_CONNECT}, {0x22, MyCmd.Keycode.AUDIO},

    };
    private final int mPhoneStatus = HFP_INFO_INITIAL;
    byte[] mLcdInfo = new byte[]{(byte) 0xc0, 0x08, 0, 0, 0, 0, 0, 0, 0, 0};
    private int mCallingTime;


    public BenTengRaise() {
        mIdAC = 0x21;
        buildCmdDoor((byte) 0x28, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
        buildCmdRadarFront((byte) 0x23, (byte) 0x1, (byte) 0x4);
        buildCmdRadarBack((byte) 0x22, (byte) 0x1, (byte) 0x4);
        buildCmdAngle((byte) 0x30, (byte) 0x0, 540);
        buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdOutTemp((byte) 0x28, (byte) 0x10);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
        initPannelKey();
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private void initPannelKey() {
        mIdKey2 = 0x24;
        if (CarUtil.getModelId() == 9) {
            if (CarUtil.getCarTypeConfig() == 2) {
                for (int i = 0; i < KEYS_WHEEL.length; ++i) {
                    if (KEYS_WHEEL2[i][0] == 0x5) {
                        KEYS_WHEEL2[i][1] = MyCmd.Keycode.BACK;
                    } else if (KEYS_WHEEL2[i][0] == 0x11) {
                        KEYS_WHEEL2[i][1] = MyCmd.Keycode.BT_DIAL;
                    } else if (KEYS_WHEEL2[i][0] == 0x12) {
                        KEYS_WHEEL2[i][1] = MyCmd.Keycode.BT_HANG;
                    } else if (KEYS_WHEEL2[i][0] == 0x14) {
                        KEYS_WHEEL2[i][1] = MyCmd.Keycode.BT;
                    } else if (KEYS_WHEEL2[i][0] == 0x15) {
                        KEYS_WHEEL2[i][1] = MyCmd.Keycode.MODLE;
                    } else if (KEYS_WHEEL2[i][0] == 0x16) {
                        KEYS_WHEEL2[i][1] = MyCmd.Keycode.SPEECH;
                    }
                }
            }
        }
        MAP_KEYS2 = KEYS_WHEEL2;
    }

    public int getAngleValue(byte[] data) {

        int angle;
        int max;

        angle = ((data[3] & 0xff) | (((data[2] & 0x7f)) << 8));
        if ((data[2] & 0x80) != 0) {
            angle = -angle;
        }
        max = 5400;

        angle = ((angle * 3000) / max);
        if (angle > -50 && angle < 50) {
            angle = 50;
        }

        return angle;
    }

    private int getACTempPriv(byte data) {
        if ((data & 0xff) == 0xf1) {
            data = (byte) 0;
        } else if ((data & 0xff) == 0xf2) {
            data = (byte) 0xff;
        } else if ((data & 0x7f) >= 0 && (data & 0x7f) <= 28) {
            data = (byte) (36 + ((data & 0x7f)));
        }
        return data & 0xff;
    }

    public void parseACInfo(byte[] data) {
        byte[] airData = new byte[8];
        airData[0] = (byte) (((data[2] & 0x4c)) | ((data[2] & 0x10) << 1) | ((data[2] & 0x02) >> 1));
        airData[0] |= 0x80;


        switch ((data[3] & 0xff)) {
            case 1:
                airData[1] = (byte) (0x40);
                break;
            case 2:
                airData[1] = (byte) (0x60);
                break;
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 4:
                airData[1] = (byte) (0xa0);
                break;
            case 5:
                airData[1] = (byte) (0x80);
                break;
            default:
                airData[1] = 0;
                break;
        }
        airData[1] |= (byte) (data[4] & 0x0f);


        if ((data[5] & 0x80) == 0 || (data[6] & 0x80) == 0) {
            airData[7] = 0x40;
            airData[2] = data[5];
            airData[3] = data[6];
        } else {
            airData[2] = (byte) getACTempPriv(data[5]);
            airData[3] = (byte) getACTempPriv(data[6]);
        }

        if ((data[4] & 0x0f) == 0) {
            //			Util.zeroBuf(airData);
        }
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        mLcdInfo[2] = 1;

        if (b[0] >= 0x10) { // am
            mLcdInfo[3] = 0x10;
        } else {
            mLcdInfo[3] = 0;
        }

        sendDataToCanbox(mLcdInfo, mLcdInfo.length);
    }

    public void setMediaSrc(int source) {// default is simple box
        byte s = -1;
        switch (source) {
            case MyCmd.SOURCE_BT:
                s = 0xb;
                break;
            case MyCmd.SOURCE_AV_OFF:
                s = 0;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x08;
                break;
        }
        if (s != -1) {
            mLcdInfo[2] = s;
            mLcdInfo[3] = 0;
            sendDataToCanbox(mLcdInfo, mLcdInfo.length);
        }
    }

    public void setPhone(int status, String num) {// default is simple box


        //		mPhoneStatus = status;
        //		byte s = 0;
        //		switch (status) {
        //		case HFP_INFO_INITIAL:
        //		case HFP_INFO_READY:
        //		case HFP_INFO_CONNECTING:
        //		case HFP_INFO_CONNECTED:
        //			s = 0;
        //			break;
        //		case HFP_INFO_CALLED:
        //			s = 2;
        //			break;
        //		case HFP_INFO_INCOMING:
        //			s = 1;
        //			break;
        //		case HFP_INFO_CALLING:
        //			s = 4;
        //			break;
        //		}
        //
        //		byte[] data2 = new byte[] { (byte) 0xc5, 0x2, 0, s };
        //		sendDataToCanbox(data2, data2.length);
        //
        //		if (num == null) {
        //			num = " ";
        //		}
        //
        //		byte[] n = num.getBytes();
        //		int num_len = n.length;
        //		if (num_len > 31) {
        //			num_len = 31;
        //		}
        //
        //		data2 = new byte[num_len + 8];
        //		data2[0] = (byte) 0xca;
        //		data2[1] = (byte) (data2.length - 2);
        //		data2[2] = 5;
        //		data2[3] = (byte) s;
        //		data2[4] = (byte) ((mCallingTime & 0xff00) >> 8);
        //		data2[5] = (byte) (mCallingTime & 0xff);
        //		data2[6] = (byte) 1;
        //		data2[7] = (byte) num_len;
        //
        //		byteArrayCopy(data2, n, 8, 0, num_len);
        //
        //		sendDataToCanbox(data2, data2.length);


    }
}
