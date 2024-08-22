package com.zhuchao.android.car.cartype.raise;

import android.provider.Settings;
import android.util.Log;

import com.common.utils.MyCmd;
import com.common.utils.SettingProperties;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;


public class JiLiRaise extends Canbox {

    public JiLiRaise() {
        mIdAC = 0x23;
        buildCmdRepeatSendCarType(getCarTypeCmd());
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x2);
        buildCmdRadarBack((byte) 0x25, (byte) 0x0, (byte) 7);
        buildCmdRadarFront((byte) 0x26, (byte) 0x0, (byte) 7);
        buildCmdAngle((byte) 0x30, (byte) 0x3, 0x1545);
        buildCmdOutTemp((byte) 0x28, (byte) 0x1);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdKey = 0x20;
        MAP_KEYS = KEYS_WHEEL;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
        setVoiceSupportRaise();
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x27, 0x52, 0x4e, 0x4f, 0x50};


    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{(byte) 0xee, 0x02, 0x70, 0};
        switch (CarUtil.getModelId()) {
            case 16:
            case 17:
                cmd[3] = 0;
                break;
            case 4:
            case 5:
                cmd[3] = 1;
                break;
            case 1:
                cmd[3] = 2;
                break;
            case 34:
                cmd[3] = 3;
                break;
            case 14:
                cmd[3] = 4;
                break;
            case 9:
                cmd[3] = 5;
                break;
            case 13:
                cmd[3] = 6;
                break;
            case 15:
            case 31:
                cmd[3] = 7;
                break;
            case 18:
            case 36:
                cmd[3] = 8;
                break;
            case 21:
                cmd[3] = 9;
                break;
            case 19:
                cmd[3] = 0xa;
                break;
            case 20:
                cmd[3] = 0xb;
                break;
            case 22:
                cmd[3] = 0xc;
                break;
            case 23:
            case 26:
                cmd[3] = 0xd;
                break;
            case 24:
                cmd[3] = 0xe;
                break;
            case 27:
                cmd[3] = 0xf;
                break;
            case 29:
            case 39:
                cmd[3] = 0x10;
                break;
            case 30:
                cmd[3] = 0x11;
                break;
            case 3:
            case 10:
            case 33:
                cmd[3] = 0x12;
                break;
            case 28:
            case 38:
                cmd[3] = 0x13;
                break;
            case 37:
                cmd[3] = 0x14;
                break;
            case 40:
                cmd[3] = 0x15;
                break;
            default:
                return null;
        }
        return cmd;
    }

    private final static byte[][] KEYS_WHEEL = {
            {0x1, AK_KEYPAD_VOLUME_A}, {0x2, AK_KEYPAD_VOLUME_D},

            {0x3, KEY_NEXTSONG}, {0x4, KEY_PREVIOUSSONG},

            {0x6, MyCmd.Keycode.MUTE}, {0x7, KEY_SOURCE},


            {0x8, MyCmd.Keycode.BT}, {0x9, MyCmd.Keycode.PREVIOUS}, {0xa, MyCmd.Keycode.NEXT}, {0xb, MyCmd.Keycode.BT_DIAL}, {0xc, MyCmd.Keycode.BT_HANG}, {0xd, MyCmd.Keycode.HOME},
            {0xe, MyCmd.Keycode.PLAY_PAUSE}, {0xf, MyCmd.Keycode.MODLE}, {0x10, MyCmd.Keycode.SPEECH},
            //			{ 0x40, MyCmd.Keycode },
            {(byte) 0x80, MyCmd.Keycode.HOME},
            //			{ 0xae, MyCmd.Keycode },
            //			{ 0xaf, MyCmd.Keycode },
            //			{ 0xb0, MyCmd.Keycode },

    };


    private byte getACType() {
        byte t = 0;
        switch (CarUtil.getModelId()) {
            case 7:
            case 34:
                t = 1;
                break;
        }
        return t;
    }


    public void parseCanboxData(byte[] data, int len) {
        if (data[0] == 0x22) {
            parseACInfo2(data);
        } else {
            super.parseCanboxData(data, len);
        }
    }


    public int getACTempPriv(byte data) {
        if ((data & 0xff) == 0) {
            data = (byte) 0;
        } else if ((data & 0xff) == 0x7f) {
            data = (byte) 0xff;
        } else if ((data & 0xff) == 0x20) {
            data = (byte) 32;
        } else if ((data & 0xff) == 0x21) {
            data = (byte) 33;
        } else if ((data & 0xff) == 0x22) {
            data = (byte) 34;
        } else {
            data = (byte) (35 + (data - 0x1));
        }
        return data & 0xff;
    }


    public void parseACInfo2(byte[] data) {
        airData[7] = (byte) ((data[2] & 0x80) >> 7);
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    private final byte[] airData = new byte[8];

    public void parseACInfo(byte[] data) {


        airData[0] = (byte) (((data[2] & 0xec)) | ((data[2] & 0x02) >> 1) | ((data[2] & 0x01) << 1));
        airData[4] = (byte) ((data[2] & 0x10) >> 2);

        switch ((data[3] & 0xff)) {
            case 1://平行
                airData[1] = (byte) (0x40);
                break;
            case 2://平行下
                airData[1] = (byte) (0x60);
                break;
            case 3://下
                airData[1] = (byte) (0x20);
                break;
            case 4://上下
                airData[1] = (byte) (0xa0);
                break;
            case 5://上
                airData[1] = (byte) (0x80);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) ((data[4] & 0xf));

        airData[2] = data[6];
        airData[3] = data[7];

        airData[4] = (byte) (((data[8] & 0x3)) | ((data[8] & 0xc) << 2) | ((data[2] & 0x10) >> 2) | ((data[8] & 0x80) << 0));

        if (getACType() == 1) {
            airData[7] = 0x40;
            airData[2] = data[5];
            airData[3] = airData[2];
        } else {
            airData[2] = (byte) getACTempPriv(data[6]);
            airData[3] = (byte) getACTempPriv(data[7]);
        }

        if ((airData[1] & 0x0f) == 0) {
            //			Util.zeroBuf(airData);
        }
        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }


    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {

    }

    public void setMediaSrc(int source, byte type, byte[] b) {

        if (b[0] > 0x10) {
            b[0] = 0x10;
        }
        byte[] mData = new byte[]{
                (byte) 0xc0, 0x5, 0x1, 0x1, b[0], b[1], b[2]
        };

        sendDataToCanbox(mData, mData.length);
    }

    public void setMediaSrc(int source) {// default is simple box
        byte s = 0;
        byte mediaType = 0;
        switch (source) {
            case MyCmd.SOURCE_RADIO:
                //			s = 1;
                //			mediaType = 0x10;
                return;
            case MyCmd.SOURCE_MUSIC:
            case MyCmd.SOURCE_VIDEO:
                s = 8;
                mediaType = (byte) 0xff;
                break;
            case MyCmd.SOURCE_AUX:
                s = 0x0c;
                mediaType = 0x30;
                break;
            case MyCmd.SOURCE_BT:
                s = 0x0b;
                mediaType = 0x40;
                break;
            case MyCmd.SOURCE_AV_OFF:
                s = 0x00;
                mediaType = 0x0;
                break;
            default:
                s = 0xc;
                mediaType = 0x0;
                break;
        }

        byte[] mData = new byte[]{(byte) 0xc0, 0x2, s, mediaType};

        sendDataToCanbox(mData, mData.length);
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

            if (num_len > 43) {
                num_len = 43;
            }
            byte[] data;
            if (num_len == 0) {
                data = new byte[4];

                data[0] = index;
                data[1] = 2;
                data[2] = 0x10;
                data[3] = 0;
            } else {

                int len = num_len + 3;

                data = new byte[len];

                data[0] = index;
                data[1] = (byte) (num_len + 1);
                data[2] = 0x11;
                for (int i = 0; i < num_len && i < (data[1]); ++i) {
                    data[3 + i] = n[i + 2];

                    //					if (i % 2 == 0) {
                    //						data[3 + i] = n[i + 3];
                    //					} else {
                    //						data[3 + i] = n[i + 1];
                    //					}

                }
            }
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

            Log.d("Nissan2013Simple", "sendId3" + e);
        }
    }

    String mName = null;
    String mArtist = null;
    String mAlbum = null;


    public void setSongName(String s) {
        sendId3((byte) 0x70, s);
        mName = s;
    }

    public void setSongAritst(String s) {
        sendId3((byte) 0x71, s);
        mArtist = s;
    }

    public void setSongAlbum(String s) {
        sendId3((byte) 0x72, s);
        mAlbum = s;
    }

    public void setPhone(int status, String num) {// default is simple box
        switch (status) {
            case HFP_INFO_INITIAL:
            case HFP_INFO_READY:
            case HFP_INFO_CONNECTING:
            case HFP_INFO_CONNECTED:
                status = 0;
                break;
            case HFP_INFO_CALLED:
                status = 2;
                break;
            case HFP_INFO_INCOMING:
                status = 1;
                break;
            case HFP_INFO_CALLING:
                status = 4;
                break;
        }
        byte[] data = new byte[4];// {(byte)0xc5, 0x1, (byte)status};
        data[0] = (byte) 0xc5;
        data[1] = (byte) (2);
        data[2] = (byte) 0x44;
        data[3] = (byte) status;

        sendDataToCanbox(data, data.length);

        if (num == null) {
            num = " ";
        }

        try {

            byte[] n = getBytesUnicodeLittleEndian(num); //del 0xff 0xfe

            int num_len = n.length;

            if (num_len > 43) {
                num_len = 43;
            }

            if ((n[0] & 0xff) == 0xff && (n[1] & 0xff) == 0xfe) {
                num_len -= 2;
            }

            data = new byte[num_len + 4];// {(byte)0xc5, 0x1, (byte)status};
            data[0] = (byte) 0xcA;
            data[1] = (byte) (num_len + 2);
            data[2] = (byte) 0x3;
            data[3] = (byte) 0x11;

            for (int i = 0; i < num_len && i < (data[1]); ++i) {
                data[4 + i] = n[i + 2];

            }

            Util.doSleep(50);
            sendDataToCanbox(data, data.length);
        } catch (Exception e) {

        }

    }

    public void updateTime() {
        Date curDate = new Date(System.currentTimeMillis());
        byte h = (byte) curDate.getHours();

        h = fixTimeHour(h);
        byte ampm = 0;
        String strTimeFormat = Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.System.TIME_12_24);

        byte format = 1;

        String date_foramt = SettingProperties.getProperty(mContext, SettingProperties.KEY_DATE_FORMAT);
        if (date_foramt != null) {
            if ("dd/MM/yyyy".equals(date_foramt)) {
                format = 0;
            } else if ("MM/dd/yyyy".equals(date_foramt)) {
                format = 2;
            }
        }

        //		if ("12".equals(strTimeFormat)) {
        //			if (h > 12) {
        //				h -= 12;
        //			} else if (h == 0) {
        //				h = 12;
        //			}
        //			h |= 0x80;
        //		} else {
        //			ampm = 1;
        //		}

        byte m = (byte) curDate.getMinutes();
        byte s = (byte) curDate.getSeconds();

        //		Log.d("cccc", ""+curDate.getYear());
        byte y = (byte) (curDate.getYear() - 100);
        byte mon = (byte) (curDate.getMonth() + 1);
        byte d = (byte) curDate.getDate();
        byte[] buf = new byte[]{
                (byte) 0xa6, 0x06, y, mon, d, h, m, s
        };

        sendDataToCanbox(buf, buf.length);
    }

    public int getUpdateTime() {
        return 60000;
    }
}
