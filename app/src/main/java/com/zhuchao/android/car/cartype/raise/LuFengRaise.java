package com.zhuchao.android.car.cartype.raise;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;

import java.util.Calendar;


public class LuFengRaise extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {0x27, 0x23, 0x24, 0x25};
    private final static byte[][] KEYS_WHEEL = {{0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.PREVIOUS}, {0x4, MyCmd.Keycode.NEXT}, {0x5, MyCmd.Keycode.HOME}, {0x6, MyCmd.Keycode.SPEECH}, {0x7, MyCmd.Keycode.BT_DIAL}, {0x8, MyCmd.Keycode.BT_HANG},};

    public LuFengRaise() {
        //		buildCmdRepeatSendCarType(getCarTypeCmd());
        buildCmdDoor((byte) 0x22, (byte) 0x2, (byte) 0xfc, (byte) 0x02);
        //		buildCmdRadarFront((byte) 0x23, (byte) 0x0, (byte) 0xa);
        //		buildCmdRadarBack((byte) 0x22, (byte) 0x0, (byte) 0xa);
        //		buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
        //		buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        //		buildCmdOutTemp((byte) 0x41, (byte) 0x10);
        buildCmdVersion((byte) 0x7f, (byte) 0x0);
        mIdAC = 0x23;
        mIdKey = 0x21;
        MAP_KEYS = KEYS_WHEEL;

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 1) {
            data = (byte) 0;
        } else if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else {
            data = (byte) (34 + (data & 0xff));
        }
        return data;
    }

    public void parseACInfo(byte[] data) {

        byte[] airData = new byte[8];

        airData[0] = (byte) ((data[2] & 0xe8) | ((data[2] & 0x01) << 1));

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


        airData[2] = data[5];
        airData[3] = data[5];

        airData[5] |= 0x80;
        super.parseACInfo(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }


    public int getOutTemp(byte[] data) {//
        short t = (short) ((data[10] & 0xff) | ((data[9] & 0xff) << 8));
        return t;
    }


    public int getUpdateTime() {
        return 60000;
    }

    public void updateTime() {

        Calendar c = Calendar.getInstance();

        byte h = (byte) c.get(Calendar.HOUR_OF_DAY);
        h = fixTimeHour(h);

        byte m = (byte) c.get(Calendar.MINUTE);

        byte[] buf = new byte[]{(byte) 0x83, 0x03, 8, h, m};


        sendDataToCanbox(buf, buf.length);

    }

}
