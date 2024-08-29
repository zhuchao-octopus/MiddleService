package com.zhuchao.android.car.cartype.binarytek;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;
import com.zhuchao.android.car.cartype.CarUtil;


public class VWBNR extends Canbox {

    private final static int HIDE_RADAR = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_RADAR) {
                RadarManager.stop();
            }
            super.handleMessage(msg);
        }
    };
    byte[] mAirData = new byte[8];
    private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;
    private byte mDoorStatus = 0;

    public VWBNR() {
        //		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x01, 0x2,
        //				0x3, 0x0, 0x0 });
        //		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x02, 0x0,
        //				0x0, 0x0, 0x1 });
    }

    private void parseWheelKey(byte[] data, int len) {
        if (doKeyStudy(data[2], data[3])) {
            return;
        }
        switch (data[2]) {
            case 0x0:
                doKey(0, 0);
                break;
            case 0x1:
                doKey(AK_KEYPAD_VOLUME_A, data[3]); //vol+
                break;
            case 0x2:
                doKey(AK_KEYPAD_VOLUME_D, data[3]);//vol-
                break;
            case 0x3:
                doKey(KEY_NEXTSONG, data[3]);
                break;
            case 0x4:
                doKey(KEY_PREVIOUSSONG, data[3]);
                break;
            case 0x5:
                doKey(KEY_BT, data[3]);
                break;
            case 0x6:
                doKey(AK_KEYPAD_MUTE_FAKE, data[3]);//mute
                break;
            case 0x7:
                doKey(KEY_MODE, data[3]);
                break;
            case 0x8:
                doKey(KEY_MIC, data[3]);
                break;
            case 0x9:
                doKey(MyCmd.Keycode.BT_DIAL, data[3]);
                break;
            case 0xa:
                doKey(MyCmd.Keycode.BT_HANG, data[3]);
                break;
            case 0xb:
                doKey(KEY_MIC, data[3]);
                break;
        }
    }

    private void parseACInfo(byte[] data, int len) {
        byte[] airData = new byte[8];
        airData[7] = (byte) (((data[7] & 0x80) >> 1));

        if (data[4] >= 0x1f) {
            data[4] = (byte) 0xff;
        } else if (data[4] > 0) {
            if ((airData[7] & 0x40) == 0) {
                data[4] = (byte) ((17.5f + (0.5f * data[4])) * 2);
            }
        }
        if (data[5] >= 0x1f) {
            data[5] = (byte) 0xff;
        } else if (data[5] > 0) {
            if ((airData[7] & 0x40) == 0) {
                data[5] = (byte) ((17.5f + (0.5f * data[5])) * 2);
            } else {
                data[5] = data[4];
            }
        }


        airData[0] = (byte) ((data[2] & 0xfe) | ((data[6] & 0x40) >> 6));

        airData[1] = (byte) (data[3] & 0xff);
        airData[2] = (byte) (data[4] & 0xff);
        airData[3] = (byte) (data[5] & 0xff);

        airData[4] = (byte) ((data[6] & 0xf7) | ((data[2] & 0x01) << 3));


        boolean airControl = false;
        if ((data[2] & 0x80) != 0) {
            if (!Util.isBufEquals(airData, mAirData)) {
                Util.byteArrayCopy(mAirData, airData, 0, 0, airData.length);
                airControl = true;
            }
        }

        Handler handler = getHandler("CanService");
        if (airControl && null != handler) {
            handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
        }
    }

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 2000);
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
                parseACInfo(data, len);
            }
            break;
            case 0x22: // Radar back
            {
                byteArrayCopy(mRadar, data, 0, 2, 4);

                if (!Util.isZero(mRadar)) {
                    RadarManager.start(mContext);
                    checkHideRadar();
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_BACK));
                }
            }
            break;
            case 0x23: // Radar front
            {
                byteArrayCopy(mRadar, data, 4, 2, 4);
                if (!Util.isZero(mRadar)) {
                    RadarManager.start(mContext);
                    checkHideRadar();
                }
                Handler handler = getHandler(RadarManager.TAG);
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                }
            }
            break;
            case 0x25: // Radar status
            {
                byte[] status = new byte[2];
                status[0] = data[2];
                status[1] = data[3];

                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_STATUS, status));
                }
            }
            break;
            case 0x26: {
                Handler handler = getHandler("Reverse");
                if (null != handler) {
                    short a = (short) ((data[2] & 0xff) | ((data[3] & 0xff) << 8));// bu


                    //				int angle = (a * 300 / 10000); //old pro is wrong?
                    int angle = (a * 300 / 540);

                    handler.sendMessage(handler.obtainMessage(CANBOX_STEER_ANGLE, angle, 10));
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
            case 0x27: {
                //no need auto now!!
                //			if(data[2]==2||data[2]==3){
                //				if (!CarUtil.mIsNeedSendEQ) {
                //					CarUtil.mIsNeedSendEQ = true;
                //					McuManager mcu = McuManager.getInstanse();
                //					if (mcu != null) {
                //						mcu.setAudio(0x6, 0x2);
                //						mcu.setAudio(0x6, 0x4);
                //					}
                //				}
                //			}else{
                //				CarUtil.mIsNeedSendEQ=false;
                //			}

                returnEQData(data);
            }
            break;
            case 0x41: {
                switch (data[2]) {
                    case 1: {
                        if (mDoorStatus != (byte) (data[3] & 0x1F)) {
                            mDoorStatus = (byte) (data[3] & 0x1F);
                            Handler handler = getHandler("CanService");
                            if (null != handler) {
                                handler.sendMessage(handler.obtainMessage(CANBOX_DOOR_STATUS, mDoorStatus, 0));

                            }
                        }
                    }
                    break;
                    case 2:
                        if (data.length > 0xd) {
                            int i = (short) (((data[9] & 0xff) << 8) | (data[10] & 0xff));

                            updateOutDoorTemp(i);

                        }
                        break;
                }
            }
            sendCanboxInfo("com.canboxsetting", data);
            break;
        }


        returnDriveData(data);
    }

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
        String s;
        if (CarUtil.mTempUnit == 2) {
            float t = temp / 10.0f;
            temp = (int) (((t) * 1.8f + 32) * 10);
            s = temp + mContext.getResources().getString(R.string.temp_unic_fahrenheit);
        } else {
            s = String.format("%d.%01d °C", temp / 10, temp % 10);
        }

        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);


    }

    public void setReverseRadaVol(byte param) {
        byte[] data = new byte[]{(byte) 0xc6, 0x2, 0x0, param};
        sendDataToCanbox(data, data.length);
    }

    public void setParkCarMode(byte param) {
        byte[] data = new byte[]{(byte) 0xc6, 0x2, 0x2, param};
        sendDataToCanbox(data, data.length);
    }

    public void requestInfo(byte param) {
        byte[] data = new byte[]{(byte) 0x90, 0x2, param, 0};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte min = (byte) ((time / 60) % 60);
        byte sec = (byte) ((time) % 60);
        //		++play;
        byte[] data;

        if (MyCmd.SOURCE_DVD != source) {
            data = new byte[]{(byte) 0xc3, 0x6, (byte) (total & 0xFF), (byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF), (byte) ((play >> 8) & 0xFF), min, sec};
        } else {
            data = new byte[]{(byte) 0xc3, 0x6, (byte) (1 & 0xFF), (byte) ((play) & 0xFF), (byte) (total & 0xFF), (byte) ((0) & 0xFF), min, sec};
        }
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        setMediaSrc(0);
        if (b[0] != 0x10) {
            b[0] += 1;
        }
        byte[] data = new byte[]{(byte) 0xc2, 0x4, b[0], b[1], b[2], 0};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source) {//default is simple box
        byte s = 0;
        byte mediaType = 0;
        switch (source) {
            case 0:
                s = 1;
                mediaType = 1;
                break;
            case 1:
                s = 2;
                mediaType = 0x10;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 6;
                mediaType = 0x12;
                break;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 0x09;
                mediaType = 0x11;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x07;
                mediaType = 0x30;
                byte[] data2 = new byte[]{(byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0};
                sendDataToCanbox(data2, data2.length);
                break;
            case MyCmd.SOURCE_DTV:
                s = 0x0A;
                mediaType = 0x30;
                byte[] data3 = new byte[]{(byte) 0xc3, 0x6, 0, 0, 0, 0, 0, 0};
                sendDataToCanbox(data3, data3.length);
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0b;
                mediaType = 0x30;
                break;
            default:
                s = 0x00;
                mediaType = 0x0;
                break;
        }
        byte[] data;
        if (s == 0xb || s == 0x7) {
            data = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0};
        } else {
            data = new byte[]{(byte) 0xc0, 0x2, s, mediaType};
        }

        sendDataToCanbox(data, data.length);
    }

    public void setVolume(int volume) {

        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);

        if (CarUtil.mIsNeedSendEQ) {

            Util.doSleep(1);
            byte[] buf = new byte[4];
            buf[0] = (byte) 0xa0;
            buf[1] = 0x2;
            buf[2] = 0x0;
            buf[3] = (byte) volume;

            sendDataToCanbox(buf, buf.length);
        }
    }

    public boolean requestAngleData() {
        byte[] data3 = new byte[]{(byte) 0x90, 0x2, 0x26, 0};
        sendDataToCanbox(data3, data3.length);
        return true;
    }

    public void sendEqToCanbox(byte[] eq) {
        //Log.d("abcd", "sendEqToCanbox");
        if (eq != null && eq.length >= 11) {
            byte[] buf = new byte[4];
            int i;
            buf[0] = (byte) 0xa0;
            buf[1] = 0x2;


            buf[2] = 0x2;
            buf[3] = eq[0];
            sendDataToCanbox(buf, buf.length);
            Util.doSleep(1);

            buf[2] = 0x1;
            buf[3] = eq[1];
            sendDataToCanbox(buf, buf.length);
            Util.doSleep(1);

            buf[2] = 0x2;
            buf[3] = eq[0];
            sendDataToCanbox(buf, buf.length);
            Util.doSleep(1);

            buf[2] = 0x3;
            buf[3] = (byte) (((eq[2] + eq[3] + eq[4]) / 3) - 10);
            if (buf[3] < -9) {
                buf[3] = -9;
            }
            if (buf[3] > 9) {
                buf[3] = 9;
            }
            sendDataToCanbox(buf, buf.length);
            Util.doSleep(1);

            buf[2] = 0x4;
            buf[3] = (byte) (((eq[8] + eq[9] + eq[10]) / 3) - 10);
            if (buf[3] < -9) {
                buf[3] = -9;
            }
            if (buf[3] > 9) {
                buf[3] = 9;
            }
            sendDataToCanbox(buf, buf.length);
            Util.doSleep(1);

            buf[2] = 0x5;
            buf[3] = (byte) (((eq[5] + eq[6] + eq[7]) / 3) - 10);
            if (buf[3] < -9) {
                buf[3] = -9;
            }
            if (buf[3] > 9) {
                buf[3] = 9;
            }
            sendDataToCanbox(buf, buf.length);
            Util.doSleep(1);

        }
    }

    private void returnDriveData(byte[] buf) {
        if (mRequestDriveData > 0) {
            boolean update = true;
            switch (buf[0]) {
                case 0x41: {
                    switch (buf[2]) {
                        case 0x1:
                            mDriveData[11] &= ~0x3;
                            if ((buf[3] & 0x80) > 0) {
                                mDriveData[11] |= 2;
                            } else {
                                mDriveData[11] |= 1;
                            }
                            if ((buf[3] & 0x20) > 0) {
                                mDriveData[10] |= 1;
                            } else {
                                mDriveData[10] &= ~1;
                            }
                            break;
                        case 0x2:
                            int speed = ((buf[6] & 0xff) | ((buf[5] & 0xff) << 8)) / 100;
                            mDriveData[1] = (byte) (speed & 0xff);
                            mDriveData[2] = (byte) ((speed & 0xff00) >> 8);


                            mDriveData[3] = buf[4];
                            mDriveData[4] = buf[3];
                            mDriveData[5] = buf[13];
                            mDriveData[6] = buf[12];
                            mDriveData[7] = buf[11];
                            mDriveData[12] = buf[14];

                            speed = ((mDriveData[5] & 0xff) | ((mDriveData[6] & 0xff) << 8) | ((mDriveData[7] & 0xff) << 8));
                            speed *= 10;

                            mDriveData[5] = (byte) (speed & 0xff);
                            mDriveData[6] = (byte) ((speed & 0xff00) >> 8);
                            mDriveData[7] = (byte) ((speed & 0xff0000) >> 16);
                            break;
                    }
                }
                break;
                case 0x24:
                    if ((buf[2] & 0x2) == 0) {
                        mDriveData[10] = 3;
                    } else {
                        if ((buf[2] & 0x1) != 0) {
                            mDriveData[10] = 1;
                        } else {
                            mDriveData[10] = 4;
                        }
                    }

                    if ((buf[2] & 0x4) != 0) {
                        mDriveData[14] = 1;
                    } else {
                        mDriveData[14] = 0;
                    }
                    break;
                default:
                    update = false;
                    break;
            }
            if (update) {
                returnDriveData();
            }
        }
    }

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (19 << 8) | 19;

            byte[] buf = new byte[]{(byte) 0x90, 0x2, 0x51, 0};
            sendDataToCanbox(buf, buf.length);
        } else {
            byte[] buf = new byte[]{(byte) 0xa0, 0x2, 0x0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    buf[2] = 5;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    buf[2] = 4;
                    break;
                case EQ_CMD_SET_LOW:
                    buf[2] = 3;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    buf[2] = 2;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    buf[2] = 1;
                    break;
                default:
                    return 0;
            }

            sendDataToCanbox(buf, buf.length);
        }
        return ret;
    }

    private void returnEQData(byte[] buf) {
        byte[] data = new byte[5];
        data[0] = buf[8];
        data[1] = buf[7];
        data[2] = buf[6];
        data[3] = buf[5];
        data[4] = buf[4];
        super.returnEQData(EQ_CMD_SET_ALL_DATA, data);
    }


    public boolean isSupportCompass() {
        return true;
    }

    public void updateCompass(int compass) {
        int direction = compassAngleToDirect16(compass);

        byte[] buf = new byte[]{(byte) (0xa2), 0x2, (byte) 0x80, (byte) (direction & 0xff)};

        sendDataToCanbox(buf, buf.length);
    }
}
