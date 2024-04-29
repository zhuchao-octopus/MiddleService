package com.zhuchao.android.car.cartype.simple;

import android.os.Handler;
import android.os.Message;

import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.McuManager;


public class CarMazdaBT50Simple extends Canbox {

    public CarMazdaBT50Simple() {

        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });

    }

    private final static byte[][] mKeyPannel = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x3, MyCmd.Keycode.MULT_NEXT_AND_HANG}, {0x4, MyCmd.Keycode.MULT_PREV_AND_RECEIVE}, {0x7, MyCmd.Keycode.KEY_MIC},


            {0x20, MyCmd.Keycode.NUMBER0}, {0x21, MyCmd.Keycode.NUMBER1}, {0x22, MyCmd.Keycode.NUMBER2}, {0x23, MyCmd.Keycode.NUMBER3}, {0x24, MyCmd.Keycode.NUMBER4}, {0x25, MyCmd.Keycode.NUMBER5},
            {0x26, MyCmd.Keycode.NUMBER6}, {0x27, MyCmd.Keycode.NUMBER7}, {0x28, MyCmd.Keycode.NUMBER8}, {0x29, MyCmd.Keycode.NUMBER9}, {0x2a, MyCmd.Keycode.NUMBER_STAR},
            {0x2b, MyCmd.Keycode.NUMBER_POUND},


            {0x33, MyCmd.Keycode.RADIO}, {0x34, MyCmd.Keycode.RADIO}, {0x35, MyCmd.Keycode.DVD}, {0x36, MyCmd.Keycode.AUX_IN}, {0x37, MyCmd.Keycode.HOME}, {0x38, MyCmd.Keycode.EQ},
            {0x39, MyCmd.Keycode.BT}, {0x3D, MyCmd.Keycode.TIME_SETTING}, {0x3F, MyCmd.Keycode.POWER},


            {0x48, MyCmd.Keycode.PLAY_PAUSE}, {0x49, MyCmd.Keycode.PREVIOUS}, {0x4a, MyCmd.Keycode.NEXT}, {0x4b, MyCmd.Keycode.PREVIOUS}, {0x4c, MyCmd.Keycode.NEXT},

            {0x53, MyCmd.Keycode.MULT_NEXT_AND_HANG}, {0x52, MyCmd.Keycode.MULT_PREV_AND_RECEIVE},


            {0x54, MyCmd.Keycode.EJECT}, {0x56, MyCmd.Keycode.RDS_TA_SWITCH}, {0x57, MyCmd.Keycode.SETUP}, {0x59, MyCmd.Keycode.EQ}, {0x5a, MyCmd.Keycode.MUTE}, {0x5b, MyCmd.Keycode.DARK},


            {0x5c, MyCmd.Keycode.PREVIOUS}, {0x5e, MyCmd.Keycode.NEXT}, {0x5d, MyCmd.Keycode.PREVIOUS}, {0x5f, MyCmd.Keycode.NEXT},

            {0x6f, MyCmd.Keycode.AUDIO}, {(byte) 0x86, MyCmd.Keycode.PLAY_PAUSE}, {(byte) 0xf0, MyCmd.Keycode.VOLUME_UP}, {(byte) 0xf1, MyCmd.Keycode.VOLUME_DOWN},
            //		{(byte) 0xf2, MyCmd.Keycode.KEY_TURN_A },
            //		{(byte) 0xf3, MyCmd.Keycode.KEY_TURN_D },

    };

    private void parseWheelKey(byte[] data, int len) {

        if (doKeyStudy(data[2], data[3])) {
            return;
        }

        byte key = 0;
        if (mKeyPannel == null) {
            return;
        }
        for (int i = 0; i < mKeyPannel.length; ++i) {
            if (mKeyPannel[i][0] == data[2]) {
                key = mKeyPannel[i][1];
                //				if (CarUtil.getChangeKey() == 2){
                //					if (data[2] == 0x2){
                //						key = AK_KEYPAD_VOLUME_A;
                //					} else if (data[2] == 0x1){
                //						key = AK_KEYPAD_VOLUME_D;
                //					}
                //				}
                break;
            }
        }

        if (key != 0) {

            if (((data[2] & 0xff) == 0xf0) || ((data[2] & 0xff) == 0xf1)) {
                int step = data[3] & 0xff;
                if (step == 0) {
                    return;
                }
                // step = 0x10;
                if (step <= 8) {
                    doKeyStep(key, step);
                } else {
                    McuManager mcu = McuManager.getInstance();
                    if (mcu != null) {
                        int v = mcu.getMcuVolume();
                        switch (key) {
                            case AK_KEYPAD_VOLUME_A:
                                // if (v >= McuManager.MAX_VOLUME) {
                                // return;
                                // }
                                v += step;
                                break;
                            case AK_KEYPAD_VOLUME_D:
                                // if (v <= 0) {
                                // return;
                                // }
                                v -= step;
                                break;
                        }

                        if (v < 0) {
                            v = 0;
                        } else if (v > McuManager.MAX_VOLUME) {
                            v = McuManager.MAX_VOLUME;
                        }
                        mcu.setVolume(v);
                        Util.setFileValue("/sys/class/ak/source/beep", "2");
                    }
                }
            } else {
                doKey(key, data[3]);
            }
        } else {
            if (data[3] == 0) {
                doKey(0, 0);
            }
        }
    }

    //	private boolean isOneKey(byte b) {
    //		if (((b & 0xff) == 0xf0) || ((b & 0xff) == 0xf1)
    //				|| ((b & 0xff) == 0xf2) || ((b & 0xff) == 0xf3)) {
    //			return true;
    //		}
    //
    //		return false;
    //	}

    private final static int SHOW_VOLUME_STEP = 1;

    private void doKeyStep(int key, int step) {
        mHandler.removeMessages(SHOW_VOLUME_STEP);
        doKey(key, 1);
        doKey(key, 0);
        --step;
        if (step > 0) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW_VOLUME_STEP, key, step), 30);
        }
    }

    byte[] airData = new byte[8];

    private void parseACInfo(byte[] data, int len) {
        //		boolean acMaxUsed = true;
        if (len > 8 && (data[8] & 0x1) == 0) {//v 1.02
            data[6] &= ~0x4;
        }

        if (data[4] == 0x7f) {
            data[4] = (byte) 0xff;
        } else if (data[4] == 0x0) {
            data[4] = 0;
        } else if (data[4] < 0x1f || data[4] > 0x3b) {
            data[4] = (byte) 0xfa;
        }

        if (data[5] == 0x7f) {
            data[5] = (byte) 0xff;
        } else if (data[5] == 0x0) {
            data[5] = 0;
        } else if (data[5] < 0x1f || data[5] > 0x3b) {
            data[5] = (byte) 0xfa;
        }

        boolean outDoorTemp = false;
        boolean airControl = false;
        if (airData[0] != (byte) (data[2] & 0xff) || airData[1] != (byte) (data[3] & 0xff) || airData[2] != (byte) (data[4] & 0xff) || airData[3] != (byte) (data[5] & 0xff) || airData[4] != (byte) (data[6] & 0xff)) {
            airControl = true;
            airData[0] = (byte) (data[2] & 0xff);
            airData[1] = (byte) (data[3] & 0xff);
            airData[2] = (byte) (data[4] & 0xff);
            airData[3] = (byte) (data[5] & 0xff);
            airData[4] = (byte) (data[6] & 0xff);
        }
        if (airData[6] != (byte) (data[7] & 0xff)) {
            outDoorTemp = true;
            airData[6] = (byte) (data[7] & 0xff);

        }

        Handler handler = getHandler("CanService");
        if (null != handler) {
            if (airControl) {

                int msg = CANBOX_HIDE_AIR;
                if (/*(data[2] & 0x80) != 0 && */((data[3] & 0x10) != 0)) {
                    msg = CANBOX_RETURN_AIR;
                }

                handler.sendMessage(handler.obtainMessage(msg, airData));
            }
        }
        if (outDoorTemp) {
            // handler.sendMessage(handler.obtainMessage(CANBOX_OUT_DOOR_TEMP,
            // airData[6], 0));

            updateOutDoorTemp(airData[6]);

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
        String s;
        if (CarUtil.mTempUnit == 2) {
            temp = (int) ((temp) * 1.8f + 32);
            s = temp + mContext.getResources().getString(R.string.temp_unic_fahrenheit);
        } else {
            s = temp + mContext.getResources().getString(R.string.temp_unic_centigrade);
        }

        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);


    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKey(data, len);
            }
            break;
            case 0x21: {
                if (!MachineConfig.VALUE_SYSTEM_UI_KLD7_1992.equals(GlobalDefinition.mSystemUI)) {
                    parseACInfo(data, len);
                }
            }
            break;
            case 0x22: // Radar back
            {
                byteArrayCopy(mRadar, data, 0, 2, 4);

                boolean zero = Util.isZero(mRadar);
                if (!zero) {
                    RadarManager.start(mContext);
                    checkHideRadar();
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                }
            }
            break;
            case 0x24: {
                int door = (data[2]);
                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1) | ((door & 0x04) << 3));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }
            }
            case 0x30: {
                byte[] version = new byte[data.length - 2];
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
            switch (msg.what) {
                case HIDE_RADAR:
                    RadarManager.stop();
                    break;
                case SHOW_VOLUME_STEP:
                    doKeyStep(msg.arg1, msg.arg2);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private int mDoorStatus = 0;

    private int mLedStatus = 0;

    public void setCanboxLED(int type, int status) {
        int set = 0;
        //		status = status & 0x1;
        //		switch (type) {
        //		case CarUtil.CANBOX_LED_DISC:
        //			set = 0x1;
        //			break;
        //		case CarUtil.CANBOX_LED_PANNEL:
        //			set |= (0x1 << 1);
        //			set |= (0x1 << 2);
        //			break;
        //		case CarUtil.CANBOX_LED_ALL:
        //			set = 0x7;
        //			break;
        //		}
        //
        //		if (status == 0) {
        //			mLedStatus &= ~set;
        //		} else {
        //			mLedStatus |= set;
        //		}
        //
        //		byte[] buf = { (byte) 0xc6, 0x2, (byte) 0xa2, (byte) mLedStatus };
        //		sendDataToCanbox(buf, buf.length);

        status = status & 0x1;
        if (type == CarUtil.CANBOX_LED_ILL) {
            set = 0x7;
        }

        if (status == 0) {
            mLedStatus &= ~set;
        } else {
            mLedStatus |= set;
        }

        byte[] buf = {(byte) 0xc6, 0x2, (byte) 0xa2, (byte) mLedStatus};
        sendDataToCanbox(buf, buf.length);
    }

}
