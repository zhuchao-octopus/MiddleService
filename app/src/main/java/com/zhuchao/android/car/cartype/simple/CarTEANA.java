package com.zhuchao.android.car.cartype.simple;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class CarTEANA extends Canbox {
    public CarTEANA() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    private void parseWheelKey(byte[] data, int len) {
        if (doKeyStudy(data[2], data[3])) {
            return;
        }

        switch (data[2]) {
            case 0x00: {
                doKey(0, 0);
            }
            break;
            case 0x1:
                doKey(AK_KEYPAD_VOLUME_A, data[3]); // vol+
                break;
            case 0x2:
                doKey(AK_KEYPAD_VOLUME_D, data[3]);// vol-
                break;
            case 0x03: {
                doKey(KEY_PREVIOUSSONG, data[3]);
            }
            break;
            case 0x04: {
                doKey(KEY_NEXTSONG, data[3]);
            }
            break;
            case 0x07: {
                doKey(KEY_MODE, data[3]);
            }
            break;
            case 0x09: {
                doKey(KEY_BT_DIAL, data[3]);
            }
            break;
            case 0x0A: {
                doKey(KEY_BT_HANG, data[3]);
            }
            break;
            case 0x15: {
                doKey(KEY_BACK, data[3]);//return
            }
            break;
            case 0x16: {
                doKey(KEY_HOMEPAGE, data[3]); //enter
            }
            break;
        }
    }

    public void setMediaSrc(int source) {
        byte s;
        byte mediaType = 0;
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                s = 1;
                mediaType = 1;
                break;
            case MyCmd.SOURCE_DVD:
                s = 2;
                mediaType = 0x10;
                break;
            case MyCmd.SOURCE_BT:
                s = 0xb;
                mediaType = 0x0;
                break;
            case MyCmd.SOURCE_DTV:
                s = 3;
                mediaType = 0x0;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 6;
                mediaType = 0x0;
                break;

            case MyCmd.SOURCE_AUX:
                s = 7;
                mediaType = 0x0;
                break;
            case MyCmd.SOURCE_MUSIC:
                s = 8;
                mediaType = 0x10;
                break;
            case MyCmd.SOURCE_VIDEO:
                s = 8;
                mediaType = 0x0;
                break;
            default:
                s = 0x0c;
                mediaType = 0x30;
                break;
        }
        byte[] data = new byte[]{(byte) 0xc0, 0x8, s, mediaType, 0, 0, 0, 0, 0, 0};
        sendDataToCanbox(data, data.length);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
        byte s;
        byte mediaType = 0;
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                s = 1;
                mediaType = 1;
                break;
            case MyCmd.SOURCE_DVD:
                s = 2;
                mediaType = 0x10;
                break;
            case MyCmd.SOURCE_DTV:
                s = 3;
                mediaType = 0x13;
                break;
            case MyCmd.SOURCE_IPOD:
                s = 6;
                mediaType = 0x0;
                break;

            case MyCmd.SOURCE_AUX:
                s = 7;
                mediaType = 0x0;
                break;

            case MyCmd.SOURCE_MUSIC:
                s = 8;
                mediaType = 0x13;
                break;
            case MyCmd.SOURCE_VIDEO:
                s = 8;
                mediaType = 0x13;
                break;
            default:
                s = 0x0c;
                mediaType = 0x30;
                break;
        }
        byte[] data = new byte[]{
                (byte) 0xc0, 0x8, s, mediaType, (byte) (play & 0xff), (byte) ((play >> 8) & 0xff), 0, (byte) (time / 3600), (byte) (time / 60), (byte) (time % 60)
        };
        sendDataToCanbox(data, data.length);
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
        byte[] data = new byte[]{(byte) 0xc0, 0x8, 1, type, b[0], b[1], b[2], b[3], b[4], b[5]};
        sendDataToCanbox(data, data.length);
    }

    public void setVolume(int volume) {
        byte[] data = new byte[]{(byte) 0xc4, 0x1, (byte) volume};
        sendDataToCanbox(data, data.length);
    }

    public void setPhone(int status, String num) {
        byte[] n = num.getBytes();
        byte[] data = new byte[n.length + 3];//{(byte)0xc5, 0x1, (byte)status};
        data[0] = (byte) 0xc5;
        data[1] = (byte) (n.length + 1);
        data[2] = (byte) status;
        byteArrayCopy(data, n, 3, 0, n.length);

        sendDataToCanbox(data, data.length);
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == 0x20) {
            parseWheelKey(data, len);
        }
    }

}
