package com.zhuchao.android.car.cartype.raise;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.RadarManager;


public class CarMazda extends Canbox {

    public CarMazda() {
        //		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x01, 0x1,
        //				0x3, 0x0, 0x0 });
        //		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] {  0x05, 0x02, 0x4,
        //				0x2, 0x2, 0x0 });

    }

    private void parseWheelKey(byte[] data, int len) {
        if (doKeyStudy(data[3], (data[3] == 0) ? 0 : 1)) {
            return;
        }

        switch (data[3]) {
            case 0x0:
                doKey(0, 0);
                break;
            case 0x2:
                doKey(MyCmd.Keycode.RADIO, 1);
                break;
            case 0x3:
                doKey(MyCmd.Keycode.HOME, 1);
                break;
            case 0x4:
                doKey(MyCmd.Keycode.AS, 1);
                break;
            case 0x5:
                doKey(MyCmd.Keycode.EQ, 1);
                break;
            case 0x7:
                doKey(MyCmd.Keycode.POWER, 1);
                break;
            case 0x17:
                doKey(MyCmd.Keycode.BT_DIAL, 1);
                break;
            case 0x18:
                doKey(MyCmd.Keycode.MULT_PREV_AND_RECEIVE, 1);
                break;
            case 0x14:
                doKey(AK_KEYPAD_VOLUME_A, 1); // vol+
                break;
            case 0x15:
                doKey(AK_KEYPAD_VOLUME_D, 1);// vol-
                break;
            case 0x13:
                doKey(KEY_PREVIOUSSONG, 1);
                break;
            case 0x12:
                doKey(KEY_NEXTSONG, 1);
                break;
            case 0x30:
                doKey(KEY_BT_DIAL, 1);
                break;
            case 0x31:
                doKey(KEY_BT_HANG, 1);
                break;
            case 0x16:
                doKey(AK_KEYPAD_MUTE_FAKE, 1);// mute
                break;
            case 0x11:
                doKey(KEY_MODE, 1);
                break;
        }
    }

    private void parseACInfo(byte[] data, int len) {

        if (len < 5) {
            return;
        }

        if ((data[3] & 0x80) != 0) {
            int temp = ((data[1] & 0xff) - 100) * 10;
            temp += ((data[4] & 0xff));
            updateOutDoorTemp(temp);
            return;
        }

        byte[] airData = new byte[8];
        if ((data[1] & 0xff) == 0xff) {
            airData[2] = (byte) 0xff;
        } else if ((data[1] & 0xff) == 0xfe) {
            airData[2] = (byte) 0xfa;
        } else if ((data[1] & 0xff) == 0) {
            airData[2] = 0;
        } else if ((data[1] & 0xff) >= 16 && (data[1] & 0xff) <= 32) {
            airData[2] = (byte) (((data[1] & 0xff) * 2) + ((data[4] == 0x05) ? 1 : 0));
        }
        //		else if ((data[1] & 0xff) >= 1 && (data[1] & 0xff) <= 7) {
        //			airData[2] = (byte) (((data[1] & 0xff) * 2) + ((data[4] == 0x05) ? 1
        //					: 0));
        //		}
        else {
            return;
        }


        airData[0] = (byte) (((data[2] & 0x80) >> 2) | ((data[2] & 0x04) << 1) | ((data[2] & 0x01) << 6));
        airData[0] |= 0x80;
        airData[1] = (byte) (((data[2] & 0x40) << 1) | ((data[2] & 0x10) << 1) | ((data[2] & 0x08) << 3));
        airData[1] |= (data[3] & 0x0F);

        airData[3] = airData[2];
        super.parseACInfo(airData);
        //		airData[4] = (byte) (data[6] & 0xff);
        //		Handler handler = getHandler("CanService");
        //		if (null != handler) {
        //			handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR,
        //					airData));
        //		}
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case (byte) 0xfc: {
                switch (data[2]) {
                    case 0x12:
                        parseWheelKey(data, len);
                        break;
                    case 0x32:
                        if (data[3] == 0) {
                            for (int i = 0; i < 4; ++i) {
                                switch (data[5 + i]) {
                                    case 0:
                                        mRadar[i] = 1;
                                        break;
                                    case 1:
                                        mRadar[i] = 6;
                                        break;
                                    case 2:
                                        mRadar[i] = 0xa;
                                        break;
                                    case 3:
                                        mRadar[i] = 0;
                                        break;
                                }
                            }
                        } else if (data[3] == 1) {
                            for (int i = 0; i < 4; ++i) {
                                if (data[5 + i] == 0xff || data[5 + i] >= 15) {
                                    mRadar[i] = 0;
                                } else {
                                    mRadar[i] = data[5 + i];
                                }
                            }
                        }

                        boolean zero = Util.isZero(mRadar);
                        if (!zero) {
                            RadarManager.start(mContext);
                            checkHideRadar();
                        }
                        Handler handler = getHandler(RadarManager.TAG);
                        if (null != handler) {
                            handler.sendMessage(handler.obtainMessage(CANBOX_RADAR_FRONT));
                        }

                        break;
                }

            }
            break;
            case (byte) 0xfd: {
                if (data.length == 5) {
                    parseACInfo(data, len);
                } else if (data[1] == 0x9 && data.length > 6) {
                    returnEQData(data);
                }
            }
        }
    }

    private final byte mDoorStatus = 0;

    public void startConnect() {//default is simple box

    }

    public void stopConnect() {//default is simple box
    }

    private byte Sum(byte[] data, int len) {
        byte sum = 0;

        for (int i = 0; i < len; ++i) {
            sum += data[i];
        }
        return (byte) (sum & 0xFF);
    }

    public void sendDataToCanbox(byte[] data, int len) {
        byte[] send = new byte[len + 3];
        send[0] = (byte) (len + 2);
        send[1] = (byte) 0xfd;
        send[len + 2] = Sum(data, len);
        byteArrayCopy(send, data, 2, 0, len);
        sendCommonDataToCanbox(send);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte[] data = new byte[]{
                0xf, 0x07, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20
        };

        int seconds = time % 60;
        int minutes = (time / 60) % 60;
        int hours = time / 3600;
        switch (source) {
            case MyCmd.SOURCE_DVD: {
                data[2] = 'D';
                data[3] = 'V';
                data[4] = 'D';

                data[6] = (byte) ('0' + (hours % 10));
                data[7] = ':';
                data[8] = (byte) ('0' + (minutes / 10));
                data[9] = (byte) ('0' + (minutes % 10));
                data[10] = ':';
                data[11] = (byte) ('0' + (seconds / 10));
                data[12] = (byte) ('0' + (seconds % 10));


                break;
            }
            case MyCmd.SOURCE_MUSIC: {
                data[2] = 'M';
                data[3] = 'U';
                data[4] = 'S';
                data[5] = 'I';
                data[6] = 'C';

                //			if ((play/100) != 0){
                //				data[8] = (byte)('0'+(play/100));
                //			}
                //			if ((play/10) != 0){
                //				data[9] = (byte)('0'+((play%100)/10));
                //			}
                //			data[10] = (byte)('0'+(play%10));

                data[8] = (byte) ('0' + (minutes / 10));
                data[9] = (byte) ('0' + (minutes % 10));
                data[10] = ':';
                data[11] = (byte) ('0' + (seconds / 10));
                data[12] = (byte) ('0' + (seconds % 10));

                break;
            }
            case MyCmd.SOURCE_VIDEO: {
                data[2] = 'V';
                data[3] = 'D';
                data[4] = 'O';

                data[6] = (byte) ('0' + (hours % 10));
                data[7] = ':';
                data[8] = (byte) ('0' + (minutes / 10));
                data[9] = (byte) ('0' + (minutes % 10));
                data[10] = ':';
                data[11] = (byte) ('0' + (seconds / 10));
                data[12] = (byte) ('0' + (seconds % 10));
                break;
            }
            case MyCmd.SOURCE_IPOD: {
                data[2] = 'i';
                data[3] = 'P';
                data[4] = 'o';
                data[5] = 'd';
                break;
            }
        }


        sendDataToCanbox(data, (byte) data.length);

    }

    public void setMediaSrc(int source) {//default is simple box
        byte[] data = new byte[]{
                0xf, 0x07, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20
        };


        switch (source) {
            case MyCmd.SOURCE_DVD: {
                data[2] = 'D';
                data[3] = 'V';
                data[4] = 'D';
                break;
            }
            case MyCmd.SOURCE_MUSIC: {
                data[2] = 'M';
                data[3] = 'U';
                data[4] = 'S';
                data[5] = 'I';
                data[6] = 'C';
                break;
            }
            case MyCmd.SOURCE_VIDEO: {
                data[2] = 'V';
                data[3] = 'I';
                data[4] = 'D';
                data[5] = 'E';
                data[6] = 'O';
                break;
            }
            case MyCmd.SOURCE_IPOD: {
                data[2] = 'i';
                data[3] = 'P';
                data[4] = 'o';
                data[5] = 'd';
                break;
            }
            case MyCmd.SOURCE_DTV: {
                data[2] = 'D';
                data[3] = 'T';
                data[4] = 'V';
                break;
            }
            case MyCmd.SOURCE_AUX: {
                data[2] = 'A';
                data[3] = 'U';
                data[4] = 'X';
                break;
            }
            case MyCmd.SOURCE_BT: {
                data[2] = 'A';
                data[3] = '2';
                data[4] = 'D';
                data[4] = 'P';
                break;
            }
        }
        sendDataToCanbox(data, (byte) data.length);
    }


    public void setMediaSrcASC(byte[] b) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {//default is simple box
        byte[] data = null;

        int freq = (b[1] & 0xff) | ((b[2] & 0xff) << 8);
        Log.e("", freq + ":" + b[1] + ":" + b[2] + ":");
        if (source == MyCmd.SOURCE_RADIO) {
            data = new byte[]{0xf, 0x07, 0x46, 0x4d, 0x31, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};

            if (b[0] == 16) { //am
                data[2] = 'A';
                //	data[3] = 'M';
                data[4] = 0x20;

                data[6] = (byte) ('0' + (freq / 1000));
                if (data[6] == '0') {
                    data[6] = 0x20;
                }

                data[7] = (byte) ('0' + ((freq % 1000) / 100));
                data[8] = (byte) ('0' + ((freq % 100) / 10));
                data[9] = (byte) ('0' + ((freq % 10)));

                data[11] = 'K';
                data[12] = 'H';
                data[13] = 'z';
            } else {
                data[2] = 'F';
                //	data[3] = 'M';
                if (b[0] == 0) {
                    data[4] = '1';
                } else if (b[0] == 1) {
                    data[4] = '2';
                } else if (b[0] == 2) {
                    data[4] = '3';
                }
                if ((freq / 10000) != 0) {
                    data[6] = (byte) ('0' + (freq / 10000));
                }
                data[7] = (byte) ('0' + ((freq % 10000) / 1000));
                data[8] = (byte) ('0' + ((freq % 1000) / 100));
                data[9] = 0x2e;
                data[10] = (byte) ('0' + ((freq % 100) / 10));
                data[11] = 'M';
                data[12] = 'H';
                data[13] = 'z';
            }
        }
        ////		byte []data = new byte[]{0xf, 0x07, 0x46, 0x4d, 0x31, 0x20, 0x31, 0x30, 0x35, 0x2e,
        ////				0x32, 0x4d, 0x48,
        ////				0x7a};
        if (data != null) {
            sendDataToCanbox(data, (byte) data.length);
        }
    }

    private void checkHideRadar() {
        mHandler.removeMessages(HIDE_RADAR);
        // if (mRadarSwitch != 1) {
        // int i;
        // for (i = 0; i < mRadar.length; ++i) {
        // if(mRadar[i]!=0){
        // break;
        // }
        // }
        // if (i >= mRadar.length) {
        mHandler.sendEmptyMessageDelayed(HIDE_RADAR, 5000);
        // }

        // }
    }

    private final static int HIDE_RADAR = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_RADAR) {
                RadarManager.stop();
            }
        }
    };

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (63 << 16) | (17 << 8) | 13;

            byte[] buf = new byte[]{(byte) 0x90, 0x2, 0x51, 0};
            sendDataToCanbox(buf, buf.length);
        } else {
            byte[] buf = new byte[]{0x5, (byte) 0x84, 0x0, (byte) data};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    data += 0xa;
                    buf[2] = 5;
                    break;
                case EQ_CMD_SET_LOW:
                    data += 0xa;
                    buf[2] = 4;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    data += 0x8;
                    buf[2] = 1;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    data += 0x8;
                    buf[2] = 2;
                    break;
                case EQ_CMD_SET_VOLUME:
                    buf[2] = 7;
                    break;
                default:
                    return 0;
            }
            buf[3] = (byte) data;
            sendDataToCanbox(buf, buf.length);
        }
        return ret;
    }

    private void returnEQData(byte[] buf) {
        byte[] data = new byte[6];

        data[0] = (byte) (buf[6] & 0xff);

        data[2] = (byte) (buf[5] & 0xff);
        data[3] = (byte) (buf[3] & 0xff);
        data[4] = (byte) (buf[4] & 0xff);

        data[5] = buf[7];

        data[0] -= 0xa;
        data[2] -= 0xa;
        data[3] -= 8;
        data[4] -= 8;

        super.returnEQData(EQ_CMD_SET_ALL_DATA, data);
    }
}
