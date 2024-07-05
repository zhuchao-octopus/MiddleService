package com.zhuchao.android.car.cartype;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.canbox.CanService;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.update.UpdateHiWorld;
import com.zhuchao.android.car.cartype.update.UpdateLuZheng;
import com.zhuchao.android.car.cartype.update.UpdateRaise;
import com.zhuchao.android.car.cartype.update.UpdateSimple;
import com.zhuchao.android.car.manager.McuManager;
import com.zhuchao.android.fbase.MMLog;

import java.io.File;

public class CarUtil {

    public static final String TAG = "CarUtil";
    private Canbox mCanbox = null;
    private static CarUtil mCarUtil = null;
    public static int m360UI = 0;
    public static int mTempUnit = 0;
    // public static int mMileagepUnit = 0;

    public static final int INVALID_OUT_DOOR_TEMP = Integer.MAX_VALUE;
    public static final int CLEAR_OUT_DOOR_TEMP = Integer.MAX_VALUE - 1;

    public static final int SWITCH_CANBOX_REVERSE = (1 << 0);
    public static final int SWITCH_CANBOX_LR_TURNER_LIGHT = (1 << 1);
    public static final int SWITCH_CANBOX_BRAKE = (1 << 2);


    public static final int AC_CONFIG_TEMP_CHANGE = (1 << 2);
    public static final int AC_CONFIG_HIDE = (1 << 3);
    public static final int AC_CONFIG_OURDOOR_HIDE = (1 << 4);

    public static int mSettingCanboxBrake = 0;
    public static int mPreCanboxBrake = 0;

    private static String mCanboxType = null;
    private static final int TIME_CANBOX_UPDATE_TIME = 60000;

    public final static String PG = "/dev/ptyCan";

    private CarUtil() {
        clear();
        mCanboxType = getCanboxSetting();
        MMLog.d(TAG, "mCanboxType:" + mCanboxType + ",mProIndex:" + mProIndex);

        initCanboxBrake();
        mCanbox = CanboxToPro.getPro(mCanboxType, mProVersion, mProIndex);

        if (mCanbox != null) {
            mIsUpdating = false;
            mCanbox.setContext(GlobalDefinition.getContext());
            mCanbox.startConnect();
        } else {
            MMLog.d(TAG, "mCanbox == null!!!!!!!!!!!");
        }

        initPGBin();
    }

    public static CarUtil getCarUtilInstance() {
        if (mCarUtil != null) {
            mCarUtil.clear();
        }
        // if (null == mCarUtil) {
        mCarUtil = new CarUtil();
        // }
        return mCarUtil;
    }

    public static Canbox getCanboxInstance() {
        if (mCarUtil != null) {
            return mCarUtil.mCanbox;
        }
        return null;
    }


    public static void initCanboxBrake() {
        int v = MachineConfig.getPropertyInt(MachineConfig.KEY_CAN_BOX_PG_SWITCH);
        Log.d(TAG, "initCanboxBrake:" + v);
        mSettingCanboxBrake = (v & CarUtil.SWITCH_CANBOX_BRAKE);
        mPreCanboxBrake = 0;
    }

    public static void updateCanboxBrake(int brake) {
        if (mSettingCanboxBrake != 0) {
            McuManager mMcuManager = McuManager.getInstance();
            if (mMcuManager != null) {
                MMLog.d(TAG, "updateCanboxBrake:" + " brake " + brake + ":" + mPreCanboxBrake + "mBrakeSwitch:" + mMcuManager.mBrakeSwitch);
                if (mMcuManager.mBrakeSwitch == 0) {
                    return;
                }
                if (mPreCanboxBrake != brake) {
                    mPreCanboxBrake = brake;

                    if (brake == 0) {
                        if (mMcuManager.mBrake == 1) {
                            brake = 1;
                        }
                    }

                    Util.setFileValue("/sys/class/ak/source/brake_status", mPreCanboxBrake);

                    if (GlobalDefinition.getContext() != null) {
                        BroadcastUtil.sendByCarService(GlobalDefinition.getContext(), MyCmd.Cmd.MCU_BRAK_CAR_STATUS, brake);
                    }
                    mMcuManager.setBrakeProp();
                }
            }
        }
    }

    public static void updateTempUnit(int i) {
        mTempUnit = i;
        if (mCarUtil != null && mCarUtil.mCanbox != null) {
            mCarUtil.mCanbox.updateOutDoorTemp(INVALID_OUT_DOOR_TEMP);
        }
    }


    private void initPGBin() {
        File f = new File(PG);
        if (f.exists()) {
            Util.setFileValue(PG, "RESET");
        }
    }

    public static int isNeedSendTime() {
        if (mCanboxType != null) {

            if (mUpdateTime != -2) {
                return mUpdateTime;
            }

            if (mCarUtil != null && mCarUtil.mCanbox != null) {
                if (mCarUtil.mCanbox.getUpdateTime() != Integer.MAX_VALUE) {
                    return mCarUtil.mCanbox.getUpdateTime();
                }
            }

            if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_HY) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_RAM_FIAT) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_NISSAN2013) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_NISSAN_RAISE) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_PORSCHE_UNION) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_FORD_EXPLORER_SIMPLE) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_HONDA_DA_SIMPLE) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_NISSAN_BINARYTEK) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_JEEP_SIMPLE) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_TOUAREG_HIWORLD) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_HAFER_H2) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_FORD_RAISE) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_SMART_HAOZHENG) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_JEEP_XINBAS) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_OUSHANG_RAISE) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_FIAT_EGEA_RAISE) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_ZHONGXING_OD) || mCanboxType.equals(MachineConfig.VALUE_CANBOX_HY_RAISE)) {
                return TIME_CANBOX_UPDATE_TIME;

            } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_ACCORD_BINARYTEK)) {
                return 20000;
            } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_VW_MQB_RAISE)) {
                return TIME_CANBOX_UPDATE_TIME;
            } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_X30_RAISE)) {
                return 1000;
            } else if (mCanboxType.equals(MachineConfig.VALUE_CANBOX_LANDROVER_HAOZHENG)) {
                return -1;
            }
        }
        return 0;
    }

    public static void sendVolumeToCanbox(byte volume) {
        if (mCarUtil != null && mCarUtil.mCanbox != null) {
            mCarUtil.mCanbox.setVolume(volume);
        }
    }

    public static boolean mIsNeedSendEQ = false;

    public static void setMcuEQZoneUsed(int i) {
        McuManager mcu = McuManager.getInstance();
        if (mcu != null) {
            mcu.setEQZoneUsed(i);
        }
    }

    public static void sendEqToCanbox(byte[] buf) {
        if (mCarUtil != null && mCarUtil.mCanbox != null) {
            mCarUtil.mCanbox.sendEqToCanbox(buf);
        }
    }

    public static void updateCanboxExData() {
        if (mCarUtil != null && mCarUtil.mCanbox != null) {
            mCarUtil.mCanbox.updateCanboxExData();
        }
    }

    public static final int CANBOX_LED_DISC = 1;
    public static final int CANBOX_LED_PANNEL = 2;
    public static final int CANBOX_LED_ILL = 0xfffe;
    public static final int CANBOX_LED_ALL = 0xffff;

    public static void setCanboxLED(int type, int status) {
        if (mCarUtil != null && mCarUtil.mCanbox != null) {
            mCarUtil.mCanbox.setCanboxLED(type, status);
        }
    }

    public static void notifyReverse(int status) {
        if (mCarUtil != null && mCarUtil.mCanbox != null) {
            mCarUtil.mCanbox.notifyReverse(status);
        }
    }

    public static String getCanboxType() {
        return mCanboxType;
    }

    public static void sendDataToCanbox(byte[] buf) {
        if (mCarUtil != null && mCarUtil.mCanbox != null) {
            if (buf != null) {
                mCarUtil.mCanbox.sendDataToCanbox(buf, buf.length);
            }
        }
    }

    public static String getCanboxVersion() {
        if (mCarUtil.mCanbox != null) {
            mCarUtil.mCanbox.requestVersion();
            if (mCarUtil.mCanbox.mVersionEx == null) {
                return mCarUtil.mCanbox.mVersion;
            } else {
                return mCarUtil.mCanbox.mVersion + " ---  " + mCarUtil.mCanbox.mVersionEx;
            }
        }
        return null;
    }

    public static boolean mIsUpdating = false;

    public void canboxParser(byte[] data, int len) {
        try {

            if (mCanbox != null) {
                int type = mCanbox.getReturnType();

                if (type == -1) {
                    type = CanboxToPro.getReturnMsgType(mCanboxType, mProVersion, mProIndex);
                }

                if (type == 0xff) {    //hiworld is update

                    mCanbox.parseCanboxData(data, len);

                } else if (type == 1) {
                    len -= 3;
                    byte[] d = new byte[len];
                    mCanbox.byteArrayCopy(d, data, 0, 2, len);
                    mCanbox.parseCanboxData(d, len);
                } else if (type == 2) {
                    len -= 4;
                    byte[] d = new byte[len];
                    mCanbox.byteArrayCopy(d, data, 0, 4, len);
                    byte change = d[0];
                    d[0] = d[1];
                    d[1] = change;
                    mCanbox.parseCanboxData(d, len);
                } else if (type == 3) {

                    mCanbox.parseCanboxData(data, len);

                } else if (type == 4) {
                    len -= 3;
                    byte[] d = new byte[len];
                    mCanbox.byteArrayCopy(d, data, 0, 3, len);
                    byte change = d[0];
                    d[0] = d[1];
                    d[1] = change;
                    mCanbox.parseCanboxData(d, len);
                } else {
                    len -= 3;
                    byte[] d = new byte[len];
                    mCanbox.byteArrayCopy(d, data, 0, 3, len);
                    mCanbox.parseCanboxData(d, len);
                }

                if ((GlobalDefinition.getContext() != null) && isDataDistribution()) {
                    Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
                    i.putExtra("buf", data);
                    GlobalDefinition.getContext().sendBroadcast(i);
                }
            } else if (false) {
                //no used now
                if (((data[0] & 0xff) == 0x2e) && ((data[1] & 0xff) == 0x78) && ((data[2] & 0xff) == 0x3) && ((data[3] & 0xff) == 0x1) && ((data[4] & 0xff) == 0xec) && ((data[5] & 0xff) == 0x1) && ((data[6] & 0xff) == 0x96)) {
                    Log.d(TAG, "in auto test");
                    MachineConfig.setProperty(MachineConfig.KEY_CAN_BOX, "\u5176\u5B83\u5176\u5B83Test,v3,z300,i90811,h2,j02030000,l00000001");
                    CanService.updateCanboxEx();

                }
            }
            // else if (mUpdatFile != null) {
            // if (mCanbox != null) {
            // mCanbox.parseCanboxData(data, len);
            // }
            // }
        } catch (Exception e) {
            MMLog.d("CarUtil", "canboxParser err" + e);
        }
    }

    private void clear() {
        mIsNeedSendEQ = false;
        if (mCanbox != null) {
            mCanbox.clear();
        }
    }

    private static int mKeyType = 0;
    private static int mChangeKey = 0;
    private static int mFrontDoor = 0;
    private static int mBackDoor = 0;
    private static int mAirCondition = 0;
    private static int mCarType = 0;
    private static int mCarType2 = 0;
    private static int mCarEQ = 0;
    private static int mOtherSettings = 0;
    private static int mProVersion = 0;
    private static int mProIndex = -1;
    private static int mUpdateTime = -2;
    private static int mCarTypeConfig = -1;
    private static int mManaId = -1;
    private static int mCateId = -1;
    private static int mModelId = -1;

    private static int mExternalRadarId = 0;

    public static int getExternalRadarId() {
        return mExternalRadarId;
    }

    public static int getManaId() {
        return mManaId;
    }

    public static int getCatelId() {
        return mCateId;
    }

    public static int getModelId() {
        return mModelId;
    }

    public static int getCarTypeConfig() {
        return mCarTypeConfig;
    }

    public static int getProIndex() {
        return mProIndex;
    }

    public static int getCarType() {
        return mCarType;
    }

    public static int getCarType2() {
        return mCarType2;
    }

    public static int getKeyType() {
        return mKeyType;
    }

    public static int getCarEQ() {
        return mCarEQ;
    }

    public static int getChangeKey() {
        return mChangeKey;
    }

    public static int getFrontDoor() {
        return mFrontDoor;
    }

    public static int getBackDoor() {
        return mBackDoor;
    }

    public static int getAirCondition() {
        return mAirCondition;
    }

    public static boolean isHideAirCondition() {
        if (mProVersion >= 3) {
            return (mAirCondition & AC_CONFIG_HIDE) != 0;
        } else {
            return mAirCondition == 2;
        }
    }

    public static boolean isChangeAirCondition() {
        if (mProVersion >= 3) {
            return (mAirCondition & AC_CONFIG_TEMP_CHANGE) != 0;
        } else {
            return mAirCondition == 1;
        }
    }

    public static boolean isHideOurDoorTemp() {
        if (mProVersion >= 3) {
            return (mAirCondition & AC_CONFIG_OURDOOR_HIDE) != 0;
        }

        return false;
    }

    public static int getCanboxProVersion() {
        return mProVersion;
    }

    private String getCanboxSetting() {
        mKeyType = 0;
        mChangeKey = 0;
        mFrontDoor = 0;
        mBackDoor = 0;
        mAirCondition = 0;
        mCarType = 0;
        mCarType2 = 0;
        mCarEQ = 0;
        mOtherSettings = 0;
        mProVersion = 0;
        mProIndex = -1;

        mExternalRadarId = 0;

        mExternalRadarId = MachineConfig.getPropertyInt(MachineConfig.KEY_EXTERNAL_BOX);

        mCanboxType = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAN_BOX);
        if (mCanboxType == null) {
            mCanboxType = MachineConfig.getPropertyReadOnly(MachineConfig.KEY_CAN_BOX);
        }
        if (mCanboxType != null) {
            String[] ss = mCanboxType.split(",");
            mCanboxType = ss[0];
            try {
                mUpdateTime = -2;
                for (int i = 1; i < ss.length; ++i) {
                    if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_AIR_CONDITION)) {
                        mAirCondition = Integer.parseInt(ss[i].substring(1));
                        if (isHideOurDoorTemp()) {
                            GlobalDefinition.sendByCarServiceToSystemUI(GlobalDefinition.getContext(), "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, "");
                        }
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_KEY_TYPE)) {
                        mKeyType = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_CHANGE_KEY)) {
                        mChangeKey = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_FRONT_DOOR)) {
                        mFrontDoor = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_REAR_DOOR)) {
                        mBackDoor = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_CAR_TYPE)) {
                        mCarType = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_CAR_TYPE2)) {
                        mCarType2 = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_EQ)) {
                        mCarEQ = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_OTHER)) {
                        mOtherSettings = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_PROTOCAL_VERSION)) {
                        mProVersion = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_PROTOCAL_INDEX)) {
                        mProIndex = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_UPDATE_TIME)) {
                        mUpdateTime = 0;
                        mUpdateTime = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_CAR_CONFIG)) {
                        mCarTypeConfig = Integer.parseInt(ss[i].substring(1));
                    } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_ID)) {
                        String mProId = ss[i].substring(1);
                        if (mProId.length() >= 4) {
                            int start = 0;
                            int end = 0;
                            if (mProId.charAt(1) == '0' && mProId.charAt(2) == '0') {
                                end = 2;
                            } else if (mProId.charAt(1) == '0' && mProId.charAt(2) != 0) {
                                end = 1;
                            } else if (mProId.charAt(2) == '0') {
                                end = 2;
                            }
                            mManaId = Integer.parseInt(mProId.substring(start, end));
                            start = end + 1;

                            if (mProId.contains("-")) {
                                String[] sss = mProId.substring(start).split("-");
                                mModelId = Integer.parseInt(sss[1]);
                                mCateId = Integer.parseInt(sss[0]);
                            }
                            else
                            {
                                switch (mProId.length() - start) {
                                    case 2:
                                        mModelId = Integer.parseInt(mProId.substring(start + 1, start + 2));
                                        mCateId = Integer.parseInt(mProId.substring(start, start + 1));
                                        break;
                                    case 4:
                                        mModelId = Integer.parseInt(mProId.substring(start + 2, start + 4));
                                        mCateId = Integer.parseInt(mProId.substring(start, start + 2));
                                        break;
                                    case 3:
                                        mModelId = Integer.parseInt(mProId.substring(start + 2, start + 3));
                                        mCateId = Integer.parseInt(mProId.substring(start, start + 2));
                                        break;
                                }
                            }
                            MMLog.d(TAG, ":" + mModelId);
                        }
                    }

                }
            } catch (Exception ignored) {
            }

            String appShow = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAN_BOX_SHOW_APP);
            String appHide = MachineConfig.getPropertyOnce(MachineConfig.KEY_APP_HIDE);
            mIsShowAC = appShow != null && appShow.contains(AppConfig.HIDE_CANBOX_AC) && (appHide == null || !appHide.contains(AppConfig.HIDE_CANBOX_AC));

            mIsShowEQ = appShow != null && appShow.contains(AppConfig.HIDE_CANBOX_EQ) && (appHide == null || !appHide.contains(AppConfig.HIDE_CANBOX_EQ));
        }
        return mCanboxType;
    }

    public static boolean getRadarBeep() {
        return (mOtherSettings & MachineConfig.VALUE_CANBOX_OTHER_RADAR_BEEP) != 0;
    }

    public static boolean getHideRadarUI() {
        return (mOtherSettings & MachineConfig.VALUE_CANBOX_OTHER_RADAR_UI) != 0;
    }

    public static boolean getShowRadarUIOnlyInReverse() {
        return (mOtherSettings & MachineConfig.VALUE_CANBOX_OTHER_RADAR_UI_ONLY_IN_REVERSE) != 0;
    }

    public static byte getTimeAddOrMinus1() {
        if ((mOtherSettings & MachineConfig.VALUE_CANBOX_OTHER_HOUR_ADD_1) != 0) {
            return 1;
        } else if ((mOtherSettings & MachineConfig.VALUE_CANBOX_OTHER_HOUR_MINUS_1) != 0) {
            return -1;
        }
        return 0;
    }

    public static boolean getRightCameraExist() {
        return (mOtherSettings & MachineConfig.VALUE_CANBOX_OTHER_RIGHT_CAMERA) != 0;
    }

    public static boolean isDataDistribution() {
        return (mOtherSettings & MachineConfig.VALUE_CANBOX_OTHER_DATA_DISTRIBUTION) != 0;
    }

    public static boolean requestAngleData() {
        if (getCanboxInstance() != null) {
            return getCanboxInstance().requestAngleData();
        }
        return false;
    }

    public static String mUpdateFile;

    public static void updateCanbox(String f, Context c, String canType) {
        if (mCarUtil != null) {
            if (canType == null) {
                canType = mCanboxType;
            }
            if (canType == null) {
                Toast.makeText(c, "no canbox manufacturer update fail!", Toast.LENGTH_LONG).show();
                return;
            }
            mUpdateFile = f;
            MMLog.d(TAG, canType + " start UpdateCanbox:" + mUpdateFile);
            if (canType.contains("haozheng")) {
                mCarUtil.mCanbox = new UpdateLuZheng();
            } else if (canType.contains("hiworld")) {
                mCarUtil.mCanbox = new UpdateHiWorld();
                if (canType.endsWith("0")) {
                    ((UpdateHiWorld) mCarUtil.mCanbox).mType = 1;
                }
            } else if (canType.contains("raise")) {
                mCarUtil.mCanbox = new UpdateRaise();
            } else {
                mCarUtil.mCanbox = new UpdateSimple();
            }
            mCarUtil.mCanbox.setContext(c);
            mCarUtil.mCanbox.startConnect();
        }
    }

    private static boolean mIsShowAC = false;

    public static boolean isShowAC() {
        return mIsShowAC;
    }

    private static boolean mIsShowEQ = false;

    public static boolean isShowEQ() {
        return mIsShowEQ;
    }

    public static boolean isFocusSync3Reverse() {
        if (getCanboxType() != null && (getCanboxType().equals(MachineConfig.VALUE_CANBOX_FORD_SIMPLE) || getCanboxType().equals(MachineConfig.VALUE_CANBOX_FORD_EXPLORER_SIMPLE))) {
            return getCarType() == 3;
        }
        return false;
    }

    public static int getCanboxPhoneSource() {
        if (getCanboxType() != null && (getCanboxType().equals(MachineConfig.VALUE_CANBOX_FORD_SIMPLE) || getCanboxType().equals(MachineConfig.VALUE_CANBOX_FORD_EXPLORER_SIMPLE))) {

        }

        return MyCmd.SOURCE_AUX;
    }
}
