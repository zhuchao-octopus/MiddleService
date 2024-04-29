package com.zhuchao.android.car.cartype.bagoo;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class AudiBagoo extends Canbox {

    public AudiBagoo() {

        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x20;
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x2, MyCmd.Keycode.SPEECH}, {0x3, MyCmd.Keycode.MODLE}, {0x4, MyCmd.Keycode.VOLUME_UP}, {0x5, MyCmd.Keycode.VOLUME_DOWN}, {0x6, MyCmd.Keycode.MUTE}, {0x8, MyCmd.Keycode.PREVIOUS},
            {0x9, MyCmd.Keycode.NEXT}, {0xa, MyCmd.Keycode.PLAY_PAUSE},

    };


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
            data = new byte[]{
                    (byte) 0xc3, 0x6, (byte) (total & 0xFF), (byte) ((total >> 8) & 0xFF), (byte) (play & 0xFF), (byte) ((play >> 8) & 0xFF), min, sec
            };
        } else {
            data = new byte[]{
                    (byte) 0xc3, 0x6, (byte) (1 & 0xFF), (byte) ((play) & 0xFF), (byte) (total & 0xFF), (byte) ((0) & 0xFF), min, sec
            };
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
}
