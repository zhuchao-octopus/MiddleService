package com.zhuchao.android.car.cartype.daojun;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.McuManager;

import java.lang.reflect.Method;

public class DaoQiDaoJun extends Canbox {

    public DaoQiDaoJun() {
        buildCmdRepeatSendCarType(getCarTypeCmd(), 3);
        buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);

        buildCmdEQ((byte) 0x27, (byte) 0x0, 6);
        buildCmdAngle((byte) 0x26, (byte) 0x0, 0x2198);
        buildCmdVersion((byte) 0x30, (byte) 0x0);
        mIdAC = 0x3;
        mIdKey = 0x1;
        MAP_KEYS = KEYS_WHEEL;
        mIdKey2 = 0x2;
        MAP_KEYS2 = KEYS_WHEEL2;

        mEQData = new byte[]{0, 0, 0, 9, 9, 4};
        IDS_TO_CANBOXSETTINGS = IDS_TO_CANBOXSETTING;
    }

    private final static byte[] IDS_TO_CANBOXSETTING = {0x36};

    private final static byte[][] KEYS_WHEEL = {
            {0x1, MyCmd.Keycode.VOLUME_UP}, {0x2, MyCmd.Keycode.VOLUME_DOWN}, {0x3, MyCmd.Keycode.PREVIOUS}, {0x4, MyCmd.Keycode.NEXT}, {0x5, MyCmd.Keycode.MODLE}, {0x6, MyCmd.Keycode.SPEECH},
            {0x24, MyCmd.Keycode.BT},
    };
    private final static byte[][] KEYS_WHEEL2 = {
            {0x1, MyCmd.Keycode.POWER}, {0x16, MyCmd.Keycode.PLAY_PAUSE}, {0x17, MyCmd.Keycode.VOLUME_ROLL_UP}, {0x18, MyCmd.Keycode.VOLUME_ROLL_DOWN}, {0x19, MyCmd.Keycode.ROLL_NEXT},
            {0x1a, MyCmd.Keycode.ROLL_PREV},
    };

    private byte[] getCarTypeCmd() {
        byte[] cmd = new byte[]{
                (byte) 0x72, 0x15, 0x14, 0x1, 0x1, 0x1, 0x3, (byte) 0xaf, (byte) 0xfb, (byte) 0xfb, (byte) 0xfb, 0, 0, 0, 0, 0, 0, 0, 0
        };
        if (CarUtil.getModelId() == 0) {
            switch (CarUtil.getModelId()) {
                case 1:
                    cmd[12] = 0x1;
                    break;
                case 0:
                    cmd[12] = 0x2;
                    break;
            }
        }
        return cmd;
    }

    @Override
    public int getAngleValue(byte[] data) {

        int angle = (short) ((data[2] & 0xff) | (((data[3] & 0xff)) << 8));

        int max = 1055;
        angle = angle - 4080;
        angle = angle * 3000 / max;
        return angle;

    }

    @Override
    public int getACTemp(byte data) {
        // TODO Auto-generated method stub
        if ((data & 0xff) == 0) {

        } else if ((data & 0xff) == 0x1f) {
            data = (byte) 0xff;
        } else if ((data & 0xff) >= 1 && (data & 0xff) <= 0x12) {
            data = (byte) (26 + (((data & 0xff)) * 2));
        } else {
            data = (byte) 0xfa;
        }
        return data;
    }

    byte[] airData = new byte[12];

    public void parseACInfo(byte[] data) {

        airData[0] = (byte) (((data[2] & 0x10) >> 4) | ((data[3] & 0x10) >> 4) | ((data[2] & 0x60) << 0) | ((data[2] & 0x80) << 0) | ((data[3] & 0x20) >> 3) | ((data[8] & 0x04) << 2));

        // airData[0] |= (byte)0x80;
        switch ((data[3] & 0xf)) {
            case 3:
                airData[1] = (byte) (0x20);
                break;
            case 4:
                airData[1] = (byte) (0x60);
                break;
            case 5:
                airData[1] = (byte) (0x40);
                break;
            case 7:
                airData[1] = (byte) (0x80);
                break;
            case 8:
                airData[1] = (byte) (0xa0);
                break;
            default:
                airData[1] = 0;
                break;
        }

        airData[1] |= (byte) ((data[2] & 0xf));

        airData[2] = data[4];
        airData[3] = data[5];

        airData[4] = (byte) (((data[6] & 0x33)));
        airData[9] = (byte) (((data[8] & 0x08) << 4));

        super.parseACInfo(airData);
    }

    public void parseACInfoRear(byte[] data) {

        airData[9] = (byte) ((data[2] & 0x80) >> 0);
        airData[10] = (byte) getACTemp(data[3]);
        airData[11] = (byte) (((data[2] & 0x10) >> 0) | ((data[2] & 0x08) << 3) | ((data[2] & 0x04) << 3) | ((data[4] & 0x0f) << 0));

        super.parseACInfoRear(airData);
    }

    public void setMediaMoreInfo(int source, int play, int total, int time, int total_time) {
    }

    public void setMediaSrc(int source, byte type, byte[] b) {
    }

    public void setMediaSrc(int source) {
    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case 0x1b:
                parseACInfoRear(data);
                break;
            case (byte) 0xfe:
                McuManager mcu = McuManager.getInstance();
                Log.d("DaoQiDaoJun", "setKeepAcc:" + data[2]);
                if (mcu != null) {
                    mcu.setKeepAcc(data[2]);
                }
                break;
            default:
                super.parseCanboxData(data, len);
        }
    }

    public void sendDataToCanbox(byte[] data, int len) { // default is simple
        // canbox

        super.sendDataToCanbox(data, len);
        byte[] send = new byte[len + 2];
        send[0] = 0x2e;
        send[len + 1] = simpleSum(data, len);
        byteArrayCopy(send, data, 1, 0, len);
        Log.d("DaoQiDaoJun", "sendDataToCanbox buf:" + Util.byte2HexStr(send));
        sendCanboxInfo(send);
    }


    private void parseCanboxDataFromCanApp(byte[] data) {

        if (data != null && data.length > 2) {
            byte[] d = new byte[data.length - 2];
            byteArrayCopy(d, data, 0, 1, d.length);
            parseCanboxData(d, d.length);
        }

    }

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
        }
    };

    public int doEQCmd(int cmd, int data) {
        int ret = 0;
        if (cmd == EQ_REQUEST_ALL_MAX) {
            ret = (8 << 16) | (19 << 8) | 19;
            mHandler.removeMessages(0);
            mHandler.sendEmptyMessageDelayed(0, 200);

        } else {
            byte[] buf = new byte[]{(byte) 0xc4, 0x8, 0, 0, 0, 0, 0, 0, 0, 0};
            switch (cmd) {
                case EQ_CMD_SET_HIGH:
                    mEQData[0] = (byte) data;
                    break;
                case EQ_CMD_SET_MIDDLE:
                    mEQData[1] = (byte) data;
                    break;
                case EQ_CMD_SET_LOW:
                    mEQData[2] = (byte) data;
                    break;
                case EQ_CMD_SET_ZONE_FR:
                    mEQData[3] = (byte) data;
                    break;
                case EQ_CMD_SET_ZONE_LR:
                    mEQData[4] = (byte) data;
                    break;
                case EQ_CMD_SET_VOLUME:
                    mEQData[5] = (byte) data;
                    break;
            }

            buf[2] = mEQData[5];
            buf[4] = mEQData[4];
            buf[5] = mEQData[3];
            buf[6] = mEQData[2];
            buf[7] = mEQData[1];
            buf[8] = mEQData[0];


            sendDataToCanbox(buf, buf.length);
            returnEQData(EQ_CMD_SET_ALL_DATA, mEQData);
        }

        return ret;
    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        super.startConnect();
        registerReceiver();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.dogen.usbcameradogen", "com.dogen.usbcameradogen.services.DogenServiceManage");
                    intent.setComponent(componentName);

                    Class<?>[] paramClasses = {};
                    Method getVolumeList = Context.class.getMethod("startForegroundService", paramClasses);

                    getVolumeList.invoke(intent);

                    //					mContext.startForegroundService(intent);

                    Log.d("DaoQiDaoJun", "startForegroundService DogenServiceManage");
                } catch (Exception e) {

                    Log.d("DaoQiDaoJun", "startForegroundService err:" + e);
                }
            }
        }, 4000);

    }

    @Override
    public void stopConnect() {
        // TODO Auto-generated method stub
        super.stopConnect();
        unregisterReceiver();
    }

    public final static String MSG_CAN_SEND_DATA_ALL_INFO = "com.choiceway.eventcenter.EventUtils.MSG_CAN_SEND_DATA_ALL_INFO";
    public final static String CAR_AIR_DATA = "EventUtils.CAR_AIR_DATA";
    private BroadcastReceiver mBroadcastReceiver;

    private void unregisterReceiver() {
        if (mBroadcastReceiver != null && mContext != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void registerReceiver() {
        if (mBroadcastReceiver == null && mContext != null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (intent.getAction().equals(MSG_CAN_SEND_DATA_ALL_INFO)) {
                        byte[] buf = intent.getByteArrayExtra(CAR_AIR_DATA);
                        Log.d("DaoQiDaoJun", "MSG_CAN_SEND_DATA_ALL_INFO buf:");
                        if (buf != null) {
                            Log.d("DaoQiDaoJun", "MSG_CAN_SEND_DATA_ALL_INFO buf:" + Util.byte2HexStr(buf));

                            parseCanboxDataFromCanApp(buf);
                        }
                    }
                }
            };
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MSG_CAN_SEND_DATA_ALL_INFO);

        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }
}
