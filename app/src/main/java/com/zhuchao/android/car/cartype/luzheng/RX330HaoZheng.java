package com.zhuchao.android.car.cartype.luzheng;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;


public class RX330HaoZheng extends Canbox {

    public RX330HaoZheng() {

        buildCmdRepeatSendCarType(getCarTypeCmd());
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0xe2, 0x01, 0};
        switch (CarUtil.getModelId()) {
            case 5:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x20;
                } else {
                    cmd[2] = 0x21;
                }
                break;
            case 7:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x30;
                } else {
                    cmd[2] = 0x31;
                }
                break;
            case 6:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x50;
                } else {
                    cmd[2] = 0x51;
                }
                break;
            case 9:
            case 36://this toyota
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x60;
                } else {
                    cmd[2] = 0x61;
                }
                break;
            case 8:
            case 80://this toyota
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x62;
                } else {
                    cmd[2] = 0x63;
                }
                break;
            case 12:
                if (CarUtil.getCarTypeConfig() == 0) {
                    cmd[2] = 0x64;
                } else {
                    cmd[2] = 0x65;
                }
                break;
            default:
                return null;
        }
        return cmd;
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D}, {0x14, KEY_NEXTSONG}, {0x13, KEY_PREVIOUSSONG}, {(byte) 0x87, KEY_MUTE}, {0x7, KEY_SOURCE}, {0x8, KEY_MIC}, {0x9, KEY_BT_DIAL},
            {0xA, KEY_BT_HANG},

    };

    private final static byte[][] KEYS_WHEEL2 = {

            {0x1, MyCmd.Keycode.PREVIOUS}, {0x6, MyCmd.Keycode.PREVIOUS}, {0x7, MyCmd.Keycode.PREVIOUS}, {0x8, MyCmd.Keycode.PREVIOUS}, {0x2, MyCmd.Keycode.NEXT}, {0x3, MyCmd.Keycode.NEXT},
            {0x4, MyCmd.Keycode.NEXT}, {0x5, MyCmd.Keycode.NEXT},

            {0x10, MyCmd.Keycode.KEY_TURN_A}, {0x11, MyCmd.Keycode.KEY_TURN_D}, {0x12, MyCmd.Keycode.PLAY_PAUSE}, {0x13, MyCmd.Keycode.BACK}, {0x14, MyCmd.Keycode.MENU}, {0x15, MyCmd.Keycode.AUDIO},
            {0x16, MyCmd.Keycode.NAVIGATION},

    };
    private final static byte[][] KEYS_WHEEL1 = {

            {0x1, MyCmd.Keycode.KEY_AM}, {0x2, MyCmd.Keycode.KEY_FM}, {0x3, MyCmd.Keycode.KEY_FM}, {0x4, MyCmd.Keycode.AUX_IN}, {0x5, MyCmd.Keycode.NAVIGATION}, {0x6, MyCmd.Keycode.NAVIGATION},
            {0x7, MyCmd.Keycode.MENU}, {0x8, MyCmd.Keycode.SETUP}, {0x9, MyCmd.Keycode.BRIGHTNESS}, {0x10, MyCmd.Keycode.AUDIO}, {0x11, MyCmd.Keycode.TIME_SETTING},

    };

    // private void parseWheelKey(byte[] data) {
    // parseWheelKeyEx(0, data);
    // }
    private void parseWheelKeyEx(int groundId, byte[][] keys, byte[] data) {

        if (doKeyStudy(groundId, data[2], data[3])) {
            return;
        }

        byte key = 0;
        for (int i = 0; i < keys.length; ++i) {
            if (keys[i][0] == data[2]) {
                key = keys[i][1];
                break;
            }
        }

        if (key != 0) {
            doKey(key, data[3]);
        } else {
            if (data[3] == 0) {
                doKey(0, 0);
            }
        }
    }

    private void parseACInfo(byte[] data, int len) {

        if (!isShowAir()) {
            sendCanboxInfo("com.canboxsetting", data);
            //	return;
        }

        if (data[4] >= 0x1f) {
            data[4] = (byte) 0xff;
        } else if (data[4] > 0) {
            data[4] = (byte) ((35 + (data[4] & 0xff)));
        }

        if (data[5] >= 0x1f) {
            data[5] = (byte) 0xff;
        } else if (data[5] > 0) {
            data[5] = (byte) ((35 + (data[5] & 0xff)));
        }

        byte[] airData = new byte[8];

        airData[0] = (byte) (data[2] & 0xef);
        airData[0] |= (byte) (((data[6] & 0x40) >> 6));

        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        // airData[4] = (byte) (data[7] & 0xff);
        airData[4] |= (byte) (((data[6] & 0x20) << 2) | ((data[6] & 0x8) >> 1) | ((data[2] & 0x1) << 3));

        airData[5] = (byte) ((data[6] & 0x1));

        airData[7] = (byte) (((data[6] & 0x10) >> 4));
        airData[7] |= (byte) (((data[6] & 0x80) >> 2));

        int msg = CANBOX_HIDE_AIR;
        if ((airData[0] & 0x80) != 0) {
            msg = CANBOX_RETURN_AIR;
        }

        Handler handler = getHandler("CanService");
        if (null != handler) {
            handler.sendMessage(handler.obtainMessage(msg, airData));
        }

        int t = ((data[7] & 0xff));
        t = (-400 + t * 5);
        mUnit = (data[6] & 0x1);
        updateOutDoorTemp(t);

    }

    //	private boolean isShowAir() {
    //		if ("com.canboxsetting/com.canboxsetting.CanAirControlActivity"
    //				.equals(AppConfig.getTopActivity())) {
    //			return false;
    //		}
    //		return true;
    //	}

    private byte getRadarData(byte i) {
        byte data = 0;
        switch (i) {
            case 0:
                data = 0;
                break;
            case 1:
                data = 1;
                break;
            case 2:
                data = 4;
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

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x20: {
                parseWheelKeyEx(0, KEYS_WHEEL, data);
            }

            break;
            case 0x64: {
                parseWheelKeyEx(1, KEYS_WHEEL1, data);
            }

            break;
            case 0x65: {
                parseWheelKeyEx(2, KEYS_WHEEL2, data);
            }

            break;

            case 0x28: {
                parseACInfo(data, len);
            }
            break;
            case 0x31: {
                parseEQ(data);
            }

            case 0x32: {
                //			if (CarUtil.getCarEQ() == 0) {
                //				if ((data[2] & 0x01) != 0) {
                //					if (!CarUtil.mIsNeedSendEQ) {
                //						CarUtil.mIsNeedSendEQ = true;
                //						CarUtil.setMcuEQZoneUsed(1);
                //						McuManager mcu = McuManager.getInstanse();
                //						if (mcu != null) {
                //							mcu.setAudio(0x6, 0x2);
                //						}
                //						startEQ();
                //					}
                //				} else {
                //					if (CarUtil.mIsNeedSendEQ) {
                //						CarUtil.mIsNeedSendEQ = false;
                //						CarUtil.setMcuEQZoneUsed(0);
                //						stopEQ();
                //					}
                //				}
                //			}
            }
            break;

            case 0x1e: // Radar back
            {
                mRadarSwitch = (byte) (data[6] & 0x80);

                mRadar[0] = getRadarData(data[2]);
                mRadar[1] = getRadarData(data[3]);
                mRadar[2] = getRadarData(data[4]);
                mRadar[3] = getRadarData(data[5]);
                if (mRadarSwitch != 0) {
                    boolean zero = Util.isZero(mRadar);
                    if (!zero) {
                        RadarManager.start(mContext);
                        checkHideRadarEx(2000);
                    }
                    Handler handler = getHandler(RadarManager.TAG);
                    if (null != handler) {
                        checkHideRadar();
                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                    }
                }
            }
            break;
            case 0x1d: // Radar back
            {

                mRadar[4] = getRadarData(data[2]);
                mRadar[5] = getRadarData(data[3]);
                mRadar[6] = getRadarData(data[4]);
                mRadar[7] = getRadarData(data[5]);
                if (mRadarSwitch != 0) {
                    boolean zero = Util.isZero(mRadar);
                    if (!zero) {
                        RadarManager.start(mContext);
                        checkHideRadarEx(2000);
                    }
                    Handler handler = getHandler(RadarManager.TAG);
                    if (null != handler) {
                        checkHideRadar();
                        handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                    }
                }
            }
            break;
            case 0x30: {
                byte[] version = new byte[16];
                Util.byteArrayCopy(version, data, 0, 2, version.length);

                mVersion = (new String(version));
                // version
                break;
            }
            case 0x24: {
                int door = (data[2] & 0xf8);
                door = (((door & 0x40) >> 6) | ((door & 0x80) >> 6) | ((door & 0x10) >> 2) | ((door & 0x20) >> 2) | ((door & 0x08) << 1));

                if (mDoorStatus != door) {
                    mDoorStatus = door;
                    Handler handler = getHandler("CanService");
                    if (null != handler) {
                        handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                    }
                }

            }
            break;

            case 0x29: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    // int angle = ((data[2] & 0xff) | ((data[3] & 0x07) << 8));
                    if ((data[3] & 0x08) != 0) {
                        // data[3] &= 0xf8;
                        data[3] |= 0xf0;
                    }
                    short angle = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));
                    angle = (short) (angle * 300 / 380);
                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));
                }
            }
            break;

            case 0x40:
                if (data[2] == (byte) 0xa0) {
                    if ((data[3] & 0x10) != 0) {
                        try {
                            Intent it = new Intent(Intent.ACTION_VIEW);
                            it.setClassName("com.canboxsetting", "com.canboxsetting.MainActivity");
                            it.putExtra(MyCmd.EXTRA_COMMON_CMD, 1);
                            it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(it);
                        } catch (Exception e) {
                            // Log.e(TAG, e.getMessage());
                        }
                    }
                }
                break;
            case 0x62:
            case 0x61:
            case 0x16:
            case 0x21:
            case 0x34:
            case 0x35:
            case 0x50:
                sendCanboxInfo("com.canboxsetting", data);
                break;
        }

    }

    private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
    private int mUnit = 0;

    @SuppressLint("DefaultLocale")
    public void updateOutDoorTemp(int temp) {

        if (temp == CarUtil.INVALID_OUT_DOOR_TEMP) {
            if (mTempOutDoor != CarUtil.INVALID_OUT_DOOR_TEMP) {
                temp = mTempOutDoor;
            } else {
                return;
            }
        }
        mTempOutDoor = temp;

        if (CarUtil.mTempUnit == 2) {
            if (mUnit != 1) {
                float t = temp / 10.0f;
                temp = (int) (((t) * 1.8f + 32) * 10);
            }
            mUnit = 1;
        } else if (CarUtil.mTempUnit == 1) {
            if (mUnit == 1) {
                temp = (int) ((((float) temp / 10.0f) - 32) / 1.8f) * 10;
            }
            mUnit = 0;
        }

        String s = "";
        // if (temp >= -58 && temp <= 171) {
        if (mUnit != 1) {

            s = String.format("%d.%d%s", temp / 10, (temp % 10) >= 0 ? (temp % 10) : -(temp % 10), mContext.getResources().getString(R.string.temp_unic_centigrade));

        } else {
            s = String.format("%d%s", temp / 10, mContext.getResources().getString(R.string.temp_unic_fahrenheit));
        }

        if (s.length() > 1) {
            GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);
        }

    }

    private byte mRadarSwitch = 0;
    private int mDoorStatus = 0;


    byte[] data;

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {


    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }

    public void sendId3(byte index, String num) {


    }


    public void setVolume(int volume) {


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

    //
    // public void setPhone(int status, String num) {// default is simple box
    // switch (status) {
    // case HFP_INFO_INITIAL:
    // case HFP_INFO_READY:
    // case HFP_INFO_CONNECTING:
    // case HFP_INFO_CONNECTED:
    // status = 0;
    // break;
    // case HFP_INFO_CALLED:
    // status = 2;
    // break;
    // case HFP_INFO_INCOMING:
    // status = 1;
    // break;
    // case HFP_INFO_CALLING:
    // status = 4;
    // break;
    // }
    // byte[] data = new byte[4];// {(byte)0xc5, 0x1, (byte)status};
    // data[0] = (byte) 0xc5;
    // data[1] = (byte) (2);
    // data[2] = (byte) 0;
    // data[3] = (byte) status;
    //
    // sendDataToCanbox(data, data.length);
    //
    // if (num == null) {
    // num = " ";
    // }
    // byte[] n = num.getBytes();
    // data = new byte[n.length + 4];// {(byte)0xc5, 0x1, (byte)status};
    // data[0] = (byte) 0xcA;
    // data[1] = (byte) (n.length + 2);
    // data[2] = (byte) 1;
    // data[3] = (byte) 3;
    // byteArrayCopy(data, n, 4, 0, n.length);
    // Util.doSleep(100);
    // sendDataToCanbox(data, data.length);
    //
    // }

    //	private void setEQVolume(int volume) {
    //
    //		byte[] data = new byte[] { (byte) 0x84, 0x2, 0x7, (byte) volume };
    //		sendDataToCanbox(data, data.length);
    //	}

    private void powerEQ(boolean power) {
        byte[] buf = new byte[4];
        buf[0] = (byte) 0x84;
        buf[1] = 0x2;
        buf[2] = 0x8;
        buf[3] = (byte) (power ? 1 : 0);

        sendDataToCanbox(buf, buf.length);
    }

    private final byte[] mBufEq = new byte[8];

    public void sendEqToCanbox(byte[] eq) {
        if (eq != null && eq.length >= 11) {

            byte[] bufEq = new byte[8];
            byte[] buf = new byte[4];
            buf[0] = (byte) 0x84;
            buf[1] = 0x2;
            bufEq[3] = eq[0];
            bufEq[4] = eq[1];

            bufEq[0] = (byte) (2 + ((eq[2] + eq[3] + eq[4]) * 11 / 60));

            bufEq[1] = (byte) (2 + ((eq[5] + eq[6] + eq[7]) * 11 / 60));
            bufEq[2] = (byte) (2 + ((eq[8] + eq[9] + eq[10]) * 11 / 60));

            if (mBufEq[0] != bufEq[0]) {
                buf[2] = 0x4;
                buf[3] = bufEq[0];
                sendDataToCanbox(buf, buf.length);
                Util.doSleep(5);
            }

            if (mBufEq[1] != bufEq[1]) {
                buf[2] = 0x6;
                buf[3] = bufEq[1];
                sendDataToCanbox(buf, buf.length);
                Util.doSleep(5);
            }

            if (mBufEq[2] != bufEq[2]) {
                buf[2] = 0x5;
                buf[3] = bufEq[2];
                sendDataToCanbox(buf, buf.length);
                Util.doSleep(5);
            }
            if (mBufEq[3] != bufEq[3]) {
                buf[2] = 0x1;
                buf[3] = bufEq[3];
                sendDataToCanbox(buf, buf.length);
                Util.doSleep(5);
            }
            if (mBufEq[4] != bufEq[4]) {
                buf[2] = 0x2;
                buf[3] = bufEq[4];
                sendDataToCanbox(buf, buf.length);
                Util.doSleep(5);
            }
            Util.byteArrayCopy(mBufEq, bufEq, 0, 0, bufEq.length);
        }
    }

    private void startEQ() {
        if (!CarUtil.mIsNeedSendEQ) {
            stopEQ();
            return;
        }

        powerEQ(true); // power on
        Util.doSleep(2);
        //		mVolume = MachineConfig.getIntProperty2(SystemConfig.CANBOX_EQ_VOLUME);
        //		if (mVolume == -1) {
        //			mVolume = 40;
        //		}
        //		setEQVolume(mVolume);
        // mHandler.removeMessages(REPEAT_SEND_EQ);
        // mHandler.sendEmptyMessageDelayed(REPEAT_SEND_EQ, 300);
    }

    private void stopEQ() {
        // mHandler.removeMessages(REPEAT_SEND_EQ);
        powerEQ(false);
    }

    public void startConnect() {
        super.startConnect();
        startEQ();
        if (CarUtil.getCarType() != 0) {
            byte[] buf = new byte[]{
                    (byte) 0xe2, 0x1, (byte) (CarUtil.getCarType())
            };
            sendDataToCanbox(buf, buf.length);
            Util.doSleep(2);
        }

        //		if (CarUtil.getCarType2() != 0) {
        //			byte[] buf = new byte[] { (byte) 0xe3, 0x1,
        //					(byte) (CarUtil.getCarType2() - 1) };
        //			sendDataToCanbox(buf, buf.length);
        //		}
        {
            Util.doSleep(2);
            byte[] buf = new byte[]{
                    (byte) 0xe3, 0x1, 0
            };
            sendDataToCanbox(buf, buf.length);
        }
    }

    private final int mVolume = 40;

    public void stopConnect() {
        stopEQ();
        super.stopConnect();
    }

    public void setContext(Context c) {
        super.setContext(c);
        //		if (CarUtil.getCarEQ() == 1) {
        //			CarUtil.mIsNeedSendEQ = true;
        //			CarUtil.setMcuEQZoneUsed(1);
        //		} else {
        //			CarUtil.setMcuEQZoneUsed(0);
        //		}

    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (63 << 16) | (15 << 8) | 15;

            byte[] buf = new byte[]{(byte) 0x90, 0x2, 0x31, 0};
            sendDataToCanbox(buf, buf.length);
        } else {
            byte[] buf = new byte[]{(byte) 0x84, 0x2, 0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    buf[2] = 6;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    buf[2] = 5;
                    break;
                case EQ_CMD_SET_LOW:
                    buf[2] = 4;
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
            //buf[3] = (byte) data ;
            sendDataToCanbox(buf, buf.length);
        }
        return ret;
    }

    private void parseEQ(byte[] buf) {
        if (mEQData == null) {
            mEQData = new byte[6];
        }
        mEQData[0] = (byte) (buf[3] & 0xf);
        mEQData[1] = (byte) ((buf[4] & 0xf0) >> 4);
        mEQData[2] = (byte) ((buf[3] & 0xf0) >> 4);
        mEQData[3] = (byte) ((buf[2] & 0xf0) >> 4);
        mEQData[4] = (byte) (buf[2] & 0xf);
        mEQData[5] = buf[5];
        super.returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
    }
}
