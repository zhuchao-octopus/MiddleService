package com.zhuchao.android.car.hardware;

import android.os.Handler;

import com.common.util.Util;
import com.zhuchao.android.car.debug.DebugMessage;
import com.zhuchao.android.fbase.MMLog;

public class Mcu {
    static {
        System.loadLibrary("mcu");
    }

    private final static String TAG = "Mcu";

    public final static int MSG_RECEIVE_MCU_DATA = 0;
    public final static int MSG_RECEIVE_OS_DATA = 1;

    public static final int MCU_OPEN = 1;
    public static final int MCU_CLOSE = 2;
    public static final int MCU_WRITE_DATA = 3;
    public static final int READ_KERNEL_PRO = 4;
    // receive
    public static final int CMD_SCR_STATE = 0x10;
    public static final int CMD_TOUCH_POS = 0x20;
    public static final int CMD_KEY_EVENT = 0x30;
    public static final int CMD_SYS_SET = 0x40;
    public static final int CMD_AIR_MSG = 0x50;
    public static final int CMD_MCU_VER = 0x60;
    public static final int CMD_TIMEDATE = 0x70;
    // send
    public static final int CMD_ARM_READY = 0x01;
    public static final int CMD_SCR_MODE = 0x02;

    // param
    public static final int UP_KEY = 0x01;
    public static final int DOWN_KEY = 0x02;
    public static final int LEFT_KEY = 0x03;
    public static final int RIGHT_KEY = 0x04;
    public static final int ENTER_KEY = 0x05;
    public static final int CANCEL_KEY = 0x06;
    public static final int RETURN_KEY = 0x07;
    public static final int LEFTTURN_KEY = 0x08;
    public static final int RIGHTTURN_KEY = 0x09;
    public static final int NAVI_KEY = 0x0A;
    public static final int SPEECH_KEY = 0x0B;

    private Handler mHandler;
    private Handler mOsHandler;

    public static Mcu mThis;

    public static Mcu getInstance() {
        if (mThis == null) {
            mThis = new Mcu();
        }
        return mThis;
    }

    public void setHandler(Handler h) {
        mHandler = h;
    }

    public void setOsHandler(Handler h) {
        mOsHandler = h;
    }

    private native final int nativeSendCommand(int cmd, int param1, byte[] param2);

    public void sendCmd(int cmd) {
        nativeSendCommand(cmd, 0, null);
    }

    public int sendCmd(int cmd, byte[] param2) {
        return nativeSendCommand(cmd, param2.length, param2);
    }

    public int sendCmd(byte[] param2) {
        // byte[] param = new byte[param2.length+1];
        // Util.byteArrayCopy(param, param2, 0, 0, param2.length);
        // param[param2.length] = checkSum(param2, param2.length);
        ///if(param2.length >=2) {
            ///if (param2[0] != 0x01 && param2[1] != 0x01) MMLog.d(TAG, "sendCmd:" + Util.byteArrayToHex(param2));
        ///}
        DebugMessage.updateText(param2, false);
        return nativeSendCommand(MCU_WRITE_DATA, param2.length, param2);
    }

    public int sendKernelCmd(byte[] param2) {
        /// byte[] param = new byte[param2.length+1];
        /// Util.byteArrayCopy(param, param2, 0, 0, param2.length);
        /// param[param2.length] = checkSum(param2, param2.length);
        MMLog.d(TAG, "sendKernelCmd:" + Util.byteArrayToHex(param2));
        return nativeSendCommand(READ_KERNEL_PRO, param2.length, param2);
    }

    ///////////////////////////////////////////////////////////////////////////////////
    //receive data from uart
    private void dataCallback(byte[] param, int len) {
        if (len < 2) return;
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVE_MCU_DATA, param));
        }
    }

    private void kernelCallback(byte[] param, int len) {
        /// mHandlerKernel.sendMessage(mHandlerKernel.obtainMessage(0, param));
        ///		Log.d(TAG, "kernelCallback");
        if (mOsHandler != null && len > 2) {
            mOsHandler.sendMessage(mHandler.obtainMessage(MSG_RECEIVE_OS_DATA, param));
        }
    }

    public byte checkSum(byte[] data, int len) {
        byte sum = 0;
        for (int i = 0; i < len; ++i) {
            sum += data[i];
        }
        return (byte) ~(sum ^ 0xFF);
    }
}