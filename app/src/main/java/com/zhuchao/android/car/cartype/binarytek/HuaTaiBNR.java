package com.zhuchao.android.car.cartype.binarytek;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.canbox.Canbox;


public class HuaTaiBNR extends Canbox {

    private final static byte[] IDS_TO_CANBOXSETTING = {(byte) 0xd2};
    private final static byte[][] KEYS_WHEEL = {{0x11, MyCmd.Keycode.MODLE}, {0x12, MyCmd.Keycode.PREVIOUS}, {0x13, MyCmd.Keycode.NEXT}, {0x14, MyCmd.Keycode.VOLUME_UP}, {0x15, MyCmd.Keycode.VOLUME_DOWN}, {0x16, MyCmd.Keycode.MUTE}, {0x30, MyCmd.Keycode.BT_DIAL}, {0x31, MyCmd.Keycode.BT_HANG},};

    public HuaTaiBNR() {
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

        buildCmdAngle((byte) 0x31, (byte) 0x0, 0x1600);
        buildCmdVersion((byte) 0x30, (byte) 0x0);

        buildCmdKey((byte) 0x12, (byte) 1, (byte) 2, (byte) 0, KEYS_WHEEL);

        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }

}
