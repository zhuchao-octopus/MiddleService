package com.zhuchao.android.car.autotest;

import com.common.utils.MyCmd;

public class TestNode {

    public final static int MCU_TEST_SOURCE = 0x1000;
    public final static int MCU_TEST_ACC = 0x1002;
    public final static int MCU_TEST_REVERSE = 0x1003;
    public final static int MCU_TEST_ILL = 0x1004;
    public final static int MCU_TEST_BRAKE = 0x1005;
    public final static int MCU_TEST_FCAMERA = 0x1006;
    public final static int MCU_TEST_ANT = 0x1007;
    public final static int MCU_TEST_SWC = 0x1008;
    public final static int MCU_TEST_REAR_VIDEO = 0x1009;
    public final static int MCU_TEST_VOLUME = 0x100a;
    public final static int MCU_TEST_SWC2 = 0x11008;
    final static int TYPE_INDEPEND = 0;
    final static int TYPE_NEED_SOURCE = 1;
    final static int STATUS_TIMEOUT = -2;
    final static int STATUS_NORMAL = 0;
    final static int STATUS_TESTING = 1;
    final static int STATUS_SUCESS = 2;
    final static int STATUS_FAIL = 3;
    final static int STATUS_END = 9;
    public int mName;
    public int mType;
    public int mTimeout;
    public int mSource;
    public int mStatus;
    public long mTimeStart;
    public int mResultId;
    public int mResultData;

    public TestNode(int name, int source, int timeout) {
        mName = name;
        mSource = source;
        mTimeout = timeout;
        mType = sourceToType(source);
    }

    public TestNode(int name, int source, int timeout, int resultId) {
        mName = name;
        mSource = source;
        mTimeout = timeout;
        mResultId = resultId;
        mType = sourceToType(source);
    }

    private int sourceToType(int source) {
        int type = TYPE_INDEPEND;
        if (source >= MCU_TEST_SOURCE) {
            return TYPE_NEED_SOURCE;
        }

        switch (source) {
            case MyCmd.SOURCE_FRONT_CAMERA:
            case MyCmd.SOURCE_REVERSE:
            case MyCmd.SOURCE_AUX:
            case MyCmd.SOURCE_DVD:
                //		case MyCmd.SOURCE_BT:
            case MyCmd.SOURCE_RADIO:
            case MyCmd.SOURCE_MX51:
                type = TYPE_NEED_SOURCE;
                break;
        }
        return type;
    }

}
