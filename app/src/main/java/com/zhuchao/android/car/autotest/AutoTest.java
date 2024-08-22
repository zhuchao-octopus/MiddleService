package com.zhuchao.android.car.autotest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.common.utils.BroadcastUtil;
import com.common.utils.Kernel;
import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.common.utils.UtilSystem;
import com.common.utils.UtilSystem.StorageInfo;

import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.manager.McuManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class AutoTest {
    private final static String TAG = "AutoTest";

    @SuppressLint("StaticFieldLeak")
    public static AutoTest mThis;

    private Context mContext;
    private View mView;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private TextView mTVLog;

    private McuTest mMcuTest;

    private McuManager mMcuManager;

    private final static int NODE_TIMEOUT1 = 1000;
    private final static int NODE_TIMEOUT2 = 5000;

    private int mRadioCheckIndex;
    private final static int RDS_INDEX = 4;
    private final static int AM_INDEX = 5;
    private final static int[] RADIO_FREQS = new int[]{9810, 9850, 10650, 9050, 8800, 630, 999, 1440};
    private final TestNode[] mTestNode = {

            new TestNode(R.string.test_swc2, TestNode.MCU_TEST_SWC2, NODE_TIMEOUT1, R.id.result_swc2), new TestNode(R.string.test_swc, TestNode.MCU_TEST_SWC, NODE_TIMEOUT1, R.id.result_swc),

            new TestNode(R.string.test_acc, TestNode.MCU_TEST_ACC, NODE_TIMEOUT1, R.id.result_acc),

            new TestNode(R.string.test_reverse_signal, TestNode.MCU_TEST_REVERSE, NODE_TIMEOUT1, R.id.result_reverse_signal),

            new TestNode(R.string.test_illumination, TestNode.MCU_TEST_ILL, NODE_TIMEOUT1, R.id.result_ill),

            new TestNode(R.string.test_brake, TestNode.MCU_TEST_BRAKE, NODE_TIMEOUT1, R.id.result_brake),

            new TestNode(R.string.test_fcamera_power, TestNode.MCU_TEST_FCAMERA, NODE_TIMEOUT1, R.id.result_fcamera_power),

            new TestNode(R.string.test_ant, TestNode.MCU_TEST_ANT, NODE_TIMEOUT1, R.id.result_ant),

            new TestNode(R.string.test_rear_video, TestNode.MCU_TEST_REAR_VIDEO, 3000, R.id.result_rear_video),
            ////			new TestNode(R.string.button_text_bt, MyCmd.SOURCE_BT,
            ////					NODE_TIMEOUT1, R.id.result_bt),
            //
            new TestNode(R.string.test_f_camera, MyCmd.SOURCE_FRONT_CAMERA, 3000, R.id.result_f_camera),

            //			new TestNode(R.string.button_text_aux, MyCmd.SOURCE_AUX, 3000,
            //					R.id.result_auxin),

            new TestNode(R.string.test_b_camera, MyCmd.SOURCE_REVERSE, 3000, R.id.result_reverse), new TestNode(R.string.button_text_dvd, MyCmd.SOURCE_DVD, 3000, R.id.result_dvd),
            new TestNode(R.string.button_text_radio, MyCmd.SOURCE_RADIO, 18000, R.id.result_radio),
            //			new TestNode(R.string.test_arm, MyCmd.SOURCE_MX51, NODE_TIMEOUT2,
            //					R.id.result_arm_audio),
            //
            new TestNode(R.string.test_usb, MyCmd.SOURCE_FOR_TEST_USB, 12000, R.id.result_usb), new TestNode(R.string.test_sd, MyCmd.SOURCE_FOR_TEST_SD, 12000, R.id.result_sd),

            new TestNode(R.string.test_gps, MyCmd.SOURCE_FOR_TEST_GPS, 60000, R.id.result_gps),

            //			new TestNode(R.string.test_wifi, MyCmd.SOURCE_FOR_TEST_WIFI, 60000,
            //					R.id.result_wifi),

    };

    public AutoTest() {
        mThis = this;
    }

    public void init(Context context) {
        mContext = context;
        mMcuTest = new McuTest();
        mMcuManager = McuManager.getInstance();
        if (mView == null) {

            mView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.main_test, null);

            mLayoutParams = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.RGBA_8888);

            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            initClick();
            Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);
            mWindowManager.addView(mView, mLayoutParams);

            mTVLog = mView.findViewById(R.id.auto_test_log);
            mTVLog.setMovementMethod(ScrollingMovementMethod.getInstance());

            mView.findViewById(R.id.tr_progress).setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= 26) {
                GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SYSTEMUI_STATUS_BAR_GONE);
            }
        }
        // doStart();
    }

    private boolean mPause = false;

    private void quit() {
        doStop();
        mPause = true;

        if (mLocationManager != null) {
            // mLocationManager.removeGpsStatusListener(listener);
            //
            // mLocationManager
            // .removeUpdates((LocationListener) mLocationListener);
            // mLocationManager = null;
        }
        if (mView != null) {
            mWindowManager.removeView(mView);
            mView = null;
        }
        Kernel.doKeyEvent(Kernel.KEY_HOMEPAGE);

        if (Build.VERSION.SDK_INT >= 26) {
            GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SYSTEMUI_STATUS_BAR_VISIBLE);
        }
    }

    private static final int[] BUTTON_ON_CLICK = new int[]{R.id.start, R.id.stop, R.id.quit, R.id.set, R.id.log, R.id.testing};

    private void initClick() {
        for (int i : BUTTON_ON_CLICK) {
            View v = mView.findViewById(i);
            if (v != null) {
                v.setOnClickListener(mViewListener);
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void showPage(int id) {
        if (id == R.id.log) {
            mView.findViewById(R.id.layout_log).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.layout_set).setVisibility(View.GONE);
            mView.findViewById(R.id.layout_testing).setVisibility(View.GONE);
        } else if (id == R.id.testing) {
            mView.findViewById(R.id.layout_log).setVisibility(View.GONE);
            mView.findViewById(R.id.layout_set).setVisibility(View.GONE);
            mView.findViewById(R.id.layout_testing).setVisibility(View.VISIBLE);
        } else if (id == R.id.set) {
            mView.findViewById(R.id.layout_log).setVisibility(View.GONE);
            mView.findViewById(R.id.layout_set).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.layout_testing).setVisibility(View.GONE);
        }
    }

    private final View.OnClickListener mViewListener = new View.OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.start) {
                doStart();
            } else if (id == R.id.stop) {
                doStop();
            } else if (id == R.id.quit) {// mMcuTest.sendCmd(0x3, 0x2);
                quit();
            } else if (id == R.id.log || id == R.id.testing || id == R.id.set) {
                showPage(v.getId());
            }
        }
    };

    private final static int MSG_STAT_TEST = 0;
    private final static int MSG_TESTING = 1;
    private final static int MSG_TEST_END1 = 2;
    private final static int MSG_TEST_END_ALL = 3;
    private final static int MSG_TEST_TIMEOUT = 4;
    private final static int MSG_TEST_CHECK_WIFI = 5;

    private final static int MSG_TEST_CHECK_VOLUME = 7;
    private final static int MSG_TEST_CHECK_VIDEO = 9;

    private final static int MSG_TEST_TIME_LEFT = 8;
    private final static int MSG_ALL_TIMEOUT = 14;

    private final static int MSG_TEST_DVD = 15;
    private final static int MSG_TEST_RADIO = 16;
    private final static int TIME_ALL_TIMEOUT = 120000;

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            TestNode tn;
            switch (msg.what) {
                case MSG_TEST_TIME_LEFT:
                    updateTestTime();
                    break;
                case MSG_TEST_CHECK_VIDEO:
                    if (msg.obj != null) {
                        tn = (TestNode) msg.obj;
                        int r = isSignal();

                        Log.d("aaa", tn.mStatus + ":" + r);
                        testResult(tn, r);
                    }
                    break;
                case MSG_STAT_TEST:
                    tn = (TestNode) msg.obj;
                    doTest(tn);
                    break;

                case MSG_TEST_TIMEOUT:
                    tn = (TestNode) msg.obj;
                    testResult(tn, TestNode.STATUS_TIMEOUT);
                    if (MyCmd.SOURCE_FOR_TEST_WIFI == tn.mSource) {
                        mHandler.removeMessages(MSG_TEST_CHECK_WIFI);
                    }
                    break;
                case MSG_ALL_TIMEOUT:
                    Log.d(TAG, "MSG_ALL_TIMEOUT");
                    checkAllTestFinish(true);
                    break;
                case MSG_TEST_CHECK_WIFI:
                    checkWIFI();
                    break;
                case MSG_TEST_CHECK_VOLUME:
                    mMcuTest.sendCmd(0xa, 0x2);
                    break;
                case MSG_TEST_DVD:
                    Util.setFileValue("/sys/class/misc/ak-dvd/device/data", "0x0212");
                    break;
                case MSG_TEST_RADIO:
                    String ret = null;
                    if (mFMStress != 1) {
                        ret = "Fail";
                    }

                    tn = findNodeBySource(MyCmd.SOURCE_RADIO);
                    if (mTestNodeAudio != null && mTestNodeAudio.mSource == tn.mSource) {

                        int result_string;
                        String s;
                        if (mRadioCheckIndex < AM_INDEX) {
                            s = String.format("%d.%02d ", RADIO_FREQS[mRadioCheckIndex] / 100, RADIO_FREQS[mRadioCheckIndex] % 100);
                        } else {
                            s = RADIO_FREQS[mRadioCheckIndex] + " ";
                        }

                        if (mRadioCheckIndex == AM_INDEX || mRadioCheckIndex == RDS_INDEX) {
                            s = "\r\n " + s;
                        }
                        boolean continuTest = false;
                        if (mRadioCheckIndex == 0) {
                            mTestRadioResult = "";
                        }
                        mTestRadioResult += " " + s;
                        continuTest = testRadio();

                        if (ret == null) {
                            result_string = R.string.successed;
                            tn.mStatus = TestNode.STATUS_SUCESS;
                            // testRadio();
                            mTestRadioResult += mContext.getString(result_string);

                            View v = mView.findViewById(R.id.result_radio);
                            if (v instanceof TextView) {
                                TextView new_name = (TextView) v;

                                new_name.setText(mTestRadioResult);
                                if (mRadioCheckIndex == 0) {
                                    new_name.setTextColor(0xffffffff);
                                }
                            }

                            // if (!continuTest) {
                            // startTestNeedSource();
                            // }

                        } else {
                            result_string = R.string.fail;
                            tn.mStatus = TestNode.STATUS_FAIL;

                            mTestRadioResult += mContext.getString(result_string);

                            View v = mView.findViewById(R.id.result_radio);
                            if (v instanceof TextView) {
                                TextView new_name = (TextView) v;

                                new_name.setText(mTestRadioResult);
                                new_name.setTextColor(0xffff0000);
                            }
                        }

                        if (!continuTest) {
                            mTestNodeAudio = null;

                            Kernel.doKeyEvent(Kernel.KEY_BACK);
                            startTestNeedSource();
                        }
                    }
                    break;
            }
        }
    };

    private void checkAllTestFinish(boolean timeout) {

        int i = -1;
        for (i = 0; i < mTestNode.length; ++i) {
            if (mTestNode[i].mStatus == TestNode.STATUS_NORMAL) {
                Log.d("allen", mTestNode.length + ":" + i + ":" + mTestNode[i].mStatus);
                break;
            }
        }
        if (i >= mTestNode.length) { // ok
            timeout = false;

            ((TextView) mView.findViewById(R.id.test_status)).setText(R.string.finish);
            mHandler.removeMessages(MSG_ALL_TIMEOUT);

            doStop();

            showPage(R.id.testing);
        } else {
            if (timeout) {

                ((TextView) mView.findViewById(R.id.test_status)).setText(R.string.time_out);

                doStop();
                showPage(R.id.testing);
            }
        }
    }

    private int getForTestDependNode() {
        int i = -1;
        for (i = 0; i < mTestNode.length; ++i) {
            if (mTestNode[i].mStatus == TestNode.STATUS_NORMAL && mTestNode[i].mType == TestNode.TYPE_NEED_SOURCE) {
                return i;
            }
        }
        return -1;
    }

    private void startTestIndepend() {
        int i = -1;
        int delay = 100;
        int j = 0;
        for (i = 0; i < mTestNode.length; ++i) {
            if (mTestNode[i].mType == TestNode.TYPE_INDEPEND) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STAT_TEST, mTestNode[i]), (long) delay * j);
                ++j;
            }
        }
    }

    // private void startTestNeedSource(TestNode tn) {
    // switch(tn.mSource){
    //
    // }
    // }

    private void startTestNeedSource() {
        int i = getForTestDependNode();
        Kernel.doKeyEvent(Kernel.KEY_BACK);
        if (i >= 0 && i < mTestNode.length) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STAT_TEST, mTestNode[i]), 20);
        } else {
            checkAllTestFinish(false);
        }

    }

    private TestNode findNodeBySource(int source) {
        int i = -1;
        for (i = 0; i < mTestNode.length; ++i) {
            if (mTestNode[i].mSource == source) {
                return mTestNode[i];
            }
        }
        return null;
    }

    private void prepearCheckAudio() {
        mHandler.removeMessages(MSG_TEST_CHECK_VOLUME);
        mHandler.sendEmptyMessageDelayed(MSG_TEST_CHECK_VOLUME, 300);
    }

    private boolean testRadio() {
        prepearCheckAudio();
        ++mRadioCheckIndex;
        if (mRadioCheckIndex < RADIO_FREQS.length) {

            if (mRadioCheckIndex < AM_INDEX) {
                mMcuManager.setRadio(1, 0, 0);
            } else {
                mMcuManager.setRadio(1, 0, 3);
            }


            mMcuManager.setRadio(0x2, ((RADIO_FREQS[mRadioCheckIndex] & 0xff00) >> 8), ((RADIO_FREQS[mRadioCheckIndex] & 0xff) >> 0));

            //			Log.d("test_radio", "test:" + RADIO_FREQS[mRadioCheckIndex]);

            //			prepearCheckAudio();
            mFMStress = -1;
            mHandler.sendEmptyMessageDelayed(MSG_TEST_RADIO, 2000);
            return true;
        }
        return false;
    }

    private void doTest(TestNode tn) {
        switch (tn.mSource) {
            case MyCmd.SOURCE_BT:
                // UtilSystem.doRunActivity(mContext, AppConfig.PACKAGE_CAR_UI,
                // "com.android.car.btmusic.BTMusicActivity");
                Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
                i.setPackage("com.android.car.bt");
                mContext.sendBroadcast(i);

                break;
            case MyCmd.SOURCE_RADIO:
                // UtilSystem.doRunActivity(mContext, AppConfig.PACKAGE_CAR_UI,
                // "com.android.car.radio.RadioActivity");
                mTestNodeAudio = tn;
                mMcuManager.setSource(MyCmd.SOURCE_RADIO);

                mRadioCheckIndex = -1;
                testRadio();
                break;
            case MyCmd.SOURCE_AUX:
                mTestNodeAudio = tn;
                tn.mResultData = 0;

                setCameraSource(MyCmd.CAMERA_SOURCE_AUX);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TEST_CHECK_VIDEO, tn), 300);
                // UtilSystem.doRunActivity(mContext, AppConfig.PACKAGE_CAR_UI,
                // "com.android.car.auxplayer.AUXPlayer");
                break;
            case MyCmd.SOURCE_DVD:
                // UtilSystem.doRunActivity(mContext, AppConfig.PACKAGE_CAR_UI,
                // "com.android.car.dvd.DVDPlayer");
                mMcuManager.setDvd(1, 1);
                Util.setFileValue("/sys/class/misc/ak-dvd/device/data", "0x0212");

                mHandler.sendEmptyMessageDelayed(MSG_TEST_DVD, 1000);

                break;
            case MyCmd.SOURCE_MX51:
                mTestNodeAudio = tn;
                testArmAudio();

                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TEST_TIMEOUT, tn), tn.mTimeout);
                break;
            case MyCmd.SOURCE_FRONT_CAMERA: {
                // Intent it = new Intent(Intent.ACTION_VIEW);
                // it.setClassName("com.car.ui",
                // "com.android.car.frontcamera.FrontCameraActivity");
                // // it.putExtra("camera", 1);
                // it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                // | Intent.FLAG_ACTIVITY_NEW_TASK);
                // mContext.startActivity(it);

                setCameraSource(4);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TEST_CHECK_VIDEO, tn), 300);
            }
            break;
            case MyCmd.SOURCE_REVERSE: {
                // Intent it = new Intent(Intent.ACTION_VIEW);
                // it.setClassName("com.car.ui",
                // "com.android.car.frontcamera.FrontCameraActivity");
                // it.putExtra("camera", 1);
                // it.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                // | Intent.FLAG_ACTIVITY_NEW_TASK);
                // mContext.startActivity(it);
                setCameraSource(1);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TEST_CHECK_VIDEO, tn), 300);
            }
            break;
            default:
                if (tn.mSource > TestNode.MCU_TEST_SOURCE) {
                    int cmd = tn.mSource - TestNode.MCU_TEST_SOURCE;
                    int param = 1;
                    if (tn.mSource == TestNode.MCU_TEST_ANT || tn.mSource == TestNode.MCU_TEST_FCAMERA || tn.mSource == TestNode.MCU_TEST_REAR_VIDEO) {
                        param = 2;
                        mMcuTest.sendCmd(cmd, param);
                    } else if (tn.mSource == TestNode.MCU_TEST_ACC) {
                        param = 0;
                        mMcuTest.sendCmd(cmd, param);
                    } else if (tn.mSource == TestNode.MCU_TEST_SWC) {
                        mCurSwcNode = tn;
                        mMcuTest.sendCmd(0x8, 0, 1);
                        mMcuManager.setSwcKeyStudy(0);
                    } else if (tn.mSource == TestNode.MCU_TEST_SWC2) {
                        mCurSwcNode = tn;
                        mMcuTest.sendCmd(0x8, 1, 1);
                        mMcuManager.setSwcKeyStudy(0);
                    } else {
                        mMcuTest.sendCmd(cmd, param);
                    }

                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TEST_TIMEOUT, tn), tn.mTimeout);
                }
                break;
        }

        switch (tn.mSource) {
            case MyCmd.SOURCE_BT:
            case MyCmd.SOURCE_RADIO:
            case MyCmd.SOURCE_REVERSE:
                // case MyCmd.SOURCE_DVD:
            case MyCmd.SOURCE_AUX:
            case MyCmd.SOURCE_FRONT_CAMERA:
                setLogText(mContext.getResources().getString(tn.mName) + " " + mContext.getResources().getString(R.string.testing));
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TEST_TIMEOUT, tn), tn.mTimeout);
                prepearCheckAudio();

                break;

            case MyCmd.SOURCE_DVD:
                setLogText(mContext.getResources().getString(tn.mName) + " " + mContext.getResources().getString(R.string.testing));
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TEST_TIMEOUT, tn), tn.mTimeout);
                break;
            case MyCmd.SOURCE_FOR_TEST_GPS:
                startTestGPS(tn);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TEST_TIMEOUT, tn), tn.mTimeout);
                break;
            case MyCmd.SOURCE_FOR_TEST_WIFI:
                startTestWIFI(tn);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TEST_TIMEOUT, tn), tn.mTimeout);
                break;
            case MyCmd.SOURCE_FOR_TEST_USB:
                startTestUSB(tn);
                break;
            case MyCmd.SOURCE_FOR_TEST_SD:
                if (mTestUSBIndex != -1) {
                    mTestSDIndex = -2;
                } else {
                    startTestSD(tn);
                }
                break;
        }
    }

    private void setLogText(String text) {
        CharSequence s = mTVLog.getText();
        mTVLog.setText(s + "\n" + text);
    }

    private LocationManager mLocationManager;
    MyLocationListener mLocationListener;

    private void startTestGPS(TestNode tn) {
        tn.mStatus = 0;
        mGPSNode = tn;

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // mLocationManager.requestLocationUpdates(
                // LocationManager.GPS_PROVIDER, 0, 0,
                // (LocationListener) mLocationListener);
                mLocationListener = new MyLocationListener();
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                mLocationManager.addGpsStatusListener(listener);

                setLogText(mContext.getResources().getString(tn.mName) + " " + mContext.getResources().getString(R.string.testing));

            }
        }
        setLogText(mContext.getResources().getString(tn.mName) + " " + mContext.getResources().getString(R.string.testing));
    }

    private List<StorageInfo> mStorage;
    private int mTestUSBIndex = -1;
    private int mTestSDIndex = -1;

    private String getUSBPath(int index) {
        //		if (mStorage == null) {
        mStorage = UtilSystem.listAllStorage(mContext);
        //		}

        String ret = null;
        if (mStorage.size() > 0) {
            int j = 0;
            for (int i = 0; i < mStorage.size(); ++i) {
                if (mStorage.get(i).mType == StorageInfo.TYPE_USB) {
                    if (j == index) {
                        return mStorage.get(i).mPath;
                    }
                    ++j;
                }
            }
        }
        return ret;
    }

    private String getSDPath(int index) {
        //		if (mStorage == null) {
        mStorage = UtilSystem.listAllStorage(mContext);
        //		}

        String ret = null;
        if (mStorage.size() > 0) {
            int j = 0;
            for (int i = 0; i < mStorage.size(); ++i) {
                if (mStorage.get(i).mType == StorageInfo.TYPE_SD) {
                    if (j == index) {
                        return mStorage.get(i).mPath;
                    }
                    ++j;
                }
            }
        }
        return ret;
    }

    private void startTestUSB(TestNode tn) {
        ++mTestUSBIndex;

        String path = getUSBPath(mTestUSBIndex);
        if (path != null) {

            if (mTestUSBIndex == (TEST_STORAGE_USB_MAX - 1)) {
                tn.mStatus = TestNode.STATUS_END;
            }

            setLogText(mContext.getResources().getString(tn.mName) + (mTestUSBIndex + 1) + " " + mContext.getResources().getString(R.string.testing));

            int ret = testStorage(path);

            String result_string;
            if (ret == TEST_STORAGE_OK) {
                result_string = mContext.getResources().getString(R.string.successed);
            } else if (ret == TEST_STORAGE_READ) {
                result_string = mContext.getResources().getString(R.string.test_read) + " " + mContext.getResources().getString(R.string.successed) + " " + mContext.getResources().getString(R.string.test_write) + " " + mContext.getResources().getString(R.string.fail);
            } else if (ret == TEST_STORAGE_WRITE) {
                result_string = mContext.getResources().getString(R.string.test_write) + " " + mContext.getResources().getString(R.string.successed) + " " + mContext.getResources().getString(R.string.test_read) + " " + mContext.getResources().getString(R.string.fail);
            } else {
                result_string = mContext.getResources().getString(R.string.fail);
            }

            setLogText(mContext.getResources().getString(tn.mName) + (mTestUSBIndex + 1) + " " + result_string);

            setResultText(tn, "--" + mContext.getResources().getString(tn.mName) + (mTestUSBIndex + 1) + " " + result_string + "--");

            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STAT_TEST, tn), 1500);

        } else {

            do {
                mTestUSBIndex++;
                setLogText(mContext.getResources().getString(tn.mName) + (mTestUSBIndex) + " " + mContext.getResources().getString(R.string.fail));

                setResultText(tn, "--" + mContext.getResources().getString(tn.mName) + (mTestUSBIndex) + " " + mContext.getResources().getString(R.string.fail) + "--");

            } while (mTestUSBIndex < TEST_STORAGE_USB_MAX);

            if (mTestUSBIndex == (TEST_STORAGE_USB_MAX)) {
                tn.mStatus = TestNode.STATUS_END;
            }

            mTestUSBIndex = -1;

            if (mTestSDIndex == -2) {
                for (TestNode tnsd : mTestNode) {
                    if (tnsd.mSource == MyCmd.SOURCE_FOR_TEST_SD) {
                        mTestSDIndex = -1;
                        startTestSD(tnsd);
                        break;
                    }
                }
            }
        }

    }

    private String mSD1Name;

    private void startTestSD(TestNode tn) {
        ++mTestSDIndex;

        String path = getSDPath(mTestSDIndex);
        if (path != null) {

            if (mTestSDIndex == (TEST_STORAGE_SD_MAX - 1)) {
                tn.mStatus = TestNode.STATUS_END;
            }

            setLogText(mContext.getResources().getString(tn.mName) + (mTestSDIndex + 1) + " " + mContext.getResources().getString(R.string.testing));

            int ret = testStorage(path);

            String result_string;
            if (ret == TEST_STORAGE_OK) {
                result_string = mContext.getResources().getString(R.string.successed);
            } else if (ret == TEST_STORAGE_READ) {
                result_string = mContext.getResources().getString(R.string.test_read) + " " + mContext.getResources().getString(R.string.successed) + " " + mContext.getResources().getString(R.string.test_write) + " " + mContext.getResources().getString(R.string.fail);
            } else if (ret == TEST_STORAGE_WRITE) {
                result_string = mContext.getResources().getString(R.string.test_write) + " " + mContext.getResources().getString(R.string.successed) + " " + mContext.getResources().getString(R.string.test_read) + " " + mContext.getResources().getString(R.string.fail);
            } else {
                result_string = mContext.getResources().getString(R.string.fail);
            }

            String name;
            if (path.contains("GPSCard")) {
                name = "GPS卡";
            } else {
                name = "SD卡";
            }
            mSD1Name = name;
            setLogText(name + " " + result_string);

            setResultText(tn, "--" + name + " " + result_string + "--");

            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STAT_TEST, tn), 1500);

        } else {

            while (mTestSDIndex < TEST_STORAGE_SD_MAX) {
                String name;
                if (mSD1Name == null) {
                    if (mTestSDIndex == 0) {
                        name = "GPS卡";
                    } else {
                        name = "SD卡";
                    }
                } else {
                    if (mSD1Name.equals("SD卡")) {
                        name = "GPS卡";
                    } else {
                        name = "SD卡";
                    }
                }

                mTestSDIndex++;
                setLogText(mContext.getResources().getString(tn.mName) + (mTestSDIndex) + " " + mContext.getResources().getString(R.string.fail));


                setResultText(tn, "--" + name + " " + mContext.getResources().getString(R.string.fail) + "--");

            }
            //while (mTestSDIndex < TEST_STORAGE_SD_MAX);

            if (mTestSDIndex == (TEST_STORAGE_SD_MAX)) {
                tn.mStatus = TestNode.STATUS_END;
            }

            mTestSDIndex = -1;
            mSD1Name = null;

        }

    }

    private final static int TEST_STORAGE_USB_MAX = 4;
    private final static int TEST_STORAGE_SD_MAX = 2;

    private final static int TEST_STORAGE_WRITE = 0x1;
    private final static int TEST_STORAGE_READ = 0x2;
    private final static int TEST_STORAGE_OK = TEST_STORAGE_READ | TEST_STORAGE_WRITE;

    private int testStorage(String path) {
        int ret = 0;

        File f = new File(path);
        File[] ff = f.listFiles();
        if (ff != null) {
            ret |= TEST_STORAGE_READ;
        }

        if (!path.endsWith("/")) {
            path += "/";
        }
        String file = path + "ak_test_storage";

        try {

            FileOutputStream is = new FileOutputStream(file);
            DataOutputStream dis = new DataOutputStream(is);

            dis.write(file.getBytes());
            dis.close();
            is.close();
            ret |= TEST_STORAGE_WRITE;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    private final static int[] OTHER_TEXT_ID = new int[]{R.id.result_auxin_audio,};

    private void clear() {
        mTVLog.setText("");
        int i = -1;
        for (i = 0; i < mTestNode.length; ++i) {
            mTestNode[i].mStatus = 0;

            View v = mView.findViewById(mTestNode[i].mResultId);
            if (v instanceof TextView) {
                TextView new_name = (TextView) v;
                new_name.setText("");
            }

        }
        for (i = 0; i < OTHER_TEXT_ID.length; ++i) {

            View v = mView.findViewById(OTHER_TEXT_ID[i]);
            if (v instanceof TextView) {
                TextView new_name = (TextView) v;
                new_name.setText("");
            }

        }

    }

    private void clearMsg() {
        mHandler.removeMessages(MSG_STAT_TEST);
        mHandler.removeMessages(MSG_TEST_TIMEOUT);
        mHandler.removeMessages(MSG_TEST_CHECK_WIFI);
        mHandler.removeMessages(MSG_TEST_CHECK_VOLUME);
        mHandler.removeMessages(MSG_ALL_TIMEOUT);
        mHandler.removeMessages(MSG_TEST_TIME_LEFT);
        // mHandler.removeMessages(MSG_STAT_TEST);
    }

    private void doStop() {

        //		mMcuManager.setVoulume(10);
        clearMsg();
        stopArmAudio();
        mMcuManager.setSource(MyCmd.SOURCE_AV_OFF);
        mView.findViewById(R.id.tr_progress).setVisibility(View.GONE);

        mMcuTest.sendCmd(0x1, 0x0);
        mMcuManager.setAutoTest(0x1b, 0);
        GlobalDefinition.mIsTesting = false;
        BroadcastUtil.sendByCarService(mContext, MyCmd.Cmd.AUTO_TEST_START, 0);

    }

    private void doStart() {

        //		mMcuManager.setVoulume(30);
        mMcuManager.setAutoTest(0x1b, 1);
        mMcuTest.sendCmd(0x1, 0x1);
        GlobalDefinition.mIsTesting = true;
        // ((TextView) mView.findViewById(R.id.test_status))
        // .setText(R.string.testing);
        mView.findViewById(R.id.tr_progress).setVisibility(View.VISIBLE);
        clear();
        BroadcastUtil.sendByCarService(mContext, MyCmd.Cmd.AUTO_TEST_START, 1);

        startTestNeedSource();
        startTestIndepend();

        mHandler.sendEmptyMessageDelayed(MSG_ALL_TIMEOUT, TIME_ALL_TIMEOUT);

        mTestTime = TIME_ALL_TIMEOUT;
        updateTestTime();

    }

    private int mTestTime = 0;

    private void updateTestTime() {
        ((TextView) mView.findViewById(R.id.test_status)).setText(mContext.getString(R.string.testing) + "  " + mTestTime / 1000 + " s");
        mTestTime -= 1000;
        mHandler.removeMessages(MSG_TEST_TIME_LEFT);
        if (mTestTime > 0) {
            mHandler.sendEmptyMessageDelayed(MSG_TEST_TIME_LEFT, 1000);
        }
    }

    public void testResult(int source, int result) {
        if (mPause) {
            return;
        }
        TestNode node = findNodeBySource(source);
        Log.d(TAG, source + ":" + node);
        testResult(node, result);
    }

    private void setResultText(TestNode node, String resultText) {
        View v = mView.findViewById(node.mResultId);
        if (v instanceof TextView) {
            TextView new_name = (TextView) v;
            if (node.mResultId == R.id.result_usb || node.mResultId == R.id.result_sd) {
                resultText = new_name.getText() + " " + resultText;
            }
            new_name.setText(resultText);
        }
    }

    private void setResultText(TestNode node, int resultid, int color) {
        View v = mView.findViewById(node.mResultId);
        if (v instanceof TextView) {
            TextView new_name = (TextView) v;

            String resultText = mContext.getResources().getString(resultid);


            new_name.setText(resultText);
            new_name.setTextColor(color);
        }
    }

    private void setResultText(TestNode node, int resultid) {
        int color = 0xff000000;
        if (resultid == R.string.fail) {
            color = 0xffff0000;
        }
        setResultText(node, resultid, color);
    }

    private void setResultText(int id, String resultText) {
        View v = mView.findViewById(id);
        if (v instanceof TextView) {
            TextView new_name = (TextView) v;
            new_name.setText(resultText);
        }
    }

    private void testResult(TestNode node, int result) {
        if (node != null && node.mStatus == TestNode.STATUS_NORMAL) {

            int source = node.mSource;
            int result_string = 0;
            switch (source) {

                case MyCmd.SOURCE_RADIO:
                case MyCmd.SOURCE_BT:
                    if (result == 0) {
                        result_string = R.string.successed;
                        node.mStatus = TestNode.STATUS_SUCESS;
                    } else {
                        result_string = R.string.fail;
                        node.mStatus = TestNode.STATUS_FAIL;
                    }
                    setLogText(mContext.getResources().getString(node.mName) + " " + mContext.getResources().getString(result_string));

                    setResultText(node, result_string);

                    startTestNeedSource();

                    break;

                case MyCmd.SOURCE_AUX:
                    if (result == 0) {
                        result_string = R.string.successed;
                        setLogText(mContext.getResources().getString(node.mName) + " " + mContext.getResources().getString(result_string));

                        setResultText(node, result_string);
                        if (node.mResultData == 2) {
                            node.mStatus = TestNode.STATUS_SUCESS;
                            Kernel.doKeyEvent(Kernel.KEY_BACK);
                            startTestNeedSource();

                        } else {
                            node.mResultData = 1;
                        }

                    } else if (result == TestNode.STATUS_TIMEOUT) {
                        result_string = R.string.fail;
                        node.mStatus = TestNode.STATUS_FAIL;

                        setLogText(mContext.getResources().getString(node.mName) + "time out" + mContext.getResources().getString(result_string));

                        setResultText(node, result_string);

                        Kernel.doKeyEvent(Kernel.KEY_BACK);

                        View v = mView.findViewById(R.id.result_auxin_audio);
                        if (v instanceof TextView) {
                            TextView new_name = (TextView) v;

                            new_name.setText(result_string);
                            new_name.setTextColor(0xffff0000);
                        }

                        startTestNeedSource();
                    } else {
                        if ((System.currentTimeMillis() - node.mTimeStart) > node.mTimeout) {
                            result_string = R.string.fail;
                            // node.mStatus = TestNode.STATUS_FAIL;

                            setLogText(mContext.getResources().getString(node.mName) + " " + mContext.getResources().getString(result_string));

                            setResultText(node, result_string);

                            if (node.mResultData == 2) {
                                node.mStatus = TestNode.STATUS_FAIL;
                                Kernel.doKeyEvent(Kernel.KEY_BACK);
                                startTestNeedSource();

                            } else {
                                node.mResultData = 1;
                            }
                        }
                    }

                    break;
                case MyCmd.SOURCE_FRONT_CAMERA:
                case MyCmd.SOURCE_REVERSE:
                case MyCmd.SOURCE_DVD:
                    if (node.mSource == MyCmd.SOURCE_DVD) {
                        if (mMcuManager.getDVDStatus() != 1) {
                            result = TestNode.STATUS_TIMEOUT;
                        }
                    }

                    if (result == 0) {
                        result_string = R.string.successed;
                        node.mStatus = TestNode.STATUS_SUCESS;
                    } else if (result == TestNode.STATUS_TIMEOUT) {
                        result_string = R.string.fail;
                        node.mStatus = TestNode.STATUS_FAIL;
                    } else {
                        if ((System.currentTimeMillis() - node.mTimeStart) > node.mTimeout) {
                            result_string = R.string.fail;
                            node.mStatus = TestNode.STATUS_FAIL;
                        }
                    }

                    if (node.mStatus != TestNode.STATUS_NORMAL) {
                        setLogText(mContext.getResources().getString(node.mName) + " " + mContext.getResources().getString(result_string));

                        setResultText(node, result_string);

                        Kernel.doKeyEvent(Kernel.KEY_BACK);
                        startTestNeedSource();
                    }
                    mHandler.removeMessages(MSG_TEST_DVD);

                    break;
                case MyCmd.SOURCE_FOR_TEST_GPS:
                    if (result == TestNode.STATUS_SUCESS) {
                        result_string = R.string.successed;
                        node.mStatus = TestNode.STATUS_SUCESS;
                    } else {
                        result_string = R.string.fail;
                        node.mStatus = TestNode.STATUS_FAIL;
                    }

                    setLogText(mContext.getResources().getString(node.mName) + " " + mContext.getResources().getString(result_string));

                    setResultText(node, result_string);
                    break;
                case MyCmd.SOURCE_FOR_TEST_WIFI:
                    if (result == TestNode.STATUS_SUCESS) {
                        result_string = R.string.successed;
                        node.mStatus = TestNode.STATUS_SUCESS;
                    } else {
                        result_string = R.string.fail;
                        node.mStatus = TestNode.STATUS_FAIL;
                    }

                    setLogText(mContext.getResources().getString(node.mName) + " " + mContext.getResources().getString(result_string));

                    setResultText(node, result_string);
                    break;
                case MyCmd.SOURCE_MX51:
                    if (result == TestNode.STATUS_SUCESS) {
                        result_string = R.string.successed;
                        node.mStatus = TestNode.STATUS_SUCESS;
                    } else {
                        result_string = R.string.fail;
                        node.mStatus = TestNode.STATUS_FAIL;
                    }

                    setLogText(mContext.getResources().getString(node.mName) + " " + mContext.getResources().getString(result_string));

                    setResultText(node, result_string);

                    stopArmAudio();
                    startTestNeedSource();
                    break;
                default:
                    if (source > TestNode.MCU_TEST_SOURCE) {
                        if (result == TestNode.STATUS_SUCESS) {
                            result_string = R.string.successed;
                            node.mStatus = TestNode.STATUS_SUCESS;
                        } else {
                            result_string = R.string.fail;
                            node.mStatus = TestNode.STATUS_FAIL;
                        }

                        if (source == TestNode.MCU_TEST_SWC) {
                            mMcuManager.setSwcKeyStudy(2);
                        }

                        setLogText(mContext.getResources().getString(node.mName) + " " + mContext.getResources().getString(result_string));

                        setResultText(node, result_string);

                        startTestNeedSource();
                    }
                    break;

            }
        }
        checkAllTestFinish(false);
    }

    private TestNode mGPSNode;

    // 状态监听
    public class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            Log.d("gps_test", "onLocationChanged!!!!!!!!" + location.getLatitude() + ":" + location.getLongitude());
            if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                if (mGPSNode.mStatus == TestNode.STATUS_NORMAL) {
                    setLogText(mContext.getResources().getString(R.string.test_gps) + mContext.getResources().getString(R.string.successed));
                    // mLocationManager.removeGpsStatusListener(listener);
                    // mLocationManager
                    // .removeUpdates((LocationListener) mLocationListener);
                    testResult(mGPSNode, TestNode.STATUS_SUCESS);
                }
            }
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            Log.e(TAG, mGPSNode.mStatus + ":onGpsStatusChanged:" + event);
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:

                    Log.e("gps_test", "GPS_EVENT_FIRST_FIX");

                    if (mGPSNode.mStatus == TestNode.STATUS_NORMAL) {
                        setLogText(mContext.getResources().getString(R.string.test_gps) + mContext.getResources().getString(R.string.successed));
                        // mLocationManager.removeGpsStatusListener(listener);
                        // mLocationManager
                        // .removeUpdates((LocationListener) mLocationListener);
                        testResult(mGPSNode, TestNode.STATUS_SUCESS);
                    }
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    // 获取当前状态
                    // GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    // // 获取卫星颗数的默认最大值
                    // int maxSatellites = gpsStatus.getMaxSatellites();
                    // // 创建一个迭代器保存所有卫星
                    // Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
                    // .iterator();
                    // int count = 0;
                    // // for (int i = 0; i < 12; ++i) {
                    // // mGpsSatellite[i] = null;
                    // // }
                    // String log = "";
                    // while (iters.hasNext() && count <= maxSatellites) { // get in
                    // // used
                    // // number
                    // GpsSatellite s = iters.next();
                    // if (s.usedInFix()) {
                    // if (count >= 12)
                    // break;
                    // // mGpsSatellite[count] = s;
                    //
                    // Log.e(TAG, "1count:" + count + ":" + s.getSnr() + ":"
                    // + s.getPrn());
                    // count++;
                    // log += "(" + s.getSnr() + "," + s.getPrn() + ")";
                    // }
                    // }
                    // if (log.length() > 1) {
                    // setLogText(log);
                    // }
                    // Log.e(TAG, "GPS_EVENT_SATELLITE_STATUS:"+maxSatellites);
                    // Log.e(TAG, "GPS_EVENT_SATELLITE_STATUS");

                    //				if (mGPSNode.mStatus == TestNode.STATUS_NORMAL) {
                    //					setLogText(mContext.getResources().getString(
                    //							R.string.test_gps)
                    //							+ mContext.getResources().getString(
                    //									R.string.successed));
                    //					testResult(mGPSNode, TestNode.STATUS_SUCESS);
                    //
                    //					// mLocationManager.removeGpsStatusListener(listener);
                    //					//
                    //					// mLocationManager
                    //					// .removeUpdates((LocationListener) mLocationListener);
                    //				}

                    break;
            }
        }

    };

    private TestNode mNodeWifi;
    private WifiManager mWifiManager;

    private void startTestWIFI(TestNode tn) {
        try {
            mNodeWifi = tn;
            mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            mWifiManager.setWifiEnabled(true);

            setLogText(mContext.getResources().getString(tn.mName) + " " + mContext.getResources().getString(R.string.testing));

            mHandler.sendEmptyMessageDelayed(MSG_TEST_CHECK_WIFI, 3000);

            mWifiManager.startScan();
        } catch (Exception e) {
            Log.d(TAG, "startTestWIFI" + e);
        }
    }

    private void checkWIFI() {
        try {

            List<ScanResult> resultList;
            resultList = mWifiManager.getScanResults();
            Log.d(TAG, "checkWIFI:" + resultList.size());

            if (resultList.size() > 0) {
                int i = 0;
                if (resultList != null) {
                    for (ScanResult scan : resultList) {
                        Log.d(TAG, i + ":" + scan.SSID);
                    }
                }
                testResult(mNodeWifi, TestNode.STATUS_SUCESS);
                // setResultText(R.id.result_wifi,
                // mContext.getResources().getString(R.string.successed));
                mWifiManager.setWifiEnabled(false);
            } else {
                mHandler.sendEmptyMessageDelayed(MSG_TEST_CHECK_WIFI, 3000);

                // setResultText(R.id.result_wifi,
                // mContext.getResources().getString(R.string.fail));
            }
        } catch (Exception e) {
            Log.d(TAG, "checkWIFI:" + e);
        }
    }

    private TestNode mCurSwcNode;
    private String mTestRadioResult;

    private int mFMStress = -1;

    public void parseCanboxData(byte[] data, int len) {
        TestNode tn;
        int cmd = data[0];
        if (len > 3) {
            cmd = data[3] + TestNode.MCU_TEST_SOURCE;
        }
        switch (cmd) {
            case RADIO_RETURN:
                Log.d("test_radio", "RADIO_RETURN:" + data[1]);
                if (mFMStress != data[1]) {
                    mFMStress = data[1];
                }
                break;
            case SWC_RETURN:
                mMcuManager.setSwcKeyStudy(2);
                testResult(mCurSwcNode.mSource, TestNode.STATUS_SUCESS);

                // tn = findNodeBySource(TestNode.MCU_TEST_SWC);
                // if(tn.mResultData == 0){
                // tn.mResultData = 1;
                // mMcuTest.sendCmd(cmd, 1, 1);
                // } else if(tn.mResultData == 1){
                // mMcuManager.setSwcKeyStudy(2);
                // testResult(TestNode.MCU_TEST_SWC, TestNode.STATUS_SUCESS);
                // }

                break;
            case ILL_RETURN:
                testResult(TestNode.MCU_TEST_ILL, TestNode.STATUS_SUCESS);
                break;
            case ACC_RETURN:
                testResult(TestNode.MCU_TEST_ACC, TestNode.STATUS_SUCESS);
                break;
            case REVERSE_RETURN:
                testResult(TestNode.MCU_TEST_REVERSE, TestNode.STATUS_SUCESS);
                mMcuTest.sendCmd(0x3, 0x0);
                break;
            case BRAKE_RETURN:
                testResult(TestNode.MCU_TEST_BRAKE, TestNode.STATUS_SUCESS);
                break;

            case TestNode.MCU_TEST_REAR_VIDEO:
                tn = findNodeBySource(cmd);
                if (tn != null) {
                    int result = TestNode.STATUS_FAIL;
                    if (data[5] == 1 && data[6] == 1) {
                        result = TestNode.STATUS_SUCESS;
                    }

                    testResult(tn, result);
                }
                break;
            case TestNode.MCU_TEST_ANT:
            case TestNode.MCU_TEST_FCAMERA:

                tn = findNodeBySource(cmd);
                if (tn != null) {
                    int result = TestNode.STATUS_FAIL;
                    if (data[5] == 1) {
                        result = TestNode.STATUS_SUCESS;
                    }

                    testResult(tn, result);
                }
                break;
            case TestNode.MCU_TEST_VOLUME:
                if (mTestNodeAudio != null) {
                    // int result = (data[5]<<0)|
                    String ret = isSpeakerOK(data);
                    switch (mTestNodeAudio.mSource) {
                        case MyCmd.SOURCE_RADIO:

                            tn = findNodeBySource(MyCmd.SOURCE_RADIO);
                            if (mTestNodeAudio != null && mTestNodeAudio.mSource == tn.mSource) {

                                int result_string;
                                String s;
                                if (mRadioCheckIndex < AM_INDEX) {
                                    s = String.format("%d.%02d ", RADIO_FREQS[mRadioCheckIndex] / 100, RADIO_FREQS[mRadioCheckIndex] % 100);
                                } else {
                                    s = String.valueOf(RADIO_FREQS[mRadioCheckIndex]);
                                }

                                if (mRadioCheckIndex == AM_INDEX || mRadioCheckIndex == RDS_INDEX) {
                                    s = "\r\n" + s;
                                }
                                //						String s = String.format("%d.%02d ",
                                //								RADIO_FREQS[mRadioCheckIndex] / 100,
                                //								RADIO_FREQS[mRadioCheckIndex] % 100);
                                boolean continuTest = false;
                                if (mRadioCheckIndex == 0) {
                                    mTestRadioResult = "";
                                }
                                mTestRadioResult += " " + s;
                                continuTest = testRadio();

                                if (ret == null) {
                                    result_string = R.string.successed;
                                    tn.mStatus = TestNode.STATUS_SUCESS;
                                    // testRadio();
                                    mTestRadioResult += mContext.getString(result_string);

                                    View v = mView.findViewById(R.id.result_radio);
                                    if (v instanceof TextView) {
                                        TextView new_name = (TextView) v;

                                        new_name.setText(mTestRadioResult);
                                        if (mRadioCheckIndex == 0) {
                                            new_name.setTextColor(0xffffffff);
                                        }
                                    }

                                    // if (!continuTest) {
                                    // startTestNeedSource();
                                    // }

                                } else {
                                    result_string = R.string.fail;
                                    tn.mStatus = TestNode.STATUS_FAIL;

                                    mTestRadioResult += mContext.getString(result_string);

                                    View v = mView.findViewById(R.id.result_radio);
                                    if (v instanceof TextView) {
                                        TextView new_name = (TextView) v;

                                        new_name.setText(mTestRadioResult);
                                        new_name.setTextColor(0xffff0000);
                                    }
                                }

                                if (!continuTest) {
                                    mTestNodeAudio = null;

                                    Kernel.doKeyEvent(Kernel.KEY_BACK);
                                    startTestNeedSource();
                                }

                            }
                            break;
                        case MyCmd.SOURCE_AUX:
                            tn = findNodeBySource(MyCmd.SOURCE_AUX);
                            if (mTestNodeAudio != null && mTestNodeAudio.mSource == tn.mSource) {

                                mTestNodeAudio = null;
                                int result_string;

                                if (ret == null) {
                                    result_string = R.string.successed;

                                    View v = mView.findViewById(R.id.result_auxin_audio);
                                    if (v instanceof TextView) {
                                        TextView new_name = (TextView) v;

                                        new_name.setText(mContext.getString(result_string));
                                        new_name.setTextColor(0xffffffff);
                                    }

                                    if (tn.mResultData == 1) {
                                        tn.mStatus = TestNode.STATUS_SUCESS;
                                        Kernel.doKeyEvent(Kernel.KEY_BACK);
                                        startTestNeedSource();
                                    } else {
                                        tn.mResultData = 2;
                                    }

                                } else {
                                    result_string = R.string.fail;

                                    View v = mView.findViewById(R.id.result_auxin_audio);
                                    if (v instanceof TextView) {
                                        TextView new_name = (TextView) v;

                                        new_name.setText(ret);
                                        new_name.setTextColor(0xffff0000);
                                    }

                                    if (tn.mResultData == 1) {

                                        tn.mStatus = TestNode.STATUS_FAIL;
                                        Kernel.doKeyEvent(Kernel.KEY_BACK);
                                        startTestNeedSource();
                                    } else {
                                        tn.mResultData = 2;
                                    }

                                }
                                mTestNodeAudio = null;
                            }
                            break;
                        case MyCmd.SOURCE_MX51:
                            tn = findNodeBySource(MyCmd.SOURCE_MX51);
                            if (mTestNodeAudio != null && mTestNodeAudio.mSource == tn.mSource) {

                                mTestNodeAudio = null;
                                int result_string;
                                if (ret == null) {
                                    result_string = R.string.successed;
                                    tn.mStatus = TestNode.STATUS_SUCESS;

                                    setResultText(tn, result_string);

                                } else {
                                    result_string = R.string.fail;
                                    tn.mStatus = TestNode.STATUS_FAIL;

                                    // setResultText(tn, ret);

                                    View v = mView.findViewById(tn.mResultId);
                                    if (v instanceof TextView) {
                                        TextView new_name = (TextView) v;

                                        new_name.setText(ret);
                                        new_name.setTextColor(0xffff0000);
                                    }

                                }
                                stopArmAudio();

                                startTestNeedSource();
                                mTestNodeAudio = null;
                            }
                            break;
                    }
                }
                break;
        }
    }

    public final static int OTHER_CMD_RETURN = 0x60;

    public final static int REVERSE_RETURN = 0x70;
    public final static int BRAKE_RETURN = 0x71;
    public final static int ACC_RETURN = 0x72;
    public final static int ILL_RETURN = 0x73;

    public final static int SWC_RETURN = 0x74;


    public final static int RADIO_RETURN = 0x75;

    public static void parseTestData(byte[] data, int len) {
        if (mThis != null) {
            try {
                mThis.parseCanboxData(data, len);
            } catch (Exception e) {

            }
        }
    }

    private String isSpeakerOK(byte[] data) {
        String ret = null;

        int ok = 0;
        int start = 5;

        for (int i = start; i < data.length && i < (8 + start); ++i) {
            if (data[i] != ok) {
                if (ret == null) {
                    ret = "";
                }
                ret += (i - start + 1);
            }
        }

        if (ret != null) {
            ret += mContext.getResources().getString(R.string.fail);
        }

        return ret;

    }

    private final int mAudioOutputSource = -1;

    MediaPlayer mMediaPlayer;
    private TestNode mTestNodeAudio;

    private void testArmAudio() {
        stopArmAudio();

        mMediaPlayer = new MediaPlayer();
        try {

            // Log.d(TAG, ">>open:"+path);
            mMcuManager.setMcuSource(MyCmd.SOURCE_MX51);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource("/system/etc/test/1k.mp3");
            mMediaPlayer.prepare();
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();

            // mHandler.sendEmptyMessageDelayed(MSG_TEST_CHECK_VOLUME, 300);
            prepearCheckAudio();
        } catch (Exception e) {
            Log.d(TAG, String.valueOf(e));
        }

    }

    private void stopArmAudio() {
        Log.d("abc", "stopArmAudio");
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (Exception e) {
                Log.d(TAG, String.valueOf(e));
            }
        }
    }

    private int isSignal() {
        String source;

        // if(Util.isPX5()){
        if (Util.isRKSystem()) {
            source = readLine("/sys/class/ak/source/cvbs_status");
        } else {
            source = readLine("/sys/class/misc/mst701/device/lock");
        }
        // } else {
        // source = readLine("/sys/class/misc/mst701/device/lock");
        // }

        if (source != null && source.equals("1")) {
            return 0;
        }

        return 1;

    }

    private String readLine(String path) {

        File file = new File(path);

        String source = null;
        if (file.exists()) {
            BufferedReader buf;

            try {
                FileReader fr = new FileReader(file);
                buf = new BufferedReader(fr);
                source = buf.readLine();
                buf.close();
                fr.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return source;
    }

    public void setCameraSource(int source) {

        if (Util.isRKSystem()) {
            Util.setFileValue("/sys/class/ak/source/cam_ch", source);
        } else {
            Util.setFileValue("/sys/class/misc/mst701/device/source", source);
        }
    }

}
